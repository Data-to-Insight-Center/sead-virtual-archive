package org.dataconservancy.mhf.validators.dom.impl;

import org.dataconservancy.mhf.validators.util.ResourceResolver;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.ResourceResolverUtil;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.springframework.core.io.Resource;
import org.w3c.dom.ls.LSInput;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Implementation of the SAX LSResourceResolver interface which resolves external entities by retrieving them from
 * a {@code ResourceResolver&lt;DcsMetadataScheme>}
 */
public class LSResourceResolverImpl extends BaseLSResourceResolverImpl {

    private final ResourceResolver<DcsMetadataScheme> schemeResolver;

    public LSResourceResolverImpl(ResourceResolver<DcsMetadataScheme> schemeResolver) {
        if (schemeResolver == null) {
            throw new IllegalArgumentException("Scheme Resolver must not be null.");
        }

        this.schemeResolver = schemeResolver;
    }

    /**
     * Resolves the entity using the provided {@code systemId}.  The other parameters to this method are currently
     * ignored (except to be set, where appropriate, on the returned {@code LSInput}).
     * <p/>
     * The {@code systemId} is used to resolve the DcsMetadataScheme object from a registry, and then the
     * {@link DcsMetadataScheme#getSource() source} of the of the DcsMetadataScheme is resolved to an
     * {@code InputStream} and set on the returned {@code LSInput}.
     *
     * @param type
     * @param nsUri
     * @param publicId
     * @param systemId
     * @param baseUri
     * @return the resolved {@code LSInput}, or {@code null} if the DcsMetadataScheme cannot be resolved, or the source
     *         of the scheme cannot be resolved.
     */
    @Override
    public LSInput resolveResourceInternal(final String type, String nsUri, final String publicId,
                                           final String systemId, final String baseUri) {

        RegistryEntry<DcsMetadataScheme> schemeEntry = schemeResolver.resolve(systemId, baseUri);

        if (schemeEntry == null) {
            log.log("Unable to resolve {}, DcsMetadataScheme RegistryEntry was null.", systemId);
            return null;
        }

        if (schemeEntry.getEntry() == null) {
            log.log("Unable to resolve {}, the RegistryEntry was resolved, but the DcsMetadataScheme was null.",
                    systemId);
            return null;
        }

        DcsMetadataScheme scheme = schemeEntry.getEntry();
        final InputStream resourceStream;

        try {
            Resource streamResource = ResourceResolverUtil.resolveFileSource(scheme.getSource());
            resourceStream = streamResource.getInputStream();
        } catch (IOException e) {
            log.error(String.format(ERROR_OPENING_STREAM, systemId, scheme.getSource(),
                    e.getMessage()), e);
            return null;
        }

        return new LSInput() {
            @Override
            public Reader getCharacterStream() {
                // Default method body
                return null;
            }

            @Override
            public void setCharacterStream(Reader reader) {
                // Default method body

            }

            @Override
            public InputStream getByteStream() {
                return resourceStream;
            }

            @Override
            public void setByteStream(InputStream inputStream) {
                // Default method body

            }

            @Override
            public String getStringData() {
                // Default method body
                return null;
            }

            @Override
            public void setStringData(String s) {
                // Default method body

            }

            @Override
            public String getSystemId() {
                return systemId;
            }

            @Override
            public void setSystemId(String s) {
                // Default method body

            }

            @Override
            public String getPublicId() {
                return publicId;
            }

            @Override
            public void setPublicId(String s) {
                // Default method body

            }

            @Override
            public String getBaseURI() {
                return baseUri;
            }

            @Override
            public void setBaseURI(String s) {
                // Default method body

            }

            @Override
            public String getEncoding() {
                // Default method body
                return null;
            }

            @Override
            public void setEncoding(String s) {
                // Default method body

            }

            @Override
            public boolean getCertifiedText() {
                // Default method body
                return false;
            }

            @Override
            public void setCertifiedText(boolean b) {
                // Default method body

            }
        };

    }

}
