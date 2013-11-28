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

import org.dataconservancy.ui.BaseUnitTest;
import org.dataconservancy.ui.test.support.BaseSpringAwareTest;
import org.dataconservancy.ui.eventing.api.DefaultEventManager;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.ExceptionEvent;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fires an event, and the expected result is that an email is sent (assuming that the EmailService is properly
 * configured and enabled).
 */
public class NotificationServiceEventListenerTest extends BaseUnitTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VelocityTemplateHelper velocityTemplateHelper;

    @Test
    public void testHandleEvent() throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final DefaultEventManager eventManager = new DefaultEventManager(executor);

        eventManager.addListener(new ExceptionNotificationEventListener(notificationService, velocityTemplateHelper));
        final EventContext eventContext = new EventContext();
        eventManager.fire(eventContext, new ExceptionEvent(eventContext, new Throwable("Hello world!")));

        // Block until the event has been handled.
        executor.shutdown();
    }
}
