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
package org.dataconservancy.dcs.transform.index;

import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.transform.index.IndexOutput;

public abstract class TestableIndexOutput<K, V>
        extends IndexOutput<K, V> {

    protected IndexService<V> indexService;
    
    public TestableIndexOutput(IndexService<V> svc) {
        super(svc);
        indexService = svc;
    }

    /**
     * Determine if a particular value has been indexed.
     * <p>
     * Implementations should perform a true index lookup to determine the
     * presence of the given value.
     * </p>
     * 
     * @param value
     *        Value to search in the index.
     * @return True if indexed.
     */
    public abstract boolean hasIndexed(V value);

    /** Determine the number of entries in the index */
    public abstract long getIndexSize();

    /**
     * Close the output, and return the number of index entries that should have
     * been created as a result.
     */
    public abstract int closeAndCount();
}
