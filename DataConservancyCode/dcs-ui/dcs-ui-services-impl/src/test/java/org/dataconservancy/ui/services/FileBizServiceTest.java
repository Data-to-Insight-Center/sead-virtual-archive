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

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.annotation.DirtiesContext;

/**
 * This class tests the File Biz Service by testing retrieving an id
 */
@DirtiesDatabase
@DirtiesContext
public class FileBizServiceTest extends BaseUnitTest {
    
    @Autowired
    private ArchiveService archiveService;
    
    @Autowired
    private AuthorizationService authorizationService;
    
    @Autowired
    private RelationshipService relationshipService;
    
    private FileBizServiceImpl fileBizService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Before 
    public void setup() throws ArchiveServiceException, IOException, RelationshipConstraintException {
        fileBizService = new FileBizServiceImpl();
        fileBizService.setArchiveService(archiveService);
        fileBizService.setAuthorizationService(authorizationService);
        fileBizService.setRelationshipService(relationshipService);
        
        relationshipService.addDepositorToCollection(unauthorizedUser, collectionWithData);
        relationshipService.addAdministratorToProject(projectOne, user);
    }    
    
    /**
     * Tests that given a user with admin permissions a file object can be returned. 
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testRetrieveCorrectFile() throws RelationshipConstraintException, ArchiveServiceException, BizPolicyException {
        DataFile file = fileBizService.getFile(dataFileOne.getId(), admin);
        assertNotNull(file);
        assertTrue(file.getId().equalsIgnoreCase(dataFileOne.getId()));
        assertTrue(file.getName().equalsIgnoreCase(dataFileOne.getName()));
    }
    
    /**
     * This test is to document existing behavior after a file is removed from it's dataset null is returned when the file is requested
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testRetrieveFileRemovedFromDataSet() throws RelationshipConstraintException, ArchiveServiceException, BizPolicyException {
        relationshipService.removeDataFileFromDataSet(dataFileOne, dataItemOne);
        
        DataFile file = fileBizService.getFile(dataFileOne.getId(), admin);
        assertNull(file);
        
        //Add the file back to the data set
        relationshipService.addDataFileToDataSet(dataFileOne, dataItemOne);
    }
    
    /**
     * Test that after a data set is updated with a new file both files can be retrieved.
     * @throws IOException
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testFileFromUpdatedDataSet() throws IOException, RelationshipConstraintException, ArchiveServiceException, BizPolicyException {
        
        dataItemOne.addFile(dataFileTwo);
        relationshipService.removeDataFileFromDataSet(dataFileTwo, dataItemTwo);
        relationshipService.addDataFileToDataSet(dataFileTwo, dataItemOne);
     
        archiveService.deposit(collectionWithDataDepositID, dataItemOne);
        archiveService.pollArchive();
        
        DataFile file = fileBizService.getFile(dataFileOne.getId(), admin);
        assertNotNull(file);
        assertTrue(file.getId().equalsIgnoreCase(dataFileOne.getId()));
        assertTrue(file.getName().equalsIgnoreCase(dataFileOne.getName()));
        
        file = fileBizService.getFile(dataFileTwo.getId(), admin);
        assertNotNull(file);
        assertTrue(file.getId().equalsIgnoreCase(dataFileTwo.getId()));
        assertTrue(file.getName().equalsIgnoreCase(dataFileTwo.getName()));
    }
    
    /**
     * This test is to document existing behavior when a non existent file id is passed in should return null.
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testNonExistentFileId() throws RelationshipConstraintException, ArchiveServiceException, BizPolicyException {
        DataFile file = fileBizService.getFile("foo", admin);
        assertNull(file);
    }
    
    /**
     * This test is to document existing behavior when a non existent user is passed in: should succeed.
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     */
    @Test
    public void testNullUser() throws RelationshipConstraintException, ArchiveServiceException {
        boolean caughtException = false;
        try {
            fileBizService.getFile(dataFileOne.getId(), null);
        } catch (BizPolicyException e) {
            caughtException = true;
            assertEquals(Type.AUTHENTICATION_ERROR, e.getType());
        }
        
        assertFalse(caughtException);
    }
    
