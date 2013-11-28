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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.dcs.util.BoundedBlockingPoolExecutor;
import org.dataconservancy.dcs.util.SynchronousExecutor;
import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@SuppressWarnings("unchecked")
public class BasicExecutionEnvironmentTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(BasicExecutionEnvironmentTest.class);

    ExecutorService executor = Executors.newCachedThreadPool();

    /* Assures that the default executor is functional. */
    @Test
    public void defaultExecutorTest() {
        BasicExecutionEnvironment env = new BasicExecutionEnvironment();

        final String VALUE = "X";

        OutputProbe out = new OutputProbe();
        ControlledQueueMapping mapping = new ControlledQueueMapping();
        LinkedBlockingQueue<String> content = new LinkedBlockingQueue<String>();
        content.add(VALUE);
        QueueStringReader reader = new QueueStringReader(content);

        env.execute(reader, mapping, out);

        assertEquals(1, out.keys.size());
        assertEquals(VALUE, out.keys.get(0));
    }

    @Test
    public void givenExecutorConstructurTest() {
        ExecutorServiceProbe svc = new ExecutorServiceProbe();
        BasicExecutionEnvironment exe = new BasicExecutionEnvironment(svc);

        Reader<String, String> reader = new SimpleStringReader("test");
        Output<String, String> output = new OutputProbe();
        Mapping<String, String, String, String> mapping =
                new ControlledStringMapping();

        exe.execute(reader, mapping, output);

        assertTrue(svc.hasExecuted);
    }

    @Test
    public void givenExecutorParameterTest() {
        ExecutorServiceProbe svc = new ExecutorServiceProbe();
        BasicExecutionEnvironment exe = new BasicExecutionEnvironment();
        exe.setExecutorService(svc);

        Reader<String, String> reader = new SimpleStringReader("test");
        Output<String, String> output = new OutputProbe();
        Mapping<String, String, String, String> mapping =
                new ControlledStringMapping();

        exe.execute(reader, mapping, output);

        assertTrue(svc.hasExecuted);
    }

    @Test
    public void parallelTest() throws Exception {

        final BasicExecutionEnvironment env = new BasicExecutionEnvironment();
        env.setExecutorService(BoundedBlockingPoolExecutor.getInstance(2));

        final String VALUE = "X";

        final OutputProbe out = new OutputProbe();
        final ControlledQueueMapping mapping = new ControlledQueueMapping();
        LinkedBlockingQueue<String> content1 =
                new LinkedBlockingQueue<String>();
        LinkedBlockingQueue<String> content2 =
                new LinkedBlockingQueue<String>();
        final QueueStringReader reader =
                new QueueStringReader(content1, content2);

        Future<?> job = executor.submit(new Runnable() {

            public void run() {
                env.execute(reader, mapping, out);
            }
        });

        assertEquals(0, out.keys.size());

        content1.put(VALUE);
        content2.put(VALUE);

        job.get();

        assertEquals(2, out.keys.size());
        assertEquals(VALUE, out.keys.iterator().next());
    }

    @Test
    public void synchronousExceptionWithoutListenerTest() {

        BasicExecutionEnvironment env = new BasicExecutionEnvironment();

        final String VALUE = "X";

        OutputProbe out = new OutputProbe();
        ControlledQueueMapping mapping =
                new ControlledQueueMapping().doException();
        LinkedBlockingQueue<String> content = new LinkedBlockingQueue<String>();
        content.add(VALUE);
        QueueStringReader reader = new QueueStringReader(content);

        try {
            env.execute(reader, mapping, out);
            fail("Exception was not thrown");
        } catch (ExecutionTaskException e) {
            assertTrue(e.getCause() instanceof ControlledException);
        }
    }

    @Test
    public void parallelExceptionWithoutListenerTest() {

        BasicExecutionEnvironment env = new BasicExecutionEnvironment();
        env.setExecutorService(BoundedBlockingPoolExecutor.getInstance(2));

        final String VALUE = "X";

        OutputProbe out = new OutputProbe();
        ControlledQueueMapping mapping =
                new ControlledQueueMapping().doException();
        LinkedBlockingQueue<String> content = new LinkedBlockingQueue<String>();
        content.add(VALUE);
        QueueStringReader reader = new QueueStringReader(content);

        try {
            env.execute(reader, mapping, out);
            fail("Exception was not thrown");
        } catch (ExecutionTaskException e) {
            assertTrue(e.getCause() instanceof ControlledException);
        }
    }

    @Test
    public void exceptionEatingTest() {
        BasicExecutionEnvironment env = new BasicExecutionEnvironment();
        env.setDropFailedTasks(true);

        final String VALUE = "X";

        OutputProbe out = new OutputProbe();
        ControlledQueueMapping mapping =
                new ControlledQueueMapping().doException();
        LinkedBlockingQueue<String> content = new LinkedBlockingQueue<String>();
        content.add(VALUE);
        QueueStringReader reader = new QueueStringReader(content);

        final Set<Exception> exceptions = new HashSet<Exception>();

        env.execute(reader, mapping, out, new ExceptionListener() {

            public void exceptionThrown(ExecutionTaskException e) {
                exceptions.add(e);
            }
        });

        assertEquals(0, out.keys.size());
        assertTrue(exceptions.size() > 0);
    }

    @Test
    public void synchronousExceptionRetryTest() throws Exception {
        doRetryTest(new BasicExecutionEnvironment());
    }

    @Test
    public void parallelExceptionRetryTest() throws Exception {
        BasicExecutionEnvironment env = new BasicExecutionEnvironment();
        env.setExecutorService(BoundedBlockingPoolExecutor.getInstance(2));
        doRetryTest(env);
    }

    private void doRetryTest(final BasicExecutionEnvironment env)
            throws Exception {
        env.setDropFailedTasks(false);

        final String VALUE = "X";

        final OutputProbe out = new OutputProbe();
        final ControlledQueueMapping mapping =
                new ControlledQueueMapping().doException(true);
        LinkedBlockingQueue<String> content1 =
                new LinkedBlockingQueue<String>();
        LinkedBlockingQueue<String> content2 =
                new LinkedBlockingQueue<String>();
        content1.add(VALUE);
        content2.add(VALUE);
        final QueueStringReader reader =
                new QueueStringReader(content1, content2);

        final BlockingQueue<ExecutionTaskException> exceptions =
                new LinkedBlockingQueue<ExecutionTaskException>();

        Future<?> job = executor.submit(new Runnable() {

            public void run() {
                env.execute(reader, mapping, out, new ExceptionListener() {

                    public void exceptionThrown(ExecutionTaskException e) {
                        exceptions.add(e);
                    }
                });
            }
        });

        /* Wait for both jobs to reach the exception throwing stage */
        Set<BlockingQueue<String>> encountered =
                new HashSet<BlockingQueue<String>>();

        while (encountered.size() < 2) {
            encountered.add(mapping.exceptionProc.take());
        }

        /* Wait for the exceptions to be thrown before continuing */
        while (exceptions.size() < 2) {
            Thread.sleep(10);
        }

        assertEquals(0, out.keys.size());
        assertFalse(job.isDone());

        /* Assure that the next round will not throw an exception */
        mapping.doException(false);

        /*
         * Wait for the exception listener to be notified, then retry both jobs.
         */
        while (exceptions.size() > 0) {
            exceptions.take().retry();
        }

        /* Wait for jobs to finish */
        job.get();

        /* Make sure we have all results */
        assertEquals(2, out.keys.size());
    }

    @Test
    public void synchronousManualRetryTest() {
        doManualRetryTest(new BasicExecutionEnvironment());
    }

    @Test
    public void parallelManualRetryTest() throws Exception {
        BasicExecutionEnvironment env = new BasicExecutionEnvironment();
        env.setExecutorService(BoundedBlockingPoolExecutor.getInstance(2));
        doManualRetryTest(env);
    }

    private void doManualRetryTest(final BasicExecutionEnvironment env) {
        env.setDropFailedTasks(false);

        final String VALUE = "X";

        OutputProbe out = new OutputProbe();
        final ControlledQueueMapping mapping =
                new ControlledQueueMapping().doException();
        final LinkedBlockingQueue<String> content1 =
                new LinkedBlockingQueue<String>();
        final LinkedBlockingQueue<String> content2 =
                new LinkedBlockingQueue<String>();
        content1.add(VALUE);
        content2.add(VALUE);
        QueueStringReader reader = new QueueStringReader(content1, content2);

        final Set<Exception> exceptions = new HashSet<Exception>();

        env.execute(reader, mapping, out, new ExceptionListener() {

            public void exceptionThrown(ExecutionTaskException e) {
                exceptions.add(e);
                mapping.doException(false);
                e.getTask().run();
            }
        });

        assertEquals(2, out.keys.size());
        assertTrue(exceptions.size() > 0);
    }

    @Test
    public void randomSynchronousRetryTest() {
        int tries = 10000;
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        List<String> result =
                doRandomException(new BasicExecutionEnvironment(),
                                  tries,
                                  new ExceptionListener() {

                                      public void exceptionThrown(ExecutionTaskException e) {
                                          exceptionCount.incrementAndGet();
                                          e.retry();
                                      }
                                  });
        LOG.info("Tries: '{}', Exceptions: '{}'", tries, exceptionCount);
        verifyRandomExecutionResult(tries, result);
    }

    @Test
    public void randomParallelRetryTest() {
        int tries = 10000;
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        BasicExecutionEnvironment env =
                new BasicExecutionEnvironment(BoundedBlockingPoolExecutor.getInstance(8));
        List<String> result =
                doRandomException(env, tries, new ExceptionListener() {

                    public void exceptionThrown(ExecutionTaskException e) {
                        exceptionCount.incrementAndGet();
                        e.retry();
                    }
                });
        LOG.info("Tries: '{}', Exceptions: '{}'", tries, exceptionCount);
        verifyRandomExecutionResult(tries, result);
    }

    @Test
    public void randomSynchronousManualRetryTest() {
        int tries = 10000;
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        List<String> result =
                doRandomException(new BasicExecutionEnvironment(),
                                  tries,
                                  new ExceptionListener() {

                                      public void exceptionThrown(ExecutionTaskException e) {
                                          exceptionCount.incrementAndGet();
                                          e.getTask().run();
                                      }
                                  });
        
        LOG.info("Tries: '{}', Exceptions: '{}'", tries, exceptionCount);
        verifyRandomExecutionResult(tries, result);
    }

    @Test
    public void randomParallelManualRetryTest() {
        int tries = 10000;
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        BasicExecutionEnvironment env =
                new BasicExecutionEnvironment(BoundedBlockingPoolExecutor.getInstance(8));
        List<String> result =
                doRandomException(env, tries, new ExceptionListener() {

                    public void exceptionThrown(ExecutionTaskException e) {
                        exceptionCount.incrementAndGet();
                        e.getTask().run();
                    }
                });
        LOG.info("Tries: '{}', Exceptions: '{}'", tries, exceptionCount);

        verifyRandomExecutionResult(tries, result);
    }

    private void verifyRandomExecutionResult(int expectedSize,
                                             List<String> results) {
        for (int i = 0; i < results.size(); i++) {
            if (!results.contains(Integer.toString(i))) {
                fail(i + " was not found in result list! (size = "
                        + new HashSet<String>(results).size() + ")");
            }
        }

        assertEquals(expectedSize, results.size());
    }

    private List<String> doRandomException(BasicExecutionEnvironment env,
                                           int tries,
                                           ExceptionListener listener) {

        env.setDropFailedTasks(false);

        final int exceptionPercent = 80;

        final OutputProbe out = new OutputProbe();
        final ControlledStringMapping mapping = new ControlledStringMapping();

        String[] strings = new String[tries];

        for (int i = 0; i < tries; i++) {
            strings[i] = Integer.toString(i);
        }

        final SimpleStringReader reader = new SimpleStringReader(strings);

        /*
         * Start a thread which will untimately cause exceptions to randomly be
         * thrown as mappings are executed;
         */
        Future<?> randomizer = executor.submit(new Runnable() {

            public void run() {
                for (;;) {
                    double random = ((Math.random() * 100));

                    if (random <= exceptionPercent) {
                        mapping.doException(true);
                    } else {
                        mapping.doException(false);
                    }
                }
            }
        });

        env.execute(reader, mapping, out, listener);

        randomizer.cancel(true);

        return out.keys;
    }

    /*
     * Do an infinite transform with many exceptions along the way. WARNING:
     * this will never end, so it is @Ignored. This is intended to be run
     * manually with a profiler or other monitor to verify that there are no
     * memory leaks or other improperly managed resources from
     * BasicEcecutionEnvironment itself.
     */
    @Test
    @Ignore
    public void memoryLeakTest() {
        final AtomicInteger i = new AtomicInteger(0);
        BasicExecutionEnvironment env =
                new BasicExecutionEnvironment(BoundedBlockingPoolExecutor.getInstance(8));
        doMemoryLeak(env, new ExceptionListener() {

            public void exceptionThrown(ExecutionTaskException e) {
                i.incrementAndGet();
                e.retry();
            }
        });
    }

    private void doMemoryLeak(BasicExecutionEnvironment env,
                              ExceptionListener listener) {

        env.setDropFailedTasks(false);

        final int exceptionPercent = 80;

        final Output<String, String> out = new InfiniteOutput();
        final ControlledStringMapping mapping = new ControlledStringMapping();
        final Reader<String, String> reader = new InfiniteReader();

        /*
         * Start a thread which will untimately cause exceptions to randomly be
         * thrown as mappings are executed;
         */
        Future<?> randomizer = executor.submit(new Runnable() {

            public void run() {
                for (;;) {
                    double random = ((Math.random() * 100));

                    if (random <= exceptionPercent) {
                        mapping.doException(true);
                    } else {
                        mapping.doException(false);
                    }
                }
            }
        });

        env.execute(reader, mapping, out, listener);

        randomizer.cancel(true);
    }

    private static class SimpleStringReader
            implements Reader<String, String> {

        private int index = -1;

        public String[] content;

        private SimpleStringReader(String... strings) {
            content = strings;
        }

        public String getCurrentKey() {
            return content[index];
        }

        public String getCurrentValue() {
            return content[index];
        }

        public boolean nextKeyValue() {
            if (++index == content.length) {
                return false;
            } else {
                return true;
            }
        }

        public void close() {
        }

    }

    private static class QueueStringReader
            implements Reader<String, BlockingQueue<String>> {

        private ArrayList<BlockingQueue<String>> content =
                new ArrayList<BlockingQueue<String>>();

        private int index = -1;

        public QueueStringReader(BlockingQueue<String>... given) {
            for (BlockingQueue<String> bq : given) {
                content.add(bq);
            }
        }

        public String getCurrentKey() {
            return content.get(index).peek();
        }

        public BlockingQueue<String> getCurrentValue() {
            return content.get(index);
        }

        public boolean nextKeyValue() {
            if (++index == content.size()) {
                return false;
            } else {
                return true;
            }
        }

        public void close() {
        }
    }

    private static class ControlledStringMapping
            implements Mapping<String, String, String, String> {

        boolean doException = false;

        public ControlledStringMapping doException(boolean val) {
            doException = val;
            return this;
        }

        public void map(String key, String val, Output<String, String> writer) {

            if (doException) {
                throw new ControlledException();
            } else {
                writer.write(key, val);
            }
        }

    }

    private static class ControlledQueueMapping
            implements Mapping<String, BlockingQueue<String>, String, String> {

        boolean doException = false;

        public BlockingQueue<BlockingQueue<String>> exceptionProc =
                new LinkedBlockingQueue<BlockingQueue<String>>();

        public ControlledQueueMapping doException() {
            doException = true;
            return this;
        }

        public ControlledQueueMapping doException(boolean val) {
            doException = val;
            return this;
        }

        public void map(String key,
                        BlockingQueue<String> val,
                        Output<String, String> writer) {

            try {

                if (doException) {
                    ControlledException e = new ControlledException();
                    exceptionProc.add(val);
                    String content = val.take();
                    val.put(content);
                    throw e;
                } else {
                    String content = val.take();
                    writer.write(content, content);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ControlledException
            extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ControlledException() {
        }
    }

    private static class OutputProbe
            implements Output<String, String> {

        public List<String> keys = Collections
                .synchronizedList(new ArrayList<String>());

        public List<String> values = Collections
                .synchronizedList(new ArrayList<String>());

        public void write(String key, String value) {
            if (key == null || value == null) {
                keys.size();
            }
            keys.add(key);
            values.add(value);
        }

        public void close() {
            // Do nothing
        }
    }

    private static class ExecutorServiceProbe
            extends SynchronousExecutor {

        public boolean hasExecuted = false;

        public void execute(Runnable r) {
            hasExecuted = true;
            super.execute(r);
        }

    }

    private class InfiniteReader
            implements Reader<String, String> {

        public String getCurrentKey() {
            return "X";
        }

        public String getCurrentValue() {
            return "X";
        }

        public boolean nextKeyValue() {
            return true;
        }

        public void close() {

        }
    }

    private class InfiniteOutput
            implements Output<String, String> {

        public void write(String key, String value) {
            // Do nothing
        }

        public void close() {
            // Do nothing
        }

    }
}
