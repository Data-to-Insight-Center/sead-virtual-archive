package org.dataconservancy.mhf.validators;

import org.dataconservancy.mhf.validation.api.InvalidInstanceException;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.mhf.validators.dom.impl.LSResourceResolverImpl;
import org.dataconservancy.mhf.validators.util.ResourceResolver;
import org.dataconservancy.mhf.validators.util.XsdHttpResourceResolver;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import javax.xml.validation.SchemaFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Insures that XSD instance documents are validated according normal XML validation rules, and our additional
 * stipulations that all {@code &lt;xsd:include>}ed schema are resolved.
 */
public class XsdMetadataValidatorImplTest extends BaseXmlValidatorImplTest {

    @Override
    protected XmlMetadataValidatorImpl getUnderTest() {
        ResourceResolver<DcsMetadataFormat> formatResolver = getFormatResourceResolver();
        ResourceResolver<DcsMetadataScheme> schemeResolver = getSchemeResourceResolver();

        SchemaFactory schemaFactory = getSchemaFactory();
        schemaFactory.setResourceResolver(new LSResourceResolverImpl(schemeResolver));

        SchemaFactory xsdSchemaFactory = getSchemaFactory();
        ResourceResolver<DcsMetadataScheme> xsdSchemeResolver = new XsdHttpResourceResolver();
        xsdSchemaFactory.setResourceResolver(new LSResourceResolverImpl(xsdSchemeResolver));
        
        return new XsdMetadataValidatorImpl(schemaFactory, xsdSchemaFactory, formatResolver, schemeResolver, getEventManager());
    }

    /**
     * Attempts to validate a XSD metadata instance known to contain invalid content.  It insures that the
     * validation fails with the proper exceptions.
     *
     * @throws Exception
     */
    @Test
    public void testValidateInvalidXsd() throws Exception {
        final XsdMetadataValidatorImpl underTest = (XsdMetadataValidatorImpl) getUnderTest();
        ValidationException caught = null;
        Throwable cause = null;

        try {
            underTest.validate(invalidXsdMetadataInstance, null);
        } catch (InvalidInstanceException e) {
            caught = e;
            cause = e.getCause();
        }

        assertNotNull(caught);
        assertNotNull(cause);
        assertTrue(cause instanceof SAXParseException);
        assertTrue(caught.getMessage().startsWith("Validation failed: "));
    }

    /**
     * Attempts to validate a XSD metadata instance known to contain valid content.  The fact that no exception
     * is thrown is interpreted as successful validation.
     *
     * @throws Exception
     */
    @Test
    public void testValidateValidXsd() throws Exception {
        final XsdMetadataValidatorImpl underTest = (XsdMetadataValidatorImpl) getUnderTest();
        underTest.validate(validXsdMetadataInstance, null);
    }

    /**
     * Attempts to validate a FGDC XSD schema instance document.  This is a subtle test.  First, we're not testing
     * the conformance of an FGDC instance document with respect to the FGDC schema; we're testing the conformance
     * of the FGDC schema instance document with respect to the XSD schema.  We use the FGDC XSD schema as a test
     * because the document uses &lt;xsd:include/> statements to import the sub-sections of the schema document.
     * For example, from {@code fgdc-std-001-1998.xsd}:
     * <pre>
     *
     * &lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" blockDefault="#all">
     *
     *   ...
     *
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect01.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect02.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect03.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect04.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect05.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect06.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect07.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect08.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect09.xsd"/>
     *   &lt;xsd:include schemaLocation="fgdc-std-001-1998-sect10.xsd"/>
     *
     *   &lt;xsd:element name="metadata" type="metadataType"/>
     *   &lt;xsd:complexType name="metadataType">
     *
     *   ...
     *
     * &lt;/xsd:schema>
     *
     * </pre>
     * The Java Validation API will happily validate this schema, even if the files referenced by the
     * {@code schemaLocation} are missing.  To address this, XmlValidatorImpl first performs validation of the
     * XSD instance document, then XsdMetadataValidatorImpl attempts to instantiate a Schema and resolve the schema
     * parts if the validation succeeds.
     *
     * @throws Exception
     */
    @Test
    public void testValidateValidXsdWithIncludes() throws Exception {
        final XsdMetadataValidatorImpl underTest = (XsdMetadataValidatorImpl) getUnderTest();
        underTest.validate(validFgdcXsdMetadataInstance, validFgdcBaseUrl);
    }

    /**
     * Insures that an XSD instance document containing only an {@code &lt;xsd:include>} statement will fail validation
     * when the schemaLocation cannot be resolved.
     *
     * @throws Exception
     */
    @Test
    public void testValidateValidXsdWithMissingIncludes() throws Exception {

        Exception caught = null;
        Throwable cause = null;

        final XsdMetadataValidatorImpl underTest;
        try {
            underTest = (XsdMetadataValidatorImpl) getUnderTest();
            underTest.validate(validXsdMissingIncludeMetadataInstance, null);
        } catch (Exception e) {
            caught = e;
            cause = e.getCause();
        }

        assertNotNull(caught);
        assertNotNull(cause);

        assertTrue(caught instanceof ValidationException);
        assertTrue(cause instanceof ValidationException);
    }

    @Override
    @Test(expected = InvalidInstanceException.class)
    public void testValidateValidFgdc() throws Exception {
        super.testValidateValidFgdc();
    }

    @Override
    @Test(expected = InvalidInstanceException.class)
    public void testValidationIsDistinctFromParsing() throws Exception {
        super.testValidationIsDistinctFromParsing();
    }
}
