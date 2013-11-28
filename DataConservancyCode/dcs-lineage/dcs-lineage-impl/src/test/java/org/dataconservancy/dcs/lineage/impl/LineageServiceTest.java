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

package org.dataconservancy.dcs.lineage.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.DcpUtil;
import org.dataconservancy.dcs.index.dcpsolr.FileUtil;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.api.LineageService;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LineageServiceTest {
   
    //The index service to be used to index our sips for the query service
    private DcpIndexService indexService;
    private SolrService solr;
    private ArchiveStore archiveStore;
    protected File archivehome;
    protected File solrhome;
    private LineageService lineageService;
    
    //A list of the entity ids in order so we can check the returned lineage.
    private ArrayList<String> entityIds;
    
    public IdService idService;
    
    private String originalEntityID;
    private String middleEntityID;
    private String latestEntityID;
    private String singleEntityID;
    private String nonexistentEntityId = "idString";
    
    private String lineageID;
    private String singleEntityLineageID;
    
    private final static String ORIGINAL_INGEST_DATE = "2012-01-01T10:59:59Z";
    private final static long ORIGINAL_INGEST_DATE_MILLIS = 1325415599000l;
    
    private final static String MIDDLE_INGEST_DATE = "2012-01-08T10:59:59Z";
    private final static long MIDDLE_INGEST_DATE_MILLIS = 1326020399000l;
    
    private final static String LATEST_INGEST_DATE = "2012-01-25T10:59:59Z";
    private final static long LATEST_INGEST_DATE_MILLIS = 1327489199000l;
    
    private final static String SINGLE_ENTITY_INGEST_DATE = "2012-01-02T10:59:59Z";
    private final static long SINGLE_ENTITY_INGEST_DATE_MILLIS = 1325501999000l;

    @Before
    public void setup() throws Exception{
        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        solr = new SolrService(solrhome);

        archivehome = FileUtil.createTempDir("archive");
        archiveStore = createTestArchiveStore(archivehome);
        indexService = new DcpIndexService(archiveStore, solr);
        
        idService = new MemoryIdServiceImpl();
        Identifier nextID = idService.create(Types.DELIVERABLE_UNIT.getTypeName());
        originalEntityID = nextID.toString();
        
        nextID = idService.create(Types.DELIVERABLE_UNIT.getTypeName());
        middleEntityID = nextID.toString();
        
        nextID = idService.create(Types.DELIVERABLE_UNIT.getTypeName());
        latestEntityID = nextID.toString();
        
        nextID = idService.create(Types.DELIVERABLE_UNIT.getTypeName());
        singleEntityID = nextID.toString();
        
        nextID = idService.create(Types.LINEAGE.getTypeName());
        lineageID = nextID.toString();
        
        nextID = idService.create(Types.LINEAGE.getTypeName());
        singleEntityLineageID = nextID.toString();
        
        lineageService = new LineageServiceImpl(solr, idService);

        //Set up sips for to be indexed so they can be retrieved with the lineage service
        buildAndIndexSips();
        
        //Add the entity ids in descending order to match the lineage service impl return order
        entityIds = new ArrayList<String>();
        entityIds.add(latestEntityID);
        entityIds.add(middleEntityID);
        entityIds.add(originalEntityID);
    }
    
    /**
     * Tests getting the lineage using an id. 
     * Tests cases: using an entity id, using a lineage id,
     * using an id whose entity or lineage can't be found, retrieving a single entity lineage
     */
    @Test
    public void testGetLineage() {
        Lineage lineage = lineageService.getLineage(originalEntityID);
        Iterator<LineageEntry> lineageIter = lineage.iterator();
        testReturnedLineage(lineageIter, 0, 2, false);
         
        Lineage singleLineage = lineageService.getLineage(singleEntityID);
        Iterator<LineageEntry> singleLineageIter = singleLineage.iterator();
        testReturnedLineage(singleLineageIter, 0, 0, true);
        
        //Test the lineage id
        Lineage lineageIDLineage = lineageService.getLineage(lineageID);
        Iterator<LineageEntry> lineageIDIter = lineageIDLineage.iterator();
        testReturnedLineage(lineageIDIter, 0, 2, false);

        //Test an id that doesn't exist
        Lineage nullLineage = lineageService.getLineage(nonexistentEntityId);
        assertNull(nullLineage);
    }
                                                
    /**
     * Tests the entry range call in the lineage API. 
     * Tests cases: getting an entire lineage, getting a subset of a lineage,
     * passing the first argument as null, passing the second argument as null, entities from two different lineages, entities that are not in order,
     * and testing an id that can't be found, retrieving a single entity lineage
     */
    @Test
    public void testGetLineageForEntityRange() {
        //Test getting the entire lineage
        Lineage lineage = lineageService.getLineageForEntityRange(originalEntityID, latestEntityID);
        Iterator<LineageEntry> lineageIter = lineage.iterator();
        testReturnedLineage(lineageIter, 0, 2, false);
        
        //Test getting a subset of the lineage
        Lineage subsetLineage = lineageService.getLineageForEntityRange(originalEntityID, middleEntityID);
        Iterator<LineageEntry> subsetLineageIter = subsetLineage.iterator();
        testReturnedLineage(subsetLineageIter, 1, 2, false);
        
        //Test passing in null for the first argument
        Lineage firstToSecondLineage = lineageService.getLineageForEntityRange(null, middleEntityID);
        Iterator<LineageEntry> firstToSecondLineageIter = firstToSecondLineage.iterator();
        testReturnedLineage(firstToSecondLineageIter, 1, 2, false);
        
        //Test passing in null for the second argument
        Lineage secondToLatestLineage = lineageService.getLineageForEntityRange(middleEntityID, null);
        Iterator<LineageEntry> secondToLatestLineageIter = secondToLatestLineage.iterator();
        testReturnedLineage(secondToLatestLineageIter, 0, 1, false);
        
        //Test using single entity lineage
        Lineage singleEntityLineageFirstNull = lineageService.getLineageForEntityRange(null, singleEntityID);
        Iterator<LineageEntry> singleEntityLineageFirstNullIter = singleEntityLineageFirstNull.iterator();
        testReturnedLineage(singleEntityLineageFirstNullIter, 0, 0, true);
        
        Lineage singleEntityLineageSecondNull = lineageService.getLineageForEntityRange(singleEntityID, null);
        Iterator<LineageEntry> singleEntityLineageSecondNullIter = singleEntityLineageSecondNull.iterator();
        testReturnedLineage(singleEntityLineageSecondNullIter, 0, 0, true);

        //Test passing in entities in wrong order returns null
        Lineage wrongOrderLineage = lineageService.getLineageForEntityRange(latestEntityID, originalEntityID);
        assertNull(wrongOrderLineage);
        
        //Test passing in entities from two different lineages
        Lineage differentLineage =   lineageService.getLineageForEntityRange(singleEntityID, originalEntityID);
        assertNull(differentLineage);
        
        //Test passing in id for a non-existent entity
        Lineage nonexistentEntityLineage = lineageService.getLineageForEntityRange(nonexistentEntityId, null);
        assertNull(nonexistentEntityLineage);

    }
    
    /**
     * Tests the date range call in the API. 
     * Test cases: getting an entire lineage, returning a subset of the lineage, null first argument, null second argument,
     * retrieving a single entity lineage, passing in dates in the wrong order, first and second date are same as entity timestamp, 
     * dates that contain no lineage, passing in entity id that can't be found.
     */
    @Test
    public void testGetLineageForDateRange() {
        
        //Test getting the entire lineage
        Lineage lineage = lineageService.getLineageForDateRange(originalEntityID, ORIGINAL_INGEST_DATE_MILLIS, LATEST_INGEST_DATE_MILLIS);
        Iterator<LineageEntry> lineageIter = lineage.iterator();
        testReturnedLineage(lineageIter, 0, 2, false);
        
        //Test getting a subset of the lineage
        Lineage subsetLineage = lineageService.getLineageForDateRange(originalEntityID, ORIGINAL_INGEST_DATE_MILLIS, MIDDLE_INGEST_DATE_MILLIS);
        Iterator<LineageEntry> subsetLineageIter = subsetLineage.iterator();
        testReturnedLineage(subsetLineageIter, 1, 2, false);
        
        //Test passing in null for the first argument
        Lineage firstToSecondLineage = lineageService.getLineageForDateRange(originalEntityID, -1, MIDDLE_INGEST_DATE_MILLIS);
        Iterator<LineageEntry> firstToSecondLineageIter = firstToSecondLineage.iterator();
        testReturnedLineage(firstToSecondLineageIter, 1, 2, false);
        
        //Test passing in null for the second argument
        Lineage secondToLatestLineage = lineageService.getLineageForDateRange(originalEntityID, MIDDLE_INGEST_DATE_MILLIS, -1);
        Iterator<LineageEntry> secondToLatestLineageIter = secondToLatestLineage.iterator();
        testReturnedLineage(secondToLatestLineageIter, 0, 1, false);
        
        //Test single entity lineage
        Lineage singleEntityLineageFirstNull = lineageService.getLineageForDateRange(singleEntityID, -1, SINGLE_ENTITY_INGEST_DATE_MILLIS);
        Iterator<LineageEntry> singleEntityLineageFirstNullIter = singleEntityLineageFirstNull.iterator();
        testReturnedLineage(singleEntityLineageFirstNullIter, 0, 0, true);
        
        Lineage singleEntityLineageSecondNull = lineageService.getLineageForDateRange(singleEntityID, SINGLE_ENTITY_INGEST_DATE_MILLIS, -1);
        Iterator<LineageEntry> singleEntityLineageSecondNullIter = singleEntityLineageSecondNull.iterator();
        testReturnedLineage(singleEntityLineageSecondNullIter, 0, 0, true);
        
        //Test passing in wrong order of dates returns null
        Lineage wrongOrderLineage = lineageService.getLineageForDateRange(originalEntityID, LATEST_INGEST_DATE_MILLIS, ORIGINAL_INGEST_DATE_MILLIS);
        assertNull(wrongOrderLineage);
        
        //Test passing in identical dates equal to an entity's timestamp returns a lineage containing that entity
        Lineage bothDatesTheSameLineage =  lineageService.getLineageForDateRange(singleEntityID, SINGLE_ENTITY_INGEST_DATE_MILLIS, SINGLE_ENTITY_INGEST_DATE_MILLIS);
        Iterator<LineageEntry> bothDatesTheSameLineageIter = bothDatesTheSameLineage.iterator();
        testReturnedLineage(bothDatesTheSameLineageIter, 0, 0, true);
        
        //Test passing in dates with no entity contained  - should return an empty lineage, not null
        Lineage emptyLineage = lineageService.getLineageForDateRange(originalEntityID, ORIGINAL_INGEST_DATE_MILLIS+1, MIDDLE_INGEST_DATE_MILLIS-1);
        assertNotNull(emptyLineage);
        assertFalse(emptyLineage.iterator().hasNext());

        //Test null lineage returned if entity is not found
        Lineage nonexistentEntityLineage = lineageService.getLineageForDateRange(nonexistentEntityId, ORIGINAL_INGEST_DATE_MILLIS, LATEST_INGEST_DATE_MILLIS);
        assertNull(nonexistentEntityLineage);

    }
    
    /**
     * Tests the call to get the latest of a lineage in the API.
     * Test cases: Passing in an entity id, passing in id of a single entity lineage, passing in an entity that doesn't exist
     */
    @Test
    public void testGetLatest() {
        //Test getting the latest 
        LineageEntry latest = lineageService.getLatest(middleEntityID);
        assertTrue(latest.getEntityId().equalsIgnoreCase(latestEntityID));
        
        //Test single entity lineage
        LineageEntry singleLineage = lineageService.getLatest(singleEntityID);
        assertTrue(singleLineage.getEntityId().equalsIgnoreCase(singleEntityID));

        //Test null lineage returned if entity is not found
        LineageEntry nonexistentEntityLineageEntry = lineageService.getLatest(nonexistentEntityId);
        assertNull(nonexistentEntityLineageEntry);

    }
    
    /**
     * Tests the call to the original item of a lineage in the API.
     * Test cases: Passing in an entity id, passing in id of a single entity lineage, passing in id of an entity that doesn't exist. 
     */
    @Test
    public void testGetOriginal() {
        //Test getting the original
        LineageEntry original = lineageService.getOriginal(middleEntityID);
        assertTrue(original.getEntityId().equalsIgnoreCase(originalEntityID));
        
        //Test single entity lineage
        LineageEntry singleLineage = lineageService.getOriginal(singleEntityID);
        assertTrue(singleLineage.getEntityId().equalsIgnoreCase(singleEntityID));

        //Test null lineage returned if entity is not found
        LineageEntry nonexistentEntityLineageEntry = lineageService.getOriginal(nonexistentEntityId);
        assertNull(nonexistentEntityLineageEntry);

    }
    
    /**
     * Tests the call to get the entity for a specific date. 
     * Test cases: Passing in a date prior to entity, passing in the exact date of entity, passing a date that is prior to any entities in the lineage,
     * passing in an entity id that doesn't exist.
     */
    @Test
    public void testGetEntryForDate() {
        
        //Test getting an entry by date
        LineageEntry middleEntry = lineageService.getEntryForDate(originalEntityID, LATEST_INGEST_DATE_MILLIS-50);
        assertTrue(middleEntry.getEntityId().equalsIgnoreCase(middleEntityID));
        
        //Test attempting to get a non valid date
        LineageEntry nonEntry = lineageService.getEntryForDate(originalEntityID, ORIGINAL_INGEST_DATE_MILLIS-100000);
        assertNull(nonEntry);

        //Test getting an entry by using the exact date
        LineageEntry exactDateEntry = lineageService.getEntryForDate(originalEntityID, MIDDLE_INGEST_DATE_MILLIS);
        assertTrue(exactDateEntry.getEntityId().equalsIgnoreCase(middleEntityID));
        
        //Test null lineage returned if entity is not found
        LineageEntry nonexistentEntityLineageEntry = lineageService.getEntryForDate(nonexistentEntityId, ORIGINAL_INGEST_DATE_MILLIS - 100000);
        assertNull(nonexistentEntityLineageEntry);

    }
    
    /**
     * Tests the is latest call in the api.
     * Test cases: Passing the id of an entity that is latest in the lineage, passing in the id of entity that is not latest, 
     * passing in the id of a single entity lineage, passing in the id of an entity that doesn't exist
     */
    @Test
    public void testIsLatest() {
        //Test the latest in the lineage
        assertTrue(lineageService.isLatest(latestEntityID));
        
        //Test entity that isn't latest
        assertFalse(lineageService.isLatest(middleEntityID));
        
        //Test single entity lineage
        assertTrue(lineageService.isLatest(singleEntityID));

        //Test for non-existent lineage
        assertFalse(lineageService.isOriginal(nonexistentEntityId));
    }
    
    /**
     * Tests the is original call in the api.
     * Test cases: Passing the id of an entity that is original in the lineage, passing in the id of entity that is not the original, 
     * passing in the id of a single entity lineage, passing in the id of an entity that doesn't exist
     */
    @Test
    public void testIsOriginal() {
        //Test the original in the lineage
        assertTrue(lineageService.isOriginal(originalEntityID));
        
        //Test entity that isn't latest
        assertFalse(lineageService.isOriginal(middleEntityID));
        
        //Test single entity lineage
        assertTrue(lineageService.isOriginal(singleEntityID));

        //Test for non-existent lineage
        assertFalse(lineageService.isOriginal(nonexistentEntityId));
    }

    private void testReturnedLineage(Iterator<LineageEntry> lineageIter, int startIndex, int expectedLastIndex, boolean isSingleLineage) {
        int i = startIndex;
        while (lineageIter.hasNext()) {
            LineageEntry entry = lineageIter.next();
            if (isSingleLineage) {
                assertTrue(entry.getEntityId().equalsIgnoreCase(singleEntityID));
            } else {
                assertTrue(entry.getEntityId().equalsIgnoreCase(entityIds.get(i)));
            }
            
            if (lineageIter.hasNext()) {
                i++;                
            }
        }
        
        Assert.assertEquals(i, expectedLastIndex);
    }
    
    private static ArchiveStore createTestArchiveStore(File baseDir) {
        FilePathKeyAlgorithm eAlg = new KeyDigestPathAlgorithm("MD5", 1, 2,
                null);

        FilePathKeyAlgorithm mAlg = new KeyDigestPathAlgorithm("MD5", 1, 2,
                ".md");

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
    
    @After
    public void tearDown() throws IOException {
        if (indexService != null) {
            indexService.shutdown();
        }

        if (solrhome != null) {
            FileUtil.delete(solrhome);
        }

        if (archivehome != null) {
            FileUtil.delete(archivehome);
        }
    }

    protected void index(Dcp dcp) throws IndexServiceException,
            AIPFormatException {
        // Package must be in archive so indexing service can lookup files

        if (archiveStore != null) {
            archiveStore.putPackage(DcpUtil.asInputStream(dcp));
        }

        BatchIndexer<Dcp> batch = indexService.index();
        batch.add(dcp);
        batch.close();

        indexService.optimize();
    }
    
    private void buildAndIndexSips() throws Exception {
        Dcp originalSip = new Dcp();
        
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(originalEntityID);
        du.setLineageId(lineageID);
        // Create a DCS File entity and content for the file.
        File dataFile = File.createTempFile("tmpfile", ".xml");
        dataFile.deleteOnExit();

        FileWriter w = new FileWriter(dataFile);
        w.write("<text>This file is going to be updated.</text>");
        w.close();

        final DcsFile file = new DcsFile();

        file.setId("file");
        file.setSource(new URL("file://" + dataFile.getCanonicalPath())
                .toString());
    
        // Create the Manifestation
        final DcsManifestation man = new DcsManifestation();
        man.setDeliverableUnit(du.getId());
        man.setId("man");

        // The Manifestation is composed of a ManifestationFile, and the DcsFile
        // created above.
        final DcsManifestationFile mf = new DcsManifestationFile();
        mf.setPath("/");
        mf.setRef(new DcsFileRef(file.getId()));

        // The ManifestationFile is metadata for the Manifestation's deliverable
        // unit
        final DcsRelation rel = new DcsRelation(
                DcsRelationship.IS_METADATA_FOR, man.getDeliverableUnit());
        mf.addRel(rel);

        man.addManifestationFile(mf);

        DcsEvent e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(ORIGINAL_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));
        
        DcpUtil.add(originalSip, man, file, du, e);
        index(originalSip);

        //Update the entity id of the du and index the new sip.
        Dcp updateSip = new Dcp();
        du.setId(middleEntityID);
        e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(MIDDLE_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));
        
        DcpUtil.add(updateSip, man, file, du, e);
        index(updateSip);

        //Update the entity id of the du and index the second update sip.
        Dcp secondUpdateSip = new Dcp();
        du.setId(latestEntityID);
        
        e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(LATEST_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));
        
        DcpUtil.add(secondUpdateSip, man, file, du, e);
        index(secondUpdateSip);
        
        //Finally change the entity id and the lineage id and index the sip.
        //This is to ensure only the items in the lineage are returned
        Dcp newLineageSip = new Dcp();
        du.setId(singleEntityID);
        du.setLineageId(singleEntityLineageID);
        
        e = new DcsEvent();
        e.setId("DepositID");
        e.setEventType("ingest.complete");
        e.setDate(SINGLE_ENTITY_INGEST_DATE);
        e.addTargets(new DcsEntityReference(du.getId()));

        DcpUtil.add(newLineageSip, man, file, du, e);
        index(newLineageSip);
    }
}
