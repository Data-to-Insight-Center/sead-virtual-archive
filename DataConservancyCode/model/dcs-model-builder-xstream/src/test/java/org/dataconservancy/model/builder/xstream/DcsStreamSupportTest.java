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
import org.custommonkey.xmlunit.XMLUnit;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DcsStreamSupportTest {

    private XMLInputFactory xmlInputFactory;
    private XMLOutputFactory xmlOutputFactory;
    private DcsStreamSupport underTest;

    @Before
    public void setUp() {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLUnit.setIgnoreWhitespace(true);

        underTest = new DcsStreamSupport();
    }

    @Test
    public void testSimple() throws XMLStreamException, IOException, SAXException {
        final String XML = "<foo>bar</foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithAttributes() throws Exception {
        final String XML = "<foo baz=\"biz\">bar</foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithAttributesAndNestedElement() throws Exception {
        final String XML = "<foo baz=\"biz\"><bar id=\"anId\">I love pancakes</bar></foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithNamespaces() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String XML = "<foo xmlns=\"" + xmlns + "\" baz=\"biz\"><bar id=\"anId\">I love pancakes</bar></foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithMultipleDefaultNamespaces() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String XML = "<foo xmlns=\"" + xmlns + "\" baz=\"biz\"><bar xmlns=\"urn:unf\" id=\"anId\">I love pancakes</bar></foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithPrefixedNamespaces() throws Exception {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String XML = "<foo xmlns=\"" + xmlns + "\" xmlns:unf=\"urn:unf\" baz=\"biz\"><unf:bar id=\"anId\">I love pancakes</unf:bar></foo>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

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

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

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

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

    @Test
    public void testCopyWithMetadataXmlDefaultNamespaceMetadataContainer() throws XMLStreamException, IOException, SAXException {
        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
        final String schemaUri = "http://sdss.org/metadata/astroSchema.example.xsd";

        // Note: <md> element is in the default namespace, which, for this test, is the DCP namespace.
        final String mdBlob = "<md xmlns:astro=\"http://sdss.org/astro\">\n" +
                "          <astro:skyCoverage>all of it</astro:skyCoverage>\n" +
                "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n" +
                "        </md>\n";
        final String XML = "<metadata xmlns=\"" + xmlns + "\" schemaURI=\"" + schemaUri + "\">\n" + mdBlob + "</metadata>";
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(new StringReader(XML));
        final XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(out);

        underTest.copyNode(streamReader, streamWriter);
        streamWriter.close();

        final String result = new String(out.toByteArray());
        XMLAssert.assertXMLEqual("Expected: " + XML + "\n Actual: " + result + "\n", XML, result);
        assertEquals(XML, result);
    }

//    @Test
//    @Ignore("Not a real test, only used to look at namespace behaviors")
//    public void testNamespaceContext() throws XMLStreamException {
//        assertEquals("", XMLConstants.DEFAULT_NS_PREFIX);
//        assertEquals("", XMLConstants.NULL_NS_URI);
//        final String xmlns = DcpModelVersion.VERSION_1_0.getXmlns();
//        final String schemaUri = "http://sdss.org/metadata/astroSchema.example.xsd";
//
//        // Note: <md> element is in the default namespace, which, for this test, is the DCP namespace.
//        final String mdBlob = "<md xmlns:astro=\"http://sdss.org/astro\">\n" +
//            "          <astro:skyCoverage>all of it</astro:skyCoverage>\n" +
//            "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n" +
//            "        </md>\n";
//        final String testXml = "<metadata xmlns=\"" + xmlns + "\" schemaURI=\"" + schemaUri + "\">\n" + mdBlob + "</metadata>";
//
//        final XMLEventReader streamReader = xmlInputFactory.createXMLEventReader(new StringReader(testXml));
//
//        while (streamReader.hasNext()) {
//            XMLEvent e = streamReader.nextEvent();
//            if (!e.isStartElement()) {
//                continue;
//            }
//            String prefix = e.asStartElement().getName().getPrefix();
//
//            Iterator<Namespace> nsItr = e.asStartElement().getNamespaces(); // returns xmlns attributes and values on the current element
//            if (!nsItr.hasNext()) {
//                System.out.println(e.asStartElement().getName() + " has no namespaces (startElement.getNamespaces())");
//            } else {
//                System.out.println(e.asStartElement().getName() + " iterating over namespaces (startElement.getNamespaces())");
//            }
//            while (nsItr.hasNext()) {
//                final Namespace namespace = nsItr.next();
//                System.out.println(e.asStartElement().getName() + ": " + namespace + " (is default? "  + namespace.isDefaultNamespaceDeclaration() + ")");
//            }
//
//            NamespaceContext nsCtx = e.asStartElement().getNamespaceContext();
//            String nsUri = nsCtx.getNamespaceURI(prefix);
//            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
//                System.out.println(e.asStartElement().getName() + ": default prefix '" + prefix + "' bound to '" + nsUri + "'");
//            }
//            if (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
//                System.out.println(e.asStartElement().getName() + ": default prefix '" + XMLConstants.DEFAULT_NS_PREFIX + "' bound to '" + nsCtx.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX) + "'");
//                System.out.println(e.asStartElement().getName() + ": prefix '" + prefix + "' bound to " + nsCtx.getNamespaceURI(prefix));
//            }
//
//            String containerNs = nsCtx.getNamespaceURI("astro");
//            boolean containerPrefixIsBound = (containerNs != null && !containerNs.equals(XMLConstants.NULL_NS_URI));
//            System.out.println("astro prefix is bound: " + containerPrefixIsBound);
//        }
//    }

}
