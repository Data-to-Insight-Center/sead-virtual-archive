package org.dataconservancy.mhf.services;

import org.dataconservancy.mhf.extractor.api.MetadataExtractor;
import org.dataconservancy.mhf.finder.api.MetadataFinder;
import org.dataconservancy.mhf.instance.api.MetadataInstance;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;
import org.dataconservancy.registry.api.Registry;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.shared.test.support.SimpleRegistry;
import org.dataconservancy.registry.shared.test.support.SimpleTypedRegistry;
import org.dataconservancy.ui.model.MetadataFile;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

/**
 * Simple tests to prototype the use of the MHF and the Registry.  The tests are not meant to verify compliance with
 * a contract, but to insure that the APIs can be used in reasonable ways.
 */
public class PrototypeRegistryTest {


    /**
     * The string indicating the {@link org.dataconservancy.registry.api.RegistryEntry#getType() type} of MetadataFinder
     * registry entries.
     */
    private static final String FINDER_ENTRY_TYPE = "dataconservancy.org:mhf:finder";

    /**
     * The string indicating the {@link org.dataconservancy.registry.api.RegistryEntry#getType() type} of
     * MetadataExtractor registry entries.
     */
    private static final String EXTRACTOR_ENTRY_TYPE = "dataconservancy.org:mhf:extractor";

    /**
     * A simple test which populates an in-memory TypedRegistry, and insures that MetadataFinders can be retrieved.
     *
     * @throws Exception
     */
    @Test
    public void testRegistrySimple() throws Exception {
        // string identifying the type of registry entries found in the TypedRegistry.  This will probably be injected
        // into the client

        MetadataFinder mockFinder = mock(MetadataFinder.class);
        // Build a single registry entry for a MetadataFinder impl for MetadataFile business objects
        // Normally the registry instance will already be populated; the client of the registry API won't have
        // to do this.
        final BasicRegistryEntryImpl<MetadataFinder> finderEntry = new BasicRegistryEntryImpl<MetadataFinder>();
        final Set<String> finderEntryKeys = new HashSet<String>();
        finderEntryKeys.add(MetadataFile.class.getName());
        finderEntry.setKeys(finderEntryKeys);
        finderEntry.setDescription("Discovers embedded Metadata contained within files (e.g. EXIF in TIFF_FORMAT_ID)");
        finderEntry.setId("dataconservancy.org:mhf:finder:001");  // this id is globally unique, immutable, etc.
        finderEntry.setEntryType(FINDER_ENTRY_TYPE);
        finderEntry.setEntry(mockFinder);

        // This is the backing map of registry entries
        final Map<String, RegistryEntry<MetadataFinder>> entryMap = new HashMap<String, RegistryEntry<MetadataFinder>>();
        System.out.println(finderEntry.getId());
        System.out.println(finderEntry.getEntry());
        entryMap.put(finderEntry.getId(), finderEntry);

        // Create the TypedRegistry instance, using our backing map of entries we just created.
        // The registry instance should be injected into the service impl.
        final TypedRegistry<MetadataFinder> registry =
                newRegistry(FINDER_ENTRY_TYPE, "A Registry of Metadata Finders", entryMap);

        // provided by the caller of the service
        final MetadataFile bo = new MetadataFile();

        MetadataInstance mockInstance = mock(MetadataInstance.class);
        List<MetadataInstance> mockInstances = new ArrayList<MetadataInstance>();
        mockInstances.add(mockInstance);
        when(mockFinder.findMetadata(bo)).thenReturn(mockInstances);
        when(mockInstance.getFormatId()).thenReturn("mock:format:id");
        // This is the logic that a service implementation might execute to retrieve the MetadataFinder
        Set<RegistryEntry<MetadataFinder>> entries = registry.lookup(bo.getClass().getName());
        Set<MetadataFinder> finders = new HashSet<MetadataFinder>();
        for (RegistryEntry<MetadataFinder> entry : entries) {
            // the TypedRegistry guarantees that every entry in this Registry will be of the same type
            // there's no need to something conditional based on the entry type (which wouldn't be true if the
            // TypedRegistry were an instance of Registry, instead)
            System.out.println("adding entry " +entry.getEntry());
            finders.add(entry.getEntry());
        }

        assertEquals(1, finders.size());
        
        Collection<MetadataInstance> instances = new ArrayList<MetadataInstance>();

        // For each finder ...
        for (MetadataFinder finder : finders) {
            // ... the service implementation then invokes the finder(s) on the business object
            // storing the instances.
            instances.addAll(finder.findMetadata(bo));
        }

        MetadataExtractor mockExtractor = mock(MetadataExtractor.class);
        // Build a single registry entry for a MetadataExtractor impl for metadata instance objects
        // Normally the registry instance will already be populated; the client of the registry API won't have
        // to do this.
        final BasicRegistryEntryImpl<MetadataExtractor> extractorEntry = new BasicRegistryEntryImpl<MetadataExtractor>();
        final Set<String> extractorEntryKeys = new HashSet<String>();
        extractorEntryKeys.add("mock:format:id");
        extractorEntry.setKeys(extractorEntryKeys);
        extractorEntry.setDescription("Extracts embedded Metadata contained within files (e.g. EXIF in TIFF_FORMAT_ID)");
        extractorEntry.setId("dataconservancy.org:mhf:extractor:001");  // this id is globally unique, immutable, etc.
        extractorEntry.setEntryType(EXTRACTOR_ENTRY_TYPE);
        extractorEntry.setEntry(mockExtractor);
        
        // This is the backing map of registry entries
        final Map<String, RegistryEntry<MetadataExtractor>> extractorEntryMap = new HashMap<String, RegistryEntry<MetadataExtractor>>();
        System.out.println(extractorEntry.getId());
        System.out.println(extractorEntry.getEntry());
        extractorEntryMap.put(extractorEntry.getId(), extractorEntry);

        // Create the TypedRegistry instance, using our backing map of entries we just created.
        // The registry instance should be injected into the service impl.
        final TypedRegistry<MetadataExtractor> extractorRegistry =
                newRegistry(EXTRACTOR_ENTRY_TYPE, "A Registry of Metadata Extractors", extractorEntryMap);
        
        // Then, obtain extractors per metadata instance
        for (MetadataInstance instance : instances) {
            Set<RegistryEntry<MetadataExtractor>> returnedExtractorEntries = extractorRegistry.lookup(instance.getFormatId());
            Set<MetadataExtractor> extractors = new HashSet<MetadataExtractor>();
            for (RegistryEntry<MetadataExtractor> entry : returnedExtractorEntries) {
                // the TypedRegistry guarantees that every entry in this Registry will be of the same type
                // there's no need to something conditional based on the entry type (which wouldn't be true if the
                // TypedRegistry were an instance of Registry, instead)
                System.out.println("adding entry " +entry.getEntry());
                extractors.add(entry.getEntry());
            }

            assertEquals(1, extractors.size());
            
        }

    }

