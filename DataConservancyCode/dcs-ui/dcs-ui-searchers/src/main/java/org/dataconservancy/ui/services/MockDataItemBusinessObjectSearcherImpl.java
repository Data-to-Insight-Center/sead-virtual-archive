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

import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsDeliverableUnitRef;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.dataconservancy.ui.model.Id;
import org.dataconservancy.ui.profile.DataItemProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class MockDataItemBusinessObjectSearcherImpl extends MockBusinessObjectSearcherImpl
        implements DataItemBusinessObjectSearcher {

    public MockDataItemBusinessObjectSearcherImpl(MockArchiveUtil archiveUtil) {
        super(archiveUtil);
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

                if (du.getFormerExternalRefs().contains(business_id) && (du.getType().equals(DataItemProfile.DATASET_TYPE) || du.getType().equals(DataItemProfile.DATASET_STATE_TYPE))) {
                    dus.add(du);
                }
            }
        }

        List<DcsDeliverableUnit> dusCopy = new ArrayList<DcsDeliverableUnit>();
        dusCopy.addAll(dus);

        for (Set<DcsEntity> entities : archiveUtil.getEntities().values()) {
            for (DcsEntity entity : entities) {
                if (!(entity instanceof DcsDeliverableUnit)) {
                    continue;
                }
                for (DcsDeliverableUnit du : dusCopy) {
                    if (! (((DcsDeliverableUnit) entity).getType().equals(DataItemProfile.DATASET_STATE_TYPE) ||
                            ((DcsDeliverableUnit) entity).getType().equals(DataItemProfile.DATASET_TYPE))) continue;
                    if (((DcsDeliverableUnit) entity).getParents().contains(new DcsDeliverableUnitRef(du.getId()))) {
                        dus.add((DcsDeliverableUnit) entity);
                    }
                }
            }
        }

        // Return du without a successor that has a parent
        DcsDeliverableUnit rootDu = null;
        DcsDeliverableUnit latestStateDu = null;

        next:
        for (DcsDeliverableUnit du : dus) {
            for (DcsDeliverableUnit du2 : dus) {
                String pred_id = getPredecessorId(du2);

                if (pred_id == null && du.getParents().isEmpty()) {
                    rootDu = du;
                    continue next;
                }

                if (pred_id != null && pred_id.equals(du.getId())) {
                    rootDu = du;
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

    @Override
    public DcsFile findDataSetFile(String business_id) {
        for (Set<DcsEntity> entities : archiveUtil.getEntities().values()) {
            for (DcsEntity entity : entities) {
                if (!(entity instanceof DcsFile)) {
                    continue;
                }

                DcsFile file = (DcsFile) entity;

                for (DcsResourceIdentifier res : file.getAlternateIds()) {
                    if (res.getIdValue().equals(business_id)
                            && Id.getAuthority().equals(res.getAuthorityId())
                            && Types.DATA_SET.name().equals(res.getTypeId())) {
                        return file;
                    }
                }
            }
        }

        return null;
    }
}
