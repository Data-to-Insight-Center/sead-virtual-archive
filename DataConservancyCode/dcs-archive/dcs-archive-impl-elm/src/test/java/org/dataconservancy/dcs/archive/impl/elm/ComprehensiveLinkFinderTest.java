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
package org.dataconservancy.dcs.archive.impl.elm;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.dataconservancy.archive.impl.elm.ComprehensiveLinkFinder;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFileRef;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.dataconservancy.model.dcs.DcsManifestationFile;
import org.dataconservancy.model.dcs.DcsMetadataRef;
import org.dataconservancy.model.dcs.DcsRelation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ComprehensiveLinkFinderTest {

    private ComprehensiveLinkFinder finder = new ComprehensiveLinkFinder();

    @Test
    public void collectionRelsTest() {
        DcsCollection c = new DcsCollection();

        Set<String> refs = new HashSet<String>();

        for (int i = 0; i < 3; i++) {
            c.addMetadataRef(new DcsMetadataRef(recordRef(refs)));
        }

        c.setParent(new DcsCollectionRef(recordRef(refs)));

        Set<String> found = finder.getOutboundLinks(c).keySet();

        assertEquals(refs.size(), found.size());
        assertTrue(found.containsAll(refs));
    }

    @Test
    public void deliverableUnitRelsTest() {
        DcsDeliverableUnit du = new DcsDeliverableUnit();

        Set<String> refs = new HashSet<String>();

        for (int i = 0; i < 3; i++) {
            du.addCollection(new DcsCollectionRef(recordRef(refs)));

            du.addMetadataRef(new DcsMetadataRef(recordRef(refs)));

            du.addParent(new DcsDeliverableUnitRef(recordRef(refs)));

            DcsRelation rel = new DcsRelation();

            rel.setRef(new DcsMetadataRef(recordRef(refs)));
            du.addRelation(rel);
        }

        Set<String> found = finder.getOutboundLinks(du).keySet();

        assertEquals(refs.size(), found.size());
        assertTrue(found.containsAll(refs));
    }

    @Test
    public void manifestationRelsTest() {
        DcsManifestation m = new DcsManifestation();
        Set<String> refs = new HashSet<String>();

        for (int i = 0; i < 3; i++) {
            m.addMetadataRef(new DcsMetadataRef(recordRef(refs)));

            DcsManifestationFile mf = new DcsManifestationFile();
            mf.setPath("/");
            mf.setRef(new DcsFileRef(recordRef(refs)));
            m.addManifestationFile(mf);
        }

        m.setDeliverableUnit(recordRef(refs));

        Set<String> found = finder.getOutboundLinks(m).keySet();

        assertEquals(refs.size(), found.size());
        assertTrue(found.containsAll(refs));
    }

    @Test
    public void fileRelsTest() {
        DcsFile f = new DcsFile();
        Set<String> refs = new HashSet<String>();

        for (int i = 0; i < 3; i++) {
            f.addMetadataRef(new DcsMetadataRef(recordRef(refs)));
        }

        Set<String> found = finder.getOutboundLinks(f).keySet();

        assertEquals(refs.size(), found.size());
        assertTrue(found.containsAll(refs));
    }

    @Test
    public void eventRelsTest() {
        DcsEvent e = new DcsEvent();
        Set<String> refs = new HashSet<String>();

        for (int i = 0; i < 3; i++) {
            e.addTargets(new DcsCollectionRef(recordRef(refs)));
        }

        Set<String> found = finder.getOutboundLinks(e).keySet();

        assertEquals(refs.size(), found.size());
        assertTrue(found.containsAll(refs));
    }

    private String recordRef(Set<String> refs) {
        String ref = DcpUtil.randomId();
        refs.add(ref);
        return ref;
    }
}
