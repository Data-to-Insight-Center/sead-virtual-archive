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

package org.dataconservancy.ui.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the status properties holder.
 */
public class StatusPropertiesContextTest {

    private StatusPropertiesContext oneA = new StatusPropertiesContext();
    private StatusPropertiesContext oneB = new StatusPropertiesContext();
    private StatusPropertiesContext oneC = new StatusPropertiesContext();

    private StatusPropertiesContext different = new StatusPropertiesContext();

    @Before
    public void setUp() {

        oneA.setBuildRevision("1");
        oneA.setBuildNumber("2");
        oneA.setBuildTimeStamp("A Date");

        oneB.setBuildRevision("1");
        oneB.setBuildNumber("2");
        oneB.setBuildTimeStamp("A Date");


        oneC.setBuildRevision("1");
        oneC.setBuildNumber("2");
        oneC.setBuildTimeStamp("A Date");


        different.setBuildRevision("2");
        different.setBuildNumber("3");
        different.setBuildTimeStamp("A Different Date");

    }

    /**
     * Tests reflexive requirement
     */
    @Test
    public void testReflexive() {
        assertTrue(oneA.equals(oneA));
        assertFalse(oneA.equals(different));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(oneA.equals(oneC));
        assertTrue(oneC.equals(oneA));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(oneA.equals(oneB));
        assertTrue(oneB.equals(oneC));
        assertTrue(oneA.equals(oneC));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(oneA.equals(oneB));
        assertTrue(oneA.equals(oneB));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(oneA.equals(null));
    }

}
