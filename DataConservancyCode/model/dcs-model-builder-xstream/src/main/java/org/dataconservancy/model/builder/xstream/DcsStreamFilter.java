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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class DcsStreamFilter implements StreamFilter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean accept(XMLStreamReader xmlStreamReader) {
        switch (xmlStreamReader.getEventType()) {

            // We care about these events...
            case XMLStreamConstants.START_DOCUMENT:
            case XMLStreamConstants.START_ELEMENT:
            case XMLStreamConstants.ATTRIBUTE:
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
            case XMLStreamConstants.END_DOCUMENT:
            case XMLStreamConstants.END_ELEMENT:
            case XMLStreamConstants.NAMESPACE:
                log.trace("Accepting event '{}'", xmlStreamReader.getEventType());
                return true;

            // Not so much these events...
            case XMLStreamConstants.COMMENT:
            case XMLStreamConstants.DTD:
            case XMLStreamConstants.ENTITY_DECLARATION:
            case XMLStreamConstants.ENTITY_REFERENCE:
            case XMLStreamConstants.NOTATION_DECLARATION:
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
            case XMLStreamConstants.SPACE:
                log.trace("Ignoring event '{}'", xmlStreamReader.getEventType());
                return false;

            // Sanity check...
            default:
                throw new RuntimeException("Unhandled XML Stream Reader Event: " + xmlStreamReader.getEventType());
        }
    }
}
