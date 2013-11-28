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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.access.connector.DcsConnectorFault;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.ui.dcpmap.DcpMapper;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.it.support.ArchiveSupport;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateIdApiRequest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.profile.Profiler;
import org.dataconservancy.ui.services.ArchiveUtil;
import org.dataconservancy.ui.services.AtomDepositDocumentParser;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.DataItemBusinessObjectSearcher;
import org.dataconservancy.ui.services.DepositDocument;
import org.dataconservancy.ui.util.DcpUtil;
import org.dataconservancy.ui.util.ValidationUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.xml.sax.SAXException;

/**
 * Tests insuring that the DataItemBusinessObjectSearcher correctly searches the archive for DataItem root and state
 * DUs.
 * <p/>
 * Thoughts from Elliot when writing this annoying test: <br/>
 * <strong>Mapper implementations depend on the state of the business object's archival state.</strong> <br/>
 * <em>Why?</em><br/>
 * <ul>
 * <li>DataItem archival representation includes a reference to its containing Collection (a <parent> ref to the
 * Collection's DU in the archive).</li>
 * <li>DataItem archival representation includes references to DCS Files that may be in the archive. Occurs when
 * updating an existing DataItem in the archive, but the update doesn't change the files in the DataItem.</li>
 * <li>Similarly, archival representation of Collection objects include references to parent Collections (via <parent>
 * refs).</li>
 * <li>Generally, archival representations of an update to either a Collection or DataItem require the Mapper to
 * determine which Deliverable Units in the archive are being updated, so that the proper values can be used for the
 * isSuccessorOf relationship.</li>
 * </ul>
 * It isn't enough to shift the responsibility of determining the archive state out of the Mapper implementation. It is
 * the mapping process writ large that examines the archive state.<br/>
 * <em>Questions:</em><br/>
 * <ol>
 * <li>Is it desirable to make the mapping process independent of archive state?</li>
 * <li>It is possible to make the mapping process independent of archive state?</li>
 * <li>Is it a requirement to make the mapping process independent of archive state?</li>
 * </ol>
 */
public class DataItemBusinessObjectSearcherIT extends BaseIT {
    
    /**
     * Parameters: Collection Business ID, Deposit ID
     */
    private static final String ERR_DEPOSIT_COLLECTION = "Deposit of Collection '%s' (deposit id '%s') failed.";
    
    /**
     * Parameters: Collection Business ID, Deposit ID
     */
    private static final String ERR_COLLECTION_NOT_FOUND_IN_ARCHIVE = "Collection '%s' could not be found in the "
            + "archive after it was deposited (deposit id '%s').";
    
    /**
     * Parameters: DataItem Business ID, DataFile Business ID, Collection Business ID, Collection Archive ID
     */
    private static final String ERR_DEPOSIT_DATAITEM = "Depositing DataItem '%s' with DataFile '%s' into Collection "
            + "'%s', '%s' failed.";
    
    /**
     * Parameters: DataItem Business ID, DataFile Business ID, Collection Business ID, Collection Archive ID
     */
    private static final String ERR_DATAITEM_NOT_FOUND_IN_ARCHIVE = "DataItem '%s' (with DataFile '%s') belonging to "
            + "Collection '%s', '%s' could not be found in the archive after it was deposited.";
    
    /**
     * Keeps a count of the number of DCS UI Collections deposited by this test class
     */
    private static int collectionCounter = 0;
    
    /**
     * The HTTP API request for creating a new Data Item Identifier
     */
    private CreateIdApiRequest dataItemIdReq;
    
    /**
     * The HTTP API request for creating a new Data File Identifier
     */
    private CreateIdApiRequest dataFileIdReq;
    
    /**
     * The HTTP Client for executing HTTP requests against the UI
     */
    private HttpClient httpClient;
    
    /**
     * The UI Collection that we deposit DataItems into
     */
    private Collection dataItemCollection;
    
    /**
     * The business ID of the {@link #dataItemCollection}
     */
    private String collectionBusinessId;
    
    /**
     * The archival (DCS entity) ID of the {@link #dataItemCollection}
     */
    private String collectionArchiveId;
    
    /**
     * Utility for performing validation of DCP XML
     */
    private ValidationUtil validationUtil;
    
    /**
     * Our logger
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * The DataItemBusinessObjectSearcher under test
     */
    @Autowired
    @Qualifier("dataItemBusinessObjectSearcher")
    private DataItemBusinessObjectSearcher underTest;
    
