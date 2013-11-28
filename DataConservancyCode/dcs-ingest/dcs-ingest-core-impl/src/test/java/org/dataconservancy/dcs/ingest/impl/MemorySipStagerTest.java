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
package org.dataconservancy.dcs.ingest.impl;

import org.dataconservancy.dcs.ingest.SipStager;
import org.junit.Assert;
import org.junit.Test;

import org.dataconservancy.dcs.ingest.test.SipStagerTest;
import org.dataconservancy.model.dcp.Dcp;

public class MemorySipStagerTest
        extends SipStagerTest {

    MemoryStager stager = new MemoryStager();

    @Override
    protected SipStager getSipStager() {
        return stager;
    }

    @Test
    public void finishTest() {
        String id = stager.addSIP(new Dcp());
        stager.retire(id);
        Assert.assertNull(stager.getSIP(id));
        Assert.assertFalse(stager.getKeys().contains(id));
    }

    @Test
    public void noDeleteTest() {
        stager.setDeleteUponRetire(false);
        String id = stager.addSIP(new Dcp());
        stager.retire(id);
        Assert.assertNotNull(stager.getSIP(id));
        Assert.assertTrue(stager.getKeys().contains(id));
        stager.setDeleteUponRetire(true);
    }
}
