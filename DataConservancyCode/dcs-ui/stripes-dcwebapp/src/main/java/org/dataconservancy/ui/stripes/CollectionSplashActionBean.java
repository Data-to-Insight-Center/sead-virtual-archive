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
package org.dataconservancy.ui.stripes;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Citation;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ESIPCitationFormatterImpl;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.CitationService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.MetadataFileBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.util.ArchiveSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.dataconservancy.ui.stripes.MessageKey.*;

@UrlBinding("/collection/collection_splash.action")
public class CollectionSplashActionBean extends BaseActionBean {
    /**
     * The path to the collection splash JSP.
     */
    private final static String COLLECTION_SPLASH_PATH = "/pages/collection_splash.jsp";

    private String collectionId;

    private Collection collection;
    private Project project;
    
    private String citationString;

    private ArchiveService archiveService;
    private RelationshipService relationshipService;
    private CitationService citationService;
    private CollectionBizService collectionBizService;
    private MetadataFileBizService metadataFileBizService;

    public CollectionSplashActionBean() {
        super();

        try {
            assert(messageKeys.containsKey(MSG_KEY_EMPTY_OR_INVALID_ID));
            assert(messageKeys.containsKey(MSG_KEY_COLLECTION_DOES_NOT_EXIST));
            assert(messageKeys.containsKey(MSG_KEY_ERROR_RETRIEVING_PROJECT_FOR_COLLECTION));
        }
        catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                + MSG_KEY_EMPTY_OR_INVALID_ID + ", "
                + MSG_KEY_COLLECTION_DOES_NOT_EXIST + ", or"
                + MSG_KEY_ERROR_RETRIEVING_PROJECT_FOR_COLLECTION
                + " is missing");
        }
    }

    @DefaultHandler
    public Resolution render() throws BizInternalException, BizPolicyException {
        //Check to see if the collection id was supplied and non-empty
        if (null == collectionId || collectionId.isEmpty()) {
            return new ErrorResolution(404, messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID));
        }

        //Poll the archive - makes sure that the deposit info is up to date
        try {
            archiveService.pollArchive();
        } catch (ArchiveServiceException e) {
            return new ErrorResolution(500, String.format(messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), e.getMessage()));
        }

        //Get the deposit info for the collectionId
        List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(collectionId, ArchiveDepositInfo.Status.DEPOSITED);
        try {
            collection = collectionBizService.getCollection(collectionId, getAuthenticatedUser());
        } catch (BizInternalException e) {
            return new ErrorResolution(500, String.format(messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), e.getMessage()));
        }

        if (collection == null) {
            return new ErrorResolution(404, String.format(messageKeys.getProperty(MSG_KEY_COLLECTION_DOES_NOT_EXIST), collectionId));
        }
        //Get the project for the collection
        try {
            project = relationshipService.getProjectForCollection(collection);
        } catch (RelationshipConstraintException e) {
            return new ErrorResolution(500, messageKeys.getProperty(MSG_KEY_ERROR_RETRIEVING_PROJECT_FOR_COLLECTION));
        }

        //Get the citation from the citation service and convert it to an
        // HTML string for output to the JSP.
        Citation citation = citationService.createCitation(collection);
        ESIPCitationFormatterImpl citationFormatter = new ESIPCitationFormatterImpl();
        citationString =  citationFormatter.formatHtml(citation);

        //Send the user to the splash page
        return new ForwardResolution(COLLECTION_SPLASH_PATH);
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public Collection getCollection() {
        return collection;
    }

    public Project getProject() {
        return project;
    }
    
    public String getCitation() {
        return citationString;
    }

    public String getCollectionPublicationDate() {
        if (null == collection.getPublicationDate()) {
            return null;
        }
        return collection.getPublicationDate().toLocalDate().toString();
    }

   public List<MetadataFile> getCollectionMetadataFiles() throws CollectionException, BizInternalException,
            BizPolicyException, ArchiveServiceException, RelationshipException {
        List<MetadataFile> files = new ArrayList<MetadataFile>();
        Collection col = getCollection();
        for (String id : relationshipService.getMetadataFileIdsForBusinessObjectId(col.getId())){
            MetadataFile file = metadataFileBizService.retrieveMetadataFile(id);
            if (file != null) {
                files.add(file);
            }
        }
        return files;
   }

    @Override
    public String getPageTitle() {
        return String.format(super.getPageTitle(), collection.getTitle());
    }
    
    @SpringBean("archiveService")
    public void injectArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @SpringBean("relationshipService")
    public void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }
    
    @SpringBean("citationService")
    public void injectCitationService(CitationService citationService) {
        this.citationService = citationService;
    }
    @SpringBean("collectionBizService")
    public void injectCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }

    @SpringBean("metadataFileBizService")
    public void injectMetadataFileBizService(MetadataFileBizService metadataFileBizService){
        this.metadataFileBizService = metadataFileBizService;
    }
}
