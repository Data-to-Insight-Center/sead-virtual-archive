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
package org.dataconservancy.dcs.ingest.services.util;

import java.io.File;

import java.util.UUID;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.test.SipStagerTest;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;

public class ElmSipStagerTest
        extends SipStagerTest {

    private static ElmSipStager stager = new ElmSipStager();

    private static File baseDir;

    @BeforeClass
    public static void init() {
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());
        FilePathKeyAlgorithm eAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 1, null);
        FilePathKeyAlgorithm mAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 1, ".md");

        FsEntityStore eStore = new FsEntityStore();
        eStore.setBaseDir(baseDir.getAbsolutePath());
        eStore.setFilePathKeyAlgorithm(eAlg);
        stager.setEntityStore(eStore);

        FsMetadataStore mStore = new FsMetadataStore();
        mStore.setBaseDir(baseDir.getAbsolutePath());
        mStore.setFilePathKeyAlgorithm(mAlg);
        stager.setMetadataStore(mStore);

        stager.setIdentifierService(new MemoryIdServiceImpl());
        stager.setModelBuilder(new DcsXstreamStaxModelBuilder());
    }

    @Test
    public void finishTest() {
        String id = stager.addSIP(new Dcp());
        stager.retire(id);
        Assert.assertNull(stager.getSIP(id));
        Assert.assertFalse(stager.getKeys().contains(id));
    }

    @Test
    public void noDeleteTest() {
        stager.setDeleteUponRetire(false);
        String id = stager.addSIP(new Dcp());
        stager.retire(id);
        Assert.assertNotNull(stager.getSIP(id));
        Assert.assertTrue(stager.getKeys().contains(id));
        stager.setDeleteUponRetire(true);
    }

    protected SipStager getSipStager() {
        return stager;
    }

    @AfterClass
    public static void removeTestDir() throws Exception {
        FileUtils.deleteDirectory(baseDir);
    }

}
