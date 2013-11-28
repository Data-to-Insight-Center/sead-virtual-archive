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

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.*;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.dataconservancy.ui.util.UserVerificationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Implementation of the CollectionBizService interface
 * 
 */
public class CollectionBizServiceImpl implements CollectionBizService {

    private AuthorizationService authorizationService;
    private ArchiveService archiveService;
    private RelationshipService relationshipService;
    private ProjectBizService projectBizService;
    private UserService userService;

    //A pair of constants controlling how long the service should poll for a
    // collection
    private static final int POLL_DELAY_FACTOR_MS = 500;
    private static final int POLL_COUNT = 20;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Given an existing {@link org.dataconservancy.ui.model.Collection} update it content to the collection with matching business id.
     * The given user's credentials will be used to authenticate and authorize the request.  Collections are currently
     * updatable only by instance admins and containing project admins; this is managed in the AuthorizationService call.
     * 
     * @param collection
     * @param user
     * @return collection id
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    public String updateCollection(Collection collection, Person user) throws BizPolicyException, BizInternalException {

        //make sure collection has required fields - the boolean set to true indicates this is an
        //update, so it should have an id already
        validateCollection(collection, true);

        //make sure we get the representation of the user that is in the system - no doctored roles
        user = UserVerificationUtil.VerifyUser(userService, user);
        
        Boolean canUpdate = false;
        try{           
           canUpdate = authorizationService.canUpdateCollection(user, collection);            
        } catch (RelationshipConstraintException rce) {
            throw new BizInternalException("Relationship Constraint Exception thrown while authorizing "
                   + user.getId() + " to updateCollection()" +  collection.getId(), rce);
        }
        if (!canUpdate){
            if (user == null) {
                throw new BizPolicyException("Please log in to update to the given collection", BizPolicyException.Type.AUTHENTICATION_ERROR);
            }
            else {
                throw new BizPolicyException("This user is not authorized to update to the given collection", BizPolicyException.Type.AUTHORIZATION_ERROR);
            }
        }

        collection.setDepositorId(user.getId());
        collection.setDepositDate(new DateTime());
        collection.setContactInfoList(compactifyList(collection.getContactInfoList()));
        collection.setAlternateIds(compactifyList(collection.getAlternateIds()));
        collection.setCreators(compactifyList(collection.getCreators()));

        try{
            String depositId = archiveService.deposit(collection);

            //After the call to deposit, wait for the collection to be deposited
            Status collectionStatus = pollAndWaitForCollection(depositId);

            //It's possible that it timed out, or that it failed deposit
            if (collectionStatus == Status.PENDING) {
                throw new BizInternalException("Error depositing collection " + collection.getId() + ": archive timed out.");
            } else if (collectionStatus != Status.DEPOSITED) {
                throw new BizInternalException("Error depositing collection " + collection.getId() + ": archive deposit failed.");
            }

        } catch (ArchiveServiceException ase) {
            throw new BizInternalException("Error depositing collection " + collection.getId(), ase);
        }
        return collection.getId();
     }

