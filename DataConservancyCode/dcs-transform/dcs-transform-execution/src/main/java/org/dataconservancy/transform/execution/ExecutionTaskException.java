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

import org.dataconservancy.transform.Mapping;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.Reader;

/**
 * Thrown when a transform job fails within an ExecutionEnvironment.
 * <p>
 * Allows information to be gathered and decisions to be made about the fate of
 * the failed task when caught. The original key and value can be accessed, and
 * the task itself can be re-submitted, if appropriate.
 * </p>
 */
public abstract class ExecutionTaskException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Runnable task;

    ExecutionTaskException(Runnable task, Throwable exception) {
        super(exception);
        this.task = task;
    }

    /**
     * Get an instance of the task that failed.
     * <p>
     * If not null, the Runnable task represents a single transformation on one
     * data unit emitted by some Reader. Running this task effectively equates
     * to "try again", though it will be executed wherever it is called, rather
     * than in its native execution environment.
     * </p>
     * 
     * @return Runnable for the failed task, or null if unavailable;
     */
    public Runnable getTask() {
        return task;
    }

    /**
     * Re-run the task in its native execution environment.
     * <p>
     * If this task fails again, a new ExecutionTaskException will be thrown by
     * {@link BasicExecutionEnvironment#execute(Reader, Mapping, Output)}.
     * However, the {@link #getTask()} will return the same object instance
     * every time, as the tasks are immutable. This could be used for
     * determining if a task continually fails.
     * </p>
     */
    public abstract void retry();

    /**
     * Return the key associated with this execution task.
     * 
     * @return The key originally returned by {@link Reader#getCurrentKey()} for
     *         this task.
     */
    public abstract Object getKey();

    /**
     * Return the value associated with this execution task.
     * 
     * @return The value originially returned by
     *         {@link Reader#getCurrentValue()} for this task.
     */
    public abstract Object getValue();

}
