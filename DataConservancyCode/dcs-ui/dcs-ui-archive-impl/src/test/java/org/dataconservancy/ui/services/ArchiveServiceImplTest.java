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

import org.apache.commons.io.IOUtils;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.dao.ArchiveDepositInfoDAO;
import org.dataconservancy.ui.dcpmap.DcpMapper;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.DEPOSITED;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.PENDING;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.COLLECTION;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.DATASET;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.METADATA_FILE;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Type.REGISTRY_ENTRY;
import static org.dataconservancy.ui.services.ArchiveBusinessObjectSearcherImplTest.COLLECTION_ROOT_DU;
import static org.dataconservancy.ui.services.ArchiveBusinessObjectSearcherImplTest.COLLECTION_STATE_DU_WITH_PARENT;
import static org.dataconservancy.ui.services.ArchiveBusinessObjectSearcherImplTest.COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR;
import static org.dataconservancy.ui.services.ArchiveBusinessObjectSearcherImplTest.DATAITEM_ROOT_DU;
import static org.dataconservancy.ui.services.ArchiveBusinessObjectSearcherImplTest.DATAITEM_STATE_DU_VER_1;
import static org.dataconservancy.ui.services.ArchiveBusinessObjectSearcherImplTest.METADATAFILE_STATE_DU_WITH_PARENT;
import static org.dataconservancy.ui.services.ArchiveBusinessObjectSearcherImplTest.METADATAFILE_ROOT_DU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/**
 * A suite of unit tests for the {@link ArchiveServiceImpl} using Mockito instead of the hard-to-deal-with
 * mock objects that are used by the {@link ArchiveServiceTest}.
 *
 * @see ArchiveServiceTest
 */
public class ArchiveServiceImplTest {

    private static final String METADATA_FORMAT_DU = "<DeliverableUnit id=\"81ad5b5f-21fc-438c-ab86-e556886909b1\">\n" +
            "   <type>dataconservancy.types:registry-entry:metadataformat</type>\n" +
            "   <title>test format</title>\n" +
            "   <formerExternalRef>dataconservancy.org:formats:file:metadata:test:xml</formerExternalRef>\n" +
            "  </DeliverableUnit>";
    
    private static final String METADATA_SCHEME_DU = "<DeliverableUnit id=\"0aca0c03-86a6-4c89-b26b-5a82e9485728\">\n" +
            "   <type>dataconservancy.types:registry-entry:metadatascheme</type>\n" +
            "   <title>test master scheme</title>\n" +
            "   <formerExternalRef>www.dataconservancy.org</formerExternalRef>\n" +
            "  </DeliverableUnit>";
    
    private static final String REGISTRY_ENTRY_DU = "<DeliverableUnit id=\"32b26d0f-d822-47bf-b5a3-204bbb6d03ca\">\n" +
            "   <type>dataconservancy.org:types:registry:entry</type>\n" +
            "   <title>registry entry for test format</title>\n" +
            "   <formerExternalRef>id:registryEntry</formerExternalRef>\n" +
            "   <alternateIdentifier>\n" +
            "       <typeId>dataconservancy.org:types:registry:entry</typeId>\n" +
            "       <idValue>dataconservancy.org:formats:file:test:2013</idValue>\n" +
            "   </alternateIdentifier>\n" +
            "  </DeliverableUnit>";
    
    /**
     * Used by test methods to build DCS entities from XML serializations.
     */
    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    /**
     * Mock ArchiveDepositInfoDAO, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private ArchiveDepositInfoDAO adiDao;

    /**
     * Mock ArchiveDepositInfoDAO, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private DcsConnector connector;

    /**
     * Mock DcpMapper for DataItems, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private DcpMapper<DataItem> dataItemMapper;

    /**
     * Mock DcpMapper for DataFiles, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private DcpMapper<DataFile> dataFileMapper;

    /**
     * Mock DcpMapper for Collections, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private DcpMapper<Collection> collectionMapper;

    /**
     * Mock DcpMapper for MetadataFiles, a collaborator of the ArchiveServiceImpl. Instantiated by
     * {@link #instantiateMocks()};
     */
    private DcpMapper<MetadataFile> metadataFileMapper;
    
