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
package org.dataconservancy.dcs.ingest.deposit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;

import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;

import static org.dataconservancy.dcs.util.DateUtility.now;
import static org.dataconservancy.dcs.util.DateUtility.toIso8601;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AtomEventStatusDocumentTest {

    private static final Abdera abdera = new Abdera();

    private static EventManager manager;

    private static SipStager stager;

    @BeforeClass
    public static void init() {
        stager = new MemoryStager();
        InlineEventManager m = new InlineEventManager();
        m.setIdService(new MemoryIdServiceImpl());
        m.setSipStager(stager);
        manager = m;
    }

    @Test
    public void sameNumberOfEventsTest() {
        String id = stager.addSIP(new Dcp());
        List<DcsEvent> events = new ArrayList<DcsEvent>();

        events.add(manager.newEvent(Events.DEPOSIT));
        events.add(manager.newEvent(Events.INGEST_START));
        events.add(manager.newEvent(Events.INGEST_SUCCESS));

        manager.addEvents(id, events);

        AtomEventStatusDocument doc = new AtomEventStatusDocument(id, manager);

        Document<Feed> d = abdera.getParser().parse(doc.getInputStream());
        Feed feed = d.getRoot();

        assertEquals(3, feed.getEntries().size());
    }

    @Test
    public void eventTranslationTest() {
        String id = stager.addSIP(new Dcp());
        List<DcsEvent> events = new ArrayList<DcsEvent>();
        DcsEvent original = manager.newEvent(Events.DEPOSIT);
        original.setDetail("myDetail");
        original.setOutcome("myOutcome");
        Set<DcsEntityReference> references = new HashSet<DcsEntityReference>();
        references.add(new DcsEntityReference("test:/ref1"));
        references.add(new DcsEntityReference("test:/ref2"));
        original.setTargets(references);
        events.add(original);

        manager.addEvents(id, events);

        AtomEventStatusDocument doc = new AtomEventStatusDocument(id, manager);
        Document<Feed> d = abdera.getParser().parse(doc.getInputStream());
        Feed feed = d.getRoot();

        assertEquals(1, feed.getEntries().size());
        Entry entry = feed.getEntries().iterator().next();

        assertEquals(DateUtility.parseDate(original.getDate()), entry
                .getUpdated().getTime());
        assertEquals(original.getId(), entry.getId().toString());
        assertEquals(original.getOutcome(), entry.getContent());
        assertEquals(original.getDetail(), entry.getSummary());

        List<String> links = new ArrayList<String>();
        List<String> targets = new ArrayList<String>();
        for (Link l : entry.getLinks(Link.REL_RELATED)) {
            links.add(l.getHref().toString());
        }
        for (DcsEntityReference ref : original.getTargets()) {
            targets.add(ref.getRef());
        }

        assertTrue(links.containsAll(targets));
        assertTrue(targets.containsAll(links));
    }

    @Test
    public void eventOrderTest() {
        String id = stager.addSIP(new Dcp());
        List<DcsEvent> events = new ArrayList<DcsEvent>();

        events.add(manager.newEvent(Events.DEPOSIT));
        events.add(manager.newEvent(Events.INGEST_START));
        events.add(manager.newEvent(Events.INGEST_SUCCESS));

        manager.addEvents(id, events);

        AtomEventStatusDocument doc = new AtomEventStatusDocument(id, manager);

        Document<Feed> d = abdera.getParser().parse(doc.getInputStream());
        Feed feed = d.getRoot();

        long date = new Date().getTime();
        for (Entry e : feed.getEntries()) {
            assertTrue(e.getUpdated().getTime() <= date);
            date = e.getUpdated().getTime();
        }
    }

    @Test
    public void feedUpdatedTest() {
        String id = stager.addSIP(new Dcp());
        List<DcsEvent> events = new ArrayList<DcsEvent>();

        events.add(manager.newEvent(Events.DEPOSIT));
        events.add(manager.newEvent(Events.INGEST_START));
        DcsEvent e_last = manager.newEvent(Events.INGEST_SUCCESS);
        events.add(e_last);

        manager.addEvents(id, events);

        AtomEventStatusDocument doc = new AtomEventStatusDocument(id, manager);

        Document<Feed> d = abdera.getParser().parse(doc.getInputStream());
        Feed feed = d.getRoot();

        assertEquals(DateUtility.parseDate(e_last.getDate()), feed.getUpdated()
                .getTime());
    }

    @Test
    public void twoEventsWithSameDateTest() {
        String id = stager.addSIP(new Dcp());
        List<DcsEvent> events = new ArrayList<DcsEvent>();
        String date = toIso8601(now());
        DcsEvent e1 = new DcsEvent();
        e1.setEventType(Events.DEPOSIT);
        e1.setId("test:/1");
        e1.setDate(date);

        DcsEvent e2 = new DcsEvent();
        e2.setEventType(Events.INGEST_START);
        e2.setId("test:/2");
        e2.setDate(date);

        events.add(e1);
        events.add(e2);
        manager.addEvents(id, events);

        AtomEventStatusDocument doc = new AtomEventStatusDocument(id, manager);

        Document<Feed> d = abdera.getParser().parse(doc.getInputStream());
        Feed feed = d.getRoot();
        assertEquals(2, feed.getEntries().size());
    }

    @Test
    public void mimeTypeTest() {
        String id = stager.addSIP(new Dcp());
        List<DcsEvent> events = new ArrayList<DcsEvent>();

        events.add(manager.newEvent(Events.DEPOSIT));
        manager.addEvents(id, events);

        AtomEventStatusDocument doc = new AtomEventStatusDocument(id, manager);

        assertEquals("application/atom+xml;type=feed", doc.getMimeType());

    }
}
