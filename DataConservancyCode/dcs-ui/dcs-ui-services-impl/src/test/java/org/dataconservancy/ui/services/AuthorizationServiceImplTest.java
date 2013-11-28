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

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.ui.model.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.mockito.Matchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for the AuthorizationServiceImpl.
 */
public class AuthorizationServiceImplTest
        extends BaseUnitTest {

    /**
     * The instance of AuthorizationServiceImpl under test
     */
    private AuthorizationServiceImpl underTest;
    
    /**
     * This is a Person instance that shouldn't be authorized to access
     * anything. E.g. they shouldn't be a project admin, instance admin,
     * collection depositor, or data set depositor.
     */
    private Person unauthorizedPerson = new Person();

    /**
     * This is a Person instance that is the administrator for
     * {@link #projectOne}
     */
    private Person projectAdmin = new Person();

    /**
     * This is a Person instance that is an adminstrator on {@link #projectOne}
     */
    private Person secondProjectAdmin = new Person();

    /**
     * This is a Person instance that is a site-wide instance administrator
     * (that is, they have Role.ROLE_ADMIN).
     */
    private Person instanceAdmin = new Person();

    /**
     * This is the Person that deposited the {@link #dataItemOne}
     */
    private Person dataSetDepositor = new Person();

    /**
     * This is the Person that deposited the the {@link #collectionWithData}
     */
    private Person collectionDepositor = new Person();

    /**
     * This is the Person that deposited the the {@link #collectionWithData}
     */
    private Person subCollectionDepositor = new Person();

    /**
     * This is the Person that deposited the the {@link #collectionWithData}
     */
    private Person subCollection2Depositor = new Person();

    /**
     * This is a Person that is authorized to deposit to
     * {@link #collectionWithData}.
     */
    private Person authorizedDepositor = new Person();

    /**
     * This is the new sub collection under {@link #collectionWithData}
     */
    private Collection subCollection = new Collection();

    /**
     * This is the new sub collection under {@link #subCollection}
     */
    private Collection subCollection2 = new Collection();

    private Collection standAloneCollection = new Collection();

    RelationshipService relService;

    /**
     * Sets up the instance of AuthorizationServiceImpl under test. It mocks the
     * only collaborator: the RelationshipService.
     *
     * @throws RelationshipConstraintException
     */
    @Before
    public void setUpUnderTest() throws RelationshipException {
        standAloneCollection.setId("AuthorizationServiceImplTest:standAlongCollection:id");
        subCollectionDepositor.setId("AuthorizationServiceImpleTest:subcollection:depositor:id");
        subCollectionDepositor.setFirstNames("subCollectionDepositor");
        subCollection2Depositor.setId("AuthorizationServiceImpleTest:subcollection2:depositor:id");
        subCollection2Depositor.setFirstNames("subCollection2Depositor");


        relService = mock(RelationshipService.class);
        when(relService.getDataSetForDataFile(dataFileOne))
                .thenReturn(dataItemOne);
        when(relService.getCollectionForDataSet(dataItemOne))
                .thenReturn(collectionWithData);
        when(relService.getProjectForCollection(collectionWithData))
                .thenReturn(projectOne);
        when(relService.getProjectForCollection(standAloneCollection))
                .thenReturn(projectOne);
        when(relService.getProjectForCollection(collectionWithData.getId()))
                .thenReturn(projectOne.getId());


        Set<Person> collectionDepositors = new HashSet<Person>();
        collectionDepositors.add(collectionDepositor);
        collectionDepositors.add(authorizedDepositor);
        collectionDepositors.add(projectAdmin);
        when(relService.getDepositorsForCollection(collectionWithData.getId()))
                .thenReturn(collectionDepositors);

        //TODO: mock relationshipService to return collectionWithData as parent of subCollection
        subCollection.setId("AuthorizationServiceImplTest:subcollection:id");
        Set<String> superCollectionIds = new HashSet<String>();
        superCollectionIds.add(collectionWithData.getId());
        when(relService.getSuperCollectionIdsForCollectionId(subCollection.getId())).thenReturn(superCollectionIds);

        collectionDepositors = new HashSet<Person>();
        collectionDepositors.add(subCollectionDepositor);
        when(relService.getDepositorsForCollection(subCollection.getId())).thenReturn(collectionDepositors);

        //TODO: mock relationshipService to return subCollection as parent of subCollection2
        subCollection2.setId("AuthorizationServiceImplTest:subcollection2:id");
        superCollectionIds = new HashSet<String>();
        superCollectionIds.add(subCollection.getId());
        when(relService.getSuperCollectionIdsForCollectionId(subCollection2.getId())).thenReturn(superCollectionIds);

        collectionDepositors = new HashSet<Person>();
        collectionDepositors.add(subCollection2Depositor);
        when(relService.getDepositorsForCollection(subCollection2.getId())).thenReturn(collectionDepositors);

        Set<Person> projectAdmins = new HashSet<Person>();
        projectAdmins.add(projectAdmin);
        projectAdmins.add(secondProjectAdmin);
        when(relService.getAdministratorsForProject(projectOne))
                .thenReturn(projectAdmins);
        when(relService.getAdministratorsForProject(projectOne.getId()))
                .thenReturn(projectAdmins);


        underTest = new AuthorizationServiceImpl(relService);

    }

    /**
     * Configures the Persons used in this test.
     */
    @Before
    public void setUpPersons() {

        unauthorizedPerson.setId("http://foo.bar/person/1234");
        unauthorizedPerson.setFirstNames("Unauthorized");
        unauthorizedPerson.setLastNames("Person");
        unauthorizedPerson.setPrefix("Mr.");
        unauthorizedPerson.setSuffix("II");
        unauthorizedPerson.setMiddleNames("Middle");
        unauthorizedPerson.setPreferredPubName("U. Person");
        unauthorizedPerson.setBio("Some bio for the user.");
        unauthorizedPerson.setWebsite("www.somewebsite.com");
        unauthorizedPerson.setEmailAddress("unauthorized@foo.com");
        unauthorizedPerson.setJobTitle("Unauthorized Scientist");
        unauthorizedPerson.setDepartment("Unauthorized Department");
        unauthorizedPerson.setCity("Baltimore");
        unauthorizedPerson.setState("Maryland");
        unauthorizedPerson.setInstCompany("Unauthorized Institution/Company");
        unauthorizedPerson.setInstCompanyWebsite("www.UnauthorizedInstitutionCompany.com");
        unauthorizedPerson.setRegistrationStatus(RegistrationStatus.APPROVED);
        unauthorizedPerson.addRole(Role.ROLE_USER);
        unauthorizedPerson.setExternalStorageLinked(false);
        unauthorizedPerson.setDropboxAppKey("SomeKey");
        unauthorizedPerson.setDropboxAppSecret("SomeSecret");

        projectAdmin.setId("http://foo.bar/person/5678");
        projectAdmin.setFirstNames("Project");
        projectAdmin.setLastNames("Admin");
        projectAdmin.setPrefix("Mr.");
        projectAdmin.setSuffix("II");
        projectAdmin.setMiddleNames("Middle");
        projectAdmin.setPreferredPubName("P. Admin");
        projectAdmin.setBio("Some bio for the user.");
        projectAdmin.setWebsite("www.somewebsite.com");
        projectAdmin.setEmailAddress("projectadmin@foo.com");
        projectAdmin.setJobTitle("Project Scientist");
        projectAdmin.setDepartment("Project Department");
        projectAdmin.setCity("Baltimore");
        projectAdmin.setState("Maryland");
        projectAdmin.setInstCompany("Project Institution/Company");
        projectAdmin.setInstCompanyWebsite("www.ProjectInstitutionCompany.com");
        projectAdmin.setRegistrationStatus(RegistrationStatus.APPROVED);
        projectAdmin.addRole(Role.ROLE_USER);
        projectAdmin.setExternalStorageLinked(false);
        projectAdmin.setDropboxAppKey("SomeKey");
        projectAdmin.setDropboxAppSecret("SomeSecret");

        secondProjectAdmin.setId("http://foo.bar/person/674326");
        secondProjectAdmin.setFirstNames("Second");
        secondProjectAdmin.setLastNames("Admin");
        secondProjectAdmin.setPrefix("Mr.");
        secondProjectAdmin.setSuffix("II");
        secondProjectAdmin.setMiddleNames("Middle");
        secondProjectAdmin.setPreferredPubName("S. Admin");
        secondProjectAdmin.setBio("Some bio for the user.");
        secondProjectAdmin.setWebsite("www.somewebsite.com");
        secondProjectAdmin.setEmailAddress("secondadmin@foo.com");
        secondProjectAdmin.setJobTitle("Second Scientist");
        secondProjectAdmin.setDepartment("Second Department");
        secondProjectAdmin.setCity("Baltimore");
        secondProjectAdmin.setState("Maryland");
        secondProjectAdmin.setInstCompany("Second Institution/Company");
        secondProjectAdmin.setInstCompanyWebsite("www.SecondInstitutionCompany.com");
        secondProjectAdmin.setRegistrationStatus(RegistrationStatus.APPROVED);
        secondProjectAdmin.addRole(Role.ROLE_USER);
        secondProjectAdmin.setExternalStorageLinked(false);
        secondProjectAdmin.setDropboxAppKey("SomeKey");
        secondProjectAdmin.setDropboxAppSecret("SomeSecret");

        instanceAdmin.setId("http://foo.bar/person/4321");
        instanceAdmin.setFirstNames("Instance");
        instanceAdmin.setLastNames("Admin");
        instanceAdmin.setPrefix("Mr.");
        instanceAdmin.setSuffix("II");
        instanceAdmin.setMiddleNames("Middle");
        instanceAdmin.setPreferredPubName("I. Admin");
        instanceAdmin.setBio("Some bio for the user.");
        instanceAdmin.setWebsite("www.somewebsite.com");
        instanceAdmin.setEmailAddress("instanceadmin@foo.com");
        instanceAdmin.setJobTitle("Instance Scientist");
        instanceAdmin.setDepartment("Instance Department");
        instanceAdmin.setCity("Baltimore");
        instanceAdmin.setState("Maryland");
        instanceAdmin.setInstCompany("Instance Institution/Company");
        instanceAdmin.setInstCompanyWebsite("www.InstanceInstitutionCompany.com");
        instanceAdmin.setRegistrationStatus(RegistrationStatus.APPROVED);
        instanceAdmin.addRole(Role.ROLE_ADMIN);
        instanceAdmin.addRole(Role.ROLE_USER);
        instanceAdmin.setExternalStorageLinked(false);
        instanceAdmin.setDropboxAppKey("SomeKey");
        instanceAdmin.setDropboxAppSecret("SomeSecret");

        collectionDepositor.setId("http://foo.bar/person/8765");
        collectionDepositor.setFirstNames("Collection");
        collectionDepositor.setLastNames("Depositor");
        collectionDepositor.setPrefix("Mr.");
        collectionDepositor.setSuffix("II");
        collectionDepositor.setMiddleNames("Middle");
        collectionDepositor.setPreferredPubName("C. Depositor");
        collectionDepositor.setBio("Some bio for the user.");
        collectionDepositor.setWebsite("www.somewebsite.com");
        collectionDepositor.setEmailAddress("collectionDepositor@foo.com");
        collectionDepositor.setRegistrationStatus(RegistrationStatus.APPROVED);
        collectionDepositor.setJobTitle("Collection Scientist");
        collectionDepositor.setDepartment("Collection Department");
        collectionDepositor.setCity("Baltimore");
        collectionDepositor.setState("Maryland");
        collectionDepositor.setInstCompany("Collection Institution/Company");
        collectionDepositor.setInstCompanyWebsite("www.CollectionInstitutionCompany.com");
        collectionDepositor.addRole(Role.ROLE_USER);
        collectionDepositor.setExternalStorageLinked(false);
        collectionDepositor.setDropboxAppKey("SomeKey");
        collectionDepositor.setDropboxAppSecret("SomeSecret");

        authorizedDepositor.setId("http://foo.bar/person/1357");
        authorizedDepositor.setFirstNames("Authorized");
        authorizedDepositor.setLastNames("Depositor");
        authorizedDepositor.setPrefix("Mr.");
        authorizedDepositor.setSuffix("II");
        authorizedDepositor.setMiddleNames("Middle");
        authorizedDepositor.setPreferredPubName("A. Depositor");
        authorizedDepositor.setBio("Some bio for the user.");
        authorizedDepositor.setWebsite("www.somewebsite.com");
        authorizedDepositor.setEmailAddress("authorizedDepositor@foo.com");
        authorizedDepositor.setJobTitle("Authorized Scientist");
        authorizedDepositor.setDepartment("Authorized Department");
        authorizedDepositor.setCity("Baltimore");
        authorizedDepositor.setState("Maryland");
        authorizedDepositor.setInstCompany("Authorized Institution/Company");
        authorizedDepositor.setInstCompanyWebsite("www.AuthorizedInstitutionCompany.com");
        authorizedDepositor.setRegistrationStatus(RegistrationStatus.APPROVED);
        authorizedDepositor.addRole(Role.ROLE_USER);
        authorizedDepositor.setExternalStorageLinked(false);
        authorizedDepositor.setDropboxAppKey("SomeKey");
        authorizedDepositor.setDropboxAppSecret("SomeSecret");

        dataSetDepositor.setId("http://foo.bar/person/02468");
        dataSetDepositor.setFirstNames("DataItem");
        dataSetDepositor.setLastNames("Depositor");
        dataSetDepositor.setPrefix("Mr.");
        dataSetDepositor.setSuffix("II");
        dataSetDepositor.setMiddleNames("Middle");
        dataSetDepositor.setPreferredPubName("D. Depositor");
        dataSetDepositor.setBio("Some bio for the user.");
        dataSetDepositor.setWebsite("www.somewebsite.com");
        dataSetDepositor.setEmailAddress("dataSetDepositor@foo.com");
        dataSetDepositor.setJobTitle("DataItem Scientist");
        dataSetDepositor.setDepartment("DataItem Department");
        dataSetDepositor.setCity("Baltimore");
        dataSetDepositor.setState("Maryland");
        dataSetDepositor.setInstCompany("DataItem Institution/Company");
        dataSetDepositor.setInstCompanyWebsite("www.DataItemInstitutionCompany.com");
        dataSetDepositor.setRegistrationStatus(RegistrationStatus.APPROVED);
        dataSetDepositor.addRole(Role.ROLE_USER);
        dataSetDepositor.setExternalStorageLinked(false);
        dataSetDepositor.setDropboxAppKey("SomeKey");
        dataSetDepositor.setDropboxAppSecret("SomeSecret");

        projectOne.addPi(projectAdmin.getId());
        projectOne.addPi(secondProjectAdmin.getId());
        collectionWithData.setDepositorId(collectionDepositor.getId());
        dataItemOne.setDepositorId(dataSetDepositor.getId());

    }

    /**
     * Insures that an unauthorized user cannot retrieve a DataItem. The user is
     * not a containing Project Admin, Instance Admin, or Depositor for the
     * containing Collection.
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetUnauthorizedUser() throws Exception {
        assertTrue(underTest
                .canRetrieveDataSet(unauthorizedPerson, dataItemOne));
    }

    /**
     * Insures that a Person who has permissions to deposit to the
     * {@link #dataItemOne}s {@link #collectionWithData} can retrieve the
     * {@link #dataItemOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetAuthorizedDepositor() throws Exception {
        assertTrue(underTest.canRetrieveDataSet(authorizedDepositor,
                dataItemOne));
    }

    /**
     * Insures that the Person who is an administrator of the
     * {@link #dataItemOne}s {@link #projectOne} can retrieve the
     * {@link #dataItemOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetContainingProjectAdmin() throws Exception {
        assertTrue(underTest.canRetrieveDataSet(projectAdmin, dataItemOne));
    }

    /**
     * Insures that the Person who deposited the {@link #dataItemOne}s
     * {@link #collectionWithData} can retrieve the {@link #dataItemOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetContainingCollectionDepositor()
            throws Exception {
    	UserService userService = mock(UserService.class);
    	when(userService.get(collectionWithData.getDepositorId())).thenReturn(admin);
    	
    	Person depositor = userService.get(collectionWithData.getDepositorId());
        assertTrue(underTest.canRetrieveDataSet(depositor, dataItemOne));
    }

    /**
     * Insures that an Instance Administrator can retrieve the
     * {@link #dataItemOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetInstanceAdmin() throws Exception {
        assertTrue(underTest.canRetrieveDataSet(instanceAdmin, dataItemOne));
    }

    /**
     * Insures correct behavior when a null user is passed in.
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetNullUser() throws Exception {
        assertTrue(underTest.canRetrieveDataSet(null, dataItemOne));
    }

    /**
     * Insures correct behavior when a null dataset is passed in.
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetNullDataSet() throws Exception {
        assertFalse(underTest.canRetrieveDataSet(instanceAdmin, null));
    }

    /**
     * Insures correct behavior when a null user and dataset are passed in.
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetNullDataSetAndPerson() throws Exception {
        assertFalse(underTest.canRetrieveDataSet(null, null));
    }

    /**
     * **** Ignored because current authorization policy for this operation does
     * not rely on relationship service. Insures correct behavior when a
     * RelationshipConstraintException is thrown. This isn't an exhaustive test.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public void testCanRetrieveDataSetWhenThrowingRelationshipConstraintException()
            throws Exception {
        RelationshipService relService = mock(RelationshipService.class);
        when(relService.getCollectionForDataSet(Matchers.<DataItem> any()))
                .thenThrow(new RelationshipConstraintException("Mocked exception."));

        underTest = new AuthorizationServiceImpl(relService);

        assertFalse(underTest.canRetrieveDataSet(projectAdmin, dataItemOne));

        verify(relService).getCollectionForDataSet(dataItemOne);
    }

    /**
     * **** Ignored because the current authorization policy for this operation
     * does not rely on relationship service Insures correct behavior when a
     * RuntimeException is thrown. This isn't an exhaustive test.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public void testCanRetrieveDataSetWhenThrowingRuntimeException()
            throws Exception {
        RelationshipService relService = mock(RelationshipService.class);
        when(relService.getAdministratorsForProject(Matchers.<Project>any()))
                .thenThrow(new RuntimeException("Mocked exception."));

        underTest = new AuthorizationServiceImpl(relService);

        assertFalse(underTest.canRetrieveDataSet(projectAdmin, dataItemOne));

        verify(relService)
                .getAdministratorsForProject(Matchers.<Project>any());
    }

    /**
     * Insures that an Instance Administrator can retrieve the
     * {@link #dataFileOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataFileInstanceAdmin() throws Exception {
        assertTrue(underTest.canRetrieveDataFile(instanceAdmin, dataFileOne));
    }

    /**
     * Insures that a Project Administrator can retrieve the
     * {@link #dataFileOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataFileProjectAdmin() throws Exception {
        assertTrue(underTest.canRetrieveDataFile(instanceAdmin, dataFileOne));
    }

    /**
     * Insures that the Person who is an administrator of the
     * {@link #dataFileOne}s {@link #projectOne} can retrieve the
     * {@link #dataFileOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataFileCollectionDepositor() throws Exception {
        assertTrue(underTest.canRetrieveDataFile(collectionDepositor,
                                                 dataFileOne));
    }

    /**
     * Insures that a Person who has permissions to deposit to the
     * {@link #dataFileOne}s {@link #collectionWithData} can retrieve the
     * {@link #dataFileOne}
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataFileUnauthorizedUser() throws Exception {
        assertTrue(underTest.canRetrieveDataFile(unauthorizedPerson,
                                                 dataFileOne));
    }

    /**
     * Insures correct behavior when a null data file is passed in.
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSetNullDataFile() throws Exception {
        assertFalse(underTest.canRetrieveDataFile(instanceAdmin, null));
    }

    /**
     * Insures correct behavior when a null user and data file are passed in.
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataFileNullDataSetAndPerson() throws Exception {
        assertFalse(underTest.canRetrieveDataFile(null, null));
    }

    /**
     * Insures correct behavior when a null Collection is passed in
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveCollectionNullCollection() throws Exception {
        assertFalse(underTest.canRetrieveCollection(instanceAdmin, null));
    }

    /**
     * Insures correct behavior when a null Person is passed in. Expects that
     * null user CAN retrieve collection
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveCollectionNullPerson() throws Exception {
        assertTrue(underTest.canRetrieveCollection(null, collectionWithData));
    }

    /**
     * Insures correct behavior when a null user and a null collection are
     * passed in
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveCollectionNullPersonAndCollection()
            throws Exception {
        assertFalse(underTest.canRetrieveCollection(null, null));
    }

    /**
     * Insures that an InstanceAdmin can retrieve a Collection
     * 
     * @throws Exception
     */
    @Test
    public void testInstanceAdminCanRetrieveCollection() throws Exception {
        assertTrue(underTest.canRetrieveCollection(instanceAdmin,
                collectionWithData));
    }

    /**
     * Insures that a Project Admin can retrieve a Collection in the Project
     * 
     * @throws Exception
     */
    @Test
    public void testProjectAdminCanRetrieveCollection() throws Exception {
        assertTrue(underTest.canRetrieveCollection(projectAdmin,
                                                   collectionWithData));
    }

    /**
     * Insures that a Collection depositor can retrieve the collection
     * 
     * @throws Exception
     */
    @Test
    public void testCollectionDepositorCanRetrieveCollection() throws Exception {
        assertTrue(underTest.canRetrieveCollection(collectionDepositor,
                                                   collectionWithData));
    }

    /**
     * Insures that an unauthorized user cannot retrieve a collection.
     * 
     * @throws Exception
     */
    @Test
    public void testUnauthorizedUserCanRetrieveCollection() throws Exception {
        assertTrue(underTest.canRetrieveCollection(unauthorizedPerson,
                                                   collectionWithData));
    }

    /**
     * Tests that an instance admin can view the collection list.
     */
    @Test
    public void testInstanceAdminCanViewCollectionList() {
        assertTrue(underTest.canViewCollectionList(instanceAdmin));
    }

    /**
     * Tests that an project admin can view the collection list.
     */
    @Test
    public void testProjectAdminCanViewCollectionList() {
        assertTrue(underTest.canViewCollectionList(projectAdmin));
    }

    /**
     * Tests that an collection depositor can view the collection list.
     */
    @Test
    public void testCollectionDepositorCanViewCollectionList() {
        assertTrue(underTest.canViewCollectionList(collectionDepositor));
    }

    /**
     * Tests that an unauthorized person can view the collection list.
     */
    @Test
    public void testUnAuthorizedPersonCanViewCollectionList() {
        assertTrue(underTest.canViewCollectionList(unauthorizedPerson));
    }

    /**
     * Tests that an unlogged in user can view the collection list.
     */
    @Test
    public void testNotLoggedInUserCanViewCollectionList() {
        assertTrue(underTest.canViewCollectionList(null));
    }

    /**
     * Tests that an instance admin can update a collection.
     */
    @Test
    public void testInstanceAdminCanUpdateCollection()
            throws RelationshipConstraintException {
        assertTrue(underTest.canUpdateCollection(instanceAdmin,
                collectionWithData));
    }

    /**
     * Tests that an project admin can update a collection.
     */
    @Test
    public void testProjectAdminCanUpdateCollection()
            throws RelationshipConstraintException {
        assertTrue(underTest.canUpdateCollection(projectAdmin,
                                                 collectionWithData));
    }

    /**
     * Tests that a collection depositor cannot update the collection.
     */
    @Test
    public void testCollectionDepositorCanUpdateCollection()
            throws RelationshipConstraintException {
        assertFalse(underTest.canUpdateCollection(collectionDepositor,
                collectionWithData));
    }

    /**
     * Tests that an unauthorized person cannot update a collection.
     */
    @Test
    public void testUnAuthorizedPersonCanUpdateCollection()
            throws RelationshipConstraintException {
        assertFalse(underTest.canUpdateCollection(unauthorizedPerson,
                                                  collectionWithData));
    }

    /**
     * Tests that an unlogged in user cannot update a collection.
     */
    @Test
    public void testNotLoggedInUserCanUpdateCollection()
            throws RelationshipConstraintException {
        assertFalse(underTest.canUpdateCollection(null, collectionWithData));
    }

    /**
     * Tests that an instance admin can remove everyone on the project, except
     * for themselves.
     */
    @Test
    public void testInstanceAdminCanRemoveEveryone()
            throws RelationshipConstraintException {
        assertTrue(underTest.canRemoveDepositor(instanceAdmin,
                                                projectAdmin,
                                                collectionWithData));
        assertTrue(underTest.canRemoveDepositor(instanceAdmin,
                                                authorizedDepositor,
                                                collectionWithData));
        assertTrue(underTest.canRemoveDepositor(instanceAdmin,
                                                collectionDepositor,
                                                collectionWithData));
        assertFalse(underTest.canRemoveDepositor(instanceAdmin,
                instanceAdmin,
                collectionWithData));
    }

    /**
     * Tests that a project admin can only remove depositors
     */
    @Test
    public void testProjectAdminCanRemoveDepositors()
            throws RelationshipConstraintException {
        assertFalse(underTest.canRemoveDepositor(projectAdmin,
                                                 instanceAdmin,
                                                 collectionWithData));
        assertTrue(underTest.canRemoveDepositor(projectAdmin,
                                                collectionDepositor,
                                                collectionWithData));
        assertTrue(underTest.canRemoveDepositor(projectAdmin,
                                                authorizedDepositor,
                                                collectionWithData));
        assertFalse(underTest.canRemoveDepositor(projectAdmin,
                projectAdmin,
                collectionWithData));
        assertFalse(underTest.canRemoveDepositor(projectAdmin,
                                                 secondProjectAdmin,
                                                 collectionWithData));
    }

    /**
     * Tests that a depositor can't remove anyone
     */
    @Test
    public void testDepositorCanNotRemoveAnyone()
            throws RelationshipConstraintException {
        assertFalse(underTest.canRemoveDepositor(authorizedDepositor,
                instanceAdmin,
                collectionWithData));
        assertFalse(underTest.canRemoveDepositor(authorizedDepositor,
                projectAdmin,
                collectionWithData));
        assertFalse(underTest.canRemoveDepositor(authorizedDepositor,
                                                 collectionDepositor,
                                                 collectionWithData));
        assertFalse(underTest.canRemoveDepositor(authorizedDepositor,
                                                 authorizedDepositor,
                                                 collectionWithData));
    }

    /**
     * Tests that a regular user can't remove anyone
     */
    @Test
    public void testUserCanNotRemoveAnyone()
            throws RelationshipConstraintException {
        assertFalse(underTest.canRemoveDepositor(unauthorizedPerson,
                                                 instanceAdmin,
                                                 collectionWithData));
        assertFalse(underTest.canRemoveDepositor(unauthorizedPerson,
                                                 projectAdmin,
                                                 collectionWithData));
        assertFalse(underTest.canRemoveDepositor(unauthorizedPerson,
                                                 collectionDepositor,
                                                 collectionWithData));
        assertFalse(underTest.canRemoveDepositor(unauthorizedPerson,
                                                 authorizedDepositor,
                                                 collectionWithData));
    }

    /**
     * Tests that an instance admin can remove everyone on the project, except
     * for themselves.
     */
    @Test
    public void testInstanceAdminCanRemoveAdmins()
            throws RelationshipConstraintException {
        assertTrue(underTest.canRemoveAdmin(instanceAdmin,
                projectAdmin,
                projectOne));
        assertFalse(underTest.canRemoveAdmin(instanceAdmin,
                instanceAdmin,
                projectOne));
    }

    /**
     * Tests that a project admin can't remove any other admins
     */
    @Test
    public void testProjectAdminCanNotRemoveAdmins()
            throws RelationshipConstraintException {
        assertFalse(underTest.canRemoveAdmin(projectAdmin,
                projectAdmin,
                projectOne));
        assertFalse(underTest.canRemoveAdmin(projectAdmin,
                                             instanceAdmin,
                                             projectOne));
    }

    /**
     * Test that Project Admin can create collection under the project he/she administers
     */
    @Test
    public void testProjectAdminCanCreateCollection() {
        assertTrue(underTest.canCreateCollection(projectAdmin, projectOne));
    }

    /**
     * Test that Project Admin cannot create collection under the project he/she does not administer
     */
    @Test
    public void testProjectAdminCannotCreateCollection() {
        assertFalse(underTest.canCreateCollection(projectAdmin, projectTwo));
    }


    /**
     * Test that an instance admin can create sub collection anywhere
     */
    @Test
    public void testInstanceAdminCanCreateCollection() throws RelationshipConstraintException {
        assertTrue(underTest.canCreateCollection(instanceAdmin, collectionNoData));
        assertTrue(underTest.canCreateCollection(instanceAdmin, projectOne));
        assertTrue(underTest.canCreateCollection(instanceAdmin, projectTwo));
    }

    /**
     * Test that a collection depositor can create sub collection under the collection to which he/she can deposit
     */
    @Test
    public void testCollectionDepositorCanCreateSubCollection() throws RelationshipConstraintException {
        assertTrue(underTest.canCreateCollection(collectionDepositor,collectionWithData));
        verify(relService, times(1)).getDepositorsForCollection(collectionWithData.getId());
    }

    /**
     * Test that an instance admin can deposit into any collection
     */
    @Test
    public void testInstanceAdminCanDepositToCollection() throws RelationshipConstraintException {
        assertTrue(underTest.canDepositToCollection(instanceAdmin, collectionNoData));
        assertTrue(underTest.canDepositToCollection(instanceAdmin, collectionWithData));
    }

    /**
     * test that collection depositor can deposit to the collection he/she is assigned as depositor for.
     */
    @Test
    public void testCollectionDepositorCanDepositToCollection() throws RelationshipConstraintException {
        assertTrue(underTest.canDepositToCollection(collectionDepositor, collectionWithData));
    }

    /**
     * Test that collection depositor cannot deposit to a collection, are which he/she is NOT a depositor.
     */
    @Test
    public void testCollectionDepositorCannotDepositToCollection() throws RelationshipConstraintException {
        assertFalse(underTest.canDepositToCollection(collectionDepositor, collectionNoData));

    }

    @Test
    public void testLowerLevelCollectionDepositorCannotDepositToHigherLevelCollection() throws RelationshipConstraintException {
        assertFalse(underTest.canDepositToCollection(subCollection2Depositor, subCollection));
        assertFalse(underTest.canDepositToCollection(subCollection2Depositor, collectionWithData));
    }

    @Test
    public void testHigherLevelCollectionDepositorCanDepositToLowerLevelCollection() throws RelationshipConstraintException {
        assertTrue(underTest.canDepositToCollection(collectionDepositor, subCollection));
        assertTrue(underTest.canDepositToCollection(collectionDepositor, subCollection2));
        assertTrue(underTest.canDepositToCollection(subCollectionDepositor, subCollection2));
    }


    @Test
    public void testProjectAdminCanDepositToSubCollection() throws RelationshipConstraintException {
        assertTrue(underTest.canDepositToCollection(projectAdmin, subCollection));
        assertTrue(underTest.canDepositToCollection(projectAdmin, subCollection2));
    }
}

