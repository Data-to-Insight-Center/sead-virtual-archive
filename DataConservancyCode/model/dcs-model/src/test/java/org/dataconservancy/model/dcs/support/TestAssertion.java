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
package org.dataconservancy.model.dcs.support;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestAssertion {

    public static void assertCollectionsIdentical(Collection one, Collection two) {
        String oneHc = "Ox" + Integer.toHexString(System.identityHashCode(one));
        String twoHc = "Ox" + Integer.toHexString(System.identityHashCode(two));
        assertTrue("Expected Collection One (" + oneHc + ") to be identical to Collection Two (" + twoHc + ")",
                one == two);
    }

    public static void assertCollectionsNotIdentical(Collection one, Collection two) {
            String oneHc = "Ox" + Integer.toHexString(System.identityHashCode(one));
            String twoHc = "Ox" + Integer.toHexString(System.identityHashCode(two));
            assertFalse("Expected Collection One (" + oneHc + ") to *NOT* be identical to Collection Two (" + twoHc + ")",
                    one == two);
        }

    public static void assertCollectionsEqual(Collection<?> one, Collection<?> two) {
        String oneHc = "Ox" + Integer.toHexString(System.identityHashCode(one));
        String twoHc = "Ox" + Integer.toHexString(System.identityHashCode(two));
        assertEquals("Expected Collection One (" + oneHc + ") to be equal to Collection Two (" + twoHc + ")",
                one, two);
    }

    public static void assertCollectionsNotEqual(Collection<?> one, Collection<?> two) {
        String oneHc = "Ox" + Integer.toHexString(System.identityHashCode(one));
        String twoHc = "Ox" + Integer.toHexString(System.identityHashCode(two));
        assertFalse("Expected Collection One (" + oneHc + ") to *NOT* be identical to Collection Two (" + twoHc + ")",
                one.equals(two));
    }
}
