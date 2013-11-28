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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.access.api.IndexWriter;
import org.dataconservancy.dcs.access.api.Match;
import org.dataconservancy.dcs.access.api.SearchResult;
import org.dataconservancy.model.dcs.DcsEntity;

/**
 * See {@link org.dataconservancy.dcs.index.dcpsolr}
 */
@Deprecated
public class DcsSolrIndex {

    private static final int MAX_MATCHES = 100;

    private final SolrServer server;

    private final DcsSolrSearcher searcher;

    protected DcsSolrIndex(SolrServer server) {
        this.server = server;
        this.searcher = new DcsSolrSearcher(server);
    }

    public DcsEntity getEntity(String id) throws IOException {
        SolrDocument doc = DcsSolrMapper.lookupEntityDocument(server, id);

        if (doc == null) {
            return null;
        }

        return DcsSolrMapper.fromSolr(doc);
    }

    // TODO add structure for highlighting?

    public SearchResult search(String query,
                               int offset,
                               int maxmatches,
                               String... params) throws IOException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if ((params.length & 1) > 0) {
            throw new IllegalArgumentException("parameter name without value");
        }

        if (maxmatches <= 0 || maxmatches > MAX_MATCHES) {
            maxmatches = MAX_MATCHES;
        }

        try {
            QueryResponse resp =
                    searcher.search(query, offset, maxmatches, params);
            SolrDocumentList docs = resp.getResults();
            SearchResult result = new SearchResult(offset, docs.getNumFound());

            for (SolrDocument doc : docs) {
                DcsEntity entity = DcsSolrMapper.fromSolr(doc);
                String context = null;

                if (resp.getHighlighting() != null) {
                    Map<String, List<String>> snippets =
                            resp.getHighlighting().get(entity.getId());

                    // TODO context format?

                    if (snippets != null) {
                        StringBuilder sb = new StringBuilder();

                        for (String field : snippets.keySet()) {
                            sb.append(field + ": '" + snippets.get(field)
                                    + "' ");
                        }

                        context = sb.toString();
                    }
                }

                result.getMatches().add(new Match(entity, context));
            }

            return result;
        } catch (SolrException e) {
            throw new IOException(e);
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    public IndexWriter update(ArchiveStore store) {
        return new DcsSolrIndexWriter(server, store);
    }

    public void clear() throws IOException {
        try {
            server.deleteByQuery("*:*");
            server.commit();
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    public void optimize() throws IOException {
        try {
            server.optimize();
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    /**
     * @param id
     * @return ingest date or -1
     * @throws IOException
     */
    public long getEntityLastModified(String id) throws IOException {
        SolrDocument doc = DcsSolrMapper.lookupEntityDocument(server, id);

        if (doc == null) {
            return -1;
        }

        Date date =
                (Date) doc
                        .getFieldValue(DcsSolrField.EventField.DYNAMIC_DATE_TYPE_PREFIX
                                .solrName()
                                + "ingest");
        if (date == null) {
            return -1;
        }

        return date.getTime();
    }
}
