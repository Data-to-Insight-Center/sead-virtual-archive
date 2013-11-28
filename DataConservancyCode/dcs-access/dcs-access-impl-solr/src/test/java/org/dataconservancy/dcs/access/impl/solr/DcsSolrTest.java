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
package org.dataconservancy.dcs.access.impl.solr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import org.apache.solr.common.util.DateUtil;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.access.api.IndexWriter;
import org.dataconservancy.dcs.access.api.Match;
import org.dataconservancy.dcs.access.api.SearchResult;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadata;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class DcsSolrTest
        extends TestCase {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private DcsSolrAccessService access;

    private RandomEntityBuilder rb;

    private File solrhome;

    private File archivehome;

    private ArchiveStore archive;

    private static ArchiveStore getTestArchiveStore(File baseDir) {
        FilePathKeyAlgorithm eAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 2, null);

        FilePathKeyAlgorithm mAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 2, ".md");

        FsMetadataStore mStore = new FsMetadataStore();
        mStore.setFilePathKeyAlgorithm(mAlg);
        mStore.setBaseDir(baseDir.getPath());

        FsEntityStore eStore = new FsEntityStore();
        eStore.setFilePathKeyAlgorithm(eAlg);
        eStore.setBaseDir(baseDir.getPath());

        ElmArchiveStore aStore = new ElmArchiveStore();
        aStore.setMetadataStore(mStore);
        aStore.setEntityStore(eStore);
        aStore.init();

        return aStore;
    }

    public void setUp() throws Exception {
        // TODO wrong
        //        Logger log = Logger.getLogger("org.apache.solr");
        //        log.setUseParentHandlers(false);
        //        log.setLevel(Level.SEVERE);

        rb = new RandomEntityBuilder();

        archivehome = FileUtil.createTempDir("archive");
        archive = getTestArchiveStore(archivehome);

        solrhome = TestUtil.createSolrTestInstall();
        access = new DcsSolrAccessService(solrhome, archive);

        access.clearIndex();
    }

    public void tearDown() throws IOException {
        if (access != null) {
            access.shutdown();
        }

        if (solrhome != null) {
            FileUtil.delete(solrhome);
        }

        if (archivehome != null) {
            FileUtil.delete(archivehome);
        }
    }

    /**
     * Torture test that generates a bunch of random objects, writes them to the
     * index, reads them back and checks that they survived the transformation.
     */
    public void testSolrMapping() throws Exception {

        List<DcsEntity> entities = createEntities(10, 10);

        IndexWriter writer = access.updateIndex();

        for (DcsEntity entity : entities) {
            writer.add(entity);
        }

        writer.close();

        access.optimizeIndex();

        System.out.println("bytes per entity: "
                + (double) FileUtil.size(solrhome) / entities.size());

        for (DcsEntity entity : entities) {
            DcsEntity e = access.getEntity(entity.getId());

            assertNotNull(e);

            //System.err.println("WANT: " + entity);
            //System.err.println("GOT:  " + e);

            assertEquals(entity, e);
        }

        assertNull(access.getEntity("doesnotexist"));

        assertNull(access.getEntity("*"));
        assertNull(access.getEntity("t*"));
        assertNull(access.getEntity("!(asdjkfa({"));
        assertNull(access.getEntity(">?>{}{{DS[aldsk!#$@#Q$#323q''a"));
        assertNull(access.getEntity("\"blahsd"));
        assertNull(access.getEntity(entities.get(0).getId() + "*"));
    }

    //    public void testLargeIndex() throws Exception {
    //        int numcollection = 1000;
    //        int numdu = 10000;
    //        int numevent = 1000;
    //
    //        IndexWriter writer = access.updateIndex();
    //
    //        int count = 0;
    //
    //        for (int i = 0; i < numcollection; i++) {
    //            writer.add(rb.createCollection(null));
    //            count++;
    //        }
    //
    //        for (int i = 0; i < numevent; i++) {
    //            writer.add(rb.createEvent());
    //            count++;
    //        }
    //
    //        for (int i = 0; i < numdu; i++) {
    //            DcsDeliverableUnit du = rb.createDeliverableUnit(null);
    //
    //            writer.add(du);
    //            count++;
    //
    //            List<DcsFile> files = new ArrayList<DcsFile>();
    //            DcsManifestation man = rb.createManifestation(du.getId(), files);
    //
    //            writer.add(man);
    //            count++;
    //
    //            for (DcsFile file : files) {
    //                writer.add(file);
    //                count++;
    //            }
    //        }
    //
    //        writer.close();
    //
    //        access.optimizeIndex();
    //
    //        System.out.println("bytes per entity: "
    //                + (double) FileUtil.size(solrhome) / count);
    //
    //        System.out.println("index size: " + FileUtil.size(solrhome));
    //
    //        assertNull(access.getEntity("doesnotexist"));
    //
    //        System.out.println("Num entities: " + count);
    //    }

    public List<DcsEntity> createEntities(int numdu, int numevent) {
        List<DcsEntity> entities = new ArrayList<DcsEntity>();

        Random rand = new Random();

        for (int i = 0; i < 20; i++) {
            String colparent = null;

            if (entities.size() > 0 && rand.nextInt(10) == 0) {
                Collections.shuffle(entities);

                for (DcsEntity entity : entities) {
                    if (entity instanceof DcsCollection) {
                        colparent = entity.getId();
                    }
                }
            }

            DcsCollection col = rb.createCollection(colparent);
            entities.add(col);

            for (int j = 0; j < numdu; j++) {
                DcsDeliverableUnit du = rb.createDeliverableUnit(col.getId());
                entities.add(du);

                List<DcsFile> files = new ArrayList<DcsFile>();
                entities.add(rb.createManifestation(du.getId(), files));
                entities.addAll(files);
            }
        }

        for (int i = 0; i < numevent; i++) {
            entities.add(rb.createEvent());
        }

        return entities;
    }

    /**
     * Torture test that generates a bunch of random objects, writes them to the
     * archive, and indexes the archive.
     */
    public void testIndexingArchive() throws Exception {
        List<DcsEntity> entities = createEntities(10, 10);
        Dcp dcp = DcpUtil.add(null, entities);

        archive.putPackage(DcpUtil.asInputStream(dcp));

        access.indexArchive();
        access.optimizeIndex();

        for (DcsEntity entity : entities) {
            DcsEntity e = access.getEntity(entity.getId());

            assertNotNull(e);
            assertEquals(entity, e);
        }

        assertNull(access.getEntity("doesnotexist"));
    }

    public void testDatastreams() throws Exception {
        DcsFile file = new DcsFile();
        File ds = File.createTempFile("datastream", null);

        byte[] dscontents = new byte[] {0, 1, 2, 3, 4, 5, 6, 7};

        FileOutputStream out = new FileOutputStream(ds);
        out.write(dscontents);
        out.close();

        file.setId("test");
        file.setSource(ds.toURI().toString());
        file.setSizeBytes(dscontents.length);

        try {
            Dcp dcp = DcpUtil.add(null, file);

            archive.putPackage(DcpUtil.asInputStream(dcp));

            access.indexArchive();
            access.optimizeIndex();

            DcsEntity e = access.getEntity(file.getId());
            assertNotNull(e);
            assertEquals(file, e);

            InputStream is = access.getDatastream(file.getId());

            assertNotNull(is);

            ByteArray result = new ByteArray(10);
            result.append(is);

            assertEquals(dscontents.length, result.length);
            for (int i = 0; i < dscontents.length; i++) {
                assertEquals(dscontents[i], result.array[i]);
            }

            assertNull(access.getDatastream("blah"));
        } finally {
            ds.delete();
        }
    }

    /**
     * Torture test that generates a bunch of random objects, writes them to the
     * archive, and indexes the archive.
     */
    public void testSolrMappingCollection() throws IOException {
        IndexWriter writer = access.updateIndex();

        DcsCollection col = rb.createCollection("parent");
        writer.add(col);
        writer.close();

        DcsEntity e = access.getEntity(col.getId());

        assertNotNull(e);
        assertEquals(col, e);
    }

    public void testSolrMappingManifestation() throws IOException {
        IndexWriter writer = access.updateIndex();

        List<DcsFile> files = new ArrayList<DcsFile>();
        DcsManifestation man = rb.createManifestation("du", files);
        writer.add(man);
        for (DcsEntity file : files) {
            writer.add(file);

        }
        writer.close();

        DcsEntity e = access.getEntity(man.getId());

        //System.out.println("WANT :" + man);
        //System.out.println("GOT  :" + e);

        assertNotNull(e);
        assertEquals(man, e);
    }

    public void testSolrMappingManifestationWithDateCreated() throws IOException {
        IndexWriter writer = access.updateIndex();

        DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit("du");
        man.setDateCreated(DateUtility.toIso8601(DateUtility.now()));
        man.setId("man");

        writer.add(man);
        writer.close();

        DcsEntity e = access.getEntity(man.getId());

        assertNotNull(e);
        assertEquals(man, e);
    }

    /**
     * This test composes a single SIP with a simple object graph: DU->Manifestation->ManifestationFile->File
     *
     * The ManifestationFile asserts it is metadata for the DU using DcsRelationship.IS_METADATA_FOR.
     *
     * After performing some sanity checks, this test asserts that the Manifestation containing the ManifestationFile
     * can be be discovered by searching for the relationship.
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws AIPFormatException
     */
    public void testSolrMappingManifestationFileRelationships() throws IOException, URISyntaxException, AIPFormatException {
        final IndexWriter writer = access.updateIndex();

        // Create a DCS File entity and content for the file.
        final File content = File.createTempFile("DcsSolrTest-testSolrMappingManifestationFileRelationships", ".tmp");
        FileUtils.writeStringToFile(content, "<root>this is some metadata</root>");
        final DcsFile file = new DcsFile();
        String contentPath = content.getAbsolutePath().replace('\\', '/');
        file.setId("file");
        file.setSource(new URI("file://" + contentPath).toString());

        // Compose a DCP sip containing the file, put it in the archive, and index it.
        final Dcp fileSip = DcpUtil.add(null, file);
        archive.putPackage(DcpUtil.asInputStream(fileSip));
        access.indexArchive();
        access.optimizeIndex();

        // Create the Manifestation
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit("du");
        man.setId("man");

        // The Manifestation is composed of a ManifestationFile, and the DcsFile created above.
        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setPath("/");
        mf.setRef(new DcsFileRef(file.getId()));

        // The ManifestationFile is metadata for the Manifestation's deliverable unit
        final DcsRelation rel = new DcsRelation(DcsRelationship.IS_METADATA_FOR, man.getDeliverableUnit());
        mf.addRel(rel);

        man.addManifestationFile(mf);

        // Index the manifestation
        writer.add(man);
        writer.close();

        // Get it back
        final DcsEntity e = access.getEntity(man.getId());

        // Sanity: Assert the original manifestation is the same as the manifestation returned by the access service.
        assertNotNull(e);
        assertEquals(man, e);

        // Sanity: Perform a search of the metadata
        SearchResult result = access.search(
                QueryUtil.createLiteralQuery(DcsSolrField.MetadataField.SEARCH_TEXT.solrName(), "metadata"),
                0, -1);
        assertNotNull(result);
        assertEquals(0, result.getOffset());
        assertNotNull(result.getMatches());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());

        Match m = result.getMatches().get(0);
        assertNotNull(m);
        assertEquals(man, m.getEntity());

        // Perform a search of the relationships
        result = access.search(
                QueryUtil.createLiteralQuery(DcsSolrField.RelationField.RELATION.solrName(),
                        DcsRelationship.IS_METADATA_FOR.asString()), 0, -1);
        assertNotNull(result);
        assertEquals(0, result.getOffset());
        assertNotNull(result.getMatches());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());

        m = result.getMatches().get(0);
        assertNotNull(m);
        assertEquals(man, m.getEntity());
    }

    /**
     * This test is a sanity check, verifying that DcsFile entities may be returned as a search result from the access
     * API, in addition to DeliverableUnits and Manifestations.
     * 
     * @throws Exception
     */
    public void testMetadataRefIndexingUsingFileEntity() throws Exception {
        final DcsFile md = new DcsFile();
        final String xml = "<root><foo><bar>Some metadata about a File</bar></foo></root>";
        File tmp = File.createTempFile("metadata", ".xml");
        tmp.deleteOnExit();
        FileUtils.writeStringToFile(tmp, xml, "UTF-8");
        md.setId("id:/metadata");
        md.setName(tmp.getName());
        md.setSource(new URL("file://" + tmp.getAbsolutePath()).toString());

        final DcsFile data = new DcsFile();
        tmp = File.createTempFile("data", ".xml");
        tmp.deleteOnExit();
        FileUtils.writeStringToFile(tmp, "Some file with data.", "UTF-8");
        data.setId("id:/data");
        data.setName(tmp.getName());
        data.setSource(new URL("file://" + tmp.getAbsolutePath()).toString());

        data.addMetadataRef(new DcsMetadataRef(md.getId()));

        final Dcp dcp = new Dcp();
        DcpUtil.add(dcp, md, data);

        assertNull(access.getEntity("id:/data"));
        assertNull(access.getEntity("id:/metadata"));
        archive.putPackage(DcpUtil.asInputStream(dcp));
        access.indexArchive();

        assertNotNull(access.getEntity("id:/data"));
        assertNotNull(access.getEntity("id:/metadata"));
        assertEquals(data, access.getEntity("id:/data"));
        assertEquals(md, access.getEntity("id:/metadata"));

        final SearchResult sr = access.search(DcsSolrField.MetadataField.SEARCH_TEXT.solrName() + ":some", 0, -1);
        assertNotNull(sr);
        assertEquals(1, sr.getTotal());
        assertEquals(0, sr.getOffset());
        assertNotNull(sr.getMatches());
        assertEquals(1, sr.getMatches().size());
        assertEquals(data, sr.getMatches().get(0).getEntity());
    }

    public void testSolrMetadataRefIndexing() throws Exception {
        DcsDeliverableUnit du = rb.createDeliverableUnit(null);
        DcsFile file = new DcsFile();
        DcsMetadataRef ref = new DcsMetadataRef();

        File ds = File.createTempFile("datastream", null);
        PrintWriter pw = new PrintWriter(ds);
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<root>This is some metadata</root>";
        pw.print(xml);
        pw.close();

        file.setId("test");
        file.setSource(ds.toURI().toString());

        ref.setRef(file.getId());
        du.addMetadataRef(ref);

        try {
            Dcp dcp = DcpUtil.add(null, file, du);

            archive.putPackage(DcpUtil.asInputStream(dcp));

            access.indexArchive();
            access.optimizeIndex();

            DcsEntity e = access.getEntity(file.getId());
            assertNotNull(e);
            assertEquals(file, e);

            SearchResult result =
                    access.search("metadataSearchText:some", 0, -1);

            assertEquals(0, result.getOffset());
            assertEquals(1, result.getMatches().size());
            assertEquals(1, result.getTotal());
            Match match = result.getMatches().get(0);
            assertEquals(du, match.getEntity());
        } finally {
            ds.delete();
        }
    }

    public void testSolrMappingEvent() throws IOException {
        IndexWriter writer = access.updateIndex();

        DcsEvent ev = rb.createEvent();
        writer.add(ev);
        writer.close();

        DcsEntity e = access.getEntity(ev.getId());

        assertNotNull(e);
        assertEquals(ev, e);
    }

    public void testSolrMappingFile() throws IOException {
        IndexWriter writer = access.updateIndex();

        DcsFile file = rb.createFile();
        writer.add(file);
        writer.close();

        DcsEntity e = access.getEntity(file.getId());

        System.out.println("WANT " + file);
        System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(file, e);
    }

    public void testSolrMappingDU() throws IOException {
        IndexWriter writer = access.updateIndex();

        DcsDeliverableUnit du = rb.createDeliverableUnit("blah");
        writer.add(du);
        writer.close();

        DcsEntity e = access.getEntity(du.getId());

        System.out.println("WANT " + du);
        System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(du, e);
    }

    public void testSolorMappingDUWithRights() throws IOException {
        IndexWriter writer = access.updateIndex();

        DcsDeliverableUnit du = rb.createDeliverableUnit("blah");

        du.setRights("this is a rights statement.");
        writer.add(du);
        writer.close();

        DcsEntity e = access.getEntity(du.getId());

        System.out.println("WANT " + du);
        System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(du, e);
    }

    public void testSolorMappingDUWithRightsAndLicenseURI() throws IOException, URISyntaxException {
        IndexWriter writer = access.updateIndex();

        DcsDeliverableUnit du = rb.createDeliverableUnit("blah");
        
        du.setRights("this is a rights statement.");
             
        writer.add(du);
        writer.close();

        DcsEntity e = access.getEntity(du.getId());

        System.out.println("WANT " + du);
        System.out.println("GOT  " + e);

        assertNotNull(e);
        assertEquals(du, e);
    }


    public void testSearch() throws IOException {
        List<DcsEntity> entities = new ArrayList<DcsEntity>();

        int numdu = 10;
        for (int i = 0; i < numdu; i++) {
            DcsDeliverableUnit du = rb.createDeliverableUnit(null);
            entities.add(du);

            List<DcsFile> files = new ArrayList<DcsFile>();
            entities.add(rb.createManifestation(du.getId(), files));
            entities.addAll(files);
        }

        IndexWriter writer = access.updateIndex();

        for (DcsEntity entity : entities) {
            writer.add(entity);
        }

        writer.close();

        access.optimizeIndex();

        SearchResult result =
                access.search("entityType:DeliverableUnit", 0, numdu / 2);

        assertNotNull(result);

        assertEquals(numdu / 2, result.getMatches().size());
        assertEquals(numdu, result.getTotal());

        nextmatch: for (Match match : result.getMatches()) {
            for (DcsEntity entity : entities) {
                if (entity.getId().equals(match.getEntity().getId())) {
                    assertEquals(DcsDeliverableUnit.class, match.getEntity()
                            .getClass());
                    assertEquals(entity, match.getEntity());
                    continue nextmatch;
                }
            }

            assertTrue(false);
        }

        SearchResult resultnext =
                access.search("entityType:DeliverableUnit", result.getOffset()
                        + result.getMatches().size(), 10);

        assertEquals(numdu / 2, result.getMatches().size());
        assertEquals(numdu, result.getTotal());

        assertNotSame(result.getMatches(), resultnext.getMatches());

        SearchResult resultempty = access.search("id:doesnotexist", 0, -1);

        assertEquals(0, resultempty.getOffset());
        assertEquals(0, resultempty.getTotal());
        assertEquals(0, resultempty.getMatches().size());
    }

    public void testXMLSearch() throws IOException {
        String xml =
                "<root><rdf:RDF "
                        + "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' "
                        + "xmlns:dc='http://purl.org/dc/elements/1.1/'>"
                        + "<rdf:Description rdf:about='http://media.example.com/audio/guide.ra'>"
                        + "<dc:creator>Rose Bush</dc:creator>"
                        + "<dc:title>A Guide to Growing Roses</dc:title>"
                        + "<dc:description>Describes process for planting and nurturing different kinds of rose bushes.</dc:description>"
                        + "<dc:date>2001-01-20</dc:date>"
                        + "</rdf:Description>"
                        + "</rdf:RDF> Here is some <hi rend='italic'>text</hi> in root</root>";

        IndexWriter writer = access.updateIndex();

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("test");

        DcsMetadata md = new DcsMetadata();
        md.setMetadata(xml);

        du.addMetadata(md);

        writer.add(du);
        writer.close();

        SearchResult result = access.search("metadataSearchText:some", 0, -1);

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        Match match = result.getMatches().get(0);
        assertEquals(du, match.getEntity());

        result = access.search("ext_/root/RDF/Description/creator:rose", 0, -1);

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getEntity());

        result =
                access.search("metadataSearchText:bush",
                              0,
                              -1,
                              "hl",
                              "true",
                              "hl.fl",
                              "metadataSearchText");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getEntity());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("Bush"));

        result =
                access.search("ext_/root/RDF/Description/creator:rose",
                              0,
                              -1,
                              "hl",
                              "true",
                              "hl.requireFieldMatch",
                              "true",
                              "hl.fl",
                              "*");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getEntity());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("Rose"));

        result =
                access.search("ext_/root/hi@rend:italic",
                              0,
                              -1,
                              "hl",
                              "true",
                              "hl.fl",
                              "ext_/root/hi@rend");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getEntity());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("italic"));

        result =
                access.search("ext_/root/RDF/Description/creator:rose",
                              0,
                              -1,
                              "hl",
                              "true",
                              "hl.fl",
                              "ext_/root/RDF/Description/creator");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getEntity());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("Rose"));

    }

    private boolean isAncestor(DcsEntity entity, DcsEntity ancestor)
            throws IOException {
        SearchResult sr =
                access.search(QueryUtil.createLiteralQuery("AND", "id", entity
                        .getId(), "ancestry", ancestor.getId()), 0, 20);

        return sr.getMatches().size() == 1;
    }

    private boolean matchesEventOutcome(DcsEntity entity, String string)
            throws IOException {
        SearchResult sr =
                access.search(QueryUtil.createLiteralQuery("AND", "id", entity
                        .getId(), "eventOutcome", string), 0, 20);

        return sr.getMatches().size() == 1;
    }

    private boolean matchesTypeDate(DcsEntity entity, String type, String term)
            throws IOException {
        SearchResult sr =
                access.search(QueryUtil
                        .createLiteralQuery("id", entity.getId())
                        + " AND event_date_" + type + ":" + term, 0, 20);

        return sr.getMatches().size() == 1;
    }

    // check event targets and ancestors

    public void testSecondPassIndexing() throws IOException {
        DcsCollection top = rb.createCollection(null);
        DcsCollection col = rb.createCollection(top.getId());

        DcsDeliverableUnit dutop = rb.createDeliverableUnit(col.getId());
        DcsDeliverableUnit du = rb.createDeliverableUnit(top.getId());
        du.addParent(new DcsDeliverableUnitRef(dutop.getId()));

        List<DcsFile> files = new ArrayList<DcsFile>();
        DcsManifestation man = rb.createManifestation(dutop.getId(), files);

        DcsManifestationFile manfile = new DcsManifestationFile();
        manfile.setPath("moo");
        DcsFile file = rb.createFile();

        manfile.setRef(new DcsFileRef(file.getId()));
        man.addManifestationFile(manfile);

        DcsFile mdfile = rb.createFile();
        DcsMetadataRef mdref = new DcsMetadataRef();
        mdref.setRef(mdfile.getId());
        du.addMetadataRef(mdref);

        DcsEvent ev = rb.createEvent();
        ev.setOutcome("outcome");
        ev.setEventType("ingest");
        ev.addTargets(new DcsEntityReference(file.getId()));
        ev.addTargets(new DcsEntityReference(col.getId()));
        ev.addTargets(new DcsEntityReference(dutop.getId()));
        ev.addTargets(new DcsEntityReference(man.getId()));

        IndexWriter w = access.updateIndex();
        w.add(dutop);
        w.add(top);
        w.add(col);
        w.add(du);
        w.add(mdfile);
        w.add(man);
        w.add(ev);

        for (DcsEntity e : files) {
            w.add(e);
        }

        w.add(file);

        w.close();

        assertTrue(isAncestor(col, top));
        assertFalse(isAncestor(top, col));

        assertTrue(isAncestor(dutop, col));
        assertTrue(isAncestor(dutop, top));
        assertFalse(isAncestor(col, dutop));

        assertTrue(isAncestor(du, dutop));
        assertTrue(isAncestor(du, col));
        assertTrue(isAncestor(du, top));

        assertTrue(isAncestor(man, dutop));
        assertTrue(isAncestor(man, col));
        assertTrue(isAncestor(man, top));
        assertFalse(isAncestor(man, du));

        for (DcsEntity e : files) {
            assertTrue(isAncestor(e, top));
            assertTrue(isAncestor(e, dutop));
            assertFalse(isAncestor(e, du));
            assertTrue(isAncestor(e, man));
        }

        assertTrue(isAncestor(file, top));
        
        assertTrue(isAncestor(mdfile, du));
        assertTrue(isAncestor(mdfile, dutop));
        assertFalse(isAncestor(mdfile, man));

        assertTrue(matchesEventOutcome(col, "outcome"));
        assertTrue(matchesEventOutcome(dutop, "outcome"));
        assertFalse(matchesEventOutcome(du, "outcome"));
        assertTrue(matchesEventOutcome(man, "outcome"));
        assertTrue(matchesEventOutcome(file, "outcome"));

        for (DcsEntity e : files) {
            assertFalse(matchesEventOutcome(e, "outcome"));
        }

        assertTrue(matchesTypeDate(col, ev.getEventType(), "[* TO *]"));
        assertTrue(matchesTypeDate(dutop, ev.getEventType(), "[* TO *]"));
        assertTrue(matchesTypeDate(man, ev.getEventType(), "[* TO *]"));
        assertTrue(matchesTypeDate(file, ev.getEventType(), "[* TO *]"));
        assertFalse(matchesTypeDate(du, ev.getEventType(), "[* TO *]"));
    }

    public void testLastModifed() throws IOException {
        IndexWriter writer = access.updateIndex();

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("du");
        du.setTitle("hahah");

        DcsEvent event = new DcsEvent();
        event.setId("event");
        event.setEventType("ingest");
        Date ingestdate = new Date();
        event.setDate(DateUtil.getThreadLocalDateFormat().format(ingestdate));
        event.addTargets(new DcsEntityReference(du.getId()));

        writer.add(du);
        writer.add(event);

        writer.close();

        long lastmod = access.getEntityLastModified(du.getId());

        assertEquals(ingestdate.getTime(), lastmod);
    }
}
