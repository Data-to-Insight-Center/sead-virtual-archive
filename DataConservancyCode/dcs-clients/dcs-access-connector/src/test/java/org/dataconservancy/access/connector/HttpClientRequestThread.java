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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class HttpClientRequestThread extends AbstractRequestThread {

    private HttpClient client;

    public HttpClientRequestThread(HttpClient client, String url, boolean consumeResponse) {
        super(url, consumeResponse);
        this.client = client;
    }

    @Override
    public void run() {
        super.run();

        if (client == null) {
            throw new IllegalStateException("HttpClient must not be null.");
        }

        try {
            InputStream in = client.execute(new HttpGet(url)).getEntity().getContent();
            if (consumeResponse) {
                IOUtils.copy(in, new NullOutputStream());
            }
        } catch (ClientProtocolException e) {
            try {
                cpeHandler.handleException(e);
            } catch (ClientProtocolException e1) {
                // don't care
            }
        } catch (IOException e) {
            try {
                ioeHandler.handleException(e);
            } catch (IOException e1) {
                // don't care
            }
        }
    }

    public HttpClient getClient() {
        return client;
    }

    public void setClient(HttpClient client) {
        this.client = client;
    }
}
