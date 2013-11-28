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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

import org.dataconservancy.ui.eventing.api.EventClass;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.events.NewUserApprovalEvent;
import org.dataconservancy.ui.exceptions.RegistrationUpdateException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ADMIN_REGISTRATION_APPROVAL_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ADMIN_REGISTRATION_UPDATE_ERROR;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ADMIN_REGISTRATION_UPDATE_SUCCESS;

/**
 * Action Bean for administrator's update users' registration page.
 */
@UrlBinding(value = "/admin/updateregistrations.action")
public class AdminUpdateRegistrationManagerActionBean
        extends AdminHomeActionBean {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Validate(required = true, on = "updateRegistrations")
    private List<String> userIdsToUpdate;

    private HashMap status = new HashMap();

    @Validate(required = true, on = "updateRegistrations")
    private RegistrationStatus registrationStatus;

    public AdminUpdateRegistrationManagerActionBean() {
        super();

        // Ensure desired properties are available
        try {
            assert (messageKeys
                    .containsKey(MSG_KEY_ADMIN_REGISTRATION_UPDATE_SUCCESS));
        } catch (AssertionError e) {
            throw new RuntimeException("Missing required message key!  One of "
                    + MSG_KEY_ADMIN_REGISTRATION_APPROVAL_ERROR + ", "
                    + MSG_KEY_ADMIN_REGISTRATION_UPDATE_SUCCESS
                    + " is missing.");
        }
    }

    @DefaultHandler
    public Resolution render() {
        return new ForwardResolution("/pages/adminupdateregistrations.jsp");
    }

    public Resolution updateRegistrations() throws RegistrationUpdateException {
        final EventContext eventContext = getEventContext();
        eventContext.setEventClass(EventClass.AUDIT);

        List<Message> updatedMessages = getContext().getMessages("updated");
        for (String id : userIdsToUpdate) {
            try {
                Person user = userService.get(id);
                RegistrationStatus oldstatus =
                        user == null ? null : user.getRegistrationStatus();

                userService.updateRegistrationStatus(id, registrationStatus);
                updatedMessages
                        .add(new SimpleMessage(String.format(messageKeys
                                                                     .getProperty(MSG_KEY_ADMIN_REGISTRATION_UPDATE_SUCCESS),
                                                             id)));

                if (oldstatus == RegistrationStatus.PENDING
                        && registrationStatus == RegistrationStatus.APPROVED) {
                    eventManager.fire(eventContext,
                                      new NewUserApprovalEvent(eventContext,
                                                               user));
                }
            } catch (Exception e) {
                String message =
                        String.format(messageKeys
                                              .getProperty(MSG_KEY_ADMIN_REGISTRATION_UPDATE_ERROR),
                                      id);
                RegistrationUpdateException rue =
                        new RegistrationUpdateException(message, e);
                rue.setUserId(id);
                throw rue;
            }
        }
        return render();
    }

    public List<Person> getPendingRegistrations() {
        log.debug("Retrieving pending registrations");
        return getRegistrationsByStatus(RegistrationStatus.PENDING);
    }

    public List<Person> getApprovedRegistrations() {
        log.debug("Retrieving approved registrations");
        List<Person> approvedRegisteredUsers = new ArrayList<Person>();
        //we don't want to change status for instance admins - they can only be APPROVED
        for (Person user : getRegistrationsByStatus(RegistrationStatus.APPROVED)) {
            if (!user.getRoles().contains(Role.ROLE_ADMIN)) {
                approvedRegisteredUsers.add(user);
            }
        }
        return approvedRegisteredUsers;
    }

    public List<Person> getBlacklistedRegistrations() {
        log.debug("Retrieving blacklisted registrations");
        return getRegistrationsByStatus(RegistrationStatus.BLACK_LISTED);
    }

    public void setUserIdsToUpdate(List<String> userIds) {
        this.userIdsToUpdate = userIds;
    }

    public void setRegistrationStatus(RegistrationStatus regStatus) {
        this.registrationStatus = regStatus;
    }

    public List<Person> getRegistrationsByStatus(RegistrationStatus registrationStatus) {
        log.debug("Retrieving " + registrationStatus.toString()
                + " registrations...");
        return userService.find(registrationStatus, new Comparator<Person>() {

            @Override
            public int compare(Person a, Person b) {
                int lastNames = a.getLastNames().compareTo(b.getLastNames());
                if (lastNames != 0) {
                    return lastNames;
                }

                int firstNames = a.getFirstNames().compareTo(b.getFirstNames());
                if (firstNames != 0) {
                    return firstNames;
                }

                return a.getId().compareTo(b.getId());
            }
        });
    }

}
