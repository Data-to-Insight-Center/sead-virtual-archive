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
import org.dataconservancy.ui.model.ContactInfo;
import org.dataconservancy.ui.model.PersonName;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * a few fields (not significan't for the purpose of the related tests) of the collection object are being left out.
 * Those are:
 *   - publicationDate
 *   - depositDate
 *   - depositor
 *   - citableLocator
 *
 */
public class UpdateCollectionRequest {
    private String collectionId; // seperate string value of the collection id outside of the collection object
    private String id; // collection.id
    private String title; // collection.title
    private String summary; // collection.summary
    private List<ContactInfo> contactInfos;
    private List<String> alternateIds;
    private List<PersonName> creators;


    private final UiUrlConfig urlConfig;

    private static final String STRIPES_EVENT = "updateCollection";

    private boolean collectionSet = false;

    public UpdateCollectionRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
        contactInfos = new ArrayList<ContactInfo>();
        alternateIds = new ArrayList<String>();
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
        collection.setAlternateIds(alternateIds);
        collection.setContactInfoList(contactInfos);
        collection.setCreators(creators);
        return collection;
    }

    public void setCollection(Collection toCreate) {
        this.title = toCreate.getTitle();
        this.summary = toCreate.getSummary();
        this.id = toCreate.getId();
        this.contactInfos.addAll(toCreate.getContactInfoList());
        this.creators.addAll(toCreate.getCreators());
        this.alternateIds.addAll(toCreate.getAlternateIds());
        collectionSet = true;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getCollectionId() {
        return this.collectionId;
    }
    public HttpPost asHttpPost() {
        if (!collectionSet) {
            throw new IllegalStateException("Collection not set: call setCollection(Collection) first.");
        }
        if (collectionId == null) {
            throw new IllegalStateException("CollectionId must be set. call setCollectionId(id) first.");
        }

        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getUpdateCollectionUrl().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("collectionId", collectionId));
        params.add(new BasicNameValuePair("collection.title", title));
        params.add(new BasicNameValuePair("collection.summary", summary));
        params.add(new BasicNameValuePair("collection.id", id));
        for (int i=0; i < alternateIds.size(); i++) {
            params.add(new BasicNameValuePair("collection.alternateIds[" + i + "]", alternateIds.get(i)));
        }
        for (int i=0; i < creators.size(); i++) {
            params.add(new BasicNameValuePair("collection.creators[" + i + "].prefixes", creators.get(i).getPrefixes()));
            params.add(new BasicNameValuePair("collection.creators[" + i + "].givenNames", creators.get(i).getGivenNames()));
            params.add(new BasicNameValuePair("collection.creators[" + i + "].middleNames", creators.get(i).getMiddleNames()));
            params.add(new BasicNameValuePair("collection.creators[" + i + "].familyNames", creators.get(i).getFamilyNames()));
            params.add(new BasicNameValuePair("collection.creators[" + i + "].suffixes", creators.get(i).getSuffixes()));
        }
        for (int i=0; i < contactInfos.size(); i++) {
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].name", contactInfos.get(i).getName()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].role", contactInfos.get(i).getRole()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].physicalAddress.streetAddress", contactInfos.get(i).getPhysicalAddress().getStreetAddress()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].physicalAddress.city", contactInfos.get(i).getPhysicalAddress().getCity()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].physicalAddress.state", contactInfos.get(i).getPhysicalAddress().getState()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].physicalAddress.zipCode", contactInfos.get(i).getPhysicalAddress().getZipCode()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].physicalAddress.country", contactInfos.get(i).getPhysicalAddress().getCountry()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].emailAddress", contactInfos.get(i).getEmailAddress()));
            params.add(new BasicNameValuePair("collection.contactInfoList[" + i + "].phoneNumber", contactInfos.get(i).getPhoneNumber()));
        }
        params.add(new BasicNameValuePair(STRIPES_EVENT, "Update Collection"));

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
