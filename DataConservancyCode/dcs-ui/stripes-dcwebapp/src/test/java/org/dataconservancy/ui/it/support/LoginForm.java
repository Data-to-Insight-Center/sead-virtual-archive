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
package org.dataconservancy.ui.it.support;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.dataconservancy.ui.model.Person;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides programmatic access to the form elements of the login jsp page ({@code /pages/login.jsp}.
 * The {@link org.dataconservancy.ui.stripes.LoginActionBean} renders the form, but Spring Security is the form
 * controller.
 * <p/>
 * Programmatic access to the form elements provide necessary information for simulating user logins by POSTing to the
 * form.  This class is generally provided by Spring, and configured in the Spring Application Context.
 *
 * @deprecated use {@link LoginRequest} instead.
 */
public class LoginForm {
    private String usernameFieldName;
    private String passwordFieldName;
    private String actionPath;

    public String getUsernameFieldName() {
        return usernameFieldName;
    }

    public void setUsernameFieldName(String usernameFieldName) {
        this.usernameFieldName = usernameFieldName;
    }

    public String getPasswordFieldName() {
        return passwordFieldName;
    }

    public void setPasswordFieldName(String passwordFieldName) {
        this.passwordFieldName = passwordFieldName;
    }

    public String getActionPath() {
        return actionPath;
    }

    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    /**
     * Creates and prepares a {@code HttpPost} object ready to be executed.  That is, the caller may do the following:
     * <br/>
     * <pre>
     * HttpClient hc = .... ;       // HttpClient is created or injected
     * UiUrlConfig config = .... ;  // Test UI configuration object is injected by Spring
     * LoginForm loginForm = .... ; // Login form is injected by Spring
     *
     * Person user = .... ; // Create or inject the user you wish to authenticate as
     *
     * // Create the login request
     * HttpPost loginPost = loginForm.createLoginPost(config, user);
     *
     * // Execute the request, save the response.
     * HttpResponse response = hc.execute(loginPost);
     *
     * // Examine response for success or failure (note the HttpAssert class may be useful in this regard)
     * </pre>
     * <br/>
     *
     * @param config the UI configuration object
     * @param authenticateAs the Person to authenticate as
     * @return a fully prepared HttpPost representing a login request, ready to be executed by the caller
     */
    public HttpPost createLoginPost(UiUrlConfig config, Person authenticateAs) {
        String loginFormActionUrl = config.getBaseUrl() + actionPath;
        HttpPost loginFormPost = new HttpPost(loginFormActionUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(passwordFieldName, authenticateAs.getPassword()));
        params.add(new BasicNameValuePair(usernameFieldName, authenticateAs.getEmailAddress()));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        loginFormPost.setEntity(entity);
        return loginFormPost;
    }

    public HttpGet createLogoutRequest(UiUrlConfig config) {
        HttpGet get = new HttpGet(config.getLogoutUrl().toString());
        return get;
    }

    @Override
    public String toString() {
        return "LoginForm{" +
                "actionPath='" + actionPath + '\'' +
                ", usernameFieldName='" + usernameFieldName + '\'' +
                ", passwordFieldName='" + passwordFieldName + '\'' +
                '}';
    }
}
