package org.dataconservancy.mhf.validators;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Wrapper for SAX EntityResolvers, allowing calls to the SAX EntityResolver interface to be decorated.
 *
 * @see DebuggingEntityResolver
 */
abstract class EntityResolverWrapper extends AbstractSaxWrapper implements EntityResolver {

    private final EntityResolver wrapped;

    /**
     * Wraps an {@code EntityResolver} implementation.
     *
     * @param toWrap the resolver to be wrapped
     */
    EntityResolverWrapper(EntityResolver toWrap) {
        if (toWrap == null) {
            throw new IllegalArgumentException("The EntityResolver to wrap must not be null.");
        }
        this.wrapped = toWrap;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note:<br/>
     * First calls {@link #resolveEntityInternal(String, String)}, then calls {@code EntityResolver#resolveEntity} on
     * the wrapped instance.
     *
     * @param publicId the publicId of the entity to be resolved
     * @param systemId the systemId of the entity to be resolved
     * @return {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public final InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        try {
            resolveEntityInternal(publicId, systemId);
        } catch (Exception e) {
            // ignore
        }

        return wrapped.resolveEntity(publicId, systemId);
    }

    /**
     * Method invoked by this wrapper prior to calling the wrapped {@code EntityResolver} instance.  Subclasses should
     * override this method to decorate calls to {@code EntityResolver#resolveEntity}.  Subclasses can't influence the
     * return (this method is {@code void}), but they can do other things, like recording what entities are being
     * resolved.
     * <p/>
     * This method is a no-op unless overridden by a subclass.
     *
     * @param publicId the publicId of the entity being resolved
     * @param systemId the systemId of the entity being resolved.
     */
    void resolveEntityInternal(String publicId, String systemId) {
        // purposfully empty.
    }
}
