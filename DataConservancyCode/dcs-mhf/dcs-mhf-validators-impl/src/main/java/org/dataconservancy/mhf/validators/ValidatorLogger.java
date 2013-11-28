package org.dataconservancy.mhf.validators;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Logging facade that parameterizes the logging level instead of hard-coding the log level in the method call.
 */
public interface ValidatorLogger extends Logger {

    /**
     * The default log level used when callers invoke the un-parameterized {@code log(...)} methods.
     *
     * @return
     */
    LogLevel getLogLevel();

    /**
     * Whether or not callers should be verbose when calling this interface.
     *
     * @return
     */
    boolean isVerbose();

    void log(String format, Object arg1, Object arg2);

    void log(String format, Object arg);

    void log(String format, Object[] argArray);

    void log(Marker marker, String format, Object arg1, Object arg2);

    void log(Marker marker, String format, Object arg);

    void log(Marker marker, String format, Object[] argArray);

    void log(Marker marker, String msg);

    void log(Marker marker, String msg, Throwable t);

    void log(String msg);

    void log(String msg, Throwable t);

    void log(LogLevel level, String format, Object arg1, Object arg2);

    void log(LogLevel level, String format, Object arg);

    void log(LogLevel level, String format, Object[] argArray);

    void log(LogLevel level, Marker marker, String format, Object arg1, Object arg2);

    void log(LogLevel level, Marker marker, String format, Object arg);

    void log(LogLevel level, Marker marker, String format, Object[] argArray);

    void log(LogLevel level, Marker marker, String msg);

    void log(LogLevel level, Marker marker, String msg, Throwable t);

    void log(LogLevel level, String msg);

    void log(LogLevel level, String msg, Throwable t);

}
