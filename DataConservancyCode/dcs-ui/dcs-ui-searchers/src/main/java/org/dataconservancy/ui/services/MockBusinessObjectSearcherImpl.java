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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.ui.model.Id;

public class MockBusinessObjectSearcherImpl implements 
        CollectionBusinessObjectSearcher {
    final MockArchiveUtil archiveUtil;

    public MockBusinessObjectSearcherImpl(MockArchiveUtil archiveUtil) {
        this.archiveUtil = archiveUtil;
    }

    String getPredecessorId(DcsDeliverableUnit du) {
        for (DcsRelation rel : du.getRelations()) {
            if (rel.getRelUri().equals(DcsRelationship.IS_SUCCESSOR_OF.asString())) {
                if (rel.getRef() == null || rel.getRef().getRef() == null) {
                    return null;
                }

                return rel.getRef().getRef();
            }
        }

        return null;
    }
    
    @Override
    public BusinessObjectState findLatestState(String business_id) {
        List<DcsDeliverableUnit> dus = new ArrayList<DcsDeliverableUnit>();
        
        for (Set<DcsEntity> entities : archiveUtil.getEntities().values()) {
            for (DcsEntity entity : entities) {
                if (!(entity instanceof DcsDeliverableUnit)) {
                    continue;
                }

                DcsDeliverableUnit du = (DcsDeliverableUnit) entity;

                if (du.getFormerExternalRefs().contains(business_id)) {
                    dus.add(du);
                }
            }
        }

        // Return du without a successor that has a parent
        DcsDeliverableUnit rootDu = null;
        DcsDeliverableUnit latestStateDu = null;
        
        next: for (DcsDeliverableUnit du : dus) {
            for (DcsDeliverableUnit du2 : dus) {
                String pred_id = getPredecessorId(du2);

                if (pred_id == null && du.getParents().isEmpty()) {
                    rootDu = du;
                    continue next;
                }

                if (pred_id != null && pred_id.equals(du.getId())) {
                    continue next;
                }
            }

            latestStateDu = du;
        }

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

        return null;
    }
}
