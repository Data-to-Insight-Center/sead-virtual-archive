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
package org.dataconservancy.dcs.index.dcpsolr;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.DateUtil;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.util.DateUtility;
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

/**
 * Test additional fields added to targets of events, for the ancestor graph,
 * and for XML metadata.
 */
public class AdditionalFieldsTest extends AbstractIndexTest {
    // Do not load into the archive in order to deal with random DcsFile
    // sources.
    public void setUp() throws Exception {
        super.setUp();
        archive = null;
    }

    public void testLastModifed() throws IOException, IndexServiceException,
            AIPFormatException {
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("du");
        du.setTitle("hahah");

        DcsEvent event = new DcsEvent();
        event.setId("event");
        event.setEventType("ingest.complete");
        Date ingestdate = new Date();
        event.setDate(DateUtil.getThreadLocalDateFormat().format(ingestdate));
        event.addTargets(new DcsEntityReference(du.getId()));

        index(du, event);

        long lastmod = service.lookupEntityLastModified(du.getId());

        assertEquals(ingestdate.getTime(), lastmod);
    }

    private boolean hasAncestry(DcsEntity entity, DcsEntity... ancestors)
            throws IOException, SolrServerException {
        String[] ids = new String[ancestors.length];

        for (int i = 0; i < ancestors.length; i++) {
            ids[i] = ancestors[i].getId();
        }

        return hasFieldValues(entity.getId(),
                DcsSolrField.EntityField.ANCESTRY.solrName(), ids);
    }

    private boolean hasEventOutcome(DcsEntity entity, String outcome)
            throws IOException, SolrServerException {
        return hasFieldValues(entity.getId(),
                DcsSolrField.EventField.OUTCOME.solrName(), outcome);
    }

