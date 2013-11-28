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

import java.util.Map;

/**
 * Single unit of metadata from a MetadataStore.
 * <p>
 * Ultimately, this is little more than a series of key/value pairs.
 * </p>
 */
public interface Metadata {

    /**
     * Get the identifier value associated with this metadata.
     * 
     * @return String identifier.
     */
    public String getId();

    /**
     * Get a 'type' value associated with this metadata.
     * <p>
     * The metadata store should treat this as value as opaque, but in reality
     * it will contain a DCS entity type.
     * </p>
     * 
     * @return type string.
     */
    public String getType();

    /**
     * Get the 'src' value associated with this metadata.
     * <p>
     * This is an opaque string that is typically used for representing file
     * content location.
     * </p>
     * 
     * @return src string. May be null if there is no src value.
     */
    public String getSrc();

    /**
     * Append a map of key/value (id, type) pairs to existing metadata.
     * <p>
     * This is an idempotent, append-only operation. Technically, this is
     * nothing more then a block of key/value pairs used according to a
     * convention.
     * </p>
     * 
     * @param idAndTypeMap
     *        The convention used by the ELM store is to store values of Map(id,
     *        type)
     */
    public void addLinks(Map<String, String> idAndTypeMap);

    /**
     * Retrieve all stored key/value (id, type) pairs.
     * 
     * @return Non-redundant map of stored values
     */
    public Map<String, String> getLinks();
}
