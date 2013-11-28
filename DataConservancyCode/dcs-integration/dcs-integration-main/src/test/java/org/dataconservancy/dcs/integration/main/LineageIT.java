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
package org.dataconservancy.dcs.integration.main;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.dataconservancy.dcs.ingest.client.DepositBuilder;
import org.dataconservancy.dcs.ingest.client.DepositClient;
import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dataconservancy.dcs.integration.support.Interpolator.interpolate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The behaviors of the Lineage Service and HTTP interface are well-tested.  See the unit and integration tests
 * in the dcs-lineage-* modules, and the dcs-integration-lineage module.  The purpose of this test is to insure
 * that the production Spring wiring is working, especially Spring Web MVC components: handler mappings, etc.
 * <p/>
 * So the actual value of a response is not as important as it is to obtain the proper HTTP status code back.
 */
public class LineageIT {

    private final static Properties props = new Properties();

    /** Base URL of the DCS; e.g. http://dataconservancy.org/dcs */
    private static String baseUrl;

    /** Lineage API URL endpoint; e.g. http://dataconservancy.org/dcs/lineage */
    private static String lineageUrl;

    /** The Spring application context */
    private static ClassPathXmlApplicationContext appContext;

    /** The DCS Model Builder */
    private static DcsModelBuilder modelBuilder;

    /** The HTTP client used to execute requests against the Lineage endpoint */
    private static HttpClient httpClient;

    /** The Deposit Client used to perform deposits and interrogate the deposit status */
    private static DepositClient depositClient;

    /** The initial (prior to deposit) identifier of the DCS entity */
    private static final String duId = "du/1";

    /** The archive (after deposit) identifier of the DCS entity */
    private static String dcsDuId;

    /** The lineage established after depositing the DCS entity */
    private static String lineageId;

    @BeforeClass
    public static void beforeClass() {
         appContext =
                new ClassPathXmlApplicationContext(new String[] {
                        "depositClientContext.xml", "classpath*:org/dataconservancy/config/applicationContext.xml"});

        httpClient = appContext.getBean("httpClient", HttpClient.class);

        modelBuilder = appContext.getBean("dcsModelBuilder", DcsModelBuilder.class);

        depositClient = appContext.getBean("depositClient", DepositClient.class);
    }

    /**
     * Loads properties to set up the test, deposit a DU to establish a lineage, and retrieve the deposited DU.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws InvalidXmlException
     */
    @BeforeClass
    public static void loadProperties() throws IOException, InterruptedException, InvalidXmlException {
        // Load properties
        final URL defaultProps = LineageIT.class.getResource("/default.properties");
        assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
        baseUrl = interpolate(new StringBuilder("${dcs.baseurl}/"), 0, props).toString();
        lineageUrl = baseUrl + "lineage";

        // Establish a lineage by depositing a single object
        Dcp toDeposit = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setId(duId);
        du.setTitle("Deliverable Unit Title");
        toDeposit.addEntity(du);

        // Perform the deposit
        DepositBuilder depositBuilder = depositClient.buildDeposit(toDeposit);
        String id = depositBuilder.execute();
        Thread.sleep(10000);
        DepositInfo info = depositClient.getDepositInfo(id);
        assertTrue(info.hasCompleted());
        assertTrue(info.isSuccessful());

        // Get the minted DCS id from the deposit feed, and retrieve the
        // deposited DU
        dcsDuId = getDcsIdFromDepositedId(duId, info);
        assertNotNull("Unable to determine deposited entity id", dcsDuId);
        DcsDeliverableUnit depositedDu = (DcsDeliverableUnit) getEntity(dcsDuId);
        assertNotNull(depositedDu);
        assertNotNull(depositedDu.getLineageId());
        lineageId = depositedDu.getLineageId();
    }

    /**
     * Attempt to retrieve a non-existent lineage, expect a 404.
     *
     * @throws Exception
     */
    @Test
    public void testGetNonExistentLineage() throws Exception {
        HttpGet request = new HttpGet(lineageUrl + "/1234");
        HttpResponse response = httpClient.execute(request);
        assertEquals(404, response.getStatusLine().getStatusCode());
        response.getEntity().getContent().close();
    }

