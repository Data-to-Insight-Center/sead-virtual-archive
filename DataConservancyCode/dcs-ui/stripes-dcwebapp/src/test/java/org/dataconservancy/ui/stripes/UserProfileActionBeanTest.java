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
import java.util.List;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.junit.Before;
import org.junit.Test;

import org.apache.http.conn.ConnectTimeoutException;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

import com.dropbox.client2.exception.DropboxIOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class UserProfileActionBeanTest
        extends BaseActionBeanTest {

    @Autowired
    private JdbcTemplate template;

    private MockHttpSession userSession;

    @Autowired
    @Qualifier("delegatingUserService")
    private UserService userService;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    /**
     * Initialize the mock http session with authenticated admin credentials.
     * Tests that re-use this mock session will be already logged in.
     */
    @Before
    public void setUpMockHttpSessions() throws Exception {
        userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  "/j_spring_security_check",
                                  userSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        SecurityContext ctx =
                (SecurityContext) userSession
                        .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx
                .getAuthentication().getPrincipal()).getUsername());
    }

    /**
     * Asserts that the default handler is what we expect it to be
     */
    @Test
    public void testDefaultHandler() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  UserProfileActionBean.class,
                                  userSession);
        rt.execute();
        assertEquals(UserProfileActionBean.VIEW_PROFILE_PATH,
                     rt.getForwardUrl());
    }

    /**
     * Asserts that correct JSP and ActionBean URL is used when viewing a admin
     * profile
     */
    @Test
    public void testViewUserProfilePath() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  UserProfileActionBean.class,
                                  userSession);
        rt.execute("viewUserProfile");
        assertEquals(UserProfileActionBean.VIEW_PROFILE_PATH,
                     rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Asserts that a admin must be logged in to view their profile
     */
    @Test
    public void testViewUserProfileUnauthenticatedUser() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx, UserProfileActionBean.class);
        rt.execute("viewUserProfile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getDestination().endsWith("/login/login.action"));
    }

    /**
     * Asserts that correct JSP and ActionBean URL is used when editing a admin
     * profile
     */
    @Test
    public void testEditUserProfilePath() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  UserProfileActionBean.class,
                                  userSession);
        rt.execute("editUserProfile");
        assertEquals(UserProfileActionBean.EDIT_PROFILE_PATH,
                     rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Asserts that a admin must be logged in to edit their profile
     */
    @Test
    public void testEditUserProfileUnauthenticatedUser() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx, UserProfileActionBean.class);
        rt.execute("editUserProfile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getDestination().endsWith("/login/login.action"));
    }

    /**
     * Asserts that updates to a admin are persisted as expected.
     */
    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testUserProfileUpdated() throws Exception {
        final Person person = new Person();
        //person.setId("id:funkymonk");
        person.setId(idService.create(Types.PERSON.name()).getUrl().toString());
        person.setFirstNames("Funky");
        person.setLastNames("Monk");
        person.setPrefix("Mr.");
        person.setSuffix("II");
        person.setMiddleNames("Middle");
        person.setPreferredPubName("F. Monk");
        person.setBio("Some bio for the user.");
        person.setWebsite("www.somewebsite.com");
        person.setEmailAddress("funk_monk@gmail.com");
        person.setPassword("foobar");
        person.setPhoneNumber("123456789");
        person.setJobTitle("Funky Scientist");
        person.setDepartment("Funky Department");
        person.setCity("Baltimore");
        person.setState("Maryland");
        person.setInstCompany("Funky Institution/Company");
        person.setInstCompanyWebsite("www.FunkyInstitutionCompanyWebsite.com");
        person.setRegistrationStatus(RegistrationStatus.APPROVED);
        person.setReadOnly(false);
        person.setExternalStorageLinked(false);
        person.setDropboxAppKey("SomeKey");
        person.setDropboxAppSecret("SomeSecret");
        /*
         * person.addRole(Role.ROLE_USER); person.addRole(Role.ROLE_ADMIN);
         */

        final PersonBizPolicyConsultant pc = userService.getPolicyConsultant();
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {

            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return pc.enforceRegistrationStatusOnCreate();
            }

            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return Arrays.asList(RegistrationStatus.APPROVED);
            }

            @Override
            public RegistrationStatus getDefaultRegistrationStatus() {
                return pc.getDefaultRegistrationStatus();
            }

            @Override
            public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
                return pc.getRolesForRegistrationStatus(status);
            }
        });
        userService.create(person);
        List<Role> personsRoles = new ArrayList<Role>();
        personsRoles.add(Role.ROLE_USER);
        personsRoles.add(Role.ROLE_ADMIN);
        userService.updateRoles(person.getId(), personsRoles);

        final Person updatedPerson = new Person(person);

        final String updatedLastname = "Phooey";

        assertFalse(person.getLastNames().equals(updatedLastname));
        updatedPerson.setLastNames(updatedLastname);

        assertFalse(person.getReadOnly());

        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserProfileActionBean.class, userSession);
        rt.addParameter("editedPerson.id", updatedPerson.getId());
        rt.addParameter("editedPerson.firstNames", updatedPerson.getFirstNames());
        rt.addParameter("editedPerson.lastNames", updatedPerson.getLastNames());
        rt.addParameter("editedPerson.prefix", updatedPerson.getPrefix());
        rt.addParameter("editedPerson.suffix", updatedPerson.getSuffix());
        rt.addParameter("editedPerson.middleNames", updatedPerson.getMiddleNames());
        rt.addParameter("editedPerson.preferredPubName", updatedPerson.getPreferredPubName());
        rt.addParameter("editedPerson.jobTitle", updatedPerson.getJobTitle());
        rt.addParameter("editedPerson.department", updatedPerson.getDepartment());
        rt.addParameter("editedPerson.city", updatedPerson.getCity());
        rt.addParameter("editedPerson.state", updatedPerson.getState());
        rt.addParameter("editedPerson.website", updatedPerson.getWebsite());
        rt.addParameter("editedPerson.bio", updatedPerson.getBio());
        rt.addParameter("editedPerson.instCompany", updatedPerson.getInstCompany());
        rt.addParameter("editedPerson.instCompanyWebsite", updatedPerson.getInstCompanyWebsite());
        rt.addParameter("editedPerson.emailAddress", updatedPerson.getEmailAddress());
        rt.addParameter("editedPerson.phoneNumber", updatedPerson.getPhoneNumber());
        rt.addParameter("editedPerson.externalStorageLinked", Boolean.toString(updatedPerson.isExternalStorageLinked()));
        rt.addParameter("editedPerson.dropboxAppKey", updatedPerson.getDropboxAppKey());
        rt.addParameter("editedPerson.dropboxAppSecret", updatedPerson.getDropboxAppSecret());
        rt.execute("userProfileUpdated");

        assertEquals(302, rt.getResponse().getStatus());
        assertEquals(0, rt.getValidationErrors().size());
        assertEquals(updatedPerson, userService.get(person.getEmailAddress()));
    }

    /**
     * Asserts that a password change is persisted
     * 
     * @throws Exception
     */
    @Test
    public void testChangePassword() throws Exception {
        final Person person = new Person();
        person.setId(idService.create(Types.PERSON.name()).getUrl().toString());
        person.setFirstNames("Funky");
        person.setLastNames("Monk");
        person.setPrefix("Mr.");
        person.setSuffix("II");
        person.setMiddleNames("Middle");
        person.setPreferredPubName("F. Monk");
        person.setBio("Some bio for the user.");
        person.setWebsite("www.somewebsite.com");
        person.setEmailAddress("funk_monk1@gmail.com");
        person.setPassword("foobar");
        person.setPhoneNumber("123456789");
        person.setJobTitle("Funky Scientist");
        person.setDepartment("Funky Department");
        person.setCity("Baltimore");
        person.setState("Maryland");
        person.setInstCompany("Funky Institution/Company");
        person.setInstCompanyWebsite("www.FunkyInstitutionCompanyWebsite.com");
        person.setRegistrationStatus(RegistrationStatus.APPROVED);
        person.setReadOnly(false);
        person.setExternalStorageLinked(false);
        person.setDropboxAppKey("SomeKey");
        person.setDropboxAppSecret("SomeSecret");
        /*
         * person.addRole(Role.ROLE_USER); person.addRole(Role.ROLE_ADMIN);
         */

        final PersonBizPolicyConsultant pc = userService.getPolicyConsultant();
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {

            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return pc.enforceRegistrationStatusOnCreate();
            }

            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return Arrays.asList(RegistrationStatus.APPROVED);
            }

            @Override
            public RegistrationStatus getDefaultRegistrationStatus() {
                return pc.getDefaultRegistrationStatus();
            }

            @Override
            public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
                return pc.getRolesForRegistrationStatus(status);
            }
        });
        userService.create(person);
        List<Role> personsRoles = new ArrayList<Role>();
        personsRoles.add(Role.ROLE_USER);
        personsRoles.add(Role.ROLE_ADMIN);
        userService.updateRoles(person.getId(), personsRoles);

        final Person updatedPerson = new Person(person);

        final String confirmedPassword = "password1267";

        assertFalse(person.getReadOnly());

        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserProfileActionBean.class, userSession);
        rt.addParameter("editedPerson.id", updatedPerson.getId());
        rt.addParameter("editedPerson.firstNames", updatedPerson.getFirstNames());
        rt.addParameter("editedPerson.lastNames", updatedPerson.getLastNames());
        rt.addParameter("editedPerson.prefix", updatedPerson.getPrefix());
        rt.addParameter("editedPerson.suffix", updatedPerson.getSuffix());
        rt.addParameter("editedPerson.middleNames", updatedPerson.getMiddleNames());
        rt.addParameter("editedPerson.preferredPubName", updatedPerson.getPreferredPubName());
        rt.addParameter("editedPerson.jobTitle", updatedPerson.getJobTitle());
        rt.addParameter("editedPerson.department", updatedPerson.getDepartment());
        rt.addParameter("editedPerson.city", updatedPerson.getCity());
        rt.addParameter("editedPerson.state", updatedPerson.getState());
        rt.addParameter("editedPerson.website", updatedPerson.getWebsite());
        rt.addParameter("editedPerson.bio", updatedPerson.getBio());
        rt.addParameter("editedPerson.instCompany", updatedPerson.getInstCompany());
        rt.addParameter("editedPerson.instCompanyWebsite", updatedPerson.getInstCompanyWebsite());
        rt.addParameter("editedPerson.emailAddress", updatedPerson.getEmailAddress());
        rt.addParameter("editedPerson.phoneNumber", updatedPerson.getPhoneNumber());
        rt.addParameter("editedPerson.password", confirmedPassword);
        rt.addParameter("editedPerson.externalStorageLinked", Boolean.toString(updatedPerson.isExternalStorageLinked()));
        rt.addParameter("editedPerson.dropboxAppKey", updatedPerson.getDropboxAppKey());
        rt.addParameter("editedPerson.dropboxAppSecret", updatedPerson.getDropboxAppSecret());
        rt.execute("userProfileUpdated");

        assertEquals(302, rt.getResponse().getStatus());
        assertEquals(0, rt.getValidationErrors().size());

        assertEquals(confirmedPassword, userService.get(updatedPerson.getId())
                .getPassword());

    }
    
    /**
     * Tests the link to the link_dropbox page works. NOTE: This could intermittently throw a connection timeout
     * exception from dropbox, that can be safely ignore as it's beyond the scope of this test.
     * 
     * @throws Exception
     */
    @Test
    public void testLinkDropboxPath() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserProfileActionBean.class, userSession);
        rt.execute("linkDropbox");
        assertEquals(UserProfileActionBean.LINK_DROPBOX_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Tests that the profile gets updated with app key/secret and external storage linked flag is set to true.
     * purposefully not calling the updateProfileWithDropbox method due to Dropbox linkage constraints. This is merely
     * testing that userService updates dropbox app key/secret.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateProfileWithDropbox() throws Exception {
        final Person person = new Person();
        // person.setId("id:funkymonk");
        person.setId(idService.create(Types.PERSON.name()).getUrl().toString());
        person.setFirstNames("Funky");
        person.setLastNames("Monk");
        person.setPrefix("Mr.");
        person.setSuffix("II");
        person.setMiddleNames("Middle");
        person.setPreferredPubName("F. Monk");
        person.setBio("Some bio for the user.");
        person.setWebsite("www.somewebsite.com");
        person.setEmailAddress("funk_monk@gmail.com");
        person.setPassword("foobar");
        person.setPhoneNumber("123456789");
        person.setJobTitle("Funky Scientist");
        person.setDepartment("Funky Department");
        person.setCity("Baltimore");
        person.setState("Maryland");
        person.setInstCompany("Funky Institution/Company");
        person.setInstCompanyWebsite("www.FunkyInstitutionCompanyWebsite.com");
        person.setRegistrationStatus(RegistrationStatus.APPROVED);
        person.setReadOnly(false);
        
        final PersonBizPolicyConsultant pc = userService.getPolicyConsultant();
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {
            
            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return pc.enforceRegistrationStatusOnCreate();
            }
            
            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return Arrays.asList(RegistrationStatus.APPROVED);
            }
            
            @Override
            public RegistrationStatus getDefaultRegistrationStatus() {
                return pc.getDefaultRegistrationStatus();
            }
            
            @Override
            public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
                return pc.getRolesForRegistrationStatus(status);
            }
        });
        userService.create(person);
        List<Role> personsRoles = new ArrayList<Role>();
        personsRoles.add(Role.ROLE_USER);
        personsRoles.add(Role.ROLE_ADMIN);
        userService.updateRoles(person.getId(), personsRoles);
        
        final Person updatedPerson = new Person(person);
        String appKey = "2lhwkjh23234";
        String appSecret = "223kjhakjholol234";
        assertFalse(person.getReadOnly());
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserProfileActionBean.class, userSession);
        rt.addParameter("editedPerson.id", updatedPerson.getId());
        rt.addParameter("editedPerson.firstNames", updatedPerson.getFirstNames());
        rt.addParameter("editedPerson.lastNames", updatedPerson.getLastNames());
        rt.addParameter("editedPerson.prefix", updatedPerson.getPrefix());
        rt.addParameter("editedPerson.suffix", updatedPerson.getSuffix());
        rt.addParameter("editedPerson.middleNames", updatedPerson.getMiddleNames());
        rt.addParameter("editedPerson.preferredPubName", updatedPerson.getPreferredPubName());
        rt.addParameter("editedPerson.jobTitle", updatedPerson.getJobTitle());
        rt.addParameter("editedPerson.department", updatedPerson.getDepartment());
        rt.addParameter("editedPerson.city", updatedPerson.getCity());
        rt.addParameter("editedPerson.state", updatedPerson.getState());
        rt.addParameter("editedPerson.website", updatedPerson.getWebsite());
        rt.addParameter("editedPerson.bio", updatedPerson.getBio());
        rt.addParameter("editedPerson.instCompany", updatedPerson.getInstCompany());
        rt.addParameter("editedPerson.instCompanyWebsite", updatedPerson.getInstCompanyWebsite());
        rt.addParameter("editedPerson.emailAddress", updatedPerson.getEmailAddress());
        rt.addParameter("editedPerson.phoneNumber", updatedPerson.getPhoneNumber());
        rt.addParameter("editedPerson.externalStorageLinked", Boolean.toString(true));
        rt.addParameter("editedPerson.dropboxAppKey", appKey);
        rt.addParameter("editedPerson.dropboxAppSecret", appSecret);
        rt.execute("userProfileUpdated");
        
        assertEquals(302, rt.getResponse().getStatus());
        assertEquals(appKey, userService.get(updatedPerson.getId()).getDropboxAppKey());
        assertEquals(appSecret, userService.get(updatedPerson.getId()).getDropboxAppSecret());
        assertTrue(userService.get(updatedPerson.getId()).isExternalStorageLinked());
        
    }
}
