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

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import org.junit.After;
import org.junit.Before;

import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Setup a temporary solr server and archive store for each test. Set archive to
 * null to prevent entities and file contents being added to archive.
 */
public abstract class AbstractIndexTest extends TestCase {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected DcpIndexService service;
    protected RandomEntityBuilder rb;
    protected SolrService solr;
    protected ArchiveStore archive;
    protected File archivehome;
    protected File solrhome;

    /**
     * Stores the existing value, if any, of the 'solr.solr.home' property.
     */
    private String originalSystemSolrHome = "_FOO_";

    @Before
    public void setUp() throws Exception {
        rb = new RandomEntityBuilder();

        // Keep the existing solr.solr.home property value, if any.
        if (System.getProperties().containsKey("solr.solr.home")) {
            originalSystemSolrHome = System.getProperty("solr.solr.home");
        }

        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        System.setProperty("solr.solr.home", solrhome.getCanonicalPath());
        solr = new SolrService(solrhome);

        archivehome = FileUtil.createTempDir("archive");
        archive = createTestArchiveStore(archivehome);

        service = new DcpIndexService(archive, solr);
    }

    @After
    public void tearDown() throws IOException {
        if (service != null) {
            service.shutdown();
        }

        if (solrhome != null) {
            FileUtil.delete(solrhome);
        }

        // Clean up the pollution of System properties
        if (System.getProperties().containsKey("solr.solr.home")) {
            System.getProperties().remove("solr.solr.home");
        }

        // Reset solr.solr.home, if it existed previously.
        if (!"_FOO_".equals(originalSystemSolrHome)) {
            System.setProperty("solr.solr.home", originalSystemSolrHome);
        } else {
            System.getProperties().remove("solr.solr.home");
        }
        
        if (archivehome != null) {
            FileUtil.delete(archivehome);
        }

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

    protected boolean equals(Collection<Object> fieldvalues, String... correct) {
        HashSet<String> correct_set = new HashSet<String>();
        Collections.addAll(correct_set, correct);

        // System.out.println("In field: " + fieldvalues);
        // System.out.println("Should be: " + correct_set);

        return fieldvalues.equals(correct_set);
    }

    protected boolean hasFieldValues(String id, String field, String... values)
            throws IOException, SolrServerException {
        SolrDocument doc = solr.lookupSolrDocument(id);
        assertNotNull(doc);

        // System.out.println("____ " + field);
        //
        // for (String name : doc.getFieldNames()) {
        // System.out.println(name + " -> " + doc.getFieldValue(name));
        // }

        Collection<Object> fieldvalues = doc.getFieldValues(field);

        if (fieldvalues == null) {
            fieldvalues = new HashSet<Object>();
        }

        return equals(new HashSet<Object>(fieldvalues), values);
    }

    protected boolean hasFieldContainingSubstring(String id, String field,
            String value) throws IOException, SolrServerException {
        SolrDocument doc = solr.lookupSolrDocument(id);
        assertNotNull(doc);

        Collection<Object> fieldvalues = doc.getFieldValues(field);

        // System.out.println("____ " + field);
        //
        // for (String name : doc.getFieldNames()) {
        // System.out.println(name);
        // }

        if (fieldvalues == null) {
            return false;
        }

        for (Object o : fieldvalues) {
            // System.out.println(field + " -> " + o);

            if (o.toString().contains(value)) {
                return true;
            }
        }

        return false;
    }

    protected void index(Dcp dcp) throws IndexServiceException,
            AIPFormatException {
        // Package must be in archive so indexing service can lookup files

        if (archive != null) {
            archive.putPackage(DcpUtil.asInputStream(dcp));
        }

        BatchIndexer<Dcp> batch = service.index();
        batch.add(dcp);
        batch.close();
        
        service.optimize();
    }

    protected void index(DcsEntity... entities) throws IndexServiceException,
            AIPFormatException {
        index(DcpUtil.add(null, entities));
    }
}
