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

import com.thoughtworks.xstream.io.xml.QNameMap;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.DcsModelVersion;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CopyingStaxReaderTest {

    private XMLInputFactory xmlInputFactory;
    private XMLOutputFactory xmlOutputFactory;

    @Before
    public void setUp() {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testSimpleCopy() throws Exception {
        final String XML = "<foo>bar</foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                streamReader);

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithAttributes() throws Exception {
        final String XML = "<foo baz=\"biz\">bar</foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                xmlInputFactory.createXMLStreamReader(new StringReader(XML)));

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithAttributesAndNestedElement() throws Exception {
        final String XML = "<foo baz=\"biz\"><bar id=\"anId\">I love pancakes</bar></foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                xmlInputFactory.createXMLStreamReader(new StringReader(XML)));

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithNamespaces() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String XML = "<foo xmlns=\"" + xmlns + "\" baz=\"biz\"><bar id=\"anId\">I love pancakes</bar></foo>";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                xmlInputFactory.createXMLStreamReader(new StringReader(XML)));

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithMultipleDefaultNamespaces() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String XML = "<foo xmlns=\"" + xmlns + "\" baz=\"biz\"><bar xmlns=\"urn:unf\" id=\"anId\">I love pancakes</bar></foo>";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                xmlInputFactory.createXMLStreamReader(new StringReader(XML)));

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithPrefixedNamespaces() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String XML = "<foo xmlns=\"" + xmlns + "\" xmlns:unf=\"urn:unf\" baz=\"biz\"><unf:bar id=\"anId\">I love pancakes</unf:bar></foo>";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                xmlInputFactory.createXMLStreamReader(new StringReader(XML)));

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        //assertEquals(XML, result);
    }

    @Test
    public void testCopyWithPrefixedNamespacesWithWhiteSpace() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String XML = "<foo xmlns=\"" + xmlns + "\" xmlns:unf=\"urn:unf\" baz=\"biz\">\n" +
                           "  <unf:bar id=\"anId\">I love pancakes</unf:bar>\n" +
                           "</foo>";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                xmlInputFactory.createXMLStreamReader(new StringReader(XML)));

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        //assertEquals(XML, result);
    }

    @Test
    public void testCopyWithMetadataXml() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String schemaUri = "http://sdss.org/metadata/astroSchema.example.xsd";
        final String mdBlob = "<astro:md xmlns:astro=\"http://sdss.org/astro\">\n" +
            "          <astro:skyCoverage>all of it</astro:skyCoverage>\n" +
            "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n" +
            "        </astro:md>\n";

        final String XML = "<metadata xmlns=\"" + xmlns + "\" contentType=\"xml\" schemaURI=\"" + schemaUri + "\">\n" + mdBlob + "</metadata>";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final DcsStaxReader underTest = new DcsStaxReader(new QNameMap(),
                xmlInputFactory.createXMLStreamReader(new StringReader(XML)));

        underTest.copyNode(out);

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        //assertEquals(XML, result);
    }
}
