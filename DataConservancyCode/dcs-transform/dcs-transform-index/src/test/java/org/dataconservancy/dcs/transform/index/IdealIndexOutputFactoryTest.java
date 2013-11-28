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

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class IdealIndexOutputFactoryTest
        extends AbstractIndexOutputFactoryTest<String, String> {

    /* Just use a counter to provide unique values for newValue() and newKey() */
    private AtomicInteger counter = new AtomicInteger(0);

    protected String newValue() {
        return Integer.toString(counter.incrementAndGet());
    }

    protected String newKey() {
        return Integer.toString(counter.incrementAndGet());
    }

    protected TestableIndexOutputFactory<String, String> getFactory() {
        return new IdealIndexOutputFactory();
    }

    private static class IdealIndexOutputFactory
            extends TestableIndexOutputFactory<String, String> {

        private final IdealIndexService index = new IdealIndexService();

        public TestableIndexOutput<String, String> newOutput() {
            return new TestableIndexOutput<String, String>(index) {

                public boolean hasIndexed(String value) {
                    return index.content.contains(value);
                }

                public long getIndexSize() {
                    try {
                        return index.size();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @SuppressWarnings("unchecked")
                public int closeAndCount() {
                    try {
                        super.close();
                        return ((Set<String>) batch.getClass()
                                .getField("staged").get(batch)).size();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        public boolean hasIndexed(String value) {
            return index.content.contains(value);
        }
    }
}