    @Override
    public String createCollection(Collection collection, Person user) throws BizPolicyException, BizInternalException {
        //validate the collection being created
        validateCollection(collection, true);

        //make sure we get the representation of the user that is in the system - no doctored roles
        user = UserVerificationUtil.VerifyUser(userService, user);
        if (user == null) {
            throw new BizInternalException("Error occured when attempting to verify the user provided");
        }
        
        Project project = null;
        Collection parentCollection = null;
        
        if ((collection.getParentProjectId() == null || collection.getParentProjectId().isEmpty()) 
                && (collection.getParentId() == null || collection.getParentId().isEmpty())) {
            throw new BizInternalException("To add a collection, a parent project or collection must be specified.");
        }
        
        if (collection.getParentProjectId() != null && !collection.getParentProjectId().isEmpty()) {
            //ensure the identified project exists in the system
            project = projectBizService.getProject(collection.getParentProjectId(), user);
            if (project == null) {
                throw new BizInternalException("The identified project could not be found.");
            }
            
            //check authorization
            if (!authorizationService.canCreateCollection(user, project)) {
                throw new BizPolicyException
                        ("User does not have permission to create collection under the identified project",
                        BizPolicyException.Type.AUTHORIZATION_ERROR);
            }
        } 
        
        if (collection.getParentId() != null && !collection.getParentId().isEmpty()) {
            //Make sure parent collection exists
            parentCollection = getCollection(collection.getParentId(), user);
            if (parentCollection == null) {
                throw new BizInternalException("Super collection could not be found.");
            }

            try {
                //check authorization
                if (!authorizationService.canCreateCollection(user, parentCollection)) {
                    throw  new BizPolicyException
                            ("User does not have permission to create collection under the identified project",
                                    BizPolicyException.Type.AUTHORIZATION_ERROR);
                }
            } catch (RelationshipConstraintException e) {
                throw new BizInternalException("Error occurred when checking for CREATE permission of provided user. " + e.getMessage());
            }
            
            // Adding the child to parent collection and updating it.
            parentCollection.addChildId(collection.getId());
            updateCollection(parentCollection, user);
        }

        try {
            //Set collection deposit date
            collection.setDepositDate(new DateTime());
            //Set collection depositor
            collection.setDepositorId(user.getId());
            String depositId = archiveService.deposit(collection);
            // TODO: review this - comment arose from whether polling might someday be done in a separate thread
            // need to poll while not in production
            
            //After the call to deposit, wait for the collection to be deposited
            Status collectionStatus = pollAndWaitForCollection(depositId);


            //It's possible that it timed out, or that it failed deposit
            if (collectionStatus == Status.PENDING) {
                throw new BizInternalException("Error depositing collection " + collection.getId() + ": archive timed out.");
            } else if (collectionStatus != Status.DEPOSITED) {
                throw new BizInternalException("Error depositing collection " + collection.getId() + ": archive deposit failed.");
            }

            log.debug("Created collection with archive transaction (deposit) id: " + depositId
                    + " and business id: <" + collection.getId() + ">");
        }
        catch (ArchiveServiceException e) {
            log.warn("Could not deposit collection: " + e.getMessage(), e);
            throw new BizInternalException("Error occurred when attempting to deposit collection into the archive.");
        }

        try {
            //Update relevant relationships for the collection
            relationshipService.addDepositorToCollection(user, collection);
            
            //This only add root collections currently to maintain existing behavior we may want to change this in the future
            if (project != null && parentCollection == null) {
                relationshipService.addCollectionToProject(collection, project);
    
                // TODO: refactor getAdministratorsForProject(...) to use the Project business service.
                for (Person p : relationshipService.getAdministratorsForProject(project)) {
                    // If the depositor is also an administrator for the project, avoid creating a duplicate relationship
                    if (p.equals(user)) {
                        continue;
                    }
                    relationshipService.addDepositorToCollection(p, collection);
                }
            } 
            if (parentCollection != null) {
                //Update relevant relationships for the collection
                relationshipService.addSubCollectionToCollection(collection.getId(), parentCollection.getId());
                log.debug("Successfully updated relationships for collection with business id: " + collection.getId());
            }
            log.debug("Successfully updated relationships for collection with business id: " + collection.getId());
        }
        catch (RelationshipConstraintException e) {
            log.warn(
                    "Could not update collection relationships for Collection (" + collection.getId() + "): "
                            + e.getMessage(), e);
        }
        catch (RelationshipException e) {
            log.warn(
                     "Could not update collection relationships for Collection (" + collection.getId() + "): "
                             + e.getMessage(), e);
        }
        
        return collection.getId();
    }

