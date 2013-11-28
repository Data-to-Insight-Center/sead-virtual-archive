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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.dataconservancy.dcs.id.api.Types;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CreateIdApiRequest {

    private UiUrlConfig urlConfig;
    private Types type;

    public CreateIdApiRequest(UiUrlConfig urlConfig) {
        this.urlConfig = urlConfig;
    }

    public Types getType() {
        return type;
    }

    public void setType(Types type) {
        this.type = type;
    }

    public String execute(HttpClient hc) throws IOException {
        HttpPost request = this.asHttpPost();
        HttpResponse response = hc.execute(request);
        assertEquals(response.getStatusLine().toString(), 201, response.getStatusLine().getStatusCode());
        String createdId = response.getFirstHeader("Location").getValue();
        HttpAssert.free(response);
        return createdId;
    }

    public HttpPost asHttpPost() {
        HttpPost post = new HttpPost(urlConfig.getCreateIdApiUrl().toString());
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("type", type.name()));
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        post.setEntity(entity);
        return post;
    }
}
