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
package org.dataconservancy.ui.util;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * A utility class that can be quickly and easily used to parse and validate Data Conservancy generated XML,
 * including BOP and DCP XML.
 * <p/>
 * Example usage:
 * <pre>
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    builder.buildSip(dcp, baos);
    ValidationUtil vutil = new ValidationUtil();
    vutil.validate(ValidationUtil.DCP_SCHEMA_RESOURCE, new ByteArrayInputStream(baos.toByteArray()));
 * </pre>
 */
public class ValidationUtil {

    /**
     * The classpath resource to the Business Object Package schema, used as an
     * argument to {@link #newValidator(String)}.
     */
    public static final String BOP_SCHEMA_RESOURCE = "/businessObject.xsd";

    /**
     * The classpath resource to the Data Conservancy Package schema, used as an
     * argument to {@link #newValidator(String)}.
     */
    public static final String DCP_SCHEMA_RESOURCE = "/schema/dcp.xsd";

    /**
     * A default instance of {@code DocumentFactoryBuilder}, used if one isn't supplied on
     * construction.
     */
    private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    /**
     * A default instance of {@code SchemaFactory}, used if one isn't supplied on construction.
     */
    private SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private DcsModelBuilder dcsModelBuilder = new DcsXstreamStaxModelBuilder();

    /**
     * Constructs a {@code ValidationUtil} with a namespace-aware, non-validating, {@link DocumentBuilder parser}, a
     * {@link SchemaFactory} able to parse W3C XML Schema, and a default DCS model builder.
     */
    public ValidationUtil() {
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
    }

    /**
     * Constructs a {@code ValidationUtil} with the supplied {@code DocumentBuilderFactory}, a
     * {@link SchemaFactory} able to parse W3C XML Schema, and a default DCS model builder.
     */
    public ValidationUtil(DocumentBuilderFactory dbf) {
        this.dbf = dbf;
    }

    /**
     * Constructs a {@code ValidationUtil} with the supplied {@code DocumentBuilderFactory}, the supplied
     * {@link SchemaFactory}, and a default DCS model builder.
     */
    public ValidationUtil(DocumentBuilderFactory dbf, SchemaFactory factory) {
        this(dbf);
        this.factory = factory;
    }

    /**
     * Constructs a {@code ValidationUtil} with the supplied {@code DocumentBuilderFactory}, the supplied
     * {@link SchemaFactory}, and the supplied DCS model builder.
     */
    public ValidationUtil(DocumentBuilderFactory dbf, SchemaFactory factory, DcsModelBuilder dcsModelBuilder) {
        this(dbf, factory);
        this.factory = factory;
    }

    /**
     * Constructs a {@code ValidationUtil} with a namespace-aware, non-validating, {@link DocumentBuilder parser}, a
     * {@link SchemaFactory} able to parse W3C XML Schema, and the supplied DCS model builder.
     */
    public ValidationUtil(DcsModelBuilder dcsModelBuilder) {
        this();
        this.dcsModelBuilder = dcsModelBuilder;
    }

    /**
     * Constructs a {@code ValidationUtil} with a namespace-aware, non-validating, {@link DocumentBuilder parser}, the
     * supplied {@link SchemaFactory}, and a default DCS model builder.
     */
    public ValidationUtil(SchemaFactory factory) {
        this();
        this.factory = factory;

    }

    /**
     * Instantiates and returns a new {@code DocumentBuilder}.  If the {@link #ValidationUtil() default constructor} is
     * used, the returned {@code DocumentBuilder} is configured to properly parse Data Conservancy generated XML, and
     * be validated by {@link #newValidator(String)}.
     *
     * @return the DocumentBuilder
     * @throws RuntimeException all checked exceptions are re-thrown as RuntimeExceptions
     */
    public DocumentBuilder newDocumentBuilder() {
        try {
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Instantiates a returns a new {@code Validator}.  If the {@link #ValidationUtil() default constructor} is
     * used, the returned {@code Validator} is configured to properly validate Data Conservancy generated XML using
     * XML schema.
     *
     * @param schemaResource the schema used to validate XML supplied to the {@code Validator}
     * @return the {@code Validator}
     * @throws RuntimeException all checked exceptions are re-thrown as RuntimeExceptions
     */
    public Validator newValidator(String schemaResource) {
        // Load the schema
        final URL schemaUrl = this.getClass().getResource(schemaResource);
        assertNotNull("Unable to resolve the schema resource from the classpath: " + schemaResource,
                schemaUrl);
        Schema schema = null;
        try {
            Source schemaFile = new StreamSource(schemaUrl.openStream());
            schema = factory.newSchema(schemaFile);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // Create a Validator instance, which can be used to validate an instance document
        return schema.newValidator();
    }

    /**
     * Instantiates a returns a new {@code Validator} with the supplied {@link org.xml.sax.ErrorHandler}.  If the
     * {@link #ValidationUtil() default constructor} is used, the returned {@code Validator} is configured to properly
     * validate Data Conservancy generated XML using XML schema.  The supplied {@code ErrorHandler} will be used to
     * handle validation errors for this instance.
     *
     * @param schemaResource the schema used to validate XML supplied to the {@code Validator}
     * @param errorHandler handles all validation errors for the created {@code Validator}
     * @return the {@code Validator}
     * @throws RuntimeException all checked exceptions are re-thrown as RuntimeExceptions
     */
    public Validator newValidator(String schemaResource, ErrorHandler errorHandler) {
        Validator v = newValidator(schemaResource);
        v.setErrorHandler(errorHandler);
        return v;
    }

    public void validate(Validator validator, InputStream toValidate) throws IOException, SAXException {
        validator.validate(new StreamSource(toValidate));
    }

    public void validate(String schemaReource, InputStream toValidate) throws IOException, SAXException {
        newValidator(schemaReource).validate(new StreamSource(toValidate));
    }

    public void validate(String schemaReource, InputStream toValidate, ErrorHandler errorHandler) throws IOException,
            SAXException {
        newValidator(schemaReource, errorHandler).validate(new StreamSource(toValidate));
    }

    public void validate(Dcp packageToValidate) throws IOException, SAXException {
        ByteArrayOutputStream out = buildDcp(packageToValidate);
        validate(DCP_SCHEMA_RESOURCE, new ByteArrayInputStream(out.toByteArray()));
    }

    public void validate(Dcp packageToValidate, ErrorHandler errorHandler) throws IOException, SAXException {
        ByteArrayOutputStream out = buildDcp(packageToValidate);
        validate(DCP_SCHEMA_RESOURCE, new ByteArrayInputStream(out.toByteArray()));
    }

    private ByteArrayOutputStream buildDcp(Dcp packageToValidate) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        dcsModelBuilder.buildSip(packageToValidate, out);
        return out;
    }

}
