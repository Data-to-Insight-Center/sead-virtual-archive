package org.dataconservancy.mhf.validators;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Wrapper for SAX ContentHandlers.
 */
abstract class ContentHandlerWrapper extends AbstractSaxWrapper implements ContentHandler {

    private final ContentHandler wrapped;

    ContentHandlerWrapper(ContentHandler toWrap) {
        if (toWrap == null) {
            throw new IllegalArgumentException("The ContentHandler to wrap must not be null.");
        }
        this.wrapped = toWrap;
    }

    @Override
    public final void characters(char[] chars, int start, int length) throws SAXException {
        try {
            charactersInternal(chars, start, length);
        } catch (Exception e) {
            // ignore
        }

        wrapped.characters(chars, start, length);
    }

    void charactersInternal(char[] chars, int start, int length) {

    }

    @Override
    public final void setDocumentLocator(Locator locator) {
        try {
            setDocumentLocatorInternal(locator);
        } catch (Exception e) {
            // ignore
        }

        wrapped.setDocumentLocator(locator);
    }

    void setDocumentLocatorInternal(Locator locator) {

    }

    @Override
    public final void startDocument() throws SAXException {
        try {
            startDocumentInternal();
        } catch (Exception e) {
            // ignore
        }

        wrapped.startDocument();
    }

    void startDocumentInternal() {

    }

    @Override
    public final void endDocument() throws SAXException {
        try {
            endDocumentInternal();
        } catch (Exception e) {
            // ignore
        }

        wrapped.endDocument();
    }

    void endDocumentInternal() {

    }

    @Override
    public final void startPrefixMapping(String prefix, String uri) throws SAXException {
        try {
            startPrefixMappingInternal(prefix, uri);
        } catch (Exception e) {
            // ignore
        }

        wrapped.startPrefixMapping(prefix, uri);
    }

    void startPrefixMappingInternal(String prefix, String uri) {

    }

    @Override
    public final void endPrefixMapping(String prefix) throws SAXException {
        try {
            endPrefixMappingInternal(prefix);
        } catch (Exception e) {
            // ignore
        }

        wrapped.endPrefixMapping(prefix);
    }

    void endPrefixMappingInternal(String prefix) {

    }

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        try {
            startElementInternal(uri, localName, qName, attrs);
        } catch (Exception e) {
            // ignore
        }

        wrapped.startElement(uri, localName, qName, attrs);
    }

    void startElementInternal(String uri, String localName, String qName, Attributes attrs) {

    }

    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            endElementInternal(uri, localName, qName);
        } catch (Exception e) {
            // ignore
        }

        wrapped.endElement(uri, localName, qName);
    }

    void endElementInternal(String uri, String localName, String qName) {

    }

    @Override
    public final void ignorableWhitespace(char[] chars, int start, int length) throws SAXException {
        try {
            ignorableWhitespaceInternal(chars, start, length);
        } catch (Exception e) {
            // ignore
        }

        wrapped.ignorableWhitespace(chars, start, length);
    }

    void ignorableWhitespaceInternal(char[] chars, int start, int length) {

    }

    @Override
    public final void processingInstruction(String target, String data) throws SAXException {
        try {
            processingInstructionInternal(target, data);
        } catch (Exception e) {
            // ignore
        }

        wrapped.processingInstruction(target, data);
    }

    void processingInstructionInternal(String target, String data) {

    }

    @Override
    public final void skippedEntity(String name) throws SAXException {
        try {
            skippedEntityInternal(name);
        } catch (Exception e) {
            // ignore
        }

        wrapped.skippedEntity(name);
    }

    void skippedEntityInternal(String name) {

    }
}
