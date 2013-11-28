/*
 * Copyright 2013 The Trustees of Indiana University
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
package org.dataconservancy.dcs.query.dcpsolr;

import junit.framework.TestCase;
import org.dataconservancy.archive.api.AIPFormatException;
import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.archive.impl.elm.ElmArchiveStore;
import org.dataconservancy.archive.impl.elm.fs.FsEntityStore;
import org.dataconservancy.archive.impl.elm.fs.FsMetadataStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.api.IndexService;
import org.dataconservancy.dcs.index.api.IndexServiceException;
import org.dataconservancy.dcs.index.dcpsolr.*;
import org.dataconservancy.dcs.util.FilePathKeyAlgorithm;
import org.dataconservancy.dcs.util.KeyDigestPathAlgorithm;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.seadva.model.pack.ResearchObject;

import java.io.File;
import java.io.IOException;

/**
 * Setup a temporary solr server and archive store for each test. Set archive to
 * null to prevent entities and file contents being added to archive.
 */
public abstract class AbstractSearchTest extends TestCase {
    protected IndexService index_service;
    protected SeadDataModelQueryService query_service;
    protected SeadSolrService solr;    protected File solrhome;

    public void setUp() throws Exception {
        solrhome = FileUtil.createTempDir("solrhome");
        SolrService.createSolrInstall(solrhome);
        solr = new SeadSolrService(solrhome);

        index_service = new ROIndexService( solr);
        query_service = new SeadDataModelQueryService(solr);
    }



    public void tearDown() throws IOException, IndexServiceException {
        if (index_service != null) {
            index_service.shutdown();
        }

        if (solrhome != null) {
            FileUtil.delete(solrhome);
        }
    }

    protected void index(ResearchObject dcp) throws IndexServiceException,
            AIPFormatException {
        BatchIndexer<ResearchObject> batch = index_service.index();
        batch.add(dcp);
        batch.close();

        index_service.optimize();
    }


}
