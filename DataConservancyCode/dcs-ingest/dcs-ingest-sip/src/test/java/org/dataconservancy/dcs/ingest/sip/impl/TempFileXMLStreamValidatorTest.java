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
package org.dataconservancy.dcs.ingest.sip.impl;

import java.io.InputStream;

import org.junit.Test;

import org.dataconservancy.dcs.ingest.sip.impl.TempFileXmlStreamValidator.TempStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TempFileXMLStreamValidatorTest {

    private static final String BAD_FILE = "/nonValiddSip.xml";

    private static final String GOOD_FILE = "/validSip.xml";

    private static final TempFileXmlStreamValidator validator =
            new TempFileXmlStreamValidator("/schema/dcp.xsd");

    @Test
    public void validateValidDocumentTest() {
        validator.validating(getStream(GOOD_FILE));
        /* No exception should be thrown */
    }

    @Test(expected = Exception.class)
    public void validateInvalidDocumentTest() {
        validator.validating(getStream(BAD_FILE));
    }

    @Test
    public void nonClosingCleanupTest() throws Exception {
        TempStream stream =
                (TempStream) validator.validating(getStream(GOOD_FILE));

        assertTrue(stream.tempFile.exists());

        while (stream.read() != -1);

        assertFalse(stream.tempFile.exists());

    }

    @Test
    public void closingCleanupTest() throws Exception {
        TempStream stream =
                (TempStream) validator.validating(getStream(GOOD_FILE));

        assertTrue(stream.tempFile.exists());

        while (stream.read() != -1);
        stream.close();

        assertFalse(stream.tempFile.exists());
    }

    @Test
    public void abortiveClosingCleanupTest() throws Exception {
        TempStream stream =
                (TempStream) validator.validating(getStream(GOOD_FILE));

        assertTrue(stream.tempFile.exists());

        stream.read(new byte[2]);
        stream.close();

        assertFalse(stream.tempFile.exists());
    }

    private InputStream getStream(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new RuntimeException("no resource found for '" + path + "'");
        }
        return stream;
    }

}
