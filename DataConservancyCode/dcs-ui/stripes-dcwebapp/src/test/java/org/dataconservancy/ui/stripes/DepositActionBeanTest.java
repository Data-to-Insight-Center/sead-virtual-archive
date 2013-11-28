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
package org.dataconservancy.ui.stripes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.ui.dao.PackageDAO;
import org.dataconservancy.ui.dao.ProjectDAO;
import org.dataconservancy.ui.eventing.api.Event;
import org.dataconservancy.ui.eventing.api.EventContext;
import org.dataconservancy.ui.eventing.api.EventListener;
import org.dataconservancy.ui.eventing.api.EventManager;
import org.dataconservancy.ui.eventing.events.DataItemDepositEvent;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Package;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Relationship;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.dataconservancy.ui.util.ZipPackageExtractor;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Created by IntelliJ IDEA. User: HanhVu Date: 12/14/11 Time: 10:37 AM To change this template use File | Settings |
 * File Templates.
 */
@DirtiesDatabase(DirtiesDatabase.AFTER_EACH_TEST_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DepositActionBeanTest extends BaseActionBeanTest {
    
    @Autowired
    private RelationshipService relService;
    
    @Autowired
    private ProjectDAO projectDao;
    
    @Autowired
    private PackageDAO packageDao;
    
    @Autowired
    private ArchiveService archiveService;
    
    @Autowired
    private JdbcTemplate template;
    
    @Autowired
    @Qualifier("uiIdService")
    private IdService idService;
    
    @Autowired
    EventManager eventManager;
    
    @Autowired
    @Qualifier("eventManagerExecutorService")
    private ExecutorService executorService;
    
    private MockHttpSession userSession;
    
    private MockHttpSession adminSession;
    
    private List<String> datasetDepositIds;
    private DataItem ds;
    private static final String data_string = "REAL data, real real real. doo dah...";
    
    private final static String FILES_ONLY_ZIP = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    private final static String DIRECTORY_ZIP = "/SampleFilePackages/Windows/WindowsZipFileWDirectory.zip";
    private final static String NESTED_ZIP = "/SampleFilePackages/Windows/NestedZip.zip";
    private final static String ZIP_DIRECTORY = "./test/";
    static final String PROJECT_TABLE = "project";
    
    private String updateName = "";
    
    static final String PACKAGE_TABLE = "PACKAGE_FILE_DATA";
    
    static final String PROJ_TABLE_ROW_COUNT_QUERY = "SELECT count(*) FROM " + PROJECT_TABLE;
    
    static final String PROJ_TABLE_DELETE_ALL_ROWS_QUERY = "DELETE FROM " + PROJECT_TABLE;
    
    static final String PACKAGE_TABLE_DELETE_ALL_ROWS_QUERY = "DELETE FROM " + PACKAGE_TABLE;
    
    static final String PACKAGE_TABLE_ROW_COUNT_QUERY = "SELECT count(*) FROM " + PACKAGE_TABLE;
    
    private DepositActionBean depositActionBean;
    
    @Before
    public void setUpDB() {
        template.execute(PROJ_TABLE_DELETE_ALL_ROWS_QUERY);
        assertEquals(0, template.queryForInt(PROJ_TABLE_ROW_COUNT_QUERY));
        template.execute(PACKAGE_TABLE_DELETE_ALL_ROWS_QUERY);
        assertEquals(0, template.queryForInt(PACKAGE_TABLE_ROW_COUNT_QUERY));
    }
    
    @Before
    public void setUpMockttpSessions() throws Exception {
        
        // Mock a session for a registered, authorized user.
        userSession = new MockHttpSession(servletCtx);
        MockRoundtrip rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", userSession);
        rt.setParameter("j_username", user.getEmailAddress());
        rt.setParameter("j_password", user.getPassword());
        rt.execute();
        SecurityContext ctx = (SecurityContext) userSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(user.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        // Mock a session for a system-wide admin user
        adminSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", adminSession);
        rt.setParameter("j_username", admin.getEmailAddress());
        rt.setParameter("j_password", admin.getPassword());
        rt.execute();
        ctx = (SecurityContext) adminSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(admin.getEmailAddress(), ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        
        // Fix up the collaborating Project
        Project project = new Project();
        project.setId("project:/1");
        project.setStartDate(DateTime.now());
        project.setEndDate(DateTime.now());
        projectDao.insertProject(project);
        
        adminSession.setAttribute("project_id", project.getId());
    }
    
    // TEST as admin user
    @Test
    @DirtiesDatabase
    public void testDepositOneDataFileAsAdmin() throws Exception {
        assertEquals("Expected status to be 302.", 302, depositOneFile(adminSession));
        assertEquals("Expected there to be one deposit id", 1, datasetDepositIds.size());
        
        ArchiveSearchResult<DataItem> dataSetResults = archiveService.retrieveDataSet(datasetDepositIds.get(0));
        Iterator<DataItem> dsResultIter = dataSetResults.getResults().iterator();
        DataItem retrievedDataset = null;
        if (dsResultIter.hasNext()) {
            retrievedDataset = dsResultIter.next();
        }
        assertNotNull(retrievedDataset);
        
        assertEquals(ds.getName(), retrievedDataset.getName());
        assertEquals(ds.getDescription(), retrievedDataset.getDescription());
        
        // verify that the retrieved dataset has the proper Id prefix
        assertTrue(retrievedDataset.getId().contains("item/"));
        
        assertEquals(1, retrievedDataset.getFiles().size());
        DataFile retrievedFile = (DataFile) retrievedDataset.getFiles().get(0);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(retrievedFile.getSource()).openStream()));
        assertEquals(data_string, in.readLine());
        
        in.close();
        
        // assert that a package was inserted to the database and is retrievable
        testPackageInsertion(depositActionBean);
        // assert that proper relationship is recorded in the relationshipTable
        testDatasetCollectionRelationship(depositActionBean);
    }
    
    /**
     * Make sure event is fired for deposit.
     * 
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testDepositEventFires() throws Exception {
        final List<Package> deposited = new ArrayList<Package>();
        
        eventManager.addListener(new EventListener() {
            @Override
            public void handleEvent(EventContext eventContext, Event<?> event) {
                if (event instanceof DataItemDepositEvent) {
                    deposited.add((Package) event.getEventObject());
                }
            }
            
            @Override
            public String getName() {
                return "test listener";
            }
        });
        
        assertEquals("Expected status to be 302.", 302, depositOneFile(adminSession));
        assertEquals("Expected there to be one deposit id", 1, datasetDepositIds.size());
        
        // Wait for events to be executed
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.SECONDS);
        
        assertEquals("Expected one DataItemDepositEvent to be fired", 1, deposited.size());
    }
    
    /**
     * Test depositing dataset with a regular user's credential after assign depositing permission. Expect the
     * permission-denied-message list to be empty
     * 
     * @throws Exception
     */
    
    @Test
    @DirtiesDatabase
    public void testDepositOneFileAuthorizedDepositor() throws Exception {
        relService.addDepositorToCollection(user, collectionOne);
        assertEquals("Expected status to be 302.", 302, depositOneFile(userSession));
        assertEquals("Expected there to be one deposit id", 1, datasetDepositIds.size());
        ArchiveSearchResult<DataItem> dataSetResults = archiveService.retrieveDataSet(datasetDepositIds.get(0));
        Iterator<DataItem> dsResultIter = dataSetResults.getResults().iterator();
        DataItem retrievedDataset = null;
        if (dsResultIter.hasNext()) {
            retrievedDataset = dsResultIter.next();
        }
        assertNotNull(retrievedDataset);
        
        assertEquals(ds.getName(), retrievedDataset.getName());
        assertEquals(ds.getDescription(), retrievedDataset.getDescription());
        
        assertEquals(1, retrievedDataset.getFiles().size());
        DataFile retrievedFile = (DataFile) retrievedDataset.getFiles().get(0);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(retrievedFile.getSource()).openStream()));
        assertEquals(data_string, in.readLine());
        
        in.close();
        
        // assert that a package was inserted to the database and is retrievable
        testPackageInsertion(depositActionBean);
        // assert that proper relationship is recorded in the relationshipTable
        testDatasetCollectionRelationship(depositActionBean);
    }
    
    /**
     * Test depositing dataset without first assigning depositing permission to user. Expect the
     * permission-denied-message list to have at least one message on it.
     * 
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testDepositOneFileWithoutDepositerPermission() throws Exception {
        assertEquals("Expected status to be 400.", 400, depositOneFile(userSession));
        
        assertEquals("No deposit id should be assigned", 0, datasetDepositIds.size());
    }
    
    private int depositOneFile(MockHttpSession session) throws Exception {
        File tmp = File.createTempFile("metadata", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println(data_string);
        out.close();
        
        ds = new DataItem();
        ds.setName(tmp.getName());
        ds.setParentId(collectionOne.getId());
        /* ds.setDescription("This is the first data set"); */
        
        MockRoundtrip depositRoundTrip = new MockRoundtrip(servletCtx, DepositActionBean.class, session);
        depositRoundTrip.addParameter("currentCollectionId", collectionOne.getId());
        /*
         * depositRoundTrip.addParameter("dataSet.name", ds.getName());
         * depositRoundTrip.addParameter("dataSet.description", ds.getDescription());
         */
        depositRoundTrip.addParameter("uploadedFile", tmp.getPath());
        // Making sure the page is redirected properly after the deposit.
        depositRoundTrip.addParameter("redirectUrl", UserCollectionsActionBean.HOME_COLLECTIONS_PATH);
        depositRoundTrip.execute("deposit");
        archiveService.pollArchive();
        
        depositActionBean = depositRoundTrip.getActionBean(DepositActionBean.class);
        
        // assertNull("Expected forward URL to be null.", depositRoundTrip.getForwardUrl());
        datasetDepositIds = depositActionBean.getDepositIds();
        
        return depositRoundTrip.getResponse().getStatus();
    }
    
    private MockRoundtrip updateOneFile(MockHttpSession session, String dataSetId) throws Exception {
        File tmp = File.createTempFile("update", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println(data_string);
        out.close();
        updateName = tmp.getName();
        ds = new DataItem();
        ds.setName(updateName);
        ds.setParentId(collectionOne.getId());
        /* ds.setDescription("This is the first data set"); */
        
        MockRoundtrip depositRoundTrip = new MockRoundtrip(servletCtx, DepositActionBean.class, session);
        depositRoundTrip.addParameter("currentCollectionId", collectionOne.getId());
        depositRoundTrip.addParameter("datasetToUpdateId", dataSetId);
        depositRoundTrip.addParameter("uploadedFile", tmp.getPath());
        // Making sure the page is redirected properly after the deposit.
        depositRoundTrip.addParameter("redirectUrl", UserCollectionsActionBean.HOME_COLLECTIONS_PATH);
        depositRoundTrip.execute("update");
        archiveService.pollArchive();
        
        depositActionBean = depositRoundTrip.getActionBean(DepositActionBean.class);
        
        // assertNull("Expected forward URL to be null.", depositRoundTrip.getForwardUrl());
        datasetDepositIds = depositActionBean.getDepositIds();
        
        return depositRoundTrip;
    }
    
    /**
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testDepositOneFileOnlyZippedFileAsAdmin() throws Exception {
        testDepositOneZippedFileAsAdmin(FILES_ONLY_ZIP);
        // assert that a package was inserted to the database and is retrievable
        testPackageInsertion(depositActionBean);
        // assert that proper relationship is recorded in the relationshipTable
        testDatasetCollectionRelationship(depositActionBean);
        
    }
    
    @Test
    @DirtiesDatabase
    public void testDepositOneWDirectoryZippedFileAsAdmin() throws Exception {
        testDepositOneZippedFileAsAdmin(DIRECTORY_ZIP);
        // assert that a package was inserted to the database and is retrievable
        testPackageInsertion(depositActionBean);
        // assert that proper relationship is recorded in the relationshipTable
        testDatasetCollectionRelationship(depositActionBean);
        
    }
    
    @Test
    @DirtiesDatabase
    public void testDepositNestedZippedFileAsAdmin() throws Exception {
        testDepositOneZippedFileAsAdmin(NESTED_ZIP);
        // assert that a package was inserted to the database and is retrievable
        testPackageInsertion(depositActionBean);
        // assert that proper relationship is recorded in the relationshipTable
        testDatasetCollectionRelationship(depositActionBean);
        
    }
    
    /**
     * Tests that the default handler is executed successfully
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultHandler() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, DepositActionBean.class, adminSession);
        rt.execute();
        assertEquals("Expected a 200 HTTP status code when loading the deposit page.", 200, rt.getResponse()
                .getStatus());
        assertTrue("Expected the forward url to end with 'deposit.jsp' (full forward URL was: " + rt.getForwardUrl()
                + ")", rt.getForwardUrl().endsWith("deposit.jsp"));
    }
    
    /**
     * Tests that a dataset is correctly updated
     * 
     * @throws Exception
     */
    @Test
    @DirtiesDatabase
    public void testUpdatingDataset() throws Exception {
        // verify assumptions
        if (datasetDepositIds != null) {
            assertEquals(0, datasetDepositIds.size());
        }

        // Get the deposit id of the collection
        List<ArchiveDepositInfo> infoList = archiveService.listDepositInfo(collectionOne.getId(),
                ArchiveDepositInfo.Status.DEPOSITED);
        final String collectionDepositId = infoList.get(0).getDepositId();
        assertNotNull(collectionDepositId);
        assertTrue(collectionDepositId.length() > 0);

        // Retrieve the datasets for the collection; should be empty
        ArchiveSearchResult<DataItem> results = archiveService
                .retrieveDataSetsForCollection(collectionDepositId, -1, 0);
        java.util.Collection<DataItem> dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(0, dataItems.size());

        // First deposit one file, this is the file that will be updated.
        assertEquals("Expected deposit status to be 302.", 302, depositOneFile(adminSession));
        assertEquals("Expected there to be one deposit id", 1, datasetDepositIds.size());

        // Retrieve the datasets for the collection; only the initial deposit
        results = archiveService
                .retrieveDataSetsForCollection(collectionDepositId, -1, 0);
        dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());

        // Retrieve the dataset so we can use it's id to do the update
        ArchiveSearchResult<DataItem> dataSetResults = archiveService.retrieveDataSet(datasetDepositIds.get(0));
        Iterator<DataItem> dsResultIter = dataSetResults.getResults().iterator();
        DataItem retrievedDataset = null;
        if (dsResultIter.hasNext()) {
            retrievedDataset = dsResultIter.next();
        }
        assertNotNull(retrievedDataset);

        MockRoundtrip mrt = updateOneFile(adminSession, retrievedDataset.getId());
        assertEquals("Expected update status to be 302.", 302, mrt.getResponse().getStatus());

        String expectedRedirectURL = new RedirectResolution(CollectionDataListActionBean.class, "renderResults")
                .getPath();
        assertTrue(mrt.getDestination().contains(expectedRedirectURL));

        // Retrieve the datasets for the collection only the update should be returned.
        results = archiveService
                .retrieveDataSetsForCollection(collectionDepositId, -1, 0);
        dataItems = results.getResults();
        assertNotNull(dataItems);
        assertEquals(1, dataItems.size());

        // Check that the name of the returned dataset matches the updated name
        DataItem dataItem = dataItems.iterator().next();
        assertTrue(dataItem.getName().equals(updateName));
    }
    
    /**
     * Tests that the update page is correctly displayed
     * 
     * @throws Exception
     */
    @Test
    public void testViewUpdatePage() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, DepositActionBean.class, adminSession);
        rt.execute("renderUpdateForm");
        assertEquals("Expected a 200 HTTP status code when loading the update dataset page.", 200, rt.getResponse()
                .getStatus());
        assertTrue("Expected the forward url to end with 'updateFile.jsp' (full forward URL was: " + rt.getForwardUrl()
                + ")", rt.getForwardUrl().endsWith("updateFile.jsp"));
    }
    
    /**
     * Deposit a ZIP file (which will create a DataItem (aka Data Set) for each file in the ZIP). We expect one Package
     * to be created for the ZIP file, which is represented by a single row in the PACKAGE_TABLE. Two rows should be
     * created in the PACKAGE_FILE_DATA, one row for each file in the ZIP.
     * <p/>
     * Then attempt to update one of the data items originally deposited in the ZIP. This should result in a second
     * Package being created, which is represented by another row in the PACKAGE_TABLE. An additional row should be
     * created in the PACKAGE_FILE_DATA which represents the updated file.
     * <p/>
     * <strong>N.B.</strong>: the original two rows in the PACKAGE_FILE_DATA still remain in the data base table after
     * the update, which may not be the proper behavior.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositZipFileThenUpdateDataset() throws Exception {
        // No Packages should exist at this point
        assertEquals(0, packageDao.selectPackage().size());
        
        // Create a ZIP file to deposit, containing two text files.
        File zipFileToDeposit = File.createTempFile("DepositActionBeanTest-testDepositZipFileThenUpdateDataset-",
                ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileToDeposit));
        
        // Create a ZipEntry for the first text file.
        final String zipFileName1 = "DepositActionBeanTest-testDepositZipFileThenUpdateDataset-01.txt";
        ZipEntry zipFile = new ZipEntry(zipFileName1);
        zipOut.putNextEntry(zipFile);
        final String zipFileContent1 = "Test File One";
        IOUtils.write(zipFileContent1, zipOut);
        zipOut.closeEntry();
        
        // Create a second ZipEntry for the second text file.
        final String zipFileName2 = "DepositActionBeanTest-testDepositZipFileThenUpdateDataset-02.txt";
        zipFile = new ZipEntry(zipFileName2);
        zipOut.putNextEntry(zipFile);
        final String zipFileContent2 = "Test File Two";
        IOUtils.write(zipFileContent2, zipOut);
        zipOut.closeEntry();
        zipOut.close();
        
        List<String> dataItemsDepositIds = archiveService.listDataSets(ArchiveDepositInfo.Status.DEPOSITED);
        int startingDataItemCount = dataItemsDepositIds.size();
        MockRoundtrip depositRoundTrip = new MockRoundtrip(servletCtx, DepositActionBean.class, adminSession);
        depositRoundTrip.setParameter("currentCollectionId", collectionOne.getId()); // The collectionWithData to
                                                                                     // deposit to
        depositRoundTrip.setParameter("isContainer", "true"); // The file being deposited is a container (it's a ZIP
                                                              // file)
        depositRoundTrip.setParameter("uploadedFile", zipFileToDeposit.getAbsolutePath());
        // Making sure the page is redirected properly after the deposit.
        depositRoundTrip.addParameter("redirectUrl", UserCollectionsActionBean.HOME_COLLECTIONS_PATH);
        
        // Execute the DepositActionBean. A Package should be created by the deposit, and the Package should contain
        // two files.
        
        depositRoundTrip.execute("deposit");
        assertEquals("Expected one Package to be created by this deposit!", 1, packageDao.selectPackage().size());
        assertEquals("Expected two files to be contained in this package!", 2, packageDao.selectPackage().iterator()
                .next().getFileData().size());
        
        // Two Data Items should have been created in the Archive
        
        archiveService.pollArchive(); // Update the deposit status of any pending deposits
        dataItemsDepositIds = archiveService.listDataSets(ArchiveDepositInfo.Status.DEPOSITED);
        assertEquals(startingDataItemCount + 2, dataItemsDepositIds.size());
        
        // Retrieve one of the DataItems
        
        ArchiveSearchResult<DataItem> dataItems = archiveService.retrieveDataSet(dataItemsDepositIds.get(0));
        assertEquals(1, dataItems.getResultCount());
        DataItem dataItem = dataItems.getResults().iterator().next();
        assertNotNull(dataItem);
        
        // The Data Item should only have a single file, with a name equal to one of the two files we originally
        // deposited.
        
        assertEquals(1, dataItem.getFiles().size());
        DataFile f = dataItem.getFiles().get(0);
        assertTrue(f.getName().equals(zipFileName1) || f.getName().equals(zipFileName2));
        
        // Now, let's update the Data Item by replacing the file.
        File updateFile = File.createTempFile("DepositActionBeanTest-testDepositZipFileThenUpdateDataset-03", ".txt");
        IOUtils.write("Test Content Three", new FileOutputStream(updateFile));
        
        depositRoundTrip = new MockRoundtrip(servletCtx, DepositActionBean.class, adminSession);
        depositRoundTrip.setParameter("currentCollectionId", collectionOne.getId()); // The collectionWithData to
                                                                                     // deposit to
        depositRoundTrip.setParameter("isContainer", "false"); // The file being deposited is not a container
        depositRoundTrip.setParameter("uploadedFile", updateFile.getAbsolutePath()); // This is the file with the
                                                                                     // updated contents
        depositRoundTrip.setParameter("datasetToUpdateId", dataItem.getId()); // This is the data item we are updating
        // Making sure the page is redirected properly after the deposit.
        depositRoundTrip.addParameter("redirectUrl", UserCollectionsActionBean.HOME_COLLECTIONS_PATH);
        // Execute the DepositActionBean. A Package should be created by the deposit, and the Package should contain
        // a single file.
        
        depositRoundTrip.execute("update");
        assertEquals("Expected one Package to be created by this deposit!", 2, packageDao.selectPackage().size());
        assertEquals("Expected one file to be contained in this package!", 1, packageDao.selectPackage().get(1)
                .getFileData().size());
        
    }
    
    /**
     * <p>
     * Test depositing one zipped file.
     * </p>
     * <p>
     * Accepts name of the zip file. Deposit the zip file through
     * {@link org.dataconservancy.ui.stripes.DepositActionBean}.
     * </p>
     * <p>
     * Test for number of files returned from the archive after the deposit is equal to the number of files in the
     * original zip file. Test for the content of each returned file is the same as the original unzipped file.
     * </p>
     * <p>
     * Assumption: each of the .txt test file only has 1 line of test text.
     * </p>
     * 
     * @param zipFileName
     * @throws Exception
     */
    private void testDepositOneZippedFileAsAdmin(String zipFileName) throws Exception {
        ds = new DataItem();
        URL fileUrl = this.getClass().getResource(zipFileName);
        assertNotNull("Cannot find classpath resource " + zipFileName, fileUrl);
        
        // creating zip file from the provided file name
        File zipFile = new File(fileUrl.toURI());
        // decompressing the original files for later comparison
        ZipPackageExtractor extractor = new ZipPackageExtractor();
        extractor.setExtractDirectory(ZIP_DIRECTORY);
        List<File> unzippedOriginalFiles = extractor.getFilesFromPackageFile(zipFile.getName(), zipFile);
        int fileCount = 0;
        for (File file : unzippedOriginalFiles) {
            if (!file.isDirectory()) {
                fileCount++;
            }
        }
        
        // set up the stripes deposit request: depositing the file inside of a dataset
        MockRoundtrip depositRoundTrip = new MockRoundtrip(servletCtx, DepositActionBean.class, adminSession);
        depositRoundTrip.addParameter("currentCollectionId", collectionOne.getId());
        depositRoundTrip.addParameter("uploadedFile", zipFile.getPath());
        depositRoundTrip.addParameter("isContainer", "true");
        // Making sure the page is redirected properly after the deposit.
        depositRoundTrip.addParameter("redirectUrl", UserCollectionsActionBean.HOME_COLLECTIONS_PATH);
        // send deposit request
        depositRoundTrip.execute("deposit");
        archiveService.pollArchive();
        
        depositActionBean = depositRoundTrip.getActionBean(DepositActionBean.class);
        
        datasetDepositIds = depositActionBean.getDepositIds();
        // check to make sure the returned code from the request is good.
        assertEquals("Expected status to be 302.", 302, depositRoundTrip.getResponse().getStatus());
        
        // expect the returned number of datasets to contain the same number of files as the original zip file
        assertEquals(fileCount, datasetDepositIds.size());
        
        for (int i = 0; i < datasetDepositIds.size(); i++) {
            // retrieve deposited dataset from archive
            ArchiveSearchResult<DataItem> dataSetResults = archiveService.retrieveDataSet(datasetDepositIds.get(i));
            Iterator<DataItem> dsResultIter = dataSetResults.getResults().iterator();
            DataItem retrievedDataset = null;
            if (dsResultIter.hasNext()) {
                retrievedDataset = dsResultIter.next();
            }
            
            // expect returned dataset not to be null
            assertNotNull(retrievedDataset);
            
            BufferedReader fromTheArchive;
            BufferedReader fromDecompressedFile;
            assertEquals("There should be one file for each dataset", 1, retrievedDataset.getFiles().size());
            DataFile fileFromArchive = retrievedDataset.getFiles().get(0);
            
            for (File decompressedFile : unzippedOriginalFiles) {
                if (fileFromArchive.getName().equals(decompressedFile.getName())) {
                    if (fileFromArchive.getPath() != null) {
                        // Check if the file is in a folder
                        String fileParent = "";
                        String unpackDirectory = new File(ZIP_DIRECTORY, "/" + zipFile.getName().substring(0, zipFile.getName().length()-4)).getPath();
                        if (decompressedFile.getParent().length() > unpackDirectory.length()) {
                            // Parent is more than just the zip directory.
                            fileParent = decompressedFile.getParent().substring(unpackDirectory.length() - 1);
                        }
//                        assertEquals(fileFromArchive.getPath(), fileParent);
                    }
                    
                    // open up files from the archive and original unzipped files for content comparison
                    fromTheArchive = new BufferedReader(new InputStreamReader(
                            new URL(fileFromArchive.getSource()).openStream()));
                    fromDecompressedFile = new BufferedReader(new FileReader(decompressedFile));
                    // compare file content
                    assertEquals(fromDecompressedFile.readLine(), fromTheArchive.readLine());
                    fromDecompressedFile.close();
                    fromTheArchive.close();
                    break;
                }
            }
        }
        
        extractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY));
        zipFile.deleteOnExit();
    }
    
    /**
     * Test that relationship between deposited dataset and its containing collectionWithData exists
     */
    private void testDatasetCollectionRelationship(DepositActionBean dab) {
        Package depositPackage = depositActionBean.getDepositPackage();
        Iterator itr = depositPackage.getFileData().entrySet().iterator();
        Map.Entry<String, String> pair;
        while (itr.hasNext()) {
            pair = (Map.Entry) itr.next();
            assertTrue(relService
                    .isRelated(pair.getKey(), collectionOne.getId(), Relationship.RelType.IS_AGGREGATED_BY));
            assertTrue(relService.isRelated(collectionOne.getId(), pair.getKey(), Relationship.RelType.AGGREGATES));
        }
        
    }
    
    /**
     * Test that the package built by DepositActionBean is persisted and retrievable from DB
     * 
     * @param dab
     */
    private void testPackageInsertion(DepositActionBean dab) {
        Package generatedDepositPackage = depositActionBean.getDepositPackage();
        Package retrievedDepositPackage = packageDao.selectPackage(generatedDepositPackage.getId());
        
        assertEquals(generatedDepositPackage, retrievedDepositPackage);
    }
}
