package org.dataconservancy.mhf.eventing.events;

/**
 * A validation event with additional properties that are relevant to XML instance document validation.
 */
public class MetadataXmlValidationEvent extends MetadataValidationEvent {

    private String schemaUrl;

    private String schemaSource;

    public MetadataXmlValidationEvent(String objectId, String message, String validationFailure, ValidationType type) {
        super(objectId, message, validationFailure, type);
    }

    public String getSchemaSource() {
        return schemaSource;
    }

    public void setSchemaSource(String schemaSource) {
        this.schemaSource = schemaSource;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }
}
