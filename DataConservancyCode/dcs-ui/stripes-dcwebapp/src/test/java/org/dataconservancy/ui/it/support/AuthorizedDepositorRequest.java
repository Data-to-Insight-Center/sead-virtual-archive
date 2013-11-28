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

import java.io.UnsupportedEncodingException;

import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

public class AuthorizedDepositorRequest {
    
    private String userId;
    private String collectionId;
    private boolean userAuthorized = false;
    
    private final UiUrlConfig urlConfig;
    
    private static final String STRIPES_EVENT = "editDepositors";
    
    public AuthorizedDepositorRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }
    
    public void setAuthorizedUserForCollection(String userID, String collectionID){
        userAuthorized = true;
        this.userId = userID;
        this.collectionId = collectionID;
    }
    
    public void removeAuthorizedUserFromCollection(String userID, String collectionID){
        userAuthorized = false;
        this.userId = userID;
        this.collectionId = collectionID;
    }
    
    public String getAuthorizedUser(){
        if( !userAuthorized){
            throw new IllegalStateException("Authorized User not set up. Call set authorized user for collection");
        }
        
        return userId;
    }
    
    public String getUnAuthorizedUser(){
        if( userAuthorized){
            throw new IllegalStateException("User still authorized for collection. Call remove authorized user first.");
        }
        
        return userId;
    }
    
    public String getCollectionId(){
        return collectionId;
    }
    
    public HttpPost asHttpPost() {
        if (collectionId == null || collectionId.isEmpty() || userId == null || userId.isEmpty() ) {
            throw new IllegalStateException("Collection ID, or User ID not set: Call setAuthorizedUser or removeAuthorizedUser first");
        }

        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getUserCollectionsUrl().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("selectedCollectionId", collectionId));
        if( userAuthorized ){
            params.add(new BasicNameValuePair("userIdsToAdd", userId));
        }else {
            params.add(new BasicNameValuePair("userIdsToRemove", userId));
        }
        params.add(new BasicNameValuePair(STRIPES_EVENT, "Edit Depositors"));

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