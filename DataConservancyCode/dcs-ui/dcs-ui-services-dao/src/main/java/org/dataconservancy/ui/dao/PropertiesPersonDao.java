/*
 * Copyright 2013 Johns Hopkins University
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides Person objects from a Properties object.  See {@code defaultUsers.properties} for an example file.  This
 * implementation is read-only; mutating methods throw {@code UnsupportedOperationException}.
 * <p/>
 * An example serialization of two Person objects in the Java Properties format:
 * <code>
 * admin1.id = id:admin1
 * admin1.emailAddress = emetsger@gmail.com
 * admin1.firstName = Elliot
 * admin1.lastName = Metsger
 * admin1.password = foobar
 * admin1.phoneNumber = 5555555555
 * admin1.registrationStatus = APPROVED
 * admin1.roles = ROLE_ADMIN, ROLE_USER
 *
 * user1.id = id:user1
 * user1.emailAddress = chunkymonkey@benandjerrys.com
 * user1.firstName = Chunky
 * user1.lastName = Monkey
 * user1.password = iheartbananas
 * user1.phoneNumber = 12345678910
 * user1.registrationStatus = APPROVED
 * user1.roles = ROLE_USER
 * </code>
 *
 */
public class PropertiesPersonDao
        implements PersonDAO {

    private final static String READ_ONLY =
            "This PersonDAO implementation is read-only; mutating methods are not "
                    + "supported.";

    private static enum PERSON_FIELD {
        ID,
        EMAILADDRESS,
        FIRSTNAMES,
        LASTNAMES,
        MIDDLENAMES,
        PREFIX,
        SUFFIX,
        PREFERREDPUBNAME,
        BIO,
        WEBSITE,
        PASSWORD,
        REGISTRATIONSTATUS,
        PHONENUMBER,
        JOBTITLE,
        DEPARTMENT,
        CITY,
        STATE,
        INSTCOMPANY,
        INSTCOMPANYWEBSITE,
        ROLES,
        EXTERNALSTORAGELINKED,
        DROPBOXAPPKEY,
        DROPBOXAPPSECRET
    }

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, Person> personByEmailAddress =
            new HashMap<String, Person>();

    private Map<String, Person> personById = new HashMap<String, Person>();

    public PropertiesPersonDao(Properties users) {
        if (users == null) {
            throw new IllegalArgumentException("User properties must not be null.");
        }
        init(users);
    }

    @Override
    public Person selectPersonByEmailAddress(String emailAddress) {
        return personByEmailAddress.get(emailAddress);
    }

    @Override
    public Person selectPersonById(String id) {
        return personById.get(id);
    }

    @Override
    public List<Person> selectPerson(RegistrationStatus status,
                                     Comparator<Person> comparator) {
        List<Person> result = new ArrayList<Person>();
        for (Person p : personById.values()) {
            if (p.getRegistrationStatus() == status) {
                result.add(p);
            }
        }

        if (comparator != null) {
            Collections.sort(result, comparator);
        }
        return result;
    }

    @Override
    public List<Person> selectPerson() {
        List<Person> persons = new ArrayList<Person>();
        for (Person p : personById.values()) {
            persons.add(new Person(p));
        }

        return persons;
    }

    @Override
    public void insertPerson(Person person) {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    @Override
    public void deletePersonById(String id) {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    @Override
    public void deletePersonByEmail(String email) {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    @Override
    public void updatePerson(Person person) {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    private void init(Properties users) {
        // Each property name needs to be unique, so a unique key of some sort is used per user and used in each
        // property name:
        //  admin1.id = foo
        //  admin2.id = bar
        // In this example, 'admin1' and 'admin2' are examples of the unique key.  The implication is that each
        // user in the properties file will have a unique prefix.
        //
        // The propertyPrefixes map keys prefixes to a List of full property names that begin with that prefix:
        //  admin1 -> admin1.id
        //            admin1.roles
        //            admin1.phoneNumber
        //            admin1.emailAddress
        //            ...
        //  admin2 -> admin2.id
        //            admin2.roles
        //            ...
        Map<String, List<String>> propertyPrefixes = new HashMap<String, List<String>>();

        // Populate the propertyPrefixes map
        for (String propName : users.stringPropertyNames()) {
            try {
                String[] keyNamePair = parsePropertyName(propName);
                String key = keyNamePair[0];

                List<String> keyNames;
                if (propertyPrefixes.containsKey(key)) {
                    keyNames = propertyPrefixes.get(key);
                } else {
                    keyNames = new ArrayList<String>();
                    propertyPrefixes.put(key, keyNames);
                }

                keyNames.add(propName);
            } catch (Exception e) {
                log.debug("Unable to parse key from property name '{}'",
                          propName,
                          e);
            }
        }

        // Attempt to create a Person object for each key in the propertyPrefixes map, and populate the
        // "by email" and "by id" Person lookup maps
        for (String key : propertyPrefixes.keySet()) {
            List<String> propNames = propertyPrefixes.get(key);
            Person person = new Person();
            for (String propName : propNames) {
                PERSON_FIELD field = null;

                try {
                    field =
                            PERSON_FIELD.valueOf(parsePropertyName(propName)[1]
                                    .toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.debug("Unable to parse a Person field from property name {}",
                              propName,
                              e);
                    continue;
                }

                String propertyValue = users.getProperty(propName);
                if (propertyValue == null || propertyValue.trim().length() == 0) {
                    log.debug("Property name '" + propName + "' is either empty, null, or missing altogether.");
                    continue;
                }

                switch (field) {
                    case ID:
                        person.setId(propertyValue);
                        break;
                    case EMAILADDRESS:
                        person.setEmailAddress(propertyValue);
                        break;
                    case PHONENUMBER:
                        person.setPhoneNumber(propertyValue);
                        break;
                    case FIRSTNAMES:
                        person.setFirstNames(propertyValue);
                        break;
                    case LASTNAMES:
                        person.setLastNames(propertyValue);
                        break;
                    case MIDDLENAMES:
                        person.setMiddleNames(propertyValue);
                        break;
                    case PREFIX:
                        person.setPrefix(propertyValue);
                        break;
                    case SUFFIX:
                        person.setSuffix(propertyValue);
                        break;
                    case PREFERREDPUBNAME:
                        person.setPreferredPubName(propertyValue);
                        break;
                    case BIO:
                        person.setBio(propertyValue);
                        break;
                    case WEBSITE:
                        person.setWebsite(propertyValue);
                        break;
                    case PASSWORD:
                        person.setPassword(propertyValue);
                        break;
                    case JOBTITLE:
                        person.setJobTitle(propertyValue);
                        break;
                    case DEPARTMENT:
                        person.setDepartment(propertyValue);
                        break;
                    case CITY:
                        person.setCity(propertyValue);
                        break;
                    case STATE:
                        person.setState(propertyValue);
                        break;
                    case INSTCOMPANY:
                        person.setInstCompany(propertyValue);
                        break;
                    case INSTCOMPANYWEBSITE:
                        person.setInstCompanyWebsite(propertyValue);
                        break;
                    case ROLES:
                        String[] roles = propertyValue.split(",");
                        for (String role : roles) {
                            person.addRole(Role.valueOf(role.trim()
                                    .toUpperCase()));
                        }
                        break;
                    case REGISTRATIONSTATUS:
                        person.setRegistrationStatus(RegistrationStatus.valueOf(propertyValue));
                        break;
                    case EXTERNALSTORAGELINKED:
                        person.setExternalStorageLinked(Boolean.parseBoolean(propertyValue));
                        break;
                    case DROPBOXAPPKEY:
                        person.setDropboxAppKey(propertyValue);
                        break;
                    case DROPBOXAPPSECRET:
                        person.setDropboxAppSecret(propertyValue);
                        break;
                    default:
                        log.debug("Unknown Person field named {}", field);
                        break;
                }
            }

            personByEmailAddress.put(person.getEmailAddress(), person);
            personById.put(person.getId(), person);
        }
    }

    private String[] parsePropertyName(String propertyName) {
        if (propertyName == null) {
            throw new IllegalArgumentException("Property name must not be null.");
        }

        if (propertyName.trim().length() == 0) {
            throw new IllegalArgumentException("Property name must not be the zero-length or empty string.");
        }

        String[] parts = propertyName.trim().split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Property name must be in the form <key>.<value>");
        }

        return parts;
    }

}
