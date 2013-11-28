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

import java.io.InputStream;

import org.dataconservancy.archive.api.EntityNotFoundException;

/**
 * Opaquely stores and retrieves blobs, keyed on entity identifier.
 * <p>
 * The underlying store does not need to have any knowledge of the semantics of
 * the stored entity. In essence, it is a blob store that is able to store
 * retrieve based on an opaque provided String key.
 * </p>
 */
public interface EntityStore {

    /**
     * Store content for later retrieval by the given key.
     * <p>
     * Idempotent. 'put' content will simply replace any existing content.
     * Replacing identical content will have no net effect.
     * 
     * @param key
     *        Key that will be used for subsequent content retrieval.
     * @param stream
     */
    public void put(String key, InputStream stream);

    /**
     * Retrieve a specific entity from the store
     * 
     * @param key
     *        Opaque Identifier for a given unit of content.
     * @return Stream of content bytes.
     * @throws EntityNotFoundException
     *         if there is no content for a given key.
     */
    public InputStream get(String key) throws EntityNotFoundException;

    /**
     * Removes content stored under the given key.
     * <p>
     * If the content is not present, no exception will be thrown, and this
     * should become a no-op.
     * </p>
     * 
     * @param key
     *        Identity of the content to remove.
     */
    public void remove(String key);

}
