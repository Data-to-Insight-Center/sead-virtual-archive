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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.exceptions.UnpackException;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.DepositRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.util.ZipPackageExtractor;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DepositStatusIT extends BaseIT {
    private final static String WINDOWS_FILE_ZIP_PATH = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient hc = new DefaultHttpClient();
    
    // Someone to create the project
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    // Someone to create the Collection
    @Autowired
    @Qualifier("defaultUser")
    private Person basicUser;
    
    // Someone to be depositor on the collection
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person authorizedUser;
    
    private HttpGet logout;
    private HttpPost adminLogin;
    private HttpPost basicUserLogin;
    private HttpPost authorizedUserLogin;
    
    // Project to deposit collection to
    private static Project project;
    
    // Collection to deposit to
    private static Collection collection;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    private static File sampleDataFile = null;
    
    /**
     * Sets up the httpConnection, project, and collection, as well as getting the users all the right permissions.
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
            project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            project.addPi(basicUser.getId());
            
            HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login admin user!");
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project.getId()).toURI()), 200,
                    "Unable to retrieve project " + project);
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
            
            // Login as the basic user, create the collection, and logout.
            collection = new Collection();
            collection.setId(reqFactory.createIdApiRequest(Types.COLLECTION).execute(hc));
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            HttpAssert.assertStatus(hc, basicUserLogin, 300, 399, "Unable to login as basic user!");
            HttpAssert.assertStatus(hc, reqFactory.createCollectionRequest(collection, project).asHttpPost(), 300, 399,
                    "Unable to create collection " + collection);
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to view collection " + collection);
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout basic user!");
            
            assertNotNull("Expected a Collection to be created in the archive!",
                    archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId()));
            
            // Load sample datafile
            sampleDataFile = createSampleDataFile("DepositStatusIT", ".txt");
            sampleDataFile.deleteOnExit();
            areObjectsSeeded = true;
        }
    }
    
    private void testCanView(HttpPost login, String objectId) throws URISyntaxException, IOException {
        testView(login, objectId, 200, null);
    }
    
    private void testCanView(HttpPost login, String objectId, String state) throws URISyntaxException, IOException {
        testView(login, objectId, 200, state);
    }
    
    private void testCantView(HttpPost login, String objectId) throws URISyntaxException, IOException {
        testView(login, objectId, 401, null);
    }
    
    private void testCantView(HttpPost login, String objectId, int status) throws URISyntaxException, IOException {
        testView(login, objectId, status, null);
    }
    
    private void testView(HttpPost login, String objectId, int status, String stateString) throws URISyntaxException,
            IOException {
        HttpAssert.assertStatus(hc, login, 300, 399, "Unable to login!");
        HttpResponse response = hc.execute(new HttpGet(urlConfig.getDepositStatusUrl(objectId).toURI()));
        final String content = IOUtils.toString(response.getEntity().getContent());
        assertEquals("Expected " + status + " status code from deposit status URL!" + content, status, response
                .getStatusLine().getStatusCode());
        if (null != stateString)
            assertTrue("Expected content to contain '" + stateString + "' (full content string was: \n[" + content
                    + "])", content.contains(stateString));
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
    @Test
    public void testFailureWithNonexistantObjectId() throws URISyntaxException, IOException {
        // Login as the instance administrator and query an object that doesn't exist.
        testCantView(adminLogin, reqFactory.createIdApiRequest(Types.PACKAGE).execute(hc), 404);
    }
    
    @Test
    public void testFailureWithoutObjectId() throws URISyntaxException, IOException {
        // Login as the instance administrator and query an object that doesn't exist.
        testCantView(adminLogin, "", 400);
    }
    
    @Test
    public void testCanViewAdminBeforeAndAfterDeposit() throws IOException, URISyntaxException, InterruptedException {
        // Deposit a dataset
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId(reqFactory.createIdApiRequest(Types.DATA_SET).execute(hc));
        final Package thePackage = new Package();
        thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(hc));
        
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(
                hc,
                reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem, collection.getId(),
                        sampleDataFile).asHttpPost(), 300, 399, "Unable to deposit data as admin!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Check for view
        // Because the deposit for a DataItem blocks until deposited, this will never say pending.
        testCanView(adminLogin, thePackage.getId(), "DEPOSITED");
        
        // Poll deposit
        final DataItem actualDataSet = archiveSupport.pollAndQueryArchiveForDataItem(dataItem.getId());
        assertNotNull("The deposited DataItem (" + dataItem.getId() + ") was not found.", actualDataSet);
        
        Thread.sleep(15000);
        
        // Check for view
        testCanView(adminLogin, thePackage.getId(), "DEPOSITED");
    }
    
    @Test
    public void testCanViewBasicUserBeforeAndAfterDeposit() throws IOException, URISyntaxException,
            InterruptedException {
        // Deposit a dataset
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId(reqFactory.createIdApiRequest(Types.DATA_SET).execute(hc));
        final Package thePackage = new Package();
        thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(hc));
        
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(
                hc,
                reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem, collection.getId(),
                        sampleDataFile).asHttpPost(), 300, 399, "Unable to deposit data as admin!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Check for view
        // Because the deposit for a DataItem blocks until deposited, this will never say pending.
        testCanView(basicUserLogin, thePackage.getId(), "DEPOSITED");
        
        // Poll deposit
        final DataItem actualDataSet = archiveSupport.pollAndQueryArchiveForDataItem(dataItem.getId());
        assertNotNull("The deposited DataItem (" + dataItem.getId() + ") was not found.", actualDataSet);
        
        Thread.sleep(15000);
        
        // Check for view
        testCanView(basicUserLogin, thePackage.getId(), "DEPOSITED");
    }
    
    @Test
    public void testCantViewAuthorizedUserUserBeforeAndAfterDeposit() throws IOException, URISyntaxException,
            InterruptedException {
        // Deposit a dataset
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId(reqFactory.createIdApiRequest(Types.DATA_SET).execute(hc));
        final Package thePackage = new Package();
        thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(hc));
        
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(
                hc,
                reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem, collection.getId(),
                        sampleDataFile).asHttpPost(), 300, 399, "Unable to deposit data as admin!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Check for view
        testCantView(authorizedUserLogin, thePackage.getId());
        
        // Poll deposit
        assertNotNull("The deposited DataItem (" + dataItem.getId() + ") was not found.",
                archiveSupport.pollAndQueryArchiveForDataItem(dataItem.getId()));
        
        Thread.sleep(15000);
        
        // Check for view
        testCantView(authorizedUserLogin, thePackage.getId());
    }
    
    @Test
    public void testZipFileStatus() throws IOException, URISyntaxException, InterruptedException, UnpackException {
        // Deposit a dataset
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId(reqFactory.createIdApiRequest(Types.DATA_SET).execute(hc));
        final Package thePackage = new Package();
        thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(hc));
        
        final File zipFile = new File(DepositStatusIT.class.getResource(WINDOWS_FILE_ZIP_PATH).toURI());
        
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        final DepositRequest zipFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(thePackage,
                dataItem, collection.getId(), zipFile);
        zipFileDepositRequest.setContainer(true);
        HttpAssert.assertStatus(hc, zipFileDepositRequest.asHttpPost(), 300, 399, "Unable to deposit data as admin!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // We expect the archive collection to grow by the number of items in the zip file
        final ZipPackageExtractor zipExtractor = new ZipPackageExtractor();
        zipExtractor.setExtractDirectory("./SampleFilePackages/Windows/");
        List<File> zippedFiles = zipExtractor.getFilesFromPackageFile(zipFile.getName(), zipFile);
        
        // Poll the archive until all of the DeliverableUnits in the collection
        // are found, or polling times out. I'm Doing this as it seems to
        // alleviate the pollArchive problem.
        archiveSupport.pollAndQueryArchiveForDUsInCollection(collection.getId(), zippedFiles.size());
        
        // Check for views
        for (File fileInZip : zippedFiles) {
            testCanView(adminLogin, thePackage.getId(), fileInZip.getName() + " is DEPOSITED");
        }
        
        zipExtractor.cleanUpExtractedPackage(new File("/.SampleFilePackages/Windows/" + zipFile.getName() + "/"));
    }
    
    @Test
    public void testChangedPiList() throws IOException, URISyntaxException {
        // Deposit a dataset
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset1");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId(reqFactory.createIdApiRequest(Types.DATA_SET).execute(hc));
        final Package thePackage = new Package();
        thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(hc));
        
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(
                hc,
                reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem, collection.getId(),
                        sampleDataFile).asHttpPost(), 300, 399, "Unable to deposit data as admin!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Check for views
        testCanView(adminLogin, thePackage.getId());
        testCanView(basicUserLogin, thePackage.getId());
        testCantView(authorizedUserLogin, thePackage.getId());
        
        // Change PIs - remove basicUser and add authorizedUser
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(hc, reqFactory.createSetNewAdminForProjectRequest(authorizedUser.getId(), project)
                .asHttpPost(), 300, 399, "Unable to add a new admin to project!");
        project.addPi(authorizedUser.getId());
        HttpAssert.assertStatus(hc, reqFactory.createRemoveAdminFromProjectRequest(basicUser.getId(), project)
                .asHttpPost(), 300, 399, "Unable to remove an admin to project!");
        project.removePi(basicUser.getId());
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Check for views
        testCanView(adminLogin, thePackage.getId());
        testCantView(basicUserLogin, thePackage.getId());
        testCanView(authorizedUserLogin, thePackage.getId());
        
        // Change PIs back - remove authorizedUser and add basicUser
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(hc, reqFactory.createSetNewAdminForProjectRequest(basicUser.getId(), project)
                .asHttpPost(), 300, 399, "Unable to add a new admin to project!");
        project.addPi(basicUser.getId());
        HttpAssert.assertStatus(hc, reqFactory.createRemoveAdminFromProjectRequest(authorizedUser.getId(), project)
                .asHttpPost(), 300, 399, "Unable to remove an admin to project!");
        project.removePi(authorizedUser.getId());
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
    @Test
    public void testChangedDepositorList() throws IOException, URISyntaxException {
        // Deposit a dataset
        final DataItem dataItem = new DataItem();
        dataItem.setName("Sample Dataset2");
        dataItem.setDescription("Sample Dataset Descrption");
        dataItem.setId(reqFactory.createIdApiRequest(Types.DATA_SET).execute(hc));
        final Package thePackage = new Package();
        thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(hc));
        
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(
                hc,
                reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem, collection.getId(),
                        sampleDataFile).asHttpPost(), 300, 399, "Unable to deposit data as admin!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Check for views
        testCanView(adminLogin, thePackage.getId());
        testCanView(basicUserLogin, thePackage.getId());
        testCantView(authorizedUserLogin, thePackage.getId());
        
        // Change Depositor - add authorizedUser
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(hc, reqFactory.createSetNewDepositorRequest(authorizedUser.getId(), collection.getId())
                .asHttpPost(), 300, 399, "Unable to add new depositor to collection!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        
        // Check for views
        testCanView(adminLogin, thePackage.getId());
        testCanView(basicUserLogin, thePackage.getId());
        testCanView(authorizedUserLogin, thePackage.getId());
        
        // Change Depositor back - remove authorizedUser
        HttpAssert.assertStatus(hc, adminLogin, 300, 399, "Unable to login!");
        HttpAssert.assertStatus(hc, reqFactory.createRemoveDepositorRequest(authorizedUser.getId(), collection.getId())
                .asHttpPost(), 300, 399, "Unable to remove depositor to collection!");
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
}
