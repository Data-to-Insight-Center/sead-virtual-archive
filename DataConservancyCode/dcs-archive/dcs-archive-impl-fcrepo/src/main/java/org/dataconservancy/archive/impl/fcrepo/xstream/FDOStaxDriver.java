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
package org.dataconservancy.archive.impl.fcrepo.xstream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.io.xml.AbstractPullReader;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.StaxWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;

/**
 * The Data Conservancy Archive XStream driver. Returns {@link javax.xml.stream
 * stream-based} {@link FDOStaxReader} and {@link FDOStaxWriter}s.
 */
public class FDOStaxDriver
        extends StaxDriver {

    public FDOStaxDriver(QNameMap qnameMap) {
        super(qnameMap);
    }

    public FDOStaxDriver(QNameMap qnameMap, XmlFriendlyReplacer replacer) {
        super(qnameMap, replacer);
    }

    public FDOStaxDriver(XmlFriendlyReplacer replacer) {
        super(replacer);
    }

    @Override
    public AbstractPullReader createStaxReader(XMLStreamReader in) {
        return new FDOStaxReader(getQnameMap(), in, xmlFriendlyReplacer());
    }

    @Override
    public StaxWriter createStaxWriter(XMLStreamWriter out,
                                       boolean writeStartEndDocument)
            throws XMLStreamException {
        return new FDOStaxWriter(getQnameMap(),
                                 out,
                                 writeStartEndDocument,
                                 isRepairingNamespace(),
                                 xmlFriendlyReplacer());
    }

}
