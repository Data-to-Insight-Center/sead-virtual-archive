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

package org.seadva.registry.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.seadva.registry.database.model.obj.vaRegistry.*;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RegistryClient{

    static WebResource resource;
    static String serviceUrl;
    Gson gson;
    private static WebResource resource(){
       if(resource==null){
           Client client = Client.create();
           return client.resource(serviceUrl);
           //"http://localhost:8080/registry/rest/"
       }
       return resource;
    }

    public RegistryClient(String url){
        this.serviceUrl = url;
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    /**
     * GET methods
     *
     */

    public BaseEntity getEntity(String entityId, String type) throws IOException, ClassNotFoundException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                entityId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return (BaseEntity) gson.fromJson(writer.toString(), Class.forName(type));
    }

    public Collection getCollection(String collectionId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                collectionId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return gson.fromJson(writer.toString(), Collection.class);
    }


    public File getFile(String fileId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                fileId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return gson.fromJson(writer.toString(), File.class);
    }

    public List<AggregationWrapper> getAggregation(String parentId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("aggregation")
                .path(
                        URLEncoder.encode(
                                parentId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        Type listType = new TypeToken<ArrayList<AggregationWrapper>>() {
        }.getType();
        return gson.fromJson(writer.toString(), listType);
    }

    public MetadataType getMetadataType(String entityId) throws IOException, ClassNotFoundException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("metadata")
                .path(
                        URLEncoder.encode(
                                entityId
                        )
                )
                .queryParams(params)
                .get(ClientResponse.class);

        StringWriter writer = new StringWriter();
        IOUtils.copy(response.getEntityInputStream(), writer);
        return (MetadataType) gson.fromJson(writer.toString(), MetadataType.class);
    }

    public MetadataType getMetadataByType(String element) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        ClientResponse response = webResource.path("resource")
                .path("metadataType")
                .path(
                      element
                )
                .queryParams(params)
                .get(ClientResponse.class);

        MetadataType metadataType = null;

        if(response.getStatus()==200){
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntityInputStream(), writer);
            metadataType = (MetadataType) gson.fromJson(writer.toString(), MetadataType.class);

        }
        else if(response.getStatus()==404){
           ;//do nothing
        }
        return metadataType;
    }

    /**
     * POST (Create)  Test cases
     *
     */

    public int postCollection(Collection collection) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(collection));
        params.put("entity",values);

        List<String> types = new ArrayList<String>();
        types.add("org.seadva.registry.database.model.obj.vaRegistry.Collection");

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                collection.getId()
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);

        return response.getStatus();
    }


    public int postFile(File file) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(file));
        params.put("entity",values);

        List<String> types = new ArrayList<String>();
        types.add(File.class.getName());

        params.put("type", types);
        ClientResponse response = webResource.path("resource")
                .path(
                        URLEncoder.encode(
                                file.getId()
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);
        return response.getStatus();
    }

    /**
     *
     * @param aggregationWrappers
     * @param parentId
     * @return
     * @throws IOException
     */
    public int postAggregation(List<AggregationWrapper> aggregationWrappers, String parentId) throws IOException {
        WebResource webResource = resource();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        List<String> values = new ArrayList<String>();
        values.add(gson.toJson(aggregationWrappers));
        params.put("aggList",values);

        ClientResponse response = webResource.path("resource")
                .path("aggregation")
                .path(
                        URLEncoder.encode(
                                parentId
                        )
                )
                .queryParams(params)
                .post(ClientResponse.class);
        return response.getStatus();
    }
}