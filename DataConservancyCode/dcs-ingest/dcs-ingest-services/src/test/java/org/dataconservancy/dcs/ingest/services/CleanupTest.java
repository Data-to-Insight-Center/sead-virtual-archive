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

import org.apache.commons.io.IOUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.ingest.file.impl.MemoryFileContentStager;
import org.dataconservancy.dcs.ingest.impl.InMemoryLockService;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;

public class CleanupTest {

    private static IngestFramework ingest = new IngestFramework();

    private static Cleanup cleanup = new Cleanup();

    @BeforeClass
    public static void setUp() {
        MemoryStager ms = new MemoryStager();
        ms.setDeleteUponRetire(true);

        MemoryFileContentStager mfc = new MemoryFileContentStager();
        mfc.setSipStager(ms);

        InlineEventManager em = new InlineEventManager();
        em.setSipStager(ms);
        em.setIdService(new MemoryIdServiceImpl());

        ingest.setSipStager(ms);
        ingest.setFileContentStager(mfc);
        ingest.setEventManager(em);
        ingest.setLockService(new InMemoryLockService());

        cleanup.setIngestFramework(ingest);
    }

    @Test
    public void retireSipTest() throws IngestServiceException {
        String id = ingest.getSipStager().addSIP(new Dcp());

        cleanup.execute(id);

        Assert.assertNull(ingest.getSipStager().getSIP(id));
    }

    @Test
    public void retireStagedFilesTest() throws IngestServiceException {
        EventManager events = ingest.getEventManager();
        String id = ingest.getSipStager().addSIP(new Dcp());

        StagedFile file1 =
                ingest.getFileContentStager()
                        .add(IOUtils.toInputStream("content"), null);

        StagedFile file2 =
                ingest.getFileContentStager()
                        .add(IOUtils.toInputStream("input"), null);

        Assert.assertNotNull(ingest.getSipStager().getSIP(file1.getSipRef()));
        Assert.assertNotNull(ingest.getSipStager().getSIP(file2.getSipRef()));

        DcsEvent file1Event = events.newEvent(Events.FILE_RESOLUTION_STAGED);
        file1Event.setOutcome(file1.getReferenceURI() + " " + "resolved");
        events.addEvent(id, file1Event);

        DcsEvent file2Event = events.newEvent(Events.FILE_RESOLUTION_STAGED);
        file2Event.setOutcome(file2.getReferenceURI() + " " + "resolved");
        events.addEvent(id, file2Event);

        cleanup.execute(id);

        Assert.assertNull(ingest.getSipStager().getSIP(file1.getSipRef()));
        Assert.assertNull(ingest.getSipStager().getSIP(file2.getSipRef()));
    }
}
