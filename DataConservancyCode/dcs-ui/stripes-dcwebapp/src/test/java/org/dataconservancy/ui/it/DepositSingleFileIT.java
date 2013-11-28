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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

/**
 *
 */
public class DepositSingleFileIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person authorizedUser;
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person defaultAdmin;
    
    @Autowired
    @Qualifier("unapprovedRegisteredUser")
    private Person unapprovedRegisteredUser;
    
    @Autowired
    @Qualifier("adminUser")
    private Person adminUser;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person approvedRegisteredUser;
    
    /**
     * This is the sample data file that is deposited. The contents of the file aren't important, and is not modified in
     * any way. So this file is created only once (via {@link #createSampleDataFile(String, String)} and re-used for
     * each test method.
     */
    private static File sampleDataFile;
    
    /**
     * This is the collection that datasets are deposited into.
     * <p/>
     * It isn't modified after it is created and put in the archive (none of the tests should need to modify this
     * collection), so to save time in test fixup we keep it as a static object. Otherwise we'd need to communicate with
     * the archive for each test method, creating a new collection each time and polling the archive.
     */
    private static Collection collection;
    
    /**
     * State recording whether or not collaborating test objects have been seeded.
     * <p/>
     * This flag is modified during test setup and checked when each test method is invoked. If the flag is false, the
     * collaborating test objects are created, if true, we skip creating the test objects because they already exist.
     * That way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They
     * aren't in an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    /**
     * Configures the HttpClient. Logs into the UI as an admin and:
     * <ul>
     * <li>Creates a Project in the UI.</li>
     * <li>Creates a Collection in the Archive.</li>
     * <li>Logs out.</li>
     * </ul>
     * 
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        if (!areObjectsSeeded) {
            log.debug("Seeding collaborating test objects.");
            
            HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(defaultAdmin).asHttpPost(), 300, 399,
                    "Unable to login as an admin user!");
            log.debug("Logging in as an admin: " + defaultAdmin.getEmailAddress());
            
            Project project = new Project();
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            project.addPi(authorizedUser.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project.getId()).toURI()), 200,
                    "Unable to create project " + project);
            log.debug("Created project {}, {}", project.getId(), project);
            
            collection = new Collection();
            collection.setId("Card742SetupCollection");
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollection(project.getId(), collection);
            
            assertNotNull("Expected a Collection to be created in the archive!",
                    archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId()));
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            log.debug("Created collection {}", collection.getId());
            
            HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399,
                    "Unable to logout admin user!");
            
            log.debug("Logging out admin {}", adminUser.getEmailAddress());
            
            sampleDataFile = createSampleDataFile("DepositSingleFileIT", ".txt");
            sampleDataFile.deleteOnExit();
            
            areObjectsSeeded = true;
            
            log.debug("Successfully seeded collaborating test objects.");
        }
    }
    
    /**
     * Tests that a user - authorized to deposit to the collection (because that user is an Admin for the Project that
     * aggregates the collection) - is in fact able to make a deposit.
     * 
     * @throws Exception
     */
    @Test
    public void testAuthorizedSingleFileDeposit() throws Exception {
        // Create a sample dataset object
        final String datasetId = "Card742AuthorizedDataSet";
        final DataItem ds = createSampleDataset("Card742AuthorizedTestDataSet", "A Sample DataItem", datasetId, null);
        final DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(ds, collection.getId(),
                sampleDataFile);
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(authorizedUser).asHttpPost(), 300, 399,
                "Unable to login as " + authorizedUser);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals(302, authorizedResponse.getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                        .replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class
                                        .getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("The DeliverableUnit representing the deposited DataItem (" + ds.getId() + ") was not "
                + "found.", archiveSupport.pollAndQueryArchiveForDataItemDu(ds.getId()));
    }
    
    /**
     * Tests that an anonymous user - that is someone who is not logged in - is not able to make a deposit.
     * 
     * @throws Exception
     */
    @Test
    public void testAnonymousSingleFileDeposit() throws Exception {
        // Create a sample dataset object
        final String datasetId = "Card742AnonymousDataSet";
        final DataItem ds = createSampleDataset("Card742AnonymousTestDataSet", "A Sample DataItem", datasetId, null);
        final DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(ds, collection.getId(),
                sampleDataFile);
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Attempt to deposit it as an anonymous user (we expect failure)
        final HttpPost post = depositRequest.asHttpPost();
        HttpResponse unauthorizedResponse = hc.execute(post);
        // Spring security will ensure this.
        assertEquals(302, unauthorizedResponse.getStatusLine().getStatusCode());
        assertTrue(unauthorizedResponse.getFirstHeader("Location").getValue().endsWith(urlConfig.getLoginPath()));
        HttpAssert.free(unauthorizedResponse);
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        /*
         * assertNull("DataItem (" + ds.getId() + ") was unexpectedly deposited.",
         * archiveSupport.pollAndQueryArchiveForDatasetDu(ds.getId()));
         */
    }
    
    /**
     * Tests that an approved user - that is someone who has APPROVED registration status, is not project PI and is
     * logged in - is unable to make a deposit.
     * 
     * @throws Exception
     */
    @Test
    public void testApprovedRegisteredUserSingleFileDeposit() throws Exception {
        // Create a sample dataset object
        final String datasetId = "Card742ApprovedTestDataSet";
        final DataItem ds = createSampleDataset("Card742ApprovedTestDataSet", "A Sample DataItem", datasetId, null);
        final DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(ds, collection.getId(),
                sampleDataFile);
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(approvedRegisteredUser).asHttpPost(), 300, 399,
                "Unable to login as " + approvedRegisteredUser);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        // Attempt the deposit, we expect failure
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals(400, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        /*
         * assertNull("The DeliverableUnit representing the deposited DataItem (" + ds.getId() + ") was unexpectedly " +
         * "found.", archiveSupport.pollAndQueryArchiveForDatasetDu(ds.getId()));
         */
        
    }
    
    /**
     * Tests that an unapproved user - that is someone who has PENDING registration status and therefore cannot log in -
     * is unable to make a deposit.
     * 
     * @throws Exception
     */
    @Test
    public void testUnapprovedRegisteredUserSingleFileDeposit() throws Exception {
        // Create a sample dataset object
        final String datasetId = "Card742UnapprovedDataSet";
        final DataItem ds = createSampleDataset("Card742UnapprovedTestDataSet", "A Sample DataItem", datasetId, null);
        final DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(ds, collection.getId(),
                sampleDataFile);
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // attempt to login by POSTing to the Login Form.
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(unapprovedRegisteredUser).asHttpPost(), 300, 399,
                "Error with login attempt for " + unapprovedRegisteredUser);
        
        // Assert we are not logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Attempt to deposit it as an unapproved user (we expect failure)
        final HttpPost post = depositRequest.asHttpPost();
        HttpResponse unauthorizedResponse = hc.execute(post);
        assertEquals(302, unauthorizedResponse.getStatusLine().getStatusCode());
        assertTrue(unauthorizedResponse.getFirstHeader("Location").getValue().endsWith(urlConfig.getLoginPath()));
        HttpAssert.free(unauthorizedResponse);
        
        // Commented out because this is not necessary because spring security would not let un-logged-in user
        // access the depositing page?
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        /*
         * assertNull("DataItem (" + ds.getId() + ") was unexpectedly deposited.",
         * archiveSupport.pollAndQueryArchiveForDatasetDu(ds.getId()));
         */
    }
    
    /**
     * Tests that an instance admin - that is someone who has APPROVED registration status, and both ROLE_USER and
     * ROLE_ADMIN roles, who is not a project PI and is logged in - is able to make a deposit.
     * 
     * @throws Exception
     */
    @Test
    public void testInstanceAdministratorSingleFileDeposit() throws Exception {
        // Create a sample dataset object
        final String datasetId = "Card742AdminDataSet";
        final DataItem ds = createSampleDataset("Card742AdminTestDataSet", "A Sample DataItem", datasetId, null);
        final DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(ds, collection.getId(),
                sampleDataFile);
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(adminUser).asHttpPost(), 300, 399,
                "Unable to login as " + adminUser);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        
        assertEquals(302, authorizedResponse.getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                        .replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class
                                        .getAnnotation(net.sourceforge.stripes.action.UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("The DeliverableUnit representing the deposited DataItem (" + ds.getId() + ") was not "
                + "found.", archiveSupport.pollAndQueryArchiveForDataItemDu(ds.getId()));
        
    }
    
    /**
     * Create a new DataItem object from the supplied parameters. If a parameter is null, it won't be set on the
     * returned object.
     * 
     * @param name
     * @param description
     * @param identifier
     * @param depositor
     * @return the newly created DataItem
     */
    private DataItem createSampleDataset(String name, String description, String identifier, Person depositor) {
        final DataItem ds = new DataItem();
        if (name != null) {
            ds.setName(name);
        }
        if (description != null) {
            ds.setDescription(description);
        }
        if (identifier != null) {
            ds.setId(identifier);
        }
        if (depositor != null) {
            ds.setDepositorId(depositor.getId());
        }
        return ds;
    }
    
    /**
     * Creates a collection in the archive by invoking the add collection endpoint in the UI. No verification is done
     * that the collection is actually created. The caller will need to verify.
     * 
     * @param projectId
     *            the project to create the collection in
     * @param toCreate
     *            the collection to create
     * @throws java.io.IOException
     */
    private void createCollection(String projectId, Collection toCreate) throws IOException {
        createCollectionRequest.setCollection(toCreate);
        createCollectionRequest.setProjectId(projectId);
        hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
    }
}
