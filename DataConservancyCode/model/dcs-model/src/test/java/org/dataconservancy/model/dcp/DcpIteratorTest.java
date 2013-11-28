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
package org.dataconservancy.model.dcp;

import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsEvent;
import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class DcpIteratorTest {

    /**
     * Verifies the behavior of HashSet iterators
     */
    @Test(expected = ConcurrentModificationException.class)
    public void testHashSetIteratorIncrementalAdd() {
        HashSet s = new HashSet();
        final Object o1 = new Object();
        final Object o2 = new Object();

        s.add(o1);

        Iterator underTest = s.iterator();

        assertTrue(underTest.hasNext());
        assertTrue(o1 == underTest.next());

        s.add(o2);

        assertFalse(underTest.hasNext());
        underTest.next();
    }

    /**
     * Verifies the behavior of HashSet iterators
     */
    @Test(expected = ConcurrentModificationException.class)
    public void testHashSetIteratorBulkAdd() {
        HashSet s = new HashSet();
        final Object o1 = new Object();
        final Object o2 = new Object();
        final Object o3 = new Object();
        final Object o4 = new Object();

        s.add(o1);
        s.add(o2);

        Iterator underTest = s.iterator();

        s.add(o3);
        s.add(o4);

        underTest.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDcpIteratorRemove() {
        new Dcp().iterator().remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyDcpIteratorNext() {
        new Dcp().iterator().next();
    }

    @Test
    public void testEmptyDcpIteratorHasNext() {
        assertFalse(new Dcp().iterator().hasNext());
    }

    /**
     * Test where the iterators in the implementation are next to each other in
     * the switch.
     */
    @Test
    public void testDcpIteratorSimple() {
        Dcp sip = new Dcp();
        sip.addCollection(new DcsCollection());
        sip.addDeliverableUnit(new DcsDeliverableUnit());

        Iterator<DcsEntity> underTest = sip.iterator();

        assertTrue(underTest.hasNext());
        assertNotNull(underTest.next());
        assertTrue(underTest.hasNext());
        assertNotNull(underTest.next());
        assertFalse(underTest.hasNext());

        try {
            underTest.next();
            fail("Expected a NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
    }

    /**
     * Test where the iterators in the implementation are not next to each other in
     * the switch.
     */
    @Test
    public void testDcpIteratorNotAdjacentIterators() {
        Dcp sip = new Dcp();
        sip.addCollection(new DcsCollection());
        sip.addEvent(new DcsEvent());

        Iterator<DcsEntity> underTest = sip.iterator();

        assertTrue(underTest.hasNext());
        assertNotNull(underTest.next());
        assertTrue(underTest.hasNext());
        assertNotNull(underTest.next());
        assertFalse(underTest.hasNext());

        try {
            underTest.next();
            fail("Expected a NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
    }

    /**
     * Insures ConcurrentModificationException is thrown if <code>Iterator.next()</code> is called
     * after invoking <code>addXxx(...)</code> on <code>Dcp</code>.
     */
    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationAdd() {
        Dcp sip = new Dcp();
        DcsCollection col1 = new DcsCollection();
        DcsCollection col2 = new DcsCollection();
        col1.setId("foo");
        col2.setId("bar");

        sip.addCollection(col1);
        Iterator<DcsEntity> i = sip.iterator();
        sip.addCollection(col2);
        i.next();
    }

    /**
     * Insures ConcurrentModificationException is thrown if <code>Iterator.next()</code> is called
     * after invoking <code>setXxx(...)</code> on <code>Dcp</code>.
     */
    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentModificationSet() {
        Dcp sip = new Dcp();
        Iterator<DcsEntity> i = sip.iterator();
        sip.setCollections(new HashSet<DcsCollection>());
        i.next();
    }
}
