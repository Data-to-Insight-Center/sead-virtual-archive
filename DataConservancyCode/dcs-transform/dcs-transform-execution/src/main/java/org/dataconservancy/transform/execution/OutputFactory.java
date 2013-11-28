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

import org.dataconservancy.transform.Output;

/**
 * Creates Output instances.
 * <p>
 * An Output instance could have its own "transactional" context, as could a
 * series of related Output instances participating in the same job. Thus,
 * {@linkplain OutputFactory} produces {@link Output} instances that all exist
 * within the appropriate context;
 * </p>
 */
public interface OutputFactory<Ko, Vo> {

    /**
     * Get an Output instance.
     * 
     * @return {@link Output} instance in the proper context.
     */
    public Output<Ko, Vo> newOutput();
    
    /** Get a new instance of an output factory. */
    public OutputFactory<Ko, Vo> newInstance();

    /**
     * Close this factory instance, and perform and necessary finalization.
     * <p>
     * An optional boolean "success" parameter may be provided in order to
     * indicate success or failure in processing. A <em>true</em> value
     * indicates that processing has been successful, and all written values
     * should be processed/committed accordingly. A <em>false</em> value
     * indicates that processing has failed or is incomplete, and that
     * previously written values can be ignored. The actual behaviour (if any)
     * in response to a given success value is implementation-dependent.
     * </p>
     * 
     * @param successful
     *        Optional boolean argument for indicating success. No value implies
     *        <em>true</em>. If multiple values are given, only the first will
     *        be used.
     */
    public void close(boolean... success);
}
