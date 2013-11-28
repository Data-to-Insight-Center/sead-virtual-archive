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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.junit.Assert;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public abstract class EventManagerTest {

    public String TEST_EVENT_TYPE = "testEvent";

    protected abstract EventManager getEventManager();

    protected abstract SipStager getSipStager();

    @Test
    public void creatAndPersistTest() {
        EventManager mgr = getEventManager();
        DcsEvent e = mgr.newEvent(TEST_EVENT_TYPE);
        e.setDetail("detail");
        e.setOutcome("outcome");
        e.setTargets(getTargets());

        String id = getSipStager().addSIP(new Dcp());

        mgr.addEvent(id, e);

        Collection<DcsEvent> retrieved = mgr.getEvents(id);

        assertEquals("Expected to see one event", 1, retrieved.size());

        assertTrue("Did not see test event", retrieved.contains(e));
    }

    @Test
    public void getSingletonEventTest() {
        EventManager mgr = getEventManager();
        String id = getSipStager().addSIP(new Dcp());

        mgr.addEvent(id, mgr.newEvent(TEST_EVENT_TYPE));
        mgr.addEvent(id, mgr.newEvent("second event type"));

        assertNotNull(mgr.getEventByType(id, TEST_EVENT_TYPE));

    }

    @Test
    public void getNonexistentSingletonTest() {
        EventManager mgr = getEventManager();
        String id = getSipStager().addSIP(new Dcp());
        mgr.addEvent(id, mgr.newEvent(TEST_EVENT_TYPE));

        Assert.assertNull(mgr.getEventByType(id, "nonexistant"));
    }

    @Test(expected = Exception.class)
    public void getDuplicateSingletonTest() {
        EventManager mgr = getEventManager();
        String id = getSipStager().addSIP(new Dcp());

        mgr.addEvent(id, mgr.newEvent(TEST_EVENT_TYPE));
        mgr.addEvent(id, mgr.newEvent(TEST_EVENT_TYPE));

        assertNotNull(mgr.getEventByType(id, TEST_EVENT_TYPE));
    }

    @Test
    public void generatedValuesTest() {
        DcsEvent event1 = getEventManager().newEvent(TEST_EVENT_TYPE);
        DcsEvent event2 = getEventManager().newEvent(TEST_EVENT_TYPE);

        assertNotNull(event1.getId());
        assertNotSame(event1.getId(), event2.getId());

        assertNotNull(event1.getEventType());

        assertNotNull(event1.getDate());
    }

    private Set<DcsEntityReference> getTargets() {
        Set<DcsEntityReference> refs = new HashSet<DcsEntityReference>();

        refs.add(new DcsEntityReference("id:1"));
        refs.add(new DcsEntityReference("id:2"));

        return refs;
    }
}
