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

import java.util.Iterator;

import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.Reader;
import org.dataconservancy.transform.execution.BasicExecutionEnvironment;

public class Runner
        implements Runnable {

    private final Plan cfg;

    private final BasicExecutionEnvironment execution;

    public Runner(Plan cfg, BasicExecutionEnvironment exe) {
        this.cfg = cfg;
        execution = exe;
    }

    public <Ki, Vi, Ko, Vo> void _run() {
        Iterator<Job<Ki, Vi, Ko, Vo>> stages = cfg.getJobs();

        while (stages.hasNext()) {
            Job<Ki, Vi, Ko, Vo> stage = stages.next();

            Reader<Ki, Vi> reader = stage.getReader();
            Output<Ko, Vo> output = stage.getOutput();
            execution.execute(stage.getReader(),
                              stage.getMapping(),
                              stage.getOutput());
            /* TODO: look at ingest services for advice on what to do here */
            output.close();
            reader.close();
        }
    }

    public void run() {
        _run();
    }
}
