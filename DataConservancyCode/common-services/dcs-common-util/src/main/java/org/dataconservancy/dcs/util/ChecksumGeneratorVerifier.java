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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class provides helper methods that generate and verify a checksum. The generator methods accept an input stream
 * and return a String hex of the checksum to be stored. The verifier method can be used with either two String
 * checksums or an input stream and a stored checksum to check against.
 * 
 */
public class ChecksumGeneratorVerifier {
    
    private final static Logger LOG = LoggerFactory.getLogger(ChecksumGeneratorVerifier.class);

    /**
     * Generates an MD5 checksum for a given file.
     * 
     * @param file
     * @return String hex checksum
     */
    public String generateMD5checksum(InputStream inputStream) {
        try {
            if (inputStream != null) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] dataBytes = new byte[1024];
                int byteNumberRead = 0;
                
                while ((byteNumberRead = inputStream.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, byteNumberRead);
                }
                
                byte[] mdBytes = md.digest();
                
                // Converting to hex string.
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < mdBytes.length; i++) {
                    sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                
                inputStream.close();
                return sb.toString();
            }
            else {
                LOG.error("Input stream is null!");
            }
        }
        catch (Exception e) {
            LOG.error("Could not generate the checksum.", e);
        }
        return null;
    }

    /**
     * Generates a SHA1 checksum for a given file.
     * 
     * @param file
     * @return String hex checksum
     */
    public String generateSHA1checksum(InputStream inputStream) {
        try {
            if (inputStream != null) {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                byte[] dataBytes = new byte[1024];
                int byteNumberRead = 0;
                
                while ((byteNumberRead = inputStream.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, byteNumberRead);
                }
                
                byte[] mdBytes = md.digest();
                
                // Converting to hex string.
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < mdBytes.length; i++) {
                    sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                
                inputStream.close();
                return sb.toString();
            }
            else {
                LOG.error("Input stream is null!");
            }
        }
        catch (Exception e) {
            LOG.error("Could not generate the checksum.", e);
        }
        
        return null;
    }
    
    /**
     * Verifies two given String checksums.
     * 
     * @param checksum1
     * @param checksum2
     * @return boolean
     */
    public boolean verifyChecksum(String checksum1, String checksum2) {
        if (checksum1.equals(checksum2)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Takes a file and an algorithm (MD5 or SHA1) as well as a current checksum, generates checksum with the given
     * algorithm and verifies it against the given checksum.
     * 
     * @param inputStream
     * @param currentChecksum
     * @param algorithm
     * @return boolean
     * @throws NoSuchAlgorithmException
     */
    public boolean verifyChecksum(InputStream inputStream, String algorithm, String currentChecksum)
            throws NoSuchAlgorithmException {
        String newChecksum = null;
        if (algorithm.equalsIgnoreCase("md5")) {
            newChecksum = generateMD5checksum(inputStream);
        }
        else if (algorithm.equalsIgnoreCase("sha1")) {
            newChecksum = generateSHA1checksum(inputStream);
        }
        else {
            throw new NoSuchAlgorithmException("The given algorithm <" + algorithm + "> is not acceptable.");
        }
        if (newChecksum.equals(currentChecksum)) {
            return true;
        }
        else {
            return false;
        }
    }
}
