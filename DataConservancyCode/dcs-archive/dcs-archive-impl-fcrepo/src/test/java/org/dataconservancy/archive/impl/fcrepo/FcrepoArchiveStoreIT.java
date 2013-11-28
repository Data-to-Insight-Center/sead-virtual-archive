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
package org.dataconservancy.archive.impl.fcrepo;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class FcrepoArchiveStoreIT extends AbstractFcrepoIntegrationTest {


    @Test
    public void listEmpty() {
        Set<String> set = toSet(store.listEntities(null));
        assertEquals("Store should start empty", 0, set.size());
    }

    @Test(expected=EntityNotFoundException.class)
    public void getPackageNoSuchEntity() throws EntityNotFoundException {
        store.getPackage("no-such-entity");
    }

    @Test(expected=EntityNotFoundException.class)
    public void getContentNoSuchEntity() throws EntityNotFoundException,
                                                EntityTypeException {
        store.getContent("no-such-entity");
    }

    @Test(expected=EntityTypeException.class)
    public void getContentWrongEntityType() throws EntityNotFoundException,
                                                   EntityTypeException,
                                                   AIPFormatException {
        Dcp dcp = new Dcp();
        DcsCollection entity = createTestCollection(1);
        dcp.addCollection(entity);
        try {
            store.putPackage(getInputStream(dcp));
            store.getContent(entity.getId());
        } finally {
            deleteAllEntities();
        }
    }

    @Test
    public void getContentOfExistingFileEntity() throws EntityNotFoundException,
                                                        EntityTypeException,
                                                        AIPFormatException,
                                                        IOException {
        Dcp dcp = new Dcp();
        DcsFile entity = createTestFile(1);
        String sourceUrl = fedoraUrl + "/describe";
        entity.setSource(sourceUrl);
        dcp.addFile(entity);
        try {
            store.putPackage(getInputStream(dcp));
            String responseString = IOUtils.toString(
                    store.getContent(entity.getId()));
            assertTrue("Unexpected getContent response; expected it to contain "
                    + "the substring 'Fedora Repository' (source was "
                    + sourceUrl + "), but the response was:\n\n"
                    + responseString,
                    responseString.indexOf("Fedora Repository") != -1);
        } finally {
            deleteAllEntities();
        }
    }

    @Test
    public void putListGetCollectionOnly() {
        Dcp dcp = new Dcp();
        dcp.addCollection(createTestCollection(1));
        doPutListGetTests(dcp);
    }

    @Test
    public void putListGetFileWithEvent() {
        Dcp dcp = new Dcp();
        DcsFile file = createTestFile(1);
        dcp.addFile(file);
        dcp.addEvent(createTestEvent(1, file));
        doPutListGetTests(dcp);
    }

    @Test
    public void putListGetManifestationWithDU() {
        Dcp dcp = new Dcp();
        DcsDeliverableUnit du = createTestDeliverableUnit(1);
        dcp.addDeliverableUnit(du);
        dcp.addManifestation(createTestManifestation(1, du));
        doPutListGetTests(dcp);
    }

    // Assuming the store is empty, serialize and call putPackage with
    // the given DCP, then make sure listEntities and getPackage behave
    // as expected for all entities.
    private void doPutListGetTests(Dcp dcp) {
        try {
            store.putPackage(getInputStream(dcp));
            doListTests(getIdMap(dcp));
            doGetTests(getEntityMap(dcp));
        } catch (EntityNotFoundException e) {
            fail("getPackage(" + e.getEntityId() + ") threw EntityNotFoundException");
        } catch (InvalidXmlException e) {
            throw new RuntimeException("Programmer error: Unable to parse DCP "
                    + "stream provided by Fedora", e);
        } catch (AIPFormatException e) {
            throw new RuntimeException("Programmer error: Unable to parse DCP "
                    + "stream provided by DcsXstreamStaxModelBuilder", e);
        } finally {
            // clean up for next test
            deleteAllEntities();
        }
    }

    //
    private void doGetTests(Map<EntityType, Collection<? extends DcsEntity>> entityMap)
            throws EntityNotFoundException, InvalidXmlException {
        for (EntityType expectedType: entityMap.keySet()) {
            for (DcsEntity expectedEntity: entityMap.get(expectedType)) {
                try {
                    doGetTest(expectedType, expectedEntity);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // Make sure getPackage succeeds on the given entity, then deserialize the
    // stream and make sure it contains exactly+only the expected entity.
    private void doGetTest(EntityType expectedType, DcsEntity expectedEntity)
            throws IOException,
                   EntityNotFoundException,
                   InvalidXmlException {
        InputStream fedoraDcpStream = store.getPackage(expectedEntity.getId());
        String fedoraDcpString = IOUtils.toString(fedoraDcpStream, "UTF-8");
        String expectedDcpString = IOUtils.toString(
                getInputStream(getDcp(expectedType, expectedEntity)), "UTF-8");
        String failMsg = "Incorrect " + expectedType + " DCP XML from Fedora.\n\n"
                           + "Expected something like this:\n\n"
                           + expectedDcpString + "\n\n"
                           + "...but got this instead:\n"
                           + fedoraDcpString;
        Dcp dcpFromFedora = builder.buildSip(
                IOUtils.toInputStream(fedoraDcpString, "UTF-8"));
        checkDcpFromFedora(failMsg, dcpFromFedora, expectedType,
                expectedEntity);
    }

    // check that the given dcp from Fedora contains exactly the expected entity
    private void checkDcpFromFedora(String failMsg,
                                    Dcp dcp,
                                    EntityType type,
                                    DcsEntity entity) {
        Set<? extends DcsEntity> set;
        switch (type) {
            case COLLECTION:
                assertTrue(failMsg, dcp.getCollections().contains(entity));
                assertEquals(failMsg, 1, dcp.getCollections().size());
                assertEquals(failMsg, 0, dcp.getDeliverableUnits().size());
                assertEquals(failMsg, 0, dcp.getEvents().size());
                assertEquals(failMsg, 0, dcp.getFiles().size());
                assertEquals(failMsg, 0, dcp.getManifestations().size());
                break;
            case DELIVERABLE_UNIT:
                assertTrue(failMsg, dcp.getDeliverableUnits().contains(entity));
                assertEquals(failMsg, 0, dcp.getCollections().size());
                assertEquals(failMsg, 1, dcp.getDeliverableUnits().size());
                assertEquals(failMsg, 0, dcp.getEvents().size());
                assertEquals(failMsg, 0, dcp.getFiles().size());
                assertEquals(failMsg, 0, dcp.getManifestations().size());
                break;
            case EVENT:
                assertTrue(dcp.getEvents().contains(entity));
                assertEquals(failMsg, 0, dcp.getCollections().size());
                assertEquals(failMsg, 0, dcp.getDeliverableUnits().size());
                assertEquals(failMsg, 1, dcp.getEvents().size());
                assertEquals(failMsg, 0, dcp.getFiles().size());
                assertEquals(failMsg, 0, dcp.getManifestations().size());
                break;
            case FILE:
                assertTrue(failMsg, dcp.getFiles().contains(entity));
                assertEquals(failMsg, 0, dcp.getCollections().size());
                assertEquals(failMsg, 0, dcp.getDeliverableUnits().size());
                assertEquals(failMsg, 0, dcp.getEvents().size());
                assertEquals(failMsg, 1, dcp.getFiles().size());
                assertEquals(failMsg, 0, dcp.getManifestations().size());
                break;
            case MANIFESTATION:
                assertTrue(failMsg, dcp.getManifestations().contains(entity));
                assertEquals(failMsg, 0, dcp.getCollections().size());
                assertEquals(failMsg, 0, dcp.getDeliverableUnits().size());
                assertEquals(failMsg, 0, dcp.getEvents().size());
                assertEquals(failMsg, 0, dcp.getFiles().size());
                assertEquals(failMsg, 1, dcp.getManifestations().size());
                break;
            default:
                throw new RuntimeException("Unrecognized EntityType: " + type);
        }
    }

    private static Dcp getDcp(EntityType type, DcsEntity entity) {
        Dcp dcp = new Dcp();
        switch (type) {
            case COLLECTION:
                dcp.addCollection((DcsCollection) entity);
                break;
            case DELIVERABLE_UNIT:
                dcp.addDeliverableUnit((DcsDeliverableUnit) entity);
                break;
            case EVENT:
                dcp.addEvent((DcsEvent) entity);
                break;
            case FILE:
                dcp.addFile((DcsFile) entity);
                break;
            case MANIFESTATION:
                dcp.addManifestation((DcsManifestation) entity);
                break;
            default:
                throw new RuntimeException("Unrecognized EntityType: " + type);
        }
        return dcp;
    }

    // Ensure that various invocations of listEntities return the expected
    // entity ids
    private void doListTests(Map<EntityType, Set<String>> idMap) {
        Set allExpectedIds = new HashSet<String>();
        for (EntityType type: idMap.keySet()) {
            Set<String> expectedIds = idMap.get(type);
            Set<String> actualIds = toSet(store.listEntities(type));
            assertEquals("Unexpected # of " + type + " entities in store",
                    expectedIds.size(), actualIds.size());
            assertEquals("Unexpected ids of " + type + " entities in store",
                    expectedIds, actualIds);
            allExpectedIds.addAll(expectedIds);
        }
        Set<String> allActualIds = toSet(store.listEntities(null));
        assertEquals("Unexpected # of all entities in store",
                allExpectedIds.size(), allActualIds.size());
        assertEquals("Unexpected ids of all entities in store",
                allExpectedIds, allActualIds);
    }

    private Map<EntityType, Set<String>> getIdMap(Dcp dcp) {
        Map<EntityType, Set<String>> map = new HashMap<EntityType, Set<String>>();
        map.put(EntityType.COLLECTION, getIds(dcp.getCollections()));
        map.put(EntityType.DELIVERABLE_UNIT, getIds(dcp.getDeliverableUnits()));
        map.put(EntityType.EVENT, getIds(dcp.getEvents()));
        map.put(EntityType.FILE, getIds(dcp.getFiles()));
        map.put(EntityType.MANIFESTATION, getIds(dcp.getManifestations()));
        return map;
    }

    private Map<EntityType, Collection<? extends DcsEntity>> getEntityMap(Dcp dcp) {
        Map<EntityType, Collection<? extends DcsEntity>> map =
                new HashMap<EntityType, Collection<? extends DcsEntity>>();
        map.put(EntityType.COLLECTION, dcp.getCollections());
        map.put(EntityType.DELIVERABLE_UNIT, dcp.getDeliverableUnits());
        map.put(EntityType.EVENT, dcp.getEvents());
        map.put(EntityType.FILE, dcp.getFiles());
        map.put(EntityType.MANIFESTATION, dcp.getManifestations());
        return map;
    }

    private Set<String> getIds(Collection<? extends DcsEntity> entities) {
        Set<String> ids = new HashSet<String>();
        for (DcsEntity entity: entities) {
            ids.add(entity.getId());
        }
        return ids;
    }

    private InputStream getInputStream(Dcp dcp) {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        builder.buildSip(dcp, sink);
        return new ByteArrayInputStream(sink.toByteArray());
    }

    private static DcsCollection createTestCollection(int idSuffix) {
        String id = "testCollection" + idSuffix;
        DcsCollection entity = new DcsCollection();
        entity.setId(id);
        entity.setTitle(id + " Title");
        return entity;
    }

    private static DcsDeliverableUnit createTestDeliverableUnit(int idSuffix) {
        String id = "testDeliverableUnit" + idSuffix;
        DcsDeliverableUnit entity = new DcsDeliverableUnit();
        entity.setId(id);
        entity.setTitle(id + " Title");
        return entity;
    }

    private static DcsEvent createTestEvent(int idSuffix, DcsEntity target) {
        String id = "testEvent" + idSuffix;
        DcsEvent entity = new DcsEvent();
        entity.setId(id);
        entity.setEventType("ingest");
        entity.setDate("2010-08-01T00:00:00Z");
        entity.addTargets(new DcsEntityReference(id));
        return entity;
    }

    private static DcsFile createTestFile(int idSuffix) {
        String id = "testFile" + idSuffix;
        DcsFile entity = new DcsFile();
        entity.setId(id);
        String filename = id + ".txt";
        entity.setName(filename);
        entity.setExtant(false);
        // Note: source is not required according to the dcp schema,
        // but is required for the Fedora impl to work properly
        entity.setSource("http://example.org/" + filename);
        return entity;
    }

    private static DcsManifestation createTestManifestation(int idSuffix,
            DcsDeliverableUnit du) {
        String id = "testManifestation" + idSuffix;
        DcsManifestation entity = new DcsManifestation();
        entity.setId(id);
        entity.setDeliverableUnit(du.getId());
        return entity;
    }

}
