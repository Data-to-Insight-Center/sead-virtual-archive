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
package org.dataconservancy.transform.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;

import org.dataconservancy.dcs.util.BoundedBlockingPoolExecutor;
import org.dataconservancy.dcs.util.SynchronousExecutor;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directly-invoked execution environment for running transformations.
 * <p>
 * This is part of the default provided execution environment in the feature
 * extraction framework ({@link MappingChain} is the other part). It operates by
 * iterating through every result emitted by the provided {@link Reader}, and
 * invoking the provided {@link Mapping} into the given {@link Output} using an
 * {@link ExecutorService}.
 * </p>
 * <p>
 * By default, a simple ExecutorService runs each transform operation
 * sequentially in the current thread. Provided Executors are allowed to do
 * anything they are configured to do. A {@link BoundedBlockingPoolExecutor} may
 * be provided in order to run each mapping invocation in a parallel within a
 * fixed thread pool, for example.
 * </p>
 * <p>
 * Error handling is the responsibility of the caller.
 * {@link #execute(Reader, Mapping, Output)} will throw an
 * {@link ExecutionTaskException} if the execution of the given {@link Mapping}
 * fails for any reason. If this exception is caught and dealt with
 * appropriately, execution can continue. The {@link ExecutionTaskException}
 * provides means for running, re-submitting, or reporting on the failed task.
 * See also {@link #setDropFailedTasks(boolean)}
 * </p>
 */
public class BasicExecutionEnvironment {

    private static final Logger LOG = LoggerFactory
            .getLogger(BasicExecutionEnvironment.class);

    private ExecutorService executor = new SynchronousExecutor();

    boolean dropFailedTasks = false;

    /**
     * Replace the default ExecutorService with the provided.
     * 
     * @param ex
     *        Executor in which subsequent transformation/extractions will be
     *        performed.
     */
    public void setExecutorService(ExecutorService ex) {
        executor = ex;
    }

    /**
     * Determine if it is possible to skip failed tasks.
     * <p>
     * An {@link ExecutionTaskException} will be thrown by
     * {@link #execute(Reader, Mapping, Output)} if an individual task fails
     * abnormally. If dropFailedTasks is set to true, these exceptions can be
     * caught and discarded allowing the job to complete, albeit with missing
     * data. If it is set to false, #execute(Reader, Mapping, Output) will not
     * terminate until these tasks have been successfully completed. In that
     * case, the catcher of the exception must re-submit the task through either
     * {@link ExecutionTaskException#retry()} or manually running the task
     * obtained from {@link ExecutionTaskException#getTask()}.
     * <p>
     * 
     * @param drop
     *        Default is false;
     */
    public void setDropFailedTasks(boolean drop) {
        dropFailedTasks = drop;
    }

    /** Creates a single threaded execution environment */
    public BasicExecutionEnvironment() {

    }

    /**
     * Creates an execution environment using a provided ExecutorService.
     * <p>
     * Use this for running in more advanced modes (e.g. threaded). There are no
     * constraints on what the provided Executor is allowed to do.
     * </p>
     * 
     * @param ex
     *        ExecutorService in which each transformation/extraction will be
     *        performed.
     */
    public BasicExecutionEnvironment(ExecutorService ex) {
        executor = ex;
    }

    /**
     * Run a complete extraction/transform job.
     * 
     * @param reader
     *        Reader from which data units will be read until its exhaustion.
     * @param mapping
     *        Mapping that will be applied to all data units emitted by the
     *        given reader.
     * @param output
     *        Output into which all mapping results will be written.
     * @param exceptionListeners
     *        Optional array of exception handlers to be notified (and possibly
     *        take corrective action) when an exception occurs during execution
     */
    public <Ki, Vi, Ko, Vo> void execute(Reader<Ki, Vi> reader,
                                         Mapping<Ki, Vi, Ko, Vo> mapping,
                                         Output<Ko, Vo> output,
                                         ExceptionListener... exceptionListeners) {

        /* Currently executing jobs */
        Map<ExecutionJob<?, ?, ?, ?>, Future<?>> jobs =
                new HashMap<ExecutionJob<?, ?, ?, ?>, Future<?>>();

        /* Queue of finished jobs awaiting evaluation */
        BlockingQueue<ExecutionJob<?, ?, ?, ?>> finished =
                new LinkedBlockingQueue<ExecutionJob<?, ?, ?, ?>>();

        while (reader.nextKeyValue()) {

            ExecutionJob<Ki, Vi, Ko, Vo> job =
                    new ExecutionJob<Ki, Vi, Ko, Vo>(reader.getCurrentKey(),
                                                     reader.getCurrentValue(),
                                                     mapping,
                                                     output,
                                                     finished);

            /* See if any jobs have finished in the meantime */
            ExecutionJob<?, ?, ?, ?> finishedJob;
            LOG.debug("Seeing if jobs have finished.  Queue has '{}'",
                      finished.size());
            while ((finishedJob = finished.poll()) != null)
                finish(finishedJob, jobs, finished, exceptionListeners);

            /* Submit the job for execution. Execution might be asynchronous. */
            submitJob(job, jobs);
        }

        /*
         * While there are still known jobs that haven't finished, wait until
         * they are all done before exiting and cleaning up resources.
         */
        try {
            while (!jobs.isEmpty()) {
                LOG.debug("Going to take from queue to finish (size = '{}')",
                          finished.size());
                finish(finished.take(), jobs, finished, exceptionListeners);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            reader.close();
        }
    }

    /*
     * Submit job for possibly asynchronous execution, and add it to the list of
     * currently executing jobs.
     */
    private void submitJob(ExecutionJob<?, ?, ?, ?> job,
                           Map<ExecutionJob<?, ?, ?, ?>, Future<?>> jobs) {
        LOG.debug("Submitting job '{}'", job);
        synchronized (jobs) {
            Future<?> status = (executor.submit(job));
            jobs.put(job, status);
        }
    }

    /*
     * Perform accounting of finished tasks. If they terminated normally, remove
     * them from the list of currently executing jobs. If they finished with an
     * exception, notify exception listeners (or just throw an
     * ExectionTaskException if there are none), and let the caller deal with
     * the consequence.
     */
    private void finish(ExecutionJob<?, ?, ?, ?> job,
                        Map<ExecutionJob<?, ?, ?, ?>, Future<?>> jobs,
                        Queue<?> finished,
                        ExceptionListener... listeners) {

        if (job == null || !jobs.containsKey(job)) return;

        boolean failed = false;

        LOG.debug("Finishing: job: '{}', finished job count: '{}'",
                  job,
                  finished.size());

        Future<?> status = null;
        synchronized (jobs) {
            status = jobs.get(job);
        }

        try {
            LOG.debug("Finishing: Getting status of '{}'", job);
            status.get();
            LOG.debug("Finishing: Normal execution.  Removing job '{}'", job);
            jobs.remove(job);
        } catch (ExecutionException e) {
            failed = true;
            LOG.debug("Finishing: Exception in job '{}', '{}'", job, e);

            FutureTask<Object> future = new FutureTask<Object>(job, null);
            jobs.put(job, future);

            ExecutionJobException eje =
                    new ExecutionJobException(future, job, jobs, e.getCause());

            if (listeners.length > 0) {
                LOG.debug("Finishing: exception execution : Notifying listeners");
                for (ExceptionListener l : listeners) {
                    LOG.debug("Finishing: Notifying listener '{}'", l);
                    l.exceptionThrown(eje);
                }
            } else {
                throw eje;
            }
        } catch (Exception e) {
            failed = true;
            throw new ExecutionJobException(null, job, jobs, e);
        } finally {
            if (dropFailedTasks && failed) {
                LOG.info("Finishing: dropping failed task '{}'", job);
                jobs.remove(job);
            }
        }

        LOG.debug("Exit finishing, jobs = '{}'", jobs.size());
    }

    private class ExecutionJobException
            extends ExecutionTaskException {

        private static final long serialVersionUID = 1L;

        private final ExecutionJob<?, ?, ?, ?> job;

        private final Map<ExecutionJob<?, ?, ?, ?>, Future<?>> jobs;

        ExecutionJobException(RunnableFuture<?> taskFuture,
                              ExecutionJob<?, ?, ?, ?> task,
                              Map<ExecutionJob<?, ?, ?, ?>, Future<?>> tasks,
                              Throwable exception) {
            super(taskFuture, exception);

            job = task;
            jobs = tasks;
        }

        public void retry() {
            LOG.debug("Re-submitting job ", job);
            submitJob(job, jobs);
        }

        public Object getKey() {
            return job.key;
        }

        public Object getValue() {
            return job.val;
        }
    }

    private static class ExecutionJob<Ki, Vi, Ko, Vo>
            implements Runnable {

        final Ki key;

        final Vi val;

        final Mapping<Ki, Vi, Ko, Vo> mapping;

        final Output<Ko, Vo> output;

        final BlockingQueue<ExecutionJob<?, ?, ?, ?>> queue;

        public ExecutionJob(Ki k,
                            Vi v,
                            Mapping<Ki, Vi, Ko, Vo> m,
                            Output<Ko, Vo> o,
                            BlockingQueue<ExecutionJob<?, ?, ?, ?>> q) {

            key = k;
            val = v;
            mapping = m;
            output = o;
            queue = q;
        }

        public void run() {
            try {
                mapping.map(key, val, output);
            } finally {
                try {
                    LOG.debug("Adding job to finished queue '{}'", this);
                    queue.put(this);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
