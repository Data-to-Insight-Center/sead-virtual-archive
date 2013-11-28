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
package org.dataconservancy.dcs.archive.impl.elm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.impl.elm.DcpEntityStream;
import org.dataconservancy.archive.impl.elm.EntityStore;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

import static org.dataconservancy.dcs.archive.impl.elm.DcpUtil.randomId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DcsEntityStreamTest {

    private static final String DCP_SCHEMA_LOC = "/schema/dcp.xsd";

    private final DcsModelBuilder model = new DcsXstreamStaxModelBuilder();

    private static Schema schema;

    @BeforeClass
    public static void getSchema() throws Exception {
        schema =
                SchemaFactory
                        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new StreamSource(DcsEntityStreamTest.class
                                .getResourceAsStream(DcsEntityStreamTest.DCP_SCHEMA_LOC)));
    }

    @Test
    public void noEntityTest() throws Exception {
        Map<String, Set<String>> entities = new HashMap<String, Set<String>>();

        schema
                .newValidator()
                .validate(new StreamSource(new DcpEntityStream(entities,
                                                               new MemStore())));
    }

    @Test
    public void singleEntityTest() throws Exception {
        Map<String, Set<String>> entities = new HashMap<String, Set<String>>();
        EntityStore eStore = new MemStore();

        storeDeliverableUnit(entities, eStore);

        schema
                .newValidator()
                .validate(new StreamSource(new DcpEntityStream(entities, eStore)));

        compareEntities(new DcpEntityStream(entities, eStore), entities);
    }

    @Test
    public void multipleSametypeTest() throws Exception {
        Map<String, Set<String>> entities = new HashMap<String, Set<String>>();
        EntityStore eStore = new MemStore();

        storeDeliverableUnit(entities, eStore);
        storeDeliverableUnit(entities, eStore);
        storeDeliverableUnit(entities, eStore);

        schema
                .newValidator()
                .validate(new StreamSource(new DcpEntityStream(entities, eStore)));

        compareEntities(new DcpEntityStream(entities, eStore), entities);
    }

    @Test
    public void multipleAllTypesTest() throws Exception {
        Map<String, Set<String>> entities = new HashMap<String, Set<String>>();
        EntityStore eStore = new MemStore();

        for (int i = 0; i < 5; i++) {
            storeDeliverableUnit(entities, eStore);
            storeCollection(entities, eStore);
            storeEvent(entities, eStore);
            storeFile(entities, eStore);
            storeManifestation(entities, eStore);
        }

        schema
                .newValidator()
                .validate(new StreamSource(new DcpEntityStream(entities, eStore)));

        compareEntities(new DcpEntityStream(entities, eStore), entities);
    }

    @Test
    public void missingEntityTest() throws Exception {
        Map<String, Set<String>> entities = new HashMap<String, Set<String>>();

        EntityStore eStore = new MemStore();

        storeDeliverableUnit(entities, eStore);

        Map<String, Set<String>> goodEntities =
                new HashMap<String, Set<String>>();
        goodEntities.put(EntityType.DELIVERABLE_UNIT.toString(),
                         new HashSet<String>(entities
                                 .get(EntityType.DELIVERABLE_UNIT.toString())));

        /* Adding something to the request that isn't stored */
        entities.get(EntityType.DELIVERABLE_UNIT.toString()).add(randomId());

        schema
                .newValidator()
                .validate(new StreamSource(new DcpEntityStream(entities, eStore)));

        compareEntities(new DcpEntityStream(entities, eStore), goodEntities);
    }

    private void compareEntities(InputStream src,
                                 Map<String, Set<String>> entities)
            throws InvalidXmlException {
        Set<String> origEntities = new HashSet<String>();
        for (Set<String> set : entities.values()) {
            origEntities.addAll(set);
        }

        List<String> foundEntities = new ArrayList<String>();

        Dcp dcp = model.buildSip(src);

        for (DcsEntity c : dcp.getCollections()) {
            foundEntities.add(c.getId());
        }

        for (DcsEntity d : dcp.getDeliverableUnits()) {
            foundEntities.add(d.getId());
        }

        for (DcsEntity e : dcp.getEvents()) {
            foundEntities.add(e.getId());
        }

        for (DcsEntity f : dcp.getFiles()) {
            foundEntities.add(f.getId());
        }

        for (DcsEntity m : dcp.getManifestations()) {
            foundEntities.add(m.getId());
        }

        assertEquals(origEntities.size(), foundEntities.size());
        assertTrue(origEntities.containsAll(foundEntities));

    }

    private String storeDeliverableUnit(Map<String, Set<String>> entities,
                                        EntityStore eStore) {
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(randomId());
        du.setTitle("title");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.buildDeliverableUnit(du, out);
        eStore.put(du.getId(), new ByteArrayInputStream(out.toByteArray()));
        addTo(entities, du.getId(), EntityType.DELIVERABLE_UNIT.toString());
        return du.getId();
    }

    private String storeCollection(Map<String, Set<String>> entities,
                                   EntityStore eStore) {
        DcsCollection coll = new DcsCollection();
        coll.setId(randomId());
        coll.setTitle("title");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.buildCollection(coll, out);
        eStore.put(coll.getId(), new ByteArrayInputStream(out.toByteArray()));
        addTo(entities, coll.getId(), EntityType.COLLECTION.toString());
        return coll.getId();
    }

    private String storeEvent(Map<String, Set<String>> entities,
                              EntityStore eStore) {
        DcsEvent e = new DcsEvent();
        e.setId(randomId());
        e.setEventType("type");
        e.setDate("2001-12-31T12:00:00Z");
        e.addTargets(new DcsEntityReference(randomId()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.buildEvent(e, out);
        eStore.put(e.getId(), new ByteArrayInputStream(out.toByteArray()));
        addTo(entities, e.getId(), EntityType.EVENT.toString());
        return e.getId();
    }

    private String storeFile(Map<String, Set<String>> entities,
                             EntityStore eStore) {
        DcsFile f = new DcsFile();
        f.setId(randomId());
        f.setName("name");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.buildFile(f, out);
        eStore.put(f.getId(), new ByteArrayInputStream(out.toByteArray()));
        addTo(entities, f.getId(), EntityType.FILE.toString());
        return f.getId();
    }

    private String storeManifestation(Map<String, Set<String>> entities,
                                      EntityStore eStore) {
        DcsManifestation m = new DcsManifestation();
        m.setId(randomId());
        m.setDeliverableUnit(randomId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        model.buildManifestation(m, out);
        eStore.put(m.getId(), new ByteArrayInputStream(out.toByteArray()));
        addTo(entities, m.getId(), EntityType.MANIFESTATION.toString());
        return m.getId();
    }

    private void addTo(Map<String, Set<String>> entities, String id, String type) {
        if (!entities.containsKey(type)) {
            entities.put(type, new HashSet<String>());
        }
        entities.get(type).add(id);
    }

    private class MemStore
            implements EntityStore {

        private Map<String, byte[]> content = new HashMap<String, byte[]>();

        public InputStream get(String entityId) throws EntityNotFoundException {
            if (content.containsKey(entityId)) {
                return new ByteArrayInputStream(content.get(entityId));
            } else {
                throw new EntityNotFoundException(entityId);
            }
        }

        public void put(String entityId, InputStream stream) {
            try {
                content.put(entityId, IOUtils.toByteArray(stream));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void remove(String key) {
            if (content.containsKey(key)) {
                content.remove(key);
            }
        }
    }
}
