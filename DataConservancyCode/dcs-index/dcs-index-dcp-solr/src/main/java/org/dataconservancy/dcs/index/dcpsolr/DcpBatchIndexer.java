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
package org.dataconservancy.dcs.index.dcpsolr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EntityField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EventField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.MetadataField;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.SolrName;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.xml.sax.InputSource;

/**
 * Add packages of related entities to the index and remove individual entities
 * from the index.
 * <p>
 * Each entity is persisted to a Solr document. The entity can be recreated from
 * the document. Collections of related entities are indexed and committed to
 * Solr together. An entity with the same id as an already indexed entity will
 * replace that indexed entity.
 * <p>
 * Consistency guarantee is that each document is only written once. An entity
 * is either fully indexed or not fully indexed. But an indexed entity may
 * reference an entity that has not been indexed. This may happen if an indexed
 * entity is removed and not replaced or when batch jobs are run simultaneously.
 * An exception is the addition of a DcsEvent or DcsFile in a IS_METADATA_FOR
 * relation, which has an already indexed target. That target entity will be
 * modified in the index. An additional exception is adding a manifestation which
 * references a File already in the index. 
 * <p>
 * Multiple BatchIndexers may be running simultaneously. A commit to the Solr
 * server (on close) by one BatchIndexer will cause documents added by all
 * threads to be made readable.
 */

public class DcpBatchIndexer implements BatchIndexer<Dcp> {
    // Controls numbers of documents added at a time.
    private static final int MAX_ADD_SIZE = 1000;

    private final SolrService solr;
    private final ArchiveStore store;

    public DcpBatchIndexer(SolrService solr, ArchiveStore store) {
        this.solr = solr;
        this.store = store;
    }

    /**
     * Add a package of related entities to the index.
     * <p>
     * For each entity in the package, the package must contain all related
     * entities that are not already in the index. If an entity in the package
     * is already in the index (shares the same id as an indexed entity), it
     * will replaced the indexed entity.
     * <p>
     * For each entity A in the package, for each entity B in the archive such
     * that B is the parent (Collection or Deliverable Unit) of A, B is the
     * target of the Event A, B is the metadata target of A, B is the
     * Deliverable Unit of Manifestation A, B is a Manifestation associated with
     * File A, or B has a metadata reference to File A, B must either be in the
     * package or already have been indexed.
     * <p>
     * Replacing an already indexed entity complicates this requirement. If A
     * already exists in the index and A has changed one of the above
     * relationships, then each entity B or C in the archive such that B is
     * related to A or A to C (B RELATED A and A RELATED C), B must either be in
     * the package or already have been indexed and C must be in the package.
     * <p>
     * An additional requirement is that each entity added in a batch indexing
     * session must not be related to an entity in another package in that
     * session or a session occurring simultaneously.
     */
    public void add(Dcp dcp) throws IndexServiceException {
        List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

        // entity id -> list of events targeting entity
        Map<String, List<DcsEvent>> event_target_map = new HashMap<String, List<DcsEvent>>();
        updateEventTargets(event_target_map, dcp.getEvents());

        // entity id -> list of DcsFile metadata ids targeting entity
        Map<String, List<String>> md_target_map = new HashMap<String, List<String>>();
        updateMetadataTargets(md_target_map, dcp.getManifestations());

        // entity id -> list of ancestors
        Map<String, List<String>> ancestrymap = new HashMap<String, List<String>>();

        try {
            // Ancestor lists are computed from root to leaves

            updateCollectionAncestry(ancestrymap, dcp.getCollections());
            add(docs, ancestrymap, event_target_map, md_target_map,
                    dcp.getCollections());

            updateDeliverableUnitAncestry(ancestrymap,
                    dcp.getDeliverableUnits());
            add(docs, ancestrymap, event_target_map, md_target_map,
                    dcp.getDeliverableUnits());

            updateManifestationAncestry(ancestrymap, dcp.getManifestations());
            add(docs, ancestrymap, event_target_map, md_target_map,
                    dcp.getManifestations());

            updateFileAncestry(ancestrymap, dcp.getFiles());
            add(docs, ancestrymap, event_target_map, md_target_map,
                    dcp.getFiles());

            add(docs, ancestrymap, event_target_map, md_target_map,
                    dcp.getEvents());

            modifyIndexedEventTargets(docs, event_target_map);
            modifyIndexedMetadataFileTargets(docs, md_target_map);
            modifyIndexedFileTargets(docs, dcp.getManifestations(), ancestrymap);
            
            solr.server().add(docs);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        }
    }

