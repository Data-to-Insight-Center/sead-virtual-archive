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
package org.dataconservancy.dcs.ingest.services;

import java.io.InputStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.FileContentStager;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;

public class StagedContentResolverTest {

    private static SipStager sipStager = new MemoryStager();

    private static IngestFramework ingest = new IngestFramework();

    private static StagedContentResolver resolver;

    private static final String REF_URI = "example:/ref";

    private static final String ACCESS_URI = "example:/access";

    private static String FILE_ID = "example:/file.id";

    private static String stagedSipRef;
    private final String FILE_NAME = "examplefile.name";

    @BeforeClass
    public static void init() {
        InlineEventManager event = new InlineEventManager();
        event.setIdService(new MemoryIdServiceImpl());
        event.setSipStager(sipStager);

        ingest.setEventManager(event);
        ingest.setSipStager(sipStager);

        MockFileContentStager fcs = new MockFileContentStager();
        fcs.addID(REF_URI, ACCESS_URI);

        stagedSipRef = sipStager.addSIP(getStageDcp());
        fcs.addStagedFileSip(REF_URI, stagedSipRef);
        ingest.setFileContentStager(fcs);

        resolver = new StagedContentResolver();
        resolver.setIngestFramework(ingest);
    }

    @Before
    public void resetStagedSip() {

        sipStager.updateSIP(getStageDcp(), stagedSipRef);
    }

    private static Dcp getStageDcp() {
        Dcp stageDcp = new Dcp();
        DcsFile stagedFile = new DcsFile();
        stagedFile.setId("example:/stagedFile");
        stageDcp.addFile(stagedFile);
        return stageDcp;
    }

    @Test
    public void stagedSrcReplacedTest() {

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setName(FILE_NAME);
        file.setId("id");
        file.setSource(REF_URI);
        sip.addFile(file);

        String sipRef = addSip(REF_URI);

        Assert.assertEquals(REF_URI, file.getSource());

        resolver.execute(sipRef);

        file = sipStager.getSIP(sipRef).getFiles().iterator().next();

        Assert.assertEquals(ACCESS_URI, file.getSource());
    }

    @Test
    public void nonStagedSrcNotReplacedTest() {

        final String NON_REF_URI = "example:/nonRef";

        Dcp sip = new Dcp();

        DcsFile file = new DcsFile();
        file.setName(FILE_NAME);
        file.setId("id");
        file.setSource(REF_URI);

        DcsFile file2 = new DcsFile();
        file2.setName("name2");
        file2.setId("id2");
        file2.setSource(NON_REF_URI);

        sip.addFile(file);
        sip.addFile(file2);

        String sipRef = sipStager.addSIP(sip);

        resolver.execute(sipRef);

        boolean foundChangedFile = false;
        boolean foundUnchangedFile = false;

        for (DcsFile f : sipStager.getSIP(sipRef).getFiles()) {
            if (f.getSource().equals(ACCESS_URI)) {
                foundChangedFile = true;
            } else if (f.getSource().equals(NON_REF_URI)) {
                foundUnchangedFile = true;
            } else {
                throw new RuntimeException("Found unexpected src uri: "
                        + f.getSource());
            }
        }

        Assert.assertTrue(foundChangedFile);
        Assert.assertTrue(foundUnchangedFile);

    }

    @Test
    public void resolutionEventTest() {

        String sipRef = addSip(REF_URI);

        resolver.execute(sipRef);

        Collection<DcsEvent> events =
                ingest.getEventManager()
                        .getEvents(sipRef, Events.FILE_RESOLUTION_STAGED);

        Assert.assertEquals(1, events.size());
        DcsEvent e = events.iterator().next();

        Assert.assertEquals(Events.FILE_RESOLUTION_STAGED, e.getEventType());
        Assert.assertTrue(e.getOutcome().equals(REF_URI + " to " + ACCESS_URI));
    }

    @Test
    public void copyFileNameTest() {
        final String FILE_NAME = this.FILE_NAME;

        StagedFile staged = ingest.getFileContentStager().get(REF_URI);

        Dcp stagedFileSip = sipStager.getSIP(staged.getSipRef());

        /* Set the file name of the staged file */
        Collection<DcsFile> files = stagedFileSip.getFiles();
        files.iterator().next().setName(FILE_NAME);
        stagedFileSip.setFiles(files);

        String sipRef = addSip(REF_URI);

        resolver.execute(sipRef);

        Dcp sip = sipStager.getSIP(sipRef);

        Assert.assertEquals(FILE_NAME, sip.getFiles().iterator().next()
                .getName());

    }

