package org.dataconservancy.mhf.validators;

/**
 * String constants used for producing error or logging messages.  Generally each constant needs to be parameterized
 * using {@code String.format(...)}.
 */
class Messages {
    /**
     * Error message used when looking up a format id from the registry returns no entries.  Parameters are: format id
     */
    static final String CANNOT_RESOLVE_ENTRY_FOR_FORMAT =
            "Could not resolve the registry entry for %s";
    /**
     * Error message used when the RegistryEntry doesn't contain an
     * {@link org.dataconservancy.registry.api.RegistryEntry#getEntry() embedded}
     * DcsMetadataScheme object. Parameters are: format id
     */
    static final String MISSING_REGISTRY_ENTRY =
            "Registry entry for %s did not contain a DcsMetadataScheme entry!";
    /**
     * Error message used when the DcsMetadataScheme source property is empty or null.  Parameters are: format id
     */
    static final String MISSING_SCHEME_SOURCE =
            "Could not resolve a schema for %s";
    /**
     * Error message used when the DcsMetadataScheme source property cannot be used to construct a URL.  Parameters
     * are: format id, metadata scheme source string, reason
     */
    static final String MALFORMED_SOURCE_URL =
            "Could not create a schema URL for %s with source %s: %s";
    /**
     * Error message used when the schema referenced by the DcsMetadataScheme source URL cannot be loaded or parsed.
     * Parameters are: url used to load the schema, reason
     */
    static final String ERROR_LOADING_SCHEMA =
            "Could not load or parse schema at %s: %s";
    /**
     * Error message used when XML instance document cannot be loaded or parsed. Parameters are: reason
     */
    static final String ERROR_LOADING_INSTANCE_DOCUMENT =
            "Could not load or parse XML instance document: %s";
    /**
     * Debug message used when attempting to validate an XML instance document.  Parameters are: format id, schema
     * source url
     */
    static final String DEBUG_VALIDATION_ATTEMPT =
            "Attempting to validate an instance document with format id %s against the schema %s";
    /**
     * Debug message used when a copy of the XML instance document is made.  Parameters are: file path
     */
    static final String DEBUG_VALIDATION_FILE =
            "A copy of the XML instance document being validated is stored at %s";
    /**
     * Error message used when a copy of the instance document cannot be saved.  Parameters are: reason
     */
    static final String UNABLE_TO_COPY_INSTANCE_DOCUMENT =
            "Unable to validate the schema instance document, because a temporary file containing a copy of the " +
                    "instance document could not be saved: %s";
    /**
     * Error message used when the XML instance document fails validation.  Parameters are: format id, well-known
     * schema url, schema source url, reason
     */
    static final String INVALID_METADATA_INSTANCE =
            "Validation failed: validation of an XML instance document with format id %s failed against the schema " +
                    "%s obtained from %s: %s";
    /**
     * Success message used when the XML instance document validates.  Parameters are: format id, well-known
     * schema url, schema source url
     */
    static final String SUCCESS_VALID_METADATA_INSTANCE =
            "Validation succeeded: validation of an XML instance document with format id %s succeeded against the " +
                    "schema %s obtained from %s";
    /**
     * Error message used when the validation process fails for some other reason other than an invalid instance
     * document.  Parameters are: format id, well-known schema url, schema source url, reason
     */
    static final String ERROR_PERFORMING_VALIDATION =
            "Error performing validation of an XML instance document with format id %s against the schema %s " +
                    "obtained from %s: %s";
}