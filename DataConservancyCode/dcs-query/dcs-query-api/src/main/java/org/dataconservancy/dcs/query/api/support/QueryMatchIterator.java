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
package org.dataconservancy.dcs.query.api.support;

import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides an iterator over query search results.  Provided a {@link QueryService} and an initial
 * set of query results, the iterator will provide {@link QueryMatch} objects, re-executing the search
 * query as needed until the results are exhausted.
 *
 * @param <T> the domain object type
 */
public class QueryMatchIterator<T> implements Iterator<QueryMatch<T>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final QueryService<T> queryService;
    private final long totalResults;
    private final long step;

    private QueryResult<T> results;
    private long totalOffset;
    private long matchesOffset = 0;

    /**
     * Construct an iterator over the search results of the supplied <code>query</code>.  The query will
     * be invoked upon construction.
     *
     * @param queryService the query service
     * @param query the query string
     * @param params the query parameters
     * @throws IllegalStateException if there is an error invoking the initial query
     */
    public QueryMatchIterator(QueryService<T> queryService, String query, String... params) {
        this.queryService = queryService;
        try {
            this.results = queryService.query(query, 0, 100, params);
        } catch (QueryServiceException e) {
            throw new IllegalStateException("Could not execute initial query: " + e.getMessage(), e);
        }
        this.totalResults = results.getTotal();
        this.totalOffset = results.getOffset();
        this.step = results.getMatches().size();
    }

    /**
     * Construct an iterator over the supplied search results.  Subsequent search queries will be invoked as
     * required.
     *
     * @param queryService the query service
     * @param results the initial set of query results
     */
    public QueryMatchIterator(QueryService<T> queryService, QueryResult<T> results) {
        this.queryService = queryService;
        this.results = results;
        this.totalResults = results.getTotal();
        this.totalOffset = results.getOffset();
        this.step = results.getMatches().size();
    }

    @Override
    public boolean hasNext() {
        return totalOffset < totalResults;
    }

    /**
     * Returns the next match.  If there are no remaining entries, a {@link NoSuchElementException} is thrown.
     *
     * @return the registry entry
     */
    @Override
    public QueryMatch<T> next() {
        List<QueryMatch<T>> matches = results.getMatches();

        if (matches.isEmpty() || totalOffset == totalResults) {
            throw new NoSuchElementException();
        }

        if (matches.size() <= matchesOffset) {
            if (totalOffset < totalResults) {
                try {
                    results = queryService.query(results.getQueryString(), totalOffset, (int) step, results.getQueryParams());
                    matches = results.getMatches();
                    matchesOffset = 0;
                } catch (QueryServiceException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        QueryMatch<T> match = matches.get((int) matchesOffset);
        matchesOffset++;
        totalOffset++;

        return match;
    }

    /**
     * Unsupported by this iterator, always throws an {@link UnsupportedOperationException}
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Dumps the internal state of the iterator as a String.
     *
     * @return
     */
    @Override
    public String toString() {
        return "QueryMatchIterator{" +
                "log=" + log +
                ", queryService=" + queryService +
                ", totalResults=" + totalResults +
                ", step=" + step +
                ", results=" + results +
                ", totalOffset=" + totalOffset +
                ", matchesOffset=" + matchesOffset +
                '}';
    }
}
