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

package org.dataconservancy.ui.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.dataconservancy.ui.exceptions.EZIDServiceException;
import org.dataconservancy.ui.util.EZIDMetadata;


/** 
 * This implementation of the EZID service. This will generate an ez id with the provided metadata. 
 *
 */
public class EZIDServiceImpl implements EZIDService {
    HttpClient httpClient;
    String username;
    String password;
    String requestUrl;
    String namespace;
    
    public EZIDServiceImpl() {
        httpClient = new DefaultHttpClient();
    }

    @Override
    public String createID(EZIDMetadata metadata) throws EZIDServiceException {
        setCredentials();
        
        if (metadata == null) {
            throw new EZIDServiceException("Metadata must be set to create id");
        }
        
        HttpPost mintID = new HttpPost(buildUrl());
      
        StringEntity reqBody;
        try {
            reqBody = new StringEntity(metadata.serialize(), "text/plain", "UTF-8");
        } catch (Exception e) {
            throw new EZIDServiceException(e);
        }

        mintID.setEntity(reqBody);
        
        HttpResponse resp = null;
        
        try {
            resp = httpClient.execute(mintID);
        } catch (Exception e) {
            throw new EZIDServiceException(e);
        }
        
        String responseID = requestUrl;
        if (!responseID.endsWith("/")) {
            responseID +="/";
        }
        
        responseID += "id/";
        if (resp != null) {
            
            String response = "";
            HttpEntity respEntity = resp.getEntity();
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(respEntity.getContent(), writer);
            } catch (IllegalStateException e) {
                throw new EZIDServiceException(e);
            } catch (IOException e) {
                throw new EZIDServiceException(e);
            }
            
            response = writer.toString();
            
            if (resp.getStatusLine().getStatusCode() != 201) {
                throw new EZIDServiceException(resp.getStatusLine().getReasonPhrase() + " EZID " + response);
            } else {               
                String[] responseParts = response.split("\\s+");
                if (responseParts.length > 1 && responseParts[0].equalsIgnoreCase("success:")) { 
                    responseID += responseParts[1];
                } else {
                    throw new EZIDServiceException("Unexpected response: " + response);
                }
            }
        } else {
            throw new EZIDServiceException("Unexpected null response");
        }
        
        freeResponse(resp);
        return responseID;
    }

    @Override
    public void saveID(String id) throws EZIDServiceException {
        setCredentials();
        
        HttpPost saveID = new HttpPost(id);
        String status = "_status: public\n";
                        
        StringEntity reqBody = null;
        try {
            reqBody = new StringEntity(status, "text/plain", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new EZIDServiceException(e);
        }
        
        saveID.setEntity(reqBody);

        HttpResponse resp = null;
        try {
            resp = httpClient.execute(saveID);
        } catch (Exception e) {
            throw new EZIDServiceException(e);
        }
        
        String response = "";
        if (resp != null) {
            HttpEntity respEntity = resp.getEntity();
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(respEntity.getContent(), writer);
            } catch (IllegalStateException e) {
                throw new EZIDServiceException(e);
            } catch (IOException e) {
                throw new EZIDServiceException(e);
            }
            
            response = writer.toString();
        } else {
            throw new EZIDServiceException("Unexpected null response");
        }
        
        if (resp.getStatusLine().getStatusCode() != 200) {
             throw new EZIDServiceException(resp.getStatusLine().getReasonPhrase() + " EZID " + response);
        } else {
            String[] responseParts = response.split("\\s+");
            if (responseParts.length < 1 || !responseParts[0].equalsIgnoreCase("success:")) { 
                throw new EZIDServiceException("Unexpected response: " + response);
            } 
        }
        
        freeResponse(resp);
    }

    @Override
    public void deleteID(String id) throws EZIDServiceException {
        setCredentials();
        
        HttpDelete delete = new HttpDelete(id);
        
        HttpResponse resp = null;
        try {
            resp = httpClient.execute(delete);
        } catch (Exception e) {
            throw new EZIDServiceException(e);
        } 
        
        String response = "";
        if (resp != null) {
            HttpEntity respEntity = resp.getEntity();
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(respEntity.getContent(), writer);
            } catch (IllegalStateException e) {
                throw new EZIDServiceException(e);
            } catch (IOException e) {
                throw new EZIDServiceException(e);
            }
            
            response = writer.toString();
        } else {
            throw new EZIDServiceException("Unexpected null response");
        }            
        
        if (resp.getStatusLine().getStatusCode() != 200) {           
            throw new EZIDServiceException(resp.getStatusLine().getReasonPhrase() + " EZID " + response);
        } else {
           
            String[] responseParts = response.split("\\s+");
            if (responseParts.length < 1 || !responseParts[0].equalsIgnoreCase("success:")) { 
                throw new EZIDServiceException("Unexpected response: " + response);
            } 
        }
        
        freeResponse(resp);
    }  
    
    private void setCredentials() {
        if (httpClient instanceof DefaultHttpClient) {
            ((DefaultHttpClient) httpClient).getCredentialsProvider().setCredentials( AuthScope.ANY,
                                                            new UsernamePasswordCredentials(username, password));
        }
    }
    
    private String buildUrl() {
        String url = requestUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        
        url += "shoulder/" + namespace;
               
        return url;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setRequestUrl(String url) {
        this.requestUrl = url;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setHttpClient(HttpClient client) {
        httpClient = client;
    }
    
    private void freeResponse(HttpResponse response) {
        HttpEntity entity;
        if ((entity = response.getEntity()) != null) {
            try {
                final InputStream entityBody = entity.getContent();

                entityBody.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}