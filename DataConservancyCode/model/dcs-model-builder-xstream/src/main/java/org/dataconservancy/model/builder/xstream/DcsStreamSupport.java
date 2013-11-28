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

import java.util.Iterator;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.dataconservancy.model.dcs.support.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An attempt to provide utility methods for copying XML input and output streams without depending on XStream.
 */
public class DcsStreamSupport {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Stack<QName> nodeStack = new Stack<QName>();

    private final boolean isRepairingNamespaces = false;
    private final boolean writeStartDoc;

    public DcsStreamSupport() {
        this.writeStartDoc = false;
    }

    public DcsStreamSupport(boolean writeStartDoc) {
        this.writeStartDoc = writeStartDoc;
    }

    public void copyNode(XMLStreamReader in, XMLStreamWriter out) throws XMLStreamException {
        Assertion.notNull(in);
        Assertion.notNull(out);

        // handle the current event
        log.trace("initial handling method.");
        handleEvent(in, out, in.getEventType());

        log.trace("About to enter event loop.");
        while (in.hasNext()) {
            log.trace("In event loop, about to get the next event.");
            int event = in.next();
            log.trace("Handling the next event.");
            handleEvent(in, out, event);
            if (nodeStack.isEmpty()) {
                break;
            }
        }
    }

    private void handleEvent(XMLStreamReader in, XMLStreamWriter out, int event) throws XMLStreamException {
        log.trace("Processing event: '{}'", event);
        switch (event) {

            case XMLStreamConstants.START_DOCUMENT:   // 7
                if (writeStartDoc) {
                    log.trace("Writing start document.");
                    out.writeStartDocument();
                }
                break;
            
            case XMLStreamConstants.START_ELEMENT:    // 1
                writeStartElement(in, out, in.getName(), false);
                writeCurrentAttributes(in, out);
                break;

            case XMLStreamConstants.END_DOCUMENT:     // 8
                log.trace("Writing end document.");
                break;
            case XMLStreamConstants.END_ELEMENT:      // 2
                log.trace("Writing end element: '{}'", nodeStack.pop());
                out.writeEndElement();
                break;

            case XMLStreamConstants.ATTRIBUTE:        // 10
                log.trace("Writing attributes");
                writeCurrentAttributes(in, out);
                break;

            case XMLStreamConstants.ENTITY_REFERENCE:  // 9
            case XMLStreamConstants.CDATA:             // 12
            case XMLStreamConstants.CHARACTERS:        // 4
            case XMLStreamConstants.SPACE:             // 6
                writeValue(in, out);
                break;

            case XMLStreamConstants.NAMESPACE:         // 13
                break;

            default:
                throw new RuntimeException("Unhandled XML stream event " + event);
        }
    }

