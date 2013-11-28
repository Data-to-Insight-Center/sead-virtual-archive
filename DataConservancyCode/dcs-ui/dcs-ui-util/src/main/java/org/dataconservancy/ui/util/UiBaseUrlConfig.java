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
package org.dataconservancy.ui.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@code UiBaseUrlConfig} is used to configure the base URL of the dcs-ui web-app with hosting server information.
 */
public class UiBaseUrlConfig {

    private String hostname;
    private int port;
    private boolean isSecure;
    private String contextPath;
    private Scheme scheme;

    private enum Scheme {HTTP, HTTPS}

    public URL getBaseUrl() {
        URL u = null;
        try {
            u = new URL(
                    scheme.toString().toLowerCase(),
                    hostname,
                    port,
                    contextPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return u;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        if (hostname == null || hostname.trim().length() == 0) {
            throw new IllegalArgumentException("Hostname must not be empty or null.");
        }
        if (hostname.contains(":")) {
            throw new IllegalArgumentException("Hostname should not contain a port or url scheme, just a fully qualified domain name.");
        }
        this.hostname = hostname.toLowerCase().trim();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("Port must be 1 or greater.");
        }
        this.port = port;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
        if (secure && scheme != Scheme.HTTPS) {
            scheme = Scheme.HTTPS;
        }
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        if (contextPath == null) {
            throw new IllegalArgumentException("Context path must not be null.");
        }
        this.contextPath = normalizePath(contextPath);
    }

    public String getScheme() {
        return scheme.toString().toLowerCase();
    }

    public void setScheme(String scheme) {
        if (scheme == null || scheme.trim().length() == 0) {
            throw new IllegalArgumentException("Protocol scheme must not be empty or null.");
        }
        scheme = scheme.toUpperCase().trim();
        this.scheme = Scheme.valueOf(scheme);
        if (this.scheme == Scheme.HTTPS) {
            isSecure = true;
        }
    }

       /**
     * Lowercases and trims a String, and insures that the string doesn't
     * end with a "/".
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    private String normalizePath(String path) {
        path = path.toLowerCase().trim();
        while (path.endsWith("/") && path.length() > 0) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public String toString() {
        return "UiBaseUrlConfig{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", isSecure=" + isSecure +
                ", contextPath='" + contextPath + '\'' +
                ", scheme=" + scheme +
                '}';
    }
}
