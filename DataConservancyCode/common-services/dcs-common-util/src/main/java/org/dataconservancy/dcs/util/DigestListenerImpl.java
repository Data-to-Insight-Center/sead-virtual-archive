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

import java.io.IOException;

/**
 * A simple implementation of digest listener, providing access to the digest
 * as a byte array or a hexadecimal string.
 */
public class DigestListenerImpl implements DigestListener {
    private String asHex;
    private byte[] asByteArray;

    @Override
    public void notify(byte[] digestValue) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (byte b : digestValue) {
            sb.append(Integer.toHexString(((int) b & 0xff)));
        }
        asHex = sb.toString();
        asByteArray = digestValue;
    }

    /**
     * Obtain the digest value as a hexadecimal string, similar to the output of
     * the unix command 'md5sum'.
     *
     * @return the digest value as a hex string
     */
    public String asHex() {
        return asHex;
    }

    /**
     * Obtain the unmodified digest as a byte array.
     *
     * @return the digest as a byte array
     */
    public byte[] asByteArray() {
        return asByteArray;
    }
}
