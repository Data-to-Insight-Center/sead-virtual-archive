package org.dataconservancy.mhf.validators.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 *
 */
public class ValidationUtil {

    /**
     * A default instance of {@code SchemaFactory}, used if one isn't supplied on construction.
     */
    private SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    /**
     * Constructs a {@code ValidationUtil} with a namespace-aware, non-validating,
     * {@link javax.xml.parsers.DocumentBuilder parser}, using the supplied {@link SchemaFactory}.
     */
    public ValidationUtil(SchemaFactory factory) {
        this.factory = factory;
    }

    /**
     * Instantiates a returns a new {@code Validator} for the supplied {@code schemaResource}.
     *
     * @param schemaResource the schema used to validate XML supplied to the {@code Validator}
     * @return the {@code Validator}
     * @throws RuntimeException all checked exceptions are re-thrown as RuntimeExceptions
     */
    public Validator newValidator(String schemaResource) {
        // Load the schema
        final URL schemaUrl = this.getClass().getResource(schemaResource);
        if (schemaUrl == null) {
            throw new RuntimeException("Unable to resolve the schema resource '" + schemaResource + "' from the " +
                    "classpath: '" + schemaUrl + "'");
        }

        Source schemaFile = null;
        try {
            schemaFile = new StreamSource(schemaUrl.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return newValidator(schemaFile);
    }

    /**
     * Instantiates a returns a new {@code Validator} for the supplied {@code schemaSource}.
     *
     * @param schemaSource the schema used to validate XML supplied to the {@code Validator}
     * @return the {@code Validator}
     * @throws RuntimeException all checked exceptions are re-thrown as RuntimeExceptions
     */
    public Validator newValidator(Source schemaSource) {
        Schema schema = null;
        try {
            schema = factory.newSchema(schemaSource);
        } catch (SAXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // Create a Validator instance, which can be used to validate an instance document
        return schema.newValidator();
    }

    /**
     * Instantiates a returns a new {@code Validator} for the supplied {@code schemaResource} with the supplied
     * {@link org.xml.sax.ErrorHandler}. The supplied {@code ErrorHandler} will be used to handle validation errors for
     * the {@code Validator} instance.
     *
     * @param schemaResource the schema used to validate XML supplied to the {@code Validator}
     * @param errorHandler   handles all validation errors for the created {@code Validator}
     * @return the {@code Validator}
     * @throws RuntimeException all checked exceptions are re-thrown as RuntimeExceptions
     */
    public Validator newValidator(String schemaResource, ErrorHandler errorHandler) {
        Validator v = newValidator(schemaResource);
        v.setErrorHandler(errorHandler);
        return v;
    }

    /**
     * Instantiates a returns a new {@code Validator} for the supplied {@code schemaSource} with the supplied
     * {@link org.xml.sax.ErrorHandler}. The supplied {@code ErrorHandler} will be used to handle validation errors for
     * the {@code Validator} instance.
     *
     * @param schemaSource the schema used to validate XML supplied to the {@code Validator}
     * @param errorHandler   handles all validation errors for the created {@code Validator}
     * @return the {@code Validator}
     * @throws RuntimeException all checked exceptions are re-thrown as RuntimeExceptions
     */
    public Validator newValidator(Source schemaSource, ErrorHandler errorHandler) {
        Validator v = newValidator(schemaSource);
        v.setErrorHandler(errorHandler);
        return v;
    }

    public void validate(Validator validator, InputStream toValidate) throws IOException, SAXException {
        validator.validate(new StreamSource(toValidate));
    }

    public void validate(String schemaResource, InputStream toValidate) throws IOException, SAXException {
        newValidator(schemaResource).validate(new StreamSource(toValidate));
    }

    public void validate(String schemaResource, InputStream toValidate, ErrorHandler errorHandler) throws IOException,
            SAXException {
        newValidator(schemaResource, errorHandler).validate(new StreamSource(toValidate));
    }

    public void validate(Source schema, InputStream toValidate) throws IOException, SAXException {
        newValidator(schema).validate(new StreamSource(toValidate));
    }

    public void validate(Source schema, InputStream toValidate, ErrorHandler errorHandler) throws IOException,
            SAXException {
        newValidator(schema, errorHandler).validate(new StreamSource(toValidate));
    }

    public void validate(InputStream toValidate) {

    }

}
