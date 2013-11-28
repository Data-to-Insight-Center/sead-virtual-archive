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
package org.dataconservancy.dcs.query.endpoint.dcpsolr;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

import org.dataconservancy.dcs.query.endpoint.utils.ServletUtil;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

public class QueryServletTest
        extends AbstractQueryTest {

    private ServletTester servletContainer;
    private Dcp dcp;
    
    public void setUp() throws Exception {
        super.setUp();
        archive = null;
        
        DcsDeliverableUnit du = rb.createDeliverableUnit(null, null, true);
        du.setTitle("DUTITLE");
        
        dcp = rb.createDcp(2, 2);
        dcp.addDeliverableUnit(du);
        
        index(dcp);

        servletContainer = new ServletTester();
        servletContainer.setContextPath("/qf");
        servletContainer.addServlet(DcpQueryServlet.class, "/query/*");

        servletContainer
                .getContext()
                .getServletContext()
                .setAttribute("dcpquery.impl",
                              "org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService");
        String dsbaseurl = "/qf/datastream";
        servletContainer.getContext().getServletContext()
                .setAttribute("datastream.url", dsbaseurl);
        servletContainer.start();
    }

    public void tearDown() throws Exception {
        servletContainer.stop();
        super.tearDown();
    }

    public void testGET_Dcp() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();

        Collection<DcsFile> files = dcp.getFiles();

        request.setURI("/qf/query/"
                + ServletUtil.encodeURLPath("entityType:File"));

        response.parse(servletContainer.getResponses(request.generate()));

        assertEquals(200, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        assertNotNull(response.getContent());
        assertNotNull(response.getHeader("X-TOTAL-MATCHES"));

        assertEquals("" + files.size(), response.getHeader("X-TOTAL-MATCHES"));

        Dcp d =
                mb.buildSip(new ByteArrayInputStream(response.getContent()
                        .getBytes()));

        assertEquals(files.size(), d.getFiles().size());

        request.setURI("/qf/entity/" + ServletUtil.encodeURLPath(":DUTITLE:"));
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());

        request
                .setURI("/qf/entity/"
                        + ServletUtil
                                .encodeURLPath("title:(alksdfasd-axda+!E@!QW@#$!+@!#--~+))"));
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());

        request.setURI("/qf/entity/"
                + ServletUtil.encodeURLPath("title:test\\-escape)"));
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());

        request
                .setURI("/qf/entity/"
                        + "id%3A((%22ARC%5C-11%22)+)+OR+former%3A((%22ARC%5C-11%22)+)creator%3A((%22ARC%5C-11%22)+)+OR+subject%3A((%22ARC%5C-11%22)+)+OR+title%3A((%22ARC%5C-11%22)+)+OR+type%3A((%22ARC%5C-11%22)+)+OR+metadataSearchText%3A((%22ARC%5C-11%22)+)+OR+metadataSchema%3A((%22ARC%5C-11%22)+)fileName%3A((%22ARC%5C-11%22)+)+OR+fileSource%3A((%22ARC%5C-11%22)+)+OR+format%3A((%22ARC%5C-11%22)+)+OR+formatName%3A((%22ARC%5C-11%22)+)+OR+formatSchema%3A((%22ARC%5C-11%22)+)+OR+filePath%3A((%22ARC%5C-11%22)+)+OR+fixityValue%3A((%22ARC%5C-11%22)+)parent%3A((%22ARC%5C-11%22)+)+OR+metadataRef%3A((%22ARC%5C-11%22)+)+OR+formatSchema%3A((%22ARC%5C-11%22)+)+OR+metadataSchema%3A((%22ARC%5C-11%22)+)+OR+collection%3A((%22ARC%5C-11%22)+)+OR+relatedTo%3A((%22ARC%5C-11%22)+)+OR+eventTarget%3A((%22ARC%5C-11%22)+)+OR+deliverableunit%3A((%22ARC%5C-11%22)+)+OR+fileRef%3A((%22ARC%5C-11%22)+)+OR+hasRelationship%3A((%22ARC%5C-11%22)+)?");
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());
    }

    public void testGET_JSON() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        request.setHeader("Accept", "application/javascript");
        request
                .setURI("/qf/query/"
                        + ServletUtil
                                .encodeURLPath("entityType:DeliverableUnit AND title:DUTITLE")
                        + "?callback=js_cb");

        response.parse(servletContainer.getResponses(request.generate()));

        assertEquals(200, response.getStatus());
        assertEquals("application/javascript", response.getContentType());
        assertNotNull(response.getContent());
        assertNotNull(response.getHeader("X-TOTAL-MATCHES"));
        assertTrue(response.getContent().startsWith("js_cb("));

        assertEquals("1", response.getHeader("X-TOTAL-MATCHES"));
    }

    public void testHEAD() throws Exception {
        // ServletTester has internal errors processing head requests so use http client
        // request.setMethod("GET");
        String baseurl = servletContainer.createSocketConnector(true);

        HttpClient client = new HttpClient();

        HeadMethod head =
                new HeadMethod(baseurl + "/qf/query/"
                        + ServletUtil.encodeURLPath("entityType:File"));
        int status = client.executeMethod(head);
        assertEquals(200, status);

        assertEquals("application/xml", head.getResponseHeader("Content-Type")
                .getValue());
        assertEquals("" + dcp.getFiles().size(), head
                .getResponseHeader("X-TOTAL-MATCHES").getValue());

        head = new HeadMethod(baseurl + "/qf/query/badfield:");
        status = client.executeMethod(head);
        assertEquals(404, status);
    }
    
    public void testJavascriptHighlight() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        request.setURI("/qf/query/"
                + ServletUtil.encodeURLPath("title:DUTITLE") + "?callback=test&_hl=true&_hl.requireFieldMatch=true&_hl.fl=*");

        response.parse(servletContainer.getResponses(request.generate()));

        assertEquals(200, response.getStatus());
        assertEquals("application/javascript", response.getContentType());
        assertNotNull(response.getContent());
        assertNotNull(response.getHeader("X-TOTAL-MATCHES"));

        assertEquals("" + 1, response.getHeader("X-TOTAL-MATCHES"));
        
        // TODO actually parse json...

        assertTrue(response.getContent().contains("context"));
    }
}
