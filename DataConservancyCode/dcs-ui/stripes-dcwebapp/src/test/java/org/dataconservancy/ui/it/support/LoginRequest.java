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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Request object for performing username and password form logins. Obtain this object as an
 * {@link #asHttpPost() HTTP POST}, and you can execute it with the Apache HTTP client.
 */
public class LoginRequest {

    private final UiUrlConfig urlConfig;
    private final LoginForm form;
    private final String user;
    private final String pass;

    public LoginRequest(UiUrlConfig urlConfig, LoginForm form, String user, String pass) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UrlConfig must not be null.");
        }

        if (pass == null) {
            throw new IllegalArgumentException("Pass must not be null");
        }

        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        if (form == null) {
            throw new IllegalArgumentException("Form must not be null.");
        }

        this.pass = pass;
        this.urlConfig = urlConfig;
        this.user = user;
        this.form = form;
    }

    public HttpPost asHttpPost() {
        HttpPost loginFormPost = new HttpPost(urlConfig.getLoginPostUrl().toString());
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(form.getPasswordFieldName(), pass));
        params.add(new BasicNameValuePair(form.getUsernameFieldName(), user));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        loginFormPost.setEntity(entity);
        return loginFormPost;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "form=" + form +
                ", urlConfig=" + urlConfig +
                ", user='" + user + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }
}
