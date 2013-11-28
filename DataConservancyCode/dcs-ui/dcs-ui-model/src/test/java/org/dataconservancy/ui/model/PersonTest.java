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

package org.dataconservancy.ui.model;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class PersonTest
        extends BaseUnitTest {

    private Person two = new Person();

    private Person three = new Person();

    @Before
    public void setUp() {

        List<Role> rolesOne = Arrays.asList(Role.ROLE_ADMIN);
        List<Role> rolesTwo = Arrays.asList(Role.ROLE_USER);
        List<Role> rolesThree = Arrays.asList(Role.ROLE_USER, Role.ROLE_ADMIN);

        user.setRoles(rolesOne);
        user.setReadOnly(false);

        two.setId(user.getId());
        two.setFirstNames(user.getFirstNames());
        two.setLastNames(user.getLastNames());
        two.setPrefix(user.getPrefix());
        two.setSuffix(user.getSuffix());
        two.setMiddleNames(user.getMiddleNames());
        two.setPreferredPubName(user.getPreferredPubName());
        two.setBio(user.getBio());
        two.setWebsite(user.getWebsite());
        two.setJobTitle(user.getJobTitle());
        two.setDepartment(user.getDepartment());
        two.setCity(user.getCity());
        two.setState(user.getState());
        two.setInstCompany(user.getInstCompany());
        two.setInstCompanyWebsite(user.getInstCompanyWebsite());
        two.setEmailAddress(user.getEmailAddress());
        two.setPassword(user.getPassword());
        two.setPhoneNumber(user.getPhoneNumber());
        two.setRoles(rolesTwo);
        two.setReadOnly(false);
        two.setExternalStorageLinked(user.isExternalStorageLinked());
        two.setDropboxAppKey(user.getDropboxAppKey());
        two.setDropboxAppSecret(user.getDropboxAppSecret());

        three.setId(user.getId());
        three.setFirstNames(user.getFirstNames());
        three.setLastNames(user.getLastNames());
        three.setPrefix(user.getPrefix());
        three.setSuffix(user.getSuffix());
        three.setMiddleNames(user.getMiddleNames());
        three.setPreferredPubName(user.getPreferredPubName());
        three.setBio(user.getBio());
        three.setWebsite(user.getWebsite());
        three.setJobTitle(user.getJobTitle());
        three.setDepartment(user.getDepartment());
        three.setCity(user.getCity());
        three.setState(user.getState());
        three.setInstCompany(user.getInstCompany());
        three.setInstCompanyWebsite(user.getInstCompanyWebsite());
        three.setEmailAddress(user.getEmailAddress());
        three.setPassword(user.getPassword());
        three.setPhoneNumber(user.getPhoneNumber());
        three.setRoles(rolesThree);
        three.setReadOnly(false);
        three.setExternalStorageLinked(user.isExternalStorageLinked());
        three.setDropboxAppKey(user.getDropboxAppKey());
        three.setDropboxAppSecret(user.getDropboxAppSecret());

    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(user.equals(user));
        assertFalse(user.equals(admin));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(user.equals(two));
        assertTrue(two.equals(user));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(user.equals(two));
        assertTrue(two.equals(three));
        assertTrue(user.equals(three));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(user.equals(two));
        assertTrue(user.equals(two));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(user.equals(null));
    }

    /**
     * Tests equality between Person objects, user created using the copy
     * constructor.
     */
    @Test
    public void testEqualityUsingCopyConstructor() {
        Person copyOne = new Person(user);

        // transitive
        assertTrue(user.equals(copyOne));
        assertTrue(copyOne.equals(three));
        assertTrue(user.equals(three));

        // symmetric
        assertTrue(user.equals(copyOne));
        assertTrue(copyOne.equals(user));

        // consistent
        assertTrue(user.equals(copyOne));
        assertTrue(user.equals(copyOne));

        // nullity
        assertFalse(copyOne.equals(null));
    }

    /**
     * Tests equality against a PersonWrapper and a class extending
     * PersonWrapper
     */
    @Test
    public void testWrapperEquality() {
        assertTrue(user.equals(new PersonWrapper(user)));
        assertTrue(new PersonWrapper(user).equals(user));
        assertTrue(new PersonWrapper(user).equals(new PersonWrapper(user)));

        assertTrue(user.equals(new PersonWrapper(two)));
        assertTrue(new PersonWrapper(user).equals(two));
        assertTrue(new PersonWrapper(user).equals(new PersonWrapper(two)));

        assertTrue(user.equals(new AnotherWrapper(user)));
        assertTrue(new AnotherWrapper(user).equals(user));
        assertTrue(new AnotherWrapper(user).equals(new AnotherWrapper(user)));
    }

    /**
     * This is what the user details service does.
     */
    private static class AnotherWrapper
            extends PersonWrapper {

        AnotherWrapper(Person person) {
            super(person);
        }
    }
}
