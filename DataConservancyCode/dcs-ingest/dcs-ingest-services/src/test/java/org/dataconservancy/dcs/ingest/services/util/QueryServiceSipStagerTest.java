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
package org.dataconservancy.dcs.ingest.services.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.FileUtil;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class QueryServiceSipStagerTest {

    private static File baseDir;

    private static File solrhome;

    private static QueryService<DcsEntity> access;
    private static IndexService<Dcp> index;

    private static ArchiveStore archive;

    private static QueryServiceSipStager stager;

    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    @BeforeClass
    public static void init() {
        baseDir =
                new File(System.getProperty("java.io.tmpdir"), UUID
                        .randomUUID().toString());

        String archiveBaseDir = baseDir.getAbsolutePath();
        FsMetadataStore ms = new FsMetadataStore();
        ms.setBaseDir(archiveBaseDir);
        ms
                .setFilePathKeyAlgorithm(new KeyDigestPathAlgorithm("MD5",
                                                                    1,
                                                                    1,
                                                                    "m"));

        FsEntityStore es = new FsEntityStore();
        es.setBaseDir(archiveBaseDir);
        es.setFilePathKeyAlgorithm(new KeyDigestPathAlgorithm("MD5", 1, 1, ""));

        ElmArchiveStore eas = new ElmArchiveStore();
        eas.setMetadataStore(ms);
        eas.setEntityStore(es);
        eas.init();

        archive = eas;

        try {
            solrhome = FileUtil.createTempDir("solr");
            SolrService.createSolrInstall(solrhome);
            SolrService s = new SolrService(solrhome);
            index = new DcpIndexService(s);
            access = new DcsDataModelQueryService(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        stager = new QueryServiceSipStager();
        stager.setAccessService(access);
    }

    @Test
    public void retrieveExpectedSipTest() throws Exception {
        Dcp expectedSip = getRandomDcp();
        Dcp unwantedSip = getRandomDcp();

        archive.putPackage(dcpToStream(expectedSip));
        archive.putPackage(dcpToStream(unwantedSip));

        BatchIndexer<Dcp> indexer = index.index();
        indexer.add(expectedSip);
        indexer.add(unwantedSip);
        indexer.close();
        
        Dcp retrieved = stager.getSIP(getSipId(expectedSip));
        
        assertNotNull(retrieved);
        assertDcpEqual(expectedSip, retrieved);
    }

    @Test
    public void nullIfNotPresentTest() throws Exception {
        Dcp unwantedSip = getRandomDcp();
        archive.putPackage(dcpToStream(unwantedSip));

        BatchIndexer<Dcp> indexer = index.index();
        indexer.add(unwantedSip);
        indexer.close();
        
        access = new DcsDataModelQueryService(new SolrService(solrhome));
        stager.setAccessService(access);
        
        assertNull(stager.getSIP("not_present"));
    }

    @AfterClass
    public static void removeTestDir() throws Exception {
        FileUtils.deleteDirectory(baseDir);

        if (solrhome != null) {
            FileUtil.delete(solrhome);
        }
    }

    private void assertDcpEqual(Dcp one, Dcp two) throws IOException {
        /*
         * Doing a deep comparison would be very involved. This is good enough -
         * if their serializations are the same length, and their ingest
         * outcomes match,chances are they are equal.
         */
        assertEquals(IOUtils.toString(dcpToStream(one)).length(), IOUtils
                .toString(dcpToStream(two)).length());
        assertEquals(getSipId(one), getSipId(two));
    }

    private String getSipId(Dcp sip) {
        for (DcsEvent event : sip.getEvents()) {
            if (event.getEventType().equals(Events.INGEST_SUCCESS)) {
                return event.getOutcome();
            }
        }

        throw new RuntimeException("Malformed SIP");
    }

    private InputStream dcpToStream(Dcp dcp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        builder.buildSip(dcp, out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private Dcp getRandomDcp() {
        Dcp dcp = new Dcp();

        DcsCollection coll = new DcsCollection();
        coll.setId(UUID.randomUUID().toString());
        coll.setTitle("Random collection");
        dcp.addCollection(coll);

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(UUID.randomUUID().toString());
        du.setTitle("Random Title");
        du.addCollection(new DcsCollectionRef(coll.getId()));
        dcp.addDeliverableUnit(du);

        DcsFile file = new DcsFile();
        file.setId(UUID.randomUUID().toString());
        file.setSource("http://dataconservancy.org");
        file.setExtant(true);
        dcp.addFile(file);

        DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(file.getId()));

        DcsManifestation man = new DcsManifestation();
        man.setId(UUID.randomUUID().toString());
        man.setDeliverableUnit(du.getId());
        man.addManifestationFile(mf);
        dcp.addManifestation(man);

        /* Just a random event to make sure we get it back */
        DcsEvent event = new DcsEvent();
        event.setId(UUID.randomUUID().toString());
        event.setDate(DateUtility.toIso8601(DateUtility.now()));
        event.setEventType(Events.ID_ASSIGNMENT);
        event.setOutcome(file.getId());
        event.addTargets(new DcsFileRef(file.getId()));
        dcp.addEvent(event);

        DcsEvent ingestEvent = new DcsEvent();
        ingestEvent.setId(UUID.randomUUID().toString());
        ingestEvent.setDate(DateUtility.toIso8601(DateUtility.now()));
        ingestEvent.setEventType(Events.INGEST_SUCCESS);
        ingestEvent.setOutcome(UUID.randomUUID().toString());
        ingestEvent.addTargets(new DcsEntityReference(coll.getId()),
                               new DcsEntityReference(du.getId()),
                               new DcsEntityReference(file.getId()),
                               new DcsEntityReference(man.getId()),
                               new DcsEntityReference(event.getId()));
        dcp.addEvent(ingestEvent);

        return dcp;
    }
}
