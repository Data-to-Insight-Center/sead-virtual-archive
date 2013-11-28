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

package org.dataconservancy.ui.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates an archival search result. Provides access to the <em>total</em> number of matched objects in the
 * archive, and a collection of matching objects, which may be a subset of the total number of matching objects.
 *
 * @param <T> the type of the business object
 *
 * @see org.dataconservancy.ui.services.ArchiveService#retrieveCollection(String)
 * @see org.dataconservancy.ui.services.ArchiveService#retrieveDataSet(String)
 * @see org.dataconservancy.ui.services.ArchiveService#retrieveDataSetsForCollection(String, int, int)
 */
public class ArchiveSearchResult<T> {
    private long resultCount;
    private Collection<T> results;

    /**
     * Construct an ordered (i.e. sorted) search result.
     *
     * @param result       the ordered list of results
     * @param totalResults total number of results
     */
    public ArchiveSearchResult(List<T> result, long totalResults) {
        results = result;
        this.resultCount = totalResults;
    }

    /**
     * Construct a search result which has no defined order, but is guaranteed to not
     * contain duplicate results.
     *
     * @param result       the un-ordered set of results
     * @param totalResults total number of results
     */
    public ArchiveSearchResult(Set<T> result, long totalResults) {
        results = result;
        this.resultCount = totalResults;
    }

    /**
     * The the total number of items in the archive that matched the search.  This is not necessarily the same as {@code
     * getResults().size()}.
     *
     * @return the number of items
     */
    public long getResultCount() {
        return resultCount;
    }

    /**
     * The results, which may only be a subset of the total result.  The {@code Collection} may or may not
     * be sorted, depending on how this result was constructed.
     *
     * @return the search results
     */
    public Collection<T> getResults() {
        return results;
    }
}