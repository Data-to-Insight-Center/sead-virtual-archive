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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.FileUtil;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.transform.index.AbstractIndexOutputFactoryTest;
import org.dataconservancy.dcs.transform.index.TestableIndexOutput;
import org.dataconservancy.dcs.transform.index.TestableIndexOutputFactory;
import org.dataconservancy.model.dcp.Dcp;

import static org.junit.Assert.fail;

/**
 * Applies generic suite of tests to an IndexOutput wrapping a DcpIndexService.
 * <p>
 * </p>
 */
public class IndexOutputFactoryTest
        extends AbstractIndexOutputFactoryTest<String, Dcp> {

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

    @Ignore("This test fails on the Mac platform: DC-707")
    @Override
    @Test
    public void testThreadSafe() {
        fail("This test fails on the Mac platform: DC-707");
    }

    @Ignore
    @Test
    @Override
    public void testIsolationOfOutputs() {
        /*
         * This test fails using the DcpIndexService because DcpBatchIndexer
         * instances are NOT independent. A commit to one instance is
         * essentially equivalent to a commit to all. Thus, using the generic
         * IndexOutputFactory to wrap a DcpIndexService results in a combination
         * unsuited to concurrency.
         */
    }

    protected Dcp newValue() {
        return Util.newDcp();
    }

    /* Generate a unique string */
    protected String newKey() {
        return Integer.toString(counter.incrementAndGet());
    }

    protected TestableIndexOutputFactory<String, Dcp> getFactory() {
        return new TestSolrOutputFactory(index);
    }

    private static class TestSolrOutputFactory
            extends TestableIndexOutputFactory<String, Dcp> {

        private final DcpIndexService svc;

        public TestSolrOutputFactory(DcpIndexService svc) {
            this.svc = svc;
        }

        public TestableIndexOutput<String, Dcp> newOutput() {

            try {
                return new TestableIndexOutput<String, Dcp>(svc) {

                    private List<String> added = new ArrayList<String>();

                    public boolean hasIndexed(Dcp value) {
                        return Util.lookupAll(value, svc);
                    }

                    public long getIndexSize() {
                        try {
                            return svc.size();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    public void write(String key, Dcp value) {
                        added.addAll(Util.getAllIDs(value));
                        super.write(key, value);
                    }

                    public int closeAndCount() {
                        super.close();
                        return new HashSet<String>(added).size();
                    }
                };
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean hasIndexed(Dcp value) {
            return Util.lookupAll(value, svc);
        }

    }
}
