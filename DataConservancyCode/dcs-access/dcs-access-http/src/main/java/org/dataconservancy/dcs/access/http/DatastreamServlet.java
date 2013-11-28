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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.dcs.query.endpoint.utils.dcpsolr.Config;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatastreamServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String MIME_TYPE_URI =
            "http://www.iana.org/assignments/media-types/";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Config config;
    private DcsModelBuilder dcpbuilder;

    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
       
        this.dcpbuilder = new DcsXstreamStaxModelBuilder();
        this.config = Config.instance(getServletContext());
    }
    
    public void destroy() {
        try {
            config.dcpQueryService().shutdown();
        } catch (QueryServiceException e) {
           
        } finally {
            super.destroy();
        }
    }
    
    private DcsFile prepare(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String id = ServletUtil.getResource(req);

        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Malformed entity id " + req.getPathInfo());
            return null;
        }
        
        DcsEntity entity = null;
        
        try {
            DcsDataModelQueryService queryService = (DcsDataModelQueryService) config.dcpQueryService();
            entity = queryService.lookupEntity(id);
        } catch (Exception e) {
            final String msg = "Error performing search for entity '" + id + "': " + e.getMessage();
            log.error(msg, e);
            throw new IOException(msg, e);
        }
        
        if (entity == null) {
            final String msg = "No such entity " + id;
            log.error(msg);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return null;
        }

        if (!(entity instanceof DcsFile)) {
            final String msg = "Entity is not a file " + id;
            log.error(msg);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return null;
        }

        DcsFile file = (DcsFile) entity;

        if (file.getSizeBytes() > Integer.MAX_VALUE) {
            // TODO what response code? Do ranges?
            final String msg = "File " + entity.getId() + " too large";
            log.error(msg);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
            return null;
        }

        if (file.getSizeBytes() != -1) {
            resp.setContentLength((int) file.getSizeBytes());
        }

        for (DcsFormat fmt : file.getFormats()) {
            if (fmt.getSchemeUri() != null
                    && fmt.getSchemeUri().equals(MIME_TYPE_URI)) {
                if (fmt.getFormat() != null) {
                    resp.setContentType(fmt.getFormat());
                }
            }
        }

        resp.setHeader("ETag", file.getId());

        return file;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        DcsFile file = prepare(req, resp);

        if (file == null) {
            return;
        }
       
        String filePath = file.getSource();
        
        //Check if file path corrections need to be made for windows. 
        if( filePath.startsWith("file://") ) {
            if( filePath.substring(6, filePath.length()).contains(":/") || 
                    filePath.substring(6, filePath.length()).contains(":\\")){
                filePath = filePath.replace("file://", "file:///");
            }
        }
        
        InputStream is = null;
        if (file.isExtant()) {
            try {
                is = new URL(filePath).openStream();
            } catch (MalformedURLException e){
                final String msg = "Error creating URL for " + file.getId();
                log.error(msg);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
            } catch (IOException e){
                final String msg = "Could not resolve datastream source file for non-extant file entity id " +
                        file.getId();
                log.error(msg);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            }
        } else {
            String src = file.getSource();
            if (src.startsWith("http")) {
                // TODO HttpClient or such
                final String msg = "Resolving http datatreams for non-extant file entities is not yet supported " +
                        "(src: " + file.getSource() + ", id: " + file.getId() + ")";
                log.error(msg);
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, msg);
                return;
            } else if (src.startsWith("file:/")) {
                if( src.substring(6, src.length()).contains(":/") || 
                       src.substring(6, src.length()).contains(":\\")){
                    src = src.replace("file://", "file:///");
                }
                File srcFile = new File(new URL(src).getFile());
                if (!srcFile.exists()) {
                    final String msg = "Could not resolve datastream source file for non-extant file entity id " +
                            file.getId();
                    log.error(msg);
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
                    return;
                }
                if (!srcFile.canRead()) {
                    final String msg = "Read permission denied for datastream source (src: " + file.getSource() + ", " +
                            "id: " + file.getId() + ")";
                    log.error(msg);
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
                    return;
                }
                is = new FileInputStream(srcFile);
            } else {
                // Unhandled protocol
                final String msg = "Cannot resolved datastream source for " +
                        "non-extant file entity id " + file.getId() + ": unknown source " + file.getSource();
                log.error(msg);
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, msg);
                return;
            }
        }

        if (is == null) {
            final String msg = "No datastream in id " + file.getId();
            log.error(msg);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    msg);
            return;
        }

        copy(is, resp.getOutputStream());
        is.close();
        resp.flushBuffer();
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] buf = new byte[16 * 1024];
        int n = 0;

        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        prepare(req, resp);
    }
}
