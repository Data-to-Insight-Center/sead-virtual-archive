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
package org.dataconservancy.ui.model;

/**
 * Represents metadata files contained by  {@link Collection}.
 */
public class MetadataFile extends DataFile {
    
    private String metadataFormatId;
 
    public MetadataFile() {
    }

    public MetadataFile(MetadataFile toCopy) {
        this.id = toCopy.id;
        this.source = toCopy.source;
        this.format = toCopy.format;
        this.name = toCopy.name;
        this.path = toCopy.path;
        this.metadataFormatId = toCopy.metadataFormatId;
        this.size = toCopy.size;
        this.parentId = toCopy.parentId;
    }
    
    public MetadataFile(String id, String source, String format, String name, String path, String metadataFormatId, String parentId) {
        this.id = id;
        this.source = source;
        this.format = format;
        this.name = name;
        this.path = path;
        this.metadataFormatId = metadataFormatId;
        this.parentId = parentId;
    }
    
    public void setMetadataFormatId(String formatId) {
        this.metadataFormatId = formatId;
    }
    
    public String getMetadataFormatId() {
        return metadataFormatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetadataFile that = (MetadataFile) o;

        if (format != null ? !format.equals(that.format) : that.format != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (metadataFormatId != null ? !metadataFormatId.equals(that.metadataFormatId) : that.metadataFormatId != null) return false;
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (metadataFormatId != null ? metadataFormatId.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetadataFile{" +
                "format='" + format + '\'' +
                ", source='" + source + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", metadataFormatId='" + metadataFormatId + '\'' +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}