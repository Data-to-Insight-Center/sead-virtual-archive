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

import static org.dataconservancy.dcs.id.api.Types.COLLECTION;
import static org.dataconservancy.dcs.id.api.Types.DATA_FILE;
import static org.dataconservancy.dcs.id.api.Types.DATA_SET;
import static org.dataconservancy.dcs.id.api.Types.PACKAGE;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_DISPOSITION;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_LENGTH;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_TYPE;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.DepositRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.RequestFactory;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Integration tests for <a href="https://scm.dataconservancy.org/confluence/x/FYCZ">File API</a>
 * 
 */
public class FileHttpAPIIT extends BaseIT {
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person defaultUser;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person approvedUser;
    
    @Autowired
    protected UiUrlConfig urlConfig;
    
    @Autowired
    protected RequestFactory reqFactory;
    
    // A zip file to use to test binary retrieval is actually working.
    private final static String LINUX_FILE_ZIP_PATH = "/SampleFilePackages/Linux/SeveralFiles.zip";
    
    // HttpClient to use for testing url calls
    private HttpClient httpClient = new DefaultHttpClient();
    
    private HttpGet logout;
    private HttpPost adminUserLogin;
    private HttpPost defaultUserLogin;
    private HttpPost approvedUserLogin;
    
    private static Project project;
    private static Collection collection;
    private static DataItem dataItem;
    private static DataFile dataFile;
    
    static private byte dataFileContents[];
    static private DateTime dataSetDateTime;
    static private long dataFileLengh;
    private static DataItem dataSetToUpdate;
    private static DataFile dataFileToUpdate;
    private static DataItem updatedDataSet;
    private static DataFile updatedDataFile;
    private static String dataFileToUpdateContents;
    private static String updatedDataFileContents;
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    // counter for how many times we try
    private int tryCount;
    // maximum number of times to try
    private final int maxTries = 60;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    private String updatePackageId;
    
    private String testMethodName;
    
