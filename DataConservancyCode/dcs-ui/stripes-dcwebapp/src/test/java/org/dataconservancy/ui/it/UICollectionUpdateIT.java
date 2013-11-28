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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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
import org.dataconservancy.ui.stripes.LoginActionBean;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 7/16/12 Time: 10:03 AM To change this template use File | Settings |
 * File Templates.
 */
public class UICollectionUpdateIT extends BaseIT {
    
    private HttpPost systemAdminLogin;
    private HttpPost registeredUserLogin;
    private HttpGet logout;
    private static boolean areObjectsSeeded = false;
    private HttpClient httpClient = new DefaultHttpClient();
    private static Project project;
    private static Collection collection;
    ContactInfo startingContact1;
    ContactInfo startingContact2;
    PersonName creator1;
    PersonName creator2;
    
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
    
    @Before
    public void setUp() throws IOException, InvalidXmlException {
        // Login to start set up project/colection
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
            collection = new Collection();
            
            // Create the ID
            CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
            collection.setId(collectionIdRequest.execute(httpClient));
            
            collection.setTitle("Summer tales of dogs");
            collection.setSummary("Dogs are people too.");
            collection.setCitableLocator("http://www.imdb.com/title/32156789/");
            collection.setPublicationDate(DateTime.now());
            
            // *****************************************************
            // Set up ALTERNATE IDs for the starting collections
            // *****************************************************
            collection.getAlternateIds().add("id:alfeelikestomatoes");
            collection.getAlternateIds().add("id:willardlikesblueberries");
            // *****************************************************
            
            // **********************************************
            // Set up CREATORS for the starting collections
            // **********************************************
            String[] given1 = { "Joe", "Bob" };
            String[] middle1 = { "Franklin", "Delano" };
            String[] family1 = { "Smith", "Doe" };
            creator1 = new PersonName("Mr.", given1, middle1, family1, "III");
            
            String[] given2 = { "Paul", "Dan" };
            String[] middle2 = { "Ryan", "Smith" };
            String[] family2 = { "Smith", "Doe" };
            creator2 = new PersonName("Mr.", given2, middle2, family2, "III");
            
            collection.addCreator(creator1);
            collection.addCreator(creator2);
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
            
            startingContact2 = new ContactInfo();
            startingContact2.setName("Mr. Willard Can-do Sirk");
            startingContact2.setRole("Face mopper");
            startingContact2.setEmailAddress("Ibelieveincleanliness@hygiene.com");
            
            startingAddress = new Address();
            startingAddress.setStreetAddress("couch street");
            startingAddress.setCity("Smooth face");
            startingAddress.setCountry("What country?");
            startingContact2.setPhysicalAddress(startingAddress);
            
            collection.addContactInfo(startingContact1);
            collection.addContactInfo(startingContact2);
            
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            HttpAssert.assertStatus(httpClient, createCollectionRequest.asHttpPost(), 302);
            assertNotNull(archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId()));
            
            httpClient.execute(logout).getEntity().getContent().close();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests update a collection with permission Expects successful update
     */
    @Test
    public void testUpdateCollectionWithPermission() throws Exception {
        Collection updatedCollection = new Collection(collection);
        updatedCollection.setTitle("testUpdateCollectionWithPermission");
        
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        HttpAssert.assertStatus(httpClient, reqFactory.createUpdateCollectionRequest(updatedCollection).asHttpPost(),
                302);
        httpClient.execute(logout).getEntity().getContent().close();
        
        HttpResponse response = httpClient.execute(reqFactory.collectionSplashPageRequest(collection.getId())
                .asHttpPost());
        String content = IOUtils.toString(response.getEntity().getContent());
        assertTrue("Expected content to contain the updated collection name (" + updatedCollection.getTitle() + ")!",
                content.contains(updatedCollection.getTitle()));
        
    }
    
    /**
     * Tests update a collection without permission Expects error code 403
     */
    @Test
    public void testUpdateCollectionWithoutPermission() throws Exception {
        Collection updatedCollection = new Collection(collection);
        updatedCollection.setTitle("testUpdateCollectionWithoutPermission");
        String expectedText = "This user is not authorized to update to the given collection";
        
        httpClient.execute(registeredUserLogin).getEntity().getContent().close();
        HttpResponse response = httpClient.execute(reqFactory.createUpdateCollectionRequest(updatedCollection)
                .asHttpPost());
        String content = IOUtils.toString(response.getEntity().getContent());
        httpClient.execute(logout).getEntity().getContent().close();
        
        assertEquals(403, response.getStatusLine().getStatusCode());
        assertTrue("Expected content to contain permissions error message: \"" + expectedText + "\"",
                content.contains(expectedText));
        
        response = httpClient.execute(reqFactory.collectionSplashPageRequest(collection.getId()).asHttpPost());
        content = IOUtils.toString(response.getEntity().getContent());
        assertFalse("Expected content not to contain the updated collection name (" + updatedCollection.getTitle()
                + ")!", content.contains(updatedCollection.getTitle()));
        
    }
    
