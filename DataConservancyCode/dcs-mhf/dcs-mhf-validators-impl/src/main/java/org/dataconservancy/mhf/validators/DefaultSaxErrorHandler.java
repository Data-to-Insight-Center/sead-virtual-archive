package org.dataconservancy.mhf.validators;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.Validator;

/**
 * SAX ErrorHandler that behaves according to the default implementation documented on
 * {@link Validator#setErrorHandler(org.xml.sax.ErrorHandler)}.  It throws {@code SAXException} upon receipt of
 * a fatal error or error.  It does nothing when a warning is received.
 */
public class DefaultSaxErrorHandler implements ErrorHandler {

    /**
     * Re-throws the supplied exception.
     *
     * @param e rethrown
     * @throws SAXException
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;

    }

    /**
     * Re-throws the supplied exception.
     *
     * @param e rethrown
     * @throws SAXException
     */
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;

    }

    /**
     * A no-op; does nothing.
     *
     * @param e ignored
     * @throws SAXException
     */
    @Override
    public void warning(SAXParseException e) throws SAXException {
        // noop
    }
}
