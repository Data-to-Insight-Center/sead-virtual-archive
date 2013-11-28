package org.dataconservancy.mhf.validators.registry.impl;

import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.mhf.validators.util.MetadataFormatLoader;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Convenience implementation of a Registry containing DcsMetadataScheme objects.  Entries in this registry can be
 * looked up by well-known URLs to a schema document.
 * <p/>
 * For example, each DcsMetadataScheme RegistryEntry will have two keys:
 * <ol>
 *   <li>the full scheme {@link org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme#getSchemaUrl()
 *       url} (e.g. http://www.w3.org/2001/XMLSchema.xsd or
 *       http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998-sect05.xsd)</li>
 *   <li>The portion of the scheme url following the last trailing slash (e.g XMLSchema.xsd or
 *       fgdc-std-001-1998-sect05.xsd)</li>
 * </ol>
 */
public class SchemaRegistryImpl implements TypedRegistry<DcsMetadataScheme> {

    /**
     * Loads DcsMetadataFormat entries from the filesystem.
     */
    private Map<String, RegistryEntry<DcsMetadataFormat>> formatEntries;

    /**
     * The description of this registry instance.
     */
    private String description;

    /**
     * Creates a Registry that is populated by entries provided on the classpath in
     * {@code /org/dataconservancy/mhf/resources/registry/metadataformat/}.  The entries are serialized registry entries
     * of DcsMetadataFormat objects, in DCP format.  The entries are provided by the {@code dcs-mhf-resources} bundle,
     * so you can look there for reference.
     * <p/>
     * This implementation extracts the DcsMetadataScheme objects from the DcsMetadataFormats.
     *
     * @param id           the registry id, currently ignored
     * @param description  a string, human-readable, description of the registry
     * @param formatLoader used to recursively locate serialized DcsMetadataFormat objects on the filesystem.
     */
    public SchemaRegistryImpl(String id, String description, MetadataFormatLoader formatLoader) {
        File baseDir = new File(this.getClass().getResource(MHFResources.METADATA_FORMAT_REGISTRY_RESOURCE_PATH).getPath());
        formatEntries = new HashMap<String, RegistryEntry<DcsMetadataFormat>>();
        formatLoader.loadRegistryEntries(baseDir, formatEntries);
        this.description = description;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Provides a registry of Metadata Formats: {@code dataconservancy.types:registry-entry:metadatascheme}
     *
     * @return
     */
    @Override
    public String getType() {
        return MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE;
    }

    @Override
    public RegistryEntry<DcsMetadataScheme> retrieve(String id) {
        for (RegistryEntry<DcsMetadataFormat> formatEntry : formatEntries.values()) {
            final Collection<DcsMetadataScheme> schemes = formatEntry.getEntry().getSchemes();

            for (DcsMetadataScheme scheme : schemes) {
                RegistryEntry<DcsMetadataScheme> schemeRegistryEntry = asRegistryEntry(scheme);
                if (schemeRegistryEntry.getId().equals(id)) {
                    return schemeRegistryEntry;
                }
            }
        }

        return null;
    }

    @Override
    public Set<RegistryEntry<DcsMetadataScheme>> lookup(String... keys) {
        final Set<RegistryEntry<DcsMetadataScheme>> results = new HashSet<RegistryEntry<DcsMetadataScheme>>();

        for (RegistryEntry<DcsMetadataFormat> formatEntry : formatEntries.values()) {
            final Collection<DcsMetadataScheme> schemes = formatEntry.getEntry().getSchemes();

            for (DcsMetadataScheme scheme : schemes) {
                RegistryEntry<DcsMetadataScheme> schemeRegistryEntry = asRegistryEntry(scheme);
                if (schemeRegistryEntry.getKeys().containsAll(Arrays.asList(keys))) {
                    results.add(schemeRegistryEntry);
                }
            }
        }

        return results;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Iterator iterator() {
        Collection<DcsMetadataScheme> allSchemes = new HashSet<DcsMetadataScheme>();
        for (RegistryEntry<DcsMetadataFormat> formatEntry : formatEntries.values()) {
            allSchemes.addAll(formatEntry.getEntry().getSchemes());
        }

        return allSchemes.iterator();
    }

    /**
     * Converts a DcsMetadataScheme to a RegistryEntry.
     * <dl>
     *     <dt>Registry Entry ID</dt>
     *     <dd>Mapped from the DcsMetadataScheme source</dd>
     *     <dt>Registry Entry Keys</dt>
     *     <dd>Two keys mapped from the DcsMetadataScheme schema URL: the full schema URL, and the portion of the url
     *         after the last slash.</dd>
     *     <dt>Registry Entry Type</dt>
     *     <dd>dataconservancy.types:registry-entry:metadatascheme</dd>
     *     <dt>Registry Entry Description</dt>
     *     <dd>"Metadata Scheme for &lt;<em>schemeUrl</em>&gt;"</dd>
     * </dl>
     * Note: package-private for unit testing.
     *
     * @param scheme the DcsMetadataScheme
     * @return the DcsMetadataScheme converted to a RegistryEntry
     */
    RegistryEntry<DcsMetadataScheme> asRegistryEntry(DcsMetadataScheme scheme) {
        Set<String> keys = new HashSet<String>();
        final String schemaUrl = scheme.getSchemaUrl();
        keys.add(schemaUrl);
        keys.add(schemaUrl.substring(schemaUrl.lastIndexOf("/") + 1));
        return new BasicRegistryEntryImpl<DcsMetadataScheme>(scheme.getSource(), scheme,
                MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE, keys, "Metadata Scheme for " + schemaUrl);
    }
}
