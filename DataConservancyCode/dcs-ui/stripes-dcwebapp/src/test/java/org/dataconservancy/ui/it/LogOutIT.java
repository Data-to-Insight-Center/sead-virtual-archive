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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.LoginForm;
import org.dataconservancy.ui.it.support.LogoutRequest;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Tests related to verifying the behavior of card
 * <a href="https://scm.dataconservancy.org/issues/browse/DC-728">DC-728</a>
 */
public class LogOutIT extends BaseIT {

    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person approvedRegisteredUser;

    @Autowired
    @Qualifier("adminUser")
    private Person adminUser;

    private HttpClient hc = new DefaultHttpClient();

    private HttpGet logout;

    @Before
    public void setUp() {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);

        logout = reqFactory.createLogoutRequest().asHttpGet();
    }

    /**
     * Test user logout.
     * Successful logout should prevent user from loading user profile page.
     */
    @Test
    public void testAccessProfileAfterLogout() {
        final Person user = approvedRegisteredUser;
        final HttpPost userLogin = reqFactory.createLoginRequest(user).asHttpPost();

        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_USER,
                user.getRoles().contains(Role.ROLE_USER));


        // URL to test authentication against
        final String profileUrl = urlConfig.getProfileUrl().toString();

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc, userLogin, 300, 399);

        // Make sure login attempt was successful by trying to access UserProfile page.
        // Expect a 200, since we are logged in.
        HttpAssert.assertStatus(hc, profileUrl, 200);

        // Logout by sending GET request.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc, logout, 300, 399);


        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, profileUrl, 300, 399);

    }

    /**
     * Test user logout.
     * Successful logout should prevent user from loading user project page.
     */
    @Test
    public void testAccessProjectAfterLogout() {
        final Person user = approvedRegisteredUser;
        final HttpPost userLogin = reqFactory.createLoginRequest(approvedRegisteredUser).asHttpPost();

        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_USER,
                user.getRoles().contains(Role.ROLE_USER));


        // URL to test authentication against
        final String projectUrl = urlConfig.getProjectUrl().toString();

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc, userLogin, 300, 399);

        // Make sure login attempt was successful by trying to access UserProfile page.
        // Expect a 200, since we are logged in.
        HttpAssert.assertStatus(hc, projectUrl, 200);

        // Logout by sending GET request.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc, logout, 300, 399);


        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, projectUrl, 300, 399);

    }

    /**
     * Test user logout.
     * Successful logout should prevent user from loading user project page.
     */
    @Test
    public void testAccessAdminAfterLogout() {
        final Person user = adminUser;
        final HttpPost userLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();

        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_ADMIN,
                user.getRoles().contains(Role.ROLE_ADMIN));


        // URL to test authentication against
        final String adminUrl = urlConfig.getAdminUrl().toString();

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc, userLogin, 300, 399);

        // Make sure login attempt was successful by trying to access UserProfile page.
        // Expect a 200, since we are logged in.
        HttpAssert.assertStatus(hc, adminUrl, 200);

        // Logout by sending GET request.  Expect a 302 redirect to the original URL, the
        // User Profile URL.
        HttpAssert.assertStatus(hc, logout, 300, 399);


        // Attempt to load the user profile without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, adminUrl, 300, 399);

    }
}
