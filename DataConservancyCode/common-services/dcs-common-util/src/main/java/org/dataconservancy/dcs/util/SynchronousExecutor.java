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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Merely executes a task synchronously in the current thread.
 */
public class SynchronousExecutor
        extends AbstractExecutorService {

    public void execute(Runnable command) {
        command.run();
    }

    public void shutdown() {
        return;
    }

    public List<Runnable> shutdownNow() {
        return new ArrayList<Runnable>();
    }

    public boolean isShutdown() {
        return true;
    }

    public boolean isTerminated() {
        return true;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return true;
    }

}
