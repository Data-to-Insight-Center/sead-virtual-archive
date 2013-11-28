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

import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import org.dataconservancy.archive.api.EntityNotFoundException;
import org.dataconservancy.archive.api.EntityTypeException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;

public class FileTest extends AbstractIndexTest {
    public void testIndexingMetadataRef() throws Exception {
        DcsFile file = new DcsFile();
        file.setId("cowfile");

        String xml = "<barn><moo>Cows are the best.</moo></barn>";
        File tmp = File.createTempFile("cow", ".xml");
        tmp.deleteOnExit();

        FileWriter out = new FileWriter(tmp);
        out.write(xml);
        out.close();

        file.setName(tmp.getName());
        file.setSource(new URL("file://" + tmp.getCanonicalPath()).toString());

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("farm");
        du.addMetadataRef(new DcsMetadataRef(file.getId()));

        index(file, du);

        try {
            archive.getContent(file.getId());
        } catch (EntityNotFoundException e) {
            assertTrue(false);
        } catch (EntityTypeException e) {
            assertTrue(false);
        }

        assertTrue(hasFieldContainingSubstring(du.getId(),
                DcsSolrField.MetadataField.SEARCH_TEXT.solrName(), "best"));
        assertFalse(hasFieldContainingSubstring(du.getId(),
                DcsSolrField.MetadataField.SEARCH_TEXT.solrName(), "moo"));
        assertTrue(hasFieldContainingSubstring(du.getId(), "ext_/barn/moo",
                "best"));
    }

    /**
     * Make sure that a metadata file pointing to a du gets indexed in the du.
     * 
     * @throws Exception
     */
    public void testIndexingManifestationFile() throws Exception {
        DcsFile file = new DcsFile();

        String xml = "<barn><moo>Cows are often the best.</moo></barn>";
        File tmp = File.createTempFile("cow", ".xml");
        tmp.deleteOnExit();

        FileWriter out = new FileWriter(tmp);
        out.write(xml);
        out.close();

        file.setId("anothercow");
        file.setName(tmp.getName());
        file.setSource(new URL("file://" + tmp.getCanonicalPath()).toString());

        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("farm");

        DcsManifestation man = new DcsManifestation();
        man.setId("farmer");

        DcsManifestationFile mf = new DcsManifestationFile();
        mf.setRef(new DcsFileRef(file.getId()));

        DcsRelation rel = new DcsRelation();
        rel.setRelUri(DcsRelationship.IS_METADATA_FOR.asString());
        rel.setRef(new DcsEntityReference(du.getId()));

        mf.addRel(rel);

        man.addManifestationFile(mf);

        index(file, man, du);

        try {
            archive.getContent(file.getId());
        } catch (EntityNotFoundException e) {
            assertTrue(false);
        } catch (EntityTypeException e) {
            assertTrue(false);
        }

        assertTrue(hasFieldContainingSubstring(du.getId(),
                DcsSolrField.MetadataField.SEARCH_TEXT.solrName(), "best"));
        assertFalse(hasFieldContainingSubstring(man.getId(),
                DcsSolrField.MetadataField.SEARCH_TEXT.solrName(), "best"));
        assertFalse(hasFieldContainingSubstring(du.getId(),
                DcsSolrField.MetadataField.SEARCH_TEXT.solrName(), "moo"));
        assertTrue(hasFieldContainingSubstring(du.getId(), "ext_/barn/moo",
                "best"));
    }
}
