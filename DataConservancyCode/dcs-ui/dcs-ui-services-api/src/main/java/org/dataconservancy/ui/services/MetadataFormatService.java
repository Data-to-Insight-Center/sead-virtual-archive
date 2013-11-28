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
package org.dataconservancy.ui.services;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFormatProperties;

import java.util.Map;
import java.util.Set;

/**
 * Provides access to {@link DcsMetadataFormat} objects.
 */
public interface MetadataFormatService extends Iterable<DcsMetadataFormat> {

    /**
     * Obtain a {@code Set} of all known MetadataFormats.
     *
     * @return the MetadataFormats
     */
    public Set<DcsMetadataFormat> getMetadataFormats();

    /**
     * Obtain a {@code Set} of MetadataFormats based on their active status. DcsMetadataFormat entries that do not
     * have corresponding MetadataFormatProperties are considered ACTIVE.
     *
     * @return the active MetadataFormats if {@code isActive} is true
     * @return the inactive/deleted/deactivated MetadataFormats if {@code isActive} is false
     */
    public Set<DcsMetadataFormat> getMetadataFormats(boolean isActive);

    /**
     * Obtain Metadata Formats that are associated with the business object, keyed by the Discipline of the
     * Metadata Format.
     *
     * @param businessId the identifier of a business object
     * @return Metadata Formats that are associated with the business object, keyed by Discipline
     */
    public Map<Discipline, Set<DcsMetadataFormat>> getMetadataFormats(String businessId);

    /**
     * Obtain all of the {@code DcsMetadataFormat} aggregated by the {@code Discipline}.
     *
     * @param disciplineId The URI id of the discipline whose metadata formats will be returned
     * @return a {@code Set} of {@code DcsMetadataFormat}, may be empty but not {@code null}
     */
    public Set<DcsMetadataFormat> getMetadataFormatsForDiscipline(String disciplineId);

    /**
     * Obtain all of the {@code DcsMetadataFormat} aggregated by the {@code Discipline}.
     *
     * @param disciplineId The URI id of the discipline whose metadata formats will be returned.
     * @param isActive Instructs the methods only to return {@code MetadataFormat} with matching isActive properties
     *
     * @return a {@code Set} of {@code DcsMetadataFormat}, may be empty but not {@code null}
     */
    public Set<DcsMetadataFormat> getMetadataFormatsForDiscipline(String disciplineId, boolean isActive);

    /**
     * Obtain the specified MetadataFormat.  May return {@code null} if it cannot be found.
     *
     * @param id the identifier of the MetadataFormat
     * @return the MetadataFormat, or {@code null} if not found
     */
    public DcsMetadataFormat getMetadataFormat(String id);

    /**
     * Adds a metadata format to the registry. 
     * 
     * @throws BizInternalException if there is an error parsing the metadata scheme file, 
     * or the the deposit into the archive throws an exception.
     * @param format the {@link DcsMetadataFormat} object to add to the registry.
     */
    public String addMetadataFormat(DcsMetadataFormat format) throws BizInternalException;

    /**
     * Retrieve the business properties of the specified {@code DcsMetadataFormat}.
     *
     * @param dcsMetadataFormatId the business identifier of the {@code DcsMetadataFormat}
     * @return the business properties, or {@code null} if the format doesn't exist
     */
    public MetadataFormatProperties getProperties(String dcsMetadataFormatId);

    /**
     * Persist the properties associated with a {@code DcsMetadataFormat}.
     *
     *
     * @param dcsMetadataFormat the {@code DcsMetadataFormat} that the properties apply to
     * @param properties the business properties to persist
     * @throws IllegalArgumentException if {@code properties} is {@code null}
     * @throws BizInternalException if there is trouble persisting the properties
     */
    public void setProperties(DcsMetadataFormat dcsMetadataFormat, MetadataFormatProperties properties)
            throws BizInternalException;

}
