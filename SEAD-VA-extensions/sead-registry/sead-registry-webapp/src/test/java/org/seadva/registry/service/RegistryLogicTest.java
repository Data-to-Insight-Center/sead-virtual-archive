/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seadva.registry.service;


import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.test.framework.JerseyTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

//import org.seadva.registry.impl.registry.SeadRegistry;


@ContextConfiguration(locations = { "/applicationContext.xml" } )
public class RegistryLogicTest extends JerseyTest {

    public RegistryLogicTest() throws Exception {
        super("org.seadva.registry");

    }


    @Test
    public void testPostCollection() throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        String json =
               "{\"isObsolete\":1,\"name\":\"Eel River Steelhead Study\",\"state\":{\"stateName\":\"CuO\",\"stateType\":\"CurationObject\",\"dataIdentifiers\":[],\"dataLocations\":[],\"id\":\"state:2\",\"properties\":[]},\"dataIdentifiers\":[],\"dataLocations\":[],\"entityCreatedTime\":\"Jun 11, 2014 4:01:51 PM\",\"entityLastUpdatedTime\":\"Jun 11, 2014 4:01:51 PM\",\"entityName\":\"Eel River Steelhead Study\",\"id\":\"test_1\",\"properties\":[]}";


        List<String> values = new ArrayList<String>();
        values.add(json);
        params.put("entity", values);

        List<String> types = new ArrayList<String>();
        types.add("org.seadva.registry.database.model.obj.vaRegistry.Collection");

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                "test_1"
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);

        assertEquals(200, response.getStatus());
    }



    @Test
    public void testPostFile() throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        String json =
               "{\"isObsolete\":1,\"name\":\"Eel River Steelhead Study\",\"dataIdentifiers\":[],\"dataLocations\":[],\"entityCreatedTime\":\"Jun 11, 2014 4:01:51 PM\",\"entityLastUpdatedTime\":\"Jun 11, 2014 4:01:51 PM\",\"entityName\":\"Eel River Steelhead Study\",\"id\":\"http://seadva-test.d2i.indiana.edu:5667/sead-wf/entity/27925\",\"properties\":[]}";

        List<String> values = new ArrayList<String>();
        values.add(json);
        params.put("entity",values);

        List<String> types = new ArrayList<String>();
        types.add("org.seadva.registry.database.model.obj.vaRegistry.File");

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                "http://sead-test/fakeUri/844e89f2-5f91-4f6b-ac46-60f076fc3e25"
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testPostAggregation() throws IOException, ClassNotFoundException {
        String aggregationJson =
                "[{\"child\":{\"id\":\"http://seadva-test.d2i.indiana.edu:5667/sead-wf/entity/27925\",\"properties\":[]},\"parent\":{\"id\":\"test_1\",\"properties\":[]},\"childType\":\"org.seadva.registry.database.model.obj.vaRegistry.File\",\"parentType\":\"org.seadva.registry.database.model.obj.vaRegistry.Collection\"}]";

        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(aggregationJson);
        params.put("aggList",values);

        ClientResponse response = webResource.path("resource")
                .path("aggregation")
                .path(
                        URLEncoder.encode(
                                "http://sead-test/fakeUri/0489a707-d428-4db4-8ce0-1ace548bc653"
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);

        assertEquals(200, response.getStatus());
    }



    @Test
    public void testGetCollection() throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("collection")
                .path(
                        URLEncoder.encode(
                                "http://seadva-test.d2i.indiana.edu:5667/sead-wf/entity/27925"
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }


    @Test
    public void testGetMetadataType() throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("metadataType")
                .path(
                        "abstract"
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetIdentifierType() throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("identifiertype")
                .path(
                        "doi"
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRelationType() throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("relationType")
                .path(
                        "describes"
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetAllCollections() throws IOException {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("resource")
                .path("listCollections")
                .path("CurationObject")
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testMakeObsolete() throws IOException {
        WebResource webResource = resource();

        ClientResponse response = webResource.path("resource")
                .path("obsolete")
                .path(
                        URLEncoder.encode(
                        "http://localhost:8080/sead-wf/entity/2056"
                                )
                )
                .post(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }
}