    /**
     * Collections are currently publicly available so this method returns all collections in the archive regardless of user.
     * {@inheritDoc}
     * @throws ArchiveServiceException 
     */
    public Set<Collection> findByUser(Person user) throws BizPolicyException, BizInternalException {

        //Store results in a map so we cull out any duplicate collections(updates) and so we can ensure we get only the latest collection
        Map<String, Collection> results = new HashMap<String, Collection>();
        
        //make sure we get the representation of the user that is in the system - no doctored roles
        user = UserVerificationUtil.VerifyUser(userService, user);

        if (authorizationService.canViewCollectionList(user)) {
          
            List<String> archiveCollections = archiveService.listCollections(Status.DEPOSITED);
            if (archiveCollections != null && !archiveCollections.isEmpty()) {
                for (String collectionID : archiveCollections) {
                    try {
                        ArchiveSearchResult<Collection> result = archiveService.retrieveCollection(collectionID);
                       
                        if (result != null && result.getResultCount() > 0) {
                            Collection collection = result.getResults().iterator().next();
                            //If this is the first collection with this business ID at it to the map
                            if (!results.containsKey(collection.getId())) {
                                results.put(collection.getId(), collection);
                            } else { //Otherwise check if this is an update to what we already have an add it, otherwise it's ignored
                                if (results.get(collection.getId()).getDepositDate().isBefore(collection.getDepositDate())) {
                                    results.put(collection.getId(), collection);
                                }
                            }
                        }
                    } catch (ArchiveServiceException e) {
                        throw new BizInternalException("Error retrieving collection list for user " + user.getId() + " from the archive service", e);
                    }
                }
            }
        }
        
        Set<Collection> resultSet =  new HashSet<Collection>();
        
        if (!results.values().isEmpty()) {
            resultSet.addAll(results.values());
        }
        return resultSet;
    }
    

    public Collection getCollection(String id, Person user) throws BizPolicyException, BizInternalException {
        if(id == null){
            return null;
        }

        //make sure we get the representation of the user that is in the system - no doctored roles
        user = UserVerificationUtil.VerifyUser(userService, user);

        Collection collection = new Collection();
        Collection c = null;
        //attempt to get the collection object from the archive service
        try{
            List<ArchiveDepositInfo> archiveDepositInfo =
                    archiveService.listDepositInfo(id, ArchiveDepositInfo.Status.DEPOSITED);
            if (archiveDepositInfo != null && !archiveDepositInfo.isEmpty()) {
                ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(archiveDepositInfo.get(0).getDepositId());
                Iterator<Collection> resultIter = results.getResults().iterator();
                if (resultIter.hasNext()) {
                    c = resultIter.next();
                }
            }
        } catch (ArchiveServiceException e) {
            throw new BizInternalException("Error retrieving collection with id " + id + " from the archive service", e);
        }
        if (c != null) {
            collection = c;
        }  else {
            return null;
        }

        //if we have the collection, see if the user is authorized to retrieve it
        if (!authorizationService.canRetrieveCollection(user, collection)) {
            throw new BizPolicyException("This user is not authorized to retrieve the given collection", BizPolicyException.Type.AUTHORIZATION_ERROR);
        }

        return collection;
    }
    
    @Override
    public Collection getCollection(String id) throws BizInternalException {
        Collection collection = new Collection();
        Collection c = null;
        //attempt to get the collection object from the archive service
        try {
            List<ArchiveDepositInfo> archiveDepositInfo =
                    archiveService.listDepositInfo(id, ArchiveDepositInfo.Status.DEPOSITED);
            if (archiveDepositInfo != null && !archiveDepositInfo.isEmpty()) {
                ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(archiveDepositInfo.get(0).getDepositId());
                Iterator<Collection> resultIter = results.getResults().iterator();
                if (resultIter.hasNext()) {
                    c = resultIter.next();
                }
            }
        }
        catch (ArchiveServiceException e) {
            throw new BizInternalException("Error retrieving collection with id " + id + " from the archive service", e);
        }
        if (c != null) {
            collection = c;
            return collection;
        }  else {
            return null;
        }
    }
    
