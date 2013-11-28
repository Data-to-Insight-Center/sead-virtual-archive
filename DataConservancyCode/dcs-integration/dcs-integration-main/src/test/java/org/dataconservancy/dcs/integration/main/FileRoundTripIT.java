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
package org.dataconservancy.dcs.integration.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.ingest.client.DepositBuilder;
import org.dataconservancy.dcs.ingest.client.DepositClient;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FileRoundTripIT {

    private static final Logger log =
            LoggerFactory.getLogger(FileRoundTripIT.class);

    private static File baseDir;

    private static DepositClient depositClient;

    private static ArchiveStore archive;

    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private static Dcp template;

    private static String sampleFilePath;

    private static String templateManifestationId = "example:/man";

    private static String templateDeliverableUnitId = "example:/du";

    @BeforeClass
    public static void createSampleFile() throws IOException {
        initFiles();

        ClassPathXmlApplicationContext appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "depositClientContext.xml", "classpath*:org/dataconservancy/config/applicationContext.xml"});
        depositClient = (DepositClient) appContext.getBean("depositClient");
        archive = (ArchiveStore) appContext.getBean("org.dataconservancy.archive.api.ArchiveStore");

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(templateDeliverableUnitId);
        du.setTitle("title");

        DcsManifestation man = new DcsManifestation();
        man.setId(templateManifestationId);
        man.setDeliverableUnit(templateDeliverableUnitId);

        template = new Dcp();
        template.addDeliverableUnit(du);
        template.addManifestation(man);
    }

    @Test
    public void uploadFileRoundTripTest() throws Exception {

        DepositBuilder deposit = depositClient.buildDeposit(template);
        deposit.uploadFile(sampleFilePath);

        debugDepositSip(deposit);

        String id = tryAndVerify(deposit);
        Dcp retrieved = buildDcpFromSipInfo(id);

        Assert.assertEquals(1, retrieved.getFiles().size());

        DcsFile file = retrieved.getFiles().iterator().next();

        byte[] archivedBytes = getFileBytesFromArchive(file.getId());

        File origFile = new File(sampleFilePath);
        Assert.assertEquals(archivedBytes.length, origFile.length());

        /* file source and original location should not resemble each other */
        Assert.assertFalse(file.getSource().contains(sampleFilePath));
    }

    @Test
    public void referencedLocalNonExtantFileRoundTripTest() throws Exception {
        DepositBuilder deposit = depositClient.buildDeposit(template);
        DcsFile file = deposit.referenceFile(sampleFilePath);
        file.setExtant(false);

        debugDepositSip(deposit);

        String id = tryAndVerify(deposit);
        Dcp retrieved = buildDcpFromSipInfo(id);

        Assert.assertEquals(1, retrieved.getFiles().size());

        DcsFile retrievedFile = retrieved.getFiles().iterator().next();

        /* archived vs original file should have the same length */
        byte[] archivedBytes = getFileBytesFromArchive(retrievedFile.getId());
        File origFile = new File(sampleFilePath);
        Assert.assertEquals(archivedBytes.length, origFile.length());

        /*
         * A non-extant file should just point to the original location, perhaps
         * with a file://uri
         */
        Assert.assertTrue(file.getSource().contains(sampleFilePath));
    }

    @Test
    public void referencedLocalExtantFileRoundTripTest() throws Exception {
        DepositBuilder deposit = depositClient.buildDeposit(template);
        DcsFile file = deposit.referenceFile(sampleFilePath);
        file.setExtant(true);

        debugDepositSip(deposit);
        
        String id = tryAndVerify(deposit);
        Dcp retrieved = buildDcpFromSipInfo(id);

        Assert.assertEquals(1, retrieved.getFiles().size());

        DcsFile retrievedFile = retrieved.getFiles().iterator().next();

        /* archived vs original file should have the same length */
        byte[] archivedBytes = getFileBytesFromArchive(retrievedFile.getId());
        File origFile = new File(sampleFilePath);
        Assert.assertEquals(archivedBytes.length, origFile.length());

        /* file source and original location should not resemble each other */
        Assert.assertFalse(retrievedFile.getSource().contains(sampleFilePath));
    }

    @Ignore
    @Test
    public void perfTestUpload() {
        long start = new Date().getTime();
        for (int i = 0; i < 1000; i++) {
            DepositBuilder deposit = depositClient.buildDeposit(template);
            deposit.uploadFile(sampleFilePath);
            deposit.execute();

        }
        long end = new Date().getTime();

        long elapsed = (end - start) / (1000 * 1000);

        log.info(String.format("Upload: %s sips per second"), elapsed);
    }

    @Ignore
    @Test
    public void perfTestReferenced() {
        long start = new Date().getTime();
        for (int i = 0; i < 1000; i++) {
            DepositBuilder deposit = depositClient.buildDeposit(template);
            DcsFile file = deposit.referenceFile(sampleFilePath);
            file.setExtant(false);
            deposit.execute();
        }
        long end = new Date().getTime();

        long elapsed = (end - start) / (1000 * 1000);

        log.info(String.format("Referenced Non Extant: %s sips per second"),
                 elapsed);
    }

    @Ignore
    @Test
    public void perfTestExtant() {
        long start = new Date().getTime();
        for (int i = 0; i < 1000; i++) {
            DepositBuilder deposit = depositClient.buildDeposit(template);
            DcsFile file = deposit.referenceFile(sampleFilePath);
            file.setExtant(true);
            deposit.execute();
        }
        long end = new Date().getTime();

        long elapsed = (end - start) / (1000 * 1000);

        log.info(String
                .format("Referenced extant: %s sips per second", elapsed));
    }

    private static void initFiles() throws IOException {
        final String fileName = "file.1.0.png";
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());

        File file = new File(baseDir, fileName);
        sampleFilePath = file.getAbsolutePath();
        sampleFilePath = sampleFilePath.replace('\\', '/');
        OutputStream out = FileUtils.openOutputStream(file);
        InputStream in =
                FileRoundTripIT.class.getResourceAsStream("/" + fileName);
        IOUtils.copy(in, out);
        in.close();
        out.close();
    }

    private String tryAndVerify(DepositBuilder deposit)
            throws InterruptedException {

        String id = deposit.execute();

        DepositInfo myDeposit = depositClient.getDepositInfo(id);

        int tries = 0;
        while (!myDeposit.hasCompleted() && tries < 50) {
            tries++;
            Thread.sleep(1000);
            myDeposit = depositClient.getDepositInfo(id);
        }

        Assert.assertTrue(myDeposit.hasCompleted());
        log.info("Completed ingest " + myDeposit.getDepositID());

        Assert.assertTrue(myDeposit.isSuccessful());

        return id;
    }

    private Dcp buildDcpFromSipInfo(String id) throws Exception {
        InputStream stream =
                depositClient.getDepositInfo(id).getDepositContent()
                        .getInputStream();
        Dcp retrieved;
        try {
            retrieved = builder.buildSip(stream);
        } finally {
            stream.close();
        }
        return retrieved;
    }

    private byte[] getFileBytesFromArchive(String fileid) throws Exception {
        InputStream archivedFileContent = archive.getContent(fileid);
        byte[] bytes = IOUtils.toByteArray(archivedFileContent);
        archivedFileContent.close();
        return bytes;
    }

    private void debugDepositSip(DepositBuilder deposit) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        builder.buildSip(deposit.getSip(), os);
        log.info("Submitting the following sip for deposit: \n{}\n", IOUtils.toString(new ByteArrayInputStream(os.toByteArray())));
    }

    @AfterClass
    public static void cleanUp() {
        FileUtils.deleteQuietly(baseDir);
    }
}
