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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.UserService;
import org.joda.time.DateTime;

/**
 *
 */
public class CreateProjectRequest {
    
    private String projectId; // project.id
    private String projectName; // project.name
    private String projectDescription; // project.description
    private List<String> awardNumbers; // project.numbers
    private String fundingEntity; // project.fundingEntity
    private String startDate; // project.startDate
    private String endDate; // project.endDate
    private Set<String> pis = new HashSet<String>();
    
    private static final String STRIPES_EVENT = "userProjectAdded";
    
    private final UserService userService;
    
    private final UiUrlConfig uiUrlConfig;
    
    private boolean projectSet = false;
    
    public CreateProjectRequest(UiUrlConfig uiUrlConfig, UserService userService) {
        if (uiUrlConfig == null) {
            throw new IllegalStateException("UI URL Config must not be null");
        }
        
        if (userService == null) {
            throw new IllegalStateException("User Service must not be null");
        }
        this.uiUrlConfig = uiUrlConfig;
        this.userService = userService;
    }
    
    public void setProject(Project toCreate) {
        this.projectId = toCreate.getId();
        this.projectName = toCreate.getName();
        this.projectDescription = toCreate.getDescription();
        this.awardNumbers = toCreate.getNumbers();
        this.fundingEntity = toCreate.getFundingEntity();
        if (toCreate.getStartDate() != null) {
            this.startDate = toCreate.getStartDate().toDateTimeISO().toString();
        }
        if (toCreate.getEndDate() != null) {
            this.endDate = toCreate.getEndDate().toDateTimeISO().toString();
        }
        
        for (String p : toCreate.getPis()) {
            pis.add(p);
        }
        
        projectSet = true;
    }
    
    public Project getProject() {
        if (!projectSet) {
            throw new IllegalStateException("A project must be set first: call setProject(Project)");
        }
        
        Project p = new Project();
        
        if (projectId != null) {
            p.setId(projectId);
        }
        
        if (projectName != null) {
            p.setName(projectName);
        }
        
        if (projectDescription != null) {
            p.setDescription(projectDescription);
        }
        
        if (awardNumbers != null) {
            p.setNumbers(awardNumbers);
        }
        
        if (fundingEntity != null) {
            p.setFundingEntity(fundingEntity);
        }
        
        if (startDate != null) {
            p.setStartDate(DateTime.parse(startDate));
        }
        
        if (endDate != null) {
            p.setEndDate(DateTime.parse(endDate));
        }
        
        if (pis != null) {
            for (String userId : pis) {
                Person user = null;
                if ((user = userService.get(userId)) != null) {
                    p.addPi(userId);
                }
            }
        }
        
        return p;
        
    }
    
    public HttpPost asHttpPost() {
        if (!projectSet) {
            throw new IllegalStateException("A project must be set first: call setProject(Project)");
        }
        
        HttpPost post = null;
        try {
            post = new HttpPost(uiUrlConfig.getProjectUrl().toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("project.id", projectId));
        params.add(new BasicNameValuePair("project.name", projectName));
        params.add(new BasicNameValuePair("project.description", projectDescription));
        if (!awardNumbers.isEmpty()) {
            for (int i = 0; i < awardNumbers.size(); i++) {
                params.add(new BasicNameValuePair("numbers[" + i + "]", awardNumbers.get(i)));
            }
        }
        params.add(new BasicNameValuePair("project.fundingEntity", fundingEntity));
        params.add(new BasicNameValuePair("project.startDate", startDate));
        params.add(new BasicNameValuePair("project.endDate", endDate));
        params.add(new BasicNameValuePair(STRIPES_EVENT, "Add Project"));
        
        if (!pis.isEmpty()) {
            int i = 0;
            for (String p : pis) {
                params.add(new BasicNameValuePair("projectAdminIDList[" + i + "]", p));
                i++;
            }
        }
        
        try {
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        return post;
    }
}
