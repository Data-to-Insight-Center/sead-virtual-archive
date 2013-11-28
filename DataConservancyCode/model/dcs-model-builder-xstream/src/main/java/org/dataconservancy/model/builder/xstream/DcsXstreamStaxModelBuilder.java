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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.support.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * A stream-based model builder using XStream and StAX.
 */
public class DcsXstreamStaxModelBuilder implements DcsModelBuilder {

    /**
     * Error deserializing a stream.
     * Parameters: reason
     */
    private final static String DESER_ERR = "Error encountered deserializing a stream: %s";

    /**
     * DCP Schema 1.0 classpath resource
     */
    private final static String DCP_SCHEMA = "/schema/dcp.xsd";

    /**
     * DCP Schema
     */
    private final Schema schema;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final boolean validating;
    private XStream x;

    /**
     * Constructs a non-validating builder by default.
     */
    public DcsXstreamStaxModelBuilder() {
        this(false);
    }

    /**
     * Constructs a builder that will perform validation when de-serializing XML streams if <code>isValidating</code>
     * is <code>true</code>.  The schema used for validation is the DCP 1.0 schema.
     * <p/>
     * <strong><em>N.B.</em></strong>: currently this class will only validate incoming DCP SIPs (it will <em>not</em>
     * validate entities).  At a later time this implementation may be updated to validate entities as well.
     *
     * @param isValidating flag indicating whether or not validation should be enabled
     * @throws IllegalStateException if the DCP schema cannot be resolved or parsed
     */
    public DcsXstreamStaxModelBuilder(boolean isValidating) {
        this.validating = isValidating;
        x = XStreamFactory.newInstance();
        try {
            InputStream schemaIn = this.getClass().getResourceAsStream(DCP_SCHEMA);
            if (schemaIn == null) {
                throw new IllegalStateException("Unable to initialize " + this.getClass().getName() + ": class path " +
                        "resource " + DCP_SCHEMA + " could not be found.");
            }
            schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(schemaIn));
        } catch (SAXException e) {
            throw new IllegalStateException("Unable to initialize " + this.getClass().getName() + ": error retrieving " +
                        " or parsing class path resource " + DCP_SCHEMA + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns <code>true</code> if the builder validates XML streams when de-serializing XML streams.
     *
     * @return <code>true</code> if validation is enabled
     */
    public boolean isValidating() {
        return validating;
    }

    @Override
    public DcsDeliverableUnit buildDeliverableUnit(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final DcsDeliverableUnit du;
        try {
            du = (DcsDeliverableUnit) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return du;
    }

    @Override
    public DcsManifestation buildManifestation(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final DcsManifestation man;
        try {
            man = (DcsManifestation) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return man;
    }

    @Override
    public DcsFile buildFile(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final DcsFile file;
        try {
            file = (DcsFile) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return file;
    }


    @Override
    public Dcp buildSip(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);

        Validator validator = schema.newValidator();
        final Dcp sip;
        try {
            if (validating) {
                ByteArrayOutputStream sink = new ByteArrayOutputStream(8192);
                in = new TeeInputStream(in, sink);
                sip = (Dcp) x.fromXML(in);
                validator.validate(new StreamSource(new ByteArrayInputStream(sink.toByteArray())));
            } else {
                sip = (Dcp) x.fromXML(in);
            }
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        } catch (SAXException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        } catch (IOException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return sip;
    }

    /**
     * Returns an {@code InputStream} to a serialized version of the {@code dcp}.
     *
     * @param dcp the package in DCP format
     * @return an {@code InputStream} to the serialized package
     */
    public InputStream buildSip(Dcp dcp) {
        Assertion.notNull(dcp);
        ByteArrayOutputStream sink = new ByteArrayOutputStream(8192);
        buildSip(dcp, sink);
        return new ByteArrayInputStream(sink.toByteArray());
    }

    @Override
    public void buildDeliverableUnit(DcsDeliverableUnit du, OutputStream sink) {
        Assertion.notNull(du);
        Assertion.notNull(sink);
        x.toXML(du, sink);
    }

    @Override
    public void buildManifestation(DcsManifestation manifestation, OutputStream sink) {
        Assertion.notNull(manifestation);
        Assertion.notNull(sink);
        x.toXML(manifestation, sink);
    }

    @Override
    public void buildFile(DcsFile file, OutputStream sink) {
        Assertion.notNull(file);
        Assertion.notNull(sink);
        x.toXML(file, sink);
    }

    @Override
    public void buildSip(Dcp sip, OutputStream sink) {
        Assertion.notNull(sip);
        Assertion.notNull(sink);
        x.toXML(sip, sink);
    }

    @Override
    public DcsCollection buildCollection(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final DcsCollection c;
        try {
            c = (DcsCollection)x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return c;
    }

    @Override
    public void buildCollection(DcsCollection collection, OutputStream sink) {
        Assertion.notNull(collection);
        Assertion.notNull(sink);
        x.toXML(collection, sink);
    }

    @Override
    public DcsEvent buildEvent(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final DcsEvent e;
        try {
            e = (DcsEvent) x.fromXML(in);
        } catch (Exception ex) {
            log.debug(String.format(DESER_ERR, ex.getMessage()), ex);
            throw new InvalidXmlException(ex);
        }
        return e;
    }

    @Override
    public void buildEvent(DcsEvent event, OutputStream sink) {
        Assertion.notNull(event);
        Assertion.notNull(sink);
        x.toXML(event, sink);
    }
}
