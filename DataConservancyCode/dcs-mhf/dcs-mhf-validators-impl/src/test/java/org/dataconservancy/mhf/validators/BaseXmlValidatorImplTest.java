package org.dataconservancy.mhf.validators;

import java.net.MalformedURLException;
import java.net.URL;

import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.events.MetadataXmlValidationEvent;
import org.dataconservancy.mhf.eventing.listener.MetadataHandlingEventListener;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.instances.FileMetadataInstance;
import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.mhf.validation.api.InvalidInstanceException;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.mhf.validators.dom.impl.LSResourceResolverImpl;
import org.dataconservancy.mhf.validators.registry.impl.FormatRegistryImpl;
import org.dataconservancy.mhf.validators.registry.impl.SchemaRegistryImpl;
import org.dataconservancy.mhf.validators.util.DcsMetadataFormatResourceResolver;
import org.dataconservancy.mhf.validators.util.DcsMetadataSchemeResourceResolver;
import org.dataconservancy.mhf.validators.util.MetadataFormatLoader;
import org.dataconservancy.mhf.validators.util.ResourceResolver;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.dataconservancy.mhf.resources.MHFResources.FGDC_SCHEMA_RESOURCE_PATH;
import static org.dataconservancy.mhf.resources.MHFResources.SAMPLE_INVALID_FGDC_XML_SHAPE_FILE_MULTIPLE_ERRORS;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_INVALID_XSD_RESOURCE_PATH;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_VALID_BUT_EMPTY_XSD_RESOURCE_PATH;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_VALID_FGDC_XML_DECLARING_MAVEN_SCHEMA_RESOURCE_PATH;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_VALID_FGDC_XML_RESOURCE_PATH;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_VALID_XSD_MISSING_IMPORT;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_VALID_FGDC_SCHEMA_URL;
import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_FGDC_SCHEMA_BASE_URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Base test class containing members and setup methods for XML-related validation.  Includes creating MetadataInstances
 * used by the tests, and a registry containing schema resources used when validating the instances.
 */
@ContextConfiguration({"classpath:/org/dataconservancy/model/config/applicationContext.xml",
        "classpath*:/org/dataconservancy/mhf/config/applicationContext.xml",
        "classpath:/org/dataconservancy/registry/config/applicationContext.xml"})
public abstract class BaseXmlValidatorImplTest {

    /**
     * Logger instance for this test class.
     */
    protected ValidatorLogger log = ValidatorLoggerFactory.getLogger(this.getClass());

    /**
     * Encapsulates an FGDC XML document that is known to be <em>invalid</em> with regard to the FGDC 1998 XML schema.
     */
    protected MetadataInstance invalidFgdcMetadataInstance;

    /**
     * Encapsulates an FGDC XML document that is known to be <em>valid</em> with regard to the FGDC 1998 XML schema.
     */
    protected MetadataInstance validFgdcMetadataInstance;

    /**
     * Encapsulates an XSD document that is known to be <em>invalid</em> with regard to the XSD 2004 XML schema.
     */
    protected MetadataInstance invalidXsdMetadataInstance;

    /**
     * Encapsulates an XSD document that is known to be <em>valid</em> with regard to the XSD 2004 XML schema.
     */
    protected MetadataInstance validXsdMetadataInstance;

    protected MetadataInstance validFgdcXsdMetadataInstance;
    
    protected URL validFgdcBaseUrl;

    protected MetadataInstance validXsdMissingIncludeMetadataInstance;

    /**
     * Encapsulates a valid FGDC document that declares a schemaLocation referencing the Maven POM schema.
     */
    protected MetadataInstance validFgdcMetadataInstanceWithMavenPomSchemaDef;

    /**
     * This FGDC metadata instance is invalid, and has multiple errors in it.  We use this file to test the fact that
     * all the errors reported in this file are recorded as events.
     */
    protected MetadataInstance invalidFgdcMetadataInstanceWithMultipleErrors;

