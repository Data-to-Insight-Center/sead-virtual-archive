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

import org.apache.commons.codec.binary.Hex;
import org.junit.Before;

import java.security.MessageDigest;

public abstract class AbstractDigestNotificationTest {

    String CONTENT = "The quick brown fox jumps over the lazy dog";

    String CONTENT_MD5_HEX;

    String CONTENT_SHA1_HEX;

    String CONTENT_SHA256_HEX;

    @Before
    public void calcHashes() throws Exception {
        CONTENT_MD5_HEX =
                toHex(MessageDigest.getInstance("MD5").digest(CONTENT
                        .getBytes()));
        CONTENT_SHA1_HEX =
                toHex(MessageDigest.getInstance("SHA1").digest(CONTENT
                        .getBytes()));
        CONTENT_SHA256_HEX =
                toHex(MessageDigest.getInstance("SHA-256").digest(CONTENT
                        .getBytes()));
    }

    String toHex(byte[] data) {
        return new String(Hex.encodeHex(data));
    }

}
