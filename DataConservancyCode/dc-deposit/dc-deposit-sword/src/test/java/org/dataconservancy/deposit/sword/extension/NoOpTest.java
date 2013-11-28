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
package org.dataconservancy.deposit.sword.extension;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class NoOpTest extends SWORDExtensionTest {
    @Test
    public void setNoOpTest() {
        NoOp noop = getFactory().newExtensionElement(SWORDExtensionFactory.NO_OP);

        noop.setNoOp(true);
        assertEquals("Should be true", true, noop.getNoOp());

        noop = (NoOp) reconstitute(noop);
        assertEquals("Should be true", true, noop.getNoOp());

        noop.setNoOp(false);
        assertEquals("Should be false", false, noop.getNoOp());

        noop = (NoOp) reconstitute(noop);
        assertEquals("Should be false", false, noop.getNoOp());
    }

    @Test
    public void defaultNoOpValueTest() {
        NoOp noop = getFactory().newExtensionElement(SWORDExtensionFactory.NO_OP);
        assertEquals("Should be false", false, noop.getNoOp());
    }
}
