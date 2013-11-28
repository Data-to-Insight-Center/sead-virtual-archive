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
package org.dataconservancy.ui.dao;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = { "classpath*:/org/dataconservancy/ui/config/test-applicationContext.xml",
        "classpath*:/org/dataconservancy/ui/config/applicationContext.xml" })
public class ProjectPIDaoJdbcImplTest extends BaseModelTest {
    
    @Autowired
    @Qualifier("uiDataSource")
    private DataSource datasource;
    
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    private ProjectPIDAOJdbcImpl projectPIDao;
    
    private PersonDAO personDao;
    
    @Before
    public void setUp() {
        
        // Beans are wired up manually so they all receive the test dataSource or test jdbcTemplate
        // Right now, only the test DataSource/jdbcTemplate is seeded with created tables.
        personDao = new PersonDAOJbdcImpl(datasource);
        ProjectDAO projectDao = new ProjectDAOJdbcImpl(datasource);
        projectPIDao = new ProjectPIDAOJdbcImpl(datasource, projectDao, personDao);
        
        // Create seed business objects
        projectOne.removeAllPis();
        List<String> piIds = projectPIDao.getPIIds(projectOne.getId());
        for (String piID : piIds) {
            projectPIDao.deleteSingleProjectAdmin(projectOne.getId(), piID);
        }
        
        projectOne.addPi(admin.getId());
        projectPIDao.insertSingleProjectPI(projectOne.getId(), admin.getId());
        
        projectOne.addPi(user.getId());
        projectPIDao.insertSingleProjectPI(projectOne.getId(), user.getId());
        
        // Verify assumptions
        if (personDao.selectPerson().size() > 0) {
            StringBuilder persons = new StringBuilder("Person DAO was not empty: \n");
            for (Person p : personDao.selectPerson()) {
                persons.append(p).append("\n");
            }
            fail(persons.toString());
        }
        assertEquals(0, personDao.selectPerson().size());
        
        // Seed the persons
        if (personDao.selectPersonByEmailAddress(admin.getEmailAddress()) != null) {
            personDao.updatePerson(admin);
        }
        else {
            personDao.insertPerson(admin);
        }
        
        if (personDao.selectPersonByEmailAddress(user.getEmailAddress()) != null) {
            personDao.updatePerson(user);
        }
        else {
            personDao.insertPerson(user);
        }
        personDao.insertPerson(newUser);
        
    }
    
    @After
    public void tearDown() {
        
        // String query = "DELETE FROM " + PROJECT_ADMIN_TBL + " WHERE PROJECT_ID = ?";
        // jdbcTemplate.update(query, new Object[] { projectOne.getId() });
        
        String query = "DELETE FROM PROJECT_PI";
        jdbcTemplate.update(query);
        
        // Remove all the people
        List<Person> allPersons = personDao.selectPerson();
        for (Person p : allPersons) {
            personDao.deletePersonByEmail(p.getEmailAddress());
        }
        
        jdbcTemplate.execute("DELETE FROM PROJECT");
    }
    
    /**
     * <p>
     * Manually insert two project-admin records for the same project.
     * </p>
     * <p>
     * Test using the same project id to retrieve the inserted records
     * </p>
     */
    @Test
    public void selectTest() {
        List<String> piIds = projectPIDao.getPIIds(projectOne.getId());
        
        assertEquals(2, piIds.size());
        
        assertTrue(piIds.contains(admin.getId()));
        assertTrue(piIds.contains(user.getId()));
    }
    
    /**
     * <p>
     * Uses the dao to insert a single project-admin record
     * </p>
     */
    @Test
    public void insertSingleProjectAdminTest() {
        List<String> beforeList = projectPIDao.getPIIds(projectOne.getId());
        assertFalse(beforeList.contains(newUser.getEmailAddress()));
        projectPIDao.insertSingleProjectPI(projectOne.getId(), newUser.getEmailAddress());
        List<String> afterList = projectPIDao.getPIIds(projectOne.getId());
        assertTrue(afterList.contains(newUser.getEmailAddress()));
    }
    
    /**
     * <p>
     * Uses the dao to insert all of the project-admin relations from a given project into DB
     * </p>
     */
    @Test
    public void insertAllProjectAdminsTest() {
        Project projectTwo = new Project();
        List<String> adminsList = new ArrayList<String>();
        
        adminsList.add(admin.getId());
        
        adminsList.add(user.getId());
        
        projectTwo.setId("2");
        List<String> numbers = new ArrayList<String>();
        numbers.add("Award2");
        projectTwo.setNumbers(numbers);
        projectTwo.setName("AWARD2NAME");
        projectTwo.setDescription("THIS IS AWARD 2 DESCRIPTION");
        projectTwo.setPis(adminsList);
        
        // Verify assumptions
        assertTrue(projectPIDao.getPIIds(projectTwo.getId()).isEmpty());
        assertEquals(2, projectTwo.getPis().size());
        
        projectPIDao.insertAllProjectPIs(projectTwo);
        
        List<String> projectTwoPis = projectPIDao.getPIIds(projectTwo.getId());
        assertEquals(2, projectTwoPis.size());
        assertTrue(projectTwoPis.contains(admin.getId()));
        assertTrue(projectTwoPis.contains(user.getId()));
    }
    
    @Test
    public void deleteSingleProjectAdminTest() {
        // Verify assumptions
        assertTrue(projectOne.getPis().contains(admin.getId()));
        assertTrue(projectOne.getPis().contains(user.getId()));
        List<String> beforeList = projectPIDao.getPIIds(projectOne.getId());
        assertTrue(beforeList.contains(admin.getId()));
        assertTrue(beforeList.contains(user.getId()));
        
        projectPIDao.deleteSingleProjectAdmin(projectOne.getId(), admin.getId());
        List<String> afterList = projectPIDao.getPIIds(projectOne.getId());
        assertFalse(afterList.contains(admin.getId()));
        assertTrue(afterList.contains(user.getId()));
        
    }
}
