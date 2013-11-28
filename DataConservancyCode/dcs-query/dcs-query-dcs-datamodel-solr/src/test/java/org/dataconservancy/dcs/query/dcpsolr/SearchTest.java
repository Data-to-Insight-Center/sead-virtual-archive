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
package org.dataconservancy.dcs.query.dcpsolr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.dcs.index.dcpsolr.DcpUtil;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
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

/**
 * Test various searches involving entity attributes and relationships.
 */
public class SearchTest extends AbstractSearchTest {
    // Do not load into the archive in order to deal with random DcsFile
    // sources.
    public void setUp() throws Exception {
        super.setUp();
        archive = null;
    }

    private boolean isAncestor(DcsEntity entity, DcsEntity ancestor)
            throws IOException, QueryServiceException {
        QueryResult<DcsEntity> sr = query_service.query(SolrQueryUtil
                .createLiteralQuery("AND", "id", entity.getId(), "ancestry",
                        ancestor.getId()), 0, 20);

        if (sr.getMatches().size() != 1) {
            return false;
        }

        return sr.getMatches().get(0).getObject().equals(entity);
    }

    private boolean matchesEventOutcome(DcsEntity entity, String string)
            throws IOException, QueryServiceException {
        QueryResult<DcsEntity> sr = query_service.query(SolrQueryUtil
                .createLiteralQuery("AND", "id", entity.getId(),
                        "eventOutcome", string), 0, 20);

        if (sr.getMatches().size() != 1) {
            return false;
        }

        return sr.getMatches().get(0).getObject().equals(entity);
    }

    private boolean matchesTypeDate(DcsEntity entity, String type, String term)
            throws IOException, QueryServiceException {

        QueryResult<DcsEntity> sr = query_service.query(
                SolrQueryUtil.createLiteralQuery("id", entity.getId())
                        + " AND event_date_" + type + ":" + term, 0, 20);

        if (sr.getMatches().size() != 1) {
            return false;
        }

        return sr.getMatches().get(0).getObject().equals(entity);
    }

    public void testEventsAndAncestors() throws Exception {
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

    public void testSimpleSearches() throws Exception {
        Dcp dcp = rb.createDcp(5, 5);

        index(dcp);

        QueryResult<DcsEntity> result = query_service.query(
                "entityType:DeliverableUnit", 0, -1);

        assertNotNull(result);
        assertEquals(0, result.getOffset());
        assertEquals(dcp.getDeliverableUnits().size(), result.getMatches()
                .size());
        assertEquals(dcp.getDeliverableUnits().size(), result.getTotal());

        for (QueryMatch<DcsEntity> match : result.getMatches()) {
            assertTrue(dcp.getDeliverableUnits().contains(match.getObject()));
        }

        int middle_offset = dcp.getDeliverableUnits().size() / 2;
        int middle_size = dcp.getDeliverableUnits().size() - middle_offset;

        QueryResult<DcsEntity> middle_result = query_service.query(
                "entityType:DeliverableUnit", middle_offset, middle_size);

        assertNotNull(middle_result);
        assertEquals(middle_offset, middle_result.getOffset());
        assertEquals(middle_size, middle_result.getMatches().size());
        assertEquals(dcp.getDeliverableUnits().size(), result.getTotal());

        assertNotSame(result.getMatches(), middle_result.getMatches());

        QueryResult<DcsEntity> empty_result = query_service.query(
                "id:doesnotexist", 0, -1);

        assertNotNull(empty_result);
        assertEquals(0, empty_result.getOffset());
        assertEquals(0, empty_result.getTotal());
        assertEquals(0, empty_result.getMatches().size());
    }

    /**
     * Test various searches of indexed xml and make sure context is returned
     * when appropriate solr parameters are passed.
     * 
     * @throws Exception
     */
    public void testXMLSearch() throws Exception {
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

        QueryResult<DcsEntity> result = query_service.query(
                "metadataSearchText:some", 0, -1);

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        QueryMatch<DcsEntity> match = result.getMatches().get(0);
        assertEquals(du, match.getObject());

        result = query_service.query("ext_/root/RDF/Description/creator:rose",
                0, -1);

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getObject());

        result = query_service.query("metadataSearchText:bush", 0, -1, "hl",
                "true", "hl.fl", "metadataSearchText");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getObject());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("Bush"));

        result = query_service.query("ext_/root/RDF/Description/creator:rose",
                0, -1, "hl", "true", "hl.requireFieldMatch", "true", "hl.fl",
                "*");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getObject());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("Rose"));

        result = query_service.query("ext_/root/hi@rend:italic", 0, -1, "hl",
                "true", "hl.fl", "ext_/root/hi@rend");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getObject());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("italic"));

        result = query_service.query("ext_/root/RDF/Description/creator:rose",
                0, -1, "hl", "true", "hl.fl",
                "ext_/root/RDF/Description/creator");

        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());
        match = result.getMatches().get(0);
        assertEquals(du, match.getObject());

        assertNotNull(match.getContext());
        assertTrue(match.getContext().contains("Rose"));

    }
}
