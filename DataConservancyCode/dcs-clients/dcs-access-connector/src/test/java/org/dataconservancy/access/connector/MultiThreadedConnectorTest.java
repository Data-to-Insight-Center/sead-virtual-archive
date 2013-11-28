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
package org.dataconservancy.access.connector;


import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class MultiThreadedConnectorTest extends HttpDcsConnectorTest {

    @Override
    protected DcsConnectorConfig getConnectorConfig() {
        final DcsConnectorConfig config = new DcsConnectorConfig();
        try {
            URL u = new URL(String.format(accessServiceUrl, testServer.getServiceHostName(), testServer.getServicePort()));
            config.setScheme(u.getProtocol());
            config.setHost(u.getHost());
            config.setPort(u.getPort());
            config.setContextPath(u.getPath());
        } catch (MalformedURLException e) {
            fail("Malformed DCS access http url: " + e.getMessage());
        }
        config.setMaxOpenConn(2);
        return config;
    }

    /**
     * Multiple requests in a row should be successful because the configuration allows more than one maximum open
     * connection at a time.
     *
     * @throws java.io.IOException
     * @throws DcsClientFault
     * @throws org.xml.sax.SAXException
     */
    @Test
    @Override
    public void testSingleConnectionReuseScenario() throws IOException, DcsClientFault, SAXException {
        assertTrue(new File(entitiesDir, "test1.xml").exists());
        assertTrue(config.getMaxOpenConn() > 1);
        for (int i = 0; i < config.getMaxOpenConn(); i++) {
            underTest.getStream(config.getAccessHttpUrl() + "/entity/test1");
        }
    }

    /**
     * Making a request beyond the connection limit without closing any streams should block.
     *
     * @throws java.io.IOException
     * @throws DcsClientFault
     * @throws org.xml.sax.SAXException
     * @throws InterruptedException
     */
    @Test
    public void testMultipleConnectionReuseScenarioBlocksOk() throws IOException, DcsClientFault, SAXException, InterruptedException {
        assertTrue(new File(entitiesDir, "test1.xml").exists());
        assertTrue(config.getMaxOpenConn() > 1);
        final String entityId = config.getAccessHttpUrl() + "/entity/test1";

        // Create request threads, purposefully creating one more request thread then there are maximum allowed connections
        final List<Thread> requestThreads = createTestRequestThreads(config.getMaxOpenConn() + 1, entityId);

        // Start the threads.
        for (Thread t : requestThreads) {
            t.start();
        }

        // Sleep for 3 seconds, ensuring the threads that are able to terminate, do
        Thread.sleep(3000);

        // Verify the state of the threads.  Exactly one thread should block in the WAIT state.
        boolean foundWait = false;
        for (int i = 0; i < requestThreads.size(); i++) {
            final Thread t = requestThreads.get(i);
            if (t.getState() != Thread.State.WAITING) {
                assertEquals(Thread.State.TERMINATED, t.getState());
            } else {
                assertFalse("Multiple request threads found in the WAIT state.  Only 1 expected.", foundWait);
                assertEquals(Thread.State.WAITING, t.getState());
                foundWait = true;
            }
        }
        assertTrue("Exactly one request thread should have been found in the WAIT state", foundWait);
    }
    
    @Test
    @Ignore("TODO")
    public void testConnectionLeakWhenHttpClientThrowsException() throws IOException, InterruptedException, HttpException {
        assertTrue("The entity test1.xml does not exist.", new File(entitiesDir, "test1.xml").exists());
        assertTrue(config.getMaxOpenConn() > 1);
        final String entityId = config.getAccessHttpUrl() + "/entity/test1";

        // TODO

    }

    private List<Thread> createTestRequestThreads(final int count, final String urlToGet) {
        final List<Thread> requestThreads = new ArrayList<Thread>();
        for (int i = 0; i < count; i++) {
            final Thread reqThread = new DcsConnectorRequestThread(underTest, urlToGet, false);
            requestThreads.add(reqThread);
        }

        return requestThreads;
    }

    private List<Thread> createTestRequestThreads(final int count, final String urlToGet, final HttpClient client, final boolean consumeResponse) {
        final List<Thread> requestThreads = new ArrayList<Thread>();
        for (int i = 0; i < count; i++) {
            final Thread reqThread = new HttpClientRequestThread(client, urlToGet, consumeResponse);
            requestThreads.add(reqThread);
        }
        return requestThreads;
    }

}
