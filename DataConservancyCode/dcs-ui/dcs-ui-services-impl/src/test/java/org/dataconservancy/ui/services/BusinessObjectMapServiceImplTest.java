package org.dataconservancy.ui.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.model.*;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.util.ArchiveSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

public class BusinessObjectMapServiceImplTest extends BaseUnitTest {
    private BusinessObjectMapService map_service;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private RelationshipService rel_service;

    @Before
    public void setup() {
        map_service = new BusinessObjectMapServiceImpl(archiveService,
                rel_service);
    }

    /**
     * Test generating a map from content in the archive.
     * Case: the entire object graph is in the archive
     */
    @Test
    public void testGenerateMap() throws Exception {
        // Setup a business object hierarchy

        Project proj = projectOne;

        Collection col = collectionOne;
        Collection col2 = collectionWithData;

        BusinessObjectMap expected = new BusinessObjectMap(proj);
        expected.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap expected_col1 = new BusinessObjectMap(col);
        expected_col1.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap expected_col2 = new BusinessObjectMap(col2);
        expected_col2.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());

        BusinessObjectMap dataFileOneMap = new BusinessObjectMap(dataFileOne);
        dataFileOneMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap dataItemOneMap = new BusinessObjectMap(dataItemOne);
        dataItemOneMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        dataItemOneMap.getChildren().add(dataFileOneMap);

        BusinessObjectMap dataFileTwoMap = new BusinessObjectMap(dataFileTwo);
        dataFileTwoMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap dataItemTwoMap = new BusinessObjectMap(dataItemTwo);
        dataItemTwoMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        dataItemTwoMap.getChildren().add(dataFileTwoMap);

        expected_col2.getChildren().add(dataItemTwoMap);
        expected_col2.getChildren().add(dataItemOneMap);

        BusinessObjectMap mdMap1 = new BusinessObjectMap(metadataFileOne);
        mdMap1.setDepositStatus("DEPOSITED");
        expected_col2.getChildren().add(mdMap1);
        BusinessObjectMap mdMap2 = new BusinessObjectMap(metadataFileTwo);
        mdMap2.setDepositStatus("DEPOSITED");
        expected_col2.getChildren().add(mdMap2);

        expected_col1.getAlternateIds().add("moo");
        expected.getChildren().add(expected_col1);
        expected.getChildren().add(expected_col2);

        Map<String, List<String>> alt_ids = new HashMap<String, List<String>>();
        alt_ids.put(col.getId(), Arrays.asList("moo"));

        BusinessObjectMap map = map_service.generateMap(proj, alt_ids, true);

