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

import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_DATA_ITEM;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_DATE;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_DEPOSITOR;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_DEPOSIT_DATE;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_DESCRIPTION;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_FILES;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_NAME;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_PARENT_ID;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_PATH;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_SIZE;
import static org.dataconservancy.ui.model.builder.xstream.ConverterConstants.E_SOURCE;
import static org.dataconservancy.ui.model.builder.xstream.DataFileConverter.E_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.dataconservancy.ui.model.DataItem;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A unit test for the DataItem converter. Note: This converts a DataItem for now.
 */
public class DataItemConverterTest extends BaseConverterTest {

    final Logger log = LoggerFactory.getLogger(this.getClass());


    /**
     * Used to wrap Data Item serializations in a {@literal <dataItems>} element for validation purposes.
     */
    private final static String DATA_ITEMS_WRAPPER = "<dataItems>\n%s\n</dataItems>";


    private String FILE_XML;

    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("d M yyyy");

    private String XML;


    public void setupXML() {
        FILE_XML =
                "            <" + E_FILE + " " + DataFileConverter.E_ID + "=\"" + dataFileOne.getId() + "\" xmlns=\"" + BOP_XMLNS + "\">\n" +
                "                <" + E_PARENT_ID + ">" + dataItemOne.getId() + "</" + E_PARENT_ID + ">\n"+
                "                <" + E_SOURCE + ">" + dataFileOne.getSource() + "</" + E_SOURCE + ">\n" +
                "                <" + DataFileConverter.E_NAME + ">" + dataFileOne.getName() + "</" + DataFileConverter.E_NAME + ">\n" +
                "                <" + E_PATH + ">" + dataFileOne.getPath() + "</" + E_PATH + ">\n" +
                "                <" + E_SIZE + ">" + String.valueOf(dataFileOne.getSize()) + "</" + E_SIZE + ">\n" +
                "            </" + E_FILE + ">\n";
        
        XML =
                "    <" + E_DATA_ITEM + " " + DataItemConverter.E_ID + "=\"" + dataItemOne.getId() + "\" xmlns=\"" + BOP_XMLNS + "\">\n" +
                "        <" + E_NAME + ">" + dataItemOne.getName() + "</" + E_NAME + ">\n" +
                "        <" + E_DESCRIPTION + ">" + dataItemOne.getDescription() + "</" + E_DESCRIPTION + ">\n" +
                "        <" + E_DEPOSITOR + " " + DataItemConverter.ATTR_REF + "=\"" + admin.getId() + "\" " +"/>\n" +
                "        <" + E_DEPOSIT_DATE + ">\n" +
                "           <" + E_DATE + ">" + fmt.print(dataItemOne.getDepositDate()) + "</" + E_DATE + ">\n" +
                "        </" + E_DEPOSIT_DATE + ">\n" +
                "        <" + E_FILES + ">\n" + FILE_XML +
                "        </" + E_FILES + ">\n" +
                "        <" + E_PARENT_ID + ">" + collectionWithData.getId() + "</" + E_PARENT_ID + ">\n" +
                "    </" + E_DATA_ITEM + ">\n";
    }
    
    @Before
    public void setUp() throws Exception {

        super.setUp();
        setupXML();
        dataItemOne.setDepositorId(admin.getId());
    }

    @Test
    public void testMarshal() throws IOException, SAXException {
        XMLAssert.assertXMLEqual(XML, x.toXML(dataItemOne));
    }

    /**
     * Insures that the marshaling methods ({@link com.thoughtworks.xstream.XStream#toXML(Object)}) produce
     * valid serializations of DataItems according to the BOP schema.
     * <p/>
     * Tests that the expected XML is valid, tests that the marshaled XML is valid, and tests that round-tripped
     * XML is valid.
     *
     * @throws ParserConfigurationException
     * @throws SAXException if validation fails
     * @throws IOException
     */
    @Test
    public void testMarshalIsValid() throws ParserConfigurationException, SAXException, IOException {
        // Verify assumptions: that our expected XML is valid.  First, we wrap it in a BOP.
        final String expectedXml = String.format(BOP_WRAPPER, String.format(DATA_ITEMS_WRAPPER, XML));

        // validate the DOM tree
        validator.validate(new DOMSource(parser.parse(IOUtils.toInputStream(expectedXml))));

        // Verify the DataItem converter produces valid XML
        final String actualConvertedXml = x.toXML(dataItemOne);
        final String actualXml = String.format(BOP_WRAPPER, String.format(DATA_ITEMS_WRAPPER, actualConvertedXml));

        // validate the DOM tree
        validator.validate(new DOMSource(parser.parse(IOUtils.toInputStream(actualXml))));

        // Round Trip: XML to DataItem to XML
        final String s = x.toXML(x.fromXML(XML));
        String roundTripXml = String.format(BOP_WRAPPER, String.format(DATA_ITEMS_WRAPPER, s));

        validator.validate(new DOMSource(parser.parse(IOUtils.toInputStream(roundTripXml))));
    }

    /**
     * Insures that the DataItem serialization doesn't contain any elements named 'password'.  This is a pretty
     * crude test which just looks for strings in the serialization.
     *
     * @throws Exception
     */
    @Test
    public void testSerializationDoesNotContainPassword() throws Exception {
        final String actualXml = x.toXML(dataItemOne);

        assertFalse("Serialized XML may contain a password element!", actualXml.contains(PersonConverter.E_PASSWORD));
        assertFalse("Test XML may contain a password element!", XML.contains(PersonConverter.E_PASSWORD));

    }

    @Test
    public void testUnmarshal() {
        DataItem actual = (DataItem) x.fromXML(XML);
        assertEquals(dataItemOne, actual);
        assertEquals(dataItemOne, x.fromXML(x.toXML(dataItemOne)));
    }

    /**
     * Test that there are no errors when converting DataItems with empty fields.
     */
    @Test
    public void emptyFieldsTest() {

        // Make sure were equal to start
        assertEquals(dataItemOne, x.fromXML(x.toXML(dataItemOne)));

        // Test no name.
        dataItemOne.setName(null);
        assertEquals(dataItemOne, x.fromXML(x.toXML(dataItemOne)));

        // Test no description.
        dataItemOne.setDescription(null);
        assertEquals(dataItemOne, x.fromXML(x.toXML(dataItemOne)));

        // Test no depositor
        dataItemOne.setDepositorId(null);
        assertEquals(dataItemOne, x.fromXML(x.toXML(dataItemOne)));

        // Test no deposit date
        dataItemOne.setDepositDate(null);
        assertEquals(dataItemOne, x.fromXML(x.toXML(dataItemOne)));

    }

}
