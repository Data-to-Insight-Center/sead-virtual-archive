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
package org.dataconservancy.ui.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.exceptions.BaseUiException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class VelocityTemplateHelperTest {

    private VelocityTemplateHelper underTest;

    @Before
    public void configureEngine() {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("resource.loader", "class");
        engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.init();

        underTest = new VelocityTemplateHelper(engine);
    }

    @Test
    public void testExecuteSimple() throws Exception {
        EventContext eventContext = new EventContext();
        Object event = new BaseUiException();
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("eventContext", eventContext);
        velocityContext.put("event", event);

        String message = underTest.execute("/templates/email-exception-template.vm", velocityContext);
        assertNotNull(message);
    }

    @Test
    public void testMessage() throws Exception {
        final String user = "emetsger@gmail.com";
        final String actionBean = "ProjectActionBean.class";
        final EventClass eventClass = EventClass.EXCEPTION;
        final String requestUri = "/project/1";
        final String hostName = "localhost";
        final String originIp = "127.0.0.1";
        final String buildDate = "2012-06-26T10:57:45.351-04:00";
        final String eventDate = DateTime.now().toString(DateTimeFormat.fullDateTime());
        final String buildNumber = "850";
        final String revisionNumber = "3918";
        final String causeMessage = "this is the cause";
        final String exceptionMessage = "this is the exception";

        EventContext eventContext = new EventContext();
        eventContext.setUser(user);
        eventContext.setActionBean(actionBean);
        eventContext.setEventClass(eventClass);
        eventContext.setRequestUri(requestUri);
        eventContext.setHostName(hostName);
        eventContext.setOriginIp(originIp);
        eventContext.setBuildDate(buildDate);
        eventContext.setBuildNumber(buildNumber);
        eventContext.setRevisionNumber(revisionNumber);
        eventContext.setEventDate(eventDate);

        RuntimeException cause = new RuntimeException(causeMessage);
        Object event = new BaseUiException(exceptionMessage, cause);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("eventContext", eventContext);
        velocityContext.put("event", event);
        velocityContext.put("stackTrace", ((BaseUiException) event).asStackTraceMessage());

        String message = underTest.execute("/templates/email-exception-template.vm", velocityContext);
        assertNotNull(message);

        // TODO: is this an OK way to test this?
        assertTrue("Unexpected error message:\n" + message,
                message.startsWith("An exceptional event occurred, and we thought you should know.\n" +
                        "\n" +
                        "The event context was:\n" +
                        "   User: " + user + "\n" +
                        "   Request URI: " + requestUri + "\n" +
                        "   Request IP: " + originIp + "\n" +
                        "   Server Host Name: " + hostName + "\n" +
                        "   Action Bean: " + actionBean + "\n" +
                        "   Event Type: " + eventClass + "\n" +
                        "   Event Date: " + eventDate + "\n" +
                        "   Version: " + buildNumber + " (" + revisionNumber + ", " + buildDate + ")\n" +
                        "\n" +
                        "The exception was:\n" +
                        "   org.dataconservancy.ui.exceptions.BaseUiException: " + exceptionMessage));
    }




    
}