    private boolean hasEventDate(DcsEntity entity, String type, String date)
            throws IOException, SolrServerException {
        SolrDocument doc = solr.lookupSolrDocument(entity.getId());
        assertNotNull(doc);

        if (doc.containsKey("event_date_" + type)) {
            for (Object o : doc.getFieldValues("event_date_" + type)) {
                if (((Date) o).getTime() == DateUtility.parseDate(date)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    private boolean hasSingleEventDateField(DcsEntity entity, String type) throws Exception {
        SolrDocument doc = solr.lookupSolrDocument(entity.getId());
        assertNotNull(doc);

        return doc.getFieldValues("event_date_" + type).size() == 1;
    }

    public void testEventsAndAncestry() throws Exception {
        DcsCollection top = rb.createCollection(null);
        top.setTitle("top");

        DcsCollection col = rb.createCollection(top.getId());
        col.setTitle("col");

        DcsDeliverableUnit dutop = rb.createDeliverableUnit(col.getId(), null,
                false);
        dutop.setTitle("dutop");

        DcsDeliverableUnit du = rb.createDeliverableUnit(top.getId(), null,
                false);
        du.setTitle("du");

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

        DcsEvent ev = rb.createEvent(null);
        ev.setOutcome("outcome");
        ev.setEventType("ingest.complete");
        ev.addTargets(new DcsEntityReference(file.getId()));
        ev.addTargets(new DcsEntityReference(col.getId()));
        ev.addTargets(new DcsEntityReference(dutop.getId()));
        ev.addTargets(new DcsEntityReference(man.getId()));

        Dcp dcp = DcpUtil.add(null, dutop, top, col, du, mdfile, man, ev, file);
        DcpUtil.add(dcp, files);

        index(dcp);

        assertTrue(hasAncestry(col, top));
        assertTrue(hasAncestry(top));
        assertTrue(hasAncestry(dutop, col, top));
        assertTrue(hasAncestry(du, dutop, col, top));
        assertTrue(hasAncestry(man, dutop, col, top));
        assertTrue(hasAncestry(file, man, dutop, col, top));
        assertTrue(hasAncestry(mdfile, du, dutop, col, top));

        assertTrue(hasEventOutcome(col, ev.getOutcome()));
        assertTrue(hasEventOutcome(dutop, ev.getOutcome()));
        assertFalse(hasEventOutcome(du, ev.getOutcome()));
        assertTrue(hasEventOutcome(man, ev.getOutcome()));
        assertTrue(hasEventOutcome(file, ev.getOutcome()));

        for (DcsEntity e : files) {
            assertTrue(hasAncestry(e, man, dutop, col, top));
            assertFalse(hasEventOutcome(e, ev.getOutcome()));
        }

        assertTrue(hasEventDate(col, ev.getEventType(), ev.getDate()));
        assertTrue(hasEventDate(dutop, ev.getEventType(), ev.getDate()));
        assertFalse(hasEventDate(du, ev.getEventType(), ev.getDate()));
        assertTrue(hasEventDate(man, ev.getEventType(), ev.getDate()));
        assertTrue(hasEventDate(file, ev.getEventType(), ev.getDate()));
    }

    public void testDynamicRelationship() throws Exception {
        DcsDeliverableUnit du1 = rb.createDeliverableUnit(null, null, false);
        du1.setTitle("du1");

        final String preduri = "urn:predicate/blah";
        
        DcsDeliverableUnit du2 = rb.createDeliverableUnit(null, null,
                false);
        du2.setTitle("du2");

        DcsRelation rel = new DcsRelation();
        rel.setRelUri(preduri);
        rel.setRef(new DcsEntityReference(du2.getId()));
        du1.addRelation(rel);
        
        Dcp dcp = DcpUtil.add(null, du1, du2);
        
        index(dcp);
        
        assertNotNull(service.lookupEntity(du1.getId()));
        assertNotNull(service.lookupEntity(du2.getId()));
        
        assertTrue(hasFieldValues(du1.getId(),
                 "rel_" + preduri, du2.getId()));
    }

    public void testManifestationPointingToExistingFileAncestry() throws Exception {
        DcsDeliverableUnit du1 = rb.createDeliverableUnit(null, null, false);
        du1.setTitle("du1");
        du1.setId("du1");
        
        DcsManifestation man1 = new DcsManifestation();
        man1.setId("man1");
        
        DcsFile file1 = new DcsFile();
        file1.setId("file1");
        
        DcsManifestationFile mf1 = new DcsManifestationFile();
        
        man1.setDeliverableUnit(du1.getId());
        mf1.setRef(new DcsFileRef(file1.getId()));
        man1.addManifestationFile(mf1);
        
        index(du1, man1, file1);

        assertTrue(hasAncestry(file1, man1, du1));
        
        
        DcsDeliverableUnit du2 = rb.createDeliverableUnit(null, null, false);
        du2.setTitle("du2");
        du2.setId("du2");
        
        DcsManifestation man2 = new DcsManifestation();
        man2.setId("man2");
        
        DcsManifestationFile mf2 = new DcsManifestationFile();
        
        man2.setDeliverableUnit(du2.getId());
        mf2.setRef(new DcsFileRef(file1.getId()));
        man2.addManifestationFile(mf1);
        
        index(du2, man2);
        
        assertTrue(hasAncestry(file1, man1, du1, man2, du2));
    }
    
    public void testXMLMetadata() throws Exception {
        String xml = "<root><rdf:RDF "
                + "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' "
                + "xmlns:dc='http://purl.org/dc/elements/1.1/'>"
                + "<rdf:Description rdf:about='http://media.example.com/audio/guide.ra'>"
                + "<dc:creator>Rose Bush</dc:creator>"
                + "<dc:title>A Guide to Growing Roses</dc:title>"
                + "<dc:description>Describes process for planting and nurturing different kinds of rose bushes.</dc:description>"
                + "<dc:date>2001-01-20</dc:date>"
                + "</rdf:Description>"
                + "</rdf:RDF> Here is some <hi rend='italic'>text</hi> in root</root>";

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("test");

        DcsMetadata md = new DcsMetadata();
        md.setMetadata(xml);

        du.addMetadata(md);

        index(du);

        assertTrue(hasFieldContainingSubstring(du.getId(),
                "metadataSearchText", "some"));
        assertTrue(hasFieldContainingSubstring(du.getId(),
                "ext_/root/RDF/Description/creator", "Rose"));
        assertFalse(hasFieldContainingSubstring(du.getId(),
                "ext_/root/RDF/Description/tite", "process"));
        assertTrue(hasFieldContainingSubstring(du.getId(),
                "metadataSearchText", "kinds"));
        assertTrue(hasFieldContainingSubstring(du.getId(), "ext_/root/hi@rend",
                "italic"));
    }
    
    /**
     * Make sure there is only ever one event_date_TYPE field in an event target.
     */
    public void testSingleEventDateTypeField() throws Exception {
        // Two events with same type point to same entity
        
        DcsCollection top = rb.createCollection(null);
        top.setTitle("top");

        DcsEvent ev = rb.createEvent(null);
        ev.setOutcome("outcome");
        ev.setEventType("ingest.complete");
        ev.addTargets(new DcsEntityReference(top.getId()));

        DcsEvent ev2 = rb.createEvent(null);
        ev2.setOutcome("outcome2");
        ev2.setEventType(ev.getEventType());
        ev2.addTargets(new DcsEntityReference(top.getId()));
        

        index(top, ev, ev2);
        
        assertTrue(hasSingleEventDateField(top, ev.getEventType()));
        
        // Add event with same type to already indexed entity
                
        DcsEvent ev3 = rb.createEvent(null);
        ev3.setOutcome("outcome3");
        ev3.setEventType(ev.getEventType());
        ev3.addTargets(new DcsEntityReference(top.getId()));

        index(ev3);
        
        assertTrue(hasSingleEventDateField(top, ev3.getEventType()));
        assertTrue(hasEventDate(top, ev3.getEventType(), ev3.getDate()));
    }
}
