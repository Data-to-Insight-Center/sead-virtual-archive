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
package org.dataconservancy.dcs.ingest.file.impl;

import java.io.OutputStream;

import java.util.Date;

import org.apache.commons.io.IOUtils;

import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;

import static junit.framework.Assert.assertEquals;

public class FileDocumentBaseTest {

    private static final long LAST_MOD = new Date().getTime();

    private static final String FILE_ID = "1";

    private static final String FILE_CONTENT = "ths is the file content";

    @Test
    public void correctFileTest() {
        MockDocument doc = getDocument();
        assertEquals(FILE_ID, doc.getFile().getId());
    }

    private MockDocument getDocument() {

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId(FILE_ID);
        sip.addFile(file);

        return new MockDocument(sip);
    }

    private class MockDocument
            extends FileDocumentBase {

        public MockDocument(Dcp sip) {
            super(sip);
        }

        protected long getDocument(OutputStream out) {
            try {
                IOUtils.copy(IOUtils.toInputStream(FILE_CONTENT), out);
                return LAST_MOD;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getMimeType() {
            return "text/plain";
        }
    }
}
