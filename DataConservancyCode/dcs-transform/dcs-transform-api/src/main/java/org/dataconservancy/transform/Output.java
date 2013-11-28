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
package org.dataconservancy.transform;

/**
 * Collects output from a Mapping or Reduction.
 * <p>
 * Consider this to be a subset of hadoop 0.21
 * {@link org.apache.hadoop.mapreduce.TaskInputOutputContext} and/or
 * {@link org.apache.hadoop.mapreduce.RecordWriter}. Instances of Output are
 * used as sinks to Mapping or Reduction tasks, and may (for example) further
 * transform the data, pipe it to another process, deposit it in a repository,
 * etc.
 * </p>
 * <p>
 * Output instances must be idempotent with respect to submitted key-value pair
 * </p>
 */
public interface Output<K, V> {

    /** Write a key/value pair to the output. */
    public void write(K key, V value);

    /** Close and free up any resources */
    public void close();
}
