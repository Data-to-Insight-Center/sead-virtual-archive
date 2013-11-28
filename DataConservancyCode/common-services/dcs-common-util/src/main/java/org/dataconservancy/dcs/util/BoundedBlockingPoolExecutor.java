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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fixed-size thread pool executor that blocks until worker threads are
 * available.
 * <p>
 * This class is useful for processing arbitrarily large numbers of
 * <code>Runnable</code> tasks with a limited pool of worker threads. Upon
 * <code>execute()</code>, this class will assign the task to a worker thread if
 * one is available, or block until one is. This is useful in situations where
 * tasks are being created as continuous stream. The default Executor classes
 * available through <code>java.util.concurrent</code> do not block upon
 * <code>execute()</code> when faced with limited resources, and thus would
 * require either an arbitrarily large queue, or an unbounded number of worker
 * threads to complete the same tasks.
 * </p>
 */
public class BoundedBlockingPoolExecutor
        extends ThreadPoolExecutor {

    private static final Logger logger =
            LoggerFactory.getLogger(BoundedBlockingPoolExecutor.class);

    private final int maxThreads;

    private final Semaphore gate;

    private BoundedBlockingPoolExecutor(int corePoolSize,
                                        int maxSize,
                                        long keepAlive,
                                        TimeUnit unit,
                                        BlockingQueue<Runnable> queue,
                                        int poolSize) {
        super(corePoolSize, maxSize, keepAlive, unit, queue);
        this.maxThreads = poolSize;
        this.gate = new Semaphore(maxThreads);
    }

    public static BoundedBlockingPoolExecutor getInstance(int poolSize) {
        return new BoundedBlockingPoolExecutor(0,
                                               Integer.MAX_VALUE,
                                               60L,
                                               TimeUnit.SECONDS,
                                               new SynchronousQueue<Runnable>(),
                                               poolSize);
    }

    /**
     * Assign the current task to a worker thread, blocking if one is not
     * available.
     * 
     * @param command
     *        Task to execute in thread pool.
     */
    @Override
    public final void execute(Runnable command) {

        /*
         * We don't associate the acquire() with the try/finally block that
         * release()s the semaphore, since releasing an unacquired semaphore
         * will merely increment the count, thus unintentionally growing the
         * pool.
         */
        try {
            gate.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Execution interrupted", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing thread. Now available: "
                    + gate.availablePermits() + " of " + maxThreads);
        }
        super.execute(command);
    }

    @Override
    protected final void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        gate.release();
    }
}
