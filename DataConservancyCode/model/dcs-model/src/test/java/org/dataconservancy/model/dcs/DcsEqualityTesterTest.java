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

import org.dataconservancy.model.dcs.support.FieldFilter;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DcsEqualityTesterTest {

    @Test
    public void testEqualityTesterDcsEntity() throws Exception {
        FieldFilter underTest = new FieldFilter();
        underTest.addField(DcsEntity.class.getDeclaredField("id"));

        DcsEntity one = new DcsEntity();
        one.setId("one");
        DcsEntity two = new DcsEntity();
        two.setId("two");

        assertFalse(one.equals(two));
        assertTrue(one.equals(two, underTest));
    }

    @Test
    public void testEqualityTesterDcsDu() throws Exception {
        FieldFilter underTest = new FieldFilter();
        underTest.addField(DcsEntity.class.getDeclaredField("id"));

        DcsDeliverableUnit one = new DcsDeliverableUnit();
        one.setId("one");
        one.setType("foo");
        DcsDeliverableUnit two = new DcsDeliverableUnit();
        two.setId("two");
        two.setType("foo");
        assertFalse(one.equals(two));
        assertTrue(one.equals(two, underTest));
    }

    @Test
    public void testEqualityTesterWithCollection() throws Exception {
        FieldFilter underTest = new FieldFilter();
        underTest.addField(DcsEntity.class.getDeclaredField("id"));

        DcsDeliverableUnit one = new DcsDeliverableUnit();
        one.setId("one");
        one.setType("foo");
        one.addFormerExternalRef("formerRef1");
        one.addFormerExternalRef("formerRef2");

        DcsDeliverableUnit two = new DcsDeliverableUnit();
        two.setId("two");
        two.setType("foo");
        two.addFormerExternalRef("formerRef1");
        two.addFormerExternalRef("formerRef2");

        assertFalse(one.equals(two));
        assertTrue(one.equals(two, underTest));

        two.addFormerExternalRef("formerRef3");
        assertFalse(one.equals(two));
        assertFalse(one.equals(two, underTest));

        underTest.addField(DcsDeliverableUnit.class.getDeclaredField("formerExternalRefs"));
        assertFalse(one.equals(two));
        assertTrue(one.equals(two, underTest));
    }
    
}
