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

import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.transform.Output;

class IndexOutput<K, V>
        implements Output<K, V> {

    protected final BatchIndexer<V> batch;

    public IndexOutput(IndexService<V> svc) {
        try {
            batch = svc.index();
        } catch (IndexServiceException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(K key, V value) {
        try {
            batch.add(value);
        } catch (IndexServiceException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            batch.close();
        } catch (IndexServiceException e) {
            throw new RuntimeException(e);
        }
    }

}
