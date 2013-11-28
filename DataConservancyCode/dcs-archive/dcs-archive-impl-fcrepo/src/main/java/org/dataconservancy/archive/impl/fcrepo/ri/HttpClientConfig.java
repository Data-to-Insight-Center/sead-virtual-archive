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
package org.dataconservancy.archive.impl.fcrepo.ri;

public class HttpClientConfig {

    public static final int     DEFAULT_CONNECTION_TIMEOUT             = 30000;
    public static final int     DEFAULT_SOCKET_TIMEOUT                 = 30000;
    public static final int     DEFAULT_MAX_CONNECTIONS_PER_HOST       = 10;
    public static final int     DEFAULT_MAX_TOTAL_CONNECTIONS          = 100;
    public static final boolean DEFAULT_PREEMPTIVE_AUTHN               = false;
    public static final boolean DEFAULT_SKIP_SSL_TRUST_CHECK           = false;
    public static final boolean DEFAULT_SKIP_SSL_HOSTNAME_VERIFICATION = false;

    private int connectionTimeout;
    private int socketTimeout;
    private int maxConnectionsPerHost;
    private int maxTotalConnections;
    private boolean preemptiveAuthN;
    private boolean skipSSLTrustCheck;
    private boolean skipSSLHostnameVerification;

    /**
     * Constructs an <code>HttpClientConfig</code> with default values.
     */
    public HttpClientConfig() {
        connectionTimeout           = DEFAULT_CONNECTION_TIMEOUT;
        socketTimeout               = DEFAULT_SOCKET_TIMEOUT;
        maxConnectionsPerHost       = DEFAULT_MAX_CONNECTIONS_PER_HOST;
        maxTotalConnections         = DEFAULT_MAX_TOTAL_CONNECTIONS;
        preemptiveAuthN             = DEFAULT_PREEMPTIVE_AUTHN;
        skipSSLTrustCheck           = DEFAULT_SKIP_SSL_TRUST_CHECK;
        skipSSLHostnameVerification = DEFAULT_SKIP_SSL_HOSTNAME_VERIFICATION;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.connectionTimeout = socketTimeout;
    }

    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public boolean getPreemptiveAuthN() {
        return preemptiveAuthN;
    }

    public void setPreemptiveAuthN(boolean preemptiveAuthN) {
        this.preemptiveAuthN = preemptiveAuthN;
    }

    public boolean getSkipSSLTrustCheck() {
        return skipSSLTrustCheck;
    }

    public void setSkipSSLTrustCheck(boolean skipSSLTrustCheck) {
        this.skipSSLTrustCheck = skipSSLTrustCheck;
    }

    public boolean getSkipSSLHostnameVerification() {
        return skipSSLHostnameVerification;
    }

    public void setSkipSSLHostnameVerification(
            boolean skipSSLHostnameVerification) {
        this.skipSSLHostnameVerification = skipSSLHostnameVerification;
    }

}
