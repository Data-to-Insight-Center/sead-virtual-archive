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

import org.dataconservancy.dcs.id.api.BulkIdCreationService;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.ingest.EventManager;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.LockService;
import org.dataconservancy.dcs.ingest.SipStager;
import org.dataconservancy.dcs.ingest.impl.InMemoryLockService;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: HanhVu
 * Date: 3/14/12
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class BranchCheckerTest {

    private static BranchChecker branchChecker = new BranchChecker();
    private static SipStager sipStager = new MemoryStager();
    private static Labeller labeller = new Labeller();
    private static IdService idService = new MemoryIdServiceImpl();
    private static BulkIdCreationService bulkIdService = (BulkIdCreationService) idService;
    private static LineageLabeller lineageLabeller = new LineageLabeller();
    private static IngestFramework fwk = new IngestFramework();
    private static LockService lockService = new InMemoryLockService();
    private static DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();
    private static MockedLookupQueryService queryService = new MockedLookupQueryService();
    private static Cleanup cleanup = new Cleanup();
    private static EventManager eventMgr;

    private static Map<String, DcsEntity> seededMockArchive = new HashMap<String, DcsEntity>();
    private static final String predecessorId = "http://dataconservancy.org/du1";

    private static final String IN_SIP_BRANCHING_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/InternalBranchingSip.xml";
    private static final String ARCHIVE_BRANCHING_SIP =
            "/org/dataconservancy/dcs/ingest/services/updatingSips/ArchiveBranchingSip.xml";
    private static final String VALID_SIP =
            "/org/dataconservancy/dcs/ingest/services/exampleDIP.xml";

    @BeforeClass
    public static void setUp() {

        InlineEventManager iem = new InlineEventManager();
        iem.setIdService(bulkIdService);
        iem.setSipStager(sipStager);
        eventMgr = iem;

        fwk.setEventManager(eventMgr);
        fwk.setSipStager(sipStager);
        fwk.setLockService(lockService);
        cleanup.setIngestFramework(fwk);

        labeller.setIngestFramework(fwk);
        labeller.setIdentifierService(idService);
        labeller.setBulkIdService(bulkIdService);
        labeller.setIdPrefix("http://dataconservancy.org/");

        lineageLabeller.setIngestFramework(fwk);
        lineageLabeller.setIdentifierService(idService);
        lineageLabeller.setLookupQueryService(queryService);

        branchChecker.setQueryService(queryService);
        branchChecker.setIngestFramework(fwk);

        DcsDeliverableUnit deliverableUnit = new DcsDeliverableUnit();
        deliverableUnit.setId("http://dataconservancy.org/existingSuccessor");
        deliverableUnit.addRelation(new DcsRelation(DcsRelationship.IS_SUCCESSOR_OF, predecessorId));
        seededMockArchive.put(deliverableUnit.getId(), deliverableUnit);
        deliverableUnit = new DcsDeliverableUnit();
        deliverableUnit.setId(predecessorId);
        deliverableUnit.setLineageId(idService.create(Types.LINEAGE.getTypeName()).toString());
        seededMockArchive.put(predecessorId, deliverableUnit);

        DcsFile file = new DcsFile();
        file.setId("http://dataconservancy.org/file1");
        file.setName("file_name");
        file.setSource("file_source");
        seededMockArchive.put(file.getId(), file);
    }

    @Test(expected = IngestServiceException.class)
    public void testInSipBranching() throws Exception {
        String id = loadPreprocessSip(IN_SIP_BRANCHING_SIP);
        branchChecker.execute(id);
        cleanup.execute(id);
    }

    @Test(expected = IngestServiceException.class)
    public void testArchiveBranching() throws Exception {
        String id = loadPreprocessSip(ARCHIVE_BRANCHING_SIP);
        branchChecker.execute(id);
        cleanup.execute(id);
    }

    @Test
    public void testHappyPath() throws Exception {
        String id = loadPreprocessSip(VALID_SIP);
        try {
            branchChecker.execute(id);
        } catch (IngestServiceException e) {
            Assert.fail("A valid SIP was tested. There's not expected to be any Exception thrown. " + e.getMessage());
        }
        cleanup.execute(id);
    }

    private String loadPreprocessSip(String sipFileName) throws Exception {
        String id = fwk.getSipStager().addSIP(builder.buildSip(this.getClass()
                .getResourceAsStream(sipFileName)));
        labeller.execute(id);
        lineageLabeller.execute(id);
        return id;
    }

    private static class MockedLookupQueryService implements LookupQueryService<DcsEntity> {
        private Map<String, DcsDeliverableUnit> createdEntities = new HashMap<String, DcsDeliverableUnit>();

        /* Returns some mocked up DcsDeliverableUnit with desired attribute set*/
        @Override
        public DcsEntity lookup(String id) throws QueryServiceException {
            if (createdEntities.containsKey(id)) {
                return createdEntities.get(id);
            } else {
                DcsDeliverableUnit du = new DcsDeliverableUnit();
                du.setId(id);
                du.setLineageId(idService.create(Types.LINEAGE.getTypeName()).getUrl().toString());
                createdEntities.put(du.getId(), du);
                return du;
            }
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
            
            List<QueryMatch<DcsEntity>> result_matches = new ArrayList<QueryMatch<DcsEntity>>();
            
            for (DcsEntity e : seededMockArchive.values()) {
                if (e instanceof DcsDeliverableUnit) {
                    if (((DcsDeliverableUnit) e).getRelations().size() > 0 &&
                            ((DcsDeliverableUnit) e).getRelations().iterator().next().getRef().getRef().equals(predecessorId)) {
                        QueryMatch<DcsEntity> match = new QueryMatch<DcsEntity>(e, "context");
                        result_matches.add(match);
                        break;
                    }
                }
            }
            
            QueryResult<DcsEntity> result = new QueryResult<DcsEntity>(0, result_matches.size(), query);
            result.getMatches().addAll(result_matches);

            return result;
        }

        @Override
        public void shutdown() throws QueryServiceException {
            // do nothing
        }
    }


}
