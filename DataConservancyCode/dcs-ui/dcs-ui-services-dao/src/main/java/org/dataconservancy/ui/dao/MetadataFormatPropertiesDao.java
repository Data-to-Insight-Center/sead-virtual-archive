/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.dao;

import org.dataconservancy.ui.model.MetadataFormatProperties;

import java.util.List;

/**
 * Provides persistence and retrieval methods of the business properties associated with a metadata format.  Typically,
 * the business properties of {@code DcsMetadataFormat} objects are persisted by this DAO, using the business identifier
 * of the {@code DcsMetadataFormat} as the key.
 */
public interface MetadataFormatPropertiesDao {

    /**
     * Persist the business properties to the DAO.  Typically the
     * {@link org.dataconservancy.ui.model.MetadataFormatProperties#getFormatId() formatId} will be used as a key.
     *
     * @param properties the properties to persist
     */
    public void add(MetadataFormatProperties properties);

    /**
     * Update a properties object that already exists.  The existing properties will be replaced by {@code properties}.
     *
     * @param properties the properties to update
     */
    public void update(MetadataFormatProperties properties);

    /**
     * Obtain the business properties of the identified metadata format.
     *
     * @param metadataFormatId the business identifier of the metadata format
     * @return the business properties of the metadata format
     */
    public MetadataFormatProperties get(String metadataFormatId);

    /**
     * List all of the properties of metadata formats, including ones that has been marked as deleted.
     *
     * @return a List of all metadata formats
     */
    public List<MetadataFormatProperties> list();

    /**
     * List properties of metadata formats based on provided status (active, or not active)
     * @return a List of all metadata formats
     */
    public List<MetadataFormatProperties> list(boolean isActive);

}
