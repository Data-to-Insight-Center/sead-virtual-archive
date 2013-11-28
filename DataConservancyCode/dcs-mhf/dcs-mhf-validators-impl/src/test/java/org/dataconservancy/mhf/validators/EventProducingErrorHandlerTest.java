package org.dataconservancy.mhf.validators;

import org.dataconservancy.mhf.eventing.events.MetadataXmlParsingEvent;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dataconservancy.mhf.eventing.events.MetadataXmlParsingEvent.SEVERITY.ERROR;
import static org.dataconservancy.mhf.eventing.events.MetadataXmlParsingEvent.SEVERITY.FATAL;
import static org.dataconservancy.mhf.eventing.events.MetadataXmlParsingEvent.SEVERITY.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Insures that the ErrorProducingErrorHandler properly composes the events it emits.
 */
public class EventProducingErrorHandlerTest {

    private static final String SCHEME_SOURCE = "http://some/dcs/datastream/url";

    private static final String SCHEME_URL = "http://some/public/url";

    private static final String SCHEME_NAME = "A Metadata Scheme Name";

    private static final String SCHEME_VERSION = "2.0";

    private static final String REGISTRY_ENTRY_ID = "dataconservancy.org:registry:metadatascheme:a-scheme:id";

    private static final String REGISTRY_ENTRY_DESC = "A scheme description";

    private static final String REGISTRY_ENTRY_TYPE = "dataconservancy.org:registry:metadatascheme";

    private static final String SAXPARSE_PUBLICID = "a public id";

    private static final String SAXPARSE_SYSTEMID = "a system id";

    private static final String SAXPARSE_MESSAGE = "exception message";

    private static final int SAXPARSE_LINENO = 144;

    private static final int SAXPARSE_COLNO = 23;

    private final DateTime now = DateTime.now();

    private BasicRegistryEntryImpl<DcsMetadataFormat> registryEntry = new BasicRegistryEntryImpl<DcsMetadataFormat>();

    private DcsMetadataScheme scheme;
    private DcsMetadataFormat format;
    
    private MetadataHandlingEventManager eventManager;

    private ErrorHandler wrappedErrorHandler;

    private SAXParseException ex;


    @Before
    public void setUp() throws Exception {
        eventManager = mock(MetadataHandlingEventManager.class);

        wrappedErrorHandler = mock(ErrorHandler.class);

        scheme = new DcsMetadataScheme();
        scheme.setName(SCHEME_NAME);
        scheme.setSchemaUrl(SCHEME_URL);
        scheme.setSchemaVersion(SCHEME_VERSION);
        scheme.setSource(SCHEME_SOURCE);

        format = new DcsMetadataFormat();
        format.setName(SCHEME_NAME);
        format.setVersion(SCHEME_VERSION);
        format.setId(REGISTRY_ENTRY_ID);
        format.addScheme(scheme);
        
        registryEntry.setId(REGISTRY_ENTRY_ID);
        registryEntry.setDescription(REGISTRY_ENTRY_DESC);
        registryEntry.setEntryType(REGISTRY_ENTRY_TYPE);
        registryEntry.setEntry(format);
        registryEntry.setKeys(Arrays.asList(SCHEME_URL));

        ex = new SAXParseException(SAXPARSE_MESSAGE, SAXPARSE_PUBLICID, SAXPARSE_SYSTEMID,
                SAXPARSE_LINENO, SAXPARSE_COLNO);
    }


    @Test
    public void testHandleErrorInternal() throws Exception {
        final MetadataXmlParsingEvent expectedEvent = newExpectedEvent(ERROR, null, ex.getMessage());
        final List<MetadataXmlParsingEvent> firedEvents = new ArrayList<MetadataXmlParsingEvent>();

        collectFiredEvents(firedEvents);

        final EventProducingErrorHandler underTest = new EventProducingErrorHandler(wrappedErrorHandler,
                registryEntry, eventManager);

        underTest.handleErrorInternal(ex);

        verifiedFiredEvents(firedEvents);

        final MetadataXmlParsingEvent actual = firedEvents.get(0);
        actual.setEventGenerated(now);
        assertEquals(expectedEvent, actual);
    }

    @Test
    public void testHandleWarningInternal() throws Exception {
        final MetadataXmlParsingEvent expectedEvent = newExpectedEvent(WARN, null, ex.getMessage());
        final List<MetadataXmlParsingEvent> firedEvents = new ArrayList<MetadataXmlParsingEvent>();

        collectFiredEvents(firedEvents);

        final EventProducingErrorHandler underTest = new EventProducingErrorHandler(wrappedErrorHandler,
                registryEntry, eventManager);

        underTest.handleWarningInternal(ex);

        verifiedFiredEvents(firedEvents);

        final MetadataXmlParsingEvent actual = firedEvents.get(0);
        actual.setEventGenerated(now);
        assertEquals(expectedEvent, actual);
    }

    @Test
    public void testHandleFatalErrorInternal() throws Exception {
        final MetadataXmlParsingEvent expectedEvent = newExpectedEvent(FATAL, null, ex.getMessage());
        final List<MetadataXmlParsingEvent> firedEvents = new ArrayList<MetadataXmlParsingEvent>();

        collectFiredEvents(firedEvents);

        final EventProducingErrorHandler underTest = new EventProducingErrorHandler(wrappedErrorHandler,
                registryEntry, eventManager);

        underTest.handleFatalErrorInternal(ex);

        verifiedFiredEvents(firedEvents);

        final MetadataXmlParsingEvent actual = firedEvents.get(0);
        actual.setEventGenerated(now);
        assertEquals(expectedEvent, actual);
    }


    private void collectFiredEvents(final List<MetadataXmlParsingEvent> eventCollection) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                eventCollection.add((MetadataXmlParsingEvent) invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(eventManager).sendEvent(any(MetadataXmlParsingEvent.class));
    }

    private void verifiedFiredEvents(final List<MetadataXmlParsingEvent> eventCollection) {
        verify(eventManager).sendEvent(any(MetadataXmlParsingEvent.class));
        assertEquals(1, eventCollection.size());
    }

    private MetadataXmlParsingEvent newExpectedEvent(MetadataXmlParsingEvent.SEVERITY severity, String objectId,
                                                     String message) {
        final MetadataXmlParsingEvent expectedEvent = new MetadataXmlParsingEvent(objectId, message);
        expectedEvent.setSeverity(severity);
        expectedEvent.setColumnNumber(SAXPARSE_COLNO);
        expectedEvent.setLineNumber(SAXPARSE_LINENO);
        expectedEvent.setPublicId(SAXPARSE_PUBLICID);
        expectedEvent.setSystemId(SAXPARSE_SYSTEMID);
        expectedEvent.setSchemaDescription(REGISTRY_ENTRY_DESC);
        expectedEvent.setSchemaRegistryEntryId(REGISTRY_ENTRY_ID);
        expectedEvent.setSchemaSource(SCHEME_SOURCE);
        expectedEvent.setSchemaUrl(SCHEME_URL);
        expectedEvent.setEventGenerated(now);
        return expectedEvent;
    }

}
