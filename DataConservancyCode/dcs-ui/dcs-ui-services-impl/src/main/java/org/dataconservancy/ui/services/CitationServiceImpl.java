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
package org.dataconservancy.ui.services;

import org.dataconservancy.ui.model.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of CitationService.
 */
public class CitationServiceImpl implements CitationService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Citation citation;
    private ArchiveService archiveService;
    private RelationshipService relationshipService;
    private DateTime publicationDate;
    private DateTime archiveUpdatedDate;

    private List<PersonName> creators;
    private String title;
    private String version;
    private String publisher;
    private String citableLocator;
    private String resourceType;
    private DateTime releaseDate;
    private DateTime updatedDate;
    private DateTime accessDate;

    public CitationServiceImpl(ArchiveService archiveService, RelationshipService relationshipService){
        if (archiveService == null) {
            throw new IllegalArgumentException("archiveService must not be null.");
        }
        this.archiveService = archiveService;
        if (relationshipService == null) {
            throw new IllegalArgumentException("relationshipService must not be null.");
        }
        this.relationshipService = relationshipService;
 
    }
    
    public Citation createCitation(Collection collection){
        // for collections we will not need the optional resourceType element
        DateTime archivePublicationDate = null;
        try{
            List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(collection.getId(), ArchiveDepositInfo.Status.DEPOSITED);
            archiveUpdatedDate = infoList.get(0).getDepositDateTime();
            archivePublicationDate = infoList.get(infoList.size()-1).getDepositDateTime();
            publicationDate = collection.getPublicationDate();
            Project project = relationshipService.getProjectForCollection(collection);
            publisher = project.getPublisher();
        } catch (RelationshipConstraintException e) {
            log.error("Error getting project for collection.", e);
        } catch (IndexOutOfBoundsException e) {
            log.error("ArchiveDepositInfo list was empty", e);
        }

        creators = (collection.getCreators() != null && !collection.getCreators().isEmpty()) ? collection.getCreators() : getDefaultCreators();
        title = (collection.getTitle() != null && !collection.getTitle().isEmpty()) ? collection.getTitle() : CitationFormatter.DEFAULT_TITLE;
        //TODO: need to get version from a service, can't be on collection
        version = CitationFormatter.DEFAULT_VERSION;
        citableLocator = (collection.getCitableLocator() != null && !collection.getCitableLocator().isEmpty()) ? collection.getCitableLocator() : CitationFormatter.DEFAULT_LOCATOR;
        //take supplied publication date; if there is none, take date of first deposit
        releaseDate= publicationDate != null ? publicationDate : archivePublicationDate;
        updatedDate=archiveUpdatedDate != null ? archiveUpdatedDate : null;
        accessDate=DateTime.now();
        resourceType=null; //collections don't have a resource type

        Citation citation = new Citation(creators, releaseDate, title, version, publisher, resourceType, citableLocator, accessDate, updatedDate);
        return citation;
    }

     private List<PersonName> getDefaultCreators(){
        List<PersonName> defaultCreators = new ArrayList<PersonName>();
        PersonName defaultPersonName = new PersonName("", "", "", CitationFormatter.DEFAULT_CREATOR, "");
        defaultCreators.add(defaultPersonName);
        return defaultCreators;
    }
    
}
