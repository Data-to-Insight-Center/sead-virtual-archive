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

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;

import java.util.ArrayList;
import java.util.Collection;

import static org.dataconservancy.ui.dcpmap.AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Convenience class for making assertions about the contents of DCPs.
 */
public class MapperAssertion {

    private static final String NULL_DCP = "Supplied DCP must not be null!";

    /**
     * Asserts that there are the required number of state deliverable units (specified by {@code count}) in the
     * supplied DCP.
     * <p/>
     * Behavior for values of {@code count}:
     * <dl>
     * <dt>when count == 0</dt>
     * <dd>asserts that there are zero state deliverable units in the DCP</dd>
     * <dt>when count > 0</dt>
     * <dd>asserts that there are {@code count} state deliverable units in the DCP</dd>
     * <dt>when count < 0</dt>
     * <dd>asserts that at least one state deliverable unit was found in the DCP</dd>
     * </dl>
     *
     * @param dcp           the DCP optionally containing state Deliverable Units
     * @param expectedCount the expected number of state Deliverable Units in the DCP; may be less than zero, zero, or
     *                      greater than zero
     * @param type          The type of the state du to be returned.                     
     * @return a Collection containing the state Deliverable Units; may be empty but never null
     */
    public static Collection<DcsDeliverableUnit> assertStateDuPresent(Dcp dcp, int expectedCount, String type) {
        assertNotNull(NULL_DCP, dcp);

        boolean found = false;
        int actualCount = 0;
        ArrayList<DcsDeliverableUnit> stateDus = new ArrayList<DcsDeliverableUnit>();

        for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
            if (du.getType().equals(type)) {
                found = true;
                actualCount++;
                stateDus.add(du);
            }
        }

        if (expectedCount == 0) {
            assertFalse("Did not expect a state deliverable unit to be found in the DCP!", found);
        }

        assertTrue("Expected a state deliverable unit to be in the DCP!", found);

        if (expectedCount > 0) {
            assertEquals("Expected " + expectedCount + " state deliverable unit(s), but found " + actualCount,
                    expectedCount, actualCount);
        }

