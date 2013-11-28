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
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.dao.PersonDAOInMemoryImpl;
import org.dataconservancy.ui.exceptions.PersonUpdateException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.IdBizPolicyConsultant;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;

import static org.junit.Assert.assertEquals;

/**
 * This test suits test policy specific cases.
 */
public class UserServiceImplTest
        extends BaseUserServiceTest {

    @Override
    public UserService getUnderTest(Map<String, Person> usersToLoad,
                                    PersonBizPolicyConsultant personBizPolicyConsultant,
                                    IdBizPolicyConsultant idBizPolicyConsultant,
                                    IdService idService) {
        return new UserServiceImpl(new PersonDAOInMemoryImpl(usersToLoad),
                                   personBizPolicyConsultant,
                                   idBizPolicyConsultant,
                                   idService);
    }

    /**
     * Test Registration status policy: trying to add a valid person with no
     * preset registration status. EXPECTED: Person is accepted and created in
     * the system.
     */
    @Test
    public void testValidatePersonForCreation_ValidPerson_NoRegStatus() {
        Person validPerson = new Person();
        validPerson.setId("http:validPerson");
        validPerson.setFirstNames("firstname");
        validPerson.setLastNames("lastname");
        validPerson.setPrefix("Mr.");
        validPerson.setSuffix("II");
        validPerson.setMiddleNames("Spiderish");
        validPerson.setPreferredPubName("f. lastname");
        validPerson.setBio("Some bio for the user.");
        validPerson.setWebsite("www.somewebsite.com");
        validPerson.setJobTitle("Monkey Scientist");
        validPerson.setDepartment("Monkey Department");
        validPerson.setCity("Baltimore");
        validPerson.setState("Maryland");
        validPerson.setInstCompany("Monkey Institution/Company");
        validPerson.setInstCompanyWebsite("www.MonkeyInstitutionCompany.com");
        validPerson.setEmailAddress("email@address.com");
        validPerson.setPassword("funkychunk");
        validPerson.setPhoneNumber("555555555");
        validPerson.setExternalStorageLinked(false);
        validPerson.setDropboxAppKey("SomeKey");
        validPerson.setDropboxAppSecret("SomeSecret");

        underTest.create(validPerson);

        assertEquals(validPerson, underTest.get(validPerson.getEmailAddress()));

    }

    /**
     * Test Registration status policy: trying to add a valid person with preset
     * registration status of PENDING. EXPECTED: Person is accepted and created
     * in the system.
     */
    @Test
    public void testValidatePersonForCreation_ValidPerson_WithRegStatus() {
        Person validPerson = new Person();
        validPerson.setId("http:validPerson");
        validPerson.setFirstNames("firstname");
        validPerson.setLastNames("lastname");
        validPerson.setPrefix("Mr.");
        validPerson.setSuffix("II");
        validPerson.setMiddleNames("Spiderish");
        validPerson.setPreferredPubName("f. lastname");
        validPerson.setBio("Some bio for the user.");
        validPerson.setWebsite("www.somewebsite.com");
        validPerson.setJobTitle("Monkey Scientist");
        validPerson.setDepartment("Monkey Department");
        validPerson.setCity("Baltimore");
        validPerson.setState("Maryland");
        validPerson.setInstCompany("Monkey Institution/Company");
        validPerson.setInstCompanyWebsite("www.MonkeyInstitutionCompany.com");
        validPerson.setEmailAddress("email@address.com");
        validPerson.setPassword("funkychunk");
        validPerson.setPhoneNumber("555555555");
        validPerson.setRegistrationStatus(RegistrationStatus.PENDING);
        validPerson.setExternalStorageLinked(false);
        validPerson.setDropboxAppKey("SomeKey");
        validPerson.setDropboxAppSecret("SomeSecret");

        underTest.create(validPerson);

        assertEquals(validPerson, underTest.get(validPerson.getEmailAddress()));

    }

    /**
     * Test Registration status policy: trying to add a invalid person with no
     * firstname. EXPECTED: RuntimeBizPolicyException
     */
    @Test(expected = PersonUpdateException.class)
    public void testValidatePersonForCreation_InvalidPerson_NoFirstName() {
        Person validPerson = new Person();
        validPerson.setId("http:invalidPerson");
        validPerson.setLastNames("lastname");
        validPerson.setPrefix("Mr.");
        validPerson.setSuffix("II");
        validPerson.setMiddleNames("Spiderish");
        validPerson.setPreferredPubName("f. lastname");
        validPerson.setBio("Some bio for the user.");
        validPerson.setWebsite("www.somewebsite.com");
        validPerson.setJobTitle("Monkey Scientist");
        validPerson.setDepartment("Monkey Department");
        validPerson.setCity("Baltimore");
        validPerson.setState("Maryland");
        validPerson.setInstCompany("Monkey Institution/Company");
        validPerson.setInstCompanyWebsite("www.MonkeyInstitutionCompany.com");
        validPerson.setEmailAddress("email@address.com");
        validPerson.setPassword("funkychunk");
        validPerson.setPhoneNumber("555555555");
        validPerson.setRegistrationStatus(RegistrationStatus.PENDING);
        validPerson.setExternalStorageLinked(false);
        validPerson.setDropboxAppKey("SomeKey");
        validPerson.setDropboxAppSecret("SomeSecret");

        underTest.create(validPerson);
    }

    /**
     * Test Registration status policy: trying to add a invalid person with
     * preset roles. EXPECTED: RuntimeBizPolicyException
     */
    @Test(expected = PersonUpdateException.class)
    public void testValidatePersonForCreation_InvalidPerson_PresetRoles() {
        Person validPerson = new Person();
        validPerson.setId("http:invalidPerson");
        validPerson.setLastNames("lastname");
        validPerson.setPrefix("Mr.");
        validPerson.setSuffix("II");
        validPerson.setMiddleNames("Spiderish");
        validPerson.setPreferredPubName("f. lastname");
        validPerson.setBio("Some bio for the user.");
        validPerson.setWebsite("www.somewebsite.com");
        validPerson.setJobTitle("Monkey Scientist");
        validPerson.setDepartment("Monkey Department");
        validPerson.setCity("Baltimore");
        validPerson.setState("Maryland");
        validPerson.setInstCompany("Monkey Institution/Company");
        validPerson.setInstCompanyWebsite("www.MonkeyInstitutionCompany.com");
        validPerson.setEmailAddress("email@address.com");
        validPerson.setPassword("funkychunk");
        validPerson.setPhoneNumber("555555555");
        validPerson.setRegistrationStatus(RegistrationStatus.PENDING);
        List<Role> roleList = new ArrayList<Role>();
        roleList.add(Role.ROLE_ADMIN);
        validPerson.setExternalStorageLinked(false);
        validPerson.setDropboxAppKey("SomeKey");
        validPerson.setDropboxAppSecret("SomeSecret");

        underTest.create(validPerson);
    }

}
