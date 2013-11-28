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
package org.dataconservancy.transform.execution.run;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.Reader;
import org.dataconservancy.transform.execution.BasicExecutionEnvironment;
import org.dataconservancy.transform.execution.NoOpMapping;

import static org.junit.Assert.assertEquals;

public class RunnerTest {

    @Test
    public void testAllStagesRun() {
        final int COUNT = 50;

        MultiStagePlan plan = new MultiStagePlan(COUNT);

        assertEquals(0, plan.count);

        Runner runner = new Runner(plan, new BasicExecutionEnvironment());

        runner.run();

        assertEquals(COUNT, plan.count);
    }

    @SuppressWarnings("unchecked")
    private class MultiStagePlan
            implements Plan {

        final int stages;

        private int count = 0;

        public MultiStagePlan(int stages) {
            this.stages = stages;
        }

        public Iterator<Job<?, ?, ?, ?>> getJobs() {
            List<Job<?, ?, ?, ?>> jobs = new ArrayList<Job<?, ?, ?, ?>>();

            for (int i = 0; i < stages; i++) {
                jobs.add(getJob());
            }

            return jobs.iterator();
        }

        @Override
        public String getLabel() {
            return "label";
        }

        @Override
        public String getDescription() {
            return "description";
        }

        private Job<String, Integer, String, Integer> getJob() {
            Job<String, Integer, String, Integer> job =
                    new Job<String, Integer, String, Integer>(new CountReader(),
                                                              new NoOpMapping<String, Integer>(),
                                                              new CountOutput());
            return job;
        }

        private class CountReader
                implements Reader<String, Integer> {

            boolean hasRead = false;

            public String getCurrentKey() {
                return Integer.toString(count);
            }

            public Integer getCurrentValue() {
                return count;
            }

            public boolean nextKeyValue() {
                if (hasRead) {
                    return false;
                } else {
                    hasRead = true;
                    return true;
                }
            }

            public void close() {
                /* Do Nothing */
            }
        }

        private class CountOutput
                implements Output<String, Integer> {

            public void write(String key, Integer value) {
                count = value + 1;
            }

            public void close() {
                /* do nothing */
            }

        }
    }
}
