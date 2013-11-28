package org.dataconservancy.mhf.eventing.events;

/**
 * Event thrown by parsers of {@code MetadataInstance} content.  The MHF framework may emit events when parsing
 * content of a {@code MetadataInstance}; if the MHF wishes to communicate these events, subclasses may expose
 * parser-specific event properties.
 */
public abstract class MetadataParsingEvent extends MetadataHandlingEvent {

    /**
     * {@inheritDoc}
     *
     * @param objectId {@inheritDoc}
     * @param message {@inheritDoc}
     */
    protected MetadataParsingEvent(String objectId, String message) {
        super(objectId, message);
    }

}
