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

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.ingest.file.impl.FileSystemContentStager.FsStagedFile;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.util.FileContentDigestPathAlgorithm;
import org.dataconservancy.dcs.util.FilePathAlgorithm;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileSystemContentStagerTest
        extends FileContentStagerTest {

    private final String CONTENT = this.getClass().getName();

    private final String CONTENT2 = this.getClass().getName() + "2";

    private static FilePathAlgorithm algorithm =
            new FileContentDigestPathAlgorithm("MD5", 1, 2, true);

    private static FileSystemContentStager fsStager =
            new FileSystemContentStager();

    private static SipStager sipStager = new MemoryStager();

    @BeforeClass
    public static void init() {
        fsStager.setBaseDir(System.getProperty("java.io.tmpdir"));
        fsStager.setPathAlgorithm(algorithm);
        fsStager.setSipStager(sipStager);
    }

    @Test
    public void accessFileUriTest() throws Exception {
        StagedFile file = fsStager.add(IOUtils.toInputStream(CONTENT), null);
        assertTrue(file.getAccessURI().startsWith("file://"));

        String path = file.getAccessURI().replace("file://", "");
        assertTrue(new File(path).exists());
        assertEquals(CONTENT, IOUtils.toString(new FileInputStream(path)));

        fsStager.remove(file.getReferenceURI());
    }

    @Test
    public void defaultDeletionFileTest() throws Exception {
        File deletefile = fsStager.getDeletionFile();
        if (deletefile.exists()) {
            deletefile.delete();
        }
        FsStagedFile file =
                (FsStagedFile) fsStager.add(IOUtils.toInputStream(CONTENT),
                                            null);
        FsStagedFile file2 =
                (FsStagedFile) fsStager.add(IOUtils.toInputStream(CONTENT2),
                                            null);
        fsStager.remove(file.getReferenceURI());
        fsStager.remove(file2.getReferenceURI());

        assertTrue(deletefile.exists());
        String deleteContent =
                IOUtils.toString(new FileInputStream(deletefile));
        assertTrue(deleteContent.contains(file.getPath()));
        assertTrue(deleteContent.contains(file2.getPath()));
        assertEquals(2, deleteContent.split("\n").length);
    }

    @Test
    public void finishBehaviourTest() throws Exception {
        StagedFile staged = fsStager.add(IOUtils.toInputStream(CONTENT), null);
        File file = new File(staged.getAccessURI().replace("file://", ""));

        Assert.assertNotNull(sipStager.getSIP(staged.getSipRef()));
        assertTrue(file.exists());

        fsStager.retire(staged.getReferenceURI());

        Assert.assertNull(sipStager.getSIP(staged.getSipRef()));
        assertTrue(file.exists());
    }

    protected FileContentStager getStager() {
        return fsStager;
    }

    protected SipStager getSipStager() {
        return sipStager;
    }
}
