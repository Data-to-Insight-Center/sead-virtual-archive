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

import static org.dataconservancy.ui.model.Relationship.RelType.AGGREGATES;
import static org.dataconservancy.ui.model.Relationship.RelType.HAS_METADATA_FILE;
import static org.dataconservancy.ui.model.Relationship.RelType.HAS_SUBCOLLECTION;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_ADMINISTERED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_ADMINISTRATOR_FOR;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_AGGREGATED_BY;
import static org.dataconservancy.ui.model.Relationship.RelType.IS_SUBCOLLECTION_OF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.dao.PersonDAO;
import org.dataconservancy.ui.dao.ProjectAwardDAO;
import org.dataconservancy.ui.dao.ProjectDAO;
import org.dataconservancy.ui.dao.RelationshipDAO;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Discipline;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Relationship;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 */
@DirtiesDatabase(DirtiesDatabase.AFTER_EACH_TEST_METHOD)
public class RelationshipServiceImplTest extends BaseUnitTest {
    
    @Autowired
    private RelationshipDAO relDao;
    
    @Autowired
    private JdbcTemplate template;
    
    @Autowired
    @Qualifier("personDao")
    private PersonDAO personDao;
    
    @Autowired
    private ProjectDAO projectDao;
    
    @Autowired
    private ProjectAwardDAO projectAwardDao;
    
    @Autowired
    private ArchiveService archiveService;
    
    private RelationshipServiceImpl underTest;
    
    // FIXME: Fix this junk
    
    static final String REL_TABLE = "relationships";
    
    static final String PROJECT_TABLE = "project";
    
    static final String PERSON_TABLE = "person";
    
    static final String REL_TABLE_ROW_COUNT_QUERY = "SELECT count(*) FROM " + REL_TABLE;
    
    static final String REL_TABLE_DELETE_ALL_ROWS_QUERY = "DELETE FROM " + REL_TABLE;
    
    static final String PROJ_TABLE_ROW_COUNT_QUERY = "SELECT count(*) FROM " + PROJECT_TABLE;
    
    static final String PROJ_TABLE_DELETE_ALL_ROWS_QUERY = "DELETE FROM " + PROJECT_TABLE;
    
    static final String PERSON_TABLE_ROW_COUNT_QUERY = "SELECT count(*) FROM " + PERSON_TABLE;
    
    static final String PERSON_TABLE_DELETE_ALL_ROWS_QUERY = "DELETE FROM " + PERSON_TABLE;
    
    @Before
    public void setUp() {
        template.execute(REL_TABLE_DELETE_ALL_ROWS_QUERY);
        if (template.queryForInt(REL_TABLE_ROW_COUNT_QUERY) > 0) {
            template.execute(REL_TABLE_DELETE_ALL_ROWS_QUERY);
            // fail(REL_TABLE + " table not empty: \n" + DaoSupport.dumpTable(template, REL_TABLE).toString());
        }
        
        if (template.queryForInt(PROJ_TABLE_ROW_COUNT_QUERY) > 0) {
            // fail(PROJECT_TABLE + " table not empty: \n" + DaoSupport.dumpTable(template, PROJECT_TABLE).toString());
            template.execute(PROJ_TABLE_DELETE_ALL_ROWS_QUERY);
        }
        
        if (template.queryForInt(PERSON_TABLE_ROW_COUNT_QUERY) > 0) {
            // fail(PERSON_TABLE + " table not empty: \n" + DaoSupport.dumpTable(template, PERSON_TABLE).toString());
            template.execute(PERSON_TABLE_DELETE_ALL_ROWS_QUERY);
        }
        
        underTest = new RelationshipServiceImpl(relDao, personDao, projectDao, projectAwardDao, archiveService);
    }
    
    @After
    public void tearDown() {
        template.execute(REL_TABLE_DELETE_ALL_ROWS_QUERY);
        template.execute(PROJ_TABLE_DELETE_ALL_ROWS_QUERY);
        template.execute(PERSON_TABLE_DELETE_ALL_ROWS_QUERY);
    }
    
    @Test
    public void testAddAdministratorToProject() throws Exception {
        
        // Prepare objects
        final String projectId = "1";
        final String adminId = "adminId";
        final Project project = new Project();
        final Person admin = new Person();
        project.setId(projectId);
        admin.setId(adminId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(adminId).isEmpty());
        
        // Expected relationships to be added
        final Relationship expectedObverse = new Relationship(adminId, projectId, IS_ADMINISTRATOR_FOR);
        final Relationship expectedInverse = new Relationship(projectId, adminId, IS_ADMINISTERED_BY);
        
        // Add an admin to the project
        underTest.addAdministratorToProject(project, admin);
        
        // Verify expected relationships were added
        final Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(adminId);
        assertEquals(2, addedRels.size());
        
        // Verify that an IS_ADMINISTRATOR_FOR relationship exists between 'adminId' and 'projectId'
        assertEquals(1, addedRels.get(IS_ADMINISTRATOR_FOR).size());
        assertEquals(expectedObverse, addedRels.get(IS_ADMINISTRATOR_FOR).iterator().next());
        
        // Verify that an IS_ADMINISTERED_BY relationship exists between 'projectId' and 'adminId'
        assertEquals(1, addedRels.get(IS_ADMINISTERED_BY).size());
        assertEquals(expectedInverse, addedRels.get(IS_ADMINISTERED_BY).iterator().next());
        
    }
    
    @Test
    public void testAddAdministratorsToProject() throws Exception {
        
        // Prepare objects
        final String projectId = "1";
        final Project project = new Project();
        project.setId(projectId);
        
        final String adminOneId = "adminone@foo.com";
        final Person adminOne = new Person();
        adminOne.setId(adminOneId);
        
        final String adminTwoId = "admintwo@foo.com";
        final Person adminTwo = new Person();
        adminTwo.setId(adminTwoId);
        adminTwo.setFirstNames("Foo");
        
        // Verify assumptions
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(adminOneId).isEmpty());
        assertTrue(relDao.getRelations(adminTwoId).isEmpty());
        
        // Expected relationships to be added
        final Relationship expectedObverseOne = new Relationship(adminOneId, projectId, IS_ADMINISTRATOR_FOR);
        final Relationship expectedInverseOne = new Relationship(projectId, adminOneId, IS_ADMINISTERED_BY);
        final Relationship expectedObverseTwo = new Relationship(adminTwoId, projectId, IS_ADMINISTRATOR_FOR);
        final Relationship expectedInverseTwo = new Relationship(projectId, adminTwoId, IS_ADMINISTERED_BY);
        
        // Add the admins
        Set<Person> adminSet = new HashSet<Person>();
        adminSet.add(adminOne);
        adminSet.add(adminTwo);
        
        underTest.addAdministratorsToProject(project, adminSet);
        
        // Verify expected relationships were added - Admin One
        Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(adminOneId);
        assertEquals(2, addedRels.size());
        
        // Verify that an IS_ADMINISTRATOR_FOR relationship exists between 'adminOneId' and 'projectId'
        assertEquals(1, addedRels.get(IS_ADMINISTRATOR_FOR).size());
        assertEquals(expectedObverseOne, addedRels.get(IS_ADMINISTRATOR_FOR).iterator().next());
        
        // Verify that an IS_ADMINISTERED_BY relationship exists between 'projectId' and 'adminOneId'
        assertEquals(1, addedRels.get(IS_ADMINISTERED_BY).size());
        assertEquals(expectedInverseOne, addedRels.get(IS_ADMINISTERED_BY).iterator().next());
        
        // Verify expected relationships were added - Admin Two
        addedRels = relDao.getRelations(adminTwoId);
        assertEquals(2, addedRels.size());
        
        // Verify that an IS_ADMINISTRATOR_FOR relationship exists between 'adminTwoId' and 'projectId'
        assertEquals(1, addedRels.get(IS_ADMINISTRATOR_FOR).size());
        assertEquals(expectedObverseTwo, addedRels.get(IS_ADMINISTRATOR_FOR).iterator().next());
        
