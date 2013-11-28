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
package org.dataconservancy.ui.stripes;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.WebAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Stripes ActionBean for the login page.  It is responsible for rendering the login form, and displaying errors that
 * occurred on previous authentication attempts, if any.  Spring security processes the form submission and is
 * responsible for user authentication throughout the application.
 * <p/>
 * Authentication error messages are placed into the flash scope keyed by {@link #AUTHENTICATION_ERROR_KEY}.  The content
 * of error messages are localized in {@code StripesResources.properties}, and are keyed by the class name of the
 * authentication exception.
 *
 * @see <a href="http://static.springsource.org/spring-security/site/docs/3.0.x/reference/springsecurity.html">Spring Security 3.0.x Reference Documentation</a>
 */
@UrlBinding("/login/login.action")
public class LoginActionBean extends BaseActionBean {

    /**
     * This is the key used when placing authentication error messages into the flash scope.  The view layer can
     * use this key to display the messages, like so:
     * <code>
     *     <stripes:messages key="authentication.failure"/>
     * </code>
     */
    public static String AUTHENTICATION_ERROR_KEY = "authentication.failure";

    /**
     * The path to the login form JSP which is rendered when a user is prompted to authenticate.
     */
    public static String LOGIN_FORM_PATH = "/pages/login.jsp";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Simply renders the login form located at {@link #LOGIN_FORM_PATH}.  Places authentication errors into
     * flash scope.
     *
     * @return forward resolution to the login form
     */
    @DefaultHandler
    public Resolution renderLoginForm() {
        // check the request, then the session, for an authentication exception
        Exception authE = (Exception) getContext().getRequest().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (authE == null) {
            HttpSession s = getContext().getRequest().getSession(false);
            if (s != null) {
                synchronized (s) {
                    authE = (Exception) s.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                    // then clear the exception
                    s.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                }
            }
        }

        // if there was an authentication exception, put a failure message in the flash
        if (authE != null) {
            List<Message> failures = getContext().getMessages(AUTHENTICATION_ERROR_KEY);
            failures.add(new LocalizableMessage(this.getClass().getName() + ".loginError"));
            failures.add(new LocalizableMessage(authE.getClass().getName()));
        }

        // TODO: if (loggedIn) render message else render login form
        return new ForwardResolution(LOGIN_FORM_PATH);
    }

}
