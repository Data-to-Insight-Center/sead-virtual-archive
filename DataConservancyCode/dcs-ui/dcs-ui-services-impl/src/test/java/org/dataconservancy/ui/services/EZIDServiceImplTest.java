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

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.exceptions.EZIDServiceException;
import org.dataconservancy.ui.util.EZIDCollectionMetadataGeneratorImpl;
import org.dataconservancy.ui.util.EZIDMetadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the returns from EZID service in correct and exceptional cases. Note: This test does not communicate with the actual EZID Service.
 *
 */
public class EZIDServiceImplTest extends BaseUnitTest {
    

    private EZIDServiceImpl ezidService;
    
    private EZIDMetadata metadata;
    
    @Before
    public void setup() throws EZIDMetadataException {
        ezidService = new EZIDServiceImpl();
        ezidService.setNamespace("namespace");
        ezidService.setPassword("apitest");
        ezidService.setUsername("username");
        ezidService.setRequestUrl("www.test.com");
    
        EZIDCollectionMetadataGeneratorImpl generator = new EZIDCollectionMetadataGeneratorImpl();
        generator.setContextPath("contextPath");
        generator.setHost("host");
        generator.setPort("port");
        generator.setPublisher("publisher");
        generator.setScheme("scheme");
        
        metadata = generator.generateMetadata(collectionWithData);
    }
    
    /**
     * Tests that the call to create ID is setup and called succesfully and properly handles the return in the form it will be provided by ezid.
     * @throws ClientProtocolException
     * @throws IOException
     * @throws EZIDServiceException
     */
    @Test
    public void testSuccessfulCreateId() throws ClientProtocolException, IOException, EZIDServiceException {
        HttpResponse mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 201, "created");
        StringEntity entity = new StringEntity("success: namespace:id");
        mockResponse.setEntity(entity);
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        String id = ezidService.createID(metadata);
        assertFalse(id.isEmpty());
        assertEquals("www.test.com/id/namespace:id", id);
    }
    
    /**
     * Tests that update is called succesfully without throwing exception
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testSuccessfulUpdate() throws ClientProtocolException, IOException {
        boolean noExceptions = true;
        HttpResponse mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 200, "ok");
        StringEntity entity = new StringEntity("success: namespace:id");
        mockResponse.setEntity(entity);
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        try {
            ezidService.saveID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
           noExceptions = false;
        }     
        
        assertTrue(noExceptions);
    }
    
    /**
     * Tests that delete is called succesfully without throwing exception
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testSuccessfulDelete() throws ClientProtocolException, IOException {
        boolean noExceptions = true;
        HttpResponse mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 200, "ok");
        StringEntity entity = new StringEntity("success: namespace:id");
        mockResponse.setEntity(entity);
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpDelete.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        try {
            ezidService.deleteID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
           noExceptions = false;
        }     
        
        assertTrue(noExceptions);
    }
    
    /**
     * Tests exceptional conditions when calling the create method, including bad return code and unexpect return format
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testCreateExceptions() throws ClientProtocolException, IOException {
        HttpResponse mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 201, "created");
        StringEntity entity = new StringEntity("success: namespace:id");
        mockResponse.setEntity(entity);
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenThrow(new ClientProtocolException("Expected exception"));

        ezidService.setHttpClient(mockHttpClient);
        
        boolean caughtException = false;
        try {
            ezidService.createID(metadata);
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertEquals("org.apache.http.client.ClientProtocolException: Expected exception", e.getMessage());
        }
        
        assertTrue(caughtException);
        
        mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 404, "not found");
        mockResponse.setEntity(entity);
        
        mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        caughtException = false;
        try {
            ezidService.createID(metadata);
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertTrue(e.getMessage().contains("not found"));
        }
        
        assertTrue(caughtException);

        mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 201, "created");
        entity = new StringEntity("namespace:id");
        mockResponse.setEntity(entity);
        
        mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        caughtException = false;
        try {
            ezidService.createID(metadata);
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertEquals("Unexpected response: namespace:id", e.getMessage());
        }
        
        assertTrue(caughtException);
    }    
    
    /**
     * Tests exceptional conditions when calling the save method, including bad return code and unexpect return format
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testSaveExceptions() throws ClientProtocolException, IOException {
        HttpResponse mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 200, "ok");
        StringEntity entity = new StringEntity("success: namespace:id");
        mockResponse.setEntity(entity);
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenThrow(new ClientProtocolException("Expected exception"));

        ezidService.setHttpClient(mockHttpClient);
        
        boolean caughtException = false;
        try {
            ezidService.saveID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertEquals("org.apache.http.client.ClientProtocolException: Expected exception", e.getMessage());
        }
        
        assertTrue(caughtException);
        
        mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 404, "not found");
        mockResponse.setEntity(entity);
        
        mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        caughtException = false;
        try {
            ezidService.saveID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertTrue(e.getMessage().contains("not found"));
        }
        
        assertTrue(caughtException);

        mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 200, "ok");
        entity = new StringEntity("namespace:id");
        mockResponse.setEntity(entity);
        
        mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        caughtException = false;
        try {
            ezidService.saveID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertEquals("Unexpected response: namespace:id", e.getMessage());
        }
        
        assertTrue(caughtException);
    }    
    
    /**
     * Tests exceptional conditions when calling the delete method, including bad return code and unexpect return format
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void testDeleteExceptions() throws ClientProtocolException, IOException {
        HttpResponse mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 200, "ok");
        StringEntity entity = new StringEntity("success: namespace:id");
        mockResponse.setEntity(entity);
        
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenThrow(new ClientProtocolException("Expected exception"));

        ezidService.setHttpClient(mockHttpClient);
        
        boolean caughtException = false;
        try {
            ezidService.deleteID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertEquals("org.apache.http.client.ClientProtocolException: Expected exception", e.getMessage());
        }
        
        assertTrue(caughtException);
        
        mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 404, "not found");
        mockResponse.setEntity(entity);
        
        mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        caughtException = false;
        try {
            ezidService.deleteID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertTrue(e.getMessage().contains("not found"));
        }
        
        assertTrue(caughtException);

        mockResponse = new BasicHttpResponse(new HttpVersion(1, 1), 200, "ok");
        entity = new StringEntity("namespace:id");
        mockResponse.setEntity(entity);
        
        mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);

        ezidService.setHttpClient(mockHttpClient);
        
        caughtException = false;
        try {
            ezidService.deleteID("www.test.com/id/namespace:id");
        } catch (EZIDServiceException e) {
            caughtException = true;
            assertEquals("Unexpected response: namespace:id", e.getMessage());
        }
        
        assertTrue(caughtException);
    }    
}