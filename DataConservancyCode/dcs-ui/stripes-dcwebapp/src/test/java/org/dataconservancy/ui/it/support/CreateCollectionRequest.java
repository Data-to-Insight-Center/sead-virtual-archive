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

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.PersonName;
import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CreateCollectionRequest {

    private String projectId; // projectId
    private String id; // collection.id
    private String title; // collection.title
    private String summary; // collection.summary
    private DateTime publicationDate;
    private List<PersonName> creators;

    private final UiUrlConfig urlConfig;

    private static final String STRIPES_EVENT = "addCollection";

    private boolean collectionSet = false;

    public CreateCollectionRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
        creators = new ArrayList<PersonName>();
    }

    public Collection getCollection() {
        if (!collectionSet) {
            throw new IllegalStateException("Collection not set: call setCollection(Collection) first.");
        }

        Collection collection = new Collection();
        collection.setTitle(title);
        collection.setSummary(summary);
        collection.setId(id);
        collection.setPublicationDate(publicationDate);
        collection.setCreators(creators);
        return collection;
    }

    public void setCollection(Collection toCreate) {
        this.title = toCreate.getTitle();
        this.summary = toCreate.getSummary();
        this.id = toCreate.getId();
        this.publicationDate = toCreate.getPublicationDate();
        this.creators = toCreate.getCreators();
        collectionSet = true;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public HttpPost asHttpPost() {
        if (!collectionSet) {
            throw new IllegalStateException("Collection not set: call setCollection(Collection) first.");
        }

        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getAddCollectionUrl().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("projectId", projectId));
        params.add(new BasicNameValuePair("collection.title", title));
        params.add(new BasicNameValuePair("collection.summary", summary));
        params.add(new BasicNameValuePair("collection.id", id));
        if (publicationDate != null) {
            params.add(new BasicNameValuePair("collection.publicationDate", publicationDate.toString()));
        }

        if(creators != null && creators.size() > 0) {
            for (int i=0; i < creators.size(); i++) {
                params.add(new BasicNameValuePair("collection.creators[" + i + "].prefixes", creators.get(i).getPrefixes()));
                params.add(new BasicNameValuePair("collection.creators[" + i + "].givenNames", creators.get(i).getGivenNames()));
                params.add(new BasicNameValuePair("collection.creators[" + i + "].middleNames", creators.get(i).getMiddleNames()));
                params.add(new BasicNameValuePair("collection.creators[" + i + "].familyNames", creators.get(i).getFamilyNames()));
                params.add(new BasicNameValuePair("collection.creators[" + i + "].suffixes", creators.get(i).getSuffixes()));
            }
        }
        params.add(new BasicNameValuePair(STRIPES_EVENT, "Add Collection"));

        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        post.setEntity(entity);

        return post;
    }
}
