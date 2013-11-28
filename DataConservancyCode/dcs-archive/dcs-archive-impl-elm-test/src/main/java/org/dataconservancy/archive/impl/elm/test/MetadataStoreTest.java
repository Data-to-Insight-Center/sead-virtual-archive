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
package org.dataconservancy.archive.impl.elm.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;

import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.impl.elm.Metadata;
import org.dataconservancy.archive.impl.elm.MetadataStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class MetadataStoreTest {

    private Random random = new Random();

    @Test
    public void immediateAddTest() throws Exception {
        final String ID = randomId();
        final String TYPE = randomType();
        final String SRC = "example:/source";

        MetadataStore mStore = getWritable();

        Metadata result = mStore.add(ID, TYPE, SRC);

        assertEquals(ID, result.getId());
        assertEquals(TYPE, result.getType());
        assertEquals(SRC, result.getSrc());
    }

    @Test
    public void removalTest() throws Exception {
        final String ID = randomId();
        final String TYPE = randomType();
        final String SRC = "example:/source";

        MetadataStore mStore = getWritable();

        mStore.add(ID, TYPE, SRC);

        mStore.remove(ID);

        assertNull(mStore.get(ID));
    }

    @Test
    public void retrievedAddTest() throws Exception {
        final String ID = randomId();
        final String TYPE = randomType();
        final String SRC = "example:/source";

        MetadataStore mStore = getWritable();

        mStore.add(ID, TYPE, SRC);

        Metadata result = mStore.get(ID);

        assertEquals(ID, result.getId());
        assertEquals(TYPE, result.getType());
        assertEquals(SRC, result.getSrc());
    }

    @Test
    public void nullSrcAddTest() throws Exception {
        final String ID = randomId();
        final String TYPE = randomType();

        MetadataStore mStore = getWritable();

        assertNull(mStore.add(ID, TYPE, null).getSrc());

        Metadata result = mStore.get(ID);

        assertEquals(ID, result.getId());
        assertEquals(TYPE, result.getType());
        assertNull(result.getSrc());
    }

    @Test
    public void idempotencyAddTest() throws Exception {
        final String ID = randomId();
        final String TYPE = randomType();
        final String SRC = "example:/source";

        MetadataStore mStore = getWritable();

        mStore.add(ID, TYPE, SRC);
        mStore.add(ID, TYPE, SRC);

        Metadata result = mStore.get(ID);

        assertEquals(ID, result.getId());
        assertEquals(TYPE, result.getType());
        assertEquals(SRC, result.getSrc());
    }

    @Test
    public void immediateLinkTest() throws Exception {

        final int COUNT = 3;

        MetadataStore mStore = getWritable();

        Metadata md = mStore.add(randomId(), randomType(), null);

        Map<String, String> rels = getExampleRels(COUNT);

        md.addLinks(rels);

        assertEquals(COUNT, md.getLinks().entrySet().size());
        assertTrue(md.getLinks().entrySet().containsAll(rels.entrySet()));
    }

    @Test
    public void retrievedLinkTest() throws Exception {
        final int COUNT = 3;

        MetadataStore mStore = getWritable();

        Metadata md = mStore.add(randomId(), randomType(), null);
        final String ID = md.getId();

        Map<String, String> rels = getExampleRels(COUNT);

        mStore.get(ID).addLinks(rels);

        md = mStore.get(ID);

        assertEquals(COUNT, md.getLinks().entrySet().size());
        assertTrue(md.getLinks().entrySet().containsAll(rels.entrySet()));
    }

    @Test
    public void individualLinkTest() throws Exception {
        final int COUNT = 3;

        MetadataStore mStore = getWritable();

        Metadata md = mStore.add(randomId(), randomType(), null);
        final String ID = md.getId();

        Map<String, String> rels = new HashMap<String, String>();

        for (int i = 0; i < COUNT; i++) {
            Map<String, String> toAdd = getExampleRels(1);
            mStore.get(ID).addLinks(toAdd);
            rels.putAll(toAdd);
        }

        md = mStore.get(ID);

        assertEquals(COUNT, md.getLinks().entrySet().size());
        assertTrue(md.getLinks().entrySet().containsAll(rels.entrySet()));
    }

    @Test
    public void idempotentLinkTest() throws Exception {
        final int COUNT = 3;

        MetadataStore mStore = getWritable();

        Metadata md = mStore.add(randomId(), randomType(), null);
        final String ID = md.getId();

        Map<String, String> rels = getExampleRels(COUNT);

        mStore.get(ID).addLinks(rels);
        mStore.get(ID).addLinks(rels);
        mStore.get(ID).addLinks(rels);

        md = mStore.get(ID);

        assertEquals(COUNT, md.getLinks().entrySet().size());
        assertTrue(md.getLinks().entrySet().containsAll(rels.entrySet()));
    }

    @Test
    public void iteratorAllTest() throws Exception {
        final int COUNT = 20;
        MetadataStore mStore = getWritable();

        Map<String, String> submitted = new HashMap<String, String>();

        for (int i = 0; i < COUNT; i++) {
            String id = randomId();
            String type = randomType();
            submitted.put(id, type);
            mStore.add(id, type, null);
        }

        Map<String, String> retrieved = new HashMap<String, String>();

        for (Metadata m : mStore.getAll()) {
            retrieved.put(m.getId(), m.getType());
        }

        assertTrue(submitted.size() <= retrieved.size());
        assertTrue(retrieved.entrySet().containsAll(submitted.entrySet()));
    }

    @Test
    public void iterateRestrictedTest() {
        final int COUNT = 20;
        MetadataStore mStore = getWritable();

        Map<String, String> submitted = new HashMap<String, String>();

        for (int i = 0; i < COUNT; i++) {
            String id = randomId();
            String type = randomType();
            submitted.put(id, type);
            mStore.add(id, type, null);
        }

        Map<String, String> retrieved = new HashMap<String, String>();

        for (String type : new HashSet<String>(submitted.values())) {
            for (Metadata m : mStore.getAll(type)) {
                assertEquals(type, m.getType());
                if (submitted.containsKey(m.getId())) {
                    retrieved.put(m.getId(), m.getType());
                }
            }
        }

        assertEquals(submitted.size(), retrieved.size());
        assertTrue(retrieved.entrySet().containsAll(submitted.entrySet()));
    }

    private Map<String, String> getExampleRels(int count) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < count; i++) {
            map.put(randomId(), randomType());
        }
        return map;
    }

    protected String randomType() {
        EntityType[] types = EntityType.values();
        return types[Math.abs(random.nextInt()) % (types.length - 1)]
                .toString();

    }

    protected String randomId() {
        return "uuid:/" + UUID.randomUUID().toString();
    }

    protected abstract MetadataStore getMetadataStore();

    private MetadataStore getWritable() {
        MetadataStore store = getMetadataStore();
        assertFalse(store.isReadOnly());
        return store;
    }
}
