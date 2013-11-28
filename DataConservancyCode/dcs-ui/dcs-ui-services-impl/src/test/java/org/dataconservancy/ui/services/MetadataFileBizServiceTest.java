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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.ui.dao.ArchiveDepositInfoDAO;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;

/**
 * Tests the methods in the CollectionBizService implementation
 */
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MetadataFileBizServiceTest extends BaseUnitTest {
    
    private ArchiveService archiveService;
    private AuthorizationService authorizationService;
    private RelationshipService relationshipService;
    private ProjectBizService projectBizService;
    private ArchiveDepositInfoDAO adiDAO; 
    private MetadataFileBizServiceImpl underTest;
    
    @Before
    public void setup() {
        archiveService = mock(ArchiveService.class);
        authorizationService = mock(AuthorizationService.class);
        relationshipService = mock(RelationshipService.class);
        projectBizService = mock(ProjectBizService.class);
        adiDAO = mock(ArchiveDepositInfoDAO.class);
        underTest = new MetadataFileBizServiceImpl(archiveService);

        underTest.setAuthorizationService(authorizationService);
        underTest.setRelationshipService(relationshipService);
        underTest.setProjectBizService(projectBizService);
        underTest.setArchiveDepositInfoDao(adiDAO);
    }
    
    @Test
    public void testRetrieveMetadataFile() throws ArchiveServiceException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        adi.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        
        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);
        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        List<MetadataFile> files = new ArrayList<MetadataFile>();
        files.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> result = new ArchiveSearchResult<MetadataFile>(files, 1);
        
        when(archiveService.retrieveMetadataFile(adi.getDepositId())).thenReturn(result);
        
        MetadataFile resultFile = underTest.retrieveMetadataFile(metadataFileOne.getId());
        
        assertEquals(resultFile, metadataFileOne);
    }
    
    @Test
    public void testAddMetadataFileToCollection() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        
        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);
        when(archiveService.listDepositInfo(collectionNoData.getId(), Status.DEPOSITED)).thenReturn(adis);

        when(authorizationService.canUpdateCollection(user, collectionNoData)).thenReturn(true);
        
        //No exceptions should be thrown
        underTest.addNewMetadataFile(collectionNoData, metadataFileOne, user);
        
        verify(archiveService).deposit("deposit", metadataFileOne);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileOne.getId(), collectionNoData.getId());
    }

    @Test
    public void testAddMetadataFileToDataItem() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        metadataFileOne.setParentId(dataItemOne.getId());

        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        //No exceptions should be thrown
        underTest.addNewMetadataFile(dataItemOne, metadataFileOne, user);
        
        verify(archiveService).deposit("deposit", metadataFileOne);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileOne.getId(), dataItemOne.getId());
    }

    @Test
    public void testAddMetadataFileToDataFile() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo dataItemAdi = new ArchiveDepositInfo();
        dataItemAdi.setDepositId("deposit");
        metadataFileOne.setParentId(dataFileOne.getId());
        
        List<ArchiveDepositInfo> dataItemAdis = new ArrayList<ArchiveDepositInfo>();
        dataItemAdis.add(dataItemAdi);
        
        ArchiveDepositInfo dataFileAdi = new ArchiveDepositInfo();
        dataFileAdi.setDepositId("file_deposit");
        dataFileAdi.setObjectId(dataFileOne.getId());
        List<ArchiveDepositInfo> dataFileAdis = new ArrayList<ArchiveDepositInfo>();
        dataFileAdis.add(dataFileAdi);
               
        when(adiDAO.lookupChildren("deposit")).thenReturn(dataFileAdis);

        underTest.setArchiveDepositInfoDao(adiDAO);
        
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(dataItemAdis);
        when(relationshipService.getDataSetForDataFile(dataFileOne)).thenReturn(dataItemOne);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        //No exceptions should be thrown
        underTest.addNewMetadataFile(dataFileOne, metadataFileOne, user);

        verify(archiveService).deposit("file_deposit", metadataFileOne);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileOne.getId(), dataFileOne.getId());
    }

    @Test
    public void testAddMetadataFileToProject() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        metadataFileOne.setParentId(projectOne.getId());

        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);

        when(authorizationService.canUpdateProject(user, projectOne)).thenReturn(true);

        //No exceptions should be thrown
        underTest.addNewMetadataFile(projectOne, metadataFileOne, user);
        
        verify(archiveService).deposit("", metadataFileOne);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileOne.getId(), projectOne.getId());
    }

    @Test
    public void testAddMetadataFileToProjectMetadataFile() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        adi.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adi.setObjectId(metadataFileOne.getId());
        metadataFileOne.setParentId(projectOne.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());
        Set<MetadataFile> srSet = new HashSet<MetadataFile>();
        srSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> sr = new ArchiveSearchResult<MetadataFile>(srSet, 1);

        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        when(archiveService.listDepositInfo(projectOne.getId(), Status.DEPOSITED)).thenReturn(null);
        when(archiveService.retrieveMetadataFile(adi.getDepositId())).thenReturn(sr);
        when(authorizationService.canUpdateProject(user, projectOne)).thenReturn(true);
        when(projectBizService.getProject(projectOne.getId())).thenReturn(projectOne);

        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
        
        verify(archiveService).deposit("deposit", metadataFileTwo);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileTwo.getId(), metadataFileOne.getId());
    }

    @Test
    public void testAddMetadataFileToDataFileMetadataFile() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        adi.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adi.setObjectId(metadataFileOne.getId());
        metadataFileOne.setParentId(dataFileOne.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());
        Set<MetadataFile> srSet = new HashSet<MetadataFile>();
        srSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> sr = new ArchiveSearchResult<MetadataFile>(srSet, 1);

        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        when(archiveService.listDepositInfo(dataFileOne.getId(), Status.DEPOSITED)).thenReturn(null);
        when(archiveService.retrieveMetadataFile(adi.getDepositId())).thenReturn(sr);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);
        when(projectBizService.getProject(projectOne.getId())).thenReturn(null);
        when(relationshipService.getDataSetForDataFileId(dataFileOne.getId())).thenReturn(dataItemOne);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);


        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
        
        verify(archiveService).deposit("deposit", metadataFileTwo);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileTwo.getId(), metadataFileOne.getId());
    }

    @Test
    public void testAddMetadataFileToDataItemMetadataFile() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adiMdF = new ArchiveDepositInfo();
        adiMdF.setDepositId("deposit:1");
        adiMdF.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adiMdF.setObjectId(metadataFileOne.getId());
        List<ArchiveDepositInfo> mdfadis = new ArrayList<ArchiveDepositInfo>();
        mdfadis.add(adiMdF);

        ArchiveDepositInfo adiParent = new ArchiveDepositInfo();
        adiParent.setDepositId("deposit:2");
        adiParent.setObjectType(ArchiveDepositInfo.Type.DATASET);
        adiParent.setObjectId(dataItemOne.getId());
        List<ArchiveDepositInfo> parentadis = new ArrayList<ArchiveDepositInfo>();
        parentadis.add(adiParent);

        metadataFileOne.setParentId(dataItemOne.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());

        Set<DataItem> srSet = new HashSet<DataItem>();
        srSet.add(dataItemOne);
        ArchiveSearchResult<DataItem> srParent = new ArchiveSearchResult<DataItem>(srSet, 1);

        Set<MetadataFile> srMdFSet = new HashSet<MetadataFile>();
        srMdFSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> srMdF = new ArchiveSearchResult<MetadataFile>(srMdFSet, 1);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(mdfadis);
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(parentadis);
        when(archiveService.retrieveDataSet(adiParent.getDepositId())).thenReturn(srParent);
        when(archiveService.retrieveMetadataFile(adiMdF.getDepositId())).thenReturn(srMdF);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);


        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
        
        verify(archiveService).deposit("deposit:1", metadataFileTwo);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileTwo.getId(), metadataFileOne.getId());
    }

    @Test
    public void testAddMetadataFileToCollectionMetadataFile() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adiMdF = new ArchiveDepositInfo();
        adiMdF.setDepositId("deposit:1");
        adiMdF.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adiMdF.setObjectId(metadataFileOne.getId());
        List<ArchiveDepositInfo> mdfadis = new ArrayList<ArchiveDepositInfo>();
        mdfadis.add(adiMdF);

        ArchiveDepositInfo adiParent = new ArchiveDepositInfo();
        adiParent.setDepositId("deposit:2");
        adiParent.setObjectType(ArchiveDepositInfo.Type.COLLECTION);
        adiParent.setObjectId(collectionWithData.getId());
        List<ArchiveDepositInfo> parentadis = new ArrayList<ArchiveDepositInfo>();
        parentadis.add(adiParent);

        metadataFileOne.setParentId(collectionWithData.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());

        Set<Collection> srSet = new HashSet<Collection>();
        srSet.add(collectionWithData);
        ArchiveSearchResult<Collection> srParent = new ArchiveSearchResult<Collection>(srSet, 1);

        Set<MetadataFile> srMdFSet = new HashSet<MetadataFile>();
        srMdFSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> srMdF = new ArchiveSearchResult<MetadataFile>(srMdFSet, 1);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(mdfadis);
        when(archiveService.listDepositInfo(collectionWithData.getId(), Status.DEPOSITED)).thenReturn(parentadis);
        when(archiveService.retrieveCollection(adiParent.getDepositId())).thenReturn(srParent);
        when(archiveService.retrieveMetadataFile(adiMdF.getDepositId())).thenReturn(srMdF);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
        
        verify(archiveService).deposit("deposit:1", metadataFileTwo);
        verify(relationshipService).addMetadataFileToBusinessObject(metadataFileTwo.getId(), metadataFileOne.getId());
    }

    @Test(expected = BizInternalException.class)
    public void testEmptyParentDepositInfoThrowsException() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {

        when(archiveService.listDepositInfo(collectionNoData.getId(), Status.DEPOSITED)).thenReturn(null);

        when(authorizationService.canUpdateCollection(user, collectionNoData)).thenReturn(true);
        
        underTest.addNewMetadataFile(collectionNoData, metadataFileOne, user);
    }
    
    @Test(expected = BizInternalException.class)
    public void testArchiveItemEmptyParentIdThrowsException() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("");
        metadataFileOne.setParentId(dataItemOne.getId());

        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        underTest.addNewMetadataFile(dataItemOne, metadataFileOne, user);
    }
    
    @Test(expected = BizInternalException.class)
    public void testDataFileParentEmptyDepositInfoThrowsException() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo dataItemAdi = new ArchiveDepositInfo();
        dataItemAdi.setDepositId("deposit");
        metadataFileOne.setParentId(dataFileOne.getId());
        
        List<ArchiveDepositInfo> dataItemAdis = new ArrayList<ArchiveDepositInfo>();
        dataItemAdis.add(dataItemAdi);
        
        ArchiveDepositInfo dataFileAdi = new ArchiveDepositInfo();
        dataFileAdi.setDepositId("file_deposit");
        dataFileAdi.setObjectId(dataFileOne.getId());
        List<ArchiveDepositInfo> dataFileAdis = new ArrayList<ArchiveDepositInfo>();
        dataFileAdis.add(dataFileAdi);
               
        when(adiDAO.lookupChildren("deposit")).thenReturn(dataFileAdis);

        underTest.setArchiveDepositInfoDao(adiDAO);
        
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(null);
        when(relationshipService.getDataSetForDataFile(dataFileOne)).thenReturn(dataItemOne);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        underTest.addNewMetadataFile(dataFileOne, metadataFileOne, user);
    }
    
    @Test(expected = BizInternalException.class)
    public void testDataFileEmptyParentDepositIdThrowsException() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo dataItemAdi = new ArchiveDepositInfo();
        dataItemAdi.setDepositId("deposit");
        metadataFileOne.setParentId(dataFileOne.getId());
        
        List<ArchiveDepositInfo> dataItemAdis = new ArrayList<ArchiveDepositInfo>();
        dataItemAdis.add(dataItemAdi);
        
        ArchiveDepositInfo dataFileAdi = new ArchiveDepositInfo();
        dataFileAdi.setDepositId("");
        dataFileAdi.setObjectId(dataFileOne.getId());
        List<ArchiveDepositInfo> dataFileAdis = new ArrayList<ArchiveDepositInfo>();
        dataFileAdis.add(dataFileAdi);
               
        when(adiDAO.lookupChildren("deposit")).thenReturn(dataFileAdis);

        underTest.setArchiveDepositInfoDao(adiDAO);
        
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(dataItemAdis);
        when(relationshipService.getDataSetForDataFile(dataFileOne)).thenReturn(dataItemOne);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        underTest.addNewMetadataFile(dataFileOne, metadataFileOne, user);
    }
    
    @Test(expected = BizInternalException.class)
    public void testDataFileReturnsNullDataItemThrowsException() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        dataItemOne.setId("");
        ArchiveDepositInfo dataItemAdi = new ArchiveDepositInfo();
        dataItemAdi.setDepositId("deposit");
        metadataFileOne.setParentId(dataFileOne.getId());
        
        List<ArchiveDepositInfo> dataItemAdis = new ArrayList<ArchiveDepositInfo>();
        dataItemAdis.add(dataItemAdi);
        
        ArchiveDepositInfo dataFileAdi = new ArchiveDepositInfo();
        dataFileAdi.setDepositId("file_deposit");
        dataFileAdi.setObjectId(dataFileOne.getId());
        List<ArchiveDepositInfo> dataFileAdis = new ArrayList<ArchiveDepositInfo>();
        dataFileAdis.add(dataFileAdi);
               
        when(adiDAO.lookupChildren("deposit")).thenReturn(dataFileAdis);

        underTest.setArchiveDepositInfoDao(adiDAO);
        
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(dataItemAdis);
        when(relationshipService.getDataSetForDataFile(dataFileOne)).thenReturn(dataItemOne);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        underTest.addNewMetadataFile(dataFileOne, metadataFileOne, user);
    }

    @Test(expected = BizInternalException.class)
    public void testDataFileParentLookupExceptionThrowsBizException() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo dataItemAdi = new ArchiveDepositInfo();
        dataItemAdi.setDepositId("deposit");
        metadataFileOne.setParentId(dataFileOne.getId());
        
        List<ArchiveDepositInfo> dataItemAdis = new ArrayList<ArchiveDepositInfo>();
        dataItemAdis.add(dataItemAdi);
        
        ArchiveDepositInfo dataFileAdi = new ArchiveDepositInfo();
        dataFileAdi.setDepositId("file_deposit");
        dataFileAdi.setObjectId(dataFileOne.getId());
        List<ArchiveDepositInfo> dataFileAdis = new ArrayList<ArchiveDepositInfo>();
        dataFileAdis.add(dataFileAdi);
               
        when(adiDAO.lookupChildren("deposit")).thenReturn(dataFileAdis);

        underTest.setArchiveDepositInfoDao(adiDAO);
        
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(dataItemAdis);
        when(relationshipService.getDataSetForDataFile(dataFileOne)).thenThrow(new RelationshipConstraintException("foo"));
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(true);

        underTest.addNewMetadataFile(dataFileOne, metadataFileOne, user);
    }
    
    @Test(expected = BizPolicyException.class)
    public void testAddMetadataFileToCollectionWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        when(authorizationService.canUpdateCollection(newUser, collectionNoData)).thenReturn(false);
        
        underTest.addNewMetadataFile(collectionNoData, metadataFileOne, newUser);
    }

    @Test(expected = BizPolicyException.class)
    public void testAddMetadataFileToProjectWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        when(authorizationService.canUpdateProject(newUser, projectOne)).thenReturn(false);

        underTest.addNewMetadataFile(projectOne, metadataFileOne, newUser);
    }

    @Test(expected = BizPolicyException.class)
    public void testAddMetadataFileToDataItemWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canUpdateCollection(newUser, collectionWithData)).thenReturn(false);

        underTest.addNewMetadataFile(dataItemOne, metadataFileOne, newUser);
    }


    @Test(expected = BizPolicyException.class)
    public void testAddMetadataFileToDataFileWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        when(relationshipService.getDataSetForDataFile(dataFileOne)).thenReturn(dataItemOne);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);
        when(authorizationService.canUpdateCollection(newUser, collectionWithData)).thenReturn(false);

        underTest.addNewMetadataFile(dataFileOne, metadataFileOne, newUser);
    }

    @Test(expected=BizPolicyException.class)
    public void testAddMetadataFileToProjectMetadataFileWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        adi.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adi.setObjectId(metadataFileOne.getId());
        metadataFileOne.setParentId(projectOne.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());
        Set<MetadataFile> srSet = new HashSet<MetadataFile>();
        srSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> sr = new ArchiveSearchResult<MetadataFile>(srSet, 1);

        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        when(archiveService.listDepositInfo(projectOne.getId(), Status.DEPOSITED)).thenReturn(null);
        when(archiveService.retrieveMetadataFile(adi.getDepositId())).thenReturn(sr);
        when(authorizationService.canUpdateProject(user, projectOne)).thenReturn(false);
        when(projectBizService.getProject(projectOne.getId())).thenReturn(projectOne);
        when(relationshipService.getDataSetForDataFileId(projectOne.getId())).thenReturn(null);
        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
    }

    @Test(expected=BizPolicyException.class)
    public void testAddMetadataFileToDataFileMetadataFileWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        adi.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adi.setObjectId(metadataFileOne.getId());
        metadataFileOne.setParentId(dataFileOne.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());
        Set<MetadataFile> srSet = new HashSet<MetadataFile>();
        srSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> sr = new ArchiveSearchResult<MetadataFile>(srSet, 1);

        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        when(archiveService.listDepositInfo(dataFileOne.getId(), Status.DEPOSITED)).thenReturn(null);
        when(archiveService.retrieveMetadataFile(adi.getDepositId())).thenReturn(sr);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(false);
        when(projectBizService.getProject(projectOne.getId())).thenReturn(null);
        when(relationshipService.getDataSetForDataFileId(dataFileOne.getId())).thenReturn(dataItemOne);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);


        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
    }

    @Test(expected = BizPolicyException.class)
    public void testAddMetadataFileToDataItemMetadataFileWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adiMdF = new ArchiveDepositInfo();
        adiMdF.setDepositId("deposit:1");
        adiMdF.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adiMdF.setObjectId(metadataFileOne.getId());
        List<ArchiveDepositInfo> mdfadis = new ArrayList<ArchiveDepositInfo>();
        mdfadis.add(adiMdF);

        ArchiveDepositInfo adiParent = new ArchiveDepositInfo();
        adiParent.setDepositId("deposit:2");
        adiParent.setObjectType(ArchiveDepositInfo.Type.DATASET);
        adiParent.setObjectId(dataItemOne.getId());
        List<ArchiveDepositInfo> parentadis = new ArrayList<ArchiveDepositInfo>();
        parentadis.add(adiParent);

        metadataFileOne.setParentId(dataItemOne.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());

        Set<DataItem> srSet = new HashSet<DataItem>();
        srSet.add(dataItemOne);
        ArchiveSearchResult<DataItem> srParent = new ArchiveSearchResult<DataItem>(srSet, 1);

        Set<MetadataFile> srMdFSet = new HashSet<MetadataFile>();
        srMdFSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> srMdF = new ArchiveSearchResult<MetadataFile>(srMdFSet, 1);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(mdfadis);
        when(archiveService.listDepositInfo(dataItemOne.getId(), Status.DEPOSITED)).thenReturn(parentadis);
        when(archiveService.retrieveDataSet(adiParent.getDepositId())).thenReturn(srParent);
        when(archiveService.retrieveMetadataFile(adiMdF.getDepositId())).thenReturn(srMdF);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(false);
        when(relationshipService.getCollectionForDataSet(dataItemOne)).thenReturn(collectionWithData);


        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
    }

    @Test(expected=BizPolicyException.class)
    public void testAddMetadataFileToCollectionMetadataFileWithoutPermission() throws RelationshipConstraintException, ArchiveServiceException, BizInternalException, BizPolicyException {
        ArchiveDepositInfo adiMdF = new ArchiveDepositInfo();
        adiMdF.setDepositId("deposit:1");
        adiMdF.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        adiMdF.setObjectId(metadataFileOne.getId());
        List<ArchiveDepositInfo> mdfadis = new ArrayList<ArchiveDepositInfo>();
        mdfadis.add(adiMdF);

        ArchiveDepositInfo adiParent = new ArchiveDepositInfo();
        adiParent.setDepositId("deposit:2");
        adiParent.setObjectType(ArchiveDepositInfo.Type.COLLECTION);
        adiParent.setObjectId(collectionWithData.getId());
        List<ArchiveDepositInfo> parentadis = new ArrayList<ArchiveDepositInfo>();
        parentadis.add(adiParent);

        metadataFileOne.setParentId(collectionWithData.getId());
        metadataFileTwo.setParentId(metadataFileOne.getId());

        Set<Collection> srSet = new HashSet<Collection>();
        srSet.add(collectionWithData);
        ArchiveSearchResult<Collection> srParent = new ArchiveSearchResult<Collection>(srSet, 1);

        Set<MetadataFile> srMdFSet = new HashSet<MetadataFile>();
        srMdFSet.add(metadataFileOne);
        ArchiveSearchResult<MetadataFile> srMdF = new ArchiveSearchResult<MetadataFile>(srMdFSet, 1);

        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(mdfadis);
        when(archiveService.listDepositInfo(collectionWithData.getId(), Status.DEPOSITED)).thenReturn(parentadis);
        when(archiveService.retrieveCollection(adiParent.getDepositId())).thenReturn(srParent);
        when(archiveService.retrieveMetadataFile(adiMdF.getDepositId())).thenReturn(srMdF);
        when(authorizationService.canDepositToCollection(user, collectionWithData)).thenReturn(false);

        //No exceptions should be thrown
        underTest.addNewMetadataFile(metadataFileOne, metadataFileTwo, user);
    }

    @Test
    public void testRetrieveMetadataFileNullId() throws ArchiveServiceException {
        MetadataFile file = underTest.retrieveMetadataFile(null);
        
        assertNull(file);
    }
    
    @Test
    public void testGetLastModified() {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        adi.setDepositDateTime(DateTime.now());
        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);
        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        
        DateTime lastModified = underTest.getLastModifiedDate(metadataFileOne.getId());
        
        assertEquals(adi.getDepositDateTime(), lastModified);
    }
    
    @Test
    public void testGetLastModifiedNullID() {
        DateTime lastModified = underTest.getLastModifiedDate(null);
        assertNull(lastModified);
    }
    
    @Test(expected = BizInternalException.class)
    public void testAddMetadataFileArchiveException() throws Exception {
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        
        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);
        when(archiveService.listDepositInfo(collectionNoData.getId(), Status.DEPOSITED)).thenReturn(adis);
        
        when(authorizationService.canUpdateCollection(user, collectionNoData)).thenReturn(true);

        when(archiveService.deposit(adi.getDepositId(), metadataFileOne)).thenThrow(new ArchiveServiceException());
        
        underTest.addNewMetadataFile(collectionNoData, metadataFileOne, user);
    }
    
    @Test(expected = ArchiveServiceException.class)
    public void testRetrieveMetadataFileArchiveException() throws ArchiveServiceException {
        
        ArchiveDepositInfo adi = new ArchiveDepositInfo();
        adi.setDepositId("deposit");
        adi.setObjectType(ArchiveDepositInfo.Type.METADATA_FILE);
        
        List<ArchiveDepositInfo> adis = new ArrayList<ArchiveDepositInfo>();
        adis.add(adi);
        when(archiveService.listDepositInfo(metadataFileOne.getId(), Status.DEPOSITED)).thenReturn(adis);
        
        when(archiveService.retrieveMetadataFile(adi.getDepositId())).thenThrow(new ArchiveServiceException());
        
        underTest.retrieveMetadataFile(metadataFileOne.getId());
    }
}