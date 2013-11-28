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
package org.dataconservancy.dcs.query.dcpsolr;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.xml.sax.SAXException;

public class DcsDataModelQueryService implements LookupQueryService<DcsEntity> {
    private final static int MAX_MATCHES = 10000;

    private final SolrService solr;

    public DcsDataModelQueryService() throws QueryServiceException {
        try {
            this.solr = new SolrService();
        } catch (IOException e) {
            throw new QueryServiceException(e);
        } catch (ParserConfigurationException e) {
            throw new QueryServiceException(e);
        } catch (SAXException e) {
            throw new QueryServiceException(e);
        }
    }

    public DcsDataModelQueryService(SolrService solr) {
        this.solr = solr;
    }

    public QueryResult<DcsEntity> query(String query, long offset, int matches,
            String... params) throws QueryServiceException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if (offset > Integer.MAX_VALUE) {
            throw new QueryServiceException("offset too large");
        }

        if ((params.length & 1) > 0) {
            throw new IllegalArgumentException("parameter name without value");
        }

        if (matches <= 0 || matches > MAX_MATCHES) {
            matches = MAX_MATCHES;
        }

        try {
            QueryResponse resp = solr.search(query, (int) offset, matches,
                    params);
            SolrDocumentList docs = resp.getResults();
            QueryResult<DcsEntity> result = new QueryResult<DcsEntity>(offset,
                    docs.getNumFound(), query, params);

            for (SolrDocument doc : docs) {
                DcsEntity entity = solr.asEntity(doc);
                String context = null;

                if (resp.getHighlighting() != null) {
                    Map<String, List<String>> snippets = resp.getHighlighting()
                            .get(entity.getId());

                    // Add "field: snippet" to context

                    if (snippets != null) {
                        StringBuilder sb = new StringBuilder();

                        for (String field : snippets.keySet()) {
                            sb.append(field + ": '" + snippets.get(field)
                                    + "' ");
                        }

                        context = sb.toString();
                    }
                }

                result.getMatches().add(
                        new QueryMatch<DcsEntity>(entity, context));
            }

            return result;
        } catch (SolrException e) {
            throw new QueryServiceException(e);
        } catch (SolrServerException e) {
            throw new QueryServiceException(e);
        } catch (IOException e) {
            throw new QueryServiceException(e);
        }
    }

    public void shutdown() {
        solr.shutdown();
    }

    public long size() throws QueryServiceException {
        try {
            return solr.size();
        } catch (SolrServerException e) {
            throw new QueryServiceException(e);
        }
    }

    @Override
    public DcsEntity lookup(String id) throws QueryServiceException {
        return lookupEntity(id);
    }

    /**
     * @param id
     * @return Entity with the given identifier or null if the entity is not in
     *         the index.
     * @throws QueryServiceException
     */
    public DcsEntity lookupEntity(String id) throws QueryServiceException {
        try {
            return solr.lookupEntity(id);
        } catch (IOException e) {
            throw new QueryServiceException(e);
        } catch (SolrServerException e) {
            throw new QueryServiceException(e);
        }
    }

    /**
     * Return the date of the last event targeting this entity.
     * 
     * @param id
     *            entity id
     * @return ingest date or -1
     * @throws QueryServiceException
     */
    public long lookupEntityLastModified(String id)
            throws QueryServiceException {
        try {
            return solr.lookupEntityLastModified(id);
        } catch (IOException e) {
            throw new QueryServiceException(e);
        } catch (SolrServerException e) {
            throw new QueryServiceException(e);
        }
    }
}
