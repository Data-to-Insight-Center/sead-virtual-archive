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
 * Manage an index.
 */
public interface IndexService<T> {

    /**
     * @return A BatchIndexer for adding objects to the index.
     * 
     * @throws IndexServiceException
     */
    public BatchIndexer<T> index() throws IndexServiceException;

    /**
     * Remove all of the objects in the index.
     * 
     * @throws IndexServiceException
     */
    public void clear() throws IndexServiceException;

    /**
     * Attempt to make future access to the index more efficient. Should be
     * called after a large batch update.
     * 
     * @throws IndexServiceException
     */
    public void optimize() throws IndexServiceException;

    /**
     * @return the number of objects indexed
     * 
     * @throws IndexServiceException
     */
    public long size() throws IndexServiceException;

    /**
     * Stops the service and cleans up. This method must be called when the
     * service is done being used.
     * 
     * @throws IndexServiceException
     */
    public void shutdown() throws IndexServiceException;
}
