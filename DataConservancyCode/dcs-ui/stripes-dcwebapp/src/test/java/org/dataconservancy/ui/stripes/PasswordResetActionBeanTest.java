/*
 * Copyright 2013 Johns Hopkins University
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

import net.sourceforge.stripes.mock.MockRoundtrip;
import org.dataconservancy.ui.exceptions.PasswordResetServiceException;
import org.dataconservancy.ui.model.PasswordResetRequest;
import org.dataconservancy.ui.services.PasswordResetService;
import org.dataconservancy.ui.services.PasswordResetServiceImpl;
import org.dataconservancy.ui.services.UserService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import static junit.framework.Assert.assertEquals;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_PASSWORD_RESET_REQUEST_FAIL;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the PasswordResetActionBean
 */
public class PasswordResetActionBeanTest extends BaseActionBeanTest{
    
    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserService userService;
    
    private PasswordResetRequest validRequest; 
        
    @Before
    public void setup() throws PasswordResetServiceException {
        validRequest = new PasswordResetRequest("123", DateTime.now(), user.getEmailAddress());
    }

    /**
     * The reset request form page should be accessed by a non-registered user, with no requestId
     */
    @Test
    public void testRenderRequestForm() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.execute();
        assertEquals(PasswordResetActionBean.PASSWORD_RESET_REQUEST_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * The password reset form page should be accessed by a non-registered user, with any requestId
     */
    @Test
    public void testRenderResetForm() throws Exception {        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.setParameter("requestId", "anyOldString");
        rt.execute();
        assertEquals(PasswordResetActionBean.PASSWORD_RESET_SUBMIT_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }


    /**
     * Test that a valid email address returns user to correct page
     */
    @Test
    public void testRequestSubmitSuccessForValidAddress() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.setParameter("emailAddress", user.getEmailAddress());
        rt.execute("submitPasswordResetRequest");
        assertEquals(PasswordResetActionBean.REQUEST_SUCCESS_PATH, rt.getForwardUrl());
        assertEquals(200,rt.getResponse().getStatus());
    }

    /**
     * Test that an invalid email address returns user to the correct page
     */
    @Test
    public void testRequestSubmitRequestSuccessForInvalidAddress() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.setParameter("emailAddress", "bogus@nowhere.org");
        rt.execute("submitPasswordResetRequest");
        assertEquals(PasswordResetActionBean.PASSWORD_RESET_REQUEST_PATH, rt.getForwardUrl());
        assertEquals(200,rt.getResponse().getStatus());
    }

    /**
     * Test that the expected error is handled when the password reset service fails to create a request
     */
    @DirtiesContext
    @Test
    public void testErrorCreatingRequest() throws Exception {

        String expectedExceptionString = "Error creating password reset request from testErrorCreatingRequest";

        // Create a mock password reset service to inject into the
        // PasswordResetActionBean for testing exception handling
        PasswordResetService mockPasswordResetService = mock(PasswordResetServiceImpl.class);
        doThrow(new PasswordResetServiceException(expectedExceptionString)).when(mockPasswordResetService).create(user);

        // Inject the mockPasswordResetService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("passwordResetService", mockPasswordResetService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.setParameter("emailAddress", user.getEmailAddress());
        rt.execute("submitPasswordResetRequest");

        //Verify 500 status and the password request error string
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(PasswordResetActionBean.class).messageKeys.getProperty(MSG_KEY_PASSWORD_RESET_REQUEST_FAIL)),
                rt.getResponse().getErrorMessage());
    }


    /**
     * Test that a valid password reset submission returns user to the correct page
     */
    @DirtiesContext
    @Test
    public void testPasswordResetSuccess() throws Exception {
        // Create a mock password reset service to inject into the
        // PasswordResetActionBean for testing exception handling
        PasswordResetService mockPasswordResetService = mock(PasswordResetServiceImpl.class);
        when(mockPasswordResetService.getActiveRequest("123")).thenReturn(validRequest);

        // Inject the mockPasswordResetService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("passwordResetService", mockPasswordResetService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.setParameter("emailAddress", user.getEmailAddress());
        rt.setParameter("password", "555555");
        rt.setParameter("confirmedPassword", "555555");
        rt.setParameter("requestId", "123");
        rt.execute("submitNewPassword");
      
        assertEquals(PasswordResetActionBean.LOGIN_FORM_PATH, rt.getForwardUrl());
        assertEquals(200,rt.getResponse().getStatus());
    }

    /**
     * Test that password reset submission with invalid request id returns user to the correct page
     */
    @DirtiesContext
    @Test
    public void testPasswordResetInvalidRequestIdFails() throws Exception {
        // Create a mock password reset service to inject into the
        // PasswordResetActionBean for testing exception handling
        PasswordResetService mockPasswordResetService = mock(PasswordResetServiceImpl.class);
        when(mockPasswordResetService.getActiveRequest("666")).thenReturn(null);

        // Inject the mockPasswordResetService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("passwordResetService", mockPasswordResetService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.setParameter("emailAddress", user.getEmailAddress());
        rt.setParameter("password", "555555");
        rt.setParameter("confirmedPassword", "555555");
        rt.setParameter("requestId", "666");
        rt.execute("submitNewPassword");

        assertEquals(PasswordResetActionBean.PASSWORD_RESET_SUBMIT_PATH, rt.getForwardUrl());
        assertEquals(200,rt.getResponse().getStatus());
    }

    /**
     * Test that password reset submission with invalid user id returns user to the correct page
     */
    @DirtiesContext
    @Test
    public void testPasswordResetInvalidUserIdFails() throws Exception {
        // Create a mock password reset service to inject into the
        // PasswordResetActionBean for testing exception handling
        PasswordResetService mockPasswordResetService = mock(PasswordResetServiceImpl.class);
        when(mockPasswordResetService.getActiveRequest("234")).thenReturn(validRequest);

        // Inject the mockPasswordResetService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("passwordResetService", mockPasswordResetService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        MockRoundtrip rt = new MockRoundtrip(servletCtx, PasswordResetActionBean.class);
        rt.setParameter("emailAddress", "bogus@bog.us");
        rt.setParameter("password", "555555");
        rt.setParameter("confirmedPassword", "555555");
        rt.setParameter("requestId", "234");
        rt.execute("submitNewPassword");

        assertEquals(PasswordResetActionBean.PASSWORD_RESET_SUBMIT_PATH, rt.getForwardUrl());
        assertEquals(200,rt.getResponse().getStatus());
    }

}
