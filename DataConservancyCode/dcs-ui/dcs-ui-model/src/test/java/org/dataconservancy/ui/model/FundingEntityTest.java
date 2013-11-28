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


public class FundingEntityTest {

    private FundingEntity one;
    private FundingEntity equals;
    private FundingEntity transitive;
    private FundingEntity different;

    @Before
    public void SetUp() {
        one = new FundingEntity("United Federation of Planets", "NCC-1701-D");
        equals = new FundingEntity("United Federation of Planets", "NCC-1701-D");
        transitive = new FundingEntity("United Federation of Planets", "NCC-1701-D");
        different = new FundingEntity("Romulan Star Empire", "Warbird");
    }

    @Test
    public void testReflexive() {
        assertTrue(one.equals(one));
        assertFalse(one.equals(different));
    }

    @Test
    public void testSymmetric() {
        assertTrue(one.equals(equals));
        assertTrue(equals.equals(one));
    }

    @Test
    public void testTransitive() {
        assertTrue(one.equals(equals));
        assertTrue(equals.equals(transitive));
        assertTrue(one.equals(transitive));
    }

    @Test
    public void testConsistent() {
        assertTrue(one.equals(equals));
        assertTrue(one.equals(equals));
    }

    @Test
    public void testNonNull() {
        assertFalse(one.equals(null));
    }

    @Test
    public void testEqualityUsingCopyConstructor() {
        FundingEntity copy = new FundingEntity(one);

        // transitive
        assertTrue(one.equals(copy));
        assertTrue(copy.equals(equals));
        assertTrue(one.equals(equals));

        // symmetric
        assertTrue(one.equals(copy));
        assertTrue(copy.equals(one));

        // consistent
        assertTrue(one.equals(copy));
        assertTrue(one.equals(copy));

        // nullity
        assertFalse(copy.equals(null));
    }


}
