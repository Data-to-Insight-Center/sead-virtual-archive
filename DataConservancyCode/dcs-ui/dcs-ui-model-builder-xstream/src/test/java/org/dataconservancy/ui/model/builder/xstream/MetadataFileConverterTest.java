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
package org.dataconservancy.ui.model.builder.xstream;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.PersonName;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.dom.DOMSource;
import java.io.IOException;

import static org.dataconservancy.ui.model.builder.xstream.ConverterTestConstants.METADATA_FILES_WRAPPER;
import static org.dataconservancy.ui.model.builder.xstream.MetadataFileConverter.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A unit test for the File converter. Note: This converts a DataFile for now.
 */
/*
 * TODO: if MetadataFile and DataFile is consolidated into one class. This test is no longer needed. if MetadataFile and DataFile remain separate, remove this TODO.
 */
public class MetadataFileConverterTest extends BaseConverterTest {

    static final String XMLNS = "http://dataconservancy.org/schemas/bop/1.0";
    final Logger log = LoggerFactory.getLogger(this.getClass());

    private String XML;


    private void setupXML() {
        XML =
                "    <" + E_METADATA_FILE + " " + MetadataFileConverter.E_ID + "=\"" + metadataFileOne.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                        "        <" + E_PARENT_ID + ">" + metadataFileOne.getParentId() + "</" + E_PARENT_ID + ">\n" +
                        "        <" + E_SOURCE + ">" + metadataFileOne.getSource() + "</" + E_SOURCE + ">\n" +
                        "        <" + E_FORMAT + ">" + metadataFileOne.getFormat() + "</" + E_FORMAT + ">\n" +
                        "        <" + E_NAME + ">" + metadataFileOne.getName() + "</" + E_NAME + ">\n" +
                        "        <" + E_PATH + ">" + metadataFileOne.getPath() + "</" + E_PATH + ">\n" +
                        "        <" + E_METADATA_FORMAT + ">" + metadataFileOne.getMetadataFormatId() + "</" + E_METADATA_FORMAT + ">\n" +
                "    </" + E_METADATA_FILE + ">";

    }
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupXML();
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(metadataFileOne));
        assertTrue(true);
    }

    @Test
    public void testUnmarshal() {
        MetadataFile actual = (MetadataFile) x.fromXML(XML);
        assertEquals(metadataFileOne, actual);
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));
    }
    /**
     * Test which insures that the expected XML is valid, marshaled XML is valid, and round-tripped XML is valid.
     *
     * @throws Exception
     */
    @Test
    public void testMarshalIsValid() throws Exception {
        // Verify assumptions: test that our expected XML is valid.
        final String expectedXml = String.format(BOP_WRAPPER, String.format(METADATA_FILES_WRAPPER, XML));

        final Document expectedDom = parser.parse(IOUtils.toInputStream(expectedXml));
        validator.validate(new DOMSource(expectedDom));

        // Verify that our serialized XML is valid.
        final String actualXml = String.format(BOP_WRAPPER, String.format(METADATA_FILES_WRAPPER, x.toXML(metadataFileOne)));
        final Document actualDom = parser.parse(IOUtils.toInputStream(actualXml));
        validator.validate(new DOMSource(actualDom));

        // Verify that round-tripped XML is valid (XML -> Object -> XML)
        final String roundTrippedXml = String.format(BOP_WRAPPER, String.format(METADATA_FILES_WRAPPER, x.toXML(x.fromXML(XML))));
        final Document roundTrippedDom = parser.parse(IOUtils.toInputStream(roundTrippedXml));
        validator.validate(new DOMSource(roundTrippedDom));
    }

    /**
     * Test that there are no errors when converting Files with empty fields.
     */
    @Test
    public void emptyFieldsTest() {

        // Make sure were equal to start
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));

        // Test no name.
        metadataFileOne.setName(null);
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));

        // Test no source.
        metadataFileOne.setSource(null);
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));

        // Test no path
        metadataFileOne.setPath(null);
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));

        // Test no format
        metadataFileOne.setFormat(null);
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));
        
        //Test no metadata format id
        metadataFileOne.setMetadataFormatId(null);
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));
        
        //Test no metadata parent id
        metadataFileOne.setParentId(null);
        assertEquals(metadataFileOne, x.fromXML(x.toXML(metadataFileOne)));

    }

}
