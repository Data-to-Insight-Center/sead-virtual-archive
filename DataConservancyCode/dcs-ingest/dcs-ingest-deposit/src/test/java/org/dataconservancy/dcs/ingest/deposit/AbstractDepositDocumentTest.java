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
package org.dataconservancy.dcs.ingest.deposit;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import org.junit.Test;

import org.dataconservancy.deposit.DepositDocument;

import static junit.framework.Assert.assertEquals;

public class AbstractDepositDocumentTest {

    private final String CONTENT = "This is the content";

    private final String MIME = "text/plain";

    private final long LAST_MOD = 123456;

    @Test
    public void lastMoificationDateTest() {
        DepositDocument doc = new MockDocument();
        assertEquals(LAST_MOD, doc.getLastModified());
    }

    @Test
    public void contentLengthTest() throws Exception {
        DepositDocument doc = new MockDocument();
        long length = CONTENT.getBytes().length;

        assertEquals(length, IOUtils.toByteArray(doc.getInputStream()).length);
    }

    @Test
    public void metadataTest() {
        DepositDocument doc = new MockDocument();
        assertEquals(CONTENT.getBytes().length, Long.parseLong(doc
                .getMetadata().get("Content-Length")));
    }

    @Test
    public void mimeTypeTest() {
        DepositDocument doc = new MockDocument();
        assertEquals(MIME, doc.getMimeType());
    }

    private class MockDocument
            extends AbstractDepositDocument {

        protected long getDocument(OutputStream out) {
            try {
                IOUtils.copy(IOUtils.toInputStream(CONTENT), out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return LAST_MOD;
        }

        @Override
        public String getMimeType() {
            return MIME;
        }

    }
}
