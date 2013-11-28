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
package org.dataconservancy.archive.impl.fcrepo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides a one-way, repeatable (deterministic) mapping of arbitrary id
 * strings to Fedora PIDs.
 */
public class PIDMapper {

    private final static String HEXCHARS = "0123456789abcdef";

    private final static int MAX_NAMESPACE_LENGTH = 64 - 1 - 16; // 47

    private final String pidNamespace;

    /**
     * Constructs a <code>PIDMapper.</code>
     *
     * @param pidNamespace the Fedora PID namespace to use for all mappings.
     * @throws IllegalArgumentException if the namespace would result in PIDs
     *         that don't conform to the <a href="https://wiki.duraspace.org/x/eABI">
     *         Fedora Identifiers specification</a>.
     */
    public PIDMapper(String pidNamespace) {
        if (pidNamespace == null || pidNamespace.length() == 0) {
            throw new IllegalArgumentException("pidNamespace must be specified");
        }
        if (pidNamespace.length() > 47) {
            throw new IllegalArgumentException("pidNamespace cannot exceed 47 chars");
        }
        for (char c: pidNamespace.toCharArray()) {
            if (! ( (c >= 'a' && c <= 'z') ||
                    (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') ||
                    (c == '-' || c == '.') ) ) {
                throw new IllegalArgumentException("Illegal character in "
                        + "pidNamespace: " + c);
            }
        }
        this.pidNamespace = pidNamespace;
    }

    /**
     * Gets a PID for the given id.
     *
     * @param id the id.
     * @return the corresponding PID.
     * @throws IllegalArgumentException if the id is null or empty.
     */
    public String getPID(String id) {
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("id must be specified");
        }
        StringBuilder builder = new StringBuilder(pidNamespace + ":");
        builder.append(getHexDigest(id));
        return builder.toString();
    }

    // Computes the MD5 digest of the given string using UTF8 encoding,
    // and returns it as a lowercase hexadecimal string
    private static String getHexDigest(String id) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(id.getBytes("UTF-8"));
            return getHexString(digest.digest());
        } catch (UnsupportedEncodingException wontHappen) {
            throw new RuntimeException(wontHappen);
        } catch (NoSuchAlgorithmException wontHappen) {
            throw new RuntimeException(wontHappen);
        }
    }

    // Gets a lowercase hex representation of the given bytes
    private static String getHexString(byte[] raw) {
        StringBuilder builder = new StringBuilder(raw.length * 2);
        for (byte b: raw) {
            builder.append(HEXCHARS.charAt((b & 0xF0) >> 4))
                   .append(HEXCHARS.charAt((b & 0x0F)));
        }
        return builder.toString();
    }
}
