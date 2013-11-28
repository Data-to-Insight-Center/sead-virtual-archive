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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.it.support.AuthorizedDepositorRequest;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.util.ETagCalculator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This tests the HTTP API for Collection
 */
public class HttpCollectionAPIIT extends BaseIT {
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person instanceAdminUser;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person projectAdminUser;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person depositorUser;
    
    @Autowired
    protected UiUrlConfig urlConfig;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private AuthorizedDepositorRequest authorizedDepositorRequest;
    
    private HttpPost instanceAdminLogin;
    
    private HttpGet logout;
    
    private HttpPost projectAdminLogin;
    
    private HttpPost depositorLogin;
    
    private static Project project;
    
    private static Collection collectionOne;
    
    private static Collection collectionTwo;
    
    private static String collectionOne_etag;
    
    private static DateTime modifiedDate;
    
    private static Collection collection;
    
    // HttpClient to use for testing url calls
    private HttpClient httpClient = new DefaultHttpClient();
    
    @Autowired
    BusinessObjectBuilder businessObjectBuilder;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    @Before
    public void setup() throws IllegalStateException, ClientProtocolException, IOException, URISyntaxException,
            InvalidXmlException {
        
        instanceAdminLogin = reqFactory.createLoginRequest(instanceAdminUser).asHttpPost();
        projectAdminLogin = reqFactory.createLoginRequest(projectAdminUser).asHttpPost();
        depositorLogin = reqFactory.createLoginRequest(depositorUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        // Set up hc for the tests
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(httpClient, instanceAdminLogin, 300, 399, "Unable to login as admin user!");
            
            project = new Project();
            project.setName("Test_Project_HttpCollectionAPI");
            project.setDescription("Test project");
            project.addNumber("314159");
            project.setFundingEntity("Sayeed");
            project.setStartDate(new DateTime(2012, 5, 4, 0, 0));
            project.setEndDate(new DateTime(2013, 12, 23, 0, 0));
            project.addPi(projectAdminUser.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(httpClient);
            
            collectionOne = new Collection();
            collectionOne.setTitle("CollectionOne");
            collectionOne.setSummary("CollectionOne for HttpCollectionAPI Integration Test");
            collectionOne.setDepositorId(instanceAdminUser.getId());
            collectionOne.setId(obtainId(Types.COLLECTION));
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collectionOne.setCreators(creators);
            collectionOne.setParentProjectId(project.getId());
            
            collectionTwo = new Collection();
            collectionTwo.setTitle("CollectionTwo");
            collectionTwo.setSummary("collectionTwo for HttpCollectionAPI Integration Test");
            collectionTwo.setId(obtainId(Types.COLLECTION));
            collectionTwo.setCreators(creators);
            collectionTwo.setParentProjectId(project.getId());
            
            createCollectionRequest.setCollection(collectionOne);
            createCollectionRequest.setProjectId(project.getId());
            HttpAssert.assertStatus(httpClient, createCollectionRequest.asHttpPost(), 302);
            HttpAssert.assertStatus(httpClient, new HttpGet(urlConfig.getViewCollectionUrl(collectionOne.getId())
                    .toURI()), 200, "Unable to create collection " + collectionOne);
            
            createCollectionRequest.setCollection(collectionTwo);
            createCollectionRequest.setProjectId(project.getId());
            httpClient.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(httpClient, new HttpGet(urlConfig.getViewCollectionUrl(collectionTwo.getId())
                    .toURI()), 200, "Unable to create collection " + collectionTwo);
            
            HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
            
            Collection retrievedCollection = archiveSupport.pollAndQueryArchiveForCollection(collectionOne.getId());
            collectionOne_etag = ETagCalculator.calculate(String.valueOf(retrievedCollection.hashCode()));
            areObjectsSeeded = true;
        }
        
    }
    
    @Test
    public void testGetCollectionByDepositor() throws Exception {
        // Log in as instance admin to assign collection depositor
        HttpAssert.assertStatus(httpClient, instanceAdminLogin, 300, 399, "Unable to login as admin user!");
        
        // assigning collection depositor role
        authorizedDepositorRequest.setAuthorizedUserForCollection(depositorUser.getEmailAddress(),
                collectionOne.getId());
        HttpAssert.assertStatus(httpClient, authorizedDepositorRequest.asHttpPost(), 300, 399,
                "Unable to authorize user " + depositorUser + " for deposit!");
        // Log out as the instance admin
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
        
        // Log in as depositor to test
        HttpAssert.assertStatus(httpClient, depositorLogin, 300, 399, "Unable to login as admin user!");
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, null, null);
        
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        verifyGoodResponse(authorizedResponse);
    }
    
    /**
     * Request as non depositor. Expects good response
     * 
     * @throws Exception
     */
    @Test
    public void testGetCollectionByUnauthorizedUser() throws Exception {
        
        // User in depositor log in request is not set as depositor for collection one
        
        // Log in as depositor to test
        HttpAssert.assertStatus(httpClient, depositorLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        verifyGoodResponse(authorizedResponse);
    }
    
    @Test
    public void testGetCollectionByUnauthenticatedUser() throws Exception {
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, null, null);
        
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        verifyGoodResponse(authorizedResponse);
    }
    
