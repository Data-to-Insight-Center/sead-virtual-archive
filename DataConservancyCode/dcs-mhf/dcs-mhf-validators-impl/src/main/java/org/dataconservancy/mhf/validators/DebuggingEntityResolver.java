package org.dataconservancy.mhf.validators;

import org.xml.sax.EntityResolver;

/**
 * Logs calls to {@code EntityResolver#resolveEntity}.
 */
class DebuggingEntityResolver extends EntityResolverWrapper {

    DebuggingEntityResolver(EntityResolver toWrap) {
        super(toWrap);
    }

    /**
     * Logs calls to the {@code EntityResolver#resolveEntity} at the log level defined in the
     * {@code dcs-mhf.properties}.
     *
     * @param publicId the publicId of the entity being resolved
     * @param systemId the systemId of the entity being resolved
     */
    @Override
    void resolveEntityInternal(String publicId, String systemId) {
        log.log("resolveEntityInternal: publicId '{}' systemId '{}'", publicId, systemId);
    }
}