        // Verify that an IS_ADMINISTERED_BY relationship exists between 'projectId' and 'adminTwoId'
        assertEquals(1, addedRels.get(IS_ADMINISTERED_BY).size());
        assertEquals(expectedInverseTwo, addedRels.get(IS_ADMINISTERED_BY).iterator().next());
        
    }
    
    @Test
    public void testRemoveAdministratorFromProject() throws Exception {
        Project project = new Project();
        String projectId = "1";
        project.setId(projectId);
        Person admin = new Person();
        final String personId = "admin";
        admin.setId(personId);
        relDao.addRelation(personId, projectId, IS_ADMINISTRATOR_FOR);
        relDao.addRelation(projectId, personId, IS_ADMINISTERED_BY);
        
        assertEquals(2, relDao.getRelations(projectId).size());
        assertEquals(2, relDao.getRelations(personId).size());
        
        underTest.removeAdministratorFromProject(admin, project);
        
        assertEquals(0, relDao.getRelations(projectId).size());
        assertEquals(0, relDao.getRelations(personId).size());
    }
    
    @Test
    public void testRemoveAdministratorsFromProject() throws Exception {
        Project project = new Project();
        String projectId = "1";
        project.setId(projectId);
        
        Person adminOne = new Person();
        final String adminOneId = "id:adminOne";
        adminOne.setId(adminOneId);
        // adminOne.setEmailAddress(adminOneId);
        
        Person adminTwo = new Person();
        final String adminTwoId = "id:adminTwo";
        adminTwo.setId(adminTwoId);
        adminTwo.setEmailAddress(adminTwoId);
        Set<Person> admins = new HashSet<Person>();
        admins.add(adminOne);
        admins.add(adminTwo);
        
        for (Person p : admins) {
            relDao.addRelation(p.getId(), projectId, IS_ADMINISTRATOR_FOR);
            relDao.addRelation(projectId, p.getId(), IS_ADMINISTERED_BY);
        }
        
        assertEquals(2, relDao.getRelations(projectId, IS_ADMINISTRATOR_FOR).size());
        assertEquals(2, relDao.getRelations(projectId, IS_ADMINISTERED_BY).size());
        assertEquals(2, relDao.getRelations(adminOneId).values().size());
        assertEquals(2, relDao.getRelations(adminTwoId).values().size());
        
        underTest.removeAdministratorsFromProject(project, admins);
        
        assertEquals(0, relDao.getRelations(projectId).size());
        assertEquals(0, relDao.getRelations(adminOneId).size());
        assertEquals(0, relDao.getRelations(adminOneId).size());
    }
    
    @Test
    public void testGetAdministratorsForProject() throws Exception {
        Project project = new Project();
        String projectId = "1";
        project.setId(projectId);
        
        Person adminOne = new Person();
        final String adminOneId = "id:adminOne";
        adminOne.setId(adminOneId);
        adminOne.setEmailAddress(adminOneId);
        adminOne.setRegistrationStatus(RegistrationStatus.APPROVED);
        Person adminTwo = new Person();
        final String adminTwoId = "id:adminTwo";
        adminTwo.setId(adminTwoId);
        adminTwo.setRegistrationStatus(RegistrationStatus.APPROVED);
        Set<Person> expectedAdmins = new HashSet<Person>();
        expectedAdmins.add(adminOne);
        expectedAdmins.add(adminTwo);
        
        for (Person p : expectedAdmins) {
            relDao.addRelation(p.getId(), projectId, IS_ADMINISTRATOR_FOR);
            relDao.addRelation(projectId, p.getId(), IS_ADMINISTERED_BY);
            personDao.insertPerson(p);
            assertNotNull(personDao.selectPersonById(p.getId()));
        }
        
        assertEquals(2, relDao.getRelations(projectId, IS_ADMINISTRATOR_FOR).size());
        assertEquals(2, relDao.getRelations(projectId, IS_ADMINISTERED_BY).size());
        assertEquals(2, relDao.getRelations(adminOneId).values().size());
        assertEquals(2, relDao.getRelations(adminTwoId).values().size());
        
        final Set<Person> actualAdmins = underTest.getAdministratorsForProject(project);
        
        assertEquals(expectedAdmins.size(), actualAdmins.size());
    }
    
    @Test
    public void testGetAdministratorsForProjectNotNull() throws Exception {
        // create Project, no relations in rel table
        final Project project = new Project();
        final String projectId = "id://ProjectNotNull";
        project.setId(projectId);
        
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertNotNull(underTest.getAdministratorsForProject(project));
    }
    
    @Test
    public void testGetProjectsForAdministrator() throws Exception {
        Project projectOne = new Project();
        String projectOneId = "1111";
        projectOne.setId(projectOneId);
        projectOne.setStartDate(DateTime.now());
        projectOne.setEndDate(DateTime.now());
        
        Project projectTwo = new Project();
        String projectTwoId = "2222";
        projectTwo.setId(projectTwoId);
        projectTwo.setStartDate(DateTime.now());
        projectTwo.setEndDate(DateTime.now());
        
        final Set<Project> projects = new HashSet<Project>();
        projects.add(projectOne);
        projects.add(projectTwo);
        
        Person admin = new Person();
        final String adminOneId = "admin";
        admin.setId(adminOneId);
        
        for (Project p : projects) {
            relDao.addRelation(adminOneId, p.getId(), IS_ADMINISTRATOR_FOR);
            relDao.addRelation(p.getId(), adminOneId, IS_ADMINISTERED_BY);
            projectDao.insertProject(p);
            assertNotNull(projectDao.selectProject(p.getId()));
            assertEquals(p, projectDao.selectProject(p.getId()));
        }
        
        assertEquals(1, relDao.getRelations(projectOneId, IS_ADMINISTRATOR_FOR).size());
        assertEquals(1, relDao.getRelations(projectOneId, IS_ADMINISTERED_BY).size());
        assertEquals(1, relDao.getRelations(projectTwoId, IS_ADMINISTRATOR_FOR).size());
        assertEquals(1, relDao.getRelations(projectTwoId, IS_ADMINISTERED_BY).size());
        assertEquals(2, relDao.getRelations(adminOneId).values().size());
        
        final Set<Project> actualProjects = underTest.getProjectsForAdministrator(admin);
        
        assertEquals(projects, actualProjects);
    }
    
    @Test
    public void testGetProjectsForAdministratorNotNull() throws Exception {
        // create Administrator, no relations in rel table
        final Person admin = new Person();
        final String personId = "id://AdminNotNull";
        admin.setId(personId);
        admin.setEmailAddress("admin@not.null");
        
        assertTrue(relDao.getRelations(personId).isEmpty());
        assertNotNull(underTest.getProjectsForAdministrator(admin));
    }
    
    @Test
    public void testGetProjectForCollection() throws RelationshipConstraintException {
        Project projectOne = new Project();
        String projectOneId = "3";
        projectOne.setId(projectOneId);
        projectOne.setStartDate(DateTime.now());
        projectOne.setEndDate(DateTime.now());
        
        String collId = "4";
        Collection collection = mock(Collection.class);
        when(collection.getId()).thenReturn(collId);
        
        relDao.addRelation(projectOne.getId(), collection.getId(), AGGREGATES);
        relDao.addRelation(collection.getId(), projectOne.getId(), IS_AGGREGATED_BY);
        projectDao.insertProject(projectOne);
        assertNotNull(projectDao.selectProject(projectOne.getId()));
        assertEquals(projectOne, projectDao.selectProject(projectOne.getId()));
        assertEquals(2, relDao.getRelations(collection.getId()).size());
        assertEquals(2, relDao.getRelations(projectOne.getId()).size());
        
        Project project = underTest.getProjectForCollection(collection);
        assertEquals(project, projectOne);
    }
    
    @Test
    public void testGetProjectForCollectionNotNull() throws RelationshipConstraintException {
        // create Collection, no relations in rel table
        final Collection collection = new Collection();
        final String collectionId = "id://CollectionNotNull";
        collection.setId(collectionId);
        
        assertTrue(relDao.getRelations(collectionId).isEmpty());
        assertNotNull(underTest.getProjectForCollection(collection));
    }
    
    @Test
    public void testAddDepositorToCollection() throws Exception {
        String personId = "foo";
        Person depositor = new Person();
        depositor.setId(personId);
        
        String collId = "2";
        Collection collection = mock(Collection.class);
        when(collection.getId()).thenReturn(collId);
        
        assertEquals(0, relDao.getRelations(collId).size());
        assertEquals(0, relDao.getRelations(personId).size());
        
        underTest.addDepositorToCollection(depositor, collection);
        
        assertEquals(2, relDao.getRelations(collId).size());
        assertEquals(2, relDao.getRelations(personId).size());
    }
    
    @Test
    public void testRemoveDepositorFromCollection() throws Exception {
        String personId = "foo";
        Person depositor = new Person();
        depositor.setId(personId);
        
        String collId = "2";
        Collection collection = mock(Collection.class);
        when(collection.getId()).thenReturn(collId);
        
        relDao.addRelation(personId, collId, Relationship.RelType.IS_DEPOSITOR_FOR);
        relDao.addRelation(collId, personId, Relationship.RelType.ACCEPTS_DEPOSIT);
        
        assertEquals(2, relDao.getRelations(collId).size());
        assertEquals(2, relDao.getRelations(personId).size());
        
        underTest.removeDepositorFromCollection(depositor, collection);
        
        assertEquals(0, relDao.getRelations(collId).size());
        assertEquals(0, relDao.getRelations(personId).size());
    }
    
    @Test
    public void testGetDepositorsForCollection() throws Exception {
        String personOneId = "id:foo";
        Person depositorOne = new Person();
        depositorOne.setId(personOneId);
        depositorOne.setEmailAddress(personOneId);
        depositorOne.setRegistrationStatus(RegistrationStatus.APPROVED);
        
        String personTwoId = "id:Bar";
        Person depositorTwo = new Person();
        depositorTwo.setId(personTwoId);
        depositorTwo.setEmailAddress(personTwoId);
        depositorTwo.setRegistrationStatus(RegistrationStatus.APPROVED);
        
        Set<Person> expectedDepositors = new HashSet<Person>();
        expectedDepositors.add(depositorOne);
        expectedDepositors.add(depositorTwo);
        
        String collId = "2";
        Collection collection = mock(Collection.class);
        when(collection.getId()).thenReturn(collId);
        
        for (Person depositor : expectedDepositors) {
            personDao.insertPerson(depositor);
            relDao.addRelation(depositor.getId(), collId, Relationship.RelType.IS_DEPOSITOR_FOR);
            relDao.addRelation(collId, depositor.getId(), Relationship.RelType.ACCEPTS_DEPOSIT);
        }
        
        assertEquals(2, relDao.getRelations(collId, Relationship.RelType.IS_DEPOSITOR_FOR).size());
        assertEquals(2, relDao.getRelations(collId, Relationship.RelType.ACCEPTS_DEPOSIT).size());
        assertEquals(2, relDao.getRelations(personOneId).size());
        assertEquals(2, relDao.getRelations(personTwoId).size());
        
        Set<Person> actualDepositors = underTest.getDepositorsForCollection(collection);
        
        assertEquals(expectedDepositors.size(), actualDepositors.size());
    }
    
    @Test
    public void testGetDataSetIsForCollectionId() throws Exception {
        String datasetid1 = "id:dataset1";
        String datasetid2 = "id:dataset2";
        String datasetid3 = "id:dataset3";
        
        Set<String> expectedDataSetIds = new HashSet<String>();
        expectedDataSetIds.add(datasetid1);
        expectedDataSetIds.add(datasetid2);
        expectedDataSetIds.add(datasetid3);
        
        String collId = "2";
        
        for (String datasetId : expectedDataSetIds) {
            relDao.addRelation(datasetId, collId, Relationship.RelType.IS_AGGREGATED_BY);
            relDao.addRelation(collId, datasetId, Relationship.RelType.AGGREGATES);
        }
        
        assertEquals(3, relDao.getRelations(collId, Relationship.RelType.AGGREGATES).size());
        assertEquals(3, relDao.getRelations(collId, Relationship.RelType.IS_AGGREGATED_BY).size());
        assertEquals(2, relDao.getRelations(datasetid1).size());
        assertEquals(2, relDao.getRelations(datasetid2).size());
        assertEquals(2, relDao.getRelations(datasetid3).size());
        
        Set<String> actualDataSetIds = underTest.getDataSetIdsForCollectionId(collId);
        
        assertEquals(expectedDataSetIds, actualDataSetIds);
    }
    
    @Test
    public void testGetSubCollectionIdsForCollectionId() throws Exception {
        String parent = "id:c1";
        String kid1 = "id:c2";
        String kid2 = "id:c3";
        
        Set<String> kids = new HashSet<String>();
        kids.add(kid1);
        kids.add(kid2);

        for (String id: kids) {
            relDao.addRelation(parent, id, Relationship.RelType.HAS_SUBCOLLECTION);
            relDao.addRelation(id, parent, Relationship.RelType.IS_SUBCOLLECTION_OF);
        }
        
        assertEquals(2, relDao.getRelations(parent, Relationship.RelType.HAS_SUBCOLLECTION).size());
        assertEquals(2, relDao.getRelations(parent, Relationship.RelType.IS_SUBCOLLECTION_OF).size());                
        assertEquals(2, relDao.getRelations(kid1).size());
        assertEquals(2, relDao.getRelations(kid2).size());
        
        Set<String> result = underTest.getSubCollectionIdsForCollectionId(parent);
        
        assertEquals(kids, result);
    }
    
    @Test
    public void testGetSuperCollectionIdsForCollectionId() throws Exception {
        String parent = "id:c1";
        String kid1 = "id:c2";
        String kid2 = "id:c3";
        
        Set<String> kids = new HashSet<String>();
        kids.add(kid1);
        kids.add(kid2);
        
        Set<String> parents = new HashSet<String>();
        parents.add(parent);

        for (String id: kids) {
            relDao.addRelation(parent, id, Relationship.RelType.HAS_SUBCOLLECTION);
            relDao.addRelation(id, parent, Relationship.RelType.IS_SUBCOLLECTION_OF);
        }
        
        assertEquals(2, relDao.getRelations(parent, Relationship.RelType.HAS_SUBCOLLECTION).size());
        assertEquals(2, relDao.getRelations(parent, Relationship.RelType.IS_SUBCOLLECTION_OF).size());                
        assertEquals(2, relDao.getRelations(kid1).size());
        assertEquals(2, relDao.getRelations(kid2).size());
        
        Set<String> result = underTest.getSuperCollectionIdsForCollectionId(kid1);        
        assertEquals(parents, result);
        
        result = underTest.getSuperCollectionIdsForCollectionId(kid2);
        assertEquals(parents, result);
    }
    
    @Test
    public void testGetDepositorsForCollectionNotNull() throws Exception {
        // create Collection, no relations in rel table
        final Collection collection = new Collection();
        final String collectionId = "id://CollectionNotNull";
        collection.setId(collectionId);
        assertTrue(relDao.getRelations(collectionId).isEmpty());
        assertNotNull(underTest.getDepositorsForCollection(collection));
    }
    
    @Test
    public void testGetCollectionsForDepositor() throws Exception {
        // Seed the Collection in the archive
        Collection expectedCollection = new Collection();
        expectedCollection.setId("collection:/1");
        archiveService.deposit(expectedCollection);
        
        // Tell the archiveService to update the deposit status of the Collection
        archiveService.pollArchive();
        
        // The Depositor
        Person depositor = new Person();
        depositor.setId("foo@bar.baz");
        
        // Verify no relationships exist yet.
        assertEquals(0, underTest.getCollectionsForDepositor(depositor).size());
        
        // Manually create the relationships via the relationship dao
        relDao.addRelation(expectedCollection.getId(), depositor.getId(), Relationship.RelType.ACCEPTS_DEPOSIT);
        relDao.addRelation(depositor.getId(), expectedCollection.getId(), Relationship.RelType.IS_DEPOSITOR_FOR);
        
        // Verify we can get the Collection for a depositor
        final Set<Collection> collections = underTest.getCollectionsForDepositor(depositor);
        
        assertEquals(1, collections.size());
        assertEquals(expectedCollection, collections.iterator().next());
    }
    
    @Test
    public void testGetCollectionsForDepositorNotNull() throws Exception {
        // create Depositor, no relations in rel table
        final Person depositor = new Person();
        final String depositorId = "id://DepositorNotNull";
        depositor.setId(depositorId);
        depositor.setEmailAddress("depositor@not.null");
        
        assertTrue(relDao.getRelations(depositorId).isEmpty());
        assertNotNull(underTest.getCollectionsForDepositor(depositor));
    }
    
    @Test
    public void testGetCollectionsForAdministrator() throws Exception {
        // The Collections
        Collection collectionOne = new Collection();
        Collection collectionTwo = new Collection();
        collectionOne.setId("collection:/1");
        collectionTwo.setId("collection:/2");
        
        // The Administrator
        Person admin = new Person();
        admin.setId("foo@bar.baz");
        
        // The Project
        Project project = new Project();
        project.setId("project:/1");
        project.addPi(admin.getId());
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        
        // Seed collections in the archive
        archiveService.deposit(collectionOne);
        archiveService.deposit(collectionTwo);
        archiveService.pollArchive();
        
        // Seed the project in the project DAO
        projectDao.insertProject(project);
        
        // Verify no relationships exist
        assertEquals(0, underTest.getCollectionsForAdministrator(admin).size());
        
        // Manually create and seed the relationships using the relationship dao
        Set<Relationship> projectAdminRels = new HashSet<Relationship>();
        projectAdminRels.add(new Relationship(project.getId(), admin.getId(), IS_ADMINISTERED_BY));
        projectAdminRels.add(new Relationship(admin.getId(), project.getId(), IS_ADMINISTRATOR_FOR));
        
        Set<Relationship> collectionRels = new HashSet<Relationship>();
        collectionRels.add(new Relationship(collectionOne.getId(), project.getId(), IS_AGGREGATED_BY));
        collectionRels.add(new Relationship(project.getId(), collectionOne.getId(), AGGREGATES));
        collectionRels.add(new Relationship(collectionTwo.getId(), project.getId(), IS_AGGREGATED_BY));
        collectionRels.add(new Relationship(project.getId(), collectionTwo.getId(), AGGREGATES));
        
        relDao.addRelations(projectAdminRels);
        relDao.addRelations(collectionRels);
        
        // Verify we can get the Collections the Admin administers
        final Set<Collection> collections = underTest.getCollectionsForAdministrator(admin);
        assertEquals(2, collections.size());
        assertTrue(collections.contains(collectionOne));
        assertTrue(collections.contains(collectionTwo));
    }
    
    @Test
    public void testGetCollectionsForAdministratorNotNull() throws Exception {
        // create Admin, no relations in rel table
        final Person admin = new Person();
        final String adminId = "id://AdminNotNull";
        admin.setId(adminId);
        admin.setEmailAddress("admin@not.null");
        
        assertTrue(relDao.getRelations(adminId).isEmpty());
        assertNotNull(underTest.getCollectionsForAdministrator(admin));
    }
    
    @Test
    public void testGetMetadataFormatsForDiscipline() throws Exception {
        String disciplineOne = "monkey";
        String disciplineTwo = "cow";
        
        String metadataOne = "banana";
        String metadataTwo = "moo";
        String metadataThree = "beef";
        
        Set<Relationship> metadataRels = new HashSet<Relationship>();
        metadataRels.add(new Relationship(disciplineOne, metadataOne, AGGREGATES));
        metadataRels.add(new Relationship(disciplineTwo, metadataTwo, AGGREGATES));
        metadataRels.add(new Relationship(disciplineTwo, metadataThree, AGGREGATES));
        
        relDao.addRelations(metadataRels);
        
        Set<String> metadataSetOne = underTest.getMetadataFormatsForDiscipline(disciplineOne);
        assertEquals(1, metadataSetOne.size());
        assertTrue(metadataSetOne.contains(metadataOne));
        
        Set<String> metadataSetTwo = underTest.getMetadataFormatsForDiscipline(disciplineTwo);
        assertEquals(2, metadataSetTwo.size());
        assertTrue(metadataSetTwo.contains(metadataTwo));
        assertTrue(metadataSetTwo.contains(metadataThree));
        
    }
    
    @Test
    public void testDetMetadataFormatsForDisciplineNotNull() throws Exception {
        // create DisciplineId, no relations in rel table
        final String disciplineId = "id://DisciplineNotNull";
        
        assertTrue(relDao.getRelations(disciplineId).isEmpty());
        assertNotNull(underTest.getMetadataFormatsForDiscipline(disciplineId));
    }
    
    @Test
    public void testGetDisciplinesForMetadataFormats() throws Exception {
        String disciplineIdOne = "discipline:/1";
        String disciplineIdTwo = "discipline:/2";
        String metadataId = "uri:dublincore:1.0";
        
        final Set<String> expectedDisciplineIds = new HashSet<String>();
        expectedDisciplineIds.add(disciplineIdOne);
        expectedDisciplineIds.add(disciplineIdTwo);
        
        final Set<Relationship> rels = new HashSet<Relationship>();
        rels.add(new Relationship(disciplineIdOne, metadataId, AGGREGATES));
        rels.add(new Relationship(metadataId, disciplineIdOne, IS_AGGREGATED_BY));
        rels.add(new Relationship(disciplineIdTwo, metadataId, AGGREGATES));
        rels.add(new Relationship(metadataId, disciplineIdTwo, IS_AGGREGATED_BY));
        relDao.addRelations(rels);
        
        assertEquals(expectedDisciplineIds, underTest.getDisciplinesForMetadataFormats(metadataId));
    }
    
    @Test
    public void testAddDisciplineToMetadataFormat() throws Exception {
        String disciplineId = "discipline:/1";
        String metadataId = "uri:dublincore:1.0";
        
        Relationship expectedObverseRel = new Relationship(disciplineId, metadataId, AGGREGATES);
        Relationship expectedInverseRel = new Relationship(metadataId, disciplineId, IS_AGGREGATED_BY);
        
        Discipline discipline = new Discipline("Discipline One", disciplineId);
        DcsMetadataFormat mdf = new DcsMetadataFormat();
        mdf.setName("Metadata Format One");
        mdf.setId(metadataId);
        mdf.setVersion("1.0");
        
        underTest.addDisciplineToMetadataFormat(discipline, mdf);
        
        assertTrue(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRel));
        assertTrue(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRel));
    }
    
    @Test
    public void testAddDisciplineToMetadataFormats() throws Exception {
        String disciplineId = "discipline:/1";
        String metadataIdOne = "uri:dublincore:1.0";
        String metadataIdTwo = "uri:tei:1.0";
        
        Relationship expectedObverseRelOne = new Relationship(disciplineId, metadataIdOne, AGGREGATES);
        Relationship expectedInverseRelOne = new Relationship(metadataIdOne, disciplineId, IS_AGGREGATED_BY);
        Relationship expectedObverseRelTwo = new Relationship(disciplineId, metadataIdTwo, AGGREGATES);
        Relationship expectedInverseRelTwo = new Relationship(metadataIdTwo, disciplineId, IS_AGGREGATED_BY);
        
        Discipline discipline = new Discipline("Discipline One", disciplineId);
        DcsMetadataFormat mdfOne = new DcsMetadataFormat();
        mdfOne.setId(metadataIdOne);
        mdfOne.setName("Metadata Format One");
        mdfOne.setVersion("1.0");
        
        DcsMetadataFormat mdfTwo = new DcsMetadataFormat();
        mdfTwo.setId(metadataIdTwo);
        mdfTwo.setName("Metadata Format Two");
        mdfTwo.setVersion("1.0");
        
        Set<DcsMetadataFormat> mdfs = new HashSet<DcsMetadataFormat>();
        mdfs.add(mdfOne);
        mdfs.add(mdfTwo);
        
        underTest.addDisciplineToMetadataFormats(discipline, mdfs);
        
        assertTrue(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRelOne));
        assertTrue(relDao.getRelations(metadataIdOne, IS_AGGREGATED_BY).contains(expectedInverseRelOne));
        assertTrue(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRelTwo));
        assertTrue(relDao.getRelations(metadataIdTwo, IS_AGGREGATED_BY).contains(expectedInverseRelTwo));
    }
    
    @Test
    public void testAddMetadataFormatToDisciplines() throws Exception {
        String disciplineIdOne = "discipline:/1";
        String disciplineIdTwo = "discipline:/2";
        String metadataId = "uri:dublincore:1.0";
        
        Relationship expectedObverseRelOne = new Relationship(disciplineIdOne, metadataId, AGGREGATES);
        Relationship expectedInverseRelOne = new Relationship(metadataId, disciplineIdOne, IS_AGGREGATED_BY);
        Relationship expectedObverseRelTwo = new Relationship(disciplineIdTwo, metadataId, AGGREGATES);
        Relationship expectedInverseRelTwo = new Relationship(metadataId, disciplineIdTwo, IS_AGGREGATED_BY);
        
        Discipline disciplineOne = new Discipline("Discipline One", disciplineIdOne);
        Discipline disciplineTwo = new Discipline("Discipline Two", disciplineIdTwo);
        DcsMetadataFormat mdf = new DcsMetadataFormat();
        mdf.setName("Metadata Format One");
        mdf.setVersion("1.0");
        mdf.setId(metadataId);
        
        Set<Discipline> disciplines = new HashSet<Discipline>();
        disciplines.add(disciplineOne);
        disciplines.add(disciplineTwo);
        
        underTest.addMetadataFormatToDisciplines(mdf, disciplines);
        
        assertTrue(relDao.getRelations(disciplineIdOne, AGGREGATES).contains(expectedObverseRelOne));
        assertTrue(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRelOne));
        assertTrue(relDao.getRelations(disciplineIdTwo, AGGREGATES).contains(expectedObverseRelTwo));
        assertTrue(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRelTwo));
        
    }
    
    @Test
    public void testRemoveDisciplineFromMetadataFormat() throws Exception {
        String disciplineId = "discipline:/1";
        String metadataId = "uri:dublincore:1.0";
        
        Relationship expectedObverseRel = new Relationship(disciplineId, metadataId, AGGREGATES);
        Relationship expectedInverseRel = new Relationship(metadataId, disciplineId, IS_AGGREGATED_BY);
        Set<Relationship> expectedRelations = new HashSet<Relationship>();
        expectedRelations.add(expectedInverseRel);
        expectedRelations.add(expectedObverseRel);
        
        Discipline discipline = new Discipline("Discipline One", disciplineId);
        DcsMetadataFormat mdf = new DcsMetadataFormat();
        mdf.setId(metadataId);
        mdf.setName("Metadata Format One");
        mdf.setVersion("1.0");
        
        relDao.addRelations(expectedRelations);
        assertTrue(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRel));
        assertTrue(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRel));
        
        underTest.removeDisciplineFromMetadataFormat(discipline, mdf);
        
        assertFalse(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRel));
        assertFalse(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRel));
    }
    
    @Test
    public void testRemoveDisciplinesFromMetadataFormat() throws Exception {
        String disciplineIdOne = "discipline:/1";
        String disciplineIdTwo = "discipline:/2";
        String metadataId = "uri:dublincore:1.0";
        
        Relationship expectedObverseRelOne = new Relationship(disciplineIdOne, metadataId, AGGREGATES);
        Relationship expectedInverseRelOne = new Relationship(metadataId, disciplineIdOne, IS_AGGREGATED_BY);
        Relationship expectedObverseRelTwo = new Relationship(disciplineIdTwo, metadataId, AGGREGATES);
        Relationship expectedInverseRelTwo = new Relationship(metadataId, disciplineIdTwo, IS_AGGREGATED_BY);
        Set<Relationship> expectedRelations = new HashSet<Relationship>();
        expectedRelations.add(expectedInverseRelOne);
        expectedRelations.add(expectedObverseRelOne);
        expectedRelations.add(expectedInverseRelTwo);
        expectedRelations.add(expectedObverseRelTwo);
        
        Discipline disciplineOne = new Discipline("Discipline One", disciplineIdOne);
        Discipline disciplineTwo = new Discipline("Discipline One", disciplineIdTwo);
        DcsMetadataFormat mdf = new DcsMetadataFormat();
        mdf.setName("Metadata Format One");
        mdf.setVersion("1.0");
        mdf.setId(metadataId);
        Set<Discipline> disciplines = new HashSet<Discipline>();
        disciplines.add(disciplineOne);
        disciplines.add(disciplineTwo);
        
        relDao.addRelations(expectedRelations);
        assertTrue(relDao.getRelations(disciplineIdOne, AGGREGATES).contains(expectedObverseRelOne));
        assertTrue(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRelOne));
        assertTrue(relDao.getRelations(disciplineIdTwo, AGGREGATES).contains(expectedObverseRelTwo));
        assertTrue(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRelTwo));
        
        underTest.removeDisciplinesFromMetadataFormat(disciplines, mdf);
        
        assertFalse(relDao.getRelations(disciplineIdOne, AGGREGATES).contains(expectedObverseRelOne));
        assertFalse(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRelOne));
        assertFalse(relDao.getRelations(disciplineIdTwo, AGGREGATES).contains(expectedObverseRelTwo));
        assertFalse(relDao.getRelations(metadataId, IS_AGGREGATED_BY).contains(expectedInverseRelTwo));
    }
    
    @Test
    public void testRemoveMetadataFormatsFromDiscipline() throws Exception {
        String disciplineId = "discipline:/1";
        String metadataIdOne = "uri:dublincore:1.0";
        String metadataIdTwo = "uri:dublincore:2.0";
        
        Relationship expectedObverseRelOne = new Relationship(disciplineId, metadataIdOne, AGGREGATES);
        Relationship expectedInverseRelOne = new Relationship(metadataIdOne, disciplineId, IS_AGGREGATED_BY);
        Relationship expectedObverseRelTwo = new Relationship(disciplineId, metadataIdTwo, AGGREGATES);
        Relationship expectedInverseRelTwo = new Relationship(metadataIdTwo, disciplineId, IS_AGGREGATED_BY);
        Set<Relationship> expectedRelations = new HashSet<Relationship>();
        expectedRelations.add(expectedInverseRelOne);
        expectedRelations.add(expectedObverseRelOne);
        expectedRelations.add(expectedInverseRelTwo);
        expectedRelations.add(expectedObverseRelTwo);
        
        Discipline discipline = new Discipline("Discipline One", disciplineId);
        DcsMetadataFormat mdfOne = new DcsMetadataFormat();
        mdfOne.setId(metadataIdOne);
        mdfOne.setName("Metadata Format One");
        mdfOne.setVersion("1.0");
        
        DcsMetadataFormat mdfTwo = new DcsMetadataFormat();
        mdfTwo.setId(metadataIdTwo);
        mdfTwo.setName("Metadata Format Two");
        mdfTwo.setVersion("1.0");
        
        Set<DcsMetadataFormat> mdfs = new HashSet<DcsMetadataFormat>();
        mdfs.add(mdfOne);
        mdfs.add(mdfTwo);
        
        relDao.addRelations(expectedRelations);
        assertTrue(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRelOne));
        assertTrue(relDao.getRelations(metadataIdOne, IS_AGGREGATED_BY).contains(expectedInverseRelOne));
        assertTrue(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRelTwo));
        assertTrue(relDao.getRelations(metadataIdTwo, IS_AGGREGATED_BY).contains(expectedInverseRelTwo));
        
        underTest.removeMetadataFormatsFromDiscipline(mdfs, discipline);
        
        assertFalse(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRelOne));
        assertFalse(relDao.getRelations(metadataIdOne, IS_AGGREGATED_BY).contains(expectedInverseRelOne));
        assertFalse(relDao.getRelations(disciplineId, AGGREGATES).contains(expectedObverseRelTwo));
        assertFalse(relDao.getRelations(metadataIdTwo, IS_AGGREGATED_BY).contains(expectedInverseRelTwo));
    }
    
    @Test
    public void testAddCollectionToProject() throws Exception {
        
        // Prepare objects
        final String projectId = "1";
        final String collectionId = "collectionId";
        final Project project = new Project();
        final Collection collection = new Collection();
        project.setId(projectId);
        collection.setId(collectionId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(collectionId).isEmpty());
        
        // Expected relationships to be added
        final Relationship expectedObverse = new Relationship(collectionId, projectId, IS_AGGREGATED_BY);
        final Relationship expectedInverse = new Relationship(projectId, collectionId, AGGREGATES);
        
        // Add a collection to the project
        underTest.addCollectionToProject(collection, project);
        
        // Verify expected relationships were added
        final Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(collectionId);
        assertEquals(2, addedRels.size());
        
        // Verify that an IS_AGGREGATED_BY relationship exists between 'projectId' and 'collectionId'
        assertEquals(1, addedRels.get(IS_AGGREGATED_BY).size());
        assertEquals(expectedObverse, addedRels.get(IS_AGGREGATED_BY).iterator().next());
        
        // Verify that an AGGREGATES relationship exists between 'collectionId' and 'projectId'
        assertEquals(1, addedRels.get(AGGREGATES).size());
        assertEquals(expectedInverse, addedRels.get(AGGREGATES).iterator().next());
        
    }
    
    @Test
    public void testAddSubCollectionToCollection() throws Exception {
        String parent = "id:c1";
        String kid = "id:c2";
        
        // Verify assumptions
        assertTrue(relDao.getRelations(parent).isEmpty());
        assertTrue(relDao.getRelations(kid).isEmpty());

        // Expected relationships to be added
        final Relationship expectedObverse = new Relationship(kid, parent, IS_SUBCOLLECTION_OF);
        final Relationship expectedInverse = new Relationship(parent, kid, HAS_SUBCOLLECTION);
        
        // Add a collection to the project
        underTest.addSubCollectionToCollection(kid, parent);
        
        // Verify expected relationships were added
        final Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(parent);
        assertEquals(2, addedRels.size());
        
        // Verify expected relations
        
        assertEquals(1, addedRels.get(IS_SUBCOLLECTION_OF).size());
        assertEquals(expectedObverse, addedRels.get(IS_SUBCOLLECTION_OF).iterator().next());
        
        assertEquals(1, addedRels.get(HAS_SUBCOLLECTION).size());
        assertEquals(expectedInverse, addedRels.get(HAS_SUBCOLLECTION).iterator().next());        
    }
    
    @Test
    public void testRemoveCollectionFromProject() throws Exception {
        String parent = "id:c1";
        String kid = "id:c2";
        
        relDao.addRelation(kid, parent, IS_SUBCOLLECTION_OF);
        relDao.addRelation(parent, kid, HAS_SUBCOLLECTION);
        
        assertEquals(2, relDao.getRelations(parent).size());
        assertEquals(2, relDao.getRelations(kid).size());
        
        underTest.removeSubCollectionFromCollection(kid, parent);
        
        assertEquals(0, relDao.getRelations(parent).size());
        assertEquals(0, relDao.getRelations(kid).size());
    }
    
    @Test
    public void testRemoveSubCollectionFromCollection() throws Exception {
        Project project = new Project();
        String projectId = "1";
        project.setId(projectId);
        Collection collection = new Collection();
        final String collectionId = "collection";
        collection.setId(collectionId);
        relDao.addRelation(collectionId, projectId, IS_AGGREGATED_BY);
        relDao.addRelation(projectId, collectionId, AGGREGATES);
        
        assertEquals(2, relDao.getRelations(projectId).size());
        assertEquals(2, relDao.getRelations(collectionId).size());
        
        underTest.removeCollectionFromProject(collection, project);
        
        assertEquals(0, relDao.getRelations(projectId).size());
        assertEquals(0, relDao.getRelations(collectionId).size());
        
    }
    
    @Test
    public void testGetCollectionsForProject() throws Exception {
        Project project = new Project();
        String projectId = "1";
        project.setId(projectId);
        
        Collection collection1 = new Collection();
        final String collectionOneId = "collectionOne";
        collection1.setId(collectionOneId);
        Collection collection2 = new Collection();
        final String collectionTwoId = "collectionTwo";
        collection2.setId(collectionTwoId);
        
        Set<Collection> expectedCollections = new HashSet<Collection>();
        expectedCollections.add(collection1);
        expectedCollections.add(collection2);
        
        for (Collection c : expectedCollections) {
            relDao.addRelation(c.getId(), projectId, IS_AGGREGATED_BY);
            relDao.addRelation(projectId, c.getId(), AGGREGATES);
            archiveService.deposit(c);
        }
        
        archiveService.pollArchive();
        
        assertEquals(2, relDao.getRelations(projectId, AGGREGATES).size());
        assertEquals(2, relDao.getRelations(projectId, IS_AGGREGATED_BY).size());
        assertEquals(2, relDao.getRelations(collectionOneId).values().size());
        assertEquals(2, relDao.getRelations(collectionTwoId).values().size());
        
        final Set<Collection> actualCollections = underTest.getCollectionsForProject(project);
        
        assertEquals(expectedCollections, actualCollections);
        
    }
    
    @Test
    public void testGetCollectionForProjectSetNotNull() throws Exception {
        // create Project, no relations in rel table
        final Project project = new Project();
        final String projectId = "id://ProjectNotNull";
        project.setId(projectId);
        
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertNotNull(underTest.getCollectionsForProject(project));
    }
    
    @Test
    public void testAddDataSetToCollection() throws Exception {
        
        // Prepare objects
        final String collectionId = "1";
        final String dataSetId = "dataSetId";
        final Collection collection = new Collection();
        final DataItem dataItem = new DataItem();
        collection.setId(collectionId);
        dataItem.setId(dataSetId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(collectionId).isEmpty());
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        
        // Expected relationships to be added
        final Relationship expectedObverse = new Relationship(dataSetId, collectionId, IS_AGGREGATED_BY);
        final Relationship expectedInverse = new Relationship(collectionId, dataSetId, AGGREGATES);
        
        // Add a dataset to the collection
        underTest.addDataSetToCollection(dataItem, collection);
        
        // Verify expected relationships were added
        final Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(dataSetId);
        assertEquals(2, addedRels.size());
        
        // Verify that an IS_AGGREGATED_BY relationship exists between 'projectId' and 'collectionId'
        assertEquals(1, addedRels.get(IS_AGGREGATED_BY).size());
        assertEquals(expectedObverse, addedRels.get(IS_AGGREGATED_BY).iterator().next());
        
        // Verify that an AGGREGATES relationship exists between 'collectionId' and 'projectId'
        assertEquals(1, addedRels.get(AGGREGATES).size());
        assertEquals(expectedInverse, addedRels.get(AGGREGATES).iterator().next());
    }
    
    @Test
    public void testRemoveDataSetFromCollection() throws Exception {
        final Collection collection = new Collection();
        final String collectionId = "1";
        collection.setId(collectionId);
        final DataItem dataItem = new DataItem();
        final String dataSetId = "dataSet1";
        dataItem.setId(dataSetId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(collectionId).isEmpty());
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        
        relDao.addRelation(dataSetId, collectionId, IS_AGGREGATED_BY);
        relDao.addRelation(collectionId, dataSetId, AGGREGATES);
        
        assertEquals(2, relDao.getRelations(collectionId).size());
        assertEquals(2, relDao.getRelations(dataSetId).size());
        
        underTest.removeDataSetFromCollection(dataItem, collection);
        
        assertEquals(0, relDao.getRelations(collectionId).size());
        assertEquals(0, relDao.getRelations(dataSetId).size());
        
    }
    
    @Test
    public void testGetCollectionForDataSet() throws Exception {
        final Collection collection = new Collection();
        final String collectionId = "1";
        collection.setId(collectionId);
        
        final DataItem dataItem = new DataItem();
        final String dataSetId = "dataset1";
        dataItem.setId(dataSetId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(collectionId).isEmpty());
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        
        relDao.addRelation(dataSetId, collectionId, IS_AGGREGATED_BY);
        relDao.addRelation(collectionId, dataSetId, AGGREGATES);
        
        assertEquals(2, relDao.getRelations(collectionId).size());
        assertEquals(2, relDao.getRelations(dataSetId).size());
        
        archiveService.deposit(collection);
        archiveService.pollArchive();
        
        // now get the collection aggregating the dataset
        assertEquals(collection, underTest.getCollectionForDataSet(dataItem));
        
    }
    
    @Test
    public void testGetCollectionForDataSetNotNull() throws Exception {
        // create DataItem, no relations in rel table
        final DataItem dataItem = new DataItem();
        final String dataSetId = "dataset13793";
        dataItem.setId(dataSetId);
        
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        assertNotNull(underTest.getCollectionForDataSet(dataItem));
    }
    
    @Test
    public void testAddDataFileToDataSet() throws Exception {
        final String dataFileId = "id://1093";
        final DataFile dataFile = new DataFile();
        dataFile.setId(dataFileId);
        
        final String dataSetId = "id://3511";
        final DataItem dataItem = new DataItem();
        dataItem.setId(dataSetId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        
        // Expected relationships to be added
        final Relationship expectedObverse = new Relationship(dataFileId, dataSetId, IS_AGGREGATED_BY);
        final Relationship expectedInverse = new Relationship(dataSetId, dataFileId, AGGREGATES);
        
        // Add a dataset to the collection
        underTest.addDataFileToDataSet(dataFile, dataItem);
        
        // Verify expected relationships were added
        final Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(dataFileId);
        assertEquals(2, addedRels.size());
        
        // Verify that an IS_AGGREGATED_BY relationship exists between 'dataFileId' and 'dataSetId'
        assertEquals(1, addedRels.get(IS_AGGREGATED_BY).size());
        assertEquals(expectedObverse, addedRels.get(IS_AGGREGATED_BY).iterator().next());
        
        // Verify that an AGGREGATES relationship exists between 'dataSetId' and 'dataFileId'
        assertEquals(1, addedRels.get(AGGREGATES).size());
        assertEquals(expectedInverse, addedRels.get(AGGREGATES).iterator().next());
    }
    
    /**
     * Test that when this method is called on a dataset, the existing relationship is wiped out and new ones are added.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateDataFileRelationshipForDataSet() throws Exception {
        final String dataFileId = "id://1093";
        final DataFile dataFile = new DataFile();
        dataFile.setId(dataFileId);
        
        final String dataSetId = "id://3511";
        final DataItem dataItem = new DataItem();
        dataItem.setId(dataSetId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        
        // Expected relationships to be added
        final Relationship initialExpectedObverse = new Relationship(dataFileId, dataSetId, IS_AGGREGATED_BY);
        final Relationship initialExpectedInverse = new Relationship(dataSetId, dataFileId, AGGREGATES);
        
        // Add a dataset to the collection
        underTest.addDataFileToDataSet(dataFile, dataItem);
        
        // Verify expected relationships were added
        final Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(dataFileId);
        assertEquals(2, addedRels.size());
        
        // Verify that an IS_AGGREGATED_BY relationship exists between 'dataFileId' and 'dataSetId'
        assertEquals(1, addedRels.get(IS_AGGREGATED_BY).size());
        assertEquals(initialExpectedObverse, addedRels.get(IS_AGGREGATED_BY).iterator().next());
        
        // Verify that an AGGREGATES relationship exists between 'dataSetId' and 'dataFileId'
        assertEquals(1, addedRels.get(AGGREGATES).size());
        assertEquals(initialExpectedInverse, addedRels.get(AGGREGATES).iterator().next());
        
        // Making updates to the dataset
        final String newDataFileId1 = "id://11111";
        final DataFile newDataFile1 = new DataFile();
        newDataFile1.setId(newDataFileId1);
        
        final String newDataFileId2 = "id://22222";
        final DataFile newDataFile2 = new DataFile();
        newDataFile2.setId(newDataFileId2);
        
        // Adding new file, old file were never added to the dataset for this test (only relationship were added in the
        // relationship table) so no need to remove the existing file from the dataset.
        dataItem.addFile(newDataFile1);
        dataItem.addFile(newDataFile2);
        
        // Update relationship
        underTest.updateDataFileRelationshipForDataSet(dataItem);
        
        // Expects: old relationship is gone, and new ones are added.
        // Verify expected relationships were added
        final Map<Relationship.RelType, Set<Relationship>> updatedRelsMap = relDao.getRelations(dataSetId);
        assertEquals(2, updatedRelsMap.size());
        
        final Relationship expectedObverseFile1 = new Relationship(newDataFileId1, dataSetId, IS_AGGREGATED_BY);
        final Relationship expectedInverseFile1 = new Relationship(dataSetId, newDataFileId1, AGGREGATES);
        
        final Relationship expectedObverseFile2 = new Relationship(newDataFileId2, dataSetId, IS_AGGREGATED_BY);
        final Relationship expectedInverseFile2 = new Relationship(dataSetId, newDataFileId2, AGGREGATES);
        
        // Verify that an IS_AGGREGATED_BY relationship exists between 'dataFileId' and 'dataSetId'
        assertNotNull(updatedRelsMap.get(Relationship.RelType.AGGREGATES));
        assertEquals(2, updatedRelsMap.get(Relationship.RelType.AGGREGATES).size());
        assertTrue(updatedRelsMap.get(Relationship.RelType.AGGREGATES).contains(expectedInverseFile1));
        assertTrue(updatedRelsMap.get(Relationship.RelType.AGGREGATES).contains(expectedInverseFile2));
        assertFalse(updatedRelsMap.get(Relationship.RelType.AGGREGATES).contains(initialExpectedInverse));
        
        // Verify that an AGGREGATES relationship exists between 'dataSetId' and 'dataFileId'
        assertNotNull(updatedRelsMap.get(Relationship.RelType.IS_AGGREGATED_BY));
        assertEquals(2, updatedRelsMap.get(Relationship.RelType.IS_AGGREGATED_BY).size());
        assertTrue(updatedRelsMap.get(Relationship.RelType.IS_AGGREGATED_BY).contains(expectedObverseFile1));
        assertTrue(updatedRelsMap.get(Relationship.RelType.IS_AGGREGATED_BY).contains(expectedObverseFile1));
        assertFalse(updatedRelsMap.get(Relationship.RelType.IS_AGGREGATED_BY).contains(initialExpectedObverse));
    }
    
    @Test
    public void testGetDataSetForDataFile() throws Exception {
        
        final DataItem dataItem = new DataItem();
        final String dataSetId = "id://Set0987";
        dataItem.setId(dataSetId);
        dataItem.setName("test data set");
        
        final DataFile dataFile = new DataFile();
        final String dataFileId = "id://File7654";
        dataFile.setId(dataFileId);
        
        dataFile.setName("gorilla");
        java.io.File tmp = java.io.File.createTempFile("grr", null);
        tmp.deleteOnExit();
        dataFile.setSource(tmp.toURI().toURL().toExternalForm());
        dataFile.setSize(0);
        dataItem.addFile(dataFile);
        
        final Collection collection = new Collection();
        final String collectionId = "id://collection9292929";
        collection.setId(collectionId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        
        relDao.addRelation(dataFileId, dataSetId, IS_AGGREGATED_BY);
        relDao.addRelation(dataSetId, dataFileId, AGGREGATES);
        
        assertEquals(2, relDao.getRelations(dataSetId).size());
        assertEquals(2, relDao.getRelations(dataFileId).size());
        
        String col_deposit_id = archiveService.deposit(collection);
        archiveService.pollArchive();
        assertNotNull(col_deposit_id);
        String ds_deposit_id = archiveService.deposit(col_deposit_id, dataItem);
        assertNotNull(ds_deposit_id);
        archiveService.pollArchive();
        // now get the dataSet aggregating the dataFile
        assertEquals(dataItem.getId(), underTest.getDataSetForDataFile(dataFile).getId());
        
    }
    
    @Test
    public void testGetDataSetForDataFileNotNull() throws Exception {
        // create DataFile, no relations in rel table
        final DataFile dataFile = new DataFile();
        final String dataFileId = "id://File73354";
        dataFile.setId(dataFileId);
        
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        assertNotNull(underTest.getDataSetForDataFile(dataFile));
    }
    
    @Test
    public void testGetDataSetForDataFileId() throws Exception {
        
        final DataItem dataItem = new DataItem();
        final String dataSetId = "id://Set0987";
        dataItem.setId(dataSetId);
        dataItem.setName("test data set");
        
        final DataFile dataFile = new DataFile();
        final String dataFileId = "id://File7654";
        dataFile.setId(dataFileId);
        
        dataFile.setName("gorilla");
        java.io.File tmp = java.io.File.createTempFile("grr", null);
        tmp.deleteOnExit();
        dataFile.setSource(tmp.toURI().toURL().toExternalForm());
        dataFile.setSize(0);
        dataItem.addFile(dataFile);
        
        final Collection collection = new Collection();
        final String collectionId = "id://collection9292929";
        collection.setId(collectionId);
        
        // Verify assumptions
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        
        relDao.addRelation(dataFileId, dataSetId, IS_AGGREGATED_BY);
        relDao.addRelation(dataSetId, dataFileId, AGGREGATES);
        
        assertEquals(2, relDao.getRelations(dataSetId).size());
        assertEquals(2, relDao.getRelations(dataFileId).size());
        
        String col_deposit_id = archiveService.deposit(collection);
        archiveService.pollArchive();
        assertNotNull(col_deposit_id);
        String ds_deposit_id = archiveService.deposit(col_deposit_id, dataItem);
        assertNotNull(ds_deposit_id);
        archiveService.pollArchive();
        // now get the dataSet aggregating the dataFile
        assertEquals(dataItem.getId(), underTest.getDataSetForDataFileId(dataFile.getId()).getId());
        
    }
    
    @Test
    public void testGetDataSetForDataFileIdNotNull() throws Exception {
        // create DataFile, no relations in rel table
        final DataFile dataFile = new DataFile();
        final String dataFileId = "id://File73354";
        dataFile.setId(dataFileId);
        
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        assertNotNull(underTest.getDataSetForDataFileId(dataFile.getId()));
    }
    
    @Test
    public void testRemoveDataFileFromDataSet() {
        final DataItem dataItem = new DataItem();
        final String dataSetId = "id://Set0988";
        dataItem.setId(dataSetId);
        dataItem.setName("test data set");
        
        final DataFile dataFile = new DataFile();
        final String dataFileId = "id://File7655";
        dataFile.setId(dataFileId);
        // Verify assumptions
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        
        relDao.addRelation(dataFileId, dataSetId, IS_AGGREGATED_BY);
        relDao.addRelation(dataSetId, dataFileId, AGGREGATES);
        
        assertEquals(2, relDao.getRelations(dataSetId).size());
        assertEquals(2, relDao.getRelations(dataFileId).size());
        
        underTest.removeDataFileFromDataSet(dataFile, dataItem);
        
        assertTrue(relDao.getRelations(dataSetId).isEmpty());
        assertTrue(relDao.getRelations(dataFileId).isEmpty());
        
    }
    @Test
    public void testAddMetadataFileToBusinessObject() throws Exception {
        final String metadataFileId = "id://1093";

        final String dataItemId  ="id:/3511";

        // Verify assumptions
        assertTrue(relDao.getRelations(metadataFileId).isEmpty());
        assertTrue(relDao.getRelations(dataItemId).isEmpty());

        // Expected relationships to be added
        final Relationship expectedInverse = new Relationship(dataItemId, metadataFileId, HAS_METADATA_FILE);

        // Add metadata file to the dataset
        underTest.addMetadataFileToBusinessObject(metadataFileId, dataItemId);

        // Verify expected relationship was added
        final Map<Relationship.RelType, Set<Relationship>> addedRels = relDao.getRelations(metadataFileId);
        assertEquals(1, addedRels.size());

        // Verify that an AGGREGATES relationship exists between 'dataSetId' and 'dataFileId'
        assertEquals(1, addedRels.get(HAS_METADATA_FILE).size());
        assertEquals(expectedInverse, addedRels.get(HAS_METADATA_FILE).iterator().next());
    }

    @Test
    public void testRemoveMetadataFileFromBusinessObject() {

        final String projectId = "id://Project1786";
        final String metadataFileId = "id://MetadataFile7655";

        // Verify assumptions
        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(metadataFileId).isEmpty());

        relDao.addRelation(projectId, metadataFileId, HAS_METADATA_FILE);

        assertEquals(1, relDao.getRelations(metadataFileId).size());
        assertEquals(1, relDao.getRelations(projectId).size());

        underTest.removeMetadataFileFromBusinessObject(metadataFileId, projectId);

        assertTrue(relDao.getRelations(projectId).isEmpty());
        assertTrue(relDao.getRelations(metadataFileId).isEmpty());

    }

    @Test
    public void testGetMetadataFilesIdsForBusinessObjecId() throws Exception{
        final String metadataFileId1 = "id:MetadataFile234";
        final String metadataFileId2 = "id:MetadataFile2345";
        final String metadataFileId3 = "id:MetadataFile2346";

        final String businessObject1 = "id:BusinessObject3251235235";

        //Verify Assumptions
        assertTrue(relDao.getRelations(metadataFileId1).isEmpty());
        assertTrue(relDao.getRelations(metadataFileId2).isEmpty());
        assertTrue(relDao.getRelations(metadataFileId3).isEmpty());
        assertTrue(relDao.getRelations(businessObject1).isEmpty());

        relDao.addRelation(businessObject1, metadataFileId1, HAS_METADATA_FILE);
        relDao.addRelation(businessObject1, metadataFileId2, HAS_METADATA_FILE);
        relDao.addRelation(businessObject1, metadataFileId3, HAS_METADATA_FILE);

        assertEquals(1, relDao.getRelations(metadataFileId1).size());
        assertEquals(1, relDao.getRelations(metadataFileId2).size());
        assertEquals(1, relDao.getRelations(metadataFileId3).size());

        Set<String> metadataFileIds = underTest.getMetadataFileIdsForBusinessObjectId(businessObject1);

        assertTrue(metadataFileIds.contains(metadataFileId1));
        assertTrue(metadataFileIds.contains(metadataFileId2));
        assertTrue(metadataFileIds.contains(metadataFileId3));
    }

    @Test(expected=RelationshipConstraintException.class)
    public void testAddMetadataToSecondBusinessObjectThrowsException() throws Exception{
        final String metadataFileId1 = "id:MetadataFile234887";
        final String businessObjectId1 = "id:Businessbject5662";
        final String businessObjectId2 = "id:BusinessObject7990";

        //Verify Assumptions
        assertTrue(relDao.getRelations(metadataFileId1).isEmpty());
        assertTrue(relDao.getRelations(businessObjectId1).isEmpty());
        assertTrue(relDao.getRelations(businessObjectId2).isEmpty());

        relDao.addRelation(businessObjectId1, metadataFileId1, HAS_METADATA_FILE);

        assertEquals(1, relDao.getRelations(metadataFileId1).size());
        assertEquals(1, relDao.getRelations(businessObjectId1).size());

        underTest.addMetadataFileToBusinessObject(metadataFileId1, businessObjectId2);

    }

}
