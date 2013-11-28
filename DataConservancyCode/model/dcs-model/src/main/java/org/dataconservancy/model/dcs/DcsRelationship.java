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
package org.dataconservancy.model.dcs;


/**
 * Relationship types supported by the Data Conservancy.
 */
public enum DcsRelationship {

    /**
     * Asserts that the current entity is metadata for the related entity.
     *
     * Inverse of {@link #HAS_METADATA}.
     */
    IS_METADATA_FOR("urn:dataconservancy.org:rel/isMetadataFor"),

    /**
     * Asserts that the current entity is described by the related entity.
     *
     * Inverse of {@link #IS_METADATA_FOR}.
     */
    HAS_METADATA("urn:dataconservancy.org:rel/hasMetadata"),

    /**
     * Asserts that the current entity is the successor of by the related entity and is in the same lineage.
     *
     */
    IS_SUCCESSOR_OF("urn:dataconservancy.org:rel/isSuccessorOf"),

    /**
     * Asserts that the current entity is a registry entry for the related entity.
     * 
     * Inverse of {@link #HAS_REGISTRY_ENTRY}.
     */
    IS_REGISTRY_ENTRY_FOR("urn:dataconservancy.org:rel/isRegistryEntryFor"),
    
    /**
     * Asserts that the current entity has a registry entry in the related entity.
     * 
     * Inverse of {@link #IS_REGISTRY_ENTRY_FOR}.
     */
    HAS_REGISTRY_ENTRY("urn:dataconservancy.org:rel/hasRegistryEntry");
    

    private final String uri;

    private DcsRelationship(String uri) {
        this.uri = uri;
    }

    /**
     * Obtain a string representation of the type.  This should be a URI.
     *
     * @return a URI string representing the relationship type
     */
    public String asString() {
        return uri;
    }

    /**
     * Obtain a DcsRelationship from a string representation.  May return <code>null</code> if the type isn't supported.
     *
     * @param relType a string representing the relationship type
     * @return the DcsRelationship or <code>null</code> if it isn't supported.
     */
    public static DcsRelationship fromString(String relType) {
        for (DcsRelationship value : values()) {
            if (value.asString().equals(relType)) {
                return value;
            }
        }

        return null;
    }
}
