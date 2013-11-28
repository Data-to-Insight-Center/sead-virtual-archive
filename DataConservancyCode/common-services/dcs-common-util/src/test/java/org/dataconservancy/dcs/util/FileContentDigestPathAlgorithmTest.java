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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;

public class FileContentDigestPathAlgorithmTest
        extends FilePathAlgorithmTest {

    private static final String FILE = "/file.txt";

    private static final String FILE_MD5 = "1e75d5298fd12184f34bad372a81b3e6";

    private static final String FILE_SHA1 =
            "428deee4e21cd468d81595597befdc9f21fdf8ee";

    @Test
    public void filenameMatchesCalculatedMd5HashTest() throws Exception {
        FilePathAlgorithm md5 =
                new FileContentDigestPathAlgorithm("MD5", 1, 1, true);
        FilePathSource source = prepareSource(md5, null);
        assertEquals("file name does not match hash", FILE_MD5, new File(source
                .getPathName()).getName());
    }

    @Test
    public void filenameMatchesCalculatedShaHashTest() throws Exception {
        FilePathAlgorithm sha =
                new FileContentDigestPathAlgorithm("SHA-1", 1, 1, true);
        FilePathSource source = prepareSource(sha, null);
        assertEquals("file name does not match hash",
                     FILE_SHA1,
                     new File(source.getPathName()).getName());
    }

    @Test
    public void directoryDimensionsTest() throws Exception {
        final int WIDTH = 2;
        final int DEPTH = 3;
        final int DIRNAME_LENGTH = 2 * WIDTH;
        FilePathAlgorithm md5 =
                new FileContentDigestPathAlgorithm("MD5", WIDTH, DEPTH, true);
        FilePathSource source = prepareSource(md5, null);
        String path = source.getPathName();

        for (int level = 0; level < DEPTH; level++) {
            int offset = (DIRNAME_LENGTH * level) + level;
            assertFalse(path.substring(offset, offset + DIRNAME_LENGTH)
                    .contains(File.separator));
            assertEquals(File.separatorChar, path.charAt(offset
                    + DIRNAME_LENGTH));
        }
    }

    @Test
    public void relevantContentHintTest() throws Exception {
        Map<String, String> hints = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("MD5", getBytes(FILE_MD5), hints);
        FileContentDigestPathAlgorithm alg =
                new FileContentDigestPathAlgorithm("MD5", 1, 1, true);

        /* Should get path name without using input stream */
        FilePathSource source = alg.getPath(null, hints);
        assertNotNull(source.getPathName());

        /*
         * If the hash is correct, this should be the same path name as the
         * calculated, unhinted version.
         */
        assertEquals(source.getPathName(), prepareSource(alg, null)
                .getPathName());
    }

    @Test
    public void hintPathKeyTest() throws Exception {
        Map<String, String> hints = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("MD5", getBytes(FILE_MD5), hints);
        FileContentDigestPathAlgorithm alg =
                new FileContentDigestPathAlgorithm("MD5", 1, 1, true);

        /* Should get path name without using input stream */
        FilePathSource source = alg.getPath(null, hints);
        assertNotNull(source.getPathName());

        Assert.assertTrue(source.getPathName().contains(source.getPathKey()));
    }

    @Test
    public void irrelevantHintTest() throws Exception {
        Map<String, String> hints = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("SHA1", getBytes(FILE_SHA1), hints);
        FileContentDigestPathAlgorithm alg =
                new FileContentDigestPathAlgorithm("MD5", 1, 1, true);

        /* Should ignore hint about irrelevant algorithm */
        FilePathSource source = alg.getPath(null, hints);
        assertNull(source.getPathName());
    }

    @Test
    public void wrongHintCalculatedTest() throws Exception {
        Map<String, String> hints = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("MD5", getBytes(FILE_SHA1), hints);
        FileContentDigestPathAlgorithm alg =
                new FileContentDigestPathAlgorithm("MD5", 1, 1, true);

        /* Should get path name without using input stream */
        FilePathSource source = alg.getPath(null, hints);
        assertNotNull(source.getPathName());

        /*
         * If the hash is in correct, and this a calculate path algorithm, this
         * should correct and this NOT equal to the pre-hinted version.
         */
        assertNotSame(source.getPathName(), prepareSource(alg, hints)
                .getPathName());
    }

    @Test
    public void wrongHintNotCalculatedTest() throws Exception {
        Map<String, String> hints = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("MD5", getBytes(FILE_SHA1), hints);
        FileContentDigestPathAlgorithm alg =
                new FileContentDigestPathAlgorithm("MD5", 1, 1, false);

        /* Should get path name without using input stream */
        FilePathSource source = alg.getPath(null, hints);
        assertNotNull(source.getPathName());

        /*
         * Since calculation is turned off, we will use the incorrect hash value
         */
        assertEquals(source.getPathName(), prepareSource(alg, hints)
                .getPathName());
    }

    @Test
    public void keyIsHashTest() throws Exception {
        FilePathAlgorithm sha1 =
                new FileContentDigestPathAlgorithm("SHA1", 1, 1, true);
        FilePathSource source = prepareSource(sha1, null);
        assertEquals(FILE_SHA1, source.getPathKey());
    }

    protected FilePathAlgorithm getAlgorithm() {
        return new FileContentDigestPathAlgorithm("MD5", 1, 1, true);
    }

    private FilePathSource prepareSource(FilePathAlgorithm alg,
                                         Map<String, String> hints)
            throws Exception {
        return prepareSource(alg,
                             this.getClass().getResourceAsStream(FILE),
                             hints);
    }

    private byte[] getBytes(String hex) throws DecoderException {
        return Hex.decodeHex(hex.toCharArray());
    }
}
