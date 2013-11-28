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
import com.thoughtworks.xstream.io.xml.StaxReader;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.support.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Stack;

import static org.dataconservancy.model.dcs.support.Util.isEmptyOrNull;

/**
 * Data Conservancy stream reader which supports XML namespaces, and copying xml streams.
 */
public class DcsStaxReader extends StaxReader implements NsAwareStream, CopyingStreamReader {

    private final static String DEFAULT_NS = DcpModelVersion.VERSION_1_0.getXmlns();

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private final XMLStreamReader in;
    private final boolean isNsRepairing;
    private final Stack<QName> nodeStack = new Stack<QName>();

    public DcsStaxReader(QNameMap qnameMap, XMLStreamReader in) {
        super(qnameMap, in);
        this.in = in;
        isNsRepairing = false;
    }

    public DcsStaxReader(QNameMap qnameMap, XMLStreamReader in, XmlFriendlyReplacer replacer) {
        super(qnameMap, in, replacer);
        this.in = in;
        isNsRepairing = false;
    }

    /**
     * Copies the current node and its children to the <code>sink</code>.
     *
     * @param sink the output sink
     */
    @Override
    public void copyNode(OutputStream sink) throws XMLStreamException {
        Assertion.notNull(sink);

        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", isNsRepairing);
        final XMLStreamWriter writer = outputFactory.createXMLStreamWriter(sink);

        final DcsStreamSupport dcsStream = new DcsStreamSupport();
        dcsStream.copyNode(in, writer);
        writer.flush();
        writer.close();
    }

    /**
     * Copies the current node and its children to the <code>writer</code>.
     *
     * @param writer the writer to write to
     */
    public void copyNode(DcsStaxWriter writer) throws XMLStreamException {
        Assertion.notNull(writer);
        final DcsStreamSupport dcsStream = new DcsStreamSupport();
        dcsStream.copyNode(in, writer.getXMLStreamWriter());
    }

    @Override
    public QName getQname() {
        return in.getName();
    }

}
