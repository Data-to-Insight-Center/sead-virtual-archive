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

package org.dataconservancy.ui.services;

import java.util.HashSet;
import java.util.Set;

import org.dataconservancy.dcs.id.api.IdService;
import org.dataconservancy.dcs.id.api.Types;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.util.UserVerificationUtil;

/**
 * An implementation of {@link ProjectBizService}
 */
public class ProjectBizServiceImpl implements ProjectBizService {
    
    private RelationshipService relationshipService;
    private ProjectService projectService;
    private IdService idService;
    private UserService userService;
    private AuthorizationService authorizationService;
    
    @Override
    public String updateProject(Project project, Person user) throws BizPolicyException, BizInternalException {
        // check for null arguments
        if (project == null) {
            throw new IllegalArgumentException("Project must be set and NOT null, before calling update project");
        }
        
        // If project is a new one, create a new project in the system
        // If a project with matching business id is already in the system, update that project with the given
        // information.
        
        if (project.getId() != null & projectService.isExisting(project.getId())) {
            return updateExistingProject(project, user);
        }
        else {
            return addProject(project, user);
        }
        
    }
    
    @Override
    public Set<Project> findByAdmin(Person admin) {
        if (admin == null) {
            return null;
        }
        if (admin.getRoles().contains(Role.ROLE_ADMIN)) {
            return new HashSet<Project>(projectService.getAll());
        }
        else {
            return relationshipService.getProjectsForAdministrator(admin);
        }
    }
    
    private String addProject(Project project, Person user) throws BizPolicyException {
        
        // Verify the user is who they say they are
        user = UserVerificationUtil.VerifyUser(userService, user);
        
        if (!authorizationService.canCreateProject(user)) {
            if (user == null) {
                throw new BizPolicyException("Please log in to add a project to the system.", Type.AUTHENTICATION_ERROR);
            }
            else {
                throw new BizPolicyException("This user is not authorized to add a project to the system.",
                        Type.AUTHORIZATION_ERROR);
            }
        }
        
        // assign project id
        project.setId(idService.create(Types.PROJECT.name()).getUrl().toString());
        
        validateProject(project);
        
        // set up proper relationships for project and its administrator
        // convert a pis list from the project to a set
        Set<String> pisSet = new HashSet<String>(project.getPis());
        // when the code get this point, user is an instance admin see AuthorizationServiceImpl for details
        // -> add instance admin as project admin too
        pisSet.add(user.getId());
        
        for (String pi : pisSet) {
            Person piPerson = userService.get(pi);
            relationshipService.addAdministratorToProject(project, piPerson);
        }
        
        // Subtle point: project's pis list does not include the instance admin. It is a true PI list, not an admin list
        
        Project createdProject = projectService.create(project);
        
        return createdProject.getId();
        
    }
    
    private String updateExistingProject(Project project, Person user) throws BizPolicyException, BizInternalException {
        
        // Verify that the user is who they say they are
        user = UserVerificationUtil.VerifyUser(userService, user);
        
        if (!authorizationService.canUpdateProject(user, project)) {
            if (user == null) {
                throw new BizPolicyException("Please login to make updates to the given project",
                        Type.AUTHENTICATION_ERROR);
            }
            else {
                throw new BizPolicyException("This user is not authorized to make updates to the given project",
                        Type.AUTHORIZATION_ERROR);
            }
        }
        
        validateProject(project);
        
        /*
         * Admins are passed in from the UI separate from the actual project we need to go through and resolve the old
         * list of PIs with the new list of PIs. There could be any number of additions or deletions from this list. The
         * only change not allowed is the current user can't remove themselves as a PI on the project.
         * 
         * To resolve these lists we will first get the old list of administrators for the project from the relationship
         * service. We'll then go through the new list of admins, people who don't appear in the old list we will
         * attempt to hydrate and provide the proper relationships. People who do exist in the old admin list will be
         * removed from that list. After the list of new admins is exhausted we will remove what ever users are left in
         * the old project admin list.
         */
        
        // Get the list of admins on the project pre-update
        Set<Person> oldProjectAdminList = relationshipService.getAdministratorsForProject(project);
        // Loop through all the people in the new admin list, figure out who is new, and who is already existing.
        for (String pi : project.getPis()) {
            
            // Check that the new person exists in the system if not log a message
            if (pi != null) {
                Person hydratedPi = userService.get(pi);
                // If the person is in the old list remove them from it. This person will already be in the relationship
                // service.
                // But add them to the project.
                if (oldProjectAdminList.contains(hydratedPi)) {
                    oldProjectAdminList.remove(hydratedPi);
                }
                else {
                    // They're not in the old list so we need to add them to the project and collections
                    relationshipService.addAdministratorToProject(project, hydratedPi);
                    
                    for (Collection col : relationshipService.getCollectionsForProject(project)) {
                        relationshipService.addDepositorToCollection(hydratedPi, col);
                    }
                }
            }
        }
        
        // If a user still exists in the old project admin list at this point it means they are not in the new project
        // admin list
        // We will now remove all those users unless it's the currently logged in user.
        for (Person oldAdmin : oldProjectAdminList) {
            // Make sure it's not the currently logged in user.
            if (!oldAdmin.getId().equalsIgnoreCase(user.getId())) {
                relationshipService.removeAdministratorFromProject(oldAdmin, project);
                project.removePi(oldAdmin.getId());
                
                // Loop through the collections in the project and remove admins as depositors
                for (Collection col : relationshipService.getCollectionsForProject(project)) {
                    relationshipService.removeDepositorFromCollection(oldAdmin, col);
                }
            }
        }
        
        try {
            projectService.update(project);
        }
        catch (ProjectServiceException e) {
            throw new BizInternalException(e);
        }
        return project.getId();
    }
    
