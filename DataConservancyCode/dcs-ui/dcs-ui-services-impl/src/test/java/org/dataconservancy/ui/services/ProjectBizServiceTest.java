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

package org.dataconservancy.ui.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

/**
 * This class tests the Project Biz Service by updating various details of a projectOne, and then using the projectOne
 * service to retrieve the new projectOne.
 */
@DirtiesDatabase
@DirtiesContext
public class ProjectBizServiceTest extends BaseUnitTest {
    
    @Autowired
    private AuthorizationService authorizationService;
    
    @Autowired
    private RelationshipService relationshipService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Autowired
    private ArchiveService archiveService;
    
    private Person one;
    
    private Person two;
    
    private ProjectBizServiceImpl projectBizService;
    
    // private static boolean areObjectsSeeded = false;
    
    @Before
    public void setUp() throws Exception {
        final PersonBizPolicyConsultant pc = userService.getPolicyConsultant();
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {
            
            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return false;
            }
            
            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return pc.allowedRegistrationStatusOnCreate();
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
        
        projectBizService = new ProjectBizServiceImpl();
        projectBizService.setAuthorizationService(authorizationService);
        projectBizService.setProjectService(projectService);
        projectBizService.setRelationshipService(relationshipService);
        projectBizService.setIdService(idService);
        projectBizService.setUserService(userService);
        
        one = new Person();
        one.setId("id:one");
        one.setEmailAddress("Email1@email.com");
        one.setFirstNames("Person");
        one.setLastNames("One");
        one.setPrefix("Mr.");
        one.setSuffix("II");
        one.setMiddleNames("Middle");
        one.setPreferredPubName("P. One");
        one.setBio("Some bio for the user.");
        one.setWebsite("www.somewebsite.com");
        one.setPhoneNumber("1111111111");
        one.setRegistrationStatus(RegistrationStatus.APPROVED);
        one.setJobTitle("Person Scientist");
        one.setDepartment("Person Department");
        one.setCity("Baltimore");
        one.setState("Maryland");
        one.setInstCompany("Person Institution/Company");
        one.setInstCompanyWebsite("www.PersonInstitutionCompany.com");
        one.setPassword("password1");
        one.setExternalStorageLinked(false);
        one.setDropboxAppKey("SomeKey");
        one.setDropboxAppSecret("SomeSecret");
        
        if (null == userService.get("id:one")) {
            userService.create(one);
        }
        
        two = new Person();
        two.setId("id:two");
        two.setEmailAddress("Email2@email.com");
        two.setFirstNames("Person");
        two.setLastNames("Two");
        two.setPrefix("Mr.");
        two.setSuffix("II");
        two.setMiddleNames("Middle");
        two.setPreferredPubName("P. Two");
        two.setBio("Some bio for the user.");
        two.setWebsite("www.somewebsite.com");
        two.setPhoneNumber("2222222222");
        two.setJobTitle("Person Scientist");
        two.setDepartment("Person Department");
        two.setCity("Baltimore");
        two.setState("Maryland");
        two.setInstCompany("Person Institution/Company");
        two.setInstCompanyWebsite("www.PersonInstitutionCompany.com");
        two.setRegistrationStatus(RegistrationStatus.APPROVED);
        two.setPassword("password2");
        two.setExternalStorageLinked(false);
        two.setDropboxAppKey("SomeKey");
        two.setDropboxAppSecret("SomeSecret");
        
        if (null == userService.get("id:two")) {
            userService.create(two);
        }
        
        projectOne.removeAllPis();
        relationshipService.removeAdministratorFromProject(admin, projectOne);
        
        projectOne.addPi(one.getId());
        projectService.update(projectOne);
        
        relationshipService.addAdministratorToProject(projectOne, one);
        
        // TODO: This adds the value repeatedly (I think) so it has been moved to the test.
        // collectionOne = new Collection();
        // collectionOne.setId("A1B1C1");
        // relationshipService.addCollectionToProject(collectionOne, projectOne);
    }
    
