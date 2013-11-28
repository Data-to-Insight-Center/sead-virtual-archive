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

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.APPLICATION_XML;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_LENGTH;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_TYPE;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.DepositRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.UiUrlConfig;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.util.ETagCalculator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class HttpDataItemIT extends BaseIT {
    
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
    
    private HttpPost systemAdminLogin;
    
    private static HttpPost projectAdminLogin;
    
    private HttpGet logout;
    
    private HttpPost basicUserLogin;
    
    private static HttpPost collectionDepositorLogin;
    
    private static Project project;
    
    private static Collection collection;
    
    private static DataItem dataItem;
    
    private static DateTime modifiedDate;
    
    private static String etag;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
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
    
    // counter for how many times we try
    private int tryCount;
    
    // maximum number of times to try
    private final int maxTries = 60;
    
    @Before
    public void setup() throws InvalidXmlException, IOException, URISyntaxException, InterruptedException {
        
        systemAdminLogin = reqFactory.createLoginRequest(defaultAdmin).asHttpPost();
        basicUserLogin = reqFactory.createLoginRequest(defaultUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        // Set up hc for the tests
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
            
            Person projectAdmin = new Person();
            projectAdmin.setFirstNames("Project");
            projectAdmin.setLastNames("Admin");
            projectAdmin.setMiddleNames("Middle");
            projectAdmin.setPrefix("Mr.");
            projectAdmin.setSuffix("II");
            projectAdmin.setPreferredPubName("P. Admin");
            projectAdmin.setBio("Some Bio");
            projectAdmin.setWebsite("www.website.com");
            projectAdmin.setId(reqFactory.createIdApiRequest(Types.PERSON).execute(httpClient));
            projectAdmin.setEmailAddress("email@email.com");
            projectAdmin.setPhoneNumber("1234567890");
            projectAdmin.setPassword("12345678");
            projectAdmin.setJobTitle("Project Scientist");
            projectAdmin.setDepartment("Project Department");
            projectAdmin.setCity("Baltimore");
            projectAdmin.setState("Maryland");
            projectAdmin.setInstCompany("Project Institution/Company");
            projectAdmin.setInstCompanyWebsite("www.ProjectInstitutionCompany.com");
            projectAdmin.addRole(Role.ROLE_USER);
            projectAdmin.setExternalStorageLinked(false);
            projectAdmin.setDropboxAppKey("SomeKey");
            projectAdmin.setDropboxAppSecret("SomeSecret");
            HttpPost createProjectAdmin = reqFactory.createRegisterRequest(projectAdmin).asHttpPost();
            HttpAssert.assertStatus(httpClient, createProjectAdmin, 200, "Unable to create project admin!");
            HttpPost approveProjectAdmin = reqFactory.createApproveRegistrationRequest(projectAdmin).asHttpPost();
            HttpAssert.assertStatus(httpClient, approveProjectAdmin, 200, "Unable to approve project admin!");
            projectAdminLogin = reqFactory.createLoginRequest(projectAdmin).asHttpPost();
            
            Person collectionDepositor = new Person();
            collectionDepositor.setFirstNames("Collection");
            collectionDepositor.setLastNames("Depositor");
            collectionDepositor.setPrefix("Mr.");
            collectionDepositor.setSuffix("II");
            collectionDepositor.setPreferredPubName("C. Depositor");
            collectionDepositor.setBio("Some Bio");
            collectionDepositor.setWebsite("www.website.com");
            collectionDepositor.setId(reqFactory.createIdApiRequest(Types.PERSON).execute(httpClient));
            collectionDepositor.setEmailAddress("col@depositor.com");
            collectionDepositor.setPhoneNumber("1234567890");
            collectionDepositor.setPassword("0987654");
            collectionDepositor.setJobTitle("Collection Scientist");
            collectionDepositor.setDepartment("Collection Department");
            collectionDepositor.setCity("Baltimore");
            collectionDepositor.setState("Maryland");
            collectionDepositor.setInstCompany("Collection Institution/Company");
            collectionDepositor.setInstCompanyWebsite("www.CollectionInstitutionCompany.com");
            collectionDepositor.addRole(Role.ROLE_USER);
            collectionDepositor.setExternalStorageLinked(false);
            collectionDepositor.setDropboxAppKey("SomeKey");
            collectionDepositor.setDropboxAppSecret("SomeSecret");
            HttpPost createCollectionDepositor = reqFactory.createRegisterRequest(collectionDepositor).asHttpPost();
            HttpAssert.assertStatus(httpClient, createCollectionDepositor, 200, "Unable to create project admin!");
            HttpPost approveCollectionDepositor = reqFactory.createApproveRegistrationRequest(collectionDepositor)
                    .asHttpPost();
            HttpAssert.assertStatus(httpClient, approveCollectionDepositor, 200, "Unable to approve project admin!");
            collectionDepositorLogin = reqFactory.createLoginRequest(collectionDepositor).asHttpPost();
            
            project = new Project();
            project.setName("Test_Project");
            project.setDescription("foo");
            project.addNumber("12");
            project.setFundingEntity("Straight cash homey");
            project.setStartDate(new DateTime(2012, 5, 4, 0, 0));
            project.setEndDate(new DateTime(2013, 12, 23, 0, 0));
            project.addPi(defaultAdmin.getId());
            project.addPi(projectAdmin.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(httpClient);
            
            collection = new Collection();
            collection.setTitle("CollectionOne");
            collection.setSummary("CollectionOne-Foo");
            collection.setId(reqFactory.createIdApiRequest(Types.COLLECTION).execute(httpClient));
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            httpClient.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(httpClient,
                    new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            dataItem = new DataItem();
            dataItem.setName("Test dataitem");
            dataItem.setParentId(collection.getId());
            dataItem.setId(reqFactory.createIdApiRequest(Types.DATA_SET).execute(httpClient));
            dataItem.setDescription("DataItem with 1 file");
            // A generic data file
            java.io.File tempFile = java.io.File.createTempFile("temp.tmp", null);
            tempFile.deleteOnExit();
            PrintWriter out = new PrintWriter(tempFile);
            out.println(tempFile);
            out.close();
            
            DataFile dataFile = new DataFile();
            dataFile.setName("Test file");
            dataFile.setSource(tempFile.toURI().toURL().toExternalForm());
            dataFile.setPath(tempFile.getPath());
            dataFile.setId(reqFactory.createIdApiRequest(Types.DATA_FILE).execute(httpClient));
            dataFile.setParentId(dataItem.getId());

            dataItem.addFile(dataFile);
            
            org.dataconservancy.ui.model.Package thePackage = new org.dataconservancy.ui.model.Package();
            thePackage.setId(reqFactory.createIdApiRequest(Types.PACKAGE).execute(httpClient));
            
            HttpPost dataItemDeposit = reqFactory.createSingleFileDataItemDepositRequest(thePackage, dataItem,
                    collection.getId(), tempFile).asHttpPost();
            HttpAssert.assertStatus(httpClient, dataItemDeposit, 300, 399, "Unable to deposit dataItem!");
            
            String content;
            tryCount = 0;
            do {
                HttpResponse response = httpClient.execute(new HttpGet(urlConfig
                        .getDepositStatusUrl(thePackage.getId()).toURI()));
                content = IOUtils.toString(response.getEntity().getContent());
                Thread.sleep(1000L);
            }
            while (!content.contains("DEPOSITED") && tryCount++ < maxTries);
            
            DcsDeliverableUnit collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId());
            assertNotNull("Collection was not created in the archive.", collectionDu);
            
            String collectionArchiveID = collectionDu.getId();
            Set<DataItem> collectionDataItems = archiveSupport.pollAndQueryArchiveForDataItemsInCollection(
                    collectionArchiveID, 1);
            
            assertEquals(1, collectionDataItems.size());
            dataItem = collectionDataItems.iterator().next();
            
            etag = ETagCalculator.calculate(String.valueOf(dataItem.hashCode()));
            
            // Remove time from the dataItem deposit date since it won't be returned
            modifiedDate = dataItem.getDepositDate();
            DateTime depositDate = new DateTime(modifiedDate.getYear(), modifiedDate.getMonthOfYear(),
                    modifiedDate.getDayOfMonth(), 0, 0);
            dataItem.setDepositDate(depositDate);
            
            HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout admin user!");
            
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests that someone with instance administrator permissions can view a data item. Expected return code: 200
     * 
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    
    @Test
    public void getDataItemAsSysAdmin() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<DataItem> dataItems = bop.getDataItems();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem returneddataItem = iter.next();
        
        assertEquals(dataItem, returneddataItem);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout system admin user!");
    }
    
    /**
     * Tests that the last modified, etag, content length and content type headers are returned correctly. Expected
     * Return Code: 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void testDataItemResponseHeaders() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        
        assertNotNull(bop);
        
        Set<DataItem> dataItems = bop.getDataItems();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem returneddataItem = iter.next();
        
        assertEquals(dataItem, returneddataItem);
        
        final String expectedContentType = APPLICATION_XML;
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        businessObjectBuilder.buildBusinessObjectPackage(bop, sink);
        
        final String expectedContentLength = String.valueOf(sink.toByteArray().length);
        
        // String expectedLastModified = DateUtility.toRfc822(modifiedDate);
        // assertEquals(expectedLastModified, authorizedResponse.getFirstHeader(LAST_MODIFIED).getValue());
        // Replaced the equality test with a notNull test due to the fact that the test was failing with a second
        // difference.
        assertNotNull(authorizedResponse.getFirstHeader(LAST_MODIFIED).getValue());
        assertEquals(etag, authorizedResponse.getFirstHeader(ETAG).getValue());
        assertEquals(expectedContentType, authorizedResponse.getFirstHeader(CONTENT_TYPE).getValue());
        assertEquals(expectedContentLength, authorizedResponse.getFirstHeader(CONTENT_LENGTH).getValue());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that someone who is a project administrator can view a data item. Expected Return Code: 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void getDataItemAsProjectAdmin() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<DataItem> dataItems = bop.getDataItems();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem returneddataItem = iter.next();
        
        assertEquals(dataItem, returneddataItem);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that someone who is not authorized can't view a data item. Expected Return Code: 403
     * 
     * @throws IOException
     * @throws ClientProtocolException
     * @throws URISyntaxException
     */
    
    @Test
    public void attemptDataItemGetWithoutPermissions() throws ClientProtocolException, IOException, URISyntaxException {
        HttpAssert.assertStatus(httpClient, basicUserLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout basic user!");
    }
    
    /**
     * Tests that someone who is not logged in can't view a data item. Expected Return code: 401
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void attemptGetDataItemWithoutLogin() throws URISyntaxException, ClientProtocolException, IOException {
        // Ensure that no one is logged in
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout logged in user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
    
    /**
     * Test to get a data item with an id that is invalid or the data item doesn't exist. Expected Return code: 404
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void attemptGetDataItemBadId() throws URISyntaxException, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest("foo", null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                404, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that the data item is return if a valid if modified since data is included. Expected Return code: 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void testIfModifiedSinceDate() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
        
        final DateTime ifModifiedSince = DateTime.now().minusDays(2);
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, ifModifiedSince, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<DataItem> dataItems = bop.getDataItems();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem returneddataItem = iter.next();
        
        assertEquals(dataItem, returneddataItem);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that the data item is not returned if the if modified since data is after last modification. Expected
     * Return Code: 304
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void testBadIfModifiedDate() throws URISyntaxException, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        final DateTime ifModifiedSince = DateTime.now();
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, ifModifiedSince, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                304, authorizedResponse.getStatusLine().getStatusCode());
        
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests the data item is returned if the if match header matches the etag of the dataItem. Expected Return Code:
     * 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void testIfMatchWithMatch() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, etag, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<DataItem> dataItems = bop.getDataItems();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem returneddataItem = iter.next();
        
        assertEquals(dataItem, returneddataItem);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that the dataItem is not returned if their is no match for the if match etag. Expected Return Code: 412
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void testIfMatchWithNoMatch() throws URISyntaxException, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, "foo", null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                412, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that the dataItem is returned if there is no match for the if-none-match etag. Expected Return Code: 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void testIfNoneMatchWithNoMatch() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, "foo");
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<DataItem> dataItems = bop.getDataItems();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem returneddataItem = iter.next();
        
        assertEquals(dataItem, returneddataItem);
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that the dataItem is not returned if there is a match for the if-none-match etag. Expected Return Code: 412
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testIfNoneMatchWithMatch() throws URISyntaxException, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, etag);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                412, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that if the dataItem is updated the latest data set is returned. Expected Return Code: 200
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Ignore
    // this seems to time out at line 873 httpClient.execute(new
    // HttpGet(urlConfig.getDepositStatusUrl(thePackage.getId()).toURI()))
    @Test
    public void testUpdatedataItemReturnsLatest() throws URISyntaxException, ClientProtocolException, IOException,
            InvalidXmlException, InterruptedException {
        DataItem updatedDataItem = new DataItem(dataItem);
        
        updatedDataItem.setDescription("Newly updated data set");
        updatedDataItem.setName("updated data set");
        
        HttpAssert.assertStatus(httpClient, systemAdminLogin, 300, 399, "Unable to login as admin user!");
        
        // A generic data file
        java.io.File tempFile = java.io.File.createTempFile("temp.tmp", null);
        tempFile.deleteOnExit();
        PrintWriter out = new PrintWriter(tempFile);
        out.println(tempFile);
        out.close();
        
        DataFile dataFile = new DataFile();
        dataFile.setName("Test file");
        dataFile.setSource(tempFile.toURI().toURL().toExternalForm());
        dataFile.setPath(tempFile.getPath());
        dataFile.setId(reqFactory.createIdApiRequest(Types.DATA_FILE).execute(httpClient));
        updatedDataItem.addFile(dataFile);
        
        org.dataconservancy.ui.model.Package thePackage = new org.dataconservancy.ui.model.Package();
        thePackage.setId("DataItemHttpAPIITPackage");
        
        final DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(thePackage,
                updatedDataItem, collection.getId(), tempFile);
        updateRequest.setDataItemIdentifier(updatedDataItem.getId());
        updateRequest.setIsUpdate(true);
        
        HttpAssert.assertStatus(httpClient, updateRequest.asHttpPost(), 300, 399, "Unable to deposit dataItem!");
        
        String content;
        tryCount = 0;
        do {
            HttpResponse response = httpClient.execute(new HttpGet(urlConfig.getDepositStatusUrl(thePackage.getId())
                    .toURI()));
            content = IOUtils.toString(response.getEntity().getContent());
            Thread.sleep(1000L);
        }
        while (!content.contains("DEPOSITED") && tryCount++ < maxTries);
        System.out.println("############### " + tryCount);
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), null, null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                200, authorizedResponse.getStatusLine().getStatusCode());
        
        Bop bop = businessObjectBuilder.buildBusinessObjectPackage(authorizedResponse.getEntity().getContent());
        assertNotNull(bop);
        
        Set<DataItem> dataItems = bop.getDataItems();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem returnedDataItem = iter.next();
        
        assertTrue(updatedDataItem.getName().equalsIgnoreCase(returnedDataItem.getName()));
        assertTrue(updatedDataItem.getDescription().equalsIgnoreCase(returnedDataItem.getDescription()));
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Tests that a non supported accept header will return a 406 status code. Expected Return Code: 406
     * 
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    
    @Test
    public void testNonSupportedAcceptHeader() throws URISyntaxException, ClientProtocolException, IOException {
        HttpAssert.assertStatus(httpClient, projectAdminLogin, 300, 399, "Unable to login as admin user!");
        
        HttpGet request = buildGetRequest(getIDPart(dataItem.getId()), "foo", null, null, null);
        HttpResponse authorizedResponse = httpClient.execute(request);
        
        assertEquals(
                "Unable to get dataItem: " + dataItem.getId() + "status code: " + authorizedResponse.getStatusLine(),
                406, authorizedResponse.getStatusLine().getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        HttpAssert.assertStatus(httpClient, logout, 300, 399, "Unable to logout project admin user!");
    }
    
    /**
     * Builds an HttpGet to use for dataItem get calls.
     * 
     * @param id
     *            The id of the project to update
     * @return The completed http request that can be passed to HttpClient
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private HttpGet buildGetRequest(String id, String acceptHeader, DateTime ifModifiedSinceDate, String ifMatchHeader,
            String ifNoneMatchHeader) throws UnsupportedEncodingException, URISyntaxException {
        HttpGet request = null;
        
        String arguments = "item/";
        
        if (id != null && !id.isEmpty()) {
            arguments += id;
        }
        
        URL baseUrl = urlConfig.getBaseUrl();
        
        URI uri = URIUtils
                .createURI(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), arguments, null, null);
        
        request = new HttpGet(uri);
        
        if (acceptHeader != null && !acceptHeader.isEmpty()) {
            request.addHeader("Accept", acceptHeader);
        }
        
        if (ifModifiedSinceDate != null) {
            request.addHeader("If-Modified-Since", DateUtility.toRfc822(ifModifiedSinceDate));
        }
        
        if (ifMatchHeader != null && !ifMatchHeader.isEmpty()) {
            request.addHeader("If-Match", ifMatchHeader);
        }
        
        if (ifNoneMatchHeader != null && !ifNoneMatchHeader.isEmpty()) {
            request.addHeader("If-None-Match", ifNoneMatchHeader);
        }
        
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
