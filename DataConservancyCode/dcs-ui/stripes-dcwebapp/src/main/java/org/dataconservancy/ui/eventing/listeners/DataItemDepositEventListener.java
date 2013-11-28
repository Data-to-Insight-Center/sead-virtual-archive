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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.velocity.VelocityContext;
import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventTopic;
import org.dataconservancy.ui.model.Notification;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.dataconservancy.ui.stripes.DepositStatusActionBean;
import org.dataconservancy.ui.util.UiBaseUrlConfig;

/**
 * Sends email to user who deposits an item in a collection.  This <em>must</em> be configured
 * to use the Velocity template {@code /templates/data-item-deposit-notification.vm}.
 */
public class DataItemDepositEventListener extends NotificationServiceEventListener {

    /**
     * Used to lookup depositing url.
     */
    private UserService userService;

    /**
     * Used to construct the base URL of the UI
     */
    private UiBaseUrlConfig urlConfig;

    /**
     * Constructs a new listener which responds to user data item deposit events.
     *
     * @param notificationService the notification service
     * @param velocityHelper the velocity helper
     * @param userService the user service
     * @param urlConfig the URL configuration of the UIs
     */
    public DataItemDepositEventListener(NotificationService notificationService,
                                                 VelocityTemplateHelper velocityHelper, UserService userService,
                                                 UiBaseUrlConfig urlConfig) {
        super(notificationService, velocityHelper);
        this.userService = userService;
        this.urlConfig = urlConfig;
    }

    /**
     * Returns true if the Event is a {@link org.dataconservancy.ui.eventing.api.EventTopic#DATA_ITEM_DEPOSIT}, of class {@link EventClass#AUDIT}, and the
     * object is a {@link Package}.
     *
     * @param eventContext {@inheritDoc}
     * @param event {@inheritDoc}
     * @param <T> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected <T> boolean canHandleEvent(EventContext eventContext, Event<T> event) {
        return event.getEventTopic() == EventTopic.DATA_ITEM_DEPOSIT
                && event.getEventClass() == EventClass.AUDIT
                && event.getEventObject() instanceof org.dataconservancy.ui.model.Package;
    }

    /**
     * In addition to name/value pairs added by the superclass, this implementation binds the name 'person' to the
     * Person object who deposited an item, and the name 'depositStatusLink' to the HTTP
     * url of the deposit status action bean.
     *
     * @param velocityContext the context to populate with name/value pairs
     * @param event the event
     * @param <T>
     */
    @Override
    protected <T> void populateContext(VelocityContext velocityContext, Event<T> event) {
        super.populateContext(velocityContext, event);
        velocityContext.put("package", event.getEventObject());
        velocityContext.put("person", getDepositor(event));

        org.dataconservancy.ui.model.Package pkg = (Package) event.getEventObject();
        
        String base = urlConfig.getBaseUrl().toString();        
        String path = DepositStatusActionBean.class.getAnnotation(UrlBinding.class).value();
        
        String query;
        
        try {
            query = "?objectId=" + URLEncoder.encode(pkg.getId(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Better way to handle this?
            throw new RuntimeException(e);
        }        
        
        velocityContext.put("depositStatusLink", base + path + query);
    }

    /**
     * Assumption that the user set by the event context is a Person in the UserService.
     * 
     * @return Person depositing
     */
    private Person getDepositor(Event<?> event) {
        String user = event.getEventContext().getUser();
        
        if (user == null) {
            return null;
        }
        
        return userService.get(user);        
    }
    
    /**
     * Set the recipients to the depositor.
     * 
     * @param event the {@code Event} which triggered the notification
     * @param recipients the recipients of the notification
     * @param sender the sender of the notification
     * @param subject the subject of the notification
     * @param message the message body of the notification
     * @return the Notification containing the original recipients plus all users with {@code Role.ROLE_ADMIN}
     */
    @Override
    protected Notification buildNotification(Event<?> event, List<String> recipients, String sender, String subject, String message) {
        Notification n = super.buildNotification(event, recipients, sender, subject, message);

        if (recipients == null) {
            recipients = new ArrayList<String>();
        }

        recipients.add(getDepositor(event).getEmailAddress());
        
        n.setRecipient(recipients.toArray(new String[] {}));

        return n;
    }
}
