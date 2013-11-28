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
import org.dataconservancy.transform.execution.OutputFactory;

/**
 * Produces {@link Output} instances associated with a common batch indexing
 * task.
 * <p>
 * BatchIndexOutputFactory is instantiated during the lifetime of a single batch
 * indexing operation. All {@link Output} instances produced by the factory
 * instance contribute to the same index. {@link Output#close()} on any single
 * output will have no effect. {@link #close(boolean...)} on the
 * {@linkplain BatchIndexOutputFactory} will cause all results written by every
 * output to be committed to the index in a batch. The factory cannot be reused
 * after this happens.
 * </p>
 */
public class BatchIndexOutputFactory<K, V>
        implements OutputFactory<K, V> {

    private BatchIndexer<V> indexer;

    private final IndexService<V> svc;

    public BatchIndexOutputFactory(IndexService<V> svc) {
        this.svc = svc;
    }

    public BatchIndexOutputFactory<K, V> newInstance() {
        return new BatchIndexOutputFactory<K, V>(svc);
    }

    public Output<K, V> newOutput() {
        if (indexer == null) {
            try {
                indexer = svc.index();
            } catch (IndexServiceException e) {
                throw new RuntimeException(e);
            }
        }
        return new BatchOutputImpl();
    }

    private class BatchOutputImpl
            implements Output<K, V> {

        public void write(K key, V value) {
            synchronized (indexer) {
                try {
                    indexer.add(value);
                } catch (IndexServiceException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void close() {
            /* Do nothing */
        }
    }

    public void close(boolean... success) {
        try {
            if (indexer != null && (success.length == 0 || success[0] == true)) {
                indexer.close();
            }
        } catch (IndexServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
