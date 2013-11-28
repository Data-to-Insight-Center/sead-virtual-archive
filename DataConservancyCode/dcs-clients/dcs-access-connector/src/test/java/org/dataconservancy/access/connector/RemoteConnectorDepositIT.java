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
package org.dataconservancy.access.connector;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test uploadFile and depositSIP against a remote dcs instance.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class RemoteConnectorDepositIT {
    @Autowired
    @Qualifier("remoteDcsConnectorConfig")
    private DcsConnectorConfig config;

    @Test
    public void testUploadFile() throws Exception {
        HttpDcsConnector con = new HttpDcsConnector(config,
                new DcsXstreamStaxModelBuilder());

        File tmp = File.createTempFile("tmp", null);
        tmp.deleteOnExit();

        PrintWriter out = new PrintWriter(tmp);
        out.println("moo");
        out.close();

        {
            InputStream is = new FileInputStream(tmp);
            String id = con.uploadFile(is, tmp.length());
            is.close();

            assertNotNull(id);
        }

        // make sure connection released so we can upload again

        {
            InputStream is = new FileInputStream(tmp);
            String id = con.uploadFile(is, tmp.length());
            is.close();

            assertNotNull(id);
        }
    }

    @Test
    public void testUploadSip() throws Exception {
        HttpDcsConnector con = new HttpDcsConnector(config,
                new DcsXstreamStaxModelBuilder());

        Dcp sip = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setTitle("banana");
        du.setId("cow");
        sip.addDeliverableUnit(du);

        {
            URL ticket = con.depositSIP(sip);
            assertNotNull(ticket);
        }

        // make sure connection released so we can deposit again

        {
            URL ticket = con.depositSIP(sip);
            assertNotNull(ticket);
        }
    }
}
