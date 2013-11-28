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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.dataconservancy.dcs.ingest.LockManager;
import org.dataconservancy.dcs.ingest.LockService;

public class InMemoryLockService
        implements LockService {

    final Map<String, LockManager> managers =
            new HashMap<String, LockManager>();

    final ConcurrentHashMap<Object, Object> lockValues =
            new ConcurrentHashMap<Object, Object>();

    @Override
    public LockManager getLockManager(String sipId) {

        if (!managers.containsKey(sipId)) {
            managers.put(sipId, new HashMapLockManager(sipId));
        }

        return managers.get(sipId);
    }

    @Override
    public boolean hasLockManager(String id) {
        return managers.containsKey(id);
    }

    private class HashMapLockManager
            implements LockManager {

        final List<Lock> managedLocks = new ArrayList<Lock>();

        final String sip;

        public HashMapLockManager(String sipId) {
            sip = sipId;
        }

        @Override
        public Lock obtainLock(Object key, Object value) {
            Lock lock = new HashMapLock(key, value);
            managedLocks.add(lock);
            return lock;
        }

        @Override
        public Object getValue(Object key) {
            return lockValues.get(key);
        }

        @Override
        public void releaseLocks() {
            for (Lock l : managedLocks) {
                l.unlock();
            }
        }

        public void close() {
            releaseLocks();
            managers.remove(sip);
        }
    }

    public class HashMapLock
            implements Lock {

        private Object key;

        private Object value;

        public HashMapLock(Object lkey, Object lvalue) {
            key = lkey;
            value = lvalue;
        }

        public void lock() {
            /*
             * For now, we don't wait for lock to be become available, and just
             * fail.
             */
            if (!tryLock()) {
                throw new RuntimeException("Could not obtain lock - already locked by someone else");
            }
        }

        public void lockInterruptibly() throws InterruptedException {
            tryLock();
        }

        public boolean tryLock() {

            Object inserted = lockValues.putIfAbsent(key, value);
            return (inserted == null);
        }

        public boolean tryLock(long time, TimeUnit unit)
                throws InterruptedException {
            /* Right now, we don't wait */
            return tryLock();
        }

        public void unlock() {
            lockValues.remove(key);
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

    }

}
