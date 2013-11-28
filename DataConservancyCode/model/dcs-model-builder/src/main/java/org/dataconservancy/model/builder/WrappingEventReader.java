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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.dataconservancy.model.builder.ModelConstants.E_COLLECTION;
import static org.dataconservancy.model.builder.ModelConstants.E_COLLECTIONS;
import static org.dataconservancy.model.builder.ModelConstants.E_DU;
import static org.dataconservancy.model.builder.ModelConstants.E_DUS;
import static org.dataconservancy.model.builder.ModelConstants.E_EVENT;
import static org.dataconservancy.model.builder.ModelConstants.E_EVENTS;
import static org.dataconservancy.model.builder.ModelConstants.E_FILE;
import static org.dataconservancy.model.builder.ModelConstants.E_FILES;
import static org.dataconservancy.model.builder.ModelConstants.E_MANIFESTATION;
import static org.dataconservancy.model.builder.ModelConstants.E_MANIFESTATIONS;
import static org.dataconservancy.model.builder.ModelConstants.Q_COLLECTIONS;
import static org.dataconservancy.model.builder.ModelConstants.Q_DCP;
import static org.dataconservancy.model.builder.ModelConstants.Q_DUS;
import static org.dataconservancy.model.builder.ModelConstants.Q_EVENTS;
import static org.dataconservancy.model.builder.ModelConstants.Q_FILES;
import static org.dataconservancy.model.builder.ModelConstants.Q_MANIFESTATIONS;

/**
 * <em>DO NOT USE</em>
 *
 * Intended to wrap DCS entities in DCP packaging, so they could be valid XML.
 *
 * Does not work properly.
 */
class WrappingEventReader implements XMLEventReader {
    private enum Mode {
        WRAPPING, PASSTHRU
    }

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * The XMLEventReader that we are wrapping.
     */
    private final XMLEventReader reader;

    /**
     * Factory for producing XMLEvents.
     */
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    /**
     * The QNames used to generate events that wrap the reader.
     * Populated by {@link #prepareWrappingStack(javax.xml.namespace.QName)} via a call to
     * {@link #setMode(javax.xml.stream.events.XMLEvent)}
     */
    private ListIterator<QName> wrapperQnames = new ArrayList<QName>().listIterator();

    /**
     * Whether or not we are wrapping a stream with artificial events or just passing events back.  Set
     * by calling {@link #setMode(javax.xml.stream.events.XMLEvent)}
     */
    private Mode mode;

    private QName readerRootElement;

    private boolean seenEndDocument = false;
    private boolean seenStartDocument = false;
    private boolean sentEndDocument = false;

    public WrappingEventReader(XMLEventReader reader) {
        this.reader = reader;
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        log.trace("Entering nextEvent()");
        XMLEvent e = reader.peek();
        if (log.isTraceEnabled() && e != null) {
            log.trace("next event type is {}", e.getEventType());
        }

        // This happens when nextEvent() is called after an END_DOCUMENT event.
        // Let the underlying reader decide how to handle this.
        if (e == null) {
            return reader.nextEvent();
        }

        setMode(e);


        switch (e.getEventType()) {

            // If starting an element...
            case XMLStreamConstants.START_ELEMENT:

                // If we are wrapping, and there are still wrapped events to send...
                if (wrapperQnames.hasPrevious() && mode == Mode.WRAPPING) {
                    // ...get and return the next wrapped event
                    return getNextWrappedStartElementEvent();
                }

                return reader.nextEvent();


            // If ending a document...
            case XMLStreamConstants.END_DOCUMENT:

                // record we've seen the END_DOCUMENT event
                seenEndDocument = true;

                // If we're wrapping, and there are still wrapped events to send...
                if (wrapperQnames.hasNext() && mode == Mode.WRAPPING) {
                    // ...get and return the next wrapped event
                    return getNextWrappedEndElementEvent();
                } else {
                    // ... otherwise send the actual END_DOCUMENT event
                    sentEndDocument = true;
                    return reader.nextEvent();
                }

                // If starting a document...
            case XMLStreamConstants.START_DOCUMENT:
                seenStartDocument = true;
                return reader.nextEvent();

//           If ending an element
            case XMLStreamConstants.END_ELEMENT:
                if (e.asEndElement().getName().equals(readerRootElement)) {
                    // we don't want DCP namespaces to go out of scope.
                    // read the actual event
                    final EndElement actualEvent = reader.nextEvent().asEndElement();
                    return eventFactory.createEndElement(actualEvent.getName(), null);
                }
                return reader.nextEvent();
            
                // ... otherwise just return the next real event

            default:
                return reader.nextEvent();
        }

    }

