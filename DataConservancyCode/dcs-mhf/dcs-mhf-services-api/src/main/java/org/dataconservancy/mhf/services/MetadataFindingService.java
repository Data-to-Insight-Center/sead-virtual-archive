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

import java.util.List;

import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.ui.model.BusinessObject;

/**
 * This service is responsible for finding all the {@code MetadataInstance} objects contained in a given {@code BusinessObject}. 
 */
public interface MetadataFindingService {
    
    /**
     * Given a {@link BusinessObject}, use the appropriate {@link org.dataconservancy.mhf.finder.api.MetadataFinder} to identify
     * and retrieve metadata from the object for further processing.
     * @param businessObject - The {@link BusinessObject} whose metadata is to be retrieved
     * @return A list of {@link MetadataInstance} found in the {@link BusinessObject}
     */
    public List<MetadataInstance> findMetadata(BusinessObject bo);
}