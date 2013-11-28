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
import org.junit.runner.RunWith;

import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.api.BatchIndexer;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.dataconservancy.dcs.index.dcpsolr.SolrService;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollection;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * Reference implementation of simply indexing archived entries, deposited in
 * order.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
@ContextConfiguration(locations = {"classpath*:org/dataconservancy/config/applicationContext.xml"})
public class BaselineFunctionalityIT
        extends DcpSolrRebuildTest {

    /* XXX: Hack due to possible index bug */
    private static final boolean USE_ONE_BIG_PACKAGE_WORKAROUND = true;

    DcsXstreamStaxModelBuilder builder = new DcsXstreamStaxModelBuilder(true);

    private static DcsDataModelQueryService loadedQueryService;

    @Autowired
    private SolrService solr;

    @Autowired
    private ArchiveStore archive;

    @Autowired
    private OrderedDcpEntitySource source;

    @BeforeClass
    public static void createDcsHome() {
        DcsHome.prepareHome(BaselineFunctionalityIT.class);
    }

    @Override
    protected DcsDataModelQueryService doRebuild(OrderedDcpEntitySource source) {

        if (loadedQueryService != null) {
            return loadedQueryService;
        }

        assertTrue(builder.isValidating());

        try {

            DcpIndexService indexService = new DcpIndexService(archive, solr);

            IdRelationValidator validator = new IdRelationValidator();

            Dcp everythingInOnePackage = new Dcp();

            for (Dcp pkg : source.getPackages()) {

                BatchIndexer<Dcp> index = indexService.index();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                builder.buildSip(pkg, out);

                validator.validate(pkg);

                archive.putPackage(new ByteArrayInputStream(out.toByteArray()));

                if (USE_ONE_BIG_PACKAGE_WORKAROUND) {
                    addTo(everythingInOnePackage, pkg);
                } else {
                    index.add(pkg);
                    index.close();
                }
            }

            if (USE_ONE_BIG_PACKAGE_WORKAROUND) {
                BatchIndexer<Dcp> index = indexService.index();
                index.add(everythingInOnePackage);
                index.close();
            }

            loadedQueryService = new DcsDataModelQueryService(solr);
            return loadedQueryService;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SolrDocument lookupSolrDocument(String id) {
        try {
            return solr.lookupSolrDocument(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addTo(Dcp dcp, Dcp stuff) {
        for (DcsDeliverableUnit du : stuff.getDeliverableUnits()) {
            dcp.addDeliverableUnit(du);
        }

        for (DcsCollection coll : stuff.getCollections()) {
            dcp.addCollection(coll);
        }

        for (DcsManifestation man : stuff.getManifestations()) {
            dcp.addManifestation(man);
        }

        for (DcsFile file : stuff.getFiles()) {
            dcp.addFile(file);
        }

        for (DcsEvent event : stuff.getEvents()) {
            dcp.addEvent(event);
        }

    }

    @Override
    protected OrderedDcpEntitySource getEntitySource() {
        return source;
    }
}