    @Test
    public void copyFixityTest() {
        DcsFixity FIXITY = new DcsFixity();
        HashSet<DcsFixity> fixitySet = new HashSet<DcsFixity>();
        FIXITY.setAlgorithm("ALG");
        FIXITY.setValue("val");
        fixitySet.add(FIXITY);

        StagedFile staged = ingest.getFileContentStager().get(REF_URI);

        Dcp stagedFileSip = sipStager.getSIP(staged.getSipRef());

        /* Set the fixity value of the staged file */
        Collection<DcsFile> files = stagedFileSip.getFiles();
        files.iterator().next().setFixity(fixitySet);
        stagedFileSip.setFiles(files);
        sipStager.updateSIP(stagedFileSip, staged.getSipRef());

        String sipRef = addSip(REF_URI);

        resolver.execute(sipRef);

        Dcp sip = sipStager.getSIP(sipRef);

        boolean fixityFound = false;
        for (DcsFixity fx : sip.getFiles().iterator().next().getFixity()) {
            fixityFound = true;
            Assert.assertEquals(FIXITY, fx);
        }

        Assert.assertTrue(fixityFound);
    }

    @Test(expected = Exception.class)
    public void conflictingFixityTest() {
        DcsFixity stagedFileFixity = new DcsFixity();
        HashSet<DcsFixity> fixitySet = new HashSet<DcsFixity>();
        stagedFileFixity.setAlgorithm("ALG");
        stagedFileFixity.setValue("val");
        fixitySet.add(stagedFileFixity);

        DcsFixity stagedSipFixity = new DcsFixity();
        stagedSipFixity.setAlgorithm("ALG");
        stagedSipFixity.setValue("val2");

        StagedFile staged = ingest.getFileContentStager().get(REF_URI);

        Dcp stagedFileSip = sipStager.getSIP(staged.getSipRef());

        /* Set the fixity value of the staged file */
        Collection<DcsFile> files = stagedFileSip.getFiles();
        files.iterator().next().setFixity(fixitySet);
        stagedFileSip.setFiles(files);
        sipStager.updateSIP(stagedFileSip, staged.getSipRef());

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setId("id");
        file.setSource(REF_URI);
        file.addFixity(stagedSipFixity);
        sip.addFile(file);

        String sipRef = sipStager.addSIP(sip);

        resolver.execute(sipRef);
    }

    @Test
    public void otherFixityLeftAloneTest() {
        DcsFixity stagedFileFixity = new DcsFixity();
        HashSet<DcsFixity> fixitySet = new HashSet<DcsFixity>();
        stagedFileFixity.setAlgorithm("ALG");
        stagedFileFixity.setValue("val");
        fixitySet.add(stagedFileFixity);

        DcsFixity stagedSipFixity = new DcsFixity();
        stagedSipFixity.setAlgorithm("ALG2");
        stagedSipFixity.setValue("val2");

        StagedFile staged = ingest.getFileContentStager().get(REF_URI);

        Dcp stagedFileSip = sipStager.getSIP(staged.getSipRef());

        /* Set the fixity value of the staged file */
        Collection<DcsFile> files = stagedFileSip.getFiles();
        files.iterator().next().setFixity(fixitySet);
        stagedFileSip.setFiles(files);
        sipStager.updateSIP(stagedFileSip, staged.getSipRef());

        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setName("name");
        file.setId("id");
        file.setSource(REF_URI);
        file.addFixity(stagedSipFixity);
        sip.addFile(file);

        String sipRef = sipStager.addSIP(sip);

        resolver.execute(sipRef);

        sip = sipStager.getSIP(sipRef);

        file = sip.getFiles().iterator().next();

        Assert.assertEquals(2, file.getFixity().size());
        Assert.assertTrue(file.getFixity().contains(stagedSipFixity));
    }

