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

import java.util.Comparator;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;

import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.NewUserApprovalEvent;
import org.dataconservancy.ui.exceptions.RegistrationUpdateException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ADMIN_REGISTRATION_APPROVAL_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ADMIN_REGISTRATION_APPROVAL_SUCCESS;

/**
 * Action Bean for administrator's registration managing page.
 */

@UrlBinding("/admin/registrations.action")
public class AdminRegistrationManagerActionBean
        extends AdminHomeActionBean {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private List<String> userIdsToApprove;

    public AdminRegistrationManagerActionBean() {
        super();

        // Ensure desired properties are available
        try {
            assert (messageKeys
                    .containsKey(MSG_KEY_ADMIN_REGISTRATION_APPROVAL_SUCCESS));
            assert (messageKeys
                    .containsKey(MSG_KEY_ADMIN_REGISTRATION_APPROVAL_ERROR));
        } catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                    + MSG_KEY_ADMIN_REGISTRATION_APPROVAL_ERROR + ", "
                    + MSG_KEY_ADMIN_REGISTRATION_APPROVAL_SUCCESS
                    + " is missing.");
        }

    }

    @DefaultHandler
    public Resolution render() {
        return new ForwardResolution("/pages/adminregistrations.jsp");
    }

    public Resolution approveRegistrations() throws RegistrationUpdateException {
        final EventContext eventContext = getEventContext();
        eventContext.setEventClass(EventClass.AUDIT);

        List<Message> approvedMessages = getContext().getMessages("approved");
        for (String id : userIdsToApprove) {
            try {
                userService
                        .updateRegistrationStatus(id,
                                                  RegistrationStatus.APPROVED);
                eventManager
                        .fire(eventContext,
                              new NewUserApprovalEvent(eventContext,
                                                       userService.get(id)));
                approvedMessages
                        .add(new SimpleMessage(String.format(messageKeys
                                                                     .getProperty(MSG_KEY_ADMIN_REGISTRATION_APPROVAL_SUCCESS),
                                                             id)));
            } catch (Exception e) {
                String message =
                        String.format(messageKeys
                                              .getProperty(MSG_KEY_ADMIN_REGISTRATION_APPROVAL_ERROR),
                                      id);
                RegistrationUpdateException rue =
                        new RegistrationUpdateException(message, e);
                rue.setUserId(id);
                throw rue;
            }
        }

        return render();
    }

    public List<Person> getPersonsToApprove() {
        return getPendingRegistrations();
    }

    public void setUserIdsToApprove(List<String> userIds) {
        this.userIdsToApprove = userIds;
    }

    public List<Person> getPendingRegistrations() {
        log.debug("Retrieving pending registrations...");
        return userService.find(RegistrationStatus.PENDING,
                                new Comparator<Person>() {

                                    @Override
                                    public int compare(Person a, Person b) {
                                        int lastNames =
                                                a.getLastNames().compareTo(b
                                                        .getLastNames());
                                        if (lastNames != 0) {
                                            return lastNames;
                                        }

                                        int firstNames =
                                                a.getFirstNames().compareTo(b
                                                        .getFirstNames());
                                        if (firstNames != 0) {
                                            return firstNames;
                                        }

                                        return a.getId().compareTo(b.getId());
                                    }
                                });
    }

}
