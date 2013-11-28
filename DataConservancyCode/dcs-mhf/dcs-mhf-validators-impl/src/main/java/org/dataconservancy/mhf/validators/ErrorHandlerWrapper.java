package org.dataconservancy.mhf.validators;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Wrapper for SAX ErrorHandlers. Allows subclasses to decorate {@link ErrorHandler} methods, without influencing the
 * results of the wrapped {@code ErrorHandler}.
 * <p/>
 * Example wrapper implementations include emitting log messages or producing events for invoked methods.  This wrapper
 * implementation makes it impossible for subclasses to modify the behavior (including thrown exceptions) of the
 * wrapped {@code ErrorHandler}.
 */
public abstract class ErrorHandlerWrapper extends AbstractSaxWrapper implements ErrorHandler {


    /**
     * The wrapped ErrorHandler, which itself may be an instance of {@code ErrorHandlerWrapper}
     */
    private final ErrorHandler wrapped;

    /**
     * Constructs a new instance, wrapping the supplied {@code ErrorHandler}.
     *
     * @param toWrap the {@code ErrorHandler} being wrapped, must not be {@code null}
     * @throws IllegalArgumentException if {@code toWrap} is {@code nul}
     */
    public ErrorHandlerWrapper(ErrorHandler toWrap) {
        if (toWrap == null) {
            throw new IllegalArgumentException("ErrorHandler to wrap must not be null.");
        }
        this.wrapped = toWrap;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note:<br/>
     * Invokes {@link #handleErrorInternal(org.xml.sax.SAXParseException)}, then invokes
     * {@link #error(org.xml.sax.SAXParseException)} on the wrapped instance.  This method is declared final so that
     * subclasses may not override this behavior.
     *
     * @param e {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    final public void error(SAXParseException e) throws SAXException {
        try {
            handleErrorInternal(e);
        } catch (Exception ex) {
            // ignore
        }

        wrapped.error(e);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note:<br/>
     * Invokes {@link #handleErrorInternal(org.xml.sax.SAXParseException)}, then invokes
     * {@link #warning(org.xml.sax.SAXParseException)} on the wrapped instance.  This method is declared final so that
     * subclasses may not override this behavior.
     *
     * @param e {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    final public void warning(SAXParseException e) throws SAXException {
        try {
            handleWarningInternal(e);
        } catch (Exception ex) {
            // ignore
        }

        wrapped.warning(e);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Implementation note:<br/>
     * Invokes {@link #handleErrorInternal(org.xml.sax.SAXParseException)}, then invokes
     * {@link #fatalError(org.xml.sax.SAXParseException)} on the wrapped instance.  This method is declared final so that
     * subclasses may not override this behavior.
     *
     * @param e {@inheritDoc}
     * @throws SAXException {@inheritDoc}
     */
    @Override
    final public void fatalError(SAXParseException e) throws SAXException {
        try {
            handleFatalErrorInternal(e);
        } catch (Exception ex) {
            // ignore
        }
        wrapped.fatalError(e);
    }

    /**
     * Subclasses must implement this method, decorating the {@link #error(org.xml.sax.SAXParseException)} method as
     * desired.  This will be called prior to calling {@code error(SAXParseException)} on the wrapped instance.  This
     * method may throw exceptions, but they will be ignored.
     *
     * @param e the SAXParseException
     */
    abstract void handleErrorInternal(SAXParseException e);

    /**
     * Subclasses must implement this method, decorating the {@link #warning(org.xml.sax.SAXParseException)} method as
     * desired.  This will be called prior to calling {@code warning(SAXParseException)} on the wrapped instance.  This
     * method may throw exceptions, but they will be ignored.
     *
     * @param e the SAXParseException
     */
    abstract void handleWarningInternal(SAXParseException e);

    /**
     * Subclasses must implement this method, decorating the {@link #fatalError(org.xml.sax.SAXParseException)} method
     * as desired.  This will be called prior to calling {@code fatalError(SAXParseException)} on the wrapped instance.
     * This method may throw exceptions, but they will be ignored.
     *
     * @param e the SAXParseException
     */
    abstract void handleFatalErrorInternal(SAXParseException e);

}