    /**
     * The Profile of the DataItem
     */
    @Autowired
    private Profile<DataItem> dataItemProfile;
    
    /**
     * The Profiler of the DataItem
     */
    @Autowired
    @Qualifier("datasetMapper")
    private Profiler<DataItem> dataItemProfiler;
    
    /**
     * The mapper for the DataItem (maps DataItems back and forth to DCPs)
     */
    @Autowired
    @Qualifier("datasetMapper")
    private DcpMapper<DataItem> dataItemMapper;
    
    /**
     * The mapper for Collection objects
     */
    @Autowired
    @Qualifier("collectionMapper")
    private DcpMapper<Collection> collectionMapper;
    
    /**
     * The DCS Model Builder
     */
    @Autowired
    private DcsModelBuilder builder;
    
    /**
     * Utility methods for communicating with the DCS archive
     */
    @Autowired
    @Qualifier("dcsArchiveUtil")
    private ArchiveUtil archiveUtil;
    
    /**
     * Allocates identifiers for the DataItem and DataFile used in test methods Creates and deposits a Collection that
     * DataItems will be deposited to in test methods
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        httpClient = new DefaultHttpClient();
        dataItemIdReq = reqFactory.createIdApiRequest(Types.DATA_SET);
        dataFileIdReq = reqFactory.createIdApiRequest(Types.DATA_FILE);
        validationUtil = new ValidationUtil();
        
        // Create a Collection for DataItems created deposited during test methods.
        collectionBusinessId = reqFactory.createIdApiRequest(Types.COLLECTION).execute(httpClient);
        dataItemCollection = new Collection();
        dataItemCollection.setId(collectionBusinessId);
        dataItemCollection.setTitle(this.getClass().getName() + " " + collectionCounter++);
        dataItemCollection.setSummary(this.getClass().getName() + " " + collectionCounter);
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
        dataItemCollection.setCreators(creators);
        
        // Create a DCP for the Collection
        final Dcp collectionDcp = collectionMapper.toDcp(null, dataItemCollection);
        
        // Validate it against the schema
        validationUtil.validate(collectionDcp);
        
        // Deposit the Collection in the Archive
        final URL depositId = connector.depositSIP(collectionDcp);
        
        assertTrue(String.format(ERR_DEPOSIT_COLLECTION, collectionBusinessId, depositId.toString()),
                pollDepositFeedForCompletion(depositId, new AtomDepositDocumentParser(archiveUtil)));
        
        // Poll until the Collection is available in the archive
        final DcsDeliverableUnit collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(dataItemCollection
                .getId());
        assertNotNull(String.format(ERR_COLLECTION_NOT_FOUND_IN_ARCHIVE, collectionBusinessId, depositId.toString()),
                collectionDu);
        
        collectionArchiveId = collectionDu.getId();
        
        log.debug("Deposited Collection (business id: {}, archive id: {})\n{}", new Object[] { collectionBusinessId,
                collectionArchiveId, dataItemCollection });
    }
    
    /**
     * Insures that the state of a DataItem with a single version in the archive can be retrieved.
     * <p/>
     * Creates a DataItem, maps the DataItem to a DCP, deposits the DCP to the archive. Then the state is retrieved
     * using the DataItemBusinessObjectSearcher, and verified.
     * 
     * @throws Exception
     */
    @Test
    public void testFindLatestStateOneVersion() throws Exception {
        log.debug("Starting testFindLatestStateOneVersion");
        
        // Create a DataItem with a single DataFile
        
        final DataItem dataItem = new DataItem();
        final String dataItemId = dataItemIdReq.execute(httpClient);
        final String dataFileId = dataFileIdReq.execute(httpClient);
        
        dataItem.setId(dataItemId);
        dataItem.setName(this.getClass().getName() + "-testFindLatestStateOneVersion");
        dataItem.addFile(fromJavaFile(dataFileId,
                java.io.File.createTempFile(this.getClass().getName() + "-testFindLatestStateOneVersion", ".tmp")));
        
        // Convert the DataItem to a DCP; insure that the DCP is valid, and conforms to the DataItem Profile.
        final Dcp dataItemDcp = mapDataItem(collectionArchiveId, dataItem);
        
        if (log.isDebugEnabled()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            builder.buildSip(dataItemDcp, out);
            log.debug(
                    "Depositing DataItem ({}) to {}: \n{}",
                    new Object[] { dataItemId, collectionArchiveId,
                            IOUtils.toString(new ByteArrayInputStream(out.toByteArray())) });
        }
        
        // Deposit the DataItem in the archive
        final URL depositId = connector.depositSIP(dataItemDcp);
        
        log.debug("Deposit feed: {}", depositId.toString());
        
        // Poll until the deposit completes
        assertTrue(
                String.format(ERR_DEPOSIT_DATAITEM, dataItemId, dataFileId, collectionBusinessId, collectionArchiveId),
                pollDepositFeedForCompletion(depositId, new AtomDepositDocumentParser(archiveUtil)));
        
        // Poll until the DataItem is available in the archive
        assertNotNull(String.format(ERR_DATAITEM_NOT_FOUND_IN_ARCHIVE, dataItemId, dataFileId, collectionBusinessId,
                collectionArchiveId), archiveSupport.pollAndQueryArchiveForDataItem(dataItem.getId()));
        
        // Perform the test
        
        final BusinessObjectState state = underTest.findLatestState(dataItem.getId());
        
        assertStateObjectIsValid(dataItemId, state, false);
        
        // This makes assumptions about how a DataItem is mapped; specifically that the state DU contains the
        // the name of the DataItem in the <title> of the DU
        assertEquals(dataItem.getName(), state.getLatestState().getTitle());
    }
    
