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
import org.dataconservancy.access.connector.CountableIterator;
import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.profile.Profile;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
public class DataItemBusinessObjectSearcherImplTest extends ArchiveBusinessObjectSearcherImplTest {

    @Override
    ArchiveBusinessObjectSearcherImpl getInstanceUnderTest() {
        final Profile mockProfile = mock(Profile.class);
        final ParentSearcher mockParentSearcher = mock(ParentSearcher.class);
        when(mockProfile.getType()).thenReturn(DataItemProfile.DATASET_TYPE);
        return new DataItemBusinessObjectSearcherImpl(mockConnector, mockIdService, mockProfile, mockParentSearcher);
    }

    @Test
    public void testFindDataSetFile() throws Exception {

        // Map the DataItem to a Dcp and find the DcsFile for the DataFile
//
//        DataSetMapper dsmap = new DataSetMapper(mock(DataItemBusinessObjectSearcher.class), mock(UserService.class),
//                mock(ArchiveUtil.class));
//
//        Dcp dcp = dsmap.toDcp("parent", dataItemOne);
//
//        Set<DcsEntity> expected = new HashSet<DcsEntity>();
//
//        for (DcsFile file : dcp.getFiles()) {
//            if (file.getName().equals(dataFileOne.getName())) {
//                expected.add(file);
//            }
//        }
//
//        assertEquals(1, expected.size());
//
        final String expectedFileId = dataFileOne.getId();

        // This test is sort of uninteresting since it mostly depends on the
        // mocked DcsConnector

        DcsConnector con = Mockito.mock(DcsConnector.class);
        DcsFile expectedEntity = new DcsFile();
        final Iterator<DcsFile> expectedEntitiesIterator = Arrays.asList(expectedEntity).iterator();
        expectedEntity.setId(expectedFileId);
        when(con.search(anyString(), anyInt(), anyInt())).thenReturn(new CountableIterator<DcsEntity>() {
            @Override
            public long count() {
                return 1;
            }

            @Override
            public boolean hasNext() {
                return expectedEntitiesIterator.hasNext();
            }

            @Override
            public DcsEntity next() {
                return expectedEntitiesIterator.next();
            }

            @Override
            public void remove() {
                expectedEntitiesIterator.remove();
            }
        });

        DataItemBusinessObjectSearcherImpl bos = new DataItemBusinessObjectSearcherImpl(
                con, mock(IdService.class), mock(Profile.class), mock(ParentSearcher.class));

        DcsFile test = bos.findDataSetFile(dataFileOne.getId());

        assertEquals(expectedFileId, test.getId());
    }

    /**
     * Tests that when a object graph containing the first version of a DataItem DU is returned by the connector, that
     * the ArchiveBusinessObjectSearcherImpl correctly returns it as the latest business object DU, and as the root DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestDataItemWhenQueryReturnsFirstVersion() throws Exception {
        final Collection<DcsEntity> dus = new HashSet<DcsEntity>();
        final DcsDeliverableUnit expectedRootDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(DATAITEM_ROOT_DU));
        final DcsDeliverableUnit expectedStateDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(DATAITEM_STATE_DU_VER_1));
        dus.add(expectedRootDu);
        dus.add(expectedStateDu);
        final Iterator<DcsEntity> entityIterator = dus.iterator();

        when(mockConnector.search(DATAITEM_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET))
                .thenReturn(new CountableIterator<DcsEntity>() {
                    @Override
                    public long count() {
                        return dus.size();
                    }

                    @Override
                    public boolean hasNext() {
                        return entityIterator.hasNext();
                    }

                    @Override
                    public DcsEntity next() {
                        return entityIterator.next();
                    }

                    @Override
                    public void remove() {
                        entityIterator.remove();

                    }
                });

        BusinessObjectState state = underTest.findLatestState(DATAITEM_BIZ_ID);

        assertNotNull(state);
        assertEquals(expectedRootDu, state.getRoot());
        assertEquals(expectedStateDu, state.getLatestState());

        verify(mockConnector).search(DATAITEM_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
    }

    /**
     * Tests that when a object graph containing the first and second version of a DataItem DU is returned by the
     * connector, that the ArchiveBusinessObjectSearcherImpl correctly returns the latest business object DU.
     *
     * @throws Exception
     */
    @Test
    public void testFindLatestDataItemWhenQueryReturnsSingleDuSecondVersion() throws Exception {
        final Collection<DcsEntity> dus = new HashSet<DcsEntity>();
        final DcsDeliverableUnit expectedRootDu = builder.buildDeliverableUnit(
                IOUtils.toInputStream(DATAITEM_ROOT_DU));
        final DcsDeliverableUnit expectedStateDuV1 = builder.buildDeliverableUnit(
                IOUtils.toInputStream(DATAITEM_STATE_DU_VER_1));
        final DcsDeliverableUnit expectedStateDuV2 = builder.buildDeliverableUnit(
                IOUtils.toInputStream(DATAITEM_STATE_DU_VER_2));
        dus.add(expectedStateDuV2);
        dus.add(expectedStateDuV1);
        dus.add(expectedRootDu);
        final Iterator<DcsEntity> entityIterator = dus.iterator();

        when(mockConnector.search(DATAITEM_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET))
                .thenReturn(new CountableIterator<DcsEntity>() {
                    @Override
                    public long count() {
                        return dus.size();
                    }

                    @Override
                    public boolean hasNext() {
                        return entityIterator.hasNext();
                    }

                    @Override
                    public DcsEntity next() {
                        return entityIterator.next();
                    }

                    @Override
                    public void remove() {
                        entityIterator.remove();
                    }
                });

        BusinessObjectState state = underTest.findLatestState(DATAITEM_BIZ_ID);

        verify(mockConnector).search(DATAITEM_SEARCH_QUERY, MAX_RESULTS, RESULT_OFFSET);
        
        assertNotNull(state);
        assertEquals(expectedRootDu, state.getRoot());
        assertEquals(expectedStateDuV2, state.getLatestState());

    }

}
