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

package org.dataconservancy.dcs.ingest;

import java.util.concurrent.locks.Lock;

/**
 * Provides simple key/value based locking for ingest services.
 * <p>
 * A LockManager is a factory for {@link Lock} instances which may be used to
 * form critical sections within an ingest pipeline. Locks are associated with a
 * given key and value. A given key can be locked exactly once until the lock is
 * relinquished. Concurrent invocations of lock on the same key will fail until
 * {@link Lock#unlock() is invoked}.
 * </p>
 * <p>
 * <h2>Typical Usage
 * </p>
 * <code>
 * IngestFramework framework;
 * 
 * // Get the lock service from the ingest framework
 * LockService lockService = framework.getLockService();
 * 
 * // Use the convention that a LockManager is named by the Sip
 * LockManager manager = lockService.getLockManager(sipId);
 * 
 * // This obtains a Lock instance that can be locked.  Choice of key
 * // and value is arbitrary, and depends on what is being locked 
 * // (e.g. lineageID, successor, etc)
 * Lock lock = manager.obtainLock(key, value);
 * 
 * //Lock on the given key
 * lock.lock();
 * </code>
 * </p>
 * 
 * Locks define a critical section in the code.  If this critical section terminates within
 * the same ingest stage, the lock can be released via {@link Lock#unlock()}.  Otherwise,
 * all locks will be released upon ingest cleanup after success or failure.
 */
public interface LockManager {

    /**
     * Obtain a lock given a key, value
     * 
     * @param key
     *        Primary identifier/object which differentiates locks from one
     *        another.
     * @param value
     *        Proposed value to be associated with a lock
     * @return
     */
    public Lock obtainLock(Object key, Object value);

    /** Obtain any present value associated with the given key */
    public Object getValue(Object key);

    /** Release any locks associated with the current lock manager. */
    public void releaseLocks();

    /** Release any locks and delete the lock manager. */
    public void close();
}
