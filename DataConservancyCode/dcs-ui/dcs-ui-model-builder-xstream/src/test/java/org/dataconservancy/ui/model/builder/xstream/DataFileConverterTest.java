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

import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.ui.model.DataFile;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.dataconservancy.ui.model.builder.xstream.DataFileConverter.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A unit test for the File converter. Note: This converts a DataFile for now.
 */
public class DataFileConverterTest extends BaseConverterTest {
    
    static final String XMLNS = "http://dataconservancy.org/schemas/bop/1.0";
    final Logger log = LoggerFactory.getLogger(this.getClass());


    private String XML;

    
    private void setupXML() {
        dataFileOne.setFormat("format");
        XML =
                "    <" + E_FILE + " " + DataFileConverter.E_ID + "=\"" + dataFileOne.getId() + "\" xmlns=\"" + XMLNS + "\">\n" +
                "        <" + E_PARENT_ID + ">" + dataItemOne.getId() + "</" + E_PARENT_ID + ">\n" +
                "        <" + E_SOURCE + ">" + dataFileOne.getSource() + "</" + E_SOURCE + ">\n" +
                "        <" + E_FORMAT + ">" + dataFileOne.getFormat() + "</" + E_FORMAT + ">\n" +
                "        <" + E_NAME + ">" + dataFileOne.getName() + "</" + E_NAME + ">\n" +
                "        <" + E_PATH + ">" + dataFileOne.getPath() + "</" + E_PATH + ">\n" +
                "        <" + E_SIZE + ">" + String.valueOf(dataFileOne.getSize()) + "</" + E_SIZE + ">\n" +
                "    </" + E_FILE + ">";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupXML();
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(dataFileOne));
        assertTrue(true);
    }

    @Test
    public void testUnmarshal() {
        DataFile actual = (DataFile) x.fromXML(XML);
        assertEquals(dataFileOne, actual);
        assertEquals(dataFileOne, x.fromXML(x.toXML(dataFileOne)));
    }

    /**
     * Test that there are no errors when converting Files with empty fields.
     */
    @Test
    public void emptyFieldsTest() {

        // Make sure were equal to start
        assertEquals(dataFileOne, x.fromXML(x.toXML(dataFileOne)));

        // Test no name.
        dataFileOne.setName(null);
        assertEquals(dataFileOne, x.fromXML(x.toXML(dataFileOne)));

        // Test no source.
        dataFileOne.setSource(null);
        assertEquals(dataFileOne, x.fromXML(x.toXML(dataFileOne)));

        // Test no path
        dataFileOne.setPath(null);
        assertEquals(dataFileOne, x.fromXML(x.toXML(dataFileOne)));

        // Test no format
        dataFileOne.setFormat(null);
        assertEquals(dataFileOne, x.fromXML(x.toXML(dataFileOne)));

        // Test no metadata files
     //   dataFileOne.setMetadataFiles(null);
     //   assertEquals(dataFileOne, x.fromXML(x.toXML(dataFileOne)));

    }

}
