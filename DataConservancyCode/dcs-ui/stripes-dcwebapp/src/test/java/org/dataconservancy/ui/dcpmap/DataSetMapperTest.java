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
package org.dataconservancy.ui.dcpmap;

import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dataconservancy.ui.DirtiesContextBaseUnitTest;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.services.DataFileBusinessObjectSearcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.profile.Profiler;
import org.dataconservancy.ui.services.ArchiveUtil;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.DataItemBusinessObjectSearcher;
import org.dataconservancy.ui.services.MetadataFileBusinessObjectSearcher;
import org.dataconservancy.ui.util.DcpUtil;
import org.dataconservancy.ui.util.ValidationUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import static org.dataconservancy.ui.util.MappingUtil.findDcsFile;
import static org.dataconservancy.ui.util.MappingUtil.getPredecessorId;
import static org.dataconservancy.ui.util.MappingUtil.getRootDuFromDataItemDcp;
import static org.dataconservancy.ui.util.MappingUtil.getStateDuFromDataItemDcp;
import static org.dataconservancy.ui.util.MappingUtil.hasManifestationFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataSetMapperTest extends DirtiesContextBaseUnitTest {

    @Autowired
    @Qualifier("dataItemProfile")
    private Profile<DataItem> dataSetProfile;

    @Autowired
    private DcsModelBuilder builder;

    @Autowired
    @Qualifier("businessObjectBuilder")
    private BusinessObjectBuilder bob;

    @Autowired
    @Qualifier("datasetMapper")
    private Profiler<DataItem> profiler;

    private DataItemBusinessObjectSearcher boSearcher = mock(DataItemBusinessObjectSearcher.class);

    private ArchiveUtil archiveUtil = mock(ArchiveUtil.class);
    
    private MetadataFileBusinessObjectSearcher mfSearcher = mock(MetadataFileBusinessObjectSearcher.class);

    private DataSetMapper underTest;

    private static final List<DataFile> NO_DATAFILES = Collections.emptyList();

    private static final List<String> NO_METADATA_FILE_IDS = Collections.emptyList();

    @Before
    public void setUp() {
        underTest = new DataSetMapper(boSearcher, archiveUtil, mfSearcher);

        // Because DataSetMapper is no longer responsible for mapping DataFiles, we take the DataFiles off
        // of the DataItems used in the mapping tests, so that the equals method can be used.
        this.dataItemOne = new DataItem(dataItemOne.getName(), dataItemOne.getDescription(), dataItemOne.getId(), 
                dataItemOne.getDepositorId(), dataItemOne.getDepositDate(), NO_DATAFILES,
                NO_METADATA_FILE_IDS, dataItemOne.getParentId());

        this.dataItemTwo = new DataItem(dataItemTwo.getName(), dataItemTwo.getDescription(), dataItemTwo.getId(),
                dataItemTwo.getDepositorId(), dataItemTwo.getDepositDate(), NO_DATAFILES,
                NO_METADATA_FILE_IDS, dataItemTwo.getParentId());
    }

    @Test
    @Ignore("I sent a message to the jodatime list, no response yet.")
    public void testDateTimeEquality() throws Exception {
        final DateTime now = DateTime.now().toDateTimeISO();
        final DateTime parsed = DateTime.parse(now.toDateTimeISO().toString());
        assertEquals(now.getZone(), parsed.getZone());
        assertEquals(now, parsed);
    }

    /**
     * Tests the
     * {@link DataSetMapper#toDcp(String, org.dataconservancy.ui.model.DataItem)}
     * and {@link DataSetMapper#fromDcp(org.dataconservancy.model.dcp.Dcp)}.
     * <p/>
     * The Mapper is used to map a DataItem (e.g. ds) to a DCP and from a DCP
     * back to a DataItem (e.g. ds'). DataItem <em>ds</em> and <em>ds'</em> should
     * be equal according to {@link DataItem#equals(Object)}.
     * 
     * @throws Exception
     */
    @Test
    public void testMappingRoundTrip() throws Exception {
        DataFileMapper dfMapper = new DataFileMapper(bob, mock(DataFileBusinessObjectSearcher.class));

        // The DataSetMapper under test
        final DataSetMapper underTest = new DataSetMapper(
                mock(DataItemBusinessObjectSearcher.class), archiveUtil, mock(MetadataFileBusinessObjectSearcher.class));


        // Perform the mapping of the DataItem to a DCP package.
        final Dcp mappedDcp = underTest.toDcp("collectionId", dataItemOne);
        assertNotNull("Expected a DCP to be generated!", mappedDcp);

        // Assert the generated DCP conforms to the DataItem profile
        assertTrue("The generated DCP does not conform to the profile '"
                + dataSetProfile + "'!",
                profiler.conforms(mappedDcp));

        // Map from the generated DCP back to the DataItem
        final DataItem roundTripDs = underTest.fromDcp(mappedDcp);
        assertNotNull("Expected a DataItem to be generated!", roundTripDs);

        // The round-tripped DataItem should be equal to the original DataItem
        assertEquals("DataItem to DCP to DataItem' mapping failed!", dataItemOne,
                roundTripDs);
    }

    @Test
    public void testMappingToDcp() throws Exception {      

        final String collectionToDepositTo = "collectionId";
        dataFileOne.setPath(System.getProperty("java.io.tmpdir"));
        final Dcp dcp = underTest.toDcp(collectionToDepositTo, dataItemOne);
        assertNotNull("Expected a DCP to be produced by the DataSetMapper!",
                dcp);
        // It's easier, somewhat, to work with the DCP as a Map keyed by entity
        // identifiers.
        final Map<String, DcsEntity> dcpMap = DcpUtil.asMap(dcp);

        // There should be a Root DU as the head of the object graph representing the
        // DataItem
        // - it should have a DCS entity ID equal to the business ID
        // - it should have the DataItem business ID in the formerExternalRef
        // - it should have the DataFiles business IDs in the formerExternal Ref
        // - it should have a parent DU with an id equal to
        // 'collectionToDepositTo'
        // - it should have type equal to its profile type
        assertNotNull(
                "Mapped DCP does not contain a DataItem DU for id "
                        + dataItemOne.getId(), dcpMap.get(dataItemOne.getId()));

        final DcsDeliverableUnit dsDu = (DcsDeliverableUnit) dcpMap.get(dataItemOne
                .getId());
        assertEquals(dataItemOne.getId(), dsDu.getId());
        assertTrue(dsDu.getFormerExternalRefs().contains(dataItemOne.getId()));
        for (DataFile dsFile : dataItemOne.getFiles()) {
            assertTrue(dsDu.getFormerExternalRefs().contains(dsFile.getId()));
        }
        assertTrue(dsDu.getParents().contains(
                new DcsDeliverableUnitRef(collectionToDepositTo)));
        assertEquals(dataSetProfile.getType(), dsDu.getType());

        // There should be a State DU which has the Root DU as its parent
        // - it has a random identifier
        // - it has a type corresponding to its profile
        DcsDeliverableUnit stateDu = null;
        for (DcsDeliverableUnit candidateStateDu : dcp.getDeliverableUnits()) {
            if (candidateStateDu.getType().equals(DataItemProfile.DATASET_STATE_TYPE)) {
                if (candidateStateDu.getParents().contains(new DcsDeliverableUnitRef(dsDu.getId()))) {
                    stateDu = candidateStateDu;
                }
            }
        }
        assertNotNull("Package did not contain a State DU!", stateDu);

        // There should be two Manifestations attached to the State DU:
        // - one representing DataItem metadata
        // - one representing DataItem Files metadata
        // - each Manifestation should refer to the State DU
        // - each Manifestation should have a technical environment
        // - have at least one DcsFile
        final Collection<DcsManifestation> manifestations = dcp.getManifestations();
        assertEquals(2, manifestations.size());
        for (DcsManifestation man : manifestations) {
            assertEquals(stateDu.getId(), man.getDeliverableUnit());
            assertTrue(man.getTechnicalEnvironment().size() > 0);
            assertTrue(man.getManifestationFiles().size() > 0);
        }

        boolean found = false;
        boolean pathFound = false;

        // Assert that there is a Manifestation for DataItem Metadata by
        // evaluating the technical environment
        found = false;
        for (DcsManifestation man : manifestations) {
            if (man.getTechnicalEnvironment().contains(
                    DataSetMapper.DATASET_METADATA_TECHENV)) {
                found = true;
            }
        }
        assertTrue(found);

        found = false;
        // Assert that there is a Manifestation for DataItem File Metadata by
        // evaluating the technical environment
        for (DcsManifestation man : manifestations) {
            if (man.getTechnicalEnvironment().contains(
                    DataSetMapper.DATASET_FILE_METADATA_TECHENV)) {
                found = true;
            }
        }
        assertTrue(found);

        // Assert that each ManifestationFile in each Manifestation refers to an
        // existing DcsFile in the DCP
        for (DcsManifestation man : manifestations) {
            for (DcsManifestationFile manF : man.getManifestationFiles()) {
                assertTrue(dcpMap.containsKey(manF.getRef().getRef()));
            }
        }

        ValidationUtil vutil = new ValidationUtil();
        vutil.validate(dcp);
    }

    @Test(expected = DcpMappingException.class)
    public void testMapDatasetWithNoIdentifier() throws Exception {
        // Compose a DataItem with no Identifier
        dataItemOne.setId(null);

        underTest.toDcp("parent-id", dataItemOne);
    }

    @Test(expected = DcpMappingException.class)
    public void testMapDatasetWithNullParentIdentifier() throws Exception {
        // map it with no parent id
        underTest.toDcp(null, dataItemOne);
    }

    @Test(expected = DcpMappingException.class)
    public void testMapDatasetWithEmptyParentIdentifier() throws Exception {       
        // map it with empty parent id
        underTest.toDcp(" ", dataItemOne);
    }

    @Test
    public void testMapDatasetWithNoFiles() throws Exception {
        // This succeeds now because the mapper is not responsible for mapping files, so it doesn't check to
        // see that there are any
        underTest.toDcp("parent-id", dataItemOne);
    }

    @Test(expected = DcpMappingException.class)
    @Ignore("Need to decide what the behavior of this should be")
    public void testMapDatasetWithNoDepositDate() throws Exception {
        dataItemOne.setDepositDate(null);

        underTest.toDcp("parent-id", dataItemOne);
    }

    @Test(expected = DcpMappingException.class)
    @Ignore("Need to decide what the behavior of this should be")
    public void testMapDatasetWithNoDepositor() throws Exception {
        dataItemOne.setDepositorId(null);
        underTest.toDcp("parent-id", dataItemOne);
    }

    @Test(expected = DcpMappingException.class)
    @Ignore("Need to decide what the behavior of this should be")
    public void testMapDatasetWithNonExistentCollection() throws Exception {
        // map it with empty parent id
        underTest.toDcp("non-existent-collection-id", dataItemOne);
    }

    private DataFile addFile(DataItem ds, String id, String name) throws IOException {
        final DataFile file = new DataFile();
        file.setId(id);
        file.setParentId(ds.getId());
        file.setName(name);
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        file.setSource(tmp.toURI().toString());
        file.setPath(System.getProperty("java.io.tmpdir"));
        Resource r = new UrlResource(file.getSource());
        file.setSize(r.contentLength());

        ds.addFile(file);
        
        return file;
    }
    
    @Test
    @Ignore("DataSetMapper is no longer responsible for mapping files.")
    public void testMappingUpdateKeepingFile() throws Exception {

        final String collectionId = "cow";
        final Dcp dcp1 = underTest.toDcp(collectionId, dataItemOne);

        // Add ds2 with same business id as ds1 and file2 same business id as
        // file1

        final DataItem ds2 = new DataItem();
        ds2.setName("ds2");
        ds2.setId(dataItemOne.getId());

        final DataFile file2 = new DataFile();
        file2.setId(dataFileOne.getId());
        file2.setName(dataFileOne.getName());
        file2.setPath(dataFileOne.getPath());

        ds2.addFile(file2);

        DataItemBusinessObjectSearcher searcher = mock(DataItemBusinessObjectSearcher.class);
        when(searcher.findDataSetFile(file2.getId())).thenReturn(findDcsFile(dcp1, dataFileOne.getName()));
        when(searcher.findLatestState(ds2.getId())).thenReturn(new BusinessObjectState() {
            @Override
            public DcsDeliverableUnit getRoot() {
                DcsDeliverableUnit du = getRootDuFromDataItemDcp(dcp1);
                assertNotNull(du);
                return du;
            }

            @Override
            public DcsDeliverableUnit getLatestState() {
                DcsDeliverableUnit du = getStateDuFromDataItemDcp(dcp1);
                assertNotNull(du);
                return du;
            }
        });
        
        underTest.setBoSearcher(searcher);
        final Dcp dcp2 = underTest.toDcp(collectionId, ds2);

        assertIsSuccessor(dcp2, dcp1);

        DcsFile dcsfile1 = findDcsFile(dcp1, dataFileOne.getName());
        assertNotNull(dcsfile1);
        
        DcsFile dcsfile2 = findDcsFile(dcp2, dataFileOne.getName());
        assertNull(dcsfile2);
        
        assertTrue(hasManifestationFile(dcp2, dcsfile1.getId()));

        // Make sure we can retrieve ds2.
        
        // Setup the Dcp as it would be returned for ds2
        
        dcp2.addFile(dcsfile1);
        
        // Setup ds2 to hold file1
        ds2.getFiles().clear();
        ds2.addFile(dataFileOne);
        
        DataItem test_ds2 = underTest.fromDcp(dcp2);
        
        assertEquals(ds2, test_ds2);
    }
    
    @Test
    @Ignore("DataSetMapper is no longer responsible for mapping files.")
    public void testMappingUpdateReplacingFile() throws Exception {
        final String collectionId = "cow";


        // Add ds2 with same business id as ds1 and new file file2

        // Mock the searcher's interaction with the archive: returning null for
        // finding the latest state is the expected response when the business
        // object has not yet been deposited to the archive.
        DataItemBusinessObjectSearcher searcher = mock(DataItemBusinessObjectSearcher.class);
        when(searcher.findLatestState(dataItemOne.getId())).thenReturn(null);
        underTest.setBoSearcher(searcher);

        final Dcp dcp1 = underTest.toDcp(collectionId, dataItemOne);

        // Add ds2 with same business id as ds1 and new file file2

        final DataItem ds2 = new DataItem();
        final String ds2Name = "ds2";
        final String file2Id = "file2";
        ds2.setName(ds2Name);
        ds2.setId(dataItemOne.getId());

        final DataFile file2 = addFile(ds2, file2Id, file2Id);

        // Mock the searcher's interaction with the archive: returning the previous
        // DataItem DU in response to the search is the expected response when the previous
        // DataItem has been deposited to the archive
        searcher = mock(DataItemBusinessObjectSearcher.class);
        when(searcher.findLatestState(ds2.getId())).thenReturn(new BusinessObjectState() {
            @Override
            public DcsDeliverableUnit getRoot() {
                return getRootDuFromDataItemDcp(dcp1);
            }

            @Override
            public DcsDeliverableUnit getLatestState() {
                return getStateDuFromDataItemDcp(dcp1);
            }
        });

        underTest.setBoSearcher(searcher);
        final Dcp dcp2 = underTest.toDcp(collectionId, ds2);

        assertIsSuccessor(dcp2, dcp1);
        
        DcsFile dcsfile1 = findDcsFile(dcp1, dataFileOne.getName());
        DcsFile dcsfile2 = findDcsFile(dcp2, file2.getName());
        
        assertNotNull(dcsfile1);
        assertNotNull(dcsfile2);

        assertFalse(hasManifestationFile(dcp1, file2.getId()));
        assertTrue(hasManifestationFile(dcp1, dataFileOne.getId()));
        
        assertTrue(hasManifestationFile(dcp2, file2.getId()));
        assertFalse(hasManifestationFile(dcp2, dataFileOne.getId()));
        
        // Make sure we can retrieve ds2.
        
        // Setup the Dcp as it would be returned for ds2
        
        DataItem test_ds2 = underTest.fromDcp(dcp2);
        
        assertEquals(ds2, test_ds2);
    }

    private void assertIsSuccessor(Dcp dcp2, Dcp dcp1) {
        assertEquals(2, dcp1.getDeliverableUnits().size());
        DcsDeliverableUnit du1 = getStateDuFromDataItemDcp(dcp1);

        assertEquals(2, dcp2.getDeliverableUnits().size());
        DcsDeliverableUnit du2 = getStateDuFromDataItemDcp(dcp2);

        String pred_id = getPredecessorId(du2);

        assertNotNull(pred_id);
        assertEquals(du1.getId(), pred_id);
    }
    
    @Test
    public void testMappingWithMetadataFile() throws Exception {
        final String collectionToDepositTo = "collectionId";
        dataFileOne.setPath(System.getProperty("java.io.tmpdir"));
        
        //Set up the metadata file for the data item.
        final String metadataFileArchiveId = "archive:1";
        BusinessObjectState metadataFileState = mock(BusinessObjectState.class);
        DcsDeliverableUnit root = new DcsDeliverableUnit();
        root.setId(metadataFileArchiveId);
        when(metadataFileState.getRoot()).thenReturn(root);
        when(mfSearcher.findLatestState(metadataFileOne.getId())).thenReturn(metadataFileState);
        final Dcp dcp = underTest.toDcp(collectionToDepositTo, dataItemOne);
        assertNotNull("Expected a DCP to be produced by the DataSetMapper!",
                dcp);
        // It's easier, somewhat, to work with the DCP as a Map keyed by entity
        // identifiers.
        final Map<String, DcsEntity> dcpMap = DcpUtil.asMap(dcp);

        // There should be a Root DU as the head of the object graph representing the
        // DataItem
        // - it should have a DCS entity ID equal to the business ID
        // - it should have the DataItem business ID in the formerExternalRef
        // - it should have the DataFiles business IDs in the formerExternal Ref
        // - it should have a parent DU with an id equal to
        // 'collectionToDepositTo'
        // - it should have type equal to its profile type
        assertNotNull(
                "Mapped DCP does not contain a DataItem DU for id "
                        + dataItemOne.getId(), dcpMap.get(dataItemOne.getId()));

        final DcsDeliverableUnit dsDu = (DcsDeliverableUnit) dcpMap.get(dataItemOne
                .getId());
        assertEquals(dataItemOne.getId(), dsDu.getId());
        assertTrue(dsDu.getFormerExternalRefs().contains(dataItemOne.getId()));
        for (DataFile dsFile : dataItemOne.getFiles()) {
            assertTrue(dsDu.getFormerExternalRefs().contains(dsFile.getId()));
        }
        assertTrue(dsDu.getParents().contains(
                new DcsDeliverableUnitRef(collectionToDepositTo)));
        assertEquals(dataSetProfile.getType(), dsDu.getType());

        // There should be a State DU which has the Root DU as its parent
        // - it has a random identifier
        // - it has a type corresponding to its profile
        DcsDeliverableUnit stateDu = null;
        for (DcsDeliverableUnit candidateStateDu : dcp.getDeliverableUnits()) {
            if (candidateStateDu.getType().equals(DataItemProfile.DATASET_STATE_TYPE)) {
                if (candidateStateDu.getParents().contains(new DcsDeliverableUnitRef(dsDu.getId()))) {
                    stateDu = candidateStateDu;
                }
            }
        }
        assertNotNull("Package did not contain a State DU!", stateDu);


        ValidationUtil vutil = new ValidationUtil();
        vutil.validate(dcp);
    }
}
