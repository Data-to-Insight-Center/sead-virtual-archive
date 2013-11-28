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

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;
import org.dataconservancy.ui.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SpringSecurityLoginTest extends BaseActionBeanTest {

    /**
     * Simple test: when attempting to access the AdminHomeActionBean you are redirected to the login page.  This
     * is implemented by Spring Security.
     */
    @Test
    public void testAdminAccessPromptsLogin() throws Exception {
        MockHttpSession session = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AdminHomeActionBean.class, session);
        rt.execute();
        assertEquals(302, rt.getResponse().getStatus());
        assertNull("Expected to be redirected, not forwarded.", rt.getForwardUrl());
        assertTrue(rt.getRedirectUrl().endsWith("/login/login.action"));
        assertNull("No users were authenticated, so there shouldn't be a SecurityContext present in the session.",
                session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
    }

    /**
     * Simple test: attempt to login as an admin user.  The login request is submitted directly to the url that
     * Spring Security intercepts for form based login.
     *
     * @throws Exception
     */
    @Test
    public void testValidLogin() throws Exception {
        // This session stores access to the Spring Security Context over the course of the test
        MockHttpSession session = new MockHttpSession(servletCtx);

        // Initial req to admin home page, should result in a 302 to the login form
        // No users should be authenticated as no credentials were submitted.
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AdminHomeActionBean.class, session);
        rt.execute();
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().endsWith("/login/login.action"));
        assertNull("No users were authenticated, so there shouldn't be a SecurityContext present in the session.",
                session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));

        // Submit the login form with admin credentials, expect to be redirected to the AdminHome
        // One user - the admin user - should have been authenticated
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", session);
        rt.setParameter("j_username", "admin");
        rt.setParameter("j_password", "ilovec00kies!");
        rt.execute();
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue("Expected to be redirected to a url ending in '/admin/adminhome.action?', but was " + rt.getRedirectUrl() + " instead.",
                rt.getRedirectUrl().endsWith("/admin/adminhome.action?"));

        SecurityContext ctx = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertEquals("admin", ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());

        // Attempt to go back to the admin home page
        // Should result in a 200
        rt = new MockRoundtrip(servletCtx, AdminHomeActionBean.class, session);
        rt.execute();
        assertEquals("Expected 200 but was " + rt.getResponse().getStatus(), 200, rt.getResponse().getStatus());
        assertEquals("Expected to be forwarded to /pages/adminhome.jsp", "/pages/adminhome.jsp", rt.getForwardUrl());

        ctx = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertEquals("admin", ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
    }

    /**
     * Simple test: attempt to access the admin home page with invalid credentials.  The login request is submitted
     * directly to the url that Spring Security intercepts for form based login.
     *
     * @throws Exception
     */
    @Test
    public void testInvalidLogin() throws Exception {
        // This session stores access to the Spring Security Context over the course of the test
        MockHttpSession session = new MockHttpSession(servletCtx);

        // Initial req to admin home page, should result in a 302 to the login form
        // No users should be authenticated as no credentials were submitted.
        MockRoundtrip rt = new MockRoundtrip(servletCtx, AdminHomeActionBean.class, session);
        rt.execute();
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().endsWith("/login/login.action"));
        assertNull("No users were authenticated, so there shouldn't be a SecurityContext present in the session.",
                session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));

        // Submit the login form with invalid credentials, expect to be redirected back to the login form
        // No users should be authenticated
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", session);
        rt.setParameter("j_username", "admin");
        rt.setParameter("j_password", "sadtrombone");
        rt.execute();
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue("Expected to be redirected to a url ending in '/login/login.action', but was " + rt.getRedirectUrl() + " instead.",
                rt.getRedirectUrl().endsWith("/login/login.action"));
        assertNull("No users were authenticated, so there shouldn't be a SecurityContext present in the session.",
                session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));

    }

}
