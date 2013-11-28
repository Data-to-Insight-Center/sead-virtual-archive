package org.dataconservancy.mhf.validators;

import java.net.URL;

import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.events.MetadataXmlValidationEvent;
import org.dataconservancy.mhf.eventing.listener.MetadataHandlingEventListener;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.mhf.validators.util.ResourceResolver;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.validation.SchemaFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.dataconservancy.mhf.resources.MHFTestResources.SAMPLE_FGDC_SCHEMA_BASE_URL;

/**
 * A BaseXmlValidatorImplTest that wires its components from production application contexts.  This class also overrides
 * the event-related test methods in order to use a concrete implementation of MetadataHandlingEventManager.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringXsdMetadataValidatorImplIT extends XsdMetadataValidatorImplTest {

    @Autowired
    private SchemaFactory schemaFactory;

    @Autowired
    private ResourceResolver<DcsMetadataFormat> formatResolver;

    @Autowired
    private ResourceResolver<DcsMetadataScheme> schemeResolver;

    @Autowired
    private MetadataHandlingEventManager eventManager;

    private EventListener listener;

    @Override
    protected MetadataHandlingEventManager getEventManager() {
        return eventManager;
    }

    @Override
    protected ResourceResolver<DcsMetadataFormat> getFormatResourceResolver() {
        return formatResolver;
    }

    @Override
    protected ResourceResolver<DcsMetadataScheme> getSchemeResourceResolver() {
        return schemeResolver;
    }

    @Override
    protected SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Before
    public void setUpListener() {
        this.listener = new EventListener();
        eventManager.registerListener(this.listener);
    }

    @Test
    public void testFailureEventFired() throws Exception {
        try {
            final XmlMetadataValidatorImpl underTest = getUnderTest();
            underTest.validate(invalidFgdcMetadataInstance, null);
            fail("Expected a validation exception to be thrown.");
        } catch (ValidationException e) {
            // ignore
        }

        final MetadataValidationEvent actualEvent =
                assertEventsContainsOneAndOnlyOneMetadataValidationEvent(listener.events);

        assertEquals(MetadataValidationEvent.ValidationType.FAILURE, actualEvent.getType());
    }

    @Test
    public void testErrorEventFired() throws Exception {
        try {
            final XmlMetadataValidatorImpl underTest = getUnderTest();
            underTest.validate(invalidFgdcMetadataInstance, null);
            fail("Expected a validation exception to be thrown.");
        } catch (ValidationException e) {
            // ignore
        }

        final MetadataValidationEvent actualEvent =
                assertEventsContainsOneAndOnlyOneMetadataValidationEvent(listener.events);

        assertEquals(MetadataValidationEvent.ValidationType.FAILURE, actualEvent.getType());
    }

    @Test
    public void testSuccessEventFired() throws Exception {
        final XmlMetadataValidatorImpl underTest = getUnderTest();
        underTest.validate(validFgdcXsdMetadataInstance, new URL(SAMPLE_FGDC_SCHEMA_BASE_URL));

        final MetadataValidationEvent actualEvent =
                assertEventsContainsOneAndOnlyOneMetadataValidationEvent(listener.events);

        assertEquals(MetadataValidationEvent.ValidationType.PASS, actualEvent.getType());
        assertTrue(actualEvent instanceof MetadataXmlValidationEvent);

        final MetadataXmlValidationEvent actualXmlEvent = (MetadataXmlValidationEvent) actualEvent;

        assertEquals(actualXmlEvent.getSchemaUrl(), "http://www.w3.org/2001/XMLSchema.xsd");
        //        assertTrue(actualXmlEvent.getSchemaSource().endsWith(
        //                this.getClass().getResource(MHFResources.XSD_SCHEMA_RESOURCE_PATH).toExternalForm()));
    }

    @Override
    public void testMultipleErrorsFiredWhenValidatingAnXmlFileWithMultipleErrors() throws Exception {
        try {
            final XmlMetadataValidatorImpl underTest = getUnderTest();
            underTest.validate(invalidFgdcMetadataInstanceWithMultipleErrors, validFgdcBaseUrl);
            fail("Expected a validation exception to be thrown.");
        } catch (ValidationException e) {
            // ignore
        }

        final MetadataValidationEvent actualEvent =
                assertEventsContainsOneAndOnlyOneMetadataValidationEvent(listener.events);
        assertEquals(119, listener.events.size());
        assertEquals(MetadataValidationEvent.ValidationType.FAILURE, actualEvent.getType());
    }

    private class EventListener implements MetadataHandlingEventListener {
        private List<MetadataHandlingEvent> events = new ArrayList<MetadataHandlingEvent>();

        @Override
        public void onMetadataFileHandlingEvent(MetadataHandlingEvent event) {
            events.add(event);
        }
    }
}
