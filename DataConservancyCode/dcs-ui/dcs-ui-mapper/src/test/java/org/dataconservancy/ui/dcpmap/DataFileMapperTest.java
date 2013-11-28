/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.ui.dcpmap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcp.DcpModelVersion;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.Id;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.model.builder.xstream.XstreamBusinessObjectBuilder;
import org.dataconservancy.ui.model.builder.xstream.XstreamBusinessObjectFactory;
import org.dataconservancy.ui.profile.DataFileProfile;
import org.dataconservancy.ui.services.BusinessObjectState;
import org.dataconservancy.ui.services.DataFileBusinessObjectSearcher;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.dataconservancy.ui.dcpmap.AbstractVersioningMapper.STATE_MANIFESTATION_TYPE;
import static org.dataconservancy.ui.dcpmap.MapperAssertion.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the DataFileMapper
 */
public class DataFileMapperTest {

    private static final String ROOT_DU_TYPE = DataFileProfile.DATAFILE_ROOTDU_TYPE;

    private static final String STATE_DU_TYPE = DataFileProfile.DATAFILE_STATEDU_TYPE;

    private static final String DATAFILE_MAN_TYPE = DataFileProfile.DATAFILE_MANIFESTATION_TYPE;

    private static final String DCS_FILE_DATAFILE_ID = "datafile0";

    private static final String DATAFILE_ID_TYPE = "DATA_FILE";

    private static final String DU_TITLE = "title";

    private static final String ROOT_DU_TEMPLATE =
            "<DeliverableUnit xmlns=\"" + DcpModelVersion.VERSION_1_0.getXmlns() + "\" id=\"%s\" lineageId=\"%s\">\n" +
            "  <parent>%s</parent>\n" +
            "  <type>" + ROOT_DU_TYPE + "</type>\n" +
                    "  <" + DU_TITLE + ">title</title>\n" +
            "  <formerExternalRef>%s</formerExternalRef>\n" +
            "</DeliverableUnit>";

    private static final String STATE_DU_TEMPLATE =
                "<DeliverableUnit xmlns=\"" + DcpModelVersion.VERSION_1_0.getXmlns() + "\" id=\"%s\" lineageId=\"%s\">\n" +
                "  <parent>%s</parent>\n" +
                "  <type>" + STATE_DU_TYPE + "</type>\n" +
                        "  <" + DU_TITLE + ">title</title>\n" +
                "  <formerExternalRef>%s</formerExternalRef>\n" +
                "</DeliverableUnit>";

    private HierarchicalPrettyPrinter hpp = new HierarchicalPrettyPrinter();

    private DcsModelBuilder dmb = new DcsXstreamStaxModelBuilder();

    private DataFileMapper underTest;

    private DataFileBusinessObjectSearcher searcher;

    private BusinessObjectBuilder bob;

    @Before
    public void setUp() throws Exception {
        XstreamBusinessObjectFactory xstreamFactory = new XstreamBusinessObjectFactory();
        bob = new XstreamBusinessObjectBuilder(xstreamFactory.createInstance());
        searcher = mock(DataFileBusinessObjectSearcher.class);

        underTest = new DataFileMapper(bob, searcher);
    }

