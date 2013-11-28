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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DigestNotificationOutputStreamTest extends AbstractDigestNotificationTest {

    @Test
    public void singleHashTest() throws Exception {
        final List<String> visited = new ArrayList<String>();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DigestNotificationOutputStream filtered =
                new DigestNotificationOutputStream(out, MessageDigest
                        .getInstance("MD5"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_MD5_HEX, toHex(digestValue));
                        visited.add("MD5");
                    }
                });

        IOUtils.write(CONTENT, filtered);
        filtered.close();
        assertEquals(CONTENT, new String(out.toByteArray(), "UTF-8"));
        assertEquals(1, visited.size());
        assertTrue(visited.contains("MD5"));
    }

    @Test
    public void chainedHashTest() throws Exception {
        final List<String> visited = new ArrayList<String>();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DigestNotificationOutputStream filtered =
                new DigestNotificationOutputStream(out, MessageDigest
                        .getInstance("MD5"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_MD5_HEX, toHex(digestValue));
                        visited.add("MD5");
                    }
                });

        filtered = new DigestNotificationOutputStream(filtered, MessageDigest
                        .getInstance("SHA-1"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_SHA1_HEX, toHex(digestValue));
                        visited.add("SHA-1");
                    }
                });

        filtered =
                new DigestNotificationOutputStream(filtered, MessageDigest
                        .getInstance("SHA-256"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_SHA256_HEX, toHex(digestValue));
                        visited.add("SHA-256");
                    }
                });

        IOUtils.write(CONTENT, filtered);
        filtered.close();
        assertEquals(CONTENT, new String(out.toByteArray(), "UTF-8"));
        assertEquals(3, visited.size());
        assertTrue(visited.contains("MD5"));
        assertTrue(visited.contains("SHA-1"));
        assertTrue(visited.contains("SHA-256"));
    }

    @Test
    public void listenerNotifiedOnceTest() throws IOException, NoSuchAlgorithmException {
        final AtomicInteger count = new AtomicInteger(0);
        final OutputStream s = new DigestNotificationOutputStream(new ByteArrayOutputStream(),
                MessageDigest.getInstance("MD5"),
                new DigestListener() {
                    @Override
                    public void notify(byte[] digestValue) throws IOException {
                        count.getAndIncrement();
                    }
                });

        IOUtils.copy(new ByteArrayInputStream(CONTENT.getBytes()), s);
        assertEquals(0, count.intValue());
        s.close();
        assertEquals(1, count.intValue());
        s.close();
        assertEquals(1, count.intValue());
    }
}
