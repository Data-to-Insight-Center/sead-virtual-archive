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

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the Apache HTTP Client, overriding the {@link DefaultHttpClient#createClientRequestDirector(org.apache.http.protocol.HttpRequestExecutor, org.apache.http.conn.ClientConnectionManager, org.apache.http.ConnectionReuseStrategy, org.apache.http.conn.ConnectionKeepAliveStrategy, org.apache.http.conn.routing.HttpRoutePlanner, org.apache.http.protocol.HttpProcessor, org.apache.http.client.HttpRequestRetryHandler, org.apache.http.client.RedirectHandler, org.apache.http.client.AuthenticationHandler, org.apache.http.client.AuthenticationHandler, org.apache.http.client.UserTokenHandler, org.apache.http.params.HttpParams)}
 * method.  This enables mocking of the {@link org.apache.http.client.RequestDirector#execute(org.apache.http.HttpHost, org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)}
 * method.  Because most mock frameworks are unable to mock final methods, {@link org.apache.http.impl.client.AbstractHttpClient#execute(org.apache.http.client.methods.HttpUriRequest)} and
 * friends cannot be mocked (<code>DefaultHttpClient</code> is a subclass of <code>AbstractHttpClient</code>).
 */
public class MockDefaultHttpClient extends DefaultHttpClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private RequestDirector mockDirector;
    private ClientConnectionManager mockConnectionManager;

    public MockDefaultHttpClient(HttpParams params) {
        super(params);
    }

    public MockDefaultHttpClient(ClientConnectionManager conman, HttpParams params) {
        super(conman, params);
    }

    /**
     * Returns the mock director if {@link #setMockDirector(org.apache.http.client.RequestDirector)} has been called,
     * otherwise this delegates to the superclass.
     *
     * @param requestExec
     * @param conman
     * @param reustrat
     * @param kastrat
     * @param rouplan
     * @param httpProcessor
     * @param retryHandler
     * @param redirectHandler
     * @param targetAuthHandler
     * @param proxyAuthHandler
     * @param stateHandler
     * @param params
     * @return the request director
     */
    @Override
    protected RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec, ClientConnectionManager conman, ConnectionReuseStrategy reustrat, ConnectionKeepAliveStrategy kastrat, HttpRoutePlanner rouplan, HttpProcessor httpProcessor, HttpRequestRetryHandler retryHandler, RedirectHandler redirectHandler, AuthenticationHandler targetAuthHandler, AuthenticationHandler proxyAuthHandler, UserTokenHandler stateHandler, HttpParams params) {
        if (mockDirector == null) {
            log.debug("Returning the superclass RequestDirector because the mockDirector is null.");
            return super.createClientRequestDirector(requestExec, conman, reustrat, kastrat, rouplan, httpProcessor, retryHandler, redirectHandler, targetAuthHandler, proxyAuthHandler, stateHandler, params);    //To change body of overridden methods use File | Settings | File Templates.
        }

        return mockDirector;
    }

    /**
     * Returns the mock connection manager if {@link #setMockConnectionManager(org.apache.http.conn.ClientConnectionManager)}
     * has been called, otherwise this delegates to the superclass.
     *
     * @return the client connection manager
     */
    @Override
    protected ClientConnectionManager createClientConnectionManager() {
        if (mockConnectionManager == null) {
            log.debug("Returning the superclass ClientConnectionManager because the mockConnectionManager is null.");
            return super.createClientConnectionManager();
        }

        return mockConnectionManager;
    }

    /**
     * Obtain the mock <code>RequestDirector</code>, may be null.
     *
     * @return the request director
     */
    public RequestDirector getMockDirector() {
        return mockDirector;
    }

    /**
     * Set the mock <code>RequestDirector</code>.  If unset, the super implementation
     * will be returned by {@link #createClientRequestDirector(org.apache.http.protocol.HttpRequestExecutor, org.apache.http.conn.ClientConnectionManager, org.apache.http.ConnectionReuseStrategy, org.apache.http.conn.ConnectionKeepAliveStrategy, org.apache.http.conn.routing.HttpRoutePlanner, org.apache.http.protocol.HttpProcessor, org.apache.http.client.HttpRequestRetryHandler, org.apache.http.client.RedirectHandler, org.apache.http.client.AuthenticationHandler, org.apache.http.client.AuthenticationHandler, org.apache.http.client.UserTokenHandler, org.apache.http.params.HttpParams)}.
     * Otherwise, <code>mockDirector</code> will be returned.
     *
     * @param mockDirector the mock request director to return
     */
    public void setMockDirector(RequestDirector mockDirector) {
        this.mockDirector = mockDirector;
    }

    /**
     * Obtain the mock <code>ClientConnectionManager</code>, may be null.
     *
     * @return the connection manager
     */
    public ClientConnectionManager getMockConnectionManager() {
        return mockConnectionManager;
    }

    /**
     * Set the mock <code>ClientConnectionManager</code>.  If unset, the super implementation
     * will be returned by {@link #createClientConnectionManager()}. Otherwise, <code>mockConnectionManager</code>
     * will be returned.
     *
     * @param mockConnectionManager the mock connection manager to return
     */
    public void setMockConnectionManager(ClientConnectionManager mockConnectionManager) {
        this.mockConnectionManager = mockConnectionManager;
    }
}
