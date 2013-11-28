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

import java.io.IOException;
import java.io.InputStream;

import org.dataconservancy.model.dcs.DcsEntity;

/**
 * See {@link org.dataconservcancy.dcs.query.api.QueryMatch}
 *
 */
@Deprecated
public interface AccessService {

    // TODO separate exception to indicate inability to run query as given? 

    /**
     * Page through entities matching a query.
     * 
     * @param query
     * @param offset
     *        offset into total matches
     * @param maxmatches
     *        max matches returned
     * @param params
     *        name,value pairs to set search parameters
     * @return result of searching
     * @throws IOException
     *         on error searching
     */
    public SearchResult search(String query,
                               int offset,
                               int maxmatches,
                               String... params) throws IOException;

    // TODO add query constructor
    // public String construct(String op, String field_value_pairs...)

    /**
     * Retrieve a DcsEntity.
     * 
     * @param id
     * @return entity with given id or null if it does not exist
     * @throws IOException
     */
    public DcsEntity getEntity(String id) throws IOException;

    /**
     * Retrieve the data held by a DcsEntity directly from the archive.
     * 
     * @param id
     * @return data or null if the entity does not exist or does not hold any
     *         data
     */
    public InputStream getDatastream(String id);

    /**
     * Add all of of the entities stored in the associated archive to its index.
     * TODO this may belong somewhere else
     * 
     * @throws IOException
     */
    public void indexArchive() throws IOException;

    /**
     * @return a mechanism to modify the index.
     * @throws IOException
     */
    public IndexWriter updateIndex() throws IOException;

    /**
     * Remove all entities in the index.
     * 
     * @throws IOException
     */
    public void clearIndex() throws IOException;

    /**
     * Attempt to make future access to the index more efficient. Should be
     * called after a large batch update.
     * 
     * @throws IOException
     */
    public void optimizeIndex() throws IOException;

    /**
     * Stops the service and cleans up. This method must be called when the
     * service is done being used.
     * 
     * @throws IOException
     */
    public void shutdown() throws IOException;

    /**
     * @param id
     * @return when the entity was last modified or -1 if the time is unknown or
     *         the entity does not exist
     */
    public long getEntityLastModified(String id) throws IOException;
}
