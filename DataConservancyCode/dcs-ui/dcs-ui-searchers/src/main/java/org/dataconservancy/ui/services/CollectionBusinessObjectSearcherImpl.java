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
package org.dataconservancy.ui.services;

import org.dataconservancy.access.connector.DcsConnector;
import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.ui.dcpmap.AbstractVersioningMapper;
import org.dataconservancy.ui.profile.CollectionProfile;
import org.dataconservancy.ui.util.SolrQueryUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An implementation of  {@link CollectionBusinessObjectSearcher}.
 */
public class CollectionBusinessObjectSearcherImpl extends ArchiveBusinessObjectSearcherImpl
        implements CollectionBusinessObjectSearcher {

    /**
     * business id, count, query
     */
    private static final String ERR_NO_ROOT_DU = "Expected a Collection Root DU to be found for '%s'.  Query " +
            "produced %s candidates, but none were a Root DU (Query was: '%s').";

    /**
     * business id, count, query
     */
    private static final String ERR_UNEXPECTED_STATE_DU_COUNT = "Expected a single State DU to be found for '%s', " +
            "found %s.  (Query was: '%s')";


    public CollectionBusinessObjectSearcherImpl(DcsConnector connector, IdService idService) {
        super(connector, idService);
    }


    @Override
    public BusinessObjectState findLatestState(String business_id) {
        String query1 = SolrQueryUtil.createLiteralQuery("AND", "entityType",
                "DeliverableUnit", "former", business_id, "type", AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE);

        String query2 = SolrQueryUtil.createLiteralQuery("AND", "entityType",
                "DeliverableUnit", "former", business_id, "type", CollectionProfile.STATE_DU_TYPE);

        String query = "(" + query1 + ") OR (" + query2 + ")";

        List<DcsDeliverableUnit> dus = new ArrayList<DcsDeliverableUnit>();

        // The Root DU of the object graph
        DcsDeliverableUnit rootDu = null;
        
        for (DcsEntity entity : performSearch(query)) {
            if (!(entity instanceof DcsDeliverableUnit)) {
                continue;
            }

            DcsDeliverableUnit du = (DcsDeliverableUnit) entity;
            
            if (du.getType().equals(AbstractVersioningMapper.ROOT_DELIVERABLE_UNIT_TYPE)) {
                rootDu = du;
            } else {            
                dus.add(du);
            }
        }

        // The most recent version of the state DU of the object graph
        DcsDeliverableUnit latestStateDu = null;

        // Figure out the State DU: the DU with no predecessor that has parents
        Set<String> predecessorIds = new HashSet<String>();

        for (DcsDeliverableUnit du : dus) {
            predecessorIds.add(getPredecessorId(du));
        }

        Iterator<DcsDeliverableUnit> itr = dus.iterator();
        outer:
        while (itr.hasNext()) {
            DcsDeliverableUnit du = itr.next();
            for (String predecessorId : predecessorIds) {

                // Remove any DUs that are predecessors
                if (du.getId().equals(predecessorId)) {
                    itr.remove();
                    continue outer;
                }
            }
        }

        // We should be left with a single DU which is our latest state, or none.  But never multiple candidates.
        if (dus.size() > 1) {
            log.warn(String.format(ERR_UNEXPECTED_STATE_DU_COUNT, business_id, dus.size(), query));
        } else if (dus.size() == 1) {
            latestStateDu = dus.iterator().next();
        }

        // In order to return an anonymous inner class, the objects have to be final.
        final DcsDeliverableUnit rootDuCopy = (rootDu != null) ?
                new DcsDeliverableUnit(rootDu) : null;
        final DcsDeliverableUnit latestStateDuCopy = (latestStateDu != null) ?
                new DcsDeliverableUnit(latestStateDu) : null;

        if (rootDuCopy != null || latestStateDuCopy != null) {
            return new BusinessObjectState() {
                @Override
                public DcsDeliverableUnit getRoot() {
                    return rootDuCopy;
                }

                @Override
                public DcsDeliverableUnit getLatestState() {
                    return latestStateDuCopy;
                }
            };
        }

        // If both the Root DU and State DU are null, just return null.
        return null;
    }
    
}
