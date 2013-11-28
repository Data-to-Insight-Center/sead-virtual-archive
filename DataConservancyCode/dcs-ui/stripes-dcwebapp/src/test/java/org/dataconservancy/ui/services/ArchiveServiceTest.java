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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.ui.DirtiesContextBaseUnitTest;
import org.dataconservancy.ui.dcpmap.CollectionMapper;
import org.dataconservancy.ui.dcpmap.DataFileMapper;
import org.dataconservancy.ui.dcpmap.DataSetMapper;
import org.dataconservancy.ui.dcpmap.MetadataFileMapper;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

/**
 * A suite of unit tests for the {@link ArchiveServiceImpl}, based on {@code Autowired} Spring components and mock
 * objects.  A cousin class, {@link ArchiveServiceImplTest} takes another approach and uses Mockito to test the
 * {@code ArchiveServiceImpl}.
 *
 * @see ArchiveServiceImplTest
 */
@DirtiesDatabase
@DirtiesContext
public class ArchiveServiceTest extends DirtiesContextBaseUnitTest {
    @Autowired
    private DataSetMapper mockDatasetMapper;

    @Autowired
    @Qualifier("mockDataFileMapper")
    private DataFileMapper mockDatafileMapper;

    @Autowired
    private CollectionMapper mockCollectionMapper;
    
    @Autowired
    private MetadataFileMapper mockMetadataFileMapper;
    
    @Autowired
    private MetadataFormatMapper metadataFormatMapper;

    @Autowired
    @Qualifier("org.dataconservancy.ui.services.InMemoryArchiveServiceImpl")
    private ArchiveService inMemoryArchiveService;

    @Autowired
    @Qualifier("AlwaysFailingInMemoryArchiveServiceImpl")
    private ArchiveService alwaysFailingInMemoryArchiveService;

    @Resource(name = "mockDepositDocumentResolver")
    private DepositDocumentResolver mockDepositDocumentResolver;

    @Autowired
    private MockArchiveUtil mockArchiveUtil;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Autowired
    private Profile<DataItem> dataItemProfile;

    @Before
    public void setUp() {
        MockArchiveDepositInfoDAO mockArchiveDepositInfoDAO = new MockArchiveDepositInfoDAO();
        inMemoryArchiveService = new InMemoryArchiveServiceImpl(mockDatasetMapper, dataItemProfile, mockCollectionMapper,
                mockMetadataFileMapper, mockDatafileMapper, metadataFormatMapper, mockDepositDocumentResolver, mockArchiveUtil, idService, false, mockArchiveDepositInfoDAO);
        alwaysFailingInMemoryArchiveService = new InMemoryArchiveServiceImpl(mockDatasetMapper, dataItemProfile, mockCollectionMapper,
                mockMetadataFileMapper, mockDatafileMapper, metadataFormatMapper, mockDepositDocumentResolver, mockArchiveUtil, idService, true, mockArchiveDepositInfoDAO);
        mockArchiveUtil.getEntities().clear();
    }

    /**
     * Relies on knowing the mock implementation makes deposit synchronous.
     *
     * @throws Exception
     */
    @Test
    public void testDepositCollection() throws Exception {
       
        String deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(deposit_id);

        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(deposit_id));

