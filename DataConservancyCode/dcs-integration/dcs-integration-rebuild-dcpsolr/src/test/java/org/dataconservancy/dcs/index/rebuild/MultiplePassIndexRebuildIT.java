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
package org.dataconservancy.dcs.index.rebuild;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.solr.common.SolrDocument;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.index.rebuild.dcpsolr.MultiplePassIndexRebuild;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.transform.execution.BasicExecutionEnvironment;
import org.dataconservancy.transform.execution.run.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ContextConfiguration(locations = {"classpath*:org/dataconservancy/config/applicationContext.xml"})
public class MultiplePassIndexRebuildIT
        extends DcpSolrRebuildTest {

    @Autowired
    private SolrService solr;

    @Autowired
    private ArchiveStore archive;

    @Autowired
    private OrderedDcpEntitySource source;

    @Autowired
    private BasicExecutionEnvironment execution;

    @Autowired
    private MultiplePassIndexRebuild<String, Dcp> rebuildConfig;

    private static DcsDataModelQueryService loadedQueryService;

    @BeforeClass
    public static void createDcsHome() {
        DcsHome.prepareHome(MultiplePassIndexRebuildIT.class);
    }

    @Override
    protected DcsDataModelQueryService doRebuild(OrderedDcpEntitySource src) {

        if (loadedQueryService != null) {
            return loadedQueryService;
        }

        System.out.println("LOADING");
        try {
            for (Dcp pkg : source.getPackages()) {

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                builder.buildSip(pkg, out);

                archive.putPackage(new ByteArrayInputStream(out.toByteArray()));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("DOING REBUILD");
        Runner rebuilder = new Runner(rebuildConfig, execution);
        rebuilder.run();

        loadedQueryService = new DcsDataModelQueryService(solr);

        return loadedQueryService;
    }

    @Override
    protected SolrDocument lookupSolrDocument(String id) {
        try {
            return solr.lookupSolrDocument(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected OrderedDcpEntitySource getEntitySource() {
        return source;
    }

}
