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

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
public class ProjectServiceTest extends BaseUnitTest {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JdbcTemplate template;
    
    private static final String DELETE_ALL_ROWS_FROM_PROJECT_QUERY = "DELETE FROM PROJECT";
    
    private static final String DELETE_ALL_ROWS_FROM_PERSON_QUERY = "DELETE FROM PERSON";
    
    private static final String COUNT_ALL_ROWS_QUERY = "SELECT COUNT(*) FROM PROJECT";
    
    @Before
    public void setUp() {
        
        template.execute(DELETE_ALL_ROWS_FROM_PROJECT_QUERY);
        
        newUser.setRegistrationStatus(RegistrationStatus.APPROVED);
        user.setRegistrationStatus(RegistrationStatus.APPROVED);
        createOrUpdateUserBypassingRegistrationCreationPolicy(newUser);
        createOrUpdateUserBypassingRegistrationCreationPolicy(user);
        
        // Verify the persons were added to the user service
        assertEquals(newUser, userService.get(newUser.getEmailAddress()));
        assertEquals(user, userService.get(user.getEmailAddress()));
        
        projectOne.removeAllPis();
        projectOne.setId(idService.create(Types.PROJECT.name()).getUrl().toString());
        List<String> adminsList = new ArrayList<String>();
        adminsList.add(newUser.getId());
        adminsList.add(user.getId());
        
        projectOne.setPis(adminsList);
        
        if (template.queryForInt(COUNT_ALL_ROWS_QUERY) > 0) {
            final StringBuffer projectTable = new StringBuffer("PROJECT table was not empty:\n");
            template.query("SELECT * FROM PROJECT", new RowCallbackHandler() {
                
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    projectTable.append("ID: [").append(rs.getString(1)).append("] ");
                    projectTable.append("Name: [").append(rs.getString(2)).append("] ");
                    projectTable.append("Description: [").append(rs.getString(3)).append("] ");
                    projectTable.append("Start Date: [").append(rs.getString(4)).append("] ");
                    projectTable.append("End Date: [").append(rs.getString(5)).append("] ");
                    projectTable.append("Storage Allocated: [").append(rs.getLong(6)).append("] ");
                    projectTable.append("Storage Used: [").append(rs.getLong(7)).append("] ");
                    projectTable.append("Funding Entity: [").append(rs.getString(8)).append("] ");
                    projectTable.append("Publisher: [").append(rs.getString(9)).append("] ");
                    projectTable.append("\n");
                }
            });
            
            fail(projectTable.toString());
        }
        
