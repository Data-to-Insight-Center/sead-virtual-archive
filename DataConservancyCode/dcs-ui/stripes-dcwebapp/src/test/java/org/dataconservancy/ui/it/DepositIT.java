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
package org.dataconservancy.ui.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.DepositRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.ProjectAdminRequest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.stripes.UserCollectionsActionBean;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DepositIT extends BaseIT {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    // Someone to create the project
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    // Someone to create the Collection
    @Autowired
    @Qualifier("defaultUser")
    private Person piAssignedAtCreation;
    
    // Someone to deposit to the collection
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person piAssignedOnEdit;
    
    private HttpPost piAssignedOnEditLogin;
    
    private HttpPost piAssignedAtCreationLogin;
    
    private HttpPost adminUserLogin;
    
    private HttpGet logout;
    
    // Collection to deposit to
    private static Collection collection;
    
    // Project to hold the collections
    private static Project project;
    
    // Sample data file to use in the upload
    private static File sampleDataFile;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    /**
     * Sets up the httpConnection, project, collection, and dataset
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        piAssignedOnEditLogin = reqFactory.createLoginRequest(piAssignedOnEdit).asHttpPost();
        piAssignedAtCreationLogin = reqFactory.createLoginRequest(piAssignedAtCreation).asHttpPost();
        adminUserLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
            
            project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            project.addPi(piAssignedAtCreation.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            collection = new Collection();
            collection.setId("DepositIT-setUp-collection");
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
            
            sampleDataFile = createSampleDataFile("DepositIT", ".txt");
            sampleDataFile.deleteOnExit();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * This test verifies that an admin can be added to the project and can deposit into collections associated with the
     * project. It also tests that an original admin added at the creation of the project is still able to make
     * deposits.
     * 
     * @throws Exception
     */
    @Test
    public void testNewProjectAdminCanDeposit() throws Exception {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        ProjectAdminRequest req = reqFactory.createSetNewAdminForProjectRequest(piAssignedOnEdit.getId(), project);
        
        HttpAssert.assertStatus(hc, req.asHttpPost(), 300, 399, "Unable to add basic user " + piAssignedOnEdit
                + "as admin on project!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Login as the new PI, deposit file, and logout.
        HttpAssert.assertStatus(hc, piAssignedOnEditLogin, 300, 399, "Unable to login as basic user!");
        
        // Dataset for deposit
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset 1");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId("DepositIT-testProjectAdminCanDeposit-dataSet-1");
        
        DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem, collection.getId(),
                sampleDataFile);
        
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals("Unable to deposit with newly added admin.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("Dataset was not created in the archive.",
                archiveSupport.pollAndQueryArchiveForDataItemDu(dataItem.getId()));
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logoout pi assigned on edit.");
        
        // Check that the original PI added at creation of the project can still deposit.
        // Login as the original PI, deposit file, and logout.
        
        HttpAssert.assertStatus(hc, piAssignedAtCreationLogin, 300, 399, "Unable to login pi Assigned at creation.");
        
        // Dataset for deposit
        final DataItem originalAdminDataSet = new DataItem();
        originalAdminDataSet.setName("Sample Dataset 2");
        originalAdminDataSet.setDescription("Sample Dataset Descrption");
        originalAdminDataSet.setId("DepositIT-testProjectAdminCanDeposit-dataSet-2");
        
        DepositRequest originalAdminDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                originalAdminDataSet, collection.getId(), sampleDataFile);
        
        authorizedResponse = hc.execute(originalAdminDepositRequest.asHttpPost());
        assertEquals("Unable to deposit with Admin added at creation of project", 302, authorizedResponse
                .getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout pi Assigned At Creation");
    }
    
    /**
     * This test tests that user can be added an Admin on a project and make a deposit. It then verifies that if the
     * user is removed as an Admin they will no longer be able to deposit.
     * <p/>
     * Note: This test only tests the authority to deposit it doesn't test that a deposit was successfully made in the
     * Archive.
     * 
     * @throws Exception
     */
    @Test
    public void testExProjectAdminCannotDeposit() throws Exception {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        ProjectAdminRequest req = reqFactory.createSetNewAdminForProjectRequest(piAssignedOnEdit.getEmailAddress(),
                project);
        HttpAssert.assertStatus(hc, req.asHttpPost(), 300, 399, "Unable to add basic user " + piAssignedOnEdit
                + "as admin on project!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Login as the basic user (a PI), deposit file, and logout.
        HttpAssert.assertStatus(hc, piAssignedOnEditLogin, 300, 399, "Unable to login pi Assigned on Edit");
        
        // Dataset for deposit
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset 3");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId("DepositIT-testProjectAdminCannotDeposit-dataSet-3");
        
        DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem, collection.getId(),
                sampleDataFile);
        
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals("Unable to deposit with Admin added to the project", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("Collection was not created in the archive.",
                archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId()));
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout basic user!");
        
        // Login as the administrator user (the project creator), to remove basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        req = reqFactory.createRemoveAdminFromProjectRequest(piAssignedOnEdit.getEmailAddress(), project);
        HttpAssert.assertStatus(hc, req.asHttpPost(), 300, 399, "Unable to remove piAssignedOnEdit " + piAssignedOnEdit
                + "as admin on project!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout as PI assigned on edit");
        
        // Login as the basic user (a PI), try to deposit file(should fail), and logout.
        HttpAssert.assertStatus(hc, piAssignedOnEditLogin, 300, 399, "Unable to login as PI assigned on edit");
        
        // This deposit should fail because the user is no longer a pi on the project
        DepositRequest failedDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem,
                collection.getId(), sampleDataFile);
        
        HttpAssert.assertStatus(hc, failedDepositRequest.asHttpPost(), 400, 499, "Removed Admin was able to desposit.");
    }
    
    /**
     * Tests that an admin created with the project is able to deposit into the project. Note: This test only test the
     * authority to deposit, it does not test that a deposit was successfully made to the archive.
     * 
     * @throws Exception
     */
    @Test
    public void testProjectAdminCanDeposit() throws Exception {
        // Login as the basic user (a PI), deposit file, and logout.
        HttpAssert.assertStatus(hc, piAssignedAtCreationLogin, 300, 399, "Unable to login as basic user!");
        
        // Dataset for deposit
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset 4");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId("DepositIT-testProjectAdminCanDeposit-dataSet-4");
        
        DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem, collection.getId(),
                sampleDataFile);
        
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals("Unable to deposit with Admin added at creation of project", 302, authorizedResponse
                .getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout PI assigned at creation");
    }
    
    /**
     * Tests that the person who created the project is able to deposit into it. Note: This test only test the authority
     * to deposit is correct, it doesn't test that a deposit was successfully made in the archive.
     * 
     * @throws Exception
     */
    @Test
    public void testProjectCreatorCanDeposit() throws Exception {
        // Login as the administrator user (the project creator), deposit file, and logout.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        // Dataset for deposit
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset 5");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId("DepositIT-testProjectCreatorCanDeposit-dataSet-5");
        
        DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem, collection.getId(),
                sampleDataFile);
        
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals("Unable to deposit with Admin", 302, authorizedResponse.getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
    }
}
