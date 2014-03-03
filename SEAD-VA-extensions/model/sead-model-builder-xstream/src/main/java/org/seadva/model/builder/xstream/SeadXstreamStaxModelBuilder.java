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
package org.seadva.model.builder.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.dataconservancy.model.dcs.support.Assertion;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

/**
 * A stream-based model builder using XStream and StAX.
 */
public class SeadXstreamStaxModelBuilder extends DcsXstreamStaxModelBuilder{

   private final static String DESER_ERR = "Error encountered deserializing a stream: %s";

   private final static String DCP_SCHEMA = "/schema/dcp.xsd";

    private final Schema schema;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final boolean validating;
    private XStream x;

    public SeadXstreamStaxModelBuilder() {
        this(false);
    }

    public SeadXstreamStaxModelBuilder(boolean isValidating) {
        this.validating = isValidating;
        x = SeadXStreamFactory.newInstance();
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


    @Override
    public ResearchObject buildSip(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);

        Validator validator = schema.newValidator();
        final ResearchObject sip;
        try {
            if (validating) {
                ByteArrayOutputStream sink = new ByteArrayOutputStream(8192);
                in = new TeeInputStream(in, sink);
                sip = (ResearchObject) x.fromXML(in);
                validator.validate(new StreamSource(new ByteArrayInputStream(sink.toByteArray())));
            } else {
                sip = (ResearchObject) x.fromXML(in);
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

    public void buildSip(Dcp sip, OutputStream sink)
    {
        Assertion.notNull(sip);
        Assertion.notNull(sink);
        this.x.toXML((ResearchObject)sip, sink);
    }

    public void buildSip(ResearchObject sip, OutputStream sink) {
        Assertion.notNull(sip);
        Assertion.notNull(sink);
        x.toXML(sip, sink);
    }


}