    // Handle the addition of a manifestion pointing to an existing File.
    // The File document must be rewritten to update ancestry.
    private void modifyIndexedFileTargets(List<SolrInputDocument> docs,
            Collection<DcsManifestation> manifestations,
            Map<String, List<String>> ancestrymap) throws IOException, SolrServerException {
        
        for (DcsManifestation man: manifestations) {
            for (DcsManifestationFile mf: man.getManifestationFiles()) {
                DcsEntityReference target_ref = mf.getRef();

                if (target_ref == null) {
                    continue;
                }

                String target_id = target_ref.getRef();

                if (target_id == null) {
                    continue;
                }
                
                SolrDocument target_doc = solr.lookupSolrDocument(target_id);
                
                if (target_doc == null) {
                    continue;
                }
                
                // Manifestation is pointing to an existing File
                
                List<String> ancestors = new ArrayList<String>();
                ancestors.add(man.getId());
                
                List<String> man_ancestors = ancestrymap.get(man.getId());
                
                if (man_ancestors != null) {
                    ancestors.addAll(man_ancestors);
                }
                
                addAncestorsFromSolr(target_doc, ancestors);
                
                // Change the ancestry field in the existing document
                
                SolrInputDocument new_target_doc = dup(target_doc);
                
                new_target_doc.removeField(EntityField.ANCESTRY.solrName());
                new_target_doc.addField(EntityField.ANCESTRY.solrName(), ancestors);
                
                docs.add(new_target_doc);
            }
        }
    }

    // Handle the case of a new event having an already indexed target
    private void modifyIndexedEventTargets(List<SolrInputDocument> docs,
            Map<String, List<DcsEvent>> targetmap) throws IOException,
            SolrServerException {
        for (String target_id : targetmap.keySet()) {
            SolrDocument target_doc = solr.lookupSolrDocument(target_id);

            if (target_doc != null) {
                SolrInputDocument new_target_doc = dup(target_doc);

                for (DcsEvent event : targetmap.get(target_id)) {
                    addEventToTarget(new_target_doc, event);
                }

                docs.add(new_target_doc);
            }
        }
    }

    // Handle the case of a metadata file having an already indexed target

    private void modifyIndexedMetadataFileTargets(List<SolrInputDocument> docs,
            Map<String, List<String>> targetmap) throws IOException,
            SolrServerException {
        for (String target_id : targetmap.keySet()) {
            SolrDocument target_doc = solr.lookupSolrDocument(target_id);

            if (target_doc != null) {
                SolrInputDocument new_target_doc = dup(target_doc);

                for (String file_id : targetmap.get(target_id)) {
                    addXmlFileToTarget(new_target_doc, file_id);
                }

                docs.add(new_target_doc);
            }
        }
    }

    private void updateEventTargets(Map<String, List<DcsEvent>> targetmap,
            Collection<DcsEvent> events) {
        for (DcsEvent event : events) {
            for (DcsEntityReference target : event.getTargets()) {
                String target_id = null;

                if (target != null) {
                    target_id = target.getRef();
                }

                if (target_id == null) {
                    break;
                }

                List<DcsEvent> list = targetmap.get(target_id);

                if (list == null) {
                    list = new ArrayList<DcsEvent>();
                    targetmap.put(target_id, list);
                }

                list.add(event);
            }
        }
    }

