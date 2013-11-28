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

package org.dataconservancy.mhf.extractor.api;

import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;

import java.util.Collection;

/**
 * Provides method(s) to extract metadata information from specifically formatted {@code MetadataInstance} and put into
 */
public interface MetadataExtractor {
    /**
     * Extracts metadata from the supplied {@code MetadataInstance}, transforming it into a format-independent
     * {@code MetadataRepresentation}.
     *
     * @param instance metadata in a specific format
     * @return a specific implementation of a {@code MetadataRepresentation} object that can be processed by the system.
     */
    public Collection<MetadataRepresentation> extractMetadata(MetadataInstance instance) throws ExtractionException;
}
