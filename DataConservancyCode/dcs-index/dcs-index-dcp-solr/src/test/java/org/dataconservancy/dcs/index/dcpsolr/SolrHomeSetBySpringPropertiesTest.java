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
import org.apache.solr.core.CoreContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A test ensuring that the 'solr.solr.home' property can be set by the Spring container.  This test requires that
 * a solr.solr.home be defined in <code>dcpsolr.properties</code>, and that <strong>no</strong> solr.solr.home System
 * property exists.  To be thorough, it checks that the value of solr.solr.home has been interpolated, and that the
 * home directory is created.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/org/dataconservancy/config/applicationContext.xml"})
public class SolrHomeSetBySpringPropertiesTest {


    /**
     * The solrBootstrap bean creates the solr.solr.home directory and populates it.  It returns
     * the String to the created directory.
     */
    @Resource(name = "solrBootstrap")
    private String solrHome;

    private String originalSolrHome = "_FOO_";

    @Before
    public void setUp() {
        if (System.getProperties().containsKey("solr.solr.home")) {
            originalSolrHome = System.getProperty("solr.solr.home");
            System.getProperties().remove("solr.solr.home");
        }
    }

    @After
    public void tearDown() {
        if (!"_FOO_".equals(originalSolrHome)) {
            System.setProperty("solr.solr.home", originalSolrHome);
        } else {
            System.getProperties().remove("solr.solr.home");
        }
    }

    @Test
    public void testSetByDcpSolrProperties() throws IOException {
        assertNotNull("Solr home was not injected!", solrHome);
        assertFalse("System property solr.solr.home (value: '" + System.getProperty("solr.solr.home") + "') should not exist!",
                System.getProperties().containsKey("solr.solr.home"));

        // ensure the property has been interpolated (it shouldn't contain '${' or '}'
        assertFalse("Solr home " + solrHome + " has not been interpolated!",
                solrHome.contains("${") || solrHome.contains("}"));

        assertTrue("solr.solr.home " + solrHome + " should have been created!",
                new File(solrHome).exists());
    }

    @Test
    public void testStartSolrDefaultContainer() throws IOException, SolrServerException, SAXException, ParserConfigurationException {
        System.setProperty("solr.solr.home", solrHome);
        final File solrXml = new File(solrHome, "solr.xml");
        assertTrue("Cannot find '" + solrXml.getAbsolutePath(), solrXml.exists() && solrXml.canRead() && solrXml.isFile());
        final CoreContainer container = new CoreContainer(solrHome, solrXml);
        final EmbeddedSolrServer server = new EmbeddedSolrServer(container, "default");

        // TODO: I'm not sure how to interpret the ping status response.
//        final int pingStatus = server.ping().getStatus();
//        assertTrue("Unable to start Embedded Solr Server: " + pingStatus,
//                pingStatus >= 200 && pingStatus < 400);
    }

}
