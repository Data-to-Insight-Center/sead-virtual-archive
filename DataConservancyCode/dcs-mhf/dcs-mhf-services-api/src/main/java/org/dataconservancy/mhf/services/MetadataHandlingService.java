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

package org.dataconservancy.mhf.services;

import java.net.URL;

import java.util.Set;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.ui.model.BusinessObject;

/**
 * Main service entry point for clients of the mhf. This service is responsible for validating and extracting metadata from a given business object.
 * 
 * Results of the extraction will be returned by the service calls, results of the validation will be returned as {@code MetadataHandlingEvent} objects, through the 
 * {@code MetadataHandlingEventManager}. 
 *
 */
public interface MetadataHandlingService {
    
    /**
     * Validates and extracts metadata from the given business object. 
     * @param businessObject The business object to validate and extract metadata from.
     * @param parentId The id of the aggregating business object if one exists.
     * @return The set of {@code AttributeSet}s that are extracted from the {@code BusinessObject}. Or an empty set if nothing was extracted.  
     */
    public Set<AttributeSet> validateAndExtractMetadata(BusinessObject businessObject, String parentId, URL baseUrl);
    
    /**
     * Validates the metadata from the given business object.
     * 
     * @param businessObject The business object for which to validate metadata.
     */
    public void validateMetadata(BusinessObject businessObject, URL baseUrl);

    /**
     * Extracts the metadata from the given business object.
     *
     * @param businessObject The business object from which to extract metadata.
     */
    public Set<AttributeSet> extractMetadata(BusinessObject businessObject);
}