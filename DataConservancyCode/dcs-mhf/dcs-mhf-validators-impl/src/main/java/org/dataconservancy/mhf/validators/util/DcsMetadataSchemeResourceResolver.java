package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;

import java.util.Set;

/**
 * Resolves {@code DcsMetadataScheme} objects from the Registry.  This implementation allows {@code resource} strings to
 * be in two different forms:
 * <ol>
 *     <li>The entire <em>schema url</em> (e.g. http://www.w3.org/2001/XMLSchema.xsd)</li>
 *     <li>The portion of the <em>schema url</em> after the last slash (e.g. XMLSchema.xsd)</li>
 * </ol>
 * See {@link #resolveResourceInternal(String)} for more details.
 */
public class DcsMetadataSchemeResourceResolver extends BaseResourceResolver<DcsMetadataScheme> {

    /**
     * The {@code TypedRegistry} instance containing {@code DcsMetadataScheme} objects
     */
    private TypedRegistry<DcsMetadataScheme> schemeRegistry;

    /**
     * Constructs a new instance backed by the supplied {@code registry}
     *
     * @param registry the registry used to resolve {@code DcsMetadataScheme} objects, must not be {@code null}
     * @throws IllegalArgumentException if {@code registry} is {@code null}
     */
    public DcsMetadataSchemeResourceResolver(TypedRegistry<DcsMetadataScheme> registry) {
        if (registry == null) {
            throw new IllegalArgumentException("DcsMetadataScheme registry must not be null.");
        }
        this.schemeRegistry = registry;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * Resource string may take two forms.  The first form is the full schema url
     * E.g.: "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd".  The second form is the portion of the url
     * after the last trailing slash E.g.: "fgdc-std-001-1998.xsd".
     *
     * @param resourceString the resource string, generally a DcsMetadataScheme schema URL
     * @param baseUrl the base url of the resource to be resolved, currently ignored
     * @return {@inheritDoc}
     */
    @Override
    protected RegistryEntry<DcsMetadataScheme> resolveResourceInternal(String resourceString, String baseUrl) {
        Set<RegistryEntry<DcsMetadataScheme>> scheme = schemeRegistry.lookup(resourceString);

        if (scheme.isEmpty()) {
            log.log("DcsMetadataScheme registry didn't contain any schemes for resource string '" +
                    resourceString + "'");
            return null;
        }

        if (scheme.size() > 1) {
            log.log("DcsMetadataScheme registry contained more than one DcsMetadataScheme for resource string '" +
                    resourceString + "'");
        }

        return scheme.iterator().next();
    }

}
