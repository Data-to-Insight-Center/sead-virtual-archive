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

package org.dataconservancy.ui.it;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.LoginForm;
import org.dataconservancy.ui.it.support.ProfileForm;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Integration test that tests registering a new user with the system and then
 * editing the profile of a new user. In the system.
 */
public class ProfileEditIT extends BaseIT {

    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person registeredUser;

    @Autowired
    @Qualifier("newUser")
    private Person newUser;

    @Autowired
    @Qualifier("registrationForm")
    private ProfileForm registrationForm;

    @Autowired
    @Qualifier("editProfileForm")
    private ProfileForm editForm;

    @Autowired
    private LoginForm loginForm;

    @Autowired
    private UiUrlConfig config;

    private HttpClient hc = new DefaultHttpClient();

    @Autowired
    private UserService userService;

    @Before
    public void setUp() {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
                                    Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                                    CookiePolicy.BEST_MATCH);
    }

    /**
     * Test registering a user with the system. Currently just checks for a
     * successful return after the registration form is submitted.
     */
    @Test
    public void testRegistration() {

        HttpAssert
                .assertStatus(hc, registrationForm
                        .createRegistrationPost(config,
                                                newUser,
                                                newUser.getPassword()), 200);

        //If registration is successful user should be stored
        /*
         * Person newRegisteredUser =
         * userService.get(newUser.getEmailAddress());
         * Assert.assertNotNull(newRegisteredUser);
         */
    }

    /**
     * Test editing the profile of a user. This test will change the password of
     * a user then login in with the new password credentials.
     */
    @Test
    public void testUserProfileEdit() {

        Person editedUser = new Person(registeredUser);
        editedUser.setPassword("12345");
        editedUser.setFirstNames("MonkeySee");
        //Redirect to the profile page should be returned.
        HttpAssert.assertStatus(hc, editForm
                .editProfilePost(config,
                                 editedUser,
                                 registeredUser.getPassword(),
                                 editedUser.getPassword()), 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect back to the login url
        HttpAssert.assertStatus(hc,
                                loginForm.createLoginPost(config, editedUser),
                                300,
                                399);
        /*
         * Person postEditUser = userService.get(editedUser.getEmailAddress());
         * Assert.assertNotNull(postEditUser); Assert.assertEquals(editedUser,
         * postEditUser);
         */
    }

}