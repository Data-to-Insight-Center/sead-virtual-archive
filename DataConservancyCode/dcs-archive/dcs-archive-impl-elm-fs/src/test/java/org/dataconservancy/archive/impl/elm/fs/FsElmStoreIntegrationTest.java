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
package org.dataconservancy.archive.impl.elm.fs;

import java.io.File;
import java.io.InputStream;

import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.ExtendedFullDipTreeLogic;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;

public class FsElmStoreIntegrationTest {

    private static final String EXAMPLE_FILE =
            "/org/dataconservancy/archive/impl/elm/test/ManyRelationships.xml";

    private static File baseDir;

    private static ElmArchiveStore store;

    @Test
    public void smokeTest() throws Exception {
        InputStream dcp = this.getClass().getResourceAsStream(EXAMPLE_FILE);
        Assert.assertNotNull(dcp);

        store.putPackage(dcp);

        IOUtils.toString(store.getPackage("example:/DeliverableUnit/child"));

        IOUtils
                .toString(store
                        .getFullPackage("example:/DeliverableUnit/child"));

        IOUtils.toString(store.getPackage("example:/File/Content/1",
                                          new ExtendedFullDipTreeLogic()));

        Iterator<String> entities =
                store.listEntities(EntityType.DELIVERABLE_UNIT);

        Assert.assertTrue(entities.hasNext());

    }

    @BeforeClass
    public static void init() {
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());

        store = new ElmArchiveStore();

        FilePathKeyAlgorithm eAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 1, null);
        FilePathKeyAlgorithm mAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 1, ".md");

        FsEntityStore eStore = new FsEntityStore();
        eStore.setBaseDir(baseDir.getAbsolutePath());
        eStore.setFilePathKeyAlgorithm(eAlg);
        store.setEntityStore(eStore);

        FsMetadataStore mStore = new FsMetadataStore();
        mStore.setBaseDir(baseDir.getAbsolutePath());
        mStore.setFilePathKeyAlgorithm(mAlg);
        store.setMetadataStore(mStore);

        store.init();
    }

    @AfterClass
    public static void removeTestDir() throws Exception {
        FileUtils.deleteDirectory(baseDir);
    }
}
