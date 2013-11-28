package org.dataconservancy.mhf.validators.dom.impl;

import org.dataconservancy.mhf.validators.ValidatorLogger;
import org.dataconservancy.mhf.validators.ValidatorLoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Base implementation of the {@code LSResourceResolver} interface, used to resolve references to external resources
 * in XML documents.
 */
public abstract class BaseLSResourceResolverImpl implements LSResourceResolver {

    /**
     * Error message logged when opening an input stream fails.  Parameters are: requested resource, resolved URL,
     * reason why opening the input stream failed
     */
    protected static final String ERROR_OPENING_STREAM = "Successfully resolved resource %s to %s, but failed " +
            "opening the stream: %s";

    protected final ValidatorLogger log = ValidatorLoggerFactory.getLogger(this.getClass());

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * Simply logs the resolution attempt and forwards to
     * {@link #resolveResourceInternal(String, String, String, String, String)}
     *
     * @param type {@inheritDoc}
     * @param nsUri {@inheritDoc}
     * @param publicId {@inheritDoc}
     * @param systemId {@inheritDoc}
     * @param baseUri {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public LSInput resolveResource(String type, String nsUri, String publicId, String systemId, String baseUri) {
        log.log("Resolving Type: '{}' Namespace URI: '{}' PublicID: '{}' SystemID: '{}' Base URI: '{}'",
                new Object[]{type, nsUri, publicId, systemId, baseUri});

        return resolveResourceInternal(type, nsUri, publicId, systemId, baseUri);
    }

    /**
     * To be implemented by all subclasses.
     *
     * @param type
     * @param nsUri
     * @param publicId
     * @param systemId
     * @param baseUri
     * @return
     */
    protected abstract LSInput resolveResourceInternal(String type, String nsUri, String publicId, String systemId,
                                                       String baseUri);


}
