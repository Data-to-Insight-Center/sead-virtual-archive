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

package org.dataconservancy.ui.api.support;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for handling various aspects of the Lineage HTTP API
 * request/response.
 */
//TODO: This should be reconciled with the Lineage HTTP request util into one general request util.
public class RequestUtil {

    private static final String HOST_HEADER = "Host";

    private static final String LOCAL_HOST_IPV4 = "127.0.0.1";

    private static final String LOCAL_HOST_IPV6 = "0:0:0:0:0:0:0:1%0";

    private static final String LOCAL_HOST_NAME = "localhost";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean considerHostHeader = true;

    private boolean considerRemotePort = true;

    private boolean considerLocalPort = true;

    private boolean performLocalhostIpTranslation = true;
    
    private boolean alwaysIncludePort = false;

    public boolean isConsiderHostHeader() {
        return considerHostHeader;
    }

    public void setConsiderHostHeader(boolean considerHostHeader) {
        this.considerHostHeader = considerHostHeader;
    }

    public boolean isConsiderLocalPort() {
        return considerLocalPort;
    }

    public void setConsiderLocalPort(boolean considerLocalPort) {
        this.considerLocalPort = considerLocalPort;
    }

    public boolean isConsiderRemotePort() {
        return considerRemotePort;
    }

    public void setConsiderRemotePort(boolean considerRemotePort) {
        this.considerRemotePort = considerRemotePort;
    }

    public boolean isPerformLocalhostIpTranslation() {
        return performLocalhostIpTranslation;
    }

    public void setPerformLocalhostIpTranslation(boolean performLocalhostIpTranslation) {
        this.performLocalhostIpTranslation = performLocalhostIpTranslation;
    }
    
    public void setAlwaysIncludePort(boolean includePort) {
        this.alwaysIncludePort = includePort;
    }
    
    public boolean isAlwaysIncludePort() {
        return alwaysIncludePort;
    }

    //TODO: This may need to be updated to match existing ID formats

    /**
     * Build the original request url in the form http://hostname/request/uri.
     * <p/>
     * If you include a 'Host' HTTP header (which is the form hostname:port),
     * that is what will be used to re-construct the requested URL. If you don't
     * include a 'Host' header, the the behavior is to use
     * HttpServletRequest.getRemoteHost() to determine the hostname, and
     * HttpServletRequest.getRemotePort() to determine the port number. The
     * logic is influenced by three member variables of RequestUtil:
     * considerHostHeader, considerRemotePort, and considerLocalPort. The
     * summary above is the default behavior (by default all three flags are
     * true).
     * 
     * @param request
     *        the request
     * @return the request url
     */
    public String buildRequestUrl(HttpServletRequest request) {
        log.trace("{}: Building request url for {}", this, request);

        StringBuilder url = new StringBuilder("http");
        if (request.isSecure()) {
            url.append("s");
        }
        url.append("://");

        log.trace("Scheme: {}", url.toString());

        url.append(determineHostName(request));

        log.trace("Hostname: {}", url.toString());

        int port = determinePort(request);

        if(alwaysIncludePort) {
            url.append(":").append(port);
        } else {
            if (port == 80) {
                if (request.isSecure()) {
                    url.append(":80");
                }
            } else if (port == 443) {
                if (!request.isSecure()) {
                    url.append(":443");
                }
            }  else {
                url.append(":").append(port);
            }
        }

        log.trace("Port: {}", url.toString());

        String requestURI = request.getRequestURI();
        log.trace("Request URI: {}", requestURI);

        url.append(requestURI);

        log.trace("Built request url {} for request {}",
                  url.toString(),
                  request);

        return url.toString();
    }