    /**
     * Insures that the the most recent state of a DataItem with two versions in the archive can be retrieved.
     * <p/>
     * Creates the first DataItem, maps the DataItem to a DCP, deposits the DCP to the archive. Then the state is
     * retrieved using the DataItemBusinessObjectSearcher, and verified.
     * <p/>
     * Modify the deposited DataItem, map the modified DataItem to a DCP, and deposit the DCP to the archive. Because
     * the modified DataItem shares the same business id as the first DataItem, the modified DataItem is an update to
     * the first DataItem, and is mapped appropriately. The state is retrieved using the DataItemBusinessObjectSearcher,
     * and verified.
     * 
     * @throws Exception
     */
    @Test
    public void testFindLatestStateTwoVersions() throws Exception {
        log.debug("Starting testFindLatestStateTwoVersions");
        // Create a DataItem with a single DataFile
        
        final DataItem dataItem1 = new DataItem();
        final String dataItemId = dataItemIdReq.execute(httpClient);
        final String dataFileId = dataFileIdReq.execute(httpClient);
        
        dataItem1.setId(dataItemId);
        dataItem1.setName(this.getClass().getName() + "-testFindLatestStateOneVersion");
        dataItem1.addFile(fromJavaFile(dataFileId,
                java.io.File.createTempFile(this.getClass().getName() + "-testFindLatestStateOneVersion", ".tmp")));
        
        // Convert the DataItem to a DCP; insure that the DCP is valid, and conforms to the DataItem Profile.
        Dcp dataItemDcp = mapDataItem(collectionArchiveId, dataItem1);
        
        if (log.isDebugEnabled()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            builder.buildSip(dataItemDcp, out);
            log.debug(
                    "Depositing DataItem ({}) to {}: \n{}",
                    new Object[] { dataItemId, collectionArchiveId,
                            IOUtils.toString(new ByteArrayInputStream(out.toByteArray())) });
        }
        
        // Deposit the DataItem in the archive
        final URL depositId = connector.depositSIP(dataItemDcp);
        
        log.debug("Deposit feed: {}", depositId.toString());
        
        // Poll until the deposit completes
        assertTrue(
                String.format(ERR_DEPOSIT_DATAITEM, dataItemId, dataFileId, collectionBusinessId, collectionArchiveId),
                pollDepositFeedForCompletion(depositId, new AtomDepositDocumentParser(archiveUtil)));
        
        // Poll until the DataItem is available in the archive
        assertNotNull(String.format(ERR_DATAITEM_NOT_FOUND_IN_ARCHIVE, dataItemId, dataFileId, collectionBusinessId,
                collectionArchiveId), archiveSupport.pollAndQueryArchiveForDataItem(dataItem1.getId()));
        
        // Get the state
        final BusinessObjectState dataItem1State = underTest.findLatestState(dataItemId);
        
        // Assert that the BusinessObjectState exists, that it has a Root and State DU, and that the relationships
        // between the archival entities are correct
        assertStateObjectIsValid(dataItemId, dataItem1State, false);
        
        // Create an update to the first Data Item, just changing its name.
        final DataItem dataItem2 = new DataItem(dataItem1);
        dataItem2.setName(this.getClass().getName() + ": Updated name.");
        
        // Verify that the original and updated DataItem have the same business id
        assertEquals(dataItem1.getId(), dataItem2.getId());
        
        // Verify that the original and updated DataItem have two different names
        assertFalse(dataItem1.getName().equals(dataItem2.getName()));
        
        // Convert the second DataItem to a DCP; insure that the DCP is valid, and conforms to the DataItem Profile.
        dataItemDcp = mapDataItem(collectionArchiveId, dataItem2);
        
        // The DataItem mapping contains a Root DU, which - because we deposited version 1 already - already
        // exists in the archive. We need to replace the Root DU in this DCP with a reference to the one
        // that exists in the archive.
        Map<String, DcsEntity> foo = DcpUtil.asMap(dataItemDcp);
        DcsDeliverableUnit fooStateDu = null;
        Iterator<Map.Entry<String, DcsEntity>> fooItr = foo.entrySet().iterator();
        while (fooItr.hasNext()) {
            Map.Entry e = fooItr.next();
            // Remove the Root DU
            if (e.getValue() instanceof DcsDeliverableUnit
                    && ((DcsDeliverableUnit) e.getValue()).getType().equals(DataItemProfile.DATASET_TYPE)) {
                fooItr.remove();
            }
            
            // Find the State DU
            if (e.getValue() instanceof DcsDeliverableUnit
                    && ((DcsDeliverableUnit) e.getValue()).getType().equals(DataItemProfile.DATASET_STATE_TYPE)) {
                fooStateDu = ((DcsDeliverableUnit) e.getValue());
            }
        }
        
        assertNotNull(fooStateDu);
        DcsDeliverableUnit updatedStateDu = new DcsDeliverableUnit(fooStateDu);
        Set<DcsDeliverableUnitRef> parents = new HashSet<DcsDeliverableUnitRef>();
        parents.add(new DcsDeliverableUnitRef(dataItem1State.getRoot().getId()));
        updatedStateDu.setParents(parents);
        foo.put(updatedStateDu.getId(), updatedStateDu);
        
        dataItemDcp = DcpUtil.add(null, foo.values());
        
        if (log.isDebugEnabled()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            builder.buildSip(dataItemDcp, out);
            log.debug("Depositing DataItem ({}) to {}: \n{}", new Object[] { dataItem2.getId(), collectionArchiveId,
                    IOUtils.toString(new ByteArrayInputStream(out.toByteArray())) });
        }
        
        // Deposit the DataItem in the archive
        final URL depositId2 = connector.depositSIP(dataItemDcp);
        
        log.debug("Deposit feed: {}", depositId2.toString());
        
        assertTrue(
                String.format(ERR_DEPOSIT_DATAITEM, dataItemId, dataFileId, collectionBusinessId, collectionArchiveId),
                pollDepositFeedForCompletion(depositId2, new AtomDepositDocumentParser(archiveUtil)));
        
        // Poll until the DataItem is available in the archive
        assertNotNull(String.format(ERR_DATAITEM_NOT_FOUND_IN_ARCHIVE, dataItemId, dataFileId, collectionBusinessId,
                collectionArchiveId), archiveSupport.pollAndQueryArchiveForDataItem(dataItem2.getId()));
        
        log.debug("Found DataItem version 2 in the archive: {}",
                archiveSupport.pollAndQueryArchiveForDataItem(dataItem2.getId()));
        
        // Perform the test
        
        final BusinessObjectState dataItem2state = underTest.findLatestState(dataItem2.getId());
        
        assertStateObjectIsValid(dataItem2.getId(), dataItem2state, true);
        
        // This makes assumptions about how a DataItem is mapped; specifically that the state DU contains the
        // the name of the DataItem in the <title> of the DU
        assertEquals(dataItem2.getName(), dataItem2state.getLatestState().getTitle());
        
        // The Root DU of each state should be the same
        assertEquals(dataItem1State.getRoot(), dataItem2state.getRoot());
    }
    
