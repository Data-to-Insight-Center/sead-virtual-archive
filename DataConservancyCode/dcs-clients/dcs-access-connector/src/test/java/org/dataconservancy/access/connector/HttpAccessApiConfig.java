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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Encapsulates the configuration for the Data Conservancy Access HTTP API.
 */
class HttpAccessApiConfig {

    /**
     * The "base" url to the HTTP Access API.  E.g. <code>http://dcservice.dataconservancy.org:8080/dcs/</code> or
     * <code>http://localhost:8080</code>.
     */
    static final String BASE_ENDPOINT_PROPERTY = "access.url.base";

    /**
     * This is a partial identifier of a known <em>extant</em> DCS File entity.  For example, if the full identifier is
     * <code>http://localhost:8080/entity/test10<code> then the partial identifier would be <code>test10</code>.
     * If the full identifier were <code>http://dcservice.dataconservancy.org:8080/dcs/entity/66</code> then the
     * partial identifier would be <code>66</code>.
     */
    static final String FILE_ENTITY_EXTANT_PROPERTY = "file.entity.extant";

    /**
     * This is a partial identifier of a known <em>non-extant</em> DCS File entity.  For example, if the full identifier is
     * <code>http://localhost:8080/entity/test10<code> then the partial identifier would be <code>test10</code>.
     * If the full identifier were <code>http://dcservice.dataconservancy.org:8080/dcs/entity/66</code> then the
     * partial identifier would be <code>66</code>.
     */
    static final String FILE_ENTITY_NON_EXTANT_PROPERTY = "file.entity.nonextant";

    /**
     * This is the path on the filesystem to a directory containing DCP xml of entities known to reside on the
     * other side of the HTTP Access API.
     */
    static final String KNOWN_ENTITIES_PROPERTY = "known.entities";

    private String datastreamEndpoint;

    private String entityEndpoint;

    private String extantFileEntity;

    private File knownEntitiesBasedir;

    private String nonExtantFileEntity;

    private String queryEndpoint;

    private String host;

    private int port;

    private String scheme;

    private String contextPath;

    /**
     * Instantiates a config with no state.
     */
    HttpAccessApiConfig() {

    }

    /**
     * The Data Conservancy HTTP endpoint for retrieving entities.
     *
     * @return
     */
    String getEntityEndpoint() {
        return entityEndpoint;
    }

    /**
     * The Data Conservancy HTTP endpoint for executing search queries.
     *
     * @return
     */
    String getQueryEndpoint() {
        return queryEndpoint;
    }

    /**
     * The Data Conservancy HTTP endpoint for retrieving streams.
     *
     * @return
     */
    String getDatastreamEndpoint() {
        return datastreamEndpoint;
    }

    /**
     * The Data Conservancy HTTP identifier for a File entity that is extant.
     *
     * @return
     */
    String getExtantFileEntity() {
        return extantFileEntity;
    }

    /**
     * The Data Conservancy HTTP identifier for a File entity that is <em>not</em> extant.
     *
     * @return
     */
    String getNonExtantFileEntity() {
        return nonExtantFileEntity;
    }

    public String getBaseEndpoint() {
        try {
            return new URL(scheme, host, port, contextPath).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getScheme() {
        return scheme;
    }

    public String getContextPath() {
        return contextPath;
    }

    public boolean isSecure() {
        return scheme != null && scheme.startsWith("https");
    }

    /**
     * Returns <code>true</code> if this configuration is running on the local host.
     * 
     * @return
     */
    boolean isLocal() {
        URL baseUrl = null;
        try {
            baseUrl = new URL(scheme, host, port, contextPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        final String host = baseUrl.getHost();
        InetAddress addy = null;
        try {
            if (host.matches("^[0-9]*") && host.split("\\.").length == 4) {
                final String[] hostPart = host.split("\\.");
                addy = InetAddress.getByAddress(new byte[]{Byte.parseByte(hostPart[0], 10),
                        Byte.parseByte(hostPart[1], 10), Byte.parseByte(hostPart[2], 10),
                        Byte.parseByte(hostPart[3], 10)});
            } else {
                addy = InetAddress.getByName(host);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException("Base endpoint host '" + host + "' cannot be resolved", e);
        }

        if (addy == null) {
            throw new RuntimeException("Base endpoint could not be converted to an InetAddress instance.");
        }

        return addy.isLoopbackAddress() || addy.isAnyLocalAddress();
    }

    File getKnownEntitiesBasedir() {
        return knownEntitiesBasedir;
    }

    public void setDatastreamEndpoint(String datastreamEndpoint) {
        try {
            this.datastreamEndpoint = new URL(scheme, host, port, contextPath + datastreamEndpoint).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setEntityEndpoint(String entityEndpoint) {
        try {
            this.entityEndpoint = new URL(scheme, host, port, contextPath + entityEndpoint).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    public void setExtantFileEntity(String extantFileEntity) {
        this.extantFileEntity = getEntityForPartialFileId(extantFileEntity);
    }

    public void setKnownEntitiesBasedir(File knownEntitiesBasedir) {
        this.knownEntitiesBasedir = knownEntitiesBasedir;
    }

    public void setNonExtantFileEntity(String nonExtantFileEntity) {
        this.nonExtantFileEntity = getEntityForPartialFileId(nonExtantFileEntity);
    }

    public void setQueryEndpoint(String queryEndpoint) {
        try {
            this.queryEndpoint = new URL(scheme, host, port, contextPath + queryEndpoint).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    private String getEntityForPartialFileId(String partialEntityId) {
        final String orig = partialEntityId;
        // Strip off any preceeding slashes
        partialEntityId = stripPreceedingSlashes(partialEntityId);

        if (partialEntityId.length() == 0) {
            throw new RuntimeException("The partial entity id '" + partialEntityId + "' is likely malformed.");
        }

        return getEntityEndpoint() + "/" + partialEntityId;
    }

    private static String stripPreceedingSlashes(String s) {
        while (s.startsWith("/") && s.length() > 1) {
            s = s.substring(1);
        }

        if (s.equals("/")) {
            return "";
        }

        return s;
    }

    private static String stripTrailingSlashes(String s) {
        while (s.endsWith("/") && s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }

        if (s.equals("/")) {
            return "";
        }

        return s;
    }
}
