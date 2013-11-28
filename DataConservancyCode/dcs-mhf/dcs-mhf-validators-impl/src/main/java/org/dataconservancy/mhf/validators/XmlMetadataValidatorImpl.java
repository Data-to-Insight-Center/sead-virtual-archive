package org.dataconservancy.mhf.validators;

import org.dataconservancy.mhf.eventing.events.MetadataHandlingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataParsingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataXmlParsingEvent;
import org.dataconservancy.mhf.eventing.events.MetadataXmlValidationEvent;
import org.dataconservancy.mhf.eventing.manager.MetadataHandlingEventManager;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.validation.api.ValidationException;
import org.dataconservancy.mhf.validators.util.ResourceResolver;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;

/**
 * Validates XML instance documents based on format identifier.  It does not perform schema detection.
 *
 * @see BaseXmlValidator
 */
public class XmlMetadataValidatorImpl extends BaseXmlValidator {

    public XmlMetadataValidatorImpl(SchemaFactory schemaFactory,
                                    ResourceResolver<DcsMetadataFormat> formatResourceResolver,
                                    ResourceResolver<DcsMetadataScheme> schemeResourceResolver,
                                    MetadataHandlingEventManager eventManager) {
        super(schemaFactory, formatResourceResolver, schemeResourceResolver, eventManager);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * Validates a {@code MetadataInstance} by:
     * <ol>
     *     <li>Resolving the schema URL used to validate the instance, using the {@code MetadataInstance} format id</li>
     *     <li>Producing a {@code Schema} by parsing the resolved schema URL</li>
     *     <li>Producing a {@code Validator} from the {@code Schema}</li>
     *     <li>Configuring the {@code Validator} with the {@code LSResourceResolver}</li>
     *     <li>Configuring the {@code Validator} with an internally-managed SAX {@code ErrorHandler}</li>
     *     <li>Invoking validation</li>
     * </ol>
     * If the {@code MetadataInstance} is an XSD instance document, one further step is taken (assuming that the
     * previous validation steps succeed): the {@code SchemaFactory} is used to instantiate a {@code Schema} from
     * the XSD instance document.  This insures that all &lt;xsd:include> references can be resolved in order for
     * validation to succeed.
     *
     * @param metadataInstance {@inheritDoc}
     * @throws ValidationException if {@code metadataInstance} is {@code null}, or its format id or content stream
     *                             is {@code null}.  If the schema cannot be resolved.  If the {@code Schema} cannot
     *                             be produced.  If the validation process fails, or if the content is invalid.
     */
    @Override
    public void validateInternal(MetadataInstance metadataInstance, RegistryEntry<DcsMetadataFormat> entry,
                                 URL schemaUrl, Schema schema, URL baseUrl) throws Exception {
        final String formatId = metadataInstance.getFormatId();
        Validator validator = schema.newValidator();
        validator.setResourceResolver(lsResourceResolver);
        final CapturingSaxErrorHandler captureHandler = new CapturingSaxErrorHandler();
        validator.setErrorHandler(new DebuggingErrorHandler(
                new EventProducingErrorHandler(captureHandler, entry, eventManager)));
        log.log(String.format(Messages.DEBUG_VALIDATION_ATTEMPT, formatId, schemaUrl.toExternalForm()));
        validator.validate(new SAXSource(new InputSource(metadataInstance.getContent())));
        if (!captureHandler.getErrors().isEmpty()) {
            // throw the first exception we see at error or fatal; we don't throw for warnings.
            for (CapturingSaxErrorHandler.SaxError err : captureHandler.getErrors()) {
                if (err.severity == CapturingSaxErrorHandler.SEVERITY.ERROR ||
                        err.severity == CapturingSaxErrorHandler.SEVERITY.FATAL) {
                    throw err.exception;
                }
            }
        }
    }

}
