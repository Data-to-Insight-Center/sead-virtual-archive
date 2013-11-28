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
package org.dataconservancy.dcs.query.dcpsolr;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

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
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;

/**
 * Setup a temporary solr server and archive store for each test. Set archive to
 * null to prevent entities and file contents being added to archive.
 */
public abstract class AbstractSearchTest extends TestCase {
    protected DcpIndexService index_service;
    protected DcsDataModelQueryService query_service;
    protected RandomEntityBuilder rb;
    protected SolrService solr;
    protected ArchiveStore archive;
    protected File archivehome;
    protected File solrhome;

    public void setUp() throws Exception {
        rb = new RandomEntityBuilder();

        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        solr = new SolrService(solrhome);

        archivehome = FileUtil.createTempDir("archive");
        archive = createTestArchiveStore(archivehome);
        index_service = new DcpIndexService(archive, solr);
        query_service = new DcsDataModelQueryService(solr);
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

    public void tearDown() throws IOException {
        if (index_service != null) {
            index_service.shutdown();
        }

        if (solrhome != null) {
            FileUtil.delete(solrhome);
        }

        if (archivehome != null) {
            FileUtil.delete(archivehome);
        }
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

    protected void index(DcsEntity... entities) throws IndexServiceException,
            AIPFormatException {
        index(DcpUtil.add(null, entities));
    }
}
