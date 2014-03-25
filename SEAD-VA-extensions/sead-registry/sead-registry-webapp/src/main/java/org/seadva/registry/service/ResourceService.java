/*
 * Copyright 2014 The Trustees of Indiana University
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

import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.IOUtils;
import org.seadva.registry.api.Resource;
import org.seadva.registry.api.ResourceType;
import org.seadva.registry.impl.registry.SeadRegistry;
import org.seadva.registry.impl.resource.ContainerResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * REST interface
 */

@Path("/resource")
public class ResourceService {

    public static SeadRegistry seadRegistry;
    @GET
    @Path("/{entityId}")
    @Produces("text/plain")
    public String getEntity(@PathParam("entityId") String id) throws Exception {
        Resource resource = seadRegistry.getResource(id);
        Gson gson = new Gson();

        String json = gson.toJson(resource);
        return json;

    }

    @POST
    @Path("/putCol")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response putContainerResource(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws IOException

    {

        StringWriter json = new StringWriter();

        IOUtils.copy(uploadedInputStream, json);

        String jsonString = json.toString();
        Gson gson = new Gson();

        Resource containerResource = gson.fromJson(jsonString, ContainerResource.class);

        seadRegistry.putResource(ResourceType.CONTAINER, containerResource);

        Response.ResponseBuilder responseBuilder = Response.ok();

        return responseBuilder.build();
    }

}
