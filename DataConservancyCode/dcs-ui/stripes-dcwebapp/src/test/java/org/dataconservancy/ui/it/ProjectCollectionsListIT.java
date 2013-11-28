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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.sourceforge.stripes.action.UrlBinding;

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
import org.dataconservancy.ui.it.support.ListProjectCollectionsRequest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.stripes.UserCollectionsActionBean;
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
public class ProjectCollectionsListIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person defaultAdmin;
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    private ListProjectCollectionsRequest listProjectCollectionsRequest;
    
    private HttpClient hc = new DefaultHttpClient();
    
    private static File windowsFileOnlyZip;
    
    private final static String WINDOWS_FILE_ZIP_PATH = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    
    private final static String ZIP_DIRECTORY_PATH = "./test/";
    
    private static String project1Id;
    
    private static String project2Id;
    
    private static Collection collection1;
    
    private static Collection collection2;
    
    private static Collection collection3;
    
    private String collection1ArchiveID;
    
    private String collection2ArchiveID;
    
    private String collection3ArchiveID;
    
    private ZipPackageExtractor zipExtractor;
    
    private int zipFileSize;
    
    private static boolean areObjectsSeeded;
    
    private HttpPost defaultAdminLogin;
    
    private HttpGet logout;
    
    private DateTime depositDate = DateTime.now();
    
    private MetadataFile metadataFile = new MetadataFile();
    
    /**
     * Set up: In the role of defaultAdmin: create project 1&2, add collection 3 to project 2, add collection 1&2 to
     * project 1. deposit a zip file into collection 1.
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
        HttpAssert.assertStatus(hc, defaultAdminLogin, 300, 399, "Unable to login as " + defaultAdmin);
        if (!areObjectsSeeded) {
            // create a project
            project1Id = createProject();
            project2Id = createProject();
            // test that the project has been created: can be displayed on screen
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project1Id).toURI()), 200,
                    "Unable to view project if ID " + project1Id + ". It may not have been successfully created.");
            
            // test that the project has been created: can be displayed on screen
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project2Id).toURI()), 200,
                    "Unable to view project if ID " + project2Id + ". It may not have been successfully created.");
            
            collection3 = new Collection();
            // deposit anotherCollection into Project 2
            depositCollectionIntoProject(project2Id, collection3);
            // test that collection was deposited: can be loaded on screen
            HttpAssert
                    .assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection3.getId()).toURI()), 200,
                            "Unable to view collection " + collection3.getId()
                                    + ". It may not have been successfully created.");
            DcsDeliverableUnit collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(collection3.getId());
            assertNotNull("Collection was not created in the archive.", collectionDu);
            collection3ArchiveID = collectionDu.getId();
            
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
            
            collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(collection1.getId());
            assertNotNull("Collection was not created in the archive.", collectionDu);
            collection1ArchiveID = collectionDu.getId();
            
            // insert collection into project
            collection2 = new Collection();
            // deposit collection2 into project2
            depositCollectionIntoProject(project1Id, collection2);
            // test that collection was deposited: can be loaded on screen
            HttpAssert
                    .assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection2.getId()).toURI()), 200,
                            "Unable to view collection " + collection2.getId()
                                    + ". It may not have been successfully created.");
            
            collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(collection2.getId());
            assertNotNull("Collection was not created in the archive.", collectionDu);
            collection2ArchiveID = collectionDu.getId();
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
            Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(
                    collection1ArchiveID, zipFileSize);
            
            // Query the archive for the number of items in the collection
            int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collection1ArchiveID);
            checkZipFileDeposit(fileDus, zippedFiles, zipFileSize, actualArchiveCollectionSize);
            
            // Clean up
            zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
            fileDus.clear();
            zippedFiles.clear();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Test that collections listed in collection list page belongs to the appropriate project Scan the http response
     * for expected collection values.
     * 
     * @throws java.io.IOException
     */
    @Test
    public void testCorrectCollectionsListed() throws IOException, InterruptedException {
        Thread.sleep(10000);
        
        // HttpAssert.assertStatus(hc, urlConfig.getListProjectCollectionsUrl().toString(), 200);
        HttpResponse response = hc.execute(listProjectCollectionsRequest.asHttpPost(project1Id));
        final InputStream content = response.getEntity().getContent();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(content));
        String strLine;
        
        String[] expectedElements1 = {
                collection1.getTitle(),
                "<td>" + depositDate.getYear() + "-" + depositDate.getMonthOfYear() + "-" + depositDate.getDayOfMonth()
                        + "</td>",
                "<td>" + defaultAdmin.getFirstNames() + " " + defaultAdmin.getLastNames() + "</td>",
                "<td>" + collection1.getSummary() + "</td>",
                "<td>"
                        + Integer.toString(archiveSupport
                                .queryArchiveForNumberOfItemsInCollection(collection1ArchiveID)) + "</td>" };
        // "<td>" + zipFileSize + "</td>"};
        
        String[] expectedElements2 = {
                collection2.getTitle(),
                "<td>" + depositDate.getYear() + "-" + depositDate.getMonthOfYear() + "-" + depositDate.getDayOfMonth()
                        + "</td>",
                "<td>" + defaultAdmin.getFirstNames() + " " + defaultAdmin.getLastNames() + "</td>",
                "<td>" + collection2.getSummary() + "</td>",
                "<td>"
                        + Integer.toString(archiveSupport
                                .queryArchiveForNumberOfItemsInCollection(collection2ArchiveID)) + "</td>" };
        
        String[] notExpectedElementsOfCollection3 = {
                collection3.getTitle(),
                "<td>" + depositDate.getYear() + "-" + depositDate.getMonthOfYear() + "-" + depositDate.getDayOfMonth()
                        + "</td>",
                "<td>" + defaultAdmin.getFirstNames() + " " + defaultAdmin.getLastNames() + "</td>",
                "<td>" + collection3.getSummary() + "</td>",
                "<td>"
                        + Integer.toString(archiveSupport
                                .queryArchiveForNumberOfItemsInCollection(collection3ArchiveID)) + "</td>" };
        
        int matchCount1 = 0; // number of table data elements we have found for collection1
        int matchCount2 = 0; // number of table data elements we have found for collection2
        int matchCount3 = 0; // number of table data elements we have found for anotherCollection
        
        // look for each array of elements, elements for each collection have to be in order
        // collections themselves may be in any order
        while ((strLine = br.readLine()) != null) {
            for (; matchCount1 < expectedElements1.length && strLine.contains(expectedElements1[matchCount1]); matchCount1++)
                ;
            for (; matchCount2 < expectedElements2.length && strLine.contains(expectedElements2[matchCount2]); matchCount2++)
                ;
            for (; matchCount3 < notExpectedElementsOfCollection3.length
                    && strLine.contains(notExpectedElementsOfCollection3[matchCount3]); matchCount3++)
                ;
        }
        
        assertEquals(5, matchCount1);
        assertEquals(5, matchCount2);
        assertEquals(0, matchCount3);
        content.close();
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
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "render")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "render")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("render"));
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
        logout = reqFactory.createLogoutRequest().asHttpGet();
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
}
