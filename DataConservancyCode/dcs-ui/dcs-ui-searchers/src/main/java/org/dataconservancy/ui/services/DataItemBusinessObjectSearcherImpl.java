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
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.support.HierarchicalPrettyPrinter;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Id;
import org.dataconservancy.ui.profile.DataItemProfile;
import org.dataconservancy.ui.profile.Profile;
import org.dataconservancy.ui.util.SolrQueryUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link DataItemBusinessObjectSearcher}.
 */
public class DataItemBusinessObjectSearcherImpl extends ArchiveBusinessObjectSearcherImpl implements DataItemBusinessObjectSearcher {

    private Profile<DataItem> profile;

    private ParentSearcher parentSearcher;

    /**
     * business id, count, query
     */
    private static final String ERR_UNEXPECTED_STATE_DU_COUNT = "Expected a single State DU to be found for '%s', " +
            "found %s.  (Query was: '%s')\n  Found the following state Deliverable Units:\n%s";

    public DataItemBusinessObjectSearcherImpl(DcsConnector connector, IdService idService, Profile<DataItem> profile,
                                              ParentSearcher parentSearcher) {
        super(connector, idService);
        this.profile = profile;
        this.parentSearcher = parentSearcher;
    }

    @Override
    public DcsFile findDataSetFile(String business_id) {
        String query = SolrQueryUtil.createLiteralQuery("AND", "entityType",
                "File", "resourceValue", business_id, "resourceAuthority",
                Id.getAuthority(), "resourceType", Types.DATA_FILE.name());

        Collection<DcsEntity> result = performSearch(query);

        if (result.size() == 0) {
            return null;
        }

        if (result.size() > 1) {
            log.info(String.format(ERR_PERFORMING_SEARCH, query,
                    "Should only be one matching DcsFile"));
        }

        return (DcsFile) result.iterator().next();
    }

    @Override
    public BusinessObjectState findLatestState(String business_id) {
        String query = SolrQueryUtil.createLiteralQuery("AND", "entityType",
                "DeliverableUnit", "former", business_id, "type", profile.getType());


        // TODO cleaner if search sorted on ingest.complete date (no connector
        // support for sort)

        List<DcsDeliverableUnit> dus = new ArrayList<DcsDeliverableUnit>();

        for (DcsEntity entity : performSearch(query)) {
            if (!(entity instanceof DcsDeliverableUnit)) {
                continue;
            }

            dus.add((DcsDeliverableUnit) entity);
        }

        List<DcsDeliverableUnit> dusCopy = new ArrayList<DcsDeliverableUnit>();
        dusCopy.addAll(dus);

        for (DcsDeliverableUnit du : dusCopy) {
            for (DcsEntity candidate : parentSearcher.getParentsOf(du.getId())) {
                if (candidate instanceof DcsDeliverableUnit) {
                    dus.add((DcsDeliverableUnit) candidate);
                }
            }
        }

        // The Root DU of the object graph
        DcsDeliverableUnit rootDu = null;

        // The most recent version of the state DU of the object graph
        DcsDeliverableUnit latestStateDu = null;

        for (DcsDeliverableUnit du : dus) {
            if (du.getType().equals(DataItemProfile.DATASET_TYPE) && getPredecessorId(du) == null && !du.getParents().isEmpty()) {
                rootDu = du;
                break;
            }
        }

        Set<String> predecessorIds = new HashSet<String>();
        for (DcsDeliverableUnit du : dus) {
            String predId = getPredecessorId(du);
            if (predId != null) {
                predecessorIds.add(predId);
            }
        }

        Iterator<DcsDeliverableUnit> itr = dus.iterator();
        outer:
        while (itr.hasNext()) {
            DcsDeliverableUnit du = itr.next();

            if (!du.getType().equals(DataItemProfile.DATASET_STATE_TYPE)) {
                itr.remove();
                continue;
            }

            for (String predId : predecessorIds) {
                if (du.getId().equals(predId)) {
                    itr.remove();
                    continue outer;
                }
            }
        }

        if (dus.size() > 1) {
            HierarchicalPrettyPrinter hpp = new HierarchicalPrettyPrinter();
            hpp.incrementDepth();
            for (DcsDeliverableUnit du : dus) {
                du.toString(hpp);
            }
            log.warn(String.format(ERR_UNEXPECTED_STATE_DU_COUNT, business_id, dus.size(), query, hpp.toString()));
        }

        if (dus.size() > 0) {
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

                @Override
                public String toString() {
                    return "Root DU '" +
                                ((rootDuCopy != null) ? rootDuCopy.toString() : "null") + "'" +
                            " State DU '" +
                                ((latestStateDuCopy != null) ? latestStateDuCopy.toString() : "null") + "'";
                }
            };
        }

        // If both the Root DU and State DU are null, just return null.
        return null;
    }
    
}
