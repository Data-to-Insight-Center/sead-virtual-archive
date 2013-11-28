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

package org.dataconservancy.ui.services;

import org.dataconservancy.ui.exceptions.PersonUpdateException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.Arrays;
import java.util.Collections;

import static org.dataconservancy.ui.model.RegistrationStatus.PENDING;
import static org.junit.Assert.*;

/**
 * The purpose of this test is to document the production policy behaviors of
 * the UserService.
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class})
public class UserServicePolicyIT
        implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private UserService underTest;

    @Before
    public void setUp() {
        // TODO: figure out a better way to obtain the production runtime impl of the user service being used.
        // this is probably not the user service running in jetty, but this does give us access to the
        // policy consultant
        underTest =
                applicationContext.getBean("delegatingUserService",
                                           UserService.class);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Insures that Person with an empty roles list who is created has their
     * roles set according to their registration status.
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePersonWithEmptyRoles() throws Exception {
        Person toCreate = new Person();
        toCreate.setEmailAddress("choo5@choo.com");
        toCreate.setFirstNames("Choo");
        toCreate.setLastNames("ChooTrain");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("C. ChooTrain");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Choo Scientist");
        toCreate.setDepartment("Choo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Choo Institution/Company");
        toCreate.setInstCompanyWebsite("www.ChooInstitutionCompany.com");
        toCreate.setRegistrationStatus(PENDING);
        toCreate.setId("http:UserServicePolicyIT-2");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        // Set their roles to an empty list
        toCreate.setRoles(Collections.<Role> emptyList());

        // Attempt to create the person.
        Person created = underTest.create(toCreate);

        // Assert that they have a role that maps to their registration status
        assertEquals(underTest
                             .getPolicyConsultant()
                             .getRolesForRegistrationStatus(created.getRegistrationStatus()),
                     created.getRoles());

    }

    /**
     * Insures that Person with a {@code null} roles list who is created has
     * their roles set according to their registration status.
     * <p/>
     * Right now, the Person class doesn't allow a null list to be set on it, so
     * this test throws an IAE. we leave the test here to document this
     * behavior, and if the IAE behavior changes, this test will have to be
     * updated.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreatePersonWithNullRoles() throws Exception {
        Person toCreate = new Person();
        toCreate.setEmailAddress("choo6@choo.com");
        toCreate.setFirstNames("Choo");
        toCreate.setLastNames("ChooTrain");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("C. ChooTrain");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Choo Scientist");
        toCreate.setDepartment("Choo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Choo Institution/Company");
        toCreate.setInstCompanyWebsite("www.ChooInstitutionCompany.com");
        toCreate.setRegistrationStatus(PENDING);
        toCreate.setId("http:UserServicePolicyIT-3");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        // Set their roles to a null list (currently produces an IAE)
        toCreate.setRoles(null);

        // Attempt to create the person.
        Person created = underTest.create(toCreate);

        // Assert that they have a role that maps to their registration status
        assertEquals(underTest
                             .getPolicyConsultant()
                             .getRolesForRegistrationStatus(created.getRegistrationStatus()),
                     created.getRoles());
    }

    /**
     * Insures that Person with existing roles list who is created has their
     * roles set according to their registration status.
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePersonWithExistingRoles() throws Exception {
        Person toCreate = new Person();
        toCreate.setEmailAddress("choo7@choo.com");
        toCreate.setFirstNames("Choo");
        toCreate.setLastNames("ChooTrain");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("C. ChooTrain");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Choo Scientist");
        toCreate.setDepartment("Choo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Choo Institution/Company");
        toCreate.setInstCompanyWebsite("www.ChooInstitutionCompany.com");
        toCreate.setRegistrationStatus(PENDING);
        toCreate.setId("http:UserServicePolicyIT-4");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        // Set their roles to a list that should never be set by policy (e.g. persons shouldn't ever
        // be able to be created with a ROLE_ADMIN role)
        toCreate.setRoles(Arrays.asList(Role.ROLE_ADMIN));

        // Attempt to create the person.
        Person created = underTest.create(toCreate);

        // Assert that they have a role that maps to their registration status
        assertEquals(underTest
                             .getPolicyConsultant()
                             .getRolesForRegistrationStatus(created.getRegistrationStatus()),
                     created.getRoles());

        // And for good measure, make sure that it doesn't include Role.ROLE_ADMIN.
        assertFalse(created.getRoles().contains(Role.ROLE_ADMIN));
    }

    /**
     * Insures that Person with a null registration status is created and
     * assigned the default registration status.
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePersonWithNullRegistrationStatus() throws Exception {
        Person toCreate = new Person();
        toCreate.setEmailAddress("choo8@choo.com");
        toCreate.setFirstNames("Choo");
        toCreate.setLastNames("ChooTrain");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("C. ChooTrain");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Choo Scientist");
        toCreate.setDepartment("Choo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Choo Institution/Company");
        toCreate.setInstCompanyWebsite("www.ChooInstitutionCompany.com");
        toCreate.setId("http:UserServicePolicyIT-5");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        // Verify assumptions
        assertNull(toCreate.getRegistrationStatus());

        // Attempt to create the person.
        Person created = underTest.create(toCreate);

        // Assert that they have a registration status equal to the default registration status
        assertEquals(underTest.getPolicyConsultant()
                             .getDefaultRegistrationStatus(),
                     created.getRegistrationStatus());
    }

    /**
     * Insures that Person with a registration status different from the default
     * raises an exception.
     * 
     * @throws Exception
     */
    @Test(expected = PersonUpdateException.class)
    public void testCreatePersonWithNonDefaultRegistrationStatus()
            throws Exception {
        Person toCreate = new Person();
        toCreate.setEmailAddress("choo9@choo.com");
        toCreate.setFirstNames("Choo");
        toCreate.setLastNames("ChooTrain");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("C. ChooTrain");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("foobarpass");
        toCreate.setPhoneNumber("1234567890");
        toCreate.setJobTitle("Choo Scientist");
        toCreate.setDepartment("Choo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Choo Institution/Company");
        toCreate.setInstCompanyWebsite("www.ChooInstitutionCompany.com");
        toCreate.setId("http:UserServicePolicyIT-6");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        // Set the person's registration status to a not-default, non-null registration status
        for (RegistrationStatus candidate : RegistrationStatus.values()) {
            if (!candidate.equals(underTest.getPolicyConsultant()
                    .getDefaultRegistrationStatus())) {
                toCreate.setRegistrationStatus(candidate);
                break;
            }
        }

        // Verify above logic worked.
        assertNotNull(toCreate.getRegistrationStatus());
        assertFalse(toCreate.getRegistrationStatus().equals(underTest
                .getPolicyConsultant().getDefaultRegistrationStatus()));

        // Attempt to create the person.
        underTest.create(toCreate);
    }

    /**
     * Ensure that trying to create the same person twice raises an exception
     * 
     * @throws Exception
     */
    @Test(expected = DuplicateKeyException.class)
    public void testCantCreateSameUserTwice() throws Exception {
        Person toCreate = new Person();
        toCreate.setEmailAddress("UserServicePolicyIT-7@imagination.com");
        toCreate.setFirstNames("Adam");
        toCreate.setLastNames("Apocalypse");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("A. Apocalypse");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setPassword("youshallnot");
        toCreate.setPhoneNumber("8003253535");
        toCreate.setJobTitle("Adam Scientist");
        toCreate.setDepartment("Adam Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Adam Institution/Company");
        toCreate.setInstCompanyWebsite("www.AdamInstitutionCompany.com");
        toCreate.setRegistrationStatus(RegistrationStatus.PENDING);
        toCreate.setId("http:UserServicePolicyIT-7");
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        // Attempt to create the person.
        Person created = underTest.create(toCreate);

        //make sure the user is created
        assertEquals(toCreate, created);

        //attempt to create the user again
        Person recreated = underTest.create(toCreate);

        assertEquals(toCreate, recreated);
    }
}
