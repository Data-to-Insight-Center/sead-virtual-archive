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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Implementation of MetadataInstance, in which the {@link InputStream} contains serialized {@link MetadataAttributeSet}.
 */
public class AttributeSetMetadataInstance implements MetadataInstance {

    private final String formatId;
    private final byte[] metadataByteArray;

    public AttributeSetMetadataInstance(String formatId, byte[] metadataByteArray) {
        if (metadataByteArray.length == 0) {
            throw new IllegalArgumentException("ByteArray cannot be empty");
        }

        if (formatId == null || formatId.trim().length() == 0) {
            throw new IllegalArgumentException("Format ID must not be null or an empty string.");
        }

        this.formatId = formatId;
        this.metadataByteArray = metadataByteArray;
    }

    @Override
    public InputStream getContent() {
        return new ByteArrayInputStream(metadataByteArray);
    }

    @Override
    public String getFormatId() {
        return formatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeSetMetadataInstance)) return false;

        AttributeSetMetadataInstance that = (AttributeSetMetadataInstance) o;

        if (formatId != null ? !formatId.equals(that.formatId) : that.formatId != null) return false;
        if (!Arrays.equals(metadataByteArray, that.metadataByteArray)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = formatId != null ? formatId.hashCode() : 0;
        result = 31 * result + (metadataByteArray != null ? Arrays.hashCode(metadataByteArray) : 0);
        return result;
    }
}