    /**
     * Tests update a collection when not logged in Expects error code 401
     */
    @Test
    public void testUpdateCollectionWhileLoggedOut() throws Exception {
        Collection updatedCollection = new Collection(collection);
        updatedCollection.setTitle("testUpdateCollectionWhileLoggedOut");
        
        HttpResponse response = httpClient.execute(reqFactory.createUpdateCollectionRequest(updatedCollection)
                .asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, response.getStatusLine().getStatusCode());
        assertTrue(
                LoginActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "renderLoginForm")
                        + " not in " + response.getFirstHeader("Location").getValue(),
                response.getFirstHeader("Location")
                        .getValue()
                        .contains(
                                LoginActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "renderLoginForm")));
    }
    
    /**
     * Not sure if this should be an IT or a UT, leaning toward UT
     */
    @Test
    public void testUpdateNonExistingCollection() throws Exception {
        Collection updatedCollection = new Collection(collection);
        updatedCollection.setTitle("testUpdateCollectionWithNoPermission");
        updatedCollection.setId("ThisIsABogusId");
        
        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            properties.load(in);
            in.close();
        }
        String expectedText = String.format((String) properties.get("error.generic-error"), "");
        
        httpClient.execute(systemAdminLogin).getEntity().getContent().close();
        HttpResponse response = httpClient.execute(reqFactory.createUpdateCollectionRequest(updatedCollection)
                .asHttpPost());
        String content = IOUtils.toString(response.getEntity().getContent());
        httpClient.execute(logout).getEntity().getContent().close();
        
        assertEquals(500, response.getStatusLine().getStatusCode());
        assertTrue("Expected content to contain the generic error message: \"" + expectedText + "\"",
                content.contains(expectedText));
        
        response = httpClient.execute(reqFactory.collectionSplashPageRequest(collection.getId()).asHttpPost());
        content = IOUtils.toString(response.getEntity().getContent());
        assertFalse("Expected content not to contain the updated collection name (" + updatedCollection.getTitle()
                + ")!", content.contains(updatedCollection.getTitle()));
    }
    
    /**
     * Test handling 2 update requests on the same collection at the same time Expects: both updates will go through;
     * the later one will overwrite the earlier update
     */
    @Test
    public void testConcurrentUpdateHandling() throws IOException {
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
        
        Collection updatedCollection1 = new Collection(this.collection);
        // edit collection's title
        String newCollectionTitle1 = "Collection update #1";
        updatedCollection1.setTitle(newCollectionTitle1);
        
        Collection updatedCollection2 = new Collection(this.collection);
        // remove one contactInfo from origCollection
        ContactInfo removedContactInfo = updatedCollection2.getContactInfoList().get(0);
        updatedCollection2.getContactInfoList().remove(removedContactInfo);
        
        // add an alternate id
        String additionalId = "id:additionalId";
        updatedCollection2.getAlternateIds().add(additionalId);
        
        // edit collection's title
        String newCollectionTitle = "Collection update #2.";
        updatedCollection2.setTitle(newCollectionTitle);
        
        updateCollectionRequest = reqFactory.createUpdateCollectionRequest(updatedCollection1);
        HttpAssert.assertStatus(httpClient, updateCollectionRequest.asHttpPost(), 302);
        
        UpdateCollectionRequest anotherUpdateCollectionRequest = reqFactory
                .createUpdateCollectionRequest(updatedCollection2);
        HttpAssert.assertStatus(httpClient, anotherUpdateCollectionRequest.asHttpPost(), 302);
        
        viewCollectionDetailsRequest.setCollectionIdToView(collection.getId());
        HttpResponse authorizedResponse = httpClient.execute(viewCollectionDetailsRequest.asHttpPost());
        assertEquals("Depositing dataset failed, expected return 200, but was: "
                + authorizedResponse.getStatusLine().getStatusCode(), 200, authorizedResponse.getStatusLine()
                .getStatusCode());
        
        String content = IOUtils.toString(authorizedResponse.getEntity().getContent());
        // assert that the second update is the last one through and overwrote other updates
        assertTrue(content.contains(updatedCollection2.getTitle()));
        assertFalse(content.contains(collection.getTitle()));
        assertFalse(content.contains(updatedCollection1.getTitle()));
        
        // assert that removed contact info isn't there
        assertFalse(content.contains(removedContactInfo.getName()));
        assertTrue(content.contains(updatedCollection1.getContactInfoList().get(0).getName()));
        // assert that the additional alternate id is there
        assertTrue(content.contains(additionalId));
        assertTrue(content.contains(updatedCollection2.getAlternateIds().get(1)));
        assertTrue(content.contains(updatedCollection2.getAlternateIds().get(0)));
        
        authorizedResponse.getEntity().getContent().close();
    }
    
    private void checkCollectionMembershipAfterUpdate() {
        
    }
    
}
