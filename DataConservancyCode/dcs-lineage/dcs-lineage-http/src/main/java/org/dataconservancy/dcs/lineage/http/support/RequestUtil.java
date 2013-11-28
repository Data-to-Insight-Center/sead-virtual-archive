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
package org.dataconservancy.dcs.lineage.http.support;

import java.io.IOException;

import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import org.dataconservancy.dcs.lineage.api.Lineage;
import org.dataconservancy.dcs.lineage.api.LineageEntry;
import org.dataconservancy.model.dcs.DcsEntity;
import org.joda.time.DateTime;

/**
 * Utility methods for handling various aspects of the Lineage HTTP API request/response.
 */
public class RequestUtil {

    private static final String HOST_HEADER = "Host";

    private static final String LOCAL_HOST_IPV4 = "127.0.0.1";

    private static final String LOCAL_HOST_IPV6 = "0:0:0:0:0:0:0:1%0";

    private static final String LOCAL_HOST_NAME = "localhost";

    private boolean considerHostHeader = true;

    private boolean considerRemotePort = true;

    private boolean considerLocalPort = true;

    private boolean performLocalhostIpTranslation = true;

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

    /**
     * Calculates a MD5 digest over the list of entity ids, suitable for use as an
     * ETag.  If the list is empty, {@code null} is returned.
     *
     * @param entityIds the entities
     * @return the MD5 digest, or {@code null} if {@code entityIds} is empty.
     */
    public static String calculateDigest(List<String> entityIds) {
        if (entityIds.isEmpty()) {
            return null;
        }
        
        NullOutputStream nullOut = new NullOutputStream();
        DigestOutputStream digestOut = null;

        try {
            digestOut = new DigestOutputStream(nullOut, MessageDigest.getInstance("MD5"));
            for (String id : entityIds) {
                IOUtils.write(id, digestOut);
            }
            digestOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return digestToHexString(digestOut.getMessageDigest().digest());
    }

    /**
     * Convenience method, simply delegates to {@link #calculateDigest(java.util.List)}.  If the
     * {@code entities} list is empty, {@code null} is returned.
     *
     * @param entities the entities
     * @return the MD5 digest, or {@code null} if the {@code entities List} is empty.
     */
    public static String calculateDigestForEntities(List<DcsEntity> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        List<String> ids = new ArrayList<String>();
        for (DcsEntity e : entities) {
            ids.add(e.getId());
        }

        return calculateDigest(ids);
    }

    /**
     * Convenience method, simply delegates to {@link #calculateDigest(java.util.List)}.  If the
     * lineage is {@code null} or empty, {@code null} is returned.
     *
     * @param l the lineage to calculate a digest for
     * @return the MD5 digest, or {@code null} if the {@code entities List} is empty.
     */
    public static String calculateDigestForLineage(Lineage l) {
        if (l == null || !l.iterator().hasNext()) {
            return null;
        }

        List<String> ids = new ArrayList<String>();
        for (LineageEntry entry : l) {
            ids.add(entry.getEntityId());
        }

        return calculateDigest(ids);
    }

    public DateTime determineLastModified(List<String> entityIds) {
        throw new UnsupportedOperationException("TODO: Implement");
    }

    public String createEtag(String entityId) {
        return calculateDigest(Arrays.asList(entityId));
    }

    public DateTime determineLastModified(String entityId) {
        throw new UnsupportedOperationException("TODO: Implement");
    }

    public boolean determineIfModifiedSince(String id, DateTime modifiedSince) {
        throw new UnsupportedOperationException("TODO: Implement");
    }

    public boolean determineIfModified(String id, String etag) {
        throw new UnsupportedOperationException("TODO: Implement");
    }

    /**
     * Build the original request url in the form http://hostname/request/uri.
     * <p/>
     * If you include a 'Host' HTTP header (which is the form hostname:port), that is what will be used to re-construct
     * the requested URL. If you don't include a 'Host' header, the the behavior is to use
     * HttpServletRequest.getRemoteHost() to determine the hostname, and HttpServletRequest.getRemotePort() to determine
     * the port number.  The logic is influenced by three member variables of RequestUtil: considerHostHeader,
     * considerRemotePort, and considerLocalPort.  The summary above is the default behavior (by default all three flags
     * are true).
     *
     * @param request the request
     * @return the request url
     */
    public String buildRequestUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder("http");
        if (request.isSecure()) {
            url.append("s");
        }
        url.append("://");

        url.append(determineHostName(request));
        
        int port = determinePort(request);

        if (port == 80) {
            if (request.isSecure()) {
                url.append(":80");
            }
        } else if (port == 443) {
            if (!request.isSecure()) {
                url.append(":443");
            }
        } else {
            url.append(":").append(port);
        }

        url.append(request.getRequestURI());

        return url.toString();
    }

    /**
     * Converts a digest represented as a byte array to a hexadecimal string, a la 'md5sum'.
     *
     * @param digest the digest
     * @return the digest in hexadecimal string form
     */
    private static String digestToHexString(byte[] digest) {
        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            final String hex = Integer.toHexString(b & 0x000000ff);
            if (hex.length() == 1) {
                result.append(0);
            }
            result.append(hex);
        }

        return result.toString();
    }

    /**
     * Determine the host name that the client targeted with their {@code request}.  If {@code considerHostHeader} is
     * {@code true}, and a HTTP {@code Host} header is present, the value of the header will be used as the host name.
     * If the header is not present, or if {@code considerHostHeader} is {@code false}, the host name will be determined
     * using {@link javax.servlet.http.HttpServletRequest#getRemoteHost()}.  If {@code performLocalhostIpTranslation}
     * is {@code true}, and the host name is {@code LOCAL_HOST_IPV4} or {@code LOCAL_HOST_IPV6}, then the host name
     * will be set to {@code LOCAL_HOST_NAME}.
     *
     * @param request the request
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
        if (considerHostHeader && hostHeader != null && hostHeader.trim().length() != 0) {
            hostName = parseHostHeader(hostHeader)[0];
        }

        // Either the 'Host' header wasn't considered, or parsing it failed for some reason.
        // So we fall back on request.getRemoteHost()
        if (hostName == null) {
            hostName = request.getRemoteHost();
        }


        if (performLocalhostIpTranslation) {
            if (LOCAL_HOST_IPV4.equals(hostName) || LOCAL_HOST_IPV6.equals(hostName)) {
                hostName = LOCAL_HOST_NAME;
            }
        }

        return hostName;
    }

    /**
     * Determine the port number that the client targeted with their {@code request}.  If {@code considerHostHeader} is
     * {@code true}, and a HTTP {@code Host} header is present, the value of the port in the header will be used as the
     * port number. If the header is not present, or if {@code considerHostHeader} is {@code false}, the port number
     * will be determined using {@link javax.servlet.http.HttpServletRequest#getRemotePort()}
     * (if {@code considerRemotePort} is {@code true}) followed by {@link javax.servlet.http.HttpServletRequest#getLocalPort()}
     * (if {@code considerLocalPort} is {@code true}).
     *
     * @param request the request
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
        if (considerHostHeader && hostHeader != null && hostHeader.trim().length() != 0) {
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
     * Intended to parse the value of the HTTP {@code Host} header.  Typically values will look like
     * {@code hostname:port}.
     *
     * @param hostHeaderValue the value of the {@code Host} HTTP header, must not be {@code null} or empty.
     * @return an array with index 0 containing the host name, and index 1 containing the port
     * @throws IllegalArgumentException if {@code hostHeaderValue} is {@code null} or the empty string
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
            hostAndPort = new String[]{hostAndPort[0], "80"};
        }

        return hostAndPort;
    }

}
