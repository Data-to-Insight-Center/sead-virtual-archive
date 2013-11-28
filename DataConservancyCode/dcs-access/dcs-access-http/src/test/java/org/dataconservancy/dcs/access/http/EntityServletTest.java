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
import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.DcpUtil;
import org.dataconservancy.dcs.index.dcpsolr.FileUtil;
import org.dataconservancy.dcs.index.dcpsolr.RandomEntityBuilder;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import junit.framework.TestCase;

public class EntityServletTest
        extends TestCase {

    private List<DcsEntity> entities;

    private ServletTester servletContainer;
   
    private File solrhome;
    protected DcpIndexService indexService;
    protected SolrService solr;
    protected ArchiveStore archive;
    protected File archivehome;
    private String testhost;
    private String dsbaseurl;
    
    public void setUp() throws Exception {
        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        SolrService solr = new SolrService(solrhome);
        
        archivehome = FileUtil.createTempDir("archive");
        archive = createTestArchiveStore(archivehome);
        indexService = new DcpIndexService(archive, solr);

        indexService.clear();

        RandomEntityBuilder rb = new RandomEntityBuilder();
        DcsDeliverableUnit du = rb.createDeliverableUnit(null, null, false);

        System.out.println("Servlet Test Starting du: " + du);
        List<DcsFile> files = new ArrayList<DcsFile>();
        DcsManifestation man = rb.createManifestation(du.getId(), files);

        entities = new ArrayList<DcsEntity>();
        entities.add(du);
        entities.add(man);
        entities.add(rb.createEvent(null));
        entities.add(rb.createCollection(null));
        entities.addAll(files);

        servletContainer = new ServletTester();

        String baseurl = servletContainer.createSocketConnector(true);
        testhost = new URL(baseurl).getAuthority();

        int idcount = 0;
        for (DcsEntity entity : entities) {
            entity.setId(baseurl + "/access/entity/"
                    + entity.getClass().getName() + "," + idcount++);
        }
        
        index(DcpUtil.add(null, entities));

        servletContainer.setContextPath("/access");
        servletContainer.addServlet(EntityServlet.class, "/entity/*");
        servletContainer.getContext().getServletContext()
                .setAttribute("dcpquery.impl",
                              "org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService");        
        dsbaseurl =  baseurl + "/access/datastream/";
        servletContainer.getContext().getServletContext()
                .setAttribute("datastream.url", dsbaseurl);
        servletContainer.start();
    }

    public void tearDown() throws Exception {
        servletContainer.stop();
        indexService.shutdown();
        FileUtil.delete(solrhome);
        FileUtil.delete(archivehome);
    }

    private static ArchiveStore createTestArchiveStore(File baseDir) {
        FilePathKeyAlgorithm eAlg = new KeyDigestPathAlgorithm("MD5", 1, 2,
                null);

        FilePathKeyAlgorithm mAlg = new KeyDigestPathAlgorithm("MD5", 1, 2,
                ".md");

        FsMetadataStore mStore = new FsMetadataStore();
        mStore.setFilePathKeyAlgorithm(mAlg);
        mStore.setBaseDir(baseDir.getPath());

        FsEntityStore eStore = new FsEntityStore();
        eStore.setFilePathKeyAlgorithm(eAlg);
        eStore.setBaseDir(baseDir.getPath());

        ElmArchiveStore aStore = new ElmArchiveStore();
        aStore.setMetadataStore(mStore);
        aStore.setEntityStore(eStore);
        aStore.init();

        return aStore;
    }
    
    protected void index(Dcp dcp) throws IndexServiceException,
            AIPFormatException {
        // Package must be in archive so indexing service can lookup files
        
        if (archive != null) {
            archive.putPackage(DcpUtil.asInputStream(dcp));
        }
        
        BatchIndexer<Dcp> batch = indexService.index();
        batch.add(dcp);
        batch.close();
        
        indexService.optimize();
    }
    
    public void testGET_DCP() throws Exception {
        DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Accept", "application/xml");
        request.setHeader("Host", testhost);
        request.setVersion("HTTP/1.0");

        for (DcsEntity entity : entities) {
            request.setURI(entity.getId());

            response.parse(servletContainer.getResponses(request.generate()));
            assertEquals(200, response.getStatus());
            assertEquals("application/xml", response.getContentType());

            String text = response.getContent();
            if( entity instanceof DcsDeliverableUnit )
            {
                System.out.println("Servlet test response: " + response.getContent());
            }
            assertNotNull(text);

            byte[] bytes = text.getBytes("UTF-8");
            assertNotNull(response.getHeader("Content-Length"));
            assertEquals(response.getHeader("Content-Length"), ""
                    + bytes.length);
            assertNotNull(response.getHeader("ETag"));

            Dcp dcp = mb.buildSip(new ByteArrayInputStream(bytes));
           
            
            List<DcsEntity> list = DcpUtil.asList(dcp);
           
            assertEquals(1, list.size());

            DcsEntity e = list.get(0);
            if( entity instanceof DcsDeliverableUnit )
            {
                System.out.println("Servlet test dcp: " + dcp);
                System.out.println("Servlet test entity: " + e);
            }
            if (e instanceof DcsFile) {
                assertEquals(dsbaseurl + ServletUtil.encodeURLPath(e.getId()), ((DcsFile) e).getSource());
            } else {
                assertEquals(entity, e);
            }
        }
    }

    public void testGET_JSON() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Accept", "application/javascript");
        request.setHeader("Host", testhost);
        request.setVersion("HTTP/1.0");

        for (DcsEntity entity : entities) {
            request.setURI(entity.getId());

            response.parse(servletContainer.getResponses(request.generate()));

            assertEquals(200, response.getStatus());
            assertEquals("application/javascript", response.getContentType());

            String text = response.getContent();
            assertNotNull(text);

            assertNotNull(response.getHeader("Content-Length"));
            assertEquals(response.getHeader("Content-Length"), ""
                    + text.getBytes().length);
            assertNotNull(response.getHeader("ETag"));
        }

        // test callback

        request.setURI(entities.get(0).getId() + "?callback=js_cb");
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(200, response.getStatus());
        assertEquals("application/javascript", response.getContentType());

        assertNotNull(response.getContent());
        assertTrue(response.getContent().startsWith("js_cb("));
    }

    public void testGET_Javascript() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", testhost);
        request.setVersion("HTTP/1.0");

        for (DcsEntity entity : entities) {
            request.setURI(entity.getId() + "?callback=js_cb");

            response.parse(servletContainer.getResponses(request.generate()));

            assertEquals(200, response.getStatus());
            assertEquals("application/javascript", response.getContentType());

            String text = response.getContent();
            assertNotNull(text);
            assertTrue(response.getContent().startsWith("js_cb("));

            assertNotNull(response.getHeader("Content-Length"));
            assertEquals(response.getHeader("Content-Length"), ""
                    + text.getBytes().length);
            assertNotNull(response.getHeader("ETag"));
        }
    }
    
    public void testHead() throws Exception {
        HttpClient client = new HttpClient();

        for (DcsEntity entity : entities) {
            HeadMethod head = new HeadMethod(entity.getId());

            int status = client.executeMethod(head);
            assertEquals(200, status);

            assertEquals("application/xml", head
                    .getResponseHeader("Content-Type").getValue());
            assertTrue(head.getResponseContentLength() > 0);
            assertNotNull(head.getResponseHeader("ETag"));
        }

        HeadMethod head = new HeadMethod(entities.get(0).getId() + "blah");
        int status = client.executeMethod(head);
        assertEquals(404, status);
    }
}
