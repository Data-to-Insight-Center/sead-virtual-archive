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

import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.ui.model.BusinessObject;

/**
 * Service responsible for validating metadata associated with {@code BusinessObject} and {@code MetadataInstance} objects.
 * Note: This service only validates that the given metadata is off the type it is supposed to be. No heuristics are performed to validate that fields 
 * in the metadata are there as expected. 
 */
public interface MetadataValidationService {
    
    /**
     * Validates all metadata associated with the given {@code BusinessObject}
     * @param bo The {@code BusinessObject} to validate. 
     * @param baseUrl The baseUrl of the MetadataInstance used to read any includes if they exist, may be empty or null.
     * @return true if all the metadata associated with the business object is valid false otherwise
     */
    public boolean validate(BusinessObject bo, URL baseUrl);
    
    /**
     * Validates that the {@code MetadataInstance} is of the proper type.
     * @param businessObjectId the {@link String} identifier of the business object from which the {@link MetadataInstance} retrieved.
     * @param mi The {@code MetadataInstance} to validate. 
     * @param baseUrl The baseUrl of the MetadataInstance used to read any includes if they exist, may be empty or null.
     * @return true if the instance is of the correct type, and passes schema validation(if available), false otherwise.
     */
    public boolean validate(String businessObjectId, MetadataInstance mi, URL baseUrl);
}