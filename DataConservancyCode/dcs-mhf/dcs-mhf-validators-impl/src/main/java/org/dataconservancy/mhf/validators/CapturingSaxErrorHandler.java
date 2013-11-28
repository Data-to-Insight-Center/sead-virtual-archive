package org.dataconservancy.mhf.validators;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * A SAX error handler which captures and stores errors internally.  It relies on the caller to interrogate the stored
 * events to determine whether or not to throw SAXExceptions.
 */
public class CapturingSaxErrorHandler implements ErrorHandler {

    public enum SEVERITY {
        WARNING,
        ERROR,
        FATAL
    }

    public static class SaxError {
        final SAXParseException exception;
        final SEVERITY severity;

        public SaxError(SAXParseException exception, SEVERITY severity) {
            this.exception = exception;
            this.severity = severity;
        }

    }

    private final List<SaxError> errors = new ArrayList<SaxError>();

    @Override
    public void error(SAXParseException e) throws SAXException {
        errors.add(new SaxError(e, SEVERITY.ERROR));
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        errors.add(new SaxError(e, SEVERITY.WARNING));
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        errors.add(new SaxError(e, SEVERITY.FATAL));
    }

    public List<SaxError> getErrors() {
        return errors;
    }
}
