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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.CreateIdApiRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.UpdateCollectionRequest;
import org.dataconservancy.ui.it.support.ViewCollectionDetailsRequest;
import org.dataconservancy.ui.model.Address;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 7/25/12 Time: 11:28 AM To change this template use File | Settings |
 * File Templates.
 */
@Ignore("EZID service down")
public class EZIDCitableLocatorIT extends BaseIT {
    private HttpPost systemAdminLogin;
    private HttpPost registeredUserLogin;
    private HttpGet logout;
    private static boolean areObjectsSeeded = false;
    private HttpClient httpClient = new DefaultHttpClient();
    private static Project project;
    private static Collection collectionForReservingEZID;
    private static Collection collectionForConfirmingEZID;
    ContactInfo startingContact1;
    PersonName creator1;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person projectAdmin;
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person systemAdmin;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person registeredUser;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    private UpdateCollectionRequest updateCollectionRequest;
    
    @Autowired
    private ViewCollectionDetailsRequest viewCollectionDetailsRequest;
    
    // counter for how many times we have tried to poll the archive
    private int pollCount;
    // maximum number of times to poll
    private final int maxPollTimes = 60;
    
    @Before
    public void setUp() throws IOException, InvalidXmlException {
        // Login to start set up project/collection
        systemAdminLogin = reqFactory.createLoginRequest(systemAdmin).asHttpPost();
        registeredUserLogin = reqFactory.createLoginRequest(registeredUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            HttpAssert.assertStatus(httpClient, systemAdminLogin, 302);
            // Set up project for the collection:
            project = new Project();
            
            project.setDescription("Things about dogs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("Dollar tree");
            project.setStartDate(DateTime.now());
            project.setName("The dogs' house");
            project.addNumber("1234");
            project.addPi(projectAdmin.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(httpClient);
            // Make sure the project was create - loadable
            HttpAssert.assertStatus(httpClient, urlConfig.getProjectUrl(project.getId()).toString(), 200);
            
            // Set up a collection.
            collectionForReservingEZID = new Collection();
            
            // Create the ID
            CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
            collectionForReservingEZID.setId(collectionIdRequest.execute(httpClient));
            
            collectionForReservingEZID.setTitle("Summer tales of dogs");
            collectionForReservingEZID.setSummary("Dogs are people too.");
            collectionForReservingEZID.setPublicationDate(DateTime.now());
            
            // *****************************************************
            // Set up ALTERNATE IDs for the starting collections
            // *****************************************************
            collectionForReservingEZID.getAlternateIds().add("id:alfeelikestomatoes");
            collectionForReservingEZID.getAlternateIds().add("id:willardlikesblueberries");
            // *****************************************************
            
            // **********************************************
            // Set up CREATORS for the starting collections
            // **********************************************
            String[] given1 = { "Joe", "Bob" };
            String[] middle1 = { "Franklin", "Delano" };
            String[] family1 = { "Smith", "Doe" };
            
            creator1 = new PersonName("Mr.", given1, middle1, family1, "III");
            
            collectionForReservingEZID.addCreator(creator1);
            // *********************************************
            
            // **********************************************
            // Set up CONTACT INFOs for the starting collections
            // **********************************************
            startingContact1 = new ContactInfo();
            startingContact1.setName("Mr. Alford Can-go Sirk");
            startingContact1.setRole("Fly hunter");
            startingContact1.setEmailAddress("fliestastegood@nature.com");
            
            Address startingAddress = new Address();
            startingAddress.setStreetAddress("Deck Door");
            startingAddress.setCity("Back Of House");
            startingAddress.setCountry("What country?");
            startingContact1.setPhysicalAddress(startingAddress);
            
            collectionForReservingEZID.addContactInfo(startingContact1);
            
            createCollectionRequest.setCollection(collectionForReservingEZID);
            createCollectionRequest.setProjectId(project.getId());
            HttpAssert.assertStatus(httpClient, createCollectionRequest.asHttpPost(), 302);
            assertNotNull(archiveSupport.pollAndQueryArchiveForCollectionDu(collectionForReservingEZID.getId()));
            
            collectionForConfirmingEZID = new Collection(collectionForReservingEZID);
            // give colelctionForConfirmingEZID a new business ID
            collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
            collectionForConfirmingEZID.setId(collectionIdRequest.execute(httpClient));
            // change up its title just for kick
            collectionForConfirmingEZID.setTitle("Shiny");
            
            // depositing the collectionForConfirmingEZID into the archive
            createCollectionRequest.setCollection(collectionForConfirmingEZID);
            createCollectionRequest.setProjectId(project.getId());
            HttpAssert.assertStatus(httpClient, createCollectionRequest.asHttpPost(), 302);
            assertNotNull(archiveSupport.pollAndQueryArchiveForCollectionDu(collectionForConfirmingEZID.getId()));
            
            httpClient.execute(logout).getEntity().getContent().close();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Test that CitableLocator link is visible to authorized user (in this case system admin)
     * 
     * @throws IOException
     */
    @Test
    public void testGetCitableLocatorLinkVisibleToAuthorizedUser() throws IOException {
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        viewCollectionDetailsRequest.setCollectionIdToView(collectionForReservingEZID.getId());
        HttpResponse authorizedResponse = httpClient.execute(viewCollectionDetailsRequest.asHttpPost());
        StatusLine statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        
        String content = IOUtils.toString(authorizedResponse.getEntity().getContent());
        assertTrue(content.contains("citable_locator_confirmation.action?reserveDOI="));
        freeResponse(authorizedResponse);
    }
    
    /**
     * Test that CitableLocator link is not visible to unauthorized user
     * 
     * @throws IOException
     */
    @Test
    public void testGetCitableLocatorLinkUnavailableToUnauthorizedUser() throws IOException {
        httpClient.execute(registeredUserLogin).getEntity().getContent().close();
        viewCollectionDetailsRequest.setCollectionIdToView(collectionForReservingEZID.getId());
        HttpResponse authorizedResponse = httpClient.execute(viewCollectionDetailsRequest.asHttpPost());
        StatusLine statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        
        String content = IOUtils.toString(authorizedResponse.getEntity().getContent());
        assertTrue(!content.contains("citable_locator_confirmation.action?reserveDOI="));
        freeResponse(authorizedResponse);
    }
    
    /**
     * Test reserving/creating ezid by authorized user Expected - returned code: 200 - landing page:
     * citable_locator_confirmation.jsp
     */
    @Test
    public void testCreateEZIDCitableLocatorByAuthorizedUser() throws IOException, InterruptedException {
        
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        
        Object archivedObject = null;
        // Get collection from the archive to get the state before reserving Id action
        pollCount = 0;
        do {
            archivedObject = archiveSupport.pollAndQueryArchiveForCollection(collectionForReservingEZID.getId());
            Thread.sleep(1000L);
        }
        while (archivedObject == null && pollCount++ < maxPollTimes);
        
        // assert collection did not have a citable locator value
        assertNull(((Collection) archivedObject).getCitableLocator());
        HttpResponse response = httpClient.execute(reqFactory.createCitableLocatorRequest(
                collectionForReservingEZID.getId(), null).reserveAsHttpGet());
        StatusLine statusLine = response.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        String content = IOUtils.toString(response.getEntity().getContent());
        freeResponse(response);
        // Get collection from the archive to get the state after reserving Id action
        pollCount = 0;
        do {
            archivedObject = archiveSupport.pollAndQueryArchiveForCollection(collectionForReservingEZID.getId());
            Thread.sleep(1000L);
        }
        while (archivedObject == null && pollCount++ < maxPollTimes);
        // assert that reserving a ezid did not give value to the collection's citable locator.
        assertNull(((Collection) archivedObject).getCitableLocator());
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Test reserving/creating ezid by unauthorized user Expected - returned code: 403
     */
    @Test
    public void testCreateEZIDCitableLocatorByUnauthorizedUser() throws IOException {
        httpClient.execute(registeredUserLogin).getEntity().getContent().close();
        HttpAssert.assertStatus(httpClient,
                reqFactory.createCitableLocatorRequest(collectionForReservingEZID.getId(), null).reserveAsHttpGet(),
                403);
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Test confirming/saving ezid by authorized user Expected - returned code 302 - landing page: collection_view.jsp
     */
    @Test
    public void testConfirmingEZIDCitableLocatorByAuthorizedUser() throws IOException, InterruptedException {
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        Object archivedObject = null;
        // Get collection from the archive to get the state before
        pollCount = 0;
        do {
            archivedObject = archiveSupport.pollAndQueryArchiveForCollection(collectionForConfirmingEZID.getId());
            Thread.sleep(1000L);
        }
        while (archivedObject == null && pollCount++ < maxPollTimes);
        
        // assert collection did not have a citable locator value
        assertNull(((Collection) archivedObject).getCitableLocator());
        
        // reserve an ID to be confirmed
        HttpResponse response = httpClient.execute(reqFactory.createCitableLocatorRequest(
                collectionForConfirmingEZID.getId(), null).reserveAsHttpGet());
        StatusLine statusLine = response.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        String content = IOUtils.toString(response.getEntity().getContent());
        String reservedID = getReservedEZID(content);
        freeResponse(response);
        
        HttpAssert.assertStatus(httpClient,
                reqFactory.createCitableLocatorRequest(collectionForConfirmingEZID.getId(), reservedID)
                        .confirmAsHttpPost(), 302);
        
        pollCount = 0;
        do {
            archivedObject = archiveSupport.pollAndQueryArchiveForCollection(collectionForConfirmingEZID.getId());
            Thread.sleep(1000L);
        }
        while (archivedObject == null && pollCount++ < maxPollTimes);
        
        assertNotNull(((Collection) archivedObject).getCitableLocator());
        
        // make sure the "get citable locator" link is no longer visible to user:
        viewCollectionDetailsRequest.setCollectionIdToView(collectionForConfirmingEZID.getId());
        HttpResponse authorizedResponse = httpClient.execute(viewCollectionDetailsRequest.asHttpPost());
        statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        
        content = IOUtils.toString(authorizedResponse.getEntity().getContent());
        assertTrue(!content.contains("citable_locator_confirmation.action?reserveDOI="));
        
        // make sure the reserved Id is part of the rendered content
        assertTrue(content.contains(reservedID));
        freeResponse(authorizedResponse);
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Test confirming/saving ezid by unauthorized user Expected - returned code 403 - landing page: collection_view.jsp
     */
    @Test
    public void testConfirmingEZIDCitableLocatorByUnauthorizedUser() throws IOException {
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        HttpResponse createResponse = httpClient.execute(reqFactory.createCitableLocatorRequest(
                collectionForConfirmingEZID.getId(), null).reserveAsHttpGet());
        StatusLine statusLine = createResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        String content = IOUtils.toString(createResponse.getEntity().getContent());
        String ezid = getReservedEZID(content);
        createResponse.getEntity().getContent().close();
        freeResponse(createResponse);
        
        httpClient.execute(logout).getEntity().getContent().close();
        
        assertNotNull(ezid);
        
        // try to confirm the reservation - regular user should be denied
        httpClient.execute(registeredUserLogin).getEntity().getContent().close();
        HttpAssert.assertStatus(httpClient,
                reqFactory.createCitableLocatorRequest(collectionForConfirmingEZID.getId(), ezid).confirmAsHttpPost(),
                403);
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Test canceling/deleting ezid by authorized user Expected - return code 302 - landing page: collection_view.jsp
     */
    @Test
    public void testCancelingEZIDCitableLocatorByAuthorizedUser() throws IOException {
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        HttpResponse createResponse = httpClient.execute(reqFactory.createCitableLocatorRequest(
                collectionForReservingEZID.getId(), null).reserveAsHttpGet());
        StatusLine statusLine = createResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        String content = IOUtils.toString(createResponse.getEntity().getContent());
        String ezid = getReservedEZID(content);
        createResponse.getEntity().getContent().close();
        freeResponse(createResponse);
        
        assertNotNull(ezid);
        
        HttpResponse cancelResponse = httpClient.execute(reqFactory.createCitableLocatorRequest(
                collectionForReservingEZID.getId(), ezid).cancelAsHttpPost());
        statusLine = cancelResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 302, statusLine.getStatusCode());
        cancelResponse.getEntity().getContent().close();
        freeResponse(cancelResponse);
        Collection retrievedCollection = archiveSupport.pollAndQueryArchiveForCollection(collectionForReservingEZID
                .getId());
        ezid = retrievedCollection.getCitableLocator();
        assertNull(ezid);
        
        // MAKE SURE THAT THE LINK TO GET CITABLE LOCATOR IS STILL AVAILABLE
        viewCollectionDetailsRequest.setCollectionIdToView(collectionForReservingEZID.getId());
        HttpResponse authorizedResponse = httpClient.execute(viewCollectionDetailsRequest.asHttpPost());
        statusLine = authorizedResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        
        content = IOUtils.toString(authorizedResponse.getEntity().getContent());
        assertTrue(content.contains("citable_locator_confirmation.action?reserveDOI="));
        freeResponse(authorizedResponse);
        httpClient.execute(logout).getEntity().getContent().close();
        
    }
    
    /**
     * Test canceling/deleting ezid by unauthorized user Expected - return code 403 - landing page: collection_view.jsp
     */
    @Test
    public void testCancelingEZIDCitableLocatorByUnauthorizedUser() throws IOException {
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        HttpResponse createResponse = httpClient.execute(reqFactory.createCitableLocatorRequest(
                collectionForReservingEZID.getId(), null).reserveAsHttpGet());
        StatusLine statusLine = createResponse.getStatusLine();
        assertEquals(statusLine.getReasonPhrase(), 200, statusLine.getStatusCode());
        String content = IOUtils.toString(createResponse.getEntity().getContent());
        String ezid = getReservedEZID(content);
        createResponse.getEntity().getContent().close();
        freeResponse(createResponse);
        
        assertNotNull(ezid);
        
        httpClient.execute(logout).getEntity().getContent().close();
        
        // try to cancel the reservation - regular user should be denied
        httpClient.execute(registeredUserLogin).getEntity().getContent().close();
        HttpAssert.assertStatus(httpClient,
                reqFactory.createCitableLocatorRequest(collectionForReservingEZID.getId(), ezid).cancelAsHttpPost(),
                403);
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    private String getReservedEZID(String content) {
        String reservedID;
        
        // This parsing assumes that all ezid on a EZID test interface we're hitting will have this prefix. S
        int startingIndex = content.indexOf("https://n2t.net/ezid");
        String[] spaceDelimitedContent = content.substring(startingIndex).split(" ");
        reservedID = spaceDelimitedContent[0];
        return reservedID;
    }
}
