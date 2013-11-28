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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class provide method to map a row returned from PERSON table result set
 * to a {@link org.dataconservancy.ui.model.Person} object.
 */
class PersonRowMapper
        implements RowMapper<Person> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Person mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        Person person = new Person();
        person.setId(resultSet.getString("ID"));
        person.setFirstNames(resultSet.getString("FIRST_NAME"));
        person.setLastNames(resultSet.getString("LAST_NAME"));
        person.setMiddleNames(resultSet.getString("MIDDLE_NAME"));
        person.setPrefix(resultSet.getString("PREFIX"));
        person.setSuffix(resultSet.getString("SUFFIX"));
        person.setPreferredPubName(resultSet.getString("PREFERRED_PUB_NAME"));
        person.setBio(resultSet.getString("BIO"));
        person.setWebsite(resultSet.getString("WEBSITE"));
        person.setCity(resultSet.getString("CITY"));
        person.setState(resultSet.getString("STATE"));
        person.setInstCompany(resultSet.getString("INST_COMPANY"));
        person.setInstCompanyWebsite(resultSet.getString("INST_COMPANY_WEBSITE"));
        person.setDepartment(resultSet.getString("DEPARTMENT"));
        person.setJobTitle(resultSet.getString("JOB_TITLE"));
        person.setEmailAddress(resultSet.getString("EMAIL_ADDRESS"));
        person.setPhoneNumber(resultSet.getString("PHONE"));
        person.setPassword(resultSet.getString("PASSWORD"));
        person.setRegistrationStatus(RegistrationStatus.valueOf(resultSet
                .getString("REGISTRATION_STATUS").toUpperCase()));
        person.setExternalStorageLinked(Boolean.parseBoolean(resultSet.getString("EXTERNAL_STORAGE_LINKED")));
        person.setDropboxAppKey(resultSet.getString("DROPBOX_APP_KEY"));
        person.setDropboxAppSecret(resultSet.getString("DROPBOX_APP_SECRET"));

        String rolesCsv = resultSet.getString("ROLES");
        if (rolesCsv != null && !rolesCsv.isEmpty()) {
            String[] roles = rolesCsv.split(",");
            for (String role : roles) {
                try {
                    person.addRole(Role.valueOf(role.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.debug("Could not add role " + role + " to user "
                                      + person.getEmailAddress() + ": "
                                      + e.getMessage(),
                              e);
                }
            }
        }
        return person;

    }

}
