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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.ui.dao.ArchiveDepositInfoDAO;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.BusinessObject;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.dataconservancy.ui.util.ContentTypeUtil;
import org.joda.time.DateTime;



/**
 * Service to handle metadata files in the UI. This service is the main entry point for validating, indexing and archiving metadata files.
 *
 */
public class MetadataFileBizServiceImpl implements MetadataFileBizService {
    private final ArchiveService archiveService;
    private AuthorizationService authorizationService;
    private RelationshipService relationshipService;
    private ProjectBizService projectBizService;
    private ArchiveDepositInfoDAO archiveDepositInfoDao;
    
    public MetadataFileBizServiceImpl(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    public void addNewMetadataFile(BusinessObject parent, MetadataFile file, Person user) throws BizInternalException, BizPolicyException {
        file.setParentId(parent.getId());// make sure the parent id is set on the metadata file
        boolean canUpdate = userCanUpdateMetadataFileForBusinessObject(parent, file, user);
        if (!canUpdate){
            if (user == null) {
                throw new BizPolicyException("Please log in to update to the given collection or project", BizPolicyException.Type.AUTHENTICATION_ERROR);
            }
            else {
                throw new BizPolicyException("This user is not authorized to update to the given collection or project", BizPolicyException.Type.AUTHORIZATION_ERROR);
            }
        }

        //TODO Call mhf to validate file
        String parentDepositId = "";
        if (parent instanceof Collection || parent instanceof DataItem || parent instanceof MetadataFile) {
            List<ArchiveDepositInfo> adi = archiveService.listDepositInfo(parent.getId(), ArchiveDepositInfo.Status.DEPOSITED);
            if (adi != null && adi.size() > 0) {
                parentDepositId = adi.get(0).getDepositId();
            } else {
                throw new BizInternalException("Failed to find Metadata File parent deposit id for: " + parent.getId());
            }
            
            if (parentDepositId.isEmpty()) {
                throw new BizInternalException("Error getting deposit id for Data File: " + parent.getId());
            }
            
        } else if (parent instanceof DataFile) {
            //If we have a Data File we need to get the containing DataItem and then look at it's children ADIs to find the one for the file we want.
            try {
                DataItem containingDataItem = relationshipService.getDataSetForDataFile((DataFile)parent);
                //This could be an empty data item so check that it has an id.
                if (containingDataItem.getId() != null && !containingDataItem.getId().isEmpty()) {
                    List<ArchiveDepositInfo> dataItemAdi = archiveService.listDepositInfo(containingDataItem.getId(), ArchiveDepositInfo.Status.DEPOSITED);
                    if (dataItemAdi != null && dataItemAdi.size() > 0) {
                        List<ArchiveDepositInfo> fileAdis = archiveDepositInfoDao.lookupChildren(dataItemAdi.get(0).getDepositId());
                        if (fileAdis != null) {
                            for (ArchiveDepositInfo fileAdi : fileAdis) {
                                if (fileAdi.getObjectId().equals(parent.getId())) {
                                    parentDepositId = fileAdi.getDepositId();
                                    break;
                                }
                            }
                        }
                    } else {
                        throw new BizInternalException("Error getting deposit id for Data Item: " + containingDataItem.getId());
                    }           
                    if (parentDepositId.isEmpty()) {
                        throw new BizInternalException("Error getting deposit id for Data File: " + parent.getId());
                    }                    
                } else {
                    throw new BizInternalException("Error getting parent Data Item for Data File: " + parent.getId());
                }                
            } catch (RelationshipConstraintException e) {
                throw new BizInternalException("Error retrieving DataItem containing data file.", e);
            }
        }
        try {
            archiveService.deposit(parentDepositId, file);
            relationshipService.addMetadataFileToBusinessObject(file.getId(), parent.getId());
        } catch(ArchiveServiceException ase) {
            throw new BizInternalException("Error depositing metadata file with ID: " + file.getId()
                    + " and Name: " + file.getName(), ase);
        } catch (RelationshipConstraintException e) {
             throw new BizInternalException("Error adding HAS_METADATA_FILE relationship between business object with ID: " + parent.getId()
                    + " and metadata file with ID: " + file.getId(), e);
        }
    }

    public void removeMetadataFile(BusinessObject parent, MetadataFile file, Person user) throws BizPolicyException, BizInternalException {
        boolean canUpdate = userCanUpdateMetadataFileForBusinessObject(parent, file, user);
        if (!canUpdate){
            if (user == null) {
                throw new BizPolicyException("Please log in to update to the given collection or project", BizPolicyException.Type.AUTHENTICATION_ERROR);
            }
            else {
                throw new BizPolicyException("This user is not authorized to update to the given collection or project", BizPolicyException.Type.AUTHORIZATION_ERROR);
            }
        }
        relationshipService.removeMetadataFileFromBusinessObject(file.getId(), parent.getId());
    }

    private boolean userCanUpdateMetadataFileForBusinessObject(BusinessObject parent, MetadataFile file, Person user) throws BizInternalException, BizPolicyException {
        Boolean canUpdate = false;

        //here we try to establish the authorization for the user to update the appropriate business object. this is
        //the parent unless the parent does not have permissions on it - we go up the graph until we can settle the
        //authorization question. this functionality can go into authorization service once we settle how to handle that

        if(parent instanceof Project){
            canUpdate = authorizationService.canUpdateProject(user, (Project) parent);
        } else if (parent instanceof Collection) {
            try{
                canUpdate = authorizationService.canUpdateCollection(user, (Collection)parent);
            } catch (RelationshipConstraintException rce) {
                throw new BizInternalException("Relationship Constraint Exception thrown while authorizing " +
                        user.getId() + " to update Collection " +  parent.getId(), rce);
            }
        } else if (parent instanceof DataItem){
            try {
                Collection collection = relationshipService.getCollectionForDataSet((DataItem) parent);
                canUpdate = authorizationService.canDepositToCollection(user, collection);
            } catch (RelationshipConstraintException rce) {
                throw new BizInternalException("Relationship Constraint Exception thrown while authorizing " +
                        user.getId() + " to update DataItem " +  parent.getId(), rce);
            }

        } else if (parent instanceof MetadataFile){
            //we need to resolve this metadata file up the graph until we find a business object with authz set
            String parentId = parent.getId();
            List<ArchiveDepositInfo> adiList;
            ArchiveDepositInfo adi;
            ArchiveDepositInfo.Type type;
            Collection collection = null;
            boolean done = false;

            adiList = archiveService.listDepositInfo(parentId, Status.DEPOSITED);

            while(!done){
                if(adiList != null && adiList.size()>0) {//have a parent id pointing to some first-class archive object - what is it?
                    adi = adiList.get(0);
                    parentId = adi.getObjectId();
                    type = adi.getObjectType();
                    if(type.equals(ArchiveDepositInfo.Type.METADATA_FILE)){
                        try{
                            parentId = retrieveMetadataFile(parentId).getParentId();
                            adiList = archiveService.listDepositInfo(parentId, Status.DEPOSITED);//not done yet, go around again
                        } catch (ArchiveServiceException e) {
                            throw(new BizInternalException("Archive Service Exception thrown retrieving parent MetadataFile " + parentId + " for " +
                                    " MetadataFile " + file.getId() + " on behalf of user " + user, e));
                        }
                    } else if(type.equals(ArchiveDepositInfo.Type.DATA_FILE)) {
                          try{
                              DataItem dataItem = relationshipService.getDataSetForDataFileId(parentId);
                              collection = relationshipService.getCollectionForDataSet(dataItem);
                              canUpdate = authorizationService.canDepositToCollection(user, collection);
                              done = true;
                          } catch (RelationshipConstraintException e) {
                              throw (new BizInternalException("Relationship Constraint Exception thrown while authorizing "  +
                                      user.getId() + " to update DataItem" +  parent.getId(), e));
                          } 
                    } else if(type.equals(ArchiveDepositInfo.Type.DATASET)) {
                        try{
                            DataItem dataItem = retrieveDataSet(parentId);
                            collection = relationshipService.getCollectionForDataSet(dataItem);
                            canUpdate = authorizationService.canDepositToCollection(user, collection);
                            done = true;
                        } catch (ArchiveServiceException e) {
                            throw (new BizInternalException("Archive Service Exception thrown retrieving parent DataItem " + parentId + " for " +
                                    " MetadataFile " + file.getId() + " on behalf of user "  + user, e));
                        } catch (RelationshipConstraintException e) {
                            throw (new BizInternalException("Relationship Constraint Exception thrown while authorizing "  +
                                    user.getId() + " to update DataItem" +  parent.getId(), e));
                        }
                    } else if(type.equals(ArchiveDepositInfo.Type.COLLECTION)) {
                        try{
                            collection = retrieveCollection(parentId);
                            canUpdate = authorizationService.canDepositToCollection(user, collection);
                            done = true;
                        } catch (ArchiveServiceException e) {
                            throw (new BizInternalException("Archive Service Exception thrown retrieving parent Collection " + parentId + " for " +
                                    " MetadataFile " + file.getId() + " on behalf of user "  + user, e));
                        } catch (RelationshipConstraintException e) {
                            throw (new BizInternalException("Relationship Constraint Exception thrown while authorizing "  +
                                    user.getId() + " to update Collection" +  parent.getId(), e));
                        }
                    }
                } else { //adiList has nothing
                    //this could be a Project or a DataFile - these do not have adiLists
                    Project project = projectBizService.getProject(parentId);
                    if (project != null){
                        canUpdate = authorizationService.canUpdateProject(user, project);
                        done = true;
                    } else {
                        try{
                            DataItem dataItem = relationshipService.getDataSetForDataFileId(parentId);
                            if(dataItem != null){
                                collection = relationshipService.getCollectionForDataSet(dataItem);
                                canUpdate = authorizationService.canDepositToCollection(user, collection);
                                done = true;
                            } else {//id did not correspond to anything either inside or outside of the archive
                                //authorization question can't be settled. get out of the loop, canUpdate is false
                                done = true;
                            }
                        } catch (RelationshipConstraintException rce){
                            throw new BizInternalException("Relationship Constraint Exception thrown while authorizing " +
                                    user.getId() + " to update DataFile" +  parent.getId(), rce);
                        }
                    }
                }
            }
        } else if (parent instanceof DataFile){//this must come after MetadataFile since MetadataFile extends DataFile
            try {
                DataItem dataItem = relationshipService.getDataSetForDataFile((DataFile) parent);
                Collection collection = relationshipService.getCollectionForDataSet(dataItem);
                canUpdate = authorizationService.canDepositToCollection(user, collection);
            } catch (RelationshipConstraintException e) {
                throw  (new BizInternalException("Relationship Constraint Exception thrown while authorizing "  +
                        user.getId() + " to update DataFile" +  parent.getId(), e));
            }
        }
        return canUpdate;
    }
    
    public MetadataFile retrieveMetadataFile(String id) throws ArchiveServiceException {
        MetadataFile mf = null;
        archiveService.pollArchive();
       
        List<ArchiveDepositInfo> info = archiveService.listDepositInfo(id, Status.DEPOSITED);
            
        if (!info.isEmpty()) {
            ArchiveSearchResult<MetadataFile> sr = null;
            final ArchiveDepositInfo mdfAdi = info.get(0);
            if (mdfAdi.getObjectType() != ArchiveDepositInfo.Type.METADATA_FILE) {
                // the id didn't specify a metadata file.
                return mf;
            }
            sr = archiveService
                          .retrieveMetadataFile(mdfAdi.getDepositId());
            
            if (sr != null && sr.getResults().iterator().hasNext()) {
                mf = sr.getResults().iterator().next();
            }
        }
        return mf;
    }

    private DataItem retrieveDataSet(String id) throws ArchiveServiceException {
        DataItem di = null;
        archiveService.pollArchive();

        List<ArchiveDepositInfo> info = archiveService.listDepositInfo(id, Status.DEPOSITED);

        if (!info.isEmpty()) {
            ArchiveSearchResult<DataItem> sr = null;
            sr = archiveService
                    .retrieveDataSet(info.get(0).getDepositId());

            if (sr != null && sr.getResults().iterator().hasNext()) {
                di = sr.getResults().iterator().next();
            }
        }
        return di;
    }

    private Collection retrieveCollection(String id) throws ArchiveServiceException {
        Collection col = null;
        archiveService.pollArchive();

        List<ArchiveDepositInfo> info = archiveService.listDepositInfo(id, Status.DEPOSITED);

        if (!info.isEmpty()) {
            ArchiveSearchResult<Collection> sr = null;
            sr = archiveService
                    .retrieveCollection(info.get(0).getDepositId());

            if (sr != null && sr.getResults().iterator().hasNext()) {
                col = sr.getResults().iterator().next();
            }
        }
        return col;
    }


    public DateTime getLastModifiedDate(String id) {
        DateTime lastModifiedDate = null;

        if (id != null && !id.isEmpty()) {
            List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(id, Status.DEPOSITED);
            if (!depositInfo.isEmpty()) {
                if (depositInfo != null && !depositInfo.isEmpty()) {
                    lastModifiedDate = depositInfo.get(0).getDepositDateTime();
                }
            }
        }

        return lastModifiedDate;
    }

    public String getMimeType(File file) throws IOException {
        String contentType = ContentTypeUtil.detectMimeType(file);
        return contentType;
    }

    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public void setRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    public void setProjectBizService(ProjectBizService projectBizService) {
        this.projectBizService = projectBizService;
    }
    
    public void setArchiveDepositInfoDao(ArchiveDepositInfoDAO archiveDepositInfoDao) {
        this.archiveDepositInfoDao = archiveDepositInfoDao;
    }

}