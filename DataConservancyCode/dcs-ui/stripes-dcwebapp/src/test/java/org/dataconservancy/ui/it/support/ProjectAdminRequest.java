/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.ui.it.support;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.dataconservancy.ui.model.Project;

public class ProjectAdminRequest {
    private Project project;
    private String admin;
    private boolean userToBeAdded = false;
    
    private final UiUrlConfig urlConfig;
    
    private static final String STRIPES_EVENT = "userProjectUpdated";
    
    public ProjectAdminRequest(UiUrlConfig urlConfig) {
        if (urlConfig == null) {
            throw new IllegalArgumentException("UiUrlConfig must not be null.");
        }
        this.urlConfig = urlConfig;
    }
    
    /**
     * Sets the user as an admin on the project.
     * 
     * @param userID
     *            The id of the user to add as an admin.
     * @param project
     *            The project the user should be added to.
     */
    public void setNewAdminForProject(String userID, Project project) {
        userToBeAdded = true;
        this.admin = userID;
        this.project = project;
    }
    
    /**
     * Removes the user from the admin list on the project.
     * 
     * @param userID
     *            The id of the user to remove.
     * @param project
     *            The project hte user should be removed from.
     */
    public void removeAdminFromProject(String userID, Project project) {
        userToBeAdded = false;
        this.admin = userID;
        this.project = project;
    }
    
    public String getProjectAdmin() {
        if (!userToBeAdded) {
            throw new IllegalStateException("Authorized User not set up. Call set authorized user for collection");
        }
        
        return admin;
    }
    
    public String getExProjectAdmin() {
        if (userToBeAdded) {
            throw new IllegalStateException("User still authorized for collection. Call remove authorized user first.");
        }
        
        return admin;
    }
    
    public Project getProject() {
        return project;
    }
    
    public HttpPost asHttpPost() {
        if (project == null || admin == null || admin.isEmpty()) {
            throw new IllegalStateException(
                    "Project, or Admin not set: Call setNewAdminForProject or removeAdminFromProject first");
        }
        
        HttpPost post = null;
        try {
            post = new HttpPost(urlConfig.getProjectUrl().toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        
        if (userToBeAdded) {
            // Make sure all existing pis are kept on the project
            int i = 0;
            for (i = 0; i < project.getPis().size(); i++) {
                params.add(new BasicNameValuePair("projectAdminList[" + i + "]", project.getPis().get(i)));
            }
            params.add(new BasicNameValuePair("projectAdminList[" + i + "]", admin));
        }
        else {
            int index = 0;
            for (int i = 0; i < project.getPis().size(); i++) {
                String pi = project.getPis().get(i);
                if (!pi.equalsIgnoreCase(admin)) {
                    params.add(new BasicNameValuePair("projectAdminList[" + index + "]", pi));
                    index++;
                }
            }
            
        }
        params.add(new BasicNameValuePair("project.id", project.getId()));
        params.add(new BasicNameValuePair("project.name", project.getName()));
        params.add(new BasicNameValuePair("project.description", project.getDescription()));
        if (!project.getNumbers().isEmpty()) {
            for (int i = 0; i < project.getNumbers().size(); i++) {
                params.add(new BasicNameValuePair("project.numbers[" + i + "]", project.getNumbers().get(i)));
            }
        }
        params.add(new BasicNameValuePair("project.fundingEntity", project.getFundingEntity()));
        params.add(new BasicNameValuePair("project.startDate", project.getStartDate().toString()));
        params.add(new BasicNameValuePair("project.endDate", project.getEndDate().toString()));
        params.add(new BasicNameValuePair("forwardRequestSource", "/pages/view_projects_list.jsp"));
        params.add(new BasicNameValuePair(STRIPES_EVENT, "Update Project"));
        
        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(params, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        post.setEntity(entity);
        
        return post;
    }
}