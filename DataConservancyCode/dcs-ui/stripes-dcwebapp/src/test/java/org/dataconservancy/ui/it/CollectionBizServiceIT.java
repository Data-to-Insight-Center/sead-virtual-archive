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

package org.dataconservancy.ui.it;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_CLASS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.model.Address;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.CollectionBizServiceImpl;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Tests the methods in the CollectionBizService implementation
 */

@DirtiesDatabase(AFTER_CLASS)
@DirtiesContext
public class CollectionBizServiceIT extends BaseIT {
    
    @Autowired
    private ArchiveService archiveService;
    
    @Autowired
    private RelationshipService relationshipService;
    
    @Autowired
    private ProjectBizService projectBizService;
    
    @Autowired
    private AuthorizationService authorizationService;
    
    @Autowired
    private UserService userService;

    private CollectionBizServiceImpl underTest;
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person admin;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person randomUser;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person projectAdmin;
    
    private Person bogusUser;
    
    private Project project;
    private Collection collection1;
    private Collection updatedCollection;
    
    private String alternateIdOne = "AlternateIdOne";
    private String alternateIdTwo = "AlternateIdTwo";
    
    private HttpClient hc = new DefaultHttpClient();
    
    private PersonName name1 = new PersonName(null, "FirstFirst", "FirstMiddle", "FirstLast", null);
    private PersonName name2 = new PersonName(null, "SecondFirst", "SecondMiddle", "SecondLast", null);
    
    private Collection subCollection;
    
    private static int archiveCollectionCount = 0;
    
    /**
     * set up objects needed for the tests
     * 
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Before
    public void setup() throws ArchiveServiceException, RelationshipConstraintException, BizInternalException,
            BizPolicyException, IOException {
        
        underTest = new CollectionBizServiceImpl();
        underTest.setAuthorizationService(authorizationService);
        underTest.setArchiveService(archiveService);
        underTest.setProjectBizService(projectBizService);
        underTest.setRelationshipService(relationshipService);
        underTest.setUserService(userService);
        
        project = new Project();
        project.setId(reqFactory.createIdApiRequest(Types.PROJECT).execute(hc));
        project.addNumber("AWARD0");
        project.setName("AWARD0NAME");
        project.setDescription("THIS IS AWARD 0 DESCRIPTION");
        project.setPublisher("THIS IS PUBLISHER 0");
        project.addPi(admin.getId());
        
        project.setStartDate(new DateTime("2010-05-26"));
        project.setEndDate(new DateTime("2016-05-26"));
        
        project.setStorageAllocated(1000000000000L);
        project.setStorageUsed(500000000000L);
        project.setFundingEntity("Sayeed");
        
        projectBizService.updateProject(project, admin);
        
        // set up Collections
        collection1 = new Collection();
        collection1.setId(reqFactory.createIdApiRequest(Types.COLLECTION).execute(hc));
        collection1.setSummary("Collection 1 summary");
        collection1.setTitle("brief title");
        collection1.setParentProjectId(project.getId());
        Address address1 = new Address("One First Ave.", "Yourtown", "Alaska", "00000", "USA");
        ContactInfo contact1 = new ContactInfo("Boss of You", "Contact One", "cone@gmail.biz", "555-1212", address1);
        ContactInfo contact2 = new ContactInfo("King of the World", "Contact Two", "ctwo@gmail.biz", "222-1515",
                address1);
        
        List<ContactInfo> ciList = new ArrayList<ContactInfo>();
        ciList.add(contact1);
        ciList.add(contact2);
        collection1.setContactInfoList(ciList);
        
        List<String> alternateIds = new ArrayList<String>();
        alternateIds.add(alternateIdOne);
        alternateIds.add(alternateIdTwo);
        
        List<PersonName> creatorList = new ArrayList<PersonName>();
        creatorList.add(name1);
        creatorList.add(name2);
        collection1.setCreators(creatorList);
        collection1.setDepositDate(DateTime.now());
        
        // for testing update method - don't deposit here
        updatedCollection = new Collection();
        updatedCollection.setId(collection1.getId());
        updatedCollection.setTitle("Updated Collection");
        updatedCollection.setSummary("This is an update of Collection one");
        updatedCollection.setAlternateIds(alternateIds);
        updatedCollection.setCreators(creatorList);
        updatedCollection.setParentProjectId(project.getId());
        relationshipService.addCollectionToProject(collection1, project);
        relationshipService.addAdministratorToProject(project, projectAdmin);
        
        underTest.updateCollection(collection1, admin);
        // keep track of every deposit so we know how many collections are in the
        // archive - need to know this to be sure we get back everything when we find all by user
        archiveCollectionCount++;
        
        // bogus user - do not add via user service
        bogusUser = new Person();
        bogusUser.setId("id:shadyhacker");
        bogusUser.setEmailAddress("shady@hacker.ru");
        bogusUser.setPassword("");
        // let's make it interesting, give the bogus user roles
        List<Role> bogusRoles = new ArrayList<Role>();
        bogusRoles.add(Role.ROLE_ADMIN);
        bogusRoles.add(Role.ROLE_USER);
        bogusUser.setRoles(bogusRoles);
        
        // For testing the creation of child collection
        subCollection = new Collection();
        subCollection.setId("ChildCollectionId");
        subCollection.setTitle("Child Collection Title");
        subCollection.setSummary("Child Collection Summary.");
        subCollection.setCreators(creatorList);
        subCollection.setParentId(collection1.getId());

    }
    
    /**
     * test that an instance admin can update a collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws ArchiveServiceException
     */
    @Test
    public void testUpdateCollectionInstanceAdmin() throws BizInternalException, BizPolicyException,
            ArchiveServiceException {
        underTest.updateCollection(updatedCollection, admin);
        
        Collection retrievedCollection = underTest.getCollection(updatedCollection.getId(), admin);
        assertEquals(updatedCollection.getTitle(), retrievedCollection.getTitle());
        assertEquals(updatedCollection.getSummary(), retrievedCollection.getSummary());
        assertEquals(updatedCollection.getCreators(), retrievedCollection.getCreators());
        assertTrue(updatedCollection.getAlternateIds().contains(alternateIdOne));
        assertTrue(updatedCollection.getAlternateIds().contains(alternateIdTwo));
        
        underTest.updateCollection(collection1, admin);
        
        retrievedCollection = underTest.getCollection(collection1.getId(), admin);
        assertEquals(collection1.getTitle(), retrievedCollection.getTitle());
        assertEquals(collection1.getSummary(), retrievedCollection.getSummary());
        assertEquals(collection1.getAlternateIds(), retrievedCollection.getAlternateIds());
        assertTrue(retrievedCollection.getCreators().contains(name1));
        assertTrue(retrievedCollection.getCreators().contains(name2));
    }
    
