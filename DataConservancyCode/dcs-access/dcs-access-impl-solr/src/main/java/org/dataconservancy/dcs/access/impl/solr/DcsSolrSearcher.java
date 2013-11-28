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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
/**
 * See {@link org.dataconservancy.dcs.index.dcpsolr}
 */

@Deprecated
public class DcsSolrSearcher {

    private final SolrServer server;

    protected DcsSolrSearcher(SolrServer server) {
        this.server = server;
    }

    public QueryResponse search(String query,
                                int offset,
                                int maxmatches,
                                String... params) throws SolrServerException {
        SolrQuery q = new SolrQuery(query);

        q.setStart(offset);
        q.setRows(maxmatches);

        for (int i = 0; i < params.length;) {
            String name = params[i++];
            String val = params[i++];

            q.setParam(name, val);
        }

        return server.query(q);
    }

    public interface ResultIterator {

        QueryResponse next() throws SolrServerException;
    }

    /**
     * If there are > Integer.MAX_INTEGER results then all of the results will
     * not be retrieved.
     * 
     * @param query
     * @param maxmatches
     * @param params
     *        name,value pairs of Solr params
     * @return QueryResponse or null if no more results
     */
    public ResultIterator search(final String query,
                                 final int maxmatches,
                                 final String... params) {

        // When offset > Integer.MAX_VALUE, stop.
        return new ResultIterator() {

            int offset = 0;

            public QueryResponse next() throws SolrServerException {
                if (offset < 0) {
                    return null;
                }

                QueryResponse next = search(query, offset, maxmatches, params);

                if (next == null || next.getResults().size() == 0) {
                    return null;
                } else {
                    if (offset + next.getResults().getNumFound() > Integer.MAX_VALUE) {
                        offset = -1;
                    } else {
                        offset += next.getResults().getNumFound();
                    }
                }

                return next;
            }
        };
    }
}
