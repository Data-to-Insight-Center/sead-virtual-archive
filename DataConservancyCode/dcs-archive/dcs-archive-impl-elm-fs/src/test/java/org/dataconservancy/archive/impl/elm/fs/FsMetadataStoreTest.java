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

import java.util.UUID;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.dataconservancy.archive.impl.elm.MetadataStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.archive.impl.elm.test.MetadataStoreTest;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;

public class FsMetadataStoreTest
        extends MetadataStoreTest {

    private static File baseDir;

    @BeforeClass
    public static void getTempDir() {
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());
    }

    @Override
    protected MetadataStore getMetadataStore() {
        FsMetadataStore mStore = new FsMetadataStore();
        mStore.setBaseDir(baseDir.toString());
        mStore.setFilePathKeyAlgorithm(new KeyDigestPathAlgorithm("MD5",
                                                                 1,
                                                                 1,
                                                                 ".md.csv"));
        return mStore;
    }

    @AfterClass
    public static void removeTestDir() throws Exception {
        FileUtils.deleteDirectory(baseDir);
    }

}
