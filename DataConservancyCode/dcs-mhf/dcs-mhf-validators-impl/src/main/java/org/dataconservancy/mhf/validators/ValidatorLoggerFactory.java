package org.dataconservancy.mhf.validators;

import java.io.IOException;

/**
 * Logging factory which produces {@code ValidatorLogger} instances.
 */
public class ValidatorLoggerFactory {

    private static final Object lock = new Object();

    private static Slf4jLoggerFactoryImpl validatorLoggingFactory;

    private ValidatorLoggerFactory() {
        try {
            this.validatorLoggingFactory = new Slf4jLoggerFactoryImpl(new MhfProperties());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static ValidatorLogger getLogger(String name) {
        synchronized (lock) {
            if (validatorLoggingFactory == null) {
                new ValidatorLoggerFactory();
            }

            return (ValidatorLogger) validatorLoggingFactory.getLogger(name);
        }
    }

    public static ValidatorLogger getLogger(Class clazz) {
        synchronized (lock) {
            if (validatorLoggingFactory == null) {
                new ValidatorLoggerFactory();
            }

            return (ValidatorLogger) validatorLoggingFactory.getLogger(clazz.getName());
        }
    }
}
