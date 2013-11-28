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
package org.dataconservancy.dcs.ingest.client.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;

import static org.dataconservancy.dcs.ingest.client.impl.MockFileDepositManager.FILE_SRC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DualManagerDepositTest {

    private static MockFileDepositManager fileManager =
            new MockFileDepositManager();

    private static File baseDir;

    private static String sampleFilePath;

    private static final String sampleFileMime = "image/png";

    private static final String algorithm = "SHA-1";

    private static final String simpleDcpManifestationId =
            "example:/manifestation";

    @BeforeClass
    public static void createSampleFile() throws IOException {
        final String fileName = "file.1.0.png";
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());

        File file = new File(baseDir, fileName);
        sampleFilePath = file.getAbsolutePath();
        sampleFilePath = sampleFilePath.replace('\\', '/');

        OutputStream out = FileUtils.openOutputStream(file);
        InputStream in =
                DualManagerDepositTest.class
                        .getResourceAsStream("/" + fileName);
        IOUtils.copy(in, out);
        out.close();
    }

    @AfterClass
    public static void removeTestDir() throws Exception {
        System.gc();
        FileUtils.deleteDirectory(baseDir);        
    }

    @Test
    public void uploadedFileReturnedInfoTest() {

        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);

        examineUploadedFile(deposit.uploadFile(sampleFilePath));
    }

    @Test
    public void uploadedMetadataReturnedInfoTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);
        examineUploadedFile(deposit.uploadMetadata(sampleFilePath,
                                                   simpleDcpManifestationId));

    }

    @Test
    public void uploadedFileMutabilityTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);

        DcsFile file = deposit.uploadFile(sampleFilePath);

        file.setExtant(true);

        Assert.assertTrue(deposit.getSip().getFiles().iterator().next()
                .isExtant());

        file.setExtant(false);

        Assert.assertFalse(deposit.getSip().getFiles().iterator().next()
                .isExtant());
    }

    @Test
    public void uploadedFileSentInfoTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);

        deposit.uploadFile(sampleFilePath);

        Map<String, String> sentMetadata =
                fileManager.getLastDepositInfo().getMetadata();

        assertEquals(sampleFileMime, sentMetadata
                .get(HttpHeaderUtil.CONTENT_TYPE));

        assertTrue(HttpHeaderUtil.getDigests(sentMetadata)
                .containsKey(algorithm));

        assertTrue(sentMetadata.containsKey(HttpHeaderUtil.CONTENT_DISPOSITION));

        assertTrue(sentMetadata.containsKey(HttpHeaderUtil.CONTENT_LENGTH));

    }

    @Test
    public void referenceFileInfoReturnedTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(), null, null, null, null);
        DcsFile file = deposit.referenceFile(sampleFilePath);

        assertTrue(file.getSource().contains(sampleFilePath));

        assertNotNull(file.getName());
    }

    @Test
    public void referenceMetadataFileInfoReturnedTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(), null, null, null, null);
        DcsFile file =
                deposit.referenceMetadata(sampleFilePath,
                                          simpleDcpManifestationId);

        assertTrue(file.getSource().contains(sampleFilePath));

        assertNotNull(file.getName());
    }

    @Test
    public void uploadFileDefaultManifestationLinksTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);
        DcsFile file = deposit.uploadFile(sampleFilePath);

        DcsManifestation manifestation =
                deposit.getSip().getManifestations().iterator().next();

        assertEquals(1, manifestation.getManifestationFiles().size());

        for (DcsManifestationFile dmf : manifestation.getManifestationFiles()) {
            assertEquals(file.getId(), dmf.getRef().getRef());
        }
    }

    @Test
    public void uploadFileSpecificManifestationLinksTest() {

        final String ID = "my:/id";
        Dcp initial = getSimpleDcp();
        DcsManifestation myManifestation = new DcsManifestation();
        myManifestation.setId(ID);
        initial.addManifestation(myManifestation);

        DualManagerDeposit deposit =
                new DualManagerDeposit(initial,
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);

        DcsFile file = deposit.uploadFile(sampleFilePath, ID);

        for (DcsManifestation manifestation : deposit.getSip()
                .getManifestations()) {
            if (manifestation.getId().equals(ID)) {
                assertEquals(1, manifestation.getManifestationFiles().size());
                for (DcsManifestationFile dmf : manifestation
                        .getManifestationFiles()) {
                    assertEquals(file.getId(), dmf.getRef().getRef());
                }
            } else {
                assertEquals(0, manifestation.getManifestationFiles().size());
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void uploadFileNonexistantManifestationTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);
        deposit.uploadFile(sampleFilePath, "ain't in here");
    }

    @Test
    public void referenceFileDefaultManifestationTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);
        DcsFile file = deposit.referenceFile(sampleFilePath);

        DcsManifestation manifestation =
                deposit.getSip().getManifestations().iterator().next();

        assertEquals(1, manifestation.getManifestationFiles().size());

        for (DcsManifestationFile dmf : manifestation.getManifestationFiles()) {
            assertEquals(file.getId(), dmf.getRef().getRef());
        }
    }

    @Test
    public void referenceFileSpecificManifestationTest() {
        final String ID = "my:/id";
        Dcp initial = getSimpleDcp();
        DcsManifestation myManifestation = new DcsManifestation();
        myManifestation.setId(ID);
        initial.addManifestation(myManifestation);

        DualManagerDeposit deposit =
                new DualManagerDeposit(initial,
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);

        DcsFile file = deposit.referenceFile(sampleFilePath, ID);

        for (DcsManifestation manifestation : deposit.getSip()
                .getManifestations()) {
            if (manifestation.getId().equals(ID)) {
                assertEquals(1, manifestation.getManifestationFiles().size());
                for (DcsManifestationFile dmf : manifestation
                        .getManifestationFiles()) {
                    assertEquals(file.getId(), dmf.getRef().getRef());
                }
            } else {
                assertEquals(0, manifestation.getManifestationFiles().size());
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void referenceFileNonexistantManifestationTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       null,
                                       algorithm);
        deposit.referenceFile(sampleFilePath, "ain't in here");
    }

    @Test
    public void uploadMetadataLinksTest() {

        Dcp sip = getSimpleDcp();
        DcsManifestation noMetadata = new DcsManifestation();
        noMetadata.setId("ain't got no metadata");
        sip.addManifestation(noMetadata);

        DualManagerDeposit deposit =
                new DualManagerDeposit(sip, null, null, fileManager, algorithm);
        DcsFile file =
                deposit
                        .uploadMetadata(sampleFilePath,
                                        simpleDcpManifestationId);

        for (DcsManifestation man : deposit.getSip().getManifestations()) {
            if (man.getId().equals(simpleDcpManifestationId)) {
                assertEquals(1, man.getMetadataRef().size());

                for (DcsMetadataRef dmr : man.getMetadataRef()) {
                    assertEquals(file.getId(), dmr.getRef());
                }
            } else {
                assertEquals(0, man.getMetadataRef().size());
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void uploadMetadataNonExistantEntityTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       fileManager,
                                       algorithm);
        deposit.uploadFile(sampleFilePath, "this does not exist!");
    }

    @Test
    public void referenceMetadataLinksTest() {
        Dcp sip = getSimpleDcp();
        DcsManifestation noMetadata = new DcsManifestation();
        noMetadata.setId("ain't got no metadata");
        sip.addManifestation(noMetadata);

        DualManagerDeposit deposit =
                new DualManagerDeposit(sip, null, null, null, algorithm);
        DcsFile file =
                deposit.referenceMetadata(sampleFilePath,
                                          simpleDcpManifestationId);

        for (DcsManifestation man : deposit.getSip().getManifestations()) {
            if (man.getId().equals(simpleDcpManifestationId)) {
                assertEquals(1, man.getMetadataRef().size());

                for (DcsMetadataRef dmr : man.getMetadataRef()) {
                    assertEquals(file.getId(), dmr.getRef());
                }
            } else {
                assertEquals(0, man.getMetadataRef().size());
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void referenceMetadataNonexistantEntityTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       null,
                                       algorithm);

        deposit.referenceMetadata(sampleFilePath, "Ths doesn't exist");
    }

    @Test
    public void addEmptyEntityTest() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(new Dcp(), null, null, null, algorithm);
        DcsManifestation man = deposit.add(new DcsManifestation());
        assertNotNull(man.getId());

        assertEquals(1, deposit.getSip().getManifestations().size());
    }

    @Test
    public void testPopulatedEntity() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(new Dcp(), null, null, null, algorithm);

        DcsManifestation man = new DcsManifestation();
        man.setId(simpleDcpManifestationId);

        DcsManifestation added = deposit.add(man);

        assertEquals(simpleDcpManifestationId, added.getId());

        assertEquals(1, deposit.getSip().getManifestations().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConflictingEntity() {
        DualManagerDeposit deposit =
                new DualManagerDeposit(getSimpleDcp(),
                                       null,
                                       null,
                                       null,
                                       algorithm);
        DcsManifestation man = new DcsManifestation();
        man.setId(simpleDcpManifestationId);

        deposit.add(man);
    }

    private void examineUploadedFile(DcsFile file) {
        assertEquals(FILE_SRC, file.getSource());

        boolean foundFixity = false;
        for (DcsFixity fx : file.getFixity()) {
            if (fx.getAlgorithm().equals(algorithm)) {
                foundFixity = true;
                assertNotNull(fx.getValue());
            }
        }
        assertTrue("fixity was not found", foundFixity);

        boolean foundMime = false;
        for (DcsFormat format : file.getFormats()) {
            if (format.getSchemeUri()
                    .equals("http://www.iana.org/assignments/media-types/")
                    && format.getFormat().equals(sampleFileMime)) {
                foundMime = true;
            }
        }
        assertTrue("Did not detect or repost mime type", foundMime);

        assertTrue(file.getSizeBytes() > 0);

        assertTrue(file.isExtant());

        assertNotNull(file.getName());
    }

    private Dcp getSimpleDcp() {
        Dcp dcp = new Dcp();
        DcsManifestation mf = new DcsManifestation();
        mf.setId(simpleDcpManifestationId);
        dcp.addManifestation(mf);
        return dcp;
    }
}