    /**
     * test that a project admin for the project containing a collection can update that collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     * 
     */
    @Test
    public void testUpdateCollectionProjectAdmin() throws BizInternalException, BizPolicyException,
            ArchiveServiceException, RelationshipConstraintException {
        underTest.updateCollection(updatedCollection, projectAdmin);
        
        Collection retrievedCollection = underTest.getCollection(updatedCollection.getId(), projectAdmin);
        assertEquals(updatedCollection.getTitle(), retrievedCollection.getTitle());
        assertEquals(updatedCollection.getSummary(), retrievedCollection.getSummary());
        assertEquals(updatedCollection.getCreators(), retrievedCollection.getCreators());
        assertTrue(updatedCollection.getAlternateIds().contains(alternateIdOne));
        assertTrue(updatedCollection.getAlternateIds().contains(alternateIdTwo));
        
        underTest.updateCollection(collection1, projectAdmin);
        
        retrievedCollection = underTest.getCollection(collection1.getId(), projectAdmin);
        assertEquals(collection1.getTitle(), retrievedCollection.getTitle());
        assertEquals(collection1.getSummary(), retrievedCollection.getSummary());
        assertEquals(collection1.getAlternateIds(), retrievedCollection.getAlternateIds());
        assertTrue(retrievedCollection.getCreators().contains(name1));
        assertTrue(retrievedCollection.getCreators().contains(name2));
    }
    
    /**
     * test that a project admin for the project can create a collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     * 
     */
    @Test
    public void testCreateCollectionProjectAdmin() throws BizInternalException, BizPolicyException,
            ArchiveServiceException, RelationshipConstraintException {
        underTest.createCollection(collection1, admin);
        
        Collection retrievedCollection = underTest.getCollection(collection1.getId(), admin);
        assertEquals(collection1.getId(), retrievedCollection.getId());
        assertEquals(collection1.getTitle(), retrievedCollection.getTitle());
        assertEquals(collection1.getSummary(), retrievedCollection.getSummary());
        assertEquals(collection1.getCreators(), retrievedCollection.getCreators());
        assertEquals(collection1.getDepositDate(), retrievedCollection.getDepositDate());
        assertEquals(collection1.getContactInfoList(), retrievedCollection.getContactInfoList());
    }
    
