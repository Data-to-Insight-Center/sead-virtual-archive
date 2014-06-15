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

package org.dataconservancy.dcs.access.http;

import org.dataconservancy.dcs.access.http.dataPackager.ZipPackageCreator;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.SeadConfig;
import org.dataconservancy.dcs.query.endpoint.utils.ServletUtil;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class PackageDataServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String MIME_TYPE_URI =
            "http://www.iana.org/assignments/media-types/";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private SeadConfig config;
    private String cachePath;

    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
        this.config = SeadConfig.instance(
                getServletContext());
        cachePath = config.getTmpPath()+"/packageSip/";
        if(!(new File(cachePath)).exists())
            new File(cachePath).mkdirs();
    }
    
    public void destroy() {
        try {
            config.dcpQueryService().shutdown();

        } catch (QueryServiceException e) {
           
        } finally {
            super.destroy();
        }
    }
    
    private void prepare(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, InvalidXmlException {

        String link = ServletUtil.getResource(req);

        link = link.replace(":/","://").replace(":///","://");
        if (link == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Malformed entity id " + req.getPathInfo());
            return;
        }


        ZipPackageCreator zipPackageCreator = new ZipPackageCreator();
        zipPackageCreator.setCachePath(cachePath);
        zipPackageCreator.setConfig(config);
        zipPackageCreator.getPackage("/"+link, resp.getOutputStream());

     /*   resp.setHeader("ETag", file.getName());
        resp.setHeader("fileName", file.getName());
        if(file.getFormats().size()>0){
            resp.setHeader("Content-Type", file.getFormats().iterator().next().getFormat());
        }
        resp.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + file.getName());*/
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            prepare(req, resp);
        } catch (InvalidXmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
//        getPackageLinks(ResearchObject dcp);
//        getPackage(String link, OutputStream stream);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            prepare(req, resp);
        } catch (InvalidXmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
