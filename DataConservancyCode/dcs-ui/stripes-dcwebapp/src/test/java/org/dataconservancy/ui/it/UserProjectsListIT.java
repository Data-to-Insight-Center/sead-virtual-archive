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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.RequestFactory;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.RegistrationStatus;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by IntelliJ IDEA. User: jrm Date: 5/30/12 Time: 8:37 AM To change this template use File | Settings | File
 * Templates.
 */
public class UserProjectsListIT extends BaseIT {
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person defaultAdmin;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person defaultUser;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person approvedUser;
    
    @Autowired
    protected UiUrlConfig urlConfig;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    protected RequestFactory reqFactory;
    
    private HttpPost adminLogin;
    
    private HttpGet logout;
    
    private HttpPost basicUserLogin;
    
    private HttpPost approvedUserLogin;
    
    private static Project projectOne;
    
    private static Project projectTwo;
    
    private static Project projectThree;
    
    private static Collection collectionOne;
    
    private static Collection collectionTwo;
    
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
        
        adminLogin = reqFactory.createLoginRequest(defaultAdmin).asHttpPost();
        basicUserLogin = reqFactory.createLoginRequest(defaultUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        approvedUserLogin = reqFactory.createLoginRequest(approvedUser).asHttpPost();
        
        // Set up hc for the tests
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
            
            projectOne = new Project();
            projectOne.setName("Test_Project_1");
            projectOne.setDescription("Test Project For Card 999");
            projectOne.addNumber("9991");
            projectOne.setFundingEntity("Wallet");
            projectOne.setStartDate(new DateTime(2012, 5, 4, 0, 0));
            projectOne.setEndDate(new DateTime(2013, 12, 23, 0, 0));
            projectOne.addPi(defaultAdmin.getId());
            
            projectTwo = new Project();
            projectTwo.setName("Test_Project_2");
            projectTwo.setDescription("Test Project Two For Card 999");
            projectTwo.addNumber("9992");
            projectTwo.setFundingEntity("Wallet");
            projectTwo.setStartDate(new DateTime(2012, 5, 4, 0, 0));
            projectTwo.setEndDate(new DateTime(2013, 12, 23, 0, 0));
            projectTwo.addPi(defaultUser.getId());
            
            projectThree = new Project();
            projectThree.setName("Test_Project_3");
            projectThree.setDescription("Test Project Three For Card 999");
            projectThree.addNumber("9993");
            projectThree.setFundingEntity("Wallet");
            projectThree.setStartDate(new DateTime(2012, 5, 4, 0, 0));
            projectThree.setEndDate(new DateTime(2013, 12, 23, 0, 0));
            projectThree.addPi(defaultUser.getId());
            
            projectOne = reqFactory.createProjectApiAddRequest(projectOne).execute(httpClient);
            
            projectTwo = reqFactory.createProjectApiAddRequest(projectTwo).execute(httpClient);
            
            projectThree = reqFactory.createProjectApiAddRequest(projectThree).execute(httpClient);
            
            // add some collectionOne to Project One
            collectionOne = new Collection();
            collectionOne.setTitle("CollectionOne");
            collectionOne.setSummary("CollectionOne-Foo");
            collectionOne.setId("99911");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collectionOne.setCreators(creators);
            
            collectionTwo = new Collection();
            collectionTwo.setTitle("CollectionTwo");
            collectionTwo.setSummary("collectionTwo - foo");
            collectionTwo.setId("99922");
            collectionTwo.setCreators(creators);
            
            createCollectionRequest.setCollection(collectionOne);
            createCollectionRequest.setProjectId(projectOne.getId());
            httpClient.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(httpClient, new HttpGet(urlConfig.getViewCollectionUrl(collectionOne.getId())
                    .toURI()), 200, "Unable to create collection " + collectionOne);
            
            createCollectionRequest.setCollection(collectionTwo);
            createCollectionRequest.setProjectId(projectOne.getId());
            httpClient.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(httpClient, new HttpGet(urlConfig.getViewCollectionUrl(collectionTwo.getId())
                    .toURI()), 200, "Unable to create collection " + collectionTwo);
            
            HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
            
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests getting all projects for an instance administrator. Should return all three projects.
     * 
     * @throws java.io.IOException
     * @throws org.apache.http.client.ClientProtocolException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     * @throws IllegalStateException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testGetProjectsForInstanceAdmin() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        // get all projects for the instance admin
        HttpGet getRequest = buildGetProjectRequest(null, false);
        HttpResponse authorizedResponse = httpClient.execute(getRequest);
        
        assertEquals(
                "Unable to get projects for defaultAdmin: " + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        // should see all three setup projects
        assertTrue(bop.getProjects().size() >= 3);
        assertTrue(bop.getProjects().contains(projectOne));
        assertTrue(bop.getProjects().contains(projectTwo));
        assertTrue(bop.getProjects().contains(projectThree));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    @Test
    public void testGetProjectsForProjectAdmin() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        HttpAssert.assertStatus(httpClient, basicUserLogin, 300, 399, "Unable to login as admin user!");
        
        // get all projects for the instance admin
        HttpGet getRequest = buildGetProjectRequest(null, false);
        HttpResponse authorizedResponse = httpClient.execute(getRequest);
        
        assertEquals(
                "Unable to get projects for defaultAdmin: " + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        // should see setup projects two and three
        assertTrue(bop.getProjects().size() >= 2);
        assertTrue(bop.getProjects().contains(projectTwo));
        assertTrue(bop.getProjects().contains(projectThree));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
    }
    
    @Test
    public void testGetProjectsForUserWithNoProjects() throws ClientProtocolException, IOException,
            IllegalStateException, InvalidXmlException, URISyntaxException {
        
        Person newPerson = new Person();
        newPerson.setFirstNames("Willy-bean");
        newPerson.setLastNames("Sirk");
        newPerson.setPrefix("Mr.");
        newPerson.setSuffix("II");
        newPerson.setMiddleNames("Middle");
        newPerson.setPreferredPubName("W. Sirk");
        newPerson.setBio("Some bio for the user.");
        newPerson.setWebsite("www.somewebsite.com");
        newPerson.setPassword("iambiggerthanAlfee");
        newPerson.setEmailAddress("willardhasemail@too.com");
        newPerson.setPhoneNumber("507.555.1212");
        newPerson.setJobTitle("Willy-bean Scientist");
        newPerson.setDepartment("Willy-bean Department");
        newPerson.setCity("Baltimore");
        newPerson.setState("Maryland");
        newPerson.setInstCompany("Willy-bean Institution/Company");
        newPerson.setInstCompanyWebsite("www.Willy-beanInstitutionCompany.com");
        newPerson.setRegistrationStatus(RegistrationStatus.PENDING);
        newPerson.setExternalStorageLinked(false);
        newPerson.setDropboxAppKey("SomeKey");
        newPerson.setDropboxAppSecret("SomeSecret");
        
        createNewPerson(newPerson);
        
        HttpPost newUserLogin = reqFactory.createLoginRequest(newPerson).asHttpPost();
        
        HttpAssert.assertStatus(httpClient, newUserLogin, 300, 399, "Unable to login as new user!");
        
        // get all projects for the new user
        HttpGet getRequest = buildGetProjectRequest(null, false);
        HttpResponse authorizedResponse = httpClient.execute(getRequest);
        
        assertEquals("Unable to get projects for approved user: status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        // should see setup projects two and three
        assertEquals(0, bop.getProjects().size());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout approved user!");
    }
    
    @Test
    public void testGetProjectsForNotLoggedInUser() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
        
        HttpGet getRequest = buildGetProjectRequest(null, false);
        HttpResponse authorizedResponse = httpClient.execute(getRequest);
        assertEquals("Did not get Authorization error for not logged in user: " + authorizedResponse.getStatusLine(),
                401, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
    }
    
    /**
     * Builds an HttpGet to use for project get calls.
     * 
     * @param id
     *            The id of the project to update
     * @return The completed http request that can be passed to HttpClient
     * @throws java.io.UnsupportedEncodingException
     * @throws java.net.URISyntaxException
     */
    private HttpGet buildGetProjectRequest(String id, boolean collections) throws UnsupportedEncodingException,
            URISyntaxException {
        HttpGet request = null;
        
        String arguments = "/project";
        
        if (id != null && !id.isEmpty()) {
            arguments += "/" + id;
        }
        
        if (collections) {
            arguments += "/collections";
        }
        
        URL baseUrl = urlConfig.getBaseUrl();
        
        URI uri = URIUtils
                .createURI(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), arguments, null, null);
        
        request = new HttpGet(uri);
        request.addHeader("Content-Type", "text/xml");
        return request;
    }
    
    private void createNewPerson(Person newPerson) throws URISyntaxException, IOException, InvalidXmlException {
        
        final DefaultHttpClient hc = new DefaultHttpClient();
        
        // Override the redirect strategy to redirect on POST. So we can just test for 200 statuses in the
        // unit test.
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        hc.setRedirectStrategy(new RedirectStrategy() {
            
            @Override
            public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
                    throws ProtocolException {
                if (!redirectStrategy.isRedirected(request, response, context)) {
                    return response.getStatusLine().getStatusCode() == 302;
                }
                return true;
            }
            
            @Override
            public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
                    throws ProtocolException {
                return redirectStrategy.getRedirect(request, response, context);
            }
        });
        // Register a new user
        HttpAssert.assertStatus(hc, reqFactory.createRegisterRequest(newPerson).asHttpPost(), 200);
        
        // Login as admin
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(defaultAdmin).asHttpPost(), 200);
        
        // Approve registration
        HttpAssert.assertStatus(hc, reqFactory.createApproveRegistrationRequest(newPerson).asHttpPost(), 200);
        
        // Logout admin
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 200);
        
    }
    
}
