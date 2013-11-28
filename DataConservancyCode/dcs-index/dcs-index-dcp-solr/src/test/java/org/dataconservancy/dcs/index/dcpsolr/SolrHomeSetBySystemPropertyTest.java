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
package org.dataconservancy.dcs.index.dcpsolr;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.core.CoreContainer;
import org.dataconservancy.dcs.index.dcpsolr.support.SolrBootstrap;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A test ensuring that the value of 'solr.solr.home' has been set by a system property (see the Maven surefire
 * plugin configuration, and the @Before and @After methods of {@link AbstractIndexTest}), and that the SolrBootstrap
 * successfully creates a Solr installation.
 */
public class SolrHomeSetBySystemPropertyTest {

    @Test
    public void testSetBySystemProperty() throws IOException {
        assertTrue("Missing system property: solr.solr.home!",
                System.getProperties().containsKey("solr.solr.home"));

        final String solrHome = System.getProperty("solr.solr.home");

        // ensure the property has been interpolated (it shouldn't contain '${' or '}'
        assertFalse("System property solr.solr.home has not been interpolated!",
                solrHome.contains("${") || solrHome.contains("}"));

        // ensure that the SolrBootstrap class reports the same solr.solr.home as the
        // system property
        final String solrBootstrapHome = SolrBootstrap.createIfNecessary(System.getProperty("solr.solr.home"));

        assertEquals("SolrBootstrap solr.solr.home differs from the System property solr.solr.home",
                solrHome, solrBootstrapHome);

        assertTrue("solr.solr.home " + solrHome + " should have been created!",
                new File(solrHome).exists());
    }

    @Test
    public void testStartSolrDefaultContainer() throws IOException, SolrServerException {
        CoreContainer container = new CoreContainer(System.getProperty("solr.solr.home"));
        EmbeddedSolrServer server = new EmbeddedSolrServer(container, "default");

        // TODO: I'm not sure how to interpret the ping status response.
//        final SolrPingResponse pingResponse = server.ping();
//        final int pingStatus = pingResponse.getStatus();
//        assertTrue("Unable to start Embedded Solr Server: " + pingStatus + " (" + pingResponse.getElapsedTime() + ")",
//                pingStatus >= 200 && pingStatus < 400);
    }

}
