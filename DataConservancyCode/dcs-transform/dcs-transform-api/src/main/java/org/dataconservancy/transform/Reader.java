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
 * Breaks data into individual units or 'records'.
 * <p>
 * Consider this to be a subset of hadoop 0.21
 * {@link org.apache.hadoop.mapreduce.RecordReader}. Basically, this takes some
 * source of input data (files, database, DCS repository, search query) and
 * represents it in a key-value form. The key or value types may be arbitrary
 * complex, depending on the application.
 * </p>
 */
public interface Reader<K, V> {

    public K getCurrentKey();

    public V getCurrentValue();

    public boolean nextKeyValue();

    public void close();
}
