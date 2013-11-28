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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.ui.it.support.BaseIT;
import org.dataconservancy.ui.it.support.CreateCollectionRequest;
import org.dataconservancy.ui.it.support.DepositRequest;
import org.dataconservancy.ui.it.support.HttpAssert;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.stripes.UserCollectionsActionBean;
import org.dataconservancy.ui.util.GZipPackageExtractor;
import org.dataconservancy.ui.util.TarPackageExtractor;
import org.dataconservancy.ui.util.ZipPackageExtractor;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DepositZipFileIT extends BaseIT {
    private final static String WINDOWS_FILE_ZIP_PATH = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    private final static String WINDOWS_DIRECTORY_ZIP_PATH = "/SampleFilePackages/Windows/WindowsZipFileWDirectory.zip";
    private final static String WINDOWS_NESTED_ZIP_PATH = "/SampleFilePackages/Windows/NestedZip.zip";
    private final static String LINUX_FILE_ZIP_PATH = "/SampleFilePackages/Linux/SeveralFiles.zip";
    private final static String LINUX_DIRECTORY_ZIP_PATH = "/SampleFilePackages/Linux/Directory.zip";
    private final static String LINUX_NESTED_ZIP_PATH = "/SampleFilePackages/Linux/SeveralZipFiles.zip";
    private final static String ZIP_DIRECTORY_PATH = "./test/";
    private final static String SINGLE_FILE_PATH = "/SampleFilePackages/File1.txt";
    private final static String EMPTY_ZIP_PATH = "/SampleFilePackages/empty.zip";
    private final static String WINDOWS_FILE_TAR_PATH = "/SampleFilePackages/Windows/WindowsTarFile.tar";
    private final static String WINDOWS_FILE_GZIP_PATH = "/SampleFilePackages/Windows/GzippedFile1.txt.gz";
    private final static String WINDOWS_FILE_TAR_GZIP_PATH = "/SampleFilePackages/Windows/WindowsTarFile.tar.gz";
    
    private final static String ERROR_MSG_UNPACK_FILE = "Error occur when trying to deposit the provided file";
    
    private HttpClient hc = new DefaultHttpClient();
    
    @Autowired
    private CreateCollectionRequest createCollectionRequest;
    
    // Someone to create the project
    @Autowired
    @Qualifier("defaultAdmin")
    private Person adminUser;
    
    private HttpPost adminUserLogin;
    
    private HttpGet logout;
    
    // Collection to deposit to
    private static Collection collection;
    
    // Project to hold the collections
    private static Project project;
    
    /**
     * State for the test. This flag is modified during test setup and checked when each test method is invoked. That
     * way, we don't end up wasting time creating a bunch of objects (they aren't modified by the tests). They aren't in
     * an {@code @BeforeClass} because doing everything static gets annoying.
     */
    private static boolean isObjectsSeeded = false;
    
    private static File windowsFileOnlyZip;
    private static File windowsDirectoryZip;
    private static File windowsNestedZip;
    private static File linuxFileOnlyZip;
    private static File linuxDirectoryZip;
    private static File linuxNestedZip;
    private static File singleFile;
    private static File emptyZip;
    private static File windowsFileOnlyTar;
    private static File windowsFileOnlyGzip;
    private static File windowsFileTarGzip;
    
    private ZipPackageExtractor zipExtractor;
    private TarPackageExtractor tarExtractor;
    private GZipPackageExtractor gzipExtractor;
    
    private static String collectionArchiveID;
    
    /**
     * Creates File objects for the zip files to be used in the test.
     */
    @BeforeClass
    public static void loadZipFiles() {
        URL windowsFileOnlyURL = DepositZipFileIT.class.getResource(WINDOWS_FILE_ZIP_PATH);
        URL windowsDirectoryZipURL = DepositZipFileIT.class.getResource(WINDOWS_DIRECTORY_ZIP_PATH);
        URL windowsNestedZipURL = DepositZipFileIT.class.getResource(WINDOWS_NESTED_ZIP_PATH);
        
        URL linuxFileOnlyURL = DepositZipFileIT.class.getResource(LINUX_FILE_ZIP_PATH);
        URL linuxDirectoryZipURL = DepositZipFileIT.class.getResource(LINUX_DIRECTORY_ZIP_PATH);
        URL linuxNestedZipURL = DepositZipFileIT.class.getResource(LINUX_NESTED_ZIP_PATH);
        
        URL singleFileURL = DepositZipFileIT.class.getResource(SINGLE_FILE_PATH);
        URL EmptyZipURL = DepositZipFileIT.class.getResource(EMPTY_ZIP_PATH);
        
        URL windowsFileOnlyTarURL = DepositZipFileIT.class.getResource(WINDOWS_FILE_TAR_PATH);
        URL windowsFileOnlyGZipURL = DepositZipFileIT.class.getResource(WINDOWS_FILE_GZIP_PATH);
        URL windowsFileTarGZipURL = DepositZipFileIT.class.getResource(WINDOWS_FILE_TAR_GZIP_PATH);
        
        try {
            windowsFileOnlyZip = new File(windowsFileOnlyURL.toURI());
            windowsDirectoryZip = new File(windowsDirectoryZipURL.toURI());
            windowsNestedZip = new File(windowsNestedZipURL.toURI());
            
            linuxFileOnlyZip = new File(linuxFileOnlyURL.toURI());
            linuxDirectoryZip = new File(linuxDirectoryZipURL.toURI());
            linuxNestedZip = new File(linuxNestedZipURL.toURI());
            
            singleFile = new File(singleFileURL.toURI());
            emptyZip = new File(EmptyZipURL.toURI());
            
            windowsFileOnlyTar = new File(windowsFileOnlyTarURL.toURI());
            windowsFileOnlyGzip = new File(windowsFileOnlyGZipURL.toURI());
            windowsFileTarGzip = new File(windowsFileTarGZipURL.toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Error creating zip file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sets the up the project and collection to deposit into.
     */
    @Before
    public void setUp() throws Exception {
        archiveSupport.setPollCount(30);
        
        hc.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        hc.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        zipExtractor = new ZipPackageExtractor();
        tarExtractor = new TarPackageExtractor();
        gzipExtractor = new GZipPackageExtractor();
        adminUserLogin = reqFactory.createLoginRequest(adminUser).asHttpPost();
        logout = reqFactory.createLogoutRequest().asHttpGet();
        
        // Login as the administrator
        HttpAssert.assertStatus(hc, adminUserLogin, 300, 399, "Unable to login as admin user!");
        
        if (!isObjectsSeeded) {
            project = new Project();
            
            project.setDescription("A seeded project for use with ITs");
            project.setEndDate(DateTime.now());
            project.setFundingEntity("NSF");
            project.setStartDate(DateTime.now());
            project.setName("Seeded Project");
            project.addNumber("1234");
            project.setFundingEntity("Cash Money");
            
            project = reqFactory.createProjectApiAddRequest(project).execute(hc);
            
            collection = new Collection();
            collection.setId("DepositZipFileIT-collection");
            collection.setTitle("Seeded Collection");
            collection.setSummary("A seeded collection for use with ITs");
            List<PersonName> creators = new ArrayList<PersonName>();
            creators.add(new PersonName("Mr.", "John", "Jack", "Doe", "II"));
            collection.setCreators(creators);
            
            createCollectionRequest.setCollection(collection);
            createCollectionRequest.setProjectId(project.getId());
            hc.execute(createCollectionRequest.asHttpPost()).getEntity().getContent().close();
            HttpAssert.assertStatus(hc, new HttpGet(urlConfig.getViewCollectionUrl(collection.getId()).toURI()), 200,
                    "Unable to create collection " + collection);
            
            DcsDeliverableUnit collectionDu = archiveSupport.pollAndQueryArchiveForCollectionDu(collection.getId());
            assertNotNull("Collection was not created in the archive.", collectionDu);
            collectionArchiveID = collectionDu.getId();
            isObjectsSeeded = true;
        }
    }
    
    /**
     * This test tests that Zip Files that contain nothing but files can be deposit. This test will test a zip generated
     * on windows, linux and mac.
     */
    @Test
    public void testDepositFileOnlyZip() throws Exception {
        
        // Insure we have a empty archive with no items in the collection
        int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        assertEquals("Unexpected number of items in the collection", 0, actualArchiveCollectionSize);
        
        // Create the Deposit request
        final String windowsFileDatasetId = "Windows-FileOnlyZip";
        final DataItem windowsFileDs = createSampleDataset("WindowsFileOnlyZip", "A Sample Zip DataItem",
                windowsFileDatasetId, null);
        final DepositRequest windowsFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                windowsFileDs, collection.getId(), windowsFileOnlyZip);
        windowsFileDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of items in the zip file
        zipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        List<File> zippedFiles = zipExtractor.getFilesFromPackageFile(windowsFileOnlyZip.getName(), windowsFileOnlyZip);
        int expectedArchiveCollectionSize = zippedFiles.size();
        
        // Perform the deposit;
        depositPackageFile(windowsFileDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkPackageFileDeposit(fileDus, zippedFiles, expectedArchiveCollectionSize, actualArchiveCollectionSize);
        
        // Clean up
        zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        zippedFiles.clear();
        
        // Create another Deposit request, this time with a zip file created on the linux platform.
        final String linuxFileDatasetId = "Linux-FileOnlyZip";
        final DataItem linuxFileDs = createSampleDataset("LinuxFileOnlyZip", "A Sample Zip DataItem",
                linuxFileDatasetId, null);
        final DepositRequest linuxFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(linuxFileDs,
                collection.getId(), linuxFileOnlyZip);
        linuxFileDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of files in the zip file
        zipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        zippedFiles = zipExtractor.getFilesFromPackageFile(linuxFileOnlyZip.getName(), linuxFileOnlyZip);
        expectedArchiveCollectionSize = actualArchiveCollectionSize + zippedFiles.size();
        
        // Perform the deposit; testing with a zip file created on the linux platform
        depositPackageFile(linuxFileDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkPackageFileDeposit(fileDus, zippedFiles, expectedArchiveCollectionSize, actualArchiveCollectionSize);
        
        // Clean up
        zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        zippedFiles.clear();
    }
    
    /**
     * Tests depositing a tar file containing files. 
     * @throws Exception
     */
    @Test
    public void testDepositFileOnlyTar() throws Exception {
        
        int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        // Create the Deposit request
        final String windowsFileDatasetId = "Windows-FileOnlyTar";
        final DataItem windowsFileDs = createSampleDataset("WindowsFileOnlyTar", "A Sample Tar DataItem",
                windowsFileDatasetId, null);
        final DepositRequest windowsFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                windowsFileDs, collection.getId(), windowsFileOnlyTar);
        windowsFileDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of items in the zip file
        tarExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        List<File> tarredFiles = tarExtractor.getFilesFromPackageFile(windowsFileOnlyTar.getName(), windowsFileOnlyTar);
        int expectedArchiveCollectionSize = actualArchiveCollectionSize + tarredFiles.size();
        
        // Perform the deposit;
        depositPackageFile(windowsFileDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkPackageFileDeposit(fileDus, tarredFiles, expectedArchiveCollectionSize, actualArchiveCollectionSize);
        
        // Clean up
        tarExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        tarredFiles.clear();
    }
    
    /**
     * Tests depositing a gzip file containing a text file 
     * @throws Exception
     */
    @Test
    public void testDepositFileOnlyGzip() throws Exception {
        
        int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        // Create the Deposit request
        final String windowsFileDatasetId = "Windows-FileOnlyGzip";
        final DataItem windowsFileDs = createSampleDataset("WindowsFileOnlyGZip", "A Sample Gzip DataItem",
                windowsFileDatasetId, null);
        final DepositRequest windowsFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                windowsFileDs, collection.getId(), windowsFileOnlyGzip);
        windowsFileDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of items in the zip file
        gzipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        List<File> gzippedFiles = gzipExtractor.getFilesFromPackageFile(windowsFileOnlyGzip.getName(), windowsFileOnlyGzip);
        int expectedArchiveCollectionSize = actualArchiveCollectionSize + 1;
        
        // Perform the deposit;
        depositPackageFile(windowsFileDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkPackageFileDeposit(fileDus, gzippedFiles, expectedArchiveCollectionSize, actualArchiveCollectionSize);
        
        // Clean up
        gzipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        gzippedFiles.clear();
    }
    
    /**
     * Tests depositing a gzip file containing a tar file. 
     * This test documents current behavior that only a single tar file will be deposited. The tar will not be unpacked. 
     * @throws Exception
     */
    @Test
    public void testDepositTarGzip() throws Exception {
        
        int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        // Create the Deposit request
        final String windowsFileDatasetId = "Windows-TarGzip";
        final DataItem windowsFileDs = createSampleDataset("WindowsTarGZip", "A Sample Tar Gzip DataItem",
                windowsFileDatasetId, null);
        final DepositRequest windowsFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                windowsFileDs, collection.getId(), windowsFileTarGzip);
        windowsFileDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of items in the zip file
        gzipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        List<File> gzippedFiles = gzipExtractor.getFilesFromPackageFile(windowsFileTarGzip.getName(), windowsFileTarGzip);
        int expectedArchiveCollectionSize = actualArchiveCollectionSize + 1;
        
        // Perform the deposit;
        depositPackageFile(windowsFileDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkPackageFileDeposit(fileDus, gzippedFiles, expectedArchiveCollectionSize, actualArchiveCollectionSize);
        
        // Clean up
        gzipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        gzippedFiles.clear();
    }
    
    /**
     * This test will test that a zip file containing a zip is deposit correctly.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositWithNestedZip() throws Exception {
        // Get the number of items already in the collection
        int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        // Create the Deposit request
        final String windowsNestedDatasetId = "Windows-NestedZip";
        final DataItem windowsNestedDs = createSampleDataset("WindowsNestedZip", "A Sample Nested Zip DataItem",
                windowsNestedDatasetId, null);
        final DepositRequest windowsNestedDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                windowsNestedDs, collection.getId(), windowsNestedZip);
        windowsNestedDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of items in the zip file
        zipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        List<File> zippedFiles = zipExtractor.getFilesFromPackageFile(windowsNestedZip.getName(), windowsNestedZip);
        int expectedArchiveCollectionSize = zippedFiles.size() + actualArchiveCollectionSize;
        
        // Perform the deposit; testing depositing a zip file that contains just files created on windows.
        depositPackageFile(windowsNestedDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkPackageFileDeposit(fileDus, zippedFiles, expectedArchiveCollectionSize, actualArchiveCollectionSize);
        
        // Clean up
        zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        zippedFiles.clear();
        
        // Create another Deposit request, this time with a zip file created on the linux platform.
        final String linuxNestedDatasetId = "Linux-NestedZip";
        final DataItem linuxNestedDs = createSampleDataset("LinuxNestedZip", "A Sample Nested Zip DataItem",
                linuxNestedDatasetId, null);
        final DepositRequest linuxNestedDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                linuxNestedDs, collection.getId(), linuxNestedZip);
        linuxNestedDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of files in the zip file
        zipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        zippedFiles = zipExtractor.getFilesFromPackageFile(linuxNestedZip.getName(), linuxNestedZip);
        expectedArchiveCollectionSize = actualArchiveCollectionSize + zippedFiles.size();
        
        // Perform the deposit; testing with a zip file created on the linux platform
        depositPackageFile(linuxNestedDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkPackageFileDeposit(fileDus, zippedFiles, expectedArchiveCollectionSize, actualArchiveCollectionSize);
        
        // Clean up
        zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        zippedFiles.clear();
    }
    
    /**
     * This test will test that a zip file that has nested folders is deposited correctly. It will ensure all files are
     * deposited and that folder structure is maintained.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositWithFolders() throws Exception {
        // Check for the number of files in the collection
        int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        // Create the Deposit request
        final String windowsDatasetId = "Windows-DirectoryZip";
        final DataItem windowsDs = createSampleDataset("WindowsDirectoryZip", "A Sample Zip with folders DataItem",
                windowsDatasetId, null);
        final DepositRequest windowsDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(windowsDs,
                collection.getId(), windowsDirectoryZip);
        windowsDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of items in the zip file
        zipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        List<File> zippedFiles = zipExtractor.getFilesFromPackageFile(windowsDirectoryZip.getName(), windowsDirectoryZip);
        int fileCount = 0;
        for (File file : zippedFiles) {
            if (!file.isDirectory()) {
                fileCount++;
            }
        }
        int expectedArchiveCollectionSize = fileCount + actualArchiveCollectionSize;
        // Perform the deposit; testing depositing a zip file that contains just files created on windows.
        depositPackageFile(windowsDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        Set<DcsDeliverableUnit> fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        checkZipFileWithFoldersDeposit(fileDus, zippedFiles, expectedArchiveCollectionSize, 
                                       actualArchiveCollectionSize, ZIP_DIRECTORY_PATH + "/" + windowsDirectoryZip.getName().substring(0, windowsDirectoryZip.getName().length()-4));
        
        // Clean up
        zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        zippedFiles.clear();
        
        // Create another Deposit request, this time with a zip file created on the linux platform.
        final String linuxDatasetId = "Linux-DirectoryZip";
        final DataItem linuxDs = createSampleDataset("LinuxDirectoryZip", "A Sample Zip with folders DataItem",
                linuxDatasetId, null);
        final DepositRequest linuxDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(linuxDs,
                collection.getId(), linuxDirectoryZip);
        linuxDepositRequest.setContainer(true);
        
        // We expect the archive collection to grow by the number of files in the zip file
        zipExtractor.setExtractDirectory(ZIP_DIRECTORY_PATH);
        zippedFiles = zipExtractor.getFilesFromPackageFile(linuxDirectoryZip.getName(), linuxDirectoryZip);
        fileCount = 0;
        for (File file : zippedFiles) {
            if (!file.isDirectory()) {
                fileCount++;
            }
        }
        expectedArchiveCollectionSize = actualArchiveCollectionSize + fileCount;
        
        // Perform the deposit; testing with a zip file created on the linux platform
        depositPackageFile(linuxDepositRequest);
        
        // Poll the archive until all of the DeliverableUnits in the collection are found, or polling times out
        fileDus = archiveSupport.pollAndQueryArchiveForDUsInCollection(collectionArchiveID,
                expectedArchiveCollectionSize);
        
        // Query the archive for the number of items in the collection
        actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        checkZipFileWithFoldersDeposit(fileDus, zippedFiles, expectedArchiveCollectionSize, 
                                       actualArchiveCollectionSize, ZIP_DIRECTORY_PATH + "/" + linuxDirectoryZip.getName().substring(0, linuxDirectoryZip.getName().length()-4));
        
        // Clean up
        zipExtractor.cleanUpExtractedPackage(new File(ZIP_DIRECTORY_PATH));
        fileDus.clear();
        zippedFiles.clear();
    }
    
    /**
     * This test will ensure that if the collection box is not checked a file will be deposited as a single file.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositZipFileAsSingleFileDeposit() throws Exception {
        // Insure we have a empty archive with no items in the collection
        int actualArchiveCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        // Create the Deposit request
        final String zipFileDatasetId = "ZipAsFile";
        final DataItem zipFileDs = createSampleDataset("ZipAsFile", "A Sample Zip File", zipFileDatasetId, null);
        final DepositRequest zipFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(zipFileDs,
                collection.getId(), windowsFileOnlyZip);
        
        // Perform the deposit;
        depositPackageFile(zipFileDepositRequest);
        
        // Poll the archive until the DeliverableUnit representing the DataItem is found, or the polling times out.
        assertNotNull("Dataset was not created in the archive.",
                archiveSupport.pollAndQueryArchiveForDataItemDu(zipFileDs.getId()));
        
        // Query the archive for the number of items in the collection now
        int newCollectionSize = queryNumberOfItemsInCollection(collectionArchiveID);
        
        assertEquals("There should only be one file added to the collection", actualArchiveCollectionSize + 1,
                newCollectionSize);
    }
    
    /**
     * This test will check that if an empty zip file is submitted as a package nothing will be added to the archive.
     * The system should report an error.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositEmptyZipFile() throws Exception {
        // Create the Deposit request
        final String emptyZipFileDatasetId = "EmptyZipFile";
        final DataItem emptyZipFileDs = createSampleDataset("EmptyZipFile", "An empty Zip File", emptyZipFileDatasetId,
                null);
        final DepositRequest emptyZipFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(
                emptyZipFileDs, collection.getId(), emptyZip);
        emptyZipFileDepositRequest.setContainer(true);
        
        // Perform the deposit;
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(emptyZipFileDepositRequest.asHttpPost());
        assertEquals("Depositing empty zip file failed, expected return 500, but was: "
                + authorizedResponse.getStatusLine().getStatusCode(), 500, authorizedResponse.getStatusLine()
                .getStatusCode());
        
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
        final String errorMessage = String.format(props.getProperty("error.error-depositing-file"), emptyZip.getName());
        
        final InputStream content = authorizedResponse.getEntity().getContent();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(content));
        String strLine;
        boolean foundErrorMsg = false;
        
        while ((strLine = br.readLine()) != null) {
            if (strLine.contains(errorMessage)) {
                foundErrorMsg = true;
                break;
            }
        }
        
        assertTrue(foundErrorMsg);
        content.close();
        HttpAssert.free(authorizedResponse);
        
    }
    
    /**
     * This test will check that when a single file is marked as a package the system will report an error and not
     * deposit the file.
     * 
     * @throws Exception
     */
    @Test
    public void testDepositSingleFileAsPackage() throws Exception {
        // Create the Deposit request
        final String nonZipFileDatasetId = "NonZipFile";
        final DataItem nonZipFileDs = createSampleDataset("NonZipFile", "A txt File deposit as container",
                nonZipFileDatasetId, null);
        final DepositRequest nonZipFileDepositRequest = reqFactory.createSingleFileDataItemDepositRequest(nonZipFileDs,
                collection.getId(), singleFile);
        nonZipFileDepositRequest.setContainer(true);
        
        // Perform the deposit;
        // Attempt the deposit, we expect success
        HttpResponse authorizedResponse = hc.execute(nonZipFileDepositRequest.asHttpPost());
        assertEquals("Depositing empty zip file failed, expected return 500, but was: "
                + authorizedResponse.getStatusLine().getStatusCode(), 500, authorizedResponse.getStatusLine()
                .getStatusCode());
        
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/errorUiText.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        final String errorMessage = String.format(props.getProperty("error.error-depositing-file"),
                singleFile.getName());
        
        final InputStream content = authorizedResponse.getEntity().getContent();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(content));
        String strLine;
        boolean foundErrorMsg = false;
        
        while ((strLine = br.readLine()) != null) {
            if (strLine.contains(errorMessage)) {
                foundErrorMsg = true;
                break;
            }
        }
        
        assertTrue(foundErrorMsg);
        content.close();
        HttpAssert.free(authorizedResponse);
    }
    
    /**
     * Deposits a zip file and returns the list of deliverable units created in the archive for the zip file.
     * 
     * @param depositRequest
     *            A fully completed deposit request for the zip file to be executed.
     * @return A list of deliverable units representing the files in the zip, or null if no deposit was found.
     */
    private void depositPackageFile(DepositRequest depositRequest) throws Exception {
        
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
    
    private int queryNumberOfItemsInCollection(String collectionId) {
        return archiveSupport.queryArchiveForNumberOfItemsInCollection(collectionId);
    }
    
    private void checkPackageFileDeposit(Set<DcsDeliverableUnit> returnedDus, List<File> zippedFiles,
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
    
    private void checkZipFileWithFoldersDeposit(Set<DcsDeliverableUnit> returnedDus, List<File> zippedFiles,
            int expectedArchiveCount, int actualArchiveCount, String unpackDirectory) {
        // Assert the proper number of DUs from the zip file were found
        assertNotNull("The list of file deliverable units was null.", returnedDus);
        assertEquals("Not all of the deliverable units from the zip file were found.", expectedArchiveCount,
                actualArchiveCount);
        
        // Assert that the file names of files in the zip file match the title in the DU (is this really a profile
        // issue?)
        int matchedFiles = 0;
        boolean matchFound = false;
        for (File file : zippedFiles) {
            if (!file.isDirectory()) {
                matchFound = false;
                String fileParent = "";
                
                if (file.getParent().length() > unpackDirectory.length()) {
                    // Parent is more than just the zip directory.
                    fileParent = file.getParent().substring(unpackDirectory.length() - 1);
                }
                
                for (DcsDeliverableUnit du : returnedDus) {
                    
                    if (du.getTitle().equalsIgnoreCase(file.getName())) {
                        
                        if (!fileParent.isEmpty()) {
                            Set<DcsManifestation> manifestations = archiveSupport.queryDeliverableUnitForManifestations(du
                                    .getId());
                            
                            for (DcsManifestation man : manifestations) {
                                java.util.Collection<DcsManifestationFile> manFiles = man.getManifestationFiles();
                                
                                for (DcsManifestationFile manFile : manFiles) {
                                    if (manFile.getPath() != null && manFile.getPath().equalsIgnoreCase(fileParent)) {
                                        matchedFiles++;
                                        matchFound = true;
                                        break;
                                    }
                                }
                                
                                if (matchFound) {
                                    // Break from manifestation loop
                                    break;
                                }
                            }
                        }
                        else { // If file parent is empty then just matching the file names is a match
                            matchedFiles++;
                            matchFound = true;
                        }
                        
                        if (matchFound) {
                            // Break from DU loop
                            break;
                        }
                    }
                }
            }
        }
        
        int fileCount = 0;
        for (File file : zippedFiles) {
            if (!file.isDirectory()) {
                fileCount++;
            }
        }
        assertEquals("Could not find a deliverable unit for all files", fileCount, matchedFiles);
    }
    
    private DataItem createSampleDataset(String name, String description, String identifier, Person depositor) {
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
        return ds;
    }
    
    @After
    public void userLogOut() {
        HttpAssert.assertStatus(hc, logout, 300, 399, "Unable to logout admin user!");
    }
    
}