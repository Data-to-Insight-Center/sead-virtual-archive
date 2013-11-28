/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.ui.stripes;

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.mhf.instance.api.MetadataFormatId;
import org.dataconservancy.mhf.resources.MHFTestResources;
import org.dataconservancy.registry.api.TypedRegistry;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventListener;
import org.dataconservancy.ui.eventing.api.EventManager;
import org.dataconservancy.ui.eventing.events.ExceptionEvent;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.MetadataResult;
import org.dataconservancy.ui.model.MetadataFormatProperties;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.MetadataFormatService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ARCHIVE_PROBLEM;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_EMPTY_OR_INVALID_ID;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED;
import static org.dataconservancy.ui.stripes.MessageKey.MSG_KEY_ERROR_UPDATING_COLLECTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class MetadataFileActionBeanTest extends BaseActionBeanTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    @Autowired
    private RelationshipService relService;
    
    @Autowired
    @Qualifier("formatRegistryImpl")
    private TypedRegistry<DcsMetadataFormat> dcsMetadataFormatTypedRegistry;

    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    EventManager eventManager;
    
    @Autowired
    @Qualifier("eventManagerExecutorService")
    private ExecutorService executorService;

    private MockHttpSession userSession;
    private MockHttpSession adminSession;

    /**
     * Initialize the mock http session with authenticated user credentials.  Tests that re-use this mock session
     * will be already logged in.
     */
    @Before
    public void setUpMockHttpSessions() throws Exception {
        userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) userSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());

       // Mock a session for a system-wide admin user
        adminSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        ctx = (SecurityContext) adminSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
    }
 
    /**
     * Tests adding a new metadata file to a collection. Tests that the file has been added and that existing files remain.
     * @throws Exception
     */
    @Test
    public void testsSaveAndDoneCollectionMetadataFile() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        
        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));

        Collection updatedCollection = retrieveUpdatedCollection();
        Set<String> metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(updatedCollection.getId());
        assertEquals(3, metadataFiles.size());
    
        assertTrue(metadataFiles.contains(metadataFileTwo.getId()));
        assertTrue(metadataFiles.contains(metadataFileOne.getId()));
    }
    
    //The form will redirect back to the add metadata page that page should be submitted with a new file and make sure both are added. 
    @Test
    public void testSaveAndAddMoreCollectionMetadataFile() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        Set<String> metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(2, metadataFiles.size());
        assertTrue(metadataFiles.contains(metadataFileTwo.getId()));
        assertTrue(metadataFiles.contains(metadataFileOne.getId()));

        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndAddMoreMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("displayMetadataFileForm"));
       
        //Test that one file has been added to the collection yet.
        Collection updatedCollection = retrieveUpdatedCollection();
        metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(3, metadataFiles.size());
        
        //Now add the 2nd file this time call save and done
        MetadataFile md2 = new MetadataFile();            
        md2.setName("2nd new file");
        java.io.File tmp2 = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp2.deleteOnExit();
        md2.setSource(tmp2.toURI().toString());
        md2.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md2.getName());
        rt.addParameter("metadataFile.source", md2.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md2.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp2.getPath());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));

        //Test that both files have been added to the collection
        metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(4, metadataFiles.size());

    }
    
    /**
     * Test to ensure that if cancel is pressed no files are added to the collection.
     * @throws Exception
     */
    @Test
    public void testCancel() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        
        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setFormat("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("cancel");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));
        
        Collection updatedCollection = retrieveUpdatedCollection();
        Set<String> files = relService.getMetadataFileIdsForBusinessObjectId(updatedCollection.getId());
        assertEquals(2, files.size());
        
        assertTrue(files.contains(metadataFileTwo.getId())); 
        assertTrue(files.contains(metadataFileOne.getId()));
    }
    
    /**
     * Tests that if the user hits save and add more, then hits cancel their initial save is added to the collection. 
     * @throws Exception
     */
    @Test
    public void testSaveAndAddMoreFollowedByCancelCollection() throws Exception {
       MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        Set<String> metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(2, metadataFiles.size());

        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndAddMoreMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("displayMetadataFileForm"));
       
        //Test that one file have been added to the collection yet.
        //Collection updatedCollection = retrieveUpdatedCollection();
        metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(3, metadataFiles.size());
        
        //Now add the 2nd file this time call cancel
        MetadataFile md2 = new MetadataFile();            
        md2.setName("2nd new file");
        java.io.File tmp2 = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp2.deleteOnExit();
        md2.setSource(tmp2.toURI().toString());
        md2.setFormat("dc:format:metadata/AudubonCore");
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md2.getName());
        rt.addParameter("metadataFile.source", md2.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md2.getFormat());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("cancel");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));
        
        //Test that only the first file has been added to the collection
        metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(3, metadataFiles.size());
        
        assertTrue(metadataFiles.contains(metadataFileTwo.getId()));
        assertTrue(metadataFiles.contains(metadataFileOne.getId()));
    }
    
    /**
     * Tests the deleting a metadata file from a collection. Tests that the file is removed and the other file still remains. 
     * @throws Exception
     */
    @Test
    public void testDeleteCollectionMetadataFile() throws Exception {

        Set<String> files = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(2, files.size());
        assertTrue(files.contains(metadataFileTwo.getId()));
        assertTrue(files.contains(metadataFileOne.getId()));

        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFileID", metadataFileOne.getId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("deleteMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));
        
        files = relService.getMetadataFileIdsForBusinessObjectId(collectionWithData.getId());
        assertEquals(1, files.size());

        assertTrue(files.contains(metadataFileTwo.getId()));
        
        assertFalse(files.contains(metadataFileOne.getId()));
    }

    /**
     * Tests updating a collection metadata file. Tests that the new file is in the collection and the file that was updated is no longer in the collection.
     * @throws Exception
     */
    @Test
    public void testUpdateCollectionMetadataFile() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        md.setSource(metadataFileOne.getSource());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("metadataFileID", metadataFileOne.getId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));
        
        Collection updatedCollection = retrieveUpdatedCollection();
        Set<String> files = relService.getMetadataFileIdsForBusinessObjectId(updatedCollection.getId());
        assertEquals(2, files.size());

        
        assertTrue(files.contains(metadataFileOne.getId()));
        assertTrue(files.contains(metadataFileTwo.getId()));
    }
    
    /**
     * Tests that a user without permissions recieves the proper return code and error message.
     * @throws Exception
     */
    @Test
    public void testCollectionMetadataFileChangesNotAuthorized() throws Exception {        
        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, userSession);


        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());

        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, userSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(403, rt.getResponse().getStatus());
        assertTrue(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED).equalsIgnoreCase(rt.getResponse().getErrorMessage()));
        
        java.io.File tmp2 = java.io.File.createTempFile(this.getClass()
                                                       .getName() + "2-", ".txt");
        tmp2.deleteOnExit();
        //This should be a 403 becuase the user won't be able to deposit a metadata file.
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, userSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("uploadedFile", tmp2.getPath());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());


        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, userSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndAddMoreMetadataFile");
        assertEquals(403, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, userSession);

        rt.addParameter("metadataFileID", metadataFileOne.getId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("deleteMetadataFile");
        assertEquals(403, rt.getResponse().getStatus());
        assertTrue(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ERROR_METADATA_FILE_NOT_AUTHORIZED).equalsIgnoreCase(rt.getResponse().getErrorMessage()));
    }
    
    /**
     * Tests that when a file is added without the name being specified the name of the uploaded file will be supplied. 
     * @throws Exception
     */
    @Test
    public void testSaveAndDoneNoFilenameProvided() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        
        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        //Check that the file name was properly set 
        String metadataFileName = rt.getActionBean(MetadataFileActionBean.class).getMetadataFile().getName();
        assertEquals(tmp.getName(), metadataFileName);
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));   
        
        Collection updatedCollection = retrieveUpdatedCollection();
        Set<String> metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(updatedCollection.getId());
        assertEquals(3, metadataFiles.size());
    
        assertTrue(metadataFiles.contains(metadataFileTwo.getId()));
        assertTrue(metadataFiles.contains(metadataFileOne.getId()));
    }
    
    /**
     * Tests that when a file is added without the name being specified the name of the uploaded file will be supplied. 
     * @throws Exception
     */
    @Test
    public void testSaveAndAddMoreNoFilenameProvidedFollowedByDone() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        
        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

       //Test that no files have been added to the collection yet.
        Collection updatedCollection = retrieveUpdatedCollection();
        Set<String> metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(updatedCollection.getId());
        assertEquals(2, metadataFiles.size());


        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndAddMoreMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("displayMetadataFileForm"));
        
        //Test that one file has been added to the collection yet.
        updatedCollection = retrieveUpdatedCollection();
        metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(updatedCollection.getId());
        assertEquals(3, metadataFiles.size());
        
        //Now add the 2nd file this time call save and done
        MetadataFile md2 = new MetadataFile();            
        md2.setName("2nd new file");
        java.io.File tmp2 = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp2.deleteOnExit();
        md2.setSource(tmp2.toURI().toString());
        md2.setMetadataFormatId("dc:format:metadata/AudubonCore");
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md2.getName());
        rt.addParameter("metadataFile.source", md2.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md2.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp2.getPath());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("parentID", collectionWithData.getId());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(302, rt.getResponse().getStatus());
        assertTrue(rt.getRedirectUrl().contains("viewCollectionDetails"));
        
        
        //Test that both files have been added to the collection now.
        updatedCollection = retrieveUpdatedCollection();
        metadataFiles = relService.getMetadataFileIdsForBusinessObjectId(updatedCollection.getId());
        assertEquals(4, metadataFiles.size());

        boolean fileNameOneFound = false;
        boolean fileNameTwoFound = false;
        
        for (String fileId : metadataFiles) {
            if (!fileId.equalsIgnoreCase(metadataFileOne.getId()) 
                    && !fileId.equalsIgnoreCase(metadataFileTwo.getId())) {
                MetadataFile mdFile = retrieveMetadataFile(fileId);
                if (mdFile.getName().equalsIgnoreCase(tmp.getName())) {
                    fileNameOneFound = true;
                } else if (mdFile.getName().equals(md2.getName())) {
                    fileNameTwoFound = true;
                }
            }
        }
        
        assertTrue(fileNameOneFound);
        assertTrue(fileNameTwoFound);
        assertTrue(metadataFiles.contains(metadataFileTwo.getId()));
        assertTrue(metadataFiles.contains(metadataFileOne.getId()));
    }

    /**
     * Test routing of preview metadata ingest event.  It should go to the metadata_file_ingest_preview.jsp page.
     * @throws Exception
     */
    @Test
    public void testPreviewMetadataIngestEventLoadsPreviewMetadataIngestPage() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        MetadataFile md = new MetadataFile();
        md.setName("new file");

        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        assertTrue(rt.getForwardUrl().contains("metadata_file_ingest_preview.jsp"));
    }
    
    @Test
    public void testPreviewMetadataIngestFormatNotFound() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        MetadataFile md = new MetadataFile();
        md.setName("new file");

        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        assertTrue(rt.getForwardUrl().contains("metadata_file_ingest_preview.jsp"));
        String message = rt.getActionBean(MetadataFileActionBean.class).getMessage();
        assertEquals(String.format("Metadata Validator for format %s not found. Your system administrator has been contacted. Please try again later.", md.getMetadataFormatId()), message);
    }

    @Test
    public void testPreviewMetadataIngestFileAlreadyValidatedMessage() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        rt.addParameter("metadataFile.name", metadataFileOne.getName());
        rt.addParameter("metadataFile.source",metadataFileOne.getSource());
        rt.addParameter("metadataFile.metadataFormatId", metadataFileOne.getFormat());
        rt.addParameter("metadataFileID", metadataFileOne.getId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());

        String message = rt.getActionBean(MetadataFileActionBean.class).getMessage();
        assertEquals(String.format("The file %s has already been validated and stored in the archive.", metadataFileOne.getName()), message);
    }

    @Test
    public void testPreviewMetadataIngestFormatDoesNotValidateMessage() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        String testFormatId = "test.format.id";

        MetadataFile md = new MetadataFile();
        md.setName("new file");

        java.io.File tmp = java.io.File.createTempFile(this.getClass().getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId(testFormatId);
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        MetadataFormatProperties metadataFormatProperties = new MetadataFormatProperties();
        metadataFormatProperties.setFormatId(testFormatId);
        metadataFormatProperties.setValidates(false);

        MetadataFormatService mockFormatService = mock(MetadataFormatService.class);
        when(mockFormatService.getProperties(testFormatId)).thenReturn(metadataFormatProperties);

        // Inject the mockFormatService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("metadataFormatService", mockFormatService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        assertTrue(rt.getForwardUrl().contains("metadata_file_ingest_preview.jsp"));
        String message = rt.getActionBean(MetadataFileActionBean.class).getMessage();
        assertEquals(String.format("No validation is performed for %s.", md.getMetadataFormatId()), message);
    }
    
    @Test
    public void testPreviewMetadataIngestFormatNotFoundFiresEvent() throws Exception {
        final List<Throwable> exceptions = new ArrayList<Throwable>();
        
        eventManager.addListener(new EventListener() {
            @Override
            public void handleEvent(EventContext eventContext, Event<?> event) {
                if (event instanceof ExceptionEvent) {
                    exceptions.add((Throwable) event.getEventObject());
                }
            }
            
            @Override
            public String getName() {
                return "test listener";
            }
        });
        
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        MetadataFile md = new MetadataFile();
        md.setName("new file");

        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        assertTrue(rt.getForwardUrl().contains("metadata_file_ingest_preview.jsp"));
        
        // Wait for events to be executed
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.SECONDS);
        
        assertEquals(1, exceptions.size());
    }
    
    @Test
    public void testPreviewMetadataIngestFormatUpdateNoValidation() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        md.setSource(metadataFileOne.getSource());
        md.setMetadataFormatId("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("metadataFileID", metadataFileOne.getId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        
        assertTrue(rt.getForwardUrl().contains("metadata_file_ingest_preview.jsp"));
        String message = rt.getActionBean(MetadataFileActionBean.class).getMessage();
        assertEquals(String.format("The file %s has already been validated and stored in the archive.", md.getName()), message);
    }
    
    @Test
    public void testPreviewFailedValidation() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        MetadataFile md = new MetadataFile();
        md.setName("new file");

        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setMetadataFormatId(MetadataFormatId.FGDC_XML_FORMAT_ID);
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        assertTrue(rt.getForwardUrl().contains("metadata_file_ingest_preview.jsp"));
        MetadataResult result = rt.getActionBean(MetadataFileActionBean.class).getValidationResult();
        assertNotNull(result);
        assertTrue(result.hasErrors());
        
        //Test that the continue behavior works correctly
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.execute("displayMetadataFileForm");
        assertEquals(200, rt.getResponse().getStatus());
        
        MetadataFile mdFile = rt.getActionBean(MetadataFileActionBean.class).getMetadataFile();
        assertEquals(md.getName(), mdFile.getName());
    }
    
    @Test
    public void testPreviewPassValidation() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);

        MetadataFile md = new MetadataFile();
        md.setName("new file");
        String fgdcXmlMetadataFilePath = MHFTestResources.SAMPLE_VALID_FGDC_XML_RESOURCE_PATH;
        File sampleMetadataFile = new File(this.getClass().getResource(fgdcXmlMetadataFilePath).getPath());

        md.setSource(sampleMetadataFile.getPath());
        md.setMetadataFormatId(MetadataFormatId.FGDC_XML_FORMAT_ID);

        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("uploadedFile", sampleMetadataFile.getPath());
        rt.execute("previewMetadataIngest");
        assertEquals(200, rt.getResponse().getStatus());
        assertTrue(rt.getForwardUrl().contains("metadata_file_ingest_preview.jsp"));
        MetadataResult result = rt.getActionBean(MetadataFileActionBean.class).getValidationResult();
        assertNotNull(result);
        assertFalse(result.hasErrors());
    }
    
    /**
     * Tests that proper error is returned when there is an exception calling poll archive.
     * @throws Exception 
     */
    @Test
    public void testArchiveServiceException() throws Exception {
        String expectedExceptionString = "Archive exception polling archive";

        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setFormat("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());
        
        // Create a mock archive service to inject into the
        // CollectionSplashActionBean for testing exception handling
        ArchiveService mockArchiveService = mock(ArchiveService.class);
        doThrow(new ArchiveServiceException(expectedExceptionString)).when(mockArchiveService).pollArchive();

        // Inject the mockArchiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("archiveService", mockArchiveService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), expectedExceptionString), rt.getResponse().getErrorMessage());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("saveAndAddMoreMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), expectedExceptionString), rt.getResponse().getErrorMessage());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.metadataFormatId", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("deleteMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ARCHIVE_PROBLEM), expectedExceptionString), rt.getResponse().getErrorMessage());
    }
    
    /**
     * Tests that proper error code and message is returned when there is a collection biz service
     * @throws Exception 
     */
    @Test
    public void testCollectionBizInternalException() throws Exception {
        String expectedExceptionString = "BizInternalException Updating Collection";

        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setFormat("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());
        
        ArchiveService mockArchiveService = mock(ArchiveService.class);
        when(mockArchiveService.listDepositInfo(collectionWithData.getId(), ArchiveDepositInfo.Status.DEPOSITED)).thenReturn(archiveService.listDepositInfo(collectionWithData.getId(), ArchiveDepositInfo.Status.DEPOSITED));

        CollectionBizService mockCollectionBizService = mock(CollectionBizService.class);
        when(mockCollectionBizService.getCollection(collectionWithData.getId(), admin)).thenThrow(new BizInternalException(expectedExceptionString));

        // Inject the mockArchiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("archiveService", mockArchiveService);
        springContext.getBeanFactory().registerSingleton("collectionBizService", mockCollectionBizService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.format", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION), expectedExceptionString), rt.getResponse().getErrorMessage());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.format", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.addParameter("uploadedFile", tmp.getPath());
        rt.execute("saveAndAddMoreMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION), expectedExceptionString), rt.getResponse().getErrorMessage());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.format", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("deleteMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_ERROR_UPDATING_COLLECTION), expectedExceptionString), rt.getResponse().getErrorMessage());
    }
    
    /**
     * Tests that proper error code and message is returned when there is a collection biz service
     * @throws Exception 
     */
    @Test
    public void testUnknownIdentifierTypeException() throws Exception {
        String expectedExceptionString = "Unknown ID [" + collectionWithData.getId() + "] " +
                "type [Unknown]";

        MetadataFile md = new MetadataFile();            
        md.setName("new file");
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        md.setSource(tmp.toURI().toString());
        md.setFormat("dc:format:metadata/AudubonCore");
        Resource r = new UrlResource(md.getSource());
        md.setSize(r.contentLength());

        IdService mockIdService = mock(IdService.class);
        when(mockIdService.fromUrl(new URL(collectionWithData.getId()))).thenThrow(new IdentifierNotFoundException(expectedExceptionString));
        when(mockIdService.fromUid(collectionWithData.getId())).thenThrow(new IdentifierNotFoundException(expectedExceptionString));

        // Inject the mockArchiveService into the spring context
        GenericWebApplicationContext springContext = (GenericWebApplicationContext)servletCtx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        springContext.getBeanFactory().registerSingleton("uiIdService", mockIdService);
        servletCtx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, springContext);

        MockRoundtrip rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.format", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("saveAndDoneMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID), expectedExceptionString), rt.getResponse().getErrorMessage());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.format", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("saveAndAddMoreMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID), expectedExceptionString), rt.getResponse().getErrorMessage());
        
        rt = new MockRoundtrip(servletCtx, MetadataFileActionBean.class, adminSession);
        rt.addParameter("metadataFile.name", md.getName());
        rt.addParameter("metadataFile.source", md.getSource());
        rt.addParameter("metadataFile.format", md.getMetadataFormatId());
        rt.addParameter("parentID", collectionWithData.getId());
        rt.addParameter("redirectUrl", "viewCollectionDetails");
        rt.execute("deleteMetadataFile");
        assertEquals(500, rt.getResponse().getStatus());
        assertEquals(String.format(rt.getActionBean(MetadataFileActionBean.class).messageKeys.getProperty(MSG_KEY_EMPTY_OR_INVALID_ID), expectedExceptionString), rt.getResponse().getErrorMessage());
    }
    
    /*This retrieves the collection after it's status and the status of all it's 
     * metadata files have been changed to deposited
     * 
     * @return
     */
    private Collection retrieveUpdatedCollection() throws ArchiveServiceException, InterruptedException {
        Collection updatedCollection = null;
        archiveService.pollArchive();
        List<ArchiveDepositInfo> info = archiveService.listDepositInfo(collectionWithData.getId(), null);
        Status collectionDepositStatus  = info.get(0).getDepositStatus();

        int pollCount=0;
        int maxPollTimes = 60;
        while (collectionDepositStatus != Status.DEPOSITED && collectionDepositStatus != Status.FAILED && pollCount++ < maxPollTimes) {
            Thread.sleep(1000L);
            archiveService.pollArchive();
            info = archiveService.listDepositInfo(collectionWithData.getId(), null);
            collectionDepositStatus = info.get(0).getDepositStatus();
        }
        
        String depositID = info.get(0).getDepositId();
        ArchiveSearchResult<Collection> result = archiveService.retrieveCollection(depositID);
        if (result.getResults().size() > 0) {
            updatedCollection = result.getResults().iterator().next();
        }
        
        return updatedCollection;
    }
    
    /*This retrieves the metadata file from the archive after it has been deposited.
     * @return
     */
    private MetadataFile retrieveMetadataFile(String archiveId) throws ArchiveServiceException, InterruptedException {
        MetadataFile mdFile = null;
        archiveService.pollArchive();
        List<ArchiveDepositInfo> info = archiveService.listDepositInfo(archiveId, null);
        Status collectionDepositStatus  = info.get(0).getDepositStatus();

        int pollCount=0;
        int maxPollTimes = 60;
        while (collectionDepositStatus != Status.DEPOSITED && collectionDepositStatus != Status.FAILED && pollCount++ < maxPollTimes) {
            Thread.sleep(1000L);
            archiveService.pollArchive();
            info = archiveService.listDepositInfo(archiveId, null);
            collectionDepositStatus = info.get(0).getDepositStatus();
        }
        
        String depositID = info.get(0).getDepositId();
        ArchiveSearchResult<MetadataFile> result = archiveService.retrieveMetadataFile(depositID);
        if (result.getResults().size() > 0) {
            mdFile = result.getResults().iterator().next();
        }
        
        return mdFile;
    }
}
