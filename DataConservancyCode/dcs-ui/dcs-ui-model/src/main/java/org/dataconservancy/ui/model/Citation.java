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


import org.joda.time.DateTime;

import java.net.URI;
import java.util.List;

/**
 * A {@code Citation} represents a collection's citation information.
 */
public class Citation {
    private List<PersonName> creators;
    private DateTime releaseDate;
    private String title;
    private String version;
    private String publisher;
    private String locator;
    private String resourceType;
    private DateTime accessDate;
    private DateTime updatedDate;


    public Citation(List<PersonName> creators, DateTime releaseDate, String title, String version, String publisher,
                    String resourceType,String locator, DateTime accessDate, DateTime updatedDate){
        this.creators = creators;
        this.releaseDate = releaseDate;
        this.title = title;
        this.version = version;
        this.publisher = publisher;
        this.resourceType = resourceType;
        this.locator = locator;
        this.accessDate = accessDate;
        this.updatedDate = updatedDate;
    }
    
    public Citation(Citation citation){
        this.creators = citation.creators;
        this.releaseDate = citation.releaseDate;
        this.title = citation.title;
        this.version = citation.version;
        this.publisher = citation.publisher;
        this.resourceType = citation. resourceType;
        this.locator = citation.locator;
        this.accessDate = citation. accessDate;
        this.updatedDate = updatedDate;
    }

    /**
     * Gets a string representing the author(s) for the cited object
     * @return creator
     */
    public List<PersonName> getCreators(){
        return this.creators;
    }

    /**
     * Sets the string representing the author(s) for the cited object
     * @param creators
     */
    public void setCreators(List <PersonName> creators){
        this.creators = creators;
    }
    
    /**
     * Gets the release date for the cited object
     * @return releaseDate
     */
    public DateTime getReleaseDate(){
        return this.releaseDate;
    }

    /**
     * Sets the release date for the cited object
     * @param releaseDate
     */
    public void setReleaseDate(DateTime releaseDate){
        this.releaseDate = releaseDate;
    }

    /**
     * Gets the title for the cited object
     * @return title
     */
    public String getTitle(){
        return this.title;
    }

    /**
     * Sets the title for the citation
     */
    public void setTitle(String title){
        this.title=title;
    }

    /**
     * Gets the version for the cited object
     * @return version
     */
    public String getVersion(){
        return this.version;
    }

    /**
     * Sets the version for the cited object
     * @param version
     */
    public void setVersion(String version){
        this.version = version;
    }

    /**
     * Gets the publisher or distributor for the cited object
     * @return publisher
     */
    public String getPublisher(){
        return this.publisher;
    }

    /**
     * Sets the publisher or distributor for the cited object
     * @param publisher
     */
    public void setPublisher(String publisher){
        this.publisher = publisher;
    }

    /**
     * Gets the locator, identifier or distribution medium for the cited object
     * @return locator
     */
    public String getLocator(){
        return this.locator;
    }

    /**
     * Sets the locator, identifier or distribution medium for the cited object
     * @param locator
     */
    public void setLocator(String locator){
        this.locator = locator;
    }

    /**
     * Gets the resource type for the cited object
     * @return resourceType
     */
    public String getResourceType(){
        return this.resourceType;
    }

    /**
     * Sets the resource type for the cited object
     * @param resourceType
     */
    public void setResourceType(String resourceType){
        this.resourceType = resourceType;
    }

    /**
     * Gets the access date and time for the cited object
     * @return accessDate
     */
    public DateTime getAccessDate(){
        return this.accessDate;
    }

    /**
     *  Sets the access date and time for the cited object
     * @param accessDate
     */
    public void setAccessDate(DateTime accessDate){
        this.accessDate = accessDate;
    }
    
    /**
     * Gets the most recent update date and time for the cited object
     * @return updatedDate
     */
    public DateTime getUpdatedDate(){
        return this.updatedDate;
    }

    /**
     *  Sets the updated date and time for the cited object
     * @param updatedDate
     */
    public void setUpdatedDate(DateTime updatedDate){
        this.updatedDate = updatedDate;
    }    
}