    /**
     * Mocks the scenario when creating a DCP for a DataFile that is <em>not</em> in the archive.
     *
     * @throws Exception
     */
    @Test
    public void testToDcpWithNoArchiveStateAndLocalFile() throws Exception {
        final String archiveParentEntityId = "archive:dataitem:1";
        final String businessDataFileId = "business:datafile:1";

        final File sourceFile = createTemp();
        final DataFile df = new DataFile();
        df.setSize(sourceFile.length());
        df.setSource(sourceFile.getPath());
        df.setFormat("application/text");
        df.setId(businessDataFileId);
        df.setName(sourceFile.getName());

        when(searcher.findLatestState(businessDataFileId)).thenReturn(null);

        final Dcp dcp = underTest.toDcp(archiveParentEntityId, df);
        assertNotNull(dcp);

//        for (DcsEntity e : dcp) {
//            e.toString(hpp);
//        }
//
//        System.out.println(hpp.toString());

        final DcsDeliverableUnit rootDu = assertRootDuPresent(dcp);
        final DcsDeliverableUnit stateDu = assertSingleStateDuPresent(dcp, STATE_DU_TYPE);
        assertFormerRef(stateDu, businessDataFileId);
        assertHasParentRef(stateDu, rootDu.getId());
        assertHasSingleManifestation(stateDu.getId(), STATE_MANIFESTATION_TYPE, dcp);
        final DcsManifestation fileMan = assertHasSingleManifestation(stateDu.getId(), DATAFILE_MAN_TYPE, dcp);
        final DcsFile file = assertHasFile(fileMan.getManifestationFiles().iterator().next().getRef().getRef(), dcp);
        assertEquals(DCS_FILE_DATAFILE_ID, file.getId());
        assertTrue(file.getAlternateIds().contains(new DcsResourceIdentifier(Id.getAuthority(),
                businessDataFileId, DATAFILE_ID_TYPE)));
    }

