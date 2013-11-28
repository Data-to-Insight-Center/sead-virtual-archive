package org.dataconservancy.mhf.validators.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.dataconservancy.mhf.validators.ValidatorLogger;
import org.dataconservancy.mhf.validators.ValidatorLoggerFactory;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Deserializes DCPs of Registry Entries of DcsMetadataFormats.
 */
public class MetadataFormatLoader {
    
    private static final ValidatorLogger LOG = ValidatorLoggerFactory.getLogger(MetadataFormatLoader.class);

    private final DcsModelBuilder modelBuilder;

    private final MetadataFormatMapper formatMapper;

    public MetadataFormatLoader(MetadataFormatMapper formatMapper, DcsModelBuilder modelBuilder) {
        if (formatMapper == null) {
            throw new IllegalArgumentException("Metadata Format Mapper must not be null.");
        }

        if (modelBuilder == null) {
            throw new IllegalArgumentException("DcsModelBuilder must not be null.");
        }

        this.formatMapper = formatMapper;
        this.modelBuilder = modelBuilder;
    }

    /**
     * Loads {@code DcsMetadataFormat} objects into the supplied {@code schemes Collection}.
     * <p/>
     * This method recursively lists all files under the {@code resourceBase}, and attempts to deserialize each file
     * it finds as a {@code DcsMetadataFormat}.  Files that cannot be deserialized as metadata formats are ignored.
     * The discovered metadata formats are decorated with a {@link FormatWithLoaderInfo}, placed into the
     * {@code schemes Collection}, and returned.
     *
     * @param resourceBase the base directory to start scanning from
     * @param formats      the {@code Collection} this method populates with discovered metadata formats
     * @throws IllegalArgumentException if {@code resourceBase} does not exist, isn't readable, or isn't a directory
     */
    public void loadMetadataFormats(File resourceBase, Collection<FormatWithLoaderInfo> formats) {

        LOG.log("Deserializing DcsMetadataFormats from '{}'", resourceBase.getAbsolutePath());

        Collection<File> files = listAllFiles(resourceBase);

        // Attempt to deserialize every file as a DcsMetadataFormat.  If we can't deserialize a file, just ignore it.

        for (File candidateFile : files) {
            try {
                RegistryEntry<DcsMetadataFormat> formatRegistryEntry = deserializeEntry(candidateFile);
                DcsMetadataFormat metadataFormat = formatRegistryEntry.getEntry();

                if (LOG.isVerbose()) {
                    LOG.log("Deserialized file '{}' to a DcsMetadataFormat: {}", candidateFile.getAbsolutePath(),
                            metadataFormat);
                }
              
                formats.add(new FormatWithLoaderInfo(candidateFile,
                        metadataFormat));
            } catch (Exception e) {
                if (LOG.isVerbose()) {
                    LOG.log("Error deserializing file '" + candidateFile.getAbsolutePath() + "' to a " +
                            "DcsMetadataFormat object: " + e.getMessage(), e);
                }
            }
        }

        LOG.log("Loaded " + formats.size() + " metadata schemes from " + resourceBase.getAbsolutePath());
    }

    public void loadRegistryEntries(File resourceBase, Map<String, RegistryEntry<DcsMetadataFormat>> formatEntries) {

        LOG.log("Deserializing DcsMetadataFormats from '{}'", resourceBase.getAbsolutePath());

        Collection<File> files = listAllFiles(resourceBase);

        // Attempt to deserialize every file as a DcsMetadataFormat.  If we can't deserialize a file, just ignore it.

        for (File candidateFile : files) {
            try {
                RegistryEntry<DcsMetadataFormat> formatRegistryEntry = deserializeEntry(candidateFile);
                DcsMetadataFormat metadataFormat = formatRegistryEntry.getEntry();

                formatEntries.put(formatRegistryEntry.getId(), formatRegistryEntry);

                if (LOG.isVerbose()) {
                    LOG.log("Deserialized file '{}' to a DcsMetadataFormat: {}", candidateFile.getAbsolutePath(),
                            metadataFormat);
                }

                LOG.log("Loaded registry entry: entry id '{}', entry type URI '{}', entry keys '{}' entry object type '{}'",
                        new Object[]{formatRegistryEntry.getId(), formatRegistryEntry.getType(),
                                concatKeys(formatRegistryEntry.getKeys()), metadataFormat.getClass().getName()});

            } catch (Exception e) {
                if (LOG.isVerbose()) {
                    LOG.log("Error deserializing file '" + candidateFile.getAbsolutePath() + "' to a " +
                            "DcsMetadataFormat object: " + e.getMessage(), e);
                }
            }
        }

        LOG.log("Loaded " + formatEntries.size() + " metadata formats from " + resourceBase.getAbsolutePath());

    }

    private RegistryEntry<DcsMetadataFormat> deserializeEntry(File candidateFile) throws InvalidXmlException, FileNotFoundException {
        final FileInputStream in = new FileInputStream(candidateFile);
        try {
            Dcp registryEntryDcp = modelBuilder.buildSip(in);
            return formatMapper.from(
                    formatMapper.discover(registryEntryDcp).iterator().next(), registryEntryDcp, null);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private Collection<File> listAllFiles(File resourceBase) {
        if (!resourceBase.exists()) {
            throw new IllegalArgumentException("Resource base '" + resourceBase + "' doesn't exist (or may not " +
                    "be readable.");
        }

        if (!resourceBase.isDirectory()) {
            throw new IllegalArgumentException("Resource base '" + resourceBase.getAbsolutePath() + "' must be a " +
                    "directory.");
        }

        if (!resourceBase.canRead()) {
            throw new IllegalArgumentException("Directory '" + resourceBase.getAbsolutePath() + "' cannot be read.");
        }

        return FileUtils.listFiles(resourceBase, FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY);
    }

    private String concatKeys(Collection<String> keys) {
        final String sep = ",";
        final StringBuilder sb = new StringBuilder();
        final int size = keys.size();
        int count = 0;
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            sb.append(itr.next());
            count++;
            if (count < size) {
                sb.append(sep);
            }
        }

        return sb.toString();
    }

    /**
     * Wraps a {@code DcsMetadataFormat} object to include the file its serialization was loaded from.
     */
    public class FormatWithLoaderInfo {

        /**
         * The file that contained the {@link #format} serialization
         */
        private File loadedFrom;

        /**
         * The DcsMetadataFormat loaded from {@link #loadedFrom}
         */
        private DcsMetadataFormat format;

        public FormatWithLoaderInfo(File loadedFrom, DcsMetadataFormat format) {
            this.loadedFrom = loadedFrom;
            this.format = format;
        }

        public File getLoadedFrom() {
            return loadedFrom;
        }

        public void setLoadedFrom(File loadedFrom) {
            this.loadedFrom = loadedFrom;
        }

        public DcsMetadataFormat getFormat() {
            return format;
        }

        public void setScheme(DcsMetadataFormat format) {
            this.format = format;
        }
    }

}
