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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.DepositRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.ViewCollectionDetailsRequest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.util.ZipPackageExtractor;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 1/4/12 Time: 2:09 PM To change this template use File | Settings | File
 * Templates.
 */
public class ViewCollectionDetailsIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person defaultAdmin;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private ViewCollectionDetailsRequest viewCollectionDetailsRequest;
    
    private HttpClient hc = new DefaultHttpClient();
    
    private static File windowsFileOnlyZip;
    private final static String WINDOWS_FILE_ZIP_PATH = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    private final static String ZIP_DIRECTORY_PATH = "./test/";
    
    private static String project1Id;
    private static Collection collection1;
    
    private ZipPackageExtractor zipExtractor;
    private int zipFileSize;
    
    private static boolean areObjectsSeeded;
    
    static private HttpPost defaultAdminLogin;
    static private HttpGet logout;
    
    private MetadataFile metadataFile = new MetadataFile();
    
    /**
     * Set up: In the role of adminUser: create project, 3 collections 1,2,3. Add approved registered user as depositor
     * for collection 1&2. Logout
     * 
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    @Before
    public void setUp() throws IOException, URISyntaxException, Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        // log in as admin
        defaultAdminLogin = reqFactory.createLoginRequest(defaultAdmin).asHttpPost();
        if (!areObjectsSeeded) {
            HttpAssert.assertStatus(hc, defaultAdminLogin, 300, 399, "Unable to login as " + defaultAdmin);
            // create a project
            project1Id = createProject();
            // test that the project has been created: can be displayed on screen
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project1Id).toURI()), 200,
                    "Unable to view project if ID " + project1Id + ". It may not have been successfully created.");
            
            // insert collection into project
            collection1 = new Collection();
            // deposit collection1 into project 1
            depositCollectionIntoProject(project1Id, collection1);
            // HttpAssert.setLogging(true);
            // test that collection was deposited: can be loaded on screen
            HttpAssert
                    .assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection1.getId()).toURI()), 200,
                            "Unable to view collection " + collection1.getId()
                                    + ". It may not have been successfully created.");
            
            DcsDeliverableUnit collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(collection1.getId());
            assertNotNull("Collection was not created in the archive.", collectionDu);
            String collectionArchiveID = collectionDu.getId();
            
            // load zip file for an abbreviated deposit of multiple test files.
            loadZipFile();
            // set up deposit request to deposit into collection 1
            final DepositRequest windowsFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                    new DataItem("name", "description", "id", defaultAdmin.getId(), new DateTime(), null,
                            new ArrayList<String>(), collection1.getId()), collection1.getId(), windowsFileOnlyZip);
            
            windowsFileDepositRequest.setContainer(true);
            
            // Perform the deposit into Collection 1;
            depositZipFile(windowsFileDepositRequest);
            
            // We expect the archive collection to grow by the number of items in the zip file
            zipExtractor = new ZipPackageExtractor();
            zipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
            List<File> zippedFiles = zipExtractor.getFilesFromPackageFile(windowsFileOnlyZip.getName(), windowsFileOnlyZip);
            zipFileSize = zippedFiles.size();
            // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
            Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                    zipFileSize);
            
            // Query the archive for the number of items in the collection
            int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
            
            checkZipFileDeposit(fileDus, zippedFiles, zipFileSize, actualArchiveCollectionSize);
            
            // Create logout request
            logout = reqFactory.createLogoutRequest().asHttpGet();
            
            // Clean up
            zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
            fileDus.clear();
            zippedFiles.clear();
            areObjectsSeeded = true;
        }
    }
    
    @Test
    public void testViewCollectionDetails() throws Exception {
        viewCollectionDetailsRequest.setCollectionIdToView(collection1.getId());
        HttpResponse authorizedResponse = hc.execute(viewCollectionDetailsRequest.asHttpPost());
        assertEquals("Depositing dataset failed, expected return 200, but was: "
                + authorizedResponse.getStatusLine().getStatusCode(), 200, authorizedResponse.getStatusLine()
                .getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
    }
    
    @Test
    public void testAnonymousViewCollection() throws IOException {
        ViewCollectionDetailsRequest vcdr = new ViewCollectionDetailsRequest(urlConfig);
        vcdr.setCollectionIdToView(collection1.getId());
        HttpResponse response = hc.execute(vcdr.asHttpPost());
        String content = IOUtils.toString(response.getEntity().getContent());
        assertEquals("Expected 200 from anonymous view of collection.", 200, response.getStatusLine().getStatusCode());
        assertFalse("Expected text to not contain a link to deposit data.", content.contains("Deposit data"));
    }
    
    @Test
    public void testAdminViewCollection() throws IOException {
        ViewCollectionDetailsRequest vcdr = new ViewCollectionDetailsRequest(urlConfig);
        vcdr.setCollectionIdToView(collection1.getId());
        
        HttpAssert.assertStatus(hc, defaultAdminLogin, 300, 399, "Unable to login!");
        HttpResponse response = hc.execute(vcdr.asHttpPost());
        
        String content = IOUtils.toString(response.getEntity().getContent());
        assertEquals("Expected 200 from anonymous view of collection.", 200, response.getStatusLine().getStatusCode());
        assertTrue("Expected text to contain a link to deposit data.", content.contains("Deposit data"));
    }
    
    private int queryNumberOfItemsInCollection(String collectionId) {
        return archiveSupport.queryArchiveForNumberOfItemsInCollection(collectionId);
    }
    
    private void checkZipFileDeposit(Set<DcsDeliverableUnit> returnedDus, List<File> zippedFiles,
            int expectedArchiveCount, int actualArchiveCount) {
        // Assert the proper number of DUs from the zip file were found
        assertNotNull("The list of file deliverable units was null.", returnedDus);
        assertEquals("Not all of the deliverable units from the zip file were found.", expectedArchiveCount,
                actualArchiveCount);
        
        // Assert that the file names of files in the zip file match the title in the DU (is this really a profile
        // issue?)
        int matchedFiles = 0;
        for (DcsDeliverableUnit du : returnedDus) {
            for (File file : zippedFiles) {
                if (du.getTitle().equalsIgnoreCase(file.getName())) {
                    matchedFiles++;
                }
            }
        }
        
        assertEquals("Could not find a deliverable unit for all files", zippedFiles.size(), matchedFiles);
    }
    
    private void depositZipFile(DepositRequest depositRequest) throws Exception {
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals("Depositing dataset failed, expected return 302, but was: "
                + authorizedResponse.getStatusLine().getStatusCode(), 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
    }
    
    private void loadZipFile() {
        URL windowsFileOnlyURL = DepositZipFileIT.class.getResource(WINDOWS_FILE_ZIP_PATH);
        try {
            windowsFileOnlyZip = new File(windowsFileOnlyURL.toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Error creating zip file: " + e.getMessage(), e);
        }
    }
    
    private void depositCollectionIntoProject(String projectId, Collection collection) throws IOException {
        String collectionNumber = String.valueOf(new Random().nextInt());
        collection.setId("Card863Collection" + collectionNumber);
        collection.setTitle("Seeded Collection " + collectionNumber);
        collection.setSummary("A seeded collection for use with ITs");
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(new PersonName("Mr.", "Jack", "John", "Doe", "II"));
        collection.setCreators(creators);
        createCollectionRequest.setCollection(collection);
        createCollectionRequest.setProjectId(projectId);
        hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
        
    }

    private void addMetadateFileToCollection(Collection collection) {
        try {
            File tmp = createSampleDataFile("ProjectCollectionsListIT-", ".txt");
            tmp.deleteOnExit();
            metadataFile.setSource(tmp.toURI().toURL().toExternalForm());
        }
        catch (IOException e) {
            log.error("Failed during file upload", e);
        }
    }
    
    private String createProject() throws Exception {
        final Project project = new Project();
        String projectNumber = String.valueOf(new Random().nextInt());
        
        project.setDescription("A seeded project for use with ITs " + projectNumber);
        project.setEndDate(DateTime.now());
        project.setFundingEntity("NSF");
        project.setStartDate(DateTime.now());
        project.setName("Seeded Project " + projectNumber);
        project.addNumber("1234");
        project.setFundingEntity("Cash Money");
        
        return reqFactory.createProjectApiAddRequest(project).execute(hc).getId();
    }
    
    @After
    public void cleanUp() {
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
}
