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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class IdSetTest {

    private IdSet set = new IdSet();

    @Test
    public void testAdd() throws Exception {
        IdSet<DcsDeliverableUnit> set = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("id:/foo");
        set.add(du);

        assertTrue(set.contains(du));
        assertEquals(1, set.size());
        assertTrue(set.iterator().hasNext());
        assertEquals(du, set.iterator().next());
        assertSame(du, set.iterator().next());

        // mutate the du
        du.setId("id:/bar");

        assertSame(du, set.iterator().next());
        assertEquals(du, set.iterator().next());
    }

    @Test
    public void testAddAll() throws Exception {
        IdSet<DcsDeliverableUnit> one = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit duOne = new DcsDeliverableUnit();
        duOne.setId("id:/foo");
        one.add(duOne);

        IdSet<DcsDeliverableUnit> two = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit duTwo = new DcsDeliverableUnit();
        duTwo.setId("id:/foo");
        two.add(duTwo);

        assertTrue(one.contains(duOne));
        assertFalse(one.contains(duTwo));

        assertFalse(two.contains(duOne));
        assertTrue(two.contains(duTwo));

        one.addAll(two);

        assertTrue(one.contains(duOne));
        assertTrue(one.contains(duTwo));

        assertFalse(two.contains(duOne));
        assertTrue(two.contains(duTwo));
    }

    @Test
    public void testClearAndIsEmpty() throws Exception {
        IdSet<DcsDeliverableUnit> set = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("id:/foo");
        set.add(du);

        assertEquals(1, set.size());
        assertTrue(set.contains(du));
        assertTrue(set.iterator().hasNext());
        assertEquals(du, set.iterator().next());
        assertFalse(set.isEmpty());

        set.clear();

        assertEquals(0, set.size());
        assertFalse(set.contains(du));
        assertFalse(set.iterator().hasNext());
        assertTrue(set.isEmpty());
    }

    @Test
    public void testRemove() throws Exception {
        IdSet<DcsDeliverableUnit> set = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId("id:/foo");
        set.add(du);


    }

    @Test
    public void testRemoveAll() throws Exception {

    }

    @Test
    public void testRetainAll() throws Exception {

    }

    @Test
    public void testToArray() throws Exception {

    }

    @Test
    public void testIterator() throws Exception {

    }

    @Test
    public void testSize() throws Exception {

    }

    @Test
    public void testContains() throws Exception {

    }

    @Test
    public void testContainsAll() throws Exception {

    }

    @Test
    public void testEqualsEmpty() throws Exception {
        assertTrue(new IdSet().equals(new IdSet()));
    }

    @Test
    public void testEquals() throws Exception {
        IdSet<DcsDeliverableUnit> setOne = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit duOne = new DcsDeliverableUnit();
        duOne.setId("id:/foo");
        setOne.add(duOne);

        IdSet<DcsDeliverableUnit> setTwo = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit duTwo = new DcsDeliverableUnit();
        duTwo.setId("id:/foo");
        setTwo.add(duTwo);

        assertEquals(duOne, duTwo);
        assertEquals(setOne, setTwo);

        DcsDeliverableUnit duThree = new DcsDeliverableUnit();
        duThree.setId("id:/bar");
        setTwo.add(duThree);

        assertFalse(setOne.equals(setTwo));
    }

    @Test
    public void testEqualsContract() throws Exception {
        IdSet<DcsDeliverableUnit> setOne = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit duOne = new DcsDeliverableUnit();
        duOne.setId("id:/foo");
        setOne.add(duOne);

        IdSet<DcsDeliverableUnit> setTwo = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit duTwo = new DcsDeliverableUnit();
        duTwo.setId("id:/foo");
        setTwo.add(duTwo);

        IdSet<DcsDeliverableUnit> setThree = new IdSet<DcsDeliverableUnit>();
        DcsDeliverableUnit duThree = new DcsDeliverableUnit();
        duThree.setId("id:/foo");
        setThree.add(duThree);

        // symmetric
        assertTrue(setOne.equals(setOne));

        // reflexive
        assertTrue(setTwo.equals(setOne) && setOne.equals(setTwo));

        // transitive
        assertTrue(setOne.equals(setTwo));
        assertTrue(setTwo.equals(setThree));
        assertTrue(setOne.equals(setTwo));

        // consistent
        assertTrue(setOne.equals(setTwo) && setOne.equals(setTwo));

        // modify contents setOne
        duOne.setId("id:/bar");
        assertFalse(setOne.equals(setTwo));
    }

    @Test
    public void testHashCode() throws Exception {

    }
}
