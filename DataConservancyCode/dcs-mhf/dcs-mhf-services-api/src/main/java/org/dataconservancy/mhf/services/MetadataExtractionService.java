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

import java.util.Set;

import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;

/**
 * This service extracts metadata from a given {@code MetadataInstance}. This service should be used in conjunction with {@code MetadataFindingService}
 * and {@code MetadataValidationService}.
 *
 */
public interface MetadataExtractionService {
    
    /**
     * Extracts all the attribute sets from the given {@code MetadataInstance}.
     * @param <T>
     * @param <T>
     * @param mi The {@code MetadataInstance} to extract metadata from. 
     * @return A set of all the {@code AttributeSet} object extracted from the metadata.
     */
    <T> Set<MetadataRepresentation<T>> extract(MetadataInstance mi);
}