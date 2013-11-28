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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import org.custommonkey.xmlunit.XMLUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.dataconservancy.dcs.util.DigestListener;
import org.dataconservancy.dcs.util.DigestNotificationOutputStream;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFixity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public abstract class AbstractHttpConnectorTest extends AbstractEntityTest {

    static final Logger log = LoggerFactory.getLogger(HttpDcsConnectorTest.class);

    static LocalTestServer testServer;
    static String accessServiceUrl = "http://%s:%s";

    DcsConnectorConfig config;
    HttpDcsConnector underTest;

    @BeforeClass
    public static void setUp() throws Exception {

        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);

        final BasicHttpProcessor httpProc = new BasicHttpProcessor();
        testServer = new LocalTestServer(httpProc, null);

        testServer.register("/entity/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                final String requestURI = request.getRequestLine().getUri();
                final String requestedEntity = requestURI.substring(requestURI.lastIndexOf("/") + 1);
                log.trace("processing request for entity {}", requestedEntity);
                final File requestedEntityFile = new File(entitiesDir, requestedEntity + ".xml");
                if (!requestedEntityFile.exists()) {
                    response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                } else {
                    response.setStatusCode(HttpStatus.SC_OK);
                    response.setHeader("content-type", "application/xml");
                    response.setEntity(new InputStreamEntity(new FileInputStream(requestedEntityFile), -1));
                }
            }
        });

        testServer.register("/query/*", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                final String requestURI = request.getRequestLine().getUri();
                final String query = requestURI.substring(requestURI.lastIndexOf("/") + 1);
                
                int max = -1;
                int offset = 0;

                //For some reason request.getParams wasn't working so parse the string
                String[] params = query.split("&");  
                  
                for (String param : params)  
                {  
                    String name = param.split("=")[0];  
                    if( name.equalsIgnoreCase("max")){
                        max = Integer.parseInt(param.split("=")[1]); 
                    }
                    else if( name.equalsIgnoreCase("offset")) {
                        offset = Integer.parseInt(param.split("=")[1]); 
                    }
                }  
                
                if( max == -1){
                    max = allTestEntities.size();
                }
                
                log.trace("processing request for query {} (note that the query itself is ignored and the response is hardcoded)", query);
                // basically we iterate over all the entities in the file system and return a DCP of Files.  The actual query is ignored

                final Dcp dcp = new Dcp();
                int count = 0;
                for (DcsEntity e : allTestEntities) {
                    if (e instanceof DcsFile) {
                        if( count >= offset){
                            dcp.addFile((DcsFile) e);
                        }
                        count++;
                        if( count == max){
                            break;
                        }
                    }
                }

                count = dcp.getFiles().size();
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mb.buildSip(dcp, baos);

                response.setStatusCode(HttpStatus.SC_OK);
                response.setHeader("content-type", "application/xml");
                response.setHeader("X-TOTAL-MATCHES", String.valueOf(count));
                response.setEntity(new BufferedHttpEntity(new ByteArrayEntity(baos.toByteArray())));
            }
        });
        
        testServer.register("/deposit/file", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                log.trace("file upload");
                
                response.setStatusCode(HttpStatus.SC_ACCEPTED);
                response.setHeader("content-type", "application/atom+xml;type=entry");
                response.setHeader("Location", "http://dataconservancy.org/deposit/003210.atom");
                response.setHeader("X-dcs-src", "http://dataconservancy.org/deposit/003210.file");
                response.setEntity(new StringEntity("<?xml version='1.0'?>" +
                        "<entry xmlns='http://www.w3.org/2005/Atom' xmlns:dc='http://dataconservancy.org/ns/'>" +
                        "</entry>", "UTF-8"));
            }
        });
        
        testServer.register("/deposit/sip", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                log.trace("sip upload");
                
                response.setStatusCode(HttpStatus.SC_ACCEPTED);
                response.setHeader("content-type", "application/atom+xml;type=entry");
                response.setHeader("Location", "http://dcservice.moo.org:8080/dcs/content/sipDeposit/4331419");
                response.setEntity(new StringEntity( "<?xml version='1.0'?>" +
                        "<entry xmlns='http://www.w3.org/2005/Atom' xmlns:sword='http://purl.org/net/sword/'><id>deposit:/sipDeposit/4331419</id><content type='application/xml' src='http://dcservice.moo.org:8080/dcs/content/sipDeposit/4331419' /><title type='text'>Deposit 4331419</title><updated>2011-10-28T18:12:50.171Z</updated><author><name>Depositor</name></author><summary type='text'>ingesting</summary><link href='http://dcservice.moo.org:8080/dcs/status/sipDeposit/4331419' type='application/atom+xml; type=feed' rel='alternate' title='Processing Status' /><sword:treatment>Deposit processing</sword:treatment></entry>"
                        ));
            }
        });
        
        testServer.start();
        log.info("Test HTTP server listening on {}:{}", testServer.getServiceHostName(), testServer.getServicePort());
    }

    @Before
    public void setUpClient() throws MalformedURLException {
        config = getConnectorConfig();
        underTest = new HttpDcsConnector(config, mb);
    }

    /**
     * Implementers are expected to provide a new instance of a DcsConnectorConfig.  This method is called prior to
     * each test method being executed.
     * 
     * @return
     */
    protected abstract DcsConnectorConfig getConnectorConfig();

    @AfterClass
    public static void tearDown() throws Exception {
        testServer.stop();
    }

    /**
     * Asserts that the supplied input stream has the expected fixity.
     *
     * @param expectedFixity the expected fixity value of the stream
     * @param in             the input stream
     * @throws java.security.NoSuchAlgorithmException
     * @throws IOException
     */
    void assertFixtyDigestEqual(final DcsFixity expectedFixity, final InputStream in)
            throws NoSuchAlgorithmException, IOException {
        assertNotNull("Fixity must not be null", expectedFixity);
        assertNotNull("Inputstream must not be null", in);

        final StringBuilder actualDigest = new StringBuilder();
        final NullOutputStream nullOs = new NullOutputStream();
        final DigestNotificationOutputStream digestOut = new DigestNotificationOutputStream(nullOs,
                MessageDigest.getInstance(expectedFixity.getAlgorithm()), new DigestListener() {
                    @Override
                    public void notify(byte[] digestValue) throws IOException {
                        for (byte b : digestValue) {
                            actualDigest.append(String.format("%02x", b));
                        }

                    }
                }
        );

        IOUtils.copy(in, digestOut);
        digestOut.close();
        assertEquals(expectedFixity.getValue(), actualDigest.toString());
    }


}
