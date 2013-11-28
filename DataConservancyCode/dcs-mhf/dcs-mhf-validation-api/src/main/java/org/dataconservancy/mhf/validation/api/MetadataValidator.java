/*
 * Copyright 2013 Johns Hopkins University
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.mhf.validation.api;


import java.net.URL;

import org.dataconservancy.mhf.instance.api.MetadataInstance;

/**
 * Provides a mechanism for callers to validate the content of a {@code MetadataInstance} based on its format.
 */
public interface MetadataValidator <T> {

    /**
     * Validates metadata represented in a specific format, ie. a {@code MetadataInstance}.
     * Given a {@code MetadataInstance} object, {@code MetadataValidator} of that specific format
     * will examine the object and determine whether the {@code MetadataInstance} object conforms
     * to the format it claims to conform to.
     *
     * @param metadataInstance - metadata in a specified format that is to be validated.
     * @param baseUrl - The base url of the metadata instance used to resolve, any included files, can be empty or null
     * @throws {@code ValidationException} when an error occurs performing validation
     * @throws {@code InvalidInstanceException} when the metadata instance is invalid
     **/
    public void validate(MetadataInstance metadataInstance, URL baseUrl) throws ValidationException;
}
