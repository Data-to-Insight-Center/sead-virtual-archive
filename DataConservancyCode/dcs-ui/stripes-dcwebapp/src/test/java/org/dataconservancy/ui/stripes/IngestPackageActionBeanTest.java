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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.dataconservancy.ui.exceptions.IngestException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class IngestPackageActionBeanTest extends BaseActionBeanTest {
    MockHttpSession adminSession;
    MockHttpSession userSession;

    private DepositManager depositManager;
    private DepositInfo depositInfoSuccess;
    private DepositInfo depositInfoFailure;
    private InputStream input;

    @Before
    public void setUpMockttpSessions() throws Exception {
        
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
        
        depositManager = mock(DepositManager.class);
        depositInfoImpl();

        input = IngestPackageActionBeanTest.class.getResourceAsStream("./sample2.xml");

    }

    /**
     * Verify 200 for administrative access to this page.
     * 
     * @throws Exception
     */
    @Test
    public void testAdminAccess() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, IngestPackageActionBean.class, adminSession);
        rt.execute();

        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Verify anonymous access disallowed.
     * 
     * @throws Exception
     */
    @Test
    public void testAnonAccess() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, IngestPackageActionBean.class);
        rt.execute();

        assertEquals(302, rt.getResponse().getStatus());
    }
    
    /**
     * Verify authorized user can access the page.
     * 
     * @throws Exception
     */
    @Test
    public void testAuthorizedAccess() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, IngestPackageActionBean.class, userSession);
        rt.execute();
        
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Test that admin can ingest a package successfully.
     * 
     * @throws Exception
     */
    @Test
    public void testAdminIngestSuccessful() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, IngestPackageActionBean.class, adminSession);
        rt.execute();
        when(depositManager.deposit(input, null, null, null)).thenReturn(depositInfoSuccess);

        Assert.assertNotNull("Summary can't be null.", depositInfoSuccess.getSummary());
        Assert.assertNotNull("ID can't be null.", depositInfoSuccess.getDepositID());
        Assert.assertTrue("The ingest must have been successful.", depositInfoSuccess.isSuccessful());
        Assert.assertTrue("The ingest must have been completed.", depositInfoSuccess.hasCompleted());
    }
    
    /**
     * Test the failure of package ingest
     * 
     * @throws Exception
     */
    @Test
    public void testAdminIngestFailure() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, IngestPackageActionBean.class, adminSession);
        rt.execute();
        when(depositManager.deposit(input, null, null, null)).thenReturn(depositInfoFailure);
        
        Assert.assertNotNull("Summary can't be null.", depositInfoFailure.getSummary());
        Assert.assertNotNull("ID can't be null.", depositInfoFailure.getDepositID());
        Assert.assertFalse("The ingest must have been a failure.", depositInfoFailure.isSuccessful());
        Assert.assertTrue("The ingest must have been completed though a failure.", depositInfoFailure.hasCompleted());
    }

    /**
     * Insures that the package file that was uploaded is deleted by the action bean after the deposit is attempted.
     *
     * @throws Exception
     */
    @Test
    public void testUploadedFileDeleted() throws Exception {
        File toUpload = mock(File.class);
        File temp = File.createTempFile("IngestPackageActionBean-", ".tmp");
        String tempName = temp.getName();
        String path = temp.getAbsolutePath();
        when(toUpload.getName()).thenReturn(tempName);
        when(toUpload.getAbsolutePath()).thenReturn(path);
        when(toUpload.exists()).thenReturn(true);
        when(toUpload.canWrite()).thenReturn(true);

        IngestPackageActionBean underTest = new IngestPackageActionBean();
        underTest.setUploadedFile(new FileBean(toUpload, "application/text", toUpload.getName()));
        try {
            underTest.ingest();
        } catch (Exception e) {
            // don't care
        }
        verify(toUpload).delete();
    }

    /**
     * Implementations of DepositInfo to test different scenarios
     */
    private void depositInfoImpl() {
        depositInfoSuccess = new DepositInfo() {
            
            @Override
            public boolean isSuccessful() {
                return true;
            }
            
            @Override
            public boolean hasCompleted() {
                return true;
            }
            
            @Override
            public String getSummary() {
                return "The ingest was successful.";
            }
            
            @Override
            public Map<String, String> getMetadata() {
                return null;
            }
            
            @Override
            public String getManagerID() {
                return null;
            }
            
            @Override
            public DepositDocument getDepositStatus() {
                return null;
            }
            
            @Override
            public String getDepositID() {
                return "UniqueDepositIDSuccess";
            }
            
            @Override
            public DepositDocument getDepositContent() {
                return null;
            }
        };
        
        depositInfoFailure = new DepositInfo() {
            
            @Override
            public boolean isSuccessful() {
                return false;
            }
            
            @Override
            public boolean hasCompleted() {
                return true;
            }
            
            @Override
            public String getSummary() {
                return "The ingest has failed.";
            }
            
            @Override
            public Map<String, String> getMetadata() {
                return null;
            }
            
            @Override
            public String getManagerID() {
                return null;
            }
            
            @Override
            public DepositDocument getDepositStatus() {
                return null;
            }
            
            @Override
            public String getDepositID() {
                return "UniqueDepositIDFailure";
            }
            
            @Override
            public DepositDocument getDepositContent() {
                return null;
            }
        };
    }

}
