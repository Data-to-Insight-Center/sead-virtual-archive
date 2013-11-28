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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventManager;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.StatusPropertiesContext;
import org.dataconservancy.ui.services.UserService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * {@code BaseActionBean} includes properties and methods commonly used by its descendant classes.
 */
public abstract class BaseActionBean implements ActionBean {
    private ActionBeanContext abc;
    UserService userService;
    EventManager eventManager;

    /**
     * Contains the build context: SVN revision, build number, and build timestamp.  If it isn't {@code null}, they
     * will be included in the Event Context of fired events.
     */
    private StatusPropertiesContext buildContext;

    /**
     * The HTTP header which may be set by Apache HTTPD mod_proxy.  The value of the header contains the host name
     * that the proxy server forwarded the request for.  If there is only one proxy between the client and the server,
     * the value represents the host name of the client.  Otherwise it will contain a comma delimited list containing
     * the client and all intervening proxy servers.
     */
    private final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * The HTTP header which may be set by Apache HTTPD mod_proxy.  The value of the header contains the original
     * {@code Host} header sent by the client.
     */
    private final String X_FORWARDED_HOST = "X-Forwarded-Host";

    /**
     * Properties object which maps message keys to messages.  After messages are looked up via
     * their key, they are (optionally) parameterized and then returned to the user.
     */
    final Properties messageKeys;

    protected BaseActionBean() {
        // Load static text
        try {
            messageKeys = loadPropertiesFile("/pageText/errorUiText.properties");
            loadPropertiesFile(messageKeys, "/pageText/commonUiText.properties");
            loadPropertiesFile(messageKeys, "/pageText/adminregistrations.properties");
            loadPropertiesFile(messageKeys, "/pageText/adminupdateregistrations.properties");
            loadPropertiesFile(messageKeys,"/pageText/collection_data_list.properties");
            loadPropertiesFile(messageKeys, "/pageText/usercollections.properties");
            loadPropertiesFile(messageKeys, "/pageText/deposit.properties");
            loadPropertiesFile(messageKeys, "/terms_of_use.properties");
            loadPropertiesFile(messageKeys, "/pageText/passwordreset.properties");
            loadPropertiesFile(messageKeys, "/pageText/metadata.properties");
            loadPropertiesFile(messageKeys, "/pageText/userProfileText.properties");
            loadPropertiesFile(messageKeys, "/pageText/ingestPackage.properties");
        } catch (IOException e) {
            throw new RuntimeException("Unable to load static text: " + e.getMessage(), e);
        }
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.abc = context;
    }

    @Override
    public ActionBeanContext getContext() {
        return abc;
    }

    public Person getAuthenticatedUser() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication authn = ctx.getAuthentication();

        if (authn != null && authn.getPrincipal() != null
                && authn.getPrincipal() instanceof Person) {
            Person loggedInUser = (Person) authn.getPrincipal();
            if (loggedInUser != null && userService != null) {
                return userService.get(loggedInUser.getEmailAddress());
            }
            return loggedInUser;
        }
        return null;
    }

    public void loadPropertiesFile(Properties props, String filename) throws IOException {
        //load props with the properties in the file
        if (null == props) {
            props = new Properties();
        }
        InputStream in = getClass().getResourceAsStream(filename);
        if (null != in) {
            props.load(in);
            in.close();
        }
    }

    public Properties loadPropertiesFile(String filename) throws IOException {
        //create a new properties object with the data in the file
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream(filename);
        if (null != in) {
            props.load(in);
            in.close();
        }
        return props;
    }

    public String propertiesFileLoadErrorString() {
        return "Unable to load properties files.";
    }
    public ErrorResolution propertiesFileLoadError() {
        return new ErrorResolution(500, propertiesFileLoadErrorString());
    }

    public String getPageTitle() {
        Properties pageTitleProps;
        try {
            pageTitleProps = loadPropertiesFile("/StripesResources.properties");
            assert(pageTitleProps.containsKey(this.getClass().getCanonicalName() + ".pageTitle"));
        }
        catch (Exception e) {
            return "???" + this.getClass().getCanonicalName() + ".pageTitle???";
        }
        return pageTitleProps.getProperty(this.getClass().getCanonicalName() + ".pageTitle");
    }

    @SpringBean("delegatingUserService")
    public void injectUserService(UserService userService) {
        this.userService = userService;
    }

    @SpringBean("eventManager")
    public void injectEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * The Spring-configured {@code StatusPropertiesContext}.  If this object is injected, error messages will
     * be supplied with this context.
     *
     * @param buildContext the StatusPropertiesContext
     */
    @SpringBean("statusPropertiesContext")
    public void injectStatusPropertiesContext(StatusPropertiesContext buildContext) {
        this.buildContext = buildContext;
    }

    EventContext getEventContext() {
        EventContext eventContext = new EventContext();
        final HttpServletRequest req = getContext().getRequest();
        eventContext.setRequestUri(req.getRequestURI());

        // Handle requests that may have been proxied by Apache http mod_proxy; don't report the proxy server
        // information, report the client's information
        if (req.getHeader(X_FORWARDED_FOR) != null) {
            eventContext.setOriginIp(req.getHeader(X_FORWARDED_FOR));
        } else {
            eventContext.setOriginIp(req.getRemoteAddr());
        }

        if (req.getHeader(X_FORWARDED_HOST) != null) {
            eventContext.setHostName(req.getHeader(X_FORWARDED_HOST));
        } else {
            eventContext.setHostName(req.getServerName());
        }

        if (buildContext != null) {
            eventContext.setBuildDate(buildContext.getBuildTimeStamp());
            eventContext.setBuildNumber(buildContext.getBuildNumber());
            eventContext.setRevisionNumber(buildContext.getBuildRevision());
        } else {
            eventContext.setBuildDate("Unknown");
            eventContext.setBuildNumber("Unknown");
            eventContext.setRevisionNumber("Unknown");
        }

        eventContext.setEventDate(DateTime.now().toString(DateTimeFormat.fullDateTime()));

        eventContext.setActionBean(this.getClass().getName());
        eventContext.setUser((getAuthenticatedUser() != null) ?
                getAuthenticatedUser().getEmailAddress() : "No authenticated user.");

        return eventContext;

    }

}
