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
package org.dataconservancy.dcs.index.api;

/**
 * Add objects to and remove objects from an index. The index may be used during
 * a batch indexing job. Changes to the index may not occur until
 * {@link #close()} is called. Multiple batch indexers may simultaneously modify
 * the index, but a batch indexer instance is not MT safe.
 * 
 * A failure while modifying or closing the index always leaves the index in a
 * consistent state.
 */

public interface BatchIndexer<T> {
    /**
     * Add an object to the index.
     * 
     * @param obj
     * @throws IndexServiceException
     */
    public void add(T obj) throws IndexServiceException;

    /**
     * Remove data from the index associated with the identified object. The
     * object must have been added during a different batch indexing job. May
     * not be supported by all implementations.
     * 
     * @param id
     * @throws IndexServiceException
     */
    public void remove(String id) throws IndexServiceException,
            UnsupportedOperationException;

    /**
     * Finishes indexing, and cleans up. Close must be called to finish a batch
     * indexing job. This object must not be used after close is called.
     * 
     * @throws IndexServiceException
     */

    public void close() throws IndexServiceException;
}
