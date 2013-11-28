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

import org.junit.Test;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class LoginActionBeanTest extends BaseActionBeanTest {

    private MockHttpSession userSession;
    
    @Autowired
    private UserService userService;
    
    /**
     * Simple test: the login JSP is rendered when accessing the LoginActionBean, and there is no authenticated
     * user.
     */
    @Test
    public void testRenderLoginForm() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, LoginActionBean.class);
        rt.execute();
        assertEquals(LoginActionBean.LOGIN_FORM_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Test to ensure that getAuthenticated User returns the user that has been logged in. It should login in to users and have access to the second logged in user.
     */
    @Test
    public void testGetAuthenticatedUser() throws Exception{
        userSession = new MockHttpSession(servletCtx);
            
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        
        SecurityContext ctx = (SecurityContext) userSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        //Log in a second user, context should get the new user
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        
        ctx = (SecurityContext) userSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
    }

}
