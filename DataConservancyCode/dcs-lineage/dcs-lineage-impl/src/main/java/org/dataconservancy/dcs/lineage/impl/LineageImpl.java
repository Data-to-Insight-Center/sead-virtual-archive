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

import java.util.Iterator;
import java.util.List;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;

public class LineageImpl implements Lineage {

    List<LineageEntry> lineageEntries;
    String lineageId; 
    
    /**
     * Constructor that takes the ordered lineage
     * @param lineage The list of lineage entries from newest first to oldest last
     */
    public LineageImpl(List<LineageEntry> lineage) {
        lineageEntries = lineage;
        
        if (!lineageEntries.isEmpty()) {
            //All the entries have the same lineage id so just use the first one
            lineageId = lineageEntries.get(0).getLineageId();
        }
        
        //TODO: Check the order of the lineage and sort if necessary
    }
    
    @Override
    public String getId() {
        return lineageId;
    }

    @Override
    public LineageEntry getOldest() {
       LineageEntry oldest = null;
       
       if (!lineageEntries.isEmpty()) {
           oldest = lineageEntries.get(lineageEntries.size()-1);
       }
       return oldest;
    }

    @Override
    public LineageEntry getNewest() {
        LineageEntry newest = null;
        
        if (!lineageEntries.isEmpty()) {
            newest = lineageEntries.get(0);
        }
        
        return newest;
    }

    @Override
    public boolean contains(String id) {
        
        boolean found = false;
        
        if (lineageId.equalsIgnoreCase(id)) {
            found = true;
        } else {
            for (LineageEntry entry : lineageEntries) {
                if (entry.getEntityId().equalsIgnoreCase(id)) {
                    found = true;
                    break;
                }
            }
        }
        
        return found;
    }

    @Override
    public Iterator<LineageEntry> iterator() {
        return lineageEntries.iterator();
    }
    
    /**
     * As per the javadoc for Lineage two lineages are considered to be equal if they share the same lineage id. 
     * This equals checks to ensure that the lineage ids are the same and nothing more.
     */
    @Override
    public boolean equals(Object o) {
        Lineage otherLineage = (Lineage)o;
        
        //As per the java doc two lineages are equal if they have the same id
        return lineageId.equalsIgnoreCase(otherLineage.getId());
    }
    
}