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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
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

public class AddDepositorIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private AuthorizedDepositorRequest authorizedDepositorRequest;
    
    // Someone to create the project
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    // Someone to create the Collection
    @Autowired
    @Qualifier("defaultUser")
    private Person basicUser;
    
    // Someone to deposit to the collection
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person authorizedUser;
    
    private HttpGet logout;
    
    private HttpPost adminLogin;
    
    private HttpPost basicUserLogin;
    
    private HttpPost authorizedUserLogin;
    
    // Collection to deposit to
    private static Collection collection;
    
    // Sample data file to use in the upload
    private static java.io.File sampleDataFile;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    /**
     * Sets up the httpConnection, project, collection, and dataset
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        logout = reqFactory.createLogoutRequest().asHttpGet();
        adminLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        basicUserLogin = reqFactory.createLoginRequest(basicUser).asHttpPost();
        authorizedUserLogin = reqFactory.createLoginRequest(authorizedUser).asHttpPost();
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login as admin user!");
            
            Project project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            List<String> numbers = new ArrayList<String>();
            numbers.add("1234");
            numbers.add("5678");
            project.setNumbers(numbers);
            project.setFundingEntity("Cash Money");
            project.addPi(basicUser.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
            
            // Login as the basic user, create the collection, and logout.
            HttpAssert.assertStatus(hc, basicUserLogin, 300, 399, "Unable to login as basic user!");
            
            collection = new Collection();
            collection.setId("AddDepositorIT-setUp-collection");
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout baisc user!");
            
            assertNotNull("Expected a Collection to be created in the archive!",
                    archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId()));
            
            sampleDataFile = createSampleDataFile("AddDepositorIT", ".txt");
            sampleDataFile.deleteOnExit();
            areObjectsSeeded = true;
        }
    }
    
    @Test
    public void testAddDepositorAndDeposit() throws IOException, URISyntaxException {
        // Login as the basic user, create the depositor, and logout.
        HttpAssert.assertStatus(hc, basicUserLogin, 300, 399, "Unable to login as an basic user!");
        
        authorizedDepositorRequest.setAuthorizedUserForCollection(authorizedUser.getEmailAddress(), collection.getId());
        // hc.execute(authorizedDepositorRequest.asHttpPost());
        HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                + authorizedUser + " for deposit!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout basic user!");
        
        // Login as the authorized user, deposit file, and logout.
        HttpAssert.assertStatus(hc, authorizedUserLogin, 300, 399, "Unable to login as authorized user!");
        
        // Dataset for deposit
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset1");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId("AddDepositorIT-testAddDepositorAndDeposit-dataSet");
        
        DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem, collection.getId(),
                sampleDataFile);
        
        HttpAssert.assertStatus(hc, depositRequest.asHttpPost(), 300, 399,
                "Unable to deposit with authorized depositor!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout authorized user!");
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("The DeliverableUnit representing the deposited DataItem (" + dataItem.getId() + ") was not "
                + "found.", archiveSupport.pollAndQueryArchiveForDataItemDu(dataItem.getId()));
    }
    
    @Test
    public void testDepositWithoutDepositAuthorization() throws IOException, URISyntaxException {
        // Login as the basic user, remove the depositor (if it exists), and logout.
        HttpAssert.assertStatus(hc, basicUserLogin, 300, 399, "Unable to login as an basic user!");
        
        authorizedDepositorRequest.removeAuthorizedUserFromCollection(authorizedUser.getEmailAddress(),
                collection.getId());
        HttpAssert.assertStatus(hc, authorizedDepositorRequest.asHttpPost(), 300, 399, "Unable to authorize user "
                + authorizedUser + " for deposit!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout basic user!");
        
        // Login as the authorized user, deposit file, and logout.
        HttpAssert.assertStatus(hc, authorizedUserLogin, 300, 399, "Unable to login as authorized user!");
        
        // Dataset for deposit
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset2");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId("AddDepositorIT-testDepositWithoutDepositAuthorization-dataSet");
        
        DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(dataItem, collection.getId(),
                sampleDataFile);
        
        HttpAssert.assertStatus(hc, depositRequest.asHttpPost(), 400, 499,
                "Able to deposit without authorized deposit!");
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout authorized user!");
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        // Since the return value from the deposit request was 400, this test is commented out to speed up ITs.
        // assertNull("The DeliverableUnit representing the deposited DataItem (" + dataSet.getId() + ") was " +
        // "found and should not be.", archiveSupport.pollAndQueryArchiveForDatasetDu(dataSet.getId()));
    }
}
