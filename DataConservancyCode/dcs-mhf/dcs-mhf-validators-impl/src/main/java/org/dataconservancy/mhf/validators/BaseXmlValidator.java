package org.dataconservancy.mhf.validators;

import org.dataconservancy.mhf.eventing.events.MetadataValidationEvent;
import org.dataconservancy.mhf.eventing.events.MetadataXmlValidationEvent;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.validation.api.MetadataValidator;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.mhf.validators.dom.impl.LSResourceResolverImpl;
import org.dataconservancy.mhf.validators.util.ResourceResolver;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.ResourceResolverUtil;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.springframework.core.io.Resource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.net.URL;

/**
 * A validator capable of validating XML instance documents against a schema.  The schema used to validate the XML
 * instance document is looked up by the format identifier of the {@code MetadataInstance}.  Specifically,
 * implementations are not expected to perform schema detection.  This means that an XML document may refer to one
 * schema (e.g., by a {@code xsd:schemaUrl}), but be validated by a different schema (the one that was resolved by the
 * format identifier of the {@code MetadataInstance}).
 * <p/>
 * Implementations are provided with three collaborating objects to support validation:
 * <ol>
 *     <li>{@code SchemaFactory}: used to produce {@code Schema} objects, representing the grammar used for
 *         validation</li>
 *     <li>{@code ResourceResolver}: used to resolve external references present in XML documents.  This includes
 *         entity or DTD references in the instance document, or XSD documents included or imported by
 *         another XSD schema</li>
 *     <li>{@code LSResourceResolver}: Java Validation API version of a {@code ResourceResolver}.  It is meant to be
 *         supplied to {@code Validator} instances.</li>
 * </ol>
 * A typical implementation will use the {@code MetadataInstance} format identifier to resolve a reference to an XSD
 * schema; this is the schema that will be used to validate the {@code MetadataInstance} content.  The
 * {@code SchemaFactory} is invoked, producing a {@code Schema} object for the XSD schema.  A {@code Validator} is
 * produced from the {@code Schema}, and configured with the {@code LSResourceResolver}.  Finally, validation is
 * performed by invoking the validation method on the {@code Validator}, providing the content of the
 * {@code MetadataInstance} as an argument.
 */
public abstract class BaseXmlValidator extends BaseValidatorImpl implements MetadataValidator {

    /**
     * Resolves external references present in XML documents.  It can also be used to resolve the schema used to
     * validate instance documents.  That is, it can resolve format identifiers as well as system identifiers of
     * referenced XML entities.
     */
    protected final ResourceResolver resourceResolver;

    /**
     * Produces {@code Schema} objects encapsulating the constraints used when validating instance documents
     */
    protected final SchemaFactory schemaFactory;

    /**
     * Resolves external references present in XML documents.  The {@code LSResourceResolver} is compatible
     * with the Java Validation API.  Typically the {@code LSResourceResolver} implementation will wrap the
     * {@link #resourceResolver ResourceResolver}.
     */
    protected final LSResourceResolver lsResourceResolver;

    /**
     * The exception handler which encapsulates event firing and exception throwing logic.  This handler should
     * be used in lieu of constructing and throwing ValidationExceptions in this class.
     */
    protected final EventProducingExceptionHandler exceptionHandler;

    /**
     * The MetadataHandlingEventManager used to fire events.
     */
    protected final MetadataHandlingEventManager eventManager;

    /**
     * Verbosity flag: when true causes the log level to be elevated to INFO from DEBUG, and XML parsing events are
     * logged.
     */
    boolean verbose = false;

