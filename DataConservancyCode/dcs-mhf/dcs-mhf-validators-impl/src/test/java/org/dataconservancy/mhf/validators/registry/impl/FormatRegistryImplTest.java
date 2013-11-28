package org.dataconservancy.mhf.validators.registry.impl;

import org.dataconservancy.mhf.validators.util.MetadataFormatLoader;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;

/**
 * Implementation of AbstractFormatRegistryImplTest which returns a manually-wired instance of FormatRegistryImpl
 */
public class FormatRegistryImplTest extends AbstractFormatRegistryImplTest {

    @Override
    protected FormatRegistryImpl getUnderTest() {
        // MetadataFormatLoader deserializes RegistryEntries from the file system.
        final MetadataFormatLoader formatLoader = new MetadataFormatLoader(new MetadataFormatMapper(new MetadataSchemeMapper()),
                new DcsXstreamStaxModelBuilder());

        return new FormatRegistryImpl(REGISTRY_ID, REGISTRY_DESC, formatLoader);
    }

}
