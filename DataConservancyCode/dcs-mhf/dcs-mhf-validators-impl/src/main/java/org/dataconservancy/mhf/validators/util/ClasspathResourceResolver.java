package org.dataconservancy.mhf.validators.util;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.registry.impl.metadata.shared.MetadataFormatMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataRegistryConstant;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSchemeMapper;
import org.dataconservancy.registry.impl.metadata.shared.MetadataSerializer;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolves {@code DcsMetadataFormat} registry entries by loading their serializations from the classpath.  This
 * implementation interprets the {@code resourceBase} supplied on construction as something that resides on the
 * classpath; in other words, the {@code resourceBase} is searched for on the classpath using a {@code ClassLoader}. The
 * behavior of the resolver can be influenced by supplying a non-empty base resource path and a non-null
 * {@code ClassLoader}.
 * <p/>
 * This implementation does not support loading metadata schemes from a jar file.  The metadata schemes must lie
 * on a classpath on the filesystem.
 */
public class ClasspathResourceResolver extends BaseResourceResolver<DcsMetadataFormat> {

    /**
     * The ClassLoader used to search the classpath for the resource
     */
    private final ClassLoader loader;

    /**
     * Represents the base directory that DcsMetadataFormat serializations will reside under.
     * <p/>
     * The string will be resolved on the classpath to a file, and that file will become the base directory that is
     * searched for DcsMetadataFormat serializations.  The string may be empty but never {@code null}.
     *
     * @see #checkResourceBase()
     * @see #getResourceBase()
     */
    private final String resourceBase;

    /**
     * Used to deserialize DcsMetadataFormat objects from the filesystem
     */
    private final MetadataSerializer<DcsMetadataFormat> serializer;

    /**
     * Collection containing the deserialized DcsMetadataFormat objects.  This collection is populated by
     * {@link MetadataFormatLoader#loadMetadataFormats(java.io.File, java.util.Collection)}
     * on construction, and interrogated by {@link #resolveResourceInternal(String)}.
     */
    private final Set<MetadataFormatLoader.FormatWithLoaderInfo> metadataFormats =
            new HashSet<MetadataFormatLoader.FormatWithLoaderInfo>();

    private MetadataFormatMapper metadataFormatMapper = new MetadataFormatMapper(new MetadataSchemeMapper());

    private DcsModelBuilder modelBuilder = new DcsXstreamStaxModelBuilder(true);

    /**
     * Constructs a new instance using an empty {@code resourceBase}, and the {@code ClassLoader} that loaded
     * this instance.
     * <p/>
     * Upon construction, the base of the class loader will be resolved to a directory, and that directory will be
     * recursively scanned for serializations of DcsMetadataScheme objects.
     *
     * @param serializer the {@code MetadataSerializer} used to deserialize DcsMetadataFormat objects from the
     *                   filesystem.
     * @throws IllegalArgumentException if {@code resourceBase} cannot be resolved, is not a directory, or is not readable.
     */
    public ClasspathResourceResolver(MetadataSerializer<DcsMetadataFormat> serializer) {
        this("", ClasspathResourceResolver.class.getClassLoader(), serializer);
    }

    /**
     * Constructs a new instance using the supplied {@code resourceBase}, and the {@code ClassLoader} that loaded
     * this instance.
     * <p/>
     * Upon construction, the {@code resourceBase} will be resolved to a directory, and that directory will be
     * recursively scanned for serializations of DcsMetadataScheme objects.

     * @param resourceBase will be resolved to a classpath resource; must not be {@code null}, but may
     *                     be the empty string.
     * @param serializer the {@code MetadataSchemeSerializer} used to deserialize DcsMetadataFormat objects from the
     *                   filesystem.
     * @throws IllegalArgumentException if {@code resourceBase} or {@code serializer} is {@code null}; if the
     *                                  {@code resourceBase} cannot be resolved, is not a directory, or is not readable.
     */
    public ClasspathResourceResolver(String resourceBase, MetadataSerializer<DcsMetadataFormat> serializer) {
        if (resourceBase == null) {
            throw new IllegalArgumentException("Classpath resource base must not be null (but empty strings are " +
                    "allowed).");
        }

        if (serializer == null) {
            throw new IllegalArgumentException("MetadataSchemeSerializer must not be null.");
        }

        this.resourceBase = resourceBase.trim();
        this.loader = this.getClass().getClassLoader();
        this.serializer = serializer;
        checkResourceBase();
        MetadataFormatLoader formatLoader = new MetadataFormatLoader(metadataFormatMapper, modelBuilder);
        formatLoader.loadMetadataFormats(new File(getResourceBase().getPath()), this.metadataFormats);
    }

