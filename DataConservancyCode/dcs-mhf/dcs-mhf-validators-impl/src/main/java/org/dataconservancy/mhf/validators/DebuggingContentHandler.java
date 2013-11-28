package org.dataconservancy.mhf.validators;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

/**
 *
 */
class DebuggingContentHandler extends ContentHandlerWrapper {

    private boolean debugCharsInternal = true;

    DebuggingContentHandler(ContentHandler toWrap) {
        super(toWrap);
    }

    public boolean isDebugCharsInternal() {
        return debugCharsInternal;
    }

    public void setDebugCharsInternal(boolean debugCharsInternal) {
        this.debugCharsInternal = debugCharsInternal;
    }

    @Override
    void charactersInternal(char[] chars, int start, int length) {
        if (debugCharsInternal) {
            log.log("charactersInternal: chars '{}' start '{}' length '{}'",
                    new Object[] { truncater.truncate(String.valueOf(chars)), start, length });
        }
    }

    @Override
    void endDocumentInternal() {
        log.log("endDocumentInternal");
    }

    @Override
    void endElementInternal(String uri, String localName, String qName) {
        log.log("endElementInternal: uri '{}' localName '{}' qName '{}'",
                new Object[] { uri, localName, qName });
    }

    @Override
    void endPrefixMappingInternal(String prefix) {
        log.log("endPrefixMappingInternal: prefix '{}'", prefix);
    }

    @Override
    void ignorableWhitespaceInternal(char[] chars, int start, int length) {
        log.log("ignorableWhitespaceInternal: chars '{}' start '{}' length '{}'",
                new Object[] { truncater.truncate(String.valueOf(chars)), start, length });
    }

    @Override
    void processingInstructionInternal(String target, String data) {
        log.log("processingInstructionInternal: target '{}' data '{}'", target, data);
    }

    @Override
    void setDocumentLocatorInternal(Locator locator) {
        log.log("setDocumentLocatorInternal: locator '{}'", locator);
    }

    @Override
    void skippedEntityInternal(String name) {
        log.log("skippedEntityInternal: name '{}'", name);
    }

    @Override
    void startDocumentInternal() {
        log.log("startDocumentInternal");
    }

    @Override
    void startElementInternal(String uri, String localName, String qName, Attributes attrs) {
        log.log("startElementInternal: uri '{}' localName '{}' qName '{}' attrs '{}'",
                new Object[] { uri, localName, qName, new PrettyPrintAttributes(attrs) });
    }

    @Override
    void startPrefixMappingInternal(String prefix, String uri) {
        log.log("startPrefixMappingInternal: prefix '{}' uri '{}'", prefix, uri);
    }

    private class PrettyPrintAttributes {
        private Attributes attrs;

        private PrettyPrintAttributes(Attributes attrs) {
            this.attrs = attrs;
        }

        @Override
        public String toString() {
            if (attrs != null && attrs.getLength() > 0) {
                StringBuilder sb = new StringBuilder("Attributes: ");
                int i = attrs.getLength();
                for (int m = 0; m < i; m++) {
                    String uri = attrs.getURI(m);
                    String localName = attrs.getLocalName(m);
                    String qName = attrs.getQName(m);
                    String type = attrs.getType(m);
                    String value = attrs.getValue(m);
                    sb.append("[").append(m).append("] uri '").append(uri).append("' localName '")
                            .append(localName).append("' qName '").append(qName).append("' type '").append(type)
                            .append("' value '").append(value).append("'");
                    if (m + 1 < i) {
                        sb.append(" ");
                    }
                }

                return sb.toString();
            }

            return "no attributes";
        }
    }

}
