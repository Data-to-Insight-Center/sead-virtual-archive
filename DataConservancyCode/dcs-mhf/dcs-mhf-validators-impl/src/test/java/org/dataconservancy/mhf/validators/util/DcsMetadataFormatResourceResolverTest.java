package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class DcsMetadataFormatResourceResolverTest {

    private static final String FGDC_SCHEMA_URL = "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd";

    private static final String FGDC_SCHEMA_VERSION = "1998";

    private static final String FGDC_SCHEMA_RESOURCE = "/org/dataconservancy/mhf/resources/schemas/fgdc1998/fgdc-std-001-1998.xsd";

    private static final String RESOLUTION_STR_WITH_VERSION = FGDC_SCHEMA_URL + "," + FGDC_SCHEMA_VERSION;

    private static final String RESOLUTION_STR_RESOURCE_ONLY = FGDC_SCHEMA_URL;

    private static final String RESOLUTION_STR_TRAILING_COMMA = FGDC_SCHEMA_URL + ",";

    private static final String REGISTRY_ENTRY_TYPE = "dataconservancy.org:mhf:metadataformats";

    private Set<RegistryEntry<DcsMetadataFormat>> entries = new HashSet<RegistryEntry<DcsMetadataFormat>>();

    private URL fgdcSchema = null;

    private DcsMetadataScheme fgdcScheme = null;
    private DcsMetadataFormat fgdcFormat = null;

    @Before
    public void setUp() throws Exception {
        fgdcSchema = this.getClass().getResource(FGDC_SCHEMA_RESOURCE);
        assertNotNull("Unable to resolve " + FGDC_SCHEMA_RESOURCE + " on the classpath.", fgdcSchema);

        fgdcScheme = new DcsMetadataScheme();
        fgdcScheme.setName("FGDC 1998");
        fgdcScheme.setSchemaVersion("1998");
        fgdcScheme.setSource(fgdcSchema.toExternalForm());
        fgdcScheme.setSchemaUrl(FGDC_SCHEMA_URL);

        fgdcFormat = new DcsMetadataFormat();
        fgdcFormat.setVersion("1998");
        fgdcFormat.setName("FGDC 1998");
        fgdcFormat.setId("format/FGDC 1998");
        fgdcFormat.addScheme(fgdcScheme);
        
        RegistryEntry<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>("entry/1", fgdcFormat,
                REGISTRY_ENTRY_TYPE, Arrays.asList(FGDC_SCHEMA_URL, FGDC_SCHEMA_VERSION),
                "Example FGDC Metadata Format Registry Entry with a URL and Version.");

        entries.add(entry);
    }

    @Test
    public void testResolveSchemaUrlAndVersionOk() throws Exception {
        TypedRegistry<DcsMetadataFormat> registry = mock(TypedRegistry.class);
        when(registry.lookup(FGDC_SCHEMA_URL, FGDC_SCHEMA_VERSION)).thenReturn(entries);

        final DcsMetadataFormatResourceResolver underTest = new DcsMetadataFormatResourceResolver(registry);
        final RegistryEntry<DcsMetadataFormat> metadataFormat = underTest.resolve(RESOLUTION_STR_WITH_VERSION, "");

        assertNotNull(metadataFormat);
        assertNotNull(metadataFormat.getEntry());
        assertFalse(metadataFormat.getEntry().getSchemes().isEmpty());

        final URL actualFgdcSchema = new URL(metadataFormat.getEntry().getSchemes().iterator().next().getSource());

        verify(registry).lookup(FGDC_SCHEMA_URL, FGDC_SCHEMA_VERSION);
        assertNotNull(actualFgdcSchema);
        assertEquals(fgdcSchema, actualFgdcSchema);
    }

    @Test
    public void testResolveSchemaAndVersionNotFoundInRegistry() throws Exception {
        TypedRegistry<DcsMetadataFormat> registry = mock(TypedRegistry.class);
        when(registry.lookup(anyString())).thenReturn(Collections.<RegistryEntry<DcsMetadataFormat>>emptySet());

        final DcsMetadataFormatResourceResolver underTest = new DcsMetadataFormatResourceResolver(registry);

        assertNull(underTest.resolve(RESOLUTION_STR_WITH_VERSION, ""));
        verify(registry).lookup(FGDC_SCHEMA_URL, FGDC_SCHEMA_VERSION);
    }

    @Test
    public void testResolveSchemaOk() throws Exception {
        TypedRegistry<DcsMetadataFormat> registry = mock(TypedRegistry.class);
        when(registry.lookup(FGDC_SCHEMA_URL)).thenReturn(entries);

        final DcsMetadataFormatResourceResolver underTest = new DcsMetadataFormatResourceResolver(registry);
        final RegistryEntry<DcsMetadataFormat> metadataFormat = underTest.resolve(RESOLUTION_STR_RESOURCE_ONLY, "");

        assertNotNull(metadataFormat);
        assertNotNull(metadataFormat.getEntry());
        assertFalse(metadataFormat.getEntry().getSchemes().isEmpty());

        final URL actualFgdcSchema = new URL(metadataFormat.getEntry().getSchemes().iterator().next().getSource());

        verify(registry).lookup(FGDC_SCHEMA_URL);
        assertNotNull(actualFgdcSchema);
        assertEquals(fgdcSchema, actualFgdcSchema);
    }

    @Test
    public void testResolveSchemaEmptyVersionOk() throws Exception {
        TypedRegistry<DcsMetadataFormat> registry = mock(TypedRegistry.class);
        when(registry.lookup(FGDC_SCHEMA_URL)).thenReturn(entries);

        final DcsMetadataFormatResourceResolver underTest = new DcsMetadataFormatResourceResolver(registry);
        final RegistryEntry<DcsMetadataFormat> metadataFormat = underTest.resolve(RESOLUTION_STR_TRAILING_COMMA, "");

        assertNotNull(metadataFormat);
        assertNotNull(metadataFormat.getEntry());
        assertFalse(metadataFormat.getEntry().getSchemes().isEmpty());

        final URL actualFgdcSchema = new URL(metadataFormat.getEntry().getSchemes().iterator().next().getSource());

        verify(registry).lookup(FGDC_SCHEMA_URL);
        assertNotNull(actualFgdcSchema);
        assertEquals(fgdcSchema, actualFgdcSchema);
    }

    @Test
    public void testResolveSchemaMultipleMatches() throws Exception {
        TypedRegistry<DcsMetadataFormat> registry = mock(TypedRegistry.class);

        RegistryEntry<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>("entry/2", fgdcFormat,
                        REGISTRY_ENTRY_TYPE, Arrays.asList(FGDC_SCHEMA_URL, FGDC_SCHEMA_VERSION),
                        "Another FGDC Metadata Scheme Registry Entry with a URL and Version.");
        entries.add(entry);
        assertEquals(2, entries.size());

        when(registry.lookup(FGDC_SCHEMA_URL)).thenReturn(entries);

        final DcsMetadataFormatResourceResolver underTest = new DcsMetadataFormatResourceResolver(registry);
        final RegistryEntry<DcsMetadataFormat> metadataFormat = underTest.resolve(RESOLUTION_STR_TRAILING_COMMA, "");

        assertNotNull(metadataFormat);
        assertNotNull(metadataFormat.getEntry());
        assertFalse(metadataFormat.getEntry().getSchemes().isEmpty());

        final URL actualFgdcSchema = new URL(metadataFormat.getEntry().getSchemes().iterator().next().getSource());

        verify(registry).lookup(FGDC_SCHEMA_URL);
        assertNotNull(actualFgdcSchema);
        assertEquals(fgdcSchema, actualFgdcSchema);
    }
}
