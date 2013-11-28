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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.transform.Output;

public class IdealBatchIndexOutputFactoryTest
        extends AbstractBatchIndexOutputFactoryTest<String, String> {

    private static final AtomicInteger counter = new AtomicInteger();

    private IdealIndexService indexService = new IdealIndexService();

    protected TestableBatchIndexOutputFactory<String, String> newFactory() {
        return new IdealBatchIndexOutputFactory(indexService);
    }

    protected String newValue() {
        return Integer.toString(counter.incrementAndGet());
    }

    protected String newKey() {
        return Integer.toString(counter.incrementAndGet());
    }

    private class IdealBatchIndexOutputFactory
            extends TestableBatchIndexOutputFactory<String, String> {

        private final Set<String> added = new HashSet<String>();

        public IdealBatchIndexOutputFactory(IndexService<String> svc) {
            super(svc);
        }

        public boolean hasIndexed(String value) {
            return indexService.content.contains(value);
        }

        public Output<String, String> newOutput() {
            final Output<String, String> delegate = super.newOutput();
            return new Output<String, String>() {

                public void write(String key, String value) {
                    // TODO Auto-generated method stub
                    delegate.write(key, value);
                    synchronized (added) {
                        added.add(key);
                    };
                }

                public void close() {
                    delegate.close();
                }
            };
        }

        public long getIndexSize() {
            try {
                return indexService.size();
            } catch (IndexServiceException e) {
                throw new RuntimeException(e);
            }
        }

        public int closeAndCount(boolean... success) {
            super.close(success);
            if (success.length == 0 || success[0]) {
                return added.size();
            } else {
                return 0;
            }
        }
    }
}
