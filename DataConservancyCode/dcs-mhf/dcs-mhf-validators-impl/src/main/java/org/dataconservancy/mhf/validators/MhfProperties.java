package org.dataconservancy.mhf.validators;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Provides programmatic access to the MHF configuration properties.
 */
class MhfProperties {

    static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.DEBUG;

    static final boolean DEFAULT_VERBOSITY = false;

    static final String DEFAULT_MHF_PROPERTIES_RESOURCE = "/org/dataconservancy/mhf/config/dcs-mhf.properties";

    private static final String KEY_LOGLEVEL = "dcs.mhf.loglevel";

    private static final String KEY_VERBOSE = "dcs.mhf.verbose";

    private Properties props;

    private LogLevel logLevel;

    private boolean verbose;

    /**
     * Attempts to load properties from the default classpath resource
     * {@code /org/dataconservancy/mhf/config/dcs-mhf.properties}.
     *
     * @throws IOException if the properties cannot be found or not loaded
     */
    MhfProperties() throws IOException {
        this(DEFAULT_MHF_PROPERTIES_RESOURCE);
    }

    /**
     * Attempts to load properties from the supplied classpath resource.
     *
     * @param propertiesResource the classpath resource identifying the MHF properties file
     * @throws IOException if the properties cannot be found or not loaded
     */
    MhfProperties(String propertiesResource) throws IOException {
        final URL propertiesLocation = this.getClass().getResource(propertiesResource);
        if (propertiesLocation != null) {
            props = new Properties();
            props.load(propertiesLocation.openStream());

            if (props.containsKey(KEY_LOGLEVEL) && props.getProperty(KEY_LOGLEVEL) != null) {
                logLevel = LogLevel.valueOf(props.getProperty(KEY_LOGLEVEL).trim().toUpperCase());
            } else {
                logLevel = DEFAULT_LOG_LEVEL;
            }

            if (props.containsKey(KEY_VERBOSE) && props.getProperty(KEY_VERBOSE) != null) {
                verbose = Boolean.parseBoolean(props.getProperty(KEY_VERBOSE).trim());
            } else {
                verbose = DEFAULT_VERBOSITY;
            }
        }
    }

    /**
     * The log level in use.
     *
     * @return
     */
    LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * If the framework should be verbose in its logging.
     * 
     * @return
     */
    boolean isVerbose() {
        return verbose;
    }
}
