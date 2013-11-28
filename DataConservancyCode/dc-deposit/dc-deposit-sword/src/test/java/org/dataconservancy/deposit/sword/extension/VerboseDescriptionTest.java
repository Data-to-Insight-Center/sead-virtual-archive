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

import junit.framework.Assert;
import org.junit.Test;

public class VerboseDescriptionTest extends SWORDExtensionTest {
    private static final String VERBOSE_DESCRIPTION = "verbose";

    @Test
    public void testSetVerboseDescription() {
        VerboseDescription vbd = getFactory().newExtensionElement(
            SWORDExtensionFactory.VERBOSE_DESCRIPTION);

        vbd.setVerboseDescription(VERBOSE_DESCRIPTION);

        vbd = (VerboseDescription) reconstitute(vbd);

        Assert.assertEquals("Verbose description is incorrect",
                            VERBOSE_DESCRIPTION,
                            vbd.getVerboseDescription());
    }
}
