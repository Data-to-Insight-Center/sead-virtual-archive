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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.CitationService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Test the UI for Collection Splash Page
 */
public class CollectionSplashPageIT extends BaseIT {
    
    private HttpClient httpClient = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private CitationService citationService;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person authenticatedUser;
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person systemAdmin;
    
    private HttpPost systemAdminLogin;
    private HttpGet logout;
    
    private static boolean areObjectsSeeded = false;
    
    private static Collection collectionOne;
    private static Collection collectionTwo;
    private static Project project;
    
    @Before
    public void setUp() throws IOException, InvalidXmlException {
        // Login to start set up project/colection
        systemAdminLogin = reqFactory.createLoginRequest(systemAdmin).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            HttpAssert.assertStatus(httpClient, systemAdminLogin, 302);
            // Set up project for the collection:
            project = new Project();
            
            project.setDescription("A description");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("A funder");
            project.setStartDate(DateTime.now());
            project.setName("Project");
            project.addNumber("1234");
            project.setFundingEntity("A funder");
            project.addPi(authenticatedUser.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(httpClient);
            // Make sure the project was create - loadable
            HttpAssert.assertStatus(httpClient, urlConfig.getProjectUrl(project.getId()).toString(), 200);
            
            // Set up a collection.
            collectionOne = new Collection();
            collectionOne.setId(idService.create(Types.COLLECTION.name()).getUrl().toString());
            collectionOne.setTitle("My Collection ONE Title");
            collectionOne.setSummary("My Collection ONE Summary");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collectionOne.setCreators(creators);
            
            createCollectionRequest.setCollection(collectionOne);
            createCollectionRequest.setProjectId(project.getId());
            HttpAssert.assertStatus(httpClient, createCollectionRequest.asHttpPost(), 302);
            assertNotNull(archiveSupport.pollAndQueryArchiveForCollectionDu(collectionOne.getId()));
            
            httpClient.execute(logout).getEntity().getContent().close();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Test loading the splash page as a logged-in user. Expects page load successfully with return code of 200
     */
    @Test
    public void testAccessPageAsLoggedInUser() throws Exception {
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 302);
        HttpAssert.assertStatus(httpClient, reqFactory.collectionSplashPageRequest(collectionOne.getId()).asHttpPost(),
                200);
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Test loading the splash page as a non-logged in-user. Expects page load successfully with return code of 200
     */
    @Test
    public void testAccessPageAsNotLoggedInUser() throws Exception {
        HttpAssert.assertStatus(httpClient, reqFactory.collectionSplashPageRequest(collectionOne.getId()).asHttpPost(),
                200);
    }
    
    /**
     * Test loading the splash page for a non-existing collection (bad collection id) Expects page load to fail with
     * return code of 404
     */
    @Test
    public void testAccessNonExistingCollection() throws Exception {
        HttpAssert.assertStatus(httpClient, reqFactory.collectionSplashPageRequest("Bogus!").asHttpPost(), 404);
    }
    
    /**
     * Test loading the splash page for a request without a collection id Expects page load to fail with return code of
     * 404
     */
    @Test
    public void testAccessWithoutCollection() throws Exception {
        HttpAssert.assertStatus(httpClient, reqFactory.collectionSplashPageRequest(null).asHttpPost(), 404);
    }
}
