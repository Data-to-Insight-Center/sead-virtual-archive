package org.dataconservancy.mhf.representations;

import org.dataconservancy.mhf.representation.api.AttributeSet;
import org.dataconservancy.mhf.representation.api.MetadataRepresentation;

/**
 * A {@code MetadataRepresentation} that encapsulates an {@code AttributeSet}.
 */
public class AttributeSetMetadataRepresentation extends BaseMetadataRepresentation<AttributeSet> {

    public static final String REPRESENTATION_ID = "dataconservancy.org:mhf:representations:attribute-set:1.0";

    private AttributeSet attributeSet;

    /**
     * Constructs a new AttributeSetMetadataRepresentation.  The supplied {@code AttributeSet} must not be null.
     *
     * @param attributeSet the AttributeSet
     */
    public AttributeSetMetadataRepresentation(AttributeSet attributeSet) {
        if (attributeSet == null) {
            throw new IllegalArgumentException("AttributeSet must not be null.");
        }

        this.attributeSet = attributeSet;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * Returns the {@code AttributeSet} encapsuated by this {@code MetadataRepresentation}.
     *
     * @return the {@code AttributeSet}
     */
    @Override
    public AttributeSet getRepresentation() {
        return attributeSet;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note: <br/>
     * Returns the representation ID that identifies the {@link #getRepresentation() representation} as an
     * {@code AttributeSet}.
     *
     * @return the {@code AttributeSet} representation id
     */
    @Override
    public String getRepresentationId() {
        return REPRESENTATION_ID;
    }

}
