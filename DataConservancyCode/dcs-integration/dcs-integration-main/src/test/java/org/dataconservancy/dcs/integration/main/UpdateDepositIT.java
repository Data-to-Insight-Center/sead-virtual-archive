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
package org.dataconservancy.dcs.integration.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.ingest.client.DepositBuilder;
import org.dataconservancy.dcs.ingest.client.DepositClient;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UpdateDepositIT {
    private static final int NUM_CONCURRENT_DEPOSITS = 5;
    private static final String SUCCESSOR_URI = "urn:dataconservancy.org:rel/isSuccessorOf";
    private static final Logger log = LoggerFactory
            .getLogger(UpdateDepositIT.class);

    private static DepositClient depositClient;
    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @BeforeClass
    public static void setup() throws IOException {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
                new String[]{"depositClientContext.xml",
                        "classpath*:org/dataconservancy/config/applicationContext.xml"});
        depositClient = (DepositClient) appContext.getBean("depositClient");
    }

    // return the corresponding dip from the archive or null on deposit failure

    private Dcp deposit(SIP sip) throws InterruptedException,
            InvalidXmlException, IOException {
        DepositBuilder deposit = depositClient.buildDeposit(sip.asDcp());

        for (SIPFile sf : sip.files()) {
            deposit.uploadFile(sf.path, sf.man_ids.toArray(new String[]{}));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        builder.buildSip(deposit.getSip(), os);

        String id = deposit.execute();

        DepositInfo myDeposit = depositClient.getDepositInfo(id);

        int tries = 0;
        while (!myDeposit.hasCompleted() && tries < 50) {
            tries++;
            Thread.sleep(1000);
            myDeposit = depositClient.getDepositInfo(id);
        }

        Assert.assertTrue(myDeposit.hasCompleted());

        log.info("Completed ingest " + myDeposit.getDepositID());
        log.info("Ingest success " + myDeposit.isSuccessful());
        log.debug("Ingest status " + IOUtils.toString(myDeposit.getDepositStatus().getInputStream()));

        if (!myDeposit.isSuccessful()) {
            return null;
        }

        InputStream stream = depositClient.getDepositInfo(id)
                .getDepositContent().getInputStream();
        try {
            return builder.buildSip(stream);
        } finally {
            stream.close();
        }
    }

    private static String randomString(int length) {
        char[] buf = new char[length];

        Random rand = new Random();

        for (int i = 0; i < length; i++) {
            buf[i] = (char) ('a' + rand.nextInt(26));
        }

        return new String(buf);
    }

    private static File createTempTextFile(String contents) throws IOException {
        File tmp = File.createTempFile("tmpfile", ".txt");
        tmp.deleteOnExit();

        FileWriter w = new FileWriter(tmp);
        w.write(contents);
        w.close();

        return tmp;
    }

    private static class SIP {
        private List<SIPVersion> versions;
        private List<SIPFile> files;
        private int nextid;

        public SIP() {
            this.versions = new ArrayList<SIPVersion>();
            this.files = new ArrayList<SIPFile>();
        }

        public List<SIPFile> files() {
            return files;
        }

        public DcsManifestation addVersion(String predecessor, String title) {
            SIPVersion v = new SIPVersion(predecessor, nextid(), title);
            versions.add(v);

            return v.addManifestation(nextid());
        }

        public DcsDeliverableUnit addVersionWithExistingFile(
                String predecessor, String title, String file_id) {
            DcsManifestation man = addVersion(predecessor, title);

            DcsManifestationFile mf = new DcsManifestationFile();
            mf.setRef(new DcsFileRef(file_id));

            man.addManifestationFile(mf);

            return versions.get(versions.size() - 1).getDeliverableUnit();
        }

        public DcsDeliverableUnit addVersionWithNewFile(String predecessor,
                                                        String title) throws IOException {
            DcsManifestation man = addVersion(predecessor, title);

            String path = createTempTextFile(randomString(10))
                    .getCanonicalPath();

            files.add(new SIPFile(path, man.getId()));

            return versions.get(versions.size() - 1).getDeliverableUnit();
        }

        public Dcp asDcp() {
            Dcp dcp = new Dcp();

            for (SIPVersion v : versions) {
                dcp.addDeliverableUnit(v.getDeliverableUnit());

                for (DcsManifestation m : v.getManifestations()) {
                    dcp.addManifestation(m);
                }
            }

            return dcp;
        }

        private String nextid() {
            return "" + nextid++;
        }
    }

    // A file attached to a list of manifestations
    private static class SIPFile {
        private final String path;
        private final List<String> man_ids;

        public SIPFile(String path, String... man_ids) {
            this.path = path;
            this.man_ids = new ArrayList<String>();

            for (String id : man_ids) {
                this.man_ids.add(id);
            }
        }
    }

    private static class SIPVersion {
        private final DcsDeliverableUnit du;
        private final List<DcsManifestation> mans;

        public SIPVersion(String predecessor, String du_id, String title) {
            du = new DcsDeliverableUnit();
            du.setId(du_id);
            du.setTitle(title);
            mans = new ArrayList<DcsManifestation>();

            if (predecessor != null) {
                DcsRelation rel = new DcsRelation();

                rel.setRef(new DcsEntityReference(predecessor));
                rel.setRelUri(SUCCESSOR_URI);

                du.addRelation(rel);
            }
        }

        public DcsDeliverableUnit getDeliverableUnit() {
            return du;
        }

        public List<DcsManifestation> getManifestations() {
            return mans;
        }

        public DcsManifestation addManifestation(String man_id) {
            DcsManifestation man = new DcsManifestation();

            man.setId(man_id);
            man.setDeliverableUnit(du.getId());
            mans.add(man);

            return man;
        }
    }

    @Test
    public void testManifestationPointingToExistingFile() throws Exception {
        SIP sip1 = new SIP();
        sip1.addVersionWithNewFile(null, "sip1");
        Dcp result1 = deposit(sip1);

        assertNotNull(result1);
        assertEquals(1, result1.getFiles().size());

        DcsFile file1 = result1.getFiles().iterator().next();

        SIP sip2 = new SIP();
        sip2.addVersionWithExistingFile(null, "sip2", file1.getId());
        Dcp result2 = deposit(sip2);

        assertNotNull(result2);
        assertEquals(0, result2.getFiles().size());
        assertEquals(1, result2.getManifestations().size());

        DcsManifestation man2 = result2.getManifestations().iterator().next();

        assertEquals(1, man2.getManifestationFiles().size());

        DcsManifestationFile mf2 = man2.getManifestationFiles().iterator()
                .next();

        assertEquals(file1.getId(), mf2.getRef().getRef());
    }

    @Test
    public void testUpdateReplacingFile() throws Exception {
        SIP sip1 = new SIP();
        sip1.addVersionWithNewFile(null, "sip1");
        Dcp result1 = deposit(sip1);

        assertNotNull(result1);
        assertEquals(1, result1.getDeliverableUnits().size());
        assertEquals(1, result1.getFiles().size());
        assertEquals(1, result1.getManifestations().size());

        DcsDeliverableUnit du1 = result1.getDeliverableUnits().iterator()
                .next();
        DcsFile file1 = result1.getFiles().iterator().next();

        SIP sip2 = new SIP();
        sip2.addVersionWithNewFile(du1.getId(), "sip2");
        Dcp result2 = deposit(sip2);

        assertNotNull(result2);
        assertEquals(1, result2.getDeliverableUnits().size());
        assertEquals(1, result2.getFiles().size());
        assertEquals(1, result2.getManifestations().size());

        DcsDeliverableUnit du2 = result2.getDeliverableUnits().iterator()
                .next();
        DcsFile file2 = result2.getFiles().iterator().next();

        assertNotNull(du1.getLineageId());
        assertNotNull(du2.getLineageId());
        assertEquals(du1.getLineageId(), du2.getLineageId());

        assertFalse(file1.getId().equals(file2.getId()));
    }

    @Test
    public void testForbidBranching() throws Exception {
        SIP sip1 = new SIP();
        sip1.addVersion(null, "sip1");
        Dcp result1 = deposit(sip1);

        assertNotNull(result1);
        assertEquals(1, result1.getDeliverableUnits().size());
        assertEquals(1, result1.getManifestations().size());

        DcsDeliverableUnit du1 = result1.getDeliverableUnits().iterator()
                .next();

        SIP sip2 = new SIP();
        sip2.addVersion(du1.getId(), "sip2");
        Dcp result2 = deposit(sip2);

        assertNotNull(result2);
        assertEquals(1, result2.getDeliverableUnits().size());
        assertEquals(1, result2.getManifestations().size());

        DcsDeliverableUnit du2 = result2.getDeliverableUnits().iterator()
                .next();

        assertNotNull(du1.getLineageId());
        assertNotNull(du2.getLineageId());
        assertEquals(du1.getLineageId(), du2.getLineageId());

        SIP sip3 = new SIP();
        sip3.addVersion(du1.getId(), "sip3");

        Dcp result3 = deposit(sip3);

        assertNull(result3);
    }

    @Test
    public void testUpdateKeepingFile() throws Exception {
        SIP sip1 = new SIP();
        sip1.addVersionWithNewFile(null, "sip1");
        Dcp result1 = deposit(sip1);

        assertNotNull(result1);
        assertEquals(1, result1.getDeliverableUnits().size());
        assertEquals(1, result1.getFiles().size());
        assertEquals(1, result1.getManifestations().size());

        DcsDeliverableUnit du1 = result1.getDeliverableUnits().iterator()
                .next();
        DcsFile file1 = result1.getFiles().iterator().next();

        SIP sip2 = new SIP();
        sip2.addVersionWithExistingFile(du1.getId(), "sip2", file1.getId());
        Dcp result2 = deposit(sip2);

        assertNotNull(result2);
        assertEquals(1, result2.getDeliverableUnits().size());
        assertEquals(0, result2.getFiles().size());
        assertEquals(1, result2.getManifestations().size());

        DcsDeliverableUnit du2 = result2.getDeliverableUnits().iterator()
                .next();

        assertNotNull(du1.getLineageId());
        assertNotNull(du2.getLineageId());
        assertEquals(du1.getLineageId(), du2.getLineageId());

        DcsManifestation man2 = result2.getManifestations().iterator().next();

        assertEquals(1, man2.getManifestationFiles().size());

        DcsManifestationFile mf2 = man2.getManifestationFiles().iterator()
                .next();

        assertEquals(file1.getId(), mf2.getRef().getRef());
    }

    @Test
    public void testMultipleVersionsInSameLineage() throws Exception {
        SIP sip = new SIP();

        DcsDeliverableUnit du1 = sip.addVersionWithNewFile(null, "v1");
        DcsDeliverableUnit du2 = sip.addVersionWithNewFile(du1.getId(), "v2");

        assertNull(du1.getLineageId());
        assertNull(du2.getLineageId());

        Dcp result = deposit(sip);

        assertNotNull(result);
        assertEquals(2, result.getDeliverableUnits().size());
        assertEquals(2, result.getFiles().size());

        Iterator<DcsDeliverableUnit> iter = result.getDeliverableUnits()
                .iterator();

        du1 = iter.next();
        du2 = iter.next();

        assertNotNull(du1.getLineageId());
        assertNotNull(du2.getLineageId());
        assertEquals(du1.getLineageId(), du2.getLineageId());
    }

    @Test
    public void testMultipleVersionsInDifferentLineages() throws Exception {
        SIP sip = new SIP();

        DcsDeliverableUnit du1 = sip.addVersionWithNewFile(null, "du1");
        DcsDeliverableUnit du2 = sip.addVersionWithNewFile(du1.getId(), "du2");

        assertNull(du1.getLineageId());
        assertNull(du2.getLineageId());

        DcsDeliverableUnit dux1 = sip.addVersionWithNewFile(null, "dux1");
        DcsDeliverableUnit dux2 = sip.addVersionWithNewFile(dux1.getId(),
                "dux2");

        assertNull(dux1.getLineageId());
        assertNull(dux2.getLineageId());

        Dcp result = deposit(sip);

        assertNotNull(result);
        assertEquals(4, result.getDeliverableUnits().size());
        assertEquals(4, result.getManifestations().size());
        assertEquals(4, result.getFiles().size());

        du1 = du2 = dux1 = dux2 = null;

        for (DcsDeliverableUnit du : result.getDeliverableUnits()) {
            String s = du.getTitle();

            if (s.equals("du1")) {
                du1 = du;
            } else if (s.equals("du2")) {
                du2 = du;
            } else if (s.equals("dux1")) {
                dux1 = du;
            } else if (s.equals("dux2")) {
                dux2 = du;
            } else {
                assertFalse("Could not find dus by title", true);
            }
        }

        assertNotNull(du1);
        assertNotNull(du2);
        assertNotNull(dux1);
        assertNotNull(dux2);

        assertNotNull(du1.getLineageId());
        assertNotNull(du2.getLineageId());
        assertEquals(du1.getLineageId(), du2.getLineageId());

        assertNotNull(dux1.getLineageId());
        assertNotNull(dux2.getLineageId());
        assertEquals(dux1.getLineageId(), dux2.getLineageId());

        assertFalse(du1.getLineageId().equals(dux1.getLineageId()));
    }

    @Test
    public void testConcurrentUpdatesToSameLineage() throws Exception {
        SIP sip1 = new SIP();

        sip1.addVersionWithNewFile(null, "v11");

        Dcp result1 = deposit(sip1);

        assertNotNull(result1);
        assertEquals(1, result1.getDeliverableUnits().size());

        final DcsDeliverableUnit du1 = result1.getDeliverableUnits().iterator()
                .next();

        assertNotNull(du1.getLineageId());

        // Attempt to update the du from sip1 simultaneously from a number of
        // threads. Only one update should succeed.

        Thread[] threads = new Thread[NUM_CONCURRENT_DEPOSITS];

        final ConcurrentHashMap<String, String> success = new ConcurrentHashMap<String, String>();

        for (int i = 0; i < threads.length; i++) {
            final String thread_name = "" + i;

            threads[i] = new Thread(new Runnable() {
                public void run() {
                    SIP sip2 = new SIP();

                    try {
                        sip2.addVersion(du1.getId(), "v2 " + thread_name);
                        Dcp result2 = deposit(sip2);

                        if (result2 == null) {
                            // Another update is in progress or succeeded
                        } else {
                            assertEquals(1, result2.getDeliverableUnits()
                                    .size());
                            DcsDeliverableUnit du2 = result2
                                    .getDeliverableUnits().iterator().next();
                            assertNotNull(du2.getLineageId());
                            assertEquals(du1.getLineageId(), du2.getLineageId());

                            success.put(du2.getId(), du2.getLineageId());
                        }
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        log.info("Deposit failed: " + sw.toString());
                    }
                }
            }, thread_name);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        assertEquals(1, success.size());
    }

    @Test
    public void testConcurrentUpdatesToDifferentLineages() throws Exception {
        // Attempt to update different lineages simultaneously from a number of
        // threads. All updates should succeed.

        DcsDeliverableUnit[] predecessors = new DcsDeliverableUnit[NUM_CONCURRENT_DEPOSITS];

        for (int i = 0; i < predecessors.length; i++) {
            SIP sip = new SIP();

            sip.addVersionWithNewFile(null, "v11");

            Dcp result = deposit(sip);

            assertNotNull(result);
            assertEquals(1, result.getDeliverableUnits().size());

            final DcsDeliverableUnit du = result.getDeliverableUnits()
                    .iterator().next();

            assertNotNull(du.getLineageId());

            predecessors[i] = du;
        }

        Thread[] threads = new Thread[NUM_CONCURRENT_DEPOSITS];

        final ConcurrentHashMap<String, String> success = new ConcurrentHashMap<String, String>();

        for (int i = 0; i < threads.length; i++) {
            final String thread_name = "" + i;
            final DcsDeliverableUnit pred = predecessors[i];

            threads[i] = new Thread(new Runnable() {
                public void run() {
                    SIP sip2 = new SIP();
                    final String title = "v2 " + thread_name;

                    try {
                        sip2.addVersion(pred.getId(), title);
                        Dcp result2 = deposit(sip2);

                        assertNotNull(result2);
                        assertEquals(1, result2.getDeliverableUnits().size());
                        DcsDeliverableUnit du2 = result2.getDeliverableUnits()
                                .iterator().next();
                        assertNotNull(du2.getLineageId());

                        assertEquals(pred.getLineageId(), du2.getLineageId());

                        success.put(du2.getId(), du2.getLineageId());
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        fail("SIP deposit (with predecessor [" + pred.getId() + "] and title [" + title + "]) failed: "
                                + sw.toString());
                    }
                }
            }, thread_name);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        assertEquals(NUM_CONCURRENT_DEPOSITS, success.size());
    }
}
