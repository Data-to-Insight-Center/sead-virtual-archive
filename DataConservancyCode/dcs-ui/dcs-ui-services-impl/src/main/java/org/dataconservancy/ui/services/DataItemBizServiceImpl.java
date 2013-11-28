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

import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.util.ArchiveSearchResult;

/**
 * Implementation of the DataItem business service which retrieves DataItems
 * from an archive.
 */
public class DataItemBizServiceImpl implements DataItemBizService {
    private final ArchiveService archiveService;
    private final AuthorizationService authService;
    private RelationshipService relationshipService;
    private CollectionBizService collectionBizService;
    private UserService userService;
    
    public DataItemBizServiceImpl(ArchiveService archiveService,
            AuthorizationService authService) {
        this.archiveService = archiveService;
        this.authService = authService;
    }

    @Override
    public List<DataItem> getDataItemVersions(String business_id, Person user)
            throws ArchiveServiceException, BizPolicyException {
        return get_versions(business_id, user);
    }

    private List<DataItem> get_versions(String business_id, Person user)
            throws ArchiveServiceException, BizPolicyException {

        /*if (user == null) {
            throw new BizPolicyException(
                    "You must be logged in to access files.",
                    Type.AUTHENTICATION_ERROR);
        }*/

        List<DataItem> result = new ArrayList<DataItem>();

        for (ArchiveDepositInfo info : archiveService.listDepositInfo(
                business_id, Status.DEPOSITED)) {

            // TODO: Only one DataItem can be matched by deposit id. Should
            // refactor ArchiveService.
            ArchiveSearchResult<DataItem> sr = archiveService
                    .retrieveDataSet(info.getDepositId());

            for (DataItem ds : sr.getResults()) {
                if (!authService.canRetrieveDataSet(user, ds)) {
                    throw new BizPolicyException(
                            "This user is not authorized to retrieve DataItem: "
                                    + ds, Type.AUTHORIZATION_ERROR);
                }

                result.add(ds);
            }
        }

        return result;
    }

    @Override
    public DataItem getDataItem(String business_id, Person user)
            throws ArchiveServiceException, BizPolicyException {
        List<DataItem> versions = get_versions(business_id, user);

        if (versions.size() == 0) {
            return null;
        }

        return versions.get(0);
    }
    
    @Override
    public DataItem getDataItem(String businessId) throws ArchiveServiceException, BizPolicyException {
        List<DataItem> result = new ArrayList<DataItem>();
        
        for (ArchiveDepositInfo info : archiveService.listDepositInfo(businessId, Status.DEPOSITED)) {
            
            ArchiveSearchResult<DataItem> sr = archiveService.retrieveDataSet(info.getDepositId());
            
            for (DataItem ds : sr.getResults()) {
                result.add(ds);
            }
        }
        
        if (result.size() == 0) {
            return null;
        }
        else {
            // returning the first version of the DataItem.
            return result.get(0);
        }
    }

    @Override
    public String addDataItem(DataItem item, String collectionId, Person user)
            throws ArchiveServiceException, BizPolicyException,
            BizInternalException {
        
        Collection parentCollection = collectionBizService.getCollection(collectionId);
        if (parentCollection == null) {
            throw new BizInternalException("Collection " + collectionId + " could not be found.");
        }
        
        if (user == null) {
            throw new BizPolicyException("User must not be null to add a new data item", BizPolicyException.Type.AUTHENTICATION_ERROR);
        }
        
        try {
            if (!authService.canDepositToCollection(user, parentCollection)) {
                throw new BizPolicyException("User: " + user + "doesn't have permission to deposit to collection " + collectionId, 
                                             BizPolicyException.Type.AUTHORIZATION_ERROR);
            }
        } catch (RelationshipConstraintException e) {
            throw new BizInternalException(e);
        }
        
        final String parentDepositId = archiveService
                .listDepositInfo(collectionId, ArchiveDepositInfo.Status.DEPOSITED).get(0).getDepositId();
       
        try {
            // Add the relationships to the collection and data files
            relationshipService.addDataSetToCollection(item, parentCollection);
            // After relationship is inserted, deposit the Data Item
            archiveService.deposit(parentDepositId, item);
            relationshipService.updateDataFileRelationshipForDataSet(item);
        }
        catch (RelationshipConstraintException e) {            
            relationshipService.removeDataSetFromCollection(item, parentCollection);
            for (DataFile dataFile : item.getFiles()) {
                relationshipService.removeDataFileFromDataSet((DataFile) dataFile, item);
            }
            throw new BizInternalException(e);
        }
        catch (ArchiveServiceException e) {
            // if deposit to archive fail, remove the relationship, log and throw exception
            relationshipService.removeDataSetFromCollection(item, parentCollection);
            for (DataFile dataFile : item.getFiles()) {
                relationshipService.removeDataFileFromDataSet((DataFile) dataFile, item);
            }           
            throw e;
        }
        
        return item.getId();
    }
    
    @Override
    public DataFile getDataFile(String businessId)
            throws ArchiveServiceException, BizInternalException {
        DataFile expectedFile = null;
        if (businessId != null) {
            try {
                DataItem containingItem = relationshipService.getDataSetForDataFileId(businessId);
                for (DataFile file : containingItem.getFiles()) {
                    if (file.getId().equals(businessId)) {
                        expectedFile = file;
                        break;
                    }
                }
            } catch (RelationshipConstraintException e) {
                throw new BizInternalException("Error retrieving data item for data file id: " + businessId, e);
            }
        }
        return expectedFile;
    }
    
    public void setRelationshipService(RelationshipService relationshipService){
        this.relationshipService = relationshipService;
    }

    public void setCollectionBizService(CollectionBizService collectionBizService){
        this.collectionBizService = collectionBizService;
    }

    public void setUserService(UserService userService){
        this.userService = userService;
    }
}
