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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import junit.framework.TestCase;

public class QueryServletTest
        extends TestCase {

   
    private ServletTester servletContainer;

    private File solrhome;
    protected DcpIndexService index_service;
    protected SolrService solr;
    protected ArchiveStore archive;
    protected File archivehome;
    
    private Dcp dcp;

    public void setUp() throws Exception {
        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        solr = new SolrService(solrhome);
       
        archivehome = FileUtil.createTempDir("archive");
        archive = createTestArchiveStore(archivehome);
        index_service = new DcpIndexService(archive, solr);

        RandomEntityBuilder rb = new RandomEntityBuilder();
        DcsDeliverableUnit du = rb.createDeliverableUnit(null, null, false);

        du.setTitle("blah");

        List<DcsFile> files = new ArrayList<DcsFile>();
        DcsManifestation man = rb.createManifestation(du.getId(), files);

        dcp = new Dcp();
        
        dcp.addDeliverableUnit(du);
        dcp.addManifestation(man);
        dcp.addEvent(rb.createEvent(null));
        dcp.addCollection(rb.createCollection(null));
        for( DcsFile file : files) {
            dcp.addFile(file);
        }
        
        index(dcp);
        
        servletContainer = new ServletTester();
        servletContainer.setContextPath("/access");
        servletContainer.addServlet(QueryServlet.class, "/query/*");
        servletContainer
                .getContext()
                .getServletContext()
                .setAttribute("dcpquery.impl",
                              "org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService");
        String dsbaseurl = "/access/datastream";
        servletContainer.getContext().getServletContext()
                .setAttribute("datastream.url", dsbaseurl);
        servletContainer.start();
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
        
        BatchIndexer<Dcp> batch = index_service.index();
        batch.add(dcp);
        batch.close();
        
        index_service.optimize();
    }
    
    public void tearDown() throws Exception {
        servletContainer.stop();
        
        if( solrhome != null ) {
            FileUtil.delete(solrhome);
        }
        
        if( archivehome != null ) {
            FileUtil.delete(archivehome);
        }
    }

    public void testGET_Dcp() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        DcsModelBuilder mb = new DcsXstreamStaxModelBuilder();

        Collection<DcsFile> files = dcp.getFiles();

        request.setURI("/access/query/"
                + ServletUtil.encodeURLPath("entityType:File"));

        response.parse(servletContainer.getResponses(request.generate()));
        
        assertEquals(200, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        assertNotNull(response.getContent());
        assertNotNull(response.getHeader("X-TOTAL-MATCHES"));

        assertEquals("" + files.size(), response.getHeader("X-TOTAL-MATCHES"));

        Dcp dcp =
                mb.buildSip(new ByteArrayInputStream(response.getContent()
                        .getBytes()));

        assertEquals(files.size(), dcp.getFiles().size());

        request.setURI("/access/entity/" + ServletUtil.encodeURLPath(":blah:"));
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());

        request
                .setURI("/access/entity/"
                        + ServletUtil
                                .encodeURLPath("title:(alksdfasd-axda+!E@!QW@#$!+@!#--~+))"));
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());

        request.setURI("/access/entity/"
                + ServletUtil.encodeURLPath("title:test\\-escape)"));
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());

        request
                .setURI("/access/entity/"
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

        Collection<DcsDeliverableUnit> dus = dcp.getDeliverableUnits();

        request.setHeader("Accept", "application/javascript");
        request
                .setURI("/access/query/"
                        + ServletUtil
                                .encodeURLPath("entityType:DeliverableUnit AND title:blah")
                        + "?callback=js_cb");

        response.parse(servletContainer.getResponses(request.generate()));

        assertEquals(200, response.getStatus());
        assertEquals("application/javascript", response.getContentType());
        assertNotNull(response.getContent());
        assertNotNull(response.getHeader("X-TOTAL-MATCHES"));
        assertTrue(response.getContent().startsWith("js_cb("));

        assertEquals("" + dus.size(), response.getHeader("X-TOTAL-MATCHES"));
    }

    public void testHEAD() throws Exception {
        Collection<DcsFile> files = dcp.getFiles();

        // ServletTester has internal errors processing head requests so use http client
        // request.setMethod("GET");
        String baseurl = servletContainer.createSocketConnector(true);

        HttpClient client = new HttpClient();

        HeadMethod head =
                new HeadMethod(baseurl + "/access/query/"
                        + ServletUtil.encodeURLPath("entityType:File"));
        int status = client.executeMethod(head);
        assertEquals(200, status);

        assertEquals("application/xml", head.getResponseHeader("Content-Type")
                .getValue());
        assertEquals("" + files.size(), head
                .getResponseHeader("X-TOTAL-MATCHES").getValue());

        head = new HeadMethod(baseurl + "/access/query/badfield:");
        status = client.executeMethod(head);
        assertEquals(404, status);
    }
    
    public void testJavascriptHighlight() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        request.setURI("/access/query/"
                + ServletUtil.encodeURLPath("title:blah") + "?callback=test&_hl=true&_hl.requireFieldMatch=true&_hl.fl=*");

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
