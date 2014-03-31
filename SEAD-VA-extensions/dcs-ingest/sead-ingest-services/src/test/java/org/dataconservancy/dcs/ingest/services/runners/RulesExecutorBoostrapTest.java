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

import org.dataconservancy.dcs.id.impl.MemoryIdServiceImpl;
import org.dataconservancy.dcs.ingest.IngestFramework;
import org.dataconservancy.dcs.ingest.impl.InlineEventManager;
import org.dataconservancy.dcs.ingest.impl.MemoryStager;
import org.dataconservancy.dcs.ingest.services.IngestService;
import org.dataconservancy.dcs.ingest.services.IngestServiceException;
import org.dataconservancy.dcs.ingest.services.runners.RulesExecutorBootstrap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class RulesExecutorBoostrapTest {

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
    public void rulesExecutorTest() throws Exception {


        RulesExecutorBootstrap runner = new RulesExecutorBootstrap();
        Map<String,IngestService> serviceMap = new HashMap<String, IngestService>();
        serviceMap.put("test", new MockSuccessfulService());
        runner.setServiceMap(serviceMap);
        runner.setIngestFramework(fwk);
        runner.setExecutor(Executors.newFixedThreadPool(1));

        String id = fwk.getSipStager().addSIP((ResearchObject)new SeadXstreamStaxModelBuilder().buildSip(getClass().getResourceAsStream("../rood_d1_sip.xml")));
        runner.startIngest(id);

        /* Let the executor execute */
        Thread.sleep(1000*5);
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

            System.out.println("Executing");
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
