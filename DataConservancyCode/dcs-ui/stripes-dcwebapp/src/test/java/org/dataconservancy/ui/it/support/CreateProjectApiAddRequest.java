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

package org.dataconservancy.ui.it.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URISyntaxException;

import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.model.builder.xstream.XstreamBusinessObjectBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Project creation request which uses the project HTTP API.
 */
public class CreateProjectApiAddRequest {

    private final UiUrlConfig uiUrlConfig;

    private final BusinessObjectBuilder builder;

    private final Project project;

    public CreateProjectApiAddRequest(UiUrlConfig uiUrlConfig, Project project, BusinessObjectBuilder builder) {
        if (uiUrlConfig == null) {
            throw new IllegalArgumentException("UI URL Config must not be null");
        }

        if (builder == null) {
            throw new IllegalArgumentException("Business Object Builder must not be null.");
        }

        this.uiUrlConfig = uiUrlConfig;
        this.project = project;
        this.builder = builder;
    }

    public HttpPost asHttpPost() {
        HttpPost post = null;

        try {
            post = new HttpPost(uiUrlConfig.getProjectApiUrl().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        builder.buildProject(project, sink);
        ByteArrayEntity projectEntity = new ByteArrayEntity(sink.toByteArray());

        projectEntity.setContentEncoding("UTF-8");
        post.addHeader("Content-Type", "text/xml");

        post.setEntity(projectEntity);

        return post;
    }

    public Project execute(HttpClient client) throws IOException,
            InvalidXmlException {
        HttpResponse resp = client.execute(asHttpPost());

        int status = resp.getStatusLine().getStatusCode();

        if (status != 200 && status != 201) {
            throw new IOException("Unable to add project " + project + "; "
                    + resp.getStatusLine());
        }

        InputStream is = resp.getEntity().getContent();
        Bop bop = builder.buildBusinessObjectPackage(is);

        assertNotNull(bop);

        Set<Project> projects = bop.getProjects();
        assertNotNull(projects);
        assertEquals(1, projects.size());

        Project returnedProject = projects.iterator().next();
        assertNotNull(returnedProject);
        is.close();

        return returnedProject;
    }
}
