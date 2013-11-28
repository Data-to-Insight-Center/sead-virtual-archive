/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.access.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.XStream;

import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.dcs.query.endpoint.utils.dcpsolr.Config;
import org.dataconservancy.dcs.query.endpoint.utils.dcpsolr.ResultFormat;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private DcsModelBuilder dcpbuilder;

    private XStream jsonbuilder;

    private Config config;

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);

        this.dcpbuilder = new DcsXstreamStaxModelBuilder();
        this.config = Config.instance(getServletContext());
        this.jsonbuilder = DcpUtil.toJSONConverter();
    }

    public void destroy() {
        try {
            config.dcpQueryService().shutdown();
        } catch (QueryServiceException e) {
           
        } finally {
            super.destroy();
        }
    }

    private DcsEntity getEntity(HttpServletRequest req) throws IOException, QueryServiceException {
        String id = ServletUtil.getEntityId(req);

        if (id == null) {
            return null;
        }

        DcsDataModelQueryService queryService = (DcsDataModelQueryService) config.dcpQueryService();

        // TODO: If not found by the query service, search the archive.
        return queryService.lookupEntity(id);
    }

    // TODO worth it to set content-length?
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ByteArray output = new ByteArray(8 * 1024);

        serializeEntity(req, resp, output.asOutputStream());
        resp.setContentLength(output.length);
        resp.getOutputStream().write(output.array, 0, output.length);
        
        resp.flushBuffer();
    }

    private void serializeEntity(HttpServletRequest req,
                                 HttpServletResponse resp,
                                 OutputStream os) throws IOException {
        ResultFormat fmt = ResultFormat.find(req);

        if (fmt == null) {
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                           "Unknown response format requested");
            return;
        }

        DcsEntity entity = null;

        try {
            entity = getEntity(req);
        } catch (QueryServiceException e) {
            final String msg = "Error retrieving entity " + req.getPathInfo() + ": " + e.getMessage();
            log.debug(msg, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        }

        if (entity == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No such entity " + req.getPathInfo());
            return;
        }

        resp.setContentType(fmt.mimeType());
        resp.setHeader("ETag", entity.getId());

        if (entity instanceof DcsFile) {
            DcsFile file = (DcsFile) entity;

            file.setSource(config.publicDatastreamUrl()
                    + ServletUtil.encodeURLPath(file.getId()));
        }

        // TODO resp.setCharacterEncoding(), peek inside xml ?
        Dcp dcp = DcpUtil.add(null, entity);

        if (fmt == ResultFormat.JSON || fmt == ResultFormat.JAVASCRIPT) {
            String jsoncallback = req.getParameter("callback");

            if (jsoncallback != null) {
                os.write(jsoncallback.getBytes("UTF-8"));
                os.write('(');
            }

            jsonbuilder.toXML(dcp, os);

            if (jsoncallback != null) {
                os.write(')');
            }
        } else if (fmt == ResultFormat.DCP) {
            dcpbuilder.buildSip(dcp, os);
        }

        os.flush();
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        serializeEntity(req, resp, os);
        resp.setContentLength(os.size());
    }

    protected long getLastModified(HttpServletRequest req) {
        String id;
        id = ServletUtil.getResource(req);

        if (id == null) {
            log.debug("Error retrieving last modified date for entity " + req.getPathInfo() +
                    ": Could not parse entity id from request.");
            return -1;
        }

        DcsDataModelQueryService queryService = (DcsDataModelQueryService) config.dcpQueryService();
        try {
            return queryService.lookupEntityLastModified(id);
        } catch (QueryServiceException e) {
            final String msg = "Error retrieving last modified date for entity " + req.getPathInfo() + ": " + e.getMessage();
            log.debug(msg, e);
        }

        return -1;
    }
}
