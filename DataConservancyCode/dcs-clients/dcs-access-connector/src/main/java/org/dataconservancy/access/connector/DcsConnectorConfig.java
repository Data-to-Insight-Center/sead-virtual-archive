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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Encapsulates the parameters and values used to configure the Data Conservancy connector.
 */
public class DcsConnectorConfig {

    private String scheme;
    private String host;
    private int port;
    private String contextPath;

    private int maxOpenConn = 1;
    private int connPoolTimeout = -1;
    private int connTimeout = -1;

    /**
     * Create a default configuration with one maximum open connection, and indefinite timeouts.
     */
    public DcsConnectorConfig() {

    }

    /**
     * Copy constructor.
     *
     * @param toCopy the instance to copy
     */
    public DcsConnectorConfig(DcsConnectorConfig toCopy) {
        this.scheme = toCopy.scheme;
        this.host = toCopy.host;
        this.port = toCopy.port;
        this.contextPath = toCopy.contextPath;
        
        this.maxOpenConn = toCopy.maxOpenConn;
        this.connTimeout = toCopy.connTimeout;
        this.connPoolTimeout = toCopy.connPoolTimeout;
    }

    
    /**
     * @param p1
     * @param p2
     * @return paths joined with exactly one / between them
     */
    private static String join_paths(String p1, String p2) {
        if (p1.endsWith("/")) {
            p1 = p1.substring(0, p1.length() - 1);
        }

        if (p2.startsWith("/")) {
            p2 = p2.substring(1);
        }

        return p1 + "/" + p2;
    }

    /**
     * @return base url for uploading files when composing a SIP
     */
    public URL getUploadFileUrl() {
        try {
            return new URL(scheme, host, port, join_paths(contextPath, "deposit/file"));
        } catch (MalformedURLException e) {
            throw new DcsConnectorRuntimeException(e);
        }
    }

    /**
     * @return base url for depositing a SIP
     */
    public URL getDepositSipUrl() {
        try {
            return new URL(scheme, host, port, join_paths(contextPath, "deposit/sip"));
        } catch (MalformedURLException e) {
            throw new DcsConnectorRuntimeException(e);
        }
    }

    /**
     * The base url to the HTTP access API.  For example:
     * <pre>    http://dcservice.dataconservancy.org:8080/dcs</pre>
     * Clients are responsible for appending the various servlet paths for the various endpoints such
     * as the query, datastream, and entity endpoints.
     *
     * @return the base url to the HTTP access API
     */
    public URL getAccessHttpUrl() {
        try {
            return new URL(scheme, host, port, contextPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The maximum number of connections the DCS connector is allowed to have open simultaneously.
     *
     * @return the maximum number of open connections
     */
    public int getMaxOpenConn() {
        return maxOpenConn;
    }

    /**
     * Set the maximum number of connections the DCS connector is allowed to have open simultaneously.
     *
     * @param maxOpenConn the maximum number of open connections
     * @throws IllegalArgumentException if <code>maxOpenConn</code> is less than 1
     */
    public void setMaxOpenConn(int maxOpenConn) {
        if (maxOpenConn < 1) {
            throw new IllegalArgumentException("Maximum open connections must be 1 or greater.");
        }
        this.maxOpenConn = maxOpenConn;
    }

    /**
     * How long (in seconds) the DCS connector should wait for a free connection from the connection pool.  If
     * set to a value less than <code>1</code>, the connector will wait forever for a free connection.
     *
     * @return the connection pool timeout value in seconds
     */
    public int getConnPoolTimeout() {
        return connPoolTimeout;
    }

    /**
     * Sets how long (in seconds) the DCS connector should wait for a free connection from the connection pool.  If
     * set to a value less than <code>1</code>, the connector will wait forever for a free connection.
     *
     * @param connPoolTimeout the connection pool timeout value in seconds
     */
    public void setConnPoolTimeout(int connPoolTimeout) {
        if (connPoolTimeout < 1) {
            connPoolTimeout = -1;
        }
        this.connPoolTimeout = connPoolTimeout;
    }

    /**
     * How long (in seconds) the DCS connector should wait when making a connection to the remote host.
     * If set to a value less than <code>1</code>, the connector will wait forever to make the connection.
     *
     * @return the connection timeout value in seconds
     */
    public int getConnTimeout() {
        return connTimeout;
    }

    /**
     * Sets how long (in seconds) the DCS connector should wait when making a connection to the remote host.
     * If set to a value less than <code>1</code>, the connector will wait forever to make the connection.
     *
     * @param connTimeout the connection timeout value in seconds
     */
    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String toString() {
        return "DcsConnectorConfig{" +
                "scheme='" + scheme + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", contextPath='" + contextPath + '\'' +
                ", maxOpenConn=" + maxOpenConn +
                ", connPoolTimeout=" + connPoolTimeout +
                ", connTimeout=" + connTimeout +
                '}';
    }
}
