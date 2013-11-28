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

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ChecksumGeneratorVerifierTest {
    
    private ChecksumGeneratorVerifier checksumGeneratorVerifier;
    private InputStream file1;
    private String file1MD5checksum = "1e75d5298fd12184f34bad372a81b3e6";
    private String file1SHA1checksum = "428deee4e21cd468d81595597befdc9f21fdf8ee";
    private InputStream modifiedFile1;

    @Before
    public void setUp() {
        checksumGeneratorVerifier = new ChecksumGeneratorVerifier();
        file1 = ChecksumGeneratorVerifierTest.class.getResourceAsStream("/file.txt");
        modifiedFile1 = ChecksumGeneratorVerifierTest.class.getResourceAsStream("/modifiedFile.txt");
    }
    
    @Test
    public void testGenerateMD5checksum() {
        String checksum = checksumGeneratorVerifier.generateMD5checksum(file1);
        Assert.assertEquals(file1MD5checksum, checksum);
        String modifiedChecksum = checksumGeneratorVerifier.generateMD5checksum(modifiedFile1);
        Assert.assertNotSame("The modified file should not have the same checksum.", file1MD5checksum, modifiedChecksum);
    }
    
    @Test
    public void testGenerateSHA1checksum() {
        String checksum = checksumGeneratorVerifier.generateSHA1checksum(file1);
        Assert.assertEquals(file1SHA1checksum, checksum);
        String modifiedChecksum = checksumGeneratorVerifier.generateSHA1checksum(modifiedFile1);
        Assert.assertNotSame("The modified file should not have the same checksum.", file1SHA1checksum,
                modifiedChecksum);
    }
    
    @Test
    public void testVerifyMD5checksumWithTwoStrings() {
        String md5Checksum = checksumGeneratorVerifier.generateMD5checksum(file1);
        Assert.assertTrue("The checksums don't match.",
                checksumGeneratorVerifier.verifyChecksum(file1MD5checksum, md5Checksum));
    }
    
    @Test
    public void testVerifySHA1checksumWithTwoStrings() {
        String sha1Checksum = checksumGeneratorVerifier.generateSHA1checksum(file1);
        Assert.assertTrue("The checksums don't match.",
                checksumGeneratorVerifier.verifyChecksum(file1SHA1checksum, sha1Checksum));
    }
    
    @Test
    public void testVerifyMD5checksumWithOneFileAndOneString() throws NoSuchAlgorithmException {
        Assert.assertTrue("The checksums don't match.",
                checksumGeneratorVerifier.verifyChecksum(file1, "md5", file1MD5checksum));
    }
    
    @Test
    public void testVerifySHA1checksumWithOneFileAndOneString() throws NoSuchAlgorithmException {
        Assert.assertTrue("The checksums don't match.",
                checksumGeneratorVerifier.verifyChecksum(file1, "sha1", file1SHA1checksum));
    }
    
}