    /**
     * Update metadata information on the projectOne, and ensure that it has been correctly changed while mainting
     * unupdated information.
     */
    @Test
    @DirtiesDatabase
    public void testUpdatingProjectInfoByInstanceAdmin() throws BizPolicyException, BizInternalException {
        projectOne.setName("NEW_NAME");
        projectOne.setFundingEntity("CASH");
        List<String> numbers = new ArrayList<String>();
        numbers.add("12345");
        numbers.add("6789");
        projectOne.setNumbers(numbers);
        
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        projectBizService.updateProject(projectOne, one);
        
        Project newProject = projectService.get(projectOne.getId());
        
        Assert.assertNotNull(newProject);
        Assert.assertTrue(newProject.getName().equalsIgnoreCase("NEW_NAME"));
        Assert.assertTrue(newProject.getNumbers().contains("12345"));
        Assert.assertTrue(newProject.getNumbers().contains("6789"));
        Assert.assertTrue(newProject.getFundingEntity().equalsIgnoreCase("CASH"));
        
        Assert.assertTrue(newProject.getDescription().equalsIgnoreCase(projectOne.getDescription()));
        Assert.assertEquals(1, newProject.getPis().size());
        
        Assert.assertTrue(newProject.getPis().get(0).equalsIgnoreCase(one.getId()));
    }
    
    /**
     * Update metadata information on the projectOne, and ensure that it has been correctly changed while mainting
     * unupdated information.
     */
    @Test(expected = BizPolicyException.class)
    @DirtiesDatabase
    public void testUpdatingProjectInfoByRegisteredUser() throws BizPolicyException, BizInternalException {
        projectOne.setName("NEW_NAME");
        projectOne.setFundingEntity("CASH");
        List<String> numbers = new ArrayList<String>();
        numbers.add("12345");
        numbers.add("6789");
        projectOne.setNumbers(numbers);
        
        List<Role> roles = two.getRoles();
        roles.add(Role.ROLE_USER);
        userService.updateRoles(two.getId(), roles);
        projectBizService.updateProject(projectOne, two);
        
    }
    
    /**
     * Test updating the PIs on a projectOne by an instance admin. This tests both adding and removing a pi.
     * 
     * @throws ProjectServiceException
     * @throws BizPolicyException
     */
    @Test
    @DirtiesDatabase
    public void testUpdateProjectAdminsByInstanceAdmin() throws BizPolicyException, BizInternalException {
        // Test adding a PI
        projectOne.addPi(one.getId());
        projectOne.addPi(two.getId());
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        
        projectBizService.updateProject(projectOne, one);
        
        Project newProject = projectService.get(projectOne.getId());
        
        Assert.assertNotNull(newProject);
        Assert.assertEquals(2, newProject.getPis().size());
        
        Assert.assertEquals(2, relationshipService.getAdministratorsForProject(projectOne).size());
        
        // Test removing a PI
        projectOne.removeAllPis();
        projectOne.addPi(one.getId());
        
        projectBizService.updateProject(projectOne, one);
        
        newProject = projectService.get(projectOne.getId());
        
        Assert.assertNotNull(newProject);
        Assert.assertEquals(1, newProject.getPis().size());
        
        Assert.assertEquals(1, relationshipService.getAdministratorsForProject(projectOne).size());
        Assert.assertEquals(one, relationshipService.getAdministratorsForProject(projectOne).iterator().next());
    }
    
    /**
     * Test updating a projectOne with invalid values (null) by an instance admin.
     * 
     * @throws ProjectServiceException
     * @throws BizPolicyException
     */
    @Test(expected = BizPolicyException.class)
    @DirtiesDatabase
    public void testUpdateProjectWithInvalidValuesNull() throws BizPolicyException, BizInternalException {
        // Test adding a PI
        projectOne.addPi(one.getId());
        projectOne.addPi(two.getId());
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        
        projectOne.setName(null);
        projectBizService.updateProject(projectOne, one);
    }
    
    /**
     * Test updating a projectOne with invalid values (empty) by an instance admin.
     * 
     * @throws ProjectServiceException
     * @throws BizPolicyException
     */
    @Test(expected = BizPolicyException.class)
    @DirtiesDatabase
    public void testUpdateProjectWithInvalidValuesEmptyString() throws BizPolicyException, BizInternalException {
        // Test adding a PI
        projectOne.addPi(one.getId());
        projectOne.addPi(two.getId());
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        
        projectOne.setFundingEntity("");
        projectBizService.updateProject(projectOne, one);
    }
    