    /**
     * In this test we see how an entire extraction pipeline may interact with a façade Registry interface.  Finders
     * are looked up, Instances produced, and transformed to Representations.
     *
     * @throws Exception
     */
    @Test
    public void testFinderExtractionRepresentationPipeline() throws Exception {
        // Assume a registry façade, that is, it isn't an instance of TypedRegistry, just Registry. So it can
        // contain entries of multiple types.
        // The Registry is injected into the service implementation
        final Registry registry = new SimpleRegistry();

        // The type string for finders and extractors are probably injected into the service impl.  But they
        // are available as fields on this class.

        // The business object we are finding/extracting for
        final MetadataFile mdf = new MetadataFile();

        // A container for the finders we obtain from the registry
        final Set<MetadataFinder> finders = new HashSet<MetadataFinder>();

        // Obtain the finders from the registry, keyed by the business object class name
        for (RegistryEntry entry : registry.lookup(mdf.getClass().getName())) {
            if (FINDER_ENTRY_TYPE.equals(entry.getType())) {
                finders.add((MetadataFinder) entry.getEntry());
            }
        }

        // A container for the MetadataInstances discovered on the object
        final Collection<MetadataInstance> instances = new ArrayList<MetadataInstance>();

        // Iterate over the finders, and discover all the available MetadataInstances on the BO
        for (MetadataFinder finder : finders) {
            instances.addAll(finder.findMetadata(mdf));
        }

        // A container for all of the Extractors
        final Map<MetadataInstance, Collection<MetadataExtractor>> extractors =
                new HashMap<MetadataInstance, Collection<MetadataExtractor>>();

        // Obtain the extractors from the registry, keyed by the format identifier (and representation id??) of the MdI
        for (MetadataInstance instance : instances) {
            Collection<MetadataExtractor> extractorsForInstance;

            if (extractors.containsKey(instance)) {
                extractorsForInstance = extractors.get(instance);
            } else {
                extractorsForInstance = new ArrayList<MetadataExtractor>();
                extractors.put(instance, extractorsForInstance);
            }

            for (RegistryEntry entry : registry.lookup(instance.getFormatId())) {
                if (EXTRACTOR_ENTRY_TYPE.equals(entry.getType())) {
                    extractorsForInstance.add((MetadataExtractor) entry.getEntry());
                }
            }
        }

        // Container for MetadataRepresentations produced by extraction
        final Collection<MetadataRepresentation> representations = new ArrayList<MetadataRepresentation>();

        // Iterate over each MetadataInstance, performing extraction, and populating the representations.
        for (MetadataInstance instance : extractors.keySet()) {
            for (MetadataExtractor extractor : extractors.get(instance)) {
                Collection<MetadataRepresentation> reps = extractor.extractMetadata(instance);
                representations.addAll(reps);
            }
        }
    }

    /**
     * Produces an empty TypedRegistry.  Callers are responsible for setting properties on the registry instance.
     *
     * @param <T> the type of object managed by the registry
     * @return a new, empty, registry
     */
    private <T> TypedRegistry<T> newRegistry() {
        return new SimpleTypedRegistry<T>();
    }

    /**
     * Produces a TypedRegistry backed by the supplied {@code entryMap}.
     *
     * @param entryType the string identifying the type of entries in the registry
     * @param desc a short description of the registry
     * @param entryMap the map of registry entries backing the registry
     * @param <T> the type of object managed by the registry
     * @return a new, populated and configured, registry
     */
    private <T> TypedRegistry<T> newRegistry(String entryType, String desc, Map<String, RegistryEntry<T>> entryMap) {
        final SimpleTypedRegistry<T> registry = new SimpleTypedRegistry<T>();
        registry.setDescription(desc);
        registry.setType(entryType);
        registry.setEntries(entryMap);

        return registry;
    }
}
