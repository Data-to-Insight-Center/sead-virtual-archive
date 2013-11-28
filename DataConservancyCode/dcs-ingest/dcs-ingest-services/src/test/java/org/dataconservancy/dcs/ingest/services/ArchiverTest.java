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
package org.dataconservancy.dcs.ingest.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

public class ArchiverTest {

    private static SipStager stager = new MemoryStager();

    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @Test
    public void hasSameContentTest() throws Exception {

        FakeArchive archive = new FakeArchive();

        Archiver archiver = getArchiver();
        archiver.setArchiveStore(archive);

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setExtant(false);
        file.setName("Name");
        file.setId("one");
        sip.addFile(file);

        String id = stager.addSIP(sip);

        archiver.execute(id);

        Assert.assertNotNull(archive.lastPkg);
        ByteArrayOutputStream written = new ByteArrayOutputStream();

        IOUtils.copy(archive.lastPkg, written);

        Dcp depackaged =
                builder
                        .buildSip(new ByteArrayInputStream(written
                                .toByteArray()));
        for (DcsEvent e : depackaged.getEvents()) {
            sip.addEvent(e);
        }

        Assert.assertEquals(sip, builder
                .buildSip(new ByteArrayInputStream(written.toByteArray())));
    }

    @Test
    public void hasArchiveEventTest() throws Exception {
        FakeArchive archive = new FakeArchive();

        Archiver archiver = getArchiver();
        archiver.setArchiveStore(archive);

        Dcp sip = new Dcp();

        String id = stager.addSIP(sip);

        archiver.execute(id);

        Assert.assertNotNull(archive.lastPkg);

        Dcp depackaged = builder.buildSip(archive.lastPkg);

        Assert.assertEquals(1, depackaged.getEvents().size());
    }

    @Test
    public void archiveEventContentTest() throws Exception {
        List<String> ids = Arrays.asList("1", "2", "3", "4", "5", "6");

        FakeArchive archive = new FakeArchive();

        Archiver archiver = getArchiver();
        archiver.setArchiveStore(archive);

        Dcp sip = new Dcp();

        DcsCollection coll = new DcsCollection();
        coll.setId(ids.get(0));
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(ids.get(1));
        DcsEvent e = new DcsEvent();
        e.setId(ids.get(2));
        DcsFile file = new DcsFile();
        file.setId(ids.get(3));
        DcsManifestation m = new DcsManifestation();
        m.setId(ids.get(4));
        DcsManifestation m2 = new DcsManifestation();
        m2.setId(ids.get(5));

        sip.addCollection(coll);
        sip.addDeliverableUnit(du);
        sip.addEvent(e);
        sip.addFile(file);
        sip.addManifestation(m);
        sip.addManifestation(m2);

        String id = stager.addSIP(sip);

        archiver.execute(id);

        Dcp depackaged = builder.buildSip(archive.lastPkg);

        DcsEvent archiveEvent = null;

        for (DcsEvent eV : depackaged.getEvents()) {
            if (Events.ARCHIVE.equals(eV.getEventType())) {
                archiveEvent = eV;
            }
        }

        Assert.assertNotNull(e);

        for (DcsEntityReference er : archiveEvent.getTargets()) {
            Assert.assertTrue(ids.contains(er.getRef()));
        }

        Assert.assertEquals(ids.size(), archiveEvent.getTargets().size());

        Assert.assertEquals(Integer.toString(ids.size()),(archiveEvent
                .getOutcome()));

    }

    public Archiver getArchiver() {
        IngestFramework fwk = new IngestFramework();
        InlineEventManager mgr = new InlineEventManager();
        mgr.setIdService(new MemoryIdServiceImpl());
        mgr.setSipStager(stager);

        fwk.setSipStager(stager);
        fwk.setEventManager(mgr);

        Archiver archiver = new Archiver();

        archiver.setIngestFramework(fwk);
        archiver.setModelBuilder(builder);

        return archiver;
    }

    private static class FakeArchive
            implements ArchiveStore {

        public InputStream lastPkg;

        public InputStream getContent(String entityId)
                throws EntityNotFoundException, EntityTypeException {
            return null;
        }

        public InputStream getFullPackage(String entityId)
                throws EntityNotFoundException {
            return null;
        }

        public InputStream getPackage(String entityId)
                throws EntityNotFoundException {
            return null;
        }

        public Iterator<String> listEntities(EntityType type) {
            return null;
        }

        public void putPackage(InputStream dcpStream) throws AIPFormatException {
            lastPkg = dcpStream;
        }

    }
}
