/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.dcs.integration.main;

import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.integration.bootstrap.MetadataFormatRegistryBootstrap;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Insures the proper behavior of the MetadataFormatRegistryBootstrap class.
 */
public class MetadataFormatRegistryBootstrapIT {

    private static final String[] EMPTY_QUERY_PARAMS = new String[] {};

    private static final String duQuery = SolrQueryUtil.createLiteralQuery("entityType", "DeliverableUnit");

    private static ApplicationContext appContext;

    private static MetadataFormatRegistryBootstrap underTest;

    private static DcsDataModelQueryService queryService;

    @BeforeClass
    public static void beforeClass() {
        appContext =
                new ClassPathXmlApplicationContext("/WEB-INF/applicationContext.xml",
                        "classpath*:org/dataconservancy/config/applicationContext.xml",
                        "classpath*:org/dataconservancy/model/config/applicationContext.xml",
                        "classpath*:org/dataconservancy/mhf/config/applicationContext.xml",
                        "classpath*:org/dataconservancy/registry/config/applicationContext.xml");

        assertNotNull("Unable to obtain application context.", appContext);

        underTest = appContext.getBean("metadataFormatBootstrap", MetadataFormatRegistryBootstrap.class);

        assertNotNull("Unable to obtain metadataFormatBootstrap bean.", underTest);

        queryService = appContext.getBean("org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService",
                DcsDataModelQueryService.class);

        assertNotNull("Unable to obtain the query service.", queryService);

    }

    /**
     * Insures that if the MetadataFormatRegistryBootstrap is invoked twice, that it won't put duplicate registry
     * entries in the archive.
     *
     * @throws Exception
     */
    @Test
    public void testPopulateDuplicateEntries() throws Exception {

        // Currently the bootstrapper is invoked when the Spring container is started, so we cannot test for an
        // empty archive, and it is pointless to invoke the boostrapper before we know how many DeliverableUnits
        // are in the archive.
//        // Assert the archive is empty
//        QueryResult result = queryService.query(duQuery, 0, 1, EMPTY_QUERY_PARAMS);
//        assertEquals(0, result.getTotal());
//
//        // bootstrap the archive with registry entries
//        underTest.bootstrapFormats();

        // The archive shouldn't be empty.  Record the number of DUs found.
        QueryResult result = queryService.query(duQuery, 0, 1, EMPTY_QUERY_PARAMS);
        final long archiveDuCount = result.getTotal();
        assertTrue(archiveDuCount > 1);

        // call the bootstrap again.  The number of the DUs in the archive shouldn't change, because the
        // registry entries have already been bootstrapped.
        underTest.bootstrapFormats();

        assertEquals(archiveDuCount, queryService.query(duQuery, 0, 1, EMPTY_QUERY_PARAMS).getTotal());
    }
}
