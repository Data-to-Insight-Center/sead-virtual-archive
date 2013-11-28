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

package org.dataconservancy.ui.stripes;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.dao.DaoSupport;
import org.dataconservancy.ui.model.CitationFormatter;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.CitationService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by IntelliJ IDEA. User: jrm Date: 11/4/11 Time: 2:56 PM To change
 * this template use File | Settings | File Templates.
 */
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserCollectionsActionBeanTest
        extends BaseActionBeanTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    @Autowired
    private RelationshipService relService;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Autowired
    private CitationService citationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Person user1;

    private Person user2;

    private Person user3;

    private Person user4;

    private Person user5;

    private Person projectPiInstanceAdmin;

    private Person projectPi;

    private final static String BAD_COLLECTION_ID = "badID";

    private static String[] MONTHS =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};

    private MockHttpSession userSession;

    private MockHttpSession adminSession;

    /**
     * Initialize the mock http session with authenticated user credentials.
     * Tests that re-use this mock session will be already logged in.
     */
    @Before
    public void setUpMockHttpSessions() throws Exception {
        userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  "/j_spring_security_check",
                                  userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        SecurityContext ctx =
                (SecurityContext) userSession
                        .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx
                .getAuthentication().getPrincipal()).getUsername());

        // Mock a session for a system-wide admin user
        adminSession = new MockHttpSession(servletCtx);
        rt =
                new MockRoundtrip(servletCtx,
                                  "/j_spring_security_check",
                                  adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        ctx =
                (SecurityContext) adminSession
                        .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx
                .getAuthentication().getPrincipal()).getUsername());
    }

    @Before
    public void setUpObjects() throws Exception {

        if (jdbcTemplate.queryForInt("SELECT count(*) from PERSON") > 0) {
            final StringBuilder msg =
                    new StringBuilder("Expected no Persons in the Persons table: ");
            msg.append(DaoSupport.dumpTable(jdbcTemplate, "PERSON"));

            fail(msg.toString());
        }

        assertEquals(0, jdbcTemplate.queryForInt("SELECT count(*) from PERSON"));

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

        user1 = new Person();
        user1.setId("id:foo");
        user1.setEmailAddress("user1@test.org");
        user1.setFirstNames("User");
        user1.setLastNames("One");
        user1.setPrefix("Mr.");
        user1.setSuffix("II");
        user1.setMiddleNames("Middle");
        user1.setPreferredPubName("U. One");
        user1.setBio("Some bio for the user.");
        user1.setWebsite("www.somewebsite.com");
        user1.setPassword("password1");
        user1.setPhoneNumber("5550000001");
        user1.setJobTitle("User Scientist");
        user1.setDepartment("User Department");
        user1.setCity("Baltimore");
        user1.setState("Maryland");
        user1.setInstCompany("User Institution/Company");
        user1.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user1.setRegistrationStatus(RegistrationStatus.APPROVED);
        user1.setExternalStorageLinked(false);
        user1.setDropboxAppKey("SomeKey");
        user1.setDropboxAppSecret("SomeSecret");

        userService.create(user1);

        user2 = new Person();
        user2.setId("id:bar");
        user2.setEmailAddress("user2@test.org");
        user2.setFirstNames("User");
        user2.setLastNames("Two");
        user2.setPrefix("Mr.");
        user2.setSuffix("II");
        user2.setMiddleNames("Middle");
        user2.setPreferredPubName("U. Two");
        user2.setBio("Some bio for the user.");
        user2.setWebsite("www.somewebsite.com");
        user2.setPassword("password2");
        user2.setPhoneNumber("5550000002");
        user2.setJobTitle("User Scientist");
        user2.setDepartment("User Department");
        user2.setCity("Baltimore");
        user2.setState("Maryland");
        user2.setInstCompany("User Institution/Company");
        user2.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user2.setRegistrationStatus(RegistrationStatus.APPROVED);
        user2.setExternalStorageLinked(false);
        user2.setDropboxAppKey("SomeKey");
        user2.setDropboxAppSecret("SomeSecret");

        userService.create(user2);

        user3 = new Person();
        user3.setId("id:baz");
        user3.setEmailAddress("user3@test.org");
        user3.setFirstNames("User");
        user3.setLastNames("Three");
        user3.setPrefix("Mr.");
        user3.setSuffix("II");
        user3.setMiddleNames("Middle");
        user3.setPreferredPubName("U. Three");
        user3.setBio("Some bio for the user.");
        user3.setWebsite("www.somewebsite.com");
        user3.setPassword("password3");
        user3.setPhoneNumber("5550000003");
        user3.setJobTitle("User Scientist");
        user3.setDepartment("User Department");
        user3.setCity("Baltimore");
        user3.setState("Maryland");
        user3.setInstCompany("User Institution/Company");
        user3.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user3.setRegistrationStatus(RegistrationStatus.PENDING);
        user3.setExternalStorageLinked(false);
        user3.setDropboxAppKey("SomeKey");
        user3.setDropboxAppSecret("SomeSecret");

        userService.create(user3);

        user4 = new Person();
        user4.setId("id:user4");
        user4.setEmailAddress("user4@test.org");
        user4.setFirstNames("User");
        user4.setLastNames("Four");
        user4.setPrefix("Mr.");
        user4.setSuffix("II");
        user4.setMiddleNames("Middle");
        user4.setPreferredPubName("U. Four");
        user4.setBio("Some bio for the user.");
        user4.setWebsite("www.somewebsite.com");
        user4.setPassword("password4");
        user4.setPhoneNumber("5550000004");
        user4.setJobTitle("User Scientist");
        user4.setDepartment("User Department");
        user4.setCity("Baltimore");
        user4.setState("Maryland");
        user4.setInstCompany("User Institution/Company");
        user4.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user4.setRegistrationStatus(RegistrationStatus.APPROVED);
        user4.setExternalStorageLinked(false);
        user4.setDropboxAppKey("SomeKey");
        user4.setDropboxAppSecret("SomeSecret");

        userService.create(user4);

        user5 = new Person();
        user5.setId("id:buz");
        user5.setEmailAddress("user5@test.org");
        user5.setFirstNames("User");
        user5.setLastNames("Five");
        user5.setPrefix("Mr.");
        user5.setSuffix("II");
        user5.setMiddleNames("Middle");
        user5.setPreferredPubName("U. Five");
        user5.setBio("Some bio for the user.");
        user5.setWebsite("www.somewebsite.com");
        user5.setPassword("password5");
        user5.setPhoneNumber("5550000005");
        user5.setJobTitle("User Scientist");
        user5.setDepartment("User Department");
        user5.setCity("Baltimore");
        user5.setState("Maryland");
        user5.setInstCompany("User Institution/Company");
        user5.setInstCompanyWebsite("www.UserInstitutionCompany.com");
        user5.setRegistrationStatus(RegistrationStatus.APPROVED);
        user5.setExternalStorageLinked(false);
        user5.setDropboxAppKey("SomeKey");
        user5.setDropboxAppSecret("SomeSecret");

        userService.create(user5);
        userService.updateRoles(user5.getId(),
                                Arrays.asList(Role.ROLE_USER, Role.ROLE_ADMIN));

        projectPiInstanceAdmin = new Person();
        projectPiInstanceAdmin.setId("id:piInstanceAdmin");
        projectPiInstanceAdmin.setEmailAddress("piInstanceAdmin@test.org");
        projectPiInstanceAdmin.setFirstNames("PI");
        projectPiInstanceAdmin.setLastNames("InstanceAdmin");
        projectPiInstanceAdmin.setPrefix("Mr.");
        projectPiInstanceAdmin.setSuffix("II");
        projectPiInstanceAdmin.setMiddleNames("Middle");
        projectPiInstanceAdmin.setPreferredPubName("P. InstanceAdmin");
        projectPiInstanceAdmin.setBio("Some bio for the user.");
        projectPiInstanceAdmin.setWebsite("www.somewebsite.com");
        projectPiInstanceAdmin.setPassword("foobar");
        projectPiInstanceAdmin.setPhoneNumber("5550000005");
        projectPiInstanceAdmin.setJobTitle("PI Scientist");
        projectPiInstanceAdmin.setDepartment("PI Department");
        projectPiInstanceAdmin.setCity("Baltimore");
        projectPiInstanceAdmin.setState("Maryland");
        projectPiInstanceAdmin.setInstCompany("PI Institution/Company");
        projectPiInstanceAdmin.setInstCompanyWebsite("www.PIInstitutionCompany.com");
        projectPiInstanceAdmin.setExternalStorageLinked(false);
        projectPiInstanceAdmin.setDropboxAppKey("SomeKey");
        projectPiInstanceAdmin.setDropboxAppSecret("SomeSecret");

        projectPiInstanceAdmin
                .setRegistrationStatus(RegistrationStatus.APPROVED);

        userService.create(projectPiInstanceAdmin);
        userService.updateRoles(projectPiInstanceAdmin.getId(), Arrays.asList(Role.ROLE_USER, Role.ROLE_ADMIN));

        projectPi = new Person();
        projectPi.setId("id:pi");
        projectPi.setEmailAddress("pi@test.org");
        projectPi.setFirstNames("PI");
        projectPi.setLastNames("Admin");
        projectPi.setPrefix("Mr.");
        projectPi.setSuffix("II");
        projectPi.setMiddleNames("Middle");
        projectPi.setPreferredPubName("P. Admin");
        projectPi.setBio("Some bio for the user.");
        projectPi.setWebsite("www.somewebsite.com");
        projectPi.setPassword("foobar");
        projectPi.setPhoneNumber("5550000005");
        projectPi.setJobTitle("PI Scientist");
        projectPi.setDepartment("PI Department");
        projectPi.setCity("Baltimore");
        projectPi.setState("Maryland");
        projectPi.setInstCompany("PI Institution/Company");
        projectPi.setInstCompanyWebsite("www.PIInstitutionCompany.com");
        projectPi.setRegistrationStatus(RegistrationStatus.APPROVED);
        projectPi.setExternalStorageLinked(false);
        projectPi.setDropboxAppKey("SomeKey");
        projectPi.setDropboxAppSecret("SomeSecret");

        userService.create(projectPi);
        userService.updateRoles(projectPi.getId(), Arrays.asList(Role.ROLE_USER));

        relService.addCollectionToProject(collectionNoData, projectOne);
    }

    /**
     * Asserts that the default handler is what we expect it to be
     */
    @Test
    public void testDefaultHandler() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  UserCollectionsActionBean.class,
                                  userSession);
        rt.execute();
        assertEquals(UserCollectionsActionBean.HOME_COLLECTIONS_PATH,
                     rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    /**
     * Asserts that correct JSP and ActionBean URL is used to edit depositors
     *
     * @throws Exception
     */
    @Test
    public void testEditDepositorsPath() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  UserCollectionsActionBean.class,
                                  userSession);
        rt.execute("editCollectionDepositors");
        assertEquals(UserCollectionsActionBean.EDIT_DEPOSITORS_PATH,
                     rt.getForwardUrl());
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    public void testEditDepositors() throws Exception {
        // start with user2, then remove user2 and add user1
        // attempt to add user3, which should fail (not APPROVED)
        // add user4 ( project admin) and user 5 (admin) then attempt to remove.
        // our implementation adds before removing

        relService.addDepositorToCollection(user2, collectionNoData);
        relService.addAdministratorToProject(projectOne, user4);
        final Project project = relService.getProjectForCollection(collectionNoData);
        final Set<Person> initialDepositors = relService.getDepositorsForCollection(collectionNoData);
        assertEquals(projectOne, project);
        assertEquals(1, initialDepositors.size());

        MockRoundtrip trip = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, adminSession);

        trip.addParameter("userIdsToAdd", user1.getEmailAddress());
        trip.addParameter("userIdsToRemove", user2.getEmailAddress());
        trip.addParameter("userIdsToAdd", user3.getEmailAddress());
        trip.addParameter("userIdsToAdd", user4.getEmailAddress());
        trip.addParameter("userIdsToRemove", user4.getEmailAddress());
        trip.addParameter("userIdsToAdd", user5.getEmailAddress());
        trip.addParameter("userIdsToRemove", user5.getEmailAddress());
        trip.setParameter("selectedCollectionId", collectionNoData.getId());
        trip.execute("editDepositors");

        assertEquals(302, trip.getResponse().getStatus());

        // Verify expected relationships were added
        final Set<Person> addedDepositors = relService.getDepositorsForCollection(collectionNoData);

        assertEquals(1, addedDepositors.size());
        assertTrue(addedDepositors.contains(user1));
        assertFalse(addedDepositors.contains(user2));
        assertFalse(addedDepositors.contains(user3));

        //cleanup
        relService.removeDepositorFromCollection(user1, collectionNoData);
        relService.removeCollectionFromProject(collectionNoData, projectOne);
        relService.removeAdministratorFromProject(user4, projectOne);
        relService.removeDepositorFromCollection(user1, collectionNoData);
        relService.removeDepositorFromCollection(user4, collectionNoData);
        relService.removeDepositorFromCollection(user5, collectionNoData);
    }

    /**
     * Tests that an administrator can remove everyone but themselves.
     *
     * @throws Exception
     */
    @Test
    public void testInstanceAdminCanRemoveEveryone() throws Exception {
        relService.addDepositorToCollection(user2, collectionNoData);
        relService.addAdministratorToProject(projectOne, admin);
        relService.addDepositorToCollection(admin, collectionNoData);
        relService
                .addAdministratorToProject(projectOne, projectPiInstanceAdmin);
        relService.addDepositorToCollection(projectPiInstanceAdmin,
                                            collectionNoData);
        relService.addAdministratorToProject(projectOne, projectPi);
        relService.addDepositorToCollection(projectPi, collectionNoData);

        final Project project = relService.getProjectForCollection(collectionNoData);
        final Set<Person> initialDepositors = relService.getDepositorsForCollection(collectionNoData);
        assertEquals(projectOne, project);
        assertEquals(4, initialDepositors.size());

        MockRoundtrip trip =
                new MockRoundtrip(servletCtx,
                                  UserCollectionsActionBean.class,
                                  adminSession);
        trip.addParameter("userIdsToRemove", user2.getId());
        trip.addParameter("userIdsToRemove", projectPi.getId());
        trip.addParameter("userIdsToRemove", projectPiInstanceAdmin.getId());
        trip.addParameter("userIdsToRemove", admin.getId());
        trip.setParameter("selectedCollectionId", collectionNoData.getId());
        trip.execute("editDepositors");

        assertEquals(302, trip.getResponse().getStatus());

        // Verify that on the current user remains since they couldn't delete themselves
        final Set<Person> remainingDepositors =
                relService.getDepositorsForCollection(collectionNoData);
        assertEquals(1, remainingDepositors.size());
        assertTrue(remainingDepositors.contains(admin));

        //cleanup
        relService.removeDepositorFromCollection(admin, collectionNoData);
        relService.removeCollectionFromProject(collectionNoData, projectOne);
        relService.removeAdministratorFromProject(admin, projectOne);
    }

    /**
     * Tests that a project admin can only remove other project admins and depositors and not system admins.
     *
     * @throws Exception
     */
    @Test
    public void testProjectAdminRemove() throws Exception {
        relService.addDepositorToCollection(user2, collectionNoData);
        relService.addAdministratorToProject(projectOne, user);
        relService.addDepositorToCollection(user, collectionNoData);
        relService
                .addAdministratorToProject(projectOne, projectPiInstanceAdmin);
        relService.addDepositorToCollection(projectPiInstanceAdmin,
                                            collectionNoData);
        relService.addAdministratorToProject(projectOne, projectPi);
        relService.addDepositorToCollection(projectPi, collectionNoData);

        final Project project = relService.getProjectForCollection(collectionNoData);
        final Set<Person> initialDepositors = relService.getDepositorsForCollection(collectionNoData);
        assertEquals(projectOne, project);
        assertEquals(4, initialDepositors.size());

        MockRoundtrip trip =
                new MockRoundtrip(servletCtx,
                                  UserCollectionsActionBean.class,
                                  userSession);
        trip.addParameter("userIdsToRemove", user2.getId());
        trip.addParameter("userIdsToRemove", projectPi.getId());
        trip.addParameter("userIdsToRemove", projectPiInstanceAdmin.getId());
        trip.addParameter("userIdsToRemove", user.getId());
        trip.setParameter("selectedCollectionId", collectionNoData.getId());
        trip.execute("editDepositors");

        assertEquals(302, trip.getResponse().getStatus());

        // Verify that on the current user remains since they couldn't delete themselves
        final Set<Person> remainingDepositors =
                relService.getDepositorsForCollection(collectionNoData);
        assertEquals(3, remainingDepositors.size());
        assertTrue(remainingDepositors.contains(user));
        assertTrue(remainingDepositors.contains(projectPiInstanceAdmin));

        //cleanup
        relService.removeDepositorFromCollection(user, collectionNoData);
        relService.removeDepositorFromCollection(projectPiInstanceAdmin, collectionNoData);
        relService.removeCollectionFromProject(collectionNoData, projectOne);
        relService.removeAdministratorFromProject(user, projectOne);
        relService.removeAdministratorFromProject(projectPiInstanceAdmin,
                                                  projectOne);
    }

    /**
     * Tests that a collection depositor can't remove anyone.
     *
     * @throws Exception
     */
    @Test
    public void testDepositorRemove() throws Exception {
        relService.addDepositorToCollection(user2, collectionNoData);
        relService.addDepositorToCollection(user, collectionNoData);
        relService
                .addAdministratorToProject(projectOne, projectPiInstanceAdmin);
        relService.addDepositorToCollection(projectPiInstanceAdmin,
                                            collectionNoData);
        relService.addAdministratorToProject(projectOne, projectPi);
        relService.addDepositorToCollection(projectPi, collectionNoData);

        final Project project = relService.getProjectForCollection(collectionNoData);
        final Set<Person> initialDepositors = relService.getDepositorsForCollection(collectionNoData);
        assertEquals(projectOne, project);
        assertEquals(4, initialDepositors.size());

        MockRoundtrip trip =
                new MockRoundtrip(servletCtx,
                                  UserCollectionsActionBean.class,
                                  userSession);
        trip.addParameter("userIdsToRemove", user2.getId());
        trip.addParameter("userIdsToRemove", projectPi.getId());
        trip.addParameter("userIdsToRemove", projectPiInstanceAdmin.getId());
        trip.addParameter("userIdsToRemove", user.getId());
        trip.setParameter("selectedCollectionId", collectionNoData.getId());
        trip.execute("editDepositors");

        assertEquals(401, trip.getResponse().getStatus());

        // Verify that on the current user remains since they couldn't delete themselves
        final Set<Person> remainingDepositors =
                relService.getDepositorsForCollection(collectionNoData);
        assertEquals(4, remainingDepositors.size());
        assertTrue(remainingDepositors.contains(user));
        assertTrue(remainingDepositors.contains(projectPiInstanceAdmin));
        assertTrue(remainingDepositors.contains(user2));
        assertTrue(remainingDepositors.contains(projectPi));

        //cleanup
        relService.removeDepositorFromCollection(user, collectionNoData);
        relService.removeDepositorFromCollection(projectPiInstanceAdmin, collectionNoData);
        relService.removeDepositorFromCollection(user2, collectionNoData);
        relService.removeDepositorFromCollection(projectPi, collectionNoData);
        relService.removeCollectionFromProject(collectionNoData, projectOne);
        relService.removeAdministratorFromProject(user, projectOne);
        relService.removeAdministratorFromProject(projectPiInstanceAdmin,
                                                  projectOne);
    }

    @Test
    public void testGetDepositorsForCollection() throws Exception {

        relService.addDepositorToCollection(user1, collectionNoData);
        relService.addDepositorToCollection(user2, collectionNoData);
        final Set<Person> initialCollectionDepositors = relService.getDepositorsForCollection(collectionNoData);
        assertEquals(2, initialCollectionDepositors.size());

        MockRoundtrip trip = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, userSession);
        trip.setParameter("selectedCollectionId", collectionNoData.getId());
        trip.execute("editCollectionDepositors");

        UserCollectionsActionBean bean = trip.getActionBean(UserCollectionsActionBean.class);
        final List<Person> finalCollectionDepositors = bean.getDepositorsForCollection(collectionNoData);
        assertEquals(2, finalCollectionDepositors.size());

        //cleanup
        relService.removeDepositorFromCollection(user1, collectionNoData);
        relService.removeDepositorFromCollection(user2, collectionNoData);
    }

    @Test
    public void testViewCollectionDetailsPath() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, userSession);
        rt.execute("viewCollectionDetails");
        // A valid collection ID wasn't supplied
        assertEquals(404, rt.getResponse().getStatus());
    }

    @Test
    public void testViewCollectionDetailsPathWithValidCollection()
            throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  UserCollectionsActionBean.class,
                                  userSession);
        // A valid collection ID is supplied supplied
        rt.addParameter("selectedCollectionId", collectionNoData.getId());
        rt.execute("viewCollectionDetails");
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals(UserCollectionsActionBean.VIEW_COLLECTION_DETAILS_PATH,
                     rt.getForwardUrl());
    }

    @Test
    public void testViewCollection() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, adminSession);
        trip.setParameter("selectedCollectionId", collectionNoData.getId());
        trip.execute("viewCollectionDetails");
        assertEquals(200, trip.getResponse().getStatus());
    }

    @Test
    public void testViewNonExistentCollection() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, adminSession);
        trip.setParameter("selectedCollectionId", BAD_COLLECTION_ID);
        trip.execute("viewCollectionDetails");
        assertEquals(404, trip.getResponse().getStatus());
    }

    @Test
    public void testGenerateCitation() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, userSession);
        // A valid collection ID is supplied supplied
        rt.addParameter("selectedCollectionId", collectionWithData.getId());
        rt.execute("viewCollectionDetails");
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals(UserCollectionsActionBean.VIEW_COLLECTION_DETAILS_PATH, rt.getForwardUrl());
        Map<CitationFormatter.CitationFormat, String> citations = rt.getActionBean(UserCollectionsActionBean.class).getCitations();
        assertNotNull(citations);
        // TODO This will need adjusting when Version and Locator are fully implemented
        String expectedStringESIPStandard = "Beanert McGee, S.B.W. and P.B. Beanert. (2013). <i>Collection with data.</i> <i>Version [Version].</i> THIS IS PUBLISHER 1. http://foo. Accessed " + DateTime.now().getDayOfMonth() + " " + MONTHS[DateTime.now().getMonthOfYear() - 1] + " " + DateTime.now().getYear() + ".";

        assertEquals(expectedStringESIPStandard, citations.get(CitationFormatter.CitationFormat.ESIP));
    }

    @Test
    public void testAdminViewCollectionList() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, adminSession);
        trip.execute("render");
        assertEquals(200, trip.getResponse().getStatus());
    }

    @Test
    public void testUserCanViewCollectionList() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, userSession);
        trip.execute("render");
        assertEquals(200, trip.getResponse().getStatus());
    }

    @Test
    public void testNonLoggedInUserCanViewCollectionList() throws Exception {
        MockHttpSession session = new MockHttpSession(servletCtx);
        MockRoundtrip trip = new MockRoundtrip(servletCtx, "/usercollections/render/", session);
        trip.execute();
        assertEquals(200, trip.getResponse().getStatus());
    }

    /**
     * Tests that adding a non existent user doesn't cause a null pointer exception
     *
     * @throws Exception
     */
    @Test
    public void testNonExistentUserAddAsDepositor() throws Exception {
        MockRoundtrip trip = new MockRoundtrip(servletCtx, UserCollectionsActionBean.class, adminSession);
        trip.addParameter("userIdsToAdd", "foo@email.com");
        trip.setParameter("selectedCollectionId", collectionNoData.getId());
        trip.execute("editDepositors");

        assertEquals(302, trip.getResponse().getStatus());
    }
}
