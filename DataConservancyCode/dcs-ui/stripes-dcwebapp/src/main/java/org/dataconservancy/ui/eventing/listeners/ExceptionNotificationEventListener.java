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

import org.apache.velocity.VelocityContext;
import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventTopic;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Sends an email reporting system exceptions to the configured {@link #setEmailRecipient(java.util.List) recipients}.
 */
public class ExceptionNotificationEventListener extends NotificationServiceEventListener {

    public ExceptionNotificationEventListener(NotificationService notificationService,
                                              VelocityTemplateHelper velocityHelper) {
        super(notificationService, velocityHelper);
    }

    @Override
    protected <T> boolean canHandleEvent(EventContext eventContext, Event<T> event) {
        return event.getEventClass() == EventClass.EXCEPTION && event.getEventTopic() == EventTopic.EXCEPTION;
    }

    /**
     * In addition to the name/value pairs provided by the superclass, this implementation adds the stacktrace
     * to the velocity context bound by the name 'stackTrace'.
     *
     * @param velocityContext {@inheritDoc}
     * @param event the event {@inheritDoc}
     */
    @Override
    protected <T> void populateContext(VelocityContext velocityContext, Event<T> event) {
        super.populateContext(velocityContext, event);
        velocityContext.put("stackTrace", asStackTraceMessage((Throwable) event.getEventObject()));
    }

    /**
     * Render the stack trace of {@code t} as a String.
     *
     * @param t the Throwable
     * @return the stack trace, as a String
     */
    protected String asStackTraceMessage(Throwable t) {
        final StringWriter stringWriter = new StringWriter(2048);
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        writer.flush();
        writer.close();
        return stringWriter.toString();
    }
}
