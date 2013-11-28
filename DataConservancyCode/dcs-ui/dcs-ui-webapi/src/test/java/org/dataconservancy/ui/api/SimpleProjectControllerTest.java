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

package org.dataconservancy.ui.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Tests the behavior of the ProjectController
 */
@DirtiesDatabase(DirtiesDatabase.AFTER_EACH_TEST_METHOD)
@DirtiesContext
public class SimpleProjectControllerTest extends BaseUnitTest {
    
    /**
     * This class tests the project controller that runs the project HTTP API. The Following Conditions are currently
     * tested: Updating project metadata information Updating project admin list Checking only project admins can update
     * Adding a project Get a list of collections associated with a project.
     */
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectBizService projectBizService;
    
    @Autowired
    private RelationshipService relationshipService;
    
    @Autowired
    private AuthorizationService authorizationService;

    @Resource(name = "inMemoryArchiveService")
    private ArchiveService archiveService;
    
    private ProjectController controller;
    
    @Autowired
    private BusinessObjectBuilder businessObjectBuilder;
    
    private RequestUtil requestUtil;
    
    private final static String TEST_ID_PART = "1";
    
    private final static String TEST_ID_PART_TWO = "2";
    
    private final static String ID_PREFIX = "http://test.org/project/";
    
    private final static String ID_PREFIX_WITH_PORT = "http://test.org:80/project/";
    
    private final static String ADMIN_ID = "admin@email.com";
    
    private final static String PROJECT_ADMIN_ID = "projectAdmin@email.com";
    
    private String testProjectIdOne;
    
    private String testProjectIdTwo;
    
    @Before
    public void setup() throws ArchiveServiceException, RelationshipConstraintException {
        
        final PersonBizPolicyConsultant pc = userService.getPolicyConsultant();
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {
            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return pc.enforceRegistrationStatusOnCreate();
            }
            
            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return Arrays.asList(RegistrationStatus.PENDING, RegistrationStatus.APPROVED);
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
        
        requestUtil = mock(RequestUtil.class);
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn(projectOne.getId());
        
        String idPrefix;
        if (requestUtil.isAlwaysIncludePort()) {
            idPrefix = ID_PREFIX_WITH_PORT;
        }
        else {
            idPrefix = ID_PREFIX;
        }
        
        testProjectIdOne = idPrefix + TEST_ID_PART;
        testProjectIdTwo = idPrefix + TEST_ID_PART_TWO;
        
        controller = new ProjectController(projectService, projectBizService, userService, businessObjectBuilder,
                requestUtil, authorizationService);
    }
    
