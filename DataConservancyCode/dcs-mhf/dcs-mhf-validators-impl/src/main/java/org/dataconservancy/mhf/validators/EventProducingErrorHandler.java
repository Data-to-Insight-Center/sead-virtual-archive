package org.dataconservancy.mhf.validators;

import org.dataconservancy.mhf.eventing.events.MetadataXmlParsingEvent;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * SAX error handler that produces {@code MetadataParsingEvent}s without re-throwing any of the SAXParserExceptions.
 * This error handler is initialized with the same {@code DcsMetadataScheme} that represents the schema being used
 * to validate the XML instance document.  This is so the events produced by this handler contain all the required
 * properties.
 */
public class EventProducingErrorHandler extends ErrorHandlerWrapper {

    /**
     * The RegistryEntry identifier of the DcsMetadataScheme being used to validate the XML document.
     */
    private String schemaRegistryEntryId;

    /**
     * The well-known URL to the XML schema being used to validate the XML document.  Distinct from the
     * {@link #schemaSource}, which is where the content of the XML schema may be retrieved from.
     */
    private String schemaUrl;

    /**
     * The URL to the content of the XML schema being used to validate the XML instance document.
     */
    private String schemaSource;

    /**
     * The description of the DcsMetadataScheme being used to validate the XML instance document.
     */
    private String description;

    /**
     * The MHF event manager used to fire events.
     */
    private MetadataHandlingEventManager eventManager;

    /**
     * Private constructor.  This instance should always be provided a MetadataHandlingEventManager.
     *
     * @param metadataFormat the DcsMetadataScheme being used to validate the XML instance document
     */
    private EventProducingErrorHandler(ErrorHandler toWrap, RegistryEntry<DcsMetadataFormat> metadataFormat) {
        super(toWrap);
        if (metadataFormat == null) {
            throw new IllegalArgumentException("The registry entry must not be null.");
        }

        if (metadataFormat.getEntry() == null) {
            throw new IllegalArgumentException("The metadata scheme wrapped by the registry entry must not be null.");
        }

        DcsMetadataScheme scheme = null;
        if (metadataFormat.getEntry().getSchemes().iterator().hasNext()) {
            scheme = metadataFormat.getEntry().getSchemes().iterator().next();
        }
        
        if (scheme != null) {
            this.schemaUrl = scheme.getSchemaUrl();
            this.schemaSource = scheme.getSource();
        }
        
        this.schemaRegistryEntryId = metadataFormat.getId();
        this.description = metadataFormat.getDescription();
    }

    /**
     * Constructs an instance of the error handler.
     *
     * @param metadataFormat the DcsMetadataScheme being used to validate the XML instance document
     * @param eventManager the MetadataHandlingEventManager used to fire events to clients of the MHF
     */
    public EventProducingErrorHandler(ErrorHandler toWrap, RegistryEntry<DcsMetadataFormat> metadataFormat,
                                      MetadataHandlingEventManager eventManager) {
        this(toWrap, metadataFormat);
        if (eventManager == null) {
            throw new IllegalArgumentException("Event Manager must not be null.");
        }
        this.eventManager = eventManager;
    }

    @Override
    public void handleErrorInternal(SAXParseException e) {
        handleExceptionAndFireEvent(MetadataXmlParsingEvent.SEVERITY.ERROR, e);
    }

    @Override
    public void handleFatalErrorInternal(SAXParseException e) {
        handleExceptionAndFireEvent(MetadataXmlParsingEvent.SEVERITY.FATAL, e);
    }

    @Override
    public void handleWarningInternal(SAXParseException e) {
        handleExceptionAndFireEvent(MetadataXmlParsingEvent.SEVERITY.WARN, e);
    }

    /**
     * The identifier of the registry entry containing the DcsMetadataScheme used to validate the XML instance
     * document.
     *
     * @return the registry entry identifier
     */
    public String getSchemaRegistryEntryId() {
        return schemaRegistryEntryId;
    }

    /**
     * The location of the content of the XSD schema used to validate the XML instance document.
     *
     * @return the location of the XSD schema content
     */
    public String getSchemaSource() {
        return schemaSource;
    }

    /**
     * The public, well-known, location of the XSD schema, distinct from the {@link #getSchemaSource() source}.
     *
     * @return the well-known url of the XSD schema
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }

    /**
     * The description of the DcsMetadataScheme used to validate the XML instance document.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates, populates, and fires a MetadataXmlParsingEvent.
     * <p/>
     * Package-private for unit testing.
     *
     * @param level the severity of the SAXParserException being thrown
     * @param e the SAXParserException
     */
    void handleExceptionAndFireEvent(MetadataXmlParsingEvent.SEVERITY level, SAXParseException e) {
        MetadataXmlParsingEvent event = new MetadataXmlParsingEvent(null, e.getMessage());
        event.setSeverity(level);
        populateEvent(event, e);
        eventManager.sendEvent(event);
    }

    /**
     * Populates the supplied event with information from the SAXParseException.
     * <p/>
     * Package-private for unit testing.
     *
     * @param event the event to populate
     * @param e the SAXParseException to populate from
     * @return the populated event
     */
    MetadataXmlParsingEvent populateEvent(MetadataXmlParsingEvent event, SAXParseException e) {
        event.setSchemaRegistryEntryId(schemaRegistryEntryId);
        event.setSchemaUrl(schemaUrl);
        event.setSchemaSource(schemaSource);
        event.setSchemaDescription(description);
        if (e.getLineNumber() > -1) {
            event.setLineNumber(e.getLineNumber());
        }

        if (e.getColumnNumber() > -1) {
            event.setColumnNumber(e.getColumnNumber());
        }

        if (e.getPublicId() != null) {
            event.setPublicId(e.getPublicId());
        }

        if (e.getSystemId() != null) {
            event.setSystemId(e.getSystemId());
        }

        return event;
    }

}
