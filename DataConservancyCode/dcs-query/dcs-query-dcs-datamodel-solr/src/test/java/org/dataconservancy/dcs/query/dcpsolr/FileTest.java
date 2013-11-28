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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;

public class FileTest extends AbstractSearchTest {
    /**
     * This test composes a single SIP with a simple object graph:
     * DU->Manifestation->ManifestationFile->File
     * 
     * The ManifestationFile asserts it is metadata for the DU using
     * DcsRelationship.IS_METADATA_FOR.
     * 
     * After performing some sanity checks, this test asserts that the
     * Manifestation containing the ManifestationFile can be be discovered by
     * searching for the relationship and that the XML metadata is indexed with
     * the Deliverable Unit.
     * 
     * @throws Exception
     */

    public void testManifestationFileRelationships() throws Exception {
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("du");

        // Create a DCS File entity and content for the file.
        final File content = createTempXMLFile("<blah>Cows are very fond of metadata.</blah>");

        final DcsFile file = new DcsFile();

        file.setId("file");
        file.setSource(new URL("file://" + content.getCanonicalPath())
                .toString());

        // Compose a DCP sip containing the file, put it in the archive, and
        // index it.

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

        index(man, file, du);

        // Get it back
        final DcsEntity e = query_service.lookupEntity(man.getId());

        // Sanity: Assert the original manifestation is the same as the
        // manifestation returned by the access service.
        assertNotNull(e);
        assertEquals(man, e);

        // Sanity: Perform a search of the metadata.
        // The xml is associated with the target of the relation
        QueryResult<DcsEntity> result = query_service.query(SolrQueryUtil
                .createLiteralQuery(
                        DcsSolrField.MetadataField.SEARCH_TEXT.solrName(),
                        "metadata"), 0, -1);

        assertNotNull(result);
        assertEquals(0, result.getOffset());
        assertNotNull(result.getMatches());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());

        QueryMatch<DcsEntity> m = result.getMatches().get(0);
        assertNotNull(m);
        assertEquals(du, m.getObject());

        // Perform a search of the relationships
        result = query_service.query(SolrQueryUtil.createLiteralQuery(
                DcsSolrField.RelationField.RELATION.solrName(),
                DcsRelationship.IS_METADATA_FOR.asString()), 0, -1);

        assertNotNull(result);
        assertEquals(0, result.getOffset());
        assertNotNull(result.getMatches());
        assertEquals(1, result.getMatches().size());
        assertEquals(1, result.getTotal());

        m = result.getMatches().get(0);
        assertNotNull(m);
        assertEquals(man, m.getObject());
    }

    private File createTempXMLFile(String contents) throws IOException {
        File tmp = File.createTempFile("tmpfile", ".xml");
        tmp.deleteOnExit();

        FileWriter w = new FileWriter(tmp);
        w.write(contents);
        w.close();

        return tmp;
    }

    /**
     * This test is a sanity check, verifying that DcsFile entities may be
     * returned as a search result from the access API, in addition to
     * DeliverableUnits and Manifestations.
     * 
     * @throws Exception
     */
    public void testMetadataRefIndexingUsingFileEntity() throws Exception {
        final DcsFile md = new DcsFile();
        final String xml = "<root><foo><bar>Some metadata about a File</bar></foo></root>";

        File tmp = createTempXMLFile(xml);

        md.setId("id:/metadata");
        md.setName(tmp.getName());
        md.setSource(new URL("file://" + tmp.getAbsolutePath()).toString());

        final DcsFile data = new DcsFile();

        tmp = createTempXMLFile("Some file with data.");

        data.setId("id:/data");
        data.setName(tmp.getName());
        data.setSource(new URL("file://" + tmp.getAbsolutePath()).toString());

        data.addMetadataRef(new DcsMetadataRef(md.getId()));

        assertNull(query_service.lookupEntity("id:/data"));
        assertNull(query_service.lookupEntity("id:/metadata"));

        index(data, md);

        assertNotNull(query_service.lookupEntity("id:/data"));
        assertNotNull(query_service.lookupEntity("id:/metadata"));
        assertEquals(data, query_service.lookupEntity("id:/data"));
        assertEquals(md, query_service.lookupEntity("id:/metadata"));

        final QueryResult<DcsEntity> sr = query_service.query(
                DcsSolrField.MetadataField.SEARCH_TEXT.solrName() + ":some", 0,
                -1);
        assertNotNull(sr);
        assertEquals(1, sr.getTotal());
        assertEquals(0, sr.getOffset());
        assertNotNull(sr.getMatches());
        assertEquals(1, sr.getMatches().size());
        assertEquals(data, sr.getMatches().get(0).getObject());
    }

    public void testSolrMetadataRefIndexing() throws Exception {
        DcsDeliverableUnit du = rb.createDeliverableUnit(null, null, false);
        DcsFile file = new DcsFile();
        DcsMetadataRef ref = new DcsMetadataRef();

        File ds = createTempXMLFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>This is some metadata</root>");

        file.setId("test");
        file.setSource(new URL("file://" + ds.getCanonicalPath()).toString());

        ref.setRef(file.getId());
        du.addMetadataRef(ref);

        try {
            index(file, du);

            DcsEntity e = query_service.lookupEntity(file.getId());
            assertNotNull(e);
            assertEquals(file, e);

            QueryResult<DcsEntity> result = query_service.query(
                    "metadataSearchText:some", 0, -1);

            assertEquals(0, result.getOffset());
            assertEquals(1, result.getMatches().size());
            assertEquals(1, result.getTotal());

            QueryMatch<DcsEntity> match = result.getMatches().get(0);
            assertEquals(du, match.getObject());
        } finally {
            ds.delete();
        }
    }
}
