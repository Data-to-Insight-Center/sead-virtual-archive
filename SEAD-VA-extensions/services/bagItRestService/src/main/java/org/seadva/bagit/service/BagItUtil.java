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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.IOUtils;
import org.dspace.foresite.OREException;
import org.seadva.bagit.util.NotFoundException;
import org.seadva.bagit.model.PackageDescriptor;
import org.seadva.bagit.event.api.Event;
import org.seadva.bagit.impl.ConfigBootstrap;
import org.seadva.bagit.util.Constants;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URISyntaxException;

/**
 * BagIt Utility service interface to generate ORE for Bags
 */

@Path("/bagUtil")
public class BagItUtil {

    public BagItUtil() {}


    @Context
    ServletContext context;

    /**
     * Method to generate a bag with ORE when user supplies a holey BagIt Bag (without ORE)
     * @param uploadedInputStream
     * @param fileDetail
     * @return
     * @throws java.io.IOException
     * @throws org.dspace.foresite.OREException
     * @throws java.net.URISyntaxException
     */

    @POST
    @Path("/OreBag")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response getHoleyOREBagFromFetch(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws IOException, OREException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException

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
                }
            }
        }

        Constants.bagDir = Constants.homeDir + "bag/";
        Constants.unzipDir = Constants.bagDir + "unzip/";

        if(!new File(Constants.bagDir).exists()) {
            new File(Constants.bagDir).mkdirs();
        }
        if(!new File(Constants.unzipDir).exists()) {
            new File(Constants.unzipDir).mkdirs();
        }

        String zippedBag = Constants.unzipDir + fileDetail.getFileName();

        IOUtils.copy(uploadedInputStream, new FileOutputStream(zippedBag));
        new ConfigBootstrap().load();
        PackageDescriptor packageDescriptor = new PackageDescriptor(fileDetail.getFileName().replace(".zip",""),zippedBag,"");

        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.UNZIP_BAG, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_FETCH, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_DATA_DIR, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.ZIP_BAG, packageDescriptor);

        if(packageDescriptor.getBagPath()==null)
            throw new NotFoundException("Failed to create a zipped BagIt Bag. Please ensure the basic bag you uploaded is valid.");

        String[] filename = packageDescriptor.getBagPath().split("/");

        Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(packageDescriptor.getBagPath()));

        responseBuilder.header("Content-Type", "application/zip");
        responseBuilder.header("Content-Disposition",
                "inline; filename=" + filename[filename.length-1]);
        return responseBuilder.build();
    }

    /**
     * Method to generate a not-holey bag with ORE when user supplies a not-holey BagIt Bag (without ORE)
     * @param uploadedInputStream
     * @param fileDetail
     * @return
     * @throws java.io.IOException
     * @throws org.dspace.foresite.OREException
     * @throws java.net.URISyntaxException
     */

    @POST
    @Path("/OreDataBag")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response getOREBagFromDir(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws IOException, OREException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException

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

            String path =  context.getRealPath("/WEB-INF/Config.properties");
            if(context.getInitParameter("testPath")!=null) {
                if (path.contains("/"))
                    path = path.replace("/./", context.getInitParameter("testPath"));
                else if (path.contains("\\"))
                    path = path.replace("\\.\\", context.getInitParameter("testPath"));
            }

            IOUtils.copy(new FileInputStream(
                   path)
                 , writer);

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

        Constants.bagDir = Constants.homeDir + "bag/";
        Constants.unzipDir = Constants.bagDir + "unzip/";

        if(!new File(Constants.bagDir).exists()) {
            new File(Constants.bagDir).mkdirs();
        }
        if(!new File(Constants.unzipDir).exists()) {
            new File(Constants.unzipDir).mkdirs();
        }

        String zippedBag = Constants.unzipDir+"/"+fileDetail.getFileName();

        IOUtils.copy(uploadedInputStream, new FileOutputStream(zippedBag));

        new ConfigBootstrap().load();
        PackageDescriptor packageDescriptor = new PackageDescriptor(fileDetail.getFileName().replace(".zip",""),zippedBag,"");

        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.UNZIP_BAG, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_DIRECTORY, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_MANIFEST, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.ZIP_BAG, packageDescriptor);

        String finalZippedBag = packageDescriptor.getBagPath();

        if(finalZippedBag==null)
            throw new NotFoundException("Failed to create a zipped BagIt Bag. Please ensure the basic bag you uploaded is valid.");

        String[] filename = finalZippedBag.split("/");

        Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(finalZippedBag));

        responseBuilder.header("Content-Type", "application/zip");
        responseBuilder.header("Content-Disposition",
                "inline; filename=" + filename[filename.length-1]);
        return responseBuilder.build();
    }

}
