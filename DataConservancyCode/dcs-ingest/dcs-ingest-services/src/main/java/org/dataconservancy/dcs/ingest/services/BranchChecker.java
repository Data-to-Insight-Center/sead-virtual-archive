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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.dataconservancy.dcs.index.dcpsolr.DcsSolrField;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.ingest.LockManager;
import org.dataconservancy.dcs.ingest.LockService;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.springframework.beans.factory.annotation.Required;

public class BranchChecker extends IngestServiceBase implements IngestService {


    private QueryService<DcsEntity> queryService;

    @Required

    public void setQueryService(QueryService<DcsEntity> queryService) {
        this.queryService = queryService;
    }

    public void execute(String sipRef) throws IngestServiceException {
        if (isDisabled()) {
            return;
        }

        Dcp sip = ingest.getSipStager().getSIP(sipRef);

        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();

        for (DcsDeliverableUnit du : dus) {
            check(du, dus, sipRef);
        }

    }

    private String getIsSuccessorOfTarget(DcsRelation rel)
            throws IngestServiceException {
        if (rel.getRef() == null || rel.getRef().getRef() == null) {
            throw new IngestServiceException(
                    "isSuccessorOf relationship does not have target");
        }

        return rel.getRef().getRef();
    }

    private String getPredecessorId(DcsDeliverableUnit du)
            throws IngestServiceException {
        boolean has_predecessor = false;
        String pred_id = null;

        for (DcsRelation rel : du.getRelations()) {
            if (rel.getRelUri().equals(DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                if (has_predecessor) {
                    throw new IngestServiceException(
                            "At most one prececessor allowed: " + du.getId());
                }

                has_predecessor = true;

                pred_id = getIsSuccessorOfTarget(rel);
            }
        }

        return pred_id;
    }

    private void check(DcsDeliverableUnit du, Collection<DcsDeliverableUnit> dus,
            String sipRef) throws IngestServiceException {
        String pred_id = getPredecessorId(du);

        if (pred_id == null) {
            return;
        }

        // Check for predecessor in SIP
        DcsDeliverableUnit pred = null;

        for (DcsDeliverableUnit test : dus) {
            if (test.getId().equals(pred_id)) {
                pred = test;
                break;
            }
        }

        // Check for branching
        if (pred != null) {
            // Check for branching in SIP
            for (DcsDeliverableUnit test : dus) {
                if (test.getId().equals(du.getId())) {
                    continue;
                }

                String test_pred = getPredecessorId(test);

                if (test_pred != null && test_pred.equals(pred_id)) {
                    throw new IngestServiceException("No branching allowed: "
                            + du.getId());
                }
            }
        } else {
            // Check for branching in the archive
            // Lock lineage
            LockService lockService = ingest.getLockService();
            LockManager manager = lockService.getLockManager(sipRef);
            Lock lock = manager.obtainLock(du.getLineageId(), du.getId());

            if (!lock.tryLock()) {
                throw new IngestServiceException(
                        "Concurrent updates of same lineage not allowed: "
                                + du.getId());
            }

            checkArchiveForBranching(du, pred_id, sipRef);

        }
    }

    private void checkArchiveForBranching(DcsDeliverableUnit deliverableUnit,
            String predId, String sipRef) throws IngestServiceException {
        String query = SolrQueryUtil.createLiteralQuery("AND", "rel_"
                + DcsRelationship.IS_SUCCESSOR_OF.asString(), predId,
                DcsSolrField.EntityField.TYPE.solrName(),
                DcsSolrField.EntityTypeValue.DELIVERABLE_UNIT.solrValue());

        LockService lockService = ingest.getLockService();
        LockManager manager = lockService.getLockManager(sipRef);
        Lock lock = manager.obtainLock(deliverableUnit.getLineageId(), deliverableUnit.getId());

        try {
            QueryResult<DcsEntity> result = queryService.query(query, 0, -1);
            
            if (result.getTotal() > 0) {
                lock.unlock();
                throw new IngestServiceException(String.format(
                        "No branching allowed: the target predecessor %s "
                                + "of %s already has a successor", predId,
                        deliverableUnit.getId()));
            }
        } catch (QueryServiceException e) {
            lock.unlock();
            throw new IngestServiceException(e);
        }
    }

}
