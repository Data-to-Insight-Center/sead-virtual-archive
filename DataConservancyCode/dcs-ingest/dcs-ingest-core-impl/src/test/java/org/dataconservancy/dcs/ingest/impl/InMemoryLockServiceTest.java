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

package org.dataconservancy.dcs.ingest.impl;

import java.util.concurrent.locks.Lock;

import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.LockManager;
import org.dataconservancy.dcs.ingest.LockService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InMemoryLockServiceTest {

    final String ID = "the_id";

    final String KEY = "the_key";

    final String VALUE = "the_value";

    @Test
    public void hasLockManagerTest() {
        InMemoryLockService ls = new InMemoryLockService();

        assertFalse(ls.hasLockManager("whatever"));

        LockManager lm = ls.getLockManager(ID);
        assertNotNull(lm);

        assertTrue(ls.hasLockManager(ID));

    }

    @Test
    public void hasClosedLockManagerTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);

        assertTrue(ls.hasLockManager(ID));
        lm.close();
        assertFalse(ls.hasLockManager(ID));
    }

    @Test
    public void simpleUncontestedObtainLockTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);

        Lock lock = lm.obtainLock(KEY, VALUE);
        assertTrue(lock.tryLock());
    }

    @Test
    public void simpleUncontestedMultiLockTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);

        Lock lock = lm.obtainLock(KEY, VALUE);
        assertTrue(lock.tryLock());

        Lock lock2 = lm.obtainLock("another_key", VALUE);
        assertTrue(lock2.tryLock());

    }

    @Test
    public void simpleContestedObtainLockTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);

        Lock lock = lm.obtainLock(KEY, VALUE);
        assertTrue(lock.tryLock());

        assertFalse(lm.obtainLock(KEY, VALUE).tryLock());
        assertFalse(lm.obtainLock(KEY, "anotherValue").tryLock());

        lock.unlock();

        assertTrue(lm.obtainLock(KEY, VALUE).tryLock());
    }

    @Test
    public void crossManagerLockTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);
        LockManager lm2 = ls.getLockManager("another_id");

        Lock lock = lm.obtainLock(KEY, VALUE);
        assertTrue(lock.tryLock());

        assertFalse(lm2.obtainLock(KEY, VALUE).tryLock());
        assertFalse(lm2.obtainLock(KEY, "anotherValue").tryLock());

        lock.unlock();

        assertTrue(lm2.obtainLock(KEY, VALUE).tryLock());
    }

    @Test
    public void getValueTest() {
        InMemoryLockService ls = new InMemoryLockService();
        LockManager lm = ls.getLockManager(ID);

        assertNull(lm.getValue(KEY));

        Lock lock = lm.obtainLock(KEY, VALUE);

        assertNull(lm.getValue(KEY));

        lock.lock();

        assertEquals(VALUE, lm.getValue(KEY));

        lock.unlock();

        assertNull(lm.getValue(KEY));
    }

    @Test
    public void unlockTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);
        Lock lock = lm.obtainLock(KEY, VALUE);
        lock.lock();
        assertTrue(ls.lockValues.containsKey(KEY));

        assertFalse(lm.obtainLock(KEY, VALUE).tryLock());

        lock.unlock();
        assertFalse(ls.lockValues.containsKey(KEY));
        assertTrue(lm.obtainLock(KEY, VALUE).tryLock());
    }

    @Test
    public void releaseLocksTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);

        Lock lock = lm.obtainLock(KEY, VALUE);
        Lock lock2 = lm.obtainLock("another_key", "another_value");
        assertTrue(lock.tryLock());
        assertTrue(lock2.tryLock());
        assertTrue(ls.lockValues.containsKey(KEY));
        assertTrue(ls.lockValues.containsKey("another_key"));

        assertFalse(lm.obtainLock(KEY, VALUE).tryLock());
        assertFalse(lm.obtainLock("another_key", "anotherValue").tryLock());

        lm.releaseLocks();

        assertNull(lm.getValue(KEY));
        assertNull(lm.getValue("another_key"));
        assertFalse(ls.lockValues.contains(KEY));
        assertFalse(ls.lockValues.contains("another_key"));

        assertTrue(lm.obtainLock(KEY, VALUE).tryLock());
        assertTrue(lm.obtainLock("another_key", "anotherValue").tryLock());
    }

    @Test
    public void closeManagerTest() {
        InMemoryLockService ls = new InMemoryLockService();

        LockManager lm = ls.getLockManager(ID);
        LockManager lm2 = ls.getLockManager(ID);

        assertTrue(ls.managers.containsKey(ID));
        assertTrue(ls.managers.containsValue(lm));
        assertTrue(ls.managers.containsValue(lm2));
        assertTrue(ls.hasLockManager(ID));

        Lock lock = lm.obtainLock(KEY, VALUE);
        Lock lock2 = lm.obtainLock("another_key", "another_value");
        assertTrue(lock.tryLock());
        assertTrue(lock2.tryLock());
        assertTrue(ls.lockValues.containsKey(KEY));

        assertFalse(lm.obtainLock(KEY, VALUE).tryLock());
        assertFalse(lm.obtainLock("another_key", "anotherValue").tryLock());

        lm.close();

        assertNull(lm.getValue(KEY));
        assertNull(lm.getValue("another_key"));

        assertFalse(ls.managers.containsKey(ID));
        assertFalse(ls.managers.containsValue(lm));
        assertFalse(ls.managers.containsValue(lm2));
        assertFalse(ls.lockValues.contains(KEY));
        assertFalse(ls.hasLockManager(ID));
    }
}
