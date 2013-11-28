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

/**
 * Encapsulates archival entities that can be used to reconstruct the state of a business object.  Instances of
 * this interface are returned by the {@link BusinessObjectSearcher}, which performs searches of the archive for
 * the current state of a business object.
 * <p/>
 * Mappers (see the {@link org.dataconservancy.ui.dcpmap.CollectionMapper} and {@link org.dataconservancy.ui.dcpmap.DataSetMapper}), in concert with profiles,
 * are the established way for converting a business object to its archival representation.  That pattern does not
 * provide access to a {@link BusinessObjectSearcher} per business object type. 
 *
 * @see <a href="https://scm.dataconservancy.org/confluence/x/HwCq>https://scm.dataconservancy.org/confluence/x/HwCq</a>
 */
public interface BusinessObjectState {

    /**
     * Returns the Deliverable Unit which is the root node in the object graph representing the Business Object.
     *
     * @return the DcsDeliverableUnit representing the latest state of the buiness object
     */
    public DcsDeliverableUnit getRoot();

    /**
     * Returns the Deliverable Unit which represents the latest state of the Business Object.  Depending on how
     * objects are mapped, the same Deliverable Unit may be returned by {@link #getRoot()}.
     *
     * @return the DcsDeliverableUnit representing the latest state of the business object
     */
    public DcsDeliverableUnit getLatestState();
}
