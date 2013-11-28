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

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 *
 */
public class HttpDcsSearchIteratorTest extends AbstractHttpConnectorTest {

    private HttpDcsSearchIterator searchItr;
    private final int EXPECTED_ENTITY_COUNT = 134;

    @Before
    public void setUpItr() {
        searchItr = new HttpDcsSearchIterator(new DefaultHttpClient(), config,
                new DcsXstreamStaxModelBuilder(), "this can be any query");
    }

    @Override
    protected DcsConnectorConfig getConnectorConfig() {
        DcsConnectorConfig config = new DcsConnectorConfig();
        try {
            URL u = new URL(String.format(accessServiceUrl, testServer.getServiceHostName(), testServer.getServicePort()));
            config.setScheme(u.getProtocol());
            config.setHost(u.getHost());
            config.setPort(u.getPort());
            config.setContextPath(u.getPath());
            config.setMaxOpenConn(1);
        } catch (MalformedURLException e) {
            fail("Unable to construct connector configuration: " + e.getMessage());
        }

        return config;
    }

    @Test
    public void simpleSearchOk() {
        searchItr.hasNext();
        assertEquals(EXPECTED_ENTITY_COUNT, searchItr.getTotalHits());
    }

    @Test
    public void simpleIteration() {
        searchItr.hasNext();
        assertEquals(EXPECTED_ENTITY_COUNT, searchItr.getTotalHits());
        int count = 0;
        while (searchItr.hasNext()) {
            searchItr.next();
            count++;
        }

        assertEquals(EXPECTED_ENTITY_COUNT, count);
    }

    @Test
    public void testHasNextWhenExceptionThrownRefreshingIterator() throws IOException, InvalidXmlException {
        HttpDcsSearchIterator itrSpy = spy(searchItr);
        doThrow(new IOException()).when(itrSpy).refreshCurrentIterator();
        assertFalse(itrSpy.hasNext());
        assertNull(itrSpy.getCurrentItr());
    }
    
    @Test
    public void testOffsetIteration(){
        searchItr = new HttpDcsSearchIterator(new DefaultHttpClient(), config,
                                              new DcsXstreamStaxModelBuilder(), "this can be any query", -1, 10);
        searchItr.hasNext();
        assertEquals(EXPECTED_ENTITY_COUNT-10, searchItr.getTotalHits());
        int count = 0;
        while (searchItr.hasNext()) {
            searchItr.next();
            count++;
        }

        assertEquals(EXPECTED_ENTITY_COUNT-10, count);
    }
    
    @Test 
    public void testLimitedReturnedResultsIteration(){
        searchItr = new HttpDcsSearchIterator(new DefaultHttpClient(), config,
                                             new DcsXstreamStaxModelBuilder(), "this can be any query", 10, 0);
        searchItr.hasNext();

        assertEquals(10, searchItr.getTotalHits());
        int count = 0;
        while (searchItr.hasNext()) {
            searchItr.next();
            count++;
        }

        assertEquals(10, count);
    }

}
