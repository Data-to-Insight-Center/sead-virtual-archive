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
package org.dataconservancy.ui.dcpmap;

import static org.dataconservancy.ui.util.MappingUtil.getStateDuFromCollectionDcp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.ui.DirtiesContextBaseUnitTest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.profile.CollectionProfile;
import org.dataconservancy.ui.services.ArchiveUtil;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.CollectionBusinessObjectSearcher;
import org.dataconservancy.ui.services.MetadataFileBusinessObjectSearcher;
import org.dataconservancy.ui.services.ParentSearcher;
import org.dataconservancy.ui.services.RelationshipService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class CollectionMapperTest extends DirtiesContextBaseUnitTest {

    @Autowired
    private RelationshipService relationshipService;

    @Test
    public void testMapping() throws Exception {

        MetadataFile mf = new MetadataFile();
        mf.setFormat("jpg");
        mf.setName("banana.jpg");
        mf.setId("mf:1");
        File tmp = File.createTempFile("image", ".jpg");
        tmp.deleteOnExit();

        FileWriter out = new FileWriter(tmp);
        out.write("stuff");
        out.close();

        mf.setSource(tmp.toURI().toURL().toExternalForm());
        for(String metadataFileId : relationshipService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId())){
            relationshipService.removeMetadataFileFromBusinessObject(metadataFileId, collectionWithData.getId());
        }
        relationshipService.addMetadataFileToBusinessObject(mf.getId(), collectionWithData.getId());

        MetadataFileBusinessObjectSearcher searcher = mock(MetadataFileBusinessObjectSearcher.class);

             
        CollectionMapper mapper = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), searcher, mock(ParentSearcher.class), null);
        when(searcher.findLatestState(mf.getId())).thenReturn(new BusinessObjectState() {
            @Override
            public DcsDeliverableUnit getRoot() {
                DcsDeliverableUnit rootDu = new DcsDeliverableUnit();
                rootDu.setId("id:rootDu");
                return rootDu;
            }

            @Override
            public DcsDeliverableUnit getLatestState() {
                DcsDeliverableUnit stateDu = new DcsDeliverableUnit();
                stateDu.setId("id:stateDu");
                return stateDu;
            }
        });
        
        DcsFile metadata_file = new DcsFile();
        metadata_file.setId("id:dcsfile");
        when(searcher.findMetadataFile(mf.getId())).thenReturn(metadata_file);
        
        Dcp dcp = mapper.toDcp(null, collectionWithData);

        assertNotNull(dcp);

        DcsDeliverableUnit du = getStateDuFromCollectionDcp(dcp, CollectionProfile.STATE_DU_TYPE);
        assertNotNull(du);

        assertTrue(du.getAlternateIds().size() > 0);

        assertTrue(dcp.getManifestations().size() > 0);
        assertTrue(dcp.getFiles().size() > 0);

        Collection test = mapper.fromDcp(dcp);

        assertNotNull(test);

        assertEquals(collectionWithData, test);
    }

    /**
     * Insures that no exceptions are thrown when providing an empty alternate id for a collection.  In addition,
     * it insures that an empty alternate id is not set on the mapped DCP.
     *
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithEmptyAlternateIds() throws Exception {
        collectionWithData.setAlternateIds(Arrays.asList(" "));

        CollectionMapper underTest = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp mappedDcp = underTest.toDcp(null, collectionWithData);

        assertNotNull(mappedDcp);

        for (DcsDeliverableUnit du: mappedDcp.getDeliverableUnits()) {
            assertTrue(du.getAlternateIds().isEmpty());
        }
    }

    /**
     * Insures that no exceptions are thrown when providing an empty alternate id for a collection.  In addition,
     * it insures that a second, valid, alternate id provided at the same time is mapped properly.
     *
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithEmptyAndNonEmptyAlternateIds() throws Exception {
        final String alternateId = "http://foo.bar.com/alternate/id";
        collectionWithData.setAlternateIds(Arrays.asList(" ", alternateId));

        CollectionMapper underTest = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp mappedDcp = underTest.toDcp(null, collectionWithData);

        assertNotNull(mappedDcp);
        
        for (DcsDeliverableUnit du: mappedDcp.getDeliverableUnits()) {
            if (!du.getAlternateIds().isEmpty()) {
                assertEquals(1, du.getAlternateIds().size());
                assertEquals(alternateId, du.getAlternateIds().iterator().next().getIdValue());
            }
        }
    }

    /**
     * Insures that no exceptions are thrown when providing a zero-length alternate id for a collection.  In addition,
     * it insures that a zero-length alternate id is not set on the mapped DCP.
     *
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithZeroLengthAlternateIds() throws Exception {
        collectionWithData.setAlternateIds(Arrays.asList(""));

        CollectionMapper underTest = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp mappedDcp = underTest.toDcp(null, collectionWithData);

        assertNotNull(mappedDcp);
        
        for (DcsDeliverableUnit du: mappedDcp.getDeliverableUnits()) {
            assertTrue(du.getAlternateIds().isEmpty());
        }
    }
    

    /**
     * Insures that no exceptions are thrown when providing a null alternate id for a collection.  In addition,
     * it insures that a null alternate id is not set on the mapped DCP.
     *
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithNullAlternateIds() throws Exception {
        collectionWithData.setAlternateIds(Arrays.asList((String)null));

        CollectionMapper underTest = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp mappedDcp = underTest.toDcp(null, collectionWithData);

        assertNotNull(mappedDcp);
        
        for (DcsDeliverableUnit du: mappedDcp.getDeliverableUnits()) {
            assertTrue(du.getAlternateIds().isEmpty());
        }
    }

    /**
     * Insures that no exceptions are thrown when providing a null alternate id List for a collection.  In addition,
     * it insures that a null alternate id is not set on the mapped DCP.
     *
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithNullAlternateIdsList() throws Exception {
        collectionWithData.setAlternateIds(null);

        CollectionMapper underTest = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp mappedDcp = underTest.toDcp(null, collectionWithData);

        assertNotNull(mappedDcp);
        
        for (DcsDeliverableUnit du: mappedDcp.getDeliverableUnits()) {
            assertTrue(du.getAlternateIds().isEmpty());
        }
    }

    /**
     * Insures that no exceptions are thrown when providing a empty alternate id list for a collection.  In addition,
     * it insures that an alternate id is not set on the mapped DCP.
     *
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithEmpytAlternateIdsList() throws Exception {
        collectionWithData.setAlternateIds(new ArrayList<String>());

        CollectionMapper underTest = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp mappedDcp = underTest.toDcp(null, collectionWithData);

        assertNotNull(mappedDcp);
        
        for (DcsDeliverableUnit du: mappedDcp.getDeliverableUnits()) {
            assertTrue(du.getAlternateIds().isEmpty());
        }
    }
    
    /**
     * Make sure a collection with a parent set has a state du pointing to the root du of the parent
     * Business object.
     * 
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithParent() throws Exception {
        final DcsDeliverableUnit parent_du = new DcsDeliverableUnit();
        parent_du.setId("dcs:parent)");
        parent_du.addFormerExternalRef("col:parent");
        
        collectionNoData.setParentId("col:parent");

        CollectionBusinessObjectSearcher colSearcher = mock(CollectionBusinessObjectSearcher.class);

        BusinessObjectState col_state = new BusinessObjectState() {
            public DcsDeliverableUnit getRoot() {
                return parent_du;
            }

            public DcsDeliverableUnit getLatestState() {
                return null;
            }
        };
        
        when(colSearcher.findLatestState("col:parent")).thenReturn(col_state);
        
        CollectionMapper underTest = new CollectionMapper(colSearcher, mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp mappedDcp = underTest.toDcp(null, collectionNoData);
        
        assertNotNull(mappedDcp);
        
        boolean tested_parent_id = false;
        
        for (DcsDeliverableUnit du : mappedDcp.getDeliverableUnits()) {
            if (du.getType().equals(CollectionProfile.STATE_DU_TYPE)
                    && du.getFormerExternalRefs().contains(collectionNoData.getId())) {
                assertTrue(du.getParents().contains(
                        new DcsDeliverableUnitRef(parent_du.getId())));
                tested_parent_id = true;
            }
        }
        
        assertTrue(tested_parent_id);
    }
    
    /**
     * Make sure that children of a collection are found when mapping back from the archive.
     * 
     * @throws Exception
     */
    @Test
    public void testMapCollectionWithParentFromArchive() throws Exception {
        Collection parent = collectionWithData;
        Collection child = collectionNoData;

        child.setParentId(parent.getId());
        
        // Map parent to dcp

        CollectionMapper parent_mapper = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp parent_dcp = parent_mapper.toDcp(null, parent);

        assertNotNull(parent_dcp);
        
        DcsDeliverableUnit parent_root_du = null;
        
        for (DcsDeliverableUnit du: parent_dcp.getDeliverableUnits()) {
            if (du.getType().equals(AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE)) {
                parent_root_du = du;
            }
        }
        
        assertNotNull(parent_root_du);

        // Map child to dcp 
        
        
        CollectionBusinessObjectSearcher colSearcher = mock(CollectionBusinessObjectSearcher.class);

        final DcsDeliverableUnit object_state_root = parent_root_du;
        
        BusinessObjectState col_state = new BusinessObjectState() {
            public DcsDeliverableUnit getRoot() {
                return object_state_root;
            }

            public DcsDeliverableUnit getLatestState() {
                return null;
            }
        };
                
        when(colSearcher.findLatestState(parent.getId())).thenReturn(col_state);
        
        CollectionMapper child_mapper = new CollectionMapper(colSearcher, mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), null);
        Dcp child_dcp = child_mapper.toDcp(null, child);
        assertNotNull(child_dcp);
        
        DcsDeliverableUnit child_state_du = null;
        
        for (DcsDeliverableUnit du: child_dcp.getDeliverableUnits()) {
            if (du.getType().equals(CollectionProfile.STATE_DU_TYPE)) {
                child_state_du = du;
            }
        }
        
        assertNotNull(child_state_du);
        
        // Map parent back from dcp 
        
        ParentSearcher parentSearcher = mock(ParentSearcher.class);

        java.util.Collection<DcsDeliverableUnit> child_dus = new ArrayList<DcsDeliverableUnit>();
        child_dus.add(child_state_du);
        
        when(parentSearcher.getParentsOf(anyString(), eq(DcsDeliverableUnit.class))).thenReturn(child_dus);
        
        CollectionBusinessObjectSearcher boSearcher = mock(CollectionBusinessObjectSearcher.class);
        
        final DcsDeliverableUnit final_child_state_du = child_state_du;
        
        BusinessObjectState child_state = new BusinessObjectState() {
            public DcsDeliverableUnit getRoot() {
                return null;
            }

            public DcsDeliverableUnit getLatestState() {
                return final_child_state_du;
            }
        };
        
        when(boSearcher.findLatestState(child.getId())).thenReturn(child_state);
        
        CollectionMapper underTest = new CollectionMapper(boSearcher, mock(MetadataFileBusinessObjectSearcher.class), parentSearcher, null);
        Collection parent_test = underTest.fromDcp(parent_dcp);
        
        parent.getChildrenIds().add(child.getId());
        assertEquals(parent, parent_test);
        
        // Map child back
        
        ArchiveUtil archive = mock(ArchiveUtil.class);
        
        when(archive.getEntity(parent_root_du.getId())).thenReturn(parent_root_du);
        
        underTest = new CollectionMapper(mock(CollectionBusinessObjectSearcher.class), mock(MetadataFileBusinessObjectSearcher.class), mock(ParentSearcher.class), archive);
        Collection child_test = underTest.fromDcp(child_dcp);
        
        assertEquals(child, child_test);
    }
}
