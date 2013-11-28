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
package org.dataconservancy.ui.services;

import org.dataconservancy.ui.dao.RelationshipDAO;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Relationship;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests that demonstrate that transaction handling is configured and working properly for the RelationshipService.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesDatabase
public class RelationshipServiceTransactionsTest extends BaseUnitTest{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Injected RelationshipService under test.  Must come from Spring, because the underlying impl must be
     * wrapped by a proxy which handles transactions.
     */
    @Autowired
    private RelationshipService underTest;

    /**
     * The underlying RelationshipDAO which will be partially mocked to throw exceptions.
     */
    @Autowired
    private RelationshipDAO relDao;

    /**
     * Add an administrator to a project, which involves inserting two rows into the relationship table.  This test
     * mocks the RelationshipDAO, and the mock throws an exception when the second row is inserted into the table.
     * The first row should be rolled back as a result.
     */
    @Test
    public void testAddAdministratorToProjectWithTransactionException() {

        // Prepare collaborating objects
        final String projectId = String.valueOf(1);
        final String adminId = "adminId";
        final Project project = new Project();
        final Person admin = new Person();
        project.setId(projectId);
        admin.setId(adminId);

        // Verify assumptions
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(adminId).isEmpty());

        // Prepare mock object to throw a runtime exception when adding the second relationship
        RelationshipDAO mock = spy(relDao);

        // N.B.: we need to be specific with our matchers to the addRelation(...) stubs.
        doCallRealMethod()
                .when(mock)
                .addRelation(adminId, projectId, Relationship.RelType.IS_ADMINISTRATOR_FOR);

        doThrow(new RuntimeException())
                .when(mock)
                .addRelation(projectId, adminId, Relationship.RelType.IS_ADMINISTERED_BY);


        // Test object which uses the mock.
        underTest.setRelationshipDao(mock);

        // Add an admin to the project
        try {
            underTest.addAdministratorToProject(project, admin);
            fail("RuntimeException expected.");
        } catch (RuntimeException e) {
            // expected
        }

        // The transaction should be rolled back, no objects in the relationships table.
        assertEquals(0, relDao.getRelations(projectId).size());
        assertEquals(0, relDao.getRelations(adminId).size());

        verify(mock).addRelation(adminId, projectId, Relationship.RelType.IS_ADMINISTRATOR_FOR);
        verify(mock).addRelation(projectId, adminId, Relationship.RelType.IS_ADMINISTERED_BY);

    }

    @Test
    public void testInterleavedAddAndRemove() throws InterruptedException {

        // Prepare collaborating objects
        final String projectId = String.valueOf(1);
        final String adminId = "adminId";
        final Project project = new Project();
        final Person admin = new Person();
        project.setId(projectId);
        admin.setId(adminId);

        // Verify assumptions
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(adminId).isEmpty());

        // Prepare mock object
        RelationshipDAO mock = spy(relDao);

        // N.B.: we need to be specific with our matchers to the addRelation(...) stubs.
        doCallRealMethod()
                .when(mock)
                .addRelation(adminId, projectId, Relationship.RelType.IS_ADMINISTRATOR_FOR);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(10000); // sleep for 10 seconds and then assert that the first relationship is there
                assertEquals(1, relDao.getRelations(adminId, Relationship.RelType.IS_ADMINISTRATOR_FOR).size());
                return null;
            }
        }).when(mock)
          .addRelation(projectId, adminId, Relationship.RelType.IS_ADMINISTERED_BY);


        // Test object which uses the mock.
        underTest.setRelationshipDao(mock);

        /*
         * Thread 1: Add an admin to the project, which is mocked to sleep for 10 seconds
         * after adding (and asserting the existence of) the obverse relationship (the inverse is never added).
         * In the mean time (between adding the obverse and inverse relationships), thread 2 will attempt to
         * remove the relationship.
         */
        final Thread slow = new Thread(new Runnable() {
            @Override
            public void run() {
                underTest.addAdministratorToProject(project, admin);
            }
        }, "slow");

        /*
         * Thread 2: Remove the admin from the project, running at full speed.
         */
        final Thread normal = new Thread(new Runnable() {
            @Override
            public void run() {
                underTest.removeAdministratorFromProject(admin, project);
            }
        }, "normal");

        // Start threads
        slow.start();
        Thread.sleep(1000); // Sleep 1 second to allow the slow thread to start first
        normal.start();

        // Wait for them to complete
        try {
            slow.join();
            normal.join();
        } catch (InterruptedException e) {
            // ignore
        }
        
        // The interleaved calls should be executed in order, so the relations table should be empty
        assertEquals(0, relDao.getRelations(projectId).size());
        assertEquals(0, relDao.getRelations(adminId).size());

        // Verify our mocks were called
        verify(mock).addRelation(adminId, projectId, Relationship.RelType.IS_ADMINISTRATOR_FOR);
        verify(mock).addRelation(projectId, adminId, Relationship.RelType.IS_ADMINISTERED_BY);
    }

    @Test
    @Ignore("This test doesn't really test anything, because nothing is ever added to the database.")
    public void testAddAdminstratorsToProjectWithTransactionException() throws Exception {

        // Prepare collaborating objects
        final String projectId = String.valueOf(1);
        final Project project = new Project();
        project.setId(projectId);

        final String adminOneId = "adminOne@foo.bar.com";
        final Person adminOne = new Person();
        adminOne.setEmailAddress(adminOneId);

        final String adminTwoId = "adminTwo@foo.bar.com";
        final Person adminTwo = new Person();
        adminTwo.setEmailAddress(adminTwoId);

        final Set<Person> projectAdmins = new HashSet<Person>();
        projectAdmins.add(adminOne);
        projectAdmins.add(adminTwo);

        // Verify assumptions
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(adminOneId).isEmpty());
        assertTrue(relDao.getRelations(adminTwoId).isEmpty());

        // Prepare mock object to throw a runtime exception when adding a set of admins
        RelationshipDAO mock = spy(relDao);
        
        doThrow(new RuntimeException())
                .when(mock)
                .addRelations(Matchers.<Set<Relationship>>any());

        // Test object which uses the mock.
        underTest.setRelationshipDao(mock);

        // Add a Set of administrators to the project
        try {

            underTest.addAdministratorsToProject(project, projectAdmins);
            fail("RuntimeException expected.");
        } catch (RuntimeException e) {
            // expected
        }

        // The transaction should be rolled back, no objects in the relationships table.
        assertEquals(0, relDao.getRelations(projectId).size());
        assertEquals(0, relDao.getRelations(adminOneId).size());
        assertEquals(0, relDao.getRelations(adminTwoId).size());

        verify(mock).addRelations(Matchers.<Set<Relationship>>any());

    }
}
