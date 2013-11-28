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

import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.registry.api.RegistryEntry;
import org.dataconservancy.registry.api.support.BasicRegistryEntryImpl;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataScheme;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateIdApiRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.model.ArchiveDepositInfo.Status;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@DirtiesDatabase(AFTER_CLASS)
@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
public class ArchiveServiceIT extends BaseIT {
    
    @Autowired
    public ArchiveService archiveService;
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    @Qualifier("defaultAdmin")
    public Person adminUser;
    
    private HttpPost adminUserLogin;
    
    private HttpGet logout;
    
    // Project to hold the collections
    private static Project project;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean areObjectsSeeded = false;
    
    
    // counter for how many times we have tried to poll the archive
    private int pollCount;
    // maximum number of times to poll
    private final int maxPollTimes = 60;
    
    @Before
    public void setup() throws Exception {
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        adminUserLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        if (!areObjectsSeeded) {
            
            // Login as the administrator and create a project. Assign the
            // basic user as the Pi and logout.
            HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
            
            project = new Project();
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            project.addPi(adminUser.getId());
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            areObjectsSeeded = true;
            
            HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
        }
        
    }
    
    /**
     * Test depositing collection.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositCollection() throws Exception {
        Collection col = new Collection();
        
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("this is a collection");
        col.setDepositDate(DateTime.now());
        String deposit_id = archiveService.deposit(col);
        
        assertNotNull(deposit_id);
        
        assertEquals(Status.PENDING, archiveService.getDepositStatus(deposit_id));

        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(deposit_id));
        
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(deposit_id);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(col);
        
        assertNotNull(test);
        assertEquals(col, test);
        
        assertTrue(archiveService.listCollections(null).contains(deposit_id));
    }
    
    /**
     * Test depositing dataset containing a single file.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositSingleFileDataset() throws Exception {
        // depositing the containing collection
        Collection col = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("TestCollection for dataset deposit");
        col.setDepositDate(DateTime.now());
        
        String collectionDepositId = archiveService.deposit(col);
        assertNotNull(collectionDepositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(collectionDepositId));
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(collectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(collectionDepositId));
        
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(collectionDepositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        
        if (resultIter.hasNext()) {
            resultIter.next();
        }
        assertNotNull(col);
        
        // creating dataset to be deposited
        DataItem dsToDeposit = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        dsToDeposit.setId(dataItemIdRequest.execute(hc));
        dsToDeposit.setName("Test dataset #1");
        dsToDeposit.setDescription("Every dog loves a bone");
        dsToDeposit.setDepositorId(adminUser.getId());
        dsToDeposit.setDepositDate(new DateTime());
        
        // set up datafile to be deposited with dataset
        addFile(dsToDeposit, "a_file");
        
        archiveService.deposit(collectionDepositId, dsToDeposit);
    }
    
    @Test
    public void testDepositMetadataFormatRegistryEntry() throws Exception {
        DcsMetadataFormat format = new DcsMetadataFormat();
        format.setName("test format");
        format.setVersion("1.0");
        format.setId("format:test");
        
        java.io.File tmp = java.io.File.createTempFile("testFile", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println("metadata");
        out.close();
        
        DcsMetadataScheme scheme = new DcsMetadataScheme();
        scheme.setName("test scheme");
        scheme.setSchemaUrl("www.dataconservancy.org");
        scheme.setSource(tmp.toURI().toURL().toExternalForm());
        scheme.setSchemaVersion("1.0");
        format.addScheme(scheme);
        
        String[] keys = {"dataconservancy.org:formats:file:test:2013"};

        RegistryEntry<DcsMetadataFormat> entry = new BasicRegistryEntryImpl<DcsMetadataFormat>("id:registryEntry", format, "DcsMetadataFormat", Arrays.asList(keys), "registry entry for test format");
        
        String deposit_id = archiveService.deposit(entry);

        assertNotNull(deposit_id);

        assertEquals(Status.PENDING, archiveService.getDepositStatus(deposit_id));

        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);

        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(deposit_id));        
    }
    
    private DataFile addFile(DataItem ds, String name) throws Exception {
        DataFile file = new DataFile();
        file.setParentId(ds.getId());

        CreateIdApiRequest fileIdRequest = reqFactory.createIdApiRequest(Types.DATA_FILE);
        file.setId(fileIdRequest.execute(hc));
        file.setName(name);
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass().getName() + "-", ".txt");
        FileUtils.writeStringToFile(tmp, "ArchiveServiceIT temp file.");
        tmp.deleteOnExit();
        file.setSource(tmp.toURI().toString());
        file.setPath(System.getProperty("java.io.tmpdir"));
        
        Resource r = new UrlResource(file.getSource());
        file.setSize(r.contentLength());
        
        ds.addFile(file);
        
        return file;
    }
    
    @Test
    public void testDepositDatasetUpdateReplacingFile() throws Exception {
        // depositing the containing collection
        Collection col = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("TestCollection for dataset deposit");
        col.setDepositDate(DateTime.now());
        
        String collectionDepositId = archiveService.deposit(col);
        assertNotNull(collectionDepositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(collectionDepositId));
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(collectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(collectionDepositId));
        
        DataItem ds1 = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds1.setId(dataItemIdRequest.execute(hc));
        ds1.setParentId(col.getId());
        ds1.setName("Test dataset #1");
        ds1.setDescription("Every dog loves a bone");
        ds1.setDepositorId(adminUser.getId());
        ds1.setDepositDate(new DateTime());
        
        addFile(ds1, "file1");
        
        String ds1_deposit_id = archiveService.deposit(collectionDepositId, ds1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds1_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds1_deposit_id));
        
        ArchiveSearchResult<DataItem> result1 = archiveService.retrieveDataSet(ds1_deposit_id);
        
        assertEquals(1, result1.getResultCount());
        assertEquals(1, result1.getResults().size());
        
        DataItem ds1_test = result1.getResults().iterator().next();
        assertEquals(ds1.getId(), ds1_test.getId());
        assertEquals(ds1.getName(), ds1_test.getName());
        
        // Update ds1
        
        DataItem ds2 = new DataItem();
        ds2.setId(ds1.getId());
        ds2.setName("Moo");
        ds2.setDescription("Cows are the best");
        ds2.setDepositorId(adminUser.getId());
        ds2.setDepositDate(new DateTime());
        
        addFile(ds2, "file2");
        
        String ds2_deposit_id = archiveService.deposit(collectionDepositId, ds2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds2_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds2_deposit_id));
        
        ArchiveSearchResult<DataItem> result2 = archiveService.retrieveDataSet(ds2_deposit_id);
        
        assertEquals(1, result2.getResultCount());
        assertEquals(1, result2.getResults().size());
        
        DataItem ds2_test = result2.getResults().iterator().next();
        
        assertEquals(ds2.getId(), ds2_test.getId());
        assertEquals(ds2.getName(), ds2_test.getName());
        
        assertEquals(1, ds2_test.getFiles().size());
        assertEquals(ds2.getFiles().get(0).getId(), ds2_test.getFiles().get(0).getId());
        assertEquals(ds2.getFiles().get(0).getName(), ds2_test.getFiles().get(0).getName());
    }
    
    @Test
    public void testDepositDatasetUpdateKeepingFile() throws Exception {
        // depositing the containing collection
        Collection col = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("TestCollection for dataset deposit");
        col.setDepositDate(DateTime.now());
        
        String collectionDepositId = archiveService.deposit(col);
        
        assertNotNull(collectionDepositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(collectionDepositId));
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(collectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(collectionDepositId));
        
        DataItem ds1 = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds1.setId(dataItemIdRequest.execute(hc));
        ds1.setParentId(col.getId());
        ds1.setName("Test dataset #1");
        ds1.setDescription("Every dog loves a bone");
        ds1.setDepositorId(adminUser.getId());
        ds1.setDepositDate(new DateTime());
        
        DataFile file1 = addFile(ds1, "file1");
        DataFile filetest = new DataFile();
        filetest.setId(file1.getId());
        
        String ds1_deposit_id = archiveService.deposit(collectionDepositId, ds1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds1_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds1_deposit_id));
        ArchiveSearchResult<DataItem> result1 = archiveService.retrieveDataSet(ds1_deposit_id);
        
        assertEquals(1, result1.getResultCount());
        assertEquals(1, result1.getResults().size());
        
        DataItem ds1_test = result1.getResults().iterator().next();
        
        assertNotNull(ds1_test);
        
        assertEquals(ds1.getId(), ds1_test.getId());
        assertEquals(ds1.getName(), ds1_test.getName());
        assertEquals(1, ds1_test.getFiles().size());
        assertNotNull(ds1_test.getFiles().get(0));
        // assertEquals(ds1.getFiles().get(0).getId(), ds1_test.getFiles().get(0).getId());
        
        // Update ds1
        
        DataItem ds2 = new DataItem();
        ds2.setId(ds1.getId());
        ds2.setParentId(col.getId());
        ds2.setName("Moo");
        ds2.setDescription("Cows are the best");
        ds2.setDepositorId(adminUser.getId());
        ds2.setDepositDate(new DateTime());
        
        final DataFile file2 = new DataFile();
        file2.setId(file1.getId());
        file2.setName(file1.getName());
        file2.setPath(file1.getPath());
        
        ds2.addFile(file2);
        
        String ds2_deposit_id = archiveService.deposit(collectionDepositId, ds2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
            ;
        }
        while (archiveService.getDepositStatus(ds2_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds2_deposit_id));
        
        ArchiveSearchResult<DataItem> result2 = archiveService.retrieveDataSet(ds2_deposit_id);
        
        assertEquals(1, result2.getResultCount());
        assertEquals(1, result2.getResults().size());
        
        final DataItem actualDs2 = result2.getResults().iterator().next();
        
        assertNotNull(actualDs2);
        
        // Modify ds2 to have file1 just like it should
        
        ds2.getFiles().remove(file2);
        ds2.addFile(file1);
        
        assertEquals(file1.getId(), ds2.getFiles().get(0).getId());
        final DataItem expectedDs2 = ds2;
        
        assertEquals(expectedDs2.getId(), actualDs2.getId());
        assertEquals(expectedDs2.getName(), actualDs2.getName());
        
        assertEquals(1, actualDs2.getFiles().size());
        assertEquals(expectedDs2.getFiles().get(0).getId(), actualDs2.getFiles().get(0).getId());
        assertEquals(expectedDs2.getFiles().get(0).getName(), actualDs2.getFiles().get(0).getName());
    }
    
    private MetadataFile addMetadataFile(String id, String name) throws IOException {
        final MetadataFile file = new MetadataFile();
        file.setId(id);
        file.setName(name);
        file.setMetadataFormatId("format:id");
        java.io.File tmp = java.io.File.createTempFile(this.getClass().getName() + "-", ".txt");
        tmp.deleteOnExit();
        file.setSource(tmp.toURI().toString());
        file.setPath(System.getProperty("java.io.tmpdir"));
        
        Resource r = new UrlResource(file.getSource());
        file.setSize(r.contentLength());
        
        return file;
    }
    
    @Test
    public void testDepositMetadataFile() throws Exception {
        Collection col1 = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col1.setId(collectionIdRequest.execute(hc));
        col1.setSummary("test collection version 1");
        col1.setDepositorId(adminUser.getId());
        col1.setDepositDate(new DateTime());
        
        String col_deposit_id = archiveService.deposit(col1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        CreateIdApiRequest metadataFileIdRequest = reqFactory.createIdApiRequest(Types.METADATA_FILE);
        MetadataFile file1 = addMetadataFile(metadataFileIdRequest.execute(hc), "file1");
        
        String mf_deposit_id = archiveService.deposit(col_deposit_id, file1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(mf_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(mf_deposit_id));
        
        ArchiveSearchResult<MetadataFile> results = archiveService.retrieveMetadataFile(mf_deposit_id);
        assertEquals(1, results.getResultCount());
        
        Iterator<MetadataFile> resultIter = results.getResults().iterator();
        MetadataFile test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(test);
        
        assertEquals(file1.getId(), test.getId());
        assertEquals(file1.getName(), test.getName());
        assertEquals(file1.getMetadataFormatId(), test.getMetadataFormatId());
    }
    
    @Test
    public void testDepositMetadataFileUpdateKeepingFile() throws Exception {
        Collection col1 = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col1.setId(collectionIdRequest.execute(hc));
        col1.setSummary("test collection version 1");
        col1.setDepositorId(adminUser.getId());
        col1.setDepositDate(new DateTime());
        
        String col_deposit_id = archiveService.deposit(col1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        CreateIdApiRequest metadataFileIdRequest = reqFactory.createIdApiRequest(Types.METADATA_FILE);
        MetadataFile file1 = addMetadataFile(metadataFileIdRequest.execute(hc), "file1");
        
        String mf_deposit_id = archiveService.deposit(col_deposit_id, file1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(mf_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(mf_deposit_id));
        
        ArchiveSearchResult<MetadataFile> results = archiveService.retrieveMetadataFile(mf_deposit_id);
        assertEquals(1, results.getResultCount());
        
        // Update the metadata file
        MetadataFile file2 = new MetadataFile();
        file2.setId(file1.getId());
        file2.setName("new name");
        file2.setPath(file1.getPath());
        
        String mf2_deposit_id = archiveService.deposit(col_deposit_id, file2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
            
        }
        while (archiveService.getDepositStatus(mf2_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(mf2_deposit_id));
        
        ArchiveSearchResult<MetadataFile> result2 = archiveService.retrieveMetadataFile(mf2_deposit_id);
        
        assertEquals(1, result2.getResultCount());
        assertEquals(1, result2.getResults().size());
        
        MetadataFile mf2_test = result2.getResults().iterator().next();
        
        assertNotNull(mf2_test);
        
        // TODO: The resulting test needs to be fleshed out some to verify the file was kept. -BMB
    }
    
    @Test
    public void testDepositMetadataFileUpdateReplacingFile() throws Exception {
        Collection col1 = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col1.setId(collectionIdRequest.execute(hc));
        col1.setSummary("test collection version 1");
        col1.setDepositorId(adminUser.getId());
        col1.setDepositDate(new DateTime());
        
        String col_deposit_id = archiveService.deposit(col1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        CreateIdApiRequest metadataFileIdRequest = reqFactory.createIdApiRequest(Types.METADATA_FILE);
        MetadataFile file1 = addMetadataFile(metadataFileIdRequest.execute(hc), "file1");
        
        String mf_deposit_id = archiveService.deposit(col_deposit_id, file1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(mf_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(mf_deposit_id));
        
        ArchiveSearchResult<MetadataFile> results = archiveService.retrieveMetadataFile(mf_deposit_id);
        assertEquals(1, results.getResultCount());
        
        // Update the metadata file
        MetadataFile file2 = addMetadataFile(file1.getId(), "file2");
        
        String mf2_deposit_id = archiveService.deposit(col_deposit_id, file2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
            
        }
        while (archiveService.getDepositStatus(mf2_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(mf2_deposit_id));
        
        ArchiveSearchResult<MetadataFile> result2 = archiveService.retrieveMetadataFile(mf2_deposit_id);
        
        assertEquals(1, result2.getResultCount());
        assertEquals(1, result2.getResults().size());
        
        MetadataFile mf2_test = result2.getResults().iterator().next();
        
        assertNotNull(mf2_test);
        
        // TODO: The resulting test needs to be fleshed out some to verify the file was replaced. -BMB
        
    }
    
    /**
     * Test retrieving dataset for a collection. Case: collection contains only one dataset. Expects one correct dataset
     * in return.
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveSingleChild() throws Exception {
        Collection col = new Collection();
        
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("collections are great");
        col.setDepositDate(DateTime.now());
        
        String col_deposit_id = archiveService.deposit(col);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        DataItem ds = new DataItem();
        
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds.setId(dataItemIdRequest.execute(hc));
        ds.setParentId(col.getId());
        ds.setDescription("this is a data set");
        ds.setName("kazaam");
        
        addFile(ds, "data_file");
        
        String ds_deposit_id = archiveService.deposit(col_deposit_id, ds);
        assertNotNull(ds_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id));
        
        ArchiveSearchResult<DataItem> results = archiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());
        // Can't just compare ds to the retrieved ds since the source file might change.
        DataItem dataItem = dataItems.iterator().next();
        assertEquals(ds.getId(), dataItem.getId());
        assertEquals(ds.getName(), dataItem.getName());
        
    }
    
    /**
     * Test retrieving dataset for a collection. Case: collection contains multiple non-versioned (no updated) datasets.
     * Expects all of the dataset in the collection to be returned.
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveMultipleChildren() throws Exception {
        Collection col = new Collection();
        
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("collections are great");
        col.setDepositDate(DateTime.now());
        
        String col_deposit_id = archiveService.deposit(col);
        
        assertNotNull(col_deposit_id);
        
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        DataItem ds = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds.setId(dataItemIdRequest.execute(hc));
        ds.setParentId(col.getId());
        ds.setDescription("this is a data set");
        ds.setName("nameisrequired");
        
        addFile(ds, "data_file1");
        
        String ds_deposit_id = archiveService.deposit(col_deposit_id, ds);
        
        assertNotNull(ds_deposit_id);
        
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id));
        
        DataItem dsTwo = new DataItem();
        dsTwo.setId(dataItemIdRequest.execute(hc));
        dsTwo.setParentId(col.getId());
        dsTwo.setDescription("this is a 2nd data set");
        dsTwo.setName("nameisstillrequired");
        
        addFile(dsTwo, "data_file2");
        
        String ds_two_deposit_id = archiveService.deposit(col_deposit_id, dsTwo);
        
        assertNotNull(ds_two_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_two_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_two_deposit_id));
        
        ArchiveSearchResult<DataItem> results = archiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(2, dataItems.size());
        
        Iterator<DataItem> iter = dataItems.iterator();
        DataItem dataItem = (DataItem) iter.next();
        List<String> dataSetIDs = new ArrayList<String>();
        List<String> dataSetNames = new ArrayList<String>();
        
        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());
        
        DataItem dataSetTwo = (DataItem) iter.next();
        dataSetIDs.add(dataSetTwo.getId());
        dataSetNames.add(dataSetTwo.getName());
        
        assertEquals(2, dataSetIDs.size());
        assertEquals(2, dataSetNames.size());
        
        // Can't guarantee order of return so just make sure we have both data sets
        assertTrue(dataSetIDs.contains(ds.getId()));
        assertTrue(dataSetNames.contains(ds.getName()));
        assertTrue(dataSetIDs.contains(dsTwo.getId()));
        assertTrue(dataSetNames.contains(dsTwo.getName()));
    }
    
    /**
     * Test retrieving multiple children in a collection and page through results.
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveRetrieveMultipleChildrenPaginated() throws Exception {
        Collection col = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("collections are great");
        col.setDepositDate(DateTime.now());
        
        String col_deposit_id = archiveService.deposit(col);
        
        assertNotNull(col_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        DataItem ds = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds.setId(dataItemIdRequest.execute(hc));
        ds.setParentId(col.getId());
        ds.setDescription("this is a data set");
        ds.setName("nameisrequired");
        
        addFile(ds, "data_file3");
        
        String ds_deposit_id = archiveService.deposit(col_deposit_id, ds);
        
        assertNotNull(ds_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id));
        
        DataItem dsTwo = new DataItem();
        
        dsTwo.setId(dataItemIdRequest.execute(hc));
        dsTwo.setParentId(col.getId());
        dsTwo.setDescription("this is a 2nd data set");
        dsTwo.setName("nameisstillrequired");
        
        addFile(dsTwo, "data_file4");
        
        String ds_two_deposit_id = archiveService.deposit(col_deposit_id, dsTwo);
        
        assertNotNull(ds_two_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_two_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_two_deposit_id));
        
        ArchiveSearchResult<DataItem> results = archiveService.retrieveDataSetsForCollection(col_deposit_id, 1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        List<String> dataSetIDs = new ArrayList<String>();
        List<String> dataSetNames = new ArrayList<String>();
        
        assertEquals(1, dataItems.size());
        
        DataItem dataItem = dataItems.iterator().next();
        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());
        
        results = archiveService.retrieveDataSetsForCollection(col_deposit_id, 1, 1);
        dataItems = results.getResults();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        dataItem = dataItems.iterator().next();
        dataSetIDs.add(dataItem.getId());
        dataSetNames.add(dataItem.getName());
        
        assertEquals(2, dataSetIDs.size());
        assertEquals(2, dataSetNames.size());
        
        // Can't guarantee order of return so just make sure we have both data sets
        assertTrue(dataSetIDs.contains(ds.getId()));
        assertTrue(dataSetNames.contains(ds.getName()));
        assertTrue(dataSetIDs.contains(dsTwo.getId()));
        assertTrue(dataSetNames.contains(dsTwo.getName()));
    }
    
    /**
     * Test retrieving dataset for a collection. Case: Multiple (at least 2) collections in the archive, each with at
     * least one dataset. Expects correct returned for each tested collection.
     * 
     * @throws Exception
     */
    @Test
    public void testGetChildrenMultipleCollections() throws Exception {
        Collection col = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("collections are great");
        col.setDepositDate(DateTime.now());
        
        String col_deposit_id = archiveService.deposit(col);
        
        assertNotNull(col_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        Collection colTwo = new Collection();
        colTwo.setId(collectionIdRequest.execute(hc));
        colTwo.setTitle("collections are still great");
        col.setDepositDate(DateTime.now());
        
        String col_deposit_id_two = archiveService.deposit(colTwo);
        
        assertNotNull(col_deposit_id_two);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id_two) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id_two));
        
        DataItem ds = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds.setId(dataItemIdRequest.execute(hc));
        ds.setParentId(col.getId());
        ds.setDescription("this is a data set");
        ds.setName("nameisrequired");
        
        addFile(ds, "data_file5");
        String ds_deposit_id = archiveService.deposit(col_deposit_id, ds);
        
        assertNotNull(ds_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id));
        
        DataItem dsTwo = new DataItem();
        
        dsTwo.setId(dataItemIdRequest.execute(hc));
        dsTwo.setParentId(colTwo.getId());
        dsTwo.setDescription("this is a 2nd data set");
        dsTwo.setName("nameisstillrequired");
        
        addFile(dsTwo, "data_file6");
        
        String ds_two_deposit_id = archiveService.deposit(col_deposit_id_two, dsTwo);
        
        assertNotNull(ds_two_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_two_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_two_deposit_id));
        
        ArchiveSearchResult<DataItem> results = archiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        
        DataItem dataItem = dataItems.iterator().next();
        assertEquals(ds.getId(), dataItem.getId());
        assertEquals(ds.getName(), dataItem.getName());
        assertEquals(ds.getFiles().get(0).getName(), dataItem.getFiles().get(0).getName());
        
        results = archiveService.retrieveDataSetsForCollection(col_deposit_id_two, -1, 0);
        dataItems = results.getResults();
        assertNotNull(dataItems);
        
        assertEquals(1, dataItems.size());
        dataItem = dataItems.iterator().next();
        assertEquals(dsTwo.getId(), dataItem.getId());
        assertEquals(dsTwo.getName(), dataItem.getName());
        assertEquals(dsTwo.getFiles().get(0).getName(), dataItem.getFiles().get(0).getName());
        
    }
    
    @Test
    public void testRetrieveDataSetsForCollectionWithUpdates() throws Exception {
        Collection col = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col.setId(collectionIdRequest.execute(hc));
        col.setTitle("collections say moo");
        col.setDepositDate(DateTime.now());
        
        String col_deposit_id = archiveService.deposit(col);
        
        assertNotNull(col_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col_deposit_id));
        
        DataItem ds1 = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds1.setId(dataItemIdRequest.execute(hc));
        ds1.setParentId(col.getId());
        ds1.setDescription("this is data set one");
        ds1.setName("dataset1");
        
        DataFile data_file_1 = new DataFile();
        data_file_1.setName("gorilla");
        java.io.File tmp1 = java.io.File.createTempFile("grr", null);
        FileUtils.writeStringToFile(tmp1, "A temp test file for ArchiveService IT");
        tmp1.deleteOnExit();
        data_file_1.setSource(tmp1.toURI().toURL().toExternalForm());
        data_file_1.setSize(tmp1.length());
        CreateIdApiRequest fileIdRequest = reqFactory.createIdApiRequest(Types.DATA_FILE);
        data_file_1.setId(fileIdRequest.execute(hc));
        
        ds1.addFile(data_file_1);
        
        String ds_deposit_id_1 = archiveService.deposit(col_deposit_id, ds1);
        
        assertNotNull(ds_deposit_id_1);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id_1) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id_1));
        
        DataItem ds2 = new DataItem();
        ds2.setId(dataItemIdRequest.execute(hc));
        ds2.setParentId(col.getId());
        ds2.setDescription("this is data set two");
        ds2.setName("dataset2");
        
        DataFile data_file_2 = new DataFile();
        data_file_2.setName("gorilla");
        java.io.File tmp2 = java.io.File.createTempFile("grr", null);
        FileUtils.writeStringToFile(tmp2, "A temp test file for ArchiveService IT");
        tmp2.deleteOnExit();
        data_file_2.setSource(tmp2.toURI().toURL().toExternalForm());
        data_file_2.setId(fileIdRequest.execute(hc));
        data_file_2.setSize(tmp2.length());
        
        ds2.addFile(data_file_2);
        
        String ds_deposit_id_2 = archiveService.deposit(col_deposit_id, ds2);
        
        assertNotNull(ds_deposit_id_2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id_2) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id_2));
        // this is an update to ds1
        DataItem ds3 = new DataItem();
        ds3.setId(ds1.getId());
        ds3.setParentId(col.getId());
        ds3.setDescription("this is data set three");
        ds3.setName("dataset3");
        
        DataFile data_file_3 = new DataFile();
        data_file_3.setName("gorilla");
        java.io.File tmp3 = java.io.File.createTempFile("grr", null);
        FileUtils.writeStringToFile(tmp3, "A temp test file for ArchiveService IT");
        tmp3.deleteOnExit();
        data_file_3.setSource(tmp3.toURI().toURL().toExternalForm());
        data_file_3.setId(fileIdRequest.execute(hc));
        data_file_3.setSize(tmp3.length());
        
        ds3.addFile(data_file_3);
        
        String ds_deposit_id_3 = archiveService.deposit(col_deposit_id, ds3);
        
        assertNotNull(ds_deposit_id_3);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id_3) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id_3));
        
        // this is an update to data set 3
        DataItem ds4 = new DataItem();
        ds4.setId(ds3.getId());
        ds4.setParentId(col.getId());
        ds4.setDescription("this is data set four");
        ds4.setName("dataset4");
        
        DataFile data_file_4 = new DataFile();
        data_file_4.setName("gorilla");
        java.io.File tmp4 = java.io.File.createTempFile("grr", null);
        FileUtils.writeStringToFile(tmp4, "A temp test file for ArchiveService IT");
        tmp4.deleteOnExit();
        data_file_4.setSource(tmp4.toURI().toURL().toExternalForm());
        data_file_4.setId(fileIdRequest.execute(hc));
        data_file_4.setSize(tmp4.length());
        ds4.addFile(data_file_4);
        
        String ds_deposit_id_4 = archiveService.deposit(col_deposit_id, ds4);
        
        assertNotNull(ds_deposit_id_4);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds_deposit_id_4) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds_deposit_id_4));
        
        ArchiveSearchResult<DataItem> results = archiveService.retrieveDataSetsForCollection(col_deposit_id, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        
        assertEquals(2, dataItems.size());
        
        List<String> dataSetIDs = new ArrayList<String>();
        List<String> dataSetNames = new ArrayList<String>();
        
        Iterator<DataItem> iter = dataItems.iterator();
        
        while (iter.hasNext()) {
            DataItem dataItem = (DataItem) iter.next();
            
            dataSetIDs.add(dataItem.getId());
            dataSetNames.add(dataItem.getName());
        }
        
        assertTrue(dataSetIDs.contains(ds2.getId()));
        assertTrue(dataSetIDs.contains(ds4.getId()));
        
        assertFalse(dataSetNames.contains(ds1.getName()));
        assertTrue(dataSetNames.contains(ds2.getName()));
        assertFalse(dataSetNames.contains(ds3.getName()));
        assertTrue(dataSetNames.contains(ds4.getName()));
    }
    
    @Test
    public void testRetrieveChildrenOfVersionedCollection() throws Exception {
        // Add a collection
        
        Collection col1 = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        col1.setId(collectionIdRequest.execute(hc));
        col1.setTitle("collections say moo");
        col1.setDepositDate(DateTime.now());
        
        String col1_deposit_id = archiveService.deposit(col1);
        
        assertNotNull(col1_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {

            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col1_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col1_deposit_id));
        
        // Add a DataItem to the collection
        
        DataItem ds1 = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        ds1.setId(dataItemIdRequest.execute(hc));
        ds1.setParentId(col1.getId());
        ds1.setName("Anger");
        ds1.setDescription("Every dog loves a bone");
        ds1.setDepositorId(adminUser.getId());
        ds1.setDescription("angry");
        ds1.setDepositDate(new DateTime());
        addFile(ds1, "moo");
        
        String ds1_deposit_id = archiveService.deposit(col1_deposit_id, ds1);
        
        assertNotNull(ds1_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(ds1_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(ds1_deposit_id));
        
        // Update the collection
        
        Collection col2 = new Collection();
        col2.setId(col1.getId());
        col2.setSummary("collection version 2");
        col2.setDepositorId(adminUser.getId());
        col2.setDepositDate(DateTime.now());
        
        String col2_deposit_id = archiveService.deposit(col2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col2_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col2_deposit_id));
        
        // Make sure ds can be retrieved from each collection version
        
        ArchiveSearchResult<DataItem> search1 = archiveService.retrieveDataSetsForCollection(col1_deposit_id, -1, 0);
        
        assertEquals(1, search1.getResultCount());
        assertEquals(ds1.getId(), search1.getResults().iterator().next().getId());
        
        ArchiveSearchResult<DataItem> search2 = archiveService.retrieveDataSetsForCollection(col2_deposit_id, -1, 0);
        
        assertEquals(1, search2.getResultCount());
        assertEquals(ds1.getId(), search2.getResults().iterator().next().getId());
    }
    
    @Test
    public void testRetrieveCollectionVersions() throws Exception {
        // Add a collection
        
        Collection col1 = new Collection();
        col1.setId("colmoo");
        col1.setTitle("collections say moo");
        col1.setDepositDate(DateTime.now());
        
        String col1_deposit_id = archiveService.deposit(col1);
        
        assertNotNull(col1_deposit_id);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col1_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col1_deposit_id));
        
        // Update the collection
        
        Collection col2 = new Collection();
        col2.setId(col1.getId());
        col2.setTitle("collection version 2");
        col2.setDepositDate(DateTime.now());
        
        String col2_deposit_id = archiveService.deposit(col2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(col2_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(col2_deposit_id));
        
        // Make sure both versions can be retrieved by respective deposit ids.
        
        ArchiveSearchResult<Collection> result1 = archiveService.retrieveCollection(col1_deposit_id);
        ArchiveSearchResult<Collection> result2 = archiveService.retrieveCollection(col2_deposit_id);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.getResultCount());
        assertEquals(1, result2.getResultCount());
        
        Collection test1 = result1.getResults().iterator().next();
        Collection test2 = result2.getResults().iterator().next();
        
        assertNotNull(test1);
        assertNotNull(test2);
        
        assertEquals(col1.getId(), test1.getId());
        assertEquals(col2.getId(), test1.getId());
        
        assertEquals(col1.getTitle(), test1.getTitle());
        assertEquals(col2.getTitle(), test2.getTitle());
    }


    
    @Test
    public void testDepositSubCollectionToParentWithoutMetadataFile() throws IOException, ArchiveServiceException,
            InterruptedException {

        // Creating a collection (to be considered the parent)
        Collection collection = new Collection();
        
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        collection.setId(collectionIdRequest.execute(hc));
        collection.setTitle("this is a collection");
        collection.setDepositDate(DateTime.now());
        String depositId = archiveService.deposit(collection);
        
        assertNotNull(depositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(depositId));
        
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(depositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(depositId));
        
        // Creating a collection (to be considered the child)
        Collection subCollection = new Collection();
        
        collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        subCollection.setId(collectionIdRequest.execute(hc));
        subCollection.setTitle("this is a sub collection");
        subCollection.setDepositDate(DateTime.now());
        subCollection.setParentId(collection.getId());

        String subCollectionDepositId = archiveService.deposit(subCollection);
        
        assertNotNull(subCollectionDepositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(subCollectionDepositId));
        
        // Update children for equality tests
        collection.getChildrenIds().add(subCollection.getId());
        
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(subCollectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(subCollectionDepositId));

        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(depositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(collection);
        
        assertNotNull(test);
        assertEquals(collection, test);
        
        results = archiveService.retrieveCollection(subCollectionDepositId);
        resultIter = results.getResults().iterator();
        test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(subCollection);
        
        assertNotNull(test);
        assertEquals(subCollection, test);
        
        assertTrue(archiveService.listCollections(null).contains(depositId));
        assertTrue(archiveService.listCollections(null).contains(subCollectionDepositId));
    }
    
    /**
     * Make sure that the children of a collection are the latest version.
     * 
     * @throws IOException
     * @throws ArchiveServiceException
     * @throws InterruptedException
     */
    @Test
    public void testUpdateSubCollection() throws IOException, ArchiveServiceException,
            InterruptedException {

        // Creating a collection (to be considered the parent)
        Collection collection = new Collection();
        
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        collection.setId(collectionIdRequest.execute(hc));
        collection.setTitle("this is a collection");
        collection.setDepositDate(DateTime.now());
        String depositId = archiveService.deposit(collection);
        
        assertNotNull(depositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(depositId));
        
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(depositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(depositId));
        
        // Creating a collection (to be considered the child)
        Collection subCollection = new Collection();
        
        collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        subCollection.setId(collectionIdRequest.execute(hc));
        subCollection.setTitle("this is a sub collection");
        subCollection.setDepositDate(DateTime.now());
        subCollection.setParentId(collection.getId());

        String subCollectionDepositId = archiveService.deposit(subCollection);
        
        assertNotNull(subCollectionDepositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(subCollectionDepositId));
        
        // Update children for equality tests
        collection.getChildrenIds().add(subCollection.getId());
        
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(subCollectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(subCollectionDepositId));

        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(depositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(collection);
        
        assertNotNull(test);
        assertEquals(collection, test);
        
        results = archiveService.retrieveCollection(subCollectionDepositId);
        resultIter = results.getResults().iterator();
        test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(subCollection);
        
        assertNotNull(test);
        assertEquals(subCollection, test);
        
        
        // Update the subcollection to not be a subcollection
        
        subCollection.setTitle("this is a new better version");
        subCollection.setDepositorId("Bessie");
        subCollection.setDepositDate(DateTime.now());
        subCollection.setParentId(null);
        collection.getChildrenIds().clear();

        String subCollectionDepositId2 = archiveService.deposit(subCollection);
        
        assertNotNull(subCollectionDepositId2);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(subCollectionDepositId2));
        
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(subCollectionDepositId2) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(subCollectionDepositId2));
        
        results = archiveService.retrieveCollection(subCollectionDepositId2);
        resultIter = results.getResults().iterator();
        test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(subCollection);
        
        assertNotNull(test);
        assertEquals(subCollection, test);
        
        results = archiveService.retrieveCollection(depositId);
        resultIter = results.getResults().iterator();
        test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(collection);
        
        assertNotNull(test);
        assertEquals(collection, test);
    }
    
    @Test
    public void testDepositSubCollectionToParentWithDataItem() throws Exception {
        DataItem dataItem = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        dataItem.setId(dataItemIdRequest.execute(hc));
        dataItem.setName("Test Data Item");
        dataItem.setDescription("This is a test Data Item.");
        dataItem.setDepositorId(adminUser.getId());
        dataItem.setDepositDate(new DateTime());
        addFile(dataItem, "file");
        
        // Creating a collection (to be considered the parent)
        Collection collection = new Collection();
        
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        collection.setId(collectionIdRequest.execute(hc));
        collection.setTitle("this is a collection");
        collection.setDepositDate(DateTime.now());
        String depositId = archiveService.deposit(collection);
        
        assertNotNull(depositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(depositId));
        
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(depositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(depositId));

        // Depositing the dataItem to the parent.
        String di_deposit_id = archiveService.deposit(depositId, dataItem);
        
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(di_deposit_id) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(di_deposit_id));

        // Creating a collection (to be considered the child)
        Collection subCollection = new Collection();
        
        collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        subCollection.setId(collectionIdRequest.execute(hc));
        subCollection.setTitle("this is a sub collection");
        subCollection.setDepositDate(DateTime.now());
        subCollection.setParentId(collection.getId());

        collection.getChildrenIds().add(subCollection.getId());
        
        String subCollectionDepositId = archiveService.deposit(subCollection);
        
        assertNotNull(subCollectionDepositId);
        assertEquals(Status.PENDING, archiveService.getDepositStatus(subCollectionDepositId));

        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(subCollectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(depositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(collection);
        
        assertNotNull(test);
        assertEquals(collection, test);
        
        results = archiveService.retrieveCollection(subCollectionDepositId);
        resultIter = results.getResults().iterator();
        test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(subCollection);
        
        assertNotNull(test);
        assertEquals(subCollection, test);
        
        assertTrue(archiveService.listCollections(null).contains(depositId));
        assertTrue(archiveService.listCollections(null).contains(subCollectionDepositId));
    }
    
    @Test
    public void testDepositNewVersionOfSubCollections() throws Exception {

        // Add a parent collection
        Collection collection = new Collection();
        CreateIdApiRequest collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        collection.setId(collectionIdRequest.execute(hc));
        collection.setTitle("This is a test parent collection.");
        collection.setDepositDate(DateTime.now());
        
        String collectionDepositId = archiveService.deposit(collection);
        assertNotNull(collectionDepositId);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(collectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(collectionDepositId));

        // Creating a collection (to be considered the child)
        Collection subCollection = new Collection();
        
        collectionIdRequest = reqFactory.createIdApiRequest(Types.COLLECTION);
        subCollection.setId(collectionIdRequest.execute(hc));
        subCollection.setTitle("this is a sub collection");
        subCollection.setDepositDate(DateTime.now());
        subCollection.setParentId(collection.getId());
        List<String> childrenIds = new ArrayList<String>();
        childrenIds.add(subCollection.getId());
        collection.setChildrenIds(childrenIds);

        String subCollectionDepositId = archiveService.deposit(subCollection);

        assertNotNull(subCollectionDepositId);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(subCollectionDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(subCollectionDepositId));
        
        // Add a DataItem to the parent collection
        DataItem dataItem = new DataItem();
        CreateIdApiRequest dataItemIdRequest = reqFactory.createIdApiRequest(Types.DATA_SET);
        dataItem.setId(dataItemIdRequest.execute(hc));
        dataItem.setName("Test Data Item");
        dataItem.setDescription("This is a test Data Item.");
        dataItem.setDepositorId(adminUser.getId());
        dataItem.setDepositDate(new DateTime());
        
        addFile(dataItem, "moofile");
        
        String dataItemDepositId = archiveService.deposit(collectionDepositId, dataItem);
        
        assertNotNull(dataItemDepositId);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(dataItemDepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(dataItemDepositId));
        
        // Update the collection
        
        Collection collectionV2 = new Collection();
        collectionV2.setId(collection.getId());
        collectionV2.setSummary("test parent collection version 2");
        collectionV2.setDepositorId(adminUser.getId());
        collectionV2.setDepositDate(DateTime.now());
        collectionV2.getChildrenIds().add(subCollection.getId());
        
        String collectionV2DepositId = archiveService.deposit(collectionV2);
        
        pollCount = 0;
        // Wait long enough for deposit to complete
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.getDepositStatus(collectionV2DepositId) == Status.PENDING && pollCount++ < maxPollTimes);
        
        assertEquals(Status.DEPOSITED, archiveService.getDepositStatus(collectionV2DepositId));
        
        // Make sure ds can be retrieved from each collection version
        
        ArchiveSearchResult<DataItem> search1 = archiveService
                .retrieveDataSetsForCollection(collectionDepositId, -1, 0);
        
        assertEquals(1, search1.getResultCount());
        assertEquals(dataItem.getId(), search1.getResults().iterator().next().getId());
        
        ArchiveSearchResult<DataItem> search2 = archiveService.retrieveDataSetsForCollection(collectionV2DepositId, -1,
                0);
        
        assertEquals(1, search2.getResultCount());
        assertEquals(dataItem.getId(), search2.getResults().iterator().next().getId());
        
        // Make sure parent collection versions and child can be retrieved
        
        ArchiveSearchResult<Collection> results = archiveService.retrieveCollection(collectionDepositId);
        Iterator<Collection> resultIter = results.getResults().iterator();
        Collection test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(collection);
        
        assertNotNull(test);
        assertEquals(collection, test);
        
        results = archiveService.retrieveCollection(collectionV2DepositId);
        resultIter = results.getResults().iterator();
        test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(collection);
        
        assertNotNull(test);
        assertEquals(collectionV2, test);
        
        results = archiveService.retrieveCollection(subCollectionDepositId);
        resultIter = results.getResults().iterator();
        test = null;
        if (resultIter.hasNext()) {
            test = resultIter.next();
        }
        assertNotNull(subCollection);
        
        assertNotNull(test);
        assertEquals(subCollection, test);
    }
    
}
