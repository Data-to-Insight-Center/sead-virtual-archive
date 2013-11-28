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

import org.dataconservancy.dcs.transform.index.IndexOutputFactory;

public abstract class TestableIndexOutputFactory<K, V>
        extends IndexOutputFactory<K, V> {

    public abstract TestableIndexOutput<K, V> newOutput();

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

}
