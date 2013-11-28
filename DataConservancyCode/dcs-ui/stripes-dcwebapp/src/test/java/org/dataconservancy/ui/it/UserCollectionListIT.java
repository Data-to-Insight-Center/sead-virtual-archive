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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.dataconservancy.ui.it.support.ListCollectionsRequest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.stripes.UserCollectionsActionBean;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 1/4/12 Time: 2:09 PM To change this template use File | Settings | File
 * Templates.
 */
public class UserCollectionListIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person defaultAdmin;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person defaultUser;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person approvedRegisteredUser;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private AuthorizedDepositorRequest authorizedDepositorRequest;
    
    @Autowired
    private ListCollectionsRequest listCollectionsRequest;
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    @Qualifier("dataItemProfile")
    private Profile<DataItem> datasetProfile;
    
    /**
     * A simple counter which is used to create identifiers.
     * <p/>
     * The identifier service isn't used, because if the Spring App Context is torn down, identifiers will be re-created
     * and duplicated for each test method resulting in duplicate key exceptions in various databases.
     */
    
    private static Collection collection1;
    private static Collection collection2;
    private static Collection collection3;
    private static File sampleDataFile;
    
    private static boolean areObjectsSeeded;
    
    private HttpPost defaultAdminLogin;
    private HttpPost approvedRegisteredUserLogin;
    private HttpGet logout;
    
    /**
     * Set up: In the role of adminUser: create project, 3 collections 1,2,3. Add approved registered user as depositor
     * for collection 1&2. Logout
     * 
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        defaultAdminLogin = reqFactory.createLoginRequest(defaultAdmin).asHttpPost();
        approvedRegisteredUserLogin = reqFactory.createLoginRequest(approvedRegisteredUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            HttpAssert.assertStatus(hc, defaultAdminLogin, 300, 399, "Unable to login as " + defaultAdmin);
            
            Project project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            project.addPi(defaultUser.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project.getId()).toURI()), 200,
                    "Unable to create project " + project);
            
            collection1 = new Collection();
            collection1.setId("Card759collection1");
            collection1.setTitle("Seeded Collection 1");
            collection1.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection1.setCreators(creators);
            createCollection(project.getId(), collection1);
            // HttpAssert.setLogging(true);
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection1.getId()).toURI()), 200,
                    "Unable to create collection " + collection1);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project.getId()).toURI()), 200,
                    "Unable to create project " + project);
            
            collection2 = new Collection();
            collection2.setId("Card759collection2");
            collection2.setTitle("Seeded Collection 2");
            collection2.setSummary("A seeded collection for use with ITs");
            collection2.setCreators(creators);
            createCollection(project.getId(), collection2);
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection2.getId()).toURI()), 200,
                    "Unable to create collection " + collection2);
            
            collection3 = new Collection();
            collection3.setId("Card759collection3");
            collection3.setTitle("Seeded Collection 3");
            collection3.setSummary("A seeded collection for use with ITs");
            collection3.setCreators(creators);
            createCollection(project.getId(), collection3);
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection3.getId()).toURI()), 200,
                    "Unable to create collection " + collection3);
            
            // /// ADD regular user as depositor for collection 2&3
            authorizedDepositorRequest.setAuthorizedUserForCollection(approvedRegisteredUser.getEmailAddress(),
                    collection1.getId());
            HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                    + approvedRegisteredUser + " for deposit!");
            
            authorizedDepositorRequest.setAuthorizedUserForCollection(approvedRegisteredUser.getEmailAddress(),
                    collection2.getId());
            HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                    + approvedRegisteredUser + " for deposit!");
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
            // load sample data file
            sampleDataFile = createSampleDataFile("UserCollectionListIT", ".txt");
            sampleDataFile.deleteOnExit();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * log Test that collections listed in collection list page are indeed depositable by approvedRegistered user. Log
     * on as approved registered user. Request collection listing page. Scan the http response for expected collections.
     * Verify that non-depositable collection is listed.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testCorrectCollectionsListed() throws IOException {
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, approvedRegisteredUserLogin, 300, 399, "Unable to login as "
                + approvedRegisteredUser);
        
        HttpAssert.assertStatus(hc, urlConfig.getListCollectionsUrl().toString(), 200);
        HttpResponse response = hc.execute(listCollectionsRequest.asHttpPost());
        final InputStream content = response.getEntity().getContent();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(content));
        String strLine;
        boolean foundCollection1 = false;
        boolean foundCollection2 = false;
        boolean foundCollection3 = false;
        while ((strLine = br.readLine()) != null) {
            if (foundCollection1 == false && strLine.contains(collection1.getTitle())) {
                foundCollection1 = true;
                continue;
            }
            if (foundCollection2 == false && strLine.contains(collection2.getTitle())) {
                foundCollection2 = true;
                continue;
            }
            if (foundCollection3 == false && strLine.contains(collection3.getTitle())) {
                foundCollection3 = true;
            }
        }
        assertTrue(foundCollection1);
        assertTrue(foundCollection2);
        assertTrue(foundCollection3);
        content.close();
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Test that all collections appear even if user is not logged in
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testCorrectCollectionsListedNotLoggedIn() throws IOException {
        
        HttpAssert.assertStatus(hc, urlConfig.getListCollectionsUrl().toString(), 200);
        HttpResponse response = hc.execute(listCollectionsRequest.asHttpPost());
        final InputStream content = response.getEntity().getContent();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(content));
        String strLine;
        boolean foundCollection1 = false;
        boolean foundCollection2 = false;
        boolean foundCollection3 = false;
        while ((strLine = br.readLine()) != null) {
            if (foundCollection1 == false && strLine.contains(collection1.getTitle())) {
                foundCollection1 = true;
                continue;
            }
            if (foundCollection2 == false && strLine.contains(collection2.getTitle())) {
                foundCollection2 = true;
                continue;
            }
            if (foundCollection3 == false && strLine.contains(collection3.getTitle())) {
                foundCollection3 = true;
            }
        }
        assertTrue(foundCollection1);
        assertTrue(foundCollection2);
        assertTrue(foundCollection3);
        content.close();
    }
    
    @Test
    public void testDepositableCollection() throws IOException {
        
        // Create a sample dataset object
        final DataItem ds = new DataItem();
        
        ds.setName("Sample Dataset for Card 759");
        ds.setDescription("Sample Dataset Description for card 759");
        ds.setId("Card759DepositableCollectionTestCollection");
        DepositRequest depositRequest = new DepositRequest(urlConfig);
        depositRequest.setDataItem(ds);
        depositRequest.setCollectionId(collection1.getId());
        depositRequest.setFileToDeposit(sampleDataFile);
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, approvedRegisteredUserLogin, 300, 399, "Unable to login as "
                + approvedRegisteredUser);
        
        // Attempt the deposit, we expect success
        final HttpPost post = depositRequest.asHttpPost();
        HttpResponse authorizedResponse = hc.execute(post);
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
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("The DeliverableUnit representing the deposited DataItem (" + ds.getId() + ") was not "
                + "found.", archiveSupport.pollAndQueryArchiveForDataItemDu(ds.getId()));
        
    }
    
    private void createCollection(String projectId, Collection toCreate) throws IOException {
        createCollectionRequest.setCollection(toCreate);
        createCollectionRequest.setProjectId(projectId);
        hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
    }
}
