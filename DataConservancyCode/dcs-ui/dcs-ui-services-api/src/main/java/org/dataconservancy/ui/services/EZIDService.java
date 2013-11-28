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

import org.dataconservancy.ui.exceptions.EZIDServiceException;
import org.dataconservancy.ui.util.EZIDMetadata;


/** 
 * This service generates EZIDs using DOI. A DataCite metadata profile will be created from the Collection provided.
 *
 */
public interface EZIDService {
    
    /**
     * This will reserve an ez id for the given collection. The id will not be public until save id is called.
     * @param metadata The EZID metadata produced by the metadata generator, for the business object.
     * @return A string representing the ez id, or an empty string if the id could not be formed. This ID will need to be used to save or delete the id.
     */
    public String createID(EZIDMetadata metadata) throws EZIDServiceException;
    
    /**
     * This will make the passed in EZID public if it's not already public.
     * @param id The ez id to make public.
     */
    public void saveID(String id) throws EZIDServiceException;
    
    /**
     * Deletes the given id, only ids that haven't been saved can be deleted. 
     * @param id The ez id to delete. 
     */
    public void deleteID(String id) throws EZIDServiceException;
    
}