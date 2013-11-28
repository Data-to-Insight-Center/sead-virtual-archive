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

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;

import org.dataconservancy.model.builder.xstream.DcsStaxWriter;
import org.dataconservancy.model.builder.xstream.DcsStreamSupport;
import org.dataconservancy.model.dcs.support.Assertion;

/**
 * Provides a StaxWriter specialized for FDO processing.
 * 
 * @author Daniel Davis
 * @version $Id$
 */
public class FDOStaxWriter
        extends StaxWriter {
    
    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    public FDOStaxWriter(QNameMap qnameMap, XMLStreamWriter out)
            throws XMLStreamException {
        super(qnameMap, out);
    }

    public FDOStaxWriter(QNameMap qnameMap,
                         XMLStreamWriter out,
                         boolean writeEnclosingDocument,
                         boolean namespaceRepairingMode)
            throws XMLStreamException {
        super(qnameMap, out, writeEnclosingDocument, namespaceRepairingMode);
    }

    public FDOStaxWriter(QNameMap qnameMap,
                         XMLStreamWriter out,
                         boolean writeEnclosingDocument,
                         boolean namespaceRepairingMode,
                         XmlFriendlyReplacer replacer)
            throws XMLStreamException {
        super(qnameMap, out, false, namespaceRepairingMode, replacer);
        if (writeEnclosingDocument) {
            // XStream does not handle this properly.
            out.writeStartDocument("UTF-8", "1.0");
        }
    }

    @Override
    public XMLStreamWriter getXMLStreamWriter() {
        return super.getXMLStreamWriter();
    }

    /**
     * Copies the current node and its children to the <code>sink</code>.
     *
     * @param in
     * @param out the output sink
     * @throws javax.xml.stream.XMLStreamException
     */
    public void copyNode(String in, XMLStreamWriter out) throws XMLStreamException {
        Assertion.notNull(in);
        Assertion.notNull(out);

        copyNode(inputFactory.createXMLStreamReader(new StringReader(in)), out);
    }

    /**
     * Copies the current node and its children to the <code>writer</code>.
     *
     * @param in
     * @param writer the writer to write to
     * @throws javax.xml.stream.XMLStreamException
     */
    public void copyNode(XMLStreamReader in, DcsStaxWriter writer) throws XMLStreamException {
        Assertion.notNull(in);
        Assertion.notNull(writer);
        copyNode(in, writer.getXMLStreamWriter());
    }

    /**
     * Copies the current node and its children to the <code>sink</code>.
     *
     * @param in
     * @param out the output sink
     * @throws javax.xml.stream.XMLStreamException
     */
    public void copyNode(XMLStreamReader in, XMLStreamWriter out) throws XMLStreamException {
        Assertion.notNull(in);
        Assertion.notNull(out);

        final DcsStreamSupport dss = new DcsStreamSupport();
        dss.copyNode(in, out);
    }

}
