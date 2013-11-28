package org.dataconservancy.mhf.validators;



/**
 * Abstract class for wrappers of SAX handlers.
 */
abstract class AbstractSaxWrapper {

    /**
     * Shared logger for subclass implementations
     */
    final ValidatorLogger log = ValidatorLoggerFactory.getLogger(this.getClass());

    /**
     * A truncator for long strings
     */
    final StringTruncate truncater = new StringTruncate();

    /**
     * A flag indicating whether or not the wrapper should be verbose.  What is "verbose" is determined by the
     * implementation.
     */
    boolean verbose = false;

    AbstractSaxWrapper() {
        this.verbose = log.isVerbose();
    }

    /**
     * A flag indicating whether or not the wrapper should be verbose.  What is "verbose" is determined by the
     * implementation, but it would often mean logging more information.
     *
     * @return true if implementations are verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * A flag indicating whether or not the wrapper should be verbose.  What is "verbose" is determined by the
     * implementation, but it would often mean logging more information.
     *
     * @param verbose if implementations should be verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
