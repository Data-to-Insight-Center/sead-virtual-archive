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

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;

@RunWith(BlockJUnit4ClassRunner.class)
public class HierarchyTest
        extends AbstractIndexTest {

    @Test
    public void testIndividualDUCommits() throws Exception {
        List<DcsDeliverableUnit> DUs = getHierarchicalDUs(5);

        /* Index each entity individually */
        for (DcsDeliverableUnit du : DUs) {
            index(du);
        }

        /* Test that every DU contains its predecessors in its ancestry */
        testIndexAncestry(DUs);
    }

    @Test
    @Ignore("These fail due to possible index bug")
    public void testBatchDUCommits() throws Exception {
        List<DcsDeliverableUnit> DUs = getHierarchicalDUs(5);

        /* Index as a batch */
        BatchIndexer<Dcp> indexer = service.index();
        for (DcsDeliverableUnit du : DUs) {
            Dcp dcp = new Dcp();
            dcp.addDeliverableUnit(du);
            indexer.add(dcp);
        }
        indexer.close();

        /* Test that every DU contains its predecessors in its ancestry */
        testIndexAncestry(DUs);
    }

    @Test
    public void testIndividualCollectionCommits() throws Exception {

        List<DcsCollection> colls = getHierarchicalCollections(5);

        /* Index each entity individually */
        for (DcsCollection coll : colls) {
            index(coll);
        }

        /* Test that every Collection contains its predecessors in its ancestry */
        testIndexAncestry(colls);
    }

    @Test
    @Ignore("These fail due to possible index bug")
    public void testBatchCollectionCommits() throws Exception {
        List<DcsCollection> colls = getHierarchicalCollections(5);

        /* Index as a batch */
        BatchIndexer<Dcp> indexer = service.index();
        for (DcsCollection coll : colls) {
            Dcp dcp = new Dcp();
            dcp.addCollection(coll);
            indexer.add(dcp);
        }
        indexer.close();

        /* Test that every collection contains its predecessors in its ancestry */
        testIndexAncestry(colls);
    }

    @Test
    public void testSingleDcpDUCommit() throws Exception {
        List<DcsDeliverableUnit> DUs = getHierarchicalDUs(5);
        Dcp dcp = new Dcp();

        for (DcsDeliverableUnit du : DUs) {
            dcp.addDeliverableUnit(du);
        }

        /* Index all DUs at once in a single Dcp */
        index(dcp);

        testIndexAncestry(DUs);
    }

    @Test
    public void testSingleDcpCollectionCommit() throws Exception {

        List<DcsCollection> colls = getHierarchicalCollections(5);
        Dcp dcp = new Dcp();

        /* Index each entity individually */
        for (DcsCollection coll : colls) {
            dcp.addCollection(coll);
        }

        index(dcp);

        testIndexAncestry(colls);
    }

    private void testIndexAncestry(List<? extends DcsEntity> entities)
            throws Exception {
        
        /* Test that every DU contains its predecessors in its ancestry */
        for (int current = 1; current < entities.size(); current++) {
            for (int predecessor = 0; predecessor < current; predecessor++) {

                assertTrue(entities.get(predecessor).getId()
                                   + " is not in ancestry of "
                                   + entities.get(current).getId(),
                           hasFieldContainingSubstring(entities.get(current)
                                                               .getId(),
                                                       "ancestry",
                                                       entities.get(predecessor)
                                                               .getId()));
            }
        }
    }

    private List<DcsDeliverableUnit> getHierarchicalDUs(int numberOfLevels) {
        List<DcsDeliverableUnit> duList = new ArrayList<DcsDeliverableUnit>();

        /* Add root level DU */
        duList.add(rb.createDeliverableUnit(null, null, false));

        /* Add additional levels */
        for (int i = 1; i < numberOfLevels; i++) {

            /* Make the previous DU its parent, and add to list */
            duList.add(rb.createDeliverableUnit(null,
                                                duList.get(i - 1).getId(),
                                                false));
        }

        return duList;
    }

    private List<DcsCollection> getHierarchicalCollections(int numberOfLevels) {
        List<DcsCollection> collectionList = new ArrayList<DcsCollection>();

        /* Add root level DU */
        collectionList.add(rb.createCollection(null));

        /* Add additional levels */
        for (int i = 1; i < numberOfLevels; i++) {

            /* Make the previous DU its parent, and add to list */
            collectionList.add(rb.createCollection(collectionList.get(i - 1)
                    .getId()));
        }

        return collectionList;
    }
}
