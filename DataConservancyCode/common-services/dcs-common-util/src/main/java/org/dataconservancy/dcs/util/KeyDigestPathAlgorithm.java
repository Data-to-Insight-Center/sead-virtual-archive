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

import java.security.MessageDigest;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

/**
 * Creates paths based upon a digest of their provided key string.
 */
public class KeyDigestPathAlgorithm
        implements FilePathKeyAlgorithm {

    private final int directoryWidth;

    private final int directoryDepth;

    private final String algorithm;

    private final String fileSuffix;

    public KeyDigestPathAlgorithm(String algo,
                                  int width,
                                  int depth,
                                  String suffix) {
        algorithm = algo;

        directoryWidth = width;

        directoryDepth = depth;

        if (suffix != null && !"".equals(suffix)) {
            fileSuffix = suffix.replaceFirst("^[^\\.]", "." + suffix.charAt(0));
        } else {
            fileSuffix = "";
        }

    }

    /**
     * If no key is provided, generate a digest based on a random key.
     * {@inheritDoc}
     */
    public FilePathSource getPath(InputStream in, Map<String, String> hints) {
        return getPath(in, hash(UUID.randomUUID().toString()), hints);
    }

    public FilePathSource getPath(InputStream in,
                                  String key,
                                  Map<String, String> hints) {
        return new IdPathSource(in, key);
    }

    public boolean isContentAddressable() {
        return false;
    }

    public String lookupPathName(String pathKey) {
        return appendDirectories(hash(pathKey)) + fileSuffix;
    }

    private class IdPathSource
            implements FilePathSource {

        private final InputStream is;

        private final String nameHash;

        private final String pathKey;

        public IdPathSource(InputStream in, String key) {
            is = in;

            nameHash = hash(key);

            pathKey = key;
        }

        public InputStream getInputStream() {
            return is;
        }

        public String getPathKey() {
            return pathKey;
        }

        @Override
        public String getPathName() {
            return appendDirectories(nameHash) + fileSuffix;
        }

    }

    public String getSuffix() {
        return fileSuffix;
    }

    /**
     * Calculate a hash code over the supplied string.
     *
     * @param in the string
     * @return the hash code, encoded in hexadecimal.
     * @throws RuntimeException if the hash cannot be calculated
     */
    String hash(String in) {
        try {
            return new String(Hex.encodeHex(MessageDigest
                    .getInstance(algorithm).digest(in.getBytes("UTF-8"))));
        } catch (Exception e) {
            throw new RuntimeException("Could not calculate id hash", e);
        }
    }

    private String appendDirectories(String fileName) {
        StringBuilder path = new StringBuilder();
        int length = 2 * directoryWidth;
        for (int depth = 0; depth < directoryDepth; depth++) {
            int start = 2 * directoryWidth * depth;
            path.append(fileName.subSequence(start, start + length));
            if (length > 0) {
                path.append(File.separator);
            }
        }

        path.append(fileName);
        return path.toString();
    }

}
