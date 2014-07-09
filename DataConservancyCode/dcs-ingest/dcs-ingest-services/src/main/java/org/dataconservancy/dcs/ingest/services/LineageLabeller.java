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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.query.api.LookupQueryService;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

/**
 * Assign lineage identifiers to Deliverable Units.
 */
public class LineageLabeller extends IngestServiceBase implements IngestService {

    private IdService idService;
    private LookupQueryService<DcsEntity> queryService;

    @Required
    public void setLookupQueryService(LookupQueryService<DcsEntity> queryService) {
        this.queryService = queryService;
    }

    @Required
    public void setIdentifierService(IdService ids) {
        idService = ids;
    }

    private String getIsSuccessorOfTarget(DcsRelation rel)
            throws IngestServiceException {
        if (rel.getRef() == null || rel.getRef().getRef() == null) {
            throw new IngestServiceException(
                    "isSuccessorOf relationship does not have target");
        }

        return rel.getRef().getRef();
    }

    // Return the predecessor target of a successor relationship of a du
    private DcsDeliverableUnit lookupPredecessorInArchive(String pred_id)
            throws IngestServiceException {
        try {
            DcsEntity entity = queryService.lookup(pred_id);
            if (entity == null) {
                throw new IngestServiceException("Predeccessor not found " + pred_id);
            }

            if (!(entity instanceof DcsDeliverableUnit)) {
                throw new IngestServiceException(
                        "Predeccessor must be a Deliverable Unit: " + entity);
            }

            return (DcsDeliverableUnit) entity;
        } catch (QueryServiceException e) {
            throw new IngestServiceException(
                    "Failure using query service to lookup " + pred_id);
        }
    }

    private void checkPredecessor(DcsDeliverableUnit du, DcsDeliverableUnit pred)
            throws IngestServiceException {
        if (pred.getLineageId() == null) {
            throw new IngestServiceException(
                    "Predeccessor does not have lineage id" + pred);
        }

        if (du.getLineageId() != null) {
            if (!du.getLineageId().equals(pred.getLineageId())) {
                throw new IngestServiceException(
                        "Update lineage id set incorrectly on " + du.getId());
            }
        }
    }

    private DcsEvent createLineageIdAssignmentEvent(DcsDeliverableUnit du,
                                                    DcsDeliverableUnit pred) {
        DcsEvent event = ingest.getEventManager()
                .newEvent(Events.ID_ASSIGNMENT);
        event.setOutcome(String.format(
                "LineageId %s is getting assigned to deliverable unit %s",
                pred.getLineageId(), du.getId()));
        event.setDetail(String
                .format("Deliverable unit %s is getting assigned its predecessor's lineageId %s",
                        du.getId(), pred.getLineageId()));
        event.addTargets(new DcsEntityReference(du.getId()));

        return event;
    }

    private DcsEvent createNewLineageEvent(DcsDeliverableUnit du) {
        DcsEvent event = ingest.getEventManager()
                .newEvent(Events.ID_ASSIGNMENT);
        event.setOutcome(String.format(
                "LineageId %s is getting assigned to deliverable unit %s",
                du.getLineageId(), du.getId()));
        event.setDetail(String
                .format("Deliverable unit %s is the first of its lineage. Newly minted lineageId %s is assigned to this deliverable unit.",
                        du.getId(), du.getLineageId()));
        event.addTargets(new DcsEntityReference(du.getId()));

        return event;
    }

    private DcsEvent createUpdateEvent(DcsDeliverableUnit du,
                                       DcsDeliverableUnit pred) {
        DcsEvent event = ingest.getEventManager().newEvent(Events.DU_UPDATE);
        event.setOutcome(String.format("Update %s with %s", pred.getId(),
                du.getId()));
        event.setDetail("Update '" + pred.getId() + "' of lineage "
                + pred.getLineageId() + " with '" + du.getId() + "'");
        event.addTargets(new DcsEntityReference(pred.getId()));

        return event;
    }

    // Recursively label predecessors in SIP.
    // The base case is a du in the archive or without a predecessor in the SIP.
    // Keep track of labeled dus to avoid cycles and mistakenly reassigning lineage ids.

    private void label(Collection<DcsDeliverableUnit> dus, DcsDeliverableUnit du,
                       List<DcsEvent> events, Set<String> labelled)
            throws IngestServiceException {
        boolean has_predecessor = false;
        String pred_id;
        DcsDeliverableUnit pred;
        if (labelled.contains(du.getId())) {

            // TODO What if SIP already had lineage ids set?
            if (du.getLineageId() == null) {
                throw new IngestServiceException(
                        "Cycle of successors in SIP detected: " + du.getId());
            }

            return;
        }

        labelled.add(du.getId());
        for (DcsRelation rel : du.getRelations()) {
            if (rel.getRelUri().equals(DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                if (has_predecessor) {
                    throw new IngestServiceException(
                            "At most one predecessor allowed: " + du.getId());
                }

                has_predecessor = true;

                pred_id = getIsSuccessorOfTarget(rel);
                pred = null;

                for (DcsDeliverableUnit test : dus) {
                    if (test.getId().equals(pred_id)) {
                        pred = test;
                        label(dus, test, events, labelled);
                        break;
                    }
                }

                if (pred == null) {
                    pred = lookupPredecessorInArchive(pred_id);
                }
                if (pred != null) {
                    checkPredecessor(du, pred);
                    du.setLineageId(pred.getLineageId());

                    events.add(createLineageIdAssignmentEvent(du, pred));
                    events.add(createUpdateEvent(du, pred));
                } else {
                    throw new IngestServiceException(String.format("Predecessor of DeliverableUnit %s could not be found in the SIP " +
                            "or in the archive or it is not itself a DeliverableUnit.", du.getId()));

                }
            }
        }
        if (!has_predecessor) {
            du.setLineageId(idService.create(Types.LINEAGE.getTypeName()).getUrl().toString());
            events.add(createNewLineageEvent(du));
        }
    }

    @Override
    public void execute(String sipRef) throws IngestServiceException {
        if (isDisabled()) {
            return;
        }

        Dcp sip = ingest.getSipStager().getSIP(sipRef);

        List<DcsEvent> events = new ArrayList<DcsEvent>();
        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
        Set<String> labelled = new HashSet<String>();

        for (DcsDeliverableUnit du : dus) {
            label(dus, du, events, labelled);
        }

        sip.setDeliverableUnits(dus);

        for (DcsEvent e : events) {
            ingest.getEventManager().addEvent(sipRef, e);
        }

        ingest.getSipStager().updateSIP(sip, sipRef);
    }
}