    /**
     * Determine the host name that the client targeted with their
     * {@code request}. If {@code considerHostHeader} is {@code true}, and a
     * HTTP {@code Host} header is present, the value of the header will be used
     * as the host name. If the header is not present, or if
     * {@code considerHostHeader} is {@code false}, the host name will be
     * determined using
     * {@link javax.servlet.http.HttpServletRequest#getRemoteHost()}. If
     * {@code performLocalhostIpTranslation} is {@code true}, and the host name
     * is {@code LOCAL_HOST_IPV4} or {@code LOCAL_HOST_IPV6}, then the host name
     * will be set to {@code LOCAL_HOST_NAME}.
     * 
     * @param request
     *        the request
     * @return the host name targeted by the {@code request}
     */
    private String determineHostName(HttpServletRequest request) {

        String hostName = null;

        // If there is a 'Host' header with the request, and if
        // we are supposed to consider it when determining the host name,
        // then use it.

        // This is the best way to go, because the client is indicating
        // what host and port they targeted
        final String hostHeader = request.getHeader(HOST_HEADER);
        if (considerHostHeader && hostHeader != null
                && hostHeader.trim().length() != 0) {
            hostName = parseHostHeader(hostHeader)[0];
        }

        // Either the 'Host' header wasn't considered, or parsing it failed for some reason.
        // So we fall back on request.getRemoteHost()
        if (hostName == null) {
            hostName = request.getRemoteHost();
        }

        if (performLocalhostIpTranslation) {
            if (LOCAL_HOST_IPV4.equals(hostName)
                    || LOCAL_HOST_IPV6.equals(hostName)) {
                hostName = LOCAL_HOST_NAME;
            }
        }

        return hostName;
    }

    /**
     * Determine the port number that the client targeted with their
     * {@code request}. If {@code considerHostHeader} is {@code true}, and a
     * HTTP {@code Host} header is present, the value of the port in the header
     * will be used as the port number. If the header is not present, or if
     * {@code considerHostHeader} is {@code false}, the port number will be
     * determined using
     * {@link javax.servlet.http.HttpServletRequest#getRemotePort()} (if
     * {@code considerRemotePort} is {@code true}) followed by
     * {@link javax.servlet.http.HttpServletRequest#getLocalPort()} (if
     * {@code considerLocalPort} is {@code true}).
     * 
     * @param request
     *        the request
     * @return the port number targeted by the {@code request}
     */
    private int determinePort(HttpServletRequest request) {

        int portNumber = -1;

        // If there is a 'Host' header with the request, and if
        // we are supposed to consider it when determining the port,
        // then use it.

        // This is the best way to go, because the client is indicating
        // what host and port they targeted
        final String hostHeader = request.getHeader(HOST_HEADER);
        if (considerHostHeader && hostHeader != null
                && hostHeader.trim().length() != 0) {
            String temp = parseHostHeader(hostHeader)[1];
            if (temp != null) {
                portNumber = Integer.parseInt(temp);
            }
        }

        // Either the 'Host' header wasn't considered, or parsing it failed for some reason.

        if (portNumber == -1 && considerRemotePort) {
            portNumber = request.getRemotePort();
        }

        if (portNumber == -1 && considerLocalPort) {
            portNumber = request.getLocalPort();
        }

        return portNumber;
    }

    /**
     * Intended to parse the value of the HTTP {@code Host} header. Typically
     * values will look like {@code hostname:port}.
     * 
     * @param hostHeaderValue
     *        the value of the {@code Host} HTTP header, must not be
     *        {@code null} or empty.
     * @return an array with index 0 containing the host name, and index 1
     *         containing the port
     * @throws IllegalArgumentException
     *         if {@code hostHeaderValue} is {@code null} or the empty string
     */
    private String[] parseHostHeader(String hostHeaderValue) {
        // Just to be sure
        if (hostHeaderValue == null || hostHeaderValue.trim().length() == 0) {
            throw new IllegalArgumentException("Host header shouldn't be null or empty.");
        } else {
            hostHeaderValue = hostHeaderValue.trim();
        }

        String[] hostAndPort = hostHeaderValue.split(":");

        // handle the case of a missing port (default to port 80)
        if (hostAndPort.length == 1) {
            hostAndPort = new String[] {hostAndPort[0], "80"};
        }

        return hostAndPort;
    }

    @Override
    public String toString() {
        return "RequestUtil{" + "considerHostHeader=" + considerHostHeader
                + ", considerRemotePort=" + considerRemotePort
                + ", considerLocalPort=" + considerLocalPort
                + ", performLocalhostIpTranslation="
                + performLocalhostIpTranslation + '}';
    }
}