    /**
     * The event manager to supply to the validator under test.  Normally mocked.
     */
    protected MetadataHandlingEventManager eventManager;

    /**
     * Instantiates new MetadataInstances that are used in the test methods.
     * <dl>
     * <dt>invalidFgdcMetadataInstance</dt>
     * <dd>encapsulates an FGDC XML document that is known to be <em>invalid</em> with regard to the FGDC 1998
     * XML schema</dd>
     * <dt>validFgdcMetadataInstance</dt>
     * <dd>encapsulates an FGDC XML document that is known to be <em>valid</em> with regard to the FGDC 1998 XML
     * schema</dd>
     * <dt>invalidXsdMetadataInstance</dt>
     * <dd>encapsulates an XSD document that is known to be <em>invalid</em> with regard to the XSD 2004 XML
     * schema</dd>
     * <dt>validXsdMetadataInstance</dt>
     * <dd>encapsulates an XSD document that is known to be <em>valid</em> with regard to the XSD 2004 XML
     * schema</dd>
     * <dt>validFgdcMetadataInstanceWithMavenPomSchemaDef</dt>
     * <dd>encapsulates a valid FGDC document that declares a schemaLocation referencing the Maven POM schema.</dd>
     * </dl>
     * @throws MalformedURLException 
     */
    @Before
    public void setUpMetadataInstances() throws MalformedURLException {
        invalidFgdcMetadataInstance = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID,
                this.getClass().getResource(SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH));

