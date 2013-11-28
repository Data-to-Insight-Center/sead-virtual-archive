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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.model.builder.InvalidXmlException;
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
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class HttpProjectIT extends BaseIT {
    
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
    
    private HttpPost adminLogin;
    
    private HttpGet logout;
    
    private HttpPost basicUserLogin;
    
    private static Project project;
    
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
        
        // Set up hc for the tests
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
            
            project = new Project();
            project.setName("Test_Project");
            project.setDescription("foo");
            project.addNumber("12");
            project.setFundingEntity("Straight cash homey");
            project.setStartDate(new DateTime(2012, 5, 4, 0, 0));
            project.setEndDate(new DateTime(2013, 12, 23, 0, 0));
            project.addPi(defaultAdmin.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(httpClient);
            
            collectionOne = new Collection();
            collectionOne.setTitle("CollectionOne");
            collectionOne.setSummary("CollectionOne-Foo");
            collectionOne.setId("12345");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collectionOne.setCreators(creators);
            
            collectionTwo = new Collection();
            collectionTwo.setTitle("CollectionTwo");
            collectionTwo.setSummary("collectionTwo - foo");
            collectionTwo.setId("54321");
            collectionTwo.setCreators(creators);
            
            createCollectionRequest.setCollection(collectionOne);
            createCollectionRequest.setProjectId(project.getId());
            httpClient.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(httpClient, new HttpGet(urlConfig.getViewCollectionUrl(collectionOne.getId())
                    .toURI()), 200, "Unable to create collection " + collectionOne);
            
            createCollectionRequest.setCollection(collectionTwo);
            createCollectionRequest.setProjectId(project.getId());
            httpClient.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(httpClient, new HttpGet(urlConfig.getViewCollectionUrl(collectionTwo.getId())
                    .toURI()), 200, "Unable to create collection " + collectionTwo);
            
            HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
            
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests updating a project metadata. Tests cases: updating the metadata of a project
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws InvalidXmlException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    @Test
    public void testUpdateProject() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        Project newProject = new Project(project);
        newProject.setName("Update");
        newProject.setDescription("foo-%2fupdate");
        newProject.setEndDate(new DateTime(2014, 2, 10, 0, 00));
        
        HttpPut request = buildUpdateRequest(getIDPart(project.getId()), newProject);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to update project: " + project.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<Project> projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        Project returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        assertEquals(newProject, returnedProject);
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests updating a project metadata. Tests cases: updating the metadata of a project
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws InvalidXmlException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    @Test
    public void testUpdateProjectAdminList() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        // Test adding a new admin
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        Project newProject = new Project(project);
        newProject.addPi(defaultUser.getId());
        
        HttpPut request = buildUpdateRequest(getIDPart(project.getId()), newProject);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to update project: " + project.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<Project> projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        Project returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        assertEquals(2, returnedProject.getPis().size());
        
        // Now test removing the admin
        newProject.removePi(defaultUser.getId());
        
        request = buildUpdateRequest(getIDPart(project.getId()), newProject);
        authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to update project: " + project.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        assertEquals(1, returnedProject.getPis().size());
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests the exception handling when updating a project Tests cases: Trying to update project without proper
     * permissions, Updating a project whose ID doesn't match the request, trying to update a project that doesn't
     * exist, adding a PI that isn't registered in the system.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws InvalidXmlException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    @Test
    public void testUpdateProjectExceptions() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        // Test updating project without permissions
        HttpAssert.assertStatus(httpClient, basicUserLogin, 300, 399, "Unable to login as basic user!");
        
        Project newProject = new Project(project);
        newProject.setName("Update");
        newProject.setDescription("foo-update");
        newProject.setEndDate(new DateTime(2014, 2, 10, 0, 00));
        
        HttpPut request = buildUpdateRequest(getIDPart(project.getId()), newProject);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to update project: " + getIDPart(project.getId()) + "status code: "
                        + authorizedResponse.getStatusLine(), 403, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
        
        // Test updating project with ID that doesn't match.
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        newProject = new Project(project);
        newProject.setId("foo");
        
        request = buildUpdateRequest(getIDPart(project.getId()), newProject);
        authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to update project: " + project.getId() + "status code: " + authorizedResponse.getStatusLine(),
                400, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        // Test adding PI that isn't registered in the system.
        Person dummy = new Person();
        dummy.setId("id:dummy");
        dummy.setEmailAddress("foo@fake.email");
        
        newProject.addPi(dummy.getId());
        
        request = buildUpdateRequest(getIDPart(project.getId()), newProject);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to update project: " + project.getId() + " status: " + authorizedResponse.getStatusLine(),
                400, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        // Test updating project that doesn't exist
        newProject = new Project(project);
        newProject.setId("foo");
        
        request = buildUpdateRequest("foo", newProject);
        authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to update project: " + project.getId() + "status code: " + authorizedResponse.getStatusLine(),
                404, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
    
    /**
     * Tests adding a project.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws InvalidXmlException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    @Test
    public void testAddProject() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        Project newProject = new Project();
        newProject.setName("New Project");
        newProject.setDescription("foo");
        newProject.addNumber("80");
        newProject.setFundingEntity("I like money");
        newProject.setStartDate(new DateTime(2012, 5, 4, 0, 0));
        newProject.setEndDate(new DateTime(2013, 12, 23, 0, 0));
        newProject.addPi(defaultAdmin.getId());
        
        HttpPost request = buildAddRequest(newProject);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to add project new project status code: " + authorizedResponse.getStatusLine(), 201,
                authorizedResponse.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<Project> projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());
        
        Project returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        // This is kind of wonky but we need to get the ID from the project we created and assign it to the project we
        // were trying to create.
        newProject.setId(returnedProject.getId());
        
        assertEquals(newProject, returnedProject);
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests the exception handling for adding a project. Test cases: Tests adding a project if not the system admin.
     * Tests that a bad request is thrown if the project is invalid.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws InvalidXmlException
     * @throws URISyntaxException
     */
    @Test
    public void testAddProjectExceptions() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        HttpAssert.assertStatus(httpClient, basicUserLogin, 300, 399, "Unable to login as basic user!");
        
        Project newProject = new Project();
        newProject.setName("New Project");
        newProject.setDescription("foo");
        newProject.setFundingEntity("I like money");
        newProject.setStartDate(new DateTime(2012, 5, 4, 0, 0));
        newProject.setEndDate(new DateTime(2013, 12, 23, 0, 0));
        newProject.addPi(defaultAdmin.getId());
        
        HttpPost request = buildAddRequest(newProject);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Expected to get a forbidden 403 but got: " + authorizedResponse.getStatusLine(), 403,
                authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
        
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        request = buildAddRequest(new Project());
        authorizedResponse = httpClient.execute(request);
        assertEquals("Expected a 406 for validation error but got: " + authorizedResponse.getStatusLine(), 406,
                authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests getting the collections that belong to the project. Test Cases: Getting a list of collections on a project
     * with collections, getting collections on a project with no collections.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws InvalidXmlException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    @Test
    public void testGetProjectCollections() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(project.getId()), true);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to get project collections: " + project.getId() + "status code: "
                        + authorizedResponse.getStatusLine(), 200, authorizedResponse.getStatusLine().getStatusCode());
        Bop returnedBop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(returnedBop);
        
        Set<Collection> collections = returnedBop.getCollections();
        assertNotNull(collections);
        assertEquals(2, collections.size());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        // Test an empty list is returned for a project with no collections
        Project newProject = new Project();
        newProject.setName("New Project");
        newProject.setDescription("foo");
        newProject.addNumber("80");
        newProject.setFundingEntity("I like money");
        newProject.setStartDate(new DateTime(2012, 5, 4, 0, 0));
        newProject.setEndDate(new DateTime(2013, 12, 23, 0, 0));
        newProject.addPi(defaultAdmin.getId());
        newProject = reqFactory.createProjectApiAddRequest(newProject).execute(httpClient);
        
        request = buildGetRequest(getIDPart(newProject.getId()), true);
        authorizedResponse = httpClient.execute(request);
        assertEquals(
                "Unable to get project collections: " + newProject.getId() + "status code: "
                        + authorizedResponse.getStatusLine(), 200, authorizedResponse.getStatusLine().getStatusCode());
        
        returnedBop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(returnedBop);
        
        collections = returnedBop.getCollections();
        assertNotNull(collections);
        assertEquals(0, collections.size());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
    }
    
    /**
     * Tests the exception handling for getting a project collections. Test cases: Tests getting collection list if not
     * an admin of the project, test getting collections on a project that doesn't exist.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     * @throws IllegalStateException
     * @throws InvalidXmlException
     * @throws URISyntaxException
     */
    @Test
    public void testGetProjectCollectionsExceptions() throws ClientProtocolException, IOException,
            IllegalStateException, InvalidXmlException, URISyntaxException {
        
        // Test getting collection as non project admin
        HttpAssert.assertStatus(httpClient, basicUserLogin, 300, 399, "Unable to login as basic user!");
        
        HttpGet request = buildGetRequest(getIDPart(project.getId()), true);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Able to get project with an non project admin " + project.getId() + "status code: "
                + authorizedResponse.getStatusLine(), 403, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
        
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        request = buildGetRequest("foo", true);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Expected a 404 for a getting project foo status code: " + authorizedResponse.getStatusLine(),
                404, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
    }
    
    /**
     * Tests getting a project.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws InvalidXmlException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    @Test
    public void testGetProject() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(project.getId()), false);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get project: " + project.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        assertEquals(1, bop.getProjects().size());
        assertEquals(project, bop.getProjects().iterator().next());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
    
    /**
     * Tests getting a project that does not exist.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws InvalidXmlException
     * @throws IllegalStateException
     * @throws URISyntaxException
     */
    @Test
    public void testGetNonExistingProject() throws ClientProtocolException, IOException, IllegalStateException,
            InvalidXmlException, URISyntaxException {
        
        HttpAssert.assertStatus(httpClient, adminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest("cowsaregreat", false);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(404, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
    
    /**
     * Builds an HttpPut to use for updating a project.
     * 
     * @param project
     *            That should be serialized and passed in the request.
     * @param id
     *            The id of the project to update
     * @return The completed http request that can be passed to HttpClient
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpPut buildUpdateRequest(String id, Project project) throws UnsupportedEncodingException,
            URISyntaxException {
        HttpPut request = null;
        
        String arguments = "/project";
        
        if (id != null && !id.isEmpty()) {
            arguments += "/" + id;
        }
        
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildProject(project, sink);
        ByteArrayEntity projectEntity = new ByteArrayEntity(sink.toByteArray());
        projectEntity.setContentEncoding("UTF-8");
        
        URL baseUrl = urlConfig.getBaseUrl();
        
        URI uri = URIUtils
                .createURI(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), arguments, null, null);
        
        request = new HttpPut(uri);
        request.addHeader("Content-Type", "text/xml");
        request.setEntity(projectEntity);
        return request;
    }
    
    /**
     * Builds an HttpPost to use for adding a project
     * 
     * @param project
     *            That should be serialized and passed in the request.
     * @return The completed http request that can be passed to HttpClient
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpPost buildAddRequest(Project project) throws UnsupportedEncodingException, URISyntaxException {
        HttpPost request = null;
        
        String arguments = "/project";
        
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildProject(project, sink);
        ByteArrayEntity projectEntity = new ByteArrayEntity(sink.toByteArray());
        projectEntity.setContentEncoding("UTF-8");
        
        URL baseUrl = urlConfig.getBaseUrl();
        
        URI uri = URIUtils
                .createURI(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), arguments, null, null);
        
        request = new HttpPost(uri);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.setEntity(projectEntity);
        return request;
    }
    
    /**
     * Builds an HttpGet to use for project get calls.
     * 
     * @param id
     *            The id of the project to update
     * @return The completed http request that can be passed to HttpClient
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpGet buildGetRequest(String id, boolean collections) throws UnsupportedEncodingException,
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
    
    // Returns just the id part of the identifier
    private String getIDPart(String id) {
        String idPart = "";
        
        String[] idParts = id.split("/");
        
        if (idParts.length != 0) {
            idPart = idParts[idParts.length - 1];
        }
        
        return idPart;
    }
}