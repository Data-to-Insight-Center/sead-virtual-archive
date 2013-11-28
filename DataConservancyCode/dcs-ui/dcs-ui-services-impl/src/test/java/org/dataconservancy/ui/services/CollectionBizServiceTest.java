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


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.*;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Tests the methods in the CollectionBizService implementation
 */
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CollectionBizServiceTest extends BaseUnitTest {

    private RelationshipService relationshipService;
    private AuthorizationService authorizationService;
    private ArchiveService archiveService;
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ProjectBizService projectBizService;

    @Autowired
    private ArchiveService inMemoryArchiveService;

    private Collection updatedCollection;

    private CollectionBizServiceImpl underTest;

    @Before
    public void setup() throws Exception {

        //Mock the requisite services
        relationshipService = mock(RelationshipService.class);
        authorizationService = mock(AuthorizationService.class);
        archiveService = mock(ArchiveService.class);
        userService = mock(UserService.class);

        //Set up the user service
        when(userService.get(admin.getId())).thenReturn(admin);

        //Create a new Biz Service
        underTest = new CollectionBizServiceImpl();

        //Set the requisite services
        underTest.setArchiveService(archiveService);
        underTest.setAuthorizationService(authorizationService);
        underTest.setUserService(userService);
        underTest.setProjectBizService(null);
        underTest.setRelationshipService(null);

      
        //for testing update method - don't deposit here
        updatedCollection = new Collection();
        updatedCollection.setId(collectionWithData.getId());
        updatedCollection.setTitle("Updated Collection");
        updatedCollection.setSummary("This is an update of Collection one");

        //Set up the mock archive service to return a list of deposit info by id on listDepositInfo
        ArchiveDepositInfo depositInfo1 = new ArchiveDepositInfo();
        depositInfo1.setDepositId(collectionWithData.getId() + "DepositId");
        depositInfo1.setDepositDateTime(collectionWithData.getDepositDate());
        List<ArchiveDepositInfo> listDepositInfo1 = new ArrayList<ArchiveDepositInfo>();
        listDepositInfo1.add(depositInfo1);
        when(archiveService.listDepositInfo(collectionWithData.getId(), ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(listDepositInfo1);

        ArchiveDepositInfo depositInfo2 = new ArchiveDepositInfo();
        depositInfo2.setDepositId(collectionNoData.getId() + "DepositId");
        List<ArchiveDepositInfo> listDepositInfo2 = new ArrayList<ArchiveDepositInfo>();
        listDepositInfo2.add(depositInfo2);
        when(archiveService.listDepositInfo(collectionNoData.getId(), ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(listDepositInfo2);

        //set up the mock archive service to return all collections for listCollections
        List<String> collectionList = new ArrayList<String>();
        collectionList.add(collectionWithData.getId() + "DepositId");
        collectionList.add(collectionNoData.getId() + "DepositId");
        when(archiveService.listCollections(ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(collectionList);

        //set up the mock archive service to return a collection by id for retrieveCollection
        List<Collection> resultList1 = new ArrayList<Collection>();
        resultList1.add(collectionWithData);
        ArchiveSearchResult<Collection> result1 = new ArchiveSearchResult<Collection>(resultList1, 1);
        when(archiveService.retrieveCollection(collectionWithData.getId() + "DepositId")).thenReturn(result1);

        List<Collection> resultList2 = new ArrayList<Collection>();
        resultList2.add(collectionNoData);
        ArchiveSearchResult<Collection> result2 = new ArchiveSearchResult<Collection>(resultList2, 1);
        when(archiveService.retrieveCollection(collectionNoData.getId() + "DepositId")).thenReturn(result2);
    }

    @Test
    public void testGetCollectionsForAuthorizedUser() throws Exception {
        when(authorizationService.canViewCollectionList(any(Person.class))).thenReturn(true);

        Set<Collection> retrievedCollections = underTest.findByUser(admin);

        verify(authorizationService).canViewCollectionList(admin);

        assertEquals(2, retrievedCollections.size());
        assertTrue(retrievedCollections.contains(collectionWithData));
        assertTrue(retrievedCollections.contains(collectionNoData));
    }

    @Test
    public void testGetCollectionsForUnauthorizedUser() throws Exception {
        when(authorizationService.canViewCollectionList(any(Person.class))).thenReturn(false);

        Set<Collection> retrievedCollections = underTest.findByUser(admin);

        verify(authorizationService).canViewCollectionList(admin);

        assertNotNull(retrievedCollections);
        assertTrue(retrievedCollections.isEmpty());
    }

    @Test
    public void testGetSpecificCollectionForAuthorizedUser() throws Exception {
        when(authorizationService.canRetrieveCollection(any(Person.class), any(Collection.class))).thenReturn(true);

        Collection retrievedCollection = underTest.getCollection(collectionWithData.getId(), admin);

        verify(authorizationService).canRetrieveCollection(admin, collectionWithData);

        assertEquals(collectionWithData, retrievedCollection);
    }

    /**
     * Expects the Unauthorized user can retrieve collection
     *
     * @throws Exception
     */
    @Test(expected = BizPolicyException.class)
    public void testGetSpecificCollectionForUnauthorizedUser() throws Exception {
        when(authorizationService.canRetrieveCollection(any(Person.class), any(Collection.class))).thenReturn(false);

        underTest.getCollection(collectionNoData.getId(), admin);
    }

    @Test
    public void testGetCollectionForNullId() throws Exception {
        Collection retrievedCollection = underTest.getCollection(null, admin);

        assertNull(retrievedCollection);
    }

    @Test
    public void testGetNonExistingCollection() throws Exception {
        Collection retrievedCollection = underTest.getCollection("BadId", admin);

        assertNull(retrievedCollection);
    }

    /**
     * Expects that null user can retrieve collection
     *
     * @throws Exception
     */
    @Test
    public void testGetCollectionForNullUser() throws Exception {
        when(authorizationService.canRetrieveCollection(any(Person.class), any(Collection.class))).thenReturn(true);

        Collection retrievedCollection = underTest.getCollection(collectionNoData.getId(), null);

        verify(authorizationService).canRetrieveCollection(null, collectionNoData);

        assertNotNull(retrievedCollection);
        assertEquals(retrievedCollection, collectionNoData);
    }

    /**
     * Test the ability of CollectionBizService to obtain the last modified date of the collection.
     * Under the current implementation, the last modified date of the collection is the date time the collection was
     * requested to be updated as recorded in the Deposit_date field on the the ARCHIVE_DEPOSIT_INFO table of the
     * record with matching business id.
     * <p/>
     * Expected to get non null date for an existing collection and null date for non existing collection
     */
    @Test
    public void testGetLastModifiedDate() throws Exception {
        DateTime lastModifiedDate = underTest.getLastModifiedDate(collectionWithData.getId());
        assertEquals(collectionWithData.getDepositDate(), lastModifiedDate);

        lastModifiedDate = underTest.getLastModifiedDate("non-existing");
        assertNull(lastModifiedDate);
    }

    @Test
    public void testUpdateCollectionForAuthorizedUser() throws Exception {
        String expectedDepositId = "IAMADEPOSITID";
        when(authorizationService.canUpdateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        when(archiveService.deposit(updatedCollection)).thenReturn(expectedDepositId);
        when(archiveService.getDepositStatus(expectedDepositId)).thenReturn(ArchiveDepositInfo.Status.DEPOSITED);

        String collectionId = underTest.updateCollection(updatedCollection, admin);

        verify(authorizationService).canUpdateCollection(admin, updatedCollection);
        verify(archiveService).deposit(updatedCollection);
        verify(archiveService).getDepositStatus(expectedDepositId);

        updatedCollection.setDepositorId(admin.getId());
        updatedCollection.setDepositDate(new DateTime());

        verify(archiveService).deposit(updatedCollection);
        assertEquals(collectionId, updatedCollection.getId());
    }

    /**
     * Test that authorized user can create a collection under a collection
     * @throws Exception
     */
    @Ignore
    @Test
    public void testCreateSubCollectionForAuthorizedUser() throws Exception {
        String expectedDepositId = "IAMADEPOSITID";
        //Set up new collection to deposit
        Collection newCollection = new Collection();
        //Set containg project
        Collection containingCollection = new Collection();

        when(authorizationService.canCreateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        when(archiveService.deposit(newCollection)).thenReturn(expectedDepositId);
        when(archiveService.getDepositStatus(expectedDepositId)).thenReturn(ArchiveDepositInfo.Status.DEPOSITED);

        //id of newCollection
        String collectionId = null;
        //TODO: CollectionBizService.createCollection() method does not yet exist
        //collectionId = underTest.createCollection(newCollection, containingCollection, admin);

        verify(authorizationService).canCreateCollection(admin, containingCollection);
        verify(archiveService).deposit(newCollection);
        verify(archiveService).getDepositStatus(expectedDepositId);
        //verify that proper relationships are added in the relationship table
        verify(relationshipService).addSubCollectionToCollection(newCollection.getId(), containingCollection.getId());


        newCollection.setDepositorId(admin.getId());
        newCollection.setDepositDate(new DateTime());

        verify(archiveService).deposit(newCollection);
        assertEquals(collectionId, newCollection.getId());
    }


    @Test(expected = BizPolicyException.class)
    public void testUpdateCollectionForUnauthorizedUser() throws Exception {
        when(authorizationService.canUpdateCollection(any(Person.class), any(Collection.class))).thenReturn(false);

        underTest.updateCollection(updatedCollection, admin);
    }


    @Test(expected = NullPointerException.class)
    public void testUpdateCollectionForNullUserAsksAuthService() throws Exception {
        String expectedDepositId = "IAMADEPOSITID";
        when(authorizationService.canUpdateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        when(archiveService.deposit(updatedCollection)).thenReturn(expectedDepositId);
        when(archiveService.getDepositStatus(expectedDepositId)).thenReturn(ArchiveDepositInfo.Status.DEPOSITED);

        underTest.updateCollection(updatedCollection, null);

        verify(authorizationService).canUpdateCollection(null, updatedCollection);
        verify(archiveService).deposit(updatedCollection);
        verify(archiveService).getDepositStatus(expectedDepositId);
    }

    @Test(expected = BizInternalException.class)
    public void testArchiveServiceThrowsExceptionInGetCollection() throws Exception {
        when(archiveService.retrieveCollection(anyString())).thenThrow(new ArchiveServiceException("foo"));

        underTest.getCollection(collectionWithData.getId(), admin);
    }

    @Test(expected = BizInternalException.class)
    public void testArchiveServiceThrowsExceptionInFindByUser() throws Exception {
        when(authorizationService.canViewCollectionList(any(Person.class))).thenReturn(true);
        when(archiveService.retrieveCollection(anyString())).thenThrow(new ArchiveServiceException("foo"));

        underTest.findByUser(admin);
    }

    @Test(expected = BizInternalException.class)
    public void testAuthorizationServiceThrowsExceptionInUpdateCollection() throws Exception {
        when(authorizationService.canUpdateCollection(admin, collectionWithData)).thenThrow(new RelationshipConstraintException("foo"));

        underTest.updateCollection(collectionWithData, admin);
    }

    /**
     * This test checks the case where the archiveService returns a failed
     * status.  The service should throw an exception.
     *
     * @throws Exception
     */
    @Test(expected = BizInternalException.class)
    public void testArchiveServiceFailsDepositThrowsExceptionInUpdateCollection() throws Exception {
        String expectedDepositId = "IAMADEPOSITID";
        when(authorizationService.canUpdateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        when(archiveService.deposit(updatedCollection)).thenReturn(expectedDepositId);
        when(archiveService.getDepositStatus(expectedDepositId)).thenReturn(ArchiveDepositInfo.Status.FAILED);

        underTest.updateCollection(updatedCollection, admin);
    }

    /**
     * This test checks the case where the archiveService returns a pending
     * status forever.  In this case, the service should not hang, but
     * throw an exception.
     *
     * @throws Exception
     */
    @Test(expected = BizInternalException.class)
    public void testArchiveServiceTakesTooLongToDepositThrowsExceptionInUpdateCollection() throws Exception {
        String expectedDepositId = "IAMADEPOSITID";
        when(authorizationService.canUpdateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        when(archiveService.deposit(updatedCollection)).thenReturn(expectedDepositId);
        when(archiveService.getDepositStatus(expectedDepositId)).thenReturn(ArchiveDepositInfo.Status.PENDING);

        underTest.updateCollection(updatedCollection, admin);
    }

    /**
     * Test test checks that when there is a failure in depositing a new data item as reflected in the archive deposit info table
     * the data item count would account for such failure
     */
    @Test
    public void testGetDataItemCountForCollectionOnDepositFailure() throws RelationshipException, ArchiveServiceException {
        String collectionId = "id:collection";
        Set<String> dataSetIds = new HashSet<String>();

        dataSetIds.add("id:dataset1");
        dataSetIds.add("id:dataset2");
        dataSetIds.add("id:dataset3");
        dataSetIds.add("id:dataset4");
        when(relationshipService.getDataSetIdsForCollectionId(collectionId)).thenReturn(dataSetIds);

        underTest.setRelationshipService(relationshipService);
        //Set up archive deposit info objects lists to be returned by archive service
        List<ArchiveDepositInfo> infoList1 = new ArrayList<ArchiveDepositInfo>();
        List<ArchiveDepositInfo> infoList2 = new ArrayList<ArchiveDepositInfo>();
        List<ArchiveDepositInfo> infoList3 = new ArrayList<ArchiveDepositInfo>();
        List<ArchiveDepositInfo> infoList4 = new ArrayList<ArchiveDepositInfo>();

        //adding element to the info lists except for infoList3 to fake the failed deposit of dataset 3.
        infoList2.add(new ArchiveDepositInfo());
        infoList1.add(new ArchiveDepositInfo());
        infoList4.add(new ArchiveDepositInfo());

        when(archiveService.listDepositInfo("id:dataset1", ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(infoList1);
        when(archiveService.listDepositInfo("id:dataset2", ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(infoList2);
        when(archiveService.listDepositInfo("id:dataset3", ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(infoList3);
        when(archiveService.listDepositInfo("id:dataset4", ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(infoList4);

        assertEquals(3,underTest.retrieveDataItemCount(collectionId));
    }
    
    /**
     * Test that if a collection is updated the same number of collections are returned when you retrieve a collection list for a user. 
     * @throws ArchiveServiceException 
     * @throws BizInternalException 
     * @throws BizPolicyException 
     * @throws RelationshipConstraintException 
     */
    @Test
    public void testUpdateCollectionReturnsSameNumberOfCollections() throws ArchiveServiceException, BizPolicyException, BizInternalException, RelationshipConstraintException {
        when(authorizationService.canViewCollectionList(any(Person.class))).thenReturn(true);
        when(authorizationService.canUpdateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        Collection collection = new Collection();
        collection.setTitle("Test collection");
        collection.setId("id");
        collection.setSummary("test");
        
        underTest.setArchiveService(inMemoryArchiveService);
        inMemoryArchiveService.deposit(collection);
        inMemoryArchiveService.pollArchive();
        Set<Collection> collections = underTest.findByUser(admin);
        assertNotNull(collections);
        int expectedSize = collections.size();
        
        Collection initialCollection = collections.iterator().next();
        initialCollection.setSummary("updated");
        
        underTest.updateCollection(initialCollection, admin);

        collections = underTest.findByUser(admin);
        assertNotNull(collections);
        assertEquals(expectedSize, collections.size());
        
        underTest.setArchiveService(archiveService);
    }
    
    /**
     * Tests the creation of a collection for a certain project.
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testCreateCollectionForProject() throws BizPolicyException, BizInternalException,
            RelationshipConstraintException, ArchiveServiceException {
        when(authorizationService.canViewCollectionList(any(Person.class))).thenReturn(true);
        when(authorizationService.canCreateCollection(any(Person.class), any(Project.class))).thenReturn(true);
        when(authorizationService.canRetrieveCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        
        Collection collection = new Collection();
        collection.setTitle("Test collection For Project");
        collection.setId("newIdForThisCollection");
        collection.setSummary("This is a test collection created for this project.");
        collection.setParentProjectId(projectOne.getId());
        // Set needed services.
        underTest.setArchiveService(inMemoryArchiveService);
        underTest.setProjectBizService(projectBizService);
        underTest.setAuthorizationService(authorizationService);
        underTest.setRelationshipService(relationshipService);

        Assert.assertNotNull("Project doesn't exist in the service.", projectService.get(projectOne.getId()));
        Assert.assertNotNull("Project doesn't exist in the biz service.",
                projectBizService.getProject(projectOne.getId(), admin));

        Assert.assertNotNull("Could not create the collection.",
                underTest.createCollection(collection, admin));
        Assert.assertNotNull("Could not get the collection from the Biz service.",
                underTest.getCollection(collection.getId(), admin));
        Set<Collection> collections = underTest.findByUser(admin);
        Assert.assertNotNull("Collections set retrieved is null.", collections);
        Assert.assertTrue("Retrieved collections set is empty.", collections.size() > 0);
        verify(authorizationService).canCreateCollection(admin, projectOne);
        verify(relationshipService).addCollectionToProject(collection, projectOne);
        verify(relationshipService).addDepositorToCollection(admin, collection);

    }
    
    /**
     * Tests that a BizInternalException is thrown if the collection is being added to an invalid project.
     * 
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    @Test(expected = BizInternalException.class)
    public void testCreateCollectionForInvalidProject() throws BizPolicyException, BizInternalException {
        Collection collection = new Collection();
        collection.setTitle("Test collection For Project");
        collection.setId("newIdForThisCollection");
        collection.setSummary("This is a test collection created for this project.");
        collection.setParentProjectId("InvalidProjectId");
        underTest.setProjectBizService(projectBizService);
        underTest.createCollection(collection, admin);
    }

    /**
     * Tests that a BizPolicyException is thrown if the collection is being added by an invalid user.
     * 
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    @Test(expected = BizPolicyException.class)
    public void testCreateCollectionByInvalidUser() throws BizPolicyException, BizInternalException {
        Collection collection = new Collection();
        collection.setTitle("Test collection For Project");
        collection.setId("newIdForThisCollection");
        collection.setSummary("This is a test collection created for this project.");
        collection.setParentProjectId(projectOne.getId());
        underTest.setProjectBizService(projectBizService);
        Assert.assertNotNull("Project doesn't exist in the service.", projectService.get(projectOne.getId()));
        Assert.assertNotNull("Project doesn't exist in the biz service.",
                projectBizService.getProject(projectOne.getId(), admin));
        underTest.createCollection(collection, admin);
    }

    /**
     * Tests that a BizPolicyException is thrown if the collection to be created is null.
     * 
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    @Test(expected = BizPolicyException.class)
    public void testCreateNullCollection() throws BizPolicyException, BizInternalException {
        Collection collection = null;
        underTest.createCollection(collection, admin);
    }

    /**
     * Tests that a BizPolicyException is thrown if the collection to be created doesn't have an ID.
     * 
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    @Test(expected = BizPolicyException.class)
    public void testCreateCollectionNoId() throws BizPolicyException, BizInternalException {
        Collection collection = new Collection();
        collection.setParentProjectId(projectOne.getId());
        underTest.createCollection(collection, admin);
    }
    
    /**
     * Tests that a BizPolicyException is thrown if the collection to be created doesn't have a title.
     * 
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    @Test(expected = BizPolicyException.class)
    public void testCreateCollectionNoTitle() throws BizPolicyException, BizInternalException {
        Collection collection = new Collection();
        collection.setId("SomeIdForThisCollection");
        collection.setParentProjectId(projectOne.getId());
        underTest.createCollection(collection, admin);
    }
    
    /**
     * Tests that a BizPolicyException is thrown if the collection to be created doesn't have a summary.
     * 
     * @throws BizPolicyException
     * @throws BizInternalException
     */
    @Test(expected = BizPolicyException.class)
    public void testCreateCollectionNoSummary() throws BizPolicyException, BizInternalException {
        Collection collection = new Collection();
        collection.setId("SomeIdForThisCollection.");
        collection.setTitle("Some Title For This Collection.");
        collection.setParentProjectId(projectOne.getId());
        underTest.createCollection(collection, admin);
    }

    /**
     * Tests the creation of a subcollection.
     * 
     * @throws BizPolicyException
     * @throws BizInternalException
     * @throws RelationshipException
     */
    @Test
    public void testCreateSubCollection() throws BizPolicyException, BizInternalException, RelationshipException {
        when(authorizationService.canViewCollectionList(any(Person.class))).thenReturn(true);
        when(authorizationService.canCreateCollection(any(Person.class), any(Project.class))).thenReturn(true);
        when(authorizationService.canUpdateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        when(authorizationService.canCreateCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        when(authorizationService.canRetrieveCollection(any(Person.class), any(Collection.class))).thenReturn(true);
        
        Collection collection = new Collection();
        collection.setTitle("Super collection.");
        collection.setId("newIdForThisSuperCollection");
        collection.setSummary("This is a super collection created for this project.");
        collection.setParentProjectId(projectOne.getId());
        // Set needed services.
        underTest.setArchiveService(inMemoryArchiveService);
        underTest.setProjectBizService(projectBizService);
        underTest.setAuthorizationService(authorizationService);
        underTest.setRelationshipService(relationshipService);
        
        Assert.assertNotNull("Project doesn't exist in the service.", projectService.get(projectOne.getId()));
        Assert.assertNotNull("Project doesn't exist in the biz service.",
                projectBizService.getProject(projectOne.getId(), admin));
        Assert.assertNotNull("Could not create the collection.",
                underTest.createCollection(collection, admin));
        Assert.assertNotNull("Could not get the collection from the Biz service.",
                underTest.getCollection(collection.getId(), admin));
        Set<Collection> collections = underTest.findByUser(admin);
        Assert.assertNotNull("Collections set retrieved is null.", collections);
        Assert.assertTrue("Retrieved collections set is empty.", collections.size() > 0);
        
        Collection subCollection = new Collection();
        subCollection.setTitle("Sub collection.");
        subCollection.setId("newIdForThisSubCollection");
        subCollection.setSummary("This is a sub collection created for this super collection.");
        subCollection.setParentId(collection.getId());
        subCollection.setParentProjectId(projectOne.getId());
        
        Assert.assertNotNull("SubCollection could not be created.", underTest.createCollection(subCollection, admin));
        collections = underTest.findByUser(admin);
        Assert.assertNotNull("Collections set retrieved is null.", collections);
        Assert.assertTrue("Retrieved collections set has less than one collection.", collections.size() > 1);
        
        verify(relationshipService).addCollectionToProject(collection, projectOne);
        verify(relationshipService).addSubCollectionToCollection(subCollection.getId(), collection.getId());
    }

}