    /**
     * Polls the deposit feed until an ingest.complete event is found, or until the polling times out.
     * 
     * @param depositId
     *            the deposit feed to poll
     * @param parser
     *            the parser which parses the feed
     * @return true if the deposit succeeded, false otherwise.
     */
    private boolean pollDepositFeedForCompletion(final URL depositId, final AtomDepositDocumentParser parser) {
        final BooleanHolder success = new BooleanHolder(false);
        
        // Poll until the deposit completes
        archiveSupport.pollAndQueryArchive(1000, 20, new ArchiveSupport.PollQuery<Object>() {
            @Override
            public Object execute() throws DcsConnectorFault {
                DepositDocument dd;
                try {
                    dd = parser.parse(depositId.openStream());
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (dd.isComplete()) {
                    success.setValue(dd.isSuccessful());
                    return dd;
                }
                
                return null;
            }
        });
        
        return success.getValue();
    }
    
    /**
     * Maps the supplied DataItem to a DCP. It insures that that DCP is valid according to the DCP 1.0 schema, and
     * insures that the DCP conforms to the DataItem profile.
     * 
     * @param collectionDcsId
     *            the DCS identifier of the DU representing the Collection the DataItem is being deposited to
     * @param dataItem
     *            the DataItem to map to a DCP
     * @return the archival representation of the DataItem, the DCP
     * @throws DcpMappingException
     * @throws IOException
     * @throws SAXException
     */
    private Dcp mapDataItem(String collectionDcsId, DataItem dataItem) throws DcpMappingException, IOException,
            SAXException {
        final Dcp dataItemDcp = dataItemMapper.toDcp(collectionDcsId, dataItem);
        validationUtil.validate(dataItemDcp);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        builder.buildSip(dataItemDcp, baos);
        assertTrue("DataItem DCP did not conform to the profile ('" + dataItemProfile + "') DataItem: \n'" + dataItem
                + "'\nDCP:\n" + new String(baos.toByteArray()), dataItemProfiler.conforms(dataItemDcp));
        return dataItemDcp;
    }
    
    /**
     * Composes and returns a {@link DataFile} from the supplied Java {@link File}. The {@code DataFile} will have the
     * supplied identifier. Note that the {@code DataFile} format is <em>not</em> set by this method.
     * 
     * 
     * @param dataFileId
     *            the identifier to be placed on the returned {@code DataFile}
     * @param javaFile
     *            the Java file used to compose and generate the returned {@code DataFile}
     * @return a {@code DataFile} based on the supplied {@code javaFile}
     */
    private static DataFile fromJavaFile(String dataFileId, File javaFile) {
        DataFile df = new DataFile();
        df.setId(dataFileId);
        df.setName(javaFile.getName());
        df.setSource(javaFile.toURI().toString());
        df.setPath(javaFile.getAbsolutePath());
        df.setSize(javaFile.length());
        
        return df;
    }
    
    /**
     * Asserts the state object isn't null, has a Root and State DU, and that the relationship between the two DUs are
     * correct.
     * 
     * @param dataItemId
     *            the identifier of the business object {@code state}
     * @param state
     *            the state being validated
     * @param isUpdate
     *            should be set to true if the presence and value of the {@code isSuccessorOf} relationship should be
     *            updated
     * @throws IOException
     *             never
     */
    private void assertStateObjectIsValid(String dataItemId, BusinessObjectState state, boolean isUpdate)
            throws IOException {
        assertNotNull("Expected to find BusinessObjectState for DataItem '" + dataItemId + "'", state);
        assertNotNull("Expected to find root DU for DataItem '" + dataItemId + "'", state.getRoot());
        assertNotNull("Expected to find state DU for DataItem '" + dataItemId + "'", state.getLatestState());
        if (log.isDebugEnabled()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            builder.buildDeliverableUnit(state.getRoot(), out);
            out.write("\n".getBytes());
            builder.buildDeliverableUnit(state.getLatestState(), out);
            log.debug("Found BusinessObjectState:\n{}\n{}", state,
                    IOUtils.toString(new ByteArrayInputStream(out.toByteArray())));
        }
        assertTrue(state.getRoot().getParents().contains(new DcsDeliverableUnitRef(collectionArchiveId)));
        assertTrue(state.getLatestState().getParents().contains(new DcsDeliverableUnitRef(state.getRoot().getId())));
        
        if (isUpdate) {
            assertEquals(1, state.getLatestState().getRelations().size());
            assertEquals(DcsRelationship.IS_SUCCESSOR_OF,
                    DcsRelationship.fromString(state.getLatestState().getRelations().iterator().next().getRelUri()));
        }
        else {
            assertTrue(state.getLatestState().getRelations().isEmpty());
        }
    }
    
    /**
     * Holder for a Boolean value; allows the value of a Boolean to be mutated in anonymous inner classes.
     */
    private class BooleanHolder {
        private boolean value;
        
        private BooleanHolder(boolean initialValue) {
            this.value = initialValue;
        }
        
        private void setValue(boolean value) {
            this.value = value;
        }
        
        private boolean getValue() {
            return value;
        }
    }
}