    private void updateMetadataTargets(Map<String, List<String>> targetmap,
            Collection<DcsManifestation> mans) {
        for (DcsManifestation man : mans) {
            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                DcsFileRef file_ref = mf.getRef();

                if (file_ref == null) {
                    continue;
                }

                String file_id = file_ref.getRef();

                if (file_id == null) {
                    continue;
                }

                for (DcsRelation rel : mf.getRelSet()) {
                    if (DcsRelationship.IS_METADATA_FOR != DcsRelationship
                            .fromString(rel.getRelUri())) {
                        continue;
                    }

                    DcsEntityReference target_ref = rel.getRef();

                    if (target_ref == null) {
                        continue;
                    }

                    String target_id = target_ref.getRef();

                    if (target_id == null) {
                        continue;
                    }

                    List<String> list = targetmap.get(target_id);

                    if (list == null) {
                        list = new ArrayList<String>();
                        targetmap.put(target_id, list);
                    }

                    list.add(file_id);
                }
            }
        }
    }

    private void add(List<SolrInputDocument> docs,
            Map<String, List<String>> ancestrymap,
            Map<String, List<DcsEvent>> event_target_map,
            Map<String, List<String>> md_target_map,
            Collection<? extends DcsEntity> entities) throws IOException,
            SolrServerException {
        for (DcsEntity entity : entities) {
            SolrInputDocument doc = DcsSolrMapper.toSolr(entity, store);

            // Add ancestry field

            List<String> ancestors = ancestrymap.get(entity.getId());

            if (ancestors != null && ancestors.size() > 0) {
                doc.addField(EntityField.ANCESTRY.solrName(), ancestors);
            }

            // Add fields from events targeting this entity.
            List<DcsEvent> events = event_target_map.get(entity.getId());

            if (events != null) {
                for (DcsEvent event : events) {
                    addEventToTarget(doc, event);
                }
            }

            // Add file contents from metadata files targeting this entity.
            List<String> file_ids = md_target_map.get(entity.getId());

            if (file_ids != null) {
                for (String file_id : file_ids) {
                    addXmlFileToTarget(doc, file_id);
                }
            }

            docs.add(doc);

            if (docs.size() > MAX_ADD_SIZE) {
                solr.server().add(docs);
                docs.clear();
            }
        }
    }

    private void addXmlFileToTarget(SolrInputDocument doc, String file_id)
            throws IOException {
        InputStream is = null;

        try {
            is = store.getContent(file_id);
            DcsSolrMapper.addXml(doc, "ext_",
                    MetadataField.SEARCH_TEXT.solrName(), new InputSource(is));
        } catch (Exception e) {
            throw new IOException("Unable to index external file " + file_id
                    + ": " + e.getMessage(), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // ignore
            }
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

    private void updateCollectionAncestry(
            Map<String, List<String>> ancestrymap, Collection<DcsCollection> col_set)
            throws IOException, SolrServerException {

        // TODO can use archive store!!
        // id -> collection
        Map<String, DcsCollection> col_map = new HashMap<String, DcsCollection>();

        for (DcsCollection col : col_set) {
            col_map.put(col.getId(), col);
        }

        for (DcsCollection col : col_set) {
            List<String> ancestors = new ArrayList<String>(2);
            ancestrymap.put(col.getId(), ancestors);

            for (;;) {
                DcsCollectionRef ref = col.getParent();

                String parent_id = null;

                if (ref != null) {
                    parent_id = ref.getRef();
                }

                if (parent_id == null) {
                    break;
                }

                if (add_if_not_present(ancestors, parent_id)) {
                    DcsCollection parent = col_map.get(parent_id);

                    if (parent == null) {
                        addAncestorsFromSolr(parent_id, ancestors);
                        break;
                    } else {
                        col = parent;
                    }
                } else {
                    break;
                }
            }

            for (DcsEntityReference ref : col.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, col.getId());
            }
        }
    }

    private void updateDeliverableUnitAncestry(
            Map<String, List<String>> ancestrymap, Collection<DcsDeliverableUnit> set)
            throws IOException, SolrServerException {
        // TODO can use archive store!!
        // id -> du
        Map<String, DcsDeliverableUnit> du_map = new HashMap<String, DcsDeliverableUnit>();

        for (DcsDeliverableUnit du : set) {
            du_map.put(du.getId(), du);
        }

        for (DcsDeliverableUnit du : set) {
            List<String> ancestors = new ArrayList<String>(2);
            ancestrymap.put(du.getId(), ancestors);

            LinkedList<DcsDeliverableUnit> todo = new LinkedList<DcsDeliverableUnit>();
            todo.add(du);

            // Find all ancestors of dus in todo

            while (!todo.isEmpty()) {
                DcsDeliverableUnit check = todo.pop();

                // Add collections

                for (DcsCollectionRef col_ref : check.getCollections()) {
                    updateAncestry(ancestrymap, du.getId(), col_ref);
                }

                // Add parents

                for (DcsDeliverableUnitRef ref : check.getParents()) {
                    String parent_id = null;

                    if (ref != null) {
                        parent_id = ref.getRef();
                    }

                    DcsDeliverableUnit parent = du_map.get(parent_id);

                    if (add_if_not_present(ancestors, parent_id)) {
                        if (parent == null) {
                            addAncestorsFromSolr(parent_id, ancestors);
                        } else {
                            todo.add(parent);
                        }
                    }
                }
            }

            for (DcsEntityReference ref : du.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, du.getId());
            }
        }
    }

    private void updateManifestationAncestry(
            Map<String, List<String>> ancestrymap, Collection<DcsManifestation> set)
            throws IOException, SolrServerException {
        for (DcsManifestation man : set) {
            List<String> ancestors = new ArrayList<String>(2);
            ancestrymap.put(man.getId(), ancestors);

            updateAncestry(ancestrymap, man.getId(), man.getDeliverableUnit());

            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                updateAncestry(ancestrymap, mf.getRef(), man.getId());
            }

            for (DcsMetadataRef ref : man.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, man.getId());
            }
        }
    }

    private void updateFileAncestry(Map<String, List<String>> ancestrymap,
                                    Collection<DcsFile> set) throws IOException, SolrServerException {
        for (DcsFile file : set) {
            for (DcsMetadataRef ref : file.getMetadataRef()) {
                updateAncestry(ancestrymap, ref, file.getId());
            }
        }
    }

    private void updateAncestry(Map<String, List<String>> ancestrymap,
            DcsEntityReference entity_ref, String ancestor_id)
            throws IOException, SolrServerException {
        if (entity_ref == null) {
            return;
        }

        updateAncestry(ancestrymap, entity_ref.getRef(), ancestor_id);
    }

    private void updateAncestry(Map<String, List<String>> ancestrymap,
            String entity_id, DcsEntityReference ancestor_ref)
            throws IOException, SolrServerException {
        if (ancestor_ref == null) {
            return;
        }

        updateAncestry(ancestrymap, entity_id, ancestor_ref.getRef());
    }

    private void updateAncestry(Map<String, List<String>> ancestrymap,
            String entity_id, String ancestor_id) throws IOException,
            SolrServerException {
        if (entity_id == null || ancestor_id == null) {
            return;
        }

        List<String> ancestors = ancestrymap.get(entity_id);

        if (ancestors == null) {
            ancestors = new ArrayList<String>(2);
            ancestrymap.put(entity_id, ancestors);
        }

        ancestors.add(ancestor_id);

        List<String> ancestor_ancestors = ancestrymap.get(ancestor_id);

        if (ancestor_ancestors == null) {
            addAncestorsFromSolr(ancestor_id, ancestors);
        } else {
            ancestors.addAll(ancestor_ancestors);
        }
    }

    /**
     * Remove an entity that is existing in the index.
     */
    public void remove(String id) throws IndexServiceException {
        try {
            solr.server().deleteById(id);
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        }
    }

    public void close() throws IndexServiceException {
        try {
            solr.server().commit();
        } catch (SolrServerException e) {
            throw new IndexServiceException(e);
        } catch (IOException e) {
            throw new IndexServiceException(e);
        }
    }

    private boolean add_if_not_present(List<String> list, String item) {
        if (list.contains(item)) {
            return false;
        }

        list.add(item);

        return true;
    }

    private void addAncestorsFromSolr(String id, List<String> result)
            throws IOException, SolrServerException {
        SolrDocument doc = solr.lookupSolrDocument(id);

        if (doc != null) {
            addAncestorsFromSolr(doc, result);
        }
    }

    private void addAncestorsFromSolr(SolrDocument doc, List<String> result)
            throws IOException, SolrServerException {
        Collection<Object> ancestors = doc.getFieldValues(EntityField.ANCESTRY
                .solrName());

        if (ancestors != null) {
            for (Object ancestor : ancestors) {
                result.add((String) ancestor);
            }
        }
    }

    private static void add(SolrInputDocument doc, SolrName field, Object value) {
        if (value != null) {
            doc.addField(field.solrName(), value);
        }
    }

    private void addEventToTarget(SolrInputDocument doc, DcsEvent event)
            throws IOException, SolrServerException {

        add(doc, EventField.DATE, event.getDate());
        add(doc, EventField.DETAIL, event.getDetail());
        add(doc, EventField.OUTCOME, event.getOutcome());
        add(doc, EventField.TYPE, event.getEventType());

        // Always remove the dynamic event_date_* field to make sure there is at most one
        
        String event_date_field =  EventField.DYNAMIC_DATE_TYPE_PREFIX.solrName()
                + event.getEventType();
        doc.removeField(event_date_field);
        doc.addField(event_date_field, event.getDate());
    }
}
