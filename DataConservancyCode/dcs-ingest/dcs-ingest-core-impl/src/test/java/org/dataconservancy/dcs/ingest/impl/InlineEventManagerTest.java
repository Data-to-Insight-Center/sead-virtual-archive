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

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.test.EventManagerTest;
import org.junit.Assert;
import org.junit.Test;

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;

public class InlineEventManagerTest
        extends EventManagerTest {

    private static final InlineEventManager mgr = new InlineEventManager();

    private static SipStager stager = new MemoryStager();

    static {
        mgr.setIdService(new MemoryIdServiceImpl());
        mgr.setSipStager(stager);
    }

    protected EventManager getEventManager() {
        return mgr;
    }

    @Test
    public void eventActuallyInSipTest() {
        DcsEvent e = mgr.newEvent("test");
        Dcp sip = new Dcp();

        String id = stager.addSIP(sip);

        mgr.addEvent(id, e);

        Assert.assertTrue(stager.getSIP(id).getEvents().contains(e));
    }

    @Test
    public void getEventByIdTest() {
        Dcp dcp1 = new Dcp();
        Dcp dcp2 = new Dcp();
        String id1 = mgr.getSipStager().addSIP(dcp1);
        String id2 = mgr.getSipStager().addSIP(dcp2);

        DcsEvent e1 = mgr.newEvent(TEST_EVENT_TYPE);
        DcsEvent e2 = mgr.newEvent(TEST_EVENT_TYPE);
        mgr.addEvent(id1, e1);
        mgr.addEvent(id2, e2);

        Assert.assertEquals("Did not find event", e1.getId(), mgr
                .findEventById(e1.getId()).getId());
    }

    protected SipStager getSipStager() {
        return stager;
    }

}
