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
package org.dataconservancy.ui.api.support;


import java.util.List;
import java.util.Set;

import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.RelationshipService;

public class MockProjectBizService implements ProjectBizService {
 
    private ProjectService projectService;
    private RelationshipService relService;
    
    public MockProjectBizService(ProjectService projService, RelationshipService relationshipService) {
        projectService = projService;
        relService = relationshipService;
    }
           
    @Override
    public String updateProject(Project project, Person user) throws BizPolicyException, BizInternalException {
        //If project is a new one, create a new project in the system
        //If a project with matching business id is already in the system, update that project with the given information.
        if (project != null && user != null) {
            if (projectService.isExisting(project.getId())) {
                if (user.getRoles().contains(Role.ROLE_ADMIN) || project.getPis().contains(user)) {
                    try {
                        updateExistingProject(project, user);
                    } catch (ProjectServiceException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (user.getRoles().contains(Role.ROLE_ADMIN)) {
                    addProject(project, user, null);
                }
            }
        } else {
            if (project == null) {
                throw new BizPolicyException("Project must not be null", Type.VALIDATION_ERROR);
            } else {
                throw new BizPolicyException("User must not be null", Type.VALIDATION_ERROR);
            }
                
        }
        return project.getId();
    }

    @Override
    public Set<Project> findByAdmin(Person user) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addProject(Project project, Person user,
        List<String> project_admins) throws BizPolicyException {
    
   
        if (!project.getPis().contains(user)) {
            project.addPi(user.getId());
        }

        projectService.create(project);        
    }
    
    public void updateExistingProject(Project project, Person user) throws BizPolicyException,
            ProjectServiceException {

        if (project == null) {
            project = new Project();
        }
        
        /*Admins are passed in from the UI separate from the actual project we need to go through
         * and resolve the old list of PIs with the new list of PIs. There could be any number of additions or deletions from this list.
         * The only change not allowed is the current user can't remove themselves as a PI on the project. 
         * 
         * To resolve these lists we will first get the old list of administrators for the project from the relationship service. 
         * We'll then go through the new list of admins, people who don't appear in the old list we will attempt to hydrate and provide the proper relationships.
         * People who do exist in the old admin list will be removed from that list. 
         * After the list of new admins is exhausted we will remove what ever users are left in the old project admin list. 
         */
        
        //Get the list of admins on the project pre-update
        List<String> oldProjectPIList = projectService.get(project.getId()).getPis();

        //Loop through all the people in the new admin list, figure out who is new, and who is already existing.
        for (String pi : project.getPis()) {
            //Try to hydrate the person
            //Person hydratedAdmin = userService.get(pi.getEmailAddress());
            
            //Check that the new person exists in the system if not log a message
            if (pi != null) {                
                //If the person is in the old list remove them from it. This person will already be in the relationship service.
                //But add them to the project. TODO: When we get rid of PIs in the project this add to project won't be necessary
                if (oldProjectPIList.contains(pi)) {
                    oldProjectPIList.remove(pi);
                } 
            }
        }
      
        //If a user still exists in the old project admin list at this point it means they are not in the new project admin list
        //We will now remove all those users unless it's the currently logged in user. 
        for (String oldPI : oldProjectPIList) {
            //Make sure it's not the currently logged in user.
            if (!oldPI.equalsIgnoreCase(user.getId())) {
                project.removePi(oldPI);
            }
        }
      
        projectService.update(project);

    }
    
    public void setProjectService(ProjectService service) {
        this.projectService = service;
    }

    @Override
    public Set<Collection> getCollectionsForProject(Project project, Person user) {
        
        Set<Collection> collections = null;
        if (project != null) {
            if (user.getRoles().contains(Role.ROLE_ADMIN) || project.getPis().contains(user)) {
                collections = relService.getCollectionsForProject(project);
            }
        }
        return collections;
    }

    @Override
    public Project getProject(String id, Person user) throws BizPolicyException {
        Project project = projectService.get(id);
        
        if (project == null) {
            return null;
        }
        
        if (!user.getRoles().contains(Role.ROLE_ADMIN) && project.getPis().contains(user)) {
            throw new BizPolicyException("User does not have read permission", Type.AUTHORIZATION_ERROR);
        }
        
        return project;
    }
    
    @Override
    public Project getProject(String id) throws BizPolicyException {
        Project project = projectService.get(id);
        
        if (project == null) {
            return null;
        }
        
        return project;
    }
}