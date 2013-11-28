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
package org.dataconservancy.dcs.ingest.services.runners;

import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.ingest.services.IngestService;
import org.dataconservancy.dcs.ingest.services.IngestServiceException;
import org.dataconservancy.model.dcp.Dcp;

public class ExecutorBootstrapTest {

    private static IngestFramework fwk = new IngestFramework();

    @BeforeClass
    public static void init() {
        fwk.setSipStager(new MemoryStager());
        InlineEventManager mgr = new InlineEventManager();
        mgr.setIdService(new MemoryIdServiceImpl());
        mgr.setSipStager(fwk.getSipStager());
        fwk.setEventManager(mgr);
    }

    @Test
    public void allServicesRunTest() throws Exception {
        MockIngestService[] services = new MockIngestService[12];

        for (int i = 0; i < services.length; i++) {
            services[i] = new MockSuccessfulService();
        }

        ExecutorBootstrap runner = new ExecutorBootstrap();
        runner.setIngestFramework(fwk);
        runner.setIngestServices(services);
        runner.setExecutor(Executors.newFixedThreadPool(1));

        String id = fwk.getSipStager().addSIP(new Dcp());
        runner.startIngest(id);

        /* Let the executor execute */
        Thread.sleep(1000);

        for (MockIngestService svc : services) {
            Assert.assertTrue(svc.hasExecuted());
        }
    }

    @Test
    public void stopRunningAfterFailureTest() throws Exception {
        int failIndex = 5;
        MockIngestService[] services = new MockIngestService[12];

        for (int i = 0; i < services.length; i++) {
            if (i != failIndex) {
                services[i] = new MockSuccessfulService();
            } else {
                services[i] = new MockFailedService();
            }
        }

        ExecutorBootstrap runner = new ExecutorBootstrap();
        runner.setIngestFramework(fwk);
        runner.setIngestServices(services);
        runner.setExecutor(Executors.newFixedThreadPool(1));

        String id = fwk.getSipStager().addSIP(new Dcp());
        runner.startIngest(id);

        /* Let the executor execute */
        Thread.sleep(500);

        for (int i = 0; i < services.length; i++) {
            if (i <= failIndex) {
                Assert.assertTrue(services[i].hasExecuted());
            } else {
                Assert.assertFalse(services[i].hasExecuted());
            }
        }
    }

    @Test
    public void ingestStartEventTest() throws Exception {
        ExecutorBootstrap runner = new ExecutorBootstrap();
        runner.setIngestFramework(fwk);
        runner.setIngestServices(new MockSuccessfulService());
        runner.setExecutor(Executors.newFixedThreadPool(1));

        String id = fwk.getSipStager().addSIP(new Dcp());
        runner.startIngest(id);

        Thread.sleep(500);

        Assert.assertNotNull(fwk.getEventManager()
                .getEventByType(id, Events.INGEST_START));
    }

    @Test
    public void failureEventTest() throws Exception {
        ExecutorBootstrap runner = new ExecutorBootstrap();
        runner.setIngestFramework(fwk);
        runner.setIngestServices(new MockFailedService());
        runner.setExecutor(Executors.newFixedThreadPool(1));

        String id = fwk.getSipStager().addSIP(new Dcp());
        runner.startIngest(id);

        Thread.sleep(500);

        Assert.assertNotNull(fwk.getEventManager()
                .getEventByType(id, Events.INGEST_FAIL));
    }

    private interface MockIngestService
            extends IngestService {

        public boolean hasExecuted();

        public void end();
    }

    private class MockSuccessfulService
            implements MockIngestService {

        private boolean executed = false;

        public boolean hasExecuted() {
            return executed;
        }

        public void execute(String sipRef) throws IngestServiceException {
            executed = true;
        }

        public void end() {
        }
    }

    private class MockFailedService
            implements MockIngestService {

        private boolean executed = false;

        public boolean hasExecuted() {
            return executed;
        }

        public void execute(String sipRef) throws IngestServiceException {
            executed = true;
            throw new IngestServiceException("I failed");
        }

        public void end() {
        }
    }
}
