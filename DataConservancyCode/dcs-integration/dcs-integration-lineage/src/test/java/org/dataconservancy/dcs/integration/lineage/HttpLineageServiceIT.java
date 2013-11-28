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
package org.dataconservancy.dcs.integration.lineage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:test-applicationContext.xml"})
public class HttpLineageServiceIT {

    private final static String BASE_URL = "/lineage/";
    
    //HttpClient to use for testing url calls
    @Autowired
    private HttpClient httpClient;
    
    @Autowired
    private DcsModelBuilder dcsModelBuilder;
    
    //A list of the entity ids in order so we can check the returned lineage.
    private ArrayList<String> entityIds;
    private ArrayList<String> singleEntityList;
    
    private static final String ORIGINAL_ENTITY_ID = "http://localhost:8080/entity/original_entity";
    private static final String MIDDLE_ENTITY_ID = "http://localhost:8080/entity/middle_entity";
    private static final String LATEST_ENTITY_ID = "http://localhost:8080/entity/latest_entity";
    private static final String SINGLE_ENTITY_ID = "http://localhost:8080/entity/single_entity";

    private static final String LINEAGE_ID = "lineage";
    private static final String SINGLE_LINEAGE_ID = "single_lineage";
    
    private final static String NONEXISTENT_ENTITY_ID = "idString";

    private final static String ORIGINAL_INGEST_DATE = "2012-01-01T10:59:59Z";
    private final static long ORIGINAL_INGEST_DATE_MILLIS = 1325415599000l;

    private final static long MIDDLE_INGEST_DATE_MILLIS = 1326020399000l;

    private final static String LATEST_INGEST_DATE = "2012-01-25T10:59:59Z";
    private final static long LATEST_INGEST_DATE_MILLIS = 1327489199000l;

    private final static long SINGLE_ENTITY_INGEST_DATE_MILLIS = 1325501999000l;

    @Before
    public void setup() throws Exception{
        //Add the entity ids in descending order to match the lineage service impl return order
        entityIds = new ArrayList<String>();
        entityIds.add(LATEST_ENTITY_ID);
        entityIds.add(MIDDLE_ENTITY_ID);
        entityIds.add(ORIGINAL_ENTITY_ID);
        
        singleEntityList = new ArrayList<String>();
        singleEntityList.add(SINGLE_ENTITY_ID);
        
        //Set up hc for the tests
        httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
    }
    
