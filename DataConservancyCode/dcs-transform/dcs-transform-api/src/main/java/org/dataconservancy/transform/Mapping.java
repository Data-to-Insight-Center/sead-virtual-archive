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
 * Transforms key/value pairs into zero or more or more output pairs.
 * <p>
 * Consider this to be a subset of hadoop
 * {@link org.apache.hadoop.mapreduce.Mapper}. In its most basic sense,
 * instances of Mapper will take input data of one type/form/format and
 * transform it into another.
 * <p/>
 * <p>
 * {@link #map(Object, Object, Output)} operations must always be stateless. All
 * map operations are independent, and are a sole function of the pair of input
 * data. Mappers may use external resources (such as registries, shared disk,
 * etc) as long as they are considered immutable in the context of a map
 * operation.
 * </p>
 */
public interface Mapping<Ki, Vi, Ko, Vo> {

    /**
     * Perform a map operation on a single key/value pair.
     * 
     * @param key
     *        Input key
     * @param val
     *        Input value
     * @param writer
     *        Sink into which the mapping output is written.
     * @throws MappingException 
     */
    public void map(Ki key, Vi val, Output<Ko, Vo> output);
}
