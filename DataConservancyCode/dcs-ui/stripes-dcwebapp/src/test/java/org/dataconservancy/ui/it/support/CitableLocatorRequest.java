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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * utility class to support reserve, confirm and cancel of citable locators
 */
public class CitableLocatorRequest {

    private String collectionId;
    private String reservedCitableLocator;

    private final UiUrlConfig urlConfig;

    private static final String RESERVE_STRIPES_EVENT = "reserveDOI";
    private static final String CONFIRM_STRIPES_EVENT = "confirmDOI";
    private static final String CANCEL_STRIPES_EVENT = "cancel";

    public CitableLocatorRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public void setReservedCitableLocator(String reservedCitableLocator) {
        this.reservedCitableLocator = reservedCitableLocator;
    }

    public String getReservedCitableLocator() {
        return reservedCitableLocator;
    }

    public HttpGet reserveAsHttpGet() throws MalformedURLException {

        HttpGet get = null;
        try {
            get = new HttpGet(urlConfig.getCitableLocatorGetUrl(collectionId, reservedCitableLocator).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if(collectionId == null){
            throw new RuntimeException("reserveAsHttpGet must not have a null collectionId.");
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("collectionId", collectionId));
        params.add(new BasicNameValuePair("reservedCitableLocator", reservedCitableLocator));
        params.add(new BasicNameValuePair(RESERVE_STRIPES_EVENT, "Reserve EZID"));

        return get;
    }

    public HttpPost confirmAsHttpPost() throws MalformedURLException {

        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getCitableLocatorPostUrl().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if(collectionId == null || reservedCitableLocator == null){
            throw new RuntimeException("confirmAsHttpPost must have non-null collectionID and reservedCitableLocator");
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("collectionId", collectionId));
        params.add(new BasicNameValuePair("reservedCitableLocator", reservedCitableLocator));
        params.add(new BasicNameValuePair(CONFIRM_STRIPES_EVENT, "Confirm EZID"));

        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        post.setEntity(entity);
        return post;
    }

    public HttpPost cancelAsHttpPost() throws MalformedURLException {

        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getCitableLocatorPostUrl().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if(collectionId == null || reservedCitableLocator == null){
            throw new RuntimeException("cancelAsHttpPost must have non-null collectionID and reservedCitableLocator");
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("collectionId", collectionId));
        params.add(new BasicNameValuePair("reservedCitableLocator", reservedCitableLocator));
        params.add(new BasicNameValuePair(CANCEL_STRIPES_EVENT, "Cancel EZID"));

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
