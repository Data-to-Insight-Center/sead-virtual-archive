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
import org.dataconservancy.model.dcs.DcsRelation;
import org.dataconservancy.model.dcs.DcsRelationship;

/**
 * An implement of {@link BusinessObjectSearcher} that search for the latest{@link BusinessObjectState} in the DCS archive.
 */
public abstract class ArchiveBusinessObjectSearcherImpl extends BaseArchiveSearcher
        implements BusinessObjectSearcher {

    private IdService idService;

    public ArchiveBusinessObjectSearcherImpl(DcsConnector connector, IdService idService) {
        super(connector);
        this.idService = idService;
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
    public abstract BusinessObjectState findLatestState(String business_id);
}
