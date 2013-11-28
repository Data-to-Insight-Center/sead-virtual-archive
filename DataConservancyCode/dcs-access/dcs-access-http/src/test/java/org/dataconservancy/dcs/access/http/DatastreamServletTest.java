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

import java.io.File;
import java.io.FileOutputStream;

import java.util.Arrays;

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
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import junit.framework.TestCase;

public class DatastreamServletTest
        extends TestCase {

    private ServletTester servletContainer;

    private File solrhome;

    private File archivehome;

    private ArchiveStore archive;
    protected DcpIndexService indexService;
    private DcsFile ds_entity;

    private File ds_file;

    private byte[] ds_contents = new byte[] {'a', 'b', 'c', 'd', 'e', 'f', 'g'};
    private String dsbaseurl;

    private static ArchiveStore getTestArchiveStore(File baseDir) {
        FilePathKeyAlgorithm eAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 2, null);

        FilePathKeyAlgorithm mAlg =
                new KeyDigestPathAlgorithm("MD5", 1, 2, ".md");

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
   

    // TODO Figure out how to set mime type and test

    public void setUp() throws Exception {
        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        SolrService solr = new SolrService(solrhome);
        
        archivehome = FileUtil.createTempDir("archive");
        archive = getTestArchiveStore(archivehome);
        
        indexService = new DcpIndexService(archive, solr);
        indexService.clear();
        
        ds_file = File.createTempFile("datastream", null);

        FileOutputStream out = new FileOutputStream(ds_file);
        out.write(ds_contents);
        out.close();

        ds_entity = new DcsFile();
        ds_entity.setId("http://dcservice.dataconservancy.org:8080/dcs/entity/66");
        ds_entity.setSource(ds_file.toURI().toString());
        ds_entity.setSizeBytes(ds_contents.length);

        DcsFormat fmt = new DcsFormat();
        fmt.setFormat("application/xbox");
        fmt.setName("XBox");
        fmt.setVersion("1.0");
        fmt.setSchemeUri("http://www.iana.org/assignments/media-types/");

        ds_entity.addFormat(fmt);
        
        Dcp dcp = DcpUtil.add(null, ds_entity);
        archive.putPackage(DcpUtil.asInputStream(dcp));        
        index(dcp);
        
        servletContainer = new ServletTester();
        servletContainer.setContextPath("/access");
        servletContainer.addServlet(DatastreamServlet.class, "/datastream/*");
        servletContainer.getContext().getServletContext()
                .setAttribute("dcpquery.impl",
                              "org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService");
        dsbaseurl =  "/access/datastream/";
        servletContainer.getContext().getServletContext()
                .setAttribute("datastream.url", dsbaseurl);
        servletContainer.start();
    }

    public void tearDown() throws Exception {
        servletContainer.stop();

        if (solrhome != null) {
            FileUtil.delete(solrhome);
        }

        if (ds_file != null) {
            ds_file.delete();
        }
    }

    public void testGET() throws Exception {
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        request.setMethod("GET");
        request.setHeader("Host", "tester");
        request.setVersion("HTTP/1.0");

        request.setURI("/access/datastream/" + ServletUtil.encodeURLPath(ds_entity.getId()));

        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals("Status message was: '" + response.getReason() + "'", 200, response.getStatus());

        assertNotNull(response.getContent());
        assertTrue(Arrays.equals(ds_contents, response.getContent().getBytes()));

        assertNotNull(response.getHeader("Content-Type"));
        assertEquals("application/xbox", response.getHeader("Content-Type"));
        assertNotNull(response.getHeader("Content-Length"));
        assertEquals(response.getHeader("Content-Length"), ""
                + ds_entity.getSizeBytes());
        assertNotNull(response.getHeader("ETag"));
        
        request.setURI("/access/datastream/" + "doesnotexist");
        response.parse(servletContainer.getResponses(request.generate()));
        assertEquals(404, response.getStatus());
    }

    public void testHead() throws Exception {
        // ServletTester has internal errors processing head requests so use http client

        String baseurl = servletContainer.createSocketConnector(true);

        HttpClient client = new HttpClient();

        HeadMethod head =
                new HeadMethod(baseurl + "/access/datastream/"
                        + ds_entity.getId());
        int status = client.executeMethod(head);
        
        assertEquals(200, status);

        assertEquals(ds_entity.getSizeBytes(), head.getResponseContentLength());
        assertNotNull(head.getResponseHeader("ETag"));
        assertNotNull(head.getResponseHeader("Content-Type"));
        assertEquals("application/xbox", head.getResponseHeader("Content-Type").getValue());
        
        head = new HeadMethod(baseurl + "/access/datastream/doesnotexist");
        status = client.executeMethod(head);
        assertEquals(404, status);
    }
    
    protected void index(Dcp dcp) throws IndexServiceException,
            AIPFormatException {

        BatchIndexer<Dcp> batch = indexService.index();
        batch.add(dcp);
        batch.close();
        
        indexService.optimize();
    }
}
