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

package org.dataconservancy.mhf.instances;

import org.dataconservancy.mhf.instance.api.MetadataInstance;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A MetadataInstance that is encapsulated in a file.
 */
public class FileMetadataInstance implements MetadataInstance {

    private final URL metadataContentRef;
    private final String formatId;

    public FileMetadataInstance(String formatId, URL metadataContentRef) {
        if (metadataContentRef == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }
        this.metadataContentRef = metadataContentRef;
        this.formatId = formatId;
    }

    @Override
    public InputStream getContent() throws IOException {
        return metadataContentRef.openStream();
    }

    @Override
    public String getFormatId() {
        return formatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMetadataInstance)) return false;

        FileMetadataInstance that = (FileMetadataInstance) o;

        if (formatId != null ? !formatId.equals(that.formatId) : that.formatId != null) return false;
        if (metadataContentRef != null ? !metadataContentRef.equals(that.metadataContentRef) : that.metadataContentRef != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = metadataContentRef != null ? metadataContentRef.hashCode() : 0;
        result = 31 * result + (formatId != null ? formatId.hashCode() : 0);
        return result;
    }
}
