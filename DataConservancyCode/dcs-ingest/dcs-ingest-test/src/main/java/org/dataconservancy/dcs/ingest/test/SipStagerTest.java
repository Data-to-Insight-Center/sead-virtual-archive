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
package org.dataconservancy.dcs.ingest.test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Abstract class containing generic SipStager test cases.
 * <p>
 * Used for testing basic functionality of sip stager implementations. At a
 * minimum, all concrete subclasses must provide an instance of a fully
 * configured SipStager to test.
 * </p>
 */
public abstract class SipStagerTest {

    private static Dcp exampleDcp;

    /** Populates an example sip */
    @BeforeClass
    public static void initExampleSips() {
        DcsCollection coll = new DcsCollection();
        coll.setId("example:/collection");

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.addCollection(new DcsCollectionRef(coll.getId()));
        du.setId("example:/deleverableUnit");

        exampleDcp = new Dcp();
        exampleDcp.addCollection(coll);
        exampleDcp.addDeliverableUnit(du);
    }

    /** Tests basic add/retrieve functionality */
    @Test
    public void addTest() {
        SipStager stager = getSipStager();
        Dcp empty = new Dcp();

        Assert.assertEquals(empty, stager.getSIP(stager.addSIP(empty)));

        Assert.assertEquals(exampleDcp, stager.getSIP(stager.addSIP(exampleDcp)));

    }

    /** Assures that all keys of added sips are retrievable */
    @Test
    public void getKeysTest() {
        SipStager stager = getSipStager();
        Set<String> ids = new HashSet<String>();

        for (int i = 0; i < 12; i++) {
            ids.add(stager.addSIP(new Dcp()));
        }

        assertTrue(stager.getKeys().containsAll(ids));
    }

    /** Assures that sip stager impls persist changes */
    @Test
    public void updateTest() {
        SipStager stager = getSipStager();
        String id = stager.addSIP(new Dcp());

        stager.updateSIP(exampleDcp, id);

        Assert.assertEquals(exampleDcp, stager.getSIP(id));
    }

    /** Assires that sip stager impls delete sips when requested */
    @Test
    public void removeTest() {
        SipStager stager = getSipStager();
        String id = stager.addSIP(new Dcp());
        stager.removeSIP(id);
        assertNull(stager.getSIP(id));
        Assert.assertFalse(stager.getKeys().contains(id));
    }

    /** Assures that impls don't panic if a nonexistent sip is requested */
    @Test
    public void nonExistentSipTest() {
        SipStager stager = getSipStager();
        assertNull(stager.getSIP(UUID.randomUUID().toString()));
    }

    /** Get a fully initialized sip stager impl */
    protected abstract SipStager getSipStager();
}
