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
package org.dataconservancy.model.builder;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.NoSuchElementException;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.dataconservancy.model.builder.ModelConstants.E_FILE;
import static org.dataconservancy.model.builder.ModelConstants.E_FILES;
import static org.dataconservancy.model.builder.ModelConstants.NS_URI;
import static org.dataconservancy.model.builder.ModelConstants.Q_DCP;
import static org.dataconservancy.model.builder.ModelConstants.Q_DU;
import static org.dataconservancy.model.builder.ModelConstants.Q_DUS;
import static org.dataconservancy.model.builder.ModelConstants.Q_FILE;
import static org.dataconservancy.model.builder.ModelConstants.Q_FILES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class WrappingEventReaderTest {

    // Event Types Crib Sheet
    // 1  START_ELEMENT
    // 2  END_ELEMENT
    // 3  PROCESSING INSTRUCTION
    // 4  CHARACTERS
    // 5  COMMENT
    // 6  SPACE
    // 7  START_DOCUMENT
    // 8  END_DOCUMENT
    // 9  ENTITY REFERENCE
    // 10 ATTRIBUTE
    // 11 DTD
    // 12 CDATA
    // 13 NAMESPACE
    // 14 NOTATION DECLARATION
    // 15 ENTITY DECLARATION

    private final String NULL_DOC = null;
    private final String EMPTY_DOC = "";
    private final String EMPTY_SINGLE_ELEMENT_DOC = "<doc/>";
    private final String EMPTY_SINGLE_ELEMENT_DOC_2 = "<doc></doc>";

    private final String SINGLE_ELEMENT_DOC_WHITESPACE = "<doc> </doc>";

    private final String SINGLE_ELEMENT_DOC_WHITESPACE_1 = "<File><doc> </doc></File>";
    private final String SINGLE_ELEMENT_DOC_WHITESPACE_2 = "<Files><File><doc> </doc></File></Files>";

    private final String DU_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!--\n" +
            "  Represents a Deliverable Unit with optional attributes and elements present.\n" +
            "  \n" +
            "  Uses <![CDATA[..]> to handle reserved XML characters.\n" +
            "-->\n" +
            "<DeliverableUnit xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\" id=\"urn:DeliverableUnit:2\">\n" +
            "  \n" +
            "  <collection ref=\"urn:Collection:4\"/>\n" +
            "  <collection ref=\"urn:Collection:5\"/>\n" +
            "  \n" +
            "  <parent ref=\"urn:DeliverableUnit:54\"/>\n" +
            "  <parent ref=\"urn:DeliverableUnit:100\"/>\n" +
            "  \n" +
            "  <type>music</type>  \n" +
            "  <title>The Twilight Saga: Eclipse (Original Motion Picture Soundtrack) [Deluxe] [+digital booklet]</title>\n" +
            "  <creator>Amazon.com</creator>\n" +
            "  <subject>music</subject>\n" +
            "  <subject>soundtracks</subject>\n" +
            "  <subject>twilight</subject>\n" +
            "  <formerExternalRef><![CDATA[http://www.amazon.com/Twilight-Saga-Eclipse-Original-Soundtrack/dp/B003P8BB5W/ref=pd_ts_zgc_dmusic_digital_music_album_display_on_website_4?ie=UTF8&s=dmusic&pf_rd_p=1264325582&pf_rd_s=right-3&pf_rd_t=101&pf_rd_i=163856011&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=0Z1QZ9YA0GP2ZE7VRHEW]]></formerExternalRef>  \n" +
            "  <digitalSurrogate>false</digitalSurrogate>\n" +
            "  \n" +
            "  <metadata schemaURI=\"http://amazon.com/schema.xsd\">\n" +
            "    <amzn:md xmlns:amzn=\"http://www.amazon.com\">\n" +
            "      <amzn:origReleaseDate>Original Release Date: May 25, 2010</amzn:origReleaseDate>\n" +
            "      <amzn:releaseDate>Release Date: May 25, 2010</amzn:releaseDate>\n" +
            "      <amzn:releaseLabel>Label: Chop Shop/Atlantic</amzn:releaseLabel>\n" +
            "      <amzn:copyright><![CDATA[TM & 2010 Summit Entertainment, LLC. All rights reserved]]></amzn:copyright>\n" +
            "      <amzn:totalLength>1:18:29</amzn:totalLength>\n" +
            "      <amzn:genre>Soundtracks</amzn:genre>\n" +
            "      <amzn:genre>General</amzn:genre>\n" +
            "      <amzn:asin>B003P8BB5W</amzn:asin>\n" +
            "    </amzn:md>\n" +
            "  </metadata>\n" +
            "  \n" +
            "  <relationship ref=\"urn:DeliverableUnit:221\" rel=\"amzn:book\"/>\n" +
            "  \n" +
            "</DeliverableUnit>";

    private final XMLInputFactory readerFactory = XMLInputFactory.newInstance();

    @Test
    public void testNextEventTypeWithWrapperEventReader() throws Exception {
        final InputStream xmlIn = IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE);
        commonNextEventTypeTest(new WrappingEventReader(readerFactory.createXMLEventReader(xmlIn)));
    }    
    
    @Test
    public void testNextEventTypeWithDefaultEventReader() throws Exception {
        final InputStream xmlIn = IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE);
        commonNextEventTypeTest(readerFactory.createXMLEventReader(xmlIn));
    }
    
    private void commonNextEventTypeTest(XMLEventReader reader) throws XMLStreamException {
        assertTrue(reader.hasNext());
        assertEquals(START_DOCUMENT, reader.nextEvent().getEventType());
        assertTrue(reader.hasNext());
        assertEquals(START_ELEMENT, reader.nextEvent().getEventType());
        assertTrue(reader.hasNext());
        assertEquals(CHARACTERS, reader.nextEvent().getEventType());
        assertTrue(reader.hasNext());
        assertEquals(END_ELEMENT, reader.nextEvent().getEventType());
        commonEndDocumentTest(reader);
    }

    @Test
    public void testNextEventTypeWrappingLevelOneWithWrapperEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>
        // But the wrapper should wrap it to:
        // <dcp xmlns="http://dataconservancy.org/schemas/dcm/1.0"><Files><File><doc> </doc></File></Files></dcp>

        final InputStream xmlIn = IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_1);
        final WrappingEventReader reader = new WrappingEventReader(readerFactory.createXMLEventReader(xmlIn));

        commonWrappedEventTypeTest(reader);

        commonEndDocumentTest(reader);
    }

    private void commonWrappedEventTypeTest(WrappingEventReader reader) throws XMLStreamException {
        assertTrue(reader.hasNext()); // START_DOC
        XMLEvent e = reader.nextEvent();
        assertEquals(START_DOCUMENT, e.getEventType());

        assertTrue(reader.hasNext()); // START_ELEMENT dcp
        e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(Q_DCP, e.asStartElement().getName());

        assertTrue(reader.hasNext()); // START_ELEMENT Files
        e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(E_FILES, e.asStartElement().getName().getLocalPart());

        commonFileWrappingTest(reader);

        assertTrue(reader.hasNext()); // END_ELEMENT Files
        assertEquals(END_ELEMENT, reader.nextEvent().getEventType());

        assertTrue(reader.hasNext()); // END_ELEMENT dcp
        assertEquals(END_ELEMENT, reader.nextEvent().getEventType());
    }

    @Test
    public void testNextEventTypeWrappingFileWithDefaultEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>

        final InputStream xmlIn = IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_1);
        final XMLEventReader reader = readerFactory.createXMLEventReader(xmlIn);

        assertTrue(reader.hasNext()); // START_DOC
        assertEquals(START_DOCUMENT, reader.nextEvent().getEventType());

        commonFileWrappingTest(reader);

        commonEndDocumentTest(reader);
    }

    @Test
    public void testNextEventTypeWrappingFilesWithWrapperEventReader() throws Exception {
        // The input XML is:
        // <Files><File><doc> </doc></File></Files>
        // But the wrapper should wrap it to:
        // <dcp xmlns="http://dataconservancy.org/schemas/dcm/1.0"><Files><File><doc> </doc></File></Files></dcp>

        final InputStream xmlIn = IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_2);
        final WrappingEventReader reader = new WrappingEventReader(readerFactory.createXMLEventReader(xmlIn));

        commonWrappedEventTypeTest(reader);

        commonEndDocumentTest(reader);
    }

    @Test
    public void testNextEventTypeWrappingFilesWithDefaultEventReader() throws Exception {
        // The input XML is:
        // <Files><File><doc> </doc></File></Files>

        final InputStream xmlIn = IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_2);
        final XMLEventReader reader = readerFactory.createXMLEventReader(xmlIn);

        assertTrue(reader.hasNext()); // START_DOC
        assertEquals(START_DOCUMENT, reader.nextEvent().getEventType());

        commonFilesWrappingTest(reader);

        commonEndDocumentTest(reader);
    }

    private void commonFilesWrappingTest(XMLEventReader reader) throws XMLStreamException {
        assertTrue(reader.hasNext()); // START_ELEMENT Files
        assertEquals(START_ELEMENT, reader.nextEvent().getEventType());

        commonFileWrappingTest(reader);

        assertTrue(reader.hasNext()); // END_ELEMENT Files
        assertEquals(END_ELEMENT, reader.nextEvent().getEventType());
    }

    private void commonFileWrappingTest(XMLEventReader reader) throws XMLStreamException {
        assertTrue(reader.hasNext()); // START_ELEMENT File
        XMLEvent e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(E_FILE, e.asStartElement().getName().getLocalPart());

        assertTrue(reader.hasNext()); // START_ELEMENT doc
        e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals("doc", e.asStartElement().getName().getLocalPart());

        assertTrue(reader.hasNext()); // CHARACTERS
        e = reader.nextEvent();
        assertEquals(CHARACTERS, e.getEventType());

        assertTrue(reader.hasNext()); // END_ELEMENT doc
        e = reader.nextEvent();
        assertEquals(END_ELEMENT, e.getEventType());
        assertEquals("doc", e.asEndElement().getName().getLocalPart());

        assertTrue(reader.hasNext()); // END_ELEMENT File
        e = reader.nextEvent();
        assertEquals(END_ELEMENT, e.getEventType());
        assertEquals(E_FILE, e.asEndElement().getName().getLocalPart());
    }

    private void commonEndDocumentTest(XMLEventReader reader) throws XMLStreamException {
        assertTrue(reader.hasNext());
        assertEquals(END_DOCUMENT, reader.nextEvent().getEventType());

        assertFalse(reader.hasNext());

        try {
            reader.nextEvent();
            fail("Expected NoSuchElementException to be thrown.");
        } catch (NoSuchElementException e) {
            // expected
        }
    }
    
    @Test
    @Ignore("TODO")
    public void testHasNext() throws Exception {

    }

    @Test
    @Ignore("TODO")
    public void testNext() throws Exception {

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveWithDefaultEventReader() throws Exception {
        final XMLEventReader reader = readerFactory.createXMLEventReader(IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE));
        assertTrue(reader.hasNext());
        reader.nextEvent();
        reader.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveWithWrapperEventReader() throws Exception {
        final XMLEventReader reader = new WrappingEventReader(readerFactory.createXMLEventReader(
                IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE)));
        assertTrue(reader.hasNext());
        reader.nextEvent();
        reader.remove();
    }

    @Test
    public void testPeekWithDefaultEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>

        final XMLEventReader reader = readerFactory.createXMLEventReader(
                IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_1));

        // START_DOCUMENT
        assertEquals(START_DOCUMENT, reader.peek().getEventType());
        reader.nextEvent();

        commonPeekFileTest(reader);

        // END_DOCUMENT
        assertEquals(END_DOCUMENT, reader.peek().getEventType());

        commonEndDocumentTest(reader);
    }

    @Test
    public void testPeekWithWrapperEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>
        // But the wrapper should wrap it to:
        // <dcp xmlns="http://dataconservancy.org/schemas/dcm/1.0"><Files><File><doc> </doc></File></Files></dcp>

        final XMLEventReader reader = new WrappingEventReader(readerFactory.createXMLEventReader(
                IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_1)));

        // START_DOCUMENT
        assertEquals(START_DOCUMENT, reader.peek().getEventType());
        // idempotent
        assertEquals(START_DOCUMENT, reader.peek().getEventType());
        assertEquals(START_DOCUMENT, reader.nextEvent().getEventType());

        // <dcp>
        assertEquals(START_ELEMENT, reader.peek().getEventType());
        // idempotent
        assertEquals(START_ELEMENT, reader.peek().getEventType());
        assertEquals(START_ELEMENT, reader.nextEvent().getEventType());

        // <Files>
        assertEquals(START_ELEMENT, reader.peek().getEventType());
        // idempotent
        assertEquals(START_ELEMENT, reader.peek().getEventType());
        assertEquals(START_ELEMENT, reader.nextEvent().getEventType());

        commonPeekFileTest(reader);

        // </Files>
        assertEquals(END_ELEMENT, reader.peek().getEventType());
        // idempotent
        assertEquals(END_ELEMENT, reader.peek().getEventType());
        assertEquals(END_ELEMENT, reader.nextEvent().getEventType());

        // </dcp>
        assertEquals(END_ELEMENT, reader.peek().getEventType());
        // idempotent
        assertEquals(END_ELEMENT, reader.peek().getEventType());
        assertEquals(END_ELEMENT, reader.nextEvent().getEventType());

        // END_DOCUMENT
        assertEquals(END_DOCUMENT, reader.peek().getEventType());
        // idempotent
        assertEquals(END_DOCUMENT, reader.peek().getEventType());

        commonEndDocumentTest(reader);
    }

    private void commonPeekFileTest(XMLEventReader reader) throws XMLStreamException {
        // The input XML is:
        // <File><doc> </doc></File>

        // <File>
        XMLEvent peekEvent = reader.peek();
        assertEquals(START_ELEMENT, peekEvent.getEventType());
        assertEquals(E_FILE, peekEvent.asStartElement().getName().getLocalPart());
        // idempotent
        assertEquals(peekEvent, reader.peek());
        XMLEvent actualEvent = reader.nextEvent();
        assertEquals(peekEvent, actualEvent);

        // <doc>
        peekEvent = reader.peek();
        assertEquals(START_ELEMENT, peekEvent.getEventType());
        assertEquals("doc", peekEvent.asStartElement().getName().getLocalPart());
        // idempotent
        assertEquals(peekEvent, reader.peek());
        actualEvent = reader.nextEvent();
        assertEquals(peekEvent, actualEvent);

        peekEvent = reader.peek();
        assertEquals(CHARACTERS, peekEvent.getEventType());
        // idempotent
        assertEquals(peekEvent, reader.peek());
        actualEvent = reader.nextEvent();
        assertEquals(peekEvent, actualEvent);

        // </doc>
        peekEvent = reader.peek();
        assertEquals(END_ELEMENT, peekEvent.getEventType());
        assertEquals("doc", peekEvent.asEndElement().getName().getLocalPart());
        // idempotent
        assertEquals(peekEvent, reader.peek());
        actualEvent = reader.nextEvent();
        assertEquals(peekEvent, actualEvent);

        // </File>
        peekEvent = reader.peek();
        assertEquals(END_ELEMENT, peekEvent.getEventType());
        assertEquals(E_FILE, peekEvent.asEndElement().getName().getLocalPart());
        // idempotent
        assertEquals(peekEvent, reader.peek());
        actualEvent = reader.nextEvent();
        // fails, bc we are preserving namespaces? assertEquals(peekEvent, actualEvent);
        // do this instead
        assertEquals(peekEvent.asEndElement().getName().getLocalPart(), actualEvent.asEndElement().getName().getLocalPart());
    }

    @Test
    @Ignore("TODO")
    public void testGetElementText() throws Exception {
    }

    @Test
    @Ignore("FIXME: currently failing: java.lang.ClassCastException: com.sun.xml.internal.stream.events.StartElementEvent cannot be cast to javax.xml.stream.events.EndElement " +
	"at org.dataconservancy.model.builder.WrappingEventReaderTest.testNextTagWithWrappingEventReader(WrappingEventReaderTest.java:460)")
    public void testNextTagWithWrappingEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>
        // But the wrapper should wrap it to:
        // <dcp xmlns="http://dataconservancy.org/schemas/dcm/1.0"><Files><File><doc> </doc></File></Files></dcp>

        final XMLEventReader reader = new WrappingEventReader(readerFactory.createXMLEventReader(
                IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_1)));

        commonStartDocumentNextTagTest(reader);
        
        // <dcp>
