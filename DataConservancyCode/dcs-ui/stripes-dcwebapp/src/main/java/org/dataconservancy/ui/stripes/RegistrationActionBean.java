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

package org.dataconservancy.ui.stripes;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.NewUserRegistrationEvent;
import org.dataconservancy.ui.exceptions.RegistrationUpdateException;
import org.dataconservancy.ui.model.Notification;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.services.NotificationService;

/**
 * Registers new users with the system. This is responsible for rendering the
 * registration form, and persisting the new user information.
 */
@UrlBinding("/registration/registration.action")
public class RegistrationActionBean
        extends BaseActionBean {

    /**
     * The path to the registration form JSP which is rendered when a user
     * clicks registration.
     */
    public static final String SRC_FORM = "/pages/registration.jsp";

    /**
     * The path to the registration complete page. This is rendered when a
     * registration is successful.
     */
    public static final String SUCCESS = "/pages/regcomplete.jsp";

    @ValidateNestedProperties({
            @Validate(field = "firstNames", required = true),
            @Validate(field = "middleNames", required = false),
            @Validate(field = "lastNames", required = true),
            @Validate(field = "prefix", required = false),
            @Validate(field = "suffix", required = false),
            @Validate(field = "preferredPubName", required = false),
            @Validate(field = "password", required = true, minlength = 5, maxlength = 20),
            @Validate(field = "emailAddress", required = true, maxlength = 50, converter = EmailTypeConverter.class),
            @Validate(field = "phoneNumber", required = true, maxlength = 50),
            @Validate(field = "jobTitle", required = true),
            @Validate(field = "department", required = true),
            @Validate(field = "city", required = true),
            @Validate(field = "state", required = true),
            @Validate(field = "instCompany", required = true),
            @Validate(field = "instCompanyWebsite", required = true),
            @Validate(field = "website", required = false),
            @Validate(field = "bio", required = false)})
    private Person user;

    @Validate(required = true, minlength = 5, maxlength = 20, expression = "this == user.password")
    private String confirmedPassword;

    @ValidationMethod
    public void checkExistingEmail() {
        if (userService.get(user.getEmailAddress()) != null) {
            getContext()
                    .getValidationErrors()
                    .add("user.emailAddress",
                         new SimpleError("A user with the supplied email address already exists in the system."));
        }
    }

    private NotificationService notificationService;

    private Notification notification;

    /**
     * Redirects to the registration entry form.
     */
    @DefaultHandler
    @DontValidate
    public Resolution handle() {
        return new ForwardResolution(SRC_FORM);
    }

    /**
     * Handles the registration of a user with the system. And calls the
     * notification service to deliver a notification of a new registration.
     */
    public Resolution register() throws RegistrationUpdateException {
        //Set the person's registration status to "Pending"
        user.setRegistrationStatus(RegistrationStatus.PENDING);

        try {
            user = userService.create(user);
        } catch (Exception e) {
            RegistrationUpdateException rue =
                    new RegistrationUpdateException("Error registering user "
                            + user.getId() + " (" + user.getEmailAddress()
                            + "): " + e.getMessage(), e);
            if (user.getId() == null) {
                rue.setUserId(user.getEmailAddress());
            } else {
                rue.setUserId(user.getId());
            }
            throw rue;
        }

        final EventContext eventContext = getEventContext();
        eventContext.setEventClass(EventClass.AUDIT);
        eventManager.fire(eventContext,
                          new NewUserRegistrationEvent(eventContext, user));

        // Forward user to registration complete page
        return new ForwardResolution(SUCCESS);
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public String getConfirmedPassword() {
        return confirmedPassword;
    }

    public void setConfirmedPassword(String confirmedPassword) {
        this.confirmedPassword = confirmedPassword;
    }

    @SpringBean("notificationService")
    public void injectNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @SpringBean("registrationNotification")
    public void injectEmail(Notification notification) {
        this.notification = notification;
    }

}
