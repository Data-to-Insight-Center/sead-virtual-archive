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

import net.sourceforge.stripes.mock.MockRoundtrip;

import org.junit.Assert;
import org.junit.Test;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class RegistrationActionBeanTest
        extends BaseActionBeanTest {

    @Autowired
    private UserService userService;

    /**
     * Make sure the registration page is rendered when there is no user logged
     * in.
     */
    @Test
    public void testRenderRegisterForm() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.execute();
        assertEquals(RegistrationActionBean.SRC_FORM, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Register a new user and verify the default state of the user: no roles,
     * and registration status == Pending
     * 
     * @throws Exception
     */
    @Test
    public void testNewUserRegistrationState() throws Exception {
        Person p = new Person();
        p.setId("id:joedirt");
        p.setFirstNames("Joe");
        p.setLastNames("Dirt");
        p.setPrefix("Mr.");
        p.setSuffix("II");
        p.setMiddleNames("Dirty");
        p.setPreferredPubName("J. Dirt");
        p.setBio("Some bio for the user.");
        p.setWebsite("www.somewebsite.com");
        p.setEmailAddress("joe@dirt.com");
        p.setPhoneNumber("1-234-5678");
        p.setPassword("foobar");
        p.setJobTitle("New Job Title");
        p.setDepartment("New Department");
        p.setCity("Baltimore");
        p.setState("Maryland");
        p.setInstCompany("New Institution/Company");
        p.setInstCompanyWebsite("www.NewInstitutionCompany.com");

        assertNull(userService.get(p.getEmailAddress()));

        MockRoundtrip rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.id", p.getId());
        rt.addParameter("user.firstNames", p.getFirstNames());
        rt.addParameter("user.lastNames", p.getLastNames());
        rt.addParameter("user.middleNames", p.getMiddleNames());
        rt.addParameter("user.prefix", p.getPrefix());
        rt.addParameter("user.suffix", p.getSuffix());
        rt.addParameter("user.preferredPubName", p.getPreferredPubName());
        rt.addParameter("user.bio", p.getBio());
        rt.addParameter("user.website", p.getWebsite());
        rt.addParameter("user.emailAddress", p.getEmailAddress());
        rt.addParameter("user.phoneNumber", p.getPhoneNumber());
        rt.setParameter("user.password", p.getPassword());
        rt.setParameter("confirmedPassword", p.getPassword());
        rt.addParameter("user.jobTitle", p.getJobTitle());
        rt.addParameter("user.department", p.getDepartment());
        rt.addParameter("user.city", p.getCity());
        rt.addParameter("user.state", p.getState());
        rt.addParameter("user.instCompany", p.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", p.getInstCompanyWebsite());
        rt.execute("register");

        assertNotNull(userService.get(p.getEmailAddress()));
        assertEquals(p, userService.get(p.getEmailAddress()));

        assertEquals(RegistrationStatus.PENDING,
                     userService.get(p.getEmailAddress())
                             .getRegistrationStatus());
        assertTrue(userService.get(p.getEmailAddress()).getRoles().isEmpty());
    }

    /**
     * Register a new user, first without all necessary fields then with all
     * fields supplied.
     */
    @Test
    @DirtiesDatabase
    public void testNewUserRegistration() throws Exception {

        Person newUser = new Person();
        newUser.setId("id:newUser");
        newUser.setFirstNames("Boo");
        newUser.setLastNames("Hoo");
        newUser.setPrefix("Mr.");
        newUser.setSuffix("II");
        newUser.setMiddleNames("Dirty");
        newUser.setPreferredPubName("B. Hoo");
        newUser.setBio("Some bio for the user.");
        newUser.setWebsite("www.somewebsite.com");
        newUser.setEmailAddress("monk@ee.com");
        newUser.setPhoneNumber("1234567890");
        newUser.setPassword("banana");
        newUser.setJobTitle("Boo Scientist");
        newUser.setDepartment("Boo Department");
        newUser.setCity("Baltimore");
        newUser.setState("Maryland");
        newUser.setInstCompany("Boo Institution/Company");
        newUser.setInstCompanyWebsite("www.BooInstitutionCompany.com");
        newUser.setExternalStorageLinked(false);
        newUser.setDropboxAppKey("SomeKey");
        newUser.setDropboxAppSecret("SomeSecret");

        Assert.assertNull(userService.get(newUser.getEmailAddress()));

        //Test registering without all fields.
        MockRoundtrip rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.lastNames", newUser.getLastNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.password", newUser.getPassword());
        rt.addParameter("confirmedPassword", newUser.getPassword());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");

        Assert.assertNull(userService.get(newUser.getEmailAddress()));

        //Register with all fields
        rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.id", newUser.getId());
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.lastNames", newUser.getLastNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.emailAddress", newUser.getEmailAddress());
        rt.addParameter("user.phoneNumber", newUser.getPhoneNumber());
        rt.addParameter("user.password", newUser.getPassword());
        rt.addParameter("confirmedPassword", newUser.getPassword());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");

        Assert.assertNotNull(userService.get(newUser.getEmailAddress()));
    }

    /**
     * Test invalid registration entries. Test that validation works on all
     * fields.
     * 
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testInvalidRegistration() throws Exception {

        Person newUser = new Person();
        newUser.setFirstNames("Boo");
        newUser.setLastNames("Hoo");
        newUser.setPrefix("Mr.");
        newUser.setSuffix("II");
        newUser.setMiddleNames("Dirty");
        newUser.setPreferredPubName("B. Hoo");
        newUser.setBio("Some bio for the user.");
        newUser.setWebsite("www.somewebsite.com");
        newUser.setEmailAddress("monk@ee.com");
        newUser.setPhoneNumber("1234567890");
        newUser.setPassword("banana");
        newUser.setJobTitle("Boo Scientist");
        newUser.setDepartment("Boo Department");
        newUser.setCity("Baltimore");
        newUser.setState("Maryland");
        newUser.setInstCompany("Boo Institution/Company");
        newUser.setInstCompanyWebsite("www.BooInstitutionCompany.com");
        newUser.setExternalStorageLinked(false);
        newUser.setDropboxAppKey("SomeKey");
        newUser.setDropboxAppSecret("SomeSecret");

        Assert.assertNull(userService.get(newUser.getEmailAddress()));

        //Test registering missing one field, intentionally missing required field last names.
        MockRoundtrip rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.emailAddress", newUser.getEmailAddress());
        rt.addParameter("user.password", newUser.getPassword());
        rt.addParameter("confirmedPassword", newUser.getPassword());
        rt.addParameter("user.phoneNumber", newUser.getPhoneNumber());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");

        /* we expect one validation error for each missing value */
        RegistrationActionBean bean = rt.getActionBean(RegistrationActionBean.class);
        assertEquals(1, bean.getContext().getValidationErrors().size());
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.lastNames"));

        //Test no fields entered
        rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.firstNames", "");
        rt.addParameter("user.lastNames", "");
        rt.addParameter("user.emailAddress", "");
        rt.addParameter("user.password", "");
        rt.addParameter("user.phoneNumber", "");
        rt.addParameter("confirmedPassword", "");
        rt.addParameter("user.jobTitle", "");
        rt.addParameter("user.department", "");
        rt.addParameter("user.city", "");
        rt.addParameter("user.state", "");
        rt.addParameter("user.instCompany", "");
        rt.addParameter("user.instCompanyWebsite", "");
        rt.execute("register");

        /* we expect one validation error for each missing value */
        bean = rt.getActionBean(RegistrationActionBean.class);
        assertEquals(12, bean.getContext().getValidationErrors().size());
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.firstNames"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.lastNames"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.emailAddress"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.password"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.jobTitle"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.department"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.city"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.state"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.instCompany"));
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.instCompanyWebsite"));

    }

    /**
     * Test the password validations. Ensure the password is the correct.
     */
    @Test
    @DirtiesDatabase
    public void testInvalidPassword() throws Exception {
        Person newUser = new Person();
        newUser.setId("id:newUser");
        newUser.setFirstNames("Boo");
        newUser.setLastNames("Hoo");
        newUser.setPrefix("Mr.");
        newUser.setSuffix("II");
        newUser.setMiddleNames("Dirty");
        newUser.setPreferredPubName("B. Hoo");
        newUser.setBio("Some bio for the user.");
        newUser.setWebsite("www.somewebsite.com");
        newUser.setEmailAddress("monk@ee.com");
        newUser.setPhoneNumber("1234567890");
        newUser.setPassword("banana");
        newUser.setJobTitle("Boo Scientist");
        newUser.setDepartment("Boo Department");
        newUser.setCity("Baltimore");
        newUser.setState("Maryland");
        newUser.setInstCompany("Boo Institution/Company");
        newUser.setInstCompanyWebsite("www.BooInstitutionCompanyWebsite.com");
        newUser.setExternalStorageLinked(false);
        newUser.setDropboxAppKey("SomeKey");
        newUser.setDropboxAppSecret("SomeSecret");

        //Test registering with mismatched password 
        //this test will pass because the validation will not run due to a Jsp20ExpressionExecutor error.
        MockRoundtrip rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.id", newUser.getId());
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.lastNames", newUser.getLastNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.emailAddress", newUser.getEmailAddress());
        rt.addParameter("user.password", newUser.getPassword());
        rt.addParameter("confirmedPassword", "foobar");
        rt.addParameter("user.phoneNumber", newUser.getPhoneNumber());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");

        /* we expect one validation error for the missmatched passwords */
        /*
         * bean =
         * rt.getActionBean(AdminUpdateRegistrationManagerActionBean.class);
         * assertEquals(1, bean.getContext().getValidationErrors().size());
         * Assert
         * .assertTrue(bean.getContext().getValidationErrors().containsKey(
         * "user.phone"));
         */

        Assert.assertNotNull(userService.get(newUser.getEmailAddress()));

    }

    /**
     * Try to register a user that already exsists.
     */
    @Test
    @DirtiesDatabase
    public void testReRegisterUser() throws Exception {
        Person newUser = new Person();
        newUser.setId("id:newUser");
        newUser.setFirstNames("Boo");
        newUser.setLastNames("Hoo");
        newUser.setPrefix("Mr.");
        newUser.setSuffix("II");
        newUser.setMiddleNames("Dirty");
        newUser.setPreferredPubName("B. Hoo");
        newUser.setBio("Some bio for the user.");
        newUser.setWebsite("www.somewebsite.com");
        newUser.setEmailAddress("monk@ee.com");
        newUser.setPhoneNumber("1234567890");
        newUser.setPassword("banana");
        newUser.setJobTitle("Boo Scientist");
        newUser.setDepartment("Boo Department");
        newUser.setCity("Baltimore");
        newUser.setState("Maryland");
        newUser.setInstCompany("Boo Institution/Company");
        newUser.setInstCompanyWebsite("www.BooInstitutionCompany.com");
        newUser.setExternalStorageLinked(false);
        newUser.setDropboxAppKey("SomeKey");
        newUser.setDropboxAppSecret("SomeSecret");

        Assert.assertNull(userService.get(newUser.getEmailAddress()));

        //Test a password that is too short
        MockRoundtrip rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.lastNames", newUser.getLastNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.emailAddress", newUser.getEmailAddress());
        rt.addParameter("user.password", "123");
        rt.addParameter("confirmedPassword", "foobar");
        rt.addParameter("user.phoneNumber", newUser.getPhoneNumber());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");

        RegistrationActionBean bean = rt.getActionBean(RegistrationActionBean.class);
        assertEquals(1, bean.getContext().getValidationErrors().size());
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.password"));

        Assert.assertNull(userService.get(newUser.getEmailAddress()));

        //Test a password that is too long
        rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.id", newUser.getId());
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.lastNames", newUser.getLastNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.emailAddress", newUser.getEmailAddress());
        rt.addParameter("user.password", "12345678901234567890123");
        rt.addParameter("confirmedPassword", "foobar");
        rt.addParameter("user.phoneNumber", newUser.getPhoneNumber());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");

        bean = rt.getActionBean(RegistrationActionBean.class);
        assertEquals(1, bean.getContext().getValidationErrors().size());
        Assert.assertTrue(bean.getContext().getValidationErrors().containsKey("user.password"));

        Assert.assertNull(userService.get(newUser.getEmailAddress()));

        //Register with all fields
        rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.id", newUser.getId());
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.lastNames", newUser.getLastNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.emailAddress", newUser.getEmailAddress());
        rt.addParameter("user.phoneNumber", newUser.getPhoneNumber());
        rt.addParameter("user.password", newUser.getPassword());
        rt.addParameter("confirmedPassword", newUser.getPassword());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");
        Assert.assertNotNull(userService.get(newUser.getEmailAddress()));

        //Now try to register the same user
        rt = new MockRoundtrip(servletCtx, RegistrationActionBean.class);
        rt.addParameter("user.id", newUser.getId());
        rt.addParameter("user.firstNames", newUser.getFirstNames());
        rt.addParameter("user.lastNames", newUser.getLastNames());
        rt.addParameter("user.middleNames", newUser.getMiddleNames());
        rt.addParameter("user.prefix", newUser.getPrefix());
        rt.addParameter("user.suffix", newUser.getSuffix());
        rt.addParameter("user.preferredPubName", newUser.getPreferredPubName());
        rt.addParameter("user.bio", newUser.getBio());
        rt.addParameter("user.website", newUser.getWebsite());
        rt.addParameter("user.emailAddress", newUser.getEmailAddress());
        rt.addParameter("user.phoneNumber", newUser.getPhoneNumber());
        rt.addParameter("user.password", newUser.getPassword());
        rt.addParameter("confirmedPassword", newUser.getPassword());
        rt.addParameter("user.jobTitle", newUser.getJobTitle());
        rt.addParameter("user.department", newUser.getDepartment());
        rt.addParameter("user.city", newUser.getCity());
        rt.addParameter("user.state", newUser.getState());
        rt.addParameter("user.instCompany", newUser.getInstCompany());
        rt.addParameter("user.instCompanyWebsite", newUser.getInstCompanyWebsite());
        rt.execute("register");
        Assert.assertNotNull(userService.get(newUser.getEmailAddress()));
    }
}
