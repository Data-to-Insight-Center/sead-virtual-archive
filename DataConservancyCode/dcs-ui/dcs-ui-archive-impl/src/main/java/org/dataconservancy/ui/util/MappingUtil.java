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
package org.dataconservancy.ui.util;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.profile.support.CollectionMatchStrategy;
import org.dataconservancy.registry.api.support.BasicRegistryEntryMapper;
import org.dataconservancy.ui.dcpmap.AbstractVersioningMapper;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.profile.DataFileProfile;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.services.BusinessObjectSearcher;
import org.dataconservancy.ui.services.BusinessObjectState;

import static org.dataconservancy.ui.profile.DataItemProfile.DATASET_STATE_TYPE;
import static org.dataconservancy.ui.profile.DataItemProfile.DATASET_TYPE;
import static org.dataconservancy.ui.profile.DataItemProfile.DATA_SET_METADATA_TECH_ENV;

/**
 * Utility methods for supporting DCP to Business Object mapping.
 */
public class MappingUtil {

    /**
     * This is the value of the &lt;DeliverableUnit/&gt; &lt;type/&gt; element for Deliverable Units representing
     * the root of a {@link Collection}.
     *
     * @see BusinessObjectState#getRoot()
     */
    public final static String COLLECTION_ROOT_DU_TYPE = AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE;
    
    /**
     * This is the value of the &lt;DeliverableUnit/&gt; &lt;type/&gt; element for Deliverable Units representing
     * the root of a {@link MetadataFile}.
     *
     * @see BusinessObjectState#getRoot()
     */
    public final static String METADATA_FILE_ROOT_DU_TYPE = AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE;

    /**
     * This is the value of the &lt;DeliverableUnit/&gt; &lt;type/&gt; element for Deliverable Units represeenting
     * a {@link RegistryEntry}.
     */
    public final static String REGISTRY_ENTRY_DU_TYPE = BasicRegistryEntryMapper.REGISTRY_ENTRY_DU_TYPE;

