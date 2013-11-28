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

import static junit.framework.Assert.assertEquals;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.DEPOSITED;
import static org.dataconservancy.ui.test.support.DirtiesDatabase.AFTER_EACH_TEST_METHOD;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import net.sourceforge.stripes.mock.MockHttpSession;
import net.sourceforge.stripes.mock.MockRoundtrip;

import org.dataconservancy.ui.dao.ProjectDAO;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.CollectionException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.CollectionBizService;
import org.dataconservancy.ui.services.DataItemTransportService;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.test.support.DirtiesDatabase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase(AFTER_EACH_TEST_METHOD)
public class CollectionDataListActionBeanTest extends BaseActionBeanTest {
    
    @Autowired
    @Qualifier("defaultUser")
    private Person projectAdmin;
    
    @Autowired
    private RelationshipService relService;
    
    @Autowired
    private ProjectDAO projectDao;
    
    @Resource(name = "inMemoryArchiveService")
    private ArchiveService archiveService;
    
    private MockHttpSession userSession;
    
    private MockHttpSession adminSession;
    
    private MockHttpSession projectAdminSession;
    
    private static final String data_string = "This is a data file.";
    
    Properties props;
    
    // counter for how many times we have tried to poll the archive
    private int pollCount;
    // maximum number of times to poll
    private final int maxPollTimes = 60;
    
    @Before
    public void setup() throws Exception {
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
        assertTrue(user.getRoles().contains(Role.ROLE_USER));
        assertFalse(user.getRoles().contains(Role.ROLE_ADMIN));
        
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
        assertTrue(admin.getRoles().contains(Role.ROLE_USER));
        assertTrue(admin.getRoles().contains(Role.ROLE_ADMIN));
        
        // Mock a session for the project admin user
        projectAdminSession = new MockHttpSession(servletCtx);
        rt = new MockRoundtrip(servletCtx, "/j_spring_security_check", projectAdminSession);
        rt.setParameter("j_username", projectAdmin.getEmailAddress());
        rt.setParameter("j_password", projectAdmin.getPassword());
        rt.execute();
        ctx = (SecurityContext) projectAdminSession
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertNotNull("Spring Security Context was null!", ctx);
        assertEquals(projectAdmin.getEmailAddress(),
                ((UserDetails) ctx.getAuthentication().getPrincipal()).getUsername());
        assertTrue(projectAdmin.getRoles().contains(Role.ROLE_USER));
        assertFalse(projectAdmin.getRoles().contains(Role.ROLE_ADMIN));
        
        Set<Person> admin = new HashSet<Person>();
        admin.add(projectAdmin);
        relService.addAdministratorsToProject(projectOne, admin);
        adminSession.setAttribute("project_id", projectOne.getId());
        
        props = new Properties();
        InputStream in = getClass().getResourceAsStream("/pageText/collection_data_list.properties");
        if (null != in) {
            props.load(in);
            in.close();
        }
        
    }
    
