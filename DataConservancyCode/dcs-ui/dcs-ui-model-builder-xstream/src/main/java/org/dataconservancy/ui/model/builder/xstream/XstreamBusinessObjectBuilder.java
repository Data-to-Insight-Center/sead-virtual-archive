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

package org.dataconservancy.ui.model.builder.xstream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

import org.apache.commons.io.input.TeeInputStream;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.support.Assertion;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * XstreamBusinessObjectBuilder is an Xstream implementation of
 * {@link org.dataconservancy.ui.model.builder.BusinessObjectBuilder}.
 */
public class XstreamBusinessObjectBuilder
        implements BusinessObjectBuilder {

    /**
     * Error deserializing a stream. Parameters: reason
     */
    private final static String DESER_ERR =
            "Error encountered deserializing a stream: %s";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final boolean validating;

    private XStream x;

    private Validator validator;

    private DocumentBuilder parser;

    /**
     * The classpath resource to the Business Object Package schema.
     */
    private static final String BOP_SCHEMA_RESOURCE = "/businessObject.xsd";

    /**
     * Constructs a non-validating builder by default.
     */
    public XstreamBusinessObjectBuilder(XStream xStream) {
        x = xStream;
        validating = false;
    }

    /**
     * Constructs a builder that will perform validation when de-serializing XML streams if <code>isValidating</code>
     * is <code>true</code>.  The schema used for validation is the BOP 1.0 schema.
     * <p/>
     * <strong><em>N.B.</em></strong>: currently this class will only validate incoming DC BOPs (it will <em>not</em>
     * validate entities).  At a later time this implementation may be updated to validate entities as well.
     *
     * @param isValidating flag indicating whether or not validation should be enabled
     * @throws IllegalStateException if the BOP schema cannot be resolved or parsed
     */
    public XstreamBusinessObjectBuilder(XStream xStream, boolean isValidating) {
        x = xStream;
        validating = isValidating;

        if(validating){
            try{
            // Create a namespace-aware parser that will parse XML into a DOM tree.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            parser = dbf.newDocumentBuilder();

            // Create a SchemaFactory
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the schema
            final URL businessObjectSchema = this.getClass().getResource(BOP_SCHEMA_RESOURCE);
            Source schemaFile = new StreamSource(businessObjectSchema.openStream());
            Schema schema = factory.newSchema(schemaFile);

            // Create a Validator instance, which can be used to validate an instance document
            validator = schema.newValidator();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException("Unable to initialize " + this.getClass().getName() + ": error configuring parser: " +
                      e.getMessage(), e);
            } catch (SAXException e) {
                throw new IllegalStateException("Unable to initialize " + this.getClass().getName() + ": error retrieving " +
                        " or parsing class path resource " + BOP_SCHEMA_RESOURCE + ": " + e.getMessage(), e);
            } catch (IOException e) {
                 throw new IllegalStateException("Unable to initialize " + this.getClass().getName() + ": IO error: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Bop buildBusinessObjectPackage(InputStream in)
            throws InvalidXmlException{
        Assertion.notNull(in);
        final Bop bop;
        try{
            if(validating){
                ByteArrayOutputStream sink = new ByteArrayOutputStream(8192);
                in = new TeeInputStream(in, sink);
                bop = (Bop) x.fromXML(in);
                validator.validate(new StreamSource(new ByteArrayInputStream(sink.toByteArray())));
            } else {
                bop = (Bop) x.fromXML(in);
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
        return bop;
    }

    @Override
    public void buildBusinessObjectPackage(Bop bop, OutputStream sink) {
        Assertion.notNull(bop);
        Assertion.notNull(sink);
        x.toXML(bop, sink);
    }

    @Override
    public Project buildProject(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final Project project;
        try {
            project = (Project) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return project;
    }

    @Override
    public void buildProject(Project project, OutputStream sink) {
        Assertion.notNull(project);
        Assertion.notNull(sink);
        x.toXML(project, sink);
    }

    @Override
    public Collection buildCollection(InputStream in)
            throws InvalidXmlException {
        Assertion.notNull(in);
        final Collection collection;
        try {
            collection = (Collection) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return collection;
    }

    @Override
    public void buildCollection(Collection collection, OutputStream sink) {
        Assertion.notNull(collection);
        Assertion.notNull(sink);
        x.toXML(collection, sink);
    }

    @Override
    public Person buildPerson(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final Person person;
        try {
            person = (Person) x.fromXML(in);
        } catch (StreamException e) {
            log.debug(String.format(DESER_ERR, e.getMessage()), e);
            throw new InvalidXmlException(e);
        }
        return person;
    }

    @Override
    public void buildPerson(Person person, OutputStream sink) {
        Assertion.notNull(person);
        Assertion.notNull(sink);
        x.toXML(person, sink);
    }

    @Override
    public void buildDataFile(DataFile dataFile, OutputStream sink) {
        Assertion.notNull(dataFile);
        Assertion.notNull(sink);
        x.toXML(dataFile, sink);
    }

    @Override
    public DataFile buildDataFile(InputStream in) throws InvalidXmlException {
        Assertion.notNull(in);
        final DataFile dataFile;
        try {
            dataFile = (DataFile) x.fromXML(in);
        } catch (Exception e) {
            final String msg = String.format(DESER_ERR, e.getMessage());
            log.debug(msg, e);
            throw new InvalidXmlException(msg, e);
        }

        return dataFile;
    }

    @Override
    public void buildMetadataFile(MetadataFile metadataFile, OutputStream sink) {
        Assertion.notNull(metadataFile);
        Assertion.notNull(sink);
        x.toXML(metadataFile, sink);
    }

    @Override
    public MetadataFile buildMetadataFile(InputStream in) throws  InvalidXmlException {
        Assertion.notNull(in);
        final MetadataFile metadataFile;
        try{
            metadataFile = (MetadataFile) x.fromXML(in);
        } catch (Exception e) {
            final String msg = String.format(DESER_ERR, e.getMessage());
            log.debug(msg, e);
            throw new InvalidXmlException(msg, e);
        }

        return metadataFile;
    }
}