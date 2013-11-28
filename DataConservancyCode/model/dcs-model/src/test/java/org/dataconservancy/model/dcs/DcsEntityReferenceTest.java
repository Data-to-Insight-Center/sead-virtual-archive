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
package org.dataconservancy.model.dcs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class DcsEntityReferenceTest {

    private static final String ref = "targetId";

    @Test
    public void testCopyConstructorA() {
        final DcsEntityReference underTest = new DcsEntityReference(ref);
        assertEquals(underTest, new DcsEntityReference(underTest));
    }

    @Test
    public void testCopyConstructorB() {
        final DcsEntityReference underTest = new DcsEntityReference();
        underTest.setRef(ref);
        assertEquals(underTest, new DcsEntityReference(underTest));
    }

}
