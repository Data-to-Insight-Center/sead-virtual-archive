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

import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;

public class IdealIndexService
        implements IndexService<String> {

    final Set<String> content = new HashSet<String>();

    public BatchIndexer<String> index() throws IndexServiceException {
        return new BatchIndexer<String>() {

            public final Set<String> staged = new HashSet<String>();

            public void add(String obj) throws IndexServiceException {
                synchronized (staged) {
                    staged.add(obj);
                }
            }

            public void remove(String id) throws IndexServiceException,
                    UnsupportedOperationException {
                /* Does nothing */
            }

            public void close() throws IndexServiceException {
                synchronized (content) {
                    content.addAll(staged);
                }
            }
        };
    }

    public void clear() throws IndexServiceException {
        content.clear();
    }

    public void optimize() throws IndexServiceException {
        /* Does nothing */
    }

    public long size() throws IndexServiceException {
        synchronized (content) {
            return content.size();
        }
    }

    public void shutdown() throws IndexServiceException {
        /* Does nothing */
    }

}
