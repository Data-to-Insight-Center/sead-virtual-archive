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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.velocity.VelocityContext;
import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventTopic;
import org.dataconservancy.ui.model.Notification;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.dataconservancy.ui.stripes.HomeActionBean;
import org.dataconservancy.ui.util.UiBaseUrlConfig;

/**
 * Sends email to a user when their account is approved.  This <em>must</em> be configured
 * to use the Velocity template {@code /templates/user-approval-notification.vm}.
 */
public class ApprovalNotificationEventListener extends NotificationServiceEventListener {

    /**
     * Used to construct the base URL of the UI
     */
    private UiBaseUrlConfig urlConfig;

    /**
     * Constructs a new listener which responds to user approval events.
     *
     * @param notificationService the notification service
     * @param velocityHelper the velocity helper
     * @param urlConfig the URL configuration of the UIs
     */
    public ApprovalNotificationEventListener(NotificationService notificationService,
                                                 VelocityTemplateHelper velocityHelper,
                                                 UiBaseUrlConfig urlConfig) {
        super(notificationService, velocityHelper);
        this.urlConfig = urlConfig;
    }

    /**
     * Returns true if the Event is a {@link org.dataconservancy.ui.eventing.api.EventTopic#USER_APPROVAL}, of class {@link EventClass#AUDIT}, and the
     * object is a {@link Person}.
     *
     * @param eventContext {@inheritDoc}
     * @param event {@inheritDoc}
     * @param <T> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected <T> boolean canHandleEvent(EventContext eventContext, Event<T> event) {
        return event.getEventTopic() == EventTopic.USER_APPROVAL
                && event.getEventClass() == EventClass.AUDIT
                && event.getEventObject() instanceof Person;
    }

    /**
     * In addition to name/value pairs added by the superclass, this implementation binds the name 'person' to the
     * Person object who was approved, the name 'instanceHomeLink' to the HTTP
     * url of the Home Action Bean, and the name instanceName to the StripesResources property for same..
     *
     * @param velocityContext the context to populate with name/value pairs
     * @param event the event
     * @param <T>
     */
    @Override
    protected <T> void populateContext(VelocityContext velocityContext, Event<T> event) {
        Properties instanceProps = new Properties();
        String instanceNameKey = "dataConservancyInstance.name";
        String instanceNameValue;
        try {
            instanceProps = loadPropertiesFile("/StripesResources.properties");
            instanceNameValue =  instanceProps.getProperty(instanceNameKey);
        }
        catch (Exception e) {
            instanceNameValue= instanceNameKey +  "????";
        }
        super.populateContext(velocityContext, event);
        velocityContext.put("person", event.getEventObject());
        velocityContext.put("instanceHomeLink", urlConfig.getBaseUrl().toString() +
                HomeActionBean.class.getAnnotation(UrlBinding.class).value());
        velocityContext.put("instanceName", instanceNameValue);
    }

    /**
     * Add the user to the recipients.
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
        
        Person user = (Person) event.getEventObject();
        recipients.add(user.getEmailAddress());
        n.setRecipient(recipients.toArray(new String[] {}));

        return n;
    }


    public Properties loadPropertiesFile(String filename) throws IOException {
        //create a new properties object with the data in the file
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream(filename);
        if (null != in) {
            props.load(in);
            in.close();
        }
        return props;
    }
}
