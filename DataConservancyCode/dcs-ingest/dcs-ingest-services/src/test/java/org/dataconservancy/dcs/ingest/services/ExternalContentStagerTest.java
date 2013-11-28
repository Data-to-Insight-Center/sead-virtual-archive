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

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.ingest.file.impl.MemoryFileContentStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;

public class ExternalContentStagerTest {

    private static File baseDir;

    private static String sampleFilePath;

    private static IngestFramework ingest = new IngestFramework();

    @BeforeClass
    public static void setup() throws Exception {
        initFiles();

        BulkIdCreationService ids = new MemoryIdServiceImpl();
        SipStager stager = new MemoryStager();

        InlineEventManager mgr = new InlineEventManager();
        mgr.setIdService(ids);
        mgr.setSipStager(stager);

        ingest.setEventManager(mgr);
        ingest.setSipStager(stager);

        MemoryFileContentStager fcs = new MemoryFileContentStager();
        fcs.setSipStager(stager);

        ingest.setFileContentStager(fcs);
    }

    @Test
    public void fileUriTest() throws Exception {
        String origSrc = "";
        if( sampleFilePath.contains(":/")){
            origSrc = "file://" + sampleFilePath;
        }else {
            origSrc = "file:///" + sampleFilePath;
        }

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource(origSrc);
        file.setExtant(true);
        sip.addFile(file);
        String id = ingest.getSipStager().addSIP(sip);

        ExternalContentStager ecs = new ExternalContentStager();
        ecs.setIngestFramework(ingest);

        ecs.execute(id);

        sip = ingest.getSipStager().getSIP(id);
        DcsFile retrieved = sip.getFiles().iterator().next();
        Assert.assertTrue(origSrc != retrieved.getSource());
        Assert.assertTrue(ingest.getFileContentStager().contains(retrieved
                .getSource()));
    }

    @Test(expected = Exception.class)
    public void nonexistantUriTest() throws Exception {
        String origSrc = "file://" + sampleFilePath + ".notThere";

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource(origSrc);
        file.setExtant(true);
        sip.addFile(file);
        String id = ingest.getSipStager().addSIP(sip);

        ExternalContentStager ecs = new ExternalContentStager();
        ecs.setIngestFramework(ingest);

        ecs.execute(id);

        sip = ingest.getSipStager().getSIP(id);
        DcsFile retrieved = sip.getFiles().iterator().next();
        Assert.assertTrue(origSrc != retrieved.getSource());
        Assert.assertTrue(ingest.getFileContentStager().contains(retrieved
                .getSource()));
    }

    @Test
    public void untouchedNonExtantFileUriTest() throws Exception {
        String origSrc = "file://" + sampleFilePath;

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource(origSrc);
        file.setExtant(false);
        sip.addFile(file);
        String id = ingest.getSipStager().addSIP(sip);

        ExternalContentStager ecs = new ExternalContentStager();
        ecs.setIngestFramework(ingest);

        ecs.execute(id);

        sip = ingest.getSipStager().getSIP(id);
        DcsFile retrieved = sip.getFiles().iterator().next();
        Assert.assertEquals(origSrc, retrieved.getSource());
        Assert.assertFalse(ingest.getFileContentStager().contains(retrieved
                .getSource()));
    }

    @Test
    public void untouchedStagedFileTest() throws Exception {
        StagedFile staged =
                ingest.getFileContentStager()
                        .add(IOUtils.toInputStream("stagedContent"), null);
        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource(staged.getReferenceURI());
        file.setExtant(true);
        sip.addFile(file);
        String id = ingest.getSipStager().addSIP(sip);

        ExternalContentStager ecs = new ExternalContentStager();
        ecs.setIngestFramework(ingest);

        ecs.execute(id);

        sip = ingest.getSipStager().getSIP(id);
        DcsFile retrieved = sip.getFiles().iterator().next();
        Assert.assertEquals(staged.getReferenceURI(), retrieved.getSource());

    }

    @Test
    public void downloadEventTest() throws Exception {
        String origSrc = "";
        if( sampleFilePath.contains(":/")){
            origSrc = "file://" + sampleFilePath;
        }else {
            origSrc = "file:///" + sampleFilePath;
        }

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource(origSrc);
        file.setExtant(true);
        sip.addFile(file);
        String id = ingest.getSipStager().addSIP(sip);

        ExternalContentStager ecs = new ExternalContentStager();
        ecs.setIngestFramework(ingest);

        ecs.execute(id);

        sip = ingest.getSipStager().getSIP(id);
        DcsFile retrieved = sip.getFiles().iterator().next();

        String stagedSipRef =
                ingest.getFileContentStager().get(retrieved.getSource())
                        .getSipRef();

        DcsFile stagedFile =
                ingest.getSipStager().getSIP(stagedSipRef).getFiles()
                        .iterator().next();

        DcsEvent upload =
                ingest.getEventManager().getEventByType(stagedSipRef,
                                                        Events.FILE_DOWNLOAD);
        Assert.assertNotNull(upload);

        for (DcsEntityReference er : upload.getTargets()) {
            Assert.assertEquals(stagedFile.getId(), er.getRef());
        }
    }

    @Test
    public void calculateFixityTest() throws Exception {
        String origSrc = "";
        if( sampleFilePath.contains(":/")){
            origSrc = "file://" + sampleFilePath;
        }else {
            origSrc = "file:///" + sampleFilePath;
        }

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("example:/file");
        file.setSource(origSrc);
        file.setExtant(true);
        sip.addFile(file);
        String id = ingest.getSipStager().addSIP(sip);

        ExternalContentStager ecs = new ExternalContentStager();
        ecs.setAlwaysCalculateFixityFor("SHA-1");
        ecs.setIngestFramework(ingest);

        ecs.execute(id);

        sip = ingest.getSipStager().getSIP(id);
        DcsFile retrieved = sip.getFiles().iterator().next();

        String stagedSipRef =
                ingest.getFileContentStager().get(retrieved.getSource())
                        .getSipRef();

        DcsFile stagedFile =
                ingest.getSipStager().getSIP(stagedSipRef).getFiles()
                        .iterator().next();

        Assert.assertEquals(1, stagedFile.getFixity().size());

        DcsEvent digestEvent =
                ingest.getEventManager().getEventByType(stagedSipRef,
                                                        Events.FIXITY_DIGEST);
        Assert.assertNotNull(digestEvent);

        for (DcsEntityReference er : digestEvent.getTargets()) {
            Assert.assertEquals(stagedFile.getId(), er.getRef());
        }

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
