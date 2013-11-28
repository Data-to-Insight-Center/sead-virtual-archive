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
 * Represents the status of a UI deposit transaction with the DCS archive.
 */
public interface DepositDocument {

    /**
     * The identifier of the deposit document, typically the deposit transaction identifier.
     *
     * @return the identifier of the deposit document, may be {@code null}.
     */
    public String getId();

    /**
     * If this deposit has completed ingest in the DCS.  Normally this is indicated by the presence of an
     * {@code ingest.complete} or {@code ingest.fail} event in the deposit status Atom feed.
     *
     * @return true if the deposit is complete, false otherwise
     */
    public boolean isComplete();

    /**
     * If this deposit was successful.  Normally this is indicated by the presence of an {@code ingest.complete}
     * event in the deposit status Atom feed.
     *
     * @return true if the deposit was successful, false otherwise
     */
    public boolean isSuccessful();

    /**
     * The root Deliverable Unit associated with the deposit. May be {@code null} if the root DU can not be
     * determined.
     *
     * @return the root Deliverable Unit of the deposit, or {@code null}
     * @see ArchiveUtil#determineDepositRoot(java.util.Set) 
     */
    public DcsDeliverableUnit getRoot();

    /**
     * All entities associated with the deposit.  This includes entities that occurred originally in the SIP, as well
     * as any entities that were created as a result of ingest.
     * 
     * @return the {@code Set} of {@code DcsEntity}s that are associated with the deposit.
     *         May be empty, but not {@code null}.
     */
    public Set<DcsEntity> getEntities();
    
}
