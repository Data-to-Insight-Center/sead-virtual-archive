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
package org.dataconservancy.ui.stripes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.services.ProjectService;

/**
 * The Action Bean for the Admin user home page.
 */
@UrlBinding("/admin/adminhome.action")
public class AdminHomeActionBean extends BaseActionBean {
    
    private ProjectService projectService;
    
    private String forwardRequestSource;
    
    @DefaultHandler
    public Resolution handle() {
        return new ForwardResolution("/pages/adminhome.jsp");
    }
    
    public List<Project> getAllProjects() {
        return sortProjectListByName(projectService.getAll());
    }
    
    /**
     * A convenience method.
     * 
     * @param projectList
     * @return
     */
    private List<Project> sortProjectListByName(List<Project> projectList) {
        Collections.sort(projectList, new Comparator<Project>() {
            public int compare(Project o1, Project o2) {
                if (o1.getName() == null || o2.getName() == null)
                    return 0;
                return o1.getName().compareTo(o2.getName());
            }
        });
        return projectList;
    }
    
    @SpringBean("projectService")
    public void injectProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    public String getForwardRequestSource() {
        return forwardRequestSource;
    }
    
    public void setForwardRequestSource(String forwardRequestSource) {
        this.forwardRequestSource = forwardRequestSource;
    }
}
