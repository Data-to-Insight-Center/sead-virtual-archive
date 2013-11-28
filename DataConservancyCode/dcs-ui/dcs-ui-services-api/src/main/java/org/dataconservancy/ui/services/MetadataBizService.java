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


import java.io.IOException;

import java.net.URL;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.MetadataResult;

/**
 *  Service for validating, extracting and indexing metadata. This services handles passing business object to the metadata handling framework,
 *  and passing attribute sets to the indexing service. 
 */
public interface MetadataBizService {

    /**
     * Validates a metadata schema given a schemaUrl. Results will be returned in the MetadataResult object.
     * @param schemaUrl The url pointing to the schema file contents.
     * @return A metadata result object that will contain all of the validation messages sent during the schema validation.
     */
    public MetadataResult validateMetadataSchema(URL schemaUrl) throws IOException;
    
    /**
     * Passes the provided businessObject to the metadata handling framework to be validated and have metadata
     * extracted.
     * 
     * @param businessObject
     *            The business object to retrieve metadata for
     * @param aggregatingObjectId
     *            The id of the {@code BusinessObject} that aggregates the passed in businessObject, if one exists.
     * @return The {@code MetadataResult} representing all the events that occurred during validation and extraction and
     *         the attributes that were extracted.
     */
    public MetadataResult validateAndExtractMetadata(BusinessObject businessObject, String aggregatingObjectId);

    /**
     * Passes the provided businessObject to the metadata handling framework to have metadata extracted.
     *
     * @param businessObject
     *            The business object for which to extract metadata
     * @return The {@code MetadataResult} representing all the events that occurred during extraction and
     *         the attributes that were extracted.
     */
    public MetadataResult extractMetadata(BusinessObject businessObject);
    
    /**
     * Responsible for accepting sets of AttributeSet, transforming them input proper input for the DCS Archive Indexing Service,
     * and kicking of the DCS Archive Indexing Service.
     * @param businessId - {@link String} business identifier of the business object whose metadata is being indexed
     * @param metadataAttributeSets {@link java.util.Collection} of {@link AttributeSet} containing the metadata to be indexed
     */
    public void index(String businessId, java.util.Collection<AttributeSet> metadataAttributeSets);
    
    /**
     * Passes the given business object to the metadata validation service to be validated.
     * 
     * @param businessObject
     * @return The {@code MetadataResult} representing all the events that occurred during validation
     */
    public MetadataResult validateMetadata(BusinessObject businessObject);
    
    /**
     * Validates a test file against a new metadata schema that isn't yet in the registry.
     * 
     * @param metadataFile - The metadata file object representing the test file
     * @param metadataFormat - The new metadata format to validate against that hasn't yet been added to the registry.
     */
    public MetadataResult validateMetadata(MetadataFile metadataFile, DcsMetadataFormat metadataFormat) throws BizInternalException;

}
