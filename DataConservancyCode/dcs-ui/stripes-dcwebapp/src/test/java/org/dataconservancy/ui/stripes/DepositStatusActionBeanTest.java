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

import java.util.List;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.dao.PackageDAO;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.policy.support.PersonBizPolicyConsultant;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DirtiesDatabase
@DirtiesContext
public class DepositStatusActionBeanTest
        extends BaseActionBeanTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Autowired
    private PackageDAO packageDAO;

    @Autowired
    private RelationshipService relService;

    private org.dataconservancy.ui.model.Package thePackage;

    private Person depositor;

    private Person projectPi;

    private MockHttpSession adminSession;

    private MockHttpSession projectPiSession;

    private MockHttpSession depositorSession;

    private MockHttpSession userSession;

    @Before
    public void setUpObjectsAndSessions() throws Exception {
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

        MockRoundtrip rt;
        SecurityContext ctx;

        //Create the project PI
        projectPi = new Person();
        projectPi.setId("id:projectpi");
        projectPi.setEmailAddress("projectOne@pi.org");
        projectPi.setFirstNames("Project");
        projectPi.setLastNames("PI");
        projectPi.setPrefix("Mr.");
        projectPi.setSuffix("II");
        projectPi.setMiddleNames("Middle");
        projectPi.setPreferredPubName("P. PI");
        projectPi.setBio("Some bio for the user.");
        projectPi.setWebsite("www.somewebsite.com");
        projectPi.setPassword("password");
        projectPi.setPhoneNumber("5556667777");
        projectPi.setJobTitle("Project Scientist");
        projectPi.setDepartment("Project Department");
        projectPi.setCity("Baltimore");
        projectPi.setState("Maryland");
        projectPi.setInstCompany("Project Institution/Company");
        projectPi.setInstCompanyWebsite("www.ProjectInstitutionCompany.com");
        projectPi.setRegistrationStatus(RegistrationStatus.APPROVED);
        projectPi.setExternalStorageLinked(false);
        projectPi.setDropboxAppKey("SomeKey");
        projectPi.setDropboxAppSecret("SomeSecret");
        userService.create(projectPi);

        //Create the depositor
        depositor = new Person();
        depositor.setId("id:depositor");
        depositor.setEmailAddress("collection@depositor.org");
        depositor.setFirstNames("Collection");
        depositor.setLastNames("Depositor");
        depositor.setPrefix("Mr.");
        depositor.setSuffix("II");
        depositor.setMiddleNames("Middle");
        depositor.setPreferredPubName("C. Depositor");
        depositor.setBio("Some bio for the user.");
        depositor.setWebsite("www.somewebsite.com");
        depositor.setPassword("password");
        depositor.setPhoneNumber("5556667777");
        depositor.setJobTitle("Collection Scientist");
        depositor.setDepartment("Collection Department");
        depositor.setCity("Baltimore");
        depositor.setState("Maryland");
        depositor.setInstCompany("Collection Institution/Company");
        depositor.setInstCompanyWebsite("www.CollectionInstitutionCompany.com");
        depositor.setRegistrationStatus(RegistrationStatus.APPROVED);
        depositor.setExternalStorageLinked(false);
        depositor.setDropboxAppKey("SomeKey");
        depositor.setDropboxAppSecret("SomeSecret");
        userService.create(depositor);

        //Set up the projectOne
        relService.addAdministratorToProject(projectOne, projectPi);

        relService.addDepositorToCollection(depositor, collectionWithData);

        //Create the package
        thePackage = new org.dataconservancy.ui.model.Package();
        thePackage.setId(idService.create(Types.PACKAGE.name()).getUrl()
                .toString());
        thePackage.setPackageFileName(this.getClass().getName()
                + "package_file_name");
        thePackage.setPackageType(Package.PackageType.SIMPLE_FILE);
        thePackage.addFile(dataItemOne.getId(), dataFileOne.getName());
        packageDAO.insertPackage(thePackage);

        //Admin session setup
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

        //Project PI session setup
        projectPiSession = new MockHttpSession(servletCtx);
        rt =
                new MockRoundtrip(servletCtx,
                                  "/j_spring_security_check",
                                  projectPiSession);
        rt.setParameter("j_username", projectPi.getEmailAddress());
        rt.setParameter("j_password", projectPi.getPassword());
        rt.execute();
        ctx =
                (SecurityContext) projectPiSession
                        .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(projectPi.getEmailAddress(), ((UserDetails) ctx
                .getAuthentication().getPrincipal()).getUsername());

        //Depositor setup
        depositorSession = new MockHttpSession(servletCtx);
        rt =
                new MockRoundtrip(servletCtx,
                                  "/j_spring_security_check",
                                  depositorSession);
        rt.setParameter("j_username", depositor.getEmailAddress());
        rt.setParameter("j_password", depositor.getPassword());
        rt.execute();
        ctx =
                (SecurityContext) depositorSession
                        .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(depositor.getEmailAddress(), ((UserDetails) ctx
                .getAuthentication().getPrincipal()).getUsername());

        //User session setup
        userSession = new MockHttpSession(servletCtx);
        rt =
                new MockRoundtrip(servletCtx,
                                  "/j_spring_security_check",
                                  userSession);
        rt.setParameter("j_username", unauthorizedUser.getEmailAddress());
        rt.setParameter("j_password", unauthorizedUser.getPassword());
        rt.execute();
        ctx =
                (SecurityContext) userSession
                        .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(unauthorizedUser.getEmailAddress(), ((UserDetails) ctx
                .getAuthentication().getPrincipal()).getUsername());
    }

    @Test
    @DirtiesDatabase
    public void testAnonymousUserCantView() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx, DepositStatusActionBean.class);
        rt.addParameter("objectId", thePackage.getId());
        rt.execute();
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getDestination().endsWith("/login/login.action"));
    }

    @Test
    @DirtiesDatabase
    public void testUnaffiliatedUserCantView() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  userSession);
        rt.addParameter("objectId", thePackage.getId());
        rt.execute();
        assertEquals(401, rt.getResponse().getStatus());
    }

    @Test
    @DirtiesDatabase
    public void testDepositorCanView() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  depositorSession);
        rt.addParameter("objectId", thePackage.getId());
        rt.execute();
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    @DirtiesDatabase
    public void testProjectPiCanView() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  projectPiSession);
        rt.addParameter("objectId", thePackage.getId());
        rt.execute();
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    @DirtiesDatabase
    public void testAdminCanView() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  adminSession);
        rt.addParameter("objectId", thePackage.getId());
        rt.execute();
        assertEquals(200, rt.getResponse().getStatus());
    }

    @Test
    @DirtiesDatabase
    public void testNonExistantId() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  depositorSession);
        rt.execute();
        assertEquals(400, rt.getResponse().getStatus());
    }

    @Test
    @DirtiesDatabase
    public void testIncorrectId() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  depositorSession);
        rt.addParameter("objectId", "Bogus!");
        rt.execute();
        assertEquals(400, rt.getResponse().getStatus());
    }

    @Test
    @DirtiesDatabase
    public void testStatusIsCorrect() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  depositorSession);
        rt.addParameter("objectId", thePackage.getId());
        rt.execute();
        assertEquals(archiveService.getDepositStatus(dataItemOneDepositID)
                .toString(), rt.getActionBean(DepositStatusActionBean.class)
                .getStatusList().get(0));
    }

    @Test
    @DirtiesDatabase
    public void testGetDepositStatusForCollectionObject() throws Exception {
        MockRoundtrip rt =
                new MockRoundtrip(servletCtx,
                                  DepositStatusActionBean.class,
                                  adminSession);
        rt.addParameter("objectId", collectionOne.getId());
        rt.execute();
        assertEquals(archiveService
                             .getDepositStatus(collectionWithDataDepositID)
                             .toString(),
                     rt.getActionBean(DepositStatusActionBean.class)
                             .getStatusList().get(0));
    }
}