    @Override
    public boolean hasNext() {
        log.trace("Entering hasNext()");
        try {
            setMode(reader.peek());
        } catch (XMLStreamException e) {
            // TODO handle exception
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (mode == Mode.WRAPPING) {
            if (sentEndDocument) {
                return false;
            }
            return true;
        }

        return reader.hasNext();
    }

    @Override
    public Object next() {
        log.trace("Entering next()");
        if (hasNext()) {
            try {
                return nextEvent();
            } catch (XMLStreamException e) {
                throw new NoSuchElementException(e.getMessage());
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        log.trace("Entering remove()");
        throw new UnsupportedOperationException();
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        log.trace("Entering peek()");
        setMode(reader.peek());

        if (mode == Mode.WRAPPING) {
            if (sentEndDocument) {
                return null;
            }            
            if (seenEndDocument || reader.peek().getEventType() == XMLStreamConstants.END_DOCUMENT) {
                if (wrapperQnames.hasNext()) {
                    final QName toPeekAt = wrapperQnames.next();
                    wrapperQnames.previous(); // reset
                    return eventFactory.createEndElement(toPeekAt, null);
                } else {
                    return eventFactory.createEndDocument();
                }
            } else {
                if (wrapperQnames.hasPrevious()) {
                    final QName toPeekAt = wrapperQnames.previous();
                    wrapperQnames.next(); // reset
                    return createStartElementEvent(toPeekAt);
                } else {
                    return reader.peek();
                }
            }
        }
        return reader.peek();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        log.trace("Entering getElementText()");
        setMode(reader.peek());

        if (mode == Mode.WRAPPING) {
            if (seenStartDocument && wrapperQnames.hasPrevious()) {
                return "";
            } else if (seenEndDocument && wrapperQnames.hasNext()) {
                return "";
            } else {
                return reader.getElementText();
            }
        }

        return reader.getElementText();
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        log.trace("Entering nextTag()");
        final XMLEvent nextEvent = reader.peek();
        setMode(nextEvent);

        if (mode == Mode.WRAPPING) {
            if (seenStartDocument && wrapperQnames.hasPrevious()) {
                return getNextWrappedStartElementEvent();
            } else if (seenEndDocument && wrapperQnames.hasNext()) {
                return getNextWrappedEndElementEvent();
            }
        }


        try {
            XMLEvent e = reader.nextTag();
            return e;
        } catch (javax.xml.stream.XMLStreamException e) {
            if (mode == Mode.PASSTHRU) {
                throw e;
            } else {
                // TODO: when nextTag() throws an exception, it changes the state of the underlying reader.
                // For example:
                // assertEquals(START_DOCUMENT, reader.peek());  // ok
                // reader.nextTag()                              // exception thrown, which is OK according to nextTag()'s javadoc
                // assertEquals(START_DOCUMENT, reader.peek());  // fails.  The underlying stream has advanced
                // assertEquals(START_ELEMENT, reader.peek());   // succeeds

                // So now we are in a situation where the underlying reader has advanced, and we don't have a
                // way to sync up the state of the underlying reader with this wrapper implementation.

                if (nextEvent.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                    seenStartDocument = true;
                }

                if (nextEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    seenStartDocument = true;
                    setMode(nextEvent);
                }

                if (nextEvent.getEventType() == XMLStreamConstants.END_DOCUMENT) {
                    seenStartDocument = true;
                    seenEndDocument = true;
                }

                if (wrapperQnames.hasNext()) {
                    return getNextWrappedEndElementEvent();
                }

                throw e;
            }
        }
    }

    @Override
    public Object getProperty(String s) throws IllegalArgumentException {
        log.trace("Entering getProperty()");
        return reader.getProperty(s);
    }

    @Override
    public void close() throws XMLStreamException {
        log.trace("Entering close()");
        reader.close();
    }

    /**
     * This method determines whether or not the incoming XML stream needs to be wrapped. If the incoming stream
     * needs to be wrapped, {@link #mode} will be set to {@link Mode#WRAPPING}, otherwise it will be set to
     * {@link Mode#PASSTHRU}.  The <code>mode</code> will only be updated when the following conditions are met:
     * <ul>
     * <li>The event <code>e</code> must be <code>START_ELEMENT</code></li>
     * <li>A <code>START_DOCUMENT</code> event must have already been seen</li>
     * <li>The <code>mode</code> has not already been set</li>
     * </ul>
     * Therefore the mode is immutable once set, and can only be set after the <code>START_ELEMENT</code> event
     * (the root element of the XML document) has been seen.
     * <p>
     * An XML stream <em>must be</em> wrapped if the root element of the document represents a DC entity:
     * <ul>
     * <li>&lt;DeliverableUnit&gt;</li>
     * <li>&lt;File&gt;</li>
     * <li>&lt;Event&gt;</li>
     * <li>&lt;Collection&gt;</li>
     * <li>&lt;Manifestation&gt;</li>
     * </ul>
     * or a DC entity container:
     * <ul>
     * <li>&lt;DeliverableUnits&gt;</li>
     * <li>&lt;Files&gt;</li>
     * <li>&lt;Events&gt;</li>
     * <li>&lt;Collections&gt;</li>
     * <li>&lt;Manifestations&gt;</li>
     * </ul>
     * An XML stream <em>must not be</em> wrapped if the root element if the document represents the DCP packaging
     * element: &lt;dcp xmlns="http://dataconservancy.org/schemas/dcm/1.0"&gt;
     * </p>
     *
     * @param e the XML event, possibly <code>null</code>
     */
    private void setMode(XMLEvent e) {
        log.trace("Entering setMode({})", e);
        log.trace("Current mode: {}", mode);
        if (e == null) {
            return;
        }

        // Once the mode is set, it isn't changed.
        // If we haven't seen the startDocument event,
        // we can't tell if we are wrapping or not
        if (mode != null || !seenStartDocument) {
            return;
        }


        if (e.getEventType() == XMLStreamConstants.START_ELEMENT) {
            final QName rootElement = ((StartElement) e).getName();
            readerRootElement = rootElement;
            prepareWrappingStack(rootElement);

            // Only set the mode if we have a START_ELEMENT event
            if (wrapperQnames.hasPrevious()) {
                mode = Mode.WRAPPING;
            } else {
                mode = Mode.PASSTHRU;
            }
        }

    }

    /**
     * Prepares the stack of QNames used to wrap the incoming XML.
     */
    private QName prepareWrappingStack(QName rootElement) {

        // We know we DO NOT need to wrap the XML if the root element
        // is: <dcp>
        if (rootElement.equals(Q_DCP)) {
            return null;
        }

        final String localPart = rootElement.getLocalPart();
        QName wrapperQname = null;

        // if the element name is an entity: 
        //   <Collection>
        //   <File>
        //   <Manifestation>
        //   <DeliverableUnit>
        //   <Event>
        //
        // we need to wrap with a container element

        if (localPart.equals(E_COLLECTION)) {
            wrapperQname = Q_COLLECTIONS;
        }

        if (localPart.equals(E_FILE)) {
            wrapperQname = Q_FILES;
        }

        if (localPart.equals(E_MANIFESTATION)) {
            wrapperQname = Q_MANIFESTATIONS;
        }

        if (localPart.equals(E_DU)) {
            wrapperQname = Q_DUS;
        }

        if (localPart.equals(E_EVENT)) {
            wrapperQname = Q_EVENTS;
        }

        // if the element name is an entity container:
        //   <Collections>
        //   <Files>
        //   <DeliverableUnits>
        //   <Events>
        //   <Manifestations>
        // we need to wrap with a <dcp>

        if (localPart.equals(E_COLLECTIONS)) {
            wrapperQname = Q_DCP;
        }

        if (localPart.equals(E_FILES)) {
            wrapperQname = Q_DCP;
        }

        if (localPart.equals(E_MANIFESTATIONS)) {
            wrapperQname = Q_DCP;
        }

        if (localPart.equals(E_DUS)) {
            wrapperQname = Q_DCP;
        }

        if (localPart.equals(E_EVENTS)) {
            wrapperQname = Q_DCP;
        }

        // Push the QName on the stack
        if (wrapperQname != null) {
            wrapperQnames.add(wrapperQname);
        }

        // If we have a QName, recursively call.
        if (wrapperQname != null) {
            return prepareWrappingStack(wrapperQname);
        }
        return null;
    }

    private StartElement getNextWrappedStartElementEvent() {
        log.trace("Entering getNextWrappedStartElementEvent()");
        QName wrappedElement = wrapperQnames.previous();
        log.trace("Returning wrapped start element {}", wrappedElement);
        final List<Namespace> namespaces = new ArrayList<Namespace>(2);

        if (wrappedElement.getNamespaceURI() != null) {
            final Namespace namespace = eventFactory.createNamespace(wrappedElement.getPrefix(), wrappedElement.getNamespaceURI());
            namespaces.add(namespace);
            log.trace("Adding namespace {}", namespace);
        }

        final StartElement startEvent = eventFactory.createStartElement(wrappedElement, null, namespaces.iterator());
        log.trace("Returning wrapped start element event {}", startEvent);
        return startEvent;
    }

    private StartElement createStartElementEvent(QName name) {
        log.trace("Entering createStartElementEvent()");
        log.trace("Creating start element event for qname {}", name);
        final List<Namespace> namespaces = new ArrayList<Namespace>(2);

        if (name.getNamespaceURI() != null) {
            final Namespace namespace = eventFactory.createNamespace(name.getPrefix(), name.getNamespaceURI());
            namespaces.add(namespace);
            log.trace("Adding namespace {}", namespace);
        }
                
        final StartElement startEvent = eventFactory.createStartElement(name, null, namespaces.iterator());
        log.trace("Returning created start element event {}", startEvent);
        return startEvent;
    }

    private EndElement getNextWrappedEndElementEvent() {
        log.trace("Entering getNextWrappedEndElementEvent()");
        QName wrappedElement = wrapperQnames.next();
        return eventFactory.createEndElement(wrappedElement, null);
    }
}