//        assertEquals(START_ELEMENT, reader.nextTag().getEventType());

        // <Files>
        final XMLEvent filesStartEvent = reader.nextTag();
        assertEquals(Q_FILES, ((StartElement) filesStartEvent).getName());
        assertEquals(START_ELEMENT, filesStartEvent.getEventType());

        // <File>
        final XMLEvent fileStartEvent = reader.nextTag();
        assertEquals(Q_FILE.getLocalPart(), ((StartElement) fileStartEvent).getName().getLocalPart());
        assertEquals(START_ELEMENT, fileStartEvent.getEventType());

        commonNextTagTest(reader);

        // </Files>
        final XMLEvent filesEndEvent = reader.nextTag();
        assertEquals(Q_FILES, ((EndElement) filesEndEvent).getName());
        assertEquals(END_ELEMENT, filesEndEvent.getEventType());

        // </dcp>
        final XMLEvent dcpEndEvent = reader.nextTag();
        assertEquals(Q_DCP, ((EndElement) dcpEndEvent).getName());
        assertEquals(END_ELEMENT, dcpEndEvent.getEventType());

        commonEndDocumentNextTagTest(reader);
    }

    @Test
    @Ignore("DC-282")
    public void testNextTagWithDefaultEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>

        final XMLEventReader reader = readerFactory.createXMLEventReader(IOUtils.toInputStream(
                SINGLE_ELEMENT_DOC_WHITESPACE_1));

        commonStartDocumentNextTagTest(reader);

        commonNextTagTest(reader);

        commonEndDocumentNextTagTest(reader);
    }

    private void commonStartDocumentNextTagTest(XMLEventReader reader) throws XMLStreamException {
        try {
            reader.nextTag();
            fail("Expected XMLStreamException");
        } catch (XMLStreamException e) {
            // expected
        }

        // bug in default impl?  I would expect a call to nextTag() to not change the
        // state in the underlying reader if an exception is thrown.
        //assertEquals(START_DOCUMENT, reader.nextEvent().getEventType());

        // but instead we have this behavior
        assertEquals(START_ELEMENT, reader.nextEvent().getEventType());
    }

    private void commonEndDocumentNextTagTest(XMLEventReader reader) {
        try {
            reader.nextTag();
            fail("Expected XMLStreamException");
        } catch (XMLStreamException e) {
            // expected
        }
    }

    private void commonNextTagTest(XMLEventReader reader) throws XMLStreamException {
        // <File>
//        assertEquals(START_ELEMENT, reader.nextTag().getEventType());
        // <doc>
        assertEquals(START_ELEMENT, reader.nextTag().getEventType());

        // </doc>
        assertEquals(END_ELEMENT, reader.nextTag().getEventType());
        // </File>
        assertEquals(END_ELEMENT, reader.nextTag().getEventType());
    }

    @Test
    @Ignore("TODO")
    public void testGetProperty() throws Exception {

    }

    @Test
    public void testCloseWithDefaultEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>

        final XMLEventReader reader = readerFactory.createXMLEventReader(
                IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_1));

        commonCloseTest(reader);
    }

    @Test
    public void testCloseWithWrappingEventReader() throws Exception {
        // The input XML is:
        // <File><doc> </doc></File>
        // But the wrapper should wrap it to:
        // <dcp xmlns="http://dataconservancy.org/schemas/dcm/1.0"><Files><File><doc> </doc></File></Files></dcp>

        final XMLEventReader reader = new WrappingEventReader(readerFactory.createXMLEventReader(
                IOUtils.toInputStream(SINGLE_ELEMENT_DOC_WHITESPACE_1)));

        commonCloseTest(reader);
    }

    private void commonCloseTest(XMLEventReader reader) throws XMLStreamException {
        // The input XML is EITHER:
        // <File><doc> </doc></File>
        // OR
        // <dcp xmlns="http://dataconservancy.org/schemas/dcm/1.0"><Files><File><doc> </doc></File></Files></dcp>
        // This test works with either case.
        reader.nextEvent(); // START_DOC
        reader.nextEvent(); // START_ELEMENT

        reader.close(); // no-op

        reader.nextEvent(); // START_ELEMENT
    }

    @Test
    public void testDeliverableUnitWithWrappingEventReader() throws XMLStreamException {
        final XMLEventReader reader = new WrappingEventReader(readerFactory.createXMLEventReader(
                IOUtils.toInputStream(DU_XML)));

        XMLEvent e = reader.nextEvent();
        assertEquals(START_DOCUMENT, e.getEventType());

        e = reader.nextEvent();
        assertEquals(COMMENT, e.getEventType());

        e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(Q_DCP, e.asStartElement().getName());

        e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(Q_DUS, e.asStartElement().getName());

        e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(Q_DU, e.asStartElement().getName());

        e = reader.nextEvent();
        assertEquals(CHARACTERS, e.getEventType());
        
        e = reader.nextEvent();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(new QName(NS_URI, "collection"), e.asStartElement().getName());

        // Skip to the <metadata> element
        while ((e = reader.nextTag()) != null) {
            if (e.isStartElement() && e.asStartElement().getName().getLocalPart().equals("metadata")) {
                break;
            }
        }

        e = reader.nextTag();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(new QName("http://www.amazon.com", "md"), e.asStartElement().getName());

        e = reader.nextTag();
        assertEquals(START_ELEMENT, e.getEventType());
        assertEquals(new QName("http://www.amazon.com", "origReleaseDate"), e.asStartElement().getName());
    }        
}
