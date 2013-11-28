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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Hex;

import org.apache.commons.io.IOUtils;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.util.DigestListener;
import org.dataconservancy.dcs.util.DigestNotificationStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DigestNotificationStreamTest extends AbstractDigestNotificationTest {

    @Test
    public void singleHashTest() throws Exception {

        final List<String> visited = new ArrayList<String>();

        InputStream filtered = new ByteArrayInputStream(CONTENT.getBytes());

        filtered =
                new DigestNotificationStream(filtered, MessageDigest
                        .getInstance("MD5"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_MD5_HEX, toHex(digestValue));
                        visited.add("MD5");
                    }
                });

        assertEquals(CONTENT, IOUtils.toString(filtered));
        assertEquals(1, visited.size());
        assertTrue(visited.contains("MD5"));
    }

    @Test
    public void chainedHashTest() throws Exception {
        final List<String> visited = new ArrayList<String>();

        InputStream filtered = new ByteArrayInputStream(CONTENT.getBytes());

        filtered =
                new DigestNotificationStream(filtered, MessageDigest
                        .getInstance("MD5"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_MD5_HEX, toHex(digestValue));
                        visited.add("MD5");
                    }
                });

        filtered =
                new DigestNotificationStream(filtered, MessageDigest
                        .getInstance("SHA1"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_SHA1_HEX, toHex(digestValue));
                        visited.add("SHA1");
                    }
                });

        filtered =
                new DigestNotificationStream(filtered, MessageDigest
                        .getInstance("SHA-256"), new DigestListener() {

                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_SHA256_HEX, toHex(digestValue));
                        visited.add("SHA-256");
                    }
                });

        assertEquals(CONTENT, IOUtils.toString(filtered));
        assertEquals(3, visited.size());
        assertTrue(visited.containsAll(Arrays.asList("MD5", "SHA1", "SHA-256")));
    }

    @Test
    public void readStreamExactLengthTest() throws NoSuchAlgorithmException, IOException {
        final List<String> visited = new ArrayList<String>();

        final InputStream s = new DigestNotificationStream(new ByteArrayInputStream(CONTENT.getBytes()),
                MessageDigest.getInstance("MD5"),
                new DigestListener() {

                    @Override
                    public void notify(byte[] digestValue) throws IOException {
                        assertEquals(CONTENT_MD5_HEX, toHex(digestValue));
                        visited.add("MD5");
                    }
                });
        byte[] buf = new byte[CONTENT.getBytes().length];
        assertEquals(CONTENT.getBytes().length, s.read(buf, 0, buf.length));
        s.close();
        assertEquals(1, visited.size());
        assertEquals("MD5", visited.iterator().next());
    }

    @Test
    public void listenerNotifiedOnceTest() throws IOException, NoSuchAlgorithmException {
        final AtomicInteger count = new AtomicInteger(0);
        final InputStream s = new DigestNotificationStream(new ByteArrayInputStream(CONTENT.getBytes()),
                MessageDigest.getInstance("MD5"),
                new DigestListener() {
                    @Override
                    public void notify(byte[] digestValue) throws IOException {
                        count.getAndIncrement();
                    }
                });

        assertEquals(0, count.intValue());
        IOUtils.copy(s, new NullOutputStream());
        assertEquals(1, count.intValue());
        s.close();
        assertEquals(1, count.intValue());
    }
}
