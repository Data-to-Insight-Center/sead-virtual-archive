package org.dataconservancy.mhf.validators;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Decorates a SAX ErrorHandler by emitting debugging statements when ErrorHandler methods are called.
 */
public class DebuggingErrorHandler extends ErrorHandlerWrapper {

    public DebuggingErrorHandler(ErrorHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handleErrorInternal(SAXParseException e) {
        log.log("PARSE ERROR: {}", e.getMessage());

    }

    @Override
    public void handleWarningInternal(SAXParseException e) {
        log.log("PARSE WARNING: {}", e.getMessage());
    }

    @Override
    public void handleFatalErrorInternal(SAXParseException e) {
        log.log("FATAL PARSING ERROR: {}", e.getMessage());
    }

}
