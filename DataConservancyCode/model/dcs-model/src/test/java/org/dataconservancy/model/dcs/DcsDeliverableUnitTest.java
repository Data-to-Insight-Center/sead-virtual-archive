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
package org.dataconservancy.model.dcs;

import org.dataconservancy.model.dcs.support.CollectionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DcsDeliverableUnitTest {

    private String title = "DU Title";
    private String creatorOne = "Elliot Metsger";
    private String creatorTwo = "Mark Patton";
    private Collection<String> creators = CollectionFactory.newCollection();
    private String subjectOne = "Philosophy";
    private String subjectTwo = "Rock Climbing";
    private Collection<String> subjects = CollectionFactory.newCollection();
    private String collectionOne = "urn:collection1";
    private String collectionTwo = "urn:collection2";
    private Collection<DcsMetadataRef> mdRefs = CollectionFactory.newCollection();
    private String mdRefUriOne = "urn:metadata:1";
    private String mdRefUriTwo = "urn:metadata:2";
    private DcsMetadata mdInline = new DcsMetadata();
    private String mdSchema = "http://some/schema/uri";
    private String mdBlob = "<astro:md xmlns:astro=\"foo\"><astro:baz>biz</astro:baz></astro:md>";
    private DcsMetadataRef mdRefOne = new DcsMetadataRef();
    private DcsMetadataRef mdRefTwo = new DcsMetadataRef();
    private String relUri = "http://dataconservancy.org/rels/memberOf";
    private String refUriOne = "urn:relationship:1";
    private String refUriTwo = "urn:relationship:2";
    private DcsRelation relOne = new DcsRelation();
    private DcsRelation relTwo = new DcsRelation();
    private Collection<DcsRelation> rels = CollectionFactory.newCollection();
    private String extRefOne = "http://www.google.com";
    private String extRefTwo = "http://www.dataconservancy.org";
    private Collection<String> extRefs = CollectionFactory.newCollection();
    private String parentRef = "urn:parent1";
    private String lineageId = "urn:lineage";


    private DcsDeliverableUnit du;
    private DcsDeliverableUnit duPrime;

    @Before
    public void setUp() {
        du = new DcsDeliverableUnit();
        duPrime = new DcsDeliverableUnit();

        du.addParent(new DcsDeliverableUnitRef(parentRef));
        duPrime.addParent(new DcsDeliverableUnitRef(parentRef));

        creators.add(creatorOne);
        creators.add(creatorTwo);
        du.setCreators(creators);
        duPrime.setCreators(creators);

        subjects.add(subjectOne);
        subjects.add(subjectTwo);
        du.setSubjects(subjects);
        duPrime.setSubjects(subjects);

        du.setTitle(title);
        duPrime.setTitle(title);

        mdRefOne.setRef(mdRefUriOne);
        mdRefTwo.setRef(mdRefUriTwo);
        mdRefs.add(mdRefOne);
        mdRefs.add(mdRefTwo);
        du.setMetadataRef(mdRefs);
        duPrime.setMetadataRef(mdRefs);

        mdInline.setMetadata(mdBlob);
        mdInline.setSchemaUri(mdSchema);
        du.addMetadata(mdInline);
        duPrime.addMetadata(mdInline);

        relOne.setRelUri(relUri);
        relOne.setRef(new DcsEntityReference(refUriOne));
        relTwo.setRelUri(relUri);
        relTwo.setRef(new DcsEntityReference(refUriTwo));
        rels.add(relOne);
        rels.add(relTwo);
        du.setRelations(rels);
        duPrime.setRelations(rels);

        extRefs.add(extRefOne);
        extRefs.add(extRefTwo);
        du.setFormerExternalRefs(extRefs);
        duPrime.setFormerExternalRefs(extRefs);

        du.setLineageId(lineageId);
        duPrime.setLineageId(lineageId);
    }

    @Test
    public void testTitleOk() throws Exception {
        assertNull(new DcsDeliverableUnit().getTitle());
        assertEquals(title, du.getTitle());
        du.setTitle("Foo Title");
        assertEquals("Foo Title", du.getTitle());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyTitle() throws Exception {
        du.setTitle(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullTitle() throws Exception {
        du.setTitle(null);
    }

    @Test
    public void testLineageIdOk() throws Exception {
        assertNull(new DcsDeliverableUnit().getLineageId());
        assertEquals(lineageId, du.getLineageId());
        du.setLineageId("urn:foo");
        assertEquals("urn:foo", du.getLineageId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyLineageId() throws Exception {
        du.setLineageId(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullLineageId() throws Exception {
        du.setLineageId(null);
    }

    @Test
    public void testCreatorsOk() throws Exception {
        assertEquals(2, du.getCreators().size());
        assertEquals(creators, du.getCreators());

        final List<String> emptyCreators = new ArrayList<String>();
        du.setCreators(emptyCreators);
        assertEquals(emptyCreators, du.getCreators());

        du.addCreator(creatorOne);
        du.addCreator(creatorTwo);
        assertEquals(creators, du.getCreators());

        assertEquals(0, new DcsDeliverableUnit().getCreators().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatorsNull() throws Exception {
        du.setCreators(null);
    }

    @Test
    public void testSubjectsOk() throws Exception {
        assertEquals(2, du.getSubjects().size());
        assertEquals(subjects, du.getSubjects());

        final List<String> emptySubjects = new ArrayList<String>();
        du.setSubjects(emptySubjects);
        assertEquals(emptySubjects, du.getSubjects());

        du.addSubject(subjectOne);
        du.addSubject(subjectTwo);
        assertEquals(subjects, du.getSubjects());

        assertEquals(0, new DcsDeliverableUnit().getSubjects().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubjectsNull() throws Exception {
        du.setSubjects(null);
    }

    @Test
    @Ignore("TODO")
    public void testGetCollections() throws Exception {
    }

    @Test
    @Ignore("TODO")
    public void testSetCollections() throws Exception {
    }

    @Test
    @Ignore("TODO")
    public void testAddCollection() throws Exception {
    }

    @Test
    public void testMetadataOk() throws Exception {
        assertEquals(1, du.getMetadata().size());
        assertEquals(mdInline, du.getMetadata().iterator().next());

        final Set<DcsMetadata> emptyMd = new HashSet<DcsMetadata>();
        du.setMetadata(emptyMd);
        assertEquals(emptyMd, du.getMetadata());

        du.addMetadata(mdInline);
        assertEquals(mdInline, du.getMetadata().iterator().next());

        assertEquals(0, new DcsDeliverableUnit().getMetadata().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNunn() throws Exception {
        du.setMetadata(null);
    }

    @Test
    @Ignore("TODO")
    public void testAddMetadata() throws Exception {
    }

    @Test
    public void testMetadataRefOk() throws Exception {
        assertEquals(2, mdRefs.size());
        assertEquals(mdRefs, du.getMetadataRef());

        final List<DcsMetadataRef> emptyRefs = new ArrayList<DcsMetadataRef>();
        du.setMetadataRef(emptyRefs);
        assertEquals(emptyRefs, du.getMetadataRef());

        du.addMetadataRef(mdRefOne);
        du.addMetadataRef(mdRefTwo);
        assertEquals(mdRefs, du.getMetadataRef());

        assertEquals(0, new DcsDeliverableUnit().getMetadataRef().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMetadataRefNull() throws Exception {
        du.setMetadataRef(null);
    }

    @Test
    public void testRelationsOk() throws Exception {
        assertEquals(2, du.getRelations().size());
        assertEquals(rels, du.getRelations());

        final List<DcsRelation> emptyRelations = new ArrayList<DcsRelation>();
        du.setRelations(emptyRelations);
        assertEquals(emptyRelations, du.getRelations());

        du.addRelation(relOne);
        du.addRelation(relTwo);
        assertEquals(rels, du.getRelations());

        assertEquals(0, new DcsDeliverableUnit().getRelations().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRelationsNull() throws Exception {
        du.setRelations(null);
    }

    @Test
    public void testFormerExternalRefsOk() throws Exception {
        assertEquals(2, du.getFormerExternalRefs().size());
        assertEquals(extRefs, du.getFormerExternalRefs());

        final List<String> emptyFormerExternalRefs = new ArrayList<String>();
        du.setFormerExternalRefs(emptyFormerExternalRefs);
        assertEquals(emptyFormerExternalRefs, du.getFormerExternalRefs());

        du.addFormerExternalRef(extRefOne);
        du.addFormerExternalRef(extRefTwo);
        assertEquals(extRefs, du.getFormerExternalRefs());

        assertEquals(0, new DcsDeliverableUnit().getFormerExternalRefs().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormerExternalRefsNull() throws Exception {
        du.setFormerExternalRefs(null);
    }

    @Test
    public void testEquals() throws Exception {

        final DcsDeliverableUnit notEqual = new DcsDeliverableUnit();
        assertFalse(du.equals(notEqual));

        final DcsDeliverableUnit duCopy = new DcsDeliverableUnit(du);
        final DcsDeliverableUnit duCopyTwo = new DcsDeliverableUnit(du);

        // symmetric
        assertTrue(du.equals(du));
        assertTrue(duCopy.equals(duCopy));

        // reflexive
        assertTrue(du.equals(duCopy) && duCopy.equals(du));
        assertTrue(du.equals(duPrime) && duPrime.equals(du));

        // transitive
        assertTrue(du.equals(duCopy));
        assertTrue(du.equals(duCopyTwo));
        assertTrue(duCopy.equals(duCopyTwo));

        // consistent
        assertTrue(du.equals(duCopy) && du.equals(duCopy));
        assertTrue(duCopy.equals(du) && du.equals(duCopy));
    }

    @Test
    public void testEqualsLineageId() throws Exception {

        final DcsDeliverableUnit notEqual = new DcsDeliverableUnit(du);
        assertTrue(du.equals(notEqual));

        notEqual.setLineageId("urn:moo");
        assertFalse(du.equals(notEqual));
    }
}
