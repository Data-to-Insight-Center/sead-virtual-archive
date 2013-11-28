package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Simple test asserting that the loader finds DcsMetadataFormat registry entries.
 */
public class MetadataFormatLoaderTest {

    /**
     * Attempts to load RegistryEntry objects from {@link MHFResources#METADATA_FORMAT_REGISTRY_RESOURCE_PATH}
     *
     * @throws Exception
     */
    @Test
    public void testLoadRegistryEntries() throws Exception {
        MetadataFormatLoader underTest = new MetadataFormatLoader(
                new MetadataFormatMapper(new MetadataSchemeMapper()), new DcsXstreamStaxModelBuilder());

        File baseDir = new File(this.getClass().getResource(MHFResources.METADATA_FORMAT_REGISTRY_RESOURCE_PATH).getPath());
        Map<String, RegistryEntry<DcsMetadataFormat>> entryMap = new HashMap<String, RegistryEntry<DcsMetadataFormat>>();


        underTest.loadRegistryEntries(baseDir, entryMap);

        assertTrue(entryMap.size() > 0);
    }

}
