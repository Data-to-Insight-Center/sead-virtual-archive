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

import java.io.ByteArrayInputStream;

import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import org.dataconservancy.deposit.DepositDocument;
import org.dataconservancy.deposit.DepositInfo;

/**
 * DepositInfo based on DC conventions in atomPub entry responses.
 */
class DcsDepositInfo
        implements DepositInfo {

    private static final Abdera abdera = new Abdera();

    private final HttpClient httpClient;

    private final byte[] content;

    private final String depositDocUrl;

    private Entry entry;

    private final Header[] httpHeaders;

    public DcsDepositInfo(HttpClient client,
                          String location,
                          Header[] headers,
                          byte[] body) {
        httpClient = client;

        depositDocUrl = location;

        this.content = body;

        httpHeaders = headers;
    }

    public DepositDocument getDepositContent() {
        parseEntry();
        return new HttpDepositDocument(httpClient, entry.getContentSrc()
                .toASCIIString());
    }

    public String getDepositID() {
        return depositDocUrl;
    }

    public DepositDocument getDepositStatus() {
        parseEntry();
        return new HttpDepositDocument(httpClient, entry.getAlternateLink()
                .getHref().toASCIIString());
    }

    public String getManagerID() {
        return this.toString();
    }

    public Map<String, String> getMetadata() {
        Map<String, String> metadata = new HashMap<String, String>();
        for (Header h : httpHeaders) {
            if (!metadata.containsKey(h.getName())) {
                metadata.put(h.getName(), h.getValue());
            } else {
                metadata.put(h.getName(), String.format("%s,%s", metadata.get(h
                        .getName()), h.getValue()));
            }
        }
        return metadata;
    }

    public String getSummary() {
        parseEntry();
        return entry.getSummary();
    }

    public boolean hasCompleted() {
        return getSummary().contains("complete");
    }

    public boolean isSuccessful() {
        return getSummary().contains("success");
    }

    private void parseEntry() {
        if (entry == null) {
            Document<Entry> doc =
                    abdera.getParser().parse(new ByteArrayInputStream(content));
            entry = doc.getRoot();
        }
    }
}
