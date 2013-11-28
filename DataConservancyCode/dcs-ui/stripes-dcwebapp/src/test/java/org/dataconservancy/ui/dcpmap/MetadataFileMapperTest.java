package org.dataconservancy.ui.dcpmap;

import java.io.File;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.profile.MetadataFileProfile;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.registry.impl.metadata.shared.DcsMetadataFormat;
import org.dataconservancy.ui.BaseUnitTest;
import org.dataconservancy.ui.exceptions.DcpMappingException;
import org.dataconservancy.ui.model.MetadataFile;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.MetadataFileBusinessObjectSearcher;
import org.dataconservancy.ui.services.MetadataFormatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import static org.dataconservancy.ui.util.MappingUtil.assertIsSuccessor;
import static org.dataconservancy.ui.util.MappingUtil.findDcsFile;
import static org.dataconservancy.ui.util.MappingUtil.getRootDuFromCollectionDcp;
import static org.dataconservancy.ui.util.MappingUtil.getRootDuFromMetadataFileDcp;
import static org.dataconservancy.ui.util.MappingUtil.getStateDuFromCollectionDcp;
import static org.dataconservancy.ui.util.MappingUtil.getStateDuFromMetadataFileDcp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetadataFileMapperTest extends BaseUnitTest {

    @Autowired
    private DcsModelBuilder builder;

    private MetadataFileBusinessObjectSearcher boSearcher = mock(MetadataFileBusinessObjectSearcher.class);

    private MetadataFormatService formatService = mock(MetadataFormatService.class);

    private MetadataFileMapper underTest;

    @Before
    public void setUp() {
        underTest = new MetadataFileMapper(boSearcher);
    }
    
    /**
     * Tests the
     * {@link MetadataFileMapper#toDcp(String, org.dataconservancy.ui.model.MetadataFile)}
     * and {@link MetadataFileMapper#fromDcp(org.dataconservancy.model.dcp.Dcp)}.
     * <p/>
     * The Mapper is used to map a MetadataFile (e.g. mf) to a DCP and from a DCP
     * back to a MetadataFile (e.g. mf'). MetadataFile <em>mf</em> and <em>mf'</em> should
     * be equal according to {@link MetadataFile#equals(Object)}.
     * 
     * @throws Exception
     */
    @Test
    public void testMappingRoundTrip() throws Exception {     

        // Perform the mapping of the MetadataFile to a DCP package.
        final Dcp mappedDcp = underTest.toDcp(metadataFileOne.getParentId(), metadataFileOne);
        assertNotNull("Expected a DCP to be generated!", mappedDcp);

        // Map from the generated DCP back to the DataItem
        final MetadataFile roundTripMf = underTest.fromDcp(mappedDcp);
        assertNotNull("Expected a DataItem to be generated!", roundTripMf);

        // The round-tripped MetadataFile should be equal to the original DataItem
        assertEquals("MetadataFile to DCP to MetadataFile' mapping failed!", metadataFileOne,
                roundTripMf);
    }

    @Test
    public void testMappingToDcp() throws Exception {      

        final String collectionToDepositTo = metadataFileOne.getParentId();
        metadataFileOne.setPath(System.getProperty("java.io.tmpdir"));
        final Dcp dcp = underTest.toDcp(collectionToDepositTo, metadataFileOne);
        assertNotNull("Expected a DCP to be produced by the MetadataFileMapper!",
                dcp);
               
        DcsDeliverableUnit rootDu = getRootDuFromMetadataFileDcp(dcp);
        DcsDeliverableUnit stateDu = getStateDuFromMetadataFileDcp(dcp, MetadataFileProfile.STATE_DU_TYPE);
        
        //Both the state and root du should have former external refs for the metadata file business object
        assertTrue(rootDu.getFormerExternalRefs().contains(metadataFileOne.getId()));
        assertTrue(stateDu.getFormerExternalRefs().contains(metadataFileOne.getId()));
        
        //Make sure the state du has a parent reference to the root
        assertTrue(stateDu.getParents().contains(new DcsDeliverableUnitRef(rootDu.getId())));
        
        //The state du should have an is metadata for relationship with the collection
        assertTrue(stateDu.getRelations().contains(new DcsRelation(DcsRelationship.IS_METADATA_FOR, collectionToDepositTo)));

        final Collection<DcsManifestation> manifestations = dcp.getManifestations();
        assertEquals(2, manifestations.size());
        for (DcsManifestation man : manifestations) {
            assertEquals(stateDu.getId(), man.getDeliverableUnit());
            assertTrue(man.getManifestationFiles().size() > 0);
        }
        
        
        //Assert there are two dcs file included in the deposit one for the serialized metadata object and one for the file contents
        final Collection<DcsFile> files = dcp.getFiles();
        assertEquals(2, files.size());
        
        //Assert the file name matches the metadata file name
        ArrayList<String> fileNames = new ArrayList<String>();
        for (DcsFile file : files) {
            fileNames.add(file.getName());
        }
        assertTrue(fileNames.contains(metadataFileOne.getName()));
    }

    @Test(expected = DcpMappingException.class)
    public void testMapMetadataFileWithNoIdentifier() throws Exception {
        // Compose a MetadataFile with no Identifier
        metadataFileOne.setId(null);

        underTest.toDcp("parent-id", metadataFileOne);
    }

    @Test(expected = DcpMappingException.class)
    public void testMapDatasetWithNoName() throws Exception {
        metadataFileOne.setName(null);
        underTest.toDcp("parent-id", metadataFileOne);
    }

    public void testMappingWithMoreMetadataFileAttributesSet() throws Exception {
        
        MetadataFile mf = new MetadataFile();
        mf.setFormat("jpg");
        mf.setName("banana.jpg");

        File tmp = File.createTempFile("image", ".jpg");
        tmp.deleteOnExit();

        FileWriter out = new FileWriter(tmp);
        out.write("stuff");
        out.close();

        mf.setSource(tmp.toURI().toURL().toExternalForm());
        mf.setMetadataFormatId("image/jpg");
        mf.setId("id/121213");
        
        DcsMetadataFormat fmt = formatService.getMetadataFormats().iterator().next();
        
        MetadataFile mfTwo = new MetadataFile();
        mfTwo.setFormat("png");
        mfTwo.setName("banana.png");

        File tmpTwo = File.createTempFile("image", ".png");
        tmpTwo.deleteOnExit();

        FileWriter outTwo = new FileWriter(tmpTwo);
        outTwo.write("stuffagain");
        outTwo.close();

        mfTwo.setSource(tmpTwo.toURI().toURL().toExternalForm());
        mfTwo.setMetadataFormatId(fmt.getId());
        mfTwo.setId("id/b2323n");

        MetadataFileMapper mapper = new MetadataFileMapper(mock(MetadataFileBusinessObjectSearcher.class));

        Dcp dcp = mapper.toDcp("col1", mf);
        Dcp dcp2 = mapper.toDcp("col1", mfTwo);

        assertNotNull(dcp);
        assertTrue(dcp.getDeliverableUnits().size() > 0);
        DcsDeliverableUnit du = dcp.getDeliverableUnits().iterator().next();

        assertTrue(du.getAlternateIds().size() > 0);

        assertTrue(dcp.getManifestations().size() > 0);
        assertTrue(dcp.getFiles().size() > 0);

        MetadataFile test = mapper.fromDcp(dcp);
        MetadataFile testTwo = mapper.fromDcp(dcp2);
        
        assertNotNull(test);

        assertEquals(mf, test);
        assertEquals(mfTwo, testTwo);
    }
    
    @Test
    public void testMappingUpdateKeepingFile() throws Exception {

        final String collectionId = metadataFileOne.getParentId();
        final Dcp dcp1 = underTest.toDcp(collectionId, metadataFileOne);

        // This searcher will return null for any business object state, which is correct
        // behavior when no objects representing metadata files are in the archive.
        MetadataFileBusinessObjectSearcher searcher = mock(MetadataFileBusinessObjectSearcher.class);
        MetadataFileMapper underTest = new MetadataFileMapper(searcher);
        
        
        final MetadataFile mf2 = new MetadataFile();
        mf2.setName("metadataFile Two");
        mf2.setId(metadataFileOne.getId());
        mf2.setPath(dataFileOne.getPath());
        mf2.setParentId(metadataFileOne.getParentId());

        // This searcher will return a BusinessObjectState for Collection 1.  This is correct
        // behavior, and mimics what would happen if Collection 1 had been properly mapped and
        // deposited in the archive.
        when(searcher.findLatestState(metadataFileOne.getId())).thenReturn(new BusinessObjectState() {
            @Override
            public DcsDeliverableUnit getRoot() {
                return getRootDuFromCollectionDcp(dcp1);
            }

            @Override
            public DcsDeliverableUnit getLatestState() {
                return getStateDuFromCollectionDcp(dcp1, MetadataFileProfile.STATE_DU_TYPE);
            }
        });
        
        DcsFile dcs_file = null;
        for (DcsFile file : dcp1.getFiles()) {
            if (file.getName().equalsIgnoreCase("MetadataOne")) {
                dcs_file = file;
            }
        }
        when(searcher.findMetadataFile(metadataFileOne.getId())).thenReturn(dcs_file);
        
        final Dcp dcp2 = underTest.toDcp(collectionId, mf2);

        assertIsSuccessor(dcp2, dcp1, MetadataFileProfile.STATE_DU_TYPE);

        DcsFile dcsfile1 = findDcsFile(dcp1, metadataFileOne.getName());
        assertNotNull(dcsfile1);
        
        DcsFile dcsfile2 = findDcsFile(dcp2, metadataFileOne.getName());
        assertNull(dcsfile2);
        
        dcp2.addFile(dcsfile1);

        
        MetadataFile test_mf2 = underTest.fromDcp(dcp2);
        
        //Update mf2 to contain the parameters that it will pick up from metadataFileOne in the mapping.
        mf2.setSource(metadataFileOne.getSource());
        mf2.setMetadataFormatId(metadataFileOne.getMetadataFormatId());
        assertEquals(mf2, test_mf2);
    }
    
    @Test
    public void testMappingUpdateReplacingFile() throws Exception {
             
        final MetadataFile file = new MetadataFile();
        final String parentId = "parent";
        file.setParentId(parentId);
        file.setId("file1");
        file.setName("file1");
        
        java.io.File tmp = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        file.setSource(tmp.toURI().toString());
        file.setPath(System.getProperty("java.io.tmpdir"));
        
        Resource r = new UrlResource(file.getSource());
        file.setSize(r.contentLength());

        MetadataFileBusinessObjectSearcher searcher = mock(MetadataFileBusinessObjectSearcher.class);
        MetadataFileMapper underTest = new MetadataFileMapper(searcher);

        final Dcp dcp1 = underTest.toDcp(file.getParentId(), file);

        // Add ds2 with same business id as ds1 and new file file2
        final MetadataFile file2 = new MetadataFile();
        file2.setId("file2");
        file2.setName("file2");
        file2.setParentId(parentId);
        
        java.io.File tmp2 = java.io.File.createTempFile(this.getClass()
                .getName() + "-", ".txt");
        tmp.deleteOnExit();
        file2.setSource(tmp2.toURI().toString());
        file2.setPath(System.getProperty("java.io.tmpdir"));
        
        Resource r2 = new UrlResource(file.getSource());
        file2.setSize(r2.contentLength());
        
        when(searcher.findLatestState(file2.getId())).thenReturn(new BusinessObjectState() {
            @Override
            public DcsDeliverableUnit getRoot() {
                return getRootDuFromCollectionDcp(dcp1);
            }

            @Override
            public DcsDeliverableUnit getLatestState() {
                return getStateDuFromCollectionDcp(dcp1, MetadataFileProfile.STATE_DU_TYPE);
            }
        });

        final Dcp dcp2 = underTest.toDcp(file2.getParentId(), file2);

        assertIsSuccessor(dcp2, dcp1, MetadataFileProfile.STATE_DU_TYPE);
        
        DcsFile dcsfile1 = findDcsFile(dcp1, file.getName());
        DcsFile dcsfile2 = findDcsFile(dcp2, file2.getName());
        
        assertNotNull(dcsfile1);
        assertNotNull(dcsfile2);
        
        assertNull(findDcsFile(dcp1, file2.getName()));
        assertNull(findDcsFile(dcp2, file.getName()));
        
        // Make sure we can retrieve col2
        
        MetadataFile test_mf2 = underTest.fromDcp(dcp2);
        
        assertEquals(file2, test_mf2);
    }

    @Test
    public void testMapMetadataFileDescribesProject() throws Exception {
        final String mdfId = "mdf:1";
        final String projectId = "project:1";

        final Project project = new Project();
        project.setId(projectId);
        project.setName("Sample Project");
        project.setDescription("A sample project, described by " + mdfId);

        final File mdfFile = File.createTempFile("MetadataFileMapperTest-", ".txt");
        mdfFile.deleteOnExit();
        FileUtils.writeStringToFile(mdfFile, "This is a MdF.");

        final MetadataFile mdf = new MetadataFile();
        mdf.setFormat("application/octet-stream");
        mdf.setPath(mdfFile.getPath());
        mdf.setSize(mdfFile.length());
        mdf.setParentId(projectId);
        mdf.setId(mdfId);
        mdf.setName(mdfFile.getName());
        mdf.setSource(mdfFile.getPath());

        Dcp dcp = underTest.toDcp(projectId, mdf);

        DcsDeliverableUnit mdfDu = null;

        for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
            if (!du.getFormerExternalRefs().contains(mdfId)) {
                continue;
            }

            mdfDu = du;
            break;
        }

        assertNotNull(mdfDu);
        assertFalse(mdfDu.getRelations().isEmpty());

        final DcsRelation expectedRelation = new DcsRelation(DcsRelationship.IS_METADATA_FOR, projectId);
        assertTrue(mdfDu.getRelations().contains(expectedRelation));
    }

    @Test
    public void testMapMetadataFileNullArchiveParent() throws Exception {
        final String mdfId = "mdf:1";
        final String projectId = "project:1";

        final Project project = new Project();
        project.setId(projectId);
        project.setName("Sample Project");
        project.setDescription("A sample project, described by " + mdfId);

        final File mdfFile = File.createTempFile("MetadataFileMapperTest-", ".txt");
        mdfFile.deleteOnExit();
        FileUtils.writeStringToFile(mdfFile, "This is a MdF.");

        final MetadataFile mdf = new MetadataFile();
        mdf.setFormat("application/octet-stream");
        mdf.setPath(mdfFile.getPath());
        mdf.setSize(mdfFile.length());
        mdf.setParentId(projectId);
        mdf.setId(mdfId);
        mdf.setName(mdfFile.getName());
        mdf.setSource(mdfFile.getPath());

        Dcp dcp = underTest.toDcp(null, mdf);

        DcsDeliverableUnit mdfDu = null;

        for (DcsDeliverableUnit du : dcp.getDeliverableUnits()) {
            if (!du.getFormerExternalRefs().contains(mdfId)) {
                continue;
            }

            mdfDu = du;
            break;
        }

        assertNotNull(mdfDu);
        assertTrue(mdfDu.getRelations().isEmpty());
    }

}