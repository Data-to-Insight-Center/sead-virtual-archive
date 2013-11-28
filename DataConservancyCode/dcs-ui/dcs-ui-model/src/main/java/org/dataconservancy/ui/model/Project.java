/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

/**
 * Instances of {@code Project} represents users' projects in the system.
 */
public class Project extends BusinessObject {
    private List<String> numbers;
    private String name;
    private String description;
    private String publisher = "Johns Hopkins Data Management Service";
    
    private long storageAllocated;
    private long storageUsed;
    
    private DateTime startDate;
    private DateTime endDate;
    
    private String fundingEntity;
    
    private List<String> pis;
    
    public Project() {
        super();
        this.pis = new ArrayList<String>();
        this.numbers = new ArrayList<String>();
    }
    
    public Project(Project projectToCopy) {
        super();
        this.description = projectToCopy.getDescription();
        this.id = projectToCopy.getId();
        this.name = projectToCopy.getName();
        this.numbers = projectToCopy.getNumbers();
        this.storageAllocated = projectToCopy.getStorageAllocated();
        this.storageUsed = projectToCopy.getStorageUsed();
        this.startDate = projectToCopy.getStartDate();
        this.endDate = projectToCopy.getEndDate();
        this.fundingEntity = projectToCopy.getFundingEntity();
        this.publisher = projectToCopy.getPublisher();
        this.pis = new ArrayList<String>(projectToCopy.getPis());
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getPis() {
        return pis;
    }
    
    public void setPis(List<String> pis) {
        this.pis = pis;
    }
    
    public void removeAllPis() {
        this.pis.clear();
    }
    
    /*
     * START - Need to move the these method to a wrapper class
     */
    public void addPi(String person) {
        this.pis.add(person);
    }
    
    public void removePi(String person) {
        this.pis.remove(person);
    }
    
    /*
     * END - Need to move the these method to a wrapper class
     */
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getNumbers() {
        return numbers;
    }
    
    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }
    
    public void addNumber(String number) {
        this.numbers.add(number);
    }
    
    public void removeNumber(String number) {
        this.numbers.remove(number);
    }
    
    public void removeAllNumbers() {
        this.numbers.clear();
    }
    
    public long getStorageAllocated() {
        return storageAllocated;
    }
    
    public void setStorageAllocated(long storageAllocated) {
        this.storageAllocated = storageAllocated;
    }
    
    public long getStorageUsed() {
        return storageUsed;
    }
    
    public void setStorageUsed(long storageUsed) {
        this.storageUsed = storageUsed;
    }
    
    public DateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }
    
    public DateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }
    
    public String getFundingEntity() {
        return fundingEntity;
    }
    
    public void setFundingEntity(String fundingEntity) {
        this.fundingEntity = fundingEntity;
    }
    
    /**
     * gets the publisher/distributor for this project
     * 
     * @return publisher
     */
    public String getPublisher() {
        return publisher;
    }
    
    /**
     * sets the publisher/distributor for this project
     * 
     * @param publisher
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public String toString() {
        return "Project {id='" + id + "', " + "numbers='" + numbers + "', " + "name='" + name + "', " + "description='"
                + description + "'," + "startDate='" + startDate + "', " + "endDate='" + endDate + "', "
                + "fundingEntity='" + fundingEntity + "', " + "storageAllocated='" + storageAllocated + "', "
                + "storageUsed='" + storageUsed + "', " + "pis='" + pis + "'" + "publisher='" + publisher + "'"
        + "}";
    }
    
    /**
     * This equality function does NOT include a check for equalities in the pis. PIs for a project are being handled by
     * the relationship service.
     * 
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            if (o.getClass() != PersonWrapper.class)
                return false;
        
        Project project = (Project) o;
        
        if (id != null ? !id.equals(project.getId()) : project.getId() != null)
            return false;
        if (name != null ? !name.equals(project.getName()) : project.getName() != null)
            return false;
        if (description != null ? !description.equals(project.getDescription()) : project.getDescription() != null)
            return false;
        if (storageAllocated != project.storageAllocated)
            return false;
        if (storageUsed != project.storageUsed)
            return false;
        if (startDate != null ? !startDate.equals(project.getStartDate()) : project.getStartDate() != null)
            return false;
        if (endDate != null ? !endDate.equals(project.getEndDate()) : project.getEndDate() != null)
            return false;
        if (fundingEntity != null ? !fundingEntity.equals(project.getFundingEntity())
                : project.getFundingEntity() != null)
            return false;
        if (publisher != null ? !publisher.equals(project.getPublisher()) : project.getPublisher() != null)
            return false;
        
        return true;
    }
    
    /**
     * Calculates a hash code using the fields that are considered for {@link #equals(Object) equality}.
     * 
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        int result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (numbers != null ? numbers.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + Long.valueOf(storageAllocated).hashCode();
        result = 31 * result + Long.valueOf(storageUsed).hashCode();
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (fundingEntity != null ? fundingEntity.hashCode() : 0);
        result = 31 * result + (publisher != null ? publisher.hashCode() : 0);
        return result;
    }
    
}
