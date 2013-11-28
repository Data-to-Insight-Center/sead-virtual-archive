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

import com.thoughtworks.xstream.XStream;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.IOException;
import java.net.URL;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static org.junit.Assert.assertNotNull;


/**
 * Base test class for XStream converter tests.  It is meant to wire up the collaborating objects
 * needed for testing converters.  It also sets "global" XMLUnit test properties.
 */
public abstract class BaseConverterTest extends BaseModelTest {

    /**
     * The XStream instance, configured by {@link #setUp()}, and provided by {@link #getXstreamInstance()}
     */
    XStream x;

    /**
     * The XstreamBusinessObjectFactory, instantiated and configured by {@link #setUp()}.
     */
    XstreamBusinessObjectFactory boFactory;

    /**
     * A JAXP {@link Validator} instance, ready to validate Business Object Package serializations.
     */
    Validator validator;

    /**
     * A JAXP namespace-aware, non-validating {@link DocumentBuilder parser}, ready to parse Business Object Package
     * XML.  Validation is performed using the {@link #validator}.
     */
    DocumentBuilder parser;

    /**
     * The XML namespace used for Business Object Package serializations
     */
    static final String BOP_XMLNS = "http://dataconservancy.org/schemas/bop/1.0";

    /**
     * Used to wrap serializations in a {@literal <bop>} element for validation purposes.  Meant to be used
     * by {@link String#format(String, Object...)}.
     */
    static final String BOP_WRAPPER = "<bop " + XMLNS_ATTRIBUTE + "=\"" + BOP_XMLNS + "\" xsi:schemaLocation=\"" +
            BOP_XMLNS + " " + BOP_XMLNS + "\" " + XMLNS_ATTRIBUTE + ":xsi=\"" + W3C_XML_SCHEMA_INSTANCE_NS_URI +
            "\">\n" + "%s\n</bop>";

    /**
     * The classpath resource to the Business Object Package schema.
     */
    private static final String BOP_SCHEMA_RESOURCE = "/businessObject.xsd";

    /**
     * This method performs the following steps in order:
     * <ol>
     *     <li>Sets "global" XMLUnit properties.</li>
     *     <li>Obtains a UserService instance and sets it as package-private member, available for subclasses</li>
     *     <li>Instantiates a {@link XstreamBusinessObjectFactory}, obtains instances of the factory's collaborators, and
     * injects them into the factory.  The factory is set a package-private member, available for subclasses</li>
     *     <li>Obtains an instance of XStream, sets it as package-private member, available for subclasses</li>
     * </ol>
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        setXmlUnitOptions();

        boFactory = new XstreamBusinessObjectFactory();

        x = getXstreamInstance();
    }

    /**
     * Sets up a JAXP {@link DocumentBuilder} and {@link Validator} that subclasses can use to validate Business
     * Object Package XML.  The parser is a non-validating, namespace-aware parser.  Validation is meant to happen
     * by invoking the validator.
     * <p/>
     * Subclasses are expected to use the {@link #validator} and {@link #parser} member variables set by this method
     * to perform parsing and validating.
     * <p/>
     * Example usage, taken from {@link org.dataconservancy.ui.model.builder.xstream.DataItemConverterTest#testMarshalIsValid()}:
     * <pre>
        // Verify assumptions: that our expected XML is valid.  First, we wrap it in a BOP.
        final String expectedXml = String.format(BOP_WRAPPER, String.format(DATA_ITEMS_WRAPPER, XML));

        // validate the DOM tree
        validator.validate(new DOMSource(parser.parse(IOUtils.toInputStream(expectedXml))));

        // Verify the DataItem converter produces valid XML
        final String actualConvertedXml = x.toXML(di1);
        final String actualXml = String.format(BOP_WRAPPER, String.format(DATA_ITEMS_WRAPPER, actualConvertedXml));

        // validate the DOM tree
        validator.validate(new DOMSource(parser.parse(IOUtils.toInputStream(actualXml))));

        // Round Trip: XML to DataItem to XML
        final String s = x.toXML(x.fromXML(XML));
        String roundTripXml = String.format(BOP_WRAPPER, String.format(DATA_ITEMS_WRAPPER, s));

        validator.validate(new DOMSource(parser.parse(IOUtils.toInputStream(roundTripXml))));
     * </pre>
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @Before
    public void setUpParserAndValidator() throws ParserConfigurationException, SAXException, IOException {
        // Create a namespace-aware parser that will parse XML into a DOM tree.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        parser = dbf.newDocumentBuilder();

        // Create a SchemaFactory
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // Load the schema
        final URL businessObjectSchema = this.getClass().getResource(BOP_SCHEMA_RESOURCE);
        assertNotNull("Unable to resolve the Business Object Package schema from the classpath: " + BOP_SCHEMA_RESOURCE,
                businessObjectSchema);
        Source schemaFile = new StreamSource(businessObjectSchema.openStream());
        Schema schema = factory.newSchema(schemaFile);

        // Create a Validator instance, which can be used to validate an instance document
        validator = schema.newValidator();
    }

    /**
     * Obtain an instance of {@link CollectionConverter}, used by the {@link XstreamBusinessObjectFactory}.  By default
     * this implementation constructs a new instance with {@link #userService}.
     * <p/>
     * Subclasses can override this method to return their own instance of {@code CollectionConverter}.
     *
     * @return a CollectionConverter instance
     */
    CollectionConverter getCollectionConverter() {
        return new CollectionConverter();
    }

    /**
     * Obtain an instance of {@link ContactInfoConverter}, used by the {@link XstreamBusinessObjectFactory}.
     * <p/>
     *
     * @return a ContactInfoConverter instance
     */
    ContactInfoConverter getContactInfoConverter() {
        return new ContactInfoConverter();
    }

    /**
     * Obtain an instance of {@link ProjectConverter}, used by the {@link XstreamBusinessObjectFactory}.  By default
     * this implementation constructs a new instance with {@link #userService}.
     * <p/>
     * Subclasses can override this method to return their own instance of {@code ProjectConverter}.
     *
     * @return a ProjectConverter instance
     */
    ProjectConverter getProjectConverter() {
        return new ProjectConverter();
    }

    /**
     * Obtain an instance of {@link DataItemConverter}, used by the {@link XstreamBusinessObjectFactory}.  By default
     * this implementation constructs a new instance with {@link #userService}.
     * <p/>
     * Subclasses can override this method to return their own instance of {@code DataItemConverter}.
     *
     * @return a ProjectConverter instance
     */
    DataItemConverter getDataItemConverter() {
        return new DataItemConverter();
    }

    /**
     * Obtain a configured instance of {@code XStream}, ready to marshal and unmarshal objects.  By default this
     * implementation constructs a new instance using {@link #boFactory}.
     * <p/>
     * Subclasses can override this method to return their own instance of {@code XStream}.
     *
     * @return a XStream instance
     */
    XStream getXstreamInstance() {
        return boFactory.createInstance();
    }

    /**
     * Sets "global" properties of XMLUnit that should be shared by all the tests.
     */
    void setXmlUnitOptions() {
        // XMLUnit options shared by all the tests
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }
}