    /**
     * Mock DcpMapper for a MetadataFormat registry entry. Instantiated by {@link #instanciateMocks()};
     */
    private org.dataconservancy.profile.api.DcpMapper<RegistryEntry<DcsMetadataFormat>> metadataFormatMapper;
    
    /**
     * Mock Profile for DataItems, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private Profile<DataItem> dataItemProfile;

    /**
     * Mock DepositDocumentResolver, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private DepositDocumentResolver depositDocumentResolver;

    /**
     * Mock IdService, a collaborator of the ArchiveServiceImpl.  Instantiated by
     * {@link #instantiateMocks()}.
     */
    private IdService idService;

    private ArchiveUtil archiveUtil;

    /**
     * Controls the behavior of the background polling thread launched by ArchiveServiceImpl.  Supplied to the
     * ArchiveServiceImpl upon construction by {@link #newInstance()}.  Any number greater than 0 will instruct the
     * ArchiveServiceImpl to start a background polling thread, and sleep {@code sleepTimeMs} between invokations of
     * {@link org.dataconservancy.ui.services.ArchiveServiceImpl#pollArchive()}.
     */
    private final long sleepTimeMs = -1;

    /**
     * Instantiate mock instances of all the collaborators of {@code ArchiveServiceImpl}.  This method does not
     * configure any mock behaviors.
     */
    @Before
    public void instantiateMocks() {
        adiDao = mock(ArchiveDepositInfoDAO.class, "Archive Deposit Info DAO");
        connector = mock(DcsConnector.class);
        dataItemMapper = mock(DcpMapper.class);
        dataFileMapper = mock(DcpMapper.class);
        collectionMapper = mock(DcpMapper.class);
        metadataFileMapper = mock(DcpMapper.class);
        dataItemProfile = mock(Profile.class);
        metadataFormatMapper = mock(org.dataconservancy.profile.api.DcpMapper.class);
        depositDocumentResolver = mock(DepositDocumentResolver.class);
        idService = mock(IdService.class);
        archiveUtil = mock(ArchiveUtil.class);
    }

