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

import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * This class tests the DataItem Biz Service by testing retrieval of DataSets by
 * id.
 */
@DirtiesDatabase(DirtiesDatabase.AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DataItemBizServiceTest
        extends BaseUnitTest {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private DataItemBizService dataItemBizService;
    
    @Autowired
    private CollectionBizService collectionBizService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private Person adminUser;

    @Autowired
    private Person approvedRegisteredUser;

    @Autowired
    private Person defaultUser;

    @Autowired
    private ProjectService projectService;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    private DataItem dataItemNew;

    @Before
    public void setup() throws ArchiveServiceException, IOException,
            RelationshipConstraintException {

        // Deposit a new version of the DataItem.

        dataItemNew = new DataItem(dataItemTwo);
        dataItemNew.setName("new");
        dataItemNew.setDescription("This is a new version");
        
        relationshipService.addDataSetToCollection(dataItemNew,
                                                   collectionWithData);
        relationshipService.addDataFileToDataSet(dataFileTwo, dataItemNew);

        archiveService.deposit(collectionWithDataDepositID, dataItemNew);

        archiveService.pollArchive();
    }

    /**
     * Tests that given a user with admin permissions the latest DataItem can be
     * returned.
     * 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testRetrieveLatestDataSet()
            throws RelationshipConstraintException, ArchiveServiceException,
            BizPolicyException {
        DataItem ds =
                dataItemBizService.getDataItem(dataItemNew.getId(), adminUser);

        assertNotNull(ds);
        assertEquals(dataItemNew.getId(), ds.getId());
        assertEquals(dataItemNew.getName(), ds.getName());
    }

    /**
     * Tests that given a user with admin permissions the latest DataItem can be
     * returned and that the data sets are in the right order.
     * 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testRetrieveVersions() throws RelationshipConstraintException,
            ArchiveServiceException, BizPolicyException {
        List<DataItem> versions =
                dataItemBizService.getDataItemVersions(dataItemNew.getId(),
                                                       adminUser);

        assertNotNull(versions);
        assertEquals(2, versions.size());

        DataItem ds1 = versions.get(0);
        DataItem ds2 = versions.get(1);
        assertEquals(ds1.getId(), ds2.getId());

        assertEquals(dataItemNew.getId(), ds1.getId());
        assertEquals(dataItemNew.getName(), ds1.getName());

        assertEquals(dataItemTwo.getId(), ds2.getId());
        assertEquals(dataItemTwo.getName(), ds2.getName());
    }

    /**
     * This test is to document existing behavior when a non existent DataItem
     * id is passed in should return null or an empty list.
     * 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testNonExistentDataSet()
            throws RelationshipConstraintException, ArchiveServiceException,
            BizPolicyException {
        DataItem ds =
                dataItemBizService.getDataItem("nonexistent id", adminUser);
        assertNull(ds);

        List<DataItem> versions =
                dataItemBizService.getDataItemVersions("nonexistent id",
                                                       adminUser);
        assertNotNull(versions);
        assertEquals(0, versions.size());
    }

    /**
     * This test is to document existing behavior when a non existent user is
     * passed in should thrown an Authentication error.
     * 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     */
    @Test
    public void testNullUser() throws RelationshipConstraintException,
            ArchiveServiceException, BizPolicyException {
        DataItem ds = dataItemBizService.getDataItem(dataItemNew.getId(), null);

        assertNotNull(ds);
        assertEquals(dataItemNew.getId(), ds.getId());
        assertEquals(dataItemNew.getName(), ds.getName());
        List<DataItem> versions =
                dataItemBizService.getDataItemVersions(dataItemNew.getId(),
                                                       null);

        assertNotNull(versions);
        assertEquals(2, versions.size());

        DataItem ds1 = versions.get(0);
        DataItem ds2 = versions.get(1);

        assertEquals(dataItemNew.getId(), ds1.getId());
        assertEquals(dataItemNew.getName(), ds1.getName());

        assertEquals(dataItemTwo.getId(), ds2.getId());
        assertEquals(dataItemTwo.getName(), ds2.getName());
    }

    /**
     * Tests that a user that is an admin on the project containing the file can
     * retrieve the file.
     * 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testProjectAdmin() throws RelationshipConstraintException,
            ArchiveServiceException, BizPolicyException {
        DataItem ds =
                dataItemBizService.getDataItem(dataItemNew.getId(),
                                               approvedRegisteredUser);

        assertNotNull(ds);

        List<DataItem> versions =
                dataItemBizService.getDataItemVersions(dataItemNew.getId(),
                                                       approvedRegisteredUser);
        assertNotNull(versions);
        assertEquals(2, versions.size());
    }

    /**
     * Tests that a user who is a depositor on the collection can view the file.
     * 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testCollectionDepositor()
            throws RelationshipConstraintException, ArchiveServiceException,
            BizPolicyException {
        DataItem ds =
                dataItemBizService
                        .getDataItem(dataItemNew.getId(), defaultUser);

        assertNotNull(ds);

        List<DataItem> versions =
                dataItemBizService.getDataItemVersions(dataItemNew.getId(),
                                                       defaultUser);
        assertNotNull(versions);
        assertEquals(2, versions.size());
    }

    /**
     * Tests that trying to access the file as a user without permissions throws
     * and authorization erorr.
     * 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     */
    @Test
    public void testUserWithoutPermissions()
            throws RelationshipConstraintException, ArchiveServiceException,
            BizPolicyException {
        Person person = new Person();
        person.setId("id:person");
        person.setEmailAddress("person@people.net");
        person.setFirstNames("John");
        person.setLastNames("Doe");
        person.setPrefix("Mr.");
        person.setSuffix("II");
        person.setMiddleNames("Spiderish");
        person.setPreferredPubName("J. Doe");
        person.setBio("Some bio for the user.");
        person.setWebsite("www.somewebsite.com");
        person.setJobTitle("Monkey Scientist");
        person.setDepartment("Monkey Department");
        person.setCity("Baltimore");
        person.setState("Maryland");
        person.setInstCompany("Monkey Institution/Company");
        person.setInstCompanyWebsite("www.MonkeyInstitutionCompany.com");
        person.setRegistrationStatus(RegistrationStatus.APPROVED);
        person.setPhoneNumber("1234567890");
        person.setPassword("password");
        person.setExternalStorageLinked(false);
        person.setDropboxAppKey("SomeKey");
        person.setDropboxAppSecret("SomeSecret");

        DataItem ds =
                dataItemBizService.getDataItem(dataItemNew.getId(), person);

        assertNotNull(ds);
        assertEquals(dataItemNew.getId(), ds.getId());
        assertEquals(dataItemNew.getName(), ds.getName());
        List<DataItem> versions =
                dataItemBizService.getDataItemVersions(dataItemNew.getId(),
                                                       person);

        assertNotNull(versions);
        assertEquals(2, versions.size());

        DataItem ds1 = versions.get(0);
        DataItem ds2 = versions.get(1);

        assertEquals(dataItemNew.getId(), ds1.getId());
        assertEquals(dataItemNew.getName(), ds1.getName());

        assertEquals(dataItemTwo.getId(), ds2.getId());
        assertEquals(dataItemTwo.getName(), ds2.getName());
    }
    
    @Test
    public void testAddNewDataItem() throws Exception {
        DataItem dataItemToDeposit = new DataItem();
        dataItemToDeposit.setName("Test DataItem Add");
        dataItemToDeposit.setDescription("This is a data item");
        dataItemToDeposit.setId("2839517290");
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        DataFile dataFile = new DataFile();
        dataFile.setId("id:82382830");
        dataFile.setSource(tmp.toURI().toURL().toExternalForm());
        dataFile.setName("Data file one");
        
        Resource r = new UrlResource(dataFile.getSource());
        dataFile.setSize(r.contentLength());
        dataFile.setPath(tmp.getParent());
        dataItemToDeposit.addFile(dataFile);
        
        dataItemBizService.addDataItem(dataItemToDeposit, collectionWithData.getId(), adminUser);
        
        archiveService.pollArchive();
        
        DataItem returnedDataItem = dataItemBizService.getDataItem(dataItemToDeposit.getId(), adminUser);
        
        assertNotNull(returnedDataItem);
        
        assertEquals(dataItemToDeposit.getId(), returnedDataItem.getId());
        assertEquals(1, returnedDataItem.getFiles().size());
        assertEquals(dataItemToDeposit.getFiles().get(0).getId(), returnedDataItem.getFiles().get(0).getId());
    }
    
    @Test(expected = BizPolicyException.class)
    public void testNullUserThrowsAuthException() throws Exception {
        DataItem dataItemToDeposit = new DataItem();
        dataItemToDeposit.setName("Test DataItem Add");
        dataItemToDeposit.setDescription("This is a data item");
        dataItemToDeposit.setId("2839517290");
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        DataFile dataFile = new DataFile();
        dataFile.setId("id:82382830");
        dataFile.setSource(tmp.toURI().toURL().toExternalForm());
        dataFile.setName("Data file one");
        
        Resource r = new UrlResource(dataFile.getSource());
        dataFile.setSize(r.contentLength());
        dataFile.setPath(tmp.getParent());
        dataItemToDeposit.addFile(dataFile);
        
        dataItemBizService.addDataItem(dataItemToDeposit, collectionWithData.getId(), null);     
    }
    
    @Test
    public void testCollectionDepositorCanAddDataItem() throws Exception {
        DataItem dataItemToDeposit = new DataItem();
        dataItemToDeposit.setName("Test DataItem Add");
        dataItemToDeposit.setDescription("This is a data item");
        dataItemToDeposit.setId("2839517290");
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        DataFile dataFile = new DataFile();
        dataFile.setId("id:82382830");
        dataFile.setSource(tmp.toURI().toURL().toExternalForm());
        dataFile.setName("Data file one");
        
        Resource r = new UrlResource(dataFile.getSource());
        dataFile.setSize(r.contentLength());
        dataFile.setPath(tmp.getParent());
        dataItemToDeposit.addFile(dataFile);
        
        dataItemBizService.addDataItem(dataItemToDeposit, collectionWithData.getId(), user);
        
        archiveService.pollArchive();
        
        DataItem returnedDataItem = dataItemBizService.getDataItem(dataItemToDeposit.getId(), user);
        
        assertNotNull(returnedDataItem);
        
        assertEquals(dataItemToDeposit.getId(), returnedDataItem.getId());
        assertEquals(1, returnedDataItem.getFiles().size());
        assertEquals(dataItemToDeposit.getFiles().get(0).getId(), returnedDataItem.getFiles().get(0).getId());
    }
    
    @Test(expected = BizPolicyException.class)
    public void testLoggedInUserThrowsAuthException() throws Exception {
        DataItem dataItemToDeposit = new DataItem();
        dataItemToDeposit.setName("Test DataItem Add");
        dataItemToDeposit.setDescription("This is a data item");
        dataItemToDeposit.setId("2839517290");
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        DataFile dataFile = new DataFile();
        dataFile.setId("id:82382830");
        dataFile.setSource(tmp.toURI().toURL().toExternalForm());
        dataFile.setName("Data file one");
        
        Resource r = new UrlResource(dataFile.getSource());
        dataFile.setSize(r.contentLength());
        dataFile.setPath(tmp.getParent());
        dataItemToDeposit.addFile(dataFile);
        
        dataItemBizService.addDataItem(dataItemToDeposit, collectionWithData.getId(), newUser);
    }
    
    @Test(expected = BizInternalException.class)
    public void testAddExistingDataItemThrowsException() throws Exception {
        dataItemBizService.addDataItem(dataItemOne, collectionOne.getId(), admin);        
    }
    
    @Test
    public void testGetDataFile() throws Exception {
        DataFile returnedDataFile = dataItemBizService.getDataFile(dataFileOne.getId());
        assertNotNull(returnedDataFile);
        
        assertEquals(dataFileOne.getId(), returnedDataFile.getId());
        assertEquals(dataFileOne.getName(), returnedDataFile.getName());
    }
    
    @Test
    public void testGetMissingDataFileReturnsNull() throws Exception {
        DataFile returnedDataFile = dataItemBizService.getDataFile("foo");
        assertNull(returnedDataFile);        
    }
    
    @Test
    public void testGetNullDataFileReturnsNull() throws Exception {
        DataFile returnedDataFile = dataItemBizService.getDataFile(null);
        assertNull(returnedDataFile);
    }
}