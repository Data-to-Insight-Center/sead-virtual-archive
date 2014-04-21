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
import java.io.*;
import java.net.URISyntaxException;

/**
 * Generate SIP file from Bags
 */
@Path("/acrToBag/sip")
public class Sip {

    @Context
    ServletContext context;

    /**
     *
     * @param uploadedInputStream
     * @param fileDetail
     * @return
     * @throws java.io.IOException
     * @throws org.dspace.foresite.OREException
     * @throws java.net.URISyntaxException
     *
     *
     * Get/Generate SEAD VA SIP (that can be used to ingest data into SEAD VA)
     * from Holey Bag (containing fetch.txt, manifest.txt, valid OAI-ORE and FGDC file for collection)
     */

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public javax.ws.rs.core.Response getSIP(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws IOException, OREException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException

    {
        if(!fileDetail.getFileName().endsWith(".zip"))
            return javax.ws.rs.core.Response
                    .status(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE)
                    .header("SEAD-Exception-Name", "Not accepted")
                    .entity("Please upload a zipped SEAD BagIt file.")
                    .type(MediaType.APPLICATION_XML)
                    .build();

        if(Constants.homeDir==null){
            StringWriter writer = new StringWriter();
            String path =  context.getRealPath("/WEB-INF/Config.properties");
            if(context.getInitParameter("testPath")!=null) {
                if(path.contains("/"))
                    path = path.replace("/./", context.getInitParameter("testPath"));
                else if(path.contains("\\"))
                    path = path.replace("\\.\\", context.getInitParameter("testPath"));
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

        Constants.sipDir = Constants.homeDir+"sip";
        String unzipDir = Constants.sipDir+"/"+
                fileDetail.getFileName().replace(".zip","")+"/";


        if(!(new File(Constants.sipDir).exists())) {
            (new File(Constants.sipDir)).mkdirs();
        }
        if(!(new File(unzipDir).exists())) {
            (new File(unzipDir)).mkdirs();
        }


        String zippedBag = Constants.sipDir+"/"+fileDetail.getFileName();

        IOUtils.copy(uploadedInputStream, new FileOutputStream(zippedBag));

        new ConfigBootstrap().load();
        PackageDescriptor packageDescriptor = new PackageDescriptor(fileDetail.getFileName(), zippedBag,"");
        packageDescriptor.setPackageId(fileDetail.getFileName().replace(".zip",""));
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.UNZIP_BAG, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_SIP, packageDescriptor);


        javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.ok(new FileInputStream(packageDescriptor.getSipPath()));

        responseBuilder.header("Content-Disposition",
                "inline; filename=" + new File(packageDescriptor.getSipPath()).getName());
        return responseBuilder.build();
    }


}
