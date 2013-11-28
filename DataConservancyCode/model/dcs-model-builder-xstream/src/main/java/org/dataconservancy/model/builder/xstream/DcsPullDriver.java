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

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.AbstractXmlDriver;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.Location;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * The Data Conservancy XStream driver, utilizing XML pull parsing.  Returns {@link javax.xml.stream stream-based} {@link DcsStaxReader}
 * and {@link DcsStaxWriter}s.
 */
public class DcsPullDriver extends AbstractXmlDriver {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final XMLOutputFactory outFactory;
    private final XMLInputFactory inFactory;
    private final XmlFriendlyReplacer replacer;
    private final QNameMap qnames;

    /**
     * Creates a new DcsPullDriver.  Uses the JVM-provided {@link XMLOutputFactory} and {@link XMLInputFactory} for
     * creating XML stream readers and writers.  Specifically, the properties that are available and their defaults are
     * dependent on the JVM.
     *
     * @param qnames the QName map
     */
    public DcsPullDriver(QNameMap qnames) {
        outFactory = XMLOutputFactory.newInstance();
        inFactory = XMLInputFactory.newInstance();
        inFactory.setXMLReporter(new XMLReporter() {
            @Override
            public void report(String s, String s1, Object o, Location location) throws XMLStreamException {
                log.warn("{} {} {} {}", new Object[] {s, s1, o, location});

            }
        });
        this.replacer = new XmlFriendlyReplacer();
        this.qnames = qnames;
    }

    /**
     * Creates a new DcsPullDriver.  Uses the JVM-provided {@link XMLOutputFactory} and {@link XMLInputFactory} for
     * creating XML stream readers and writers.  Specifically, the properties that are available and their defaults are
     * dependent on the JVM.
     *
     * @param replacer the XmlFriendlyReplacer to use
     * @param qnames the QName map
     */
    public DcsPullDriver(XmlFriendlyReplacer replacer, QNameMap qnames) {
        super(replacer);
        outFactory = XMLOutputFactory.newInstance();
        inFactory = XMLInputFactory.newInstance();
        inFactory.setXMLReporter(new XMLReporter() {
            @Override
            public void report(String s, String s1, Object o, Location location) throws XMLStreamException {
                log.warn("{} {} {} {}", new Object[] {s, s1, o, location});

            }
        });
        this.replacer = replacer;
        this.qnames = qnames;
    }

    @Override
    public HierarchicalStreamReader createReader(Reader in) {
        try {
            return new DcsStaxReader(qnames, inFactory.createXMLStreamReader(in));
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public HierarchicalStreamReader createReader(InputStream in) {
        return createDcsReader(in);
    }

    public DcsStaxReader createDcsReader(InputStream in) {
        try {
            return new DcsStaxReader(qnames, inFactory.createXMLStreamReader(in));
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public HierarchicalStreamWriter createWriter(Writer out) {
        try {
            outFactory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
            return new DcsStaxWriter(qnames, outFactory.createXMLStreamWriter(out), false, true);
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public HierarchicalStreamWriter createWriter(OutputStream out) {
        return createStreamWriter(out);
    }

    public DcsStaxWriter createStreamWriter(OutputStream out) {
        try {
            outFactory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
            return new DcsStaxWriter(qnames, outFactory.createXMLStreamWriter(out), false, true);
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

}
