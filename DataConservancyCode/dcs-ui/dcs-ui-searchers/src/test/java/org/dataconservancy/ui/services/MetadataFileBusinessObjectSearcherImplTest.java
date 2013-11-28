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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import org.junit.Test;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;

import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.util.MockSearchIterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class MetadataFileBusinessObjectSearcherImplTest extends ArchiveBusinessObjectSearcherImplTest {

    @Override
    ArchiveBusinessObjectSearcherImpl getInstanceUnderTest() {
        final ParentSearcher mockParentSearcher = mock(ParentSearcher.class);
        return new MetadataFileBusinessObjectSearcherImpl(mockConnector, mockIdService);
    }

    @Test
    public void testFindMetadataFile() throws Exception {

        final String expectedId = metadataFileOne.getId();
        final DcsFile dcsFile = new DcsFile();
        dcsFile.setId(expectedId);

        // This test is sort of uninteresting since it mostly depends on the
        // mocked DcsConnector

        DcsConnector con = mock(DcsConnector.class);
        when(con.search(anyString(), anyInt(), anyInt())).thenReturn(
                new MockSearchIterator(Arrays.asList((DcsEntity) dcsFile)));

       MetadataFileBusinessObjectSearcherImpl bos = new MetadataFileBusinessObjectSearcherImpl(
                con, mock(IdService.class));

        DcsFile test = bos.findMetadataFile(metadataFileOne.getId());

        assertEquals(expectedId, test.getId());        
       
    }

    /**
     * Tests that when a object graph containing the first version of a MetadataFile DU is returned by the connector, that
     * the ArchiveBusinessObjectSearcherImpl correctly returns it as the latest business object DU, and as the root DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestBusinessObject() throws Exception {
        final DcsDeliverableUnit mf1_du = builder.buildDeliverableUnit(IOUtils.toInputStream(METADATAFILE_STATE_DU_WITH_PARENT));
        assertNotNull(mf1_du);
        final Set<DcsEntity> expected = new HashSet<DcsEntity>();
        expected.add(mf1_du);

        final DcsDeliverableUnit mf2_du = builder.buildDeliverableUnit(IOUtils.toInputStream(METADATAFILE_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        assertNotNull(mf2_du);
        expected.add(mf2_du);

        DcsConnector con = mock(DcsConnector.class);
        when(con.search(anyString(), anyInt(), anyInt())).thenReturn(
                new MockSearchIterator(expected));

        // Mock interactions with IdService to succeed
        final IdService idService = mock(IdService.class);
        final Identifier mf1Id = mock(Identifier.class);
        final Identifier mf2Id = mock(Identifier.class);

        when(idService.fromUid(metadataFileOne.getId())).thenReturn(mf1Id);
        when(idService.fromUid(metadataFileOne.getId())).thenReturn(mf2Id);
        when(mf1Id.getType()).thenReturn(Types.METADATA_FILE.name());
        when(mf2Id.getType()).thenReturn(Types.METADATA_FILE.name());
        MetadataFileBusinessObjectSearcherImpl bos = new MetadataFileBusinessObjectSearcherImpl(
                con, idService);

        // Make sure that the latest version is returned

        BusinessObjectState state = bos.findLatestState(metadataFileOne.getId());
        assertNotNull(state);
        DcsDeliverableUnit test = state.getLatestState();

        assertEquals(mf2_du, test);
    }

    /**
     * Tests that when a object graph containing the first and second version of a DataItem DU is returned by the
     * connector, that the ArchiveBusinessObjectSearcherImpl correctly returns the latest business object DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestMetadataFileWhenQueryReturnsSingleDuSecondVersion() throws Exception {
        Collection<DcsEntity> dus = new HashSet<DcsEntity>();

        final DcsDeliverableUnit expectedRootDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(METADATAFILE_ROOT_DU));
        final DcsDeliverableUnit expectedStateDuV1 = builder.buildDeliverableUnit(
                IOUtils.toInputStream(METADATAFILE_STATE_DU_WITH_PARENT));
        final DcsDeliverableUnit expectedStateDuV2 = builder.buildDeliverableUnit(
                IOUtils.toInputStream(METADATAFILE_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        dus.add(expectedStateDuV2);
        dus.add(expectedStateDuV1);
        dus.add(expectedRootDu);

        when(mockConnector.search(METADATA_FILE_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET))
                .thenReturn(new MockSearchIterator(dus));

        BusinessObjectState state = underTest.findLatestState(METADATA_FILE_BIZ_ID);

        verify(mockConnector).search(METADATA_FILE_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
        
        assertNotNull(state);
        assertEquals(expectedRootDu, state.getRoot());
        assertEquals(expectedStateDuV2, state.getLatestState());

    }

}
