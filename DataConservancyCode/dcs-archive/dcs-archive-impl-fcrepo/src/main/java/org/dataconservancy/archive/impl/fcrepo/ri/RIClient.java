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
package org.dataconservancy.archive.impl.fcrepo.ri;

import com.yourmediashelf.fedora.client.FedoraCredentials;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fedora Resource Index Client.
 */
public class RIClient {

    private final String riEndpoint;
    private final MultiThreadedHttpClient httpClient;

    public RIClient(FedoraCredentials credentials,
                    HttpClientConfig httpClientConfig) {
        this.riEndpoint = credentials.getBaseUrl() + "/risearch";
        this.httpClient = new MultiThreadedHttpClient(httpClientConfig);
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(credentials.getBaseUrl().getHost(),
                              AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(credentials.getUsername(),
                        credentials.getPassword()));
    }

    public RIQueryResult query(String sparql, boolean flush) {
        return new RIQueryResult(executeQuery(sparql, flush));
    }

    @PreDestroy
    public void close() {
        httpClient.getConnectionManager().shutdown();
    }

    private InputStream executeQuery(String sparql, boolean flush) {
        HttpPost post = new HttpPost(riEndpoint);
        List<NameValuePair> args = new ArrayList<NameValuePair>();
        if (flush) {
            args.add(new BasicNameValuePair("flush", "true"));
        }
        args.add(new BasicNameValuePair("type", "tuples"));
        args.add(new BasicNameValuePair("lang", "sparql"));
        args.add(new BasicNameValuePair("format", "Simple"));
        args.add(new BasicNameValuePair("stream", "on"));
        args.add(new BasicNameValuePair("query", sparql));
        try {
            post.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8));
        } catch (UnsupportedEncodingException wontHappen) {
            throw new RuntimeException(wontHappen);
        }
        try {
            HttpResponse response = httpClient.execute(post);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                throw new RuntimeException("Fedora Resource Index query "
                        + "returned an unexpected HTTP response code: "
                        + responseCode + ". Consult Fedora Server log for "
                        + "details.");
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return new ByteArrayInputStream(new byte[0]);
            } else {
                return entity.getContent();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
