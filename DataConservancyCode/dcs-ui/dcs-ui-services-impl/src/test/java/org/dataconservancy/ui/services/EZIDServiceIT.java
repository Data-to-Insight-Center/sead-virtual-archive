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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.ui.exceptions.EZIDMetadataException;
import org.dataconservancy.ui.exceptions.EZIDServiceException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.PersonName;
import org.dataconservancy.ui.util.EZIDMetadata;
import org.dataconservancy.ui.util.EZIDMetadataGenerator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests that communication with the external EZID service is successful.  
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/test-applicationContext.xml" })
public class EZIDServiceIT {
    
    @Autowired
    private EZIDService ezidService;
    
    private EZIDMetadata ezidMetadata;
    
    private HttpClient httpClient;
    
    @Autowired
    private EZIDMetadataGenerator<Collection> ezidCollectionMetadataGenerator;
    
    @Before
    public void setup() throws EZIDMetadataException {
        Collection collection = new Collection();
        collection.setTitle("this is the title");
        collection.setSummary("this is the summary");
        collection.setId("collection:id");
        collection.setPublicationDate(new DateTime("2011-11-8"));
        PersonName creator = new PersonName();
        creator.setFamilyNames("Smith Doe");
        creator.setGivenNames("Joe");
        collection.addCreator(creator);
        
        ezidMetadata = ezidCollectionMetadataGenerator.generateMetadata(collection);
        httpClient = new DefaultHttpClient();
        
        assertNotNull(ezidMetadata);
        
    }
    
    /**
     * Tests that an id is created without exception, and the id returned is not empty.
     * @throws EZIDServiceException
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @Test
    public void testCreateId() throws EZIDServiceException, ClientProtocolException, IOException {
        String id = ezidService.createID(ezidMetadata);
        assertFalse(id.isEmpty());
       
        HttpGet getID = new HttpGet(id);
        HttpResponse resp = httpClient.execute(getID);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        
        HttpEntity respEntity = resp.getEntity();
        
        StringWriter writer = new StringWriter();
        IOUtils.copy(respEntity.getContent(), writer); 
        String response = writer.toString();
        HashMap<String, String> metadata = parseResponse(response);
        
        assertTrue(metadata.containsKey("_status"));
        assertEquals("reserved", metadata.get("_status"));
    }
    
    /**
     * Tests that an ID can be saved without exception. Tests that the status is changed from pending to reserved. 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @Test
    public void testSave() throws ClientProtocolException, IOException {
        boolean caughtException = false;
        String id = "";
        HttpGet getID = null;
        String target = "";
        try {
            id = ezidService.createID(ezidMetadata);
            
            getID = new HttpGet(id);
            HttpResponse resp = httpClient.execute(getID);
            assertEquals(200, resp.getStatusLine().getStatusCode());
            
            HttpEntity respEntity = resp.getEntity();
            
            StringWriter writer = new StringWriter();
            IOUtils.copy(respEntity.getContent(), writer); 
            String response = writer.toString();
            HashMap<String, String> metadata = parseResponse(response);
            
            assertTrue(metadata.containsKey("_status"));
            assertEquals("reserved", metadata.get("_status"));
            
            assertTrue(metadata.containsKey("_target"));
            target = metadata.get("_target");
         
            ezidService.saveID(id);
        } catch (EZIDServiceException e) {
            caughtException = true;
        }
        
        assertFalse(caughtException);
        
        HttpResponse resp = httpClient.execute(getID);
        assertEquals(200, resp.getStatusLine().getStatusCode());
        
        HttpEntity respEntity = resp.getEntity();
        
        StringWriter writer = new StringWriter();
        IOUtils.copy(respEntity.getContent(), writer); 
        String response = writer.toString();
        HashMap<String, String> metadata = parseResponse(response);
        
        assertTrue(metadata.containsKey("_status"));
        assertEquals("public", metadata.get("_status"));
        
        //Make sure other metadata hasn't changed
        assertTrue(metadata.containsKey("_target"));
        assertEquals(target, metadata.get("_target"));
    }
    
    /**
     * Tests that an ID can be deleted without exception. Tests that the id can no longer be retrieved after it is deleted.
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @Test
    public void testDelete() throws ClientProtocolException, IOException {
        boolean caughtException = false;
        String id = "";
        HttpGet getID = null;
        try {
            id = ezidService.createID(ezidMetadata);
            getID = new HttpGet(id);
            //Assert that the id can be found after being created.
            HttpResponse resp = httpClient.execute(getID);
            assertEquals(200, resp.getStatusLine().getStatusCode());
            
            HttpEntity respEntity = resp.getEntity();
            
            StringWriter writer = new StringWriter();
            IOUtils.copy(respEntity.getContent(), writer); 
            String response = writer.toString();
            HashMap<String, String> metadata = parseResponse(response);
            
            assertTrue(metadata.containsKey("_status"));
            assertEquals("reserved", metadata.get("_status"));
            
            ezidService.deleteID(id);

        } catch (EZIDServiceException e) {
            caughtException = true;
        }
        
        assertFalse(caughtException);        
       
        HttpResponse resp = httpClient.execute(getID);
        assertEquals(400, resp.getStatusLine().getStatusCode());
        
        HttpEntity respEntity = resp.getEntity();
        
        StringWriter writer = new StringWriter();
        IOUtils.copy(respEntity.getContent(), writer); 
        String response = writer.toString();
        
        assertTrue(response.contains("no such identifier"));
    }
    
    /**
     * Tests that you get a not found exception if the EZID url isn't properly set.
     */
    @Test
    public void testCreateBadEZIDUrl() {
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("apitest");
        service.setPassword("apitest");
        service.setRequestUrl("http://www.google.com");
        boolean caughtException = false;
        try {
            service.createID(ezidMetadata);
        } catch (EZIDServiceException e) {
            caughtException = true;
        }
        
        assertTrue(caughtException);   
        
    }
    
