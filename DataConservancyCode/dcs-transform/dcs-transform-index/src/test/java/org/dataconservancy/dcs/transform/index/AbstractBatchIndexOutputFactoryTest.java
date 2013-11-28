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
package org.dataconservancy.dcs.transform.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import org.dataconservancy.transform.Output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBatchIndexOutputFactoryTest<K, V> {

    protected abstract TestableBatchIndexOutputFactory<K, V> newFactory();

    /** Create a new, random (or at least distinct) value */
    protected abstract V newValue();

    /** Create a new, random (or at least distinct) key */
    protected abstract K newKey();

    @Test
    public void testIndexedUponDefaultFactoryClose() {
        TestableBatchIndexOutputFactory<K, V> factory = newFactory();

        try {
            Output<K, V> out = factory.newOutput();

            V value = newValue();

            /* Make sure it's not already in index */
            assertFalse(factory.hasIndexed(value));

            /* Do a simple write */
            out.write(newKey(), value);

            factory.close();
            assertTrue(factory.hasIndexed(value));
        } finally {
            factory.close();
        }
    }

    @Test
    public void testNotIndexedUponOutputClose() {
        TestableBatchIndexOutputFactory<K, V> factory = newFactory();

        try {
            Output<K, V> out = factory.newOutput();

            V value = newValue();

            /* Make sure it's not already in index */
            assertFalse(factory.hasIndexed(value));

            /* Do a simple write */
            out.write(newKey(), value);

            /* Should not index at this point */
            out.close();
            assertFalse(factory.hasIndexed(value));

            /* Now it should be indexed */
            factory.close();
            assertTrue(factory.hasIndexed(value));
        } finally {
            factory.close();
        }
    }

    @Test
    public void testIndexedUponSuccessfulFactoryClose() {
        TestableBatchIndexOutputFactory<K, V> factory = newFactory();

        try {
            Output<K, V> out = factory.newOutput();

            V value = newValue();

            /* Make sure it's not already in index */
            assertFalse(factory.hasIndexed(value));

            /* Do a simple write */
            out.write(newKey(), value);

            factory.close(true);
            assertTrue(factory.hasIndexed(value));
        } finally {
            factory.close();
        }
    }

    @Test
    public void testNotIndexedUponUnsuccessfulFactoryClose() {
        TestableBatchIndexOutputFactory<K, V> factory = newFactory();

        try {
            Output<K, V> out = factory.newOutput();

            V value = newValue();

            /* Make sure it's not already in index */
            assertFalse(factory.hasIndexed(value));

            /* Do a simple write */
            out.write(newKey(), value);

            factory.close(false);
            assertFalse(factory.hasIndexed(value));
        } finally {
            factory.close();
        }
    }

    @Test
    public void testIdempotencyOfOutput() {
        TestableBatchIndexOutputFactory<K, V> factory = newFactory();
        Output<K, V> indexer = factory.newOutput();

        try {
            V value1 = newValue();
            V value2 = newValue();
            K key1 = newKey();
            K key2 = newKey();

            /* Make sure our entries aren't in the index already */
            assertFalse(factory.hasIndexed(value1));
            assertFalse(factory.hasIndexed(value2));

            /* Write same values multiple times */
            long initialSize = factory.getIndexSize();
            indexer.write(key1, value1);
            indexer.write(key2, value2);
            indexer.write(key1, value1);
            indexer.write(key1, value1);
            indexer.write(key2, value2);
            int added = factory.closeAndCount();

            /* Make sure our entries have been indexed */
            assertTrue(factory.hasIndexed(value1));
            assertTrue(factory.hasIndexed(value2));
            
            /* Make sure only the proper entries have been added */
            assertEquals(initialSize + added, factory.getIndexSize());
        } finally {
            factory.close();
        }
    }

    @Test
    public void testIdempotencyAcrossOutputs() {

        TestableBatchIndexOutputFactory<K, V> factory = newFactory();
        Output<K, V> out1 = factory.newOutput();
        Output<K, V> out2 = factory.newOutput();

        try {
            V value1 = newValue();
            V value2 = newValue();
            K key1 = newKey();
            K key2 = newKey();

            /* Make sure our entries aren't in the index already */
            assertFalse(factory.hasIndexed(value1));
            assertFalse(factory.hasIndexed(value2));

            /* Write same values multiple times to multiple Outputs */
            long initialSize = factory.getIndexSize();
            out1.write(key1, value1);
            out2.write(key1, value1);
            out1.write(key2, value2);
            out2.write(key2, value2);
            out1.write(key1, value1);
            out2.write(key1, value1);
            out1.write(key1, value1);
            out2.write(key1, value1);
            out1.write(key2, value2);
            out2.write(key2, value2);
            int added = factory.closeAndCount();

            /* Make sure our entries have been indexed */
            assertTrue(factory.hasIndexed(value1));
            assertTrue(factory.hasIndexed(value2));

            /* Make sure only the proper entries have been added */
            assertEquals(initialSize + added, factory.getIndexSize());
        } finally {
            factory.close();
        }
    }

    @Test
    public void testIdempotencyOfFactoryClose() {
        TestableBatchIndexOutputFactory<K, V> factory = newFactory();

        try {
            Output<K, V> out = factory.newOutput();

            V value = newValue();

            /* Make sure it's not already in index */
            assertFalse(factory.hasIndexed(value));

            long initialSize = factory.getIndexSize();

            /* Do a simple write */
            out.write(newKey(), value);

            int added = factory.closeAndCount();
            assertTrue(factory.hasIndexed(value));
            assertEquals(initialSize + added, factory.getIndexSize());

            /* Do another close, which should have no effect on index size */
            factory.close();
            factory.close();
            assertEquals(initialSize + added, factory.getIndexSize());
        } finally {
            factory.close();
        }
    }

    @Test
    public void testIsolationOfFactories() {
        TestableBatchIndexOutputFactory<K, V> factory1 = newFactory();
        TestableBatchIndexOutputFactory<K, V> factory2 = newFactory();
        Output<K, V> out1 = factory1.newOutput();
        Output<K, V> out2 = factory2.newOutput();

        try {
            V value1 = newValue();
            V value2 = newValue();

            /*
             * Make sure the values are indeed different, and not already
             * indexed.
             */
            assertFalse(value1.equals(value2));

            assertFalse(factory1.hasIndexed(value1));
            assertFalse(factory2.hasIndexed(value2));

            /* Write both values, but only commit/close value2 */
            out2.write(newKey(), value2);
            out1.write(newKey(), value1);
            factory2.close();

            /* value2 should be indexed, but not value1 */
            assertTrue(factory2.hasIndexed(value2));
            assertFalse(factory2.hasIndexed(value1));
            assertFalse(factory1.hasIndexed(value1));
        } finally {
            factory1.close();
        }
    }

    @Test
    public void testNonIsolationOfOutputs() {
        TestableBatchIndexOutputFactory<K, V> factory = newFactory();
        Output<K, V> out1 = factory.newOutput();
        Output<K, V> out2 = factory.newOutput();

        V value1 = newValue();
        V value2 = newValue();

        /*
         * Make sure the values are indeed different, and not already indexed.
         */
        assertFalse(value1.equals(value2));

        assertFalse(factory.hasIndexed(value1));
        assertFalse(factory.hasIndexed(value2));

        /* Write both values, to different outouts, and close the factory */
        out1.write(newKey(), value2);
        out2.write(newKey(), value1);
        factory.close();

        /* Both values should be indexed */
        assertTrue(factory.hasIndexed(value1));
        assertTrue(factory.hasIndexed(value2));
    }

    @Test
    public void testThreadSafeWithinOutputs() throws Exception {
        int count = 100;
        List<Future<V>> processed = new ArrayList<Future<V>>(count);
        List<V> values = new ArrayList<V>(count);
        ExecutorService exe = Executors.newFixedThreadPool(3);

        final TestableBatchIndexOutputFactory<K, V> factory = newFactory();
        final Output<K, V> indexer = factory.newOutput();

        try {
            long initialSize = factory.getIndexSize();

            for (int i = 0; i < count; i++) {
                processed.add(exe.submit(new Callable<V>() {

                    public V call() throws Exception {
                        V value = newValue();
                        assertFalse(factory.hasIndexed(value));
                        indexer.write(newKey(), value);
                        return value;
                    }
                }));
            }

            /* Get all results, waiting for them to finish if necessary */
            for (Future<V> task : processed) {
                values.add(task.get());
            }

            /* Make sure the size of the index has not changed! */
            assertEquals(initialSize, factory.getIndexSize());

            int added = factory.closeAndCount();

            /*
             * Make sure the size of the index has increased by the amount
             * specified
             */
            assertEquals(initialSize + added, factory.getIndexSize());

            /* Make sure all results are indexed */
            for (int i = 0; i < count; i++) {
                assertTrue(factory.hasIndexed(values.get(i)));
            }
        } finally {
            indexer.close();
        }
    }

    @Test
    public void testThreadSafeAcrossOutputs() throws Exception {
        int count = 100;
        List<Future<V>> processed = new ArrayList<Future<V>>(count);
        List<V> values = new ArrayList<V>(count);
        ExecutorService exe = Executors.newFixedThreadPool(3);

        final TestableBatchIndexOutputFactory<K, V> factory = newFactory();
        final List<Output<K, V>> outputs = new ArrayList<Output<K, V>>();
        outputs.add(factory.newOutput());
        outputs.add(factory.newOutput());

        try {
            long initialSize = factory.getIndexSize();

            final Random gen = new Random();

            for (int i = 0; i < count; i++) {
                processed.add(exe.submit(new Callable<V>() {

                    public V call() throws Exception {
                        V value = newValue();
                        assertFalse(factory.hasIndexed(value));
                        outputs.get(gen.nextInt(2)).write(newKey(), value);
                        return value;
                    }
                }));
            }

            /* Get all results, waiting for them to finish if necessary */
            for (Future<V> task : processed) {
                values.add(task.get());
            }

            /* Make sure the size of the index has not changed! */
            assertEquals(initialSize, factory.getIndexSize());

            int added = factory.closeAndCount();

            /*
             * Make sure the size of the index has increased by the amount
             * specified
             */
            assertEquals(initialSize + added, factory.getIndexSize());

            /* Make sure all results are indexed */
            for (int i = 0; i < count; i++) {
                assertTrue(factory.hasIndexed(values.get(i)));
            }
        } finally {
            factory.close();
        }
    }
}
