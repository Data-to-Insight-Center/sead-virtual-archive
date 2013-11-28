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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class FilesystemStreamSourceTest {
    private static final Calendar CLASS_INSTANTIATED_AT = Calendar.getInstance();
    private static final int expectedCount = 5;
    private File tmpDir;
    private File exampleFile;
    private final int ONE_SECOND = 1000;

    @Before
    public void setUp() throws IOException {
        tmpDir = createTemporaryDirectory();
        for (int i = 0; i < expectedCount; i++) {
            exampleFile = new File(tmpDir, String.valueOf(i));
            FileUtils.touch(exampleFile);
        }
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(tmpDir);
    }

    @Test
    public void testStreams() throws Exception {
        final FilesystemStreamSource underTest = new FilesystemStreamSource(tmpDir);
        int count = 0;
        for (String streamId : underTest.streams()) {
            count++;
        }
        assertEquals(expectedCount, count);

        // Add a file and reset the count
        FileUtils.touch(new File(tmpDir, String.valueOf(expectedCount+1)));
        count = 0;
        for (String streamId : underTest.streams()) {
            count++;
        }
        assertEquals(expectedCount+1, count);
    }

    @Test
    public void testStreamsModifiedSinceNow() throws Exception {
        Thread.sleep(ONE_SECOND);
        final Calendar methodStart = Calendar.getInstance();
        final FilesystemStreamSource underTest = new FilesystemStreamSource(tmpDir);
        int count = 0;
        for (String streamId : underTest.streams(methodStart)) {
            count++;
        }
        assertEquals(0, count);

        // Sleep for a second
        Thread.sleep(ONE_SECOND);

        // Add a file and reset the count
        FileUtils.touch(new File(tmpDir, String.valueOf(expectedCount+1)));
        count = 0;

        for (String streamId : underTest.streams(methodStart)) {
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testStreamsModifiedSinceStart() throws Exception {
        final Calendar methodStart = Calendar.getInstance();
        final FilesystemStreamSource underTest = new FilesystemStreamSource(tmpDir);
        int count = 0;
        for (String streamId : underTest.streams(CLASS_INSTANTIATED_AT)) {
            count++;
        }
        assertEquals(expectedCount, count);

        // Add a file and reset the count
        FileUtils.touch(new File(tmpDir, String.valueOf(expectedCount+1)));
        count = 0;
        for (String streamId : underTest.streams(CLASS_INSTANTIATED_AT)) {
            count++;
        }
        assertEquals(expectedCount+1, count);
    }

    @Test
    public void testGetRelativeFile() throws Exception {
        final FilesystemStreamSource underTest = new FilesystemStreamSource(tmpDir);
        // get a file by its full path
        assertNotNull(underTest.getStream(exampleFile.getAbsolutePath()));
        // get the same file relative to the base directory of the stream source
        assertNotNull(underTest.getStream(exampleFile.getName()));
    }

//    @Test
//    public void testGetStream() throws Exception {
//
//    }

    private File createTemporaryDirectory() throws IOException {
        final File tempFile = File.createTempFile("FilesystemStreamSourceTest", ".dir");
        tempFile.delete();
        FileUtils.forceMkdir(tempFile);
        return tempFile;
    }
}
