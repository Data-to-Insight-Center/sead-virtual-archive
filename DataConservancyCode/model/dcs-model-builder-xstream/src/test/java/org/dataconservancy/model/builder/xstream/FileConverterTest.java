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
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class FileConverterTest extends AbstractXstreamConverterTest {

    private static final String FILE_ID = "urn:sdss:12345/FITS_FILE";
    private static final String FILE_SRC = "http://sdss.org/files/fits/12345.fits";
    private static final String FILE_NAME = "12345.fits";
    private static final boolean isExtant = false;

    private static final String FORMAT_SCHEME = "http://www.nationalarchives.gov.uk/PRONOM/";
    private static final String FORMAT_NAME = "FITS";
    private static final String FORMAT_VALUE = "x-fmt/383";
    private static final String FORMAT_VERSION = "3.0";

    private final static String AUTHID1 = "aid123";
    private final static String TYPEID1 = "type123";
    private final static String IDVALUE1 = "id123";

    private static final String XML = "<File xmlns=\"" + XMLNS + "\" id=\"" + FILE_ID + "\" src=\"" + FILE_SRC + "\">\n" +
            "      <fileName>" + FILE_NAME + "</fileName>\n" +
            "        <" + DeliverableUnitConverter.E_ALTERNATEID + ">" +
            "        <authorityId>" + AUTHID1 + "</authorityId> " +
            "        <typeId>" + TYPEID1 + "</typeId> " +
            "        <idValue>" + IDVALUE1 + "</idValue> " +
            "        </" + FileConverter.E_ALTERNATEID + ">" +
            "\n" +
            "      <extant>" + Boolean.toString(isExtant) + "</extant>\n" +
            "      <format>\n" +
            "        <id scheme=\"" + FORMAT_SCHEME + "\">" + FORMAT_VALUE + "</id>\n" +
            "        <name>" + FORMAT_NAME + "</name>\n" +
            "        <version>" + FORMAT_VERSION + "</version>\n" +
            "      </format>\n" +
            "    </File>";

    private DcsFile f;

    @Before
    public void setUp() {
        super.setUp();

        f = new DcsFile();
        final DcsFormat fm = new DcsFormat();

        f.setId(FILE_ID);
        f.setSource(FILE_SRC);
        f.setName(FILE_NAME);
        f.setExtant(isExtant);

        fm.setFormat(FORMAT_VALUE);
        fm.setName(FORMAT_NAME);
        fm.setSchemeUri(FORMAT_SCHEME);
        fm.setVersion(FORMAT_VERSION);

        f.addFormat(fm);

        final DcsResourceIdentifier rid = new DcsResourceIdentifier();
        rid.setAuthorityId(AUTHID1);
        rid.setTypeId(TYPEID1);
        rid.setIdValue(IDVALUE1);
        f.addAlternateId(rid);

    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(f));
        //XMLAssert.assertXMLEqual(XML, x.toXML(x.fromXML(XML)));
    }

    @Test
    public void testUnmarshal() {
        assertEquals("Expected: " + f + " Actual: " + x.fromXML(XML), f, x.fromXML(XML));
        assertEquals(f, x.fromXML(x.toXML(f)));
    }

}