        assertEquals(0, template.queryForInt(COUNT_ALL_ROWS_QUERY));
    }
    
    @After
    public void tearDown() {
        template.execute(DELETE_ALL_ROWS_FROM_PROJECT_QUERY);
        template.execute(DELETE_ALL_ROWS_FROM_PERSON_QUERY);
        assertEquals(0, template.queryForInt("SELECT count(*) FROM PERSON"));
        assertEquals(0, template.queryForInt("SELECT count(*) FROM PROJECT"));
    }
    
    @Test
    public void testGetProject() {
        Project createdProject;
        
        createdProject = projectService.create(projectOne);
        
        Project retrievedProject = projectService.get(createdProject.getId());
        assertEquals(createdProject, retrievedProject);
        
        // TODO: PIs aren't considered in Project equality?
        for (String person : createdProject.getPis()) {
            assertTrue(retrievedProject.getPis().contains(person));
        }
        for (String person : retrievedProject.getPis()) {
            assertTrue(createdProject.getPis().contains(person));
        }
    }
    
    @Test
    public void testCreateProject() {
        Project createdProject;
        
        createdProject = projectService.create(projectOne);
        
        Project retrievedProject = projectService.get(createdProject.getId());
        assertEquals(retrievedProject, createdProject);
        for (String person : retrievedProject.getPis()) {
            assertTrue(createdProject.getPis().contains(person));
        }
        for (String person : createdProject.getPis()) {
            assertTrue(retrievedProject.getPis().contains(person));
        }
    }
    
    @Test
    public void testUpdateSingularField() {
        Project createdProject;
        try {
            createdProject = projectService.create(projectOne);
            assertEquals(projectOne, createdProject);
            createdProject.setName("A CHANGED PROJECT NAME");
            Project updateProject = projectService.update(createdProject);
            assertEquals(updateProject.getName(), createdProject.getName());
            assertEquals(updateProject, createdProject);
            assertTrue(!updateProject.equals(projectOne));
        }
        catch (ProjectServiceException e) {
            fail(e.toString());
        }
    }
    
    @Test
    public void testUpdatePisList() {
        Project createdProject;
        try {
            createdProject = projectService.create(projectOne);
            Person three = new Person();
            three.setId(idService.create(Types.PERSON.name()).getUrl().toString());
            three.setEmailAddress("Email3@email.com");
            three.setFirstNames("Person");
            three.setLastNames("Three");
            three.setPrefix("Mr.");
            three.setSuffix("II");
            three.setMiddleNames("Spiderish");
            three.setPreferredPubName("P. Three");
            three.setBio("Some bio for the user.");
            three.setWebsite("www.somewebsite.com");
            three.setJobTitle("Monkey Scientist");
            three.setDepartment("Monkey Department");
            three.setCity("Baltimore");
            three.setState("Maryland");
            three.setInstCompany("Monkey Institution/Company");
            three.setInstCompanyWebsite("www.MonkeyInstitutionCompany.com");
            three.setPhoneNumber("3333333333");
            three.setRegistrationStatus(RegistrationStatus.APPROVED);
            three.setPassword("password3");
            three.setExternalStorageLinked(false);
            three.setDropboxAppKey("SomeKey");
            three.setDropboxAppSecret("SomeSecret");
            createdProject.addPi(three.getId());
            createdProject.removePi(user.getId());
            projectService.update(createdProject);
            createOrUpdateUserBypassingRegistrationCreationPolicy(three);
            Project retrievedProject = projectService.get(createdProject.getId());
            assertEquals(retrievedProject, createdProject);
            for (String person : retrievedProject.getPis()) {
                assertTrue(createdProject.getPis().contains(person));
            }
            for (String person : createdProject.getPis()) {
                assertTrue(retrievedProject.getPis().contains(person));
            }
            
        }
        catch (ProjectServiceException e) {
            fail(e.toString());
        }
    }
    
    @Test
    public void testGetNonexisitingProject() {
        Project retrievedProject = projectService.get("23000");
        assertNull(retrievedProject);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateUpdateNullProject() {
        try {
            projectService.create(null);
            projectService.update(null);
        }
        catch (ProjectServiceException e) {
            fail(e.toString());
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateProjectWithNullName() {
        projectOne.setName(null);
        projectService.create(projectOne);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateProjectWithNullDates() {
        projectOne.setStartDate(null);
        projectOne.setEndDate(null);
        projectService.create(projectOne);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateProjectWithNullName() {
        try {
            Project createdProject = projectService.create(projectOne);
            createdProject.setName("");
            projectService.update(createdProject);
        }
        catch (ProjectServiceException e) {
            fail(e.toString());
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateProjectWithNullDates() {
        try {
            Project createdProject = projectService.create(projectOne);
            createdProject.setStartDate(null);
            createdProject.setEndDate(null);
            projectService.update(createdProject);
        }
        catch (ProjectServiceException e) {
            fail(e.toString());
        }
    }
    
    @Test(expected = ProjectServiceException.class)
    public void testUpdateNonExistingProject() throws ProjectServiceException {
        
        Project projectTwo = new Project();
        projectTwo.setId("300");
        List<String> numbers = new ArrayList<String>();
        numbers.add("AWARD1");
        numbers.add("AWARD2");
        projectTwo.setNumbers(numbers);
        projectTwo.setName("AWARD2NAME");
        projectTwo.setDescription("THIS IS AWARD 2 DESCRIPTION");
        projectTwo.setPublisher("THIS IS PUBLISHER 2");
        
        projectTwo.setStartDate(new DateTime("2010-05-26"));
        projectTwo.setEndDate(new DateTime("2016-05-26"));
        
        projectTwo.setStorageAllocated(1000000000000L);
        projectTwo.setStorageUsed(500000000000L);
        projectTwo.setFundingEntity("Self");
        projectService.update(projectTwo);
    }
    
    @Test
    public void testGetAll() {
        List<Project> projectList = projectService.getAll();
        assertNotNull("Expected getProjectList to return a non-null value", projectList);
        assertEquals("Expected projectList to have zero entries", 0, projectList.size());
        
        projectService.create(projectOne);
        projectList = projectService.getAll();
        assertNotNull("Expected getProjectList to return a non-null value");
        assertEquals("Expected project list to have newUser element", 1, projectList.size());
        assertTrue("Expected project list to contain projectOne", projectList.contains(projectOne));
    }
    
    @Test
    public void testIsExisting() {
        projectService.create(projectOne);
        assertTrue(projectService.isExisting(projectOne.getId()));
        assertFalse(projectService.isExisting("NoneExistingProjectId"));
    }
    
    @Test
    public void testFindByPis() {
        
        assertEquals(0, projectService.findByPI(newUser).size());
        Project projectTwo = new Project();
        projectTwo.setId("2");
        List<String> numbers = new ArrayList<String>();
        numbers.add("AWARD3");
        numbers.add("AWARD4");
        projectTwo.setNumbers(numbers);
        projectTwo.setName("AWARD2NAME");
        projectTwo.setDescription("THIS IS AWARD 2 DESCRIPTION");
        projectTwo.setPublisher("THIS IS PUBLISHER 2");
        projectTwo.addPi(newUser.getId());
        
        projectTwo.setStartDate(new DateTime("2010-05-26"));
        projectTwo.setEndDate(new DateTime("2016-05-26"));
        
        projectTwo.setStorageAllocated(1000000000000L);
        projectTwo.setStorageUsed(500000000000L);
        projectTwo.setFundingEntity("Self");
        projectService.create(projectTwo);
        
        Project projectThree = new Project();
        projectThree.setId("3");
        List<String> numbers2 = new ArrayList<String>();
        numbers2.add("AWARD5");
        numbers2.add("AWARD6");
        projectThree.setNumbers(numbers2);
        projectThree.setName("AWARD2NAME");
        projectThree.setDescription("THIS IS AWARD 3 DESCRIPTION");
        projectThree.setPublisher("THIS IS PUBLISHER 3");
        projectThree.addPi(newUser.getId());
        
        projectThree.setStartDate(new DateTime("2010-05-26"));
        projectThree.setEndDate(new DateTime("2016-05-26"));
        
        projectThree.setStorageAllocated(1000000000000L);
        projectThree.setStorageUsed(500000000000L);
        projectThree.setFundingEntity("Self");
        projectService.create(projectThree);
        
        List<Project> projectsByUserOne = projectService.findByPI(newUser);
        
        assertEquals(2, projectsByUserOne.size());
        
        assertTrue(projectsByUserOne.contains(projectThree));
        assertTrue(projectsByUserOne.contains(projectTwo));
    }

    private void createOrUpdateUserBypassingRegistrationCreationPolicy(Person userToCreateOrUpdate) {
        
        final PersonBizPolicyConsultant origPolicyConsultant = userService.getPolicyConsultant();
        
        userService.setPolicyConsultant(new PersonBizPolicyConsultant() {
            @Override
            public boolean enforceRegistrationStatusOnCreate() {
                return false;
            }
            
            @Override
            public List<RegistrationStatus> allowedRegistrationStatusOnCreate() {
                return origPolicyConsultant.allowedRegistrationStatusOnCreate();
            }
            
            @Override
            public RegistrationStatus getDefaultRegistrationStatus() {
                return origPolicyConsultant.getDefaultRegistrationStatus();
            }
            
            @Override
            public List<Role> getRolesForRegistrationStatus(RegistrationStatus status) {
                return origPolicyConsultant.getRolesForRegistrationStatus(status);
            }
        });
        
        final String personId = userToCreateOrUpdate.getId();
        if (userService.get(personId) != null) {
            userService.updateEmailAddress(personId, userToCreateOrUpdate.getEmailAddress());
            userService.updateFirstNames(personId, userToCreateOrUpdate.getFirstNames());
            userService.updateLastNames(personId, userToCreateOrUpdate.getLastNames());
            userService.updatePassword(personId, userToCreateOrUpdate.getPassword());
            userService.updatePhoneNumber(personId, userToCreateOrUpdate.getPhoneNumber());
            userService.updateRegistrationStatus(personId, userToCreateOrUpdate.getRegistrationStatus());
            userService.updateRoles(personId, userToCreateOrUpdate.getRoles());
            
        }
        else {
            userService.create(userToCreateOrUpdate);
        }
        
        userService.setPolicyConsultant(origPolicyConsultant);
    }
}
