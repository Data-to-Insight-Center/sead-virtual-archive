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

import org.dataconservancy.mhf.representations.MetadataAttribute;
import org.dataconservancy.mhf.representations.MetadataAttributeSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: hanh
 * Date: 1/18/13
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataAttributeSetTest {

    MetadataAttributeSet metadataAttributeSet1;
    MetadataAttributeSet metadataAttributeSet1Plus;
    MetadataAttributeSet metadataAttributeSet1PlusPlus;
    MetadataAttributeSet metadataAttributeSet3;
    MetadataAttribute metadataAttribute1;
    MetadataAttribute metadataAttribute1Plus;
    MetadataAttribute metadataAttribute1PlusPlus;
    MetadataAttribute metadataAttribute4;

    @Before
    public void setUp() {
        metadataAttributeSet1 = new MetadataAttributeSet("MDAttributeSet");
        metadataAttributeSet1Plus = new MetadataAttributeSet("MDAttributeSet");
        metadataAttributeSet1PlusPlus = new MetadataAttributeSet("MDAttributeSet");
        metadataAttributeSet3 = new MetadataAttributeSet("MDAttributeSet");

        metadataAttribute1 = new MetadataAttribute("name1", "type1", "value1");
        metadataAttribute1Plus = new MetadataAttribute("name2", "type2", "value3");
        metadataAttribute1PlusPlus = new MetadataAttribute("name3", "type3", "value3");
        metadataAttribute4 = new MetadataAttribute("name4", "type4", "value4");

        metadataAttributeSet1.addAttribute(metadataAttribute1);
        metadataAttributeSet1.addAttribute(metadataAttribute1Plus);
        metadataAttributeSet1.addAttribute(metadataAttribute1PlusPlus);

        metadataAttributeSet1Plus.addAttribute(metadataAttribute1);
        metadataAttributeSet1Plus.addAttribute(metadataAttribute1Plus);
        metadataAttributeSet1Plus.addAttribute(metadataAttribute1PlusPlus);

        metadataAttributeSet1PlusPlus.addAttribute(metadataAttribute1);
        metadataAttributeSet1PlusPlus.addAttribute(metadataAttribute1Plus);
        metadataAttributeSet1PlusPlus.addAttribute(metadataAttribute1PlusPlus);

        metadataAttributeSet3.addAttribute(metadataAttribute4);

    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertFalse(metadataAttributeSet1.equals(metadataAttributeSet3));
        assertFalse(metadataAttributeSet3.equals(metadataAttributeSet1));
    }


    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(metadataAttributeSet1.equals(metadataAttributeSet1Plus));
        assertTrue(metadataAttributeSet1Plus.equals(metadataAttributeSet1));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(metadataAttributeSet1.equals(metadataAttributeSet1Plus));
        assertTrue(metadataAttributeSet1Plus.equals(metadataAttributeSet1PlusPlus));
        assertTrue(metadataAttributeSet1.equals(metadataAttributeSet1PlusPlus));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(metadataAttributeSet1.equals(metadataAttributeSet1PlusPlus));
        assertTrue(metadataAttributeSet1.equals(metadataAttributeSet1PlusPlus));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(metadataAttributeSet1.equals(null));
    }

    @Test
    public void testGetAttributesByName() {
        assertTrue(metadataAttributeSet1.getAttributesByName(metadataAttribute1.getName()).contains(metadataAttribute1));
        assertFalse(metadataAttributeSet1.getAttributesByName(metadataAttribute1.getName()).contains(metadataAttribute1Plus));
        assertFalse(metadataAttributeSet1.getAttributesByName(metadataAttribute1.getName()).contains(metadataAttribute1PlusPlus));
        assertFalse(metadataAttributeSet1.getAttributesByName(metadataAttribute1.getName()).contains(metadataAttribute4));
    }
}