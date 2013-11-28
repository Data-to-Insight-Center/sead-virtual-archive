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
package org.dataconservancy.dcs.util;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeyDigestPathAlgorithmTest
        extends FilePathAlgorithmTest {

    protected FilePathKeyAlgorithm getAlgorithm() {
        return new KeyDigestPathAlgorithm("MD5", 1, 1, null);
    }

    @Test
    public void keySpecificationTest() throws Exception {
        final String KEY = "key";
        FilePathKeyAlgorithm ka = getAlgorithm();
        FilePathSource source =
                ka.getPath(IOUtils.toInputStream("in"), KEY, null);

        assertEquals(KEY, source.getPathKey());

        assertEquals(source.getPathName(), ka.lookupPathName(KEY));

        InputStream stream = source.getInputStream();
        while (stream.read() != -1);

        assertEquals(KEY, source.getPathKey());
    }

    @Test
    public void suffixTest() throws Exception {
        final String SUFFIX = ".dat";
        FilePathKeyAlgorithm ka =
                new KeyDigestPathAlgorithm("SHA1", 1, 1, SUFFIX);
        assertTrue(ka.getPath(null, "key", null).getPathName().endsWith(SUFFIX));
    }
    
    @Test
    public void emptySuffixStringTest() throws Exception {
        final String SUFFIX = "";
        FilePathKeyAlgorithm ka =
                new KeyDigestPathAlgorithm("SHA1", 1, 1, SUFFIX);
        assertFalse(ka.getPath(null, "key", null).getPathName().contains("."));
    }

    @Test
    public void directoryDimensionsTest() throws Exception {
        final int WIDTH = 2;
        final int DEPTH = 3;
        final int DIRNAME_LENGTH = 2 * WIDTH;
        FilePathKeyAlgorithm md5 =
                new KeyDigestPathAlgorithm("MD5", WIDTH, DEPTH, null);
        String path = md5.getPath(null, "id", null).getPathName();

        for (int level = 0; level < DEPTH; level++) {
            int offset = (DIRNAME_LENGTH * level) + level;
            assertFalse(path.substring(offset, offset + DIRNAME_LENGTH)
                    .contains(File.separator));
            assertEquals(File.separatorChar, path.charAt(offset
                    + DIRNAME_LENGTH));
        }
    }
}
