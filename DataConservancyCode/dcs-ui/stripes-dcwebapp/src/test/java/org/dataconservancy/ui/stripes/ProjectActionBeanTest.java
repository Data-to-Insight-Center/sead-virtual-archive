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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 11/29/11 Time: 4:58 PM To change this template use File | Settings |
 * File Templates.
 */
@DirtiesContext
public class ProjectActionBeanTest extends org.dataconservancy.ui.stripes.BaseActionBeanTest {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ProjectBizService projectBizService;
    
    @Autowired
    private RelationshipService relationshipService;
    
    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Autowired
    private JdbcTemplate template;
    
    private MockHttpSession adminSession;
    
    private MockHttpSession userSession;
    
    @Autowired
    @Qualifier("userService")
    private UserService userService;
    
    /**
     * Initialize the mock http session with authenticated user credentials. Tests that re-use this mock session will be
     * already logged in.
     */
    @Before
    public void setUpMockHttpSessions() throws Exception {
        adminSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) adminSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        userSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        ctx = (SecurityContext) userSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
    }
    
    @Before
    public void setUpUserService() {
        final PersonBizPolicyConsultant pc = userService.getPolicyConsultant();
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {
            
            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return pc.enforceRegistrationStatusOnCreate();
            }
            
            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return Arrays.asList(RegistrationStatus.APPROVED, RegistrationStatus.PENDING);
            }
            
            @Override
            public RegistrationStatus getDefaultRegistrationStatus() {
                return pc.getDefaultRegistrationStatus();
            }
            
            @Override
            public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
                return pc.getRolesForRegistrationStatus(status);
            }
        });
    }
    
    @Before
    public void verifyProjectRowCount() {
        template.execute("DELETE FROM PROJECT");
        if (template.queryForInt("SELECT COUNT(*) FROM PROJECT") > 0) {
            final StringBuffer projectTable = new StringBuffer("PROJECT table was not empty:\n");
            template.query("SELECT * FROM PROJECT", new RowCallbackHandler() {
                
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    projectTable.append("ID: [").append(rs.getString(1)).append("] ");
                    projectTable.append("Number: [").append(rs.getString(2)).append("] ");
                    projectTable.append("Name: [").append(rs.getString(3)).append("] ");
                    projectTable.append("Description: [").append(rs.getString(4)).append("] ");
                    projectTable.append("Start Date: [").append(rs.getString(5)).append("] ");
                    projectTable.append("End Date: [").append(rs.getString(6)).append("] ");
                    projectTable.append("Storage Allocated: [").append(rs.getLong(7)).append("] ");
                    projectTable.append("Storage Used: [").append(rs.getLong(8)).append("] ");
                    projectTable.append("Funding Entity: [").append(rs.getString(9)).append("] ");
                    projectTable.append("Publisher: [").append(rs.getString(10)).append("] ");
                    projectTable.append("\n");
                }
            });
            
            fail(projectTable.toString());
        }
        
        assertEquals(0, template.queryForInt("SELECT COUNT(*) FROM PROJECT"));
    }
    
    @After
    public void deleteAllProjectRows() {
        template.execute("DELETE FROM PROJECT");
    }
    
    /*
     * /** Asserts that correct JSP and ActionBean URL is used when viewing the user's project membership
     */
    @Test
    @DirtiesDatabase
    public void testViewUserProjectPath() throws Exception {
        projectService.create(projectOne);
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.addParameter("selectedProjectId", "" + projectOne.getId());
        rt.execute("viewUserProject");
        assertEquals(ProjectActionBean.VIEW_PROJECT_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Asserts that a user must be logged in to view their project memberships
     */
    @Test
    public void testViewUserProjectUnauthenticatedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserProfileActionBean.class);
        rt.execute("viewUserProject");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getDestination().endsWith("/login/login.action"));
    }
    
    /**
     * Asserts that a user must be authorized to view their project
     */
    @Test
    @DirtiesDatabase
    public void testViewUserProjectUnauthorizedUser() throws Exception {
        projectService.create(projectOne);
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, userSession);
        rt.addParameter("selectedProjectId", "" + projectOne.getId());
        rt.execute("viewUserProject");
        assertEquals(403, rt.getResponse().getStatus());
    }
    
    /**
     * Asserts that correct JSP and ActionBean URL is used when adding a project for the user
     */
    @Test
    public void testAddUserProjectPath() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.execute("addUserProject");
        assertEquals(ProjectActionBean.ADD_PROJECT_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Asserts that unauthorized user cannot go to add project form, get redirect to
     */
    @Test
    public void testAddUserProjectPathUnauthorizedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, userSession);
        rt.execute("addUserProject");
        assertEquals(403, rt.getResponse().getStatus());
    }
    
    /**
     * Asserts that a user must be logged in to add a project to their project memberships
     */
    @Test
    public void testAddUserProjectUnauthenticatedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserProfileActionBean.class);
        rt.execute("addUserProject");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getDestination().endsWith("/login/login.action"));
    }
    
    /**
     * Asserts that correct JSP and ActionBean URL is used when editing a project for the user
     */
    @Test
    @DirtiesDatabase
    public void testEditUserProjectPath() throws Exception {
        projectService.create(projectOne);
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.addParameter("selectedProjectId", "" + projectOne.getId());
        rt.execute("editUserProject");
        assertEquals(ProjectActionBean.EDIT_PROJECT_PATH, rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }
    
    /**
     * Asserts that a user must be logged in to edit a project
     */
    @Test
    public void testEditUserProjectUnauthenticatedUser() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class);
        rt.execute("editUserProject");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getDestination().endsWith("/login/login.action"));
    }
    
    /**
     * Asserts that a user with out permission trying to edit a project would result in a 403 http error code
     */
    @Test
    @DirtiesDatabase
    public void testEditUserProjectWithoutPermission() throws Exception {
        projectService.create(projectOne);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, userSession);
        rt.addParameter("selectedProjectId", "" + projectOne.getId());
        rt.execute("editUserProject");
        
        assertEquals(403, rt.getResponse().getStatus());
        
    }
    
    /**
     * Tests that a project is updated by changing the funding entity and description
     * 
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testUserProjectUpdated() throws Exception {
        Person pi = new Person();
        pi.setId("id:pieisgreat");
        pi.setEmailAddress("pieisgreat@cows.com");
        pi.setFirstNames("Cud");
        pi.setLastNames("Cow");
        pi.setPrefix("Mr.");
        pi.setSuffix("II");
        pi.setMiddleNames("Middle");
        pi.setPreferredPubName("C. Cow");
        pi.setBio("Some bio for the user.");
        pi.setWebsite("www.somewebsite.com");
        pi.setJobTitle("Cow Scientist");
        pi.setDepartment("Cow Department");
        pi.setCity("Baltimore");
        pi.setState("Maryland");
        pi.setInstCompany("Cow Institution/Company");
        pi.setInstCompanyWebsite("www.CowInstitutionCompany.com");
        pi.setRegistrationStatus(RegistrationStatus.APPROVED);
        pi.setPhoneNumber("323333333");
        pi.setPassword("password");
        pi.setExternalStorageLinked(false);
        pi.setDropboxAppKey("SomeKey");
        pi.setDropboxAppSecret("SomeSecret");
        
        userService.create(pi);
        
        projectOne.removeAllPis();
        projectOne.addPi(pi.getId());
        
        // Create base project that will be updated.
        projectService.create(projectOne);
        
        // Change a couple values of the project to test that they are being edited.
        projectOne.setFundingEntity("mark");
        projectOne.setDescription("Eating bananas");
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.addParameter("project.name", projectOne.getName());
        rt.addParameter("project.description", projectOne.getDescription());
        rt.addParameter("project.id", "" + projectOne.getId());
        rt.addParameter("project.numbers[0]", projectOne.getNumbers().get(0));
        rt.addParameter("project.numbers[1]", projectOne.getNumbers().get(1));
        rt.addParameter("project.fundingEntity", projectOne.getFundingEntity());
        rt.addParameter("projectAdminIDList[0]", pi.getEmailAddress());
        rt.addParameter("project.startDate", "2010-05-26");
        rt.addParameter("project.endDate", "2016-05-26");
        rt.addParameter("project.storageUsed", String.valueOf(projectOne.getStorageUsed()));
        rt.addParameter("project.storageAllocated", String.valueOf(projectOne.getStorageAllocated()));
        rt.addParameter("project.publisher", projectOne.getPublisher());
        
        rt.execute("userProjectUpdated");
        
        assertEquals(0, rt.getValidationErrors().size());
        assertEquals(projectOne, projectService.get(projectOne.getId()));
    }
    
    /**
     * Tests that a pi can be edited and a pi can be added to a project
     * 
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testAddandEditPI() throws Exception {
        relationshipService.removeAdministratorFromProject(admin, projectOne);
        projectOne.removeAllPis();
        Person pi = new Person();
        pi.setId("id:pidisgreat");
        pi.setEmailAddress("pieisgreat@cows.com");
        pi.setFirstNames("Cud");
        pi.setLastNames("Cow");
        pi.setPrefix("Mr.");
        pi.setSuffix("II");
        pi.setMiddleNames("Middle");
        pi.setPreferredPubName("C. Cow");
        pi.setBio("Some bio for the user.");
        pi.setWebsite("www.somewebsite.com");
        pi.setRegistrationStatus(RegistrationStatus.APPROVED);
        pi.setPhoneNumber("323333333");
        pi.setPassword("password");
        pi.setJobTitle("Cud Scientist");
        pi.setDepartment("Cud Department");
        pi.setCity("Baltimore");
        pi.setState("Maryland");
        pi.setInstCompany("Cud Institution/Company");
        pi.setInstCompanyWebsite("www.CudInstitutionCompany.com");
        pi.setExternalStorageLinked(false);
        pi.setDropboxAppKey("SomeKey");
        pi.setDropboxAppSecret("SomeSecret");
        userService.create(pi);
        
        projectOne.addPi(pi.getId());
        
        // Create base project that will be updated.
        projectService.create(projectOne);
        
        // Edit the pi on the project and test
        Person newPI = new Person();
        newPI.setId("id:newAdmin");
        newPI.setEmailAddress("newAdmin@proj");
        newPI.setFirstNames("Albert");
        newPI.setLastNames("Einstein");
        newPI.setPrefix("Mr.");
        newPI.setSuffix("II");
        newPI.setMiddleNames("Middle");
        newPI.setPreferredPubName("A. Einstein");
        newPI.setBio("Some bio for the user.");
        newPI.setWebsite("www.somewebsite.com");
        newPI.setRegistrationStatus(RegistrationStatus.APPROVED);
        newPI.setPhoneNumber("323333333");
        newPI.setPassword("password");
        newPI.setJobTitle("Albert Scientist");
        newPI.setDepartment("Albert Department");
        newPI.setCity("Baltimore");
        newPI.setState("Maryland");
        newPI.setInstCompany("Albert Institution/Company");
        newPI.setInstCompanyWebsite("www.AlbertInstitutionCompany.com");
        newPI.setExternalStorageLinked(false);
        newPI.setDropboxAppKey("SomeKey");
        newPI.setDropboxAppSecret("SomeSecret");
        userService.create(newPI);
        
        List<String> piList = new ArrayList<String>();
        piList.add(pi.getId());
        piList.add(newPI.getId());
        
        projectOne.setPis(piList);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        
        rt.addParameter("project.name", projectOne.getName());
        rt.addParameter("project.description", projectOne.getDescription());
        rt.addParameter("project.id", "" + projectOne.getId());
        rt.addParameter("project.numbers[0]", projectOne.getNumbers().get(0));
        rt.addParameter("project.numbers[1]", projectOne.getNumbers().get(1));
        rt.addParameter("project.fundingEntity", projectOne.getFundingEntity());
        rt.addParameter("projectAdminIDList[0]", projectOne.getPis().get(0));
        rt.addParameter("projectAdminIDList[1]", newPI.getId());
        rt.addParameter("project.startDate", "2010-05-26");
        rt.addParameter("project.endDate", "2016-05-26");
        rt.addParameter("project.storageUsed", String.valueOf(projectOne.getStorageUsed()));
        rt.addParameter("project.storageAllocated", String.valueOf(projectOne.getStorageAllocated()));
        rt.addParameter("project.publisher", projectOne.getPublisher());
        rt.execute("userProjectUpdated");
        
        assertEquals(0, rt.getValidationErrors().size());
        assertEquals(projectOne, projectService.get(projectOne.getId()));
        
        // Use the relationship service to check the pis on the project
        Set<Person> PIs = relationshipService.getAdministratorsForProject(projectService.get(projectOne.getId()));
        assertNotNull(PIs);
        assertEquals(2, PIs.size());
        
        assertTrue(PIs.contains(newPI));
        
        // Now test adding a new pi to the project.
        projectOne.addPi(pi.getId());
        rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        
        rt.addParameter("project.name", projectOne.getName());
        rt.addParameter("project.description", projectOne.getDescription());
        rt.addParameter("project.id", "" + projectOne.getId());
        rt.addParameter("project.numbers[0]", projectOne.getNumbers().get(0));
        rt.addParameter("project.numbers[1]", projectOne.getNumbers().get(1));
        rt.addParameter("project.fundingEntity", projectOne.getFundingEntity());
        rt.addParameter("projectAdminIDList[0]", newPI.getEmailAddress());
        rt.addParameter("projectAdminIDList[1]", pi.getEmailAddress());
        rt.addParameter("project.startDate", "2010-05-26");
        rt.addParameter("project.endDate", "2016-05-26");
        rt.addParameter("project.storageUsed", String.valueOf(projectOne.getStorageUsed()));
        rt.addParameter("project.storageAllocated", String.valueOf(projectOne.getStorageAllocated()));
        rt.addParameter("project.publisher", projectOne.getPublisher());
        rt.execute("userProjectUpdated");
        
        Project newProj = projectService.get(projectOne.getId());
        assertNotNull(newProj);
        assertEquals(0, rt.getValidationErrors().size());
        
        // Use the relationship service to check the pis on the project
        PIs = relationshipService.getAdministratorsForProject(newProj);
        assertNotNull(PIs);
        assertEquals(2, PIs.size());
        
        assertTrue(PIs.contains(newPI));
        assertTrue(PIs.contains(pi));
        
        // Ensure the rest of the project hasn't been changed.
        assertEquals(projectOne, newProj);
        
    }
    
    @Test
    @DirtiesDatabase
    public void testUserProjectAdded() throws Exception {
        Person pi = new Person();
        pi.setId("id:pi");
        pi.setEmailAddress("pi@cows.com");
        pi.setFirstNames("Moo");
        pi.setLastNames("Cow");
        pi.setPrefix("Mr.");
        pi.setSuffix("II");
        pi.setMiddleNames("Middle");
        pi.setPreferredPubName("C. Cow");
        pi.setBio("Some bio for the user.");
        pi.setWebsite("www.somewebsite.com");
        pi.setRegistrationStatus(RegistrationStatus.APPROVED);
        pi.setPhoneNumber("323333333");
        pi.setPassword("password");
        pi.setJobTitle("Moo Scientist");
        pi.setDepartment("Moo Department");
        pi.setCity("Baltimore");
        pi.setState("Maryland");
        pi.setInstCompany("Moo Institution/Company");
        pi.setInstCompanyWebsite("www.MooInstitutionCompany.com");
        pi.setExternalStorageLinked(false);
        pi.setDropboxAppKey("SomeKey");
        pi.setDropboxAppSecret("SomeSecret");
        userService.create(pi);
        
        // should not get added - not approved
        Person pi2 = new Person();
        pi2.setId("id:2ndPI");
        pi2.setEmailAddress("2ndPI@cows.com");
        pi2.setFirstNames("Hefer");
        pi2.setLastNames("Patty");
        pi2.setPrefix("Mr.");
        pi2.setSuffix("II");
        pi2.setMiddleNames("Middle");
        pi2.setPreferredPubName("H. Patty");
        pi2.setBio("Some bio for the user.");
        pi2.setWebsite("www.somewebsite.com");
        pi2.setRegistrationStatus(RegistrationStatus.APPROVED);
        pi2.setPhoneNumber("323333333");
        pi2.setPassword("password");
        pi2.setJobTitle("Hefer Scientist");
        pi2.setDepartment("Hefer Department");
        pi2.setCity("Baltimore");
        pi2.setState("Maryland");
        pi2.setInstCompany("Hefer Institution/Company");
        pi2.setInstCompanyWebsite("www.HeferInstitutionCompany.com");
        pi2.setExternalStorageLinked(false);
        pi2.setDropboxAppKey("SomeKey");
        pi2.setDropboxAppSecret("SomeSecret");
        userService.create(pi2);
        
        projectOne.removeAllPis();
        projectOne.addPi(pi.getId());
        projectOne.addPi(pi2.getId());
        
        assertNull(projectService.get(projectOne.getId()));
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        
        rt.addParameter("project.name", projectOne.getName());
        rt.addParameter("project.description", projectOne.getDescription());
        rt.addParameter("project.fundingEntity", projectOne.getFundingEntity());
        rt.addParameter("projectAdminIDList[0]", pi.getId());
        rt.addParameter("projectAdminIDList[1]", pi2.getId());
        rt.addParameter("project.numbers[0]", projectOne.getNumbers().get(0));
        rt.addParameter("project.numbers[1]", projectOne.getNumbers().get(1));
        rt.addParameter("project.startDate", "2010-05-26");
        rt.addParameter("project.endDate", "2016-05-26");
        rt.addParameter("project.storageUsed", String.valueOf(projectOne.getStorageUsed()));
        rt.addParameter("project.storageAllocated", String.valueOf(projectOne.getStorageAllocated()));
        rt.addParameter("project.publisher", projectOne.getPublisher());
        rt.execute("userProjectAdded");
        
        String createProjectId = rt.getActionBean(ProjectActionBean.class).getSelectedProjectId();
        
        // EXPECTED the following
        // - No validation error
        // - project was created
        // - project pis properly inserted
        // - mapping of project pis to project admins properly inserted
        
        // check for validation
        assertEquals(0, rt.getValidationErrors().size());
        
        // check that project was created
        assertNotNull(createProjectId);
        assertNotNull(projectService.get(createProjectId));
        projectOne.setId(createProjectId);
        Project createdProject = projectService.get(createProjectId);
        assertEquals(projectOne, createdProject);
        
        // check that project pis was properly inserted
        for (String person : createdProject.getPis()) {
            projectOne.getPis().contains(person);
        }
        for (String person : projectOne.getPis()) {
            createdProject.getPis().contains(person);
        }
        
        // check that mapping between of project admins from project pis were inserted properly
        // Use the relationship service to check that pis are admins on the project.
        Set<Person> pis = relationshipService.getAdministratorsForProject(projectService.get(projectOne.getId()));
        assertNotNull(pis);
        assertEquals(3, pis.size());
        
        // Check that both pis are admins for the project
        assertTrue(pis.contains(pi));
        assertTrue(pis.contains(pi2));
        
        List<Project> projs = projectService.findByPI(pi);
        assertEquals(1, projs.size());
        
        Project test = projs.get(0);
        test.setId(projectOne.getId()); // not important for comparison
        
        assertEquals(projectOne, test);
    }
    
    @Test
    @DirtiesDatabase
    public void testViewSelectedProject() throws Exception {
        projectService.create(projectOne);
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.addParameter("selectedProjectId", "" + projectOne.getId());
        rt.execute("viewUserProject");
        
        ProjectActionBean bean = rt.getActionBean(ProjectActionBean.class);
        
        assertEquals(projectOne.getId(), bean.getSelectedProjectId());
        assertEquals(projectOne, bean.getProject());
    }
    
    @Test
    @DirtiesDatabase
    public void testViewNonExistantProject() throws Exception {
        final String nonExistentProjectId = "foo";
        assertNull(projectService.get(nonExistentProjectId));
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.addParameter("selectedProjectId", "" + nonExistentProjectId);
        rt.execute("viewUserProject");
        
        ProjectActionBean bean = rt.getActionBean(ProjectActionBean.class);
        
        assertEquals(nonExistentProjectId, bean.getSelectedProjectId());
        assertEquals(404, rt.getResponse().getStatus());
    }
    
    /**
     * Update a project via MockRoundTrip. Mock that user came from Project List page Assert that user is redirected to
     * Project list page after project update
     * 
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testReturnToOriginAfterUpdate() throws Exception {
        Person pi = new Person();
        pi.setId("id:pieisgreat");
        pi.setEmailAddress("pieisgreat@cows.com");
        pi.setFirstNames("Cud");
        pi.setLastNames("Cow");
        pi.setPrefix("Mr.");
        pi.setSuffix("II");
        pi.setMiddleNames("Middle");
        pi.setPreferredPubName("C. Cow");
        pi.setBio("Some bio for the user.");
        pi.setWebsite("www.somewebsite.com");
        pi.setRegistrationStatus(RegistrationStatus.APPROVED);
        pi.setPhoneNumber("323333333");
        pi.setPassword("password");
        pi.setJobTitle("Cud Scientist");
        pi.setDepartment("Cud Department");
        pi.setCity("Baltimore");
        pi.setState("Maryland");
        pi.setInstCompany("Cud Institution/Company");
        pi.setInstCompanyWebsite("www.CudInstitutionCompany.com");
        pi.setExternalStorageLinked(false);
        pi.setDropboxAppKey("SomeKey");
        pi.setDropboxAppSecret("SomeSecret");
        
        userService.create(pi);
        
        projectOne.removeAllPis();
        projectOne.addPi(pi.getId());
        
        // Create base project that will be updated.
        projectService.create(projectOne);
        
        // Change a couple values of the project to test that they are being edited.
        projectOne.setFundingEntity("mark");
        projectOne.setDescription("Eating bananas");
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.addParameter("project.name", projectOne.getName());
        rt.addParameter("project.description", projectOne.getDescription());
        rt.addParameter("project.id", "" + projectOne.getId());
        rt.addParameter("project.numbers[0]", projectOne.getNumbers().get(0));
        rt.addParameter("project.numbers[1]", projectOne.getNumbers().get(1));
        rt.addParameter("project.fundingEntity", projectOne.getFundingEntity());
        rt.addParameter("projectAdminIDList[0]", pi.getEmailAddress());
        rt.addParameter("project.startDate", "2010-05-26");
        rt.addParameter("project.endDate", "2016-05-26");
        rt.addParameter("project.storageUsed", String.valueOf(projectOne.getStorageUsed()));
        rt.addParameter("project.storageAllocated", String.valueOf(projectOne.getStorageAllocated()));
        rt.addParameter("project.publisher", projectOne.getPublisher());
        // mock that user comes from Project List page
        rt.addParameter("forwardRequestSource", ProjectActionBean.VIEW_PROJECT_LIST_PATH);
        rt.execute("userProjectUpdated");
        
        // assert that user is directed back to project list page
        String expectedRedirectURLString = new RedirectResolution(ProjectActionBean.class, "viewUserProjectsList")
                .getPath();
        assertTrue(rt.getDestination().contains(expectedRedirectURLString));
        assertEquals(0, rt.getValidationErrors().size());
        assertEquals(projectOne, projectService.get(projectOne.getId()));
    }
    
    @Test
    public void testExportObjectMap() throws Exception {
        projectService.create(projectOne);
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, ProjectActionBean.class, adminSession);
        rt.addParameter("selectedProjectId", "" + projectOne.getId());
        rt.execute("exportObjectMap");
        
        assertEquals(200, rt.getResponse().getStatus());
    }

}
