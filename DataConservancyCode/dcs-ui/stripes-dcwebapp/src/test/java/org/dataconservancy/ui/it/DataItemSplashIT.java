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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests pertaining to the DataItem splash page. Checks that everyone can view the page(since it's currently available
 * anonymously). Tests that proper error message are returned for error cases.
 * 
 */
public class DataItemSplashIT extends BaseIT {
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person defaultAdmin;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person defaultUser;
    
    @Autowired
    protected UiUrlConfig urlConfig;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    private HttpPost systemAdminLogin;
    private HttpGet logout;
    
    private HttpPost basicUserLogin;
    
    private static DataItem dataItem;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    // HttpClient to use for testing url calls
    private HttpClient httpClient = new DefaultHttpClient();
    
    @Autowired
    BusinessObjectBuilder businessObjectBuilder;
    
    // counter for how many times we have tried to poll the archive
    private int pollCount;
    // maximum number of times to poll
    private final int maxPollTimes = 60;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    @Before
    public void setup() throws InvalidXmlException, IOException, URISyntaxException, InterruptedException {
        
        systemAdminLogin = reqFactory.createLoginRequest(defaultAdmin).asHttpPost();
        basicUserLogin = reqFactory.createLoginRequest(defaultUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        // Set up hc for the tests
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
            
            Project project = new Project();
            project.setName("Test_Project");
            project.setDescription("foo");
            project.addNumber("12");
            project.setFundingEntity("Straight cash homey");
            project.setStartDate(new DateTime(2012, 5, 4, 0, 0));
            project.setEndDate(new DateTime(2013, 12, 23, 0, 0));
            project.addPi(defaultAdmin.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(httpClient);
            
            Collection collection = new Collection();
            collection.setTitle("CollectionOne");
            collection.setSummary("CollectionOne-Foo");
            collection.setId(reqFactory.createIdApiRequest(Types.COLLECTION).execute(httpClient));
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            httpClient.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(httpClient,
                    new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            dataItem = new DataItem();
            dataItem.setName("Test dataitem");
            
            dataItem.setId("");
            dataItem.setDescription("DataItem with 1 file");
            // A generic data file
            java.io.File tempFile = java.io.File.createTempFile("temp.tmp", null);
            tempFile.deleteOnExit();
            PrintWriter out = new PrintWriter(tempFile);
            out.println(tempFile);
            out.close();
            
            DataFile dataFile = new DataFile();
            dataFile.setName("Test file");
            dataFile.setSource(tempFile.toURI().toURL().toExternalForm());
            dataFile.setPath(tempFile.getPath());
            dataFile.setId(reqFactory.createIdApiRequest(Types.DATA_FILE).execute(httpClient));
            dataItem.addFile(dataFile);
            
            org.dataconservancy.ui.model.Package thePackage = new org.dataconservancy.ui.model.Package();
            thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(httpClient));
            
            HttpPost dataItemDeposit = reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem,
                    collection.getId(), tempFile).asHttpPost();
            HttpAssert.assertStatus(httpClient, dataItemDeposit, 300, 399, "Unable to deposit dataItem!");
            
            String content;
            do {
                HttpResponse response = httpClient.execute(new HttpGet(urlConfig
                        .getDepositStatusUrl(thePackage.getId()).toURI()));
                content = IOUtils.toString(response.getEntity().getContent());
                Thread.sleep(1000L);
            }
            while (!content.contains("DEPOSITED") && pollCount++ < maxPollTimes);
            
            DcsDeliverableUnit collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId());
            assertNotNull("Collection was not created in the archive.", collectionDu);
            
            String collectionArchiveID = collectionDu.getId();
            Set<DataItem> collectionDataItems = archiveSupport.pollAndQueryArchiveForDataItemsInCollection(
                    collectionArchiveID, 1);
            
            assertEquals(1, collectionDataItems.size());
            dataItem = collectionDataItems.iterator().next();
            
            HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
            
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests that a system admin can view the the splash page.
     * 
     * @throws Exception
     */
    @Test
    public void testAdminCanViewSplash() throws Exception {
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpResponse authorizedResponse = httpClient.execute(createDataItemSplashPost(dataItem.getId()));
        assertEquals(200, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests that a basic logged in user can view the splash page
     */
    @Test
    public void testRegisteredUserCanViewSplash() throws Exception {
        HttpAssert.assertStatus(httpClient, basicUserLogin, 300, 399, "Unable to login as admin user!");
        
        HttpResponse authorizedResponse = httpClient.execute(createDataItemSplashPost(dataItem.getId()));
        assertEquals(200, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests that someone not logged in can view the splash page.
     * 
     * @throws Exception
     */
    @Test
    public void testAnonymousCanViewSplashPage() throws Exception {
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
        
        HttpResponse authorizedResponse = httpClient.execute(createDataItemSplashPost(dataItem.getId()));
        assertEquals(200, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
    }
    
    /**
     * Tests that a 404 is returned when no id is specified.
     * 
     * @throws Exception
     */
    @Test
    public void testNoIdReturnsError() throws Exception {
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpResponse authorizedResponse = httpClient.execute(createDataItemSplashPost(""));
        assertEquals(404, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests that a 404 is returned when bad id (doesn't point to a dataitem) is specified.
     * 
     * @throws Exception
     */
    @Test
    public void testBadIdReturnsError() throws Exception {
        
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpResponse authorizedResponse = httpClient.execute(createDataItemSplashPost("foo"));
        assertEquals(404, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    private HttpPost createDataItemSplashPost(String dataItemId) throws URISyntaxException {
        
        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getDataItemSplashUrl().toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("dataItemID", dataItemId));
        params.add(new BasicNameValuePair("render", "render"));
        
        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        post.setEntity(entity);
        
        return post;
    }
    
}