    /**
     * Returns the first DU from the DCP that has a type of type, otherwise null.  The DCP
     * should represent the archival mapping of a DC UI {@link Collection}.
     *
     * @param dcp the dcp
     * @return the DU, or null
     */
    public static DcsDeliverableUnit getStateDuFromCollectionDcp(Dcp dcp, String type) {
        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(type)) {
                return test;
            }
        }

        return null;
    }

    /**
     * Returns the first DU from the DCP that has a type of {@link #COLLECTION_ROOT_DU_TYPE}, otherwise null.  The DCP should
     * represent the archival mapping of a DC UI {@link Collection}.
     *
     * @param dcp the dcp
     * @return the DU, or null
     */
    public static DcsDeliverableUnit getRootDuFromCollectionDcp(Dcp dcp) {
        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(COLLECTION_ROOT_DU_TYPE)) {
                return test;
            }
        }

        return null;
    }

    /**
     * Returns the first DU from the DCP that has a type of {@link DataItemProfile#DATASET_STATE_TYPE}, otherwise null.
     * The DCP should represent the archival mapping of a DC UI {@link DataItem}.
     *
     * @param dcp the dcp
     * @return the DU, or null
     */
    public static DcsDeliverableUnit getStateDuFromDataItemDcp(Dcp dcp) {

        DcsManifestation dataItemMan = null;

        // Obtain the manifestation that contains the DataItem metadata, and the DataItem file metadata

        for (DcsManifestation test : dcp.getManifestations()) {
            if (test.getTechnicalEnvironment() != null) {
                if (DATA_SET_METADATA_TECH_ENV.evaluate(test.getTechnicalEnvironment(), CollectionMatchStrategy.AT_LEAST_ONE)) {
                    dataItemMan = test;
                    break;
                }
            }
        }

        if (dataItemMan == null) {
            return null;
        }

        // Obtain the DcsDeliverableUnit that the DataItem manifestation points to
        // We perform an extra sanity check in the conditional, making sure that
        // the type is what we expect.

        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(DATASET_STATE_TYPE) && dataItemMan.getDeliverableUnit().equals(test.getId())) {
                return test;
            }
        }

        return null;
    }

    public static DcsDeliverableUnit getStateDuFromDataFileDcp(Dcp dcp) {
        for (DcsDeliverableUnit candidate : dcp.getDeliverableUnits()) {
            if (DataFileProfile.DATAFILE_STATEDU_TYPE.equals(candidate.getType())) {
                return candidate;
            }
        }

        return null;
    }
    
    /**
     * Returns the first DU from the DCP that has a type of {@link DataItemProfile#DATASET_TYPE}, otherwise null.  The
     * DCP should represent the archival mapping of a DC UI {@link Collection}.
     *
     * @param dcp the dcp
     * @return the DU, or null
     */
    public static DcsDeliverableUnit getRootDuFromDataItemDcp(Dcp dcp) {
        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(DATASET_TYPE)) {
                return test;
            }
        }

        return null;
    }

    /**
     * Returns the first DU from the DCP that has a type of {@link #METADATA_FILE_ROOT_DU_TYPE}, otherwise null.  The DCP should
     * represent the archival mapping of a DC UI {@link MetadataFile}.
     *
     * @param dcp the dcp
     * @return the DU, or null
     */
    public static DcsDeliverableUnit getRootDuFromMetadataFileDcp(Dcp dcp) {
        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(METADATA_FILE_ROOT_DU_TYPE)) {
                return test;
            }
        }

        return null;
    }

    /**
     * Returns the first DU from the DCP that has a type of {@link #METADATA_FILE_STATE_DU_TYPE}, otherwise null.  The DCP
     * should represent the archival mapping of a DC UI {@link MetadataFile}.
     *
     * @param dcp the dcp
     * @return the DU, or null
     */
    public static DcsDeliverableUnit getStateDuFromMetadataFileDcp(Dcp dcp, String type) {
        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(type)) {
                return test;
            }
        }

        return null;
    }
    
    /**
     * Returns the registry entry du from the DCP that has a type of {@link #REGISTRY_ENTRY_DU_TYPE}, otherwise null. The DCP
     * should represent the archive mapping of a {@link RegistryEntry}.
     * 
     * @param dcp the dcp
     * @return the DU, or null if not found
     */
    public static DcsDeliverableUnit getRegistryEntryDuFromDcp(Dcp dcp) {
        for (DcsDeliverableUnit test : dcp.getDeliverableUnits()) {
            if (test.getType().equals(REGISTRY_ENTRY_DU_TYPE)) {
                return test;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the identifier of the predecessor of the supplied {@code du}, or null if the DU has no predecessors.
     *
     * @param du the deliverable unit
     * @return the identifier of the predecessor, or null
     */
    public static String getPredecessorId(DcsDeliverableUnit du) {
        for (DcsRelation rel : du.getRelations()) {
            if (rel.getRelUri().equals(
                    DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                if (rel.getRef() == null || rel.getRef().getRef() == null) {
                    return null;
                }

                return rel.getRef().getRef();
            }
        }

        return null;
    }

    /**
     * Finds the first DcsFile in the supplied DCP that has a {@link DcsFile#getName() name} matching {@code name};
     * otherwise {@code null} is returned.
     *
     * @param dcp the DCP
     * @param name the name of the file to retrieve
     * @return the first matching DcsFile, or {@code null}
     */
    public static DcsFile findDcsFile(Dcp dcp, String name) {
        for (DcsFile f : dcp.getFiles()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }

        return null;
    }

    /**
     * Asserts that {@code dcp2} contains a DU that is a successor of a DU contained in {@code dcp1}.
     *
     * @param dcp2
     * @param dcp1
     */
    public static void assertIsSuccessor(Dcp dcp2, Dcp dcp1, String type) {
        if (!(dcp1.getDeliverableUnits().size() > 0)) {
            throw new RuntimeException("Expected 'dcp1' to have at least one DU.");
        }

        DcsDeliverableUnit state_du1 = getStateDuFromCollectionDcp(dcp1, type);

        if (state_du1 == null) {
            throw new RuntimeException("Expected 'dcp1' to contain a State DU.");
        }

        //  The update doesn't have the root DU
        if (!(dcp2.getDeliverableUnits().size() > 0)) {
            throw new RuntimeException("Expected 'dcp2' to have at least one DU.");
        }

        DcsDeliverableUnit state_du2 = getStateDuFromCollectionDcp(dcp2, type);

        if (state_du2 == null) {
            throw new RuntimeException("Expected 'dcp2' to contain a State DU.");
        }

        String pred_id = getPredecessorId(state_du2);

        if (pred_id == null) {
            throw new RuntimeException("Expected the State DU in 'dcp2' to contain a predecessor identifier.");
        }

        if (!(state_du1.getId().equals(pred_id))) {
            throw new RuntimeException("Expected the State DU in 'dcp2' to reference the State DU in 'dcp1'");
        }
    }

    /**
     * Returns true if the supplied {@code dcp} contains a Manifestation File that references {@code target}.  Returns
     * false otherwise.
     *
     * @param dcp the dcp
     * @param target the target of a manifestation file reference
     * @return true if the dcp contains a manifestation file referencing target.
     */
    public static boolean hasManifestationFile(Dcp dcp, String target) {
        for (DcsManifestation man : dcp.getManifestations()) {
            for (DcsManifestationFile mf : man.getManifestationFiles()) {
                if (mf.getRef().getRef().equals(target)) {
                    return true;
                }
            }
        }

        return false;
    }

}
