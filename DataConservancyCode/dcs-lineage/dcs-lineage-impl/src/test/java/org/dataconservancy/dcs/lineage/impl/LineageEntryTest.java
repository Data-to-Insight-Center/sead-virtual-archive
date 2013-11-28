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

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.dcs.lineage.api.LineageEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LineageEntryTest {
    private LineageEntry entryOne;
    private LineageEntry entrySameAsOne;
    private LineageEntry entryTwo;
    
    private final static String ENTITY_ID_ONE = "urn:/entity:id";
    private final static String LINEAGE_ID_ONE = "lineage:/test:lineage";
    private final static long TIMESTAMP_ONE = 1111111l;
    
    @Before
    public void setup() {
        entryOne = new LineageEntryImpl(ENTITY_ID_ONE, LINEAGE_ID_ONE, TIMESTAMP_ONE);
        entrySameAsOne = entryOne;
        
        entryTwo = new LineageEntryImpl("urn:/entityTwo:id", "lineage:/test:lineage_two", 1010131l);
    }
    
    /**
     * Tests that the values of entry one are retrieved correctly.
     */
    @Test
    public void testGetMethods() {
        assertTrue(entryOne.getEntityId().equalsIgnoreCase(ENTITY_ID_ONE));
        
        assertTrue(entryOne.getLineageId().equalsIgnoreCase(LINEAGE_ID_ONE));
        
        assertEquals(TIMESTAMP_ONE, entryOne.getEntryTimestamp());
    }
    
    /**
     * Tests the comparison functions.
     */
    @Test
    public void testCompareTo() {
        
        assertEquals(0, entryOne.compareTo(entrySameAsOne));
        assertEquals(1, entryOne.compareTo(entryTwo));
    }
}