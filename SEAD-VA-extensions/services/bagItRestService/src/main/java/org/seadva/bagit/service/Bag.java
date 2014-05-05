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
import org.seadva.bagit.util.NotFoundException;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.model.ActiveWorkspace;
import org.seadva.bagit.model.ActiveWorkspaces;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.net.URISyntaxException;

/**
 * Service to generate Bags for ACR collections
 */
@Path("/acrToBag")
public class Bag {

    private static final Logger log =
            LoggerFactory.getLogger(Bag.class);


    //ToDo: some time stamp based caching
    @Context
    ServletContext context;


    /**
     *
     * @param request
     * @param uri
     * @param collectionId
     * @param sparqlEndpoint
     * @return
     * @throws java.io.IOException
     * @throws org.dspace.foresite.OREException
     * @throws java.net.URISyntaxException
     * @throws org.sead.acr.common.utilities.json.JSONException
     *
     * Get/Create Bag from ACR Collection
     */

    @GET
    @Path("/bag/{collectionId}")
    @Produces("application/zip")
    public javax.ws.rs.core.Response getACRBag(@Context HttpServletRequest request,
                                            @Context UriInfo uri,
                                            @PathParam("collectionId") String collectionId,
                                            @QueryParam("sparqlEpEnum") int sparqlEndpoint
    ) throws IOException, OREException, URISyntaxException, JSONException, ClassNotFoundException, InstantiationException, IllegalAccessException

    {
        String test ="<error name=\"NotFound\" errorCode=\"404\" detailCode=\"1020\" pid=\""+collectionId+"\" nodeId=\"SEAD ACR\">\n" +
                "<description>The specified object does not exist on this node.</description>\n" +
                "<traceInformation>\n" +
                "method: AcrToBagItService.getObject \n" +
                "</traceInformation>\n" +
                "</error>";

        if(Constants.homeDir==null){
            StringWriter writer = new StringWriter();
            String path =  context.getRealPath("/WEB-INF/Config.properties");
            if(context.getInitParameter("testPath")!=null){
                if(path.contains("/"))
                    path = path.replace("/./",context.getInitParameter("testPath"));
                else if(path.contains("\\"))
                    path = path.replace("\\.\\",context.getInitParameter("testPath"));
            }


            IOUtils.copy(new FileInputStream(
                    path
            ), writer);
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
            }
        }

        String guid = null;
        if(collectionId.contains("/"))
            guid = collectionId.split("/")[collectionId.split("/").length-1];
        else
            guid = collectionId.split(":")[collectionId.split(":").length-1];

        String  unzippedBagPath = Constants.unzipDir+guid+"/";

        if(!new File(Constants.bagDir).exists()) {
            new File(Constants.bagDir).mkdirs();
        }
        if(!new File(unzippedBagPath).exists()) {
            new File(unzippedBagPath).mkdirs();
        }



        //String packageName, String bagPath, String unzippedBagPath
        new ConfigBootstrap().load();
        PackageDescriptor packageDescriptor = new PackageDescriptor("","", unzippedBagPath);
        packageDescriptor.setPackageId(collectionId);

        MediciInstance instance = null;
        for(MediciInstance t_instance: Constants.acrInstances)
            if(t_instance.getId()==sparqlEndpoint)
                instance = t_instance;
        packageDescriptor.setMediciInstance(instance);


        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_ACR_COLLECTION, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_FETCH, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_MANIFEST, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_DATA_DIR, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.ZIP_BAG, packageDescriptor);

        if(packageDescriptor.getBagPath()==null)
            throw new NotFoundException(test);


        javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.ok(new FileInputStream(packageDescriptor.getBagPath()));

        responseBuilder.header("Content-Type", "application/zip");
        responseBuilder.header("Content-Disposition",
                "inline; filename=" + packageDescriptor.getPackageId().substring(packageDescriptor.getPackageId().lastIndexOf("/")+1)+".zip");
        return responseBuilder.build();
    }


    /**
     *
     * List ACR instances
     */

    @GET
    @Path("/listACR")
    @Produces(MediaType.APPLICATION_XML)
    public String viewACR(){
        ActiveWorkspaces workspaces = new ActiveWorkspaces();
        for(MediciInstance instance:Constants.acrInstances){
            ActiveWorkspace workspace = new ActiveWorkspace();
            workspace.setName(instance.getTitle());
            workspace.setId(instance.getId());
            workspaces.addActiveWorkspace(workspace);
        }

        XStream xStream = new XStream();
        xStream.alias("ActiveWorkspaces",ActiveWorkspaces.class);
        xStream.alias("ActiveWorkspace",ActiveWorkspace.class);
        return xStream.toXML(workspaces);
    }

}