    /**
     * Properly write the opening tag of an element, complete with namespaces.
     * <p/>
     * This code borrowed from XStream's {@link com.thoughtworks.xstream.io.xml.StaxWriter#startNode(String)}.
     *
     * @param out                   the writer
     * @param element               the element to write
     * @param isRepairingNamespaces if the output stream is repairing namespaces
     * @throws XMLStreamException on error
     */
    private void writeStartElement(XMLStreamReader in, XMLStreamWriter out, QName element, boolean isRepairingNamespaces)
            throws XMLStreamException {
        log.trace("Writing start element '{}'", element);
        nodeStack.push(element);
        final String prefix = element.getPrefix();
        final String uri = element.getNamespaceURI();

        // before you ask - yes it really is this complicated to output QNames to StAX
        // handling both repair namespace modes :)

        final boolean hasPrefix = prefix != null && prefix.length() > 0;
        final boolean hasURI = uri != null && uri.length() > 0;
        boolean writeNamespace = false;

        if (hasURI) {
            if (hasPrefix) {
                String currentNamespace = out.getNamespaceContext().getNamespaceURI(prefix);
                if (currentNamespace == null || !currentNamespace.equals(uri)) {
                    writeNamespace = true;
                }
            } else {
                String defaultNamespace = out.getNamespaceContext().getNamespaceURI("");
                if (defaultNamespace == null || !defaultNamespace.equals(uri)) {
                    writeNamespace = true;
                }
            }
        }

        if (hasPrefix) {
            out.setPrefix(prefix, uri);
        } else if (hasURI) {
            if (writeNamespace) {
                out.setDefaultNamespace(uri);
            }
        }

        out.writeStartElement(prefix, element.getLocalPart(), uri);
        if (hasURI && writeNamespace && !isRepairingNamespaces) {
            if (hasPrefix) {
                log.trace("Writing namespace prefix: '" + prefix + "' namespace: '" + uri);
                out.writeNamespace(prefix, uri);
            } else {
                log.trace("Writing default namespace: '" + uri);
                out.writeDefaultNamespace(uri);
            }
        }

        if (!isRepairingNamespaces) {
            final NamespaceContext writerNsCtx = out.getNamespaceContext();
            final String writerDefaultNsUri = writerNsCtx.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
            for (int i = 0; i < in.getNamespaceCount(); i++) {
                final String nsUri = in.getNamespaceURI(i);
                String nsPrefix = in.getNamespacePrefix(i);
                if (nsPrefix == null) {
                    nsPrefix = XMLConstants.DEFAULT_NS_PREFIX;
                }
                final String writerNsUri = writerNsCtx.getNamespaceURI(nsPrefix);
                log.trace("Prefix {} Reader nsuri: {} writer nsuri: {}", new Object[]{nsPrefix, nsUri, writerNsUri});
                if (writerNsUri == null && !nsUri.equals(writerDefaultNsUri)) {
                    out.writeNamespace(nsPrefix, nsUri);
                }
            }
        }
    }

    private void writeValue(XMLStreamReader in, XMLStreamWriter out) throws XMLStreamException {
        final String value = in.getText();
        log.trace("Writing value '{}'", value);
        out.writeCharacters(value);
    }

    private void writeCurrentAttributes(XMLStreamReader in, XMLStreamWriter out) throws XMLStreamException {
        for (int i = 0; i < in.getAttributeCount(); i++) {
            final QName currentAttr = in.getAttributeName(i);
            log.trace("Writing attribute '{}'", currentAttr);
            final String value = in.getAttributeValue(i);
            final String attrNsUri = currentAttr.getNamespaceURI();
            final String attrPrefix = currentAttr.getPrefix();
            final String attrLocalName = currentAttr.getLocalPart();

            if (attrLocalName != null && attrNsUri != null && attrPrefix != null) {
                out.writeAttribute(attrPrefix, attrNsUri, attrLocalName, value);
            } else if (attrLocalName != null && attrNsUri != null) {
                out.writeAttribute(attrNsUri, attrLocalName, value);
            } else {
                out.writeAttribute(attrLocalName, value);
            }
        }
    }

    private void debugNamespaceContext(XMLStreamReader in, NamespaceContext nsCtx) {
        final StringBuilder sb = new StringBuilder();
        final QName currentNode = in.getName();

        final String defaultNs = nsCtx.getNamespaceURI("");
        sb.append("Default namespace: ").append(defaultNs);

        log.debug(sb.toString());
        sb.setLength(0);
        sb.trimToSize();

        final String nsUri = currentNode.getNamespaceURI();
        final String prefix = currentNode.getPrefix();
        final String localPart = currentNode.getLocalPart();
        sb.append("Current namespace: ")
                .append("'").append(nsUri).append("', ")
                .append("prefix: '").append(prefix).append("', ")
                .append("localpart: '").append(localPart).append("'");

        log.debug(sb.toString());
        sb.setLength(0);
        sb.trimToSize();

        if (nsUri == null) {
            sb.append("Current namespace URI is null");
        } else {
            final Iterator<String> prefixes = nsCtx.getPrefixes(nsUri);
            while (prefixes.hasNext()) {
                sb.append("'").append(prefixes.next()).append("'").append(" ");
            }
        }

        log.debug(sb.toString());
    }

}
