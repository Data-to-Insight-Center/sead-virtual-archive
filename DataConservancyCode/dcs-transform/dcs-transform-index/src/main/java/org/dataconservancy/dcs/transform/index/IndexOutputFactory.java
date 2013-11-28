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
package org.dataconservancy.dcs.transform.index;

import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.execution.OutputFactory;

/**
 * Wraps an {@link IndexService} to produce independent Output writers.
 * <p>
 * Each Output instance writes directly to the index. All values written to a
 * given Output will be written to the index upon output {@link Output#close()};
 */
public class IndexOutputFactory<K, V>
        implements OutputFactory<K, V> {

    private IndexService<V> index;

    public void setIndexService(IndexService<V> svc) {
        index = svc;
    }
    
    public IndexOutputFactory<K, V> newInstance() {
        return this;
    }

    public Output<K, V> newOutput() {
        return new IndexOutput<K, V>(index);
    }

    /** Has no effect */
    public void close(boolean... success) {
        /* Does nothing */
    }
}
