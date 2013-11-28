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

import java.net.URL;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Simple tests for the PropertiesPersonDao.
 */
public class PropertiesPersonDaoTest {

    private static final String RESOURCE = "/defaultUsers-test.properties";

    private static final int EXPECTED_USER_COUNT = 9;

    private PropertiesPersonDao underTest;

    /**
     * Loads expected users from the properties file on the classpath.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        URL u = this.getClass().getResource(RESOURCE);
        assertNotNull("Classpath resource " + RESOURCE + " not found.");
        Properties userProps = new Properties();
        userProps.load(u.openStream());

        underTest = new PropertiesPersonDao(userProps);
        // Tests that all six users are present
        assertEquals(EXPECTED_USER_COUNT, underTest.selectPerson().size());
    }

    /**
     * Test that a user can be selected by their email address.
     * 
     * @throws Exception
     */
    @Test
    public void testSelectPersonByEmailAddress() throws Exception {
        Person expected = new Person();
        expected.setId("id:user1");
        expected.setEmailAddress("hanh.p.vu@gmail.com");
        expected.setFirstNames("Hanh");
        expected.setLastNames("Vu");
        expected.setPrefix("prefix");
        expected.setSuffix("suffix");
        expected.setMiddleNames("middle");
        expected.setPreferredPubName("preferredPubName");
        expected.setBio("someBio");
        expected.setWebsite("www.website.com");
        expected.setPassword("foobar");
        expected.setPhoneNumber("5555555555");
        expected.setJobTitle("jobTitle");
        expected.setDepartment("department");
        expected.setCity("city");
        expected.setState("state");
        expected.setInstCompany("institutionCompany");
        expected.setInstCompanyWebsite("www.institutionCompany.com");
        expected.setRegistrationStatus(RegistrationStatus.PENDING);
        expected.setExternalStorageLinked(false);
        expected.setDropboxAppKey("SomeKey");
        expected.setDropboxAppSecret("SomeSecret");

        assertEquals(expected,
                     underTest.selectPersonByEmailAddress("hanh.p.vu@gmail.com"));
    }

    /**
     * Test that a user can be selected by their id.
     * 
     * @throws Exception
     */
    @Test
    public void testSelectPersonById() throws Exception {
        Person expected = new Person();
        expected.setId("id:user1");
        expected.setEmailAddress("hanh.p.vu@gmail.com");
        expected.setFirstNames("Hanh");
        expected.setLastNames("Vu");
        expected.setPrefix("prefix");
        expected.setSuffix("suffix");
        expected.setMiddleNames("middle");
        expected.setPreferredPubName("preferredPubName");
        expected.setBio("someBio");
        expected.setWebsite("www.website.com");
        expected.setPassword("foobar");
        expected.setPhoneNumber("5555555555");
        expected.setJobTitle("jobTitle");
        expected.setDepartment("department");
        expected.setCity("city");
        expected.setState("state");
        expected.setInstCompany("institutionCompany");
        expected.setInstCompanyWebsite("www.institutionCompany.com");
        expected.setRegistrationStatus(RegistrationStatus.PENDING);
        expected.setExternalStorageLinked(false);
        expected.setDropboxAppKey("SomeKey");
        expected.setDropboxAppSecret("SomeSecret");

        assertEquals(expected, underTest.selectPersonById("id:user1"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInsertPerson() throws Exception {
        underTest.insertPerson(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeletePersonById() throws Exception {
        underTest.deletePersonById("id:user1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeletePersonByEmail() throws Exception {
        underTest.deletePersonByEmail("hanh.p.vu@gmail.com");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdatePerson() throws Exception {
        underTest.updatePerson(null);
    }
}
