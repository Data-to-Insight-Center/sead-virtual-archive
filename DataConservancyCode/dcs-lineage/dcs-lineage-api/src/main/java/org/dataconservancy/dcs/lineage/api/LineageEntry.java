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

/**
 * Encapsulates the properties of entries in a Lineage.
 */
public interface LineageEntry extends Comparable<LineageEntry>{

    /**
     * The DCS identifier of the DCS entity in the Lineage.
     *
     * @return the identifier of the DCS entity
     */
    public String getEntityId();

    /**
     * The opaque identifier of the the Lineage the entry belongs to.
     *
     * @return the opaque identifier of the Lineage
     */
    public String getLineageId();

    /**
     * The UTC timestamp (in milliseconds since the epoch 00:00:00, January 1, 1970) representing the time the entity
     * was versioned.  Lineage entries will most commonly be ordered by their timestamp.
     *
     * @return the timestamp
     */
    public long getEntryTimestamp();
    
}