    /**
     * This test insures that when a {@code Collection} object is deposited to the archive <em>for the first time</em>
     * (e.g. no archival representations of the {@code Collection} exist in the archive a the time of deposit), the
     * correct information is persisted with the {@code ArchiveDepositInfoDAO}.
     *
     * @throws Exception
     */
    @Test
    public void testDepositInfoInitialCollectionDeposit() throws Exception {
        // 1) Set up objects needed for mocked behaviors and test assertions

        // The Collection being deposited
        final String businessId = "id:someid";
        final Collection collectionToDeposit = new Collection();
        collectionToDeposit.setId(businessId);

        // This is the deposit ticket returned by the DcsConnector after a successful deposit
        final String ticket = "http://foo.bar.com";
        final URL depositUrl = new URL(ticket);

        // This is the DCP that is being deposited, produced by the Collection mapper mapping the Collection to a Dcp.
        final Dcp dcpToDeposit = new Dcp();
        dcpToDeposit.addDeliverableUnit(builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_ROOT_DU)));
        dcpToDeposit.addDeliverableUnit(builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT)));

        // 2) configure mock behaviors

        // When the mapper is invoked, return the Dcp to deposit.
        when(collectionMapper.toDcp(null, collectionToDeposit))
                .thenReturn(dcpToDeposit);

        // When the connector's deposit method is invoked, return the deposit url.
        when(connector.depositSIP(dcpToDeposit)).thenReturn(depositUrl);

        // When the ArchiveDepositInfoDAO's add(...) method is invoked, we make assertions about its content using
        // some advanced Mockito capabilities.
        doAnswer(new Answer<ArchiveDepositInfo>() {
            @Override
            public ArchiveDepositInfo answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Archive Deposit Info from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO", args);
                assertEquals("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO",
                        1, args.length);
                assertTrue("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO",
                        args[0] instanceof ArchiveDepositInfo);
                ArchiveDepositInfo adi = (ArchiveDepositInfo) args[0];

                // Now make assertions about the content of the ADI
                assertEquals(businessId, adi.getObjectId());
                assertEquals(COLLECTION, adi.getObjectType());
                assertNotNull(adi.getDepositDateTime());
                final long now = DateTime.now().getMillis();
                assertTrue(now >= adi.getDepositDateTime().getMillis());
                assertEquals(ticket, adi.getDepositId());
                assertEquals(PENDING, adi.getDepositStatus());
                assertNull(adi.getArchiveId());
                assertNull(adi.getStateId());

                // The add(...) method of ArchiveDepositInfoDAO is a void method, so we just return null here.
                return null;
            }
        }).when(adiDao).add(any(ArchiveDepositInfo.class));

        // 3) Execute the method under test

        // Obtain a fresh instance of ArchiveServiceImpl, which will construct a new ArchiveServiceImpl using the mocks
        // that we have configured in this test method.
        final ArchiveServiceImpl underTest = newInstance();

        // Execute our method under test: depositing a Collection, which should result in an ArchiveDepositInfo being
        // add(...)ed to the ArchiveDepositInfoDAO, thereby invoking our assertions.
        underTest.deposit(null, businessId, collectionToDeposit, collectionMapper);

        // 4) Verify mocked behavior
        
        // Finally, verify that the mocked behavior was invoked as expected.
        verify(collectionMapper).toDcp(null, collectionToDeposit);
        verify(connector).depositSIP(dcpToDeposit);
        verify(adiDao).add(any(ArchiveDepositInfo.class));
    }

    /**
     * This test insures that when a {@code Collection} object is deposited to the archive <em>as an update</em>
     * (e.g. archival representations of the {@code Collection} already exist in the archive a the time of deposit), the
     * correct information is persisted with the {@code ArchiveDepositInfoDAO}.
     *
     * @throws Exception
     */
    @Test
    public void testDepositInfoUpdateDeposit() throws Exception {

        // See the inline comments on testDepositInfoInitialDeposit() for an understanding of what is going on
        // in this test method.  They are very similar tests, and use the same pattern:
        // 1) Configure objects used for the mocked behaviors and JUnit assertions
        // 2) Configure mock behavior
        // 3) Execute the method under test (which in turn will execute JUnit assertions in the Mockito Answer)
        // 4) Verify mocked behavior

        final String businessId = "id:someid";
        final Collection collectionToDeposit = new Collection();
        collectionToDeposit.setId(businessId);
        final String ticket = "http://foo.bar.com";
        final URL depositUrl = new URL(ticket);
        final Dcp dcpToDeposit = new Dcp();
        dcpToDeposit.addDeliverableUnit(builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_ROOT_DU)));
        final DcsDeliverableUnit predecessorStateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT));
        final DcsDeliverableUnit stateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        dcpToDeposit.addDeliverableUnit(stateDu);

        when(collectionMapper.toDcp(null, collectionToDeposit))
                .thenReturn(dcpToDeposit);

        when(connector.depositSIP(Matchers.<Dcp>any())).thenReturn(depositUrl);

        when(archiveUtil.getEntity(predecessorStateDu.getId())).thenReturn(predecessorStateDu);

        doAnswer(new Answer<ArchiveDepositInfo>() {
            @Override
            public ArchiveDepositInfo answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO", args);
                assertEquals("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO",
                        1, args.length);
                assertTrue("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO",
                        args[0] instanceof ArchiveDepositInfo);
                ArchiveDepositInfo adi = (ArchiveDepositInfo) args[0];
                assertEquals(businessId, adi.getObjectId());
                assertEquals(COLLECTION, adi.getObjectType());
                assertNotNull(adi.getDepositDateTime());
                final long now = DateTime.now().getMillis();
                assertTrue("Deposit dateTime in the future: '" + adi.getDepositDateTime().getMillis() + "' less than " +
                        "'" + now + "'", now >= adi.getDepositDateTime().getMillis());
                assertEquals(ticket, adi.getDepositId());
                assertEquals(PENDING, adi.getDepositStatus());
                assertNull(adi.getArchiveId());
                assertNull(adi.getStateId());
                return null;
            }
        }).when(adiDao).add(any(ArchiveDepositInfo.class));

        final ArchiveServiceImpl underTest = newInstance();

        underTest.deposit(null, businessId, collectionToDeposit, collectionMapper);

        verify(collectionMapper).toDcp(null, collectionToDeposit);
        verify(connector).depositSIP(Matchers.<Dcp>any());
        verify(adiDao).add(any(ArchiveDepositInfo.class));
    }

    @Test
    public void testRetrieveDataSetFirstVersion() throws Exception {
        final DcsDeliverableUnit collectionDu = builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_ROOT_DU));
        final DcsDeliverableUnit rootDu = builder.buildDeliverableUnit(IOUtils.toInputStream(DATAITEM_ROOT_DU));
        final DcsDeliverableUnit stateDu = builder.buildDeliverableUnit(IOUtils.toInputStream(DATAITEM_STATE_DU_VER_1));

        final String depositId = "id:depositId";
        final String archiveId = rootDu.getId();
        final String stateId = stateDu.getId();
        final String businessId = "id:businessId";

        final ArchiveDepositInfo dataSetAdi = mock(ArchiveDepositInfo.class);

        when(dataSetAdi.getDepositStatus()).thenReturn(DEPOSITED);

        // We're returning an ADI that represents the initial deposit of a DataItem in the archive.  That means it will
        // have a archive ID and state ID.
        when(dataSetAdi.getArchiveId()).thenReturn(archiveId);
        when(dataSetAdi.getStateId()).thenReturn(stateId);
        when(dataSetAdi.getObjectType()).thenReturn(DATASET);

        final java.util.Collection<DcsEntity> ancestors = new ArrayList<DcsEntity>();
        ancestors.add(stateDu);

        final AncestrySearcher ancestrySearcher = mock(AncestrySearcher.class);
        when(ancestrySearcher.getAncestorsOf(stateId, true)).thenReturn(ancestors);

        when(archiveUtil.getEntity(archiveId)).thenReturn(rootDu);

        final java.util.Collection<DcsDeliverableUnit> parents = new ArrayList<DcsDeliverableUnit>();
        parents.add(rootDu);
        parents.add(collectionDu);

        final ParentSearcher parentSearcher = mock(ParentSearcher.class);
        when(parentSearcher.getParentsOf(stateId, DcsDeliverableUnit.class)).thenReturn(parents);

        when(adiDao.lookup(depositId)).thenReturn(dataSetAdi);

        when(dataItemMapper.fromDcp(Matchers.<Dcp>any())).thenAnswer(new Answer<DataItem>() {
            @Override
            public DataItem answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull(args);
                assertEquals(1, args.length);
                assertNotNull(args[0]);
                assertTrue(args[0] instanceof Dcp);

                final Dcp dcp = (Dcp) args[0];

                assertEquals(ancestors.size(), dcp.getDeliverableUnits().size());
                assertTrue(dcp.getDeliverableUnits().contains(rootDu));
                assertTrue(dcp.getDeliverableUnits().contains(stateDu));
                assertFalse(rootDu.equals(stateDu));

                DataItem dataItem = new DataItem();
                dataItem.setId(businessId);
                return dataItem;
            }
        });

        final ArchiveServiceImpl underTest = newInstance();
        underTest.setAncestrySearcher(ancestrySearcher);

        ArchiveSearchResult<DataItem> dataItemResult = underTest.retrieve(depositId, dataItemMapper);
        
        assertNotNull(dataItemResult);
        assertEquals(1, dataItemResult.getResultCount());
        assertEquals(businessId, dataItemResult.getResults().iterator().next().getId());


        verify(dataSetAdi).getDepositStatus();
        verify(dataSetAdi).getArchiveId();
        verify(dataSetAdi).getObjectType();
        verify(ancestrySearcher).getAncestorsOf(stateId, true);
        verify(archiveUtil).getEntity(archiveId);
        verify(adiDao).lookup(depositId);
        verify(dataItemMapper).fromDcp(Matchers.<Dcp>any());
    }

    @Test
    public void testRetrieveMetadataFileFirstVersion() throws Exception {
        final DcsDeliverableUnit rootDu = builder.buildDeliverableUnit(IOUtils.toInputStream(METADATAFILE_ROOT_DU));
        final DcsDeliverableUnit stateDu = builder.buildDeliverableUnit(IOUtils.toInputStream(METADATAFILE_STATE_DU_WITH_PARENT));

        final String depositId = "id:depositId";
        final String archiveId = rootDu.getId();
        final String stateId = stateDu.getId();
        final String businessId = "id:businessId";

        final ArchiveDepositInfo metadataFileAdi = mock(ArchiveDepositInfo.class);

        when(metadataFileAdi.getDepositStatus()).thenReturn(DEPOSITED);

        // We're returning an ADI that represents the initial deposit of a DataItem in the archive.  That means it will
        // have a archive ID and state ID.
        when(metadataFileAdi.getArchiveId()).thenReturn(archiveId);
        when(metadataFileAdi.getStateId()).thenReturn(stateId);
        when(metadataFileAdi.getObjectType()).thenReturn(METADATA_FILE);

        final java.util.Collection<DcsEntity> ancestors = new ArrayList<DcsEntity>();
        ancestors.add(stateDu);

        final AncestrySearcher ancestrySearcher = mock(AncestrySearcher.class);
        when(ancestrySearcher.getAncestorsOf(stateId, true)).thenReturn(ancestors);

        when(archiveUtil.getEntity(archiveId)).thenReturn(rootDu);
 
        when(adiDao.lookup(depositId)).thenReturn(metadataFileAdi);

        when(metadataFileMapper.fromDcp(Matchers.<Dcp>any())).thenAnswer(new Answer<MetadataFile>() {
            @Override
            public MetadataFile answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                assertNotNull(args);
                assertEquals(1, args.length);
                assertNotNull(args[0]);
                assertTrue(args[0] instanceof Dcp);
                
                final Dcp dcp = (Dcp) args[0];
                assertEquals(ancestors.size(), dcp.getDeliverableUnits().size());
                assertTrue(dcp.getDeliverableUnits().contains(stateDu));
                assertTrue(dcp.getDeliverableUnits().contains(rootDu));
                assertFalse(rootDu.equals(stateDu));

                MetadataFile metadataFile = new MetadataFile();
                metadataFile.setId(businessId);
                return metadataFile;
            }
        });

        final ArchiveServiceImpl underTest = newInstance();
        underTest.setAncestrySearcher(ancestrySearcher);

        ArchiveSearchResult<MetadataFile> metadataFileResult = underTest.retrieve(depositId, metadataFileMapper);
        
        assertNotNull(metadataFileResult);
        assertEquals(1, metadataFileResult.getResultCount());
        assertEquals(businessId, metadataFileResult.getResults().iterator().next().getId());


        verify(metadataFileAdi).getDepositStatus();
        verify(metadataFileAdi).getArchiveId();
        //Metadata file is the fourth type so it will be called 4 times, this is sort of a lame test
        verify(metadataFileAdi, times(4)).getObjectType();
        verify(ancestrySearcher).getAncestorsOf(stateId, true);
        verify(archiveUtil).getEntity(archiveId);
        verify(adiDao).lookup(depositId);
        verify(metadataFileMapper).fromDcp(Matchers.<Dcp>any());
    }
    
    /**
     * Tests that a metadata format registry entry can be deposited and retrieved
     * @throws Exception
     */
    @Test
    public void testDepositMetadataFormatRegistryEntry() throws Exception {
        final DcsDeliverableUnit formatDu = builder.buildDeliverableUnit(IOUtils.toInputStream(METADATA_FORMAT_DU));
        final DcsDeliverableUnit schemeDu = builder.buildDeliverableUnit(IOUtils.toInputStream(METADATA_SCHEME_DU));
        final DcsDeliverableUnit entryDu = builder.buildDeliverableUnit(IOUtils.toInputStream(REGISTRY_ENTRY_DU));
        
        String[] keys = {"dataconservancy.org:formats:file:test:2013"};
        
        DcsMetadataFormat format = new DcsMetadataFormat();
        format.setName("test format");
        format.setVersion("1.0");
        
        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("test scheme");
        format.addScheme(scheme);
        
        RegistryEntry<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>("id:registryEntry", format, "DcsMetadataFormat", Arrays.asList(keys), "registry entry for test format");

        final String businessId = entry.getId();        
        
        // This is the deposit ticket returned by the DcsConnector after a successful deposit
        final String ticket = "http://foo.bar.com";
        final URL depositUrl = new URL(ticket);

        // This is the DCP that is being deposited, produced by the Collection mapper mapping the Collection to a Dcp.
        final Dcp dcpToDeposit = new Dcp();
        dcpToDeposit.addDeliverableUnit(formatDu);
        dcpToDeposit.addDeliverableUnit(schemeDu);
        dcpToDeposit.addDeliverableUnit(entryDu);

        // When the mapper is invoked, return the Dcp to deposit.
        when(metadataFormatMapper.to(entry, null))
                .thenReturn(dcpToDeposit);

        // When the connector's deposit method is invoked, return the deposit url.
        when(connector.depositSIP(dcpToDeposit)).thenReturn(depositUrl);

        // When the ArchiveDepositInfoDAO's add(...) method is invoked, we make assertions about its content using
        // some advanced Mockito capabilities.
        doAnswer(new Answer<ArchiveDepositInfo>() {
            @Override
            public ArchiveDepositInfo answer(InvocationOnMock invocation) throws Throwable {

                // Extract the Archive Deposit Info from the InvocationOnMock
                Object[] args = invocation.getArguments();
                assertNotNull("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO", args);
                assertEquals("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO",
                        1, args.length);
                assertTrue("Expected one argument: the ArchiveDepositInfo being added to the ADI DAO",
                        args[0] instanceof ArchiveDepositInfo);
                ArchiveDepositInfo adi = (ArchiveDepositInfo) args[0];

                // Now make assertions about the content of the ADI
                assertEquals(businessId, adi.getObjectId());
                assertEquals(REGISTRY_ENTRY, adi.getObjectType());
                assertNotNull(adi.getDepositDateTime());
                final long now = DateTime.now().getMillis();
                assertTrue(now >= adi.getDepositDateTime().getMillis());
                assertEquals(ticket, adi.getDepositId());
                assertEquals(PENDING, adi.getDepositStatus());
                assertNull(adi.getArchiveId());
                assertNull(adi.getStateId());

                // The add(...) method of ArchiveDepositInfoDAO is a void method, so we just return null here.
                return null;
            }
        }).when(adiDao).add(any(ArchiveDepositInfo.class));

        // 3) Execute the method under test

        // Obtain a fresh instance of ArchiveServiceImpl, which will construct a new ArchiveServiceImpl using the mocks
        // that we have configured in this test method.
        final ArchiveServiceImpl underTest = newInstance();

        // Execute our method under test: depositing a Collection, which should result in an ArchiveDepositInfo being
        // add(...)ed to the ArchiveDepositInfoDAO, thereby invoking our assertions.
        underTest.deposit(businessId, entry, metadataFormatMapper);

        // 4) Verify mocked behavior
        
        // Finally, verify that the mocked behavior was invoked as expected.
        verify(metadataFormatMapper).to(entry, null);
        verify(connector).depositSIP(dcpToDeposit);
        verify(adiDao).add(any(ArchiveDepositInfo.class));
    }
    
    /**
     * This test insures that an ArchiveDepositInfo object be successfully updated when a Collection object has been
     * successfully deposited in the archive.
     *
     * @throws Exception
     */
    @Test
    @Ignore("Revist Test")
    public void testRetrieveDepositInfoWhenOneCollectionUpdateIsPending() throws Exception {

        // Construct an ADI representing the state of a Collection object that has been previously
        // deposited to the archive.
        final ArchiveDepositInfo pendingUpdate = new ArchiveDepositInfo();
        final DateTime depositDateTime = DateTime.now();
        final String depositId = "id:deposit:1";
        final String businessId = "id:object:1";
        final String rootDuId = "id:archive:1";
        final String stateId = "id:archive:2";
        pendingUpdate.setDepositDateTime(depositDateTime);
        pendingUpdate.setDepositStatus(ArchiveDepositInfo.Status.PENDING);
        pendingUpdate.setDepositId(depositId);
        pendingUpdate.setObjectId(businessId);
        pendingUpdate.setObjectType(COLLECTION);

        // Mock Deposit Document which will supply our mock root deposit DU
        final DepositDocument depositDocument = mock(DepositDocument.class);

        // Our mock root deposit DU.  In this case, the root deposit DU is the same as the
        // Root DU of a Collection serialization.
        final DcsDeliverableUnit rootDepositDu = mock(DcsDeliverableUnit.class, "Deposited Root DU");

        // Our mock state DU. The state DU is deposited with the Root DU of the Collection.
        final DcsDeliverableUnit stateDu = mock(DcsDeliverableUnit.class, "Deposited State DU");

        // When resolving the deposit id, return our mocked deposit document.
        when(depositDocumentResolver.resolve(depositId)).thenReturn(depositDocument);

        // Mock the deposit document to indicate success
        when(depositDocument.isComplete()).thenReturn(true);
        when(depositDocument.isSuccessful()).thenReturn(true);
        Set<DcsEntity> depositedEntities = new HashSet<DcsEntity>();
        depositedEntities.add(rootDepositDu);
        depositedEntities.add(stateDu);
        when(depositDocument.getEntities()).thenReturn(depositedEntities);

        // Return the root deposit DU, which is the same as the Root DU of the Collection.
        when(depositDocument.getRoot()).thenReturn(rootDepositDu);

        // Return a Set of entities in the Deposit when getEntities() is called on the
        // Deposit Document
        final HashSet<DcsEntity> depositEntities = new HashSet<DcsEntity>();
        depositEntities.add(stateDu);
        depositEntities.add(rootDepositDu);
        when(depositDocument.getEntities()).thenReturn(depositEntities);

        // Mock the behavior of the Root Collection DU to not have any relationships (e.g. no successors)
        when(rootDepositDu.getRelations()).thenReturn(Collections.<DcsRelation>emptySet());

        // Mock the correct ID on the Collection Root DU
        when(rootDepositDu.getId()).thenReturn(rootDuId);

        // Mock the correct Id on the Collection State DU
        when(stateDu.getId()).thenReturn(stateId);

        // Mock the parents on the State DU to be the Root DU
        Set<DcsDeliverableUnitRef> parents = new HashSet<DcsDeliverableUnitRef>();
        parents.add(new DcsDeliverableUnitRef(rootDuId));
        when(stateDu.getParents()).thenReturn(parents);

        // Create the instance under test
        final ArchiveServiceImpl underTest = newInstance();

        // Execute the test
        final ArchiveDepositInfo updated = underTest.retrieveDepositInfo(pendingUpdate);

        // Verify the state of the returned ArchiveDepositInfo
        assertNotNull(updated);
        assertEquals(ArchiveDepositInfo.Status.DEPOSITED, updated.getDepositStatus());
        assertEquals(rootDuId, updated.getArchiveId());
        assertEquals(stateId, updated.getStateId());

        // Verify our mocked interactions
        verify(depositDocumentResolver).resolve(depositId);
        verify(depositDocument).isComplete();
        verify(depositDocument).isSuccessful();
        verify(depositDocument).getRoot();
        verify(rootDepositDu).getRelations();
        verify(rootDepositDu, atLeastOnce()).getId();
        verify(stateDu, atLeastOnce()).getId();
        verify(stateDu).getParents();
    }

    /**
     * Convenience method for constructing a new instance of {@code ArchiveServiceImpl} using the mocks created
     * by {@link #instantiateMocks()}.
     *
     * @return
     */
    private ArchiveServiceImpl newInstance() {
        return new ArchiveServiceImpl(adiDao, dataItemMapper, dataFileMapper, collectionMapper, metadataFileMapper, metadataFormatMapper, connector, dataItemProfile,
                depositDocumentResolver, idService, archiveUtil, sleepTimeMs);
    }

}
