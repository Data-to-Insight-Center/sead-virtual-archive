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
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventListener;
import org.dataconservancy.ui.model.Email;
import org.dataconservancy.ui.model.Notification;
import org.dataconservancy.ui.services.NotificationService;
import org.dataconservancy.ui.services.VelocityTemplateHelper;

import java.util.List;

/**
 * An event listener which uses Velocity to format messages, and send notifications using the
 * {@link NotificationService}.
 */
public abstract class NotificationServiceEventListener implements EventListener {

    /**
     * A short name describing this event listener
     */
    private static final String NAME = "Notification Service Event Listener";

    /**
     * The notification service instance used to send the notifications
     */
    private final NotificationService notificationService;

    /**
     * The subject of the notifications (normally sent in email form)
     */
    private String emailSubject;

    /**
     * A list of recipients of the notifications (normally email addresses)
     */
    private List<String> emailRecipient;

    /**
     * The sender of the notification (normally an email address)
     */
    private String emailSender;

    /**
     * The default sender of the notification (only used if no {@link #emailSender} is specified)
     */
    private String defaultSender;

    /**
     * The name of the velocity template used to render the notification message.  This name is de-referenced
     * by the Velocity Engine's resource loader, so the format of the name is implementation dependent.  Normally
     * this should be a class path reference, and loaded by the Velocity ClasspathResourceLoader.
     */
    private String velocityTemplateName = "/templates/email-exception-template.vm";

    /**
     * The Velocity Template Helper: resolves velocity templates and renders them.  It is configured with the
     * Velocity Engine.
     */
    protected final VelocityTemplateHelper velocityHelper;

