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

import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class RegisterNewUserIT
        extends BaseIT {

    private final static String LOGIN_ERROR_MSG_KEY =
            "org.dataconservancy.ui.stripes.LoginActionBean.loginError";

    private static String loginErrorMsg;

    private Person toRegister;

    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;

    @BeforeClass
    public static void beforeClass() {
        ResourceBundle stripesMessages =
                ResourceBundle.getBundle("StripesResources");
        assertNotNull("Error retrieving Stripes Resource Bundle",
                      stripesMessages);
        loginErrorMsg = stripesMessages.getString(LOGIN_ERROR_MSG_KEY);
        assertNotNull("Missing message for " + LOGIN_ERROR_MSG_KEY,
                      loginErrorMsg);
    }

    @Before
    public void setUp() {
        toRegister = new Person();
        toRegister.setId("id:joedirt");
        toRegister.setFirstNames("Joe");
        toRegister.setLastNames("Dirt");
        toRegister.setPrefix("Mr.");
        toRegister.setSuffix("II");
        toRegister.setMiddleNames("Middle");
        toRegister.setPreferredPubName("J. Dirt");
        toRegister.setBio("Some bio.");
        toRegister.setWebsite("www.joedirt.com");
        toRegister.setEmailAddress("joe@dirt.com");
        toRegister.setPassword("foobar");
        toRegister.setPhoneNumber("123-456-7890");
        toRegister.setJobTitle("Joe The Scientist");
        toRegister.setDepartment("Joe Dirt Department");
        toRegister.setCity("Baltimore");
        toRegister.setState("Maryland");
        toRegister.setInstCompany("Joe Dirt Institution/Company");
        toRegister.setInstCompanyWebsite("www.JoeDirtInstitutionCompany.com");
        toRegister.setExternalStorageLinked(false);
        toRegister.setDropboxAppKey("SomeKey");
        toRegister.setDropboxAppSecret("SomeSecret");
    }

    /**
     * Verify the unregistered user cannot login. Register the unregistered
     * user. Login as admin. List pending registrations. Approve the
     * registration. Logout. Login as the newly registered user.
     * 
     * @throws Exception
     */
    @Test
    public void testApproveNewUserRegistration() throws Exception {
        final DefaultHttpClient hc = new DefaultHttpClient();

        // Override the redirect strategy to redirect on POST.  So we can just test for 200 statuses in the
        // unit test.
        final DefaultRedirectStrategy redirectStrategy =
                new DefaultRedirectStrategy();
        hc.setRedirectStrategy(new RedirectStrategy() {

            @Override
            public boolean isRedirected(HttpRequest request,
                                        HttpResponse response,
                                        HttpContext context)
                    throws ProtocolException {
                if (!redirectStrategy.isRedirected(request, response, context)) {
                    return response.getStatusLine().getStatusCode() == 302;
                }
                return true;
            }

            @Override
            public HttpUriRequest getRedirect(HttpRequest request,
                                              HttpResponse response,
                                              HttpContext context)
                    throws ProtocolException {
                return redirectStrategy.getRedirect(request, response, context);
            }
        });

        // Attempt to login as an unregistered user
        final HttpPost request =
                reqFactory.createLoginRequest(toRegister).asHttpPost();
        HttpResponse resp = hc.execute(request);
        String content = IOUtils.toString(resp.getEntity().getContent());
        assertTrue("Expected response '" + resp
                           + "' to contain failure message '" + loginErrorMsg
                           + "'.  " + "Page content was \n[" + content + "]\n",
                   content.contains(loginErrorMsg));

        // Register a new user
        HttpAssert.assertStatus(hc, reqFactory
                .createRegisterRequest(toRegister).asHttpPost(), 200);

        // Login as admin
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(adminUser)
                .asHttpPost(), 200);

        // View pending registrations
        HttpAssert.assertStatus(hc, reqFactory.listPendingRegistrations()
                .asHttpGet(), 200);

        // Approve registration
        HttpAssert
                .assertStatus(hc,
                              reqFactory
                                      .createApproveRegistrationRequest(toRegister)
                                      .asHttpPost(),
                              200);

        // Logout admin
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest()
                .asHttpGet(), 200);

        // Login as newly registered user
        resp =
                hc.execute(reqFactory.createLoginRequest(toRegister)
                        .asHttpPost());
        content = IOUtils.toString(resp.getEntity().getContent());
        assertFalse("Did NOT expect response '" + resp
                            + "' to contain failure message '" + loginErrorMsg
                            + "'.  " + "Page content was \n[" + content + "]\n",
                    content.contains(loginErrorMsg));
        assertEquals(200, resp.getStatusLine().getStatusCode());
    }
}
