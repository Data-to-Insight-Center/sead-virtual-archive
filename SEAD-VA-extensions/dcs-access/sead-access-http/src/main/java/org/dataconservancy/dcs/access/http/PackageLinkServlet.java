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

import com.google.gson.GsonBuilder;
import org.dataconservancy.dcs.access.http.dataPackager.ZipPackageCreator;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.SeadConfig;
import org.dataconservancy.dcs.query.endpoint.utils.ServletUtil;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class PackageLinkServlet
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

        String id = ServletUtil.getResource(req);

        id = id.replace(":/","://").replace(":///","://").replace(":","\\:");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Malformed entity id " + req.getPathInfo());
            return;
        }

        ResearchObject finalSip = new ResearchObject();
        int increment = 1000;
        int loop = 0;
        for(int offset=0;loop<1;offset+=increment){


            String urlString =
                    this.config.getQueryUrl()+
                    "?q=ancestry:" +
                    "(" +
                    id +
                    ")" +
                    "&offset="+
                    offset+
                    "&max=" +
                    increment;

            URL url = new URL(urlString);

            URLConnection conn = url.openConnection();
            conn.setRequestProperty("accept", "application/xml");
            conn.connect();

            ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(conn.getInputStream());
            for(DcsDeliverableUnit du: sip.getDeliverableUnits())
                finalSip.addDeliverableUnit(du);
            for(DcsManifestation manifestation:sip.getManifestations())
                finalSip.addManifestation(manifestation);
            for(DcsFile file:sip.getFiles())
                finalSip.addFile(file);
           loop++;
        }
        //Get top level DU
        String urlString =
                this.config.getQueryUrl()+
                "?q=id:" +
                "(" +
                id +
                ")"
                ;

        URL url = new URL(urlString);

        URLConnection conn = url.openConnection();
        conn.setRequestProperty("accept", "application/xml");
        conn.connect();

        ResearchObject duSip = new SeadXstreamStaxModelBuilder().buildSip(conn.getInputStream());
        if(duSip.getDeliverableUnits().size()>0)
            finalSip.addDeliverableUnit(duSip.getDeliverableUnits().iterator().next());

        ZipPackageCreator zipPackageCreator = new ZipPackageCreator();
        zipPackageCreator.setCachePath(cachePath);
        String prefix = req.getScheme() +
                        "://" + req.getServerName() +
                        ":"+ req.getServerPort() +
                        req.getContextPath()+
                        "/";
        List<String> links = zipPackageCreator.getPackageLinks(finalSip, prefix);

        resp.getWriter().write(new GsonBuilder().create().toJson(links));
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            prepare(req, resp);
        } catch (InvalidXmlException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
