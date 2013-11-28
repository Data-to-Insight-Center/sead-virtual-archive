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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.resources.MHFResources;
import org.dataconservancy.ui.dao.DisciplineDAO;
import org.dataconservancy.ui.it.support.AddMetadataFormatRequest;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.it.support.ListMetadataFormatRequest;
import org.dataconservancy.ui.it.support.MetadataFileRequest;
import org.dataconservancy.ui.it.support.MetadataPreviewScreenResponseParser;
import org.dataconservancy.ui.it.support.SaveMetadataFormatRequest;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.stripes.LoginActionBean;
import org.dataconservancy.ui.stripes.UiConfigurationActionBean;
import org.dataconservancy.ui.stripes.UserCollectionsActionBean;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MetadataFileIT extends BaseIT {
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    // Someone to create the project
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    // Someone who is an admin on the project
    @Autowired
    @Qualifier("defaultUser")
    private Person depositor;
    
    // Person with no affiliation to the project
    @Autowired
    @Qualifier("approvedRegisteredUser")
    private Person unAuthorizedUser;

    @Autowired
    private DisciplineDAO disciplineDao;
    
    private HttpPost depositorLogin;
    
    private HttpPost unAuthorizedUserLogin;
    
    private HttpPost adminUserLogin;
    
    private HttpGet logout;
    
    // Collection that we'll attach, edit and remove metadata files from
    private static Collection collection;
    
    // Project to hold the collection
    private static Project project;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    // Sample data file to use in the upload
    private static File sampleMetadataFile;
    
    private static final String MAVEN_MODEL_4_0_0_SCHEMA_URL = "http://maven.apache.org/xsd/maven-4.0.0.xsd";
    
    /**
     * Sets up the httpConnection, project, collection, and dataset
     */
    @Before
    public void setUp() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        
        depositorLogin = reqFactory.createLoginRequest(depositor).asHttpPost();
        unAuthorizedUserLogin = reqFactory.createLoginRequest(unAuthorizedUser).asHttpPost();
        adminUserLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
            
            project = new Project();
            
            project.setName("Collection project");
            project.setDescription("A simple project to hold collection");
            project.setStartDate(DateTime.now());
            project.setEndDate(DateTime.now());
            project.setFundingEntity("$$");
            project.addNumber("1");
            project.addPi(depositor.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            collection = new Collection();
            collection.setTitle("A Collection");
            collection.setId(reqFactory.createIdApiRequest(Types.COLLECTION).execute(hc));
            collection.setSummary("A collection to manipulate metadata files");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
            sampleMetadataFile = createSampleDataFile("Metadata", ".txt");
            sampleMetadataFile.deleteOnExit();
            areObjectsSeeded = true;
        }
    }
    
    /**
     * Tests that a save and done operation works correctly on a collection. That is a metadata file is saved and the
     * user is returned to the collection details view.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testSaveAndDoneCollectionMetadataFile() throws ClientProtocolException, IOException {
        
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testSaveAndDoneCollectionMetadataFile");
        metadataFile.setFormat("xml");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.ResponseHolder holder = new HttpAssert.ResponseHolder();
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.", holder);
        
        // Insure that the validation event occurred, and was successful
        MetadataPreviewScreenResponseParser parser = new MetadataPreviewScreenResponseParser(holder.getBody());
        assertEquals(String.format("Metadata Validator for format %s not found. Your system administrator has been contacted. Please try again later.", metadataFile.getMetadataFormatId()), parser.getMessage());
          
        MetadataFileRequest saveAndDoneRequest = reqFactory.createMetadataFileRequest();
        saveAndDoneRequest.setIsCollection(true);
        saveAndDoneRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        saveAndDoneRequest.setParentId(collection.getId());
        saveAndDoneRequest.setFileToDeposit(sampleMetadataFile);

        HttpResponse authorizedResponse = hc.execute(saveAndDoneRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that a save and done operation works correctly on a collection, when file name is not provided by the user. 
     * This should generate the file name based on the uploaded file.
     * That is a metadata file is saved and the user is returned to the collection details view.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testSaveAndDoneCollectionMetadataFileNoFileNameSpecified() throws ClientProtocolException, IOException {
        
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFormatId(MetadataFormatId.FGDC_XML_FORMAT_ID);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest saveAndDoneRequest = reqFactory.createMetadataFileRequest();
        saveAndDoneRequest.setIsCollection(true);
        saveAndDoneRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        saveAndDoneRequest.setParentId(collection.getId());
        saveAndDoneRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(saveAndDoneRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that a save and add more operation works correctly on a collection. That is multiple metadata files are
     * saved and the user is returned to the collection details view.
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    @Test
    public void testSaveAndAddMoreCollectionMetadataFile() throws IllegalStateException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testSaveAndAddMoreCollectionMetadataFile");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataSaveRequest = reqFactory.createMetadataFileRequest();
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_MORE_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setMetadataFile(metadataFile);
        metadataSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        // Metadata File to add
        final MetadataFile metadataFileTwo = new MetadataFile();
        metadataFileTwo.setName("MetadataTwo-testSaveAndAddMoreCollectionMetadataFile");
        metadataFileTwo.setMetadataFormatId("dc:format:metadata/DarwinCore");
        
        metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFileTwo);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setMetadataFile(metadataFileTwo);
        metadataSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that a save and add more operation works correctly on a collection, when no file name is specified. 
     * This should generate the file name based on the uploaded file.
     * That is multiple metadata files are saved and the user is returned to the collection details view.
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    @Test
    public void testSaveAndAddMoreCollectionMetadataFileNoFileName() throws IllegalStateException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFormatId(MetadataFormatId.FGDC_XML_FORMAT_ID);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataSaveRequest = reqFactory.createMetadataFileRequest();
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_MORE_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        File secondMetadataFile = createSampleDataFile("MetadataTwo", ".txt");
        secondMetadataFile.deleteOnExit();
        
        metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFormatId(MetadataFormatId.FGDC_XML_FORMAT_ID);
        metadataFileRequest.setFileToDeposit(secondMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setFileToDeposit(secondMetadataFile);
        
        authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that if cancel is pressed before the user saves any metadata files nothing is changed about the collection.
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    @Test
    public void testCancelBeforeSaveCollection() throws IllegalStateException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testCancelBeforeSaveCollection");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataCancelRequest = reqFactory.createMetadataFileRequest();
        metadataCancelRequest.setIsCollection(true);
        metadataCancelRequest.setEvent(MetadataFileRequest.STRIPES_CANCEL_EVENT);
        metadataCancelRequest.setParentId(collection.getId());
        metadataCancelRequest.setMetadataFile(metadataFile);
        metadataCancelRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataCancelRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
;
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that if the user hits cancel during a save and add more any files they saved previously will be added to
     * the collection.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Test
    public void testCancelAfterSaveAndAddMoreCollection() throws ClientProtocolException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testCancelAfterSaveAndAddMoreCollection1");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataSaveRequest = reqFactory.createMetadataFileRequest();
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_MORE_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setMetadataFile(metadataFile);
        metadataSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        metadataFile.setName("Metadata-testCancelAfterSaveAndAddMoreCollection2");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        metadataSaveRequest = reqFactory.createMetadataFileRequest();
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_CANCEL_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setMetadataFile(metadataFile);
        metadataSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that a file can be deleted from a collection successfully.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Ignore
    @Test
    public void testDeleteMetadataFileFromCollection() throws ClientProtocolException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testDeleteMetadataFileFromCollection");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataFileSaveRequest = reqFactory.createMetadataFileRequest();
        metadataFileSaveRequest.setIsCollection(true);
        metadataFileSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        metadataFileSaveRequest.setParentId(collection.getId());
        metadataFileSaveRequest.setMetadataFile(metadataFile);
        metadataFileSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataFileSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        metadataFileSaveRequest.setIsCollection(true);
        metadataFileSaveRequest.setEvent(MetadataFileRequest.STRIPES_DELETE_EVENT);
        metadataFileSaveRequest.setParentId(collection.getId());
        
        authorizedResponse = hc.execute(metadataFileSaveRequest.asHttpPost());
        assertEquals("Unable to delete metadata file.", 302, authorizedResponse.getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");

    }
    
    /**
     * Tests that the information about a metadata file (name and format) can be changed.
     * 
     * @throws IOException
     * @throws IllegalStateException
     */
    @Test
    public void testEditMetadataFileInfoCollection() throws IllegalStateException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testEditMetadataFileInfoCollection");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataSaveRequest = reqFactory.createMetadataFileRequest();
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setMetadataFile(metadataFile);
        metadataSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        final MetadataFile updatedMetadataFile = new MetadataFile();
        updatedMetadataFile.setName("Metadata Updated-testEditMetadataFileInfoCollection");
        updatedMetadataFile.setMetadataFormatId("dc:format:metadata/DarwinCore");
        
        metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(updatedMetadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        metadataSaveRequest.setIsCollection(true);
        metadataSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        metadataSaveRequest.setParentId(collection.getId());
        metadataSaveRequest.setMetadataFile(updatedMetadataFile);
        
        authorizedResponse = hc.execute(metadataSaveRequest.asHttpPost());
        assertEquals("Unable to update metadata file.", 302, authorizedResponse.getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        
        // TODO: To test this somehow we will need to call the metadata file biz service, or a metadata file API needs
        // to be created

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that the content of the metadata file can be changed.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    @Ignore
    @Test
    public void testEditMetadataFileContentsCollection() throws ClientProtocolException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testEditMetadataFileContentsCollection");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataFileSaveRequest = reqFactory.createMetadataFileRequest();
        metadataFileSaveRequest.setIsCollection(true);
        metadataFileSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        metadataFileSaveRequest.setParentId(collection.getId());
        metadataFileSaveRequest.setMetadataFile(metadataFile);
        metadataFileSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataFileSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        metadataFileSaveRequest.setIsCollection(true);
        metadataFileSaveRequest.setEvent(MetadataFileRequest.STRIPES_DELETE_EVENT);
        metadataFileSaveRequest.setParentId(collection.getId());
        
        authorizedResponse = hc.execute(metadataFileSaveRequest.asHttpPost());
        assertEquals("Unable to delete metadata file.", 302, authorizedResponse.getStatusLine().getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Tests that a depositor on the collection has the ability to add metadata files for the collection.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testDepositorCanEditCollectionMetadata() throws ClientProtocolException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, depositorLogin, 300, 399, "Unable to login as admin user!");
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testDepositorCanEditCollectionMetadata");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest metadataFileSaveRequest = reqFactory.createMetadataFileRequest();
        metadataFileSaveRequest.setIsCollection(true);
        metadataFileSaveRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        metadataFileSaveRequest.setParentId(collection.getId());
        metadataFileSaveRequest.setMetadataFile(metadataFile);
        metadataFileSaveRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(metadataFileSaveRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                        .replace("{$event}", "viewCollectionDetails")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                UserCollectionsActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "viewCollectionDetails")));
        assertTrue(authorizedResponse.getFirstHeader("Location").getValue().contains("viewCollectionDetails"));
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout depositor.");
    }
    
    /**
     * Tests that if the user is not a depositor on the collection they are not able to edit metadata files. Expected
     * return 403.
     * 
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testUnAuthorizedUserRejectedFromEditingMetadata() throws ClientProtocolException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, unAuthorizedUserLogin, 300, 399, "Unable to login as admin user!");
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testUnAuthorizedUserRejectedFromEditingMetadata");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest saveMetadataFileRequest = reqFactory.createMetadataFileRequest();
        saveMetadataFileRequest.setIsCollection(true);
        saveMetadataFileRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        saveMetadataFileRequest.setParentId(collection.getId());
        saveMetadataFileRequest.setMetadataFile(metadataFile);
        saveMetadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpResponse authorizedResponse = hc.execute(saveMetadataFileRequest.asHttpPost());
        assertEquals("Unable to add metadata file with save and done.", 403, authorizedResponse.getStatusLine()
                .getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout unauthorized user.");
    }
    
    /**
     * Tests that if no one is logged in they are not allowed to update a metadata file. Expected to return a 302
     * redirect to the login
     * 
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testNotLoggedInNotAllowedToUpdateMetadataFile() throws ClientProtocolException, IOException {
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout.");
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testNotLoggedInNotAllowedToUpdateMetadataFile");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);

        HttpResponse authorizedResponse = hc.execute(metadataFileRequest.asHttpPost());
        assertEquals("Unable to add metadata file with preview.", 302, authorizedResponse.getStatusLine()
                .getStatusCode());
        assertTrue(
                LoginActionBean.class.getAnnotation(UrlBinding.class).value().replace("{$event}", "renderLoginForm")
                        + " not in " + authorizedResponse.getFirstHeader("Location").getValue(),
                authorizedResponse
                        .getFirstHeader("Location")
                        .getValue()
                        .contains(
                                LoginActionBean.class.getAnnotation(UrlBinding.class).value()
                                        .replace("{$event}", "renderLoginForm")));
        
        authorizedResponse.getEntity().getContent().close();
        HttpAssert.free(authorizedResponse);

    }

    /**
     * Here we upload a <em>valid</em> metadata file to a Collection.  The metadata format of the metadata file requires
     * {@link org.dataconservancy.ui.model.MetadataFormatProperties#isValidates() validation}, so we insure that we
     * see a validation event.  Then we insure that the metadata file was properly uploaded.
     *
     * @throws Exception
     */
    @Test
    public void testUploadValidMetadataFile() throws Exception {
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        // We will work with FGDC 1998 as the Metadata Format
        final String formatId = MetadataFormatId.FGDC_XML_FORMAT_ID;
        final String formatUrl = "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd";

        
        // This is our valid FGDC 1988 Metadata File
        final File file = new File(this.getClass().getResource(
                MHFResources.SAMPLE_VALID_FGDC_XML_RESOURCE_PATH).getPath());

        // Insure that the system has this Metadata Format registered (it should be, as it is bootstrapped)
        UiConfigurationActionBean.MetaDataFormatTransport format = null;
        final ListMetadataFormatRequest listMdfReq = new ListMetadataFormatRequest(urlConfig, disciplineDao);
        final List<UiConfigurationActionBean.MetaDataFormatTransport> existingFormats = listMdfReq.listFormats(hc);
        for (UiConfigurationActionBean.MetaDataFormatTransport mdt : existingFormats) {
            if (formatUrl.equals(mdt.getSchemaURL())) {
                format = mdt;
                break;
            }
        }
        assertNotNull("Expected to find the format " + formatId + " already in the system.", format);

        // Insure that the Metadata Format applies at the Collection level, and that it is validating.
        assertEquals(UiConfigurationActionBean.YES, format.getAppliesToCollection());
        assertEquals(UiConfigurationActionBean.YES, format.getValidates());

        // Create the MetadataFile.
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("MetadataFileIT-" + file.getName());
        metadataFile.setMetadataFormatId(formatId);
        metadataFile.setFormat("application/xml");
        metadataFile.setSize(file.length());

        // Create the HTTP request for generating the preview
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(file);

        // Upload the MetadataFile, assert it happened successfully; the holder contains the metadata preview HTML
        HttpAssert.ResponseHolder holder = new HttpAssert.ResponseHolder();
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file!", holder);

        // Insure that the validation event occurred, and was successful
        MetadataPreviewScreenResponseParser parser = new MetadataPreviewScreenResponseParser(holder.getBody());
        assertTrue(parser.getSuccessEvents().size() > 0);
        assertTrue(parser.getFailureEvents().size() == 0);
        assertEquals(metadataFile.getName(), parser.getFilename());
        
        assertTrue(parser.getMessage().isEmpty());
        assertTrue(parser.getGeoSpatialAttributes().size() == 1);
        assertTrue(parser.getTemporalAttributes().size() == 1);

        // Save the uploaded, validated, file to the the Collection
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 302);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    /**
     * Here we upload a <em>invalid</em> metadata file to a Collection.  The metadata format of the metadata file requires
     * {@link org.dataconservancy.ui.model.MetadataFormatProperties#isValidates() validation}, so we insure that we
     * see a validation event.  Then we insure that the metadata file was properly uploaded.
     *
     * @throws Exception
     */
    @Test
    public void testUploadInValidMetadataFile() throws Exception {
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");

        // We will work with FGDC 1998 as the Metadata Format
        final String formatId = MetadataFormatId.FGDC_XML_FORMAT_ID;
        final String formatUrl = "http://www.fgdc.gov/schemas/metadata/fgdc-std-001-1998.xsd";

        
        // This is our valid FGDC 1988 Metadata File
        final File file = new File(this.getClass().getResource(
                MHFResources.SAMPLE_INVALID_FGDC_XML_RESOURCE_PATH).getPath());

        // Insure that the system has this Metadata Format registered (it should be, as it is bootstrapped)
        UiConfigurationActionBean.MetaDataFormatTransport format = null;
        final ListMetadataFormatRequest listMdfReq = new ListMetadataFormatRequest(urlConfig, disciplineDao);
        final List<UiConfigurationActionBean.MetaDataFormatTransport> existingFormats = listMdfReq.listFormats(hc);
        for (UiConfigurationActionBean.MetaDataFormatTransport mdt : existingFormats) {
            if (formatUrl.equals(mdt.getSchemaURL())) {
                format = mdt;
                break;
            }
        }
        assertNotNull("Expected to find the format " + formatId + " already in the system.", format);

        // Insure that the Metadata Format applies at the Collection level, and that it is validating.
        assertEquals(UiConfigurationActionBean.YES, format.getAppliesToCollection());
        assertEquals(UiConfigurationActionBean.YES, format.getValidates());

        // Create the MetadataFile.
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("MetadataFileIT-" + file.getName());
        metadataFile.setMetadataFormatId(formatId);
        metadataFile.setFormat("application/xml");
        metadataFile.setSize(file.length());

        // Create the HTTP request for generating the preview
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(file);

        // Upload the MetadataFile, assert it happened successfully; the holder contains the metadata preview HTML
        HttpAssert.ResponseHolder holder = new HttpAssert.ResponseHolder();
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file!", holder);

        // Insure that the validation event occurred, and failed
        MetadataPreviewScreenResponseParser parser = new MetadataPreviewScreenResponseParser(holder.getBody());
        assertTrue(parser.getSuccessEvents().size() == 0);
        assertTrue(parser.getFailureEvents().size() > 0);
        assertEquals(metadataFile.getName(), parser.getFilename());
        
        assertTrue(parser.getMessage().isEmpty());
        
        //Will contain the empty div
        assertTrue(parser.getGeoSpatialAttributes().size() == 1);
        assertTrue(parser.getGeoSpatialAttributes().get(0).isEmpty());
        
        assertTrue(parser.getTemporalAttributes().size() == 1);
        assertTrue(parser.getTemporalAttributes().get(0).isEmpty());

        // Save the uploaded, validated, file to the the Collection
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_SAVE_DONE_EVENT);
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 302);

        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }

    @Test
    public void testDisplayDepositErrors() throws ClientProtocolException, IOException {
        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        // Metadata File to add to get to the preview page.
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testSaveAndDoneCollectionMetadataFile");
        metadataFile.setFormat("xml");
        metadataFile.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.");
        
        MetadataFileRequest saveAndDoneRequest = reqFactory.createMetadataFileRequest();
        saveAndDoneRequest.setIsCollection(true);
        saveAndDoneRequest.setParentId(collection.getId());
        saveAndDoneRequest.setFileToDeposit(sampleMetadataFile);
        
        // Pretend that the save has failed and go to the display errors page.
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_DISPLAY_DEPOSIT_ERRORS_EVENT);
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200);
        
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
    }
    
    @Test
    public void testFormatNoValidationDisplaysMessage() throws Exception{

        // Login as the administrator user (the project creator), to add basic user as pi.
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        addNewFormat();
        
        // Metadata File to add
        final MetadataFile metadataFile = new MetadataFile();
        metadataFile.setName("Metadata-testSaveAndDoneCollectionMetadataFile");
        metadataFile.setFormat("xml");
        metadataFile.setMetadataFormatId("dataconservancy.org:registry:metadata-format:entry:id:Maven:4.0.0");
        
        MetadataFileRequest metadataFileRequest = reqFactory.createMetadataFileRequest();
        metadataFileRequest.setIsCollection(true);
        metadataFileRequest.setEvent(MetadataFileRequest.STRIPES_PREVIEW_EVENT);
        metadataFileRequest.setParentId(collection.getId());
        metadataFileRequest.setMetadataFile(metadataFile);
        metadataFileRequest.setFileToDeposit(sampleMetadataFile);
        
        HttpAssert.ResponseHolder holder = new HttpAssert.ResponseHolder();
        HttpAssert.assertStatus(hc, metadataFileRequest.asHttpPost(), 200, "Unable to add metadata file with preview.", holder);
        
        // Insure that the validation event occurred, and was successful
        MetadataPreviewScreenResponseParser parser = new MetadataPreviewScreenResponseParser(holder.getBody());
        assertEquals(String.format("No validation is performed for %s.", "Maven"), parser.getMessage());
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin.");
          
    }
    
    private void addNewFormat() {
        // Get a count of the current number of metadata formats in the system
        List<UiConfigurationActionBean.MetaDataFormatTransport> mdfts = getMdfs();
        int mdfCount = mdfts.size();

        // Compose the mdft to add
        final UiConfigurationActionBean.MetaDataFormatTransport mdft = new UiConfigurationActionBean().getNewMetadataFormatTransport();
        final AddMetadataFormatRequest req = new AddMetadataFormatRequest(urlConfig);
        // A unique name insures that this Metadata Format doesn't exist yet in the system (but we verify this
        // assumption anyway)
        final String name = "Maven";
        final boolean validates = false;
        final String version = "4.0.0";
        final boolean appliesToCollection = false;
        final boolean appliesToProject = false;
        final boolean appliesToItem = true;
        final List<String> disciplineIds = Arrays.asList("dc:discipline:Biology");
        mdft.setName(name);
        mdft.setVersion(version);
        mdft.setSchemaURL(MAVEN_MODEL_4_0_0_SCHEMA_URL);
        mdft.setSchemaSource(MAVEN_MODEL_4_0_0_SCHEMA_URL);
        mdft.setValidates(validates);
        mdft.setAppliesToCollection(appliesToCollection);
        mdft.setAppliesToProject(appliesToProject);
        mdft.setAppliesToItem(appliesToItem);
        mdft.setDisciplineIds(disciplineIds);

        // Insure that the new Metadata Format being added isn't in the list of existing metadata formats
        assertFalse(mdfts.contains(mdft));

        HttpAssert.assertStatus(hc, req.asHttpPost(mdft), 200);  
        
        // Now we need to persist the format in the system by emulating a click on the "save" button
        HttpAssert.assertStatus(hc, new SaveMetadataFormatRequest(urlConfig).asHttpPost(), 200);

        // insure that the format we've added was added properly (all the values for table columns were
        // persisted properly)
        mdfts = getMdfs();
        assertTrue(mdfts.contains(mdft));
        assertEquals(mdfCount + 1, mdfts.size());
    }
    
    private List<UiConfigurationActionBean.MetaDataFormatTransport> getMdfs() {
        return new ListMetadataFormatRequest(urlConfig, disciplineDao).listFormats(hc);
    }
}