    /**
     * Test updating a projectOne with invalid values (where start date is after end date) by an instance admin.
     * 
     * @throws ProjectServiceException
     * @throws BizPolicyException
     */
    @Test(expected = BizPolicyException.class)
    @DirtiesDatabase
    public void testUpdateProjectWithInvalidValuesStartDateAfterEndDate() throws BizPolicyException,
            BizInternalException {
        // Test adding a PI
        projectOne.addPi(one.getId());
        projectOne.addPi(two.getId());
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        
        projectOne.setStartDate(new DateTime("2012-05-16"));
        projectOne.setEndDate(new DateTime("2001-05-24"));
        projectBizService.updateProject(projectOne, one);
    }
    
    /**
     * Test updating the PIs on a projectOne by an projectOne admin. This tests both adding and removing a pi.
     * 
     * @throws ProjectServiceException
     * @throws BizPolicyException
     */
    @Test
    @DirtiesDatabase
    public void testUpdateProjectAdminsByProjectAdmin() throws BizPolicyException, BizInternalException {
        // Test adding a PI
        projectOne.addPi(one.getId());
        projectOne.addPi(two.getId());
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_USER);
        userService.updateRoles(one.getId(), roles);
        
        // user one's projectOne admin role has been set in the setup method.
        projectBizService.updateProject(projectOne, one);
        
        Project newProject = projectService.get(projectOne.getId());
        
        Assert.assertNotNull(newProject);
        Assert.assertEquals(2, newProject.getPis().size());
        
        Assert.assertEquals(2, relationshipService.getAdministratorsForProject(projectOne).size());
        
        // Test removing a PI
        projectOne.removeAllPis();
        projectOne.addPi(one.getId());
        
        projectBizService.updateProject(projectOne, one);
        
        newProject = projectService.get(projectOne.getId());
        
        Assert.assertNotNull(newProject);
        Assert.assertEquals(1, newProject.getPis().size());
        
