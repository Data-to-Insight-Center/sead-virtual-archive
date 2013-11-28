package org.dataconservancy.mhf.validators;

import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;

/**
 * Wrapper for SAX DTDHandlers.
 */
abstract class DTDHandlerWrapper extends AbstractSaxWrapper implements DTDHandler {

    private final DTDHandler wrapped;

    DTDHandlerWrapper(DTDHandler toWrap) {
        if (toWrap == null) {
            throw new IllegalArgumentException("The DTDHandler to wrap must not be null.");
        }
        this.wrapped = toWrap;
    }

    @Override
    public final void notationDecl(String name, String publicId, String systemId) throws SAXException {
        try {
            notationDeclInternal(name, publicId, systemId);
        } catch (Exception e) {
            // ignore
        }

        wrapped.notationDecl(name, publicId, systemId);
    }

    void notationDeclInternal(String name, String publicId, String systemId) {

    }

    @Override
    public final void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
            throws SAXException {
        try {
            unparsedEntityDeclInternal(name, publicId, systemId, notationName);
        } catch (Exception e) {
            // ignore
        }

        wrapped.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    void unparsedEntityDeclInternal(String name, String publicId, String systemId, String notationName) {

    }
}
