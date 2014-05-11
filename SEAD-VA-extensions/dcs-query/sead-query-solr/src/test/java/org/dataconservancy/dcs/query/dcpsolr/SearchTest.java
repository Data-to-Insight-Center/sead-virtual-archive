/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.query.dcpsolr;

import org.dataconservancy.dcs.index.dcpsolr.DcpUtil;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.*;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test various searches involving entity attributes and relationships.
 */
public class SearchTest extends AbstractSearchTest {
    // Do not load into the archive in order to deal with random DcsFile
    // sources.
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testEventsAndAncestors() throws Exception {

        ResearchObject ro = new ResearchObject();
        SeadDeliverableUnit du = new SeadDeliverableUnit();
        du.setId("id");
        du.setTitle("du");
        du.setAbstrct("this is a test");
        ro.addDeliverableUnit(du);

        index(ro);

        QueryResult<DcsEntity> result = query_service.query(
                "entityType:DeliverableUnit", 0, -1);

        assertNotNull(result);
        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches()
                .size());

        result = query_service.query(
            "abstract:test", 0, -1);

        assertNotNull(result);
        assertEquals(0, result.getOffset());
        assertEquals(1, result.getMatches()
        .size()); }


}