    @Test
    public void testRetrieveSpecificCollectionForInstanceAdmin() throws Exception {
        HttpAssert.assertStatus(httpClient, instanceAdminLogin, 300, 399, "Unable to login as admin user!");
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, null, null);
        
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        verifyGoodResponse(authorizedResponse);
    }
    
    @Test
    public void testRetrieveSpecificCollectionForProjectAdmin() throws Exception {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, null, null);
        
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        verifyGoodResponse(authorizedResponse);
    }
    
    /**
     * Tests that the Collection is returned if there is no match for the if-none-match etag. Expected Return Code: 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testIfNoneMatchWithNoMatch() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, null, "foo");
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get collection: " + collectionOne.getId() + "status code: "
                        + authorizedResponse.getStatusLine(), 200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<Collection> collections = bop.getCollections();
        assertNotNull(collections);
        
        assertEquals(1, collections.size());
        
        Iterator<Collection> iter = collections.iterator();
        Collection returnedCollection = iter.next();
        
        assertEquals(collectionOne.getId(), returnedCollection.getId());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that the Collection is not returned if there is a match for the if-none-match etag. Expected Return Code:
     * 412
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testIfNoneMatchWithMatch() throws Exception, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, null, collectionOne_etag);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals("Unexpectedly able to get collection: " + collectionOne.getId() + "status code: "
                + authorizedResponse.getStatusLine(), 412, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests the collection is returned if the if match header matches the etag of the collection . Expected Return
     * Code: 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testIfMatchWithMatch() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, collectionOne_etag, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get collection: " + collectionOne.getId() + "status code: "
                        + authorizedResponse.getStatusLine(), 200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<Collection> collections = bop.getCollections();
        assertNotNull(collections);
        
        assertEquals(1, collections.size());
        
        Iterator<Collection> iter = collections.iterator();
        Collection returnedCollection = iter.next();
        
        // Since deposit date is set during the deposit process the original collection doesn't have a value for
        // deposit date -> set its date to the returned collection's deposit date to compare the rest of the fields.
        collectionOne.setDepositDate(returnedCollection.getDepositDate());
        assertEquals(collectionOne, returnedCollection);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that the collection is not returned if their is no match for the if match etag. Expected Return Code: 412
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testIfMatchWithNoMatch() throws URISyntaxException, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), null, null, "foo", null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get collection: " + collectionOne.getId() + "status code: "
                        + authorizedResponse.getStatusLine(), 412, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that a non supported accept header will return a 406 status code. Expected Return Code: 406
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testNonSupportedAcceptHeader() throws URISyntaxException, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(collectionOne.getId()), "foo", null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get collection: " + collectionOne.getId() + "status code: "
                        + authorizedResponse.getStatusLine(), 406, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Test attempting to get non-existing collection. Expected returned code: 404
     */
    @Test
    public void testGetNonExistingCollection() throws Exception {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart("http://collection/45234"), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals("This collection: " + collectionOne.getId() + " is expected not to be found. "
                + authorizedResponse.getStatusLine(), 404, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    private void verifyGoodResponse(HttpResponse response) throws IOException, InvalidXmlException {
        assertEquals(
                "Unable to get collection: " + collectionOne.getId() + " status code: " + response.getStatusLine(),
                200, response.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(response.getEntity().getContent());
        assertNotNull(bop);
        assertEquals(1, bop.getCollections().size());
        Collection retrievedCollection = bop.getCollections().iterator().next();
        assertEquals(collectionOne.getId(), retrievedCollection.getId());
        assertEquals(collectionOne.getTitle(), retrievedCollection.getTitle());
        assertEquals(collectionOne.getSummary(), retrievedCollection.getSummary());
        assertEquals(collectionOne.getDepositorId(), retrievedCollection.getDepositorId());
        
        response.getEntity().getContent().close();
        freeResponse(response);
    }
    
    /**
     * Builds an HttpGet to use for collection get calls.
     * 
     * @param id
     *            The id of the collection to get
     * @return The completed http request that can be passed to HttpClient
     * @throws java.io.UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpGet buildGetRequest(String id, String acceptHeader, DateTime ifModifiedSinceDate, String ifMatchHeader,
            String ifNoneMatchHeader) throws UnsupportedEncodingException, URISyntaxException {
        HttpGet request = null;
        
        String arguments = "/collection";
        
        if (id != null && !id.isEmpty()) {
            arguments += "/" + id;
        }
        
        URL baseUrl = urlConfig.getBaseUrl();
        
        URI uri = URIUtils
                .createURI(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), arguments, null, null);
        
        request = new HttpGet(uri);
        request.addHeader("Content-Type", "text/xml");
        
        if (acceptHeader != null && !acceptHeader.isEmpty()) {
            request.addHeader("Accept", acceptHeader);
        }
        
        if (ifModifiedSinceDate != null) {
            request.addHeader("If-Modified-Since", DateUtility.toRfc822(ifModifiedSinceDate));
        }
        
        if (ifMatchHeader != null && !ifMatchHeader.isEmpty()) {
            request.addHeader("If-Match", ifMatchHeader);
        }
        
        if (ifNoneMatchHeader != null && !ifNoneMatchHeader.isEmpty()) {
            request.addHeader("If-None-Match", ifNoneMatchHeader);
        }
        
        return request;
    }
    
    // Returns just the id part of the identifier
    private String getIDPart(String id) {
        String idPart = "";
        
        String[] idParts = id.split("/");
        
        if (idParts.length != 0) {
            idPart = idParts[idParts.length - 1];
        }
        
        return idPart;
    }
    
    private String obtainId(Types idType) throws IOException {
        HttpResponse response = httpClient.execute(reqFactory.createIdApiRequest(idType).asHttpPost());
        assertEquals("Expected 200 while attepmting to create an identifier for a " + idType.getTypeName(), 201,
                response.getStatusLine().getStatusCode());
        assertNotNull("Expected a Location HTTP header to be present in the response.", response.getHeaders("Location"));
        assertEquals("Expected one Location HTTP header in the response.", 1, response.getHeaders("Location").length);
        String id = response.getHeaders("Location")[0].getValue();
        HttpAssert.free(response);
        assertNotNull("Expected a non-null identifier!", id);
        assertFalse("Expected a not-empty identifier", id.trim().length() == 0);
        return id.trim();
    }
}
