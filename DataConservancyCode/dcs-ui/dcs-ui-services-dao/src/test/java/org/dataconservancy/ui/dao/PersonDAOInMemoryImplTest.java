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

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class PersonDAOInMemoryImplTest extends BaseModelTest {

    private PersonDAOInMemoryImpl underTest;

    @Before
    public void setUp() {
        HashMap<String, Person> map = new HashMap<String, Person>();

        map.put(user.getId(), user);
        map.put(newUser.getId(), newUser);

        underTest = new PersonDAOInMemoryImpl(map);
    }

    /**
     * Get an existing person by email address
     */
    @Test
    public void testSelectExistingPersonByEmailAddress() {
        assertNotNull(underTest.selectPersonByEmailAddress(user
                .getEmailAddress()));
        assertEquals(user, underTest.selectPersonByEmailAddress(user
                .getEmailAddress()));
    }

    /**
     * Get an existing person
     */
    @Test
    public void testSelectExistingPersonById() {
        assertNotNull(underTest.selectPersonById(user.getId()));
        assertEquals(user, underTest.selectPersonById(user.getId()));
    }

    /**
     * Get a non-existent person
     */
    @Test
    public void testSelectNonExistentPerson() {
        assertNull(underTest.selectPersonByEmailAddress("non existent"));
    }

    /**
     * Add a new person, then retrieve it
     */
    @Test
    public void testInsertNonExistentPerson() {
        assertNull(underTest.selectPersonByEmailAddress(pendingUser
                .getEmailAddress()));

        underTest.insertPerson(pendingUser);

        assertNotNull(underTest.selectPersonByEmailAddress(pendingUser
                .getEmailAddress()));
        assertEquals(pendingUser,
                     underTest.selectPersonByEmailAddress(pendingUser
                             .getEmailAddress()));
    }

    /**
     * Add a person that already exists
     */
    @Test
    public void testInsertExistingPerson() {
        assertNotNull(underTest.selectPersonById(user.getId()));

        // currently works without an exception
        underTest.insertPerson(user);

        assertNotNull(underTest.selectPersonById(user.getId()));
        assertEquals(user, underTest.selectPersonById(user.getId()));
    }

    /**
     * Delete an existing person
     */
    @Test
    public void testDeleteExistingPersonByEmail() {
        assertNotNull(underTest.selectPersonByEmailAddress(user
                .getEmailAddress()));
        underTest.deletePersonByEmail(user.getEmailAddress());
    }

    /**
     * Delete an existing person
     */
    @Test
    public void testDeleteExistingPersonById() {
        assertNotNull(underTest.selectPersonById(user.getId()));
        underTest.deletePersonById(user.getId());
    }

    /**
     * Delete a non-existent person
     */
    @Test
    public void testDeleteNonExistentPersonByEmail() {
        assertNull(underTest.selectPersonByEmailAddress("foo.bar@baz.com"));

        // currently this works without an exception
        underTest.deletePersonByEmail("foo.bar@baz.com");

        assertNull(underTest.selectPersonByEmailAddress("foo.bar@baz.com"));
    }

    /**
     * Delete a non-existent person
     */
    @Test
    public void testDeleteNonExistentPersonById() {
        assertNull(underTest.selectPersonById("id:foo"));

        // currently this works without an exception
        underTest.deletePersonById("id:foo");

        assertNull(underTest.selectPersonById("id:foo"));
    }

    /**
     * Update an existing person, with changes
     */
    @Test
    public void testUpdateExistingPersonWithChanges() {
        assertEquals(user, underTest.selectPersonById(user.getId()));

        // make a change
        user.setFirstNames("Foo");
        underTest.updatePerson(user);

        assertEquals("Foo", underTest.selectPersonById(user.getId())
                .getFirstNames());
    }

    /**
     * Update an existing person, with no changes
     */
    @Test
    public void testUpdateExistingPersonWithNoChanges() {
        assertEquals(user, underTest.selectPersonById(user.getId()));

        // update without making a change
        // currently this works without an exception
        underTest.updatePerson(user);

        assertEquals(user, underTest.selectPersonById(user.getId()));
    }

    /**
     * Update a non-existent person
     */
    @Test
    public void testUpdateNonExistentPerson() {
        assertNull(underTest.selectPersonByEmailAddress("foo.bar@baz.com"));

        Person person = new Person();
        person.setId("id:foo");
        person.setFirstNames("Foo");
        person.setLastNames("Bar");
        person.setEmailAddress("foo.bar@baz.com");
        underTest.updatePerson(person);

        // Right now updating a non-existent user creates the user
        assertNotNull(underTest.selectPersonByEmailAddress("foo.bar@baz.com"));

    }

}
