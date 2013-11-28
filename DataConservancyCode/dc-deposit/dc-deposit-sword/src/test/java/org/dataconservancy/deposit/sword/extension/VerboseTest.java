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

public class VerboseTest extends SWORDExtensionTest {
    @Test
    public void setNoOpTest() {
        Verbose verbose = getFactory().newExtensionElement(SWORDExtensionFactory.VERBOSE);

        verbose.setVerbose(true);
        assertEquals("Should be true", true, verbose.getVerbose());

        verbose = (Verbose) reconstitute(verbose);
        assertEquals("Should be true", true, verbose.getVerbose());

        verbose.setVerbose(false);
        assertEquals("Should be false", false, verbose.getVerbose());

        verbose = (Verbose) reconstitute(verbose);
        assertEquals("Should be false", false, verbose.getVerbose());
    }

    @Test
    public void defaultNoOpValueTest() {
        Verbose verbose = getFactory().newExtensionElement(SWORDExtensionFactory.VERBOSE);
        assertEquals("Should be false", false, verbose.getVerbose());
    }
}
