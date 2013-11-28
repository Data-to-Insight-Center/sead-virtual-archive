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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LineageTest {
    private LineageEntry entryOne;
    private LineageEntry entryTwo;
    private LineageEntry entryThree;
    private LineageEntry secondLineageEntryOne;
    private LineageEntry secondLineageEntryTwo;
    private LineageEntry singleLineageEntry;
    
    private Lineage lineageOne;
    private Lineage lineageTwo;
    private Lineage singleLineage;
    
    private final static String LINEAGE_ID_ONE = "lineage:/test:lineage";
    private final static String LINEAGE_ID_TWO = "lineage:/test:lineage_two";
    private final static long TIMESTAMP_ONE = 1111111l;
    
    private final static String ENTITY_ID_ONE = "urn:/entity";
   
    @Before
    public void setup() {
        entryOne = new LineageEntryImpl(ENTITY_ID_ONE, LINEAGE_ID_ONE, TIMESTAMP_ONE);
        entryTwo = new LineageEntryImpl("urn:/entityTwo:id", LINEAGE_ID_ONE, TIMESTAMP_ONE-100);
        entryThree = new LineageEntryImpl("urn:/entityThree", LINEAGE_ID_ONE, TIMESTAMP_ONE-1000);
        
        List<LineageEntry> entryList = new ArrayList<LineageEntry>();
        entryList.add(entryOne);
        entryList.add(entryTwo);
        entryList.add(entryThree);
        
        lineageOne = new LineageImpl(entryList);
        
        secondLineageEntryOne = new LineageEntryImpl("urn:/entity", LINEAGE_ID_TWO, TIMESTAMP_ONE);
        secondLineageEntryTwo = new LineageEntryImpl("urn:/entityTwo:id", LINEAGE_ID_TWO, TIMESTAMP_ONE-100);
        
        List<LineageEntry> lineageTwoEntryList = new ArrayList<LineageEntry>();
        lineageTwoEntryList.add(secondLineageEntryOne);
        lineageTwoEntryList.add(secondLineageEntryTwo);
        
        lineageTwo = new LineageImpl(lineageTwoEntryList);
        
        singleLineageEntry = entryOne;
        
        List<LineageEntry> singleLineageEntryList = new ArrayList<LineageEntry>();
        singleLineageEntryList.add(singleLineageEntry);
        
        singleLineage = new LineageImpl(singleLineageEntryList);        
    }
    
    /**
     * Tests the get newest call. The lineage with multiple entities should return the newest. 
     * The single lineage should return the only entry since it is by default newest.
     */
    @Test
    public void testGetNewest() {
        //Test that the newest of the three entries is returned
        assertEquals(entryOne, lineageOne.getNewest());
        
        //Check that the single entry is returned as the newest
        assertEquals(singleLineageEntry, singleLineage.getNewest());
    }
    
    /**
     * Tests the get oldest call. The lineage with mulitple entities should return the oldest.
     * The single lineage should return the only entry since it is by default oldest.
     */
    @Test
    public void testGetOldest() {
        //Test that the last of the tree entries is returned
        assertEquals(entryThree, lineageOne.getOldest());
        
        //Check that the single entry is returned as oldest
        assertEquals(singleLineageEntry, singleLineage.getOldest());
    }
    
    /**
     * Tests the two contains calls using the entity id and the entry object.
     */
    @Test
    public void testContains() {
        //Test the entity id contains
        assertTrue(lineageOne.contains(ENTITY_ID_ONE));
        assertFalse(lineageOne.contains("FOO"));
        
        //Test the entry contains
        assertTrue(lineageOne.contains(LINEAGE_ID_ONE));
        assertFalse(lineageOne.contains(LINEAGE_ID_TWO));
    }
    
    /**
     * Tests that the id of the lineage matches the lineage of the entries. 
     */
    @Test
    public void getId() {
        assertEquals(LINEAGE_ID_ONE, lineageOne.getId());
    }
    
    /**
     * Tests the equals, since the single entity lineage has the same id as Lineage one they should be considered equals.
     */
    @Test
    public void testEquals() {
        assertTrue(lineageOne.equals(singleLineage));
        
        assertFalse(lineageOne.equals(lineageTwo));
    }
}