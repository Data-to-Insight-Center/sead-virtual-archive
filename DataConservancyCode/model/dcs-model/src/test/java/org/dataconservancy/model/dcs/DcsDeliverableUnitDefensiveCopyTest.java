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
import org.dataconservancy.model.dcs.support.TestAssertion;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.dataconservancy.model.dcs.support.TestAssertion.assertCollectionsEqual;
import static org.dataconservancy.model.dcs.support.TestAssertion.assertCollectionsIdentical;
import static org.dataconservancy.model.dcs.support.TestAssertion.assertCollectionsNotIdentical;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DcsDeliverableUnitDefensiveCopyTest {

    private Collection<DcsCollectionRef> collections = CollectionFactory.newCollection();

    private Collection<DcsMetadata> metadata = CollectionFactory.newCollection();

    private Collection<DcsMetadataRef> metadataRefs = CollectionFactory.newCollection();

    private Collection<DcsRelation> relations = CollectionFactory.newCollection();

    private Collection<String> formerRefs = CollectionFactory.newCollection();

    private Collection<DcsDeliverableUnitRef> parents = CollectionFactory.newCollection();

    private Boolean digitalSurrogate = false;

    private CoreMetadata coreMd = new CoreMetadata();

    private String lineageId = "lineageIdString";

    private DcsDeliverableUnit orig = new DcsDeliverableUnit();

    @Before
    public void setUp() throws Exception {
        collections.add(new DcsCollectionRef("collectionRef"));

        DcsMetadata dcsMetadata = new DcsMetadata();
        dcsMetadata.setMetadata("<metadataContent/>");
        dcsMetadata.setSchemaUri("metadataSchemeUri");
        metadata.add(dcsMetadata);

        metadataRefs.add(new DcsMetadataRef("metadataRef"));

        relations.add(new DcsRelation("relationUri", "relationTarget"));

        formerRefs.add("formerRef");

        parents.add(new DcsDeliverableUnitRef("parentDuRef"));

        coreMd.addCreator("creator");
        coreMd.addSubject("subject");
        coreMd.setRights("rightsString");
        coreMd.setTitle("titleString");
        coreMd.setType("typeString");

        orig.setCollections(collections);
        orig.setMetadata(metadata);
        orig.setMetadataRef(metadataRefs);
        orig.setRelations(relations);
        orig.setFormerExternalRefs(formerRefs);
        orig.setParents(parents);
        orig.setCoreMd(coreMd);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        DcsDeliverableUnit copy = new DcsDeliverableUnit(orig);

        assertEquals("Original should equal copy", orig, copy);

        assertCollectionsEqual(orig.getCollections(), copy.getCollections());
        assertCollectionsEqual(orig.getMetadata(), copy.getMetadata());
        assertCollectionsEqual(orig.getMetadataRef(), copy.getMetadataRef());
        assertCollectionsEqual(orig.getRelations(), copy.getRelations());
        assertCollectionsEqual(orig.getFormerExternalRefs(), copy.getFormerExternalRefs());
        assertCollectionsEqual(orig.getParents(), copy.getParents());

        assertCollectionsNotIdentical(orig.getCollections(), copy.getCollections());
        assertCollectionsNotIdentical(orig.getMetadata(), copy.getMetadata());
        assertCollectionsNotIdentical(orig.getMetadataRef(), copy.getMetadataRef());
        assertCollectionsNotIdentical(orig.getRelations(), copy.getRelations());
        assertCollectionsNotIdentical(orig.getFormerExternalRefs(), copy.getFormerExternalRefs());
        assertCollectionsNotIdentical(orig.getParents(), copy.getParents());
    }

    @Test
    public void testDefensiveCopy() throws Exception {
        assertCollectionsIdentical(orig.getCollections(), collections);
        assertCollectionsIdentical(orig.getMetadata(), metadata);
        assertCollectionsIdentical(orig.getMetadataRef(), metadataRefs);
        assertCollectionsIdentical(orig.getRelations(), relations);
        assertCollectionsIdentical(orig.getFormerExternalRefs(), formerRefs);
        assertCollectionsIdentical(orig.getParents(), parents);
    }
}
