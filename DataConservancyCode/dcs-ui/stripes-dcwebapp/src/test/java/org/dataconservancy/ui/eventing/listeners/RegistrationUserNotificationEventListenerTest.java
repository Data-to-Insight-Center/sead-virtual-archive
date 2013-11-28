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
import org.dataconservancy.ui.BaseUnitTest;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.NewUserApprovalEvent;
import org.dataconservancy.ui.eventing.events.NewUserRegistrationEvent;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.dataconservancy.ui.util.UiBaseUrlConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jrm
 * Date: 1/18/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationUserNotificationEventListenerTest extends BaseUnitTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VelocityTemplateHelper velocityTemplateHelper;

    @Autowired
    private UiBaseUrlConfig dcsUiBaseUrlConfig;

    private final static String INSTANCE_NAME_KEY = "dataConservancyInstance.name";
    private static String instanceNameValue;

    @BeforeClass
    public static void beforeClass() {
        ResourceBundle stripesMessages = ResourceBundle.getBundle("StripesResources");
        assertNotNull("Error retrieving Stripes Resource Bundle", stripesMessages);
        instanceNameValue = stripesMessages.getString(INSTANCE_NAME_KEY);
        assertNotNull("Missing value for " + INSTANCE_NAME_KEY, instanceNameValue);
    }


    /**
     * Make sure registration events are handled, but approval events are ignored.
     */
    @Test
    public void testCanHandleEvent() {
        RegistrationUserNotificationEventListener listener = new RegistrationUserNotificationEventListener(notificationService, velocityTemplateHelper, dcsUiBaseUrlConfig);

        EventContext context = new EventContext();

        assertFalse(listener.canHandleEvent(context, new NewUserApprovalEvent(context, user)));
        assertTrue(listener.canHandleEvent(context, new NewUserRegistrationEvent(context, user)));
    }

    /**
     * Make sure context has person, instanceHomeLink and instanceName set.
     */
    @Test
    public void testPopulateContext() {
        RegistrationUserNotificationEventListener listener = new RegistrationUserNotificationEventListener(notificationService, velocityTemplateHelper, dcsUiBaseUrlConfig);

        EventContext context = new EventContext();

        VelocityContext velocityContext = new VelocityContext();
        listener.populateContext(velocityContext, new NewUserApprovalEvent(context, user));

        assertTrue(velocityContext.containsKey("person"));
        assertTrue(velocityContext.containsKey("instanceHomeLink"));
        assertTrue(velocityContext.containsKey("instanceName"));

        assertEquals(user, velocityContext.get("person"));
        assertEquals(instanceNameValue, velocityContext.get("instanceName"));
    }

    /**
     * Make sure email recipient is user.
     */
    @Test
    public void testBuildNotification() {
        RegistrationUserNotificationEventListener listener = new RegistrationUserNotificationEventListener(notificationService, velocityTemplateHelper, dcsUiBaseUrlConfig);

        EventContext context = new EventContext();
        NewUserApprovalEvent event = new NewUserApprovalEvent(context, user);
        String message = "message";
        String sender = "sender";
        String subject = "subject";
        List<String> recipients = new ArrayList<String>();

        listener.buildNotification(event, recipients, sender, subject, message);

        assertEquals(1, recipients.size());

        assertEquals(user.getEmailAddress(), recipients.get(0));
    }

}
