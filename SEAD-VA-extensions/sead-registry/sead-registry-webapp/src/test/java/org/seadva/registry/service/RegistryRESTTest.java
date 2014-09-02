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


import com.sun.jersey.api.client.Client;
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
public class RegistryRESTTest extends JerseyTest {

    public RegistryRESTTest() throws Exception {
        super("org.seadva.registry");

    }


    /**
     * POST (Update) Test cases
     *
     */

    @Test
    public void testPostCollection() throws IOException {
        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        String json =
                "{\"isObsolete\":0,\"name\":\"Vortex2 Visualization\",\"state\":{\"stateName\":\"CuO\",\"stateType\":\"CurationObject\",\"dataIdentifiers\":[],\"dataLocations\":[],\"id\":\"state:2\",\"properties\":[]},\"versionNum\":\"1\",\"entityCreatedTime\":\"Apr 9, 2014 1:38:13 PM\",\"entityLastUpdatedTime\":\"Apr 9, 2014 1:38:13 PM\",\"entityName\":\"Vortex2 Visualization\",\"id\":\"http://sead-test/fakeUri/0489a707-d428-4db4-8ce0-1ace548bc653\",\"properties\":[{\"metadata\":{\"id\":\"md:6\",\"metadataElement\":\"creator\",\"metadataSchema\":\"http://purl.org/dc/terms/\"},\"valuestr\":\"Quan Zhou\"},{\"metadata\":{\"id\":\"md:4\",\"metadataElement\":\"isDocumentedBy\",\"metadataSchema\":\"http://purl.org/spar/cito/\"},\"valuestr\":\"http://sead-test/fakeUri/944e89f2-5f91-4f6b-ac46-60f076fc3e25\"},{\"metadata\":{\"id\":\"md:3\",\"metadataElement\":\"publisher\",\"metadataSchema\":\"http://purl.org/dc/terms/\"},\"valuestr\":\"http://d2i.indiana.edu/\"},{\"metadata\":{\"id\":\"md:6\",\"metadataElement\":\"creator\",\"metadataSchema\":\"http://purl.org/dc/terms/\"},\"valuestr\":\"Quan Zhou\"},{\"metadata\":{\"id\":\"md:12\",\"metadataElement\":\"abstract\",\"metadataSchema\":\"http://purl.org/dc/terms/\"},\"valuestr\":\"The Vortex2 project (http://www.vortex2.org/home/) supported 100 scientists using over 40 science support vehicles participated in a nomadic effort to understand tornados. For the six weeks from May 1st to June 15th, 2010, scientists went roaming from state-to-state following severe weather conditions. With the help of meteorologists in the field who initiated boundary conditions, LEAD II (https://portal.leadproject.org/gridsphere/gridsphere) delivered six forecasts per day, starting at 7am CDT, creating up to 600 weather images per day. This information was used by the VORTEX2 field team and the command and control center at the University of Oklahoma to determine when and where tornadoes are most likely to occur and to help the storm chasers get to the right place at the right time. VORTEX2 used an unprecedented fleet of cutting edge instruments to literally surround tornadoes and the supercell thunderstorms that form them. An armada of mobile radars, including the Doppler On Wheels (DOW) from the Cente\"}]}";


        List<String> values = new ArrayList<String>();
        values.add(json);
        params.put("entity", values);

        List<String> types = new ArrayList<String>();
        types.add("org.seadva.registry.database.model.obj.vaRegistry.Collection");

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
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
    public void testPostFile() throws IOException {
        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        String json =
                "{\"fileName\":\"Vortex2_Visualization.xml\",\"isObsolete\":0,\"sizeBytes\":-1,\"versionNum\":\"1\",\"entityCreatedTime\":\"Apr 9, 2014 1:38:14 PM\",\"entityLastUpdatedTime\":\"Apr 9, 2014 1:38:14 PM\",\"entityName\":\"Vortex2_Visualization.xml\",\"id\":\"http://sead-test/fakeUri/844e89f2-5f91-4f6b-ac46-60f076fc3e25\",\"properties\":[{\"metadata\":{\"id\":\"md:12\",\"metadataElement\":\"abstract\",\"metadataSchema\":\"http://purl.org/dc/terms/\"},\"valuestr\":\"This is an OPM metadata file created by the author\"},{\"metadata\":{\"id\":\"md:12\",\"metadataElement\":\"abstract\",\"metadataSchema\":\"http://purl.org/dc/terms/\"},\"valuestr\":\"This is an OPM metadata file created by the author\"}]}";

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
                "[{\"child\":{\"id\":\"http://sead-test/fakeUri/844e89f2-5f91-4f6b-ac46-60f076fc3e25\",\"properties\":[]},\"parent\":{\"id\":\"http://sead-test/fakeUri/0489a707-d428-4db4-8ce0-1ace548bc653\",\"properties\":[]},\"childType\":\"org.seadva.registry.database.model.obj.vaRegistry.File\",\"parentType\":\"org.seadva.registry.database.model.obj.vaRegistry.Collection\"}]";

        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

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

    /**
     * GET Test cases
     *
     */

    @Test
    public void testGetCollection() throws IOException {
        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("entity")
                .path(
                        URLEncoder.encode(
                                "http://sead-test/fakeUri/0489a707-d428-4db4-8ce0-1ace548bc653"
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
    public void testGetFile() throws IOException {
        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("entity")
                .path(
                        URLEncoder.encode(
                                "http://sead-test/fakeUri/944e89f2-5f91-4f6b-ac46-60f076fc3e25"
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
    public void testGetAggregation() throws IOException {
        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("aggregation")
                .path(
                        URLEncoder.encode(
                                "http://sead-test/fakeUri/0489a707-d428-4db4-8ce0-1ace548bc653"
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
        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

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
    public void testGetAllCollections() throws IOException {
        WebResource webResource = Client.create().resource("http://localhost:8080/registry/rest");

        ClientResponse response = webResource.path("resource")
                .path("listCollections")
                .path("CurationObject")
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        System.out.print(writer.toString());
        assertEquals(200, response.getStatus());
    }



}