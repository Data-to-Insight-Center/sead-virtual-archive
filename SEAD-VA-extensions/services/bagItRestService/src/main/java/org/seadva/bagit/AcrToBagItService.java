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

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.OREException;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.model.ActiveWorkspace;
import org.seadva.bagit.model.ActiveWorkspaces;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.AcrBagItConverter;
import org.seadva.bagit.util.Constants;
import org.seadva.bagit.util.ZipUtil;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.net.URISyntaxException;

/**
 * BagIt service interface
 */

@Path("/acrToBag")
public class AcrToBagItService {



    public AcrToBagItService() {}


        private static final Logger log =
            LoggerFactory.getLogger(AcrToBagItService.class);


        //ToDo: some time stamp based caching
        @Context
        ServletContext context;

        /*
        Get/Create Bag from ACR Collection
         */
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
             IOUtils.copy(new FileInputStream(
                     context.getRealPath("/WEB-INF/Config.properties")
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

        AcrBagItConverter acrBagItConverter = new AcrBagItConverter();
         MediciInstance instance = null;
         for(MediciInstance t_instance: Constants.acrInstances)
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



    /*
    List ACR instances
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

    /*
    * Get/Generate SEAD VA SIP (that can be used to ingest data into SEAD VA)
    * from Holey Bag (containing fetch.txt, manifest.txt, valid OAI-ORE and FGDC file for collection)
     */
    @POST
    @Path("/sip")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response getSIP(
                           @FormDataParam("file") InputStream uploadedInputStream,
                           @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws IOException, OREException, URISyntaxException

    {
        if(!fileDetail.getFileName().endsWith(".zip"))
            return Response
                    .status(Response.Status.NOT_ACCEPTABLE)
                    .header("SEAD-Exception-Name", "Not accepted")
                    .entity("Please upload a zipped SEAD BagIt file.")
                    .type(MediaType.APPLICATION_XML)
                    .build();

        if(Constants.homeDir==null){
            StringWriter writer = new StringWriter();
            IOUtils.copy(new FileInputStream(
                    context.getRealPath("/WEB-INF/Config.properties")
            ), writer);
            String result = writer.toString();
            String[] pairs = result.trim().split(
                    "\\w*\n|\\=\\w*");


            for (int i = 0; i + 1 < pairs.length;) {
                String name = pairs[i++].trim();
                String value = pairs[i++].trim();
                if (name.equals("bagit.home")) {
                    Constants.homeDir = value;
                }
            }
        }

        Constants.sipDir = Constants.homeDir+"/"+"sip";
        String zippedBag = Constants.sipDir+"/"+fileDetail.getFileName();
        String unzipDir = Constants.sipDir+"/"+
                fileDetail.getFileName().replace(".zip","")+"/";

        String sipPath;
        try {
            if(!(new File(Constants.sipDir).exists()))
                (new File(Constants.sipDir)).mkdirs();
            if(!(new File(unzipDir).exists()))
                (new File(unzipDir)).mkdirs();

            writeToFile(uploadedInputStream,zippedBag);

            ZipUtil.unzip(zippedBag, unzipDir);

            OreGenerator oreGenerator = new OreGenerator();
            oreGenerator.fromOAIORE(fileDetail.getFileName().replace(".zip",""), null, unzipDir);

            sipPath = Constants.sipDir+"/"
                    +fileDetail.getFileName().replace(".zip","")+"_sip.xml";
            File sipFile = new File(sipPath);

            OutputStream out = FileUtils.openOutputStream(sipFile);
            new SeadXstreamStaxModelBuilder().buildSip(oreGenerator.sip, out);
            out.close();

        } catch (IOException e) {
            throw e;
        } catch (URISyntaxException e) {
            throw e;
        } catch (OREException e) {
            throw e;
        }
        Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(sipPath));

        responseBuilder.header("Content-Disposition",
                "inline; filename=" + sipPath);
        return responseBuilder.build();
    }

    private void writeToFile(InputStream uploadedInputStream,
                             String uploadedFileLocation) {

        try {
            OutputStream out;
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }


}