        assertTrue(!inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                deposit_id));
        assertTrue(inMemoryArchiveService.listCollections(Status.PENDING).contains(
                deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(deposit_id));

        ArchiveSearchResult<Collection> results = inMemoryArchiveService.retrieveCollection(deposit_id);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(collectionNoData);
        assertEquals(collectionNoData, test);

        assertTrue(inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                deposit_id));
        assertTrue(!inMemoryArchiveService.listCollections(Status.PENDING).contains(
                deposit_id));
    }


    @Test
    public void testDepositMetadataFile() throws Exception {

        //First we need to deposit the collection
        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);

        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        assertTrue(!inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                col_deposit_id));
        assertTrue(inMemoryArchiveService.listCollections(Status.PENDING).contains(
                col_deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        //Now deposit the metadata file into the collection
        String mf_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, metadataFileOne);

        assertNotNull(mf_deposit_id);
        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(mf_deposit_id));

        assertTrue(!inMemoryArchiveService.listMetadataFiles(Status.DEPOSITED).contains(
                mf_deposit_id));
        assertTrue(inMemoryArchiveService.listMetadataFiles(Status.PENDING).contains(
                mf_deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(mf_deposit_id));

        assertTrue(inMemoryArchiveService.listMetadataFiles(Status.DEPOSITED).contains(
                mf_deposit_id));
        assertTrue(!inMemoryArchiveService.listMetadataFiles(Status.PENDING).contains(
                mf_deposit_id));

        MetadataFile test_mf = null;

        ArchiveSearchResult<MetadataFile> metadataFileResults = inMemoryArchiveService.retrieveMetadataFile(mf_deposit_id);
        Iterator<MetadataFile> mfResultIter = metadataFileResults.getResults().iterator();
        if (mfResultIter.hasNext()) {
            test_mf = mfResultIter.next();
        }
        assertNotNull(test_mf);

        assertEquals(metadataFileOne.getId(), test_mf.getId());
        assertEquals(metadataFileOne.getFormat(), test_mf.getFormat());
        assertEquals(metadataFileOne.getMetadataFormatId(), test_mf.getMetadataFormatId());
        assertEquals(metadataFileOne.getName(), test_mf.getName());
    }
    /**
     * Relies on knowing the mock implementation makes deposit synchronous.
     *
     * @throws Exception
     */
    @Test
    public void testDepositCollectionWithMetadataFiles() throws Exception {

        //First we need to deposit the collection
        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);

        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        assertTrue(!inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                col_deposit_id));
        assertTrue(inMemoryArchiveService.listCollections(Status.PENDING).contains(
                col_deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        //Now deposit the metadata file into the collection
        String mf_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, metadataFileOne);

        assertNotNull(mf_deposit_id);
        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(mf_deposit_id));

        assertTrue(!inMemoryArchiveService.listMetadataFiles(Status.DEPOSITED).contains(
                mf_deposit_id));
        assertTrue(inMemoryArchiveService.listMetadataFiles(Status.PENDING).contains(
                mf_deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(mf_deposit_id));

        assertTrue(inMemoryArchiveService.listMetadataFiles(Status.DEPOSITED).contains(
                mf_deposit_id));
        assertTrue(!inMemoryArchiveService.listMetadataFiles(Status.PENDING).contains(
                mf_deposit_id));
        
        //Finally update the collection with the new metadata file added

        String col_update_deposit_id = inMemoryArchiveService.deposit(collectionNoData);
        
        assertNotNull(col_update_deposit_id);
        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(col_update_deposit_id));
        
        assertTrue(!inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                col_update_deposit_id));
        assertTrue(inMemoryArchiveService.listCollections(Status.PENDING).contains(
                col_update_deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_update_deposit_id));

        assertTrue(inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                col_update_deposit_id));
        assertTrue(!inMemoryArchiveService.listCollections(Status.PENDING).contains(
                col_update_deposit_id));
        
        ArchiveSearchResult<Collection> results = inMemoryArchiveService.retrieveCollection(col_update_deposit_id);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test_col = null;
        if (resultIter.hasNext()) {
            test_col = resultIter.next();
        }
        assertNotNull(test_col);
        assertEquals(collectionNoData, test_col);
    }

    @Test
    public void testGetDepositStatus() throws Exception {
        assertEquals(null, inMemoryArchiveService.getDepositStatus("doesnotexist"));

        String deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(deposit_id));
    }

    @Test
    public void testListCollections() throws Exception {
        assertEquals(0, inMemoryArchiveService.listCollections(Status.PENDING).size());
        assertEquals(0, inMemoryArchiveService.listCollections(Status.FAILED).size());
        assertEquals(0, inMemoryArchiveService.listCollections(Status.DEPOSITED)
                .size());

        String deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(deposit_id));
        assertEquals(1, inMemoryArchiveService.listCollections(Status.PENDING).size());
        assertTrue(inMemoryArchiveService.listCollections(Status.PENDING).contains(
                deposit_id));
        assertEquals(0, inMemoryArchiveService.listCollections(Status.FAILED).size());
        assertEquals(0, inMemoryArchiveService.listCollections(Status.DEPOSITED)
                .size());

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(deposit_id));
        assertEquals(0, inMemoryArchiveService.listCollections(Status.PENDING).size());
        assertEquals(0, inMemoryArchiveService.listCollections(Status.FAILED).size());
        assertEquals(1, inMemoryArchiveService.listCollections(Status.DEPOSITED)
                .size());
        assertTrue(inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                deposit_id));
    }

    @Test
    public void testRetrieveCollection() throws Exception {

        String deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(deposit_id));

        ArchiveSearchResult<Collection> results = inMemoryArchiveService.retrieveCollection(deposit_id);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(test);
        assertEquals(collectionNoData, test);
    }

    @Test
    public void testListDepositInfoSimple() throws Exception {

        String depositId = inMemoryArchiveService.deposit(collectionNoData);
        inMemoryArchiveService.pollArchive();

        List<ArchiveDepositInfo> depositInfos = inMemoryArchiveService.listDepositInfo(collectionNoData.getId(), null);
        assertEquals(1, depositInfos.size());

        ArchiveDepositInfo depositInfo = depositInfos.get(0);
        assertNotNull(depositInfo.getDepositDateTime());
        assertEquals(collectionNoData.getId(), depositInfo.getObjectId());
        assertEquals(depositId, depositInfo.getDepositId());
    }

    @Test
    public void testListDepositInfoWithMultipleUpdatesToSingleCollection() throws Exception {

        final int transactionCount = 3;
        final List<String> depositIds = new ArrayList<String>(transactionCount);

        for (int i = 0; i < transactionCount; i++) {
            depositIds.add(inMemoryArchiveService.deposit(collectionNoData));
            inMemoryArchiveService.pollArchive();
            Thread.sleep(1000); // sleep a second to give enough time for the DateTime to increment
        }

        Collections.reverse(depositIds);

        final List<ArchiveDepositInfo> depositInfos = inMemoryArchiveService.listDepositInfo(collectionNoData.getId(), null);
        assertEquals("Expected a ArchiveDepositInfo for each transaction,", transactionCount, depositInfos.size());

        DateTime depositDate = DateTime.now().plusYears(1);  // initialize to some time in the future.
        for (int i = 0; i < transactionCount; i++) {
            final ArchiveDepositInfo info = depositInfos.get(i);

            // assert the depositInfos List is ordered correctly
            assertEquals("Expected deposit " + (i + 1) + " to have deposit id " + depositIds.get(i),
                    depositIds.get(i), info.getDepositId());

            assertTrue("Expected this Deposit DateTime (" + info.getDepositDateTime() + ") to be chronologically older " +
                    "than the previous Deposit DateTime (" + depositDate + ")",
                    depositDate.compareTo(info.getDepositDateTime()) > 0);

            // Store the current deposit DateTime for the next iteration
            depositDate = info.getDepositDateTime();
        }
    }

    @Test
    public void testDepositDataSetIntoCollection() throws Exception {

        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);

        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        assertTrue(!inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(
                col_deposit_id));
        assertTrue(inMemoryArchiveService.listCollections(Status.PENDING).contains(
                col_deposit_id));

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        // Depositing a DataItem invokes pollArchive() behind the scenes, because deposit(String, DataItem)
        // will deposit the DataItem, poll, then deposit each DataFile.
        // Therefore, after depositing a DataItem, the status will be DEPOSITED, not PENDING.
        String ds_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemOne);
        assertNotNull(ds_deposit_id);
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_deposit_id));
        assertTrue(inMemoryArchiveService.listDataSets(Status.DEPOSITED).contains(
                ds_deposit_id));
        assertTrue(!inMemoryArchiveService.listDataSets(Status.PENDING).contains(
                ds_deposit_id));
        inMemoryArchiveService.pollArchive();

        ArchiveSearchResult<Collection> results = inMemoryArchiveService.retrieveCollection(col_deposit_id);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test_col = null;
        if (resultIter.hasNext()) {
            test_col = resultIter.next();
        }
        assertNotNull(test_col);
        assertEquals(collectionNoData, test_col);

        DataItem test_ds = null;

        ArchiveSearchResult<DataItem> dataSetResults = inMemoryArchiveService.retrieveDataSet(ds_deposit_id);
        Iterator<DataItem> dsResultIter = dataSetResults.getResults().iterator();
        if (dsResultIter.hasNext()) {
            test_ds = dsResultIter.next();
        }
        assertNotNull(test_ds);

        assertEquals(dataItemOne.getId(), test_ds.getId());
        assertEquals(dataItemOne.getDescription(), test_ds.getDescription());

        assertEquals(1, test_ds.getFiles().size());
        DataFile test_data_file = test_ds.getFiles().get(0);

        assertEquals(dataFileOne.getName(), test_data_file.getName());
        assertEquals(dataFileOne.getId(), test_data_file.getId());
    }

    /**
     * Deposits a collection using the standard archive service (to verify the assumption that it will work).  It
     * then deposits the same collection to an archive service which will always fail the ingest (by returning
     * "ingest.failed" events).  It tests to make sure that the ArchiveDepositInfo is updated appropriately.
     *
     * @throws Exception
     */
    @Test
    public void testFailedDepositStatus() throws Exception {
        String collTxId = inMemoryArchiveService.deposit(collectionNoData);
        inMemoryArchiveService.pollArchive();
        assertEquals(Status.DEPOSITED, inMemoryArchiveService.getDepositStatus(collTxId));

        collTxId = alwaysFailingInMemoryArchiveService.deposit(collectionNoData);
        alwaysFailingInMemoryArchiveService.pollArchive();
        assertEquals(Status.FAILED, alwaysFailingInMemoryArchiveService.getDepositStatus(collTxId));
    }

    @Test
    public void testRetrieveSingleChild() throws Exception {

        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        
        String ds_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemOne);
        assertNotNull(ds_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_deposit_id));

        ArchiveSearchResult<DataItem> results = inMemoryArchiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());
        //Can't just compare ds to the retrieved ds since the source file might change.
        DataItem dataItem = dataItems.iterator().next();
        assertEquals(dataItemOne.getId(), dataItem.getId());
        assertEquals(dataItemOne.getName(), dataItem.getName());
    }

    @Test
    public void testRetrieveMultipleChildren() throws Exception {
        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        String ds_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemOne);

        assertNotNull(ds_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_deposit_id));

        String ds_two_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemTwo);

        assertNotNull(ds_two_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_two_deposit_id));

        ArchiveSearchResult<DataItem> results = inMemoryArchiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(2, dataItems.size());

        Iterator<DataItem> iter = dataItems.iterator();
        DataItem dataItem = (DataItem) iter.next();
        List<String> dataSetIDs = new ArrayList<String>();
        List<String> dataSetNames = new ArrayList<String>();

        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());

        DataItem dataSetTwo = (DataItem) iter.next();
        dataSetIDs.add(dataSetTwo.getId());
        dataSetNames.add(dataSetTwo.getName());

        assertEquals(2, dataSetIDs.size());
        assertEquals(2, dataSetNames.size());

        //Can't guarantee order of return so just make sure we have both data sets
        assertTrue(dataSetIDs.contains(dataItemOne.getId()));
        assertTrue(dataSetNames.contains(dataItemOne.getName()));
        assertTrue(dataSetIDs.contains(dataItemTwo.getId()));
        assertTrue(dataSetNames.contains(dataItemTwo.getName()));
    }

    @Test
    public void testRetrieveRetrieveMultipleChildrenPaginated() throws Exception {
        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        String ds_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemOne);

        assertNotNull(ds_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_deposit_id));

        String ds_two_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemTwo);

        assertNotNull(ds_two_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_two_deposit_id));

        ArchiveSearchResult<DataItem> results = inMemoryArchiveService.retrieveDataSetsForCollection(col_deposit_id, 1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);

        List<String> dataSetIDs = new ArrayList<String>();
        List<String> dataSetNames = new ArrayList<String>();

        assertEquals(1, dataItems.size());

        DataItem dataItem = dataItems.iterator().next();
        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());

        results = inMemoryArchiveService.retrieveDataSetsForCollection(col_deposit_id, 1, 1);
        dataItems = results.getResults();
        assertNotNull(dataItems);

        assertEquals(1, dataItems.size());

        dataItem = dataItems.iterator().next();
        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());

        assertEquals(2, dataSetIDs.size());
        assertEquals(2, dataSetNames.size());

        //Can't guarantee order of return so just make sure we have both data sets
        assertTrue(dataSetIDs.contains(dataItemOne.getId()));
        assertTrue(dataSetNames.contains(dataItemOne.getName()));
        assertTrue(dataSetIDs.contains(dataItemTwo.getId()));
        assertTrue(dataSetNames.contains(dataItemTwo.getName()));
    }

    @Test
    public void testGetChildrenMultipleCollections() throws Exception {
        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        Collection colTwo = new Collection();
        colTwo.setId("id://collectionTwo");
        colTwo.setTitle("collections are great");

        String col_deposit_id_two = inMemoryArchiveService.deposit(colTwo);

        assertNotNull(col_deposit_id_two);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id_two));

        String ds_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemOne);

        assertNotNull(ds_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_deposit_id));

        String ds_two_deposit_id = inMemoryArchiveService.deposit(col_deposit_id_two, dataItemTwo);

        assertNotNull(ds_two_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_two_deposit_id));

        ArchiveSearchResult<DataItem> results = inMemoryArchiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);

        List<String> dataSetIDs = new ArrayList<String>();
        List<String> dataSetNames = new ArrayList<String>();

        assertEquals(1, dataItems.size());

        DataItem dataItem = dataItems.iterator().next();
        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());

        results = inMemoryArchiveService.retrieveDataSetsForCollection(col_deposit_id_two, -1, 0);
        dataItems = results.getResults();
        assertNotNull(dataItems);

        assertEquals(1, dataItems.size());

        dataItem = dataItems.iterator().next();
        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());

        assertEquals(2, dataSetIDs.size());
        assertEquals(2, dataSetNames.size());

        //Can't guarantee order of return so just make sure we have both data sets
        assertTrue(dataSetIDs.contains(dataItemOne.getId()));
        assertTrue(dataSetNames.contains(dataItemOne.getName()));
        assertTrue(dataSetIDs.contains(dataItemTwo.getId()));
        assertTrue(dataSetNames.contains(dataItemTwo.getName()));
    }

    /**
     * Test the retrieval of versioned dataset for a given collection. Only the latest version of the dataset should be
     * returned.
     * @throws Exception
     */
    @Test
    public void testRetrieveVersionedDataSetsForCollection() throws Exception {
        //Depositing collection into archive
        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);
        inMemoryArchiveService.pollArchive();
        //Making sure collection is deposited
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));

        //Depositing dataset object version 1
        String ds_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemOne);

        //Making sure version 1 dataset is deposited.
        assertNotNull(ds_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_deposit_id));

        //Creating dataset object version 2
        DataItem dsTwo = new DataItem();

        dsTwo.setId(dataItemOne.getId());
        dsTwo.setDescription("this is a 2nd data set");
        dsTwo.setName("nameisstillrequired");

        dsTwo.addFile(dataFileTwo);

        //Depositing dataset object version 2
        String ds_two_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dsTwo);

        //Making sure that version 2 dataset is deposited
        assertNotNull(ds_two_deposit_id);
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_two_deposit_id));

        ArchiveSearchResult<DataItem> results = inMemoryArchiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());

        DataItem dataItem = dataItems.iterator().next();

        assertEquals(dsTwo.getId(), dataItem.getId());
        assertEquals(dsTwo.getName(), dataItem.getName());
    }
    
    @Test
    public void testRetrieveChildrenOfVersionedCollection() throws Exception {
        // Add a collection
        String col1_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col1_deposit_id);

        inMemoryArchiveService.pollArchive();
        
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col1_deposit_id));
        
        // Add a DataItem to the collection

        String ds1_deposit_id = inMemoryArchiveService.deposit(col1_deposit_id, dataItemOne);

        assertNotNull(ds1_deposit_id);
      
        inMemoryArchiveService.pollArchive();
        
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds1_deposit_id));
        
        // Update the collection
        
        Collection col2 = new Collection();
        col2.setId(collectionNoData.getId());
        col2.setSummary("collection version 2");

        String col2_deposit_id = inMemoryArchiveService.deposit(col2);

        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED, inMemoryArchiveService.getDepositStatus(col2_deposit_id));

        // Make sure we get the correct collection state back
        ArchiveSearchResult<Collection> searchResult = inMemoryArchiveService.retrieveCollection(col1_deposit_id);
        assertNotNull(searchResult);
        assertEquals(1, searchResult.getResultCount());
        assertEquals(collectionNoData,  searchResult.getResults().iterator().next());

        searchResult = inMemoryArchiveService.retrieveCollection(col2_deposit_id);
        assertNotNull(searchResult);
        assertEquals(1, searchResult.getResultCount());
        assertEquals(col2,  searchResult.getResults().iterator().next());

        // Make sure ds can be retrieved from each collection version
        
        ArchiveSearchResult<DataItem> search1 = inMemoryArchiveService.retrieveDataSetsForCollection(col1_deposit_id, -1, 0);

        assertEquals(1, search1.getResultCount());
        assertEquals(dataItemOne.getId(), search1.getResults().iterator().next().getId());
        
        ArchiveSearchResult<DataItem> search2 = inMemoryArchiveService.retrieveDataSetsForCollection(col2_deposit_id, -1, 0);
        
        assertEquals(1, search2.getResultCount());
        assertEquals(dataItemOne.getId(), search2.getResults().iterator().next().getId());
    }
    
    @Test
    public void testRetrieveCollectionVersions() throws Exception {
        // Add a collection

        String col1_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col1_deposit_id);

        inMemoryArchiveService.pollArchive();
        
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col1_deposit_id));
        
        // Update the collection
        
        Collection col2 = new Collection();
        col2.setId(collectionNoData.getId());
        col2.setTitle("collection version 2");

        String col2_deposit_id = inMemoryArchiveService.deposit(col2);

        inMemoryArchiveService.pollArchive();       

        assertEquals(Status.DEPOSITED, inMemoryArchiveService.getDepositStatus(col2_deposit_id));
        assertFalse(col1_deposit_id.equals(col2_deposit_id));
        
        // Make sure both versions can be retrieved by respective deposit ids.
      
        ArchiveSearchResult<Collection> result1 = inMemoryArchiveService.retrieveCollection(col1_deposit_id);
        ArchiveSearchResult<Collection> result2 = inMemoryArchiveService.retrieveCollection(col2_deposit_id);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.getResultCount());
        assertEquals(1, result2.getResultCount());
        
        Collection test1 = result1.getResults().iterator().next();
        Collection test2 = result2.getResults().iterator().next();
        
        assertNotNull(test1);
        assertNotNull(test2);
               
        assertEquals(collectionNoData.getId(), test1.getId());
        assertEquals(col2.getId(), test1.getId());
        
        assertEquals(collectionNoData.getTitle(), test1.getTitle());
        assertEquals(col2.getTitle(), test2.getTitle());
    }    
    
    /**
     * Make sure multiple versions of a DataItem can be retrieved by deposit id.
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveDataItemVersions() throws Exception {
        // Add a collection
        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);

        inMemoryArchiveService.pollArchive();
        
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));
        
        // Deposit a data item in into the collection
        String ds1_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, dataItemOne);

        inMemoryArchiveService.pollArchive();
        
        assertNotNull(ds1_deposit_id);
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds1_deposit_id));
        
        // Update the data item in the collection
        
        DataItem ds2 = new DataItem();

        ds2.setId(dataItemOne.getId());
        ds2.setDescription("this is a new version");
        ds2.setName("version 2");

        DataFile data_file2 = new DataFile();

        data_file2.setName("gorilla");
        File tmp2 = File.createTempFile("grr", null);
        FileUtils.writeStringToFile(tmp2, "ArchiveServiceTest#testRetrieveDataItemVersions");
        tmp2.deleteOnExit();
        data_file2.setSource(tmp2.toURI().toURL().toExternalForm());
        data_file2.setId(idService.create(Types.DATA_FILE.name()).getUrl().toString());
        data_file2.setSize(tmp2.length());
        
        ds2.addFile(data_file2);

        String ds2_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, ds2);

        inMemoryArchiveService.pollArchive();
        
        assertNotNull(ds2_deposit_id);
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds2_deposit_id));
        
        // Make sure both versions can be retrieved by respective deposit ids.
      
        ArchiveSearchResult<DataItem> result1 = inMemoryArchiveService.retrieveDataSet(ds1_deposit_id);
        ArchiveSearchResult<DataItem> result2 = inMemoryArchiveService.retrieveDataSet(ds2_deposit_id);
        
        assertNotNull(result1);
        assertNotNull(result2);
        
        assertEquals(1, result1.getResultCount());
        assertEquals(1, result2.getResultCount());
        
        DataItem test1 = result1.getResults().iterator().next();
        DataItem test2 = result2.getResults().iterator().next();
        
        assertNotNull(test1);
        assertNotNull(test2);
               
        assertEquals(dataItemOne.getId(), ds2.getId());
        assertEquals(ds2.getId(), test2.getId());
        
        assertEquals(dataItemOne.getName(), test1.getName());
        assertEquals(ds2.getName(), test2.getName());
    }    
    
    @Test
    public void testRetrieveMetadataFileVersions() throws Exception {
        // Add a collection

        String col_deposit_id = inMemoryArchiveService.deposit(collectionNoData);

        assertNotNull(col_deposit_id);

        inMemoryArchiveService.pollArchive();
        
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(col_deposit_id));
        
        // Deposit a metadata file
        String mf1_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, metadataFileOne);

        assertNotNull(mf1_deposit_id);

        inMemoryArchiveService.pollArchive();
        
        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(mf1_deposit_id));
        
        MetadataFile mf2 = new MetadataFile(metadataFileOne);
        mf2.setName("metadata version 2");

        String mf2_deposit_id = inMemoryArchiveService.deposit(col_deposit_id, mf2);

        inMemoryArchiveService.pollArchive();       

        assertEquals(Status.DEPOSITED, inMemoryArchiveService.getDepositStatus(mf2_deposit_id));
        assertFalse(col_deposit_id.equals(mf2_deposit_id));
        
        // Make sure both versions can be retrieved by respective deposit ids.
      
        ArchiveSearchResult<MetadataFile> result1 = inMemoryArchiveService.retrieveMetadataFile(mf1_deposit_id);
        ArchiveSearchResult<MetadataFile> result2 = inMemoryArchiveService.retrieveMetadataFile(mf2_deposit_id);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.getResultCount());
        assertEquals(1, result2.getResultCount());
        
        MetadataFile test1 = result1.getResults().iterator().next();
        MetadataFile test2 = result2.getResults().iterator().next();
        
        assertNotNull(test1);
        assertNotNull(test2);
               
        assertEquals(metadataFileOne.getId(), test1.getId());
        assertEquals(mf2.getId(), test1.getId());
        
        assertEquals(metadataFileOne.getName(), test1.getName());
        assertEquals(mf2.getName(), test2.getName());
    }    
    
    @Test
    public void testDepositMetadataFormatRegistryEntry() throws Exception {
        DcsMetadataFormat format = new DcsMetadataFormat();
        format.setId("dataconservancy.org:formats:file:test:2013");
        format.setName("test format");
        format.setVersion("1.0");
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println("metadata");
        out.close();
        
        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("test scheme");
        scheme.setSchemaUrl("www.dataconservancy.org");
        scheme.setSource(tmp.toURI().toURL().toExternalForm());
        scheme.setSchemaVersion("1.0");
        format.addScheme(scheme);
        
        String[] keys = {"dataconservancy.org:formats:file:test:2013"};

        RegistryEntry<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>("id:registryEntry", format, "DcsMetadataFormat", Arrays.asList(keys), "registry entry for test format");
        
        String deposit_id = inMemoryArchiveService.deposit(entry);

        assertNotNull(deposit_id);

        assertEquals(Status.PENDING,
                inMemoryArchiveService.getDepositStatus(deposit_id));
      
        inMemoryArchiveService.pollArchive();

        assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(deposit_id));        
    }
    
    @Test
    public void testDespositSubcollection() throws Exception {        
        Collection parent = collectionWithData;        
        Collection child = collectionNoData;
        
        child.setParentId(parent.getId());
        
        String parent_deposit_id;

        // Deposit parent collection
        {
            parent_deposit_id = inMemoryArchiveService.deposit(parent);

            assertNotNull(parent_deposit_id);

            inMemoryArchiveService.pollArchive();

            assertEquals(Status.DEPOSITED,
                    inMemoryArchiveService.getDepositStatus(parent_deposit_id));

            ArchiveSearchResult<Collection> result = inMemoryArchiveService
                    .retrieveCollection(parent_deposit_id);
            assertTrue(result.getResults().iterator().hasNext());
            Collection test_parent = result.getResults().iterator().next();

            assertNotNull(test_parent);
            assertEquals(parent, test_parent);
        }
        
        String child_deposit_id;

        // Deposit child collection
        {
            child_deposit_id = inMemoryArchiveService.deposit(child);

            assertNotNull(child_deposit_id);

            inMemoryArchiveService.pollArchive();

            assertEquals(Status.DEPOSITED,
                    inMemoryArchiveService.getDepositStatus(child_deposit_id));

            // Add expected child to parent so equality can be tested.
            
            parent.getChildrenIds().add(child.getId());

            {
                ArchiveSearchResult<Collection> result = inMemoryArchiveService
                        .retrieveCollection(child_deposit_id);
                assertTrue(result.getResults().iterator().hasNext());
                Collection test_child = result.getResults().iterator().next();
    
                assertNotNull(test_child);
                assertEquals(child, test_child);
                
                assertEquals(2, inMemoryArchiveService.listCollections(Status.DEPOSITED)
                        .size());
                assertTrue(inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(child_deposit_id));
            }
            
            // Make sure parent can still be retrieved
            
            {
                ArchiveSearchResult<Collection> result = inMemoryArchiveService
                        .retrieveCollection(parent_deposit_id);
                assertTrue(result.getResults().iterator().hasNext());
                Collection test_parent = result.getResults().iterator().next();
    
                assertNotNull(test_parent);
                assertEquals(parent, test_parent);
            }            
        }

        // Add a data item to the parent
        
        {
            String ds_deposit_id = inMemoryArchiveService.deposit(parent_deposit_id, dataItemOne);

            assertNotNull(ds_deposit_id);
            inMemoryArchiveService.pollArchive();

            assertEquals(Status.DEPOSITED,
                inMemoryArchiveService.getDepositStatus(ds_deposit_id));

            // Make sure child still retrievable
            
            ArchiveSearchResult<Collection> result = inMemoryArchiveService
                    .retrieveCollection(child_deposit_id);
            assertTrue(result.getResults().iterator().hasNext());
            Collection test_child = result.getResults().iterator().next();

            assertNotNull(test_child);
            assertEquals(child, test_child);
            
            assertEquals(2, inMemoryArchiveService.listCollections(Status.DEPOSITED)
                    .size());
            assertTrue(inMemoryArchiveService.listCollections(Status.DEPOSITED).contains(child_deposit_id));
        }
        
        // Update child collection and remove it as a subcollection
        {            
            child.setTitle("This is a new and much better title");
            child.setCitableLocator("cite:this");

            child.setParentId(null);
            parent.getChildrenIds().clear();

            String child_update_deposit_id = inMemoryArchiveService.deposit(child);

            assertNotNull(child_update_deposit_id);

            inMemoryArchiveService.pollArchive();

            
            assertEquals(Status.DEPOSITED,
                    inMemoryArchiveService.getDepositStatus(child_update_deposit_id));

            {
                ArchiveSearchResult<Collection> result = inMemoryArchiveService
                        .retrieveCollection(child_update_deposit_id);
                assertTrue(result.getResults().iterator().hasNext());
                Collection test_child = result.getResults().iterator().next();
    
                assertNotNull(test_child);
                assertEquals(child, test_child);
            }

            // Make sure parent can still be retrieved
            
            {
                ArchiveSearchResult<Collection> result = inMemoryArchiveService
                        .retrieveCollection(parent_deposit_id);
                assertTrue(result.getResults().iterator().hasNext());
                Collection test_parent = result.getResults().iterator().next();
    
                assertNotNull(test_parent);
                assertEquals(parent, test_parent);
            }             
        }
    }
 }
