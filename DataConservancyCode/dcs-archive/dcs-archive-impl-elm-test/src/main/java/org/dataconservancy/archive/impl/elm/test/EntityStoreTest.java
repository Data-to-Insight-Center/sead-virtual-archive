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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.junit.Assert;
import org.junit.Test;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.impl.elm.EntityStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Base class for performing general entity store tests.
 * <p>
 * TODO: real xml comparison.
 * </p>
 */
public abstract class EntityStoreTest {

    private static final String RESOURCE_BASE =
            "/org/dataconservancy/archive/impl/elm/test/";

    @Test
    public void basicRetrievalTest() throws Exception {
        String ID = "example:/basicRetrieval";
        String file = RESOURCE_BASE + "basicRetrieval.xml";
        EntityStore eStore = getEntityStore();
        InputStream originalStream = this.getClass().getResourceAsStream(file);
        String original =
                IOUtils.toString(originalStream);

        assertNotNull(original);
        InputStream inStream = IOUtils.toInputStream(original);
        eStore.put(ID, inStream);

        originalStream.close();
        inStream.close();
        assertEquals(original, IOUtils.toString(eStore.get(ID)));
    }

    @Test
    public void idempotentcyTest() throws Exception {
        String ID = "example:/idempotency";
        String file = RESOURCE_BASE + "idempotency.xml";
        EntityStore eStore = getEntityStore();
        
        InputStream originalStream = this.getClass().getResourceAsStream(file);
        String original =
                IOUtils.toString(originalStream);

        assertNotNull(original);
        
        InputStream inStream = IOUtils.toInputStream(original);
        eStore.put(ID, inStream);
        
        InputStream inStream2 = IOUtils.toInputStream(original);
        eStore.put(ID, inStream2);
        
        InputStream inStream3 = IOUtils.toInputStream(original);
        eStore.put(ID, inStream3);

        assertEquals(original, IOUtils.toString(eStore.get(ID)));

        originalStream.close();
        inStream.close();
        inStream2.close();
        inStream3.close();
    }

    @Test
    public void replacementTest() throws Exception {
        String ID = "example:/replacement";
        InputStream replacement1Stream = this.getClass()
                        .getResourceAsStream(RESOURCE_BASE
                                + "replacement_1.xml");
        String v1 =
                IOUtils.toString(replacement1Stream);
        
        InputStream replacement2Stream = this.getClass()
                        .getResourceAsStream(RESOURCE_BASE
                                + "replacement_2.xml");
        String v2 =
                IOUtils.toString(replacement2Stream);
        EntityStore eStore = getEntityStore();

        assertNotNull(v1);
        assertNotNull(v2);
        assertFalse(v1.equals(v2));
        
        replacement1Stream.close();
        replacement2Stream.close();
        
        InputStream v1Stream = IOUtils.toInputStream(v1);
        eStore.put(ID, v1Stream);

        assertEquals(v1, IOUtils.toString(eStore.get(ID)));

        InputStream v2Stream = IOUtils.toInputStream(v2);
        eStore.put(ID, v2Stream);

        assertEquals(v2, IOUtils.toString(eStore.get(ID)));
        
        v1Stream.close();
        v2Stream.close();
    }

    @Test
    public void removalTest() throws Exception {
        String ID = "example:/basicRetrieval";
        String file = RESOURCE_BASE + "basicRetrieval.xml";
        EntityStore eStore = getEntityStore();
        
        InputStream originalStream = this.getClass().getResourceAsStream(file);
        String original =
                IOUtils.toString(originalStream);

        assertNotNull(original);
        
        eStore.put(ID, originalStream);
              
        originalStream.close();       
        System.gc();
        eStore.remove(ID);

        try {
            eStore.get(ID);
            Assert.fail();
        } catch (EntityNotFoundException e) {
            /* Expected */
        }
    }

    protected abstract EntityStore getEntityStore();

}
