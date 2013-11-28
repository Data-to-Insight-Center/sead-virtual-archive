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

public class VersionTest extends SWORDExtensionTest {

    private static final String VERSION = "1.0-TEST";

    @Test
    public void setVersionTest() {
        Version v = getFactory().newExtensionElement(SWORDExtensionFactory.VERSION);
        v.setVersion(VERSION);

        v = (Version) reconstitute(v);

        Assert.assertEquals("Version is wrong", VERSION, v.getVersion());
    }
}
