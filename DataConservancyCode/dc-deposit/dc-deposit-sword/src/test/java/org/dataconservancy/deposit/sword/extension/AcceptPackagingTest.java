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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AcceptPackagingTest extends SWORDExtensionTest {
    private final String PACKAGE_NAME = "urn:test:pkg_name";
    private final String PREFERENCE = ".8";

    @Test
    public void setAcceptedPackagingTest() {
        AcceptPackaging packaging = newAcceptPackaging();
        packaging.setAcceptedPackaging(PACKAGE_NAME);

        packaging = (AcceptPackaging) reconstitute(packaging);

        assertEquals("Packaging does not match",
                     PACKAGE_NAME,
                     packaging.getAcceptedPackaging());

    }

    @Test
    public void setPreferenceTest() {
        AcceptPackaging packaging = newAcceptPackaging();
        packaging.setPreference(PREFERENCE);

        packaging = (AcceptPackaging) reconstitute(packaging);

        assertEquals("Preference does not match",
                     PREFERENCE,
                     packaging.getPreference());
    }

    @Test
    public void hasPreferenceTest() {
        AcceptPackaging packaging = newAcceptPackaging();

        packaging = (AcceptPackaging) reconstitute(packaging);
        assertNull(packaging.getPreference());
        assertFalse(packaging.hasPreference());

        packaging.setPreference(PREFERENCE);
        packaging = (AcceptPackaging) reconstitute(packaging);
        assertTrue(packaging.hasPreference());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectNonURIsTest() {
        AcceptPackaging pkg = getFactory().newExtensionElement(
            SWORDExtensionFactory.ACCEPT_PACKAGING);
        pkg.setAcceptedPackaging("not a URI");
    }


    public AcceptPackaging newAcceptPackaging() {
        getFactory().getAbdera();
        return getFactory().newExtensionElement(SWORDExtensionFactory.ACCEPT_PACKAGING);
    }

}
