/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.stripes;

import static junit.framework.Assert.assertEquals;
import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Resource;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.ui.dao.ProjectDAO;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Tests the new data item splash page action bean. Tests that correct error messages are returned for error cases. And
 * that proper people can view the page.
 * 
 * @author Firstname Lastname
 * @version $Id$
 */
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DataItemSplashActionBeanTest extends BaseActionBeanTest {
    
    @Autowired
    private RelationshipService relService;
    
    @Autowired
    private ProjectDAO projectDao;
    
    @Resource(name = "inMemoryArchiveService")
    private ArchiveService archiveService;
    
    private MockHttpSession userSession;
    
    private MockHttpSession adminSession;
    
    Properties props;
    
    @Before
    public void setup() throws Exception {
        // Mock a session for a registered, authorized user.
        userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) userSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        // Mock a session for a system-wide admin user
        adminSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        ctx = (SecurityContext) adminSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        adminSession.setAttribute("project_id", projectOne.getId());
        
        props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
    }
    
    /**
     * Tests that an admin user can view the splash page.
     * 
     * @throws Exception
     */
    @Test
    public void testAdminCanViewSplash() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, DataItemSplashActionBean.class, adminSession);
        rt.setParameter("dataItemID", dataItemOne.getId());
        rt.execute("render");
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Tests that a basic logged in user can view the splash page.
     * 
     * @throws Exception
     */
    @Test
    public void testRegisteredUserCanViewSplash() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, DataItemSplashActionBean.class, userSession);
        rt.setParameter("dataItemID", dataItemOne.getId());
        rt.execute("render");
        
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Tests that someone not logged in can view the splash page.
     * 
     * @throws Exception
     */
    @Test
    public void testAnonymousCanViewSplashPage() throws Exception {
        MockHttpSession session = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, DataItemSplashActionBean.class, session);
        rt.setParameter("dataItemID", dataItemOne.getId());
        rt.execute("render");
        
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Tests that not specifying an id returns a 404 and the proper error message.
     * 
     * @throws Exception
     */
    @Test
    public void testNoIdReturnsError() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, DataItemSplashActionBean.class, adminSession);
        rt.execute("render");
        
        assertEquals(404, rt.getResponse().getStatus());
        
        String errMessage = rt.getResponse().getErrorMessage();
        
        assertEquals(String.format(props.getProperty("error.empty-or-invalid-id"), ""), errMessage);
    }
    
    /**
     * Tests that specifying a bad id (one that doesn't point to a data item) returns a 404 and the proper error
     * message.
     * 
     * @throws Exception
     */
    @Test
    public void testBadIdReturnsError() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, DataItemSplashActionBean.class, adminSession);
        rt.setParameter("dataItemID", "foo");
        rt.execute("render");
        
        assertEquals(404, rt.getResponse().getStatus());
        
        String errMessage = rt.getResponse().getErrorMessage();
        assertEquals(String.format(props.getProperty("error.error-dataitem-not-found"), "foo"), errMessage);
    }
    
}
