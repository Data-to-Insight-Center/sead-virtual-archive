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

import net.sourceforge.stripes.action.UrlBinding;
import org.apache.velocity.VelocityContext;
import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventTopic;
import org.dataconservancy.ui.model.Notification;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;
import org.dataconservancy.ui.stripes.AdminRegistrationManagerActionBean;
import org.dataconservancy.ui.util.UiBaseUrlConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sends emails to UI Instance Administrators when a user registers for an account.  This <em>must</em> be configured
 * to use the Velocity template {@code /templates/new-registration-notification.vm}.
 */
public class RegistrationNotificationEventListener extends NotificationServiceEventListener {

    /**
     * Used to find all users with Role.ROLE_ADMIN.
     */
    private UserService userService;

    /**
     * Used to construct the base URL of the UI
     */
    private UiBaseUrlConfig urlConfig;

    /**
     * Constructs a new listener which responds to user registration events.
     *
     * @param notificationService the notification service
     * @param velocityHelper the velocity helper
     * @param userService the user service
     * @param urlConfig the URL configuration of the UIs
     */
    public RegistrationNotificationEventListener(NotificationService notificationService,
                                                 VelocityTemplateHelper velocityHelper, UserService userService,
                                                 UiBaseUrlConfig urlConfig) {
        super(notificationService, velocityHelper);
        this.userService = userService;
        this.urlConfig = urlConfig;
    }

    /**
     * Returns true if the Event is a {@link org.dataconservancy.ui.eventing.api.EventTopic#REGISTRATION}, of class {@link EventClass#AUDIT}, and the
     * object is a {@link Person}.
     *
     * @param eventContext {@inheritDoc}
     * @param event {@inheritDoc}
     * @param <T> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected <T> boolean canHandleEvent(EventContext eventContext, Event<T> event) {
        return event.getEventTopic() == EventTopic.REGISTRATION
                && event.getEventClass() == EventClass.AUDIT
                && event.getEventObject() instanceof Person;
    }

    /**
     * In addition to name/value pairs added by the superclass, this implementation binds the name 'person' to the
     * Person object who submitted their registration, and the name 'approveRegistrationsLink' to the HTTP
     * url of the Approve Registration Action Bean.
     *
     * @param velocityContext the context to populate with name/value pairs
     * @param event the event
     * @param <T>
     */
    @Override
    protected <T> void populateContext(VelocityContext velocityContext, Event<T> event) {
        super.populateContext(velocityContext, event);
        velocityContext.put("person", event.getEventObject());
        velocityContext.put("approveRegistrationsLink", urlConfig.getBaseUrl().toString() +
                AdminRegistrationManagerActionBean.class.getAnnotation(UrlBinding.class).value());
    }

    /**
     * Insures that all of the instance administrators are included in the recipients list, if they aren't there already.
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

        for (Person admin : getInstanceAdministrators()) {
            if (!recipients.contains(admin.getEmailAddress())) {
                recipients.add(admin.getEmailAddress());
            }
        }

        n.setRecipient(recipients.toArray(new String[] {}));

        return n;
    }

    /**
     * Iterates over all approved users of the system, tests if they have Role.ROLE_ADMIN, and adding them to the
     * returned Set of Persons if they are.
     *
     * @return a Set of Persons that have an Approved Registration Status and have the Role.ROLE_ADMIN
     */
    private Set<Person> getInstanceAdministrators() {
        Set<Person> administrators = new HashSet<Person>();
        for (Person p : userService.find(RegistrationStatus.APPROVED, null)) {
            if (p.getRoles().contains(Role.ROLE_ADMIN)) {
                administrators.add(p);
            }
        }

        return administrators;
    }

}
