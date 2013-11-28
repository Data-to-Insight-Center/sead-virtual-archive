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
package org.dataconservancy.dcs.lineage.http.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsEntity;
import org.springframework.web.servlet.View;

import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ENTITIES;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.ETAG;
import static org.dataconservancy.dcs.lineage.http.LineageModelAttribute.LASTMODIFIED;

/**
 *
 */
public abstract class BaseStreamingView implements View {

    protected final DcsModelBuilder builder;

    protected List<DcsEntity> entities;

    protected BaseStreamingView(DcsModelBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Model Builder must not be null.");
        }

        this.builder = builder;
    }

    protected void serializeEntities(List<DcsEntity> entities, OutputStream tempOut) {
        Dcp dcp = new Dcp();
        if (entities.size() > 0) {
            dcp.addEntity(entities.toArray(new DcsEntity[]{}));
        }
        builder.buildSip(dcp, tempOut);
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (entities == null) {
            throw new IllegalStateException("Entities must be set first: call setEntities(List<DcsEntity>)");
        }
                
        // Serialize the entities to a temporary byte array
        List<DcsEntity> entities = (List<DcsEntity>) model.get(ENTITIES.name());
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream(1024 * 4);
        serializeEntities(entities, tempOut);

        setResponseHeaders(model, response, tempOut.size());

        if (request.getMethod().equalsIgnoreCase("HEAD")) {
            streamResponse(response, new byte[] {});
        } else if (request.getMethod().equalsIgnoreCase("GET")) {
            streamResponse(response, tempOut.toByteArray());
        } else {
            // catch-all, should never be invoked because this should be dealt with in the Controller.
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request.getMethod() + " requests not allowed.");
        }
    }

    protected void setResponseHeaders(Map<String, ?> model, HttpServletResponse response, int responseLength) {
        // Set response headers
        response.setHeader("Content-Type", getContentType());
        response.setHeader("Content-Length", String.valueOf(responseLength));

        // Empty responses (i.e. an empty List of entities) will not have an ETag.
        if (model.get(ETAG.name()) != null) {
            response.setHeader("ETag", (String) model.get(ETAG.name()));
        }

        // Last modified will be null if we return an empty lineage
        if (model.get(LASTMODIFIED.name()) != null) {
            response.setHeader("Last-Modified", model.get(LASTMODIFIED.name()).toString());
        }
        
        response.setStatus(200);
    }

    protected void streamResponse(HttpServletResponse response, byte[] responseContent) throws IOException {
        // Stream the response
        OutputStream out = response.getOutputStream();
        IOUtils.write(responseContent, out);
        out.flush();
        out.close();
    }

    public List<DcsEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<DcsEntity> entities) {
        if (entities == null) {
            throw new IllegalArgumentException("Entities must not be null.");
        }
        this.entities = entities;
    }
}
