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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.exceptions.PersonUpdateException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.IdBizPolicyConsultant;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * An abstract test class for implementations of the UserService API. This class
 * (and test subclasses) should be kept up-to-date as the contract of the
 * UserService API evolves.
 */
public abstract class BaseUserServiceTest extends BaseUnitTest {

    //TODO: Add tests for the additional Person fields.

    /**
     * This field is made protected so the underTest object can be accessible in
     * subclasses.
     */
    protected UserService underTest;

    private Map<String, Person> users = new HashMap<String, Person>();

    private Person one = new Person();

    private Person two = new Person();

    @Autowired
    private IdBizPolicyConsultant idBizPolicyConsultant;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Before
    public void setUp() {

        one.setId("http:id:one");
        one.setFirstNames("Spider");
        one.setLastNames("Monkey");
        one.setPrefix("Mr.");
        one.setSuffix("II");
        one.setMiddleNames("Spiderish");
        one.setPreferredPubName("S. Monkey");
        one.setBio("Some bio for the user.");
        one.setWebsite("www.somewebsite.com");
        one.setEmailAddress("monkey@zoo.com");
        one.setPassword("password");
        one.setPhoneNumber("555555555");
        one.setJobTitle("Monkey Scientist");
        one.setDepartment("Monkey Department");
        one.setCity("Baltimore");
        one.setState("Maryland");
        one.setInstCompany("Monkey Institution/Company");
        one.setInstCompanyWebsite("www.MonkeyInstitutionCompany.com");
        one.setRegistrationStatus(RegistrationStatus.APPROVED);
        one.setExternalStorageLinked(false);
        one.setDropboxAppKey("SomeKey");
        one.setDropboxAppSecret("SomeSecret");

        two.setId("http:id:two");
        two.setFirstNames("Hairy");
        two.setLastNames("Gorilla");
        two.setPrefix("Mr.");
        two.setSuffix("II");
        two.setMiddleNames("hairyish");
        two.setPreferredPubName("H. Gorilla");
        two.setBio("Some bio for the user.");
        two.setWebsite("www.somewebsite.com");
        two.setEmailAddress("bananas@gmail.com");
        two.setPassword("password");
        two.setJobTitle("Hairy Scientist");
        two.setDepartment("Hairy Department");
        two.setCity("Baltimore");
        two.setState("Maryland");
        two.setInstCompany("Hairy Institution/Company");
        two.setInstCompanyWebsite("www.HairyInstitutionCompany.com");
        two.setPhoneNumber("555555555");
        two.setExternalStorageLinked(false);
        two.setDropboxAppKey("SomeKey");
        two.setDropboxAppSecret("SomeSecret");

        users.put(one.getId(), one);
        users.put(two.getId(), two);

        // pass a defensively-copied map so the subclass can't modify the users (this may change in the future)
        Map<String, Person> copy = new HashMap<String, Person>();
        for (Map.Entry<String, Person> toCopy : users.entrySet()) {
            Person personToCopy = toCopy.getValue();
            Person copiedPerson = new Person();
            copiedPerson.setId(personToCopy.getId());
            copiedPerson.setFirstNames(personToCopy.getFirstNames());
            copiedPerson.setLastNames(personToCopy.getLastNames());
            copiedPerson.setMiddleNames(personToCopy.getMiddleNames());
            copiedPerson.setPrefix(personToCopy.getPrefix());
            copiedPerson.setSuffix(personToCopy.getSuffix());
            copiedPerson.setPreferredPubName(personToCopy.getPreferredPubName());
            copiedPerson.setBio(personToCopy.getBio());
            copiedPerson.setWebsite(personToCopy.getWebsite());
            copiedPerson.setJobTitle(personToCopy.getJobTitle());
            copiedPerson.setDepartment(personToCopy.getDepartment());
            copiedPerson.setInstCompany(personToCopy.getInstCompany());
            copiedPerson.setInstCompanyWebsite(personToCopy.getInstCompanyWebsite());
            copiedPerson.setCity(personToCopy.getCity());
            copiedPerson.setState(personToCopy.getState());
            copiedPerson.setEmailAddress(personToCopy.getEmailAddress());
            copiedPerson.setPassword(personToCopy.getPassword());
            copiedPerson.setComparator(personToCopy.getComparator());
            copiedPerson.setPhoneNumber(personToCopy.getPhoneNumber());
            copiedPerson.setRegistrationStatus(personToCopy
                    .getRegistrationStatus());
            copiedPerson.setRoles(personToCopy.getRoles());
            copiedPerson.setExternalStorageLinked(personToCopy.isExternalStorageLinked());
            copiedPerson.setDropboxAppKey(personToCopy.getDropboxAppKey());
            copiedPerson.setDropboxAppSecret(personToCopy.getDropboxAppSecret());
            copy.put(toCopy.getKey(), copiedPerson);
        }

        underTest =
                getUnderTest(copy,
                             new AcceptApprovedAndPendingUsers(),
                             idBizPolicyConsultant,
                             idService);
    }

    /**
     * Subclasses are expected to return an instance of the UserService that is
     * under test. This method is called once for each test method. It is
     * expected that each invokation of this method will return a
     * <code>new</code> UserService, populated with the provided users, and use
     * the provided {@link PersonBizPolicyConsultant},
     * {@link IdBizPolicyConsultant}, and {@link IdService}.
     * <p/>
     * This class supplies the policy consultants because the policy will (or
     * should) influence the behavior of the implementation that is under test.
     * This class needs to be in control of the behavior of the class under test
     * so it behaves predictably.
     * 
     * @param usersToLoad
     *        the users to be loaded into the service
     * @param personBizPolicyConsultant
     *        the person business policy consultant to be used by the
     *        implementation under test
     * @param idBizPolicyConsultant
     *        the identifier business policy consultant to be used by the
     *        implementation under test
     * @param idService
     *        the identifier service to be used by the implementation under test
     * @return a <code>new</code> UserService for testing
     */
    public abstract UserService getUnderTest(Map<String, Person> usersToLoad,
                                             PersonBizPolicyConsultant personBizPolicyConsultant,
                                             IdBizPolicyConsultant idBizPolicyConsultant,
                                             IdService idService);

