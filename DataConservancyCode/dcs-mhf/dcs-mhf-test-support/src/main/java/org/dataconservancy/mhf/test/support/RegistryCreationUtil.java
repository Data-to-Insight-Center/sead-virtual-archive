package org.dataconservancy.mhf.test.support;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.shared.memory.InMemoryRegistry;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RegistryCreationUtil {
        public static final String metadataFormatEntryType = "dataconservancy.types:registry-entry:metadataformat";

        /**
         * Creates and populates a registry that is used by the {@code ResourceResolver} to provide access to schemas and
         * external schema entities from a registry, instead of looking them up online.
         *
         * @return a configured registry full of all the necessary schemas to validate our test metadata instances
         */
        public static TypedRegistry<DcsMetadataFormat> createAndPopulateSchemaRegistry(String baseDirPath) {
            Map<String, RegistryEntry<DcsMetadataFormat>> entries = new HashMap<String, RegistryEntry<DcsMetadataFormat>>();
            int id = 0;

            // Populate the registry with DcsMetadataFormat objects for each format.  We only create a full DcsMetadataScheme
            // object for the FGDC XML format and the XSD format.  The other entries are there just to simulate a registry
            // with additional entries.

            for (String formatId : MetadataFormatId.FORMAT_IDS) {
                DcsMetadataFormat mdFormat = new DcsMetadataFormat();
                mdFormat.setId(formatId);
                mdFormat.setName("Metadata Format " + formatId);
                BasicRegistryEntryImpl<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>();
                entry.setId("metadataScheme/" + id++);
                
                final String description = "Metadata Scheme for format " + formatId;
                entry.setDescription(description);
                entry.setEntryType(metadataFormatEntryType);
                entry.setKeys(Arrays.asList(formatId));

                DcsMetadataScheme mdScheme = new DcsMetadataScheme();
                mdScheme.setName(description);
                if (formatId.equals(MetadataFormatId.FGDC_XML_FORMAT_ID)) {
                    mdScheme.setSchemaUrl("http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd");
                    mdScheme.setSchemaVersion("1998");
                    mdFormat.setVersion("1998");
                    mdScheme.setSource(RegistryCreationUtil.class
                            .getResource(MHFResources.FGDC_SCHEMA_RESOURCE_PATH)
                            .toExternalForm());
                }

                if (formatId.equals(MetadataFormatId.XSD_XML_FORMAT_ID)) {
                    mdScheme.setSchemaUrl("http://www.w3.org/2001/XMLSchema.xsd");
                    mdScheme.setSchemaVersion("2004");
                    mdFormat.setVersion("2004");
                    mdScheme.setSource(RegistryCreationUtil.class
                            .getResource(MHFResources.XSD_SCHEMA_RESOURCE_PATH)
                            .toExternalForm());
                }
                
                mdFormat.addScheme(mdScheme);

                entry.setEntry(mdFormat);

                entries.put(entry.getId(), entry);
            }

            // Populate the registry with DcsMetadataScheme objects for each FGDC schema part.
            if (baseDirPath.endsWith(MHFResources.SCHEMA_BASE_RESOURCE_PATH)) {
                baseDirPath = baseDirPath.substring(0, baseDirPath.indexOf(MHFResources.SCHEMA_BASE_RESOURCE_PATH));
            }

            File baseDir = new File(baseDirPath, MHFResources.FGDC_BASE_RESOURCE_PATH);
            if (baseDir == null) {
                throw new RuntimeException("Could not create the required directory. Creation and population of schema registry is aborted.");
            }

            Collection<File> schemaFiles = null;
            try {
                schemaFiles = FileUtils.listFiles(baseDir, new WildcardFileFilter(Arrays.asList("fgdc-std-001-1998-sect*.xsd", "*.dtd", "*.sch")),
                        DirectoryFileFilter.DIRECTORY);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Unable to list files in " + baseDir.getAbsolutePath() + ": " + e.getMessage());
            }

            if (schemaFiles.size() != 12) {
                throw new RuntimeException("Exception occured when setting up In-memory-metadata-schema Registry");
            }

            BasicRegistryEntryImpl<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>();
            entry.setId("metadataFormat/" + id++);
            String description = "Metadata Format for schema file fgdc-1998";
            entry.setDescription(description);
            DcsMetadataFormat mdFormat = new DcsMetadataFormat();
            mdFormat.setId(entry.getId());
            mdFormat.setName(description);
            mdFormat.setVersion("1998");
                        
            ArrayList<String> keys = new ArrayList<String>();
            ArrayList<DcsMetadataScheme> schemes = new ArrayList<DcsMetadataScheme>();
            for (File schemaFile : schemaFiles) {
                
                entry.setEntryType(metadataFormatEntryType);
                
                if (!schemaFile.getName().equalsIgnoreCase("fgdc-std-001-1998.xsd")) {
                    keys.addAll(Arrays.asList(schemaFile.getName(),
                                              "http://www.fgdc.gov/schemas/metadata/" + schemaFile.getName()));
                }

                DcsMetadataScheme mdScheme = new DcsMetadataScheme();
                mdScheme.setName(schemaFile.getName());
                mdScheme.setSchemaUrl("http://www.fgdc.gov/schemas/metadata/" + schemaFile.getName());
                mdScheme.setSchemaVersion("1998");
                mdScheme.setSource("file:" + baseDirPath + MHFResources.FGDC_BASE_RESOURCE_PATH + schemaFile.getName());
                
                schemes.add(mdScheme);
            }
            
            mdFormat.setSchemes(schemes);

            entry.setKeys(keys);
            entry.setEntry(mdFormat);
            entries.put(entry.getId(), entry);

            // Populate the registry with DcsMetadataScheme objects for the XSD schema used to validate XSD schema instances
            BasicRegistryEntryImpl<DcsMetadataFormat> mdEntry = new BasicRegistryEntryImpl<DcsMetadataFormat>();
            mdEntry.setId("metadataScheme/" + id++);
            description = "Metadata Scheme for schema file " + MHFResources.XSD_DTD_RESOURCE_PATH;
            mdEntry.setDescription(description);
            mdEntry.setEntryType(metadataFormatEntryType);
            mdEntry.setKeys(Arrays.asList("http://www.w3.org/2001/XMLSchema.dtd", "XMLSchema.dtd"));

            DcsMetadataFormat xsdFormat = new DcsMetadataFormat();
            xsdFormat.setId(mdEntry.getId());
            xsdFormat.setName(description);
            xsdFormat.setVersion("XML Schema, 2nd Edition July 2004");
            
            DcsMetadataScheme mdScheme = new DcsMetadataScheme();
            mdScheme.setName(description);
            mdScheme.setSchemaUrl("http://www.w3.org/2001/XMLSchema.xsd");
            mdScheme.setSchemaVersion("XML Schema, 2nd Edition July 2004");
            mdScheme.setSource("file:" + baseDirPath + MHFResources.XSD_DTD_RESOURCE_PATH);
            xsdFormat.addScheme(mdScheme);
            
            mdEntry.setEntry(xsdFormat);

            entries.put(mdEntry.getId(), mdEntry);

            mdEntry = new BasicRegistryEntryImpl<DcsMetadataFormat>();
            mdEntry.setId("metadataScheme/" + id++);
            description = "Metadata Scheme for schema file " + MHFResources.XSD_DATATYPE_DTD_RESOURCE_PATH;
            mdEntry.setDescription(description);
            mdEntry.setEntryType(metadataFormatEntryType);
            mdEntry.setKeys(Arrays.asList("http://www.w3.org/2001/datatypes.dtd", "datatypes.dtd"));

            mdFormat = new DcsMetadataFormat();
            mdFormat.setId(mdEntry.getId());
            mdFormat.setName(description);
            mdFormat.setVersion("XML Schema, 2nd Edition July 2004");
            
            mdScheme = new DcsMetadataScheme();
            mdScheme.setName(description);
            mdScheme.setSchemaUrl("http://www.w3.org/2001/datatypes.dtd");
            mdScheme.setSchemaVersion("XML Schema, 2nd Edition July 2004");
            mdScheme.setSource("file:" + baseDirPath + MHFResources.XSD_DATATYPE_DTD_RESOURCE_PATH);

            mdFormat.addScheme(mdScheme);
            mdEntry.setEntry(mdFormat);

            entries.put(mdEntry.getId(), mdEntry);

            mdEntry = new BasicRegistryEntryImpl<DcsMetadataFormat>();
            mdEntry.setId("metadataScheme/" + id++);
            description = "Metadata Scheme for schema file " + MHFResources.XSD_XML_DTD_RESOURCE_PATH;
            mdEntry.setDescription(description);
            mdEntry.setEntryType(metadataFormatEntryType);
            mdEntry.setKeys(Arrays.asList("http://www.w3.org/2001/xml.xsd", "xml.xsd"));

            mdFormat = new DcsMetadataFormat();
            mdFormat.setId(mdEntry.getId());
            mdFormat.setName(description);
            mdFormat.setVersion("XML Schema, 2nd Edition July 2004");
            
            mdScheme = new DcsMetadataScheme();
            mdScheme.setName(description);
            mdScheme.setSchemaUrl("http://www.w3.org/2001/xml.xsd");
            mdScheme.setSchemaVersion("XML Schema, 2nd Edition July 2004");
            mdScheme.setSource("file:" + baseDirPath + MHFResources.XSD_XML_DTD_RESOURCE_PATH);

            mdFormat.addScheme(mdScheme);
            mdEntry.setEntry(mdFormat);

            entries.put(mdEntry.getId(), mdEntry);

            return new InMemoryRegistry("registry/1",
                    metadataFormatEntryType, entries, "Registry of supported Metadata Formats");
        }
}
