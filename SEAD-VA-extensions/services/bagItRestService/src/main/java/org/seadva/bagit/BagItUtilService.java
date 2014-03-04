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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.OREException;
import org.sead.acr.common.utilities.json.JSONException;
import org.seadva.bagit.model.MediciInstance;
import org.seadva.bagit.util.AcrBagItConverter;
import org.seadva.bagit.util.Constants;
import org.seadva.bagit.util.ZipUtil;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URISyntaxException;

/**
 * BagIt Utility service interface
 */

@Path("/bagUtil")
public class BagItUtilService {

    public BagItUtilService() {}

    @Context
    ServletContext context;

    /**
     * Method to generate a bag with ORE when user supplies a holey BagIt Bag (without ORE)
     * @param uploadedInputStream
     * @param fileDetail
     * @return
     * @throws IOException
     * @throws OREException
     * @throws URISyntaxException
     */

    @POST
    @Path("/OreBag")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response getHoleyOREBag(
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

        Constants.unzipDir = Constants.homeDir+"/"+"unzip";
        String zippedBag = Constants.unzipDir+"/"+fileDetail.getFileName();
        String unzipDir = Constants.unzipDir+"/"+
                fileDetail.getFileName().replace(".zip","")+"/";

        String sipPath;
        try {
            if(!(new File(unzipDir).exists()))
                (new File(unzipDir)).mkdirs();

            writeToFile(uploadedInputStream,zippedBag);

            ZipUtil.unzip(zippedBag, unzipDir);


            AcrBagItConverter acrBagItConverter = new AcrBagItConverter();
            String finalZippedBag = acrBagItConverter.convertFecthToORE(new File(unzipDir+"fetch.txt"));

            if(zippedBag==null)
                throw new NotFoundException("Failed to create a zipped BagIt Bag. Please ensure the basic bag you uploaded is valid.");

            String[] filename = finalZippedBag.split("/");

            Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(finalZippedBag));

            responseBuilder.header("Content-Type", "application/zip");
            responseBuilder.header("Content-Disposition",
                    "inline; filename=" + filename[filename.length-1]);
            return responseBuilder.build();
        }
        catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     * Method to generate a not-holey bag with ORE when user supplies a not-holey BagIt Bag (without ORE)
     * @param uploadedInputStream
     * @param fileDetail
     * @return
     * @throws IOException
     * @throws OREException
     * @throws URISyntaxException
     */

    @POST
    @Path("/OreDataBag")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response getOREBag(
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

        Constants.unzipDir = Constants.homeDir+""+"unzip";
        String zippedBag = Constants.unzipDir+"/"+fileDetail.getFileName();
        String unzipDir = Constants.unzipDir+"/"+
                fileDetail.getFileName().replace(".zip","")+"/";

        String sipPath;
        try {
            if(!(new File(unzipDir).exists()))
                (new File(unzipDir)).mkdirs();

            writeToFile(uploadedInputStream,zippedBag);

            ZipUtil.unzip(zippedBag, unzipDir);


            AcrBagItConverter acrBagItConverter = new AcrBagItConverter();
            String finalZippedBag = acrBagItConverter.convertDirectoryToORE(new File(unzipDir+"/data"));

            if(zippedBag==null)
                throw new NotFoundException("Failed to create a zipped BagIt Bag. Please ensure the basic bag you uploaded is valid.");

            String[] filename = finalZippedBag.split("/");

            Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(finalZippedBag));

            responseBuilder.header("Content-Type", "application/zip");
            responseBuilder.header("Content-Disposition",
                    "inline; filename=" + filename[filename.length-1]);
            return responseBuilder.build();
        }
        catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
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
