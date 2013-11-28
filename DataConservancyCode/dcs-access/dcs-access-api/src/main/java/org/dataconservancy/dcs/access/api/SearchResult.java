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
package org.dataconservancy.dcs.access.api;

import java.util.ArrayList;
import java.util.List;

/**
 * See {@link org.dataconservancy.dcs.query.api.QueryResult}
 */
@Deprecated
public class SearchResult {

    private final int offset;

    private final long total;

    private List<Match> matches;

    public SearchResult(int offset, long total) {
        this.offset = offset;
        this.total = total;
        this.matches = new ArrayList<Match>();
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
    public int getOffset() {
        return offset;
    }

    /**
     * @return matches
     */
    public List<Match> getMatches() {
        return matches;
    }

    public String toString() {
        return "SearchResult {" + "offset: '" + offset + "' total: '" + total
                + "' matches: " + matches + "}";
    }
}
