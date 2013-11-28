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
package org.dataconservancy.dcs.lineage.http.support;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Identifier;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.dcs.lineage.api.LineageService;
import org.dataconservancy.dcs.lineage.impl.LineageImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A mock implementation of the Lineage Service.
 */
public class MockLineageService implements LineageService {
    
    Map<String, Lineage> lineageMap;
    Map<String, String> entityLineageMap;
    IdService idService;

    /**
     * Allows a user of the mock service to inject a map of lineage ids to lineages.
     *
     * @param lineageMap
     */
    public void injectLineageMap(Map<String, Lineage> lineageMap) {
        this.lineageMap = lineageMap;
        if (null != lineageMap) {
            this.entityLineageMap = new HashMap<String, String>();
            for (Map.Entry<String, Lineage> entry : lineageMap.entrySet()) {
                for (LineageEntry lineageEntry : entry.getValue()) {
                    entityLineageMap.put(lineageEntry.getEntityId(), lineageEntry.getLineageId());
                }
            }
        }
    }

    /**
     * Allows a user of the mock service to inject an id service.
     * @param idService
     */
    public  void injectIdService(IdService idService) {
        this.idService = idService;
    }

    @Override
    public LineageEntry getEntryForDate(String entity_id, long date) {
        Lineage lineage = getLineage(entity_id);

        if (null == lineage) {
            return null;
        }

        for (LineageEntry entry : lineage) {
            if (entry.getEntryTimestamp() <= date) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public Lineage getLineage(String id) {
        Identifier identifier;
        try {
            identifier = idService.fromUid(id);
        } catch (IdentifierNotFoundException e) {
            return null;
        }
        if (identifier.getType().equals(Types.LINEAGE.getTypeName())) {
            return lineageMap.containsKey(identifier.getUid())
                    ? lineageMap.get(identifier.getUid())
                    : null;
        }
        else if (identifier.getType().equals(Types.DELIVERABLE_UNIT.getTypeName())) {
            return entityLineageMap.containsKey(identifier.getUid())
                    && lineageMap.containsKey(entityLineageMap.get(identifier.getUid()))
                    ? lineageMap.get(entityLineageMap.get(identifier.getUid()))
                    : null;
        }

        return null;
    }

    @Override
    public Lineage getLineageForEntityRange(String first_entity_id, String second_entity_id) {
        String idOfInterest = null != first_entity_id
                ? first_entity_id
                : second_entity_id;
        Lineage lineage = getLineage(idOfInterest);
        long startDate = -1, endDate = -1;

        if (null == lineage) {
            return null;
        }
        
        for (LineageEntry entry : lineage) {
            if (entry.getEntityId().equals(first_entity_id)) {
                startDate = entry.getEntryTimestamp();
            }
            if (entry.getEntityId().equals(second_entity_id)) {
                endDate = entry.getEntryTimestamp();
            }
        }

        //Either both of the parameters were null, or one non-null parameter
        // was not found in the lineage.
        if ((-1 == startDate && -1 == endDate) || (null != first_entity_id && -1 == startDate) || (null != second_entity_id && -1 == endDate)) {
            return null;
        }

        return getLineageForDateRange(idOfInterest, startDate, endDate);
    }

    @Override
    public Lineage getLineageForDateRange(String entity_id, long startDate, long endDate) {
        Lineage lineage = getLineage(entity_id);
        List<LineageEntry> returnedList = new ArrayList<LineageEntry>();

        if (null == lineage) {
            return null;
        }

        for (LineageEntry entry : lineage) {
            if ((-1 == startDate || entry.getEntryTimestamp() >= startDate) && (-1 == endDate || entry.getEntryTimestamp() <= endDate)) {
                returnedList.add(entry);
            }
        }

        if (returnedList.isEmpty()) {
            return null;
        }

        return new LineageImpl(returnedList);
    }

    @Override
    public LineageEntry getLatest(String entity_id) {
        Lineage lineage = getLineage(entity_id);
        if (null == lineage) {
            return null;
        }
        return lineage.getNewest();
    }

    @Override
    public LineageEntry getOriginal(String entity_id) {
        Lineage lineage = getLineage(entity_id);
        if (null == lineage) {
            return null;
        }
        return lineage.getOldest();
    }

    @Override
    public boolean isLatest(String entity_id) {
        throw new RuntimeException("This method is not implemented.");
    }

    @Override
    public boolean isOriginal(String entity_id) {
        throw new RuntimeException("This method is not implemented.");
    }
}
