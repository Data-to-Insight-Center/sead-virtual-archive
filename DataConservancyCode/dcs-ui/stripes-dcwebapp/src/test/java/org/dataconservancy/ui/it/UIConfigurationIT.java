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
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class UIConfigurationIT extends BaseIT {

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

    private HttpPost adminUserLogin;
    private HttpPost adminOnlyUserLogin;
    private HttpPost unapprovedRegisteredUserLogin;
    private HttpPost approvedRegisteredUserLogin;
    private HttpGet logout;

    @Before
    public void setUp() {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);


        adminUserLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        adminOnlyUserLogin = reqFactory.createLoginRequest(adminOnlyUser).asHttpPost();
        unapprovedRegisteredUserLogin = reqFactory.createLoginRequest(unapprovedRegisteredUser).asHttpPost();
        approvedRegisteredUserLogin = reqFactory.createLoginRequest(approvedRegisteredUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
    }


    @Test
    public void testGetUiConfigurationPageAsAdmin() {
        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_ADMIN,
                adminUser.getRoles().contains(Role.ROLE_ADMIN));

        // URL to test authentication against
        final String uiConfigUrl = urlConfig.getUiConfigUrl().toString();

        // Attempt to load the UI Config page without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, uiConfigUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399);

        // Attempt to load the UI Config page after logging in.  Expect a 200, since we are logged in.
        HttpAssert.assertStatus(hc, uiConfigUrl, 200);
    }

    @Test
    public void testGetUiConfigurationPageAsApprovedRegisteredUser() {
        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_USER,
                approvedRegisteredUser.getRoles().contains(Role.ROLE_USER));

        // URL to test authentication against
        final String uiConfigUrl = urlConfig.getUiConfigUrl().toString();

        // Attempt to load the UI Config page without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, uiConfigUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL.
        HttpAssert.assertStatus(hc, approvedRegisteredUserLogin, 300, 399);

        // Attempt to load the UI Config page after logging in.  Expect a 403, since we aren't authorized.
        HttpAssert.assertStatus(hc, uiConfigUrl, 403);
    }

    @Test
    public void testGetUiConfigurationPageAsUnapprovedRegisteredUser() {
        // Verify assumptions
        assertTrue("User should have no roles", unapprovedRegisteredUser.getRoles().isEmpty());

        // URL to test authentication against
        final String uiConfigUrl = urlConfig.getUiConfigUrl().toString();

        // Attempt to load the UI Config page without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, uiConfigUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL.
        HttpAssert.assertStatus(hc, unapprovedRegisteredUserLogin, 300, 399);

        // Attempt to load the UI Config page after logging in.  Expect a 302 back to the login url, since we aren't authorized.
        HttpAssert.assertStatus(hc, uiConfigUrl, 302);
    }

    @Test
    public void testGetUiConfigurationPageAsAdminOnlyUser() {
        // Verify assumptions
        assertTrue("User does not have the required role: " + Role.ROLE_ADMIN,
                adminOnlyUser.getRoles().contains(Role.ROLE_ADMIN));

        assertTrue("User should not have role: " + Role.ROLE_USER,
                !adminOnlyUser.getRoles().contains(Role.ROLE_USER));

        // URL to test authentication against
        final String uiConfigUrl = urlConfig.getUiConfigUrl().toString();

        // Attempt to load the UI Config page without logging in.  Expect a 302 redirect, but
        // accept a range (300-399).
        HttpAssert.assertStatus(hc, uiConfigUrl, 300, 399);

        // Login by POSTing to the Login Form.  Expect a 302 redirect to the original URL.
        HttpAssert.assertStatus(hc, adminOnlyUserLogin, 300, 399);

        // Attempt to load the UI Config page after logging in.  Expect a 302 back to the login URL since we aren't authorized
        HttpAssert.assertStatus(hc, uiConfigUrl, 302);
    }
}
