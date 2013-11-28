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

package org.dataconservancy.ui.dao;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.test.support.model.BaseModelTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
public class ProjectDAOJdbcImplTest
        extends BaseModelTest {

    @Autowired
    private ProjectDAOJdbcImpl projectDao;

    private JdbcTemplate jdbcTemplate;

    private static final String PROJECT_TBL = "PROJECT";

    private static final String DELETE_ALL_ROWS_QUERY = "DELETE FROM "
            + PROJECT_TBL;

    private static final String COUNT_ALL_ROWS_QUERY = "SELECT count(*) FROM "
            + PROJECT_TBL;

    @Before
    public void setUp() {
        jdbcTemplate = projectDao.getJdbcTemplate();
        jdbcTemplate.execute(DELETE_ALL_ROWS_QUERY);
        assertEquals(0, jdbcTemplate.queryForInt(COUNT_ALL_ROWS_QUERY));
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute(DELETE_ALL_ROWS_QUERY);
    }

    @Test
    public void testInsertSelect() {
        Project createdProject = projectDao.insertProject(projectOne);
        Project retrievedProject =
                projectDao.selectProject(createdProject.getId());
        assertEquals(projectOne, retrievedProject);
    }

    @Test
    public void testInsert() {

        Project retrievedprProject = projectDao.insertProject(projectTwo);
        assertEquals(projectTwo, retrievedprProject);
    }

    @Test
    public void testUpdate() {
        // Test assumptions
        projectDao.insertProject(projectOne);
        String newName = "Award number two";
        projectOne.setName(newName);
        projectOne.addPi(user.getId());
        projectDao.updateProject(projectOne);
        Project retrievedProject = projectDao.selectProject(projectOne.getId());
        assertEquals(newName, retrievedProject.getName());
        assertEquals(projectOne, retrievedProject);
    }

    @Test
    public void testUpdateNonexistingProject() {
        try {
            projectDao.updateProject(projectTwo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDelete() {
        projectDao.deleteProject(projectOne.getId());
        Project retrievedProject = projectDao.selectProject(projectOne.getId());
        assertNull(retrievedProject);
    }

    @Test
    public void testGetProjectList() {
        List<Project> projectList = projectDao.getProjectList();
        assertNotNull("Expected getProjectList to return a non-null value",
                      projectList);
        assertEquals("Expected projectList to have zero entries",
                     0,
                     projectList.size());

        projectDao.insertProject(projectOne);
        projectList = projectDao.getProjectList();
        assertNotNull("Expected getProjectList to return a non-null value");
        assertEquals("Expected project list to have one element",
                     1,
                     projectList.size());
        assertTrue("Expected project list to contain projectOne",
                   projectList.contains(projectOne));
    }

}