    /**
     * Mocks the scenario when creating a DCP for a DataFile that <em>is</em> in the archive.  The DataFile in
     * the archive has only one version; the archive contains one Root DU, and one State DU for the DataFile.  The
     * archive does <em>not</em> contain the DcsFile for the DataFile.
     *
     * @throws Exception
     */
    @Test
    public void testToDcpWithArchiveStateSingleVersion() throws Exception {
        final String archiveParentEntityId = "archive:dataitem:1";
        final String businessDataFileId = "business:datafile:1";

        final String archiveRootDuEntityId = "root:1";
        final String archiveStateDuEntityId = "state:1";
        final String lineageId = "lineage:1";

        final File sourceFile = createTemp();
        final DataFile df = new DataFile();
        df.setSize(sourceFile.length());
        df.setSource(sourceFile.getPath());
        df.setFormat("application/text");
        df.setId(businessDataFileId);
        df.setName(sourceFile.getName());

        when(searcher.findLatestState(businessDataFileId)).thenReturn(new BusinessObjectState() {
            @Override
            public DcsDeliverableUnit getRoot() {
                final String rootXml =
                        String.format(ROOT_DU_TEMPLATE, archiveRootDuEntityId, lineageId, archiveParentEntityId,
                                businessDataFileId);
                try {
                    return dmb.buildDeliverableUnit(IOUtils.toInputStream(rootXml));
                } catch (InvalidXmlException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            @Override
            public DcsDeliverableUnit getLatestState() {
                final String stateXml =
                        String.format(STATE_DU_TEMPLATE, archiveStateDuEntityId, lineageId, archiveRootDuEntityId,
                                businessDataFileId);
                try {
                    return dmb.buildDeliverableUnit(IOUtils.toInputStream(stateXml));
                } catch (InvalidXmlException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        final Dcp dcp = underTest.toDcp(archiveParentEntityId, df);
        assertNotNull(dcp);

//        for (DcsEntity e : dcp) {
//            e.toString(hpp);
//        }
//
//        System.out.println(hpp.toString());

        final DcsDeliverableUnit stateDu = assertSingleStateDuPresent(dcp, STATE_DU_TYPE);
        assertFormerRef(stateDu, businessDataFileId);
        assertHasSingleManifestation(stateDu.getId(), STATE_MANIFESTATION_TYPE, dcp);
        assertHasSuccessor(stateDu, archiveStateDuEntityId);
        assertHasParentRef(stateDu, archiveRootDuEntityId);
        final DcsManifestation fileMan = assertHasSingleManifestation(stateDu.getId(), DATAFILE_MAN_TYPE, dcp);
        final DcsFile file = assertHasFile(fileMan.getManifestationFiles().iterator().next().getRef().getRef(), dcp);
        assertEquals(DCS_FILE_DATAFILE_ID, file.getId());
        assertTrue(file.getAlternateIds().contains(new DcsResourceIdentifier(Id.getAuthority(),
                businessDataFileId, DATAFILE_ID_TYPE)));
    }

    /**
     * Mocks the scenario when creating a DCP for a DataFile that <em>is</em> in the archive.  The DataFile in
     * the archive has only one version; the archive contains one Root DU, and one State DU for the DataFile.  The
     * archive <em>does</em> contain the DcsFile for the DataFile.
     *
     * @throws Exception
     */
    @Test
    public void testToDcpWithArchiveStateSingleVersionFileInArchive() throws Exception {
        final String archiveParentEntityId = "archive:dataitem:1";
        final String businessDataFileId = "business:datafile:1";

        final String archiveRootDuEntityId = "root:1";
        final String archiveStateDuEntityId = "state:1";
        final String lineageId = "lineage:1";

        final String archiveDcsFileId = "dcsfile:1";

        final File sourceFile = createTemp();
        final DataFile df = new DataFile();
        df.setFormat("application/text");
        df.setId(businessDataFileId);
        df.setName(sourceFile.getName());

        when(searcher.findLatestState(businessDataFileId)).thenReturn(new BusinessObjectState() {
            @Override
            public DcsDeliverableUnit getRoot() {
                final String rootXml =
                        String.format(ROOT_DU_TEMPLATE, archiveRootDuEntityId, lineageId, archiveParentEntityId,
                                businessDataFileId);
                try {
                    return dmb.buildDeliverableUnit(IOUtils.toInputStream(rootXml));
                } catch (InvalidXmlException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            @Override
            public DcsDeliverableUnit getLatestState() {
                final String stateXml =
                        String.format(STATE_DU_TEMPLATE, archiveStateDuEntityId, lineageId, archiveRootDuEntityId,
                                businessDataFileId);
                try {
                    return dmb.buildDeliverableUnit(IOUtils.toInputStream(stateXml));
                } catch (InvalidXmlException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        final DcsFile dcsFile = new DcsFile();
        dcsFile.setId(archiveDcsFileId);
        dcsFile.setSizeBytes(sourceFile.length());
        dcsFile.setExtant(true);
        dcsFile.setName(sourceFile.getName());
        dcsFile.setSource(sourceFile.toURL().toExternalForm());
        dcsFile.addAlternateId(new DcsResourceIdentifier(Id.getAuthority(), businessDataFileId, DATAFILE_ID_TYPE));

        when(searcher.findDataFile(businessDataFileId)).thenReturn(dcsFile);

        final Dcp dcp = underTest.toDcp(archiveParentEntityId, df);
        assertNotNull(dcp);

//        for (DcsEntity e : dcp) {
//            e.toString(hpp);
//        }
//
//        System.out.println(hpp.toString());

        final DcsDeliverableUnit stateDu = assertSingleStateDuPresent(dcp, STATE_DU_TYPE);
        assertFormerRef(stateDu, businessDataFileId);
        assertHasSingleManifestation(stateDu.getId(), STATE_MANIFESTATION_TYPE, dcp);
        assertHasSuccessor(stateDu, archiveStateDuEntityId);
        assertHasParentRef(stateDu, archiveRootDuEntityId);
        final DcsManifestation fileMan = assertHasSingleManifestation(stateDu.getId(), DATAFILE_MAN_TYPE, dcp);
        final String ref = fileMan.getManifestationFiles().iterator().next().getRef().getRef();
        assertEquals(archiveDcsFileId, ref);
        assertDoesNotHaveFile(archiveDcsFileId, dcp);
    }

    @Test
    public void testFromDcp() throws Exception {
        final String arxIdRootDu = "arx:root:1";
        final String arxIdStateDu = "arx:state:1";
        final String arxIdStateMan = "arx:stateMan:1";
        final String arxIdDfMan = "arx:dfMan:2";
        final String arxIdStateFileId = "arx:stateFile:1";
        final String arxIdDfFileId = "arx:dataFile:1";
        final String arxLineageId1 = "arx:lineage:1";
        final String arxLineageId2 = "arx:lineage:2";
        final String busDfId = "businessId:df:1";

        final Dcp dcp = new Dcp();
        final File dfSource = createTemp();
        final DataFile dataFile = createDataFile(busDfId, dfSource);

        final DcsDeliverableUnit rootDu = new DcsDeliverableUnit();
        final DcsDeliverableUnit stateDu = new DcsDeliverableUnit();
        final DcsManifestation stateMan = new DcsManifestation();
        final DcsManifestation dfMan = new DcsManifestation();

        // Set up the DcsFiles

        final DcsFile dfFile = new DcsFile();
        dfFile.setId(arxIdDfFileId);
        dfFile.setExtant(true);
        dfFile.setName(dfSource.getName());
        dfFile.setSource(dfSource.toURL().toExternalForm());
        dfFile.setSizeBytes(dfSource.length());

        final DcsFile stateFile = new DcsFile();
        final File stateSource = createStateFile(dataFile);
        stateFile.setId(arxIdStateFileId);
        stateFile.setExtant(true);
        stateFile.setName(stateSource.getName());
        stateFile.setSource(stateSource.toURL().toExternalForm());
        stateFile.setSizeBytes(stateSource.length());

        // Set up the DeliverableUnits

        rootDu.setId(arxIdRootDu);
        rootDu.addFormerExternalRef(busDfId);
        rootDu.setLineageId(arxLineageId1);
        rootDu.setTitle(DU_TITLE);
        rootDu.setType(ROOT_DU_TYPE);

        stateDu.setId(arxIdStateDu);
        stateDu.addFormerExternalRef(busDfId);
        stateDu.setLineageId(arxLineageId2);
        stateDu.setTitle(DU_TITLE);
        stateDu.setType(STATE_DU_TYPE);
        stateDu.addParent(new DcsDeliverableUnitRef(rootDu.getId()));

        // Set up the Manifestations

        stateMan.setId(arxIdStateMan);
        stateMan.setDeliverableUnit(stateDu.getId());
        stateMan.setType(STATE_MANIFESTATION_TYPE);
        DcsManifestationFile stateManMf = new DcsManifestationFile();
        stateManMf.setPath(stateSource.getPath());
        stateManMf.setRef(new DcsFileRef(stateFile.getId()));
        stateMan.addManifestationFile(stateManMf);

        dfMan.setId(arxIdDfMan);
        dfMan.setDeliverableUnit(stateDu.getId());
        dfMan.setType(DATAFILE_MAN_TYPE);
        DcsManifestationFile dfManMf = new DcsManifestationFile();
        dfManMf.setPath(dfSource.getPath());
        dfManMf.setRef(new DcsFileRef(dfFile.getId()));
        dfMan.addManifestationFile(dfManMf);

        // Set up the DCP

        dcp.addEntity(rootDu, stateDu, stateMan, dfMan, dfFile, stateFile);

        // Perform the test
        final DataFile actual = underTest.fromDcp(dcp);

        assertEquals(dataFile, actual);
        dataFile.setSize(-1);
        assertFalse(dataFile.equals(actual));
    }

    private static File createTemp() throws IOException {
        File f = File.createTempFile("DataFileMapperTest-", ".tmp");
        FileUtils.writeStringToFile(f, "This is a temporary DataFile.");
        f.deleteOnExit();
        return f;
    }

    private File createStateFile(DataFile df) throws IOException {
        final File stateFile = createTemp();
        bob.buildDataFile(df, new FileOutputStream(stateFile));
        return stateFile;
    }

    private DataFile createDataFile(String dataFileBusinessId, File source) throws MalformedURLException {
        final DataFile df = new DataFile();
        df.setName(source.getName());
        df.setPath(source.getPath());
        df.setSize(source.length());
        df.setSource(source.toURL().toExternalForm());
        df.setId(dataFileBusinessId);

        return df;
    }

}
