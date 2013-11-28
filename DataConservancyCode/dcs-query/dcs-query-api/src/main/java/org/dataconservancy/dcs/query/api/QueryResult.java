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
package org.dataconservancy.dcs.query.api;

import java.util.ArrayList;
import java.util.List;

/**
 * A sublist in the complete list of objects matching a query.
 */
public class QueryResult<T> {

    private final long offset;

    private final long total;

    private String queryString;

    private String[] queryParams;

    private List<QueryMatch<T>> matches;

    /**
     * Construct a QueryResult.  The sub list of results begins at <code>offset</code>, of <code>total</code> results.
     * The <code>queryString</code> and <code>queryParams</code> were the strings used for the original query.
     *
     * The offset must not exceed total, otherwise an invalid argument exception is thrown.  The
     * <code>queryString</code> must not be null.  The <code>queryParams</code> may be empty or null.
     *
     * @param offset the offset into the results
     * @param total the total number of results
     * @param queryString the original query string that produced these results
     * @param queryParams the original query parameters, may be null or empty
     * @throws IllegalArgumentException if <code>offset</code> is greater than <code>total</code>, or if
     * <code>queryString</code> is null or empty.
     */
    public QueryResult(long offset, long total, String queryString, String... queryParams) {
        this.offset = offset;
        this.total = total;

        if (offset > total) {
            throw new IllegalArgumentException("The search result offset must not be greater than the total number of" +
                    "results.");
        }

        if (queryString == null || queryString.trim().length() == 0) {
            throw new IllegalArgumentException("The search query string must not be null or empty.");
        }

        this.queryString = queryString;
        this.queryParams = queryParams;
        this.matches = new ArrayList<QueryMatch<T>>();
    }

    /**
     * @return the total number of matches
     */
    public long getTotal() {
        return total;
    }

    /**
     * @return offset into total matches of the start of these matches
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @return matches by reference
     */
    public List<QueryMatch<T>> getMatches() {
        return matches;
    }

    /**
     * Obtain the original query string
     *
     * @return the query string
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Obtain the original query parameters, may be null.
     *
     * @return the query parameters
     */
    public String[] getQueryParams() {
        return queryParams;
    }

    public String toString() {
        return "SearchResult {" + "offset: '" + offset + "' total: '" + total
                + "' matches: " + matches + "}";
    }
}
