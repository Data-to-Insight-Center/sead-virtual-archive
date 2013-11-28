/*
 * Copyright 2013 Johns Hopkins University
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class HttpRegistryConnector extends HttpDcsConnector implements RegistryConnector {

    private static final String ENTRIES_URI_PART = "/entries";

    private static final String ENTRY_URI_PART = "/entry";

    private static final String TYPES_URI_PART = "/types";

    private static final String ID_REQUEST_PARAM = "id";

    private static final String REGISTRY_REQUEST_PARAM = "registry";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String registryEndpoint;

    private DcsConnectorConfig config;

    public HttpRegistryConnector(DcsConnectorConfig config, DcsModelBuilder mb) {
        super(config, mb);
        this.config = config;
    }

    @Override
    public Set<URL> getRegistryTypes() throws DcsClientFault {
        String endpoint = getAvailableRegistryTypesEndpoint();
        return getReferences(endpoint);
    }

    public Set<URL> getRegistryEntries(String registryType) throws DcsClientFault {
        if (registryType == null || registryType.trim().length() == 0) {
            throw new IllegalArgumentException("Registry type must not be null or the empty string.");
        }
        return getReferences(getRegistryTypeEndpoint(registryType));
    }

    public String getRegistryEndpoint() {
        return this.registryEndpoint;
    }

    public void setRegistryEndpoint(String registryEndpoint) {
        if (registryEndpoint == null || registryEndpoint.trim().length() == 0) {
            throw new IllegalArgumentException("DCS registry endpoint must not be null or the empty string.");
        }


        this.registryEndpoint = trimTrailingSlashes(config.getAccessHttpUrl().toExternalForm()) +
                trimTrailingSlashes(registryEndpoint);
    }

    private String getRegistryTypeEndpoint(String registryType) {
        return registryEndpoint + ENTRIES_URI_PART + "?" + REGISTRY_REQUEST_PARAM + "=" + registryType;
    }

    private String getAvailableRegistryTypesEndpoint() {
        return registryEndpoint + TYPES_URI_PART;
    }

    private Set<URL> getReferences(String endpoint) throws DcsClientFault {
        Set<URL> references = new HashSet<URL>();
        LineIterator lineItr = null;
        InputStream contentStream = null;
        try {
            HttpResponse response = execute(new HttpGet(endpoint), 200);
            contentStream = response.getEntity().getContent();
            lineItr = IOUtils.lineIterator(contentStream, "UTF-8");
        } catch (IOException e) {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            String msg = "Unable to read inputstream for URL " + endpoint;
            log.debug(msg);
            throw new DcsServerException(msg, e);
        }

        try {
            while (lineItr.hasNext()) {
                String regUrl = lineItr.nextLine();
                try {
                    references.add(new URL(regUrl));
                } catch (MalformedURLException e) {
                    log.debug("Received invalid registry URL {}", regUrl);
                }
            }
        } finally {
            try {
                contentStream.close();
            } catch (IOException e) {
                // ignore
            }
            lineItr.close();
        }

        return references;
    }

    private String trimTrailingSlashes(String url) {
        url = url.trim();
        while (url.endsWith("/") && url.length() > 1) {
            url = url.substring(0, url.length() - 1);
        }

        if (url.equals("/")) {
            throw new IllegalArgumentException("Invalid URI: it contained only slashes.");
        }

        return url;
    }
}
