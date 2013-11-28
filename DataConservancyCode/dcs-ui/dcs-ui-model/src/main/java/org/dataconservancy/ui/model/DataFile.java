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

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link org.dataconservancy.ui.model.File} that is meant to support data files, as oppose to {@link org.dataconservancy.ui.model.MetadataFile}.
 */
public class DataFile extends BusinessObject {

    protected String parentId;
	protected String name;
	protected String source;
	protected String format;
	protected String path;
	protected long size = -1;
	
    public DataFile() {

    }

	public DataFile(String id, String name, String source, String format, String path, long size, ArrayList<String> metadataFiles) {
        this.id = id;
		this.name = name;
		this.source = source;
		this.format = format;
		this.path = path;
		this.size = size;
	}

	public DataFile(DataFile toCopy) {
        this.id = toCopy.id;
		this.name = toCopy.name;
		this.source = toCopy.source;
		this.format = toCopy.format;
		this.path = toCopy.path;
		this.size = toCopy.size;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * File's {@code source} is a resolvable URI from which the file's byte stream can be retrieved.
     * @return
     */
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
    
    public void setPath(String path) {
        this.path = path;        
    }

    /**
     * File's {@code path} is an optional informational field describing the file's location at the time of upload.
     * @return
     */
    public String getPath() {
       return path;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size){
        this.size = size;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataFile other = (DataFile) obj;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
        if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (parentId == null) {
                    if (other.parentId != null)
                        return false;
                } else if (!parentId.equals(other.parentId))
                    return false;
        if( size != other.size){
            return false;
        }
		return true;
	}

	@Override
	public String toString() {
		return "DataFileImpl [name=" + name + ", path=" + path + ", source=" + source
				+ ", format=" + format + ", id=" + id + ", size=" + size + ", parentId=" + parentId + "]";
	} 

}
