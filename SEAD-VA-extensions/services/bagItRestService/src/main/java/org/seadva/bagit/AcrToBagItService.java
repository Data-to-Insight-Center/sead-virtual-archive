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

package org.seadva.bagit;

import org.apache.commons.io.IOUtils;
import org.dspace.foresite.OREException;
import org.sead.acr.common.utilities.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * BagIt service interface
 */

@Path("/acrToBag")
public class AcrToBagItService {



    public AcrToBagItService() {}


        private static final Logger log =
            LoggerFactory.getLogger(AcrToBagItService.class);


        //Do some time stamp based caching
        @Context
        ServletContext context;

        @GET
        @Path("/bag/{collectionId}")
        @Produces("application/zip")
        public Response getBag(@Context HttpServletRequest request,
                                 @Context UriInfo uri,
                                 @PathParam("collectionId") String collectionId,
                                 @QueryParam("sparqlEpEnum") int sparqlEndpoint
        ) throws IOException, OREException, URISyntaxException, JSONException

        {

        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1020\" pid=\""+collectionId+"\" nodeId=\"SEAD ACR\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: AcrToBagItService.getObject \n" +
                "</traceInformation>\n" +
                "</error>";

         if(Constants.homeDir==null){
             StringWriter writer = new StringWriter();
             IOUtils.copy(new FileInputStream(context.getRealPath("WEB-INF/Config.properties")), writer);
             String result = writer.toString();
             String[] pairs = result.trim().split(
                     "\\w*\n|\\=\\w*");


             for (int i = 0; i + 1 < pairs.length;) {
                 String name = pairs[i++].trim();
                 String value = pairs[i++].trim();
                 if (name.equals("bagit.home")) {
                     Constants.homeDir = value;
                     Constants.bagDir = Constants.homeDir+"bag/";
                     Constants.unzipDir = Constants.homeDir+"bag/"+"unzip/";
                 }
                 if (name.equals("acrusername")) {

                     Constants.acrusername = value.replace("/","");
                 }
                 if (name.equals("acrpassword")) {
                    Constants.acrpassword = value.replace("/","");
                 }
             }
         }

        AcrBagItConverter acrBagItConverter = new AcrBagItConverter();
         MediciInstance instance = null;
         for(MediciInstance t_instance:ServerConstants.acrInstances)
             if(t_instance.getId()==sparqlEndpoint)
                 instance = t_instance;
        String zippedBag = acrBagItConverter.convertRdfToBagit(collectionId,instance);

        if(zippedBag==null)
            throw new NotFoundException(test);

        String[] filename = zippedBag.split("/");

        Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(zippedBag));

        responseBuilder.header("Content-Type", "application/zip");
        responseBuilder.header("Content-Disposition",
                         "inline; filename=" + filename[filename.length-1]);
        return responseBuilder.build();
        }

    @GET
    @Path("/sip/{collectionId}")
    @Produces("*/*")
    public Response getSIP(@Context HttpServletRequest request,
                           @Context UriInfo uri,
                           @PathParam("collectionId") String collectionId
    ) throws IOException, OREException, URISyntaxException

    {

        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1020\" pid=\""+collectionId+"\" nodeId=\"SEAD ACR\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: AcrToBagItService.getObject \n" +
                "</traceInformation>\n" +
                "</error>";
        BagItSipConverter bagItSipConverter = new BagItSipConverter();
        String sipPath = bagItSipConverter.bagitToSIP(collectionId);

        if(sipPath == null)
            throw new NotFoundException(test);

        String[] filename = sipPath.split("/");

        Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(sipPath));

        responseBuilder.header("Content-Disposition",
                "inline; filename=" + filename[filename.length-1].replace(".xml",""));
        return responseBuilder.build();
    }

}
