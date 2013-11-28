package org.dataconservancy.mhf.finders;

import org.dataconservancy.mhf.finder.api.MetadataFinder;
import org.dataconservancy.mhf.model.builder.api.MetadataObjectBuilder;

/**
 * Base implementation of {@code MetadataFinder}.  All implementations should extend from this base class.
 */
public abstract class BaseMetadataFinder implements MetadataFinder {

    /**
     * String used in MetadataFindingException when there is a type mis-match (when an object of an unexpected type
     * is provided to the findMetadata(...) method.  Arguments are: actual type, expected type.
     */
    protected static final String TYPE_ERROR =
            "Cannot discover metadata instances from an object of type '%s', expected '%s'";

    /**
     * String used in MetadataFindingException when the source of a DataFile cannot be opened for reading.  Arguments
     * are: source, reason
     */
    protected static final String STREAM_ERROR =
            "Unable to resolve or open file source '%s': %s";

    /**
     * The builder used to serialize and de-serialize {@code AttributeSet}s
     */
    protected final MetadataObjectBuilder metadataObjectBuilder;

    /**
     * Constructs the finder.  Implementations will use the supplied {@code builder} for serializing and deserializing
     * {@code AttributeSet} objects.
     *
     * @param builder a fully-configured, ready to use, {@code MetadataObjectBuilder}
     */
    public BaseMetadataFinder(MetadataObjectBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("MetadataObjectBuilder must not be null.");
        }

        this.metadataObjectBuilder = builder;

    }

}
