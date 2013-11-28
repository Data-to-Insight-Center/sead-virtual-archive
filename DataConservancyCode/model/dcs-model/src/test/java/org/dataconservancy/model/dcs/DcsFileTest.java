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
package org.dataconservancy.model.dcs;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DcsFileTest {

    private DcsFile file;
    private DcsFile filePrime;

    private String fileName = "foo.txt";
    private boolean isExtant = true;
    private long size = 12345;
    private boolean isValid = true;
    private String fixityVal = "4b62503c9347b539802b6bb809005156";
    private String fixityAlgo = "md5";
    private String formatSchemeUri = "uri:to:pronom";
    private String formatId = "x-fmt/283";
    private String formatName = "text/plain";
    private String src = "file:///path/to/foo.txt";
    private String metadataRef = "uri:Metadata:1234";

    private String mdSchemaUri = "uri:to:some:schema";
    private String mdBlob = "    <foo:md xmlns:foo=\"http://bar/baz\">\n" +
            "      <foo:picture>some metadata about the picture</foo:picture>\n" +
            "    </foo:md>\n";
    private String inlineMetadata = "<metadata schemaURI=\"" + mdSchemaUri + "\">\n" + mdBlob + "  </metadata>";

    @Before
    public void setUp() {
        file = new DcsFile();
        filePrime = new DcsFile();

        file.setSizeBytes(size);
        filePrime.setSizeBytes(size);

        file.setExtant(isExtant);
        filePrime.setExtant(isExtant);

        file.setSource(src);
        filePrime.setSource(src);

        file.setValid(isValid);
        filePrime.setValid(isValid);

        file.setName(fileName);
        filePrime.setName(fileName);

        DcsFixity fixity = new DcsFixity();
        fixity.setValue(fixityVal);
        fixity.setAlgorithm(fixityAlgo);
        file.addFixity(fixity);
        filePrime.addFixity(fixity);

        DcsFormat format = new DcsFormat();
        format.setFormat(formatId);
        format.setName(formatName);
        format.setSchemeUri(formatSchemeUri);
        file.addFormat(format);
        filePrime.addFormat(format);

        DcsMetadataRef mdRef = new DcsMetadataRef(metadataRef);
        file.addMetadataRef(mdRef);
        filePrime.addMetadataRef(mdRef);

        DcsMetadata md = new DcsMetadata();
        md.setSchemaUri(mdSchemaUri);
        md.setMetadata(mdBlob);
        file.addMetadata(md);
        filePrime.addMetadata(md);
    }

    @Test
    public void testEquals() throws Exception {

        final DcsFile notEqual = new DcsFile();
        assertFalse(file.equals(notEqual));

        final DcsFile fileCopy = new DcsFile(file);
        final DcsFile fileCopyTwo = new DcsFile(file);

        // symmetric
        assertTrue(file.equals(file));
        assertTrue(fileCopy.equals(fileCopy));

        // reflexive
        assertTrue(file.equals(fileCopy));
        assertTrue(fileCopy.equals(file));
        assertTrue(file.equals(filePrime) && filePrime.equals(file));

        // transitive
        assertTrue(file.equals(fileCopy));
        assertTrue(file.equals(fileCopyTwo));
        assertTrue(fileCopy.equals(fileCopyTwo));

        // consistent
        assertTrue(file.equals(fileCopy) && file.equals(fileCopy));
        assertTrue(fileCopy.equals(file) && file.equals(fileCopy));
    }
}
