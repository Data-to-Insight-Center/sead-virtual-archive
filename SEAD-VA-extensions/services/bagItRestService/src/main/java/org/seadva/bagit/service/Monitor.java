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
package org.seadva.bagit.service;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.OREException;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.model.ActiveWorkspace;
import org.seadva.bagit.model.ActiveWorkspaces;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.util.Constants;
import org.seadva.bagit.util.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * ping service
 */
@Path("/monitor")
public class Monitor {


    @GET
    @Path("/ping")
    public Response ping(){
        javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.ok();
        return responseBuilder.build();
    }

}
