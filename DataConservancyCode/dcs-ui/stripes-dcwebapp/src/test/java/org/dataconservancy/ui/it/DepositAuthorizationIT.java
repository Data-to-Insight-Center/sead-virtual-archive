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

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.ui.it.support.AuthorizedDepositorRequest;
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

public class DepositAuthorizationIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private AuthorizedDepositorRequest authorizedDepositorRequest;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person depositorUser;
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    private HttpPost adminLogin;
    private HttpPost depositorLogin;
    private HttpGet logout;
    
    private static File sampleDataFile;
    
    private static Collection collection;
    private static Collection unAuthorizedCollection;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
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
        
        adminLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        depositorLogin = reqFactory.createLoginRequest(depositorUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as an admin user!");
            
            Project project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project.getId()).toURI()), 200,
                    "Unable to create project " + project);
            
            collection = new Collection();
            collection.setId("DepositAuthorizationIT:collection");
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollection(project.getId(), collection);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            // Create a seperate collection in the same project.
            unAuthorizedCollection = new Collection();
            unAuthorizedCollection.setId("DepositAuthorizationIT:unAuthorizedCollection");
            unAuthorizedCollection.setTitle("Collection No Authorized Deposit");
            unAuthorizedCollection.setSummary("This collection should not be able to be deposited into");
            unAuthorizedCollection.setCreators(creators);
            
            createCollection(project.getId(), unAuthorizedCollection);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(unAuthorizedCollection.getId())
                    .toURI()), 200, "Unable to create collection " + unAuthorizedCollection);
            
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
            
            // load data file
            sampleDataFile = createSampleDataFile("DepositAuthorizationIT", ".txt");
            sampleDataFile.deleteOnExit();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests that a user can be given authorization to deposit into a collection, and can then deposit into that
     * collection.
     * 
     * @throws Exception
     *             from HttpClient and HttpResponse
     */
    @Test
    public void testAuthorizedUserSingleFileDeposit() throws Exception {
        // Authorize a user to be able to deposit in the collection
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as an admin user!");
        
        authorizedDepositorRequest.setAuthorizedUserForCollection(depositorUser.getId(), collection.getId());
        HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                + depositorUser + " for deposit!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin!");
        
        HttpAssert.assertStatus(hc, depositorLogin, 300, 399, "Unable to login as authorized user!");
        
        final DataItem deposit = new DataItem();
        deposit.setName("Deposit Dataset");
        deposit.setDescription("Dataset to deposit");
        deposit.setId("DepositAuthzIT:testAuthorizedSingleFileDeposit");
        
        DepositRequest depositRequest = new DepositRequest(urlConfig);
        depositRequest.setDataItem(deposit);
        depositRequest.setCollectionId(collection.getId());
        depositRequest.setFileToDeposit(sampleDataFile);
        
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        StatusLine statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 302, statusLine.getStatusCode());
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
        assertNotNull("DataItem was not created in the archive.",
                archiveSupport.pollAndQueryArchiveForDataItem(deposit.getId()));
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout depositor.");
    }
    
    /**
     * Tests that after a user is removed as a depositor on a collection they can no longer deposit to that collection.
     * 
     * @throws Exception
     *             from HttpClient and HttpResponse
     */
    @Test
    public void testUnAuthorizedUserSingleFileDeposit() throws Exception {
        
        // Authorize a user to be able to deposit in the collection
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as an basic user!");
        authorizedDepositorRequest.setAuthorizedUserForCollection(depositorUser.getId(), collection.getId());
        HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                + depositorUser + " for deposit!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin!");
        
        HttpAssert.assertStatus(hc, depositorLogin, 300, 399, "Unable to login as authorized user!");
        
        final DataItem deposit = new DataItem();
        deposit.setName("Deposit Dataset");
        deposit.setDescription("Dataset to deposit");
        deposit.setId("DepositAuthzIT:testUnAuthorizedSingleFileDeposit");
        
        DepositRequest depositRequest = new DepositRequest(urlConfig);
        depositRequest.setDataItem(deposit);
        depositRequest.setCollectionId(collection.getId());
        depositRequest.setFileToDeposit(sampleDataFile);
        
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        StatusLine statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 302, statusLine.getStatusCode());
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
        assertNotNull("DataItem was not created in the archive.",
                archiveSupport.pollAndQueryArchiveForDataItem(deposit.getId()));
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout depositor.");
        
        // Now remove authorization and ensure the user can't deposit into the collection.
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as a basic user!");
        
        authorizedDepositorRequest.removeAuthorizedUserFromCollection(depositorUser.getId(), collection.getId());
        HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                + depositorUser + " for deposit!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin!");
        
        HttpAssert.assertStatus(hc, depositorLogin, 300, 399, "Unable to login as authorized user!");
        
        authorizedResponse = hc.execute(depositRequest.asHttpPost());
        statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 400, statusLine.getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout depositor.");
    }
    
    /**
     * Tests that if a user is granted permission to one collection in a project they can't deposit into another
     * collection in the project. Which they don't have permissions for.
     * 
     * @throws Exception
     *             from HttpClient and HttpResponse
     */
    @Test
    public void testAuthorizedUserWrongCollectionSingleFileDeposit() throws Exception {
        
        // Authorize a user to be able to deposit in the collection.
        // Authorize a user to be able to deposit in the collection
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as an basic user!");
        
        authorizedDepositorRequest.setAuthorizedUserForCollection(depositorUser.getId(), collection.getId());
        HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                + depositorUser + " for deposit!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin!");
        
        HttpAssert.assertStatus(hc, depositorLogin, 300, 399, "Unable to login as authorized user!");
        
        final DataItem deposit = new DataItem();
        deposit.setName("Deposit Dataset");
        deposit.setDescription("Dataset to deposit");
        deposit.setId("DepositAuthzIT:testAuthorizedUserWrongCollection");
        
        DepositRequest depositRequest = new DepositRequest(urlConfig);
        depositRequest.setDataItem(deposit);
        depositRequest.setCollectionId(collection.getId());
        depositRequest.setFileToDeposit(sampleDataFile);
        
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        StatusLine statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 302, statusLine.getStatusCode());
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
        assertNotNull("DataItem was not created in the archive.",
                archiveSupport.pollAndQueryArchiveForDataItem(deposit.getId()));
        
        // Check that authorized user can only deposit to the collection authorization was granted.
        DepositRequest depositIntoUnAuthCollectionRequest = new DepositRequest(urlConfig);
        depositIntoUnAuthCollectionRequest.setDataItem(deposit);
        depositIntoUnAuthCollectionRequest.setCollectionId(unAuthorizedCollection.getId());
        depositIntoUnAuthCollectionRequest.setFileToDeposit(sampleDataFile);
        
        authorizedResponse = hc.execute(depositIntoUnAuthCollectionRequest.asHttpPost());
        statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 400, statusLine.getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout depositor.");
    }
    
    private void createCollection(String projectId, Collection toCreate) throws IOException {
        createCollectionRequest.setCollection(toCreate);
        createCollectionRequest.setProjectId(projectId);
        hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
    }
}