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
package org.dataconservancy.model.builder.xstream;

import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class FormatConverterTest extends AbstractXstreamConverterTest {

    private static final String SCHEME = "http://www.nationalarchives.gov.uk/PRONOM/";
    private static final String SCHEME_VAL = "x-fmt/18";
    private static final String FORMAT_NAME = "FITS";
    private static final String FORMAT_VERSION = "3.0";

    private static final String XML_ONE = "<format xmlns=\"" + XMLNS + "\" >\n" +
            "        <id scheme=\"" + SCHEME + "\">" + SCHEME_VAL + "</id>\n" +
            "      </format>";

    private static final String XML_TWO = "<format xmlns=\"" + XMLNS + "\" >\n" +
            "        <id scheme=\"" + SCHEME + "\">" + SCHEME_VAL + "</id>\n" +
            "        <name>" + FORMAT_NAME + "</name>\n" +
            "        <version>" + FORMAT_VERSION + "</version>\n" +
            "      </format>";

    private DcsFormat formatOne;
    private DcsFormat formatTwo;

    private final DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();

    @Before
    public void setUp() {
        super.setUp();
        formatOne = new DcsFormat();
        formatOne.setSchemeUri(SCHEME);
        formatOne.setFormat(SCHEME_VAL);

        formatTwo = new DcsFormat(formatOne);
        formatTwo.setName(FORMAT_NAME);
        formatTwo.setVersion(FORMAT_VERSION);
    }

    @Test
    public void testMarshal() throws Exception {
        XMLAssert.assertXMLEqual(XML_ONE, x.toXML(formatOne));
        XMLAssert.assertXMLEqual(XML_TWO, x.toXML(formatTwo));

        XMLAssert.assertXMLEqual(XML_ONE, x.toXML(x.fromXML(XML_ONE)));
        XMLAssert.assertXMLEqual(XML_TWO, x.toXML(x.fromXML(XML_TWO)));
    }

    @Test
    public void testUnmarshal() throws Exception {
        assertEquals(formatOne, x.fromXML(XML_ONE));
        assertEquals(formatTwo, x.fromXML(XML_TWO));

        assertEquals(formatOne, x.fromXML(x.toXML(formatOne)));
        assertEquals(formatTwo, x.fromXML(x.toXML(formatTwo)));
    }


    /**
     * See DC-347
     */
    @Test
    public void testFileFormat() throws InvalidXmlException {
        final DcsFile file = new DcsFile();
        final DcsFormat fmt = new DcsFormat();

        // Only fails when this is set
        fmt.setSchemeUri("grr");

        file.addFormat(fmt);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        mb.buildFile(file, os);
        final DcsFile file2 =
                mb.buildFile(new ByteArrayInputStream(os.toByteArray()));

        assertEquals(file, file2);
    }

    @Test
    public void testMarshallNullValues() throws Exception {
        x.toXML(new DcsFormat());
    }
}
