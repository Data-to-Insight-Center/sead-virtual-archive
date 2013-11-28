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
package org.dataconservancy.dcs.access.impl.solr;

import java.io.IOException;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.solr.common.SolrInputDocument;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * See {@link org.dataconservancy.dcs.index.dcpsolr}
 */

@Deprecated
public class DcsSolrXmlIndexer {

    private static void addxmltext(SolrInputDocument doc,
                                   String field,
                                   StringBuilder text) {
        if (text != null && text.length() > 0) {
            doc.addField(field, text.toString());
        }
    }

    // Creates a field for the text directly contained by each element,
    // each attribute of an element, and a field for all the text in the document.

    protected static void index(final SolrInputDocument doc,
                             final String fieldprefix,
                             final String textfield,
                             InputSource input) throws SAXException,
            IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();

        final StringBuilder text = new StringBuilder();

        reader.setContentHandler(new ContentHandler() {

            final Deque<String> parents = new LinkedList<String>();

            final Deque<StringBuilder> element_texts =
                    new LinkedList<StringBuilder>();

            private String path() {
                StringBuilder sb = new StringBuilder(fieldprefix);

                for (String el : parents) {
                    sb.append('/');
                    sb.append(el);
                }

                return sb.toString();
            }

            public void startPrefixMapping(String prefix, String uri)
                    throws SAXException {

            }

            public void startElement(String uri,
                                     String localName,
                                     String qName,
                                     Attributes atts) throws SAXException {
                // ignore namespaces when constructing field names
                parents.addLast(localName);
                element_texts.addLast(new StringBuilder());

                for (int i = 0; i < atts.getLength(); i++) {
                    String attr_field = path() + "@" + atts.getLocalName(i);
                    doc.addField(attr_field, atts.getValue(i));
                }
            }

            public void startDocument() throws SAXException {

            }

            public void skippedEntity(String name) throws SAXException {

            }

            public void setDocumentLocator(Locator locator) {
            }

            public void processingInstruction(String target, String data)
                    throws SAXException {
            }

            public void ignorableWhitespace(char[] ch, int start, int length)
                    throws SAXException {
                text.append(ch, start, length);

                element_texts.getLast().append(ch, start, length);
            }

            public void endPrefixMapping(String prefix) throws SAXException {

            }

            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                addxmltext(doc, path(), element_texts.removeLast());
                parents.removeLast();
            }

            public void endDocument() throws SAXException {
                addxmltext(doc, textfield, text);
            }

            public void characters(char[] ch, int start, int length)
                    throws SAXException {
                text.append(ch, start, length);
                text.append(' ');

                element_texts.getLast().append(ch, start, length);
            }
        });

        reader.setErrorHandler(new ErrorHandler() {

            public void warning(SAXParseException exception)
                    throws SAXException {
                // TODO log?
                throw exception;
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
                throw exception;
            }

            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });

        reader.parse(input);
    }
}