    /**
     * test that a project admin for the project containing a collection can create a sub collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     * 
     */
    @Test
    public void testCreateSubCollectionToParentCollection() throws BizInternalException, BizPolicyException,
            ArchiveServiceException, RelationshipConstraintException {
        underTest.createCollection(subCollection, admin);
        
        Collection retrievedCollection = underTest.getCollection(subCollection.getId(), admin);
        assertEquals(subCollection.getId(), retrievedCollection.getId());
        assertEquals(subCollection.getTitle(), retrievedCollection.getTitle());
        assertEquals(subCollection.getSummary(), retrievedCollection.getSummary());
        assertEquals(subCollection.getCreators(), retrievedCollection.getCreators());
        assertEquals(subCollection.getParentId(), retrievedCollection.getParentId());

        
    }
    
    /**
     * test that a random user in the system cannot update a collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test(expected = BizPolicyException.class)
    public void testUpdateCollectionRandomUser() throws BizInternalException, BizPolicyException {
        underTest.updateCollection(updatedCollection, randomUser);
    }
    
    /**
     * test that a Person object for a person not in the system cannot update a collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test(expected = BizPolicyException.class)
    public void testUpdateCollectionBogusUser() throws BizInternalException, BizPolicyException {
        underTest.updateCollection(updatedCollection, bogusUser);
    }
    
    /**
     * test that a null user cannot update a collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test(expected = BizPolicyException.class)
    public void testUpdateCollectionNullUser() throws BizInternalException, BizPolicyException {
        underTest.updateCollection(updatedCollection, null);
    }
    
    /**
     * test that an instance admin can retrieve a specific collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetSpecificCollectionForInstanceAdministrator() throws BizInternalException, BizPolicyException {
        Collection retrievedCollection = underTest.getCollection(collection1.getId(), admin);
        assertEquals(collection1, retrievedCollection);
    }
    
    /**
     * test that a project administrator can retrieve a specific collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetSpecificCollectionProjectAdministrator() throws BizInternalException, BizPolicyException {
        Collection retrievedCollection = underTest.getCollection(collection1.getId(), projectAdmin);
        assertEquals(collection1, retrievedCollection);
    }
    
    /**
     * test that a random user can retrieve a specific collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetSpecificCollectionForRandomUser() throws BizInternalException, BizPolicyException {
        Collection retrievedCollection = underTest.getCollection(collection1.getId(), randomUser);
        assertEquals(collection1, retrievedCollection);
    }
    
    /**
     * test that a bogus user can retrieve a specific collection
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetSpecificCollectionForBogusUser() throws BizInternalException, BizPolicyException {
        Collection retrievedCollection = underTest.getCollection(collection1.getId(), bogusUser);
        assertEquals(collection1, retrievedCollection);
    }
    
    /**
     * test that a null user can update a specific colleciton
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetSpecificCollectionForNullUser() throws BizInternalException, BizPolicyException {
        Collection retrievedCollection = underTest.getCollection(collection1.getId(), null);
        assertEquals(collection1, retrievedCollection);
    }
    
    /**
     * test that an instance admin can retrieve all collections
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetAllCollectionsForInstanceAdministrator() throws BizInternalException, BizPolicyException {
        int retrievedCount = 0;
        Set<Collection> retrievedCollections = underTest.findByUser(admin);
        
        assertTrue(archiveCollectionCount <= retrievedCollections.size());
    }
    
    /**
     * test that a project admin can retrieve all collections
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetAllCollectionsProjectAdministrator() throws BizInternalException, BizPolicyException {
        Set<Collection> retrievedCollections = underTest.findByUser(admin);
        assertTrue(archiveCollectionCount <= retrievedCollections.size());
    }
    
    /**
     * test that a random user can retrieve all collections
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetAllCollectionsForRandomUser() throws BizInternalException, BizPolicyException {
        Set<Collection> retrievedCollections = underTest.findByUser(admin);
        assertTrue(archiveCollectionCount <= retrievedCollections.size());
    }
    
    /**
     * test that a bogus user not in the system can retrieve all collections
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetAllCollectionsForBogusUser() throws BizInternalException, BizPolicyException {
        Set<Collection> retrievedCollections = underTest.findByUser(admin);
        assertTrue(archiveCollectionCount <= retrievedCollections.size());
    }
    
    /**
     * test that a null user can retrieve all collections
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    public void testGetAllCollectionsForNullUser() throws BizInternalException, BizPolicyException {
        Set<Collection> retrievedCollections = underTest.findByUser(admin);
        assertTrue(archiveCollectionCount <= retrievedCollections.size());
    }
    
}