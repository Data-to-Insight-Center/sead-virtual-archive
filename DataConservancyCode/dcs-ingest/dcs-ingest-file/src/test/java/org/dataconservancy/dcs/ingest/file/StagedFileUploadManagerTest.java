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
package org.dataconservancy.dcs.ingest.file;

import java.io.InputStream;

import java.security.MessageDigest;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

import org.apache.commons.io.IOUtils;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.StagedFile;
import org.dataconservancy.dcs.ingest.file.impl.MemoryFileContentStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.dataconservancy.model.dcs.DcsFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StagedFileUploadManagerTest {

    private final String BASIC_CONTENT = "basic content";

    private byte[] MD5;

    private String MD5_HEX;

    private byte[] SHA;

    private String SHA_HEX;

    private static final Abdera abdera = new Abdera();

    private static final DcsModelBuilder modelBuilder =
            new DcsXstreamStaxModelBuilder();

    private static FileContentTracker fileContentTracker =
            new FileContentTracker();

    private static SipStager sipStager = new MemoryStager();

    private static IdService idService = new MemoryIdServiceImpl();

    private static EventManager eventMgr;

    StagedFileUploadManager mgr = getUploadManager();

    @BeforeClass
    public static void init() {
        InlineEventManager iem = new InlineEventManager();
        iem.setIdService((BulkIdCreationService)idService);
        iem.setSipStager(sipStager);
        eventMgr = iem;
        fileContentTracker.setSipStager(sipStager);
    }

    @Before
    public void resetTracker() throws Exception {
        fileContentTracker.reset();
        MD5 = MessageDigest.getInstance("MD5").digest(BASIC_CONTENT.getBytes());
        MD5_HEX = new String(Hex.encodeHex(MD5));

        SHA = MessageDigest.getInstance("SHA").digest(BASIC_CONTENT.getBytes());
        SHA_HEX = new String(Hex.encodeHex(SHA));

    }

    @Test
    public void fileInStagerTest() {

        assertEquals(0, fileContentTracker.getMembers().size());
        mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT), null, null, null);
        assertEquals(1, fileContentTracker.getMembers().size());
    }

    @Test
    public void initialStatusDocumentTest() throws Exception {
        DepositInfo deposit =
                mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                            null,
                            null,
                            null);
        DepositDocument status = deposit.getDepositStatus();

        assertNotNull(status);
        assertTrue(status.getMimeType().contains("application/atom+xml"));
        parseStatus(deposit);
    }

    @Test
    public void initialContentDocumentTest() throws Exception {
        DepositInfo deposit =
                mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                            null,
                            null,
                            null);
        DepositDocument content = deposit.getDepositContent();
        assertNotNull(content);
        assertTrue(content.getMimeType().contains("application/xml"));
        parseContent(deposit);
    }

    @Test
    public void retrievedStatusDocumentTest() {
        int i = 1;
        i++;
        DepositInfo deposit =
                mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                            null,
                            null,
                            null);

        /* We discard the initial info, and fetch a subsequent info */
        deposit = mgr.getDepositInfo(deposit.getDepositID());

        DepositDocument status = deposit.getDepositStatus();
        assertNotNull(status);
        assertTrue(status.getMimeType().contains("application/atom+xml"));
        parseStatus(deposit);
    }

    @Test
    public void retrievedContentDocumentTest() throws Exception {
        DepositInfo deposit =
                mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                            null,
                            null,
                            null);

        /* We discard the initial info, and fetch a subsequent info */
        deposit = mgr.getDepositInfo(deposit.getDepositID());

        DepositDocument content = deposit.getDepositContent();
        assertNotNull(content);
        assertTrue(content.getMimeType().contains("application/xml"));
        parseContent(deposit);
    }

    @Test
    public void contentTypeTest() throws Exception {
        final String MIME = "text/plain;charset=UTF=8";
        DepositInfo deposit =
                mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                            MIME,
                            null,
                            null);
        Dcp content = parseContent(deposit);

        /* verify that the dcp File contains the characterization */
        assertEquals(1, content.getFiles().size());
        DcsFile file = content.getFiles().iterator().next();

        assertEquals(1, file.getFormats().size());
        DcsFormat format = file.getFormats().iterator().next();

        assertEquals(MIME, format.getFormat());
    }

    @Test
    public void fileNameTest() throws Exception {
        final String FILENAME = "Test.tst";
        Map<String, String> metadata = new HashMap<String, String>();
        metadata
                .put(HttpHeaderUtil.CONTENT_DISPOSITION, "filename=" + FILENAME);
        DepositInfo info =
                mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                            "text/plain",
                            null,
                            metadata);
        assertEquals(FILENAME, parseContent(info).getFiles().iterator().next()
                .getName());

    }

    /* Assures that all digests in submission metadata appear in File entity */
    @Test
    public void digestsRecordedInFileEntityTest() throws Exception {
        Map<String, String> metadata = new HashMap<String, String>();

        HttpHeaderUtil.addDigest("MD5", MD5, metadata);
        HttpHeaderUtil.addDigest("SHA", SHA, metadata);

        Collection<DcsFixity> fixity =
                parseContent(mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                                         "text/plain",
                                         null,
                                         metadata)).getFiles().iterator()
                        .next().getFixity();

        Map<String, String> fixtyValues = new HashMap<String, String>();
        for (DcsFixity f : fixity) {
            fixtyValues.put(f.getAlgorithm(), f.getValue());
        }

        assertEquals(fixtyValues.toString(), 2, fixity.size());
        assertEquals(MD5_HEX, fixtyValues.get("MD5"));
        assertEquals(SHA_HEX, fixtyValues.get("SHA"));

    }

    @Test
    public void calculatedDigestFileFixityTest() throws Exception {
        StagedFileUploadManager calcMgr = getUploadManager();
        calcMgr.setAlwaysCalculateFixityFor("MD5", "SHA");

        Collection<DcsFixity> fixity =
                parseContent(calcMgr
                        .deposit(IOUtils.toInputStream(BASIC_CONTENT),
                                 "text/plain",
                                 null,
                                 null)).getFiles().iterator().next()
                        .getFixity();
        assertEquals(fixity.toString(), 2, fixity.size());
        for (DcsFixity f : fixity) {
            if (f.getAlgorithm().equals("MD5")) {
                assertEquals(toHex(MD5), f.getValue());
            }
            if (f.getAlgorithm().equals("SHA")) {
                assertEquals(toHex(SHA), f.getValue());
            }
        }

    }

    @Test
    public void optionalApplicableFixityTest() throws Exception {
        StagedFileUploadManager calcMgr = getUploadManager();
        calcMgr.setCheckFixityOnlyIfPresent("MD5");

        Map<String, String> headers = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("MD5", MD5, headers);

        Collection<DcsEvent> events =
                parseContent(calcMgr
                        .deposit(IOUtils.toInputStream(BASIC_CONTENT),
                                 "text/plain",
                                 null,
                                 headers)).getEvents();
        int count = 0;
        for (DcsEvent e : events) {
            if (e.getEventType().equals(Events.FIXITY_DIGEST)) {
                count++;
            }
        }

        assertEquals(1, count);

    }

    @Test
    public void optionalNonApplicableFixityTest() throws Exception {
        StagedFileUploadManager calcMgr = getUploadManager();
        calcMgr.setCheckFixityOnlyIfPresent("SHA");

        Map<String, String> headers = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("MD5", MD5, headers);

        Collection<DcsEvent> events =
                parseContent(calcMgr
                        .deposit(IOUtils.toInputStream(BASIC_CONTENT),
                                 "text/plain",
                                 null,
                                 headers)).getEvents();
        int count = 0;
        for (DcsEvent e : events) {
            if (e.getEventType().equals(Events.FIXITY_DIGEST)) {
                count++;
            }
        }

        assertEquals(0, count);
    }

    @Test
    public void calculatedDigestMetadataTest() throws Exception {
        Map<String, String> metadata = new HashMap<String, String>();
        StagedFileUploadManager calcMgr = getUploadManager();
        calcMgr.setAlwaysCalculateFixityFor("MD5", "SHA");
        calcMgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                        "text/plain",
                        null,
                        metadata);
        Map<String, byte[]> digests = HttpHeaderUtil.getDigests(metadata);
        assertEquals(2, digests.size());
        assertEquals(toHex(MD5), toHex(digests.get("MD5")));
        assertEquals(toHex(SHA), toHex(digests.get("SHA")));
    }

    @Test
    public void calculatedDigestEventsTest() {
        StagedFileUploadManager calcMgr = getUploadManager();
        calcMgr.setAlwaysCalculateFixityFor("MD5", "SHA");
        Feed feed =
                parseStatus(calcMgr
                        .deposit(IOUtils.toInputStream(BASIC_CONTENT),
                                 "text/plain",
                                 null,
                                 null));
        int count = 0;
        for (Entry e : feed.getEntries()) {
            if (e.getTitle().equals(Events.FIXITY_DIGEST)) {
                count++;
                String[] cksmDetails = e.getContent().split(" ");
                if (cksmDetails[0].equals("MD5")) {
                    assertEquals(toHex(MD5), cksmDetails[1]);
                } else {
                    assertEquals(toHex(SHA), cksmDetails[1]);
                }
            }
        }
        assertEquals(2, count);
    }

    @Test(expected = Exception.class)
    public void calculatedDigestMismatchTest() {
        Map<String, String> metadata = new HashMap<String, String>();
        HttpHeaderUtil.addDigest("SHA", MD5, metadata);
        StagedFileUploadManager calcMgr = getUploadManager();
        calcMgr.setAlwaysCalculateFixityFor("SHA");
        calcMgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                        "text/plain",
                        null,
                        metadata);
    }

    @Test
    public void referenceUriHeaderTest() {
        DepositInfo info =
                mgr.deposit(IOUtils.toInputStream(BASIC_CONTENT),
                            null,
                            null,
                            null);
        assertTrue(info.getMetadata().containsKey("X-dcs-src"));
    }

    @Test
    public void depositEventPresentTest() {
        Feed events =
                parseStatus(getUploadManager()
                        .deposit(IOUtils.toInputStream(BASIC_CONTENT),
                                 "text/plain",
                                 null,
                                 null));

        int count = 0;
        String sipRef = null;
        for (Entry e : events.getEntries()) {
            if (e.getTitle().equals(Events.DEPOSIT)) {
                count++;
                sipRef = e.getContent();
            }
        }

        assertEquals(1, count);
        assertNotNull(sipStager.getSIP(sipRef));
        
    }

    @Test
    public void uploadEventPresentTest() {
        Feed events =
                parseStatus(getUploadManager()
                        .deposit(IOUtils.toInputStream(BASIC_CONTENT),
                                 "text/plain",
                                 null,
                                 null));

        int count = 0;
        for (Entry e : events.getEntries()) {
            if (e.getTitle().equals(Events.FILE_UPLOAD)) {
                count++;
            }
        }

        assertEquals(1, count);

    }

    private String toHex(byte[] array) {
        return new String(Hex.encodeHex(array));
    }

    private Feed parseStatus(DepositInfo deposit) {
        return (Feed) abdera.getParser().parse(deposit.getDepositStatus()
                .getInputStream()).getRoot();
    }

    private Dcp parseContent(DepositInfo deposit) throws InvalidXmlException {
        return modelBuilder.buildSip(deposit.getDepositContent()
                .getInputStream());
    }

    private StagedFileUploadManager getUploadManager() {
        StagedFileUploadManager mgr = new StagedFileUploadManager();
        mgr.setFileContentStager(fileContentTracker);
        mgr.setIdService(idService);
        mgr.setSipStager(sipStager);
        mgr.setEventManager(eventMgr);
        return mgr;
    }

    private static class FileContentTracker
            extends MemoryFileContentStager {

        private HashSet<String> members = new HashSet<String>();

        public StagedFile add(InputStream stream, Map<String, String> metadata) {
            StagedFile file = super.add(stream, metadata);
            members.add(file.getReferenceURI());
            return file;
        }

        public void remove(String id) {
            super.remove(id);
            members.remove(id);
        }

        public Set<String> getMembers() {
            return members;
        }

        public void reset() {
            members.clear();
        }
    }
}
