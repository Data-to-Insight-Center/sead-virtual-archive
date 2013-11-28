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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;

import org.dataconservancy.ui.DirtiesContextBaseUnitTest;
import org.junit.After;
import org.junit.Before;

import org.dataconservancy.ui.model.Person;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Base test case for ActionBeans.  ActionBean unit tests should extend this test case to receive a configured
 * {@link MockServletContext}, complete with Spring Security Filters, Stripes Filters, and Stripes Dispatchers.
 * <p/>
 * This base test is configured with a <em>production</em> Spring Application Context, and initializes a mock servlet
 * using the production instance.  Subclasses interact with the application context as normal (e.g. using
 * {@code @Autowired} annotations).  Subclasses should <em>not</em> access the application context directly.
 */
public abstract class BaseActionBeanTest extends DirtiesContextBaseUnitTest implements ApplicationContextAware {

    /**
     * The mock servlet context used to execute requests against, obtained by calling {@link #getMockServletContext()}.
     */
    MockServletContext servletCtx;

    /**
     * Sub-classes should <em>not</em> be using this instance directly.
     */
    private ApplicationContext applicationContext;

    /**
     * Configures the Spring Security Filter, Stripes Filter, and Stripes Dispatcher.  Constructs a Spring
     * Application Context, and attaches it to the mock Servlet Context for use by Spring.
     */
    @Before
    public final void setUp() {
        servletCtx = getMockServletContext();

        // Spring Security Filter
        servletCtx.addFilter(DelegatingFilterProxy.class, "springSecurityFilterChain", Collections.<String, String>emptyMap());

        // Stripes Filter
        Map<String, String> filterParams = new HashMap<String, String>();
        filterParams.put("ActionResolver.Packages", "org.dataconservancy.ui.stripes");
        filterParams.put("Extension.Packages", "org.dataconservancy.ui.stripes");
        filterParams.put("Interceptor.Classes", "net.sourceforge.stripes.integration.spring.SpringInterceptor");
        filterParams.put("TypeConverterFactory.Class", "org.dataconservancy.ui.stripes.CustomTypeConverterFactory");
//        filterParams.put("Extension.Packages", "org.dataconservancy.ui.stripes.ext");
        filterParams.put("ExceptionHandler.Class", "org.dataconservancy.ui.stripes.UiExceptionHandler");
        servletCtx.addFilter(StripesFilter.class, "StripesFilter", filterParams);

        // Theme Filter
        Map<String, String> themeFilterParams = new HashMap<String, String>();
        themeFilterParams.put("themeName", "default");
        servletCtx.addFilter(ThemeFilter.class, "ThemeFilter", themeFilterParams);
        
        // Add the Stripes Dispatcher
        servletCtx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

        // Spin up a Spring Application context and attach it to the servlet context
        GenericWebApplicationContext webApplicationContext = new GenericWebApplicationContext(servletCtx);
        webApplicationContext.setParent(applicationContext);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);
    }

    @After
    public void tearDown() {
        // destroy Stripes filter for every test method; it is the second filter in the list.
        // see http://stripesframework.org/display/stripes/Unit+Testing

        // prevents the following exception on each test
        // ERROR [main]: stripes.controller.StripesFilter@82 2011-10-17 14:46:09,054
        // net.sourceforge.stripes.exception.StripesRuntimeException: Something is trying to access
        // the current Stripes configuration but the current request was never routed through the
        // StripesFilter! As a result the appropriate Configuration object cannot be located. Please
        // take a look at the exact URL in your browser's address bar and ensure that any requests
        // to that URL will be filtered through the StripesFilter according to the filter mappings
        // in your web.xml.
        servletCtx.getFilters().get(1).destroy();
    }

    /**
     * By default, this method returns an un-configured {@link MockServletContext} associated with the root (e.g. "/").
     * Subclasses should have a documented reason for overriding this method.
     * <p/>
     * This method is invoked for each test method.  Subclasses are expected to return a new instance each time this
     * method is called.
     *
     * @return the MockServletContext used for testing
     */
    MockServletContext getMockServletContext() {
        return new MockServletContext("/");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Attempts to authenticate the supplied {@code Person} against the Spring Security login form.  The supplied
     * {@code Person} must have a valid username and password.  A Stripes {@code MockHttpSession} is returned upon
     * successful authentication, which can then be used with {@code MockRoundtrip}.  Round trips that use this
     * HTTP session will be authenticated as {@code p}.
     *
     * @param p the {@code Person} to authenticate as.  The {@code Person} must have a valid username and password
     * @return the authenticated mock HTTP session
     * @throws Exception if authentication fails for any reason.
     */
    final protected MockHttpSession authenticateUser(Person p) throws Exception {
        MockHttpSession userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", p.getEmailAddress());
        rt.setParameter("j_password", p.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) userSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(p.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        return userSession;
    }
}
