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

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.output.CountingOutputStream;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.endpoint.utils.dcpsolr.Config;
import org.dataconservancy.dcs.query.endpoint.utils.dcpsolr.ResultFormat;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;

// TODO Could set last mod by using most recent last mod of results

public class QueryServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_MAX_MATCHES = 10;

    private static final int MAX_MATCHES_UPPER_BOUND = 1000;

    private DcsModelBuilder dcpbuilder;

    private XStream jsonbuilder;

    private Config config;

    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);

        this.dcpbuilder = new DcsXstreamStaxModelBuilder();
        this.config = Config.instance(getServletContext());
        this.jsonbuilder = DcpUtil.toJSONConverter();
    }

    @SuppressWarnings("unchecked")
    private void search(HttpServletRequest req,
                        HttpServletResponse resp,
                        OutputStream os) throws IOException {

        String query = req.getParameter("q");

        if (query == null) {
            query = ServletUtil.getResource(req);
        }

        if (query == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "No query specified");
            return;
        }

        int offset = 0;
        int max = DEFAULT_MAX_MATCHES;

        if (req.getParameter("offset") != null) {
            try {
                offset = Integer.parseInt(req.getParameter("offset"));
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Bad offset param");
                return;
            }
        }

        if (req.getParameter("max") != null) {
            try {
                max = Integer.parseInt(req.getParameter("max"));
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Bad max param");
                return;
            }
        }

        if (offset < 0) {
            offset = 0;
        }

        if (max < 0) {
            max = DEFAULT_MAX_MATCHES;
        }

        if (max > MAX_MATCHES_UPPER_BOUND) {
            max = MAX_MATCHES_UPPER_BOUND;
        }

        ResultFormat fmt = ResultFormat.find(req);

        if (fmt == null) {
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                           "Unknown response format requested");
            return;
        }

        resp.setContentType(fmt.mimeType());

        List<String> params = new ArrayList<String>();

        for (Object o : Collections.list(req.getParameterNames())) {
            String name = (String) o;

            if (name.startsWith("_")) {
                params.add(name.substring(1));
                params.add(req.getParameter(name));
            }
        }

        QueryResult<DcsEntity> result;

        try {
            result =
                    config.dcpQueryService().query(query,
                                           offset,
                                           max,
                                           params.toArray(new String[] {}));
        } catch (QueryServiceException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                           "Error running query: " + e.getMessage());
            return;
        }

        resp.setHeader("X-TOTAL-MATCHES", "" + result.getTotal());

        if (os == null) {
            return;
        }
        
        for (QueryMatch<DcsEntity> match : result.getMatches()) {
            DcsEntity entity = match.getObject();

            if (entity instanceof DcsFile) {
                DcsFile file = (DcsFile) entity;
                
                file.setSource(config.publicDatastreamUrl()
                               + ServletUtil.encodeURLPath(file.getId()));
            }
        }

        if (fmt == ResultFormat.JSON || fmt == ResultFormat.JAVASCRIPT) {
            String jsoncallback = req.getParameter("callback");

            if (jsoncallback != null) {
                os.write(jsoncallback.getBytes("UTF-8"));
                os.write('(');
            }

            jsonbuilder.toXML(result, os);

            if (jsoncallback != null) {
                os.write(')');
            }
        } else if (fmt == ResultFormat.DCP) {
            Dcp dcp = new Dcp();

            for (QueryMatch<DcsEntity> match : result.getMatches()) {
                DcpUtil.add(dcp, match.getObject());
            }

            dcpbuilder.buildSip(dcp, os);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        CountingOutputStream countingOs = new CountingOutputStream(resp.getOutputStream());
        search(req, resp, countingOs);
        resp.setContentLength(countingOs.getCount());
        resp.flushBuffer();
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        search(req, resp, null);
    }

}