    /**
     * Tests that a user that is an admin on the projectOne containing the file can retrieve the file.
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testProjectAdmin() throws RelationshipConstraintException, ArchiveServiceException, BizPolicyException {
        DataFile file = fileBizService.getFile(dataFileOne.getId(), user);
        assertNotNull(file);
    }
    
    /**
     * Tests that a user who is a depositor on the collectionWithData can view the file.
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     * @throws BizPolicyException
     */
    @Test
    public void testCollectionDepositor() throws RelationshipConstraintException, ArchiveServiceException, BizPolicyException {
        DataFile file = fileBizService.getFile(dataFileOne.getId(), unauthorizedUser);
        assertNotNull(file);
    }
    
    /**
     * Tests that trying to access the file as a user without permissions succeeds.
     * @throws RelationshipConstraintException
     * @throws ArchiveServiceException
     */
    @Test
    public void testUserWithoutPermissions() throws RelationshipConstraintException, ArchiveServiceException, BizPolicyException {

        DataFile file = fileBizService.getFile(dataFileOne.getId(), newUser);

        assertNotNull(file);
    }
    
    /**
     * Test that getting the last modified date returns the date the data set was last updated.
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetLastModifiedDate() throws RelationshipConstraintException {
        DateTime lastModified = fileBizService.getLastModifiedDate(dataFileOne.getId());
        assertEquals(dataItemOneDepositDate, lastModified);
    }
    
    /**
     * This test is to document existing behavior getting last modified date with a non existent file id should return null.
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetModifiedDateBadId() throws RelationshipConstraintException {
        DateTime lastModified = fileBizService.getLastModifiedDate("foo");
        assertNull(lastModified);        		
    }
    
    /**
     * Tests that if the data set is updated the new dataset modified date will be returned. 
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetModifiedDateReturnsUpdatedDate() throws IOException, ArchiveServiceException, RelationshipConstraintException {
        java.io.File tmpTwo = java.io.File.createTempFile("testTwoFile", null);
        tmpTwo.deleteOnExit();

        PrintWriter out = new PrintWriter(tmpTwo);

        out.println("testTwo");
        out.close();

        dataFileTwo.setId(dataFileTwo.getId());
        dataFileTwo.setSource(tmpTwo.toURI().toURL().toExternalForm());
        dataFileTwo.setName(dataFileTwo.getName());

        Resource r = new UrlResource(dataFileTwo.getSource());
        dataFileTwo.setSize(r.contentLength());
        dataFileTwo.setPath(tmpTwo.getParent());
        dataItemOne.addFile(dataFileTwo);
        
        relationshipService.removeDataFileFromDataSet(dataFileTwo, dataItemTwo);
        relationshipService.addDataFileToDataSet(dataFileTwo, dataItemOne);

        archiveService.deposit(collectionWithDataDepositID, dataItemOne);
        archiveService.pollArchive();
        
        List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED);
        DateTime depositDate = depositInfo.get(0).getDepositDateTime();
        
        DateTime lastModified = fileBizService.getLastModifiedDate(dataFileOne.getId());
        assertEquals(depositDate, lastModified);
    }
    
    /**
     * This test is to demonstrate existing behavior if a file is removed from a dataset null should returned when getting the modified date for that file.
     * @throws RelationshipConstraintException
     */
    @Test
    public void testGetModifiedDateForDataFileRemovedFromDataSet() throws RelationshipConstraintException {
        relationshipService.removeDataFileFromDataSet(dataFileOne, dataItemOne);
        DateTime lastModified = fileBizService.getLastModifiedDate(dataFileOne.getId());
        assertNull(lastModified);    
    }