    @Before
    public void setUp() throws Exception {
        adminUserLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        defaultUserLogin = reqFactory.createLoginRequest(defaultUser).asHttpPost();
        approvedUserLogin = reqFactory.createLoginRequest(approvedUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            log.trace("Seeding objects ...");
            HttpAssert.assertStatus(httpClient, adminUserLogin, 300, 399, "Unable to login as admin user!");
            
            // A generic project with adminUser as a PI
            project = new Project();
            project.setName("FileHttpAPIIT Test Project");
            project.setDescription("Test Project For TestHttpAPIIT");
            project.addNumber("123456");
            project.setFundingEntity("Cash");
            project.setStartDate(new DateTime(2012, 6, 13, 0, 0));
            project.setEndDate(new DateTime(2013, 6, 13, 0, 0));
            project.addPi(adminUser.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(httpClient);
            
            log.trace("Seeded Project, id {}", project.getId());
            log.trace(project.toString());
            
            // A generic collection with approvedUser as a depositor
            collection = new Collection();
            collection.setTitle("FileHttpAPIIT Test Collection");
            collection.setSummary("Test Collection for TestHttpAPIIT");
            collection.setId(reqFactory.createIdApiRequest(COLLECTION).execute(httpClient));
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            httpClient.execute(reqFactory.createCollectionRequest(collection, project).asHttpPost()).getEntity()
                    .getContent().close();
            HttpAssert.assertStatus(httpClient,
                    new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            log.trace("Seeded Collection, id {}", collection.getId());
            log.trace(collection.toString());
            
            httpClient
                    .execute(
                            reqFactory.createSetNewDepositorRequest(approvedUser.getId(), collection.getId())
                                    .asHttpPost()).getEntity().getContent().close();
            
            log.trace("Added depositor {} to Collection {}", approvedUser.getId(), collection.getId());
            
            java.io.File tempFile = new java.io.File(FileHttpAPIIT.class.getResource(LINUX_FILE_ZIP_PATH).toURI());
            dataFileContents = IOUtils.toByteArray(new FileInputStream(tempFile));
            dataFileLengh = tempFile.length();
            
            dataFile = new DataFile(null, tempFile.getName(), tempFile.toURI().toURL().toExternalForm(), URLConnection
                    .getFileNameMap().getContentTypeFor(tempFile.getName()), tempFile.getPath(), tempFile.length(),
                    new ArrayList<String>());
            
            // A list of data files
            List<DataFile> dataFileList = new ArrayList<DataFile>();
            dataFileList.add(dataFile);
            
            // A dataset to contain the data file
            dataSetDateTime = DateTime.now();
            
            dataItem = new DataItem("FileHttpAPIIT Test DataItem", "Test DataItem for TestHttpAPIIT", reqFactory
                    .createIdApiRequest(DATA_SET).execute(httpClient), approvedUser.getId(), dataSetDateTime,
                    dataFileList, new ArrayList<String>(), collection.getId());
            dataItem.setParentId(collection.getId());
            dataFile.setParentId(dataItem.getId());
            
            log.trace("Created DataItem with name {} and id {}", dataItem.getName(), dataItem.getId());
            log.trace(dataItem.toString());
            log.trace("DataItem ({}, {}) files:", dataItem.getName(), dataItem.getId());
            for (DataFile f : dataItem.getFiles()) {
                log.trace("File id {}, name {}", f.getId(), f.getName());
            }
            
            org.dataconservancy.ui.model.Package thePackage = new org.dataconservancy.ui.model.Package();
            updatePackageId = reqFactory.createIdApiRequest(PACKAGE).execute(httpClient);
            thePackage.setId(updatePackageId);
            
            log.trace("Created Package, id {}", thePackage.getId());
            log.trace(thePackage.toString());
            
            httpClient
                    .execute(
                            reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem, collection.getId(),
                                    tempFile).asHttpPost()).getEntity().getContent().close();
            
            // The Following construct seems suspicious. I am able to pull the
            // dataset from the archive via pollAndQueryArchiveForDataset, but
            // still end up making multiple calls to the getDepositStatusUrl
            // before I get back a deposited response.
            // I am, however, going to leave it be for now.
            dataItem = archiveSupport.pollAndQueryArchiveForDataItem(dataItem.getId());
            
            String content;
            tryCount = 0;
            do {
                HttpResponse response = httpClient.execute(new HttpGet(urlConfig
                        .getDepositStatusUrl(thePackage.getId()).toURI()));
                content = IOUtils.toString(response.getEntity().getContent());
                Thread.sleep(1000L);
            }
            while (!content.contains("DEPOSITED") && tryCount++ < maxTries);
            
            log.trace("Seeded Package {} and DataItem {}, {}", new Object[] { thePackage.getId(), dataItem.getId(),
                    dataItem.getName() });
            log.trace(dataItem.toString());
            for (DataFile f : dataItem.getFiles()) {
                log.trace(f.toString());
            }
            
            // This is the datafile/dataset used in the deprecated file scenario
            java.io.File tempFileToUpdate = java.io.File.createTempFile("FileToUpdateHttpAPIIT", ".txt");
            tempFileToUpdate.deleteOnExit();
            PrintWriter updatedOut = new PrintWriter(tempFileToUpdate);
            dataFileToUpdateContents = "Can haz data?  Again.";
            updatedOut.print(dataFileToUpdateContents);
            updatedOut.close();
            
            dataFileToUpdate = new DataFile(null, tempFileToUpdate.getName(), tempFileToUpdate.toURI().toURL()
                    .toExternalForm(), URLConnection.getFileNameMap().getContentTypeFor(tempFileToUpdate.getName()),
                    tempFileToUpdate.getPath(), tempFileToUpdate.getTotalSpace(), new ArrayList<String>());
            
            // A list of data files
            List<DataFile> dataFileListToUpdate = new ArrayList<DataFile>();
            dataFileListToUpdate.add(dataFileToUpdate);
            
            // A dataset to contain the data file
            dataSetDateTime = DateTime.now();
            
            dataSetToUpdate = new DataItem("FileToUpdateHttpAPIIT Test DataItem",
                    "Test DataItem to update for TestHttpAPIIT", reqFactory.createIdApiRequest(DATA_SET).execute(
                            httpClient), approvedUser.getId(), dataSetDateTime, dataFileListToUpdate,
                    new ArrayList<String>(), collection.getId());
            dataSetToUpdate.setParentId(collection.getId());
            dataFileToUpdate.setParentId(dataSetToUpdate.getId());

            log.trace("Created DataItem with name {} and id {}", dataSetToUpdate.getName(), dataSetToUpdate.getId());
            log.trace(dataSetToUpdate.toString());
            log.trace("DataItem ({}, {}) files:", dataSetToUpdate.getName(), dataSetToUpdate.getId());
            for (DataFile f : dataSetToUpdate.getFiles()) {
                log.trace("File id {}, name {}", f.getId(), f.getName());
            }
            
            org.dataconservancy.ui.model.Package packageToUpdate = new org.dataconservancy.ui.model.Package();
            packageToUpdate.setId(reqFactory.createIdApiRequest(PACKAGE).execute(httpClient));
            
            httpClient
                    .execute(
                            reqFactory.createSingleFileDataItemDepositRequest(packageToUpdate, dataSetToUpdate,
                                    collection.getId(), tempFileToUpdate).asHttpPost()).getEntity().getContent()
                    .close();
            
            dataSetToUpdate = archiveSupport.pollAndQueryArchiveForDataItem(dataSetToUpdate.getId());
            
            tryCount = 0;
            do {
                HttpResponse response = httpClient.execute(new HttpGet(urlConfig.getDepositStatusUrl(
                        packageToUpdate.getId()).toURI()));
                content = IOUtils.toString(response.getEntity().getContent());
                Thread.sleep(1000L);
            }
            while (!content.contains("DEPOSITED") && tryCount++ < maxTries);
            
            log.trace("Seeded Package {} and DataItem {}, {}",
                    new Object[] { thePackage.getId(), dataSetToUpdate.getId(), dataSetToUpdate.getName() });
            log.trace(dataSetToUpdate.toString());
            for (DataFile f : dataSetToUpdate.getFiles()) {
                log.trace(f.toString());
            }
            
            httpClient.execute(logout).getEntity().getContent().close();
            
            areObjectsSeeded = true;
        }
    }
    
    @After
    public void tearDown() throws Exception {
        log.trace("Finishing " + testMethodName);
    }
    
    /**
     * Tests getting an existing file with request headers being null. Expected return code 200. Verify response'
     * headers and content per API.
     */
    @Test
    public void testGetFile_EmptyRequestHeaderGoodRequest() throws Exception {
        logStart("testGetFile_EmptyRequestHeaderGoodRequest");
        HttpAssert.assertStatus(httpClient, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        final String fileId = dataItem.getFiles().get(0).getId();
        HttpGet request = new HttpGet(fileId);
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 200 response code retrieving '" + fileId + "': "
                + response.getStatusLine().getReasonPhrase(), 200, response.getStatusLine().getStatusCode());
        assertNotNull("Excpected an ETAG code!", response.getFirstHeader(ETAG));
        assertNotNull("Expected a content disposition header!", response.getFirstHeader(CONTENT_DISPOSITION));
        assertTrue("Expected content disposition to contain the file name!",
                response.getFirstHeader(CONTENT_DISPOSITION).getValue().contains(dataFile.getName()));
        assertNotNull("Expected a content type header!", response.getFirstHeader(CONTENT_TYPE));
        assertEquals("Expected a content type header of " + dataFile.getFormat() + "!", dataFile.getFormat(), response
                .getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals("Expected content length of " + dataFileLengh + "!", dataFileLengh,
                Long.parseLong(response.getFirstHeader(CONTENT_LENGTH).getValue()));
        assertNotNull("Expected a last modified header!", response.getFirstHeader(LAST_MODIFIED));
        
        // I would like to test for the contents of the last modified header thusly
        // assertEquals("Expected a last modified header of " + dataItem.getDepositDate() + "!",
        // dataItem.getDepositDate().toString(), response.getFirstHeader(LAST_MODIFIED).getValue());
        assertArrayEquals(dataFileContents, IOUtils.toByteArray(response.getEntity().getContent()));
        // assertEquals(dataFileContents, IOUtils.toString(response.getEntity().getContent()));
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Tests getting a File with all of the request headers appropriately set. Expect return code: 200. Verify
     * response's headers and content per API. Pay attention to request header's qualifying criteria.
     * 
     */
    @Test
    public void testGetFile_FullRequestHeadersGoodRequest() throws Exception {
        logStart("testGetFile_FullRequestHeadersGoodRequest");
        HttpAssert.assertStatus(httpClient, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        final String fileId = dataItem.getFiles().get(0).getId();
        HttpGet request = new HttpGet(fileId);
        request.setHeader("Accept", dataFile.getFormat());
        request.setHeader("If-Modified-Since", dataItem.getDepositDate().minusDays(2).toDate().toString());
        
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 200 response code!", 200, response.getStatusLine().getStatusCode());
        assertNotNull("Excpected an ETAG code!", response.getFirstHeader(ETAG));
        assertNotNull("Expected a content disposition header!", response.getFirstHeader(CONTENT_DISPOSITION));
        assertTrue("Expected content disposition to contain the file name!",
                response.getFirstHeader(CONTENT_DISPOSITION).getValue().contains(dataFile.getName()));
        assertNotNull("Expected a content type header!", response.getFirstHeader(CONTENT_TYPE));
        assertEquals("Expected a content type header of " + dataFile.getFormat() + "!", dataFile.getFormat(), response
                .getFirstHeader(CONTENT_TYPE).getValue());
        assertNotNull("Expected a last modified header!", response.getFirstHeader(LAST_MODIFIED));
        // I would like to test for the contents of the last modified header thusly
        // assertEquals("Expected a last modified header of " + dataItem.getDepositDate() + "!",
        // dataItem.getDepositDate().toString(), response.getFirstHeader(LAST_MODIFIED).getValue());
        assertArrayEquals(dataFileContents, IOUtils.toByteArray(response.getEntity().getContent()));
        assertEquals("Expected content length of " + dataFileLengh + "!", dataFileLengh,
                Long.parseLong(response.getFirstHeader(CONTENT_LENGTH).getValue()));
        // assertEquals(dataFileContents, IOUtils.toString(response.getEntity().getContent()));
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Tests attempting to get a file as a user that has not/could not log in. This should succeed.
     * 
     * Expected return code: 200
     */
    @Test
    public void testGetFile_ByUnauthenticatedUser() throws Exception {
        logStart("testGetFile_ByUnauthenticatedUser");
        
        httpClient.execute(logout).getEntity().getContent().close();
        
        final String fileId = dataItem.getFiles().get(0).getId();
        HttpGet request = new HttpGet(fileId);
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 200 response code!", 200, response.getStatusLine().getStatusCode());
        
        response.getEntity().getContent().close();
    }
    
    /**
     * Tests attempting to get a file by a user that has been authenticated Should be able to get the file. Expected
     * return code: 200.
     */
    @Test
    public void testGetFile_ByUnauthorizedUser() throws Exception {
        HttpAssert.assertStatus(httpClient, defaultUserLogin, 300, 399, "Unable to login as default user!");
        
        final String fileId = dataItem.getFiles().get(0).getId();
        HttpGet request = new HttpGet(fileId);
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 200 response code!", 200, response.getStatusLine().getStatusCode());
        
        response.getEntity().getContent().close();
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Tests attempting to retrieved a non-existing files by authorized user. Expected return code: 404
     */
    @Test
    public void testGetFile_NonExistingFile() throws Exception {
        logStart("testGetFile_NonExistingFile");
        
        HttpAssert.assertStatus(httpClient, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        final String nonExistentFileId = reqFactory.createIdApiRequest(DATA_FILE).execute(httpClient);
        HttpGet request = new HttpGet(nonExistentFileId);
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 404 response code!", 404, response.getStatusLine().getStatusCode());
        
        response.getEntity().getContent().close();
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Tests attempting to get an existing file that has not been modified since the date specified in the
     * If-Modified-Since request header. Expected return code: 304
     */
    @Test
    public void testGetFile_FileHasNotBeenModifiedSince() throws Exception {
        logStart("testGetFile_FileHasNotBeenModifiedSince");
        
        HttpAssert.assertStatus(httpClient, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        final String fileId = dataItem.getFiles().get(0).getId();
        HttpGet request = new HttpGet(fileId);
        request.setHeader("If-Modified-Since", dataItem.getDepositDate().plusDays(2).toDate().toString());
        
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 304 response code!", 304, response.getStatusLine().getStatusCode());
        
        // There is no content.
        // response.getEntity().getContent().close();
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Tests attempting to get an existing file with an unacceptable mimetype. Expected return code 406.
     */
    @Test
    public void testGetFile_UnacceptableMimeType() throws Exception {
        logStart("testGetFile_UnacceptableMimeType");
        
        HttpAssert.assertStatus(httpClient, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        final String fileId = dataItem.getFiles().get(0).getId();
        HttpGet request = new HttpGet(fileId);
        request.setHeader("Accept", "text/plain");
        
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 406 response code!", 406, response.getStatusLine().getStatusCode());
        
        response.getEntity().getContent().close();
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Tests attempting getting a file that is no longer a part of a DataItem (DataItem). Expected return code 404
     */
    @Test
    public void testGetFile_DeprecatedFile() throws Exception {
        logStart("testGetFile_DeprecatedFile");
        
        // Get all the original stuff back
        HttpAssert.assertStatus(httpClient, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        final String fileId = dataSetToUpdate.getFiles().get(0).getId();
        HttpGet request = new HttpGet(fileId);
        HttpResponse response = httpClient.execute(request);
        
        assertEquals("Expected a 200 response code!", 200, response.getStatusLine().getStatusCode());
        assertNotNull("Expected an ETAG code!", response.getFirstHeader(ETAG));
        assertNotNull("Expected a content disposition header!", response.getFirstHeader(CONTENT_DISPOSITION));
        assertTrue("Expected content disposition to contain the file name!",
                response.getFirstHeader(CONTENT_DISPOSITION).getValue().contains(dataFileToUpdate.getName()));
        assertNotNull("Expected a content type header!", response.getFirstHeader(CONTENT_TYPE));
        assertEquals("Expected a content type header of " + dataFileToUpdate.getFormat() + "!",
                dataFileToUpdate.getFormat(), response.getFirstHeader(CONTENT_TYPE).getValue());
        assertNotNull("Expected a last modified header!", response.getFirstHeader(LAST_MODIFIED));
        // I would like to test for the contents of the last modified header thusly
        // assertEquals("Expected a last modified header of " + dataSetToUpdate.getDepositDate() + "!",
        // dataSetToUpdate.getDepositDate().toString(), response.getFirstHeader(LAST_MODIFIED).getValue());
        assertEquals(dataFileToUpdateContents, IOUtils.toString(response.getEntity().getContent()));
        
        // Set up a new file and update the dataset
        // A generic data file
        java.io.File newTempFile = java.io.File.createTempFile("FileHttpAPIITUpdated", ".txt");
        newTempFile.deleteOnExit();
        PrintWriter out = new PrintWriter(newTempFile);
        updatedDataFileContents = "Can haz MOAR data?";
        out.print(updatedDataFileContents);
        out.close();
        
        updatedDataFile = new DataFile(null, newTempFile.getName(), newTempFile.toURI().toURL().toExternalForm(),
                URLConnection.getFileNameMap().getContentTypeFor(newTempFile.getName()), newTempFile.getPath(),
                newTempFile.getTotalSpace(), new ArrayList<String>());
        
        // A list of data files
        List<DataFile> newDataFileList = new ArrayList<DataFile>();
        newDataFileList.add(updatedDataFile);
        
        // A dataset to contain the data file
        DateTime newDataSetDateTime = DateTime.now();
        
        updatedDataSet = new DataItem("FileToUpdateHttpAPIIT Test DataItem",
                "Test DataItem to update for TestHttpAPIIT", dataSetToUpdate.getId(), approvedUser.getId(),
                newDataSetDateTime, newDataFileList, new ArrayList<String>(), collection.getId());
        
        org.dataconservancy.ui.model.Package updatedPackage = new org.dataconservancy.ui.model.Package();
        updatedPackage.setId(reqFactory.createIdApiRequest(PACKAGE).execute(httpClient));
        DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(updatedPackage,
                dataSetToUpdate, collection.getId(), newTempFile);
        updateRequest.setIsUpdate(true);
        httpClient.execute(updateRequest.asHttpPost()).getEntity().getContent().close();
        
        assertNotNull(archiveSupport.pollAndQueryArchiveForDataItem(updatedDataSet.getId()));
        
        String content;
        tryCount = 0;
        do {
            final String packageId = updatedPackage.getId();
            final URI depositStatusUri = urlConfig.getDepositStatusUrl(packageId).toURI();
            final HttpGet depositStatusRequest = new HttpGet(depositStatusUri);
            response = httpClient.execute(depositStatusRequest);
            assertFalse(404 == response.getStatusLine().getStatusCode());
            assertTrue(response.getStatusLine().getStatusCode() < 500);
            content = IOUtils.toString(response.getEntity().getContent());
            Thread.sleep(1000L);
        }
        while (!content.contains("DEPOSITED") && tryCount++ < maxTries);
        
        // Check for the old file
        request = buildGetRequest(getIDPart(dataSetToUpdate.getFiles().get(0).getId()), false);
        response = httpClient.execute(request);
        
        assertEquals("Expected a 404 response code!", 404, response.getStatusLine().getStatusCode());
        
        response.getEntity().getContent().close();
        
        httpClient.execute(logout).getEntity().getContent().close();
    }
    
    /**
     * Builds an HttpGet to use for getting file.
     * 
     * @param id
     *            id of the file to get
     * @return The completed http request that can be passed to HttpClient
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpGet buildGetRequest(String id, boolean collections) throws UnsupportedEncodingException,
            URISyntaxException {
        HttpGet request = null;
        
        String arguments = "/file";
        
        if (id != null && !id.isEmpty()) {
            arguments += "/" + id;
        }
        
        URL baseUrl = urlConfig.getBaseUrl();
        
        URI uri = URIUtils
                .createURI(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), arguments, null, null);
        
        request = new HttpGet(uri);
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
    
    private void logStart(String testName) {
        log.trace("Starting " + testName);
        this.testMethodName = testName;
    }
}
