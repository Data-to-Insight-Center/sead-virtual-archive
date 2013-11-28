package org.dataconservancy.mhf.validators;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Implementation of ValidatorLogger which parameterizes the logging level.
 */
class ValidatorLoggerImpl implements ValidatorLogger {

    private final Logger wrapped;
    private final MhfProperties mhfProperties;
    private final LogLevel logLevel;
    private final boolean verbose;
    private final String UNKNOWN_LOG_LEVEL = "Unknown LogLevel ";

    ValidatorLoggerImpl(MhfProperties mhfProperties, Logger wrapped) {
        if (mhfProperties == null) {
            throw new IllegalArgumentException("MHF properties must not be null.");
        }

        if (wrapped == null) {
            throw new IllegalArgumentException("The logger being wrapped must not be null");
        }

        this.mhfProperties = mhfProperties;
        this.wrapped = wrapped;
        this.logLevel = mhfProperties.getLogLevel();
        this.verbose = mhfProperties.isVerbose();
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void log(String format, Object arg1, Object arg2) {
        log(logLevel, format, arg1, arg2);
    }

    @Override
    public void log(String format, Object arg) {
        log(logLevel, format, arg);
    }

    @Override
    public void log(String format, Object[] argArray) {
        log(logLevel, format, argArray);
    }

    @Override
    public void log(Marker marker, String format, Object arg1, Object arg2) {
        log(logLevel, marker, format, arg1, arg2);
    }

    @Override
    public void log(Marker marker, String format, Object arg) {
        log(logLevel, marker, format, arg);
    }

    @Override
    public void log(Marker marker, String format, Object[] argArray) {
        log(logLevel, marker, format, argArray);
    }

    @Override
    public void log(Marker marker, String msg) {
        log(logLevel, marker, msg);
    }

    @Override
    public void log(Marker marker, String msg, Throwable t) {
        log(logLevel, marker, msg, t);
    }

    @Override
    public void log(String msg) {
        log(logLevel, msg);
    }

    @Override
    public void log(String msg, Throwable t) {
        log(logLevel, msg, t);
    }

    @Override
    public void log(LogLevel level, String format, Object arg1, Object arg2) {
        switch (level) {
            case DEBUG:
                debug(format, arg1, arg2);
                return;
            case ERROR:
                error(format, arg1, arg2);
                return;
            case INFO:
                info(format, arg1, arg2);
                return;
            case TRACE:
                trace(format, arg1, arg2);
                return;
            case WARN:
                warn(format, arg1, arg2);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, String format, Object arg) {
        switch (level) {
            case DEBUG:
                debug(format, arg);
                return;
            case ERROR:
                error(format, arg);
                return;
            case INFO:
                info(format, arg);
                return;
            case TRACE:
                trace(format, arg);
                return;
            case WARN:
                warn(format, arg);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, String format, Object[] argArray) {
        switch (level) {
            case DEBUG:
                debug(format, argArray);
                return;
            case ERROR:
                error(format, argArray);
                return;
            case INFO:
                info(format, argArray);
                return;
            case TRACE:
                trace(format, argArray);
                return;
            case WARN:
                warn(format, argArray);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, Marker marker, String format, Object arg1, Object arg2) {
        switch (level) {
            case DEBUG:
                debug(marker, format, arg1, arg2);
                return;
            case ERROR:
                error(marker, format, arg1, arg2);
                return;
            case INFO:
                info(marker, format, arg1, arg2);
                return;
            case TRACE:
                trace(marker, format, arg1, arg2);
                return;
            case WARN:
                warn(marker, format, arg1, arg2);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, Marker marker, String format, Object arg) {
        switch (level) {
            case DEBUG:
                debug(marker, format, arg);
                return;
            case ERROR:
                error(marker, format, arg);
                return;
            case INFO:
                info(marker, format, arg);
                return;
            case TRACE:
                trace(marker, format, arg);
                return;
            case WARN:
                warn(marker, format, arg);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, Marker marker, String format, Object[] argArray) {
        switch (level) {
            case DEBUG:
                debug(marker, format, argArray);
                return;
            case ERROR:
                error(marker, format, argArray);
                return;
            case INFO:
                info(marker, format, argArray);
                return;
            case TRACE:
                trace(marker, format, argArray);
                return;
            case WARN:
                warn(marker, format, argArray);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, Marker marker, String msg) {
        switch (level) {
            case DEBUG:
                debug(marker, msg);
                return;
            case ERROR:
                error(marker, msg);
                return;
            case INFO:
                info(marker, msg);
                return;
            case TRACE:
                trace(marker, msg);
                return;
            case WARN:
                warn(marker, msg);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, Marker marker, String msg, Throwable t) {
        switch (level) {
            case DEBUG:
                debug(marker, msg, t);
                return;
            case ERROR:
                error(marker, msg, t);
                return;
            case INFO:
                info(marker, msg, t);
                return;
            case TRACE:
                trace(marker, msg, t);
                return;
            case WARN:
                warn(marker, msg, t);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, String msg) {
        switch (level) {
            case DEBUG:
                debug(msg);
                return;
            case ERROR:
                error(msg);
                return;
            case INFO:
                info(msg);
                return;
            case TRACE:
                trace(msg);
                return;
            case WARN:
                warn(msg);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public void log(LogLevel level, String msg, Throwable t) {
        switch (level) {
            case DEBUG:
                debug(msg, t);
                return;
            case ERROR:
                error(msg, t);
                return;
            case INFO:
                info(msg, t);
                return;
            case TRACE:
                trace(msg, t);
                return;
            case WARN:
                warn(msg, t);
                return;
            default:
                throw new RuntimeException(UNKNOWN_LOG_LEVEL + level);
        }
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return wrapped.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        wrapped.trace(msg);

    }

    @Override
    public void trace(String format, Object arg) {
        wrapped.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        wrapped.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        wrapped.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        wrapped.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return wrapped.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        wrapped.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        wrapped.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        wrapped.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object[] argArray) {
        wrapped.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        wrapped.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return wrapped.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        wrapped.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        wrapped.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        wrapped.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        wrapped.debug(format, argArray);

    }

    @Override
    public void debug(String msg, Throwable t) {
        wrapped.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return wrapped.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        wrapped.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        wrapped.debug(marker, format, arg);

    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        wrapped.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object[] argArray) {
        wrapped.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        wrapped.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return wrapped.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        wrapped.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        wrapped.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        wrapped.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        wrapped.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        wrapped.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return wrapped.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        wrapped.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        wrapped.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        wrapped.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object[] argArray) {
        wrapped.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        wrapped.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return wrapped.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        wrapped.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        wrapped.warn(format, arg);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        wrapped.warn(format, argArray);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        wrapped.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        wrapped.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return wrapped.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        wrapped.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        wrapped.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        wrapped.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object[] argArray) {
        wrapped.warn(marker, format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        wrapped.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return wrapped.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        wrapped.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        wrapped.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        wrapped.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        wrapped.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        wrapped.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return wrapped.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        wrapped.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        wrapped.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        wrapped.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object[] argArray) {
        wrapped.error(marker, format, argArray);

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        wrapped.error(marker, msg, t);
    }
}