    /**
     * Tests that the project metadata is successfully updated.
     * 
     * @throws InvalidXmlException
     * @throws ProjectServiceException
     * @throws BizPolicyException
     * @throws IOException
     */
    @Test
    public void testUpdateProject() throws InvalidXmlException, BizInternalException, BizPolicyException, IOException {
        
        Project newProject = new Project(projectOne);
        newProject.setName("Update");
        newProject.setDescription("foo-update");
        newProject.setEndDate(new DateTime(2014, 2, 10, 0, 0));
        
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("PUT", projectOne.getId(), "test.org", 80);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildProject(newProject, sink);
        
        controller.setAuthenticatedUser(admin);
        controller.handleUpdateProjectRequest(projectOne.getId(), mimeType, sink.toByteArray(), mockReq, resp);
        
        assertNotNull(resp);
        assertEquals(resp.getErrorMessage(), 200, resp.getStatus());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp
                .getContentAsByteArray()));
        
        assertNotNull(bop);
        
        Set<Project> projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        Project returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        
        assertTrue(returnedProject.getName().equalsIgnoreCase(newProject.getName()));
        assertTrue(returnedProject.getDescription().equalsIgnoreCase(newProject.getDescription()));
        assertTrue(returnedProject.getEndDate().equals(newProject.getEndDate()));
        
        assertTrue(returnedProject.getFundingEntity().equalsIgnoreCase(newProject.getFundingEntity()));
    }
    
    /**
     * Tests that admin can be added and removed from a project
     * 
     * @throws IOException
     * @throws BizPolicyException
     * @throws ProjectServiceException
     * @throws InvalidXmlException
     * @throws BizInternalException
     */
    @Test
    public void testUpdateProjectAdmin() throws InvalidXmlException, ProjectServiceException, BizPolicyException,
            IOException, BizInternalException {
        
        // Test adding an admin to the project.
        Project newProject = new Project(projectOne);
        newProject.addPi(user.getId());
        
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("PUT", projectOne.getId(), "test.org", 80);
        
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildProject(newProject, sink);
        
        controller.setAuthenticatedUser(admin);
        controller.handleUpdateProjectRequest(newProject.getId(), mimeType, sink.toByteArray(), mockReq, resp);
        
        assertNotNull(resp);
        assertEquals(resp.getErrorMessage(), 200, resp.getStatus());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp
                .getContentAsByteArray()));
        
        assertNotNull(bop);
        
        Set<Project> projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        Project returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        
        assertEquals(newProject.getPis().size(), returnedProject.getPis().size());
        
        // Test removing an admin from the project.
        newProject.removePi(user.getId());
        
        resp = new MockHttpServletResponse();
        
        sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildProject(newProject, sink);
        
        controller.setAuthenticatedUser(admin);
        controller.handleUpdateProjectRequest(newProject.getId(), mimeType, sink.toByteArray(), mockReq, resp);
        
        assertNotNull(resp);
        bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp.getContentAsByteArray()));
        
        assertNotNull(bop);
        
        projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        
        assertEquals(newProject.getPis().size(), returnedProject.getPis().size());
    }
    
    /**
     * Tests adding a project through the API
     * 
     * @throws InvalidXmlException
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     */
    @Test
    public void testAddProject() throws InvalidXmlException, BizInternalException, BizPolicyException, IOException {
        Project newProject = new Project();
        newProject.setName("Test Project To Add");
        newProject.setDescription("adding this project");
        List<String> numbers = new ArrayList<String>();
        numbers.add("1");
        numbers.add("2");
        newProject.setNumbers(numbers);
        newProject.setFundingEntity("The Fed");
        newProject.setStartDate(new DateTime(2012, 5, 4, 0, 0));
        newProject.setEndDate(new DateTime(2013, 12, 23, 0, 0));
        newProject.addPi(admin.getId());
        
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("POST", "/project", "test.org", 80);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildProject(newProject, sink);
        
        controller.setAuthenticatedUser(admin);
        controller.handleProjectPostRequest(mimeType, sink.toByteArray(), mockReq, resp);
        
        assertNotNull(resp);
        
        assertEquals(201, resp.getStatus());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp
                .getContentAsByteArray()));
        
        assertNotNull(bop);
        
        Set<Project> projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        Project returnedProject = projects.iterator().next();
        
        // Have to set the original project id to the id set by the biz service.
        newProject.setId(returnedProject.getId());
        assertEquals(newProject, returnedProject);
    }
    
    /**
     * Tests getting the project's collections
     * 
     * @throws InvalidXmlException
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     */
    @Test
    public void testGetProjectsCollections() throws InvalidXmlException, BizInternalException, BizPolicyException,
            IOException {
        
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("GET", projectOne.getId() + "/collections", "test.org",
                80);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class)))
                .thenReturn(projectOne.getId() + "/collections");
        
        controller.setAuthenticatedUser(admin);
        controller.handleProjectCollectionsGetRequest(projectOne.getId(), mimeType, null, mockReq, resp);
        
        assertNotNull(resp);
        assertEquals(resp.getErrorMessage(), 200, resp.getStatus());
        Bop returnedBop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp
                .getContentAsByteArray()));
        assertNotNull(returnedBop);
        
        Set<Collection> collections = returnedBop.getCollections();
        assertNotNull(collections);
        assertEquals(1, collections.size());
    }
    
    /**
     * Test getting a project
     * 
     * @throws InvalidXmlException
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     */
    @Test
    public void testGetProject() throws InvalidXmlException, BizInternalException, BizPolicyException, IOException {
        
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("GET", projectOne.getId(), "test.org", 80);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        controller.setAuthenticatedUser(admin);
        controller.handleProjectGetRequest(projectOne.getId(), mimeType, null, mockReq, resp);
        
        assertNotNull(resp);
        assertEquals(resp.getErrorMessage(), 200, resp.getStatus());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp
                .getContentAsByteArray()));
        assertNotNull(bop);
        assertEquals(1, bop.getProjects().size());
        assertEquals(projectOne, bop.getProjects().iterator().next());
    }
    
    /**
     * Test getting a project that does not exist
     * 
     * @throws InvalidXmlException
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     */
    @Test
    public void testGetNonExistingProject() throws InvalidXmlException, BizInternalException, BizPolicyException,
            IOException {
        
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class))).thenReturn("  ");
        
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("GET", "/project/" + "cowsarethebest", "test.org", 80);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        controller.setAuthenticatedUser(admin);
        controller.handleProjectGetRequest(projectOne.getId(), mimeType, null, mockReq, resp);
        
        assertNotNull(resp);
        assertEquals(404, resp.getStatus());
    }
    
    /**
     * Test getting all the projects for a user
     * 
     * @throws InvalidXmlException
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testGetAllProjects() throws InvalidXmlException, BizPolicyException, IOException, ServletException {
        
        // Test getting the list of project without logging in
        final String mimeType = "application/xml";
        final MockHttpServletRequest mockReq = newMockRequest("GET", "/project", "test.org", 80);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        controller.handleEmptyGetRequest(mimeType, null, mockReq, resp);
        
        assertNotNull(resp);
        assertEquals(401, resp.getStatus());
        
        // Test getting a single project
        ProjectBizService bizService = mock(ProjectBizService.class);
        HashSet<Project> projects = new HashSet<Project>();
        projects.add(projectOne);
        
        when(bizService.findByAdmin(admin)).thenReturn(projects);
        controller.setBizService(bizService);
        
        controller.setAuthenticatedUser(admin);
        resp = new MockHttpServletResponse();
        controller.handleEmptyGetRequest(mimeType, null, mockReq, resp);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp
                .getContentAsByteArray()));
        assertNotNull(bop);
        assertEquals(1, bop.getProjects().size());
        
        // Test a query that returns no projects
        
        projects.clear();
        
        when(bizService.findByAdmin(user)).thenReturn(projects);
        controller.setAuthenticatedUser(user);
        resp = new MockHttpServletResponse();
        controller.handleEmptyGetRequest(mimeType, null, mockReq, resp);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        
        bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp.getContentAsByteArray()));
        assertNotNull(bop);
        assertEquals(0, bop.getProjects().size());
        
        // Test admin sees all projects in the system
        Project newProject = new Project();
        newProject.setId(testProjectIdTwo);
        newProject.setName("Second_Test_Project");
        newProject.setDescription("foo");
        List<String> numbers = new ArrayList<String>();
        numbers.add("54321");
        numbers.add("9876");
        newProject.setNumbers(numbers);
        newProject.setFundingEntity("moo");
        newProject.setStartDate(new DateTime(2012, 5, 4, 0, 0));
        newProject.setEndDate(new DateTime(2013, 12, 23, 0, 0));
        newProject.addPi(user.getId());
        projectService.create(newProject);
        
        controller.setBizService(projectBizService);
        controller.setAuthenticatedUser(admin);
        resp = new MockHttpServletResponse();
        controller.handleEmptyGetRequest(mimeType, null, mockReq, resp);
        assertNotNull(resp);
        assertEquals(200, resp.getStatus());
        
        bop = businessObjectBuilder.buildBusinessObjectPackage(new ByteArrayInputStream(resp.getContentAsByteArray()));
        assertNotNull(bop);
        assertEquals(projectService.getAll().size(), bop.getProjects().size());
    }
    
    /**
     * Tests the authentication of different calls. Currently tests adding a project, updating a project and getting
     * collections without correct permissions.
     * 
     * @throws InvalidXmlException
     * @throws BizInternalException
     * @throws BizPolicyException
     * @throws IOException
     */
    @Test
    public void testAuthentication() throws InvalidXmlException, BizInternalException, BizPolicyException, IOException {
        Project newProject = new Project(projectOne);
        newProject.setName("Update");
        newProject.setDescription("foo-update");
        newProject.setEndDate(new DateTime(2014, 2, 10, 0, 0));
        
        String mimeType = "application/xml";
        
        // Test updating a project without proper permissions
        MockHttpServletRequest mockReq = newMockRequest("PUT", projectOne.getId(), "test.org", 80);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildProject(newProject, sink);
        
        controller.setAuthenticatedUser(pendingUser);
        controller.handleUpdateProjectRequest(projectOne.getId(), mimeType, sink.toByteArray(), mockReq, resp);
        assertNotNull(resp);
        assertEquals(403, resp.getStatus());
        
        // Test adding a project without proper permissions
        mimeType = "application/xml";
        mockReq = newMockRequest("POST", "/project", "test.org", 80);
        resp = new MockHttpServletResponse();
        
        sink = new ByteArrayOutputStream();
        newProject.setId("http://test.org/project/2");
        businessObjectBuilder.buildProject(newProject, sink);
        
        controller.setAuthenticatedUser(pendingUser);
        controller.handleProjectPostRequest(mimeType, sink.toByteArray(), mockReq, resp);
        assertNotNull(resp);
        assertEquals(403, resp.getStatus());
        
        // Test getting the collections for a project without proper permissions
        mimeType = "application/xml";
        mockReq = newMockRequest("GET", projectOne.getId() + "/collections", "test.org", 80);
        resp = new MockHttpServletResponse();
        
        when(requestUtil.buildRequestUrl(any(HttpServletRequest.class)))
                .thenReturn(projectOne.getId() + "/collections");
        
        controller.setAuthenticatedUser(pendingUser);
        controller.handleProjectCollectionsGetRequest(projectOne.getId(), mimeType, null, mockReq, resp);
        assertNotNull(resp);
        assertEquals(403, resp.getStatus());
        
    }
    
    private MockHttpServletRequest newMockRequest(String method, String requestUri, String host, int port) {
        MockHttpServletRequest req = new MockHttpServletRequest(method, requestUri);
        req.setRemoteHost(host);
        req.setContentType("application/x-www-form-urlencoded");
        req.setRemotePort(port);
        if (port == 443) {
            req.setScheme("https");
            req.setSecure(true);
        }
        else {
            req.setScheme("http");
            req.setSecure(false);
        }
        
        return req;
    }
    
}
