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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HttpDcsConnector implements DcsConnector {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DcsModelBuilder mb;
    private final DcsConnectorConfig config;
    private final HttpClient client;

    public HttpDcsConnector(DcsConnectorConfig config, DcsModelBuilder mb) {
        if (config == null) {
            throw new IllegalArgumentException("DcsConnectorConfig must not be null.");
        }

        if (mb == null) {
            throw new IllegalArgumentException("DcsModelBuilder must not be null.");
        }

        this.config = config;
        this.mb = mb;

        final ClientConnectionManager cm;
        final BasicHttpParams httpParams = new BasicHttpParams();

        if (config.getMaxOpenConn() > 1) {
            final SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(
                    new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ConnManagerParams.setMaxTotalConnections(httpParams, config.getMaxOpenConn());
            ConnPerRouteBean connPerRoute = new ConnPerRouteBean(config.getMaxOpenConn());
            ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);
            if (config.getConnPoolTimeout() > 0) {
                ConnManagerParams.setTimeout(httpParams, config.getConnPoolTimeout() * 1000);
            }
            cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
            new ConnectionMonitorThread(cm).start();
        } else {
            cm = null;
        }

        if (config.getConnTimeout() > 0) {
            httpParams.setParameter("http.connection.timeout", config.getConnTimeout() * 1000);
            httpParams.setParameter("http.socket.timeout", config.getConnTimeout() * 1000);
        }

        this.client = new DefaultHttpClient(cm, httpParams);
        log.debug("Instantiated {} ({}) with configuration {}",
                new Object[] {this.getClass().getName(), System.identityHashCode(this), config });
    }

    HttpDcsConnector(DcsConnectorConfig config, DcsModelBuilder mb, HttpClient client) {
        if (config == null) {
            throw new IllegalArgumentException("DcsConnectorConfig must not be null.");
        }

        if (mb == null) {
            throw new IllegalArgumentException("DcsModelBuilder must not be null.");
        }

        if (client == null) {
            throw new IllegalArgumentException("HttpClient must not be null.");
        }

        this.config = config;
        this.mb = mb;
        this.client = client;
    }

    @Override
    public InputStream getStream(String streamId) throws DcsClientFault {
        final HttpGet get = new HttpGet(streamId);
        final HttpResponse response;

        // streamId may identify an entity or a datastream.

        final InputStream content;
        try {
            response = execute(get, HttpStatus.SC_OK);
            content = response.getEntity().getContent();
        } catch (IOException e) {
            throw new HttpIoException("Could not retrieve content for " + streamId + ": " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            get.abort();
            throw new DcsClientFault("Could not retrieve content for " + streamId + ": " + e.getMessage(), e);
        }
        return content;
    }

    @Override
    public Iterator<DcsFile> getFiles(String entityId) throws DcsClientFault {
        final String fileSearchQuery;

        if (entityId == null) {
            fileSearchQuery = QueryUtil.createLiteralQuery("entityType", "File");
        } else {
            String bar = QueryUtil.createLiteralQuery("AND", "entityType", "File", "ancestry", entityId);
            String foo = QueryUtil.createLiteralQuery("AND", "entityType", "File", "id", entityId);
            fileSearchQuery = bar + " OR " + foo;
        }

        final Iterator<DcsEntity> entities = search(fileSearchQuery);
        final List<DcsFile> files = new ArrayList<DcsFile>();
        while (entities.hasNext()) {
            final DcsEntity e = entities.next();
            if (e instanceof DcsFile) {
                files.add((DcsFile) e);
            }
        }
        return files.iterator();
    }

    @Override
    public CountableIterator<DcsEntity> search(String query) throws DcsClientFault {
        return new HttpDcsSearchIterator(client, new DcsConnectorConfig(config), mb, query);
    }
    
    @Override
    public CountableIterator<DcsEntity> search(String query, int maxResults, int offset) throws DcsClientFault {
        return new HttpDcsSearchIterator(client, new DcsConnectorConfig(config), mb, query, maxResults, offset);
    }

    public DcsConnectorConfig getConfig() {
        return config;
    }

    /**
     * Execute the supplied request.  Method is package-private for unit testing.
     *
     * @param req the request object
     * @param success_status http response status needed for the execution to succeed 
     * @return an HttpResponse
     * @throws DcsClientFault if the response status code is between 400 to 499 inclusive
     * @throws DcsServerException if the response status code is 500 or greater
     * @throws HttpIoException if a connection cannot be obtained from the connection pool, or an I/O error occurs
     * @throws DcsConnectorRuntimeException if any other RuntimeExceptions are caught
     */
    HttpResponse execute(HttpUriRequest req, int success_status) throws DcsClientFault {
        HttpResponse response = null;
        String requestUri = null;
        try {
            requestUri = req.getRequestLine().getUri();
            response = client.execute(req);
        } catch (ConnectionPoolTimeoutException e) {
            throw new HttpIoException("Timeout reached while obtaining an HTTP connection from the connection pool.  " +
                    "First, ensure response InputStreams are being read (this frees the connection), then try to " +
                    "increase the maximum number of connections, or increase the connection pool timeout.", e);                    
        } catch (IOException e) {
            throw new HttpIoException("Could not retrieve " + requestUri + ": " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            // thrown when trying to re-use a connection that hasn't been closed
            req.abort();
            throw new DcsClientFault(e.getMessage(), e); 
        } catch (RuntimeException e) {
            req.abort();
            throw new DcsConnectorRuntimeException(e.getMessage(), e);
        }

        final int statusCode = response.getStatusLine().getStatusCode();
        final String statusReason = response.getStatusLine().getReasonPhrase();

        if (statusCode != success_status) {

            req.abort();
            
            if (statusCode >= 400 && statusCode < 500) {
                throw new DcsClientFault("Could not retrieve " + requestUri + ": response from server was " +
                        statusCode + " '" + statusReason + "'");
            }

            if (statusCode >= 500) {
                throw new DcsServerException("Could not retrieve " + requestUri + ": response from server was " +
                        statusCode + " '" + statusReason + "'");
            }

            if (statusCode > 200 && statusCode < 300) {
                log.debug("Received status {} for {}", statusCode, requestUri);
                // TODO
            }

            if (statusCode >= 300 && statusCode < 400) {
                log.debug("Received status {} for {}", statusCode, requestUri);
                // TODO
            }

            if (statusCode >= 100 && statusCode < 200) {
                log.debug("Received status {} for {}", statusCode, requestUri);
                // TODO
            }
        }

        return response;
    }

    @Override
    public String toString() {
        return "HttpDcsConnector{" +
                "config=" + config +
                '}';
    }

    @Override
    public String uploadFile(InputStream is, long length) throws DcsClientFault {
        HttpPost post;
        
        try {
            post = new HttpPost(config.getUploadFileUrl().toURI());
        } catch (URISyntaxException e) {
            final String msg = "Malformed upload file endpoint URL " + config.getUploadFileUrl() + ": " + e.getMessage();
            log.debug(msg, e);
            throw new DcsClientFault(msg, e);
        }

        InputStreamEntity data = new InputStreamEntity(is, length);
        data.setContentType("binary/octet-stream");
        data.setChunked(true);
        post.setEntity(data);

        HttpResponse resp = execute(post, HttpStatus.SC_ACCEPTED);

        try {
            resp.getEntity().consumeContent();
        } catch (IOException e) {
            throw new DcsClientFault("Problem releasing resources", e);
        }
        
        Header header = resp.getFirstHeader("X-dcs-src");

        if (header == null) {
            throw new DcsClientFault("Server response missing required header X-dcs-src for post to " + config.getUploadFileUrl());
        }
        
        return header.getValue();
    }

    @Override
    public URL depositSIP(Dcp dcp) throws DcsClientFault {
        HttpPost post;

        try {
            post = new HttpPost(config.getDepositSipUrl().toURI());
        } catch (URISyntaxException e) {
            final String msg = "Malformed deposit sip endpoint URL "
                    + config.getUploadFileUrl() + ": " + e.getMessage();
            log.debug(msg, e);
            throw new DcsClientFault(msg, e);
        }

        post.setHeader("Content-Type", "application/xml");
        post.setHeader("X-Packaging", "http://dataconservancy.org/schemas/dcp/1.0");

        ByteArrayOutputStream dcp_buf = new ByteArrayOutputStream();
        mb.buildSip(dcp, dcp_buf);
        
        ByteArrayEntity data = new ByteArrayEntity(dcp_buf.toByteArray());
        post.setEntity(data);

        HttpResponse resp = execute(post, HttpStatus.SC_ACCEPTED);
        
        // Parse atom feed and pull out <link href> containing sip status url

        Document doc = null;
        ByteArrayOutputStream atom_buf = null;

        try {
            atom_buf = new ByteArrayOutputStream();
            resp.getEntity().writeTo(atom_buf);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new ByteArrayInputStream(atom_buf.toByteArray()));
        } catch (IOException e) {
            throw new DcsClientFault("Error reading server response", e);
        } catch (ParserConfigurationException e) {
            throw new DcsClientFault("Error parsing atom: " + atom_buf, e);
        } catch (SAXException e) {
            throw new DcsClientFault("Error parsing atom: " + atom_buf, e);
        }
        NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom",
                "link");

        if (nl.getLength() == 0) {
            throw new DcsClientFault("Could not parse atom: " + atom_buf);
        }

        String status_url = ((Element) nl.item(0)).getAttribute("href");

        if (status_url == null) {
            throw new DcsClientFault("Could not parse atom: " + atom_buf);
        }

        try {
            return new URL(status_url);
        } catch (MalformedURLException e) {
            throw new DcsClientFault("Malformed status url: " + status_url, e);
        }
    }
}
