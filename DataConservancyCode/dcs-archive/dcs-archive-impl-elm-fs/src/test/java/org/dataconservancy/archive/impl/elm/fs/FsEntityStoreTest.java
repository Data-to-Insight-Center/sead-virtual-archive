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
import java.io.FileInputStream;

import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.test.EntityStoreTest;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;

public class FsEntityStoreTest
        extends EntityStoreTest {

    private static File baseDir;

    @BeforeClass
    public static void getTempDir() {
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void filesInDirectoryTest() throws Exception {
        final String ID = "id";
        FsEntityStore eStore = getEntityStore();
        String content = "<filesInDirectoryTest />";
        eStore.put(ID, IOUtils.toInputStream(content));

        Iterator<File> fileIterator =
                FileUtils.iterateFiles(new File(eStore.getBaseDir()),
                                       null,
                                       true);
        boolean found = false;

        /* Brute force crawl directory structure to find our content */
        while (fileIterator.hasNext()) {
            if (IOUtils.toString(new FileInputStream(fileIterator.next()))
                    .equals(content)) {
                found = true;

            }
        }

        Assert.assertTrue(found);
    }

    protected FsEntityStore getEntityStore() {
        FsEntityStore store = new FsEntityStore();
        store.setBaseDir(baseDir.toString());
        store.setFilePathKeyAlgorithm(new KeyDigestPathAlgorithm("MD5",
                                                                1,
                                                                1,
                                                                null));
        return store;
    }

    @AfterClass
    public static void removeTestDir() throws Exception {
        FileUtils.deleteDirectory(baseDir);
    }
}
