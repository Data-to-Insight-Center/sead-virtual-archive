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
package org.dataconservancy.ui.services;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.packaging.ingest.api.IngestWorkflowState;
import org.dataconservancy.packaging.ingest.api.Package;
import org.dataconservancy.packaging.ingest.api.StatefulIngestServiceException;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.DataItemBizService;
import org.dataconservancy.ui.services.MetadataFileBizService;
import org.dataconservancy.ui.services.ProjectBizService;
import org.springframework.beans.factory.annotation.Required;

/**
 * This service ensures that all the external business objects referenced in the ReM can be de-referenced.
 */
//TODO: Refactor this to use the http apis to check for existence of business objects.
public class ExternalReferenceChecker extends BaseReferenceChecker {
    
    
    private CollectionBizService collectionBizService;
    private ProjectBizService projectBizService;
    private DataItemBizService dataItemBizService;
    private MetadataFileBizService metadataFileBizService;
    private IdService idService;
    
    /**
     * @param projectBizService
     *            the projectBizService to set
     */
    @Required
    public void setProjectBizService(ProjectBizService projectBizService) {
        this.projectBizService = projectBizService;
    }
    
    /**
     * @param dataItemBizService
     *            the dataItemBizService to set
     */
    @Required
    public void setDataItemBizService(DataItemBizService dataItemBizService) {
        this.dataItemBizService = dataItemBizService;
    }
    
    /**
     * @param collectionBizService
     *            the collectionBizService to set
     */
    @Required
    public void setCollectionBizService(CollectionBizService collectionBizService) {
        this.collectionBizService = collectionBizService;
    }

    /**
     * @param metadataFileBizService
     *            the metadataFileBizService to set
     */
    public void setMetadataFileBizService(MetadataFileBizService metadataFileBizService) {
        this.metadataFileBizService = metadataFileBizService;
    }
    
    @Required
    public void setIdService(IdService idService) {
        this.idService = idService;
    }  

    @Override
    public void execute(String depositId, IngestWorkflowState state) throws StatefulIngestServiceException {
        super.execute(depositId, state);
        
        if (!checkPackageReferences(depositId, state)) {
            return;
        }
        
        if (!checkProjectReferences(depositId, state)) {
            return;
        }
        
        if (!checkCollectionReferences(depositId, state)) {
            return;
        }
        
        if (!checkDataItemReferences(depositId, state)) {
            return;
        }
        
        if (!checkFileReferences(depositId, state)) {
            return;
        }
    }

    @Override
    protected boolean checkReferenceExists(String depositId,
                                           IngestWorkflowState state,
                                           String reference,
                                           String expectedReferenceType) {
        boolean found = false;
        
        /*This service only handles references to external objects.
        * So any references that don't start with http should be passed through. These will be checked by the internal
        * reference checker.*/
        if (!reference.startsWith("http:")) {
            return true;
        }
        
        try {
            if (expectedReferenceType.equalsIgnoreCase("project")) {
                found = checkProjectExists(reference);
            } else if (expectedReferenceType.equalsIgnoreCase("collection")) {
                found = checkCollectionExists(reference);
            } else if (expectedReferenceType.equalsIgnoreCase("dataItem")) {
                found = checkDataItemExists(reference);
            } else if (expectedReferenceType.equalsIgnoreCase("file")) {
                found = checkFileExists(reference);
            } else if (expectedReferenceType.equalsIgnoreCase("collection|project")) {
                if (idService.fromUrl(new URL(reference)).getType() == Types.COLLECTION.getTypeName()) {
                    found = checkCollectionExists(reference);
                } else {
                    found = checkProjectExists(reference);
                }
            }
            
            if (!found) {
                DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                String stackTrace = "";
                for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                    stackTrace += st + "\n";
                }
                event.setDetail(stackTrace);
                event.setOutcome("Package Missing external reference: " + reference);
                List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                DcsEntityReference ref = new DcsEntityReference(reference);
                refs.add(ref);
                
                event.setTargets(refs);
                
                state.getEventManager().addEvent(depositId, event);
                return false;
            }
        }
        catch (Exception e) {
            DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
            event.setDetail(e.getStackTrace().toString());
            event.setOutcome("Failed to validate external reference: " + reference + " " + e.getMessage());
            List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
            DcsEntityReference ref = new DcsEntityReference(reference);
            refs.add(ref);
            
            event.setTargets(refs);
            
            state.getEventManager().addEvent(depositId, event);
            return false;
        }
        
        return found;
    }

    @Override
    protected boolean checkMetadataReferenceExists(String depositId,
                                                   IngestWorkflowState state,
                                                   String reference) {
        try {
            if (!checkProjectExists(reference)) {
                if (!checkCollectionExists(reference)) {
                    if (!checkDataItemExists(reference)) {
                        DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
                        String stackTrace = "";
                        for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                            stackTrace += st + "\n";
                        }
                        event.setDetail(stackTrace);
                        event.setOutcome("Package Missing external reference: " + reference);
                        List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
                        DcsEntityReference ref = new DcsEntityReference(reference);
                        refs.add(ref);
                        
                        event.setTargets(refs);
                        
                        state.getEventManager().addEvent(depositId, event);
                        return false;
                    }
                }
            }
          
        }
        catch (Exception e) {
            DcsEvent event = state.getEventManager().newEvent(Package.Events.INGEST_FAIL);
            event.setDetail(e.getStackTrace().toString());
            event.setOutcome("Failed to validate external reference: " + reference + " " + e.getMessage());
            List<DcsEntityReference> refs = new ArrayList<DcsEntityReference>();
            DcsEntityReference ref = new DcsEntityReference(reference);
            refs.add(ref);
            
            event.setTargets(refs);
            
            state.getEventManager().addEvent(depositId, event);
            return false;
        }
        
        return true;
    }
    
    /*TODO: These can be refactored into one method that performs and http get on the id. 
    * The user will need to be added and retrieved from the state in this instance.
    * A return of 200 will mean the object is found, 400 not found, anything else should throw an exception. 
    */ 
    private boolean checkProjectExists(String id) throws BizPolicyException {
        boolean found = false;
        Project project = projectBizService.getProject(id);
        if (project != null) {
            found = true;
        }
        
        return found;
    }
    
    private boolean checkCollectionExists(String id) throws BizInternalException, BizPolicyException {
        boolean found = false;
        Collection collection = collectionBizService.getCollection(id);
        if (collection != null) {
            found = true;
        }
        
        return found;
    }
    
    private boolean checkDataItemExists(String id) throws ArchiveServiceException, BizPolicyException {
        boolean found = false;
        DataItem dataItem = dataItemBizService.getDataItem(id);
        if (dataItem != null) {
            found = true;
        }
        
        return found;
    }
    
    private boolean checkFileExists(String id) throws ArchiveServiceException {
        boolean found = false;
        MetadataFile file = metadataFileBizService.retrieveMetadataFile(id);
        if (file != null) {
            found = true;
        }
        
        return found;
    }
    
}
