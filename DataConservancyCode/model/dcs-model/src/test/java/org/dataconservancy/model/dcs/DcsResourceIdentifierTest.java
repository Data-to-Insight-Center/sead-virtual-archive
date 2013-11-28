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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the DcsResourceIdentifier.
 */
public class DcsResourceIdentifierTest {

    DcsResourceIdentifier rid;
    DcsResourceIdentifier ridOther;

    String authorityId = "a123";
    String typeId = "s456";
    String idValue = "r789";

    @Before
    public void setUp() throws Exception {

        rid = new DcsResourceIdentifier();
        ridOther = new DcsResourceIdentifier();

        rid.setAuthorityId(authorityId);
        ridOther.setAuthorityId(authorityId);

        rid.setTypeId(typeId);
        ridOther.setTypeId(typeId);

        rid.setIdValue(idValue);
        ridOther.setIdValue(idValue);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIdValueEmptyString() throws Exception {
        rid.setIdValue(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIdValueNullString() throws Exception {
        rid.setIdValue(null);
    }

    @Test
    public void testEquals() throws Exception {

        final DcsResourceIdentifier notEqual = new DcsResourceIdentifier();
        assertFalse(rid.equals(notEqual));

        final DcsResourceIdentifier ridCopy = new DcsResourceIdentifier(rid);
        final DcsResourceIdentifier ridCopyTwo = new DcsResourceIdentifier(rid);

        // symmetric
        assertTrue(rid.equals(rid));
        assertTrue(ridCopy.equals(ridCopy));

        // reflexive
        assertTrue(rid.equals(ridCopy) && ridCopy.equals(rid));
        assertTrue(rid.equals(ridOther) && ridOther.equals(rid));

        // transitive
        assertTrue(rid.equals(ridCopy));
        assertTrue(rid.equals(ridCopyTwo));
        assertTrue(ridCopy.equals(ridCopyTwo));

        // consistent
        assertTrue(rid.equals(ridCopy) && rid.equals(ridCopy));
        assertTrue(ridCopy.equals(ridOther) && ridCopy.equals(ridOther));


    }

}
