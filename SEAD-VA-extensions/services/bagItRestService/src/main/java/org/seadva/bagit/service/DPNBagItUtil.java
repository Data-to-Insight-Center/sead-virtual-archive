package org.seadva.bagit.service;

/**
 * Created with IntelliJ IDEA.
 * User: Aravindh
 * Date: 19/05/2014
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */

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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.net.URISyntaxException;


@Path("/dpnBagUtil")
public class DPNBagItUtil {
    public DPNBagItUtil(){}

    @Context
    ServletContext context;

    @GET
    @Path("/DPNOreDataBag")
    public Response getDPNOREBagFromDir(
            @Context HttpServletRequest request,
            @Context UriInfo uri,
            @QueryParam("dirpath") String dirpath
    ) throws IOException, OREException, URISyntaxException, ClassNotFoundException, InstantiationException, IllegalAccessException

    {
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
        Constants.untarDir = Constants.bagDir + "untar/";

        if(!new File(Constants.bagDir).exists()) {
            new File(Constants.bagDir).mkdirs();
        }
        if(!new File(Constants.untarDir).exists()) {
            new File(Constants.untarDir).mkdirs();
        }

        new ConfigBootstrap().load();
        PackageDescriptor packageDescriptor = new PackageDescriptor("dpntest","",dirpath);
        packageDescriptor.setUntarredBagPath(dirpath);

        //packageDescriptor = ConfigBootstrap.packageListener.execute(Event.UNTAR_BAG, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.PARSE_DIRECTORY, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_ORE, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.GENERATE_MANIFEST, packageDescriptor);
        packageDescriptor = ConfigBootstrap.packageListener.execute(Event.TAR_BAG, packageDescriptor);

        String finalTarredBag = packageDescriptor.getBagPath();

        if(finalTarredBag==null)
            throw new NotFoundException("Failed to create a zipped BagIt Bag. Please ensure the basic bag you uploaded is valid.");

        String[] filename = finalTarredBag.split("/");

        System.out.print(finalTarredBag);
        Response.ResponseBuilder responseBuilder = Response.ok(new FileInputStream(finalTarredBag));

        responseBuilder.header("Content-Type", "application/zip");
        responseBuilder.header("Content-Disposition",
                "inline; filename=" + filename[filename.length-1]);
        return responseBuilder.build();
    }
}