        return stateDus;
    }

    /**
     * Asserts that there is exactly one root deliverable unit in the supplied DCP.
     *
     * @param dcp the DCP optionally containing root Deliverable Units
     * @return the root DeliverableUnit, if found
     */
    public static DcsDeliverableUnit assertRootDuPresent(Dcp dcp) {
        assertNotNull(NULL_DCP, dcp);
        boolean found = false;
        int count = 0;
        DcsDeliverableUnit rootDu = null;

        for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
            if (du.getType().equals(ROOT_DELIVERABLE_UNIT_TYPE)) {
                found = true;
                rootDu = du;
                count++;
            }
        }

        assertTrue("Expected a root deliverable unit to be in the DCP!", found);
        assertEquals("Expected 1 root deliverable unit to be in the DCP, but found " + count, 1, count);
        return rootDu;
    }

    /**
     * Asserts that the supplied {@code ref} is a {@code formerExternalReference} of the supplied Deliverable Unit.
     *
     * @param du  the DeliverableUnit that must contain {@code ref} as a formerExternalRef
     * @param ref the reference
     */
    public static void assertFormerRef(DcsDeliverableUnit du, String ref) {
        assertNotNull("Supplied DU was null!", du);
        assertNotNull("Supplied reference was null!", ref);
        assertFalse("Supplied reference was empty!", ref.trim().length() == 0);

        assertTrue("Expected DU " + du.getId() + " to contain a formerExternalRef " + ref,
                du.getFormerExternalRefs().contains(ref));
    }

    /**
     * Asserts that the supplied DCP contains exactly one Manifestation (of any type) for the identified Deliverable
     * Unit.
     *
     * @param duId the DeliverableUnit identifier that must be referenced by exactly one Manifestation
     * @param dcp  the DCP containing the sought-after Manifestation
     * @return the DcsManifestation from the supplied DCP, if it exists
     */
    public static DcsManifestation assertHasSingleManifestation(String duId, Dcp dcp) {
        return assertHasManifestation(duId, dcp, 1).iterator().next();
    }

    /**
     * Asserts that the supplied DCP contains exactly one Manifestation of the specified type, for the identified
     * Deliverable Unit.
     *
     * @param duId the DeliverableUnit identifier that must be referenced by exactly one Manifestation
     * @param type the type of the DcsManifestation
     * @param dcp  the DCP optionally containing the sought-after Manifestation
     * @return the DcsManifestation from the supplied DCP, if it exists
     */
    public static DcsManifestation assertHasSingleManifestation(String duId, String type, Dcp dcp) {
        return assertHasManifestation(duId, dcp, type, 1).iterator().next();
    }

    /**
     * Asserts that the supplied DCP contains {@code count} Manifestation(s) of any type, for the identified
     * Deliverable Unit.
     * <p/>
     * Behavior for values of {@code count}:
     * <dl>
     * <dt>when count == 0</dt>
     * <dd>asserts that there are zero manifestations in the DCP for the identified DU</dd>
     * <dt>when count > 0</dt>
     * <dd>asserts that there are {@code count} manifestations in the DCP for the identified DU</dd>
     * <dt>when count < 0</dt>
     * <dd>asserts that at least one manifestation was found in the DCP for the identified DU</dd>
     * </dl>
     *
     * @param duId the DeliverableUnit identifier that must be referenced by {@code count} Manifestations
     * @param dcp  the DCP optionally containing the sought-after Manifestation
     * @return the DcsManifestation from the supplied DCP, if it exists
     */
    public static Collection<DcsManifestation> assertHasManifestation(String duId, Dcp dcp, int expectedCount) {
        return assertHasManifestation(duId, dcp, null, expectedCount);
    }

    /**
     * Asserts that the supplied DCP contains {@code count} Manifestation(s) of the specified {@code type}, for the
     * identified Deliverable Unit.
     * <p/>
     * Behavior for values of {@code count}:
     * <dl>
     * <dt>when count == 0</dt>
     * <dd>asserts that there are zero manifestations of {@code type} in the DCP for the identified DU</dd>
     * <dt>when count > 0</dt>
     * <dd>asserts that there are {@code count} manifestations of {@code type} in the DCP for the identified DU</dd>
     * <dt>when count < 0</dt>
     * <dd>asserts that at least one manifestation of {@code type} was found in the DCP for the identified DU</dd>
     * </dl>
     *
     * @param duId the DeliverableUnit identifier that must be referenced by {@code count} Manifestations
     * @param dcp  the DCP optionally containing the sought-after Manifestation
     * @param type the type of the DcsManifestation
     * @return the DcsManifestation from the supplied DCP, if it exists
     */
    public static Collection<DcsManifestation> assertHasManifestation(String duId, Dcp dcp, String type, int expectedCount) {
        boolean found = false;
        int actualCount = 0;
        ArrayList<DcsManifestation> manifestations = new ArrayList<DcsManifestation>();

        for (DcsManifestation man : dcp.getManifestations()) {
            if (man.getDeliverableUnit().equals(duId)) {
                if (type != null) {
                    if (man.getType().equals(type)) {
                        manifestations.add(man);
                        actualCount++;
                        found = true;
                    }
                } else {
                    manifestations.add(man);
                    actualCount++;
                    found = true;
                }
            }
        }

        if (expectedCount == 0) {
            assertFalse("Did not expect a manifestation to be found in the DCP for DU id " + duId + "!", found);
        }

        assertTrue("Expected a manifestation to be found in the DCP for DU id " + duId + "!", found);

        if (expectedCount > 0) {
            assertEquals("Expected " + expectedCount + " manifestation(s) for DU id " + duId + ", but found " +
                    actualCount, expectedCount, actualCount);
        }

        return manifestations;
    }

    /**
     * Asserts that the supplied DCP contains exactly one DcsFile with {@code fileId}.
     *
     * @param fileId the identifier of the DcsFile to find
     * @param dcp    the DCP optionally containing a DcsFile identified by {@code fileId}
     * @return the DcsFile from the DCP, if found
     */
    public static DcsFile assertHasFile(String fileId, Dcp dcp) {
        boolean found = false;
        DcsFile file = null;
        int count = 0;

        for (DcsFile f : dcp.getFiles()) {
            if (f.getId().equals(fileId)) {
                file = f;
                found = true;
                count++;
            }
        }

        assertTrue("Expected a single file (id " + fileId + ") to be found in the DCP!", found);
        assertEquals("Expected a single file (id " + fileId + ") to be found in the DCP, but found " + count, 1, count);
        return file;
    }

    /**
     * Asserts that the supplied DCP does <em>not</em> contain a DcsFile with {@code fileId}.
     *
     * @param fileId the identifier of the DcsFile to find
     * @param dcp the DCP optionally containing a DcsFile identified by {@code fileId}
     */
    public static void assertDoesNotHaveFile(String fileId, Dcp dcp) {
        boolean found = false;

        for (DcsFile f : dcp.getFiles()) {
            if (f.getId().equals(fileId)) {
                found = true;
            }
        }

        assertFalse("Expected no file '" + fileId + "' to be found in the DCP!", found);
    }

    /**
     * Asserts that the supplied deliverable unit has a successor {@code successorRef}
     *
     * @param du           the deliverable unit optionally containing a successor relationship pointing to {@code successorRef}
     * @param successorRef the reference to the successor of {@code du}
     */
    public static void assertHasSuccessor(DcsDeliverableUnit du, String successorRef) {
        boolean found = false;
        String val = null;
        int count = 0;
        for (DcsRelation rel : du.getRelations()) {
            if (rel.getRelUri().equals(DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                found = true;
                val = rel.getRef().getRef();
                count++;
            }
        }

        assertTrue("Expected to find an " + DcsRelationship.IS_SUCCESSOR_OF.asString() + " relationship in the " +
                "DU '" + du.getId() + "'!", found);
        assertEquals("Expected to find only 1 " + DcsRelationship.IS_SUCCESSOR_OF.asString() + " relationship in the " +
                "DU '" + du.getId() + "', found " + count, 1, count);
        assertEquals("Expected the successor reference of DU '" + du.getId() + "' to be '" + successorRef +
                "', but was '" + val + "'", successorRef, val);
    }

    /**
     * Asserts that the supplied deliverable unit has a parent reference {@code parentRef}
     *
     * @param du        the deliverable unit optionally containing a parent reference
     * @param parentRef the reference to the parent of {@code du}
     */
    public static void assertHasParentRef(DcsDeliverableUnit du, String parentRef) {
        assertTrue("Expected DU with id '" + du.getId() + "' to have a parent reference to " + parentRef,
                du.getParents().contains(new DcsDeliverableUnitRef(parentRef)));
    }

    /**
     * Asserts that there is exactly one state deliverable unit in the supplied DCP.
     *
     * @param dcp the DCP optionally containing state deliverable units
     * @param type The type of the state du to be found
     * @return the state deliverable unit, if found
     */
    public static DcsDeliverableUnit assertSingleStateDuPresent(Dcp dcp, String type) {
        return assertStateDuPresent(dcp, 1, type).iterator().next();
    }

}
