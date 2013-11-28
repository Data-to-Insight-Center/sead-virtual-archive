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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.transform.Output;
import org.dataconservancy.transform.dcp.SipDcpReader;
import org.dataconservancy.transform.execution.BasicExecutionEnvironment;
import org.dataconservancy.transform.execution.NoOpMapping;
import org.dataconservancy.transform.execution.OutputFactory;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.Assert.assertEquals;

public class SingleSipIT {

    private static DcpIndexService index;

    private static ArchiveStore archive;

    private static OutputFactory<String, Dcp> outputFactory;

    private static ConfigurableApplicationContext appContext;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUp() throws IOException {

        appContext =
                new NonValidatingClasspathXmlApplicationContext(new String[] {
                        "classpath*:org/dataconservancy/config/applicationContext.xml",
                        "IndexService-dcpsolr.xml"});

        index =
                (DcpIndexService) appContext
                        .getBean("org.dataconservancy.dcs.index.dcpsolr.DcpIndexService");

        archive =
                (ArchiveStore) appContext
                        .getBean("org.dataconservancy.archive.api.ArchiveStore");

        outputFactory =
                (OutputFactory<String, Dcp>) appContext
                        .getBean("org.dataconservancy.dcs.index.transform.IndexOutputFactory_DcpIndexService");
    }

    @AfterClass
    public static void tearDown() {
        appContext.close();
    }

    @Test
    public void testIndexAndRetrieval() throws Exception {
        BasicExecutionEnvironment env = new BasicExecutionEnvironment();

        Dcp deposited = Util.newDcp();
        DcsModelBuilder builder = new DcsXstreamStaxModelBuilder();

        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        builder.buildSip(deposited, outs);

        archive.putPackage(new ByteArrayInputStream(outs.toByteArray()));

        SipDcpReader sdc = new SipDcpReader(deposited);
        Output<String, Dcp> out = outputFactory.newOutput();

        env.execute(sdc, new NoOpMapping<String, Dcp>(), out);

        out.close();

        for (DcsEntity e : deposited) {
            assertEquals(e, index.lookupEntity(e.getId()));
        }
    }
}
