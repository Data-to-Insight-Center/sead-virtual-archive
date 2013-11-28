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
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests related to verifying the behavior of card
 * <a href="https://scm.dataconservancy.org/issues/browse/DC-728">DC-728</a>
 */
public class UserLoginIT extends BaseIT {

    @Autowired
    @Qualifier("adminUser")
    private Person adminUser;

    @Autowired
    @Qualifier("adminOnlyUser")
    private Person adminOnlyUser;


    @Autowired
    @Qualifier("unapprovedRegisteredUser")
    private Person unapprovedRegisteredUser;

    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person approvedRegisteredUser;

    private HttpClient hc = new DefaultHttpClient();

    @Before
    public void setUp() {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
    }

    /**
     * A user with the ROLE_USER should be able to successfully login.  Successful login is tested by
     * loading the user's profile page.
     */
    @Test
    public void testUserRoleLogin() {
        final Person user = approvedRegisteredUser;

        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_USER,
                user.getRoles().contains(Role.ROLE_USER));

        // URL to test authentication against
        final String profileUrl = urlConfig.getProfileUrl().toString();

        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc,
                reqFactory.createLoginRequest(user.getEmailAddress(), user.getPassword()).asHttpPost(), 300, 399);

        // Attempt to load the user profile after logging in.  Expect a 200, since we are logged in.
        HttpAssert.assertStatus(hc, profileUrl, 200);
    }

    /**
     * A user with the ROLE_ADMIN and ROLE_USER should be able to successfully login.
     */
    @Test
    public void testAdminAndUserRoleLogin() {
        final Person user = adminUser;

        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_ADMIN,
                user.getRoles().contains(Role.ROLE_ADMIN));
        assertTrue("User does not have the required role: " + Role.ROLE_USER,
                user.getRoles().contains(Role.ROLE_USER));


        // URL to test authentication against
        final String profileUrl = urlConfig.getProfileUrl().toString();

        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc,
                reqFactory.createLoginRequest(user.getEmailAddress(), user.getPassword()).asHttpPost(), 300, 399);

        // Attempt to load the user profile after logging in.  Expect a 200, since we are logged in.
        HttpAssert.assertStatus(hc, profileUrl, 200);
    }

    /**
     * A user with the ROLE_ADMIN only should not be able to login.  Right now, roles aren't inherited.  ROLE_ADMIN doesn't
     * also mean that you are ROLE_USER.  ROLE_USER is the role that provides permission to login.
     */
    @Test
    public void testAdminRoleLogin() {
        final Person user = adminOnlyUser;

        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_ADMIN,
                user.getRoles().contains(Role.ROLE_ADMIN));
        assertFalse("User must not have the required role: " + Role.ROLE_USER,
                user.getRoles().contains(Role.ROLE_USER));


        // URL to test authentication against
        final String profileUrl = urlConfig.getProfileUrl().toString();

        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc,
                reqFactory.createLoginRequest(user.getEmailAddress(), user.getPassword()).asHttpPost(), 300, 399);

        // Attempt to load the user profile after logging in.  Expect a 302 redirect back to the Login URL
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);
    }


    /**
     * When invalid credentials are supplied, the login should be denied.
     */
    @Test
    public void testInvalidLogin() {
        Person bogusUser = new Person();
        bogusUser.setId("id:shadyhacker");
        bogusUser.setEmailAddress("shady@hacker.ru");
        bogusUser.setPassword("");
        //let's make it interesting, give the bogus user roles
        List<Role> bogusRoles = new ArrayList<Role>();
        bogusRoles.add(Role.ROLE_ADMIN);
        bogusRoles.add(Role.ROLE_USER);
        bogusUser.setRoles(bogusRoles);

        // URL to test authentication against
        final String profileUrl = urlConfig.getProfileUrl().toString();

        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);
        
        //Attempt to login. expect 302 redirect to the original URL
         HttpAssert.assertStatus(hc,
                reqFactory.createLoginRequest(bogusUser.getEmailAddress(), bogusUser.getPassword()).asHttpPost(), 300, 399);

        // Assert we are not logged in
        HttpAssert.assertStatus(hc, profileUrl,  300, 399);
    }

    /**
     * An approved, registered user should be able to successfully login.  This
     * is the same as {@link #testUserRoleLogin()} right now.
     */
    @Test
    public void testApprovedRegisteredUserLogin() {
        final Person user = approvedRegisteredUser;

        // Verify assumptions
        assertEquals("User does not have the required registration status " + RegistrationStatus.APPROVED,
                RegistrationStatus.APPROVED, user.getRegistrationStatus());
        assertTrue("User does not have the required role: " + Role.ROLE_USER,
                user.getRoles().contains(Role.ROLE_USER));


        // URL to test authentication against
        final String profileUrl = urlConfig.getProfileUrl().toString();

        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc,
                reqFactory.createLoginRequest(user.getEmailAddress(), user.getPassword()).asHttpPost(), 300, 399);

        // Attempt to load the user profile after logging in.  Expect a 200, since we are logged in.
        HttpAssert.assertStatus(hc, profileUrl, 200);
    }

    /**
     * A unapproved, registered user should not be able to login.
     */
    @Test
    public void testUnapprovedRegisteredUserLogin() {
        final Person user = unapprovedRegisteredUser;

        // Verify assumptions
        assertEquals("User does not have the required registration status " + RegistrationStatus.PENDING,
                RegistrationStatus.PENDING, user.getRegistrationStatus());
        assertTrue("User is not expected to be in any roles.", user.getRoles().isEmpty());

        // URL to test authentication against
        final String profileUrl = urlConfig.getProfileUrl().toString();

        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect back to the login url
        HttpAssert.assertStatus(hc,
                reqFactory.createLoginRequest(user).asHttpPost(), 300, 399);

        // Attempt to load the user profile after failing to login.  Expect a 302 redirect back to the login url
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);
    }

}