    /**
     * Attempt to retrieve the lineage, expect a 200.
     * 
     * @throws Exception
     */
    @Test
    public void testGetLineageOk() throws Exception {
        // Retrieve the lineage, and de-serialize it to a DCP.
        HttpGet request = new HttpGet(lineageId);
        HttpResponse response = httpClient.execute(request);
        assertEquals("Unable to retrieve lineage " + lineageId,
                200, response.getStatusLine().getStatusCode());
        final InputStream content = response.getEntity().getContent();
        Dcp lineageDcp;
        try {
            lineageDcp = modelBuilder.buildSip(content);
        } finally {
            content.close();
        }
        assertNotNull("Expected a lineage DCP to be created for lineage " + lineageId, lineageDcp);
        assertEquals("Expected a single DU in the lineage", 1, lineageDcp.getDeliverableUnits().size());
    }

    /**
     * Attempt to search for the lineage using the lineage id, expect a 200.
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchUsingLineageUrl() throws Exception {
        // Insure we're using a URL
        new URL(lineageId);

        String searchUrl = lineageUrl + "/search?id=" + lineageId;
        HttpResponse response = httpClient.execute(new HttpGet(searchUrl));
        assertEquals("Unable to find lineage " + lineageId,
                200, response.getStatusLine().getStatusCode());
        final InputStream content = response.getEntity().getContent();
        Dcp lineageDcp;
        try {
            lineageDcp = modelBuilder.buildSip(content);
        } finally {
            content.close();
        }
        assertNotNull("Expected a lineage DCP to be created for lineage " + lineageId, lineageDcp);
        assertEquals("Expected a single DU in the lineage", 1, lineageDcp.getDeliverableUnits().size());
    }

    /**
     * Attempt to search for the lineage using the entity id, expect a 200.
     *
     * @throws Exception
     */
    @Test
    public void testHandleSearchUsingEntityUrl() throws Exception {
        // Insure we're using a URL
        new URL(dcsDuId);

        String searchUrl = lineageUrl + "/search?id=" + dcsDuId;
        HttpResponse response = httpClient.execute(new HttpGet(searchUrl));
        assertEquals("Unable to find lineage " + lineageId + " using the entity id " + dcsDuId,
                200, response.getStatusLine().getStatusCode());
        final InputStream content = response.getEntity().getContent();
        Dcp lineageDcp;
        try {
            lineageDcp = modelBuilder.buildSip(content);
        } finally {
            content.close();
        }
        assertNotNull("Expected a lineage DCP to be created for lineage " + lineageId, lineageDcp);
        assertEquals("Expected a single DU in the lineage", 1, lineageDcp.getDeliverableUnits().size());
    }

    /**
     * Obtain the identified entity.
     *
     * @param entityId the DCS entity ID to retrieve
     * @return the DcsEntity
     * @throws IOException
     * @throws InvalidXmlException
     */
    private static DcsEntity getEntity(String entityId) throws IOException, InvalidXmlException {
        HttpGet request = new HttpGet(entityId);
        HttpResponse response = httpClient.execute(request);
        assertEquals("Unable to retrieve entity " + entityId, 200, response.getStatusLine().getStatusCode());
        return modelBuilder.buildSip(response.getEntity().getContent()).iterator().next();
    }

    /**
     * Map the ID of the deposited DCS entity to its archived identity.
     *
     * @param depositedId the ID of the DCS entity in the deposited DCP package
     * @param info the DepositInfo, the result of a successful deposit
     * @return the archival ID of the DCS entity.
     * @throws IOException
     */
    private static String getDcsIdFromDepositedId(String depositedId, DepositInfo info) throws IOException {
        // <eventDetail>Assigned identifier 'http://localhost:8080/dcs/entity/4' to DeliverableUnit 'du/1'</eventDetail>
        Pattern p = Pattern.compile(".*<eventDetail>Assigned identifier '(.*)' to DeliverableUnit '" + depositedId + "'</eventDetail>.*");

        String depositContent = IOUtils.toString(info.getDepositContent().getInputStream());

        Matcher m = p.matcher(depositContent);

        assertTrue("Pattern '" + p.pattern() + "' didn't match '" + depositContent + "'", m.matches());
        return m.group(1);
    }

}