    /**
     * Constructs a new instance using the supplied {@code resourceBase} and {@code loader}.
     *
     * <p/>
     * Upon construction, the {@code resourceBase} will be resolved to a directory, and that directory will be
     * recursively scanned for serializations of DcsMetadataScheme objects.
     *
     * @param resourceBase will be resolved to a classpath resource; must not be {@code null}, but may
          *                be the empty string.
     * @param loader the {@code ClassLoader} used to find classpath resources
     * @throws IllegalArgumentException if {@code resourceBase}, {@code serializer} or {@code loader} is {@code null};
     *                                  if the {@code resourceBase} cannot be resolved, is not a directory, or is not
     *                                  readable.
     */
    public ClasspathResourceResolver(String resourceBase, ClassLoader loader, MetadataSerializer<DcsMetadataFormat> serializer) {
        if (resourceBase == null) {
            throw new IllegalArgumentException("Classpath resource base must not be null (but empty strings are " +
                    "allowed).");
        }

        if (loader == null) {
            throw new IllegalArgumentException("ClassLoader must not be null.");
        }

        if (serializer == null) {
            throw new IllegalArgumentException("MetadataSchemeSerializer must not be null.");
        }

        this.resourceBase = resourceBase;
        this.loader = loader;
        this.serializer = serializer;
        checkResourceBase();
        MetadataFormatLoader formatLoader = new MetadataFormatLoader(metadataFormatMapper, modelBuilder);
        formatLoader.loadMetadataFormats(new File(getResourceBase().getPath()), this.metadataFormats);
    }

    /**
     * Attempts to locate the {@link #resourceBase} on the class path
     *
     * @return the url to the {@code resourceBase}, or {@code null} if not found.
     */
    public URL getResourceBase() {
        return loader.getResource(resourceBase);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * This method interprets the {@code resourceString} as the {@code source} of the
     * {@code DcsMetadataFormat}; it attempts to match the {@code resourceString} matches the {@code source} of the
     * {@code DcsMetadataFormat}, then a {@code RegistryEntry} is composed for the matching metadata metadata and returned.
     *
     * @param resourceString the resource string to resolve; it will be matched against the {@code source} of the
     *                       deserialized {@code DcsMetadataScheme} objects.  The first match is returned.
     * @param baseUrl the base url of the resource to resolve, currently ignored
     * @return {@inheritDoc}
     */
    @Override
    public RegistryEntry<DcsMetadataFormat> resolveResourceInternal(String resourceString, String baseUrl) {

        MetadataFormatLoader.FormatWithLoaderInfo formatWithLoaderInfo = null;

        if (resourceString.trim().length() == 0) {
            log.log("Refusing to resolve an empty resource string.");
            return null;
        }

        for (MetadataFormatLoader.FormatWithLoaderInfo candidate : metadataFormats) {
            if (!candidate.getFormat().getSchemes().isEmpty()) {
                if (candidate.getFormat().getSchemes().iterator().next().getSource().equals(resourceString) ||
                        candidate.getFormat().getSchemes().iterator().next().getSource().endsWith(resourceString)) {
                    formatWithLoaderInfo = candidate;
                }
            }
        }

        if (formatWithLoaderInfo == null) {
            log.log("Unable to resolve resource '{}' to a DcsMetadataScheme (resource base was '{}')", resourceString,
                    getResourceBase().toExternalForm());
            return null;
        }

        Set<String> entryKeys = new HashSet<String>();

        if (!formatWithLoaderInfo.getFormat().getSchemes().isEmpty()) {
            if (formatWithLoaderInfo.getFormat().getSchemes().iterator().next().getSchemaUrl() != null) {
                entryKeys.add(formatWithLoaderInfo.getFormat().getSchemes().iterator().next().getSchemaUrl());
            }            
        }

        RegistryEntry<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>(
                formatWithLoaderInfo.getLoadedFrom().getAbsolutePath(), formatWithLoaderInfo.getFormat(),
                MetadataRegistryConstant.METADATASCHEME_REGISTRY_ENTRY_TYPE, entryKeys,
                formatWithLoaderInfo.getFormat().getName());

        return entry;
    }

    /**
     * Issues a warning if the {@link #resourceBase} cannot be resolved.
     */
    private void checkResourceBase() {
        final URL base = getResourceBase();
        if (base == null) {
            log.log("Could not resolve resource base '{}' with ClassLoader '{}'", resourceBase, loader);
        } else {
            if (log.isVerbose()) {
                log.log("Instantiated {}@{} with resource base '{}' that resolves to '{}'",
                        new Object[]{this.getClass().getName(), Integer.toHexString(System.identityHashCode(this)),
                                resourceBase, base.toExternalForm()});
            }
        }
    }

}
