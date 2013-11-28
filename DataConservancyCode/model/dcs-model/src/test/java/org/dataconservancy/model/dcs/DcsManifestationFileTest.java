package org.dataconservancy.model.dcs;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

public class DcsManifestationFileTest {

    /**
     * Make sure that equals and hashCode methods correctly handle equal
     * DcsManifestationFile instances that hold a relation with differently
     * implemented collections.
     */
    @Test
    public void testWithDifferentRelationCollectionImpls() throws Exception {
        DcsRelation rel1 = new DcsRelation();
        rel1.setRef(new DcsEntityReference("target"));
        rel1.setRelUri("dc:relation");

        DcsRelation rel2 = new DcsRelation();
        rel2.setRef(new DcsEntityReference("target"));
        rel2.setRelUri("dc:relation");

        Collection<DcsRelation> rel1_set = new ArrayList<DcsRelation>();
        rel1_set.add(rel1);

        Collection<DcsRelation> rel2_set = new HashSet<DcsRelation>();
        rel2_set.add(rel2);

        DcsManifestationFile mf1 = new DcsManifestationFile();
        mf1.setRef(new DcsFileRef("file"));
        mf1.setRelSet(rel1_set);

        DcsManifestationFile mf2 = new DcsManifestationFile();
        mf2.setRef(new DcsFileRef("file"));
        mf2.setRelSet(rel2_set);

        assertTrue(rel1.equals(rel2));
        assertTrue(rel1.hashCode() == rel2.hashCode());

        assertTrue(mf1.hashCode() == mf2.hashCode());
        assertTrue(mf1.equals(mf2));
        assertTrue(mf2.equals(mf1));
    }
}
