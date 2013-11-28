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

import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class ContactInfoTest
        extends BaseUnitTest {

    private ContactInfo two = new ContactInfo();

    private ContactInfo three = new ContactInfo();

    @Before
    public void setUp() {

        two.setEmailAddress(contactInfoOne.getEmailAddress());
        two.setName(contactInfoOne.getName());
        two.setPhoneNumber(contactInfoOne.getPhoneNumber());
        two.setRole(contactInfoOne.getRole());
        two.setPhysicalAddress(contactInfoOne.getPhysicalAddress());

        three.setEmailAddress(contactInfoOne.getEmailAddress());
        three.setName(contactInfoOne.getName());
        three.setPhoneNumber(contactInfoOne.getPhoneNumber());
        three.setRole(contactInfoOne.getRole());
        three.setPhysicalAddress(contactInfoOne.getPhysicalAddress());
    }

    @Test
    public void testReflexive() {
        assertTrue(contactInfoOne.equals(contactInfoOne));
        assertFalse(contactInfoOne.equals(contactInfoTwo));
    }

    /**
     * Tests symmetric requirement
     */
    @Test
    public void testSymmetric() {
        assertTrue(contactInfoOne.equals(two));
        assertTrue(two.equals(contactInfoOne));
    }

    /**
     * Tests transitive requirement
     */
    @Test
    public void testTransitive() {
        assertTrue(contactInfoOne.equals(two));
        assertTrue(two.equals(three));
        assertTrue(contactInfoOne.equals(three));
    }

    /**
     * Tests consistent requirement
     */
    @Test
    public void testConsistent() {
        assertTrue(contactInfoOne.equals(two));
        assertTrue(contactInfoOne.equals(two));
    }

    /**
     * Tests non-null requirement
     */
    @Test
    public void testNonNull() {
        assertFalse(contactInfoOne.equals(null));
    }

    /**
     * Tests equality between Person objects, contactInfoOne created using the
     * copy constructor.
     */
    @Test
    public void testEqualityUsingCopyConstructor() {
        ContactInfo copyOne = new ContactInfo(contactInfoOne);

        // transitive
        assertTrue(contactInfoOne.equals(copyOne));
        assertTrue(copyOne.equals(three));
        assertTrue(contactInfoOne.equals(three));

        // symmetric
        assertTrue(contactInfoOne.equals(copyOne));
        assertTrue(copyOne.equals(contactInfoOne));

        // consistent
        assertTrue(contactInfoOne.equals(copyOne));
        assertTrue(contactInfoOne.equals(copyOne));

        // nullity
        assertFalse(copyOne.equals(null));
    }

}
