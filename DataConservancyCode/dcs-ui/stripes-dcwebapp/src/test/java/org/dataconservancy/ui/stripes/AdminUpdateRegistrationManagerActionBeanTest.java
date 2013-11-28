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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventListener;
import org.dataconservancy.ui.eventing.api.EventManager;
import org.dataconservancy.ui.eventing.events.NewUserApprovalEvent;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA. User: jrm Date: 10/18/11 Time: 9:59 AM To change
 * this template use File | Settings | File Templates.
 */
@DirtiesContext
public class AdminUpdateRegistrationManagerActionBeanTest
        extends BaseActionBeanTest {

    @Autowired
    @Qualifier("adminUser")
    private Person adminRole;

    @Autowired
    EventManager eventManager;

    @Autowired
    @Qualifier("eventManagerExecutorService")
    private ExecutorService executorService;

    @Autowired
    private UserService userService;

    private MockHttpSession userSession;

    private Person user1;

    private Person user2;

    private Person user3;

    private Person admin1;

    /**
     * *Initialize the mock http session with authenticated user credentials.
     * Tests that re-use this mock session will be already logged in.
     * 
     * @throws Exception
     */
    @Before
    public void setUpMockHttpSessions() throws Exception {
        userSession =
                new net.sourceforge.stripes.mock.MockHttpSession(servletCtx);
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  "/j_spring_security_check",
                                  userSession);
        rt.setParameter("j_username", adminRole.getEmailAddress());
        rt.setParameter("j_password", adminRole.getPassword());
        rt.execute();

        SecurityContext ctx =
                (SecurityContext) userSession
                        .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(adminRole.getEmailAddress(), ((UserDetails) ctx
                .getAuthentication().getPrincipal()).getUsername());

    }

    /**
     * Set up fresh users for each test.
     * 
     * @throws Exception
     */
    @Before
    public void setUpUsers() throws Exception {
        final PersonBizPolicyConsultant originalPolicyConsultant =
                userService.getPolicyConsultant();
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {

            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return originalPolicyConsultant
                        .enforceRegistrationStatusOnCreate();
            }

            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return Arrays.asList(RegistrationStatus.APPROVED,
                                     RegistrationStatus.PENDING,
                                     RegistrationStatus.BLACK_LISTED);
            }

            @Override
            public RegistrationStatus getDefaultRegistrationStatus() {
                return originalPolicyConsultant.getDefaultRegistrationStatus();
            }

            @Override
            public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
                return originalPolicyConsultant
                        .getRolesForRegistrationStatus(status);
            }
        });
        user1 = new Person();
        user1.setId("id:user1");
        user1.setEmailAddress("user1@test.org");
        user1.setFirstNames("User");
        user1.setLastNames("One");
        user1.setPrefix("Mr.");
        user1.setSuffix("II");
        user1.setMiddleNames("Middle");
        user1.setPreferredPubName("U. One");
        user1.setBio("Some bio for the user.");
        user1.setWebsite("www.somewebsite.com");
        user1.setPassword("password1");
        user1.setPhoneNumber("5550000001");
        user1.setJobTitle("User Scientist");
        user1.setDepartment("User Department");
        user1.setCity("Baltimore");
        user1.setState("Maryland");
        user1.setInstCompany("User Institution/Company");
        user1.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user1.setRegistrationStatus(RegistrationStatus.PENDING);
        user1.setExternalStorageLinked(false);
        user1.setDropboxAppKey("SomeKey");
        user1.setDropboxAppSecret("SomeSecret");

        assertNull(userService.get(user1.getEmailAddress()));
        userService.create(user1);

        user2 = new Person();
        user2.setId("id:user2");
        user2.setEmailAddress("user2@test.org");
        user2.setFirstNames("User");
        user2.setLastNames("Two");
        user2.setPrefix("Mr.");
        user2.setSuffix("II");
        user2.setMiddleNames("Middle");
        user2.setPreferredPubName("U. Two");
        user2.setBio("Some bio for the user.");
        user2.setWebsite("www.somewebsite.com");
        user2.setPassword("password2");
        user2.setPhoneNumber("5550000002");
        user2.setJobTitle("User Scientist");
        user2.setDepartment("User Department");
        user2.setCity("Baltimore");
        user2.setState("Maryland");
        user2.setInstCompany("User Institution/Company");
        user2.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user2.setRegistrationStatus(RegistrationStatus.APPROVED);
        List<Role> user2Roles = new ArrayList<Role>();
        user2Roles.add(Role.ROLE_USER);
        user2.setExternalStorageLinked(false);
        user2.setDropboxAppKey("SomeKey");
        user2.setDropboxAppSecret("SomeSecret");

        assertNull(userService.get(user2.getEmailAddress()));
        userService.create(user2);
        userService.updateRoles(user2.getId(), user2Roles);

        user3 = new Person();
        user3.setId("id:user3");
        user3.setEmailAddress("user3@test.org");
        user3.setFirstNames("User");
        user3.setLastNames("Three");
        user3.setPrefix("Mr.");
        user3.setSuffix("II");
        user3.setMiddleNames("Middle");
        user3.setPreferredPubName("U. Three");
        user3.setBio("Some bio for the user.");
        user3.setWebsite("www.somewebsite.com");
        user3.setPassword("password2");
        user3.setPhoneNumber("5550000003");
        user3.setJobTitle("User Scientist");
        user3.setDepartment("User Department");
        user3.setCity("Baltimore");
        user3.setState("Maryland");
        user3.setInstCompany("User Institution/Company");
        user3.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user3.setRegistrationStatus(RegistrationStatus.BLACK_LISTED);
        user3.setExternalStorageLinked(false);
        user3.setDropboxAppKey("SomeKey");
        user3.setDropboxAppSecret("SomeSecret");

        assertNull(userService.get(user3.getEmailAddress()));
        userService.create(user3);

        admin1 = new Person();
        admin1.setId("id:fooadmin");
        admin1.setEmailAddress("admin1@test.org");
        admin1.setFirstNames("Admin");
        admin1.setLastNames("One");
        admin1.setPrefix("Mr.");
        admin1.setSuffix("II");
        admin1.setMiddleNames("Middle");
        admin1.setPreferredPubName("A. One");
        admin1.setBio("Some bio for the user.");
        admin1.setWebsite("www.somewebsite.com");
        admin1.setPassword("admin1");
        admin1.setPhoneNumber("5550000004");
        admin1.setJobTitle("Admin Scientist");
        admin1.setDepartment("Admin Department");
        admin1.setCity("Baltimore");
        admin1.setState("Maryland");
        admin1.setInstCompany("Admin Institution/Company");
        admin1.setInstCompanyWebsite("www.AdminInstitutionCompany.com");
        admin1.setRegistrationStatus(RegistrationStatus.APPROVED);
        admin1.setExternalStorageLinked(false);
        admin1.setDropboxAppKey("SomeKey");
        admin1.setDropboxAppSecret("SomeSecret");

        assertNull(userService.get(admin1.getEmailAddress()));
        userService.create(admin1);
        userService.updateRoles(admin1.getId(),
                                Arrays.asList(Role.ROLE_USER, Role.ROLE_ADMIN));

        assertEquals(user1, userService.get(user1.getEmailAddress()));
        assertEquals(user2, userService.get(user2.getEmailAddress()));
        assertEquals(user3, userService.get(user3.getEmailAddress()));
        assertEquals(admin1, userService.get(admin1.getEmailAddress()));

        assertEquals(Collections.<Role> emptyList(),
                     userService.get(user1.getEmailAddress()).getRoles());
        assertEquals(Arrays.asList(Role.ROLE_USER),
                     userService.get(user2.getEmailAddress()).getRoles());
        assertEquals(Collections.<Role> emptyList(),
                     userService.get(user3.getEmailAddress()).getRoles());
        assertEquals(Arrays.asList(Role.ROLE_USER, Role.ROLE_ADMIN),
                     userService.get(admin1.getEmailAddress()).getRoles());
    }

    @After
    public void tearDown() {
        userService.deletePerson(user1.getEmailAddress());
        userService.deletePerson(user2.getEmailAddress());
        userService.deletePerson(user3.getEmailAddress());
        userService.deletePerson(admin1.getEmailAddress());

        assertNull(userService.get(user1.getEmailAddress()));
        assertNull(userService.get(user2.getEmailAddress()));
        assertNull(userService.get(user3.getEmailAddress()));
        assertNull(userService.get(admin1.getEmailAddress()));
        super.tearDown();
    }

    /**
     * test changes to Pending. Should update status, and remove all Roles
     * 
     * @throws Exception
     */

    @Test
    public void testPendingUpdateRegistrations() throws Exception {
        MockRoundtrip trip =
                new MockRoundtrip(servletCtx,
                                  AdminUpdateRegistrationManagerActionBean.class,
                                  userSession);
        trip.setParameter("registrationStatus",
                          RegistrationStatus.PENDING.name());
        trip.addParameter("userIdsToUpdate", user1.getEmailAddress());
        trip.addParameter("userIdsToUpdate", user2.getEmailAddress());
        trip.addParameter("userIdsToUpdate", user3.getEmailAddress());
        trip.addParameter("userIdsToUpdate", admin1.getEmailAddress());
        trip.execute("updateRegistrations");

        assertEquals(200, trip.getResponse().getStatus());

        user1 = userService.get(user1.getEmailAddress());
        user2 = userService.get(user2.getEmailAddress());
        user3 = userService.get(user3.getEmailAddress());
        admin1 = userService.get(admin1.getEmailAddress());

        /* users should now be PENDING */
        assertEquals(RegistrationStatus.PENDING.toString(), user1
                .getRegistrationStatus().toString());
        assertEquals(RegistrationStatus.PENDING.toString(), user2
                .getRegistrationStatus().toString());
        assertEquals(RegistrationStatus.PENDING.toString(), user3
                .getRegistrationStatus().toString());
        assertEquals(RegistrationStatus.PENDING.toString(), admin1
                .getRegistrationStatus().toString());

        /* users should have no Roles */
        assertEquals(0, user1.getRoles().size());
        assertEquals(0, user2.getRoles().size());
        assertEquals(0, user3.getRoles().size());
        assertEquals(0, admin1.getRoles().size());

        /* there should be no validation errors */
        AdminUpdateRegistrationManagerActionBean bean =
                trip.getActionBean(AdminUpdateRegistrationManagerActionBean.class);
        assertEquals(0, bean.getContext().getValidationErrors().size());
    }

    /**
     * Test changes to Approved. Should update status, and all users should have
     * ROLE_USER. An approval event should be fired for each change from pending
     * to approved.
     * 
     * @throws Exception
     */
    @Test
    public void testApprovedUpdateRegistrations() throws Exception {
        MockRoundtrip trip =
                new MockRoundtrip(servletCtx,
                                  AdminUpdateRegistrationManagerActionBean.class,
                                  userSession);
        trip.setParameter("registrationStatus",
                          RegistrationStatus.APPROVED.name());
        trip.addParameter("userIdsToUpdate", user1.getEmailAddress());
        trip.addParameter("userIdsToUpdate", user2.getEmailAddress());
        trip.addParameter("userIdsToUpdate", user3.getEmailAddress());
        trip.addParameter("userIdsToUpdate", admin1.getEmailAddress());

        // Record users who have a approval event fired

        final List<Person> approved = new ArrayList<Person>();

        eventManager.addListener(new EventListener() {

            @Override
            public void handleEvent(EventContext eventContext, Event<?> event) {
                if (event instanceof NewUserApprovalEvent) {
                    approved.add((Person) event.getEventObject());
                }
            }

            @Override
            public String getName() {
                return "test listener";
            }
        });

        trip.execute("updateRegistrations");

        assertEquals(200, trip.getResponse().getStatus());

        user1 = userService.get(user1.getEmailAddress());
        user2 = userService.get(user2.getEmailAddress());
        user3 = userService.get(user3.getEmailAddress());
        admin1 = userService.get(admin1.getEmailAddress());

        /* users should now be APPROVED */
        assertEquals(RegistrationStatus.APPROVED, user1.getRegistrationStatus());
        assertEquals(RegistrationStatus.APPROVED, user2.getRegistrationStatus());
        assertEquals(RegistrationStatus.APPROVED, user3.getRegistrationStatus());
        assertEquals(RegistrationStatus.APPROVED,
                     admin1.getRegistrationStatus());

        /*
         * all users should have ROLE_USER; note that admin1 will have
         * ROLE_ADMIN revoked.
         */
        assertEquals(1, user1.getRoles().size());
        assertEquals(1, user2.getRoles().size());
        assertEquals(1, user3.getRoles().size());
        assertEquals(1, admin1.getRoles().size());

        assertTrue(user1.getRoles().contains(Role.ROLE_USER));
        assertTrue(user2.getRoles().contains(Role.ROLE_USER));
        assertTrue(user3.getRoles().contains(Role.ROLE_USER));
        assertTrue(admin1.getRoles().contains(Role.ROLE_USER));

        /* there should be no validation errors */
        AdminUpdateRegistrationManagerActionBean bean =
                trip.getActionBean(AdminUpdateRegistrationManagerActionBean.class);
        assertEquals(0, bean.getContext().getValidationErrors().size());

        /* Make sure that the user approval event is fired only for user1 */

        // Wait for events to be executed
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.SECONDS);

        assertEquals(1, approved.size());
        assertTrue(approved.contains(user1));
    }

    /**
     * test changes to Black-listed. Should update status, end remove all Roles
     * 
     * @throws Exception
     */

    @Test
    public void testBlacklistedUpdateRegistrations() throws Exception {
        MockRoundtrip trip =
                new MockRoundtrip(servletCtx,
                                  AdminUpdateRegistrationManagerActionBean.class,
                                  userSession);
        trip.setParameter("registrationStatus",
                          RegistrationStatus.BLACK_LISTED.name());
        trip.addParameter("userIdsToUpdate", user1.getEmailAddress());
        trip.addParameter("userIdsToUpdate", user2.getEmailAddress());
        trip.addParameter("userIdsToUpdate", user3.getEmailAddress());
        trip.addParameter("userIdsToUpdate", admin1.getEmailAddress());
        trip.execute("updateRegistrations");

        assertEquals(200, trip.getResponse().getStatus());

        user1 = userService.get(user1.getEmailAddress());
        user2 = userService.get(user2.getEmailAddress());
        user3 = userService.get(user3.getEmailAddress());
        admin1 = userService.get(admin1.getEmailAddress());

        /* users should now be BLACKLISTED */
        assertEquals(RegistrationStatus.BLACK_LISTED.toString(), user1
                .getRegistrationStatus().toString());
        assertEquals(RegistrationStatus.BLACK_LISTED.toString(), user2
                .getRegistrationStatus().toString());
        assertEquals(RegistrationStatus.BLACK_LISTED.toString(), user3
                .getRegistrationStatus().toString());
        assertEquals(RegistrationStatus.BLACK_LISTED.toString(), admin1
                .getRegistrationStatus().toString());

        /* users should have no Roles */
        assertEquals(0, user1.getRoles().size());
        assertEquals(0, user2.getRoles().size());
        assertEquals(0, user3.getRoles().size());
        assertEquals(0, admin1.getRoles().size());

        /* there should be no validation errors */
        AdminUpdateRegistrationManagerActionBean bean =
                trip.getActionBean(AdminUpdateRegistrationManagerActionBean.class);
        assertEquals(0, bean.getContext().getValidationErrors().size());
    }

    /**
     * test for validation errors : null registrationStatus and userIdsToUpdate
     * fields
     * 
     * @throws Exception
     */
    @Test
    public void testValidationErrors() throws Exception {
        MockRoundtrip trip =
                new MockRoundtrip(servletCtx,
                                  AdminUpdateRegistrationManagerActionBean.class,
                                  userSession);
        trip.setParameter("registrationStatus", "");
        trip.setParameter("userIdstoUpdate", "");
        trip.execute("updateRegistrations");

        assertEquals(200, trip.getResponse().getStatus());

        /* we expect one validation error for each missing value */
        AdminUpdateRegistrationManagerActionBean bean =
                trip.getActionBean(AdminUpdateRegistrationManagerActionBean.class);
        assertEquals(2, bean.getContext().getValidationErrors().size());
        assertTrue(bean.getContext().getValidationErrors()
                .containsKey("registrationStatus"));
        assertTrue(bean.getContext().getValidationErrors()
                .containsKey("userIdsToUpdate"));
    }
}
