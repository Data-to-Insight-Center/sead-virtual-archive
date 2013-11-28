package org.dataconservancy.mhf.validators.registry.impl;

import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.mhf.validators.util.MetadataFormatLoader;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryMapper;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant;
import org.dataconservancy.registry.shared.memory.InMemoryRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Convenience implementation of a Registry containing DcsMetadataFormat objects.  Entries in this registry can be
 * looked up by format identifiers enumerated in {@link org.dataconservancy.mhf.instance.api.MetadataFormatId}.
 */
public class FormatRegistryImpl implements TypedRegistry<DcsMetadataFormat> {

    private InMemoryRegistry delegate;

    /**
     * Creates a Registry that is populated by entries provided on the classpath in
     * {@code /org/dataconservancy/mhf/resources/registry/metadataformat/}.  The entries are serialized registry entries
     * of DcsMetadataFormat objects, in DCP format.  The entries are provided by the {@code dcs-mhf-resources} bundle,
     * so you can look there for reference.
     *
     * @param id the registry id, currently ignored
     * @param description a string, human-readable, description of the registry
     * @param formatLoader used to recursively locate serialized DcsMetadataFormat objects on the filesystem.
     */
    public FormatRegistryImpl(String id, String description, MetadataFormatLoader formatLoader) {
        File baseDir = new File(this.getClass().getResource(MHFResources.METADATA_FORMAT_REGISTRY_RESOURCE_PATH).getPath());
        Map<String, RegistryEntry<DcsMetadataFormat>> entries = new HashMap<String, RegistryEntry<DcsMetadataFormat>>();
        formatLoader.loadRegistryEntries(baseDir, entries);

        this.delegate = new InMemoryRegistry(id, BasicRegistryEntryMapper.REGISTRY_ENTRY_DU_TYPE, entries, description);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Provides a registry of Metadata Formats: {@code dataconservancy.types:registry-entry:metadataformat}
     *
     * @return
     */
    @Override
    public String getType() {
        return MetadataRegistryConstant.METADATAFORMAT_REGISTRY_ENTRY_TYPE;
    }

    @Override
    public RegistryEntry<DcsMetadataFormat> retrieve(String id) {
        return delegate.retrieve(id);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Registry entries may be looked up using metadata format identifiers, enumerated in the
     * {@link org.dataconservancy.mhf.instance.api.MetadataFormatId} class.
     *
     * @param keys {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Set<RegistryEntry<DcsMetadataFormat>> lookup(String... keys) {
        Set<RegistryEntry<DcsMetadataFormat>> entries = new HashSet<RegistryEntry<DcsMetadataFormat>>();
        for (RegistryEntry<DcsMetadataFormat> entry : delegate.lookup(keys)) {
            entries.add(entry);
        }
        return entries;
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public Iterator iterator() {
        return delegate.iterator();
    }
}
