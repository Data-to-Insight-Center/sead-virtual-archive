package org.dataconservancy.mhf.resources.registry;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.api.support.BasicRegistryEntryMapper;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSerializer;
import org.dataconservancy.registry.impl.metadata.shared.XstreamMetadataFormatSerializer;
import org.dataconservancy.registry.impl.metadata.shared.XstreamMetadataSchemeSerializer;
import org.dataconservancy.registry.shared.memory.InMemoryRegistry;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SchemaRegistryGeneratorTest {

//    @Before
//    public void setUp() throws Exception {
//        final File baseDirectory = new File("/Users/esm/dcs-mhf/dcs-mhf-resources/target/test-classes" +
//                REGISTRY_BASE_RESOURCE_PATH + "metadataformat/");
//        FileUtils.forceMkdir(baseDirectory);
//        assertTrue(baseDirectory.exists() && baseDirectory.isDirectory() && baseDirectory.canWrite());
//        this.baseDirectory = baseDirectory;
//        schemeMapper = new MetadataSchemeMapper();
//        formatMapper = new MetadataFormatMapper(schemeMapper);
//        schemeMapper.setBaseDirectory(baseDirectory);
//        formatMapper.setBaseDirectory(baseDirectory);
//    }
//
//    @Test
//    public void testGenerateDcsMetadataFormatEntry() throws Exception {
//        DcsMetadataFormat format = generateFgdcMetadataFormat();
//        formatSerializer.serialize(format, System.out);
//    }
//
//    @Test
//    public void testGenerateDcsMetadataSchemeEntry() throws Exception {
//        DcsMetadataScheme scheme = generateFgdcSection01();
//        schemeSerializer.serialize(scheme, System.out);
//    }
//
//    @Test
//    public void testGenerateRegistryEntryForFormat() throws Exception {
//        final Dcp formatDcp = generateFgdcSchemaRegistryEntryDcp();
//        final File out = new File(baseDirectory, "fgdc1998-entry.xml");
//
//        final FileOutputStream sink = new FileOutputStream(out);
//        modelBuilder.buildSip(formatDcp, sink);
//        sink.close();
//    }
//
//    @Test
//    public void testGenerateSchemaRegistry() throws Exception {
//        final String registryType = BasicRegistryEntryMapper.REGISTRY_ENTRY_DU_TYPE;
//        final String registryId = METADATA_REGISTRY_ID;
//        final String entryId = METADATA_REGISTRY_ID + "entry:id:1";
//        final String registryDesc = "Metadata Format Registry (aka Schema Registry)";
//        final Map<String, RegistryEntry<DcsMetadataFormat>> registryEntries =
//                new HashMap<String, RegistryEntry<DcsMetadataFormat>>();
//        final Dcp fgdcRegistryEntryDcp = generateFgdcSchemaRegistryEntryDcp();
//
//        final Set<String> entryIds = formatMapper.discover(fgdcRegistryEntryDcp);
//        assertEquals(1, entryIds.size());
//
//        RegistryEntry<DcsMetadataFormat> fgdcRegistryEntry = formatMapper.from(entryIds.iterator().next(),
//                fgdcRegistryEntryDcp, null);
//
//        assertNotNull(fgdcRegistryEntry);
//
//        registryEntries.put(entryId, fgdcRegistryEntry);
//
//        TypedRegistry<DcsMetadataFormat> schemaRegistry =
//                new InMemoryRegistry(registryId, registryType, registryEntries, registryDesc);
//
//        assertEquals(registryType, schemaRegistry.getType());
//        assertEquals(registryDesc, schemaRegistry.getDescription());
//
//        final int expectedEntryCount = registryEntries.size();
//        int actualEntryCount = 0;
//
//        final Iterator iterator = schemaRegistry.iterator();
//        while (iterator.hasNext()) {
//            iterator.next();
//            actualEntryCount++;
//        }
//
//        assertEquals(expectedEntryCount, actualEntryCount);
//    }

}
