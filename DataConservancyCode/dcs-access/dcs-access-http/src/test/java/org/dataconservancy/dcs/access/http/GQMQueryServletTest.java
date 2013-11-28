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
package org.dataconservancy.dcs.access.http;

import java.io.ByteArrayInputStream;

import java.net.URI;

import java.sql.SQLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

import org.junit.Assert;
import org.junit.Before;

import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.model.gqm.GQM;
import org.dataconservancy.model.gqm.GQMList;
import org.dataconservancy.model.gqm.Geometry;
import org.dataconservancy.model.gqm.Location;
import org.dataconservancy.model.gqm.Point;
import org.dataconservancy.model.gqm.builder.GQMBuilder;
import org.dataconservancy.model.gqm.builder.xstream.XstreamGQMBuilder;
import org.dataconservancy.index.gqmpsql.GqmIndexService;
import org.dataconservancy.index.gqmpsql.SpatialReferenceSystem;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import junit.framework.TestCase;

public class GQMQueryServletTest extends TestCase{
    

    private ServletTester servletContainer;
    private GQMList gqms;
    
    private GqmIndexService index_service;

    public GQMQueryServletTest() throws SQLException {
//        System.setProperty("dc.gqm.db.uri",
//                         "jdbc:postgresql://ben-test.dkc.jhu.edu/gqmtest");
//        System.setProperty("dc.gqm.db.user", "gqmtest");
//        System.setProperty("dc.gqm.db.pass", "testybanana");
    }
   
    
    @Before
    public void setUp() throws Exception { 
        gqms = new GQMList();
        
        index_service = new GqmIndexService();
        index_service.clear();
        URI srid = SpatialReferenceSystem.forEPSG(4326);
        GQM one = new GQM("ONE");
       
        one.getLocations().add(
                new Location(new Geometry(Geometry.Type.LINE,
                        new Point(0, 0), new Point(5, 0)), srid));
       
        GQM two = new GQM("TWO");
        
        two.getLocations().add(new Location(new Geometry(Geometry.Type.LINE, new Point(3,
                                        0), new Point(8, 0)), srid));
        gqms.getGQMs().add(one);
        gqms.getGQMs().add(two);
        
        BatchIndexer<GQM> batch = index_service.index();
        batch.add(one);
        batch.add(two);
        batch.close();
        
        servletContainer = new ServletTester();
        servletContainer.setContextPath("/qf");
        servletContainer.addServlet(GQMQueryServlet.class, "/query/*");

        servletContainer
                .getContext()
                .getServletContext()
                .setAttribute("gqmquery.impl",
                              "org.dataconservancy.query.gqmpsql.GqmQueryService");
        String dsbaseurl = "/access/datastream";
        servletContainer.getContext().getServletContext()
                .setAttribute("datastream.url", dsbaseurl);
        servletContainer.start();
    }
    
    public void tearDown() throws Exception {
        if( servletContainer != null ){
            servletContainer.stop();
        }        
        index_service.clear();
    }

    public void testGET_Gqm() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        GQMBuilder gqmBuilder = new XstreamGQMBuilder();

        //Valid Test that should provide match
        request.setURI("/qf/query/"
                + ServletUtil.encodeURLPath("intersects([line 'EPSG:4326' 4 -1, 4 1])"));
        
        response.parse(servletContainer.getResponses(request.generate()));

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/xml", response.getContentType());
        Assert.assertNotNull(response.getContent());
        Assert.assertNotNull(response.getHeader("X-TOTAL-MATCHES"));

        Assert.assertEquals("" + gqms.getGQMs().size(), response.getHeader("X-TOTAL-MATCHES"));

        GQMList g = gqmBuilder.buildGQMList(new ByteArrayInputStream(response.getContent()
                        .getBytes()));

        Assert.assertEquals(gqms.getGQMs().size(), g.getGQMs().size());

        //Valid test that returns no matches
        request.setURI("/qf/query/"
                       + ServletUtil.encodeURLPath("intersects([line 'EPSG:4326' 4 1, 4 4])"));

        response.parse(servletContainer.getResponses(request.generate()));
        
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/xml", response.getContentType());
        Assert.assertNotNull(response.getContent());
        Assert.assertNotNull(response.getHeader("X-TOTAL-MATCHES"));

        Assert.assertEquals("0", response.getHeader("X-TOTAL-MATCHES"));
               
        //Invalid test
        request.setURI("/qf/query/" + 
                       ServletUtil.encodeURLPath("garbageQuery"));
        response.parse(servletContainer.getResponses(request.generate()));
        Assert.assertEquals(404, response.getStatus());
        
        //Invalid test
        request.setURI("/qf/gqm/" + 
                       ServletUtil.encodeURLPath("Location:560"));
        response.parse(servletContainer.getResponses(request.generate()));
        Assert.assertEquals(404, response.getStatus());
    }

    public void testGET_JSON() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        request.setHeader("Accept", "application/javascript");
        
        request.setURI("/qf/query/"
                        + ServletUtil.encodeURLPath("intersects([line 'EPSG:4326' 4 -1, 4 1])")
                        + "?callback=js_cb");

        response.parse(servletContainer.getResponses(request.generate()));

        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("application/javascript", response.getContentType());
        Assert.assertNotNull(response.getContent());
        Assert.assertNotNull(response.getHeader("X-TOTAL-MATCHES"));
        Assert.assertTrue(response.getContent().startsWith("js_cb("));

        Assert.assertEquals("2", response.getHeader("X-TOTAL-MATCHES"));
    }

    public void testHEAD() throws Exception {
        String baseurl = servletContainer.createSocketConnector(true);
      
        HttpClient client = new HttpClient();
        HeadMethod head =
                new HeadMethod(baseurl + "/qf/query/"
                        + ServletUtil.encodeURLPath("intersects([line 'EPSG:4326' 4 -1, 4 1])"));
        int status = client.executeMethod(head);
        Assert.assertEquals(200, status);

        Assert.assertEquals("application/xml", head.getResponseHeader("Content-Type")
                .getValue());
        Assert.assertEquals("" + gqms.getGQMs().size(), head
                .getResponseHeader("X-TOTAL-MATCHES").getValue());

        head = new HeadMethod(baseurl + "/qf/query/badfield:");
        status = client.executeMethod(head);
        Assert.assertEquals(404, status);
    }
}
