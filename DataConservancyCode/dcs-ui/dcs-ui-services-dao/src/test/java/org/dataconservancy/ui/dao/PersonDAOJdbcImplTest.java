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

package org.dataconservancy.ui.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class PersonDAOJdbcImplTest extends BaseModelTest {

    @Autowired
    private PersonDAOJbdcImpl personDao;

    private JdbcTemplate jdbcTemplate;

    private static final String PERSON_TABLE = "PERSON";

    @Before
    public void setUp() {
        jdbcTemplate = personDao.getJdbcTemplate();
        jdbcTemplate.execute("DELETE FROM " + PERSON_TABLE);

        String query = "SELECT * FROM " + PERSON_TABLE;

        List<Person> people =
                (List<Person>) jdbcTemplate.query(query, new PersonRowMapper());
        StringBuilder errMsg = new StringBuilder();
        if (people.size() != 0) {
            errMsg =
                    new StringBuilder("Error: Found " + people.size()
                            + " people in the " + PERSON_TABLE + "\n");
            for (Person p : people) {
                errMsg.append(p.toString()).append("\n");
            }
            errMsg.append("\n");
        }
        assertEquals(errMsg.toString(), 0, people.size());

        query =
                " INSERT INTO "
                        + PERSON_TABLE
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int insertedRows =
                jdbcTemplate
                        .update(query,
                                new Object[] {
                                        pendingUser.getId(),
                                        pendingUser.getFirstNames(),
                                        pendingUser.getMiddleNames(),
                                        pendingUser.getLastNames(),
                                        pendingUser.getPrefix(),
                                        pendingUser.getSuffix(),
                                        pendingUser.getEmailAddress(),
                                        pendingUser.getPhoneNumber(),
                                        pendingUser.getPassword(),
                                        pendingUser.getJobTitle(),
                                        pendingUser.getDepartment(),
                                        pendingUser.getCity(),
                                        pendingUser.getState(),
                                        pendingUser.getInstCompany(),
                                        pendingUser.getInstCompanyWebsite(),
                                        pendingUser.getWebsite(),
                                        pendingUser.getBio(),
                                        pendingUser.getPreferredPubName(),
                                        pendingUser.getRegistrationStatus().name(),
                                        personDao.getRolesAsCsvString(pendingUser),
                                        Boolean.toString(pendingUser.isExternalStorageLinked()),
                                        pendingUser.getDropboxAppKey(),
                                        pendingUser.getDropboxAppSecret()});
        assertEquals(1, insertedRows);
    }

    @Test
    public void testSelectByEmail() {
        Person retrievedPerson =
                personDao.selectPersonById(pendingUser.getId());
        assertEquals(pendingUser, retrievedPerson);
    }

    @Test
    public void testSelectById() {
        Person retrievedPerson =
                personDao.selectPersonById(pendingUser.getId());
        assertEquals(pendingUser, retrievedPerson);
    }

    @Test
    public void testUpdateForAllRegistrationStatuses() {
        Person p = new Person();
        p.setId("id:foo");
        p.setFirstNames("foo");
        p.setLastNames("bar");
        p.setPrefix("Mr.");
        p.setSuffix("II");
        p.setMiddleNames("Middle");
        p.setPreferredPubName("F. Bar");
        p.setBio("Some bio for the user.");
        p.setWebsite("www.somewebsite.com");
        p.setJobTitle("Alfee Scientist");
        p.setDepartment("Alfee Department");
        p.setCity("Baltimore");
        p.setState("Maryland");
        p.setInstCompany("Alfee Institution/Company");
        p.setInstCompanyWebsite("www.AlfeeInstitutionCompany.com");
        p.setEmailAddress("foo@bar.com");
        p.setPhoneNumber("1-234-567-8901");
        p.setPassword("foobar");
        p.setRegistrationStatus(RegistrationStatus.PENDING);
        p.addRole(Role.ROLE_USER);
        p.setExternalStorageLinked(false);
        p.setDropboxAppKey("SomeKey");
        p.setDropboxAppSecret("SomeSecret");

        personDao.insertPerson(p);
        assertEquals(p, personDao.selectPersonById(p.getId()));

        for (RegistrationStatus status : RegistrationStatus.values()) {
            p.setRegistrationStatus(status);
            personDao.updatePerson(p);
        }
    }

    @Test
    public void testInsert() {
        Person personTwo = new Person();
        personTwo.setId("id:alfee");
        personTwo.setFirstNames("Alfee");
        personTwo.setLastNames("Sirk");
        personTwo.setPrefix("Mr.");
        personTwo.setSuffix("II");
        personTwo.setMiddleNames("Middle");
        personTwo.setPreferredPubName("A. Sirk");
        personTwo.setBio("Some bio for the user.");
        personTwo.setWebsite("www.somewebsite.com");
        personTwo.setEmailAddress("Alfee.Sirk@gmail.com");
        personTwo.setPhoneNumber("4444444444");
        personTwo.setRegistrationStatus(RegistrationStatus.PENDING);
        personTwo.setPassword("Alf33LikesT0mat03s");
        personTwo.setJobTitle("Alfee Scientist");
        personTwo.setDepartment("Alfee Department");
        personTwo.setCity("Baltimore");
        personTwo.setState("Maryland");
        personTwo.setInstCompany("Alfee Institution/Company");
        personTwo.setInstCompanyWebsite("www.AlfeeInstitutionCompany.com");
        personTwo.addRole(Role.ROLE_USER);
        personTwo.setExternalStorageLinked(false);
        personTwo.setDropboxAppKey("SomeKey");
        personTwo.setDropboxAppSecret("SomeSecret");

        personDao.insertPerson(personTwo);
        Person retrievedPerson = personDao.selectPersonById(personTwo.getId());
        assertEquals(personTwo, retrievedPerson);
        String query = "DELETE FROM " + PERSON_TABLE + " WHERE ID = ?";
        int removedRows =
                jdbcTemplate.update(query, new Object[] {personTwo.getId()});
        assertEquals(1, removedRows);
    }

    @Test
    public void testInsertWithRoles() {
        Person expected = new Person();
        final String emailAddress = "foo@barbaz.com";
        expected.setId("id:expected");
        expected.setFirstNames("Foo");
        expected.setLastNames("Bar");
        expected.setMiddleNames("Middle");
        expected.setEmailAddress(emailAddress);
        expected.setRegistrationStatus(RegistrationStatus.APPROVED);
        final Role expectedRole = Role.ROLE_USER;
        expected.addRole(expectedRole);

        personDao.insertPerson(expected);
        assertTrue(personDao.selectPersonByEmailAddress(emailAddress)
                .getRoles().contains(expectedRole));
    }

    @Test(expected = RuntimeException.class)
    public void testInsertWithNullRegStatus() {
        Person personTwo = new Person();
        personTwo.setId("id:alfee");
        personTwo.setFirstNames("Alfee");
        personTwo.setLastNames("Sirk");
        personTwo.setPrefix("Mr.");
        personTwo.setSuffix("II");
        personTwo.setMiddleNames("Middle");
        personTwo.setPreferredPubName("A. Sirk");
        personTwo.setBio("Some bio for the user.");
        personTwo.setWebsite("www.somewebsite.com");
        personTwo.setEmailAddress("Alfee.Sirk@gmail.com");
        personTwo.setPhoneNumber("4444444444");
        personTwo.setRegistrationStatus(null);
        personTwo.setPassword("Alf33LikesT0mat03s");
        personTwo.setJobTitle("Alfee Scientist");
        personTwo.setDepartment("Alfee Department");
        personTwo.setCity("Baltimore");
        personTwo.setState("Maryland");
        personTwo.setInstCompany("Alfee Institution/Company");
        personTwo.setInstCompanyWebsite("www.AlfeeInstitutionCompany.com");
        personTwo.setExternalStorageLinked(false);
        personTwo.setDropboxAppKey("SomeKey");
        personTwo.setDropboxAppSecret("SomeSecret");

        personDao.insertPerson(personTwo);
    }

    @Test
    public void testUpdate() {
        String newPassword = "WinterSucks!";
        pendingUser.setPassword(newPassword);
        personDao.updatePerson(pendingUser);
        Person retrievedPerson =
                personDao.selectPersonById(pendingUser.getId());
        assertEquals(pendingUser, retrievedPerson);
        assertEquals(newPassword, retrievedPerson.getPassword());
    }

    @Test
    public void testDeleteByEmail() {
        personDao.deletePersonByEmail(pendingUser.getEmailAddress());
        Person retrievedPerson =
                personDao.selectPersonByEmailAddress(pendingUser
                        .getEmailAddress());
        assertNull(retrievedPerson);
        String query =
                " INSERT INTO "
                        + PERSON_TABLE
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int insertedRows =
                jdbcTemplate
                        .update(query,
                                new Object[] {
                                        pendingUser.getId(),
                                        pendingUser.getFirstNames(),
                                        pendingUser.getMiddleNames(),
                                        pendingUser.getLastNames(),
                                        pendingUser.getPrefix(),
                                        pendingUser.getSuffix(),
                                        pendingUser.getEmailAddress(),
                                        pendingUser.getPhoneNumber(),
                                        pendingUser.getPassword(),
                                        pendingUser.getJobTitle(),
                                        pendingUser.getDepartment(),
                                        pendingUser.getCity(),
                                        pendingUser.getState(),
                                        pendingUser.getInstCompany(),
                                        pendingUser.getInstCompanyWebsite(),
                                        pendingUser.getWebsite(),
                                        pendingUser.getBio(),
                                        pendingUser.getPreferredPubName(),
                                        pendingUser.getRegistrationStatus().toString(),
                                        personDao.getRolesAsCsvString(pendingUser),
                                        Boolean.toString(pendingUser.isExternalStorageLinked()),
                                        pendingUser.getDropboxAppKey(),
                                        pendingUser.getDropboxAppSecret()});
        assertEquals(1, insertedRows);

    }

    @Test
    public void testDeleteById() {
        personDao.deletePersonById(pendingUser.getId());
        Person retrievedPerson =
                personDao.selectPersonByEmailAddress(pendingUser
                        .getEmailAddress());
        assertNull(retrievedPerson);
        String query =
                " INSERT INTO "
                        + PERSON_TABLE
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int insertedRows =
                jdbcTemplate
                        .update(query,
                                new Object[] {
                                        pendingUser.getId(),
                                        pendingUser.getFirstNames(),
                                        pendingUser.getMiddleNames(),
                                        pendingUser.getLastNames(),
                                        pendingUser.getPrefix(),
                                        pendingUser.getSuffix(),
                                        pendingUser.getEmailAddress(),
                                        pendingUser.getPhoneNumber(),
                                        pendingUser.getPassword(),
                                        pendingUser.getJobTitle(),
                                        pendingUser.getDepartment(),
                                        pendingUser.getCity(),
                                        pendingUser.getState(),
                                        pendingUser.getInstCompany(),
                                        pendingUser.getInstCompanyWebsite(),
                                        pendingUser.getWebsite(),
                                        pendingUser.getBio(),
                                        pendingUser.getPreferredPubName(),
                                        pendingUser.getRegistrationStatus().toString(),
                                        personDao.getRolesAsCsvString(pendingUser),
                                        Boolean.toString(pendingUser.isExternalStorageLinked()),
                                        pendingUser.getDropboxAppKey(),
                                        pendingUser.getDropboxAppSecret()});
        assertEquals(1, insertedRows);
    }

    @Test
    public void testSelectForPending() throws Exception {
        assertEquals(1, personDao
                .selectPerson(RegistrationStatus.PENDING, null).size());
        assertEquals(pendingUser,
                     personDao.selectPerson(RegistrationStatus.PENDING, null)
                             .iterator().next());
    }

    @Test
    public void testSelectForBlacklisted() throws Exception {
        assertEquals(0,
                     personDao.selectPerson(RegistrationStatus.BLACK_LISTED,
                                            null).size());

        Person p = new Person();
        p.setId("id:foo");
        p.setFirstNames("Foo");
        p.setLastNames("Bar");
        p.setPrefix("Mr.");
        p.setSuffix("II");
        p.setMiddleNames("Middle");
        p.setPreferredPubName("F. Bar");
        p.setBio("Some bio for the user.");
        p.setWebsite("www.somewebsite.com");
        p.setJobTitle("Alfee Scientist");
        p.setDepartment("Alfee Department");
        p.setCity("Baltimore");
        p.setState("Maryland");
        p.setInstCompany("Alfee Institution/Company");
        p.setInstCompanyWebsite("www.AlfeeInstitutionCompany.com");
        p.setEmailAddress("foo@bar.com");
        p.setPhoneNumber("1-234-567-8910");
        p.setPassword("foobar");
        p.setRegistrationStatus(RegistrationStatus.BLACK_LISTED);
        p.setExternalStorageLinked(false);
        p.setDropboxAppKey("SomeKey");
        p.setDropboxAppSecret("SomeSecret");

        assertNull(personDao.selectPersonById(p.getId()));
        personDao.insertPerson(p);
        assertEquals(p, personDao.selectPersonById(p.getId()));
        assertEquals(RegistrationStatus.BLACK_LISTED, personDao
                .selectPersonById(p.getId()).getRegistrationStatus());

        assertEquals(1,
                     personDao.selectPerson(RegistrationStatus.BLACK_LISTED,
                                            null).size());
        assertEquals(p,
                     personDao
                             .selectPerson(RegistrationStatus.BLACK_LISTED,
                                           null).iterator().next());
    }

    @Test
    public void testSelectForApproved() throws Exception {
        assertEquals(0,
                     personDao.selectPerson(RegistrationStatus.APPROVED, null)
                             .size());

        Person p = new Person();
        p.setId("id:foo");
        p.setFirstNames("Foo");
        p.setLastNames("Bar");
        p.setPrefix("Mr.");
        p.setSuffix("II");
        p.setMiddleNames("Middle");
        p.setPreferredPubName("F. Bar");
        p.setBio("Some bio for the user.");
        p.setWebsite("www.somewebsite.com");
        p.setJobTitle("Alfee Scientist");
        p.setDepartment("Alfee Department");
        p.setCity("Baltimore");
        p.setState("Maryland");
        p.setInstCompany("Alfee Institution/Company");
        p.setInstCompanyWebsite("www.AlfeeInstitutionCompany.com");
        p.setEmailAddress("foo@bar.com");
        p.setPhoneNumber("1-234-567-8910");
        p.setPassword("foobar");
        p.setRegistrationStatus(RegistrationStatus.APPROVED);
        p.setExternalStorageLinked(false);
        p.setDropboxAppKey("SomeKey");
        p.setDropboxAppSecret("SomeSecret");

        assertNull(personDao.selectPersonById(p.getId()));
        personDao.insertPerson(p);
        assertEquals(p,
                     personDao.selectPersonByEmailAddress(p.getEmailAddress()));
        assertEquals(RegistrationStatus.APPROVED,
                     personDao.selectPersonById(p.getId())
                             .getRegistrationStatus());

        assertEquals(1,
                     personDao.selectPerson(RegistrationStatus.APPROVED, null)
                             .size());
        assertEquals(p,
                     personDao.selectPerson(RegistrationStatus.APPROVED, null)
                             .iterator().next());
    }

    @After
    public void cleanUp() {
        String query = "DELETE FROM " + PERSON_TABLE;
        jdbcTemplate.execute(query);

        query = "SELECT COUNT(*) FROM " + PERSON_TABLE;
        assertEquals(0, jdbcTemplate.queryForInt(query));
    }
}
