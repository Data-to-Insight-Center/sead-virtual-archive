package org.dataconservancy.mhf.test.support;

import com.thoughtworks.xstream.XStream;
import org.dataconservancy.mhf.model.builder.xstream.XStreamAttributeValueBuilder;
import org.dataconservancy.mhf.model.builder.xstream.XstreamMetadataObjectBuilder;
import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.dataconservancy.ui.model.PersonName;

/**
 * Utility methods for instantiating and configuring concrete instances of the {@code MetadataObjectBuilder}
 */
public class BuilderTestUtil {

    /**
     * Creates and configures a new instance of XstreamMetadataObjectBuilder.  The returned instance is initialized and
     * ready to use.
     *
     * @return the XstreamMetadataObjectBuilder
     */
    public static XstreamMetadataObjectBuilder newXstreamModelBuilder() {
        final XStream xstream = new XStream();
        xstream.alias("AttributeSet", MetadataAttributeSet.class);
        xstream.alias("Attribute", MetadataAttribute.class);
        xstream.alias("PersonName", PersonName.class);
        return new XstreamMetadataObjectBuilder(xstream);
    }

    /**
     * Creates and configures a new instance of AttributeValueBuilder.  The returned instance is initialized and
     * ready to use.
     *
     * @return the XstreamMetadataObjectBuilder
     */
    public static XStreamAttributeValueBuilder newXstreamAttributeValueBuilder() {
        final XStream xstream = new XStream();
        xstream.alias("AttributeSet", MetadataAttributeSet.class);
        xstream.alias("Attribute", MetadataAttribute.class);
        return new XStreamAttributeValueBuilder(xstream);
    }

}