    /**
     * Test getting an existing user
     */
    @Test
    public void testGetExistingUser() {
        // verify assumption
        assertTrue(users.containsKey("http:id:one"));

        assertNotNull(underTest.get("http:id:one"));
        assertEquals(one, underTest.get("http:id:one"));

        assertNotNull(underTest.get(one.getId()));
        assertEquals(one, underTest.get(one.getId()));
    }

    /**
     * Test getting a non-existent user
     */
    @Test
    public void testGetNonExistingUser() {
        // verify assumption
        assertFalse(users.containsKey("foo@bar.biz"));

        assertNull(underTest.get("foo@bar.biz"));
    }

    /**
     * Test creating a non-existent user
     */
    @Test
    public void testCreateNonExistingUser() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));
        assertNull(underTest.get("1234"));

        Person toCreate = new Person();
        toCreate.setId("1234");
        toCreate.setFirstNames("Foo");
        toCreate.setLastNames("Bar");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("F. Bar");
        toCreate.setBio("Some bio for the user.");
        toCreate.setWebsite("www.somewebsite.com");
        toCreate.setEmailAddress("foo@bar.biz");
        toCreate.setPassword("password");
        toCreate.setPhoneNumber("5555555");
        toCreate.setJobTitle("Foo Scientist");
        toCreate.setDepartment("Foo Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Foo Institution/Company");
        toCreate.setInstCompanyWebsite("www.FooInstitutionCompany.com");
        toCreate.setRegistrationStatus(RegistrationStatus.APPROVED);
        toCreate.setExternalStorageLinked(false);
        toCreate.setDropboxAppKey("SomeKey");
        toCreate.setDropboxAppSecret("SomeSecret");

        underTest.create(toCreate);

        assertEquals(toCreate, underTest.get(toCreate.getEmailAddress()));
    }

    /**
     * Test creating a user that is identical to an existing user. This test is
     * mostly about documenting existing behavior.
     */
    @Test
    public void testCreateIdenticalExistingUser() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        Person toCreate = new Person();
        toCreate.setId(one.getId());
        toCreate.setFirstNames(one.getFirstNames());
        toCreate.setLastNames(one.getLastNames());
        toCreate.setPrefix(one.getPrefix());
        toCreate.setSuffix(one.getSuffix());
        toCreate.setMiddleNames(one.getMiddleNames());
        toCreate.setPreferredPubName(one.getPreferredPubName());
        toCreate.setBio(one.getBio());
        toCreate.setWebsite(one.getWebsite());
        toCreate.setEmailAddress(one.getEmailAddress());
        toCreate.setPhoneNumber(one.getPhoneNumber());
        toCreate.setPassword(one.getPassword());
        toCreate.setJobTitle(one.getJobTitle());
        toCreate.setDepartment(one.getDepartment());
        toCreate.setCity(one.getCity());
        toCreate.setState(one.getState());
        toCreate.setInstCompany(one.getInstCompany());
        toCreate.setInstCompanyWebsite(one.getInstCompanyWebsite());
        toCreate.setComparator(one.getComparator());
        toCreate.setRegistrationStatus(one.getRegistrationStatus());
        toCreate.setRoles(one.getRoles());
        toCreate.setExternalStorageLinked(one.isExternalStorageLinked());
        toCreate.setDropboxAppKey(one.getDropboxAppKey());
        toCreate.setDropboxAppSecret(one.getDropboxAppSecret());

        // Person doesn't implement Cloneable and it doesn't have a copy constructor, so at least
        // make sure Java thinks they're equal.
        assertEquals(one, toCreate);

        underTest.create(toCreate);

        assertEquals(one, underTest.get(toCreate.getEmailAddress()));
    }

    /**
     * Test creating a user that already exists, but the user to be created is
     * modified from the user of the same id. This test is mostly about
     * documenting existing behavior.
     */
    @Test
    public void testCreateModifiedExistingUser() {
        // verify assumption
        assertEquals(one, underTest.get(one.getId()));

        Person toCreate = new Person();
        toCreate.setFirstNames(one.getFirstNames());
        toCreate.setLastNames(one.getLastNames() + "some extra stuff");
        toCreate.setPrefix(one.getPrefix());
        toCreate.setSuffix(one.getSuffix());
        toCreate.setMiddleNames(one.getMiddleNames());
        toCreate.setPreferredPubName(one.getPreferredPubName());
        toCreate.setBio(one.getBio());
        toCreate.setWebsite(one.getWebsite());
        toCreate.setEmailAddress(one.getEmailAddress());
        toCreate.setPassword(one.getPassword());
        toCreate.setPhoneNumber(one.getPhoneNumber());
        toCreate.setJobTitle(one.getJobTitle());
        toCreate.setDepartment(one.getDepartment());
        toCreate.setCity(one.getCity());
        toCreate.setState(one.getState());
        toCreate.setInstCompany(one.getInstCompany());
        toCreate.setInstCompanyWebsite(one.getInstCompanyWebsite());
        toCreate.setComparator(one.getComparator());
        toCreate.setRegistrationStatus(one.getRegistrationStatus());
        toCreate.setRoles(one.getRoles());
        toCreate.setId(one.getId());
        toCreate.setExternalStorageLinked(one.isExternalStorageLinked());
        toCreate.setDropboxAppKey(one.getDropboxAppKey());
        toCreate.setDropboxAppSecret(one.getDropboxAppSecret());

        // Ensure the users aren't equal
        assertFalse(one.equals(toCreate));

        underTest.create(toCreate);

        assertEquals(toCreate, underTest.get(toCreate.getId()));
        assertFalse(one.equals(toCreate));
    }

    /**
     * Update a user's registration status that exists in the user service
     */
    @Test
    public void testUpdateExistingUserStatus() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setRegistrationStatus(RegistrationStatus.BLACK_LISTED);

        underTest.updateRegistrationStatus(one.getId(),
                                           toUpdate.getRegistrationStatus());

        assertTrue(toUpdate.getRegistrationStatus().equals(underTest
                .get(one.getEmailAddress()).getRegistrationStatus()));

        //Update the person using the email address
        toUpdate = new Person(one);
        toUpdate.setRegistrationStatus(RegistrationStatus.PENDING);

        underTest.updateRegistrationStatus(one.getEmailAddress(),
                                           toUpdate.getRegistrationStatus());

        assertTrue(toUpdate.equals(underTest.get(one.getEmailAddress())));
    }

    /**
     * Update a user's registration status that doesn't exist in the user
     * service Mostly this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserStatus() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setFirstNames("Foo");
        toUpdate.setLastNames("Bar");
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setRegistrationStatus(RegistrationStatus.APPROVED);

        boolean caughtException = false;
        try {
            underTest
                    .updateRegistrationStatus(toUpdate.getEmailAddress(),
                                              toUpdate.getRegistrationStatus());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's first name that exists in the user service
     */
    @Test
    public void testUpdateExistingUserFirstName() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setFirstNames("new first name");

        assertFalse(one.equals(toUpdate));

        underTest.updateFirstNames(one.getId(), toUpdate.getFirstNames());

        assertTrue(toUpdate.getFirstNames().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getFirstNames()));

        //Update the person using the email address
        toUpdate = new Person(one);
        toUpdate.setFirstNames("another new first name");

        assertFalse(one.equals(toUpdate));
        underTest.updateFirstNames(one.getEmailAddress(),
                                   toUpdate.getFirstNames());

        assertTrue(toUpdate.getFirstNames().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getFirstNames()));
    }

    /**
     * Update a user's last name that doesn't exist in the user service Mostly
     * this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserFirstName() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setFirstNames("Foo");
        toUpdate.setLastNames("Bar");
        toUpdate.setEmailAddress("foo@bar.biz");

        boolean caughtException = false;
        try {
            underTest.updateFirstNames(toUpdate.getEmailAddress(),
                                       toUpdate.getFirstNames());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's middle name that exists in the user service
     */
    @Test
    public void testUpdateExistingUserMiddleName() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setMiddleNames("new middle name");

        assertFalse(one.equals(toUpdate));

        underTest.updateMiddleNames(one.getId(), toUpdate.getMiddleNames());

        assertTrue(toUpdate.getMiddleNames().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getMiddleNames()));

        //Update the person using the email address
        toUpdate = new Person(one);
        toUpdate.setMiddleNames("another new middle name");

        assertFalse(one.equals(toUpdate));
        underTest.updateMiddleNames(one.getEmailAddress(),
                                    toUpdate.getMiddleNames());

        assertTrue(toUpdate.getMiddleNames().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getMiddleNames()));
    }

    /**
     * Update a user's last name that doesn't exist in the user service Mostly
     * this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserMiddleNames() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setMiddleNames("MiddleName");
        toUpdate.setEmailAddress("foo@bar.biz");

        boolean caughtException = false;
        try {
            underTest.updateMiddleNames(toUpdate.getEmailAddress(),
                                        toUpdate.getMiddleNames());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's last name that exists in the user service
     */
    @Test
    public void testUpdateExistingUserLastName() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setLastNames("new last name");

        assertFalse(one.equals(toUpdate));

        underTest.updateLastNames(one.getId(), toUpdate.getLastNames());

        assertTrue(toUpdate.getLastNames().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getLastNames()));

        //Update the person using the email address
        toUpdate = new Person(one);
        toUpdate.setLastNames("another new last name");

        assertFalse(one.equals(toUpdate));
        underTest.updateLastNames(one.getEmailAddress(),
                                  toUpdate.getLastNames());

        assertTrue(toUpdate.getLastNames().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getLastNames()));
    }

    /**
     * Update a user's last name that doesn't exist in the user service Mostly
     * this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserLastName() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setFirstNames("Foo");
        toUpdate.setLastNames("Bar");
        toUpdate.setEmailAddress("foo@bar.biz");

        boolean caughtException = false;
        try {
            underTest.updateLastNames(toUpdate.getEmailAddress(),
                                      toUpdate.getLastNames());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's phone number that exists in the user service
     */
    @Test
    public void testUpdateExistingUserPhoneNumber() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setPhoneNumber("1234567890");

        assertFalse(one.equals(toUpdate));

        underTest.updatePhoneNumber(one.getId(), toUpdate.getPhoneNumber());

        assertTrue(toUpdate.getPhoneNumber().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getPhoneNumber()));

        //Update the person using the email address
        toUpdate = new Person(one);
        toUpdate.setPhoneNumber("0987654321");

        assertFalse(one.equals(toUpdate));
        underTest.updatePhoneNumber(one.getEmailAddress(),
                                    toUpdate.getPhoneNumber());

        assertTrue(toUpdate.getPhoneNumber().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getPhoneNumber()));
    }

    /**
     * Update a user's phone number that doesn't exist in the user service
     * Mostly this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserPhoneNumber() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setFirstNames("Foo");
        toUpdate.setLastNames("Bar");
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setPhoneNumber("1234567890");

        boolean caughtException = false;
        try {
            underTest.updatePhoneNumber(toUpdate.getEmailAddress(),
                                        toUpdate.getPhoneNumber());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's password that exists in the user service
     */
    @Test
    public void testUpdateExistingUserPassword() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setPassword("new password");

        assertFalse(one.equals(toUpdate));

        underTest.updatePassword(one.getId(), toUpdate.getPassword());

        assertTrue(toUpdate.getPassword().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getPassword()));

        //Update the person using the email address
        toUpdate = new Person(one);
        toUpdate.setPassword("another new password");

        assertFalse(one.equals(toUpdate));
        underTest.updatePassword(one.getEmailAddress(), toUpdate.getPassword());

        assertTrue(toUpdate.getPassword().equalsIgnoreCase(underTest
                .get(one.getEmailAddress()).getPassword()));
    }

    /**
     * Update a user's password that doesn't exist in the user service Mostly
     * this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserPassword() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setFirstNames("Foo");
        toUpdate.setLastNames("Bar");
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setPassword("password");

        boolean caughtException = false;
        try {
            underTest.updatePassword(toUpdate.getEmailAddress(),
                                     toUpdate.getPassword());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's email address that exists in the user service
     */
    @Test
    public void testUpdateExistingUserEmailAddress() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setEmailAddress("new@email.com");

        assertFalse(one.equals(toUpdate));

        underTest.updateEmailAddress(one.getId(), toUpdate.getEmailAddress());

        assertTrue(toUpdate.getEmailAddress().equalsIgnoreCase(underTest
                .get(one.getId()).getEmailAddress()));

        //Make sure the right person is retrieved with the new email address
        assertTrue(toUpdate.equals(underTest.get(toUpdate.getEmailAddress())));

        //Update the person using the email address
        toUpdate = new Person(one);
        toUpdate.setEmailAddress("another@newemail.com");

        assertFalse(one.equals(toUpdate));
        underTest.updateEmailAddress(one.getId(), toUpdate.getEmailAddress());

        assertTrue(toUpdate.getEmailAddress().equalsIgnoreCase(underTest
                .get(one.getId()).getEmailAddress()));
        assertTrue(toUpdate.equals(underTest.get(toUpdate.getEmailAddress())));
    }

    /**
     * Update a user's email address that doesn't exist in the user service
     * Mostly this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserEmailAddress() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setFirstNames("Foo");
        toUpdate.setLastNames("Bar");
        toUpdate.setEmailAddress("foo@bar.biz");

        boolean caughtException = false;
        try {
            underTest.updateEmailAddress(toUpdate.getEmailAddress(),
                                         toUpdate.getEmailAddress());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's job title that exists in the user service
     */
    @Test
    public void testUpdateExistingUserJobTitle() {
        // verify assumption
        assertEquals(one, underTest.get(one.getId()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setJobTitle("New Job Title");

        assertFalse(one.equals(toUpdate));

        underTest.updateJobTitle(one.getId(), toUpdate.getJobTitle());

        assertTrue(toUpdate.getJobTitle().equalsIgnoreCase(underTest
                .get(one.getId()).getJobTitle()));

        //Make sure the right person is retrieved with the new job title
        assertTrue(toUpdate.getJobTitle().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getJobTitle()));

        //Update the person using the job title
        toUpdate = new Person(one);
        toUpdate.setJobTitle("Another New Job Title");

        assertFalse(one.equals(toUpdate));
        underTest.updateJobTitle(one.getId(), toUpdate.getJobTitle());

        assertTrue(toUpdate.getJobTitle().equalsIgnoreCase(underTest
                .get(one.getId()).getJobTitle()));
        assertTrue(toUpdate.getJobTitle().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getJobTitle()));
    }

    /**
     * Update a user's job title that doesn't exist in the user service Mostly
     * this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserJobTitle() {
        // verify assumption
        assertNull(underTest.get("New Job Title"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setJobTitle("New Job Title");

        boolean caughtException = false;
        try {
            underTest.updateJobTitle(toUpdate.getEmailAddress(),
                                     toUpdate.getJobTitle());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's prefix that exists in the user service
     */
    @Test
    public void testUpdateExistingUserPrefix() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setPrefix("New Prefix");

        assertFalse(one.equals(toUpdate));

        underTest.updatePrefix(one.getId(), toUpdate.getPrefix());

        assertTrue(toUpdate.getPrefix().equalsIgnoreCase(underTest
                .get(one.getId()).getPrefix()));

        //Make sure the right person is retrieved with the new prefix
        assertTrue(toUpdate.getPrefix().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getPrefix()));

        toUpdate = new Person(one);
        toUpdate.setPrefix("Another New Prefix");

        assertFalse(one.equals(toUpdate));
        underTest.updatePrefix(one.getId(), toUpdate.getPrefix());

        assertTrue(toUpdate.getPrefix().equalsIgnoreCase(underTest
                .get(one.getId()).getPrefix()));
    }

    /**
     * Update a user's prefix that doesn't exist in the user service Mostly this
     * test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserPrefix() {
        // verify assumption
        assertNull(underTest.get("New Prefix"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setJobTitle("New Prefix");

        boolean caughtException = false;
        try {
            underTest.updatePrefix(toUpdate.getEmailAddress(),
                                   toUpdate.getPrefix());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's suffix that exists in the user service
     */
    @Test
    public void testUpdateExistingUserSuffix() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setSuffix("New Suffix");

        assertFalse(one.equals(toUpdate));

        underTest.updateSuffix(one.getId(), toUpdate.getSuffix());

        assertTrue(toUpdate.getSuffix().equalsIgnoreCase(underTest
                .get(one.getId()).getSuffix()));

        //Make sure the right person is retrieved with the new suffix
        assertTrue(toUpdate.getSuffix().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getSuffix()));

        toUpdate = new Person(one);
        toUpdate.setSuffix("Another New Suffix");

        assertFalse(one.equals(toUpdate));
        underTest.updateSuffix(one.getId(), toUpdate.getSuffix());

        assertTrue(toUpdate.getSuffix().equalsIgnoreCase(underTest
                .get(one.getId()).getSuffix()));
    }

    /**
     * Update a user's suffix that doesn't exist in the user service Mostly this
     * test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserSuffix() {
        // verify assumption
        assertNull(underTest.get("New Suffix"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setSuffix("New Suffix");

        boolean caughtException = false;
        try {
            underTest.updateSuffix(toUpdate.getEmailAddress(),
                                   toUpdate.getSuffix());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's city that exists in the user service
     */
    @Test
    public void testUpdateExistingUserCity() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setCity("New City");

        assertFalse(one.equals(toUpdate));

        underTest.updateCity(one.getId(), toUpdate.getCity());

        assertTrue(toUpdate.getCity().equalsIgnoreCase(underTest
                .get(one.getId()).getCity()));

        //Make sure the right person is retrieved with the new city
        assertTrue(toUpdate.getCity().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getCity()));

        toUpdate = new Person(one);
        toUpdate.setCity("Another New City");

        assertFalse(one.equals(toUpdate));
        underTest.updateCity(one.getId(), toUpdate.getCity());

        assertTrue(toUpdate.getCity().equalsIgnoreCase(underTest
                .get(one.getId()).getCity()));
    }

    /**
     * Update a user's city that doesn't exist in the user service Mostly this
     * test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserCity() {
        // verify assumption
        assertNull(underTest.get("New City"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setCity("New City");

        boolean caughtException = false;
        try {
            underTest
                    .updateCity(toUpdate.getEmailAddress(), toUpdate.getCity());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's state that exists in the user service
     */
    @Test
    public void testUpdateExistingUserState() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setState("New State");

        assertFalse(one.equals(toUpdate));

        underTest.updateState(one.getId(), toUpdate.getState());

        assertTrue(toUpdate.getState().equalsIgnoreCase(underTest
                .get(one.getId()).getState()));

        //Make sure the right person is retrieved with the new state
        assertTrue(toUpdate.getState().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getState()));

        toUpdate = new Person(one);
        toUpdate.setState("Another New State");

        assertFalse(one.equals(toUpdate));
        underTest.updateState(one.getId(), toUpdate.getState());

        assertTrue(toUpdate.getState().equalsIgnoreCase(underTest
                .get(one.getId()).getState()));
    }

    /**
     * Update a user's state that doesn't exist in the user service Mostly this
     * test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserState() {
        // verify assumption
        assertNull(underTest.get("New State"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setState("New State");

        boolean caughtException = false;
        try {
            underTest.updateState(toUpdate.getEmailAddress(),
                                  toUpdate.getState());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's department that exists in the user service
     */
    @Test
    public void testUpdateExistingUserDepartment() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setDepartment("New Department");

        assertFalse(one.equals(toUpdate));

        underTest.updateDepartment(one.getId(), toUpdate.getDepartment());

        assertTrue(toUpdate.getDepartment().equalsIgnoreCase(underTest
                .get(one.getId()).getDepartment()));

        //Make sure the right person is retrieved with the new department
        assertTrue(toUpdate.getDepartment().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getDepartment()));

        toUpdate = new Person(one);
        toUpdate.setDepartment("Another New Department");

        assertFalse(one.equals(toUpdate));
        underTest.updateDepartment(one.getId(), toUpdate.getDepartment());

        assertTrue(toUpdate.getDepartment().equalsIgnoreCase(underTest
                .get(one.getId()).getDepartment()));
    }

    /**
     * Update a user's department that doesn't exist in the user service Mostly
     * this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserDepartment() {
        // verify assumption
        assertNull(underTest.get("New Department"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setDepartment("New Department");

        boolean caughtException = false;
        try {
            underTest.updateDepartment(toUpdate.getEmailAddress(),
                                       toUpdate.getDepartment());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's institution/company that exists in the user service
     */
    @Test
    public void testUpdateExistingUserInstCompany() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setInstCompany("New Institution/Company");

        assertFalse(one.equals(toUpdate));

        underTest.updateInstCompany(one.getId(), toUpdate.getInstCompany());

        assertTrue(toUpdate.getInstCompany().equalsIgnoreCase(underTest
                .get(one.getId()).getInstCompany()));

        //Make sure the right person is retrieved with the new Institution/Company
        assertTrue(toUpdate.getInstCompany().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getInstCompany()));

        toUpdate = new Person(one);
        toUpdate.setInstCompany("Another New Institution/Company");

        assertFalse(one.equals(toUpdate));
        underTest.updateInstCompany(one.getId(), toUpdate.getInstCompany());

        assertTrue(toUpdate.getInstCompany().equalsIgnoreCase(underTest
                .get(one.getId()).getInstCompany()));
    }

    /**
     * Update a user's instition/company that doesn't exist in the user service
     * Mostly this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserInstCompany() {
        // verify assumption
        assertNull(underTest.get("New Institution/Company"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setInstCompany("New Institution/Company");

        boolean caughtException = false;
        try {
            underTest.updateInstCompany(toUpdate.getEmailAddress(),
                                       toUpdate.getInstCompany());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }
    
    /**
     * Update a user's institution/company website that exists in the user service
     */
    @Test
    public void testUpdateExistingUserInstCompanyWebsite() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));
        
        // Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setInstCompanyWebsite("www.NewInstitutionCompanyWebsite.com");
        
        assertFalse(one.equals(toUpdate));
        
        underTest.updateInstCompanyWebsite(one.getId(), toUpdate.getInstCompanyWebsite());
        
        assertTrue(toUpdate.getInstCompanyWebsite()
                .equalsIgnoreCase(underTest.get(one.getId()).getInstCompanyWebsite()));
        
        // Make sure the right person is retrieved with the new Institution/Company Website
        assertTrue(toUpdate.getInstCompanyWebsite().equalsIgnoreCase(
                underTest.get(toUpdate.getEmailAddress()).getInstCompanyWebsite()));
        
        toUpdate = new Person(one);
        toUpdate.setInstCompanyWebsite("www.AnotherNewInstitutionCompanyWebsite.com");
        
        assertFalse(one.equals(toUpdate));
        underTest.updateInstCompanyWebsite(one.getId(), toUpdate.getInstCompanyWebsite());
        
        assertTrue(toUpdate.getInstCompanyWebsite()
                .equalsIgnoreCase(underTest.get(one.getId()).getInstCompanyWebsite()));
    }
    
    /**
     * Update a user's instition/company website that doesn't exist in the user service Mostly this test is about
     * documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserInstCompanyWebsite() {
        // verify assumption
        assertNull(underTest.get("www.NewInstitutionCompany.com"));
        
        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setInstCompanyWebsite("www.NewInstitutionCompany.com");
        
        boolean caughtException = false;
        try {
            underTest.updateInstCompanyWebsite(toUpdate.getEmailAddress(), toUpdate.getInstCompanyWebsite());
        }
        catch (PersonUpdateException e) {
            caughtException = true;
        }
        
        assertTrue(caughtException);
    }

    /**
     * Update a user's preferred published name that exists in the user service
     */
    @Test
    public void testUpdateExistingUserPreferredPubName() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setPreferredPubName("P. Name");

        assertFalse(one.equals(toUpdate));

        underTest.updatePreferredPubName(one.getId(),
                                         toUpdate.getPreferredPubName());

        assertTrue(toUpdate.getPreferredPubName().equalsIgnoreCase(underTest
                .get(one.getId()).getPreferredPubName()));

        //Make sure the right person is retrieved with the new Preferred Pub Name
        assertTrue(toUpdate.getPreferredPubName().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getPreferredPubName()));

        toUpdate = new Person(one);
        toUpdate.setPreferredPubName("Another P. Name");

        assertFalse(one.equals(toUpdate));
        underTest.updatePreferredPubName(one.getId(),
                                         toUpdate.getPreferredPubName());

        assertTrue(toUpdate.getPreferredPubName().equalsIgnoreCase(underTest
                .get(one.getId()).getPreferredPubName()));
    }

    /**
     * Update a user's preferred published name that doesn't exist in the user
     * service Mostly this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserPreferredPubName() {
        // verify assumption
        assertNull(underTest.get("P. Name"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setPreferredPubName("P. Name");

        boolean caughtException = false;
        try {
            underTest.updatePreferredPubName(toUpdate.getEmailAddress(),
                                             toUpdate.getPreferredPubName());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's bio that exists in the user service
     */
    @Test
    public void testUpdateExistingUserBio() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setBio("New Bio");

        assertFalse(one.equals(toUpdate));

        underTest.updateBio(one.getId(), toUpdate.getBio());

        assertTrue(toUpdate.getBio().equalsIgnoreCase(underTest
                .get(one.getId()).getBio()));

        //Make sure the right person is retrieved with the new bio
        assertTrue(toUpdate.getBio().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getBio()));

        toUpdate = new Person(one);
        toUpdate.setBio("Another New Bio");

        assertFalse(one.equals(toUpdate));
        underTest.updateBio(one.getId(), toUpdate.getBio());

        assertTrue(toUpdate.getBio().equalsIgnoreCase(underTest
                .get(one.getId()).getBio()));
    }

    /**
     * Update a user's bio that doesn't exist in the user service Mostly this
     * test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserBio() {
        // verify assumption
        assertNull(underTest.get("New Bio"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setBio("New Bio");

        boolean caughtException = false;
        try {
            underTest.updateBio(toUpdate.getEmailAddress(), toUpdate.getBio());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's website that exists in the user service
     */
    @Test
    public void testUpdateExistingUserWebsite() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setWebsite("www.newwebsite.com");

        assertFalse(one.equals(toUpdate));

        underTest.updateWebsite(one.getId(), toUpdate.getWebsite());

        assertTrue(toUpdate.getWebsite().equalsIgnoreCase(underTest
                .get(one.getId()).getWebsite()));

        //Make sure the right person is retrieved with the new website
        assertTrue(toUpdate.getWebsite().equalsIgnoreCase(underTest
                .get(toUpdate.getEmailAddress()).getWebsite()));

        toUpdate = new Person(one);
        toUpdate.setWebsite("www.anothernewwebsite.com");

        assertFalse(one.equals(toUpdate));
        underTest.updateWebsite(one.getId(), toUpdate.getWebsite());

        assertTrue(toUpdate.getWebsite().equalsIgnoreCase(underTest
                .get(one.getId()).getWebsite()));
    }

    /**
     * Update a user's website that doesn't exist in the user service Mostly
     * this test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserWebsite() {
        // verify assumption
        assertNull(underTest.get("www.website.com"));

        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setWebsite("www.website.com");

        boolean caughtException = false;
        try {
            underTest.updateWebsite(toUpdate.getEmailAddress(),
                                    toUpdate.getWebsite());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Update a user's role that exists in the user service
     */
    @Test
    public void testUpdateExistingUserRole() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Update the person using the id
        Person toUpdate = new Person(one);
        List<Role> roles = new ArrayList<Role>();
        roles.add(Role.ROLE_ADMIN);
        toUpdate.setRoles(roles);

        underTest.updateRoles(one.getId(), toUpdate.getRoles());

        assertTrue(toUpdate.getRoles().equals(underTest
                .get(one.getEmailAddress()).getRoles()));

        //Try setting no role
        roles.clear();
        toUpdate.setRoles(roles);

        underTest.updateRoles(one.getId(), toUpdate.getRoles());

        assertEquals(0, underTest.get(one.getEmailAddress()).getRoles().size());

        //Update the person using the email address
        toUpdate = new Person(one);
        roles.clear();
        roles.add(Role.ROLE_USER);
        toUpdate.setRoles(roles);

        assertTrue(toUpdate.getRoles().equals(underTest
                .get(one.getEmailAddress()).getRoles()));
    }

    /**
     * Update a user's role that doesn't exist in the user service Mostly this
     * test is about documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserRole() {
        // verify assumption
        assertNull(underTest.get("foo@bar.biz"));

        Person toUpdate = new Person();
        toUpdate.setFirstNames("Foo");
        toUpdate.setLastNames("Bar");
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.addRole(Role.ROLE_ADMIN);

        boolean caughtException = false;
        try {
            underTest.updateRoles(toUpdate.getEmailAddress(),
                                  toUpdate.getRoles());
        } catch (PersonUpdateException e) {
            caughtException = true;
        }

        assertTrue(caughtException);
    }

    /**
     * Tests that a person can be deleted from the system. Will check that the
     * person is not retrieved with get or find.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteUser() throws Exception {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));

        //Test deleting by id.
        underTest.deletePerson(one.getId());

        assertNull(underTest.get(one.getId()));
        assertNull(underTest.get(one.getEmailAddress()));

        assertEquals(0,
                     underTest.find(RegistrationStatus.APPROVED,
                                    one.getComparator()).size());

        //Test deleting by email address
        underTest.deletePerson(two.getId());

        assertNull(underTest.get(two.getId()));
        assertNull(underTest.get(two.getEmailAddress()));
    }

    @Test
    public void testFind() throws Exception {
        List<Person> foundPeople =
                underTest
                        .find(RegistrationStatus.APPROVED, one.getComparator());
        assertEquals(1, foundPeople.size());

        foundPeople.clear();
        foundPeople =
                underTest.find(RegistrationStatus.BLACK_LISTED,
                               two.getComparator());
        assertEquals(0, foundPeople.size());
    }

    /**
     * Tests that when a user's registration is set to approve their roles are
     * correctly set
     */
    @Test
    public void testUpdateRegistrationAssignsRoles() {
        Person toCreate = new Person();
        toCreate.setId("1234");
        toCreate.setFirstNames("Foo");
        toCreate.setLastNames("Bar");

        toCreate.setEmailAddress("foo@bar.biz");
        toCreate.setPassword("password");
        toCreate.setPhoneNumber("55555555555");
        toCreate.setJobTitle("Job Title");
        toCreate.setDepartment("Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Institution/Company");
        toCreate.setInstCompanyWebsite("www.InstitutionCompanyWebsite.com");
        toCreate.setRegistrationStatus(RegistrationStatus.PENDING);

        underTest.create(toCreate);
        assertNotNull(toCreate);

        int pendingRoleSize =
                underTest
                        .getPolicyConsultant()
                        .getRolesForRegistrationStatus(RegistrationStatus.PENDING)
                        .size();
        int approvedRoleSize =
                underTest
                        .getPolicyConsultant()
                        .getRolesForRegistrationStatus(RegistrationStatus.APPROVED)
                        .size();

        Person updatedPerson = underTest.get(toCreate.getId());

        assertEquals(toCreate, updatedPerson);
        assertEquals(pendingRoleSize, updatedPerson.getRoles().size());

        for (Role role : underTest.getPolicyConsultant()
                .getRolesForRegistrationStatus(RegistrationStatus.PENDING)) {
            assertTrue(updatedPerson.getRoles().contains(role));
        }

        underTest.updateRegistrationStatus(toCreate.getId(),
                                           RegistrationStatus.APPROVED);
        updatedPerson = underTest.get(toCreate.getId());

        assertEquals(approvedRoleSize, underTest.get(toCreate.getId())
                .getRoles().size());

        for (Role role : underTest.getPolicyConsultant()
                .getRolesForRegistrationStatus(RegistrationStatus.APPROVED)) {
            assertTrue(updatedPerson.getRoles().contains(role));
        }
    }

    /**
     * Tests that a person created with no registration status is correctly
     * assigned the default status and the roles that go with it
     */
    @Test
    public void testNullRegistrationStatusIsSetCorrectly() {
        PersonBizPolicyConsultant nullRegistration =
                new AcceptNullRegistrationStatus();
        underTest.setPolicyConsultant(nullRegistration);

        Person toCreate = new Person();
        toCreate.setId("1234");
        toCreate.setFirstNames("Foo");
        toCreate.setLastNames("Bar");
        toCreate.setPrefix("Mr.");
        toCreate.setSuffix("II");
        toCreate.setMiddleNames("Middle");
        toCreate.setPreferredPubName("F. Bar");
        toCreate.setBio("Some bio");
        toCreate.setWebsite("www.website.com");
        toCreate.setEmailAddress("foo@bar.biz");
        toCreate.setPassword("password");
        toCreate.setPhoneNumber("5555555555555");
        toCreate.setJobTitle("Job Title");
        toCreate.setDepartment("Department");
        toCreate.setCity("Baltimore");
        toCreate.setState("Maryland");
        toCreate.setInstCompany("Institution/Company");
        toCreate.setInstCompanyWebsite("www.InstitutionCompanyWebsite.com");
        toCreate.setRegistrationStatus(null);
        underTest.create(toCreate);
        assertNotNull(toCreate);

        RegistrationStatus defaultStatus =
                underTest.getPolicyConsultant().getDefaultRegistrationStatus();
        List<Role> defaultRoles =
                underTest.getPolicyConsultant()
                        .getRolesForRegistrationStatus(defaultStatus);

        Person updatedPerson = underTest.get(toCreate.getId());

        assertEquals(defaultStatus, updatedPerson.getRegistrationStatus());
        assertEquals(defaultRoles.size(), updatedPerson.getRoles().size());

        for (Role role : defaultRoles) {
            assertTrue(updatedPerson.getRoles().contains(role));
        }
    }

    /**
     * Update a user's unique dropbox app key that exists in the user service
     */
    @Test
    public void testUpdateDropboxAppKey() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));
        
        // Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setDropboxAppKey("NewAndGreatAppKey");
        
        assertFalse(one.equals(toUpdate));
        
        underTest.updateDropboxAppKey(one.getId(), toUpdate.getDropboxAppKey());
        
        assertTrue(toUpdate.getDropboxAppKey().equalsIgnoreCase(underTest.get(one.getId()).getDropboxAppKey()));
        
        // Make sure the right person is retrieved with the new app key
        assertTrue(toUpdate.getDropboxAppKey().equalsIgnoreCase(
                underTest.get(toUpdate.getEmailAddress()).getDropboxAppKey()));
        
        toUpdate = new Person(one);
        toUpdate.setDropboxAppKey("AnotherNewAneImprovedAppKey");
        
        assertFalse(one.equals(toUpdate));
        underTest.updateDropboxAppKey(one.getId(), toUpdate.getDropboxAppKey());
        
        assertTrue(toUpdate.getDropboxAppKey().equalsIgnoreCase(underTest.get(one.getId()).getDropboxAppKey()));
    }
    
    /**
     * Update a user's unique dropbox app key that doesn't exist in the user service Mostly this test is about
     * documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserDropboxAppKey() {
        // verify assumption
        assertNull(underTest.get("NewAndGreatAppKey"));
        
        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setDropboxAppKey("NewAndGreatAppKey");
        
        boolean caughtException = false;
        try {
            underTest.updateDropboxAppKey(toUpdate.getEmailAddress(), toUpdate.getDropboxAppKey());
        }
        catch (PersonUpdateException e) {
            caughtException = true;
        }
        
        assertTrue(caughtException);
    }
    
    /**
     * Update a user's unique dropbox app secret that exists in the user service
     */
    @Test
    public void testUpdateDropboxAppSecret() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));
        
        // Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setDropboxAppSecret("NewAndGreatAppSecret");
        
        assertFalse(one.equals(toUpdate));
        
        underTest.updateDropboxAppSecret(one.getId(), toUpdate.getDropboxAppSecret());
        
        assertTrue(toUpdate.getDropboxAppSecret().equalsIgnoreCase(underTest.get(one.getId()).getDropboxAppSecret()));
        
        // Make sure the right person is retrieved with the new app secret
        assertTrue(toUpdate.getDropboxAppSecret().equalsIgnoreCase(
                underTest.get(toUpdate.getEmailAddress()).getDropboxAppSecret()));
        
        toUpdate = new Person(one);
        toUpdate.setDropboxAppKey("AnotherNewAneImprovedAppSecret");
        
        assertFalse(one.equals(toUpdate));
        underTest.updateDropboxAppSecret(one.getId(), toUpdate.getDropboxAppSecret());
        
        assertTrue(toUpdate.getDropboxAppSecret().equalsIgnoreCase(underTest.get(one.getId()).getDropboxAppSecret()));
    }
    
    /**
     * Update a user's unique dropbox app secret that doesn't exist in the user service Mostly this test is about
     * documenting existing behaviors
     */
    @Test
    public void testUpdateNonExistingUserDropboxAppSecret() {
        // verify assumption
        assertNull(underTest.get("NewAndGreatAppSecret"));
        
        Person toUpdate = new Person();
        toUpdate.setEmailAddress("foo@bar.biz");
        toUpdate.setDropboxAppSecret("NewAndGreatAppSecret");
        
        boolean caughtException = false;
        try {
            underTest.updateDropboxAppSecret(toUpdate.getEmailAddress(), toUpdate.getDropboxAppSecret());
        }
        catch (PersonUpdateException e) {
            caughtException = true;
        }
        
        assertTrue(caughtException);
    }
    
    /**
     * Update a user's external storage linked flag that exists in the user service
     */
    @Test
    public void testUpdateExistingExternalStorageLinked() {
        // verify assumption
        assertEquals(one, underTest.get(one.getEmailAddress()));
        
        // Update the person using the id
        Person toUpdate = new Person(one);
        toUpdate.setExternalStorageLinked(true);
        
        assertFalse(one.equals(toUpdate));
        
        underTest.updateExternalStorageLinked(one.getId(), toUpdate.isExternalStorageLinked());
        
        assertTrue(toUpdate.isExternalStorageLinked() == (underTest.get(one.getId()).isExternalStorageLinked()));
        
        // Make sure the right person is retrieved with the new external storage linked flag
        assertTrue(toUpdate.isExternalStorageLinked() == (underTest.get(toUpdate.getEmailAddress())
                .isExternalStorageLinked()));
        
    }
    
    
    /**
     * A {@link PersonBizPolicyConsultant} that:
     * <ul>
     * <li>Only allows APPROVED and PENDING statuses on newly created users</li>
     * <li>Uses PENDING as the default registration status for newly created users without a status</li>
     * <li>Maps the APPROVED status to ROLE_USER; all other status will have no roles.</li>
     * </ul>
     */
    private class AcceptApprovedAndPendingUsers
            implements PersonBizPolicyConsultant {

        @Override
        public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
            return Arrays.asList(RegistrationStatus.APPROVED,
                                 RegistrationStatus.PENDING);
        }

        @Override
        public boolean enforceRegistrationStatusOnCreate() {
            return true;
        }

        @Override
        public RegistrationStatus getDefaultRegistrationStatus() {
            return RegistrationStatus.PENDING;
        }

        @Override
        public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
            switch (status) {
                case PENDING:
                    return Collections.emptyList();
                case BLACK_LISTED:
                    return Collections.emptyList();
                case APPROVED:
                    return Arrays.asList(Role.ROLE_USER);
            }
            return Collections.emptyList();
        }
    }

    /**
     * TODO: Javadoc
     */
    private class AcceptNullRegistrationStatus
            implements PersonBizPolicyConsultant {

        @Override
        public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
            return new ArrayList<RegistrationStatus>();
        }

        @Override
        public boolean enforceRegistrationStatusOnCreate() {
            return true;
        }

        @Override
        public RegistrationStatus getDefaultRegistrationStatus() {
            return RegistrationStatus.PENDING;
        }

        @Override
        public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
            switch (status) {
                case PENDING:
                    return Arrays.asList(Role.ROLE_USER);
                case BLACK_LISTED:
                    return Collections.emptyList();
                case APPROVED:
                    return Arrays.asList(Role.ROLE_USER);
            }
            return Collections.emptyList();
        }
    }
}
