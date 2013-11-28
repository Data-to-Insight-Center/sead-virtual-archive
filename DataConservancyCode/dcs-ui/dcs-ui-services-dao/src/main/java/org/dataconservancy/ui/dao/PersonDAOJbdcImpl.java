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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sql.DataSource;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * JDBC implementation of PersonDAO.
 */
public class PersonDAOJbdcImpl
        extends DcsUiDaoBaseImpl
        implements PersonDAO {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String PERSON_TBL = "PERSON";

    public PersonDAOJbdcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @Override
    public Person selectPersonById(String id) {
        log.trace("Selecting person with id of {}", id);
        String query = "SELECT * FROM " + PERSON_TBL + " WHERE ID = ?";

        List<Person> people =
                jdbcTemplate.query(query,
                                   new Object[] {id},
                                   new PersonRowMapper());
        if (people.size() == 0) {
            return null;
        } else {
            return people.get(0);
        }
    }

    @Override
    public Person selectPersonByEmailAddress(String emailAddress) {
        log.trace("Selecting person with email address of {}", emailAddress);
        String query =
                "SELECT * FROM " + PERSON_TBL + " WHERE EMAIL_ADDRESS = ?";

        List<Person> people =
                jdbcTemplate.query(query,
                                   new Object[] {emailAddress},
                                   new PersonRowMapper());
        if (people.size() == 0) {
            return null;
        } else {
            return people.get(0);
        }

    }

    @Override
    public List<Person> selectPerson(RegistrationStatus status,
                                     Comparator<Person> comparator) {
        log.trace("Selection people with registration status of {}", status);
        String query =
                "SELECT * FROM " + PERSON_TBL
                        + " WHERE REGISTRATION_STATUS = '" + status.name()
                        + "'";
        List<Person> people = jdbcTemplate.query(query, new PersonRowMapper());
        if (comparator != null) {
            Collections.sort(people, comparator);
        }

        return people;
    }

    @Override
    public void insertPerson(Person person) {
        log.trace("Insert Person {} into DB", person);
        String query =
                "INSERT INTO  "
                        + PERSON_TBL
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (person.getRegistrationStatus() == null) {
            throw new RuntimeException("Person " + person
                    + " must have a non-null registration status.");
        }

        jdbcTemplate.update(query,
                            new Object[] {person.getId(),
                                    person.getFirstNames(),
                                    person.getMiddleNames(),
                                    person.getLastNames(), person.getPrefix(),
                                    person.getSuffix(),
                                    person.getEmailAddress(),
                                    person.getPhoneNumber(),
                                    person.getPassword(), person.getJobTitle(),
                                    person.getDepartment(), person.getCity(),
                                    person.getState(), person.getInstCompany(), 
                                    person.getInstCompanyWebsite(),
                                    person.getWebsite(), person.getBio(),
                                    person.getPreferredPubName(),
                                    person.getRegistrationStatus().name(),
                                    getRolesAsCsvString(person),
                                    Boolean.toString(person.isExternalStorageLinked()),
                                    person.getDropboxAppKey(),
                                    person.getDropboxAppSecret(),});
    }

    @Override
    public void deletePersonByEmail(String email) {
        log.trace("Deleting person with email address of {}", email);
        String query = "DELETE FROM " + PERSON_TBL + " WHERE EMAIL_ADDRESS = ?";
        jdbcTemplate.update(query, new Object[] {email});
    }

    @Override
    public void deletePersonById(String id) {
        log.trace("Deleting person with id of {}", id);
        String query = "DELETE FROM " + PERSON_TBL + " WHERE ID = ?";
        jdbcTemplate.update(query, new Object[] {id});
    }

    /**
     * Update a person record given a person with matching Email address Email
     * Address is Primary Key and cannot be updated
     */
    @Override
    public void updatePerson(Person person) {
        log.trace("Updating person {}", person);
        String query =
                "UPDATE " + PERSON_TBL + " SET FIRST_NAME = ? "
                        + "   , MIDDLE_NAME = ?" + "   , LAST_NAME = ?"
                        + "   , PREFIX = ?" + "   , SUFFIX = ?"
                        + "   , EMAIL_ADDRESS = ?" + "   , PHONE = ?"
                        + "   , PASSWORD = ?" + "   , JOB_TITLE = ?"
                        + "   , DEPARTMENT = ?" + "   , CITY = ?"
                        + "   , STATE = ?" + "   , INST_COMPANY = ?"
                        + "   , INST_COMPANY_WEBSITE = ?"
                        + "   , WEBSITE = ?" + "   , BIO = ?"
                        + "   , PREFERRED_PUB_NAME = ?"
                        + "   , REGISTRATION_STATUS = ? , ROLES = ? "
                        + "   , EXTERNAL_STORAGE_LINKED = ?, DROPBOX_APP_KEY = ? "
                        + "   , DROPBOX_APP_SECRET = ? WHERE ID = ?";
        jdbcTemplate
                .update(query,
                        new Object[] {person.getFirstNames(),
                                person.getMiddleNames(), person.getLastNames(),
                                person.getPrefix(), person.getSuffix(),
                                person.getEmailAddress(),
                                person.getPhoneNumber(), person.getPassword(),
                                person.getJobTitle(), person.getDepartment(),
                                person.getCity(), person.getState(),
                                person.getInstCompany(), person.getInstCompanyWebsite(), 
                                person.getWebsite(), person.getBio(), person.getPreferredPubName(),
                                person.getRegistrationStatus().name(),
                                getRolesAsCsvString(person), 
                                Boolean.toString(person.isExternalStorageLinked()),
                                person.getDropboxAppKey(),
                                person.getDropboxAppSecret(), person.getId()});

    }

    public List<Person> selectPerson() {
        log.trace("Selecting all people from database");
        String query = "SELECT * FROM " + PERSON_TBL;
        List<Person> people = jdbcTemplate.query(query, new PersonRowMapper());

        return people;
    }

    /**
     * Iterates over all of the {@link Role}s for the {@code person}, and
     * returns them as a comma-separated-value string. Package-private for unit
     * testing.
     * 
     * @param person
     *        the person
     * @return the persons roles. may be empty but not null.
     */
    String getRolesAsCsvString(Person person) {
        StringBuilder roles = new StringBuilder("");
        for (int i = 0; i < person.getRoles().size(); i++) {
            if (i == person.getRoles().size() - 1) {
                roles.append(person.getRoles().get(i));
            } else {
                roles.append(person.getRoles().get(i)).append(",");
            }
        }

        return roles.toString();
    }

    /**
     * Provides method to check for existence of Person table in current
     * database.
     */
    private class CheckForPersonTable
            implements DatabaseMetaDataCallback {

        @Override
        public Object processMetaData(DatabaseMetaData dbmd)
                throws SQLException, MetaDataAccessException {
            ResultSet rs = dbmd.getTables(null, null, PERSON_TBL, null);

            if (rs.next()) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    }

}
