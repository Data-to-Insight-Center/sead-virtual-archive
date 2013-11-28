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
package org.dataconservancy.dcs.transform.index.dcpsolr;

import java.io.File;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.FileUtil;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.transform.index.AbstractBatchIndexOutputFactoryTest;
import org.dataconservancy.dcs.transform.index.TestableBatchIndexOutputFactory;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.Output;

public class BatchIndexOutputFactoryTest
        extends AbstractBatchIndexOutputFactoryTest<String, Dcp> {

    private static AtomicInteger counter = new AtomicInteger(0);

    private static DcpIndexService index;

    private static File solrhome;

    @BeforeClass
    public static void setUp() throws Exception {

        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        SolrService solr = new SolrService(solrhome);

        index = new DcpIndexService(solr);
    }

    @Ignore
    @Test
    @Override
    public void testIsolationOfFactories() {
        /*
         * This test fails using the DcpIndexService because DcpBatchIndexer
         * instances are NOT isolated. A commit to one instance is essentially
         * equivalent to a commit to all. Thus, using the generic
         * BatchIndexOutputFactory to wrap a DcpIndexService results in a
         * combination unsuited to concurrency.
         */
    }

    @Override
    protected TestableBatchIndexOutputFactory<String, Dcp> newFactory() {
        return new TestBatchSolrOutputFactory(index);
    }

    @Override
    protected String newKey() {
        return Integer.toString(counter.incrementAndGet());
    }

    /* Generate a DCP with unique entities */
    @Override
    protected Dcp newValue() {
        return Util.newDcp();
    }

    private static class TestBatchSolrOutputFactory
            extends TestableBatchIndexOutputFactory<String, Dcp> {

        private final Set<String> added = new HashSet<String>();

        public TestBatchSolrOutputFactory(IndexService<Dcp> svc) {
            super(svc);
        }

        @Override
        public Output<String, Dcp> newOutput() {
            final Output<String, Dcp> delegate = super.newOutput();
            return new Output<String, Dcp>() {

                public void write(String key, Dcp value) {
                    delegate.write(key, value);
                    synchronized (added) {
                        added.addAll(Util.getAllIDs(value));
                    };
                }

                public void close() {
                    delegate.close();
                }
            };
        }

        @Override
        public int closeAndCount(boolean... success) {
            super.close(success);
            if (success.length == 0 || success[0]) {
                return added.size();
            } else {
                return 0;
            }
        }

        @Override
        public long getIndexSize() {
            try {
                return index.size();
            } catch (IndexServiceException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasIndexed(Dcp value) {
            return Util.lookupAll(value, index);
        }

    }
}