    /**
     * Constructs a new NotificationServiceEventListener with the supplied notification service and Velocity template
     * helper.
     *
     * @param notificationService the notification service
     * @param velocityHelper the velocity helper
     */
    public NotificationServiceEventListener(NotificationService notificationService, VelocityTemplateHelper velocityHelper) {
        if (notificationService == null) {
            throw new IllegalArgumentException("NotificationService must not be null.");
        }

        if (velocityHelper == null) {
            throw new IllegalArgumentException("VelocityTemplateHelper must not be null.");
        }
        
        this.notificationService = notificationService;
        this.velocityHelper = velocityHelper;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Handles the event by rendering a notification using Velocity, and uses the {@link NotificationService} to
     * notify recipients.  This method:
     * <ol>
     *     <li>Checks with sublasses to see if they can {@link #handleEvent(EventContext, org.dataconservancy.ui.eventing.api.Event) handle the event}</li>
     *     <li>{@link #populateContext(org.apache.velocity.VelocityContext, org.dataconservancy.ui.eventing.api.Event) Populates} the
     *         {@code VelocityContext}</li>
     *     <li>Renders the Velocity template</li>
     *     <li>{@link #buildNotification(org.dataconservancy.ui.eventing.api.Event, java.util.List, String, String, String) Builds} the {@link Notification}
     *         object.</li>
     *     <li>Uses the {@code NotificationService} to send the {@code Notification}</li>
     * </ol>
     * Subclasses should implement {@link #canHandleEvent(EventContext, org.dataconservancy.ui.eventing.api.Event)}, and they can override
     * {@link #populateContext(org.apache.velocity.VelocityContext, org.dataconservancy.ui.eventing.api.Event)} and
     * {@link #buildNotification(org.dataconservancy.ui.eventing.api.Event, java.util.List, String, String, String)} to customize the notification.
     *
     * @param eventContext the event context
     * @param event the event object
     */
    @Override
    public final void handleEvent(EventContext eventContext, Event<?> event) {

        if (!canHandleEvent(eventContext, event)) {
            return;
        }

        // Place the appropriate objects in the Velocity Context (and these should also be
        // documented in the template).
        final VelocityContext velocityContext = new VelocityContext();
        populateContext(velocityContext, event);

        // Render the message (email body) using the template and velocity context
        final String message = renderVelocityTemplate(velocityTemplateName, velocityContext);

        // Compose the Notification object (most likely an email)
        final Notification notification = buildNotification(event, emailRecipient, emailSender, emailSubject, message);

        // Send the notification
        notificationService.sendNotification(notification);
    }

    /**
     * Whether or not the implementation can send a notification for the event.  If implementations return
     * {@code false}, the event will not be processed.
     *
     * @param eventContext the event content
     * @param event the event
     * @param <T> the type of object contained by the event
     * @return true if the implementation can send a notification for the event.
     */
    protected abstract <T> boolean canHandleEvent(EventContext eventContext, Event<T> event);

    /**
     * This method is responsible for composing a complete {@code Notification} object, which is immediately provided
     * to the notification service.  This implementation composes an {@link Email} object.
     *
     * @param event the {@code Event} which triggered the notification
     * @param recipients the recipients of the notification
     * @param sender the sender of the notification
     * @param subject the subject of the notification
     * @param message the message body of the notification
     * @return the notification
     */
    protected Notification buildNotification(Event<?> event, List<String> recipients, String sender, String subject, String message) {
        Notification notification = new Email();
        if (message != null) {
            notification.setNotificationMessage(message);
        }
        if (recipients != null) {
            notification.setRecipient(recipients.toArray(new String[]{}));
        }
        if (subject != null) {
            notification.setSubject(subject);
        }
        if (sender != null) {
            notification.setSender(sender);
        } else {
            notification.setSender(defaultSender);
        }
        return notification;
    }

    /**
     * This method is responsible for resolving and rendering the supplied velocity template with the supplied
     * velocity context.
     *
     * @param velocityTemplateName the name of a velocity template
     * @param velocityContext the velocity context
     * @return the rendered content of the velocity template
     */
    protected String renderVelocityTemplate(String velocityTemplateName, VelocityContext velocityContext) {
        return velocityHelper.execute(velocityTemplateName, velocityContext);
    }

    /**
     * This method is responsible for populating name/value pairs of the {@link VelocityContext} object.  Objects are
     * placed into the {@code VelocityContext} using name/value pairs.  Velocity templates have access to objects in
     * the context by looking up the name of a property.
     * <p/>
     * This implementation binds the {@link org.dataconservancy.ui.eventing.api.Event#getEventContext() event context object} to the property
     * "eventContext", and binds the {@link org.dataconservancy.ui.eventing.api.Event event object itself} to the property "event". The returned
     * {@code VelocityContext} is then provided to the
     * {@link #renderVelocityTemplate(String, org.apache.velocity.VelocityContext) render} method.
     * <p/>
     * Subclasses may override this method to supply their own name/value pairs.
     *
     * @param velocityContext the context to populate with name/value pairs
     * @param event the event
     * @param <T> the type of the object contained in the {@code event}
     */
    protected <T> void populateContext(final VelocityContext velocityContext, final Event<T> event) {
        velocityContext.put("eventContext", event.getEventContext());
        velocityContext.put("event", event);
    }

    /**
     * The name of the template used to render the notification message body.
     *
     * @return the template name
     */
    public String getVelocityTemplateName() {
        return velocityTemplateName;
    }

    public void setVelocityTemplateName(String velocityTemplateName) {
        this.velocityTemplateName = velocityTemplateName;
    }

    /**
     * The recipients of the notification.
     *
     * @return the recipients
     */
    public List<String> getEmailRecipient() {
        return emailRecipient;
    }

    public void setEmailRecipient(List<String> emailRecipient) {
        this.emailRecipient = emailRecipient;
    }

    /**
     * The sender of the notification.
     *
     * @return the sender
     */
    public String getEmailSender() {
        return emailSender;
    }

    public void setEmailSender(String emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * The subject of the notification.
     *
     * @return the subject
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    /**
     * The default sender used for notifications (only used if {@link #getEmailSender()} is not set).
     *
     * @return the default email address used to send notifications
     */
    public String getDefaultSender() {
        return defaultSender;
    }

    /**
     * The default sender used for notifications (only used if {@link #getEmailSender()} is not set).
     *
     * @param defaultSender default email address used to send notifications
     */
    public void setDefaultSender(String defaultSender) {
        this.defaultSender = defaultSender;
    }
}
