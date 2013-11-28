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

import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;

import java.util.Set;

/**
 * Common utility methods used by Archive Service implementations.
 */
public interface ArchiveUtil {

    /**
     * Retrieve the identified entity from the DCS archive.
     *
     * @param id the entity identifier
     * @return the entity, or null if it cannot be found.
     */
    public DcsEntity getEntity(String id);

    /**
     * From a pool of of candidate Deliverable Units, determine which - if any - of the Deliverable Units is
     * the root of an object graph.
     * <p/>
     * Because the User Interface maps business objects into a graph headed by a single Deliverable Unit,
     * and because UI objects are deposited one at a time into the DCS, a deposit into the archive is
     * guaranteed to have a single Deliverable Unit as the root of the object graph (though the graph may contain
     * descendant Deliverable Units as child nodes in the graph).
     * <p/>
     * Given all of the Deliverable Units that comprise a deposit, determine which Deliverable Unit is the root
     * of the object graph.  If no root can be determined, or if there are multiple possible roots, {@code null}
     * is returned.
     *
     * @param candidates candidate Deliverable Units
     * @return the root of the object graph, or {@code null} if it cannot be determined.
     * @see org.dataconservancy.ui.services.DepositDocument#getEntities() 
     */
    public DcsDeliverableUnit determineDepositRoot(Set<DcsDeliverableUnit> candidates);

}
