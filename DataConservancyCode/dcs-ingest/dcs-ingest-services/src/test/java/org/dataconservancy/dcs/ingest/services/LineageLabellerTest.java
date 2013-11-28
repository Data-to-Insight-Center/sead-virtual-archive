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
package org.dataconservancy.dcs.ingest.services;

import junit.framework.Assert;
import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: HanhVu
 * Date: 3/8/12
 * Time: 10:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class LineageLabellerTest {
    /**
     * The some of these SIP files have values that are hard coded into the seededMockArchive used in these these,
     * DO NOT alter their content without proper precaution. See seedMockArchive() method for more details.
     */
    private static final String ADD_FILE_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/AddFileSip.xml";
    private static final String INITIAL_SIP =
            "/org/dataconservancy/dcs/ingest/services/exampleDIP.xml";
    private static final String MULTIPLE_VERSIONS_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/MultipleVersionsSip.xml";
    private static final String CIRCULAR_VERSIONS_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/CircularVersionsSip.xml";
    private static final String INVALID_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/InvalidSip-MismatchedLineageIds.xml";
    private static final String FILE_SUCCESSOR_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/FileSuccessorSip.xml";
    private static final String INVALID_PRED_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/InvalidSip-FilePredecessor.xml";

    private static final String SUCCESSOR_URI = "urn:dataconservancy.org:rel/isSuccessorOf";

    private DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

    private static IdService idService = new MemoryIdServiceImpl();

    private static BulkIdCreationService bulkIdCreationService = (BulkIdCreationService) idService;

    private static SipStager sipStager = new MemoryStager();

    private static EventManager eventMgr;

    private static IngestFramework fwk = new IngestFramework();

    private static Labeller labeller = new Labeller();
    private static LineageLabeller lineageLabeller = new LineageLabeller();
    private static Map<String, DcsEntity> seededMockArchive = new HashMap<String, DcsEntity>();
    private static MockedLookupQueryService queryService = new MockedLookupQueryService();

    @BeforeClass
    public static void init() {

        InlineEventManager iem = new InlineEventManager();
        iem.setIdService(bulkIdCreationService);
        iem.setSipStager(sipStager);
        eventMgr = iem;

        fwk.setEventManager(eventMgr);
        fwk.setSipStager(sipStager);

        labeller.setIngestFramework(fwk);
        labeller.setIdentifierService(idService);
        labeller.setBulkIdService(bulkIdCreationService);
        labeller.setIdPrefix("http://dataconservancy.org/");

        lineageLabeller.setIngestFramework(fwk);
        lineageLabeller.setIdentifierService(idService);
        lineageLabeller.setLookupQueryService(queryService);

        seedMockArchive();
    }

    private static void seedMockArchive() {
        //Seed file for ADD_FILE_ZIP
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("http://localhost:8081/dcs/entity/135");
        du.setLineageId(idService.create(Types.LINEAGE.getTypeName()).getUrl().toString());
        seededMockArchive.put(du.getId(), du);

        //Seed file for INVALID_SIP
        du = new DcsDeliverableUnit();
        du.setId("http://localhost:8081/dcs/entity/186");
        du.setLineageId(idService.create(Types.LINEAGE.getTypeName()).getUrl().toString() + "LineageLabllerTest");
        seededMockArchive.put(du.getId(), du);

        //Seed file for INVALID_PRED_SIP
        DcsFile file = new DcsFile();
        file.setId("http://localhost:8081/dcs/entity/2");
        seededMockArchive.put(file.getId(), file);
    }

    /**
     * Test that given a brand new du, that is not a successor to any other du, it will receive a new lineageId
     *
     * @throws Exception
     */
    @Test
    public void testAssignInitialLineage() throws Exception {
        String id = loadPreprocessSip(INITIAL_SIP);

        Assert.assertEquals(1, sipStager.getSIP(id).getDeliverableUnits().size());

        for (DcsDeliverableUnit du : sipStager.getSIP(id).getDeliverableUnits()) {
            Assert.assertNull(du.getLineageId());
        }

        lineageLabeller.execute(id);

        Assert.assertEquals(1, sipStager.getSIP(id).getDeliverableUnits().size());

        for (DcsDeliverableUnit du : sipStager.getSIP(id).getDeliverableUnits()) {
            Assert.assertNotNull(du.getLineageId());
        }

        /* Assert that there is no du.update event was created because this was a new lineage */
        Assert.assertEquals(0, fwk.getEventManager().getEvents(id, "du.update").size());

    }

    @Test(expected = IngestServiceException.class)
    public void testInSipFileSuccessor() throws Exception {
        String id = loadPreprocessSip(FILE_SUCCESSOR_SIP);

        for (DcsDeliverableUnit du : sipStager.getSIP(id).getDeliverableUnits()) {
            Assert.assertNull(du.getLineageId());
        }

        lineageLabeller.execute(id);
    }

    /**
     * Test that given an updating SIP, with a isSuccessorOf relationship attached to a DU, that DU will be assigned
     * a lineageId that is the same to that of its predecessor. This is done implicitly
     *
     * @throws Exception
     */
    @Test
    public void testAssignImplicitLineageId() throws Exception {
        String id = loadPreprocessSip(ADD_FILE_SIP);

        /* Assumption: knowing the SIP being used in this test, there should only be ONE DU*/
        Assert.assertEquals(1, sipStager.getSIP(id).getDeliverableUnits().size());

        /* Assert that there is no du.update event in the SIP before the lineageLabeller goes to work */
        Assert.assertEquals(0, fwk.getEventManager().getEvents(id, "du.update").size());
        for (DcsDeliverableUnit du : sipStager.getSIP(id).getDeliverableUnits()) {
            /* Assert that there is no existing LineageId */
            Assert.assertNull(du.getLineageId());
        }
        /* Assign lineageId */
        lineageLabeller.execute(id);

        for (DcsDeliverableUnit du : sipStager.getSIP(id).getDeliverableUnits()) {
            /* Assert that there is NOW a lineageId value for the examined du */
            Assert.assertNotNull(du.getLineageId());
            /* Assumption: knowing the SIP being used in this test, there should only be on relationship submitted
             * along with the DU
             */
            DcsDeliverableUnit predecessor = (DcsDeliverableUnit) queryService.lookup(du.getRelations().iterator().next().getRef().getRef());

            /* Test: expect the updating du to have the same lineage id as the predecessor du*/
            Assert.assertEquals(predecessor.getLineageId(), du.getLineageId());
        }

        /* Assert that correct events are added */
        Assert.assertEquals(1, fwk.getEventManager().getEvents(id, "du.update").size());
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        builder.buildSip(sipStager.getSIP(id), content);
        String resultDcp = new String(content.toByteArray(), "UTF-8");
        File file = File.createTempFile("pre", "suf");
    }

    @Test(expected = IngestServiceException.class)
    public void testInvalidPredecessorSip() throws Exception {
        String id = loadPreprocessSip(INVALID_PRED_SIP);
        lineageLabeller.execute(id);
    }


    @Test(expected = IngestServiceException.class)
    public void testProvidedLineageIdAndPredecessorIdDoesNotMatch() throws Exception {
        String id = loadPreprocessSip(INVALID_SIP);

        /* Assumption: knowing the SIP being used in this test, there should only be ONE DU*/
        Assert.assertEquals(1, sipStager.getSIP(id).getDeliverableUnits().size());

        for (DcsDeliverableUnit du : sipStager.getSIP(id).getDeliverableUnits()) {
            /* Assert that there is no existing LineageId */
            Assert.assertNotNull(du.getLineageId());
        }
        /* Assign lineageId */
        lineageLabeller.execute(id);
    }

    /* Test labelling a SIP with multiple versions of a lineage */
    @Test
    public void testMultipleVersionsSip() throws Exception {
        String id = fwk.getSipStager().addSIP(builder.buildSip(this.getClass()
                .getResourceAsStream(MULTIPLE_VERSIONS_SIP)));
        labeller.execute(id);

        Assert.assertEquals(2, sipStager.getSIP(id).getDeliverableUnits().size());

        /* Assert that there is no du.update event in the SIP before the lineageLabeller goes to work */
        Assert.assertEquals(0, fwk.getEventManager().getEvents(id, "du.update").size());

        /* Assign lineageId */
        lineageLabeller.execute(id);
        Iterator<DcsDeliverableUnit> itr = sipStager.getSIP(id).getDeliverableUnits().iterator();
        Assert.assertEquals(2, sipStager.getSIP(id).getDeliverableUnits().size());
        DcsDeliverableUnit du1 = itr.next();
        DcsDeliverableUnit du2 = itr.next();

        Assert.assertNotNull(du1);
        Assert.assertNotNull(du2);

        DcsRelation du1Rel;
        DcsRelation du2Rel;

        Assert.assertEquals(du1.getLineageId(), du2.getLineageId());

        if (!du2.getRelations().isEmpty()) {
            du2Rel = du2.getRelations().iterator().next();
            Assert.assertEquals(SUCCESSOR_URI, du2Rel.getRelUri());
            Assert.assertEquals(du2Rel.getRef().getRef(), du1.getId());
        } else {
            du1Rel = du1.getRelations().iterator().next();
            Assert.assertTrue(du1Rel.getRelUri().equals(SUCCESSOR_URI));
            Assert.assertEquals(du1Rel.getRef().getRef(), du2.getId());
        }

    }

    /* Test labelling a SIP in which two DUs refers to one another as predecessor */
    @Test(expected = IngestServiceException.class)
    public void testCircularSuccessorRelationship() throws Exception {
        String id = fwk.getSipStager().addSIP(builder.buildSip(this.getClass()
                .getResourceAsStream(CIRCULAR_VERSIONS_SIP)));
        labeller.execute(id);

        Assert.assertEquals(2, sipStager.getSIP(id).getDeliverableUnits().size());

        /* Assign lineageId */
        lineageLabeller.execute(id);
    }

    private String loadPreprocessSip(String sipFileName) throws Exception {
        String id = fwk.getSipStager().addSIP(builder.buildSip(this.getClass()
                .getResourceAsStream(sipFileName)));
        labeller.execute(id);
        return id;
    }


    private static class MockedLookupQueryService implements LookupQueryService<DcsEntity> {
        private Map<String, DcsDeliverableUnit> createdEntities = new HashMap<String, DcsDeliverableUnit>();

        /* Returns some mocked up DcsDeliverableUnit with desired attribute set*/
        @Override
        public DcsEntity lookup(String id) throws QueryServiceException {
            if (seededMockArchive.containsKey(id)) {
                return seededMockArchive.get(id);
            } else return null;
        }

        /*
        *  Query is assumed to be generated from the following statement:
        *  String query = SolrQueryUtil.createLiteralQuery("AND",
        *        "rel_" + DcsRelationship.IS_SUCCESSOR_OF.asString(), "predID",
        *        DcsSolrField.EntityField.TYPE.solrName(), DcsSolrField.EntityTypeValue.DELIVERABLE_UNIT.solrValue());
        *  Look through the seededMockArchive to find DU with predecessor that is the same as the requested predecessor
        *  in the incoming SIP.
        *  */
        @Override
        public QueryResult<DcsEntity> query(String query, long offset, int matches, String... params) throws QueryServiceException {
            String[] queryArr = query.split(" ");
            String predecessorId = queryArr[0].split("\"")[1].replace("\\:", ":");
            QueryResult<DcsEntity> result = new QueryResult<DcsEntity>(0, 0, "blaj", "blaj");
            for (DcsEntity e : seededMockArchive.values()) {
                if (e instanceof DcsDeliverableUnit) {
                    if (((DcsDeliverableUnit) e).getRelations().size() > 0 &&
                            ((DcsDeliverableUnit) e).getRelations().iterator().next().getRef().getRef().equals(predecessorId)) {
                        QueryMatch<DcsEntity> match = new QueryMatch<DcsEntity>(e, "context");
                        result.getMatches().add(match);
                        break;
                    }
                }
            }
            return result;
        }

        @Override
        public void shutdown() throws QueryServiceException {
            // do nothing
        }
    }
}
