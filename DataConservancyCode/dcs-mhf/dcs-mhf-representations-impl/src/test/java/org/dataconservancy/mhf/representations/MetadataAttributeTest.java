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

package org.dataconservancy.mhf.representations;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetadataAttributeTest {

    MetadataAttribute metadataAttribute1;
    MetadataAttribute metadataAttribute1Plus;
    MetadataAttribute metadataAttribute1PlusPlus;
    MetadataAttribute metadataAttribute3;
    DateTime date1 = new DateTime();

    @Before
    public void setUp() {
        String date1String = String.valueOf(date1);
        metadataAttribute1 = new MetadataAttribute("name", "type", date1String);
        metadataAttribute1Plus = new MetadataAttribute("name", "type", date1String);
        metadataAttribute1PlusPlus = new MetadataAttribute("name", "type", date1String);
        metadataAttribute3 = new MetadataAttribute("name3", "type3", date1String);

    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertFalse(metadataAttribute1.equals(metadataAttribute3));
        assertFalse(metadataAttribute3.equals(metadataAttribute1));
    }


    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(metadataAttribute1.equals(metadataAttribute1Plus));
        assertTrue(metadataAttribute1Plus.equals(metadataAttribute1));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(metadataAttribute1.equals(metadataAttribute1Plus));
        assertTrue(metadataAttribute1Plus.equals(metadataAttribute1PlusPlus));
        assertTrue(metadataAttribute1.equals(metadataAttribute1PlusPlus));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(metadataAttribute1.equals(metadataAttribute1PlusPlus));
        assertTrue(metadataAttribute1.equals(metadataAttribute1PlusPlus));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(metadataAttribute1.equals(null));
    }

    @Test
    public void testDateToStringConversion() {
        assertEquals(date1,  new DateTime(metadataAttribute1.getValue()));
    }

}