    /**
     * Tests that you get an unathorized exception if the user name isn't correct.
     */
    @Test
    public void testCreateBadEZIDUsername() {
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("foo");
        service.setPassword("apitest");
        service.setRequestUrl("https://n2t.net/ezid/");
        boolean caughtException = false;
        try {
            service.createID(ezidMetadata);
        } catch (EZIDServiceException e) {
            assertEquals("UNAUTHORIZED EZID error: unauthorized - authentication failure", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);   
        
    }
    
    /**
     * Tests that an unauthorized exception is returned if the username isn't correct.
     */
    @Test
    public void testCreateBadEZIDPassword() {
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("apitest");
        service.setPassword("foo");
        service.setRequestUrl("https://n2t.net/ezid/");
        boolean caughtException = false;
        try {
            service.createID(ezidMetadata);
        } catch (EZIDServiceException e) {
            assertEquals("UNAUTHORIZED EZID error: unauthorized - authentication failure", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);   
        
    }
    
    /**
     * Tests that a bad request exception is returned when namespace isn't correct.
     */
    @Test
    public void testCreateBadEZIDNamespace() {
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("namespace");
        service.setUsername("apitest");
        service.setPassword("apitest");
        service.setRequestUrl("https://n2t.net/ezid/");
        boolean caughtException = false;
        try {
            service.createID(ezidMetadata);
        } catch (EZIDServiceException e) {
            assertEquals("BAD REQUEST EZID error: bad request - unrecognized identifier scheme", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);          
    }
    
    /**
     * Tests that if a bad id is passed to save you get a EZIDServiceException
     */
    @Test
    public void testSaveBadID() {
        
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("apitest");
        service.setPassword("apitest");
        service.setRequestUrl("http://www.google.com");
        boolean caughtException = false;
        try {
            service.saveID("foo");
        } catch (EZIDServiceException e) {
            assertEquals("java.lang.IllegalStateException: Target host must not be null, or set in parameters.", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);           
    }
    
    /**
     * Tests that if username is changed before saving an unauthorized exception is thrown.
     * @throws EZIDServiceException
     */
    @Test
    public void testSaveBadEZIDUsername() throws EZIDServiceException {
        
        String id = ezidService.createID(ezidMetadata);
        
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("foo");
        service.setPassword("apitest");
        service.setRequestUrl("https://n2t.net/ezid/");
        boolean caughtException = false;
        try {
            service.saveID(id);
        } catch (EZIDServiceException e) {
            assertEquals("UNAUTHORIZED EZID error: unauthorized - authentication failure", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);           
    }
    
    /**
     * Tests that if password is changed before saving an unauthorized exception is thrown.
     * @throws EZIDServiceException
     */
    @Test
    public void testSaveBadEZIDPassword() throws EZIDServiceException {
        
        String id = ezidService.createID(ezidMetadata);
        
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("apitest");
        service.setPassword("foo");
        service.setRequestUrl("https://n2t.net/ezid/");
        boolean caughtException = false;
        try {
            service.saveID(id);
        } catch (EZIDServiceException e) {
            assertEquals("UNAUTHORIZED EZID error: unauthorized - authentication failure", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);           
    }
      
    /**
     * Tests that if a bad id is passed to delete an EZIDServiceException is thrown.
     */
    @Test
    public void testDeleteBadID() {
       
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("apitest");
        service.setPassword("apitest");
        service.setRequestUrl("http://www.google.com");
        boolean caughtException = false;
        try {
            service.deleteID("foo");
        } catch (EZIDServiceException e) {
            assertEquals("java.lang.IllegalStateException: Target host must not be null, or set in parameters.", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);           
    }
    
    /**
     * Tests that if user name is changed before delete an authorization exception is thrown.
     * @throws EZIDServiceException
     */
    @Test
    public void testDelteBadEZIDUsername() throws EZIDServiceException {
        
        String id = ezidService.createID(ezidMetadata);
        
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("foo");
        service.setPassword("apitest");
        service.setRequestUrl("https://n2t.net/ezid/");
        boolean caughtException = false;
        try {
            service.deleteID(id);
        } catch (EZIDServiceException e) {
            assertEquals("UNAUTHORIZED EZID error: unauthorized - authentication failure", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);           
    }
    
    /**
     * Tests that if password is changed before delete an authorization is thrown. 
     * @throws EZIDServiceException
     */
    @Test
    public void testDeleteBadEZIDPassword() throws EZIDServiceException {
        
        String id = ezidService.createID(ezidMetadata);
        
        EZIDServiceImpl service = new EZIDServiceImpl();
        service.setNamespace("doi:10.5072/FK2");
        service.setUsername("apitest");
        service.setPassword("foo");
        service.setRequestUrl("https://n2t.net/ezid/");
        boolean caughtException = false;
        try {
            service.deleteID(id);
        } catch (EZIDServiceException e) {
            assertEquals("UNAUTHORIZED EZID error: unauthorized - authentication failure", e.getMessage());
            caughtException = true;
        }
        
        assertTrue(caughtException);           
    }
    
    private String unescape (String s) {
        StringBuffer b = new StringBuffer();
        int i;
        while ((i = s.indexOf("%")) >= 0) {
          b.append(s.substring(0, i));
          b.append((char) Integer.parseInt(s.substring(i+1, i+3), 16));
          s = s.substring(i+3);
        }
        b.append(s);
        return b.toString();
    }

    private HashMap<String, String> parseResponse(String response) {
        HashMap<String, String> metadata = new HashMap<String, String>();
        for (String l : response.split("[\\r\\n]+")) {
            String[] kv = l.split(":", 2);
            metadata.put(unescape(kv[0]).trim(), unescape(kv[1]).trim());
        }
        
        return metadata;
    }
    
}