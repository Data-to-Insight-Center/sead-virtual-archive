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
package org.dataconservancy.deposit;

import java.io.InputStream;

import java.util.Map;

/**
 * Represents a document describing in detail some aspect of a deposit.
 */
public interface DepositDocument {

    /**
     * Get a stream containing a document that contains a representation of some
     * deposit information.
     * 
     * @return InputStream, or null of there is no status document.
     */
    public InputStream getInputStream();

    /**
     * Get the MIME content type of the document stream.
     * 
     * @return IANA MIME type.
     */
    public String getMimeType();

    /**
     * Get the time the document was last modified, in miliseconds.
     * 
     * @return Number of miliseconds since the epoch
     */
    public long getLastModified();

    /**
     * Get additional file metadata using HTTP/1.1 header conventions.
     * <p>
     * Additional document metadata may optionally be returned in a map. By
     * convention, keys are named after http headers, and values follow their
     * http/1.1 semantics.
     * </p>
     * 
     * @return Map containing document metadata. May be null or empty.
     */
    public Map<String, String> getMetadata();

}