    /**
     * The system-wide admin user should be able to view all of the data in the collection.
     * 
     * @throws Exception
     */
    @Test
    public void testAdminUserCanViewDataList() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, adminSession);
        rt.setParameter("currentCollectionId", collectionWithData.getId());
        rt.execute("renderResults");
        
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        CollectionDataListActionBean underTest = rt.getActionBean(CollectionDataListActionBean.class);
        assertNotNull("The datasets for the collection should not be null", underTest.getCollectionDataSets());
        assertTrue(underTest.getCollectionDataSets().size() > 0);
    }
    
    
    /**
     * Make sure ArchiveService exception is not eaten and is rethrown as CollectionException.
     * 
     * @throws Exception
     */
    @Test(expected = CollectionException.class) 
    public void testThrowsCollectionServiceException() throws Exception {
        CollectionDataListActionBean underTest = new CollectionDataListActionBean();
        underTest.setCurrentCollectionId(collectionWithData.getId());

        // Mock enough to have the archive service throw an exception
        
        DataItemTransportService dts = Mockito.mock(DataItemTransportService.class);
        underTest.injectDataItemTransportService(dts);

        CollectionBizService cbs = Mockito.mock(CollectionBizService.class);
        Collection col = new Collection();
        col.setTitle("Moo!");
        Mockito.when(cbs.getCollection(Mockito.anyString(),
                (Person)Mockito.anyObject())).thenReturn(col);
        
        underTest.injectCollectionBizService(cbs);
        
        ArchiveService as = Mockito.mock(ArchiveService.class);        
        Mockito.when(as.retrieveDataSetsForCollection(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenThrow(new ArchiveServiceException());
        underTest.injectArchiveService(as);
        
        underTest.renderResults();
    }
    
    @Test
    public void testProjectAdminUserCanViewDataList() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, projectAdminSession);
        rt.setParameter("currentCollectionId", collectionWithData.getId());
        rt.execute("renderResults");
        
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        CollectionDataListActionBean underTest = rt.getActionBean(CollectionDataListActionBean.class);
        assertTrue(underTest.getCollectionDataSets().size() > 0);
    }
    
    @Test
    public void testDepositorBlockFromViewingDataList() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, userSession);
        rt.setParameter("currentCollectionId", collectionWithData.getId());
        rt.execute("renderResults");
        
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        CollectionDataListActionBean underTest = rt.getActionBean(CollectionDataListActionBean.class);
        assertTrue(underTest.getCollectionDataSets().size() > 0);
    }
    
    @Test
    public void viewEmptyCollection() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, projectAdminSession);
        rt.setParameter("currentCollectionId", collectionNoData.getId());
        rt.execute("renderResults");
        
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        String errMessage = rt.getActionBean(CollectionDataListActionBean.class).getMessage();
        
        assertTrue(errMessage.equalsIgnoreCase(String.format(props.getProperty("collection_data_list.no-data-items"),
                collectionNoData.getTitle())));
    }
    
    @Test
    public void viewNonExistentCollection() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, adminSession);
        rt.setParameter("currentCollectionId", "-1");
        rt.execute("renderResults");
        
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        String errMessage = rt.getActionBean(CollectionDataListActionBean.class).getMessage();
        assertTrue(errMessage.equalsIgnoreCase(props.getProperty("collection_data_list.no-collection")));
    }
    
    @Test
    public void viewNonExistentPage() throws Exception {
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, projectAdminSession);
        rt.setParameter("currentCollectionId", collectionWithData.getId());
        rt.setParameter("page", "2");
        rt.execute("renderResults");
        
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        String errMessage = rt.getActionBean(CollectionDataListActionBean.class).getMessage();
        assertEquals(
                String.format(props.getProperty("collection_data_list.no-data-items"), collectionWithData.getTitle()),
                errMessage);
    }
    
    @Test
    public void testDataSetsAreSorted() throws Exception {
        final int dataSetCount = 6;
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, projectAdminSession);
        rt.setParameter("currentCollectionId", collectionWithData.getId());
        rt.execute("renderResults");
        
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        
        CollectionDataListActionBean underTest = rt.getActionBean(CollectionDataListActionBean.class);
        List<DataItem> dataSetSubset = underTest.getCollectionDataSets();
        assertNotNull(dataSetSubset);
        int collectionDataSetCount = dataSetSubset.size();
        
        // Deposit another 4 DataSets.
        
        for (int i = 0; i < dataSetCount - collectionDataSetCount; i++) {
            depositDataSet(this.getClass().getName() + ".testDataSetsAreSorted" + i, collectionWithData.getId());
        }
        
        // Total datasets deposited should be 6.
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.listDataSets(null).size() < dataSetCount && pollCount++ < maxPollTimes);
        
        // Get only 2 dataSets at a time, per page
        int step = 2;
        rt.setParameter("maxResultsPerPage", String.valueOf(step));
        
        // Clear out the subsetList
        dataSetSubset.clear();
        
        // A list containing all returned DataSets, added in the order they are retrieved.
        List<DataItem> allDataSets = new ArrayList<DataItem>();
        
        for (int i = 0; i < dataSetCount / step; i++) {
            rt.setParameter("page", String.valueOf(i));
            rt.execute("renderResults");
            
            assertEquals(200, rt.getResponse().getStatus());
            assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
            
            underTest = rt.getActionBean(CollectionDataListActionBean.class);
            dataSetSubset = underTest.getCollectionDataSets();
            assertNotNull(dataSetSubset);
            assertEquals("Expected only " + step + " results per page, but was " + dataSetSubset.size() + " for page "
                    + i, step, dataSetSubset.size());
            
            // DataSets should be sorted with the most recently deposited dataset at the head of the list.
            assertTrue(dataSetSubset.get(0).getDepositDate().compareTo(dataSetSubset.get(1).getDepositDate()) > 0);
            
            allDataSets.addAll(dataSetSubset);
            dataSetSubset.clear();
        }
        
        assertEquals(dataSetCount, allDataSets.size());
        
        for (int i = 1; i < dataSetCount - 1; i++) {
            DataItem newer = allDataSets.get(i - 1);
            DataItem older = allDataSets.get(i);
            assertTrue("Expected DataItem[" + (i - 1) + "] to have a more recent deposit date than DataItem[" + i
                    + "]: " + "[" + (i - 1) + "] " + newer.getDepositDate().toDateTimeISO().toString() + ", " + "[" + i
                    + "] " + older.getDepositDate().toDateTimeISO().toString(),
                    newer.getDepositDate().compareTo(older.getDepositDate()) > 0);
        }
        
    }
    
    @DirtiesDatabase
    @Test
    public void testOldDataDoesntShowUpInCollectionDataList() throws Exception {
        // Find out how many items are in the archive before we start
        int itemsInArchiveBeforeStart = archiveService.listDataSets(DEPOSITED).size();
        
        // Get the collection deposit ID
        final String parentDepositId = archiveService
                .listDepositInfo(collectionWithData.getId(), ArchiveDepositInfo.Status.DEPOSITED).get(0).getDepositId();
        
        // Create a new dataset and data file and add it to the dataset
        final String file1name = "testOldDataDoesntShowUpInCollectionDataListFile1";
        File tmp1 = File.createTempFile(file1name, null);
        tmp1.deleteOnExit();
        
        DataItem dataSet1 = new DataItem();
        dataSet1.setName(tmp1.getName());
        final String dataSetId = "testOldDataDoesntShowUpInCollectionDataListDataSet";
        dataSet1.setId(dataSetId);
        
        DataFile dataFile1 = new DataFile();
        final String dataFile1Id = "testOldDataDoesntShowUpInCollectionDataListDataFile1";
        dataFile1.setId(dataFile1Id);
        dataFile1.setSource(tmp1.toURI().toURL().toExternalForm());
        dataFile1.setName(tmp1.getName());
        dataFile1.setPath(tmp1.getPath());
        
        UrlResource r = new UrlResource(dataFile1.getSource());
        dataFile1.setSize(r.contentLength());
        
        dataSet1.addFile(dataFile1);
        
        relService.addDataSetToCollection(dataSet1, collectionWithData);
        
        archiveService.deposit(parentDepositId, dataSet1);
        
        // Poll the archive to make sure the deposit completed
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.listDataSets(DEPOSITED).size() < itemsInArchiveBeforeStart + 1
                && pollCount++ < maxPollTimes);
        
        // Perform a round trip on the collection data list
        MockRoundtrip rt = new MockRoundtrip(servletCtx, CollectionDataListActionBean.class, projectAdminSession);
        rt.setParameter("currentCollectionId", collectionWithData.getId());
        rt.execute("renderResults");
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        
        CollectionDataListActionBean underTest = rt.getActionBean(CollectionDataListActionBean.class);
        
        // Grab the collection size before the update (but after the deposit)
        int collectionSizeBefore = underTest.getCollectionDataSets().size();
        
        // Add an update to that data item to the collection.
        final String file2name = "testOldDataDoesntShowUpInCollectionDataListFile2";
        File tmp2 = File.createTempFile(file2name, null);
        tmp2.deleteOnExit();
        
        // Create a new dataset with the same ID and a different data file and add it to the dataset
        DataItem dataSet2 = new DataItem();
        dataSet2.setName(tmp2.getName());
        dataSet2.setId(dataSetId);
        
        DataFile dataFile2 = new DataFile();
        final String dataFile2Id = "testOldDataDoesntShowUpInCollectionDataListDataFile2";
        dataFile2.setId(dataFile2Id);
        dataFile2.setSource(tmp2.toURI().toURL().toExternalForm());
        dataFile2.setName(tmp2.getName());
        dataFile2.setPath(tmp2.getPath());
        
        r = new UrlResource(dataFile2.getSource());
        dataFile2.setSize(r.contentLength());
        
        dataSet2.addFile(dataFile2);
        
        relService.addDataSetToCollection(dataSet2, collectionWithData);
        
        archiveService.deposit(parentDepositId, dataSet2);
        
        // Poll the archive to make sure the deposit completed
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.listDataSets(DEPOSITED).size() < itemsInArchiveBeforeStart + 2
                && pollCount++ < maxPollTimes);
        
        // Perform a round trip on the collection data list
        rt.setParameter("currentCollectionId", collectionWithData.getId());
        rt.execute("renderResults");
        assertEquals(200, rt.getResponse().getStatus());
        assertEquals("/pages/collection_data_list.jsp", rt.getDestination());
        
        // Grab the collection size after the update.
        int collectionSizeAfter = underTest.getCollectionDataSets().size();
        
        // Make sure that the collection has not grown.
        assertEquals(collectionSizeBefore, collectionSizeAfter);
        
        // Make sure that dataFile2 is in the list
        int dataFileInstances = 0;
        for (DataItem dataItem : underTest.getCollectionDataSets()) {
            for (DataFile file : dataItem.getFiles()) {
                if (file.getName().equals(dataFile2.getName())) {
                    ++dataFileInstances;
                }
            }
        }
        assertEquals(1, dataFileInstances);
        
        // Make sure that dataFile1 is not in the list
        for (DataItem dataItem : underTest.getCollectionDataSets()) {
            for (DataFile file : dataItem.getFiles()) {
                assertFalse(file.getName().equals(dataFile1.getName()));
            }
        }
    }
    
    private void depositDataSet(String dataSetId, String collectionId) throws Exception {
        
        final int currentDataSetCount = archiveService.listDataSets(null).size();
        final int expectedDataSetCount = currentDataSetCount + 1;
        
        File tmp = File.createTempFile("metadata", null);
        tmp.deleteOnExit();
        
        PrintWriter out = new PrintWriter(tmp);
        
        out.println(data_string);
        out.close();
        
        DataItem ds = new DataItem();
        ds.setName(tmp.getName());
        ds.setId(dataSetId);
        
        MockRoundtrip depositRoundTrip = new MockRoundtrip(servletCtx, DepositActionBean.class, adminSession);
        depositRoundTrip.addParameter("currentCollectionId", collectionId);
        depositRoundTrip.addParameter("uploadedFile", tmp.getPath());
        // Making sure the page is redirected properly after the deposit.
        depositRoundTrip.addParameter("redirectUrl", UserCollectionsActionBean.HOME_COLLECTIONS_PATH);
        
        depositRoundTrip.execute("deposit");
        
        assertEquals("Expected a dataset to be deposited, but there is an incorrect number of datasets",
                expectedDataSetCount, archiveService.listDataSets(null).size());
        
        pollCount = 0;
        do {
            archiveService.pollArchive();
            Thread.sleep(1000L);
        }
        while (archiveService.listDataSets(DEPOSITED).size() < expectedDataSetCount && pollCount++ < maxPollTimes);
    }
}
