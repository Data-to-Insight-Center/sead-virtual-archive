/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.access.impl.solr;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.access.api.IndexWriter;
import org.dataconservancy.dcs.access.impl.solr.DcsSolrField.EntityField;
import org.dataconservancy.dcs.access.impl.solr.DcsSolrField.EventField;
import org.dataconservancy.dcs.access.impl.solr.DcsSolrField.ManifestationFileField;
import org.dataconservancy.dcs.access.impl.solr.DcsSolrField.SolrName;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

// TODO rollback on exception?
// TODO ability to resume after failure?
// TODO think about how could optimize by looping over all of one entity type at a time

/**
 * See {@link org.dataconservancy.dcs.index.dcpsolr}
 */

@Deprecated
public class DcsSolrIndexWriter
        implements IndexWriter {

    private static final int MAX_ADD_SIZE = 100;

    private static final int MAX_COMMIT_SIZE = 10000;

    private int commit_count;

    private final SolrServer server;

    private List<SolrInputDocument> docs;

    // TODO use file not memory
    private final List<String> added;

    private final DcsSolrSearcher searcher;

    private final ArchiveStore store;

    protected DcsSolrIndexWriter(SolrServer server, ArchiveStore store) {
        this.server = server;
        this.docs = new ArrayList<SolrInputDocument>();
        this.added = new ArrayList<String>();
        this.searcher = new DcsSolrSearcher(server);
        this.store = store;
    }

    public void add(DcsEntity entity) throws IOException {
        docs.add(DcsSolrMapper.toSolr(entity, store));

        if (docs.size() > MAX_ADD_SIZE) {
            add_docs();
        }
    }

    // TODO have to handle fields added by second pass

    public void remove(String id) throws IOException {
        try {
            // TODO test
            server.deleteById(id);
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    private void add_docs() throws IOException {
        try {
            if (docs.size() > 0) {
                server.add(docs);

                for (SolrInputDocument doc : docs) {
                    // TODO abstract out get id
                    added.add((String) doc.getFieldValue(EntityField.ID
                            .solrName()));
                }

                commit_count += docs.size();

                if (commit_count > MAX_COMMIT_SIZE) {
                    server.commit();
                    commit_count = 0;
                }

                docs.clear();
            }
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    public void close() throws IOException {
        add_docs();

        try {
            server.commit();
        } catch (SolrServerException e) {
            throw new IOException(e);
        }

        secondPass();
    }

    // In order to change a field in a document must rewrite the entire
    // document.
    // Ugly and slow.

    // TODO This doesn't handle the case of a new Manifestation which might
    // reference add ancestors
    // to existing entities...

    private void secondPass() throws IOException {
        try {
            for (String id : added) {
                SolrDocument doc =
                        DcsSolrMapper.lookupEntityDocument(server, id);
                DcsEntity entity = DcsSolrMapper.fromSolr(doc);

                secondPassIndexAncestors(doc, entity);
            }

            server.commit();

            for (String id : added) {
                SolrDocument doc =
                        DcsSolrMapper.lookupEntityDocument(server, id);
                DcsEntity entity = DcsSolrMapper.fromSolr(doc);

                if (entity instanceof DcsEvent) {
                    secondPassIndexEvent(doc, (DcsEvent) entity);
                }
            }
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    // TODO Awful. Could also do a lot better by ordering from top to bottom.

    private void findAncestors(String id, List<String> result)
            throws IOException, SolrServerException {
        findAncestors(DcsSolrMapper.lookupEntityDocument(server, id), result);
    }

    private void findAncestors(SolrDocument doc, List<String> result)
            throws IOException, SolrServerException {
        if (doc != null) {
            // If ancestors already stored, then we are done

            // TODO ancestors not available because we don't commit yet, cache
            // somewhere??

            Collection<Object> ancestors =
                    doc.getFieldValues(EntityField.ANCESTRY.solrName());

            if (ancestors == null || ancestors.size() == 0) {
                findAncestors(DcsSolrMapper.fromSolr(doc), result);
            } else {
                for (Object ancestor : ancestors) {
                    result.add((String) ancestor);
                }
            }
        }
    }

    private void findAncestors(DcsEntity entity, List<String> result)
            throws IOException, SolrServerException {

        if (entity instanceof DcsCollection) {
            DcsCollection col = (DcsCollection) entity;
            DcsCollectionRef ref = col.getParent();

            if (ref != null) {
                if (ref.getRef() != null) {
                    result.add(ref.getRef());
                    findAncestors(ref.getRef(), result);
                }
            }
        } else if (entity instanceof DcsDeliverableUnit) {
            DcsDeliverableUnit du = (DcsDeliverableUnit) entity;
            Set<DcsDeliverableUnitRef> set = du.getParents();

            if (set != null) {
                for (DcsDeliverableUnitRef ref : set) {
                    if (ref.getRef() != null) {
                        result.add(ref.getRef());
                        findAncestors(ref.getRef(), result);
                    }
                }
            }

            Set<DcsCollectionRef> colset = du.getCollections();

            if (colset != null) {
                for (DcsCollectionRef ref : colset) {
                    if (ref.getRef() != null) {
                        result.add(ref.getRef());
                        findAncestors(ref.getRef(), result);
                    }
                }
            }
        } else if (entity instanceof DcsFile) {
            DcsFile file = (DcsFile) entity;

            String query =
                    QueryUtil
                            .createLiteralQuery("OR",
                                                ManifestationFileField.FILE_REF
                                                        .solrName(),
                                                file.getId(),
                                                EntityField.METADATA_REF
                                                        .solrName(),
                                                file.getId());
            DcsSolrSearcher.ResultIterator iter = searcher.search(query, 10);

            for (QueryResponse resp; (resp = iter.next()) != null;) {
                for (SolrDocument doc : resp.getResults()) {
                    result.add((String) doc.getFieldValue(EntityField.ID
                            .solrName()));
                    findAncestors(doc, result);
                }
            }
        } else if (entity instanceof DcsEvent) {
        } else if (entity instanceof DcsManifestation) {
            DcsManifestation man = (DcsManifestation) entity;

            if (man.getDeliverableUnit() != null) {
                result.add(man.getDeliverableUnit());
                findAncestors(man.getDeliverableUnit(), result);
            }
        } else {
            throw new IllegalArgumentException("Unhandled entity type: "
                    + entity.getClass().getName());
        }
    }

    // Commit after calling.

    private void secondPassIndexAncestors(SolrDocument entitydoc,
                                          DcsEntity entity) throws IOException,
            SolrServerException {

        List<String> ancestors = new ArrayList<String>();
        findAncestors(entity, ancestors);

        if (ancestors.size() > 0) {
            SolrInputDocument newdoc = dup(entitydoc);
            newdoc.addField(EntityField.ANCESTRY.solrName(), ancestors);
            server.add(newdoc);
        }
    }

    // Must commit after every change in case of multiple events for same entity
    private void secondPassIndexEvent(SolrDocument entitydoc, DcsEvent event)
            throws IOException, SolrServerException {

        for (DcsEntityReference ref : event.getTargets()) {
            SolrDocument refdoc =
                    DcsSolrMapper.lookupEntityDocument(server, ref.getRef());

            if (refdoc == null) {
                continue;
            }

            SolrInputDocument newrefdoc = dup(refdoc);

            copy(entitydoc,
                 newrefdoc,
                 EventField.DATE,
                 EventField.DETAIL,
                 EventField.OUTCOME,
                 EventField.TYPE);

            newrefdoc.addField(EventField.DYNAMIC_DATE_TYPE_PREFIX.solrName()
                    + event.getEventType(), event.getDate());

            server.add(newrefdoc);
        }

        if (event.getTargets().size() > 0) {
            server.commit();
        }
    }

    private SolrInputDocument dup(SolrDocument doc) {
        SolrInputDocument newdoc = new SolrInputDocument();
        copy(doc, newdoc);
        return newdoc;
    }

    private void copy(SolrDocument from, SolrInputDocument to) {
        for (String name : from.getFieldNames()) {
            to.addField(name, from.getFieldValue(name));
        }
    }

    private void copy(SolrDocument from,
                      SolrInputDocument to,
                      SolrName... fields) {
        for (SolrName field : fields) {
            if (from.containsKey(field.solrName())) {
                // don't copy if field is null

                String isnull =
                        field.solrName() + DcsSolrMapper.IS_NULL_FIELD_SUFFIX;

                if (!from.containsKey(isnull)
                        || !(Boolean) from.getFieldValue(isnull)) {

                    to.addField(field.solrName(), from.getFieldValue(field
                            .solrName()));
                }
            }
        }
    }
}
