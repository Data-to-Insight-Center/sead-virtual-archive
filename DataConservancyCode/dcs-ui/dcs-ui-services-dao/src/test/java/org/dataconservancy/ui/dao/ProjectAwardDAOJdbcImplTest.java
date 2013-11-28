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

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

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
public class ProjectAwardDAOJdbcImplTest extends BaseModelTest {
    
    @Autowired
    @Qualifier("uiDataSource")
    private DataSource datasource;
    
    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    private ProjectAwardDAOJdbcImpl projectAwardDao;
    
    @Before
    public void setUp() {
        
        // Beans are wired up manually so they all receive the test dataSource or test jdbcTemplate
        // Right now, only the test DataSource/jdbcTemplate is seeded with created tables.
        ProjectDAO projectDao = new ProjectDAOJdbcImpl(datasource);
        projectAwardDao = new ProjectAwardDAOJdbcImpl(datasource, projectDao);
        
        // Create seed business objects
        projectOne.removeAllNumbers();
        List<String> numbers = projectAwardDao.getNumbers(projectOne.getId());
        for (String number : numbers) {
            projectAwardDao.deleteSingleProjectNumber(projectOne.getId(), number);
        }
        
        projectOne.addNumber(admin.getId());
        projectAwardDao.insertSingleProjectNumber(projectOne.getId(), admin.getId());
        
        projectOne.addNumber(user.getId());
        projectAwardDao.insertSingleProjectNumber(projectOne.getId(), user.getId());
        
    }
    
    @After
    public void tearDown() {
        
        String query = "DELETE FROM PROJECT_AWARD";
        jdbcTemplate.update(query);
        
        // Remove all the numbers
        List<String> numbers = projectAwardDao.getNumbers(projectOne.getId());
        for (String number : numbers) {
            projectAwardDao.deleteSingleProjectNumber(projectOne.getId(), number);
        }
        
        jdbcTemplate.execute("DELETE FROM PROJECT");
    }
    
    /**
     * <p>
     * Manually insert two project numbers to the same project.
     * </p>
     * <p>
     * Test using the same project id to retrieve the inserted records
     * </p>
     */
    @Test
    public void selectTest() {
        List<String> numbers = projectAwardDao.getNumbers(projectOne.getId());
        
        assertEquals(2, numbers.size());
        
        assertTrue(numbers.contains(admin.getId()));
        assertTrue(numbers.contains(user.getId()));
    }
    
    /**
     * <p>
     * Uses the dao to insert a single project number record
     * </p>
     */
    @Test
    public void insertSingleProjectNumberTest() {
        List<String> beforeList = projectAwardDao.getNumbers(projectOne.getId());
        assertFalse(beforeList.contains(newUser.getId()));
        projectAwardDao.insertSingleProjectNumber(projectOne.getId(), newUser.getId());
        List<String> afterList = projectAwardDao.getNumbers(projectOne.getId());
        assertTrue(afterList.contains(newUser.getId()));
    }
    
    /**
     * <p>
     * Uses the dao to insert all of the project numbers from a given project into DB
     * </p>
     */
    @Test
    public void insertAllProjectNumbersTest() {
        Project projectTwo = new Project();
        List<String> numbers = new ArrayList<String>();
        numbers.add(admin.getId());
        numbers.add(user.getId());
        projectTwo.setId("2");
        projectTwo.setNumbers(numbers);
        projectTwo.setName("AWARD2NAME");
        projectTwo.setDescription("THIS IS AWARD 2 DESCRIPTION");
        
        // Verify assumptions
        assertTrue(projectAwardDao.getNumbers(projectTwo.getId()).isEmpty());
        assertEquals(2, projectTwo.getNumbers().size());
        
        projectAwardDao.insertAllProjectNumbers(projectTwo);
        List<String> projectTwoNumbers = projectAwardDao.getNumbers(projectTwo.getId());
        
        assertEquals(2, projectTwoNumbers.size());
        assertTrue(projectTwoNumbers.contains(admin.getId()));
        assertTrue(projectTwoNumbers.contains(user.getId()));
    }
    
    /**
     * <p>
     * Uses the dao to delete a single project number from a given project.
     * </p>
     */
    @Test
    public void deleteSingleProjectNumberTest() {
        // Verify assumptions
        assertTrue(projectOne.getNumbers().contains(admin.getId()));
        assertTrue(projectOne.getNumbers().contains(user.getId()));
        List<String> beforeList = projectAwardDao.getNumbers(projectOne.getId());
        assertTrue(beforeList.contains(admin.getId()));
        assertTrue(beforeList.contains(user.getId()));
        
        projectAwardDao.deleteSingleProjectNumber(projectOne.getId(), admin.getId());
        List<String> afterList = projectAwardDao.getNumbers(projectOne.getId());
        assertFalse(afterList.contains(admin.getId()));
        assertTrue(afterList.contains(user.getId()));
        
    }
}
