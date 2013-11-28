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

import org.dataconservancy.mhf.representation.api.Altitude;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class SimpleAltitudeImplTest {
    Altitude altitude1;
    Altitude altitude2;
    Altitude altitude3;
    Altitude altitude4;
    DateTime date1 = new DateTime();

    @Before
    public void setUp() {
        String date1String = String.valueOf(date1);
        altitude1 = new SimpleAltitudeImpl(233.36);
        altitude2 = new SimpleAltitudeImpl(233.36);
        altitude3 = new SimpleAltitudeImpl(233.36);
        altitude4 = new SimpleAltitudeImpl(363.66);

    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertFalse(altitude1.equals(altitude4));
        assertFalse(altitude4.equals(altitude1));
    }


    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(altitude1.equals(altitude2));
        assertTrue(altitude2.equals(altitude1));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(altitude1.equals(altitude2));
        assertTrue(altitude2.equals(altitude3));
        assertTrue(altitude1.equals(altitude3));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(altitude1.equals(altitude3));
        assertTrue(altitude1.equals(altitude3));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(altitude1.equals(null));
    }


}
