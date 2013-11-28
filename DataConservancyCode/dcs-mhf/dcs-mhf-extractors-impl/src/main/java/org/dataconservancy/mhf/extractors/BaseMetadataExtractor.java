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
package org.dataconservancy.mhf.extractors;

import org.dataconservancy.mhf.extractor.api.MetadataExtractor;

/**
 * Base {@code MetadataExtractor} implementation.
 */
public abstract class BaseMetadataExtractor implements MetadataExtractor {

    /**
     * The error string used in MetadataExtractorExceptions when an extractor encounters a MetadataInstance format
     * that it cannot handle.  Parameters: metadata instance format id
     */
    protected static final String INVALID_FORMAT_ERROR = "Cannot extract metadata from instances with format %s.";

    /**
     * Error string used in MetadataExtractorExceptions when a null {@code MetadataInstance} is passed into the
     * extract method.  No parameters.
     */
    protected static final String NULL_INSTANCE = "MetadataInstance must not be null! ";

    /**
     * Error string used in MetadataExtractorExceptions when inputstream to the metadata content cannot be closed.
     */
    protected static final String FAILED_CLOSE = "Could not close metadata input stream. ";

    /**
     * Error string used in MetadataExtractorExceptions when the XML document containing metadata provided would not be
     * parsed.
     */
    protected static final String UNPARSABLE_XML = "XML document provided could not be parsed. ";

    /**
     * Error string used in MetadataExtractorExceptions when the {@code InputStream} containing metadata provided could
     * not be read.
     */
    protected static final String FAILED_INPUTSTREAM_READING = "InputStream provided could not be read. ";

}
