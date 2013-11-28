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

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.InputStreamEntity;

import org.dataconservancy.dcs.util.HttpHeaderUtil;
import org.dataconservancy.deposit.DepositInfo;
import org.dataconservancy.deposit.DepositManager;
import org.dataconservancy.deposit.PackageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import static org.dataconservancy.deposit.sword.extension.SWORDExtensionFactory.Headers.PACKAGING;

/**
 * Performs remote deposit to a sword/APP based deposit service.
 * <p>
 * Expects the service to adopt the DC convention of representing deposit status
 * as alternate links.
 * </p>
 * <h2>Configuration</h2>
 * <dl>
 * <dt>{@link #setCollectionUrl(String)}</dt>
 * <dd><b>Required</b>. SWORD/AtomPub collection url.</dd>
 * <dt>{@link #setHttpClient(HttpClient)}</dt>
 * <dd><b>Required</b></dd>
 * <dt>
 * </dl>
 */
public class SwordClientManager
        implements DepositManager {

    private static final Logger log =
            LoggerFactory.getLogger(SwordClientManager.class);

    private String collectionUrl;

    private HttpClient client;

    @Required
    public void setCollectionUrl(String url) {
        collectionUrl = url;
    }

    @Required
    public void setHttpClient(HttpClient c) {
        client = c;
    }

    public DepositInfo deposit(InputStream content,
                               String contentType,
                               String packaging,
                               Map<String, String> metadata)
            throws PackageException {

        if (metadata == null) {
            metadata = new HashMap<String, String>();
        }

        HttpPost post = new HttpPost(collectionUrl);
        for (Map.Entry<String, String> val : metadata.entrySet()) {
            if (!val.getKey().equals(HttpHeaderUtil.CONTENT_LENGTH)) {
                post.setHeader(val.getKey(), val.getValue());
            }
        }

        post.setHeader(HttpHeaderUtil.CONTENT_TYPE, contentType);
        if (packaging != null) {
            post.setHeader(PACKAGING, packaging);
        }

        if (!metadata.containsKey(HttpHeaderUtil.CONTENT_LENGTH)) {
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(content);
            post.setEntity(entity);
        } else {
            post.setEntity(new InputStreamEntity(content, new Long(metadata
                    .get(HttpHeaderUtil.CONTENT_LENGTH))));
        }

        return execute(post);
    }

    public DepositInfo getDepositInfo(String id) {
        HttpGet get = new HttpGet(id);
        return execute(get);
    }

    public String getManagerID() {
        return this.toString();
    }

    private DepositInfo execute(HttpUriRequest method) {
        try {
            HttpResponse response = client.execute(method);

            int code = response.getStatusLine().getStatusCode();
            if (code >= 200 && code <= 202) {
                Header[] headers = response.getAllHeaders();
                InputStream content = response.getEntity().getContent();
                byte[] body;
                try {
                    body = IOUtils.toByteArray(content);
                } finally {
                    content.close();
                }

                if (response.containsHeader("Location")) {
                    return new DcsDepositInfo(client,
                                              response
                                                      .getFirstHeader("Location")
                                                      .getValue(),
                                              headers,
                                              body);
                } else {
                    return new DcsDepositInfo(client, method.getURI()
                            .toASCIIString(), headers, body);
                }
            } else {
                log.warn(IOUtils.toString(response.getEntity().getContent()));
                throw new RuntimeException(String
                        .format("Unexpected http code for %s: %s %s", method.getRequestLine(),
                                code, response.getStatusLine().getReasonPhrase()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
