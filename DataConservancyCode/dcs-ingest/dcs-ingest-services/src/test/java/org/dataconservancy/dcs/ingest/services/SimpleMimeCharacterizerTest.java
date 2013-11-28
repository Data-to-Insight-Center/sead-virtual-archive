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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;

import junit.framework.Assert;

public class SimpleMimeCharacterizerTest {

    private static File baseDir;

    private static String sampleFilePath;

    private static IngestFramework ingest = new IngestFramework();

    private static SimpleMimeCharacterizer mimeCharacterizer =
            new SimpleMimeCharacterizer();

    @BeforeClass
    public static void setUp() throws IOException {
        initFiles();

        ingest.setSipStager(new MemoryStager());
        InlineEventManager eventMgr = new InlineEventManager();
        eventMgr.setSipStager(ingest.getSipStager());
        eventMgr.setIdService(new MemoryIdServiceImpl());
        ingest.setEventManager(eventMgr);
        mimeCharacterizer.setIngestFramework(ingest);
    }

    @Test
    public void mimesAddedTest() throws IngestServiceException {
        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource("file://" + sampleFilePath);
        file.setName("test.txt");
        sip.addFile(file);

        String sipRef = ingest.getSipStager().addSIP(sip);
        mimeCharacterizer.execute(ingest.getSipStager().addSIP(sip));

        DcsFile retrieved =
                ingest.getSipStager().getSIP(sipRef).getFiles().iterator()
                        .next();

        Assert.assertTrue(retrieved.getFormats().size() > 0);

        Assert.assertEquals(1, ingest.getEventManager()
                .getEvents(sipRef, Events.CHARACTERIZATION_FORMAT).size());
    }

    @Test
    public void doNotAddExistingMimeTest() throws IngestServiceException {
        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource("file://" + sampleFilePath);
        file.setName("test.txt");
        sip.addFile(file);

        DcsFormat format = new DcsFormat();
        format.setSchemeUri(SimpleMimeCharacterizer.MIME_SCHEME);
        format.setFormat("text/plain");
        file.addFormat(format);

        String sipRef = ingest.getSipStager().addSIP(sip);
        mimeCharacterizer.execute(ingest.getSipStager().addSIP(sip));

        DcsFile retrieved =
                ingest.getSipStager().getSIP(sipRef).getFiles().iterator()
                        .next();

        Assert.assertEquals(1, retrieved.getFormats().size());

        Assert.assertEquals(0, ingest.getEventManager()
                .getEvents(sipRef, Events.CHARACTERIZATION_FORMAT).size());
    }

    @Test
    public void characterizationEventTest() throws IngestServiceException {
        final String fileid = "example:/file";
        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId(fileid);
        file.setSource("file://" + sampleFilePath);
        file.setName("test.txt");
        sip.addFile(file);

        String sipRef = ingest.getSipStager().addSIP(sip);
        mimeCharacterizer.execute(ingest.getSipStager().addSIP(sip));

        DcsEvent characterizationEvent =
                ingest.getEventManager()
                        .getEventByType(sipRef, Events.CHARACTERIZATION_FORMAT);
        Assert.assertEquals(1, characterizationEvent.getTargets().size());
        for (DcsEntityReference er : characterizationEvent.getTargets()) {
            Assert.assertEquals(fileid, er.getRef());
        }
        DcsFile retrieved =
                ingest.getSipStager().getSIP(sipRef).getFiles().iterator()
                        .next();
        for (DcsFormat fmt : retrieved.getFormats()) {
            Assert.assertTrue(characterizationEvent.getOutcome().contains(fmt
                    .getFormat()));
        }
    }

    @Test
    public void defaultCharacterizationTest() throws IngestServiceException {
        final String fileid = "example:/file";
        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId(fileid);
        file.setSource("file://" + sampleFilePath);

        /*
         * The file name and the file content don't give a clue as to its real
         * type, so we should fall back to the default
         */
        file.setName("test");
        sip.addFile(file);

        String sipRef = ingest.getSipStager().addSIP(sip);
        mimeCharacterizer.execute(ingest.getSipStager().addSIP(sip));

        DcsFile retrieved =
                ingest.getSipStager().getSIP(sipRef).getFiles().iterator()
                        .next();
        Assert.assertEquals(1, retrieved.getFormats().size());
        Assert.assertEquals("application/octet-stream", retrieved.getFormats()
                .iterator().next().getFormat());
    }

    private static void initFiles() throws IOException {
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());

        File file = new File(baseDir, "test.txt");
        sampleFilePath = file.getAbsolutePath();

        OutputStream out = FileUtils.openOutputStream(file);
        InputStream in = IOUtils.toInputStream("content!");
        IOUtils.copy(in, out);
        out.close();
    }

    @AfterClass
    public static void cleanUp() {
        FileUtils.deleteQuietly(baseDir);
    }
}