        validFgdcMetadataInstance = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID,
                this.getClass().getResource(SAMPLE_VALID_FGDC_XML_RESOURCE_PATH));

        invalidXsdMetadataInstance = new FileMetadataInstance(MetadataFormatId.XSD_XML_FORMAT_ID,
                this.getClass().getResource(SAMPLE_INVALID_XSD_RESOURCE_PATH));

        validXsdMetadataInstance = new FileMetadataInstance(MetadataFormatId.XSD_XML_FORMAT_ID,
                this.getClass().getResource(SAMPLE_VALID_BUT_EMPTY_XSD_RESOURCE_PATH));

        validFgdcMetadataInstanceWithMavenPomSchemaDef = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID,
                this.getClass().getResource(SAMPLE_VALID_FGDC_XML_DECLARING_MAVEN_SCHEMA_RESOURCE_PATH));

        validFgdcXsdMetadataInstance = new FileMetadataInstance(MetadataFormatId.XSD_XML_FORMAT_ID,
                new URL(SAMPLE_VALID_FGDC_SCHEMA_URL));

        validXsdMissingIncludeMetadataInstance = new FileMetadataInstance(MetadataFormatId.XSD_XML_FORMAT_ID,
                this.getClass().getResource(SAMPLE_VALID_XSD_MISSING_IMPORT));

        invalidFgdcMetadataInstanceWithMultipleErrors = new FileMetadataInstance(MetadataFormatId.FGDC_XML_FORMAT_ID,
                this.getClass().getResource(SAMPLE_INVALID_FGDC_XML_SHAPE_FILE_MULTIPLE_ERRORS));

        validFgdcBaseUrl = new URL(SAMPLE_FGDC_SCHEMA_BASE_URL);
    }

    /**
     * Simply configures a mocked MetadataHandlingEventManager.  If there are test methods interested in testing
     * the interaction of the XmlMetadataValidatorImpl and the event manager, they will need to either configure
     * behaviors on this mock or construct a new instance.
     */
    @Before
    public void setUpEventManager() {
        eventManager = mock(MetadataHandlingEventManager.class);
    }

    protected XmlMetadataValidatorImpl getUnderTest() {
        ResourceResolver<DcsMetadataFormat> formatResolver = getFormatResourceResolver();
        ResourceResolver<DcsMetadataScheme> schemeResolver = getSchemeResourceResolver();

        SchemaFactory schemaFactory = getSchemaFactory();

        schemaFactory.setResourceResolver(new LSResourceResolverImpl(schemeResolver));

        return new XmlMetadataValidatorImpl(schemaFactory, formatResolver, schemeResolver, getEventManager());
    }

    protected SchemaFactory getSchemaFactory() {
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    protected ResourceResolver<DcsMetadataFormat> getFormatResourceResolver() {
        MetadataFormatLoader loader = new MetadataFormatLoader(new MetadataFormatMapper(new MetadataSchemeMapper()),
                new DcsXstreamStaxModelBuilder());
        FormatRegistryImpl formatRegistry = new FormatRegistryImpl("ignored", "Registry of MetadataFormats", loader);
        return new DcsMetadataFormatResourceResolver(formatRegistry);
    }

    protected ResourceResolver<DcsMetadataScheme> getSchemeResourceResolver() {
        MetadataFormatLoader loader = new MetadataFormatLoader(new MetadataFormatMapper(new MetadataSchemeMapper()),
                new DcsXstreamStaxModelBuilder());
        SchemaRegistryImpl schemaRegistry = new SchemaRegistryImpl("ignoredaswell", "Registry of MetadataSchemes", loader);
        return new DcsMetadataSchemeResourceResolver(schemaRegistry);
    }

    protected MetadataHandlingEventManager getEventManager() {
        return eventManager;
    }

    protected void mockAnswerWhichCollectsEvents(final Collection<MetadataHandlingEvent> collectionOfEvents) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                collectionOfEvents.add((MetadataHandlingEvent) invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(eventManager).sendEvent(any(MetadataHandlingEvent.class));
    }

    protected MetadataValidationEvent assertEventsContainsOneAndOnlyOneMetadataValidationEvent
            (Collection<MetadataHandlingEvent> events) {
        assertNotNull(events);
        assertTrue(events.size() > 0);
        int found = 0;
        MetadataValidationEvent mve = null;

        for (MetadataHandlingEvent e : events) {
            if (e instanceof MetadataValidationEvent) {
                found++;
                mve = (MetadataValidationEvent) e;
            }
        }

        assertEquals(1, found);
        assertNotNull(mve);
        return mve;
    }

    /**
     * Attempts to validate a FGDC metadata instance known to contain invalid content.  It insures that the
     * validation fails with the proper exceptions.
     *
     * @throws Exception
     */
    @Test
    public void testValidateInvalidFgdc() throws Exception {
        ValidationException caught = null;
        Throwable cause = null;

        try {
            final XmlMetadataValidatorImpl underTest = getUnderTest();
            underTest.validate(invalidFgdcMetadataInstance, validFgdcBaseUrl);
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
     * Attempts to validate a FGDC metadata instance known to contain valid content.  The fact that no exception
     * is thrown is interpreted as successful validation.
     *
     * @throws Exception
     */
    @Test
    public void testValidateValidFgdc() throws Exception {
        final XmlMetadataValidatorImpl underTest = getUnderTest();
        underTest.validate(validFgdcMetadataInstance, validFgdcBaseUrl);
    }

    /**
     * Attempts to validate a FGDC metadata instance known to contain valid content.  This particular FGDC instance
     * document contains a reference to another schema.  If our validator performed schema detection, it would use
     * this other schema and find that the instance document doesn't validate.  However, we do not support schema
     * detection; the schema used to perform validation is not the schema referenced by the instance document, it is
     * the schema resolved by the resource resolver.  The fact that no exception is thrown is interpreted as successful
     * validation.
     *
     * @throws Exception
     */
    @Test
    public void testValidationIsDistinctFromParsing() throws Exception {
        final XmlMetadataValidatorImpl underTest = getUnderTest();
        underTest.validate(validFgdcMetadataInstanceWithMavenPomSchemaDef, validFgdcBaseUrl);
    }

    @Test
    public void testFailureEventFired() throws Exception {
        final Collection<MetadataHandlingEvent> events = new ArrayList<MetadataHandlingEvent>();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                events.add((MetadataHandlingEvent) invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(eventManager).sendEvent(any(MetadataHandlingEvent.class));

        try {
            final XmlMetadataValidatorImpl underTest = getUnderTest();
            underTest.validate(invalidFgdcMetadataInstance, validFgdcBaseUrl);
            fail("Expected a validation exception to be thrown.");
        } catch (ValidationException e) {
            // ignore
        }

        verify(eventManager, atLeastOnce()).sendEvent(any(MetadataValidationEvent.class));
        final MetadataValidationEvent actualEvent = assertEventsContainsOneAndOnlyOneMetadataValidationEvent(events);

        assertEquals(MetadataValidationEvent.ValidationType.FAILURE, actualEvent.getType());
    }

    @Test
    public void testErrorEventFired() throws Exception {
        final Collection<MetadataHandlingEvent> events = new ArrayList<MetadataHandlingEvent>();

        mockAnswerWhichCollectsEvents(events);

        try {
            final XmlMetadataValidatorImpl underTest = getUnderTest();
            underTest.validate(invalidFgdcMetadataInstance, validFgdcBaseUrl);
            fail("Expected a validation exception to be thrown.");
        } catch (ValidationException e) {
            // ignore
        }

        verify(eventManager, atLeastOnce()).sendEvent(any(MetadataValidationEvent.class));
        final MetadataValidationEvent actualEvent = assertEventsContainsOneAndOnlyOneMetadataValidationEvent(events);

        assertEquals(MetadataValidationEvent.ValidationType.FAILURE, actualEvent.getType());
    }

    /**
     * Insures that when an attempt to validate an invalid XML file is performed, all of the errors in the document
     * are reported.
     *
     * @throws Exception
     */
    @Test
    public void testMultipleErrorsFiredWhenValidatingAnXmlFileWithMultipleErrors() throws Exception {
        final Collection<MetadataHandlingEvent> events = new ArrayList<MetadataHandlingEvent>();

        mockAnswerWhichCollectsEvents(events);

        try {
            final XmlMetadataValidatorImpl underTest = getUnderTest();
            underTest.validate(invalidFgdcMetadataInstanceWithMultipleErrors, validFgdcBaseUrl);
            fail("Expected a validation exception to be thrown.");
        } catch (ValidationException e) {
            // ignore
        }

        verify(eventManager, times(119)).sendEvent(any(MetadataValidationEvent.class));
        final MetadataValidationEvent actualEvent = assertEventsContainsOneAndOnlyOneMetadataValidationEvent(events);
        assertEquals(MetadataValidationEvent.ValidationType.FAILURE, actualEvent.getType());
    }

    @Test
    public void testSuccessEventFired() throws Exception {
        final Collection<MetadataHandlingEvent> events = new ArrayList<MetadataHandlingEvent>();

        mockAnswerWhichCollectsEvents(events);

        final XmlMetadataValidatorImpl underTest = getUnderTest();
        underTest.validate(validFgdcXsdMetadataInstance, validFgdcBaseUrl);

        verify(eventManager).sendEvent(any(MetadataHandlingEvent.class));
        final MetadataValidationEvent actualEvent = assertEventsContainsOneAndOnlyOneMetadataValidationEvent(events);

        assertEquals(MetadataValidationEvent.ValidationType.PASS, actualEvent.getType());
        assertTrue(actualEvent instanceof MetadataXmlValidationEvent);

        final MetadataXmlValidationEvent actualXmlEvent = (MetadataXmlValidationEvent) actualEvent;

        assertEquals(actualXmlEvent.getSchemaUrl(), "http://www.w3.org/2001/XMLSchema.xsd");
//        assertTrue(actualXmlEvent.getSchemaSource().endsWith(
//                this.getClass().getResource(MHFResources.XSD_SCHEMA_RESOURCE_PATH).toExternalForm()));
    }

    private class EventListener implements MetadataHandlingEventListener {

        private List<MetadataHandlingEvent> events = new ArrayList<MetadataHandlingEvent>();

        @Override
        public void onMetadataFileHandlingEvent(MetadataHandlingEvent event) {
            events.add(event);
        }
    }

}
