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
import java.security.NoSuchAlgorithmException;

import java.util.Map;

import org.apache.commons.codec.binary.Hex;

/**
 * Creates file names and paths based on a cryptographic hash function of their
 * content.
 * <p>
 * Results in a directory structure that can be considered pseudo
 * content-addressable. Files will be named after the hexidecimal representation
 * of the hash value, and may be placed into a directory structure based on a
 * function of this hash value as well depending on configuration.
 * </p>
 * <p>
 * {@link FilePathSource#getPathKey()} will return the hash value as the path
 * key, and {@link FilePathAlgorithm#lookupPathName(String)} will accept the
 * hash value as a key in order to return the exact filesystem path of the file.
 * Thus, it is possible to use this class in a way that abstracts away the
 * physical filesystem path, and rely on hash-value only to retrieve files in a
 * content-addressable fashion.
 * </p>
 * <p>
 * This algorithm may be configured to calculate the hash dynamically, or use
 * provided hash values via a 'hints' map that follows http header conventions
 * in {@link FileContentDigestPathAlgorithm#getPath(InputStream, Map)}.
 * </p>
 */
public class FileContentDigestPathAlgorithm
        implements FilePathAlgorithm {

    private final String algorithm;

    private final int directoryDepth;

    private final int directoryWidth;

    private final boolean calculateDigest;

    /**
     * Create an algorithm for a given digest and directory heuristics.
     * <p>
     * </p>
     * 
     * @param algo
     *        Digest algorithm to use (MD5, SHA-1, etc) to name the file based
     *        on its content.
     * @param width
     *        2^(8*width) maximum number of directories in a given level in
     *        bytes. So 1 would be 256 directories, 2 would be 655536, etc.
     * @param depth
     *        Maximum depth of directories. 0 would imply no directory depth
     *        (e.g. will return just filenames), 1 would imply one directory
     *        depth, etc.
     * @param calculate
     *        Whether to calculate the digest, or assume that the correct
     *        didgest will be present in the hints.
     */
    public FileContentDigestPathAlgorithm(String algo,
                                          int width,
                                          int depth,
                                          boolean calculate) {
        algorithm = algo;
        directoryWidth = width;
        directoryDepth = depth;
        calculateDigest = calculate;
    }

    /**
     * Use an input stream and/or hints to generate a hash file name source.
     * <p>
     * Hints may be used to specify the hash value to use as the file name. If
     * constructed with <code>calculate = true</code>, then having the hash
     * value ahead of time may serve as a slight optimization, but ultimately
     * the hash will be verified by the algorithm, and the final correct value.
     * </p>
     * <p>
     * If constructed with <code>calculate = false</code>, the hints MUST
     * contain a hash value for the specified algorithm by the time the last
     * InputStream byte is read. This hash value will be trusted.
     * </p>
     * <p>
     * Keys for hash values are as per RFC 2616 and 3230 (
     * <code>Content-MD5</code> or <code>Digest</code>)
     * </p>
     * <p>
     * See also:
     * <ul>
     * <li>{@link HttpHeaderUtil#getDigests(Map)}</li>
     * <li>{@link HttpHeaderUtil#addDigest(String, byte[], Map)}</li>
     * </ul>
     * </p>
     * 
     * @param in
     *        InputStream that may be analyzed for hash value.
     * @param hints
     *        Used to specify hash value. May be null if not provided.
     */
    public FilePathSource getPath(InputStream in, Map<String, String> hints) {
        return new DigestFilePath(in, hints);
    }

    public String lookupPathName(String pathKey) {
        return appendDirectories(pathKey);
    }

    /** Returns true, as this algorithm is content addressable */
    public boolean isContentAddressable() {
        return true;
    }

    private String appendDirectories(String fileName) {

        if (fileName == null) return null;
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

    private class DigestFilePath
            implements FilePathSource {

        private final InputStream initialInputStream;

        private String finalFileName = null;

        private final Map<String, String> hints;

        public DigestFilePath(InputStream in, Map<String, String> hints) {
            initialInputStream = in;

            String initialAttempt = hints != null ? tryFromHints(hints) : null;
            if (initialAttempt != null) {
                finalFileName = initialAttempt.toLowerCase();
            }
            this.hints = hints;
        }

        public InputStream getInputStream() {
            /*
             * If we're not calculating the digest, just return the given input
             * stream
             */
            if (!calculateDigest) return initialInputStream;

            /* Otherwise, dynamically calculate the hash value */
            try {
                return new DigestNotificationStream(initialInputStream,
                                                    MessageDigest
                                                            .getInstance(algorithm),
                                                    new DigestListener() {

                                                        public void notify(byte[] digestValue) {
                                                            finalFileName =
                                                                    new String(Hex
                                                                            .encodeHex(digestValue));
                                                        }
                                                    });
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public String getPathName() {
            if (finalFileName != null) {

                /* If pre-specified or calculated, use that value */
                return appendDirectories(finalFileName);
            } else if (hints != null) {
                return appendDirectories(tryFromHints(hints));
            }
            return null;
        }

        public String getPathKey() {
            if (finalFileName != null) {
                return finalFileName;
            } else if (hints != null) {
                return tryFromHints(hints);
            }
            return null;
        }

        private String tryFromHints(Map<String, String> hints) {
            Map<String, byte[]> digests = HttpHeaderUtil.getDigests(hints);
            if (digests.containsKey(algorithm)) {
                return new String(Hex.encodeHex(digests.get(algorithm)));
            }
            return null;
        }
    }
}