    /**
     * Test that an authentication exception is thrown in getFile when the user is not logged in.
     */
    @Test
    public void testAuthenticationExceptionInGetFile() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canRetrieveDataFile(any(Person.class), any(DataFile.class))).thenReturn(false);

        List<ArchiveDepositInfo> infoList = new ArrayList<ArchiveDepositInfo>();
        ArchiveDepositInfo archiveDepositInfo = new ArchiveDepositInfo();
        archiveDepositInfo.setDepositId(dataItemOne.getId() + "depositId");
        infoList.add(archiveDepositInfo);

        List<DataItem> dataItems = new ArrayList<DataItem>();
        dataItems.add(dataItemOne);
        ArchiveSearchResult<DataItem> archiveSearchResult = new ArchiveSearchResult<DataItem>(dataItems, 1);

        ArchiveService mockArchiveService = mock(ArchiveService.class);
        when(mockArchiveService.listDepositInfo(anyString(), any(Status.class))).thenReturn(infoList);
        when(mockArchiveService.retrieveDataSet(archiveDepositInfo.getDepositId())).thenReturn(archiveSearchResult);

        RelationshipService mockRelationshipService = mock(RelationshipService.class);
        when(mockRelationshipService.getDataSetForDataFileId(anyString())).thenReturn(dataItemOne);


        fileBizService.setAuthorizationService(mockAuthorizationService);
        fileBizService.setArchiveService(mockArchiveService);
        fileBizService.setRelationshipService(mockRelationshipService);

        try {
            fileBizService.getFile(dataFileOne.getId(), null);
        }
        catch (BizPolicyException e) {
            //If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canRetrieveDataFile(null, dataFileOne);

            assertTrue("Expected an authorization error in the BizPolicyException!", Type.AUTHENTICATION_ERROR == e.getType());

            return;
        }

        fail("Expected a BizPolicyException!");
    }

    /**
     * Test that an authorization exception is thrown in getFile when the user does not have permission.
     */
    @Test
    public void testAuthorizationExceptionInGetFile() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canRetrieveDataFile(any(Person.class), any(DataFile.class))).thenReturn(false);

        List<ArchiveDepositInfo> infoList = new ArrayList<ArchiveDepositInfo>();
        ArchiveDepositInfo archiveDepositInfo = new ArchiveDepositInfo();
        archiveDepositInfo.setDepositId(dataItemOne.getId() + "depositId");
        infoList.add(archiveDepositInfo);
        List<DataItem> dataItems = new ArrayList<DataItem>();
        dataItems.add(dataItemOne);
        ArchiveSearchResult<DataItem> archiveSearchResult = new ArchiveSearchResult<DataItem>(dataItems, 1);

        ArchiveService mockArchiveService = mock(ArchiveService.class);
        when(mockArchiveService.listDepositInfo(anyString(), any(Status.class))).thenReturn(infoList);
        when(mockArchiveService.retrieveDataSet(archiveDepositInfo.getDepositId())).thenReturn(archiveSearchResult);

        RelationshipService mockRelationshipService = mock(RelationshipService.class);
        when(mockRelationshipService.getDataSetForDataFileId(anyString())).thenReturn(dataItemOne);

        UserService mockUserService = mock(UserService.class);
        when(mockUserService.get(admin.getId())).thenReturn(admin);

        fileBizService.setAuthorizationService(mockAuthorizationService);
        fileBizService.setArchiveService(mockArchiveService);
        fileBizService.setRelationshipService(mockRelationshipService);
        fileBizService.setUserService(mockUserService);

        try {
            fileBizService.getFile(dataFileOne.getId(), admin);
        }
        catch (BizPolicyException e) {
            //If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canRetrieveDataFile(admin, dataFileOne);

            assertTrue("Expected an authorization error in the BizPolicyException!", Type.AUTHORIZATION_ERROR == e.getType());

            return;
        }

        fail("Expected a BizPolicyException!");
    }


}