    private <T> List<T> compactifyList(List<T> list){
        //Check and cull empty list items
        if ( list != null && list.size() > 0) {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i) == null || list.get(i).toString()=="") {
                    list.remove(i);
                }
            }
        }
        return list;
    }

    //TODO:revisit this method to maybe remove isUpdate flag. as collection id is need for collection creation as well.
    //TODO: call of the createCollection would be responsible for minting the id.
    private void validateCollection(Collection collection, boolean isUpdate) throws BizPolicyException {
        if (collection == null) {
            throw new BizPolicyException("Collection cannot be null", BizPolicyException.Type.VALIDATION_ERROR);
        }
        if (isUpdate == true && (collection.getId() == null || collection.getId().isEmpty())) {
            throw new BizPolicyException("Collection's id cannot be empty or null.", BizPolicyException.Type.VALIDATION_ERROR);
        }
        if (collection.getTitle() == null || collection.getTitle().isEmpty()) {
            throw new BizPolicyException("Collection's name cannot be empty or null.", BizPolicyException.Type.VALIDATION_ERROR);
        }
        if (collection.getSummary() == null || collection.getSummary().isEmpty())  {
            throw new BizPolicyException("Collection's summary cannot be empty or null.", BizPolicyException.Type.VALIDATION_ERROR);
        }
    }
    
    public void setArchiveService(ArchiveService archiveService){
        this.archiveService = archiveService;
    }

    public void setAuthorizationService(AuthorizationService authorizationService){
        this.authorizationService = authorizationService;
    }
    
    public void setRelationshipService(RelationshipService relationshipService){
        this.relationshipService = relationshipService;
    }

    public void setProjectBizService(ProjectBizService projectBizService){
        this.projectBizService = projectBizService;
    }

    public void setUserService(UserService userService){
        this.userService = userService;
    }

    @Override
    public DateTime getLastModifiedDate(String id) throws RelationshipConstraintException {
        DateTime lastModifiedDate = null;

        List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(id, ArchiveDepositInfo.Status.DEPOSITED);

        if (depositInfo != null && !depositInfo.isEmpty()) {
            lastModifiedDate = depositInfo.get(0).getDepositDateTime();
        }

        return lastModifiedDate;
    }

    @Override
    public long retrieveDataItemCount(String id) throws RelationshipException, ArchiveServiceException {
        List<String> collectionIds = new ArrayList<String>();
        collectionIds.add(id);
        HashMap<String, Long> collectionItemCount = new HashMap<String, Long>();
        collectionItemCount.putAll(retrieveDataItemCountForList(collectionIds));
        return collectionItemCount.get(id);
    }

    @Override
    public HashMap<String, Long> retrieveDataItemCountForList(List<String> collectionIdList) throws RelationshipException, ArchiveServiceException {
        HashMap<String, Long> collectionsItemCount = new HashMap<String, Long>();
        for (String collectionId : collectionIdList){
            //get a set of datasets that are associated with the collection from RelationshipService
            long dataItemCount = 0;

            Set<String> dataSetIds = relationshipService.getDataSetIdsForCollectionId(collectionId);
            //ensure that the dataset listed are actually deposited successfully by checking their associated
            //ArchiveDepositInfoRecord.
            for (String dataSetId : dataSetIds) {
                if (isDeposited(dataSetId)) {
                    dataItemCount++;
                }
            }
           collectionsItemCount.put(collectionId, dataItemCount);
        }
        return collectionsItemCount;
    }
    
    private boolean isDeposited(String object_id) throws ArchiveServiceException {
        if (object_id == null || object_id.isEmpty()) {
            return false;
        }

        List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(object_id, ArchiveDepositInfo.Status.DEPOSITED);

        if (infoList == null || infoList.isEmpty()) {
            return false;
        }

        return true;
    }
    /**
     * This method polls for a collection and waits for it to transition from a
     * PENDING state.  It then returns the new state.  If the collection
     * remains in a PENDING state after a configured amount of time, it returns
     * PENDING.
     * 
     * @param depositId the deposit id of the collection to poll for.
     * @return the status of the collection after either waiting a specified
     *         time or the collection leaves a PENDING state.
     * @throws ArchiveServiceException
     */
    private Status pollAndWaitForCollection(String depositId) throws ArchiveServiceException {
        Status collectionStatus = Status.PENDING;
        long sleepInterval;
        int count = 0;

        while (collectionStatus == Status.PENDING && count < POLL_COUNT) {
            sleepInterval = count * POLL_DELAY_FACTOR_MS;
            try{
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                // ignore
            }

            archiveService.pollArchive();
            collectionStatus = archiveService.getDepositStatus(depositId);
            ++count;
        }

        return collectionStatus;
    }
}
