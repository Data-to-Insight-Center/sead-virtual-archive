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

/** Simply directs the input to the output */
public class NoOpMapping<K, V>
        implements Mapping<K, V, K, V> {

    /** Write the given key and value to the output */
    public void map(K key, V val, Output<K, V> output) {
        output.write(key, val);
    }

}
