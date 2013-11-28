package org.dataconservancy.mhf.validators;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

/**
 *
 */
class DebuggingDefaultHandler extends DefaultHandler {

    private static final String MISSING_WRAPPER = "%s wrapper implementation was null, so %s wasn't invoked.";

    ValidatorLogger log = ValidatorLoggerFactory.getLogger(this.getClass());

    private boolean verbose = false;

    private ContentHandler contentHandlerWrapper;

    private ErrorHandler errorHandlerWrapper;

    private DTDHandler dtdHandlerWrapper;

    private EntityResolver entityResolverWrapper;

    DebuggingDefaultHandler(ContentHandler contentHandlerWrapper) {
        this.contentHandlerWrapper = new DebuggingContentHandler(contentHandlerWrapper);
        this.errorHandlerWrapper = null;
        this.dtdHandlerWrapper = null;
        this.entityResolverWrapper = null;
    }

    DebuggingDefaultHandler(ContentHandler contentHandlerWrapper, DTDHandler dtdHandlerWrapper,
                            EntityResolver entityResolverWrapper, ErrorHandler errorHandlerWrapper) {
        this.contentHandlerWrapper = contentHandlerWrapper;
        this.dtdHandlerWrapper = dtdHandlerWrapper;
        this.entityResolverWrapper = entityResolverWrapper;
        this.errorHandlerWrapper = errorHandlerWrapper;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        if (this.contentHandlerWrapper instanceof DebuggingContentHandler) {
            ((DebuggingContentHandler) contentHandlerWrapper).setDebugCharsInternal(verbose);
        }
    }

    @Override
    public void characters(char[] chars, int i, int i1) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.characters(chars, i, i1);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "characters(...)");
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.endDocument();
        } else {
            logMissingMapper(ContentHandler.class.getName(), "endDocument()");
        }
    }

    @Override
    public void endElement(String s, String s1, String s2) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.endElement(s, s1, s2);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "endElements(...)");
        }
    }

    @Override
    public void endPrefixMapping(String s) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.endPrefixMapping(s);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "endPrefixMapping(...)");
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        if (errorHandlerWrapper != null) {
            errorHandlerWrapper.error(e);
        } else {
            logMissingMapper(ErrorHandler.class.getName(), "error(...)");
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        if (errorHandlerWrapper != null) {
            errorHandlerWrapper.fatalError(e);
        } else {
            logMissingMapper(ErrorHandler.class.getName(), "fatalError(...)");
        }
    }

    @Override
    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.ignorableWhitespace(chars, i, i1);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "ignorableWhitespace(...)");
        }
    }

    @Override
    public void notationDecl(String s, String s1, String s2) throws SAXException {
        if (dtdHandlerWrapper != null) {
            dtdHandlerWrapper.notationDecl(s, s1, s2);
        } else {
            logMissingMapper(DTDHandler.class.getName(), "notationDecl(...)");
        }
    }

    @Override
    public void processingInstruction(String s, String s1) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.processingInstruction(s, s1);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "processingInstruction(...)");
        }
    }

    @Override
    public InputSource resolveEntity(String s, String s1) throws IOException, SAXException {
        if (entityResolverWrapper != null) {
            return entityResolverWrapper.resolveEntity(s, s1);
        } else {
            logMissingMapper(EntityResolver.class.getName(), "resolveEntity(...)");
        }

        return null;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.setDocumentLocator(locator);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "setDocumentLocator(...)");
        }
    }

    @Override
    public void skippedEntity(String s) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.skippedEntity(s);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "skippedEntity(...)");
        }
    }

    @Override
    public void startDocument() throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.startDocument();
        } else {
            logMissingMapper(ContentHandler.class.getName(), "startDocument()");
        }
    }

    @Override
    public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.startElement(s, s1, s2, attributes);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "startElement(...)");
        }
    }

    @Override
    public void startPrefixMapping(String s, String s1) throws SAXException {
        if (contentHandlerWrapper != null) {
            contentHandlerWrapper.startPrefixMapping(s, s1);
        } else {
            logMissingMapper(ContentHandler.class.getName(), "startPrefixMapping(...)");
        }
    }

    @Override
    public void unparsedEntityDecl(String s, String s1, String s2, String s3) throws SAXException {
        if (dtdHandlerWrapper != null) {
            dtdHandlerWrapper.unparsedEntityDecl(s, s1, s2, s3);
        } else {
            logMissingMapper(DTDHandler.class.getName(), "unparsedEntityDecl(...)");
        }
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        if (errorHandlerWrapper != null) {
            errorHandlerWrapper.warning(e);
        } else {
            logMissingMapper(ErrorHandler.class.getName(), "warning(...)");
        }
    }

    private void logMissingMapper(String className, String methodName) {
        if (verbose) {
            log.log(String.format(MISSING_WRAPPER, className, methodName));
        }
    }
}