    @Override
    public Set<Collection> getCollectionsForProject(Project project, Person user) throws BizPolicyException {
        
        // Verify that the user is who they say they are
        user = UserVerificationUtil.VerifyUser(userService, user);
        
        Set<Collection> collections = new HashSet<Collection>();
        
        if (project == null) {
            collections = null;
        }
        else {
            if (!authorizationService.canRetrieveProjectCollections(user, project)) {
                if (user == null) {
                    throw new BizPolicyException("Please login to view collections of the given project.",
                            Type.AUTHENTICATION_ERROR);
                }
                else {
                    throw new BizPolicyException(
                            "This user is not authorized to view collections of the given project.",
                            Type.AUTHORIZATION_ERROR);
                }
            }
            else {
                collections = relationshipService.getCollectionsForProject(project);
            }
        }
        return collections;
    }
    
    private void validateProject(Project project) throws BizPolicyException {
        if (project.getId() == null || project.getId().isEmpty()) {
            throw new BizPolicyException("Project's id cannot be empty or null.", Type.VALIDATION_ERROR);
        }
        if (project.getName() == null || project.getName().isEmpty()) {
            throw new BizPolicyException("Project's name cannot be empty or null.", Type.VALIDATION_ERROR);
        }
        if (project.getDescription() == null || project.getDescription().isEmpty()) {
            throw new BizPolicyException("Project's description cannot be empty or null.", Type.VALIDATION_ERROR);
        }
        if (project.getFundingEntity() == null || project.getFundingEntity().isEmpty()) {
            throw new BizPolicyException("Project's funding entity cannot be empty or null.", Type.VALIDATION_ERROR);
        }
        for (String number : project.getNumbers()) {
            if (number == null || number.isEmpty()) {
                throw new BizPolicyException("Project's number cannot be empty or null", Type.VALIDATION_ERROR);
            }
        }
        if (project.getPublisher() == null || project.getPublisher().isEmpty()) {
            throw new BizPolicyException("Project's publisher cannot be empty or null.", Type.VALIDATION_ERROR);
        }
        if (project.getStartDate() == null || project.getEndDate() == null) {
            throw new BizPolicyException("Project's start date and end date has to be set.", Type.VALIDATION_ERROR);
        }
        if (project.getStartDate().isAfter(project.getEndDate())) {
            throw new BizPolicyException("Start date cannot be AFTER end date.", Type.VALIDATION_ERROR);
        }
        
        for (String personId : project.getPis()) {
            if (personId == null || userService.get(personId) == null) {
                throw new BizPolicyException("Project's pis must be registered users of the system.",
                        Type.VALIDATION_ERROR);
            }
        }
    }
    
    public void setRelationshipService(RelationshipService service) {
        this.relationshipService = service;
    }
    
    public void setProjectService(ProjectService service) {
        this.projectService = service;
    }
    
    public void setIdService(IdService service) {
        this.idService = service;
    }
    
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
    
    public void setUserService(UserService service) {
        this.userService = service;
    }
    
    @Override
    public Project getProject(String id, Person user) throws BizPolicyException {
        Project proj = projectService.get(id);
        
        if (proj == null) {
            return null;
        }
        
        // Verify that the user is who they say they are
        user = UserVerificationUtil.VerifyUser(userService, user);
        
        if (!authorizationService.canReadProject(user, proj)) {
            if (user == null) {
                throw new BizPolicyException("Please login to view the given project", Type.AUTHENTICATION_ERROR);
            }
            else {
                throw new BizPolicyException("This user is not authorized to view the given project",
                        Type.AUTHORIZATION_ERROR);
            }
        }
        
        return proj;
    }
    
    @Override
    public Project getProject(String id) throws BizPolicyException {
        Project proj = projectService.get(id);
        
        if (proj == null) {
            return null;
        }
        
        return proj;
    }
}
