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
package org.dataconservancy.dcs.ingest.file.impl;

import java.io.ByteArrayOutputStream;

import java.util.Collection;
import java.util.Set;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;

import junit.framework.Assert;

public class FileContentDocumentTest {

    private static SipStager stager = new MemoryStager();

    private static IdService id = new MemoryIdServiceImpl();

    private static EventManager mgr;

    private static DcsFile FILE = new DcsFile();

    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @BeforeClass
    public static void init() throws Exception {
        InlineEventManager iem = new InlineEventManager();
        iem.setIdService((BulkIdCreationService)id);
        iem.setSipStager(stager);
        mgr = iem;

        FILE.setId(id.create(Types.FILE.getTypeName()).toString());
        DcsFixity fixity = new DcsFixity();
        fixity.setAlgorithm("FAKE");
        fixity.setValue("0x80");
        FILE.addFixity(fixity);
        FILE.setSource("http://example.org/test");
    }

    @Test
    public void dcsFilesAreEqualTest() throws Exception {
        FileContentDocument document = getDocument();

        Collection<DcsFile> files =
                builder.buildSip(document.getInputStream()).getFiles();

        Assert.assertEquals(1, files.size());

        Assert.assertEquals(FILE, files.iterator().next());
    }

    @Test
    public void hasDepositEventTest() throws Exception {
        FileContentDocument document = getDocument();
        Collection<DcsEvent> events =
                builder.buildSip(document.getInputStream()).getEvents();
        Assert.assertEquals(1, events.size());
    }

    @Test
    public void hasAdditionalEventsTest() throws Exception {
        DcsEvent e1 = mgr.newEvent("Test1");
        DcsEvent e2 = mgr.newEvent("Test2");
        FileContentDocument document = getDocument(e1, e2);

        Collection<DcsEvent> events =
                builder.buildSip(document.getInputStream()).getEvents();

        Assert.assertEquals(3, events.size());
    }

    @Test
    public void mimeTypeTest() {
        FileContentDocument document = getDocument();
        Assert.assertEquals("application/xml", document.getMimeType());
    }

    private FileContentDocument getDocument(DcsEvent... additionalEvents) {
        Dcp sip = new Dcp();
        sip.addFile(FILE);
        String depositid = stager.addSIP(sip);

        DcsEvent deposit = mgr.newEvent(Events.DEPOSIT);
        deposit.setOutcome(depositid);
        deposit.addTargets(new DcsEntityReference(FILE.getId()));
        mgr.addEvent(depositid, deposit);

        for (DcsEvent e : additionalEvents) {
            e.addTargets(new DcsEntityReference(FILE.getId()));
            mgr.addEvent(depositid, e);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        builder.buildSip(sip, out);

        return new FileContentDocument(sip, depositid, mgr);
    }
}
