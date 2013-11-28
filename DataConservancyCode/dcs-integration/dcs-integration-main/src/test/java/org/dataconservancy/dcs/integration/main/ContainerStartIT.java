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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.dataconservancy.dcs.integration.support.Interpolator.interpolate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A simple IT which pings each endpoint and tests for a successful HTTP code.  This indicates
 * that the container started.
 */
public class ContainerStartIT {

    private final static Properties props = new Properties();

    private final static Logger log = LoggerFactory.getLogger(ContainerStartIT.class);

    private final HttpClient client = new DefaultHttpClient();

    private final String baseUrl = interpolate(new StringBuilder("${dcs.baseurl}"), 0, props).toString();


    @BeforeClass
    public static void loadProperties() throws IOException {
        final URL defaultProps = ContainerStartIT.class.getResource("/default.properties");
        assertNotNull("Could not resolve /default.properties from the classpath.", defaultProps);
        assertTrue("default.properties does not exist.", new File(defaultProps.getPath()).exists());
        props.load(defaultProps.openStream());
    }

    @Test
    public void testContainerUrl() throws IOException {
        final String url = baseUrl;
        assertHttpResponse(new HttpGet(url), 200, "Unable to connect to " + url +
                ": Did the container start successfully?");
    }

    /**
     * Test to ensure that the SWORD service document is returned:
     * http://localhost:8080/dcs-integration-main/deposit/
     */
    @Test
    public void testSwordDepositServiceDocument() throws IOException {
        final String url = baseUrl + "/deposit/";
        assertHttpResponse(new HttpGet(url), 200, "Unable to retrieve atompub service document " + url);
    }

    /**
     * Test to ensure that the SWORD SIP submission endpoint is up:
     * http://localhost:8080/dcs-integration-main/deposit/sip
     */
    @Test
    public void testSwordDepositSipEndpoint() throws IOException {
        final String url = baseUrl + "/deposit/sip";
        assertHttpResponse(new HttpGet(url), 405, "Unable to retrieve sip deposit endpoint " + url);
        final HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity("<dcp xmlns=\"http://dataconservancy.org/schemas/dcp/1.0\"></dcp>"));
        assertHttpResponse(post, 202, "Unable to perform a DCP sip deposit at endpoint " + url);
    }

    /**
     * Test to ensure that the SWORD File submission endpoint is up:
     * http://localhost:8080/dcs-integration-main/deposit/file
     */
    @Test
    public void testSwordDepositFileEndpoint() throws IOException {
        final String url = baseUrl + "/deposit/file";
        assertHttpResponse(new HttpGet(url), 405, "Unable to retrieve file deposit endpoint " + url);
        final HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity("Hello world."));
        assertHttpResponse(post, 202, "Unable to perform a file deposit at endpoint " + url);
    }

    /**
     * Test to ensure that the entity retrieval endpoint is up:
     * http://localhost:8080/dcs-integration-main/entity/
     */
    @Test
    @Ignore
    public void testEntityEndpoint() throws IOException {
        final String url = baseUrl + "/entity/";
        assertHttpResponse(new HttpGet(url), 200, "Unable to retrieve entity endpoint " + url);
    }

    /**
     * Test to ensure that the datastream endpoint is up:
     * http://localhost:8080/dcs-integration-main/datastream/
     */
    @Test
    @Ignore
    public void testDatastreamEndpoint() throws IOException {
        final String url = baseUrl + "/datastream/";
        assertHttpResponse(new HttpGet(url), 200, "Unable to retrieve datastream endpoint " + url);
    }

    /**
     * Test to ensure that the query endpoint is up:
     * http://localhost:8080/dcs-integration-main/query/
     */
    @Test
    @Ignore
    public void testQueryEndpoint() throws IOException {
        final String url = baseUrl + "/query/";
        assertHttpResponse(new HttpGet(url), 200, "Unable to retrieve query endpoint " + url);
    }

    private void assertHttpResponse(HttpUriRequest req, int expectedResponse) throws IOException {
        assertHttpResponse(req, expectedResponse, null);
    }

    private void assertHttpResponse(HttpUriRequest req, int expectedResponseLow, int expectedResponseHigh) throws IOException {
        assertHttpResponse(req, expectedResponseLow, expectedResponseHigh, null);
    }

    private void assertHttpResponse(HttpUriRequest req, int expectedResponse, String failMessage) throws IOException {
        final HttpResponse resp = client.execute(req);
        assertEquals(failMessage, expectedResponse, resp.getStatusLine().getStatusCode());
        resp.getEntity().consumeContent();
    }

    private void assertHttpResponse(HttpUriRequest req, int expectedResponseLow, int expectedResponseHigh, String failMessage) throws IOException {
        final HttpResponse resp = client.execute(req);
        final int actualStatusCode = resp.getStatusLine().getStatusCode();
        assertTrue(failMessage, expectedResponseLow <= actualStatusCode && expectedResponseHigh >= actualStatusCode);
        resp.getEntity().consumeContent();
    }
}
