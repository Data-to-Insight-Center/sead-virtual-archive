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
package org.dataconservancy.ui.it.support;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Provides a family of HTTP-related {@code assertXXXX(...)} methods.  There may be a separate library (HTTPUnit?) which
 * might be better suited for this type of activity.
 */
public class HttpAssert {

    private static final String STATUS_FAIL = "Unexpected status code %s when accessing %s.  Expected %s.  (HTTP reason phrase was: '%s')";
    private static final String STATUS_FAIL_WITH_MESSAGE = "%s  (Unexpected status code %s when accessing %s.  Expected %s.  HTTP reason phrase was: '%s')";
    private static final String STATUS_FAIL_RANGE = "Unexpected status code %s when accessing %s.  Expected range %s - %s inclusive.  (HTTP reason phrase was: '%s')";
    private static final String STATUS_FAIL_RANGE_WITH_MESSAGE = "%s (Unexpected status code %s when accessing %s.  Expected range %s - %s inclusive.  HTTP reason phrase was: '%s')";


    private static boolean isLogging = false;
    private static Logger LOG = LoggerFactory.getLogger(HttpAssert.class);

    public static boolean isLogging() {
        return isLogging;
    }

    public static void setLogging(boolean logging) {
        isLogging = logging;
    }

    public static void assertStatus(HttpClient hc, String url, int status) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, status, status, null, null);
    }

    public static void assertStatus(HttpClient hc, String url, int status, ResponseHolder holder) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, status, status, null, holder);
    }

    public static void assertStatus(HttpClient hc, String url, int status, String message) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, status, status, message, null);
    }

    public static void assertStatus(HttpClient hc, String url, int status, String message, ResponseHolder holder) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, status, status, message, holder);
    }

    public static void assertStatus(HttpClient hc, String url, int statusFrom, int statusTo) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, statusFrom, statusTo, null, null);
    }

    public static void assertStatus(HttpClient hc, String url, int statusFrom, int statusTo, ResponseHolder holder) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, statusFrom, statusTo, null, holder);
    }

    public static void assertStatus(HttpClient hc, String url, int statusFrom, int statusTo, String message) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, statusFrom, statusTo, message, null);
    }

    public static void assertStatus(HttpClient hc, String url, int statusFrom, int statusTo, String message,
                                    ResponseHolder holder) {
        HttpGet req = new HttpGet(url);
        assertStatus(hc, req, statusFrom, statusTo, message, holder);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int status) {
        assertStatus(hc, url, status, status, null, null);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int status, ResponseHolder holder) {
        assertStatus(hc, url, status, status, null, holder);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int status, String message) {
        assertStatus(hc, url, status, status, message, null);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int status, String message,
                                    ResponseHolder holder) {
        assertStatus(hc, url, status, status, message, holder);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int statusFrom, int statusTo) {
        assertStatus(hc, url, statusFrom, statusTo, null, null);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int statusFrom, int statusTo,
                                    ResponseHolder holder) {
        assertStatus(hc, url, statusFrom, statusTo, null, holder);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int statusFrom, int statusTo, String message) {
        assertStatus(hc, url, statusFrom, statusTo, message, null);
    }

    public static void assertStatus(HttpClient hc, HttpUriRequest url, int statusFrom, int statusTo, String message,
                                    ResponseHolder holder) {
        assertValidStatusRange(statusFrom, statusTo);


        boolean range = (statusFrom != statusTo);
        HttpResponse res = null;

        try {
            res = hc.execute(url);

            if (isLogging) {
                logRequest(url);
                logResponse(res);
            }

            final StatusLine statusLine = res.getStatusLine();
            final int rc = statusLine.getStatusCode();
            if (holder != null) holder.statusCode = rc;
            if (holder != null && res.getFirstHeader("Location") != null) holder.locationHeader = res.getFirstHeader("Location").getValue();
            final String reasonPhrase = (statusLine.getReasonPhrase() == null ? "<reason phrase was empty>"
                    : statusLine.getReasonPhrase());
            if (range) {
                String assertionMessage = null;
                if (message == null) {
                    assertionMessage = String.format(STATUS_FAIL_RANGE, rc, url.getURI().toString(), statusFrom,
                            statusTo, reasonPhrase);
                } else {
                    assertionMessage = String.format(STATUS_FAIL_RANGE_WITH_MESSAGE, message, rc,
                            url.getURI().toString(), statusFrom, statusTo, reasonPhrase);
                }
                if (holder != null) holder.reasonPhrase = assertionMessage;
                assertTrue(assertionMessage, statusFrom <= rc && rc <= statusTo);
            } else {
                String assertionMessage = null;
                if (message == null) {
                    assertionMessage = String.format(STATUS_FAIL, rc, url.getURI().toString(), statusFrom, reasonPhrase);
                } else {
                    assertionMessage = String.format(STATUS_FAIL_WITH_MESSAGE, message, rc,
                            url.getURI().toString(), statusFrom, reasonPhrase);
                }
                if (holder != null) holder.reasonPhrase = assertionMessage;
                assertEquals(assertionMessage, statusFrom, rc);
            }

            if (holder != null) {
                holder.body = freeAndCopy(res);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (res != null) {
                free(res);
            }
        }
    }

    /**
     * Free the HTTP connection by consuming the entity body of the response.  Note that this will exhaust the
     * entity body, so it will be impossible to read the entity response body after calling this method.
     *
     * @param response the HTTP response.
     */
    public static void free(HttpResponse response) {
        freeAndCopy(response, false);
    }

    /**
     * Free the HTTP connection by consuming the entity body of the response.  Note that this will exhaust the
     * entity body, but a copy of the entity body is created and returned.
     *
     * @param response the HTTP response.
     * @return InputStream to a copy of the entity body, may be null if there is no entity body.
     */
    public static InputStream freeAndCopy(HttpResponse response) {
        return freeAndCopy(response, true);
    }

    /**
     * Free the HTTP connection by consuming the entity body of the response.  Will return a copy of the entity body
     * according to {@code copy}.
     *
     * @param response the HTTP response
     * @param copy     creates a copy of the entity body if true
     * @return an InputStream to the entity body if {@code copy} is true, null otherwise.
     */
    private static InputStream freeAndCopy(HttpResponse response, boolean copy) {
        HttpEntity entity;
        if ((entity = response.getEntity()) != null) {
            try {
                final InputStream entityBody = entity.getContent();
                InputStream entityBodyCopy = null;
                if (copy) {
                    ByteArrayOutputStream copyOut = new ByteArrayOutputStream(1024);
                    IOUtils.copy(entityBody, copyOut);
                    copyOut.close();
                    entityBodyCopy = new ByteArrayInputStream(copyOut.toByteArray());
                }

                entityBody.close();
                return entityBodyCopy;
            } catch (IOException e) {
                // ignore
            }
        }

        return null;
    }
    
    private static void assertValidStatus(int status) {
        assertTrue("Status must be greater than zero!", status > 0);
    }

    private static void assertValidStatusRange(int statusFrom, int statusTo) {
        assertValidStatus(statusFrom);
        assertValidStatus(statusTo);
        assertTrue("From must be lesser than or equal to To", statusFrom <= statusTo);
    }

    private static void logRequest(HttpRequest req) {
        HttpRequestLogger.logRequest(req);
    }

    private static void logResponse(HttpResponse res) {
        HttpRequestLogger.logResponse(res);
    }

    public static class ResponseHolder {
        private int statusCode;
        private String reasonPhrase;
        private InputStream body;
        private String locationHeader;

        // in the future we can add anything we want here.

        /**
         * An inputstream to the response body
         *
         * @return
         */
        public InputStream getBody() {
            return body;
        }

        /**
         * The reason phrase associated with the status code
         *
         * @return
         */
        public String getReasonPhrase() {
            return reasonPhrase;
        }

        /**
         * The status code of the response
         *
         * @return
         */
        public int getStatusCode() {
            return statusCode;
        }

        /**
         * The value in the {@code Location} header, if any.
         *
         * @return
         */
        public String getLocationHeader() {
            return locationHeader;
        }
    }
}
