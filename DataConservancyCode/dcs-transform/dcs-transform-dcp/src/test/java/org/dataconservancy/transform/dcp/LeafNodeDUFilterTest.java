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
package org.dataconservancy.transform.dcp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsFormat;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.dcp.LeafNodeDUFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LeafNodeDUFilterTest {

    private Dcp sourceDcp;

    private LeafNodeDUFilter filter = new LeafNodeDUFilter();

    Dcp dcp1;

    Dcp dcp2;

    private static final String DU_ONE_ID = "urn:sdss:12345";

    private static final String DU_ONE_COLL_REF =
            "http://dataconservancy.org/collections/SDSS_run_5";

    private static final String DU_ONE_TITLE = "SDSS file 12345";

    private static final String DU_ONE_CREATOR =
            "Astrophysical Research Consortium (ARC)";

    private static final String DU_ONE_SUBJECT = "Astronomy";

    private static final String DU_ONE_EXTREF =
            "http://das.sdss.org/blahblahblah...";

    private static final String DU_ONE_MDREF = "urn:sdss:12345/metadata";

    private static final String DU_ONE_MD_INLINE_SCHEMA =
            "http://sdss.org/metadata/astroSchema.example.xsd";

    private static final String DU_ONE_MD_BLOB =
            "<astro:md xmlns:astro=\"http://sdss.org/astro\">\n"
                    + "          <astro:skyCoverage>all of it</astro:skyCoverage>\n"
                    + "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n"
                    + "        </astro:md>";

    private static final String DU_TWO_ID = "urn:sdss:54321";

    private static final String DU_TWO_COLL_REF =
            "http://dataconservancy.org/collections/SDSS_run_1";

    private static final String DU_TWO_TITLE = "SDSS file 54321";

    private static final String DU_TWO_CREATOR =
            "Astrophysical Research Consortium (ARC)";

    private static final String DU_TWO_SUBJECT = "Astronomy";

    private static final String DU_TWO_EXTREF =
            "http://das.sdss.org/testtesttest...";

    private static final String DU_TWO_MDREF = "urn:sdss:54321/metadata";

    private static final String DU_TWO_MD_INLINE_SCHEMA =
            "http://sdss.org/metadata/astroSchema.example.xsd";

    private static final String DU_TWO_MD_BLOB =
            "<astro:md xmlns:astro=\"http://sdss.org/astro\">\n"
                    + "          <astro:skyCoverage>all of it</astro:skyCoverage>\n"
                    + "          <astro:enfOfWorldFactor>-1</astro:enfOfWorldFactor>\n"
                    + "        </astro:md>";

    private static final String MANIFESTATION1_ID =
            "urn:sdss:12345/manifestation";

    private static final String MANIFESTATION2_ID =
            "urn:sdss:54321/manifestation";

    private static final String MF_FILEPATH = "/scans/5/";

    private static final String FILE_ID1 = "urn:sdss:12345/FITS_FILE";

    private static final String FILE_SRC1 =
            "http://sdss.org/files/fits/12345.fits";

    private static final String FILE_NAME1 = "12345.fits";

    private static final String FILE_EXT1 = "false";

    private static final String FORMAT_SCHEMEID1 =
            "http://www.nationalarchives.gov.uk/PRONOM/";

    private static final String FORMAT1 = "x-fmt/383";

    private static final String FORMAT_NAME1 = "FITS";

    private static final String FORMAT_VERSION1 = "3.0";

    private static final String FILE_ID2 = "urn:sdss:54321/metadata";

    private static final String FILE_SRC2 = "urn:dcs:uploads/0x440";

    private static final String FILE_NAME2 = "fitsDerivedMetadata.csv";

    private static final String FILE_EXT2 = "true";

    private static final String FORMAT_SCHEMEID2 =
            "http://www.nationalarchives.gov.uk/PRONOM/";

    private static final String FORMAT2 = "x-fmt/18";

    private static final String FORMAT_SCHEMEID3 =
            "http://www.iana.org/assignments/media-types/";

    private static final String FORMAT3 = "text/csv";

    private static final String EVENT1_ID = "e:1111";

    private static final String EVENT2_ID = "e:2222";

    private static final String EVENT3_ID = "e:3333";

    @Before
    public void setUp() {
        //***************************************************
        //setting up du1
        //***************************************************
        final DcsDeliverableUnit duONE = new DcsDeliverableUnit();
        duONE.setId(DU_ONE_ID);
        duONE.setTitle(DU_ONE_TITLE);
        duONE.addCreator(DU_ONE_CREATOR);
        duONE.addSubject(DU_ONE_SUBJECT);
        duONE.addFormerExternalRef(DU_ONE_EXTREF);

        final DcsCollectionRef collRef = new DcsCollectionRef();
        collRef.setRef(DU_ONE_COLL_REF);
        duONE.addCollection(collRef);

        final DcsMetadataRef mdRef = new DcsMetadataRef();
        mdRef.setRef(DU_ONE_MDREF);
        duONE.addMetadataRef(mdRef);

        final DcsMetadata mdInline = new DcsMetadata();
        mdInline.setSchemaUri(DU_ONE_MD_INLINE_SCHEMA);
        mdInline.setMetadata(DU_ONE_MD_BLOB);
        duONE.addMetadata(mdInline);

        //***************************************************
        //setting up du 2
        //***************************************************
        final DcsDeliverableUnit duTWO = new DcsDeliverableUnit();
        duTWO.setId(DU_TWO_ID);
        duTWO.setTitle(DU_TWO_TITLE);
        duTWO.addCreator(DU_TWO_CREATOR);
        duTWO.addSubject(DU_TWO_SUBJECT);
        duTWO.addFormerExternalRef(DU_TWO_EXTREF);

        final DcsCollectionRef collRef2 = new DcsCollectionRef();
        collRef2.setRef(DU_TWO_COLL_REF);
        duTWO.addCollection(collRef2);

        final DcsMetadataRef mdRef2 = new DcsMetadataRef();
        mdRef2.setRef(DU_TWO_MDREF);
        duTWO.addMetadataRef(mdRef2);

        final DcsMetadata mdInline2 = new DcsMetadata();
        mdInline2.setSchemaUri(DU_TWO_MD_INLINE_SCHEMA);
        mdInline2.setMetadata(DU_TWO_MD_BLOB);
        duTWO.addMetadata(mdInline2);

        //***************************************************
        //setting up manifestation 1
        //***************************************************
        final DcsManifestation man1 = new DcsManifestation();
        man1.setId(MANIFESTATION1_ID);
        man1.setDeliverableUnit(DU_ONE_ID);

        final DcsManifestationFile mf1 = new DcsManifestationFile();
        mf1.setRef(new DcsFileRef(FILE_ID1));
        mf1.setPath(MF_FILEPATH);
        man1.addManifestationFile(mf1);

        //***************************************************
        //setting up manifestation 2
        //***************************************************
        final DcsManifestation man2 = new DcsManifestation();
        man2.setId(MANIFESTATION2_ID);
        man2.setDeliverableUnit(DU_TWO_ID);

        final DcsManifestationFile mf2 = new DcsManifestationFile();
        mf2.setRef(new DcsFileRef(FILE_ID2));
        mf2.setPath(MF_FILEPATH);
        man2.addManifestationFile(mf2);

        //***************************************************
        //setting up manifestation 3
        //***************************************************
        final DcsManifestation man3 = new DcsManifestation();
        man3.setId(MANIFESTATION2_ID);
        man3.setDeliverableUnit(DU_TWO_ID);

        final DcsManifestationFile mf3 = new DcsManifestationFile();
        mf3.setRef(new DcsFileRef(FILE_ID2));
        mf3.setPath(MF_FILEPATH);
        man3.addManifestationFile(mf3);

        //***************************************************
        //setting up file 1
        //***************************************************
        final DcsFile fileOne = new DcsFile();
        fileOne.setId(FILE_ID1);
        fileOne.setSource(FILE_SRC1);
        fileOne.setName(FILE_NAME1);
        fileOne.setExtant(Boolean.parseBoolean(FILE_EXT1));
        final DcsFormat fileOneFmt = new DcsFormat();
        fileOneFmt.setSchemeUri(FORMAT_SCHEMEID1);
        fileOneFmt.setFormat(FORMAT1);
        fileOneFmt.setVersion(FORMAT_VERSION1);
        fileOneFmt.setName(FORMAT_NAME1);
        fileOne.addFormat(fileOneFmt);

        //***************************************************
        //setting up file 2
        //***************************************************
        final DcsFile fileTwo = new DcsFile();
        fileTwo.setId(FILE_ID2);
        fileTwo.setSource(FILE_SRC2);
        fileTwo.setName(FILE_NAME2);
        fileTwo.setExtant(Boolean.parseBoolean(FILE_EXT2));

        final DcsFormat fileTwoFmt1 = new DcsFormat();
        fileTwoFmt1.setSchemeUri(FORMAT_SCHEMEID2);
        fileTwoFmt1.setFormat(FORMAT2);
        fileTwo.addFormat(fileTwoFmt1);

        final DcsFormat fileTwoFmt2 = new DcsFormat();
        fileTwoFmt2.setSchemeUri(FORMAT_SCHEMEID3);
        fileTwoFmt2.setFormat(FORMAT3);
        fileTwo.addFormat(fileTwoFmt2);

        //***************************************************
        //setting up event 1
        //***************************************************
        final DcsEvent event1 = new DcsEvent();
        event1.setId(EVENT1_ID);
        event1.addTargets(new DcsEntityReference(duONE.getId()));
        event1.addTargets(new DcsEntityReference(man1.getId()));

        //***************************************************
        //setting up event 2
        //***************************************************
        final DcsEvent event2 = new DcsEvent();
        event2.setId(EVENT2_ID);
        event2.addTargets(new DcsEntityReference(fileOne.getId()));
        event2.addTargets(new DcsEntityReference(fileTwo.getId()));

        //***************************************************
        //setting up event 3
        //***************************************************
        final DcsEvent event3 = new DcsEvent();
        event3.setId(EVENT3_ID);
        event3.addTargets(new DcsEntityReference(duTWO.getId()));
        event3.addTargets(new DcsEntityReference(fileTwo.getId()));

        sourceDcp = new Dcp();
        sourceDcp.addDeliverableUnit(duONE);
        sourceDcp.addDeliverableUnit(duTWO);
        sourceDcp.addManifestation(man1);
        sourceDcp.addManifestation(man2);
        sourceDcp.addFile(fileOne);
        sourceDcp.addFile(fileTwo);
        sourceDcp.addEvent(event1);
        sourceDcp.addEvent(event2);
        sourceDcp.addEvent(event3);

        dcp1 = new Dcp();
        dcp1.addDeliverableUnit(duONE);
        dcp1.addManifestation(man1);
        dcp1.addFile(fileOne);
        dcp1.addEvent(event1);
        dcp1.addEvent(event2);

        dcp2 = new Dcp();
        dcp2.addDeliverableUnit(duTWO);
        dcp2.addManifestation(man2);
        // man3 is identical to man2.  when changing the backing data structure of the DCP from a Set to a List, this
        // results in duplicate manifestations being added to the dcp.
//        dcp2.addManifestation(man3);
        dcp2.addFile(fileTwo);
        dcp2.addEvent(event3);
        dcp2.addEvent(event2);

    }

    @Test
    public void testSplitting() {

        TestOutput out = new TestOutput();
        filter.map("", sourceDcp, out);

        Dcp actualDcp1 = out.results.get(DU_ONE_ID);

        assertEquals(dcp1, actualDcp1);

        Dcp actualDcp2 = out.results.get(DU_TWO_ID);
        assertEquals(dcp2, actualDcp2);
    }

    @Test
    public void testSize() {

        TestOutput out = new TestOutput();
        filter.map("", sourceDcp, out);

        assertEquals(2, out.results.size());
    }

    @Test
    public void splittedDcpsContentTest() {

        TestOutput out = new TestOutput();
        filter.map("", sourceDcp, out);

        Dcp currentDcp;
        Collection<DcsEvent> eventsList;
        Collection<DcsFile> filesList;
        Collection<DcsManifestation> manisList;

        assertEquals(sourceDcp.getDeliverableUnits().size(), out.results.size());
        //System.out.println("************************************** "+ sourceDcp.getDeliverableUnits().size());
        for (Map.Entry<String, Dcp> result : out.results.entrySet()) {
            currentDcp = result.getValue();
            assertNotNull(sourceDcp.getDeliverableUnits()
                    .contains((currentDcp.getDeliverableUnits()).toArray()[0]));
            eventsList = currentDcp.getEvents();
            filesList = currentDcp.getFiles();
            manisList = currentDcp.getManifestations();
            //System.out.println("**************** events list size from sipreader key        "+ sipReader.getCurrentKey()+ " - " + eventsList.size()); 
            //System.out.println("**************** files list size from sipreader key         "+ sipReader.getCurrentKey()+ " - " + filesList.size());
            //System.out.println("**************** manifestation list size from sipreader key "+ sipReader.getCurrentKey()+ " - " + manisList.size());
            for (DcsEvent event : eventsList) {
                assertNotNull(sourceDcp.getEvents().contains(event));
            }
            for (DcsFile file : filesList) {
                assertNotNull(sourceDcp.getFiles().contains(file));
            }
            for (DcsManifestation manifestation : manisList) {
                assertNotNull(sourceDcp.getManifestations()
                        .contains(manifestation));
            }
        }

    }

    @Test
    public void testDUsConservation() {

        TestOutput out = new TestOutput();
        filter.map("", sourceDcp, out);

        DcsDeliverableUnit toBeCompared;
        //check to make sure all DUs are still there
        for (DcsDeliverableUnit du : sourceDcp.getDeliverableUnits()) {
            assertNotNull(out.results.get(du.getId()));
            toBeCompared =
                    (DcsDeliverableUnit) out.results.get(du.getId())
                            .getDeliverableUnits().toArray()[0];
            assertEquals(toBeCompared, du);

        }
    }

    @Test
    public void dcsManifestationConservationTest() {

        TestOutput out = new TestOutput();
        filter.map("", sourceDcp, out);

        Dcp toBeTested;
        //make sure that there 's a dcp that contains each of the manifestation
        for (DcsManifestation mani : sourceDcp.getManifestations()) {
            toBeTested = out.results.get(mani.getDeliverableUnit());
            assertNotNull(toBeTested);
            assertTrue(toBeTested.getManifestations().contains(mani));

        }
    }

    @Test
    public void dcsEventConservationTest() {
        TestOutput out = new TestOutput();
        filter.map("", sourceDcp, out);

        String targetId;
        Dcp testDcp;
        boolean isAccountedFor = false;
        // for each event from the souceDcp,
        // make sure it's accounted for in the split result
        for (DcsEvent event : sourceDcp.getEvents()) {
            //System.out.println("***************************************");
            //System.out.println("looking for Event: " + event.getId());

            //for each target within an event, 
            //make sure the target and the event is contain in the same result dcp
            for (DcsEntityReference ref : event.getTargets()) {

                targetId = ref.getRef();
                //System.out.println("Looking for target ID " + targetId);
                //check if the target is a du
                testDcp = out.results.get(targetId);
                //if target is a du 
                //and the dcp containing the du also contain the event
                //then the event is accounted for.
                if (testDcp != null) {
                    //System.out.println("Found target: " + targetId);
                    assertTrue(testDcp.getEvents().contains(event));
                }
                //if the taget is NOT a du
                else {
                    for (Dcp value : out.results.values()) {
                        //System.out.println("Current DCP : " + sipReader.getCurrentKey());
                        testDcp = value;
                        // if the dcp contains event in question
                        if (testDcp.getEvents().contains(event)) {
                            //System.out.println("List of manifestations");
                            //make sure it also contain the event's target 
                            //check in manifestation
                            for (DcsManifestation mani : testDcp
                                    .getManifestations()) {
                                //System.out.println("Mani ID: " + mani.getId());
                                if (mani.getId().equals(targetId)) {
                                    //System.out.println("Found target: " + targetId);
                                    //make sure the same dcp also contain the event in question
                                    if (testDcp.getEvents().contains(event)) {
                                        //System.out.println("... in the same package as " + event.getId());
                                        isAccountedFor = true;
                                        break;
                                    } else {
                                        fail("DCP doesn't contain the event AND its target");
                                    }
                                }
                            }
                            //check in files 
                            if (!isAccountedFor) {
                                //System.out.println("List of Files: ");
                                for (DcsFile file : testDcp.getFiles()) {
                                    //System.out.println("File ID: " + file.getId());
                                    if (file.getId().equals(targetId)) {
                                        //System.out.println("Found target: " + targetId);
                                        //make sure the same dcp also contain the event in question
                                        if (testDcp.getEvents().contains(event)) {
                                            //System.out.println("... in the same package as " + event.getId());
                                            isAccountedFor = true;
                                            break;
                                        } else {
                                            fail("DCP doesn't contain the event AND its target");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //if the dcp contains the event in question...
                    //makes sure it contains the event's targets
                    assertTrue(isAccountedFor);
                }
            }
        }
    }

    private static class TestOutput
            implements Output<String, Dcp> {

        Map<String, Dcp> results = new HashMap<String, Dcp>();

        public void write(String key, Dcp value) {
            results.put(key, value);
        }

        @Override
        public void close() {
            /* Does nothing */
        }

    }
}
