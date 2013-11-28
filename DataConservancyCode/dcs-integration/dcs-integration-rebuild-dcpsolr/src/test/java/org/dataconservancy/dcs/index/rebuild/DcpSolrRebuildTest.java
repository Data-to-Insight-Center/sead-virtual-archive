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
package org.dataconservancy.dcs.index.rebuild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrDocument;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField.EntityField;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;

import static org.dataconservancy.dcs.index.solr.support.SolrQueryUtil.createLiteralQuery;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public abstract class DcpSolrRebuildTest {

    private static final String ANCESTRY = EntityField.ANCESTRY.solrName();

    private static final String METADATA_FIELD__TYPE = "ext_/metadata/type";

    private static final String METADATA_FIELD__FILE_ID =
            "ext_/metadata/fileId";

    private static final String METADATA_FIELD__ENTITY_ID =
            "ext_/metadata/entityId";

    private OrderedDcpEntitySource source;

    private DcsDataModelQueryService service;

    DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    protected abstract DcsDataModelQueryService doRebuild(OrderedDcpEntitySource src);

    protected abstract SolrDocument lookupSolrDocument(String id);

    protected abstract OrderedDcpEntitySource getEntitySource();

    /* In case a test class forgets to do this */
    @BeforeClass
    public static void createDcsHome() {
        DcsHome.prepareHome(DcpSolrRebuildTest.class);
    }

    @Before
    public void setUp() {
        source = getEntitySource();
        if (service == null) {
            service = doRebuild(source);
        }
    }

    /*
     * Makes sure that every entity is indexed, and retrievable from the index
     * without changes
     */
    @Test
    public void testAllEntities() throws Exception {
        List<DcsEntity> entities = source.getEntities(null);

        /* Get all entities */
        QueryResult<DcsEntity> results = service.query("*:*", 0, 10000);

        List<String> packageIds = new ArrayList<String>();

        for (Dcp pkg : source.getPackages()) {
            for (DcsEntity e : pkg) {
                packageIds.add(e.getId());
            }
        }

        assertEquals(entities.size(), results.getTotal());
        assertEquals(entities.size(), packageIds.size());

        List<String> searchIds = new ArrayList<String>();
        for (QueryMatch<DcsEntity> match : results.getMatches()) {
            searchIds.add(match.getObject().getId());
            assertTrue(entities.contains(match.getObject()));
            assertNotNull(lookupSolrDocument(match.getObject().getId()));
            System.out.println("Index has: " + match.getObject().getId());
        }

        assertTrue(searchIds.containsAll(packageIds));
    }

    /* Metadata describing a collection is searchable on that collection */
    @Test
    public void testSingleMetadataForCollection() {
        doTestMetadataFor(DcsCollection.class, true);
    }

    @Test
    public void testMultiMetadataForCollection() {
        doTestMetadataFor(DcsCollection.class, false);
    }

    /* Metadata describing a DU is searchable on that DU */
    @Test
    public void testSingleMetadataForDU() {
        doTestMetadataFor(DcsDeliverableUnit.class, true);
    }

    /* Metadata describing a DU is searchable on that DU */
    @Test
    public void testMultiMetadataForDU() {
        doTestMetadataFor(DcsDeliverableUnit.class, false);
    }

    /* Metadata describing a Manifestation is searchable on that Manifestation */
    @Test
    public void testSingleMetadataForManifestation() {
        doTestMetadataFor(DcsManifestation.class, true);
    }

    /* Metadata describing a Manifestation is searchable on that Manifestation */
    @Test
    public void testMultiMetadataForManifestation() {
        doTestMetadataFor(DcsManifestation.class, false);
    }

    /* Metadata describing a File */
    @Test
    public void testSingleMetadataForFile() {
        doTestMetadataFor(DcsFile.class, false);
    }

    /* Metadata describing a File */
    @Test
    public void testMultiMetadataForFile() {
        doTestMetadataFor(DcsFile.class, true);
    }

    private void doTestMetadataFor(Class<? extends DcsEntity> entityClass,
                                   boolean single) {

        /* Just to make sure we found at least one match */
        boolean foundMatch = true;

        try {
            for (DcsEntity e : source.getEntities(entityClass)) {

                Collection<DcsFile> metadataFiles =
                        source.getMetadataFilesFor(e.getId());

                if (single && metadataFiles.size() > 1) continue;

                for (DcsFile file : metadataFiles) {

                    Metadata m = source.getMetadataFactory().fromFile(file);

                    String query =
                            createLiteralQuery("AND",
                                               METADATA_FIELD__ENTITY_ID,
                                               m.getDescribedEntityId(),
                                               METADATA_FIELD__FILE_ID,
                                               m.getDescribingFileId(),
                                               METADATA_FIELD__TYPE,
                                               m.getDescribedEntityType()
                                                       .getName());

                    QueryResult<DcsEntity> result =
                            service.query(query, 0, 1000);

                    /* Our entity should be in the query results */
                    boolean found = false;

                    assertTrue((result.getTotal()) > 1);

                    for (QueryMatch<DcsEntity> match : result.getMatches()) {
                        if (match.getObject().equals(e)) {
                            found = true;
                            foundMatch = true;
                        }
                    }

                    assertTrue(found);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertTrue(foundMatch);
    }

    /*
     * Everything under a given collection contains the collection in its
     * ancestry
     */
    @Test
    public void testDescendantsOfCollection() {
        doTestDescendants(DcsCollection.class);
    }

    /*
     * Everything under a given DU contains the DU in its ancestry
     */
    @Test
    public void testDescendantsOfDu() {
        doTestDescendants(DcsDeliverableUnit.class);
    }

    /*
     * Everything under a Manifestation contains the Manifestation in its
     * ancestry
     */
    @Test
    public void testDescendantsOfManifestation() {
        doTestDescendants(DcsManifestation.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void doTestDescendants(Class<? extends DcsEntity> entityClass) {
        try {
            for (DcsEntity e : source.getEntities(entityClass)) {

                List<DcsEntity> searchDescendants = new ArrayList<DcsEntity>();
                Collection<DcsEntity> sourceDescendants =
                        source.getDescendantsOf(e.getId());

                /* Query for all ancestors */
                QueryResult<DcsEntity> result =
                        service.query(createLiteralQuery(ANCESTRY, e.getId()),
                                      0,
                                      1000);

                for (QueryMatch<DcsEntity> match : result.getMatches()) {
                    searchDescendants.add(match.getObject());
                }

                if (sourceDescendants.size() != searchDescendants.size()) {
                    System.out.println("looking for descendents of "
                            + e.getId() + " without it in ancestry");

                    for (DcsEntity d : sourceDescendants) {
                        Collection ancestry =
                                lookupSolrDocument(d.getId())
                                        .getFieldValues(ANCESTRY);
                        if (ancestry == null) {
                            System.out.println("NO ANCESTRY FOR " + d.getId());
                        } else {
                            Set<String> ancestors = new HashSet<String>();
                            ancestors.addAll(ancestry);

                            if (!ancestry.contains(e.getId())) {
                                System.out.println("Not present in "
                                        + d.getId());

                                for (Object a : ancestry) {
                                    System.out.println("ancestor of "
                                            + d.getId() + " -> " + a);
                                }
                            }
                        }
                    }
                }

                assertEquals("descendantsOf " + e.getId(),
                             sourceDescendants.size(),
                             searchDescendants.size());

                assertTrue(searchDescendants.containsAll(sourceDescendants));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Assures that content from related events is injected into the target's
     * index doc. eventDate, eventDetail, eventOutcome, eventType,
     * event_date_{event.type}
     */
    @Test
    public void testEventTargetData() throws Exception {
        for (DcsEvent event : source.getEntities(DcsEvent.class)) {
            for (DcsEntityReference target : event.getTargets()) {
                QueryResult<DcsEntity> results =
                        service.query(createLiteralQuery("AND",
                                                         "eventDate",
                                                         event.getDate(),
                                                         "eventDetail",
                                                         event.getDetail(),
                                                         "eventOutcome",
                                                         event.getOutcome(),
                                                         "eventType",
                                                         event.getEventType(),
                                                         "event_date_"
                                                                 + event.getEventType(),
                                                         event.getDate(),
                                                         "id",
                                                         target.getRef()),
                                      0,
                                      100);
                assertEquals(1, results.getTotal());
                assertEquals(target.getRef(), results.getMatches().get(0)
                        .getObject().getId());
            }
        }
    }
}
