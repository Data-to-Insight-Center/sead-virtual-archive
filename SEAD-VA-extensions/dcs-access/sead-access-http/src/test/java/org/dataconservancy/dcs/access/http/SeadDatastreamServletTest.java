/*
 * Copyright 2014 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.http;

import junit.framework.TestCase;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SeadDatastreamServletTest
        extends TestCase {

    private ServletTester servletContainer;

     private String dsbaseurl;



    MockServletConfig config;


    public void setUp() throws Exception {

        config = new MockServletConfig(new MockServletContext(), "complex");
        config.getServletContext().setAttribute("dcpquery.impl",
                "org.dataconservancy.dcs.query.dcpsolr.SeadDataModelQueryService");
        config.getServletContext().setAttribute("modelbuilder.impl",
                "org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder");
        config.getServletContext().setAttribute("sead.converter.util",
                "org.dataconservancy.dcs.query.dcpsolr.SeadUtil");
        config.getServletContext().setAttribute("sdahost",
                "your-sda-host");
        config.getServletContext().setAttribute("sdauser",
                "your-sda-user");
        config.getServletContext().setAttribute("sdapwd",
                "your-sda-pwd");
        config.getServletContext().setAttribute("sdamount",
                "mount-path-if-any");

        dsbaseurl =  "/access/datastream/";
        config.getServletContext().setAttribute("datastream.url", dsbaseurl);

        Properties props = System.getProperties();
        String solrPath = SeadDatastreamServletTest.class.getResource("./solr").getPath();
        props.setProperty("solr.solr.home", solrPath);
    }

    @Test
    public void testGetDataStream() throws Exception {
        setUp();
        List<String> ids = new ArrayList<String>();
        ids.add("http://bluespruce.pti.indiana.edu:8181/dcs-nced/entity/28929");//SDA example
        ids.add("http://bluespruce.pti.indiana.edu:8181/dcs-nced/entity/707"); //UIUC example
        ids.add("http://bluespruce.pti.indiana.edu:8181/dcs-nced/entity/680"); //IU Scholarworks);

        for(String id:ids){
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setPathInfo("/" +
                                id
            );

            SeadDatastreamServlet servlet = new SeadDatastreamServlet();
            servlet.init(config);

            for(int i=0;i<1;i++){
                MockHttpServletResponse response = new MockHttpServletResponse();
                response.setOutputStreamAccessAllowed(true);
                servlet.doGet(request, response);
                assertTrue(response.getContentAsByteArray().length>0);
            }
        }
    }
}
