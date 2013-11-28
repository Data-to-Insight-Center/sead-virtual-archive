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
 * Provides access to lineage information in the Data Conservancy.
 */
public interface LineageService {
    
    /**
     * Obtains a complete Lineage for the given id. This id can be either the entity ID or the ID of the lineage the entity is a part of.
     * <p/>
     * The returned Lineage will be complete: it will be contiguous, containing the <em>oldest</em> and <em>latest</em>
     * entries inclusive, at the time of request.
     *
     * @param id The string id to get the lineage for.
     * @return The complete lineage of the entity, including the entity. Null if the entity could not be found.
     */
    public Lineage getLineage(String id);

    /**
     * Obtains a Lineage between to the supplied entity identifiers, inclusively. 
     * <p/>
     *
     * @param first_entity_id The first entity to use for comparison. This entity should have the second entity as a successor.
     * Note: If the first entity is null the lineage returned will from the second entity to the original. 
     * @param second_entity_id The second entity to use for comparison. This entity should have the first entity as a predecessor. 
     * Note: If the second entity is null the lineage returned will be from the first entity to the latest.
     * @return The lineage that between and including two given entities, will contain just the two entities if the first is a direct predecessor of the other.
     *  Returns null if the two entities are not of the same lineage, if one or both of the entity parameters could not be found, or if the entities are out of order.
     */
    public Lineage getLineageForEntityRange(String first_entity_id, String second_entity_id);

    /**
     * Obtains a Lineage between the supplied dates, inclusively.
     * <p/>
     * 
     * @param entity_id The entity to get the lineage of.
     * @param startDate The start date for comparison, this should be the earliest date. The date should be represented in milliseconds from the epoc (Jan 1 1970) in UTC.
     * Note: If start date is negative the Lineage returned will be from the endDate to the original.
     * @param endDate The end date for comparison, this should be the latest date. The date should be represented in milliseconds from the epoc (Jan 1 1970) in UTC.
     * Note: If end date is negative the Lineage returned will be from the startDate to the latest.
     * @return The lineage of the entity between the two dates, an empty lineage if there are no entities between the two dates, or null if the entity id could not be found
     * or endDate is less than start date. 
     */
    public Lineage getLineageForDateRange(String entity_id, long startDate, long endDate);

    
    /**
     * Returns the latest LineageEntry of the given entity. The LineageEntry returned will have no successors. 
     * <p/>
     *
     * @param entity_id The string entity id of the entity to get the latest version of.
     * @return The latest entry in the lineage, or null if the entity could not be found. If the passed in entity is the latest it will be returned. 
     */
    public LineageEntry getLatest(String entity_id);

    /**
     * Returns the original LineageEntry of the given entity. The LineageEntry returned will have no predecessors.
     * <p/>
     *
     * @param entity_id The entity id of the entity to return the original of.
     * @return The original entry of the lineage, or null if the entity could not be found. If the passed in entity is the original it will be returned.
     */
    public LineageEntry getOriginal(String entity_id);
    
    /**
     * Returns the first LineageEntry that occurs equal to or prior to the provided date.
     * <p/>
     *
     * @param entity_id The entity id of the entity to query the lineage of.
     * @param date The date at which the nearest entity should be returned. The date should be represented in milliseconds from the epoc (Jan 1 1970) in UTC.
     * @return The first lineage entry prior to the provided date. Returns null if there are no entities prior to the date provided.
     * Returns null if the entity could not be found.
     */
    public LineageEntry getEntryForDate(String entity_id, long date);
    
    /**
     * Checks if the given entity is the latest in the lineage.
     * <p/>
     * 
     * @param entity_id The entity id of the entity to query.
     * @return true if the entity is the latest in the lineage, false otherwise, including if the entity could not be found.
     */
    public boolean isLatest(String entity_id);
    
    /**
     * Checks if the given entity is the oldest in the lineage.
     * <p/>
     * 
     * @param entity_id The entity id of the entity to query.
     * @return true if the entity is the oldest in the lineage, false otherwise, including if the entity could not be found.
     */
    public boolean isOriginal(String entity_id);
    
}