    /**
     * Tests getting the lineage using an id. 
     * Tests cases: using an entity id, using a lineage id,
     * using an id whose entity or lineage can't be found, retrieving a single entity lineage
     * @throws IOException 
     * @throws ClientProtocolException 
     * @throws InvalidXmlException 
     * @throws IllegalStateException 
     * @throws URISyntaxException 
     */
    @Test
    public void testGetLineage() throws ClientProtocolException, IOException, IllegalStateException, InvalidXmlException, URISyntaxException {
        HttpGet request = buildRequest("", ORIGINAL_ENTITY_ID, "");
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for: " + ORIGINAL_ENTITY_ID, 404, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        request = buildRequest("", LINEAGE_ID, "");
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for: " + LINEAGE_ID, 200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        request = buildRequest("", NONEXISTENT_ENTITY_ID, "");
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for: " + NONEXISTENT_ENTITY_ID, 404, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
                                                
    /**
     * Tests the entry range call in the lineage API. 
     * Tests cases: getting an entire lineage, getting a subset of a lineage,
     * passing the first argument as null, passing the second argument as null, entities from two different lineages, entities that are not in order,
     * and testing an id that can't be found, retrieving a single entity lineage
     * @throws IOException 
     * @throws IllegalStateException 
     * @throws InvalidXmlException 
     * @throws URISyntaxException 
     */
    @Test
    public void testGetLineageForEntityRange() throws IllegalStateException, IOException, InvalidXmlException, URISyntaxException {
        //Try getting the entire lineage
        HttpGet request = buildRequest("search", "", "from", ORIGINAL_ENTITY_ID, "to", LATEST_ENTITY_ID);
        HttpResponse authorizedResponse = httpClient.execute(request);
  
        assertEquals("Unable to get lineage for range: " + ORIGINAL_ENTITY_ID + " to " + LATEST_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test getting a subset of the lineage
        request = buildRequest("search", "", "from", ORIGINAL_ENTITY_ID, "to", MIDDLE_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_ENTITY_ID + " to " + MIDDLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(1, entityIds.size()));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test passing in null for the first argument
        request = buildRequest("search", "", "to", MIDDLE_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: null to " + MIDDLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(1, entityIds.size()));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test passing in null for the second argument
        request = buildRequest("search", "", "from", ORIGINAL_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: from " + ORIGINAL_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test using single entity lineage
        request = buildRequest("search", "", "to", SINGLE_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: to " + SINGLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, singleEntityList);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        request = buildRequest("search", "", "from", SINGLE_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: from " + SINGLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, singleEntityList);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test passing in entities in the wrong order
        request = buildRequest("search", "", "from", LATEST_ENTITY_ID, "to", ORIGINAL_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + LATEST_ENTITY_ID + " to " + ORIGINAL_ENTITY_ID, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);

        //Test passing in id for a non-existent entity
        request = buildRequest("search", "", "from", NONEXISTENT_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + NONEXISTENT_ENTITY_ID, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
    
    /**
     * Tests the date range call in the API. 
     * Test cases: getting an entire lineage, returning a subset of the lineage, null first argument, null second argument,
     * retrieving a single entity lineage, passing in dates in the wrong order, first and second date are same as entity timestamp, 
     * dates that contain no lineage, passing in entity id that can't be found.
     * @throws IOException 
     * @throws ClientProtocolException 
     * @throws InvalidXmlException 
     * @throws IllegalStateException 
     * @throws URISyntaxException 
     */
    @Test
    public void testGetLineageForDateRange() throws ClientProtocolException, IOException, IllegalStateException, InvalidXmlException, URISyntaxException {
        
        //Test getting the entire lineage
        HttpGet request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, 
                                  "from", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS), "to", String.valueOf(LATEST_INGEST_DATE_MILLIS));
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS + " to " + LATEST_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);

        //Test getting a subset of the lineage
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, 
                         "from", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS), "to", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS + " to " + MIDDLE_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(1, entityIds.size()));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
         
        //Test passing in null for the first argument
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, "to", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: to " + MIDDLE_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(1, entityIds.size()));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);      
        
        //Test passing in null for the second argument
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, "from", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: from " + MIDDLE_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(0, 2));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test single entity lineage
        request = buildRequest("search", "", "id", SINGLE_ENTITY_ID, "to", String.valueOf(SINGLE_ENTITY_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: id: " + SINGLE_ENTITY_ID + " to " + MIDDLE_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, singleEntityList);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);

        //Test single entity lineage
        request = buildRequest("search", "", "id", SINGLE_ENTITY_ID, "from", String.valueOf(SINGLE_ENTITY_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: id: " + SINGLE_ENTITY_ID + " from " + MIDDLE_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, singleEntityList);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test passing in wrong order of dates
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, 
                         "from", String.valueOf(LATEST_INGEST_DATE_MILLIS), "to", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + LATEST_INGEST_DATE_MILLIS + " to " + ORIGINAL_INGEST_DATE_MILLIS, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test passing in identical dates equal to an entity's timestamp returns a lineage containing that entity
        request = buildRequest("search", "", "id", SINGLE_ENTITY_ID, 
                         "from", String.valueOf(SINGLE_ENTITY_INGEST_DATE_MILLIS), "to", String.valueOf(SINGLE_ENTITY_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS + " to " + LATEST_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, singleEntityList);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);

        //Test passing in dates with no entity contained  - should return an empty lineage, not null
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, "from", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS+1), "to", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS+10));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS +"1" + " to " + ORIGINAL_INGEST_DATE_MILLIS+"10", 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, new ArrayList<String>());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    
        //Test null lineage returned if entity is not found
        request = buildRequest("search", "", "id", NONEXISTENT_ENTITY_ID, "from", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS), "to", String.valueOf(LATEST_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS + " to " + LATEST_INGEST_DATE_MILLIS, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test passing in timestamps as dates
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, "from", ORIGINAL_INGEST_DATE, "to", LATEST_INGEST_DATE);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE + " to " + LATEST_INGEST_DATE, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test passing in mixed timestamps and millis
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, "from", ORIGINAL_INGEST_DATE, "to", String.valueOf(LATEST_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE + " to " + LATEST_INGEST_DATE_MILLIS, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test mixing timestamp and entity id
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, "from", ORIGINAL_INGEST_DATE, "to", LATEST_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE + " to " + LATEST_INGEST_DATE_MILLIS, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
    
    /**
     * Tests the call to get the latest of a lineage in the API.
     * Test cases: Passing in an entity id, passing in id of a single entity lineage, passing in an entity that doesn't exist
     * @throws IOException 
     * @throws IllegalStateException 
     * @throws InvalidXmlException 
     * @throws URISyntaxException 
     */
    @Test
    public void testGetLatest() throws IllegalStateException, IOException, InvalidXmlException, URISyntaxException {
        //Test getting the latest 
        HttpGet request = buildRequest("latest", "", "id", MIDDLE_ENTITY_ID);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get latest for: " + MIDDLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(0, 1));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);

        //Test single entity lineage
        request = buildRequest("latest", "", "id", SINGLE_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get latest for: " + SINGLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, singleEntityList);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);       

        //Test entity not found
        request = buildRequest("latest", "", "id", NONEXISTENT_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get latest for: " + NONEXISTENT_ENTITY_ID, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);   
        
        //Test get latest with datetime stamp after latest
        request = buildRequest("latest", "", "id", MIDDLE_ENTITY_ID, "ts", String.valueOf(LATEST_INGEST_DATE_MILLIS+100));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get latest for: " + MIDDLE_ENTITY_ID + " with timestamp: " + String.valueOf(LATEST_INGEST_DATE_MILLIS+100), 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(0, 1));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test get latest for middle entity 
        request = buildRequest("latest", "", "id", MIDDLE_ENTITY_ID, "ts", String.valueOf(MIDDLE_INGEST_DATE_MILLIS+100));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get latest for: " + MIDDLE_ENTITY_ID + " with timestamp: " + String.valueOf(MIDDLE_INGEST_DATE_MILLIS+100), 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(1, 2));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }
    
    /**
     * Tests the call to the original item of a lineage in the API.
     * Test cases: Passing in an entity id, passing in id of a single entity lineage, passing in id of an entity that doesn't exist. 
     * @throws IOException 
     * @throws IllegalStateException 
     * @throws InvalidXmlException 
     * @throws URISyntaxException 
     */
    @Test
    public void testGetOriginal() throws IllegalStateException, IOException, InvalidXmlException, URISyntaxException {
        //Test getting the original
        HttpGet request = buildRequest("original", "", "id", MIDDLE_ENTITY_ID);
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get original for: " + MIDDLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(entityIds.size()-1, entityIds.size()));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test single entity lineage
        request = buildRequest("original", "", "id", SINGLE_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get original for: " + SINGLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, singleEntityList);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);

        //Test null lineage returned if entity is not found
        request = buildRequest("original", "", "id", NONEXISTENT_ENTITY_ID);
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get original for: " + NONEXISTENT_ENTITY_ID, 
                     400, 499, authorizedResponse.getStatusLine().getStatusCode());
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
    }    
    
    /**
     * Tests the if modified since 
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws InvalidXmlException 
     * @throws IllegalStateException 
     */
    @Test
    public void testIfModifiedSince() throws URISyntaxException, IOException, IllegalStateException, InvalidXmlException {
        
        //Test search request with dates
        HttpGet request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, 
                                  "from", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS), "to", String.valueOf(LATEST_INGEST_DATE_MILLIS),
                                  "if-modified-since", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        HttpResponse authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS + " to " + LATEST_INGEST_DATE_MILLIS, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test search request with entity ids
        request = buildRequest("search", "", "from", ORIGINAL_ENTITY_ID, "to", MIDDLE_ENTITY_ID, 
                               "if-modified-since", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_ENTITY_ID + " to " + MIDDLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(1, entityIds.size()));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test get call with lineage
        request = buildRequest("", LINEAGE_ID, "if-modified-since", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for: " + LINEAGE_ID, 200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds);
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test if modified since of a query with no results since if modified is specified this should return a 304         
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, "from", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS+1), "to", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS+10),
                               "if-modified-since", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS +"1" + " to " + ORIGINAL_INGEST_DATE_MILLIS+"10", 
                     304, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);
     
        //Test search with if modified past newest
        request = buildRequest("search", "", "id", ORIGINAL_ENTITY_ID, 
                              "from", String.valueOf(ORIGINAL_INGEST_DATE_MILLIS), "to", String.valueOf(LATEST_INGEST_DATE_MILLIS),
                              "if-modified-since", String.valueOf(LATEST_INGEST_DATE_MILLIS+100));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for range: " + ORIGINAL_INGEST_DATE_MILLIS + " to " + LATEST_INGEST_DATE_MILLIS, 
                     304, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);
    
        //Test get with if modified past newest
        request = buildRequest("", LINEAGE_ID, "if-modified-since", String.valueOf(LATEST_INGEST_DATE_MILLIS+100));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get lineage for: " + LINEAGE_ID, 304, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);        
        
        //Test latest 
        request = buildRequest("latest", "", "id", MIDDLE_ENTITY_ID,
                               "if-modified-since", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get latest for: " + MIDDLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(0, 1));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test latest with if modified date past latest update
        request = buildRequest("latest", "", "id", MIDDLE_ENTITY_ID,
                               "if-modified-since", String.valueOf(LATEST_INGEST_DATE_MILLIS+100));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get latest for: " + MIDDLE_ENTITY_ID, 
                     304, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);
        
        //Test original
        request = buildRequest("original", "", "id", MIDDLE_ENTITY_ID,
                               "if-modified-since", String.valueOf(MIDDLE_INGEST_DATE_MILLIS));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get original for: " + MIDDLE_ENTITY_ID, 
                     200, authorizedResponse.getStatusLine().getStatusCode());
        parseAndTestResponse(authorizedResponse, entityIds.subList(entityIds.size()-1, entityIds.size()));
        authorizedResponse.getEntity().getContent().close();
        freeResponse(authorizedResponse);
        
        //Test original past latest update
        request = buildRequest("original", "", "id", MIDDLE_ENTITY_ID,
                               "if-modified-since", String.valueOf(LATEST_INGEST_DATE_MILLIS+199));
        authorizedResponse = httpClient.execute(request);
        assertEquals("Unable to get original for: " + MIDDLE_ENTITY_ID, 
                     304, authorizedResponse.getStatusLine().getStatusCode());
        freeResponse(authorizedResponse);
    }
    
    private void parseAndTestResponse(HttpResponse response, List<String> entityIds) throws IllegalStateException, IOException, InvalidXmlException {
        InputStream resultStream = response.getEntity().getContent();

        Dcp sip = dcsModelBuilder.buildSip(resultStream);

        //Check that that the expected lineage size and the returned lineage size are the same
        assertEquals(sip.getDeliverableUnits().size(), entityIds.size());
        
        int i = 0;
        for (DcsDeliverableUnit du : sip.getDeliverableUnits()) {
            //The ids will be minted in the bootstrapper just make sure it contains the expected id portion
            assertTrue(du.getId().contains(entityIds.get(i)));
            i++;
        }
        
    }
    
    /**
     * Builds an HttpGet to use in the test given the argument and the id. 
    
     * @param argument should be one of latest, original, search.
     * @param id The lineage or entity id of the object
     * @param optionalArgument can be if-modified-since or accept
     * @return The completed http request that can be passed to HttpClient
     * @throws UnsupportedEncodingException 
     * @throws URISyntaxException 
     */
    private HttpGet buildRequest(String argument, String id, String... optionalArguments) throws UnsupportedEncodingException, URISyntaxException {
        HttpGet request = null;
        
        String arguments = "/lineage";
        
        if (argument != null && !argument.isEmpty()) {
            arguments += "/" + argument;
        }
        if (id != null && !id.isEmpty()) {
            arguments += "/" + id;
        }
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String headerName = "";
        String headerValue = "";
        
        for (int i = 0; i < optionalArguments.length; i = i+2) {
            if (!optionalArguments[i].isEmpty()) {
                if (optionalArguments[i].equalsIgnoreCase("if-modified-since")) {
                    headerName = optionalArguments[i];
                    headerValue = optionalArguments[i+1];
                } else {
                    params.add(new BasicNameValuePair(optionalArguments[i], optionalArguments[i+1]));
                }
            }
        }
      
        URI uri = URIUtils.createURI("http", "localhost", 8080, arguments, 
                                     URLEncodedUtils.format(params, "UTF-8"), null);     
        
        request = new HttpGet(uri);
        if (headerName != null && !headerName.isEmpty()
                && headerValue != null && !headerValue.isEmpty()) {
            headerValue = DateUtility.toRfc822(Long.valueOf(headerValue));
            request.setHeader(headerName, headerValue);
        }
        return request;
    }
    
    public URL getBaseUrl() {
        URL u = null;
        try {
            u = new URL(
                    "http",
                    "localhost",
                    8080,
                    "/lineage");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return u;
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
