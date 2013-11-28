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
package org.dataconservancy.dcs.ingest.client.impl;

import java.io.InputStream;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.deposit.DepositDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Fetches a remote http-based content document. */
class HttpDepositDocument
        implements DepositDocument {

    private static final Logger logger =
            LoggerFactory.getLogger(HttpDepositDocument.class);

    private final HttpClient httpClient;

    private final String docUrl;

    private Header[] headers;

    public HttpDepositDocument(HttpClient client, String url) {
        httpClient = client;
        docUrl = url;
    }

    public InputStream getInputStream() {
        return poll(new HttpGet(docUrl));
    }

    public long getLastModified() {
        poll(new HttpHead(docUrl));

        for (Header h : headers) {
            if (h.getName().equals(HttpHeaderUtil.LAST_MODIFIED)) {
                return DateUtility.parseDate(h.getValue());
            }
        }

        logger.warn("Could not get last modified date from server.  "
                + "Using current date");
        return new Date().getTime();

    }

    public Map<String, String> getMetadata() {
        poll(new HttpHead(docUrl));

        Map<String, String> metadata = new HashMap<String, String>();
        for (Header h : headers) {
            if (!metadata.containsKey(h.getName())) {
                metadata.put(h.getName(), h.getValue());
            } else {
                metadata.put(h.getName(), String.format("%s,%s", metadata.get(h
                        .getName()), h.getValue()));
            }
        }
        return metadata;
    }

    public String getMimeType() {
        if (headers == null) {
            poll(new HttpHead(docUrl));
        }

        for (Header h : headers) {
            if (h.getName().equals(HttpHeaderUtil.CONTENT_TYPE)) {
                return h.getValue();
            }
        }

        logger.warn("Server did not specify content type.  Using default");
        return "application/octet-stream";
    }

    private InputStream poll(HttpUriRequest method) {
        try {
            HttpResponse resp = httpClient.execute(method);

            int code = resp.getStatusLine().getStatusCode();
            if (code >= 200 && code <= 202) {
                headers = resp.getAllHeaders();

                return resp.getEntity().getContent();
            } else {
                throw new RuntimeException(String
                        .format("Unexpected http code: %s %s" + code, resp
                                .getStatusLine().getReasonPhrase()));
            }
        } catch (Exception e) {
            method.abort();
            throw new RuntimeException(e);
        }
    }
}
