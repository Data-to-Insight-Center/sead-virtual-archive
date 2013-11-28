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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import org.custommonkey.xmlunit.XMLAssert;

import org.junit.Test;

import org.xml.sax.SAXException;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class HttpDcsConnectorTest extends AbstractHttpConnectorTest {

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

    /**
     * Basic test ensuring we can get a stream back.
     *
     * @throws IOException
     * @throws DcsClientFault
     * @throws SAXException
     */
    @Test
    public void testSimpleStreamOk() throws IOException, DcsClientFault, SAXException {
        assertTrue(new File(entitiesDir, "test1.xml").exists());
        final InputStream response = underTest.getStream(config.getAccessHttpUrl() + "/entity/test1");
        assertNotNull(response);
        XMLAssert.assertXMLEqual(IOUtils.toString(new FileInputStream(new File(entitiesDir, "test1.xml"))), IOUtils.toString(response));
    }

    /**
     * Ensures that requesting a non existant entity results in a DcsClientFault (a 404)
     *
     * @throws IOException
     * @throws DcsClientFault
     * @throws SAXException
     */
    @Test(expected = DcsClientFault.class)
    public void testGetNonExistantStreamOk() throws IOException, DcsClientFault, SAXException {
        assertFalse(new File(entitiesDir, "foo.xml").exists());
        underTest.getStream(config.getAccessHttpUrl() + "/entity/foo");
    }

    /**
     * Two requests in a row will result in a DcsClientFault (underlying exception is an IllegalStateException) because
     * the default HttpClient connection manager only allows one connection to be in use at a time.  Because we haven't
     * read the response from the first request, the second request throws the ISE because the first requests' connection
     * is still in use.
     *
     * @throws IOException
     * @throws DcsClientFault
     * @throws SAXException
     */
    @Test(expected = DcsClientFault.class)
    public void testSingleConnectionReuseScenario() throws IOException, DcsClientFault, SAXException {
        assertTrue(new File(entitiesDir, "test1.xml").exists());
        assertTrue(new File(entitiesDir, "test2.xml").exists());
        underTest.getStream(config.getAccessHttpUrl() + "/entity/test1");
        underTest.getStream(config.getAccessHttpUrl() + "/entity/test2");
    }

    /**
     * Here we properly perform the requests.  The first request is issued, and its response is exhausted by IOUtils
     * (the underlying connection is freed by reading the stream), and then perform the second request.
     *
     * @throws IOException
     * @throws DcsClientFault
     * @throws SAXException
     */
    @Test
    public void testSingleConnectionReuseScenarioOk() throws IOException, DcsClientFault, SAXException {
        assertTrue(new File(entitiesDir, "test1.xml").exists());
        assertTrue(new File(entitiesDir, "test2.xml").exists());
        final InputStream stream = underTest.getStream(config.getAccessHttpUrl() + "/entity/test1");
        IOUtils.copy(stream, new NullOutputStream());
        underTest.getStream(config.getAccessHttpUrl() + "/entity/test2");
    }

    /**
     * Here we properly perform the requests.  The first request is issued, and its response is exhausted by the Model Builder.
     * BUT, we need to close the stream in order to free the connection, and then perform the second request.
     *
     * @throws IOException
     * @throws DcsClientFault
     * @throws SAXException
     * @throws org.dataconservancy.model.builder.InvalidXmlException
     */
    @Test
    public void testSingleConnectionReuseScenarioOkWithModelBuilder() throws IOException, DcsClientFault, SAXException, InvalidXmlException {
        assertTrue(new File(entitiesDir, "test1.xml").exists());
        assertTrue(new File(entitiesDir, "test2.xml").exists());
        final InputStream stream = underTest.getStream(config.getAccessHttpUrl() + "/entity/test1");
        mb.buildSip(stream);
        stream.close();
        underTest.getStream(config.getAccessHttpUrl() + "/entity/test2");
    }

    @Test
    public void testSimpleSearchOk() throws DcsClientFault {
        Iterator<DcsEntity> result = underTest.search("this can be any query");
        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }

        assertEquals(134, count);
    }
    
    @Test
    public void testSearchLimitedResults() throws DcsClientFault {
        //Return just the first 10 results
        Iterator<DcsEntity> result = underTest.search("query", 10, 0);
        int count = 0;
        while (result.hasNext()){
            result.next();
            count++;
        }
        
        assertEquals(10, count);
    }
    
    @Test
    public void testSearchOffsetResults() throws DcsClientFault {
        
        //Search for unlimited results offset to 124 into the list
        Iterator<DcsEntity> result = underTest.search("query", -1, 124);
        int count = 0;
        while (result.hasNext()){
            result.next();
            count++;
        }
        
        assertEquals(10, count);
    }
    

    @Test
    public void testSearchGetStreamOk() throws DcsClientFault, IOException {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        final Iterator<DcsEntity> result = underTest.search("this can be any query");
        int count = 0;
        while (result.hasNext()) {
            try {
                final InputStream stream = underTest.getStream(result.next().getId().replace("http://localhost:8080", config.getAccessHttpUrl().toString()));
                assertNotNull(stream);
                // exhaust the stream to free up connection
                IOUtils.copy(stream, nullOutputStream);
                count++;
            } catch (DcsClientFault dcsClientFault) {
//                log.debug(dcsClientFault.getMessage());
            } catch (IOException e) {
//                log.debug(e.getMessage());
            }
        }

        assertEquals(126, count);
    }

    @Test
    public void testSearchGetStreamAndBuildSipOk() throws DcsClientFault, IOException, InvalidXmlException {
        final Iterator<DcsEntity> result = underTest.search("this can be any query");
        int count = 0;
        while (result.hasNext()) {
            try {
                final InputStream stream = underTest.getStream(result.next().getId().replace("http://localhost:8080", config.getAccessHttpUrl().toString()));
                assertNotNull(stream);
                // exhaust and close the stream to free up connection
                mb.buildSip(stream);
                stream.close();
                count++;
            } catch (DcsClientFault dcsClientFault) {
                log.debug(dcsClientFault.getMessage());
            } catch (InvalidXmlException e) {
                log.debug(e.getMessage());
            } catch (IOException e) {
                log.debug(e.getMessage());
            }
        }

        assertEquals(126, count);
    }
    
    @Test
    public void testUploadFileOk() throws Exception {
        byte[] testdata = "This is a test.".getBytes("UTF-8");
       
        String id = underTest.uploadFile(new ByteArrayInputStream(testdata), -1);
        
        assertNotNull(id);
    }
    
    @Test
    public void testDepositSipOk() throws Exception {
        Dcp dcp = new Dcp();
        DcsDeliverableUnit du = new DcsDeliverableUnit();
        du.setTitle("boring sip");
        du.setId("some id");
        
        dcp.addDeliverableUnit(du);
        
        URL ticket = underTest.depositSIP(dcp);

        assertNotNull(ticket);
    }
}
