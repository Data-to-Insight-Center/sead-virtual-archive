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

package org.dataconservancy.ui.util;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.UserService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserVerificationUtilTest {

    Person person;

    UserService userService;

    @Before
    public void setUp() throws Exception {
        userService = mock(UserService.class);

        person = new Person();
        person.setId("me");
        person.setFirstNames("Foo");
        person.setLastNames("Bar");
        person.addRole(Role.ROLE_ADMIN);
        person.setPassword("em");
    }

    /**
     * Test that if the user is null, then the UserVerificationUtil returns
     * null.
     * 
     * @throws Exception
     */
    @Test
    public void testNullUserReturnsNull() throws Exception {
        assertNull(UserVerificationUtil.VerifyUser(userService, null));
    }

    /**
     * Test that if the userService is null, then the UserVerificationUtil
     * returns null.
     * 
     * @throws Exception
     */
    @Test
    public void testNullUserServiceReturnsNull() throws Exception {
        assertNull(UserVerificationUtil.VerifyUser(null, person));
    }

    /**
     * Test that if the user is not present in the userService then the
     * UserVerificationUtil returns null.
     * 
     * @throws Exception
     */
    @Test
    public void testUserNotInServiceReturnsNull() throws Exception {
        when(userService.get(anyString())).thenReturn(null);
        assertNull(UserVerificationUtil.VerifyUser(userService, person));
    }

    /**
     * Test that if the user id is in the userService but is not equal to the
     * user in the userService, then the UserVerificationUtil returns null.
     * 
     * @throws Exception
     */
    @Test
    public void testUserNotEqualReturnsNull() throws Exception {
        when(userService.get(person.getId())).thenReturn(person);

        Person differentPerson = new Person(person);
        differentPerson.setPassword("empty");

        assertNull(UserVerificationUtil
                .VerifyUser(userService, differentPerson));

        differentPerson = new Person(person);
        differentPerson.addRole(Role.ROLE_USER);

        assertNull(UserVerificationUtil
                .VerifyUser(userService, differentPerson));
    }

    /**
     * Test that if the user is in the userService then it is returned.
     * 
     * @throws Exception
     */
    @Test
    public void testUserInServiceIsEqual() throws Exception {
        when(userService.get(person.getId())).thenReturn(person);
        assertEquals(person,
                     UserVerificationUtil.VerifyUser(userService, person));
    }

}
