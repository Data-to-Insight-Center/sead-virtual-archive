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

import org.dataconservancy.model.dcs.DcsEntity;

/**
 * Write a set of dcs entities to an index.
 * <p>
 * IndexWriter implementations may internally buffer requests. Entities added to
 * an IndexWriter are not guaranteed to be submitted for indexing until
 * {@link #close() is called} is called.
 * </p>
 */
@Deprecated
public interface IndexWriter {

    /**
     * Add an entity to the index. The entity must not already exist.
     * 
     * @param entity
     * @throws IOException
     */
    public void add(DcsEntity entity) throws IOException;

    /**
     * Remove an existing entity in the index.
     * 
     * @param entity
     * @throws IOException
     */
    public void remove(String id) throws IOException;

    /**
     * Finishes indexing, and cleans up. The IndexWriter must not be used after
     * close is called and close must be called.
     * 
     * @throws IOException
     */
    public void close() throws IOException;
}
