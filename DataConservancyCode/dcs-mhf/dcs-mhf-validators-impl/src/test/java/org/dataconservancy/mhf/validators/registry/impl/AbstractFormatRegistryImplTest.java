package org.dataconservancy.mhf.validators.registry.impl;

import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.mhf.validators.util.MetadataFormatLoader;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Abstract test harness for the FormatRegistryImpl.  Allows the same tests to be executed against a manually wired
 * FormatRegistryImpl and a Spring-wired FormatRegistryImpl.
 *
 * @see FormatRegistryImplTest
 * @see FormatRegistryImplSpringTest
 */
public abstract class AbstractFormatRegistryImplTest {

    protected static final String REGISTRY_ID = "org.dataconservancy:registry:metadataformat";

    protected static final String REGISTRY_ENTRY_TYPE = "dataconservancy.types:registry-entry:metadataformat";

    protected static final String REGISTRY_DESC = "Registry of Metadata Formats";

    protected static final int EXPECTED_ENTRY_COUNT = 2;

    private Map<String, RegistryEntry<DcsMetadataFormat>> expectedEntries =
            new HashMap<String, RegistryEntry<DcsMetadataFormat>>();

    private FormatRegistryImpl underTest;

    @Before
    public void setUp() throws Exception {
        underTest = getUnderTest();

        assertNotNull(underTest);

        expectedEntries = getExpectedEntries();

        assertNotNull(expectedEntries);

        // Ensure that the formatLoader loads the expected number of entries, and that the entry objects aren't null
        // (may need to relax the requirement on the number of expected entries -- e.g. test for something greater than
        // zero)
        assertEquals(EXPECTED_ENTRY_COUNT, expectedEntries.size());
        for (RegistryEntry<DcsMetadataFormat> entry : expectedEntries.values()) {
            assertNotNull(entry.getEntry());
        }
    }

    protected abstract FormatRegistryImpl getUnderTest();

    protected Map<String,RegistryEntry<DcsMetadataFormat>> getExpectedEntries() {
        // MetadataFormatLoader deserializes RegistryEntries from the file system.
        final MetadataFormatLoader formatLoader = new MetadataFormatLoader(new MetadataFormatMapper(new MetadataSchemeMapper()),
                new DcsXstreamStaxModelBuilder());

        final Map<String, RegistryEntry<DcsMetadataFormat>> expectedEntries =
                    new HashMap<String, RegistryEntry<DcsMetadataFormat>>();

        // Use the formatLoader to obtain the expected RegistryEntries
        formatLoader.loadRegistryEntries(
                new File(this.getClass().getResource(MHFResources.METADATA_FORMAT_REGISTRY_RESOURCE_PATH).getPath()),
                expectedEntries);

        return expectedEntries;
    }


    @Test
    public void testGetType() throws Exception {
        assertEquals(REGISTRY_ENTRY_TYPE, underTest.getType());
    }

    @Test
    public void testRetrieve() throws Exception {
        for (String id : expectedEntries.keySet()) {
            final RegistryEntry<DcsMetadataFormat> actualEntry = underTest.retrieve(id);
            assertNotNull(actualEntry);
            assertEquals(expectedEntries.get(id), actualEntry);
        }
    }

    @Test
    public void testLookup() throws Exception {
        int found = 0;

        // Lookup by ID
        for (String id : expectedEntries.keySet()) {
            final RegistryEntry<DcsMetadataFormat> actualEntry = underTest.retrieve(id);
            assertNotNull(actualEntry);
            assertEquals(expectedEntries.get(id), actualEntry);
            found++;
        }

        assertEquals(EXPECTED_ENTRY_COUNT, found);

        // reset counter
        found = 0;

        // TODO Lookup by Format ID
        for (RegistryEntry e : underTest.lookup(MetadataFormatId.XSD_XML_FORMAT_ID)) {
            found++;
        }

        for (RegistryEntry e : underTest.lookup(MetadataFormatId.FGDC_XML_FORMAT_ID)) {
            found++;
        }

        assertEquals(EXPECTED_ENTRY_COUNT, found);
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(REGISTRY_DESC, underTest.getDescription());
    }

    @Test
    public void testIterator() throws Exception {
        Iterator<RegistryEntry<DcsMetadataFormat>> iterator = underTest.iterator();
        assertNotNull(iterator);

        while (iterator.hasNext()) {
            RegistryEntry<DcsMetadataFormat> entry = iterator.next();
            assertNotNull(entry);
            assertNotNull(entry.getEntry());
            expectedEntries.remove(entry.getId());
        }

        assertEquals(0, expectedEntries.size());
    }

}