        assertEquals(expected, map);
    }

    @Test
    public void testGenerateMapWithFailedObjects() throws ArchiveServiceException, InterruptedException {
        archiveService = mock(ArchiveService.class);

        //Mock archive service to return a metadata file that failed deposit, along with other items
        ArchiveDepositInfo collectionADI = new ArchiveDepositInfo();
        collectionADI.setDepositId("bogus/deposit/id");
        collectionADI.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED);
        List<ArchiveDepositInfo> adisList = new ArrayList<ArchiveDepositInfo>();
        adisList.add(collectionADI);
        when(archiveService.listDepositInfo(collectionOne.getId(), null)).thenReturn(adisList);
        List<Collection> collections = new ArrayList<Collection>();
        collections.add(collectionOne);
        ArchiveSearchResult result = new ArchiveSearchResult(collections, 1);
        when(archiveService.retrieveCollection(collectionOneDepositID)).thenReturn(result);


        ArchiveDepositInfo collectionWithDataADI = new ArchiveDepositInfo();
        collectionWithDataADI.setDepositId(collectionWithDataDepositID);
        collectionWithDataADI.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED);
        adisList = new ArrayList<ArchiveDepositInfo>();
        adisList.add(collectionWithDataADI);
        when(archiveService.listDepositInfo(collectionWithData.getId(), null)).thenReturn(adisList);
         collections = new ArrayList<Collection>();
        collections.add(collectionWithData);
         result = new ArchiveSearchResult(collections, 1);
        when(archiveService.retrieveCollection(collectionWithDataDepositID)).thenReturn(result);


        ArchiveDepositInfo metadataFileOneADI = new ArchiveDepositInfo();
        metadataFileOneADI.setDepositId(metadataFileOneDepositID);
        metadataFileOneADI.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED);
        adisList = new ArrayList<ArchiveDepositInfo>();
        adisList.add(metadataFileOneADI);
        when(archiveService.listDepositInfo(metadataFileOne.getId(), null)).thenReturn(adisList);
        List<MetadataFile> metadataFiles = new ArrayList<MetadataFile>();
        metadataFiles.add(metadataFileOne);
        result = new ArchiveSearchResult(metadataFiles, 1);
        when(archiveService.retrieveMetadataFile(metadataFileOneDepositID)).thenReturn(result);

        ArchiveDepositInfo metadataFileTwoADI = new ArchiveDepositInfo();
        metadataFileTwoADI.setDepositId(metadataFileTwoDepositID);
        metadataFileTwoADI.setDepositStatus(ArchiveDepositInfo.Status.FAILED);
        adisList = new ArrayList<ArchiveDepositInfo>();
        adisList.add(metadataFileTwoADI);
        when(archiveService.listDepositInfo(metadataFileTwo.getId(), null)).thenReturn(adisList);
        metadataFiles = new ArrayList<MetadataFile>();
        metadataFiles.add(metadataFileTwo);
        result = new ArchiveSearchResult(metadataFiles, 1);
        when(archiveService.retrieveMetadataFile(metadataFileTwoDepositID)).thenReturn(result);

        ArchiveDepositInfo dataItemOneADI = new ArchiveDepositInfo();
        dataItemOneADI.setDepositId(dataItemOneDepositID);
        dataItemOneADI.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED);
        adisList = new ArrayList<ArchiveDepositInfo>();
        adisList.add(metadataFileOneADI);
        when(archiveService.listDepositInfo(dataItemOne.getId(), null)).thenReturn(adisList);

        ArchiveDepositInfo dataItemTwoADI = new ArchiveDepositInfo();
        dataItemTwoADI.setDepositId(dataItemTwoDepositID);
        dataItemTwoADI.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED);
        adisList = new ArrayList<ArchiveDepositInfo>();
        adisList.add(metadataFileOneADI);
        when(archiveService.listDepositInfo(dataItemTwo.getId(), null)).thenReturn(adisList);
        List<DataItem> dataItems= new ArrayList<DataItem>();
        dataItems.add(dataItemTwo);
        dataItems.add(dataItemOne);
        result = new ArchiveSearchResult(dataItems, 1);
        when(archiveService.retrieveDataSetsForCollection(collectionWithDataDepositID, -1, 0)).thenReturn(result);

        map_service = new BusinessObjectMapServiceImpl(archiveService, rel_service);

        //Set up expected map with a FAILED object map.
        BusinessObjectMap expected = new BusinessObjectMap(projectOne);
        expected.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap expected_col1 = new BusinessObjectMap(collectionOne);
        expected_col1.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap expected_col2 = new BusinessObjectMap(collectionWithData);
        expected_col2.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());

        BusinessObjectMap dataFileOneMap = new BusinessObjectMap(dataFileOne);
        dataFileOneMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap dataItemOneMap = new BusinessObjectMap(dataItemOne);
        dataItemOneMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        dataItemOneMap.getChildren().add(dataFileOneMap);

        BusinessObjectMap dataFileTwoMap = new BusinessObjectMap(dataFileTwo);
        dataFileTwoMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        BusinessObjectMap dataItemTwoMap = new BusinessObjectMap(dataItemTwo);
        dataItemTwoMap.setDepositStatus(ArchiveDepositInfo.Status.DEPOSITED.toString());
        dataItemTwoMap.getChildren().add(dataFileTwoMap);

        expected_col2.getChildren().add(dataItemTwoMap);
        expected_col2.getChildren().add(dataItemOneMap);

        BusinessObjectMap mdMap1 = new BusinessObjectMap(metadataFileOne);
        mdMap1.setDepositStatus("DEPOSITED");
        expected_col2.getChildren().add(mdMap1);
        BusinessObjectMap mdMap2 = new BusinessObjectMap(metadataFileTwo);
        mdMap2.setDepositStatus("FAILED");
        mdMap2.setName(null);
        expected_col2.getChildren().add(mdMap2);

        expected_col1.getAlternateIds().add("moo");
        expected.getChildren().add(expected_col1);
        expected.getChildren().add(expected_col2);

        Map<String, List<String>> alt_ids = new HashMap<String, List<String>>();
        alt_ids.put(collectionOne.getId(), Arrays.asList("moo"));

        BusinessObjectMap map = map_service.generateMap(projectOne, alt_ids, true);
        assertEquals(expected, map);
    }

    /**
     * Test generating a map from content in the archive.
     * Case: - collection as the top object of the graph.
     *       - One of the referred-to object is not in the archive when it should be.
     * Expects:
     *       - ArchiveServiceException thrown
     *       
     */
    @Test (expected = ArchiveServiceException.class)
    public void testGenerateMapMissingObjectInGraph() throws Exception {
        // Setup a business object hierarchy

        Collection col2 = collectionWithData;
        Collection missingCollection = new Collection();
        missingCollection.setId("missingCollection/Id");
        missingCollection.setTitle("Missing collection title");
        missingCollection.setSummary("Missing collection summary");

        //add missing collection as child of collection2, but missing collection BOMap is not added to the
        //expected BOMap
        col2.addChildId(missingCollection.getId());

        BusinessObjectMap dataFileOneMap = new BusinessObjectMap(dataFileOne);
        BusinessObjectMap dataItemOneMap = new BusinessObjectMap(dataItemOne);
        dataItemOneMap.getChildren().add(dataFileOneMap);

        BusinessObjectMap dataFileTwoMap = new BusinessObjectMap(dataFileTwo);
        BusinessObjectMap dataItemTwoMap = new BusinessObjectMap(dataItemTwo);
        dataItemTwoMap.getChildren().add(dataFileTwoMap);

        Map<String, List<String>> alt_ids = new HashMap<String, List<String>>();

        map_service.generateMap(collectionWithData, alt_ids, true);
    }

    /**
     * Test generating a map from content in the archive.
     * Case: - collection as the top object of the graph.
     *       - the collection itself is not in the archive.
     *
     * Expects:
     *       - ArchiveServiceException thrown
     */
    @Test (expected = ArchiveServiceException.class)
    public void testGenerateMapMissingTopLevelCollection() throws Exception {

        Collection missingCollection = new Collection();
        missingCollection.setId("missingCollection/Id");
                
        missingCollection.setTitle("Missing collection title");
        missingCollection.setSummary("Missing collection summary");

        Collection missingSubCollection = new Collection();
        missingSubCollection.setId("blah");
        missingSubCollection.setParentId(missingCollection.getId());
        missingCollection.getChildrenIds().add(missingSubCollection.getId());
        missingSubCollection.setTitle("Sub collection title.");
        missingSubCollection.setSummary("Sub collection summary");

        Map<String, List<String>> alt_ids = new HashMap<String, List<String>>();

        map_service.generateMap(missingCollection, alt_ids, true);
    }

    /**
     * Make sure well formed xml is generated and just do a few spot checks on
     * the format. When there is a schema that can be used for testing.
     * 
     * @throws Exception
     */
    @Test
    public void testWriteXml() throws Exception {
        BusinessObjectMap root = new BusinessObjectMap("root");
        root.setName("root name");
        root.setType("Project");
        root.setDepositStatus("Success");
        root.getAlternateIds().add("mootastic");

        BusinessObjectMap child = new BusinessObjectMap("child");
        child.setName("child name");
        child.setType("Collection");
        child.setDepositStatus("Success");

        root.getChildren().add(child);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        map_service.writeXmlMap(root, bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(bis);

        assertEquals("bo", doc.getDocumentElement().getNodeName());
        assertEquals(2, doc.getElementsByTagName("id").getLength());
        assertEquals(2, doc.getElementsByTagName("name").getLength());
        assertEquals(2, doc.getElementsByTagName("type").getLength());
        assertEquals(2, doc.getElementsByTagName("depositStatus").getLength());
        assertEquals(1, doc.getElementsByTagName("alternateid").getLength());
    }

    /**
     * Make sure the method runs without an exception. Do some simplistic checks
     * on the html.
     * 
     * @throws Exception
     */
    @Test
    public void testWriteHtml() throws Exception {
        BusinessObjectMap root = new BusinessObjectMap("root");
        root.setName("root name");
        root.setType("Project");
        root.setDepositStatus("Success");
        root.getAlternateIds().add("mootastic");

        BusinessObjectMap child = new BusinessObjectMap("child");
        child.setName("child name");
        child.setType("Collection");
        child.setDepositStatus("Success");
        root.getChildren().add(child);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        map_service.writeHtmlMap(root, bos);
        String html = bos.toString("UTF-8");

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains(root.getId()));
        assertTrue(html.contains(root.getType()));
        assertTrue(html.contains(root.getName()));
        assertTrue(html.contains(root.getDepositStatus()));
        assertTrue(html.contains(root.getAlternateIds().get(0)));

        assertTrue(html.contains("<html>"));
        assertTrue(html.contains(child.getId()));
        assertTrue(html.contains(child.getType()));
        assertTrue(html.contains(child.getDepositStatus()));
        assertTrue(html.contains(child.getName()));
    }
}
