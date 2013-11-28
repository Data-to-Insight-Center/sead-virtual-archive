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

package org.dataconservancy.dcs.lineage.impl;

import org.dataconservancy.dcs.lineage.api.LineageEntry;

public class LineageEntryImpl implements LineageEntry {

    private String entityId;
    private String lineageId;
    private long entryTimestamp;

    public LineageEntryImpl(String entityId, String lineageId, long timestamp) {
        this.entityId = entityId;
        this.lineageId = lineageId;
        this.entryTimestamp = timestamp;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    @Override
    public String getLineageId() {
        return lineageId;
    }

    @Override
    public long getEntryTimestamp() {
        return entryTimestamp;
    }
    
    @Override
    public int compareTo(LineageEntry o) {
        int compare = 0;

        //Throwing null pointer to be consistent with comparable
        if (o == null) {
            throw new NullPointerException();
        }
        
        if (o.getEntityId() == null || !entityId.equalsIgnoreCase(o.getEntityId())) {
            compare = 1;
        }
        
        if (o.getLineageId() == null || !lineageId.equalsIgnoreCase(o.getLineageId())) {
            compare = 1;
        }
        
        if (entryTimestamp != o.getEntryTimestamp()) {
            compare = 1;
        }
        
        return compare;
    }
    
    @Override
    public String toString() {
        return "EntityID: " + entityId + ", LineageID: " + lineageId + ", timestamp: " + entryTimestamp;
    }
    
}