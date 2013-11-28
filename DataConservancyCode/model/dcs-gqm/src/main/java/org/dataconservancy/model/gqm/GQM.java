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
package org.dataconservancy.model.gqm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Information associated with an entity. The order of the relations, locations
 * and time intervals is not considered significant when determining equality.
 */
public class GQM {
    private String entity_id;
    private final List<Relation> relations;
    private final List<Location> locations;
    private final List<DateTimeInterval> intervals;

    public GQM(String entity_id) {
        this.entity_id = entity_id;
        this.relations = new ArrayList<Relation>(2);
        this.locations = new ArrayList<Location>(2);
        this.intervals = new ArrayList<DateTimeInterval>(2);
    }

    public String getEntityId() {
        return entity_id;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<DateTimeInterval> getIntervals() {
        return intervals;
    }

    public void setEntityId(String entity_id) {
        this.entity_id = entity_id;
    }

    public int hashCode() {
        return entity_id == null ? 0 : entity_id.hashCode();
    }

    public boolean equals(Object o) {
        GQM gqm = (GQM) o;

        if (gqm == null) {
            return false;
        }

        return Util.equals(entity_id, gqm.entity_id)
                && new HashSet<Relation>(relations)
                        .equals(new HashSet<Relation>(gqm.relations))
                && new HashSet<Location>(locations)
                        .equals(new HashSet<Location>(gqm.locations))
                && new HashSet<DateTimeInterval>(intervals)
                        .equals(new HashSet<DateTimeInterval>(gqm.intervals));
    }

    public String toString() {
        return (entity_id == null ? "" : entity_id) + "locations: " + locations
                + " relations: " + relations + " intervals: " + intervals;
    }
}
