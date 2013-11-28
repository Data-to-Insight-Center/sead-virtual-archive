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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.it.support.ArchiveSupport;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.CreateIdApiRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CollectionDataItemsListIT extends BaseIT {
    
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
    private Person nonProjectAdmin;
    
    @Autowired
    private ArchiveSupport archiveSupport;
    
    private HttpPost adminLogin;
    private HttpPost nonAdminLogin;
    
    private HttpGet logout;
    
    // Collection with data
    private static Collection collectionWithData;
    
    // Collection with no data
    private static Collection collectionNoData;
    
    // Project to hold the collections
    private static Project project;
    
    // Properties for testing static strings
    private static Properties props;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    // counter for how many times we have tried to poll the archive
    private int pollCount;
    // maximum number of times to poll
    private final int maxPollTimes = 60;
    
    /**
     * Sets up the httpConnection, project, collection, and dataset
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        nonAdminLogin = reqFactory.createLoginRequest(nonProjectAdmin).asHttpPost();
        adminLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            
            final String collectionWithDataId;
            final String collectionWithNoDataId;
            final String dataSetId;
            
            CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
            CreateIdApiRequest datasetIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
            
            collectionWithDataId = collectionIdRequest.execute(hc);
            collectionWithNoDataId = collectionIdRequest.execute(hc);
            dataSetId = datasetIdRequest.execute(hc);
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as admin user!");
            
            project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            collectionWithData = new Collection();
            collectionWithData.setId(collectionWithDataId);
            collectionWithData.setTitle("Data Collection: Card864-Collection-W-Data");
            collectionWithData.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collectionWithData.setCreators(creators);
            
            createCollectionRequest.setCollection(collectionWithData);
            createCollectionRequest.setProjectId(project.getId());
            hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(hc,
                    new HttpGet(urlConfig.getViewCollectionUrl(collectionWithData.getId()).toURI()), 200,
                    "Unable to create collection " + collectionWithData);
            
            collectionNoData = new Collection();
            collectionNoData.setId(collectionWithNoDataId);
            collectionNoData.setTitle("Empty Collection: Card864-Collection-No-Data");
            collectionNoData.setSummary("A seeded collection for use with ITs");
            collectionNoData.setCreators(creators);
            
            createCollectionRequest.setCollection(collectionNoData);
            createCollectionRequest.setProjectId(project.getId());
            hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collectionNoData.getId()).toURI()),
                    200, "Unable to create collection " + collectionNoData);
            
            final File sampleDataFile = createSampleDataFile("DepositIT", ".txt");
            sampleDataFile.deleteOnExit();
            
            final DataItem dataItem = new DataItem();
            dataItem.setName("Sample Dataset 1: Card864-testProjectAdminCanDeposit-dataSet-1");
            dataItem.setDescription("Sample Dataset Descrption");
            dataItem.setId(dataSetId);
            
            DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem,
                    collectionWithData.getId(), sampleDataFile);
            
            HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
            assertEquals("Unable to deposit with newly added admin.", 302, authorizedResponse.getStatusLine()
                    .getStatusCode());
            assertTrue(
                    UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                            .replace("{$event}", "render")
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
            
            // Trigger the polling of the archive by calling the deposit status page
            authorizedResponse = hc.execute(reqFactory.depositStatusRequest(collectionWithData.getId()).asHttpGet());
            assertEquals("Unable to retrieve the deposit status of Collection (" + collectionWithData.getId() + ")",
                    200, authorizedResponse.getStatusLine().getStatusCode());
            HttpAssert.free(authorizedResponse);
            
            pollCount = 0;
            // Poll the archive until all of our objects become available
            Object archivedObject = null;
            do {
                archivedObject = archiveSupport.pollAndQueryArchiveForCollection(collectionWithData.getId());
                Thread.sleep(1000L);
            }
            while (archivedObject == null && pollCount++ < maxPollTimes);
            
            pollCount = 0;
            archivedObject = null;
            do {
                archivedObject = archiveSupport.pollAndQueryArchiveForCollection(collectionNoData.getId());
                Thread.sleep(1000L);
            }
            while (archivedObject == null && pollCount++ < maxPollTimes);
            archivedObject = null;
            
            pollCount = 0;
            do {
                archivedObject = archiveSupport.pollAndQueryArchiveForDataItem(dataItem.getId());
                Thread.sleep(1000L);
            }
            while (archivedObject == null && pollCount++ < maxPollTimes);
            
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
            
            props = new Properties();
            InputStream in = getClass().getResourceAsStream("/pageText/collection_data_list.properties");
            if (null != in) {
                props.load(in);
                in.close();
            }
            
            areObjectsSeeded = true;
        }
    }
    
    @Test
    public void testAdminUserCanViewDataList() throws ClientProtocolException, IOException {
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as admin user!");
        HttpResponse response = hc.execute(createViewDataItemsRequest(collectionWithData.getId(), ""));
        
        assertEquals("Unable to view datasets as admin.", 200, response.getStatusLine().getStatusCode());
        checkResponse(response, String.format(props.getProperty("collection_data_list.total-data-items"),
                collectionWithData.getTitle(), 1));
        
        response.getEntity().getContent().close();
        HttpAssert.free(response);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout");
    }
    
    @Test
    public void testNonAdminCanViewDataList() throws ClientProtocolException, IOException {
        HttpAssert.assertStatus(hc, nonAdminLogin, 300, 399, "Unable to login as non admin user!");
        HttpResponse response = hc.execute(createViewDataItemsRequest(collectionWithData.getId(), ""));
        
        // Since we are displaying a message instead of an error resolution we expect the request to go through
        assertEquals("Error displaying non admin viewing data list", 200, response.getStatusLine().getStatusCode());
        checkResponse(response, String.format(props.getProperty("collection_data_list.total-data-items"),
                collectionWithData.getTitle(), 1));
        
        response.getEntity().getContent().close();
        HttpAssert.free(response);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout");
    }
    
    @Test
    public void viewEmptyCollection() throws ClientProtocolException, IOException {
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as admin user!");
        HttpResponse response = hc.execute(createViewDataItemsRequest(collectionNoData.getId(), ""));
        
        // Since we are displaying a message instead of an error resolution we expect the request to go through
        assertEquals("Error displaying empty collection", 200, response.getStatusLine().getStatusCode());
        checkResponse(response,
                String.format(props.getProperty("collection_data_list.no-data-items"), collectionNoData.getTitle()));
        
        response.getEntity().getContent().close();
        HttpAssert.free(response);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout");
    }
    
    @Test
    public void viewNonExistentCollection() throws ClientProtocolException, IOException {
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as admin user!");
        HttpResponse response = hc.execute(createViewDataItemsRequest("imaginary-collection", ""));
        
        // Since we are displaying a message instead of an error resolution we expect the request to go through
        assertEquals("Error displaying collection that doesn't exist", 200, response.getStatusLine().getStatusCode());
        checkResponse(response, props.getProperty("collection_data_list.no-collection"));
        
        response.getEntity().getContent().close();
        HttpAssert.free(response);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout");
    }
    
    @Test
    public void viewNonExistentPage() throws ClientProtocolException, IOException {
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as admin user!");
        HttpResponse response = hc.execute(createViewDataItemsRequest(collectionWithData.getId(), "2"));
        
        // Since we are displaying a message instead of an error resolution we expect the request to go through
        assertEquals("Error displaying page that doesn't exist", 200, response.getStatusLine().getStatusCode());
        checkResponse(response,
                String.format(props.getProperty("collection_data_list.no-data-items"), collectionWithData.getTitle()));
        
        response.getEntity().getContent().close();
        HttpAssert.free(response);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout");
    }
    
    private HttpPost createViewDataItemsRequest(String collectionId, String page) {
        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getCollectionDataListUrl().toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("currentCollectionId", collectionId));
        if (!page.isEmpty()) {
            params.add(new BasicNameValuePair("page", page));
        }
        
        params.add(new BasicNameValuePair("renderResults", "List Collections Data Items"));
        
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
    
    private void checkResponse(HttpResponse response, String expectedMessage) throws IllegalStateException, IOException {
        final InputStream content = response.getEntity().getContent();
        
        final String html = IOUtils.toString(content);
        
        assertTrue("Expected HTML to contain string '" + expectedMessage + "'.  Full content was: \n" + html,
                html.contains(expectedMessage));
    }
    
}
