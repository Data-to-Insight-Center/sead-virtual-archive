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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test methods provided by
 * {@link org.dataconservancy.ui.services.AuthorizationService}
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase
public class AuthorizationServiceTest
        extends BaseUnitTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RelationshipService relService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private AuthorizationService authService;

    private Person instanceAdmin;

    private Person pi;

    private Person secondProjectPi;

    private Person depositor;

    private Person randomPerson;

    //counter for how many times we have tried to poll the archive
    private int pollCount;

    //maximum number of times to poll
    private final int maxPollTimes = 60;

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

        //Set up people
        instanceAdmin = new Person();
        instanceAdmin.setId("id:instanceAdmin");
        instanceAdmin.setEmailAddress("instanceAdmin@cows.com");
        instanceAdmin.setFirstNames("Admin");
        instanceAdmin.setLastNames("Cow");
        instanceAdmin.setPrefix("Mr.");
        instanceAdmin.setSuffix("II");
        instanceAdmin.setMiddleNames("Middle");
        instanceAdmin.setPreferredPubName("A. Cow");
        instanceAdmin.setBio("Some bio.");
        instanceAdmin.setWebsite("www.website.com");
        instanceAdmin.setRegistrationStatus(RegistrationStatus.APPROVED);
        instanceAdmin.setPhoneNumber("323333333");
        instanceAdmin.setPassword("password");
        instanceAdmin.setJobTitle("Instance Scientist");
        instanceAdmin.setDepartment("Instance Department");
        instanceAdmin.setCity("Baltimore");
        instanceAdmin.setState("Maryland");
        instanceAdmin.setInstCompany("Instance Institution/Company");
        instanceAdmin.setInstCompanyWebsite("www.InstanceInstitutionCompany.com");
        instanceAdmin.setExternalStorageLinked(false);
        instanceAdmin.setDropboxAppKey("SomeKey");
        instanceAdmin.setDropboxAppSecret("SomeSecret");

        instanceAdmin = userService.create(instanceAdmin);
        List<Role> roles = instanceAdmin.getRoles();
        roles.add(Role.ROLE_ADMIN);
        userService.updateRoles(instanceAdmin.getId(), roles);

        pi = new Person();
        pi.setId("id:pieisgreat");
        pi.setEmailAddress("pieisgreat@cows.com");
        pi.setFirstNames("Cud");
        pi.setLastNames("Cow");
        pi.setPrefix("Mr.");
        pi.setSuffix("II");
        pi.setMiddleNames("Middle");
        pi.setPreferredPubName("C. Cow");
        pi.setBio("Some bio.");
        pi.setWebsite("www.website.com");
        pi.setRegistrationStatus(RegistrationStatus.APPROVED);
        pi.setPhoneNumber("323333333");
        pi.setPassword("password");
        pi.setJobTitle("Pi Scientist");
        pi.setDepartment("Pi Department");
        pi.setCity("Baltimore");
        pi.setState("Maryland");
        pi.setInstCompany("Pi Institution/Company");
        pi.setInstCompanyWebsite("www.PiInstitutionCompany.com");
        pi.setExternalStorageLinked(false);
        pi.setDropboxAppKey("SomeKey");
        pi.setDropboxAppSecret("SomeSecret");
        pi = userService.create(pi);

        secondProjectPi = new Person();
        secondProjectPi.setId("id:1241351");
        secondProjectPi.setEmailAddress("secondPi@cows.com");
        secondProjectPi.setFirstNames("Second");
        secondProjectPi.setLastNames("PI");
        secondProjectPi.setPrefix("Mr.");
        secondProjectPi.setSuffix("II");
        secondProjectPi.setMiddleNames("Middle");
        secondProjectPi.setPreferredPubName("S. Pi");
        secondProjectPi.setBio("Some bio.");
        secondProjectPi.setWebsite("www.website.com");
        secondProjectPi.setRegistrationStatus(RegistrationStatus.APPROVED);
        secondProjectPi.setPhoneNumber("1234567890");
        secondProjectPi.setPassword("blahblah");
        secondProjectPi.setJobTitle("Pi Scientist");
        secondProjectPi.setDepartment("Pi Department");
        secondProjectPi.setCity("Baltimore");
        secondProjectPi.setState("Maryland");
        secondProjectPi.setInstCompany("Pi Institution/Company");
        secondProjectPi.setInstCompanyWebsite("www.PiInstitutionCompany.com");
        secondProjectPi.addRole(Role.ROLE_ADMIN);
        secondProjectPi.setExternalStorageLinked(false);
        secondProjectPi.setDropboxAppKey("SomeKey");
        secondProjectPi.setDropboxAppSecret("SomeSecret");
        secondProjectPi = userService.create(secondProjectPi);

        randomPerson = new Person();
        randomPerson.setId("id:randonPerson");
        randomPerson.setEmailAddress("randomPerson@cows.com");
        randomPerson.setFirstNames("Random");
        randomPerson.setLastNames("Cow");
        randomPerson.setPrefix("Mr.");
        randomPerson.setSuffix("II");
        randomPerson.setMiddleNames("Middle");
        randomPerson.setPreferredPubName("R. Cow");
        randomPerson.setBio("Some bio.");
        randomPerson.setWebsite("www.website.com");
        randomPerson.setRegistrationStatus(RegistrationStatus.APPROVED);
        randomPerson.setPhoneNumber("323333333");
        randomPerson.setPassword("password");
        randomPerson.setJobTitle("Random Scientist");
        randomPerson.setDepartment("Random Department");
        randomPerson.setCity("Baltimore");
        randomPerson.setState("Maryland");
        randomPerson.setInstCompany("Random Institution/Company");
        randomPerson.setInstCompanyWebsite("www.RandomInstitutionCompany.com");
        randomPerson.setExternalStorageLinked(false);
        randomPerson.setDropboxAppKey("SomeKey");
        randomPerson.setDropboxAppSecret("SomeSecret");
        randomPerson = userService.create(randomPerson);

        depositor = new Person();
        depositor.setId("id:depositorPerson");
        depositor.setEmailAddress("depositor@cows.com");
        depositor.setFirstNames("Dee");
        depositor.setLastNames("Positor");
        depositor.setPrefix("Mr.");
        depositor.setSuffix("II");
        depositor.setMiddleNames("Middle");
        depositor.setPreferredPubName("D. Positor");
        depositor.setBio("Some bio.");
        depositor.setWebsite("www.website.com");
        depositor.setRegistrationStatus(RegistrationStatus.APPROVED);
        depositor.setPhoneNumber("8675309");
        depositor.setPassword("password");
        depositor.setJobTitle("Depositor Scientist");
        depositor.setDepartment("Depositor Department");
        depositor.setCity("Baltimore");
        depositor.setState("Maryland");
        depositor.setInstCompany("Depositor Institution/Company");
        depositor.setInstCompanyWebsite("www.DepositorInstitutionCompany.com");
        depositor.setExternalStorageLinked(false);
        depositor.setDropboxAppKey("SomeKey");
        depositor.setDropboxAppSecret("SomeSecret");
        depositor = userService.create(depositor);

        //Set up projects
        projectOne.addPi(pi.getId());
        projectOne.addPi(secondProjectPi.getId());

        projectTwo.addPi(pi.getId());
        projectTwo.addPi(secondProjectPi.getId());

        relService.addAdministratorToProject(projectTwo, instanceAdmin);
        relService.addAdministratorToProject(projectTwo, pi);
        relService.addAdministratorToProject(projectTwo, secondProjectPi);

        relService.addAdministratorToProject(projectOne, instanceAdmin);
        relService.addAdministratorToProject(projectOne, pi);
        relService.addAdministratorToProject(projectOne, secondProjectPi);

    }

    /**
     * Tests that an Instance Admin may create a {@code Project}.
     */
    @Test
    public void testCanCreateByInstanceAdmin() {
        assertTrue(authService.canCreateProject(instanceAdmin));
    }

    /**
     * Tests that any user who is not an Instance Admin may noy create a
     * {@code Project}.
     */
    @Test
    public void testCanCreateByNonInstanceAdmin() {
        assertFalse(authService.canCreateProject(pi));
        assertFalse(authService.canCreateProject(randomPerson));
    }

    /**
     * Tests that an Instance Admin and a Project Admin may update a
     * {@code Project}. Tests that any other user may not.
     */
    @Test
    public void testCanUpdateProject() {
        assertTrue(authService.canUpdateProject(pi, projectTwo));
        assertTrue(authService.canUpdateProject(instanceAdmin, projectTwo));
        assertFalse(authService.canUpdateProject(randomPerson, projectTwo));
    }

    /**
     * Tests that an Instance Admin and a Project Admin may view a
     * representation of a {@code Project}. Tests that any other user may not.
     */
    @Test
    public void testCanReadProject() {
        assertTrue(authService.canReadProject(pi, projectTwo));
        assertTrue(authService.canReadProject(instanceAdmin, projectTwo));
        assertFalse(authService.canReadProject(randomPerson, projectTwo));
    }

    /**
     * Tests that an Instance Admin and a Project Admin may view
     * {@code Collection}s in the {@code Project}. Tests that any other user may
     * not.
     */
    @Test
    public void testCanViewProjectCollections() {
        assertTrue(authService.canRetrieveProjectCollections(pi, projectTwo));
        assertTrue(authService.canRetrieveProjectCollections(instanceAdmin,
                                                             projectTwo));
        assertFalse(authService.canRetrieveProjectCollections(randomPerson,
                                                              projectTwo));
    }

    /**
     * Tests that an Instance Admin, a Project Admin or a depositor to a
     * {@code collection} may retrieve the {@code Collection}. Tests that any
     * other user may not.
     */
    @Test
    public void testCanRetrieveCollection() throws Exception {

        // Add the Collection to the Project
        relService.addCollectionToProject(collectionNoData, projectTwo);

        // Add the Depositor to the Collection
        relService.addDepositorToCollection(depositor, collectionNoData);

        assertTrue(authService.canRetrieveCollection(pi, collectionNoData));
        assertTrue(authService.canRetrieveCollection(instanceAdmin,
                                                     collectionNoData));
        assertTrue(authService.canRetrieveCollection(depositor,
                                                     collectionNoData));
        assertTrue(authService.canRetrieveCollection(randomPerson,
                                                     collectionNoData));
    }

    /**
     * Tests that an Instance Admin, Project Admin and Collection Depositor may
     * retrieve a {@code DataFile} if the {@code DataFile} belongs to the
     * appropriate {@code Collection} and {@code Project}. Tests that a random
     * user and a user represented by a {@code Person} object not in the system
     * still have permission to retrieve the {@code DataFile}.
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataFile() throws Exception {

        //Create an invalid user - do not add via user service
        Person invalid = new Person();
        invalid.setId("id:invalidPerson");
        invalid.setEmailAddress("invalid@cows.com");
        invalid.setFirstNames("IN");
        invalid.setLastNames("Valid");
        invalid.setPrefix("Mr.");
        invalid.setSuffix("II");
        invalid.setMiddleNames("Middle");
        invalid.setPreferredPubName("I. Valid");
        invalid.setBio("Some bio.");
        invalid.setWebsite("www.website.com");
        invalid.setRegistrationStatus(RegistrationStatus.APPROVED);
        invalid.setPhoneNumber("8675221309");
        invalid.setPassword("password");
        invalid.setJobTitle("Invalid Scientist");
        invalid.setDepartment("Invalid Department");
        invalid.setCity("Baltimore");
        invalid.setState("Maryland");
        invalid.setInstCompany("Invalid Institution/Company");
        invalid.setInstCompanyWebsite("www.InvalidInstitutionCompany.com");
        invalid.setExternalStorageLinked(false);
        invalid.setDropboxAppKey("SomeKey");
        invalid.setDropboxAppSecret("SomeSecret");

        relService.removeCollectionFromProject(collectionWithData, projectOne);
        relService.addCollectionToProject(collectionWithData, projectTwo);
        relService.addDepositorToCollection(depositor, collectionWithData);

        String col_dep_id = archiveService.deposit(collectionWithData);
        archiveService.pollArchive();
        archiveService.deposit(col_dep_id, dataItemOne);
        archiveService.pollArchive();

        assertTrue(authService.canRetrieveDataFile(instanceAdmin, dataFileOne));
        assertTrue(authService.canRetrieveDataFile(pi, dataFileOne));
        assertTrue(authService.canRetrieveDataFile(depositor, dataFileOne));
        assertTrue(authService.canRetrieveDataFile(randomPerson, dataFileOne));
        assertTrue(authService.canRetrieveDataFile(invalid, dataFileOne));
    }

    /**
     * This test insures that:
     * <ul>
     * <li>Blacklisted users cannot retrieve a dataset</li>
     * <li>Pending users cannot retrieve a dataset</li>
     * <li>Users that are approved but are not a Collection depositor or Project
     * admin cannot retrieve a dataset</li>
     * <li>Instance admins can retrieve a dataset</li>
     * <li>Admins of the containing project can retrieve a dataset</li>
     * <li>Depositors of the containing collection can retrieve a dataset</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testCanRetrieveDataSet() throws Exception {

        Person blacklistedPerson;
        Person pendingPerson;
        Person unauthorizedPerson;

        // Create three persons that shouldn't be authorized to retrieve the DataItem

        // This person is Blacklisted, so they shouldn't be able to retrieve a DataItem
        blacklistedPerson = new Person();
        blacklistedPerson.setId("http://foo.bar/person/1234");
        blacklistedPerson.setFirstNames("Blacklisted");
        blacklistedPerson.setLastNames("Person");
        blacklistedPerson.setPrefix("Mr.");
        blacklistedPerson.setSuffix("II");
        blacklistedPerson.setMiddleNames("Middle");
        blacklistedPerson.setPreferredPubName("B. Person");
        blacklistedPerson.setBio("Some bio.");
        blacklistedPerson.setWebsite("www.website.com");
        blacklistedPerson.setJobTitle("Invalid Scientist");
        blacklistedPerson.setDepartment("Invalid Department");
        blacklistedPerson.setCity("Baltimore");
        blacklistedPerson.setState("Maryland");
        blacklistedPerson.setInstCompany("Invalid Institution/Company");
        blacklistedPerson.setInstCompanyWebsite("www.InvalidInstitutionCompany.com");
        blacklistedPerson.setEmailAddress("blacklisted@foo.com");
        blacklistedPerson.setPassword("foobarbaz");
        blacklistedPerson.setPhoneNumber("1234567890");
        blacklistedPerson.setExternalStorageLinked(false);
        blacklistedPerson.setDropboxAppKey("SomeKey");
        blacklistedPerson.setDropboxAppSecret("SomeSecret");
        userService.create(blacklistedPerson);
        userService.updateRegistrationStatus(blacklistedPerson.getId(),
                                             RegistrationStatus.BLACK_LISTED);
        userService.updateRoles(blacklistedPerson.getId(),
                                Collections.<Role> emptyList());
        blacklistedPerson = userService.get(blacklistedPerson.getId());
        assertTrue(blacklistedPerson.getRoles().isEmpty());
        assertEquals(RegistrationStatus.BLACK_LISTED,
                     blacklistedPerson.getRegistrationStatus());

        // This person is Pending, so they shouldn't be able to retrieve a DataItem
        pendingPerson = new Person();
        pendingPerson.setId("http://foo.bar/person/5678");
        pendingPerson.setFirstNames("Pending");
        pendingPerson.setLastNames("Person");
        pendingPerson.setEmailAddress("pending@foo.com");
        pendingPerson.setPassword("foobarbaz");
        pendingPerson.setPhoneNumber("1234567890");
        pendingPerson.setPrefix("Mr.");
        pendingPerson.setSuffix("II");
        pendingPerson.setMiddleNames("Middle");
        pendingPerson.setPreferredPubName("P. Person");
        pendingPerson.setBio("Some bio.");
        pendingPerson.setWebsite("www.website.com");
        pendingPerson.setJobTitle("Invalid Scientist");
        pendingPerson.setDepartment("Invalid Department");
        pendingPerson.setCity("Baltimore");
        pendingPerson.setState("Maryland");
        pendingPerson.setInstCompany("Invalid Institution/Company");
        pendingPerson.setInstCompanyWebsite("www.InvalidInstitutionCompany.com");
        pendingPerson.setExternalStorageLinked(false);
        pendingPerson.setDropboxAppKey("SomeKey");
        pendingPerson.setDropboxAppSecret("SomeSecret");
        userService.create(pendingPerson);
        userService.updateRegistrationStatus(pendingPerson.getId(),
                                             RegistrationStatus.PENDING);
        userService.updateRoles(pendingPerson.getId(),
                                Collections.<Role> emptyList());
        pendingPerson = userService.get(pendingPerson.getId());
        assertTrue(pendingPerson.getRoles().isEmpty());
        assertEquals(RegistrationStatus.PENDING,
                     pendingPerson.getRegistrationStatus());

        // This is an approved, registered Person, but they aren't an Instance Admin, Project Admin, or Collection
        // Depositor, so they shouldn't be able to retrieve the DataItem either.
        unauthorizedPerson = new Person();
        unauthorizedPerson.setId("http://foo.bar/person/2468");
        unauthorizedPerson.setFirstNames("Unauthorized");
        unauthorizedPerson.setLastNames("Person");
        unauthorizedPerson.setEmailAddress("unauthorized@foo.com");
        unauthorizedPerson.setPassword("foobarbaz");
        unauthorizedPerson.setPhoneNumber("1234567890");
        unauthorizedPerson.setPrefix("Mr.");
        unauthorizedPerson.setSuffix("II");
        unauthorizedPerson.setMiddleNames("Middle");
        unauthorizedPerson.setPreferredPubName("U. Person");
        unauthorizedPerson.setBio("Some bio.");
        unauthorizedPerson.setWebsite("www.website.com");
        unauthorizedPerson.setJobTitle("Invalid Scientist");
        unauthorizedPerson.setDepartment("Invalid Department");
        unauthorizedPerson.setCity("Baltimore");
        unauthorizedPerson.setState("Maryland");
        unauthorizedPerson.setInstCompany("Invalid Institution/Company");
        unauthorizedPerson.setInstCompany("www.InvalidInstitutionCompany.com");
        userService.create(unauthorizedPerson);
        userService.updateRegistrationStatus(unauthorizedPerson.getId(),
                                             RegistrationStatus.APPROVED);
        userService.updateRoles(unauthorizedPerson.getId(),
                                Arrays.asList(Role.ROLE_USER));
        unauthorizedPerson = userService.get(unauthorizedPerson.getId());
        assertTrue(unauthorizedPerson.getRoles().contains(Role.ROLE_USER));
        assertEquals(RegistrationStatus.APPROVED,
                     unauthorizedPerson.getRegistrationStatus());

        // Create the Collection and DataItem used in this test, create the necessary relationships, and deposit
        // the Collection in the archive.
        collectionWithData.setDepositorId(depositor.getId());

        // Add the Collection to the Project
        relService.removeCollectionFromProject(collectionWithData, projectOne);
        relService.addCollectionToProject(collectionWithData, projectTwo);

        // Add the Depositor to the Collection
       
        relService.addDepositorToCollection(userService.get(collectionWithData.getDepositorId()),
                                            collectionWithData);

        dataItemOne.setDepositorId(depositor.getId());

        // Deposit the Collection in the Archive
        String col_dep_id = archiveService.deposit(collectionWithData);
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        } while (archiveService.getDepositStatus(col_dep_id) != ArchiveDepositInfo.Status.DEPOSITED
                && pollCount++ < maxPollTimes);

        assertTrue(authService.canRetrieveDataSet(instanceAdmin, dataItemOne));
        assertTrue(authService.canRetrieveDataSet(pi, dataItemOne));
        assertTrue(authService.canRetrieveDataSet(depositor, dataItemOne));
        assertTrue(authService.canRetrieveDataSet(randomPerson, dataItemOne));
        assertTrue(authService.canRetrieveDataSet(unauthorizedPerson,
                                                  dataItemOne));
        assertTrue(authService.canRetrieveDataSet(blacklistedPerson,
                                                  dataItemOne));
        assertTrue(authService.canRetrieveDataSet(pendingPerson, dataItemOne));
        assertTrue(authService.canRetrieveDataSet(null, dataItemOne));
    }

    /**
     * Tests that every user can view the collection list since it's publicly
     * available.
     */
    @Test
    public void testCanViewCollectionList() {
        assertTrue(authService.canViewCollectionList(instanceAdmin));
        assertTrue(authService.canViewCollectionList(pi));
        assertTrue(authService.canViewCollectionList(depositor));
        assertTrue(authService.canViewCollectionList(randomPerson));
        assertTrue(authService.canViewCollectionList(null));
    }

    /**
     * Test that only instance admins and adminsfor the containing project can
     * update collections
     * 
     * @throws RelationshipConstraintException
     */
    @Test
    public void testCanUpdateCollection()
            throws RelationshipConstraintException {

        //add collection to project
        relService.addCollectionToProject(collectionNoData, projectOne);

        assertTrue(authService.canUpdateCollection(instanceAdmin,
                                                   collectionNoData));
        assertTrue(authService.canUpdateCollection(pi, collectionNoData));
        assertFalse(authService
                .canUpdateCollection(depositor, collectionNoData));
        assertFalse(authService.canUpdateCollection(randomPerson,
                                                    collectionNoData));
    }

    /**
     * Tests that an instance admin can remove everyone on the project, except
     * for themselves.
     */
    @Test
    public void testInstanceAdminCanRemoveEveryone()
            throws RelationshipConstraintException {

        relService.addCollectionToProject(collectionNoData, projectOne);
        assertTrue(authService.canRemoveDepositor(instanceAdmin,
                                                  pi,
                                                  collectionNoData));
        assertTrue(authService.canRemoveDepositor(instanceAdmin,
                                                  depositor,
                                                  collectionNoData));
        assertFalse(authService.canRemoveDepositor(instanceAdmin,
                                                   instanceAdmin,
                                                   collectionNoData));
    }

    /**
     * Tests that a project admin can only remove depositors
     */
    @Test
    public void testProjectAdminCanRemoveDepositors()
            throws RelationshipConstraintException {
        relService.addCollectionToProject(collectionNoData, projectOne);

        assertFalse(authService.canRemoveDepositor(pi,
                                                   instanceAdmin,
                                                   collectionNoData));
        assertTrue(authService.canRemoveDepositor(pi,
                                                  depositor,
                                                  collectionNoData));
        assertFalse(authService.canRemoveDepositor(pi, pi, collectionNoData));
        assertFalse(authService.canRemoveDepositor(pi,
                                                   secondProjectPi,
                                                   collectionNoData));
    }

    /**
     * Tests that a depositor can't remove anyone
     */
    @Test
    public void testDepositorCanNotRemoveAnyone()
            throws RelationshipConstraintException {
        relService.addCollectionToProject(collectionNoData, projectOne);

        assertFalse(authService.canRemoveDepositor(depositor,
                                                   instanceAdmin,
                                                   collectionNoData));
        assertFalse(authService.canRemoveDepositor(depositor,
                                                   pi,
                                                   collectionNoData));
        assertFalse(authService.canRemoveDepositor(depositor,
                                                   depositor,
                                                   collectionNoData));
    }

    /**
     * Tests that a normal user can't remove anyone
     */
    @Test
    public void testRandomUserCanNotRemoveAnyone()
            throws RelationshipConstraintException {

        relService.addCollectionToProject(collectionNoData, projectOne);

        assertFalse(authService.canRemoveDepositor(randomPerson,
                                                   instanceAdmin,
                                                   collectionNoData));
        assertFalse(authService.canRemoveDepositor(randomPerson,
                                                   pi,
                                                   collectionNoData));
        assertFalse(authService.canRemoveDepositor(randomPerson,
                                                   depositor,
                                                   collectionNoData));
    }

    /**
     * Test instance admin can remove any admins but themselves.
     * 
     * @throws RelationshipConstraintException
     */
    @Test
    public void testInstanceAdminCanRemoveAdmins()
            throws RelationshipConstraintException {
        assertFalse(authService.canRemoveAdmin(instanceAdmin,
                                               instanceAdmin,
                                               projectOne));
        assertTrue(authService.canRemoveAdmin(instanceAdmin, pi, projectOne));
    }

    /**
     * Test project admin can't remove other project admins
     * 
     * @throws RelationshipConstraintException
     */
    @Test
    public void testProjectAdminCanNotRemoveAnyone()
            throws RelationshipConstraintException {
        assertFalse(authService.canRemoveAdmin(pi, instanceAdmin, projectOne));
        assertFalse(authService.canRemoveAdmin(pi, pi, projectOne));
    }
}
