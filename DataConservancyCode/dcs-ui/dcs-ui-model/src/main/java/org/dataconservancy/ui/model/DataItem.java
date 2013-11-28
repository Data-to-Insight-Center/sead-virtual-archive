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

import org.joda.time.DateTime;

/**
 * {@code DataItem} represent a data item in the system.
 */
public class DataItem extends BusinessObject {
	
    private String name;
    private String description;
    private String depositorID;
    private DateTime depositDate;
    private List<DataFile> files = new ArrayList<DataFile>();
    private String parentId;

    /**
     * Constructs a DataItem with no state.
     */
    public DataItem() {

    }

    /**
     * Constructs a DataItem with the supplied state.
     *
     * @param name
     * @param description
     * @param id
     * @param depositor
     * @param depositDate
     * @param files
     */
    public DataItem(String name, String description, String id,
                    String depositor, DateTime depositDate, List<DataFile> files, List<String> metadataFileIds, String parentId) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.depositorID = depositor;
        this.depositDate = depositDate;
        this.files = files;
        this.parentId = parentId;
	}

    /**
     * Constructs a DataItem, initializing this state with {@code toCopy}.
     */
    public DataItem(DataItem toCopy) {
        this.name = toCopy.getName();
        this.description = toCopy.getDescription();
        this.id = toCopy.getId();
        this.depositorID = toCopy.getDepositorId();
        this.depositDate = toCopy.getDepositDate();
        this.files = toCopy.getFiles();
        this.parentId = toCopy.getParentId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((depositDate == null) ? 0 : depositDate.hashCode());
        result = prime * result + ((depositorID == null) ? 0 : depositorID.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((files == null) ? 0 : files.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        DataItem other = (DataItem) obj;
        if (depositDate == null) {
            if (other.depositDate != null)
                return false;
        } else if (!depositDate.toString().equals(other.depositDate.toString()))
            return false;
        if (depositorID == null) {
            if (other.depositorID != null)
                return false;
        } else if (!depositorID.equals(other.depositorID))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (files == null) {
            if (other.files != null)
                return false;
        } else if (!files.equals(other.files))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        
        return true;
    }
    
    
    @Override
    public String toString() {
        return "DataItem [name=" + name + ", description=" + description
                + ", id=" + id + ", depositor=" + depositorID + ", depositDate="
                + depositDate + ", files=" + files + ", parentId= " + parentId + "]";
    }
    
    /**
     * Sets the publically viewable name of the dataset.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Retrieves the name of the dataset. 
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the description of the dataset.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Adds a file to the data set
     */
    public void addFile(DataFile file) {
        files.add(file);
    }
    
    /**
     * Retrieves the list of files associated with the dataset.
     */
    public List<DataFile> getFiles() {
        return this.files;
    }
    
    /**
     * Attach to a list of file to be associated to the data set
     * @param files
     */
    public void setFiles(List<DataFile> files) {
        this.files = files;
    }
    
    /**
     * Gets the user who deposited the data
     */
    public String getDepositorId() {
        return this.depositorID;	
    }
    
    /**
     * Sets the depositor of the dataset.
     */
    public void setDepositorId(String depositor) {
        this.depositorID = depositor;
    }
    
    /**
     * Gets the date of the deposit
     */
    public DateTime getDepositDate() {
        return this.depositDate;
    }
    
    /**
     * Sets the date of the deposit
     */
    public void setDepositDate(DateTime depositDate) {
        this.depositDate = depositDate;
    }
    
    /**
     * Sets the id of the parent of this data item. Typically this will be the id of a collection.
     * @param id
     */
    public void setParentId(String id) {
        this.parentId = id;
    }
    
    /**
     * Retrieves the parent id of this data item. 
     * @return
     */
    public String getParentId() {
        return parentId;
    }
}
