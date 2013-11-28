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
package org.dataconservancy.archive.impl.elm;

/**
 * Manages identity, type, and link metadata related to entities.
 * <p>
 * May be read-only or read-write. If read-only, the assumption is that the
 * underlying implementation is able to derive and retrieve link metadata by
 * some means on its own.
 * </p>
 */
public interface MetadataStore {

    /**
     * Retrieve metadata associated with a particular entity.
     * 
     * @param id
     *        Entity identifier.
     * @return populated metadata, or null if not found.
     */
    public Metadata get(String id);

    /**
     * Add new entity metadata to the metadata store.
     * <p>
     * This method is idempotent - calling add multiple times should not affect
     * any metadata currently in the store. If read-only, this is a no-op.
     * <p>
     * 
     * @param id
     *        Entity identifier
     * @param type
     *        Entity type
     * @param src
     *        Content location associated with an entity. Typically, this is
     *        only associated with File entities, and contains a content
     *        location URI.
     * @return created (or existing) Metadata./
     */
    public Metadata add(String id, String type, String src);

    /**
     * Retrieve all stored metadata, possibly filtered by entity type.
     * 
     * @param type
     *        If defined, the results will be filtered to match the given types.
     * @return Iterable metadata.
     */
    public Iterable<Metadata> getAll(String... type);

    /**
     * Remove metadata for a particular entity.
     * <p>
     * If metadata is not present, this method will not fail, and will be a
     * no-op. This method only removes direct metadata associated with an
     * entity: if other metadata items contain links pointing to the one being
     * deleted, their links will not be touched.
     * </p>
     * 
     * @param id
     */
    public void remove(String id);

    /**
     * Determines if the underlying metadata store is read-only.
     * 
     * @return true if read-only.
     */
    public boolean isReadOnly();
}
