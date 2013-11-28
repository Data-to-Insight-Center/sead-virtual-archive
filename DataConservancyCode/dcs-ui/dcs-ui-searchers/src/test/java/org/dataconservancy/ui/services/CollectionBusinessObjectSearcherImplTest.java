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
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.util.MockSearchIterator;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class CollectionBusinessObjectSearcherImplTest extends ArchiveBusinessObjectSearcherImplTest {

    @Override
    ArchiveBusinessObjectSearcherImpl getInstanceUnderTest() {
        return new CollectionBusinessObjectSearcherImpl(mockConnector, mockIdService);
    }

    @Test
    public void testFindLatestBusinessObject() throws Exception {
        final DcsDeliverableUnit col1_du = builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT));
        assertNotNull(col1_du);
        final Set<DcsEntity> expected = new HashSet<DcsEntity>();
        expected.add(col1_du);

        final DcsDeliverableUnit col2_du = builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        assertNotNull(col2_du);
        expected.add(col2_du);

        DcsConnector con = mock(DcsConnector.class);
        when(con.search(anyString(), anyInt(), anyInt())).thenReturn(new MockSearchIterator(expected));

        // Mock interactions with IdService to succeed
        final IdService idService = mock(IdService.class);
        final Identifier col1Id = mock(Identifier.class);
        final Identifier col2Id = mock(Identifier.class);

        when(idService.fromUid(collectionWithData.getId())).thenReturn(col1Id);
        when(idService.fromUid(collectionWithData.getId())).thenReturn(col2Id);
        when(col1Id.getType()).thenReturn(Types.COLLECTION.name());
        when(col2Id.getType()).thenReturn(Types.COLLECTION.name());
        CollectionBusinessObjectSearcherImpl bos = new CollectionBusinessObjectSearcherImpl(
                con, idService);

        // Make sure that the latest version is returned

        BusinessObjectState state = bos.findLatestState(collectionWithData.getId());
        assertNotNull(state);
        DcsDeliverableUnit test = state.getLatestState();

        assertEquals(col2_du, test);

    }

    /**
     * Tests that when a single DU representing the first version of a state DU is returned by the connector, that the
     * ArchiveBusinessObjectSearcherImpl correctly returns it as the latest business object DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestObjectWhenQueryReturnsSingleStateDu() throws Exception {
        java.util.Collection<DcsEntity> dus = new HashSet<DcsEntity>();
        final DcsDeliverableUnit expectedStateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT));
        dus.add(expectedStateDu);

        when(mockConnector.search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET)).thenReturn(new MockSearchIterator(dus));

        BusinessObjectState state = underTest.findLatestState(COLLECTION_BIZ_ID);

        assertNotNull(state);
        assertEquals(expectedStateDu, state.getLatestState());
        assertNull(state.getRoot());

        verify(mockConnector).search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
    }

    /**
     * Tests that when a single DU representing the second (latest, in this example) version of a state DU is returned
     * by the connector, that the ArchiveBusinessObjectSearcherImpl correctly returns it as the latest business object
     * DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestObjectWhenQueryReturnsSingleStateDuReferencingPredecessor() throws Exception {
        java.util.Collection<DcsEntity> dus = new HashSet<DcsEntity>();
        final DcsDeliverableUnit expectedStateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        dus.add(expectedStateDu);


        when(mockConnector.search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET)).thenReturn(new MockSearchIterator(dus));

        BusinessObjectState state = underTest.findLatestState(COLLECTION_BIZ_ID);

        assertNotNull(state);
        assertEquals(expectedStateDu, state.getLatestState());
        assertNull(state.getRoot());

        verify(mockConnector).search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
    }

    /**
     * Tests that when all versions of a of a state DU are returned by the connector, that the
     * ArchiveBusinessObjectSearcherImpl correctly returns version 2 as the latest business object DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestObjectWhenQueryReturnsAllStateDus() throws Exception {
        java.util.Collection<DcsEntity> dus = new HashSet<DcsEntity>();
        final DcsDeliverableUnit expectedStateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        dus.add(expectedStateDu);
        dus.add(builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT)));

        when(mockConnector.search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET)).thenReturn(new MockSearchIterator(dus));

        BusinessObjectState state = underTest.findLatestState(COLLECTION_BIZ_ID);

        assertNotNull(state);
        assertEquals(expectedStateDu, state.getLatestState());
        assertNull(state.getRoot());

        verify(mockConnector).search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
    }

    /**
     * Tests that when all versions of a of a state DU and the Root DU are returned by the connector, that the
     * ArchiveBusinessObjectSearcherImpl correctly returns the version 2 State DU as the latest business object DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestObjectWhenQueryReturnsRootAndStateDus() throws Exception {
        java.util.Collection<DcsEntity> dus = new HashSet<DcsEntity>();
        final DcsDeliverableUnit expectedStateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        final DcsDeliverableUnit expectedRootDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_ROOT_DU));
        dus.add(expectedStateDu);
        dus.add(builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT)));
        dus.add(expectedRootDu);

        when(mockConnector.search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET)).thenReturn(new MockSearchIterator(dus));

        BusinessObjectState state = underTest.findLatestState(COLLECTION_BIZ_ID);

        assertNotNull(state);
        assertEquals(expectedStateDu, state.getLatestState());

        assertNotNull(state.getRoot());
        assertEquals(expectedRootDu, state.getRoot());

        verify(mockConnector).search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
    }

    /**
     * Tests that when all versions of a of a state DU and the Root DU are returned in no particular order, that the
     * ArchiveBusinessObjectSearcherImpl correctly returns the version 2 State DU as the latest business object DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestObjectWhenQueryReturnsRootAndStateDusInNoParticularOrder() throws Exception {
        java.util.Collection<DcsEntity> dus = new HashSet<DcsEntity>();
        final DcsDeliverableUnit expectedStateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT_AND_PREDECESSOR));
        final DcsDeliverableUnit expectedRootDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(COLLECTION_ROOT_DU));
        dus.add(builder.buildDeliverableUnit(IOUtils.toInputStream(COLLECTION_STATE_DU_WITH_PARENT)));
        dus.add(expectedStateDu);
        dus.add(expectedRootDu);

        when(mockConnector.search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET)).thenReturn(new MockSearchIterator(dus));

        BusinessObjectState state = underTest.findLatestState(COLLECTION_BIZ_ID);

        assertNotNull(state);
        assertEquals(expectedStateDu, state.getLatestState());

        assertNotNull(state.getRoot());
        assertEquals(expectedRootDu, state.getRoot());

        verify(mockConnector).search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
    }

    /**
     * When a search of the archive yields no results, the returned state will be null.
     *
     * @throws Exception
     */
    @Test
    public void testSearchWhenQueryReturnsNoResults() throws Exception {
        when(mockConnector.search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET)).thenReturn(
                new MockSearchIterator(Collections.<DcsEntity>emptyList()));

        assertNull(underTest.findLatestState(COLLECTION_BIZ_ID));
        verify(mockConnector).search(COLLECTION_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
    }
    
}