    @Test
    public void copiedFixityEventTest() {
        final String ALGORITHM = "ALG";
        final String VALUE = "VAL";
        final String OUTCOME = String.format("%s %s", ALGORITHM, VALUE);
        final String DETAIL = "detail";

        String date = null;

        DcsFixity FIXITY = new DcsFixity();
        HashSet<DcsFixity> fixitySet = new HashSet<DcsFixity>();
        FIXITY.setAlgorithm(ALGORITHM);
        FIXITY.setValue(VALUE);
        fixitySet.add(FIXITY);

        StagedFile staged = ingest.getFileContentStager().get(REF_URI);

        Dcp stagedFileSip = sipStager.getSIP(staged.getSipRef());

        /* Set the fixity value of the staged file */
        Collection<DcsFile> files = stagedFileSip.getFiles();
        files.iterator().next().setFixity(fixitySet);
        stagedFileSip.setFiles(files);
        sipStager.updateSIP(stagedFileSip, staged.getSipRef());

        /* Add a fixity event to the staged file */
        DcsEvent stagedFileFixityEvent =
                ingest.getEventManager().newEvent(Events.FIXITY_DIGEST);
        stagedFileFixityEvent.addTargets(new DcsEntityReference(stagedFileSip
                .getFiles().iterator().next().getId()));
        stagedFileFixityEvent.setOutcome(OUTCOME);
        stagedFileFixityEvent.setDetail(DETAIL);
        date = stagedFileFixityEvent.getDate();
        ingest.getEventManager().addEvent(staged.getSipRef(),
                                          stagedFileFixityEvent);

        String sipRef = addSip(REF_URI);

        resolver.execute(sipRef);

        Collection<DcsEvent> events =
                ingest.getEventManager()
                        .getEvents(sipRef, Events.FIXITY_DIGEST);
        Assert.assertEquals(1, events.size());
        DcsEvent fixityEvent = events.iterator().next();

        /* Fixity event should point to our file entity */
        Assert.assertEquals(1, fixityEvent.getTargets().size());
        Assert.assertEquals(FILE_ID, fixityEvent.getTargets().iterator().next()
                .getRef());

        /* Fixity event should have same relevant details */
        Assert.assertEquals(DETAIL, fixityEvent.getDetail());
        Assert.assertEquals(OUTCOME, fixityEvent.getOutcome());
        Assert.assertEquals(date, fixityEvent.getDate());

    }

    @Test
    public void uploadEventTest() {
        final String OUTCOME = "outcome";
        final String DETAIL = "detail";
        String date;

        StagedFile staged = ingest.getFileContentStager().get(REF_URI);

        DcsEvent uploadEvent =
                ingest.getEventManager().newEvent(Events.FILE_UPLOAD);
        uploadEvent.addTargets(new DcsEntityReference(sipStager.getSIP(staged
                .getSipRef()).getFiles().iterator().next().getId()));
        uploadEvent.setOutcome(OUTCOME);
        uploadEvent.setDetail(DETAIL);
        date = uploadEvent.getDate();
        ingest.getEventManager().addEvent(staged.getSipRef(), uploadEvent);

        String sipRef = addSip(REF_URI);

        resolver.execute(sipRef);

        Collection<DcsEvent> events =
                ingest.getEventManager().getEvents(sipRef, Events.FILE_UPLOAD);

        Assert.assertEquals(1, events.size());
        DcsEvent inSipUploadEvent = events.iterator().next();

        Assert.assertEquals(1, inSipUploadEvent.getTargets().size());
        Assert.assertEquals(FILE_ID, inSipUploadEvent.getTargets().iterator()
                .next().getRef());
        Assert.assertEquals(OUTCOME, inSipUploadEvent.getOutcome());
        Assert.assertEquals(DETAIL, inSipUploadEvent.getDetail());
        Assert.assertEquals(date, inSipUploadEvent.getDate());

    }

    /* Stage a sip containing a single file with the given source */
    private String addSip(String src) {
        Dcp sip = new Dcp();
        DcsFile file = new DcsFile();
        file.setName(FILE_NAME);
        file.setId(FILE_ID);
        file.setSource(src);
        sip.addFile(file);

        return sipStager.addSIP(sip);
    }

    private static class MockFileContentStager
            implements FileContentStager {

        /* ReferenceURI to accessURI */
        Map<String, String> idMap = new HashMap<String, String>();

        /* ReferenceURI to sipRef */
        Map<String, String> sipMap = new HashMap<String, String>();

        public MockFileContentStager() {

        }

        public void addID(String key, String value) {
            idMap.put(key, value);
        }

        public void addStagedFileSip(String referenceURI, String sipRef) {
            sipMap.put(referenceURI, sipRef);
        }

        public StagedFile add(InputStream stream, Map<String, String> metadata) {
            /* Not used here */
            return null;
        }

        public boolean contains(String id) {
            return idMap.containsKey(id);
        }

        public StagedFile get(final String id) {
            if (idMap.containsKey(id)) {
                return new StagedFile() {

                    public String getReferenceURI() {
                        return id;
                    }

                    public InputStream getContent() {
                        /* Not used here */
                        return null;
                    }

                    public String getAccessURI() {
                        return idMap.get(id);
                    }

                    public String getSipRef() {
                        return sipMap.get(id);
                    }
                };
            } else {
                return null;
            }
        }

        public void remove(String id) {
            /* Not used here */
        }
        
        public void retire(String id) {
            /* Not used here */
        }
    }
}
