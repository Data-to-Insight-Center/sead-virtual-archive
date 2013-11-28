package org.dataconservancy.mhf.validators;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class Slf4jLoggerFactoryImpl implements ILoggerFactory {

    private MhfProperties mhfProps;

    Slf4jLoggerFactoryImpl(MhfProperties mhfProps) {
        if (mhfProps == null) {
            throw new IllegalArgumentException("MHF Properties must not be null.");
        }
        this.mhfProps = mhfProps;
    }

    @Override
    public Logger getLogger(String name) {
        Logger toWrap = LoggerFactory.getLogger(name);
        ValidatorLogger validatorLogger = new ValidatorLoggerImpl(mhfProps, toWrap);
        return validatorLogger;
    }
}
