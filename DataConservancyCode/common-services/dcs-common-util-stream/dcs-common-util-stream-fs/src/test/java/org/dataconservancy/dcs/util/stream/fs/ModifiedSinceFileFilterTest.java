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
package org.dataconservancy.dcs.util.stream.fs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ModifiedSinceFileFilterTest {
    private static final long ONE_SECOND = 1000;
    private static final Calendar CLASS_INSTANTIATED_AT = Calendar.getInstance();

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = createTemporaryDirectory();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(tempDir);
    }

    @Test
    public void testModifiedSinceAfter() throws IOException, InterruptedException {
        final File tempFile = new File(tempDir, "a file");
        FileUtils.touch(tempFile);
        log.debug("Created temp file {}", tempFile);
        Thread.sleep(ONE_SECOND);
        assertTrue(FileUtils.listFiles(tempDir, new ModifiedSinceFileFilter(Calendar.getInstance()), TrueFileFilter.TRUE).isEmpty());
    }

    @Test
    public void testModifiedSinceAfterRecurseDirectories() throws IOException, InterruptedException {
        final File tempDir = createTemporaryDirectory(this.tempDir);
        final File tempFile = new File(tempDir, "a file");
        log.debug("Created temp file {}", tempFile);
        FileUtils.touch(tempFile);
        Thread.sleep(ONE_SECOND);
        assertTrue(FileUtils.listFiles(tempDir, new ModifiedSinceFileFilter(Calendar.getInstance()), TrueFileFilter.TRUE).isEmpty());
    }

    @Test
    public void testModifiedSinceBefore() throws IOException, InterruptedException {
        final File tempFile = new File(tempDir, "a file");
        FileUtils.touch(tempFile);
        log.debug("Created temp file {}", tempFile);
        final Iterator files = FileUtils.listFiles(tempDir, new ModifiedSinceFileFilter(CLASS_INSTANTIATED_AT), TrueFileFilter.TRUE).iterator();
        int count = 0;
        while (files.hasNext()) {
            count++;
            files.next();
        }
        assertEquals(1, count);
    }

    @Test
    public void testModifiedSinceBeforeRecurseDirectories() throws IOException, InterruptedException {
        final File tempDir = createTemporaryDirectory(this.tempDir);
        final File tempFile = new File(tempDir, "a file");
        FileUtils.touch(tempFile);
        log.debug("Created temp file {}", tempFile);
        final Iterator files = FileUtils.listFiles(tempDir, new ModifiedSinceFileFilter(CLASS_INSTANTIATED_AT), TrueFileFilter.TRUE).iterator();
        int count = 0;
        while (files.hasNext()) {
            count++;
            files.next();
        }
        assertEquals(1, count);
    }

    private File createTemporaryDirectory() throws IOException {
        final File tempFile = File.createTempFile("ModifiedSinceFileFilterTest", ".dir");
        tempFile.delete();
        FileUtils.forceMkdir(tempFile);
        log.debug("Created temporary directory {}", tempFile);
        return tempFile;
    }

    private File createTemporaryDirectory(File parent) throws IOException {
        File tempFile = File.createTempFile("ModifiedSinceFileFilterTest", ".dir");
        tempFile.delete();
        tempFile = new File(parent, tempFile.getName());
        FileUtils.forceMkdir(tempFile);
        log.debug("Created temporary directory {}", tempFile);
        return tempFile;
    }
}
