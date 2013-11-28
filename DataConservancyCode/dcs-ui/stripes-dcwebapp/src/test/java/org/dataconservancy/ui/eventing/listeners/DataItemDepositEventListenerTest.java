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

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

import org.dataconservancy.ui.DirtiesContextBaseUnitTest;
import org.junit.Test;

import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.DataItemDepositEvent;
import org.dataconservancy.ui.eventing.events.NewUserApprovalEvent;
import org.dataconservancy.ui.eventing.events.NewUserRegistrationEvent;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.dataconservancy.ui.util.UiBaseUrlConfig;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Fires an event, and the expected result is that an email is sent (assuming that the EmailService is properly
 * configured and enabled).
 */
public class DataItemDepositEventListenerTest extends DirtiesContextBaseUnitTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VelocityTemplateHelper velocityTemplateHelper;

    @Autowired
    private UiBaseUrlConfig dcsUiBaseUrlConfig;
    
    @Autowired
    private UserService userService;

    
    /**
     * Make sure deposit events are handled, but other events are ignored.
     */
    @Test
    public void testCanHandleEvent() {
        DataItemDepositEventListener listener = new DataItemDepositEventListener(notificationService, velocityTemplateHelper, userService, dcsUiBaseUrlConfig);
        
        EventContext context = new EventContext();
        
        assertTrue(listener.canHandleEvent(context, new DataItemDepositEvent(context, new Package())));
        assertFalse(listener.canHandleEvent(context, new NewUserApprovalEvent(context, user)));
        assertFalse(listener.canHandleEvent(context, new NewUserRegistrationEvent(context, user)));
    }
    
    /**
     * Make sure context has person and depositStatusLink set.
     */
    @Test
    public void testPopulateContext() {
        DataItemDepositEventListener listener = new DataItemDepositEventListener(notificationService, velocityTemplateHelper, userService, dcsUiBaseUrlConfig);
        
        EventContext context = new EventContext();       
        context.setUser(user.getId());
 
        Package pkg = new Package();
        pkg.setId("fake");
        
        VelocityContext velocityContext = new VelocityContext();
        listener.populateContext(velocityContext, new DataItemDepositEvent(context, pkg));
        
        assertTrue(velocityContext.containsKey("person"));
        assertTrue(velocityContext.containsKey("depositStatusLink"));
        
        assertEquals(user, velocityContext.get("person"));        
    }
    
    /**
     * Make sure email recipient is user.
     */
    @Test
    public void testBuildNotification() {
        DataItemDepositEventListener listener = new DataItemDepositEventListener(notificationService, velocityTemplateHelper, userService, dcsUiBaseUrlConfig);

        EventContext context = new EventContext();
        context.setUser(user.getId());
 
        Package pkg = new Package();
        pkg.setId("fake");
        
        DataItemDepositEvent event = new DataItemDepositEvent(context, pkg);
        
        String message = "message";
        String sender = "sender";
        String subject = "subject";
        List<String> recipients = new ArrayList<String>();
        
        listener.buildNotification(event, recipients, sender, subject, message);
        
        assertEquals(1, recipients.size());
        
        assertEquals(user.getEmailAddress(), recipients.get(0));
    }
}
