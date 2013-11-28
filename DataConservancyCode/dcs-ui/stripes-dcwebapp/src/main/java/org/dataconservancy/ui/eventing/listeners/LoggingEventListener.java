/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.ui.eventing.listeners;

import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventListener;
import org.dataconservancy.ui.exceptions.BaseUiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Logs events to the configured log level (by default, DEBUG).
 */
public class LoggingEventListener implements EventListener {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Supported log levels; these are levels that this event listener can log at.
     */
    public enum LEVELS { ERROR, WARN, INFO, DEBUG, TRACE }

    private LEVELS level = LEVELS.DEBUG;

    @Override
    public String getName() {
        return "Logging Event Listener";
    }

    /**
     * Logs the event at the specified
     * {@link #setLevel(LoggingEventListener.LEVELS) level}.
     *
     * @param eventContext the event context
     * @param event the event object
     */
    @Override
    public void handleEvent(EventContext eventContext, Event<?> event) {
        String message = generateLogMessage(eventContext, event);

        switch (level) {
            case DEBUG:
                log.debug(message);
                break;

            case TRACE:
                log.trace(message);
                break;

            case ERROR:
                log.error(message);
                break;

            case INFO:
                log.info(message);
                break;

            case WARN:
                log.warn(message);
                break;

            default:
                throw new RuntimeException("Unknown level logging " + level + " while attempting to " +
                        "handle this event:\n" + message);

        }

    }

    public LEVELS getLevel() {
        return level;
    }

    /**
     * The logging level that the event will be logged at.
     *
     * @param level the log level
     */
    public void setLevel(LEVELS level) {
        this.level = level;
    }

    private <T> String generateLogMessage(EventContext context, Event<T> event) {
        String stackTrace = null;

        if (event.getEventClass() == EventClass.EXCEPTION) {
            if (event.getEventObject() instanceof BaseUiException) {
                stackTrace = ((BaseUiException) event).asStackTraceMessage();
            } else if (event.getEventObject() instanceof Exception) {
                StringWriter writer = new StringWriter();
                ((Exception) event).printStackTrace(new PrintWriter(writer));
                stackTrace = writer.toString();
            }

            if (stackTrace == null || stackTrace.trim().length() == 0) {
                stackTrace = "No stacktrace provided";
            }

            return "Logging exception event: " + stackTrace;
        }

        if (event.getEventClass() == EventClass.AUDIT) {
            return "Logging audit event " + event.getEventTopic() + ": " + event.getEventObject();
        }

        return "Unknown event class " + event.getEventClass();
    }
}
