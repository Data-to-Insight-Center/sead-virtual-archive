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

package org.dataconservancy.mhf.instance.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * Encapsulates the properties of an instance of metadata.  Metadata extractors will operate on {@code MetadataInstance}
 * and produce {@code MetadataRepresentation} objects.
 * <p/>
 * If this {@code MetadataInstance} is embedded in a file (for example, EXIF embedded in a TIFF_FORMAT_ID image), then
 * the {@link #getFormatId() format identifier} is that of the TIFF_FORMAT_ID, because clients of this interface will need to
 * interpret the {@link #getContent() content} provided by this interface as a TIFF_FORMAT_ID.
 * <p/>
 * If this {@code MetadataInstance} represents a stand-alone file (for example, an FGDC XML document), then the format
 * identifier will be that of FGDC XML, and the content provided by this interface will be the FGDC document itself.
 * <p/>
 * If this {@code MetadataInstance} represents metadata properties of a business object, then the format identifier will
 * identify a business object serialization, and the content provided by this interface will be the serialization of the
 * business object.
 */
public interface MetadataInstance {

    /**
     * The format identifying the {@link #getContent content} of this instance.  A {@code null} response may be used to
     * indicate the format is unknown.
     *
     * @return the string representing the format of the metadata
     */
    public String getFormatId();

    /**
     * The content of this instance.  This will  be the bytestream of the metadata itself (e.g., an FGDC document), or
     * the bytestream of the file that contains embedded metadata (e.g., a TIFF_FORMAT_ID that contains EXIF metadata).
     *
     * @return an {@code InputStream} to the metadata content, never {@code null}
     */
    public InputStream getContent() throws IOException;

}
