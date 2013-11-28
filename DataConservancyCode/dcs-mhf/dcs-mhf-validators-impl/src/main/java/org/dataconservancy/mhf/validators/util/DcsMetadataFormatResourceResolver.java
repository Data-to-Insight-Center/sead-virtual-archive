package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;

import java.util.Set;

/**
 * Resolves {@code DcsMetadataFormat} objects from the Registry.  This implementation allows {@code resource} strings to
 * be in two different forms:
 * <ol>
 *     <li><em>schema url</em><code>,</code><em>schema version</em></li>
 *     <li>a <em>format id</em> (from <code>MetadataFormatId)</code></li>
 * </ol>
 * See {@link #resolveResourceInternal(String)} for more details.
 */
public class DcsMetadataFormatResourceResolver extends BaseResourceResolver<DcsMetadataFormat> {

    /**
     * The {@code TypedRegistry} instance containing {@code DcsMetadataFormat} objects
     */
    private final TypedRegistry<DcsMetadataFormat> registry;

    /**
     * Constructs a new instance backed by the supplied {@code registry}
     *
     * @param registry the registry used to resolve {@code DcsMetadataFormat} objects, must not be {@code null}
     * @throws IllegalArgumentException if {@code registry} is {@code null}
     */
    public DcsMetadataFormatResourceResolver(TypedRegistry<DcsMetadataFormat> registry) {
        if (registry == null) {
            throw new IllegalArgumentException("Registry instance must not be null.");
        }
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * Resource string may take two forms.  The first form is schema url, schema version
     * E.g.: "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd,1.0".  The second form
     * is a format identifier from {@code MetadataFormatId}.
     * <br/>
     * Note that in the first form, the schema version is optional.  But that may lead to multiple resources
     * being resolved from the registry. If that is the case, we'll log a warning
     * and pick the first one, which will be non-deterministic behavior, since the returns from
     * the registry are in a {@code Set}.
     *
     * @param resourceString the resource string in the form "<em>schema url</em>,<em>schema version</em>", or a
     *                 format identifier from {@code MetadataFormatId}
     * @param baseUrl the base url of the resource to be resolved currently ignored
     * @return {@inheritDoc}
     */
    @Override
    protected RegistryEntry<DcsMetadataFormat> resolveResourceInternal(String resourceString, String baseUrl) {

        Set<RegistryEntry<DcsMetadataFormat>> results = null;

        if (isFormatId(resourceString)) {
            if (log.isVerbose()) {
                log.log("Parsed the resource '{}' as a format identifier.");
            }
            results = registry.lookup(resourceString);
        } else {
            String[] resourceParts = parseResource(resourceString);
            // 0: contains the schemaUrl, 1: contains the schema version (optional)
            if (log.isVerbose()) {
                log.log("Parsed the resource '{}' as a schemaUrl: '{}' and schemaVersion: '{}'",
                        new Object[] {resourceString, resourceParts[0], (resourceParts.length == 2) ? resourceParts[1] : null});
            }
            if (resourceParts != null && resourceParts.length == 2) {
                results = registry.lookup(resourceParts[0], resourceParts[1]);
            } else {
                results = registry.lookup(resourceParts[0]);
            }
        }

        if (results.size() == 0) {
            return null;
        }

        if (results.size() > 1) {
            log.log("Retrieved more than one registry entry for resource " +
                    "schemaUrl='{}' Using the first result.", resourceString);
        }

        RegistryEntry<DcsMetadataFormat> metadataFormat = results.iterator().next();

        return metadataFormat;
    }

    private boolean isFormatId(String resource) {
        return MetadataFormatId.FORMAT_IDS.contains(resource);
    }

    private String[] parseResource(String resource) {

        // Expected form of the resource string: schema url, schema version
        // E.g.: "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd,1.0"
        // The schema version is optional.  But that may lead to multiple resources
        // being resolved from the registry. If that is the case, we'll log a warning
        // and pick the first one.

        final String[] resourceParts = resource.split(",");
        String schemaUrl = resourceParts[0].trim();
        String schemaVersion = null;

        if (resourceParts.length > 1) {
            String trimmedVal = resourceParts[1].trim();
            if (trimmedVal.length() > 0) {
                schemaVersion = trimmedVal;
            }
        }

        if (schemaVersion != null) {
            return new String[]{schemaUrl, schemaVersion};
        }

        return new String[]{schemaUrl};
    }


}
