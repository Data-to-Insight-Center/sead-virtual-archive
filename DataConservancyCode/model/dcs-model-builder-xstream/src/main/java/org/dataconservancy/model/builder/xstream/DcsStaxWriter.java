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
import com.thoughtworks.xstream.io.xml.StaxWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.support.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.StringReader;

/**
 *
 */
public class DcsStaxWriter extends StaxWriter {

    private final static String DEFAULT_NS = DcpModelVersion.VERSION_1_0.getXmlns();

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final QNameMap qnameMap;
    private final XMLInputFactory inputFactory;

    private static final ThreadLocal<XMLInputFactory> inputFactoryHolder = new ThreadLocal<XMLInputFactory>() {
        @Override
        protected XMLInputFactory initialValue() {
            return XMLInputFactory.newInstance();
        }
    };

    private final XMLStreamWriter out;
    private final boolean isNsRepairing;

    public DcsStaxWriter(QNameMap qnameMap, XMLStreamWriter out) throws XMLStreamException {
        super(qnameMap, out);
        this.qnameMap = qnameMap;
        this.out = out;
        this.isNsRepairing = false;
        this.inputFactory = inputFactoryHolder.get();
    }

    public DcsStaxWriter(QNameMap qnameMap, XMLStreamWriter out, boolean writeEnclosingDocument, boolean namespaceRepairingMode) throws XMLStreamException {
        super(qnameMap, out, writeEnclosingDocument, namespaceRepairingMode);
        this.qnameMap = qnameMap;
        this.out = out;
        this.isNsRepairing = false;
        this.inputFactory = inputFactoryHolder.get();
    }

    public DcsStaxWriter(QNameMap qnameMap, XMLStreamWriter out, boolean writeEnclosingDocument, boolean namespaceRepairingMode, XmlFriendlyReplacer replacer) throws XMLStreamException {
        super(qnameMap, out, writeEnclosingDocument, namespaceRepairingMode, replacer);
        this.qnameMap = qnameMap;
        this.out = out;
        this.isNsRepairing = false;
        this.inputFactory = inputFactoryHolder.get();
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
