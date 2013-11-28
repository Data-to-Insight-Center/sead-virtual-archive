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

package org.dataconservancy.dcs.lineage.api;

import java.util.Iterator;

/**
 * Represents versions of an object in the Data Conservancy over time, by maintaining an ordered list of LineageEntry
 * objects.
 * <p/>
 * The relationship between a DCS entity and a Lineage are as follows:
 * <ul>
 *  <li>A DCS entity must belong to one and only one Lineage.</li>
 *  <li>A Lineage contains one or more DCS entities.</li>
 *  <li>The DCS entities referenced by a Lineage must all be of the same type.</li>
 *  <li>Currently only Deliverable Units may participate in a Linage</li>
 * </ul>
 * Therefore, DCS entities that belong to the same Lineage semantically represent versions of the same object.
 * <p/>
 * Note that a Lineage is not guaranteed to be complete: specifically the oldest or newest entry in a Lineage may not be
 * the <em>original</em> or <em>latest</em> entry available.  In a <em>complete</em> Lineage the oldest entry will be
 * the same as the original entry, and the newest entry will be the same as the latest entry.
 * <p/>
 * It is also not guaranteed that a Linage will be contiguous, but practical use-cases for sparse Lineages have not
 * been determined, so it is safe for callers to assume that Lineages will be contiguous.
 * <p/>
 * Finally, the order of the entries in a Lineage is determined by the implementation. It is expected that entries in
 * a Lineage will be ordered by time, either ascending or descending.
 */
public interface Lineage extends Iterable<LineageEntry> {

    /**
     * Returns an identifier for this Lineage.  The identifier of the Lineage can be used to determine if two
     * entities are different versions of the same object.
     * <p/>
     * Lineages with the same identifier are considered to be equivalent, even if they are not identical.  For example,
     * it is possible to retrieve non-identical Lineage instances that represent the same object (one instance may
     * contain a partial range, the other instance may be complete; their contents are not identical but conceptually
     * the represent the lineage of the same object).
     *
     * @return the identifier of the Lineage
     */
    public String getId();

    /**
     * Returns the entry of the oldest entity in this Lineage.  This may not be the same as the <em>original</em>
     * entry if this Lineage is not <em>complete</em>.  The oldest entry is returned regardless of the order of the
     * Lineage.
     *
     * @return the oldest entry in this Lineage
     * @see LineageService#isOriginal(String)
     */
    public LineageEntry getOldest();

    /**
     * Returns the entry of the newest entity in the Lineage.  This may not be the same as the <em>latest</em>
     * entry if this Lineage is not <em>complete</em>.  The newest entry is returned regardless of the order of the
     * Lineage.
     *
     * @return the newest entry in this Lineage
     * @see LineageService#getLatest(String)
     */
    public LineageEntry getNewest();

    /**
     * Returns true if this Lineage contains the supplied id.
     *
     * @param id The id of the item to be checked, this can be either the entity id or the lineage id. 
     * @return true if this contains the supplied entity, false otherwise.
     */
    public boolean contains(String id);

    /**
     * Provides an Iterator over the identifiers of the entities in this Lineage.  The order of iteration should be
     * documented by implementations.  Typically entries would be ordered chronologically from oldest to newest, or from
     * newest to oldest.
     * 
     * @return an implementation-dependent iterator over the entries in this Lineage
     */
    @Override
    public Iterator<LineageEntry> iterator();

}