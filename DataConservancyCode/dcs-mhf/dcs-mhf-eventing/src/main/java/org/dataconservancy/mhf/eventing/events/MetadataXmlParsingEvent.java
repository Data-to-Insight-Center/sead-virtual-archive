package org.dataconservancy.mhf.eventing.events;

/**
 * Event thrown when parsing XML content.  Practically speaking, this event will contain SAXParserException properties
 * that are obtained from a SAX ErrorHandler, plus additional information about the schema used to validate the XML
 * instance document.  Listeners (that is, clients of the MHF) may evaluate these events and decide, independent of the
 * MHF, whether or not an XML document is valid.
 */
public class MetadataXmlParsingEvent extends MetadataParsingEvent {

    /**
     * The severity of the event.  The WARN through FATAL event levels correspond to the
     * SAX ErrorHandler severity.
     */
    public static enum SEVERITY {
        /**
         * Informational
         */
        INFO,

        /**
         * Warning
         */
        WARN,

        /**
         * Error (invalid content)
         */
        ERROR,

        /**
         * Fatal (not well-formed)
         */
        FATAL
    }

    /**
     * The description of the DcsMetadataScheme used to validate the XML instance document
     */
    private String schemaDescription;

    /**
     * The registry entry identifier of the DcsMetadataScheme used to validate the XML instance document
     */
    private String schemaRegistryEntryId;

    /**
     * The well-known, public, URL of the schema used to validate the XML instance document
     */
    private String schemaUrl;

    /**
     * The source URL of the schema used to validate the XML instance document.  This is where the content of the
     * schema was retrieved from (distinct from the {@link #schemaUrl})
     */
    private String schemaSource;

    /**
     * The public id of the XML instance document being validated
     */
    private String publicId;

    /**
     * The system id of the XML instance document being validated
     */
    private String systemId;

    /**
     * The column number of the end of the XML instance document text where the event occurred
     */
    private int columnNumber;

    /**
     * The line number of the end of the XML instance document text where the event occurred
     */
    private int lineNumber;

    /**
     * The severity of the event.  The WARN through FATAL event levels correspond to the
     * SAX ErrorHandler severity.
     */
    private SEVERITY severity;

    /**
     * {@inheritDoc}
     *
     * @param objectId {@inheritDoc}
     * @param message  {@inheritDoc}
     */
    public MetadataXmlParsingEvent(String objectId, String message) {
        super(objectId, message);
    }

    /**
     * The column number of the end of the XML instance document text where the event occurred
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * The line number of the end of the XML instance document text where the event occurred
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * The public id of the XML instance document being validated
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * The description of the DcsMetadataScheme used to validate the XML instance document
     */
    public String getSchemaDescription() {
        return schemaDescription;
    }

    /**
     * The registry entry identifier of the DcsMetadataScheme used to validate the XML instance document
     */
    public String getSchemaRegistryEntryId() {
        return schemaRegistryEntryId;
    }

    /**
     * The source URL of the schema used to validate the XML instance document.  This is where the content of the
     * schema was retrieved from (distinct from the {@link #schemaUrl})
     */
    public String getSchemaSource() {
        return schemaSource;
    }

    /**
     * The well-known, public, URL of the schema used to validate the XML instance document
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }

    /**
     * The system id of the XML instance document being validated
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * The severity of the event.  The WARN through FATAL event levels correspond to the
     * SAX ErrorHandler severity.
     */
    public SEVERITY getSeverity() {
        return severity;
    }

    /**
     * The column number of the end of the XML instance document text where the event occurred
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * The line number of the end of the XML instance document text where the event occurred
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * The public id of the XML instance document being validated
     */
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setSchemaDescription(String schemaDescription) {
        this.schemaDescription = schemaDescription;
    }

    /**
     * The description of the DcsMetadataScheme used to validate the XML instance document
     */
    public void setSchemaRegistryEntryId(String schemaRegistryEntryId) {
        this.schemaRegistryEntryId = schemaRegistryEntryId;
    }

    /**
     * The source URL of the schema used to validate the XML instance document.  This is where the content of the
     * schema was retrieved from (distinct from the {@link #schemaUrl})
     */
    public void setSchemaSource(String schemaSource) {
        this.schemaSource = schemaSource;
    }

    /**
     * The well-known, public, URL of the schema used to validate the XML instance document
     */
    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }

    /**
     * The system id of the XML instance document being validated
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * The severity of the event.  The WARN through FATAL event levels correspond to the
     * SAX ErrorHandler severity.
     */
    public void setSeverity(SEVERITY severity) {
        this.severity = severity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MetadataXmlParsingEvent that = (MetadataXmlParsingEvent) o;

        if (columnNumber != that.columnNumber) return false;
        if (lineNumber != that.lineNumber) return false;
        if (publicId != null ? !publicId.equals(that.publicId) : that.publicId != null) return false;
        if (schemaDescription != null ? !schemaDescription.equals(that.schemaDescription) : that.schemaDescription != null)
            return false;
        if (schemaRegistryEntryId != null ? !schemaRegistryEntryId.equals(that.schemaRegistryEntryId) : that.schemaRegistryEntryId != null)
            return false;
        if (schemaSource != null ? !schemaSource.equals(that.schemaSource) : that.schemaSource != null) return false;
        if (schemaUrl != null ? !schemaUrl.equals(that.schemaUrl) : that.schemaUrl != null) return false;
        if (severity != that.severity) return false;
        if (systemId != null ? !systemId.equals(that.systemId) : that.systemId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (schemaDescription != null ? schemaDescription.hashCode() : 0);
        result = 31 * result + (schemaRegistryEntryId != null ? schemaRegistryEntryId.hashCode() : 0);
        result = 31 * result + (schemaUrl != null ? schemaUrl.hashCode() : 0);
        result = 31 * result + (schemaSource != null ? schemaSource.hashCode() : 0);
        result = 31 * result + (publicId != null ? publicId.hashCode() : 0);
        result = 31 * result + (systemId != null ? systemId.hashCode() : 0);
        result = 31 * result + columnNumber;
        result = 31 * result + lineNumber;
        result = 31 * result + (severity != null ? severity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetadataXmlParsingEvent{" +
                "columnNumber=" + columnNumber +
                ", schemaDescription='" + schemaDescription + '\'' +
                ", schemaRegistryEntryId='" + schemaRegistryEntryId + '\'' +
                ", schemaUrl='" + schemaUrl + '\'' +
                ", schemaSource='" + schemaSource + '\'' +
                ", publicId='" + publicId + '\'' +
                ", systemId='" + systemId + '\'' +
                ", lineNumber=" + lineNumber +
                ", severity=" + severity +
                '}';
    }
}
