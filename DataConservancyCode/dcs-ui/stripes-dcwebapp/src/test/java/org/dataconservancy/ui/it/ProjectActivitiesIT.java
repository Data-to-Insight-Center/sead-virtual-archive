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

import static junit.framework.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.DepositRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.ListProjectActivityRequest;
import org.dataconservancy.ui.it.support.ProjectAdminRequest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests related to verifying the behavior of card <a
 * href="https://scm.dataconservancy.org/issues/browse/DC-724">DC-724</a>
 */

public class ProjectActivitiesIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person userRole;
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person defaultAdmin;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person defaultUser;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private ProjectAdminRequest projectAdminRequest;
    
    @Autowired
    private ListProjectActivityRequest listProjectActivityRequest;
    
    @Autowired
    private DepositRequest depositRequest;
    
    // make these static so they persist between test methods
    private static Project project;
    private static boolean areObjectsSeeded = false;
    
    /**
     * Configures the HttpClient. Logs into the UI as an admin and:
     * <ul>
     * <li>Creates a Project in the UI with userRole as a project admin.</li>
     * <li>Creates a Collection in the Archive.</li>
     * <li>Logs out.</li>
     * </ul>
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        if (!areObjectsSeeded) {
            log.debug("Seeding collaborating test objects.");
            
            HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(defaultAdmin).asHttpPost(), 300, 399,
                    "Unable to login as an admin user!");
            log.debug("Logging in as an admin: " + defaultAdmin.getEmailAddress());
            
            project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            HttpAssert.assertStatus(hc,
                    reqFactory.createSetNewAdminForProjectRequest(userRole.getEmailAddress(), project).asHttpPost(),
                    302);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project.getId()).toURI()), 200,
                    "Unable to create project " + project);
            log.debug("Created project {}, {}", project.getId(), project);
            
            Collection collection = new Collection();
            collection.setId("ProjectActivitiesIT-SetupCollection");
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            createCollection(project.getId(), collection);
            
            assertNotNull("Expected a Collection to be created in the archive!",
                    archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId()));
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            log.debug("Created collection {}", collection.getId());
            
            HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399,
                    "Unable to logout admin user!");
            log.debug("Logging out admin {}", defaultAdmin.getEmailAddress());
            log.debug("Successfully seeded collaborating test objects for ProjectActivitiesIT.");
            
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests that a user - authorized to view the activity list (because that user is an instance Admin) - is in fact
     * able to view the list.
     * 
     * @throws Exception
     */
    
    @Test
    public void testInstanceAdminCanViewActivityList() throws Exception {
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(defaultAdmin).asHttpPost(), 300, 399,
                "Unable to login as " + userRole);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        // Attempt to view the activity page, we expect success
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getListProjectActivityUrl(project.getId()).toURI()), 200,
                "Unable to view activities for " + project.getId() + ". Possibly a permissions problem.");
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399, "Unable to logout user!");
    }
    
    /**
     * Tests that a user - authorized to view the activity list (because that user is an Admin for the Project) - is in
     * fact able to view the list.
     * 
     * @throws Exception
     */
    
    @Test
    public void testProjectAdminCanViewActivityList() throws Exception {
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(userRole).asHttpPost(), 300, 399,
                "Unable to login as " + userRole);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        // Attempt to view the activity page, we expect success
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getListProjectActivityUrl(project.getId()).toURI()), 200,
                "Unable to view activities for " + project.getId() + ". Possibly a permissions problem.");
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399, "Unable to logout user!");
    }
    
    /**
     * Tests that a registered user - not authorized to view the activity list (because that user is not an Admin for
     * the Project or an instance admin) - is in fact not able to view the list.
     * 
     * @throws Exception
     */
    
    @Test
    public void testRegisteredUserCannotViewActivityList() throws Exception {
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 302);
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(defaultUser).asHttpPost(), 300, 399,
                "Unable to login as " + userRole);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        // Attempt to view the activity page, we expect failure
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getListProjectActivityUrl(project.getId()).toURI()), 401,
                "Unexpectedly able to view activities for " + project.getId() + ". Possibly a permissions problem.");
        
        // Assert we are logged out
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399, "Unable to logout user!");
    }
    
    /**
     * Creates a collection in the archive by invoking the add collection endpoint in the UI. No verification is done
     * that the collection is actually created. The caller will need to verify.
     * 
     * @param projectId
     *            the project to create the collection in
     * @param toCreate
     *            the collection to create
     * @throws IOException
     */
    private void createCollection(String projectId, Collection toCreate) throws IOException {
        createCollectionRequest.setCollection(toCreate);
        createCollectionRequest.setProjectId(projectId);
        hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
    }
}
