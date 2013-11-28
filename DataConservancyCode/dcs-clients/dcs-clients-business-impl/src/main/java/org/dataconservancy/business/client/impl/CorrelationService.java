/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.business.client.impl;

import org.dataconservancy.business.client.Mapper;

/** Modifies an object in archival form so that it correlates with actual archival instances.
 * <p>
 * While an archive defines an archival form/type of an object, instances of archival objects
 * may not be consistent with a specific archival state (for example, archival IDs may be 
 * locally consistent within an archival object, rather than globally consistent with all 
 * other objects in the archive).  This class is used to correlate a locally consistent
 * archival object produced with a {@link Mapper} with the present state of an archive, 
 * ultimately prodicing archive objects in ready-to-deposit form.
 * </p>
 */
public interface CorrelationService<A> {

    /** Produce a ready-to-deposit object that is correlated with entities in the archive.
     * <p> 
     * All heavy lifting of resolving local identifiers with archival identifiers occurs here.
     * The resulting archival object should be fully consistent with the archive, and ready to 
     * deposit.  An optional predecessor archive ID is used to correlate this archival object with 
     * a specific predecessor.
     * </p>
     * @param archivalObject Archival object to correlate.
     * @param predecesorArchiveId Id of an archival predecessor, or null if not applicable.
     * @return A correlated archival object that is ready to deposit into the archive.
     */
    public A correlate(A archivalObject, String predecesorArchiveId);
}
