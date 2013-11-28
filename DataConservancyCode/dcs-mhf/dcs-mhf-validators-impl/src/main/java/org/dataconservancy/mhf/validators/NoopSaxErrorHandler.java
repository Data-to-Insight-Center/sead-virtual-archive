package org.dataconservancy.mhf.validators;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A SAX ErrorHandler which does nothing.
 */
public class NoopSaxErrorHandler implements ErrorHandler {

    @Override
    public void error(SAXParseException e) throws SAXException {
        // Default method body
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        // Default method body
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        // Default method body
    }

}