    /**
     * Constructs a new {@code BaseXmlValidator}, configured to validate {@code MetadataInstance} objects.  The
     * {@link #lsResourceResolver} is provided by wrapping the supplied {@code resourceResolver}.
     *
     * @param schemaFactory a configured {@code SchemaFactory}, ready to be used to produce {@code Schema} objects
     * @param formatResourceResolver a configured {@code ResourceResolver} ready to resolve DcsMetadataFormat objects
     *                               by format IDs enumerated in {@link MetadataFormatId}
     * @param schemeResourceResolver a configured {@code ResourceResolver} ready to resolve DcsMetadataScheme objects
     *                               by well known XML schema URLs (e.g. http://www.w3.org/2001/XMLSchema.xsd)
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    protected BaseXmlValidator(SchemaFactory schemaFactory, ResourceResolver<DcsMetadataFormat> formatResourceResolver,
                               ResourceResolver<DcsMetadataScheme> schemeResourceResolver,
                               MetadataHandlingEventManager eventManager) {
        if (schemaFactory == null) {
            throw new IllegalArgumentException("Schema Factory must not be null.");
        }

        if (formatResourceResolver == null) {
            throw new IllegalArgumentException("Resource Resolver must not be null.");
        }

        if (eventManager == null) {
            throw new IllegalArgumentException("MetadataHandlingEventManager must not be null.");
        }

        this.schemaFactory = schemaFactory;
        this.resourceResolver = formatResourceResolver;
        this.lsResourceResolver = new LSResourceResolverImpl(schemeResourceResolver);
        this.eventManager = eventManager;
        this.exceptionHandler = new EventProducingExceptionHandler(eventManager);
    }

    @Override
    public void validate(MetadataInstance metadataInstance, URL baseUrl) throws ValidationException {
        if (metadataInstance == null) {
            IllegalArgumentException iae = new IllegalArgumentException("MetadataInstance must not be null!");
            exceptionHandler.throwValidationException(null, null, null, iae.getMessage(), iae);
        }

        assertNonNullFormatIdAndInputStream(metadataInstance);
        final String formatId = metadataInstance.getFormatId();
        final RegistryEntry<DcsMetadataFormat> registryEntry = resourceResolver.resolve(formatId, "");
        final DcsMetadataScheme metadataScheme = getMetadataScheme(registryEntry, formatId);
        final URL schemaUrl = getSchemaUrl(formatId, metadataScheme);
        final Schema schema = getSchema(formatId, metadataScheme, schemaUrl);

        try {
            validateInternal(metadataInstance, registryEntry, schemaUrl, schema, baseUrl);
        } catch (SAXException e) {
            exceptionHandler.throwInvalidInstanceException(formatId, metadataScheme.getSchemaUrl(),
                    metadataScheme.getSource(), e.getMessage(), e);

        } catch (Exception e) {
            exceptionHandler.throwValidationException(formatId, metadataScheme.getSchemaUrl(),
                    metadataScheme.getSource(), e.getMessage(), e);
        }

        final String message = String.format(Messages.SUCCESS_VALID_METADATA_INSTANCE, formatId,
                metadataScheme.getSchemaUrl(), metadataScheme.getSource());
        MetadataXmlValidationEvent mve = new MetadataXmlValidationEvent(null,
                message, null, MetadataValidationEvent.ValidationType.PASS);
        mve.setSchemaSource(metadataScheme.getSource());
        mve.setSchemaUrl(metadataScheme.getSchemaUrl());
        fireValidationSucceededEvent(mve);
        if (log.isVerbose()) {
            log.log(message);
        }
    }

    protected abstract void validateInternal(MetadataInstance mi, RegistryEntry<DcsMetadataFormat> entry,
                                             URL schemaUrl, Schema schema, URL baseUrl) throws Exception;


    /**
     * Insures the metadata instance has a non-null format id and input stream. Throws a validation exception if either
     * of those properties are null.
     *
     * @param metadataInstance
     * @throws ValidationException
     */
    protected void assertNonNullFormatIdAndInputStream(MetadataInstance metadataInstance) throws ValidationException {
        String formatId = null;
        InputStream stream = null;

        try {
            formatId = metadataInstance.getFormatId();
            stream = metadataInstance.getContent();
        } catch (Exception e) {
            exceptionHandler.throwValidationException(null, null, null, e.getMessage(), e);
        }

        String reason = null;
        if (formatId == null) {
            reason = "MetadataInstance format identifier must not be null.";
        }

        if (stream == null) {
            reason = "MetadataInstance content stream must not be null.";
        }

        if (reason != null) {
            exceptionHandler.throwValidationException(formatId, null, null, reason, null);
        }
    }

    /**
     * Obtains the DcsMetadataScheme from the supplied RegistryEntry, and insures that the metadata scheme object
     * has a non-null source field.
     *
     * @param registryEntry
     * @param formatId
     * @return
     * @throws ValidationException
     */
    protected DcsMetadataScheme getMetadataScheme(RegistryEntry<DcsMetadataFormat> registryEntry, String formatId)
            throws ValidationException {

        String reason = null;

        if (registryEntry == null) {
            reason = String.format(Messages.CANNOT_RESOLVE_ENTRY_FOR_FORMAT, formatId);
            exceptionHandler.throwValidationException(formatId, null, null, reason, null);
        }

        final DcsMetadataFormat format = registryEntry.getEntry();

        DcsMetadataScheme scheme = null;
        
        //The master scheme will always be the first scheme in the format so this is safe
        if (format.getSchemes().iterator().hasNext() )
        {
            scheme = format.getSchemes().iterator().next();
        }
        
        if (scheme == null) {
            reason = String.format(Messages.MISSING_REGISTRY_ENTRY, formatId);
        }

        if (scheme != null &&
                (scheme.getSource() == null || scheme.getSource().trim().length() == 0)) {
            reason = String.format(Messages.MISSING_SCHEME_SOURCE, formatId);
        }

        if (reason != null) {
            exceptionHandler.throwValidationException(formatId, (scheme != null ? scheme.getSchemaUrl() : null),
                    (scheme != null ? scheme.getSource() : null), reason, null);
        }

        return scheme;
    }

    Schema getSchema(String formatId, DcsMetadataScheme scheme, URL schemaUrl) throws ValidationException {
        try {
            return schemaFactory.newSchema(schemaUrl);
        } catch (SAXException e) {
            final String reason = String.format(Messages.ERROR_LOADING_SCHEMA, schemaUrl.toExternalForm(),
                    e.getMessage());
            exceptionHandler.throwValidationException(formatId, scheme.getSchemaUrl(), scheme.getSource(), reason, e);
        }
        return null;
    }

    URL getSchemaUrl(String formatId, DcsMetadataScheme scheme) throws ValidationException {
        try {
            String schemeSource = scheme.getSource();
            Resource r = ResourceResolverUtil.resolveFileSource(schemeSource);
            return r.getURL();
        } catch (Exception e) {
            final String reason = String.format(Messages.MALFORMED_SOURCE_URL, formatId, scheme.getSource(),
                    e.getMessage());
            exceptionHandler.throwValidationException(formatId, scheme.getSchemaUrl(), scheme.getSource(), reason, e);
        }
        return null;
    }

    void fireValidationSucceededEvent(MetadataValidationEvent event) {
        eventManager.sendEvent(event);
    }

}
