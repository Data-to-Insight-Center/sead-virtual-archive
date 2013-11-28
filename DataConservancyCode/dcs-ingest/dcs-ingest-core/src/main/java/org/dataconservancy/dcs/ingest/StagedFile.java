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
package org.dataconservancy.dcs.ingest;

import java.io.InputStream;

/**
 * Individual staged file within a {@link FileContentStager}
 */
public interface StagedFile {

    /**
     * Resolvable URI that contains file content.
     * <p>
     * Should only be used internally. As in the case of file:/ URIs, may only
     * be resolvable within the local context of the DCS.
     * </p>
     * 
     * @return String containing resolvable access URI.
     */
    public String getAccessURI();

    /**
     * Non-resolvable URI identifying file content.
     * <p>
     * May be used for publicly referring to staged file content, and as a key
     * for retrieving content via {@link FileContentStager#get(String)}.
     * </p>
     * 
     * @return String containing reference URI.
     */
    public String getReferenceURI();

    /**
     * Get a reference to a SIP associated with the staging of a particular
     * file.
     * 
     * @return String containing an identifier of a staged sip
     */
    public String getSipRef();

    /**
     * Get staged file content.
     * 
     * @return InputStream containing file content bytes.
     */
    public InputStream getContent();

}