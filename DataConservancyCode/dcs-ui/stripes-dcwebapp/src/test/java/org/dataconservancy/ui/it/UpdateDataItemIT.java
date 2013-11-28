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
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
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
import org.junit.Ignore;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class UpdateDataItemIT extends BaseIT {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person authorizedDepositor;
    
    @Autowired
    @Qualifier("defaultAdmin")
    private Person admin;
    
    @Autowired
    @Qualifier("defaultUser")
    private Person userWithoutDeposit;
    
    /**
     * This is the sample data file that is deposited. The contents of the file aren't important, and is not modified in
     * any way. So this file is created only once (via {@link #createSampleDataFile(String, String)} and re-used for
     * each test method.
     */
    private static File sampleDataFile;
    
    /**
     * This is the collection that datasets are deposited into.
     * <p/>
     * It isn't modified after it is created and put in the archive (none of the tests should need to modify this
     * collection), so to save time in test fixup we keep it as a static object. Otherwise we'd need to communicate with
     * the archive for each test method, creating a new collection each time and polling the archive.
     */
    private static Collection collection;
    
    /**
     * State recording whether or not collaborating test objects have been seeded.
     * <p/>
     * This flag is modified during test setup and checked when each test method is invoked. If the flag is false, the
     * collaborating test objects are created, if true, we skip creating the test objects because they already exist.
     * That way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They
     * aren't in an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    @Before
    public void setup() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        if (!areObjectsSeeded) {
            log.debug("Seeding collaborating test objects.");
            
            HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(admin).asHttpPost(), 300, 399,
                    "Unable to login as an admin user!");
            log.debug("Logging in as an admin: " + admin.getEmailAddress());
            
            Project project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            project.addPi(authorizedDepositor.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(project.getId()).toURI()), 200,
                    "Unable to create project " + project);
            log.debug("Created project {}, {}", project.getId(), project);
            
            collection = new Collection();
            collection.setId("Card746SetupCollection");
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            
            assertNotNull("Expected a Collection to be created in the archive!",
                    archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId()));
            
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            log.debug("Created collection {}", collection.getId());
            
            HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399,
                    "Unable to logout admin user!");
            
            log.debug("Logging out admin {}", admin.getEmailAddress());
            
            sampleDataFile = createSampleDataFile("DepositSingleFileIT", ".txt");
            sampleDataFile.deleteOnExit();
            
            areObjectsSeeded = true;
            
            log.debug("Successfully seeded collaborating test objects.");
        }
    }
    
    public void testDepositerCanUpdateFile() throws Exception {
        
        final String datasetId = "Card746DepositorDataSetToUpdate";
        
        // First deposit a file as the depositor
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(authorizedDepositor).asHttpPost(), 300, 399,
                "Unable to login as " + authorizedDepositor);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        datasetDepositAndVerify(datasetId, collection.getId());
        
        // Wait for the deposit to complete
        pollAndCheckDepositStatus();
        
        datasetUpdateAndVerify(datasetId);
        
    }
    
    @Test
    public void testAdminCanUpdateFile() throws Exception {
        final String datasetId = "Card746AdminDataSetToUpdate";
        
        // First deposit a file as the depositor
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(admin).asHttpPost(), 300, 399, "Unable to login as "
                + admin);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        datasetDepositAndVerify(datasetId, collection.getId());
        
        pollAndCheckDepositStatus();
        
        datasetUpdateAndVerify(datasetId);
    }
    
    @Test
    public void testFileInDifferentCollectionBlockedFromUpdate() throws Exception {
        
        // Logging in as the admin to create a new project and collection
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(admin).asHttpPost(), 300, 399, "Unable to login as "
                + admin);
        
        // Create a new project that authorized depositor is not a pi on.
        Project differentProject = new Project();
        
        differentProject.setDescription("A seeded project for use with ITs");
        differentProject.setEndDate(DateTime.now());
        differentProject.setFundingEntity("NSF");
        differentProject.setStartDate(DateTime.now());
        differentProject.setName("Seeded Project");
        differentProject.addNumber("1234");
        differentProject.setFundingEntity("Cash Money");
        
        differentProject = reqFactory.createProjectApiAddRequest(differentProject).execute(hc);
        
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProjectUrl(differentProject.getId()).toURI()), 200,
                "Unable to create project " + differentProject);
        log.debug("Created project {}, {}", differentProject.getId(), differentProject);
        
        Collection differentCollection = new Collection();
        differentCollection.setId("Card746-AdminOnlyCollection");
        differentCollection.setTitle("Seeded Collection");
        differentCollection.setSummary("A seeded collection for use with ITs");
        List<PersonName> creators = new ArrayList<PersonName>();
        creators.add(new PersonName("Mr.", "Jack", "John", "Doe", "II"));
        differentCollection.setCreators(creators);
        createCollectionRequest.setCollection(differentCollection);
        createCollectionRequest.setProjectId(differentProject.getId());
        hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
        
        assertNotNull("Expected a Collection to be created in the archive!",
                archiveSupport.pollAndQueryArchiveForCollectionDu(differentCollection.getId()));
        
        final String datasetId = "Card746-DepositInDifferentCollection";
        
        // Have admin deposit into collection depositor doesn't have rights to.
        datasetDepositAndVerify(datasetId, differentCollection.getId());
        
        pollAndCheckDepositStatus();
        
        // Log out admin and login in depositor
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399,
                "Unable to logout admin user!");
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(authorizedDepositor).asHttpPost(), 300, 399,
                "Unable to login as " + authorizedDepositor);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        // Now try to deposit an update into that collection
        final DataItem updatedDs = createSampleDataset(collection.getId(), "Card746-ThisIsTheUpdate",
                "An updated dataset", datasetId, null);
        
        // Set the current collection to the collection we can deposit to even though the original file belongs to
        // another collection
        final DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(updatedDs,
                collection.getId(), sampleDataFile);
        updateRequest.setDataItemIdentifier(datasetId);
        updateRequest.setIsUpdate(true);
        
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(updateRequest.asHttpPost());
        assertEquals("Status message: " + authorizedResponse.getStatusLine().getReasonPhrase(), 400, authorizedResponse
                .getStatusLine().getStatusCode());
        assertTrue(
                authorizedResponse.getStatusLine().getReasonPhrase(),
                authorizedResponse
                        .getStatusLine()
                        .getReasonPhrase()
                        .equalsIgnoreCase(
                                String.format(props.getProperty("error.user-does-not-have-deposit-permission"),
                                        authorizedDepositor.getId(), differentCollection.getId())));
        
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
    }
    
    @Test
    public void testNonDepositorCantUpdate() throws Exception {
        final String datasetId = "Card746UserWithoutDeposit";
        
        // First deposit a file as the depositor
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(authorizedDepositor).asHttpPost(), 300, 399,
                "Unable to login as " + authorizedDepositor);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        datasetDepositAndVerify(datasetId, collection.getId());
        
        pollAndCheckDepositStatus();
        
        HttpAssert.assertStatus(hc, reqFactory.createLogoutRequest().asHttpGet(), 300, 399,
                "Unable to logout admin user!");
        
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(userWithoutDeposit).asHttpPost(), 300, 399,
                "Unable to login as " + userWithoutDeposit);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        final DataItem updatedDs = createSampleDataset(collection.getId(), "Card746-ThisIsTheUpdate",
                "An updated dataset", datasetId, null);
        
        final DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(updatedDs,
                collection.getId(), sampleDataFile);
        updateRequest.setDataItemIdentifier(datasetId);
        updateRequest.setIsUpdate(true);
        
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
        HttpResponse authorizedResponse = hc.execute(updateRequest.asHttpPost());
        final InputStream contentIn = authorizedResponse.getEntity().getContent();
        final String pageContent = IOUtils.toString(contentIn);
        contentIn.close();
        
        assertEquals(400, authorizedResponse.getStatusLine().getStatusCode());
        final String expectedMessage = String.format(props.getProperty("error.error-depositing-file"),
                sampleDataFile.getName());
        assertTrue("Expected the page content to contain the message '" + expectedMessage + "'.  Actual"
                + " page content was:\n[" + pageContent + "]", pageContent.contains(expectedMessage));
        
        HttpAssert.free(authorizedResponse);
    }
    
    @Test
    public void testCollectionNotValid() throws Exception {
        final String datasetId = "Card746UserWithoutDeposit";
        
        // First deposit a file as the depositor
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(authorizedDepositor).asHttpPost(), 300, 399,
                "Unable to login as " + authorizedDepositor);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        datasetDepositAndVerify(datasetId, collection.getId());
        
        pollAndCheckDepositStatus();
        
        final DataItem updatedDs = createSampleDataset(collection.getId(), "Card746-ThisIsTheUpdate",
                "An updated dataset", datasetId, null);
        
        final DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(updatedDs,
                "foo-collection", sampleDataFile);
        updateRequest.setDataItemIdentifier(datasetId);
        updateRequest.setIsUpdate(true);
        
        // get static text bundle
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
        HttpResponse authorizedResponse = hc.execute(updateRequest.asHttpPost());
        final InputStream contentIn = authorizedResponse.getEntity().getContent();
        final String pageContent = IOUtils.toString(contentIn);
        contentIn.close();
        
        assertEquals(400, authorizedResponse.getStatusLine().getStatusCode());
        final String expectedMessage = String.format(props.getProperty("error.error-depositing-file"),
                sampleDataFile.getName());
        assertTrue("Expected the page content to contain the message '" + expectedMessage + "'.  Actual"
                + " page content was:\n[" + pageContent + "]", pageContent.contains(expectedMessage));
        
        HttpAssert.free(authorizedResponse);
    }
    
    @Test
    public void testDatasetNotValid() throws Exception {
        final String datasetId = "Card746UserWithoutDeposit";
        
        // First deposit a file as the depositor
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(authorizedDepositor).asHttpPost(), 300, 399,
                "Unable to login as " + authorizedDepositor);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        datasetDepositAndVerify(datasetId, collection.getId());
        
        final DataItem updatedDs = createSampleDataset(collection.getId(), "Card746-ThisIsTheUpdate",
                "An updated dataset", "foo-dataset", null);

        final DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(updatedDs,
                collection.getId(), sampleDataFile);
        updateRequest.setDataItemIdentifier("foo-dataset");
        updateRequest.setIsUpdate(true);
        
        // get static text bundle
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(updateRequest.asHttpPost());
        assertEquals("Status message: " + authorizedResponse.getStatusLine().getReasonPhrase(), 400, authorizedResponse
                .getStatusLine().getStatusCode());
        assertTrue(
                authorizedResponse.getStatusLine().getReasonPhrase(),
                authorizedResponse
                        .getStatusLine()
                        .getReasonPhrase()
                        .equalsIgnoreCase(
                                String.format(props.getProperty("error.dataset-to-update-not-found"), "foo-dataset")));
        
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
    }
    
    @Test
    @Ignore("Given updates to the way data item and data files are deposited, Data Items will no longer have pending deposit status so this test is no longer valid.")
    public void testPendingDepositFails() throws Exception {
        final String datasetId = "Card746PendingDataSetToUpdate";
        
        // First deposit a file as the depositor
        // Login by POSTing to the Login Form. Expect a 302 redirect to the original URL
        HttpAssert.assertStatus(hc, reqFactory.createLoginRequest(authorizedDepositor).asHttpPost(), 300, 399,
                "Unable to login as " + authorizedDepositor);
        
        // Assert we are logged in
        HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getProfileUrl().toURI()), 200);
        
        datasetDepositAndVerify(datasetId, collection.getId());
        
        final DataItem updatedDs = createSampleDataset(collection.getId(), "Card746-ThisIsTheUpdate",
                "An updated dataset", datasetId, null);
        
        final DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(updatedDs,
                collection.getId(), sampleDataFile);
        
        updateRequest.setDataItemIdentifier(datasetId);
        updateRequest.setIsUpdate(true);
        
        // get static text bundle
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(updateRequest.asHttpPost());
        assertEquals("Status message: " + authorizedResponse.getStatusLine().getReasonPhrase(), 400, authorizedResponse
                .getStatusLine().getStatusCode());
        assertTrue(
                authorizedResponse.getStatusLine().getReasonPhrase(),
                authorizedResponse
                        .getStatusLine()
                        .getReasonPhrase()
                        .equalsIgnoreCase(
                                String.format(props.getProperty("error.dataset-to-update-still-pending"), datasetId)));
        
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
    }
    
    /**
     * Create a new DataItem object from the supplied parameters. If a parameter is null, it won't be set on the
     * returned object.
     * 
     * @param name
     * @param description
     * @param identifier
     * @param depositor
     * @return the newly created DataItem
     */
    private DataItem createSampleDataset(String parentCollectionId, String name, String description, String identifier,
                                         Person depositor) {
        final DataItem ds = new DataItem();
        if (name != null) {
            ds.setName(name);
        }
        if (description != null) {
            ds.setDescription(description);
        }
        if (identifier != null) {
            ds.setId(identifier);
        }
        if (depositor != null) {
            ds.setDepositorId(depositor.getId());
        }
        if (parentCollectionId != null) {
            ds.setParentId(parentCollectionId);
        }
        return ds;
    }
    
    private void datasetDepositAndVerify(String datasetId, String collectionId) throws Exception {
        // Create a sample dataset object
        final DataItem ds = createSampleDataset(collection.getId(), "Card746-ThisIsGoingToBeUpdated",
                "A Sample DataItem", datasetId, null);
        
        final DepositRequest depositRequest = reqFactory.createSingleFileDataItemDepositRequest(ds, collectionId,
                sampleDataFile);
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(depositRequest.asHttpPost());
        assertEquals("Reponse: " + authorizedResponse.getStatusLine().getReasonPhrase(), 302, authorizedResponse
                .getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("The DeliverableUnit representing the deposited DataItem (" + ds.getId() + ") was not "
                + "found.", archiveSupport.pollAndQueryArchiveForDataItem(ds.getId()));
    }
    
    private void datasetUpdateAndVerify(String datasetId) throws Exception {
        final DataItem updatedDs = createSampleDataset(collection.getId(), "Card746-ThisIsTheUpdate",
                "An updated dataset", datasetId, null);
        
        final DepositRequest updateRequest = reqFactory.createSingleFileDataItemDepositRequest(updatedDs,
                collection.getId(), sampleDataFile);
        updateRequest.setDataItemIdentifier(datasetId);
        updateRequest.setIsUpdate(true);
        
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(updateRequest.asHttpPost());
        assertEquals(authorizedResponse.getStatusLine().getReasonPhrase(), 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        DcsDeliverableUnit updatedDu = archiveSupport.pollAndQueryArchiveForUpdatedDataItemDu(updatedDs.getId(),
                updatedDs.getName());
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("The DeliverableUnit representing the deposited DataItem (" + updatedDs.getId() + ") was not "
                + "found.", updatedDu);
        
        assertEquals("Expected the title of the du to be " + updatedDs.getName() + " but was " + updatedDu.getTitle(),
                updatedDs.getName(), updatedDu.getTitle());
    }
    
    private void pollAndCheckDepositStatus() {
        // TODO: Change this to some way of checking the deposit status.
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            
        }
    }
    
}