        Assert.assertEquals(1, relationshipService.getAdministratorsForProject(projectOne).size());
        Assert.assertEquals(one, relationshipService.getAdministratorsForProject(projectOne).iterator().next());
        
    }
    
    /**
     * Test adding projectOne by instance admin. Expects success
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test
    @DirtiesDatabase
    public void testAddProjectByInstanceAdmin() throws BizInternalException, BizPolicyException {
        Project newProject = new Project();
        newProject.setId("ProjBizTestId");
        List<String> numbers = new ArrayList<String>();
        numbers.add("AWARD1");
        numbers.add("AWARD2");
        newProject.setNumbers(numbers);
        newProject.setName("AWARD1NAME");
        newProject.setDescription("THIS IS AWARD 1 DESCRIPTION");
        newProject.setPublisher("THIS IS PUBLISHER 1");
        newProject.addPi(one.getId());
        
        newProject.setStartDate(new DateTime("2010-05-26"));
        newProject.setEndDate(new DateTime("2016-05-26"));
        
        newProject.setStorageAllocated(1000000000000L);
        newProject.setStorageUsed(500000000000L);
        newProject.setFundingEntity("Self");
        
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        
        String createdProjectId = projectBizService.updateProject(newProject, one);
        newProject.setId(createdProjectId);
        
        Project createdProject = projectService.get(createdProjectId);
        Assert.assertEquals(newProject, createdProject);
        Assert.assertEquals(one, relationshipService.getAdministratorsForProject(createdProject).iterator().next());
        
    }
    
    /**
     * Test adding projectOne by a registered user. Expect failure, BizPolicyException thrown.
     * 
     * @throws BizInternalException
     * @throws BizPolicyException
     */
    @Test(expected = BizPolicyException.class)
    @DirtiesDatabase
    public void testAddProjectByRegisteredUser() throws BizInternalException, BizPolicyException {
        Project newProject = new Project();
        List<String> numbers = new ArrayList<String>();
        numbers.add("AWARD1");
        numbers.add("AWARD2");
        newProject.setNumbers(numbers);
        newProject.setName("AWARD1NAME");
        newProject.setDescription("THIS IS AWARD 1 DESCRIPTION");
        newProject.setPublisher("THIS IS PUBLISHER 1");
        newProject.addPi(one.getId());
        
        newProject.setStartDate(new DateTime("2010-05-26"));
        newProject.setEndDate(new DateTime("2016-05-26"));
        
        newProject.setStorageAllocated(1000000000000L);
        newProject.setStorageUsed(500000000000L);
        newProject.setFundingEntity("Self");
        
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_USER);
        userService.updateRoles(one.getId(), roles);
        
        String createdProjectId = projectBizService.updateProject(newProject, one);
        newProject.setId(createdProjectId);
        
        Project createdProject = projectService.get(createdProjectId);
        Assert.assertEquals(newProject, createdProject);
    }
    
    /**
     * Test creating projectOne with PI as non-registered user
     */
    @SuppressWarnings("unused")
    @Test(expected = BizPolicyException.class)
    public void testCreatingProjectWithInvalidPi() throws BizInternalException, BizPolicyException {
        two = new Person();
        two.setId("id:two2");
        two.setEmailAddress("InvalidUser@email.com");
        two.setFirstNames("Person");
        two.setLastNames("Two");
        two.setPrefix("Mr.");
        two.setSuffix("II");
        two.setMiddleNames("Middle");
        two.setPreferredPubName("P. Two");
        two.setBio("Some bio for the user.");
        two.setWebsite("www.somewebsite.com");
        two.setPhoneNumber("2222222222");
        two.setJobTitle("Person Scientist");
        two.setDepartment("Person Department");
        two.setCity("Baltimore");
        two.setState("Maryland");
        two.setInstCompany("Person Institution/Company");
        two.setInstCompanyWebsite("www.PersonInstitutionCompany.com");
        two.setRegistrationStatus(RegistrationStatus.APPROVED);
        two.setPassword("password2");
        two.setExternalStorageLinked(false);
        two.setDropboxAppKey("SomeKey");
        two.setDropboxAppSecret("SomeSecret");
        
        Project newProject = new Project();
        List<String> numbers = new ArrayList<String>();
        numbers.add("AWARD1");
        numbers.add("AWARD2");
        newProject.setNumbers(numbers);
        newProject.setName("AWARD1NAME");
        newProject.setDescription("THIS IS AWARD 1 DESCRIPTION");
        newProject.setPublisher("THIS IS PUBLISHER 1");
        newProject.addPi(two.getId());
        
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        
        String createdProjectId = projectBizService.updateProject(newProject, one);
        
    }
    
    @SuppressWarnings("unused")
    @Test
    @DirtiesDatabase
    public void testCreatingProjectWithNullPi() throws BizInternalException, BizPolicyException {
        Project newProject = new Project();
        List<String> numbers = new ArrayList<String>();
        numbers.add("AWARD1");
        numbers.add("AWARD2");
        newProject.setNumbers(numbers);
        newProject.setName("AWARD1NAME");
        newProject.setDescription("THIS IS AWARD 1 DESCRIPTION");
        newProject.setPublisher("THIS IS PUBLISHER 1");
        newProject.setFundingEntity("Cash");
        newProject.setStartDate(new DateTime(2012, 5, 4, 0, 0));
        newProject.setEndDate(new DateTime(2013, 12, 23, 0, 0));
        newProject.addPi(null);
        
        List<Role> roles = one.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(one.getId(), roles);
        
        boolean caughtException = false;
        try {
            String createdProjectId = projectBizService.updateProject(newProject, one);
        }
        catch (BizPolicyException e) {
            caughtException = true;
            assertTrue("Returned message was: " + e.getMessage(),
                    e.getMessage().equalsIgnoreCase("Project's pis must be registered users of the system."));
        }
        
        assertTrue(caughtException);
    }
    
    /**
     * Test the projectOne has the expected associated collections.
     */
    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testGetCollectionsForProject() throws Exception {
        
        Collection collectionOne = new Collection();
        collectionOne.setId("A1B1C1");
        
        archiveService.deposit(collectionOne);
        archiveService.pollArchive();
        
        relationshipService.addCollectionToProject(collectionOne, projectOne);
        
        Set<Collection> list = projectBizService.getCollectionsForProject(projectOne, one);
        assertNotNull(list);
        
        Assert.assertEquals(3, list.size());
        
        try {
            list = projectBizService.getCollectionsForProject(projectOne, two);
        }
        catch (BizPolicyException e) {
            
            // Assert that we threw the exception for an unauthorized user
            assertTrue(true);
        }
    }
    
    /**
     * Test retrieving a projectOne by projectOne admin.
     */
    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testGetExistingProject() throws Exception {
        Project test = projectBizService.getProject(projectOne.getId(), one);
        
        Assert.assertNotNull(test);
        Assert.assertEquals(projectOne, test);
    }
    
    /**
     * Test retrieving a projectOne that does not exist.
     */
    @Test
    @DirtiesDatabase
    @DirtiesContext
    public void testGetNonExistingProject() throws Exception {
        Project test = projectBizService.getProject("project_does_no_exist", one);
        
        Assert.assertNull(test);
    }
    
    /**
     * Test that an authentication exception is thrown in getProject when the user is not logged in.
     */
    @Test
    @DirtiesDatabase
    public void testAuthenticationExceptionInGetProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canReadProject(any(Person.class), any(Project.class))).thenReturn(false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        try {
            projectBizService.getProject(projectOne.getId(), null);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canReadProject(null, projectOne);
            
            assertTrue("Expected an authentication error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHENTICATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
    /**
     * Test that an authentication exception is thrown in getCollectionsForProject when the user is not logged in.
     */
    @Test
    @DirtiesDatabase
    public void testAuthenticationExceptionInGetCollectionsForProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canRetrieveProjectCollections(any(Person.class), any(Project.class))).thenReturn(
                false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        try {
            projectBizService.getCollectionsForProject(projectOne, null);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canRetrieveProjectCollections(null, projectOne);
            
            assertTrue("Expected an authentication error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHENTICATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
    /**
     * Test that an authentication exception is thrown in updateExistingProject when the user is not logged in.
     */
    @Test
    @DirtiesDatabase
    public void testAuthenticationExceptionInUpdateExistingProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canUpdateProject(any(Person.class), any(Project.class))).thenReturn(false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        try {
            projectBizService.updateProject(projectOne, null);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canUpdateProject(null, projectOne);
            
            assertTrue("Expected an authentication error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHENTICATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
    /**
     * Test that an authentication exception is thrown in addProject when the user is not logged in.
     */
    @Test
    @DirtiesDatabase
    public void testAuthenticationExceptionInAddProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canCreateProject(any(Person.class))).thenReturn(false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        Project differentProject = new Project(projectOne);
        differentProject.setId("testAuthenticationExceptionInAddProjectDifferentProject");
        
        try {
            projectBizService.updateProject(differentProject, null);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canCreateProject(null);
            
            assertTrue("Expected an authentication error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHENTICATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
    /**
     * Test that an authorization exception is thrown in getProject when the user does not have permission.
     */
    @Test
    @DirtiesDatabase
    public void testAuthorizationExceptionInGetProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canReadProject(any(Person.class), any(Project.class))).thenReturn(false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        try {
            projectBizService.getProject(projectOne.getId(), one);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canReadProject(one, projectOne);
            
            assertTrue("Expected an authorization error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHORIZATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
    /**
     * Test that an authorization exception is thrown in getCollectionsForProject when the user does not have
     * permission.
     */
    @Test
    @DirtiesDatabase
    public void testAuthorizationExceptionInGetCollectionsForProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canRetrieveProjectCollections(any(Person.class), any(Project.class))).thenReturn(
                false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        try {
            projectBizService.getCollectionsForProject(projectOne, one);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canRetrieveProjectCollections(one, projectOne);
            
            assertTrue("Expected an authorization error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHORIZATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
    /**
     * Test that an authorization exception is thrown in updateExistingProject when the user does not have permission.
     */
    @Test
    @DirtiesDatabase
    public void testAuthorizationExceptionInUpdateExistingProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canUpdateProject(any(Person.class), any(Project.class))).thenReturn(false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        try {
            projectBizService.updateProject(projectOne, one);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canUpdateProject(one, projectOne);
            
            assertTrue("Expected an authorization error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHORIZATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
    /**
     * Test that an authorization exception is thrown in addProject when the user does not have permission.
     */
    @Test
    @DirtiesDatabase
    public void testAuthorizationExceptionInAddProject() throws Exception {
        AuthorizationService mockAuthorizationService = mock(AuthorizationService.class);
        when(mockAuthorizationService.canCreateProject(any(Person.class))).thenReturn(false);
        
        projectBizService.setAuthorizationService(mockAuthorizationService);
        
        Project differentProject = new Project(projectOne);
        differentProject.setId("testAuthorizationExceptionInAddProjectDifferentProject");
        
        try {
            projectBizService.updateProject(differentProject, one);
        }
        catch (BizPolicyException e) {
            // If a BizPolicyException was thrown, check the properties
            verify(mockAuthorizationService).canCreateProject(one);
            
            assertTrue("Expected an authorization error in the BizPolicyException!",
                    BizPolicyException.Type.AUTHORIZATION_ERROR == e.getType());
            
            return;
        }
        
        fail("Expected a BizPolicyException!");
    }
    
}