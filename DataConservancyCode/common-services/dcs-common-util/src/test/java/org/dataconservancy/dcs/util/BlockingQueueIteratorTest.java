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
package org.dataconservancy.dcs.util;

import org.junit.Test;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class BlockingQueueIteratorTest {
     private static File POISON = new File("poison");

    @Test(expected = NoSuchElementException.class)
    public void testEmptyNext() {
        final BlockingQueue<File> q = new LinkedBlockingQueue<File>();
        q.add(POISON);
        final BlockingQueueIterator bqi = new BlockingQueueIterator<File>(q, POISON);
        bqi.next();
    }

    @Test
    public void testEmptyHasNext() {
        final BlockingQueue<File> q = new LinkedBlockingQueue<File>();
        q.add(POISON);
        final BlockingQueueIterator bqi = new BlockingQueueIterator<File>(q, POISON);
        assertFalse(bqi.hasNext());
        // insure its still false.
        assertFalse(bqi.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        final BlockingQueue<File> q = new LinkedBlockingQueue<File>();
        q.add(POISON);
        final BlockingQueueIterator bqi = new BlockingQueueIterator<File>(q, POISON);
        bqi.remove();
    }

    @Test
    public void testHasNext() {
        final BlockingQueue<File> q = new LinkedBlockingQueue<File>();
        q.add(new File("foo"));
        q.add(POISON);
        final BlockingQueueIterator bqi = new BlockingQueueIterator<File>(q, POISON);
        assertEquals(2, q.size());

        for (int i = 0; i != q.size(); i++) {
            assertTrue("hasNext() invocation count: " + i+1, bqi.hasNext());
        }
    }

    @Test
    public void testNext() {
        final BlockingQueue<File> q = new LinkedBlockingQueue<File>();
        final File fooFile = new File("foo");
        q.add(fooFile);
        q.add(POISON);
        final BlockingQueueIterator<File> bqi = new BlockingQueueIterator<File>(q, POISON);
        assertEquals(2, q.size());

        // the poison file should never come off the queue
        assertEquals(fooFile, bqi.next());

        try {
            bqi.next();
            fail("A NoSuchElementException should have been thrown.");
            assertFalse(bqi.hasNext());
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testTimingPopulateQueueWithOnlyPoison() {
        final Object monitor = new Object();
        final BlockingQueue<File> q = new LinkedBlockingQueue<File>();
        final BlockingQueueIterator<File> itr = new BlockingQueueIterator<File>(q, POISON);
        final PopulateQueueRunnable runnableQueuePopulator = new PopulateQueueRunnable(q, monitor, POISON);
        final Thread t = new Thread(runnableQueuePopulator, "Queue Populator");
        t.start(); // this thread waits until we notify it
        synchronized (monitor) {
            runnableQueuePopulator.go = true;
            monitor.notify(); // after notification it sleeps to give itr.hasNext() a chance to execute.
        }
        assertFalse(itr.hasNext());
        try {
            itr.next();
            fail("Expected NoSuchElementException to be thrown");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testTimingPopulateQueue() {
        final Object monitor = new Object();
        final BlockingQueue<File> q = new LinkedBlockingQueue<File>();
        final BlockingQueueIterator<File> itr = new BlockingQueueIterator<File>(q, POISON);
        final File fooFile = new File("foo");
        final PopulateQueueRunnable runnableQueuePopulator = new PopulateQueueRunnable(q, monitor, fooFile, POISON);
        final Thread t = new Thread(runnableQueuePopulator, "Queue Populator");
        t.start(); // this thread waits until we notify it.
        synchronized (monitor) {
            runnableQueuePopulator.go = true;
            monitor.notify(); // after notification it sleeps to give itr.hasNext() a chance to execute.
        }
        assertTrue(itr.hasNext());
        assertEquals(fooFile, itr.next());
        assertFalse(itr.hasNext());
        try {
            itr.next();
            fail("Expected NoSuchElementException to be thrown.");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    private class PopulateQueueRunnable implements Runnable {
        private final BlockingQueue<File> queue;
        private final File[] toPopulate;
        private final Object monitor;

        private volatile boolean go = false;

        private PopulateQueueRunnable(BlockingQueue<File> queue, Object monitor, File... toPopulate) {
            this.queue = queue;
            this.toPopulate = toPopulate;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            try {
                // wait until notified (we want the consumer to start to try to retrieve something off the queue first
                synchronized (monitor) {
                    while (!go) {
                        monitor.wait();
                    }
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }

            for (int i = 0; i < toPopulate.length; i++) {
                queue.add(toPopulate[i]);
            }
        }
    }
}
