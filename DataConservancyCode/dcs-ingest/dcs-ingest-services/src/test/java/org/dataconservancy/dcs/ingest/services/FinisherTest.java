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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsEntity;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityType;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FinisherTest {

    private static File baseDir;

    private static ElmArchiveStore store;

    private static SipStager stager = new MemoryStager();

    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private static final String EXAMPLE_SIP =
            "/org/dataconservancy/dcs/ingest/services/exampleDIP.xml";

    @Test
    public void eventAddedTest() throws Exception {
        Finisher finisher = getFinisher();
        ArchiveProbe probe = new ArchiveProbe();
        finisher.setArchiveStore(probe);

        String id =
                stager.addSIP(builder.buildSip(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));

        store.putPackage(this.getClass().getResourceAsStream(EXAMPLE_SIP));

        finisher.execute(id);

        Assert.assertTrue(probe.packageBeenPut);

        Dcp added =
                builder
                        .buildSip(new ByteArrayInputStream(probe.putPackageContent));

        Assert.assertEquals(1, added.getEvents().size());

    }

    @Test
    public void eventTargetsTest() throws Exception {

        Finisher finisher = getFinisher();
        ArchiveProbe probe = new ArchiveProbe();
        finisher.setArchiveStore(probe);

        List<String> ids = Arrays.asList("1", "2", "3", "4", "5", "6");

        Dcp sip = new Dcp();

        DcsCollection coll = new DcsCollection();
        coll.setId(ids.get(0));
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(ids.get(1));
        DcsEvent e = new DcsEvent();
        e.setId(ids.get(2));
        DcsFile file = new DcsFile();
        file.setId(ids.get(3));
        DcsManifestation m = new DcsManifestation();
        m.setId(ids.get(4));
        DcsManifestation m2 = new DcsManifestation();
        m2.setId(ids.get(5));

        sip.addCollection(coll);
        sip.addDeliverableUnit(du);
        sip.addEvent(e);
        sip.addFile(file);
        sip.addManifestation(m);
        sip.addManifestation(m2);

        String id = stager.addSIP(sip);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        builder.buildSip(sip, out);

        store.putPackage(new ByteArrayInputStream(out.toByteArray()));

        finisher.execute(id);

        Dcp added =
                builder
                        .buildSip(new ByteArrayInputStream(probe.putPackageContent));
        DcsEvent ingestSuccess = added.getEvents().iterator().next();

        Assert
                .assertEquals(Events.INGEST_SUCCESS, ingestSuccess
                        .getEventType());

        for (DcsEntityReference er : ingestSuccess.getTargets()) {
            Assert.assertTrue(ids.contains(er.getRef()));
        }

        Assert.assertEquals(ids.size(), ingestSuccess.getTargets().size());
    }

    @Test
    public void disableVerificationTest() throws Exception {
        Finisher finisher = getFinisher();
        ArchiveProbe probe = new ArchiveProbe();
        finisher.setArchiveStore(probe);
        finisher.setVerifyArchiveEntities(false);

        String id =
                stager.addSIP(builder.buildSip(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));

        store.putPackage(this.getClass().getResourceAsStream(EXAMPLE_SIP));

        finisher.execute(id);

        Assert.assertFalse(probe.packagesLookedAt);
    }

    @Test
    public void validationInvestagationTest() throws Exception {
        Finisher finisher = getFinisher();
        ArchiveProbe probe = new ArchiveProbe();
        finisher.setArchiveStore(probe);

        String id =
                stager.addSIP(builder.buildSip(this.getClass()
                        .getResourceAsStream(EXAMPLE_SIP)));

        store.putPackage(this.getClass().getResourceAsStream(EXAMPLE_SIP));

        finisher.execute(id);
        Assert.assertTrue(probe.packagesLookedAt);
    }

    @Test
    public void testConfirmEntitiesIndexedTest() throws Exception {
        Finisher finisher = getFinisher();
        ArchiveProbe probe = new ArchiveProbe();
        finisher.setArchiveStore(probe);
        LookupQueryServiceProbe queryProbe = new LookupQueryServiceProbe();
        finisher.setLookupQueryService(queryProbe);

        final Dcp sip = builder.buildSip(this.getClass().getResourceAsStream(EXAMPLE_SIP));

        String id = stager.addSIP(sip);

        store.putPackage(this.getClass().getResourceAsStream(EXAMPLE_SIP));

        finisher.execute(id);

        for (DcsEntity e : sip) {
            if (e instanceof DcsEvent && ((DcsEvent) e).getEventType().equals(Events.INGEST_SUCCESS)) {
                continue; // the ingest.success event is added after confirming the entities in the index
            }
            assertTrue("Expected " + e.getId() + " to have been returned by the query service",
                    queryProbe.probedEntities.contains(e.getId()));
        }


        boolean foundSuccess = false;
        for (DcsEvent e : sip.getEvents()) {
            if (e.getEventType().equals(Events.INGEST_SUCCESS)) {
               foundSuccess = true;
            }
        }

        assertTrue(foundSuccess);
    }

    @Test
    public void testIngestFailsIfEntitiesNotInIndex() throws Exception {
        Finisher finisher = getFinisher();
        ArchiveProbe probe = new ArchiveProbe();
        finisher.setArchiveStore(probe);
        LookupQueryServiceProbe queryProbe = new LookupQueryServiceProbe();
        queryProbe.missingEntities.add("urn:sdss:12345");
        finisher.setLookupQueryService(queryProbe);
        finisher.setMaxPollTimeMillis(2000);
        finisher.setPollIntervalMillis(100);

        final Dcp sip = builder.buildSip(this.getClass().getResourceAsStream(EXAMPLE_SIP));

        String id = stager.addSIP(sip);

        store.putPackage(this.getClass().getResourceAsStream(EXAMPLE_SIP));

        try {
            finisher.execute(id);
            fail("Expected RuntimeException!");
        } catch (RuntimeException e) {
            // expected
        }

        for (DcsEntity e : sip) {
            if (e instanceof DcsEvent && ((DcsEvent) e).getEventType().equals(Events.INGEST_SUCCESS)) {
                continue; // the ingest.success event is added after confirming the entities in the index
            }

            if (queryProbe.missingEntities.contains(e.getId())) {
                continue; // we purposefully skipped this entity
            }

            // We should find the rest of them
            assertTrue("Expected " + e.getId() + " to have been returned by the query service",
                    queryProbe.probedEntities.contains(e.getId()));
        }
    }

    @Test
    public void testConfirmIndexPollingParameters() throws Exception {
        Finisher f = getFinisher();

        long pollInterval = f.getPollIntervalMillis();
        long maxPoll = f.getMaxPollTimeMillis();

        assertTrue(maxPoll > 0 && pollInterval > 0);
        assertTrue(pollInterval < maxPoll);

        f.setPollIntervalMillis(maxPoll + 1);
        assertEquals(maxPoll + 1, f.getPollIntervalMillis());
        assertEquals(f.getPollIntervalMillis(), f.getMaxPollTimeMillis());

        f.setMaxPollTimeMillis(maxPoll - 1);
        assertEquals(maxPoll - 1, f.getMaxPollTimeMillis());
        assertEquals(f.getPollIntervalMillis(), f.getMaxPollTimeMillis());

        try {
            f.setMaxPollTimeMillis(0);
            fail();
        } catch (RuntimeException e) {
            // expected
        }

        try {
            f.setMaxPollTimeMillis(-1);
            fail();
        } catch (RuntimeException e) {
            // expected
        }

        try {
            f.setPollIntervalMillis(0);
            fail();
        } catch (RuntimeException e) {
            // expected
        }

        try {
            f.setPollIntervalMillis(-1);
            fail();
        } catch (RuntimeException e) {
            // expected
        }
    }

    public Finisher getFinisher() {
        IngestFramework fwk = new IngestFramework();
        InlineEventManager mgr = new InlineEventManager();
        mgr.setIdService(new MemoryIdServiceImpl());
        mgr.setSipStager(stager);

        fwk.setSipStager(stager);
        fwk.setEventManager(mgr);

        Finisher finisher = new Finisher();

        finisher.setIngestFramework(fwk);
        finisher.setModelBuilder(builder);
        finisher.setLookupQueryService(new LookupQueryServiceProbe());

        return finisher;
    }

    private class ArchiveProbe
            implements ArchiveStore {

        public boolean packageBeenPut = false;

        public boolean packagesLookedAt = false;

        public byte[] putPackageContent;

        public InputStream getContent(String entityId)
                throws EntityNotFoundException, EntityTypeException {
            return store.getContent(entityId);
        }

        public InputStream getFullPackage(String entityId)
                throws EntityNotFoundException {
            return store.getFullPackage(entityId);
        }

        public InputStream getPackage(String entityId)
                throws EntityNotFoundException {
            packagesLookedAt = true;
            return store.getPackage(entityId);
        }

        public Iterator<String> listEntities(EntityType type) {
            return store.listEntities(type);
        }

        public void putPackage(InputStream dcpStream) throws AIPFormatException {
            packageBeenPut = true;
            ByteArrayOutputStream trap = new ByteArrayOutputStream();

            try {
                IOUtils.copy(dcpStream, trap);
            } catch (Exception e) {
                throw new RuntimeException();
            }

            putPackageContent = trap.toByteArray();
            store.putPackage(new ByteArrayInputStream(putPackageContent));
        }

    }

    private class LookupQueryServiceProbe implements LookupQueryService<DcsEntity> {
        private Set<String> probedEntities = new HashSet<String>();
        private Set<String> missingEntities = new HashSet<String>();

        @Override
        public DcsEntity lookup(String id) throws QueryServiceException {
            if (missingEntities.contains(id)) {
                return null;
            }
            
            probedEntities.add(id);
            DcsEntity e = new DcsEntity();
            e.setId(id);
            // we have to return something, so make it something semi-sensible (the elm archive store throws exceptions)
            return e;
        }

        @Override
        public QueryResult<DcsEntity> query(String query, long offset, int matches, String... params) throws QueryServiceException {
            throw new UnsupportedOperationException("Not supported/needed for tests.");
        }

        @Override
        public void shutdown() throws QueryServiceException {
            // do nothing
        }
    }

    @BeforeClass
    public static void init() {
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());

        store = new ElmArchiveStore();

        FilePathKeyAlgorithm eAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 1, null);
        FilePathKeyAlgorithm mAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 1, ".md");

        FsEntityStore eStore = new FsEntityStore();
        eStore.setBaseDir(baseDir.getAbsolutePath());
        eStore.setFilePathKeyAlgorithm(eAlg);
        store.setEntityStore(eStore);

        FsMetadataStore mStore = new FsMetadataStore();
        mStore.setBaseDir(baseDir.getAbsolutePath());
        mStore.setFilePathKeyAlgorithm(mAlg);
        store.setMetadataStore(mStore);

        store.init();
    }

    @AfterClass
    public static void removeTestDir() throws Exception {
        FileUtils.deleteDirectory(baseDir);
    }
}
