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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.execution.OutputFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Verifies basic characteristics of an {@link OutputFactory} for indexing
 * purposes.
 * <p>
 * Instances of {@link OutputFactory} that can pass these tests meet the minimum
 * requirements in order to be used safely within the transform/feature
 * extraction framework. There may be additional index-specific considerations
 * to test for - but any implementation that fails one or more of these tests is
 * unsafe and needs special consideration in order to determine if it is
 * suitable for a given purpose and execution environment.
 * </p>
 * <p>
 * The basic characteristics tested are:
 * <dl>
 * <dt>Durability upon close()</dt>
 * <dd>Upon invoking close(), all values written to a given {@link Output}
 * should be accessible.</dd>
 * <dt>Invisibility without close()</dt>
 * <dd>If an {@link Output} instance has not been closed, items should not be
 * accessible or indexed.</dd>
 * <dt>Isolation</dt>
 * <dd>Calling close() one one {@link Output} instance should not affect the
 * operation of other instances created by the same factory.</dd>
 * <dt>Idempotency</dt>
 * <dd>A single value written many times should not affect the final outcome.</dd>
 * <dt>Thread safety</dt>
 * <dd>A given {@link} Output instance should be capable of being written by
 * multiple threads.</dd>
 * </dl>
 * <p>
 */
public abstract class AbstractIndexOutputFactoryTest<K, V> {

    @Test
    public void testIndexedUponClose() {
        TestableIndexOutputFactory<K, V> factory = getFactory();
        TestableIndexOutput<K, V> indexer = factory.newOutput();

        V value = newValue();

        /* Make sure it's not already in index */
        assertFalse(indexer.hasIndexed(value));

        /* Do a simple write */
        indexer.write(newKey(), value);
        indexer.close();

        assertTrue(indexer.hasIndexed(value));
    }

    @Test
    public void testNotIndexedWithoutClose() {
        TestableIndexOutputFactory<K, V> factory = getFactory();
        TestableIndexOutput<K, V> indexer = factory.newOutput();

        try {

            V value = newValue();
            indexer.write(newKey(), value);

            assertFalse("Entry was indexed before a close()!",
                        indexer.hasIndexed(value));
        } finally {
            indexer.close();
        }
    }

    @Test
    public void testIsolationOfOutputs() {
        TestableIndexOutputFactory<K, V> factory = getFactory();
        TestableIndexOutput<K, V> indexer1 = factory.newOutput();
        TestableIndexOutput<K, V> indexer2 = factory.newOutput();

        try {
            V value1 = newValue();
            V value2 = newValue();

            /*
             * Make sure the values are indeed different, and not already
             * indexed.
             */
            assertFalse(value1.equals(value2));

            assertFalse(indexer1.hasIndexed(value1));
            assertFalse(indexer2.hasIndexed(value2));

            /* Write both values, but only commit/close value2 */
            indexer2.write(newKey(), value2);
            indexer1.write(newKey(), value1);
            indexer2.close();

            /* value2 should be indexed, but not value1 */
            assertTrue(indexer2.hasIndexed(value2));
            assertFalse(indexer2.hasIndexed(value1));
            assertFalse(indexer1.hasIndexed(value1));
        } finally {
            indexer1.close();
            indexer2.close();
        }
    }

    @Test
    public void testIdempotency() {
        TestableIndexOutputFactory<K, V> factory = getFactory();
        TestableIndexOutput<K, V> indexer = factory.newOutput();

        try {
            V value1 = newValue();
            V value2 = newValue();
            K key1 = newKey();
            K key2 = newKey();

            /* Make sure our entries aren't in the index already */
            assertFalse(indexer.hasIndexed(value1));
            assertFalse(indexer.hasIndexed(value2));

            /* Write same values multiple times */
            long initialSize = indexer.getIndexSize();
            indexer.write(key1, value1);
            indexer.write(key2, value2);
            indexer.write(key1, value1);
            indexer.write(key1, value1);
            indexer.write(key2, value2);
            int added = indexer.closeAndCount();

            /* Make sure our entries have been indexed */
            assertTrue(indexer.hasIndexed(value1));
            assertTrue(indexer.hasIndexed(value2));

            /* Make sure only the proper entries have been added */
            assertEquals(initialSize + added, indexer.getIndexSize());
        } finally {
            indexer.close();
        }
    }

    @Test
    public void testThreadSafe() throws Exception {
        int count = 100;
        List<Future<V>> processed = new ArrayList<Future<V>>(count);
        List<V> values = new ArrayList<V>(count);
        ExecutorService exe = Executors.newFixedThreadPool(3);

        final TestableIndexOutputFactory<K, V> factory = getFactory();
        final TestableIndexOutput<K, V> indexer = factory.newOutput();

        try {
            long initialSize = indexer.getIndexSize();

            for (int i = 0; i < count; i++) {
                processed.add(exe.submit(new Callable<V>() {

                    public V call() throws Exception {
                        V value = newValue();
                        assertFalse(indexer.hasIndexed(value));
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
            assertEquals(initialSize, indexer.getIndexSize());

            int added = indexer.closeAndCount();

            /*
             * Make sure the size of the index has increased by the amount
             * specified
             */
            assertEquals(initialSize + added, indexer.getIndexSize());

            /* Make sure all results are indexed */
            for (int i = 0; i < count; i++) {
                assertTrue(indexer.hasIndexed(values.get(i)));
            }
        } finally {
            indexer.close();
        }
    }

    /** Create a new, random (or at least distinct) value */
    protected abstract V newValue();

    /** Create a new, random (or at least distinct) key */
    protected abstract K newKey();

    protected abstract TestableIndexOutputFactory<K, V> getFactory();
}
