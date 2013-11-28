package org.dataconservancy.mhf.validators;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import javax.xml.XMLConstants;
import java.util.Set;

/**
 * Records the value of {@code schemaLocation} attributes from XSD instance document's {@code &lt;import>} elements.
 */
class XsdImportCollectorContentHandlerWrapper extends ContentHandlerWrapper {

    private static final String E_INCLUDE = "include";

    private static final String A_SCHEMALOC = "schemaLocation";

    private Set<String> schemaLocations;

    XsdImportCollectorContentHandlerWrapper(ContentHandler toWrap, Set<String> schemaLocations) {
        super(toWrap);
        this.schemaLocations = schemaLocations;
    }

    @Override
    void startElementInternal(String uri, String localName, String qName, Attributes attrs) {
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(uri) &&
                E_INCLUDE.equals(localName) && attrs != null && attrs.getLength() > 0) {
            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                if (A_SCHEMALOC.equals(attrs.getLocalName(i))) {
                    schemaLocations.add(attrs.getValue(i));
                }
            }
        }
    }
}
