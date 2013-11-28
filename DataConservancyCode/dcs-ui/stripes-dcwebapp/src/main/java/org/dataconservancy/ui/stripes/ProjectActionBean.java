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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.http.HttpStatus;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.BusinessObjectMap;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.Role;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.BusinessObjectMapService;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.RelationshipService;
import org.dataconservancy.ui.stripes.ext.JodaDateTimeTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ProjectActionBean} handles operations related to creating, retrieving and updating of users' {@link Project}.
 */
@UrlBinding("/userprofile/project.action")
public class ProjectActionBean extends org.dataconservancy.ui.stripes.BaseActionBean {
    /**
     * The forward destination when viewing the user's project membership
     */
    static final String VIEW_PROJECT_LIST_PATH = "/pages/view_projects_list.jsp";
    
    /**
     * The forward destination when viewing the user's project membership
     */
    static final String VIEW_PROJECT_PATH = "/pages/view_user_project.jsp";
    
    /**
     * The forward destination when editing a project
     */
    static final String EDIT_PROJECT_PATH = "/pages/edit_user_project.jsp";
    
    /**
     * The forward destination when adding a project
     */
    static final String ADD_PROJECT_PATH = "/pages/add_user_project.jsp";
    
    static final String FORWARD_SOURCE_KEY = "FORWARD_SOURCE_KEY";
    
    private String forwardRequestSource;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private RelationshipService relationshipService;
    private ProjectBizService projectBizService;
    private AuthorizationService authorizationService;
    private BusinessObjectMapService businessObjectMapService;
    
    private List<Person> projectAdminList = new ArrayList<Person>();
    private List<String> projectAdminIDList = new ArrayList<String>();
    private List<Person> removableProjectAdminList = new ArrayList<Person>();
    
    private String selectedProjectId;
    
    // length of database field length for the description
    private final static int descriptionMaxLength = 1024;
    
    @ValidateNestedProperties({
            @Validate(field = "name", required = true, on = { "userProjectUpdated", "userProjectAdded" }),
            @Validate(field = "description", required = true, maxlength = descriptionMaxLength + 1, on = {
                    "userProjectUpdated", "userProjectAdded" }),
            @Validate(field = "fundingEntity", required = true, on = { "userProjectUpdated", "userProjectAdded" }),
            @Validate(field = "startDate", required = true, on = { "userProjectUpdated", "userProjectAdded" }, converter = JodaDateTimeTypeConverter.class),
            @Validate(field = "endDate", required = true, on = { "userProjectUpdated", "userProjectAdded" }, converter = JodaDateTimeTypeConverter.class),
            @Validate(field = "numbers[0]", required = true, on = { "userProjectUpdated", "userProjectAdded" }) })
    private Project project;
    
    @ValidationMethod
    public void checkEndDate() {
        if (project.getEndDate().isBefore(project.getStartDate())) {
            getContext().getValidationErrors().add("project.endDate",
                    new SimpleError("End date may not be before start date"));
        }
    }
    
    // if the description length is exactly equal to the descriptionMaxLength, there is a good chance it was originally
    // larger, copied in and silently truncated. we want to send a message to the flash scope to have the user check
    // this
    @ValidationMethod
    public void checkDescriptionLength() {
        if (project.getDescription().length() == descriptionMaxLength + 1) {
            getContext().getValidationErrors().add(
                    "project.description",
                    new SimpleError("Description length is limited to " + descriptionMaxLength
                            + " characters. What you entered has been truncated to 1025 characters. Please edit."));
        }
    }
    
    /**
     * Redirects to the list of user's projects
     */
    @DefaultHandler
    @DontValidate
    public Resolution viewUserProjectsList() {
        
        return new ForwardResolution(VIEW_PROJECT_LIST_PATH);
    }
    
    /**
     * Redirects to the editable view of the users projects.
     * 
     * @throws BizPolicyException
     */
    @DontValidate
    public Resolution viewUserProject() throws BizPolicyException {
        this.project = projectBizService.getProject(selectedProjectId, getAuthenticatedUser());
        
        // Set the project admin list
        
        if (project == null) {
            final String msg = "Project " + selectedProjectId + " doesn't exist.";
            log.debug(msg);
            return new ErrorResolution(404, msg);
        }
        
        if (project != null) {
            Set<Person> projectAdmins = relationshipService.getAdministratorsForProject(project);
            projectAdminList.clear();
            for (Person admin : projectAdmins) {
                projectAdminList.add(admin);
            }
        }
        
        return new ForwardResolution(VIEW_PROJECT_PATH);
    }
    
    /**
     * Redirects to the page that allows the user to add new projects to their profile.
     */
    @DontValidate
    public Resolution addUserProject() {
        // load text strings
        try {
            assert (messageKeys.containsKey("error.user-can-not-add-project"));
        }
        catch (Exception e) {
            return propertiesFileLoadError();
        }
        
        List<Message> updatedProjectMessages = getContext().getMessages("updated");
        if (getAuthenticatedUser() != null && getAuthenticatedUser().getRoles().contains(Role.ROLE_ADMIN)) {
            project = new Project();
            return new ForwardResolution(ADD_PROJECT_PATH);
        }
        else {
            final String msg = messageKeys.getProperty("error.user-can-not-add-project");
            return new ErrorResolution(HttpStatus.SC_FORBIDDEN, msg);
        }
    }
    
    /**
     * Redirects to the page where users can edit their projects.
     * 
     * @throws BizPolicyException
     * @throws RelationshipConstraintException
     */
    @DontValidate
    public Resolution editUserProject() throws BizPolicyException, RelationshipConstraintException {
        Person currentUser = getAuthenticatedUser();
        
        project = projectBizService.getProject(selectedProjectId, currentUser);
        
        if (!authorizationService.canUpdateProject(currentUser, project)) {
            return new ErrorResolution(HttpStatus.SC_FORBIDDEN,
                    "You do not have permission to edit/update this project.");
        }
        
        if (project == null) {
            return new ForwardResolution(VIEW_PROJECT_PATH);
        }
        else {
            // Set the project admin list
            // TODO refactor to use the Policy Biz Service
            Set<Person> projectAdmins = relationshipService.getAdministratorsForProject(project);
            // projectAdminIDList.clear();
            projectAdminList.clear();
            removableProjectAdminList.clear();
            for (Person admin : projectAdmins) {
                // projectAdminIDList.add(admin.getFirstNames() + " " + admin.getLastNames());
                projectAdminList.add(admin);
                if (authorizationService.canRemoveAdmin(currentUser, admin, project)) {
                    removableProjectAdminList.add(admin);
                }
            }
            return new ForwardResolution(EDIT_PROJECT_PATH);
        }
    }
    
    @DontValidate
    public Resolution exportObjectMap() throws BizPolicyException {
        Person currentUser = getAuthenticatedUser();
        project = projectBizService.getProject(selectedProjectId, currentUser);
        return new StreamingResolution("text/xml") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                BusinessObjectMap boMap = businessObjectMapService.generateMap(project, null, false);
                businessObjectMapService.writeXmlMap(boMap, response.getOutputStream());
            }
        }.setFilename("ExportedObjectMap_" + project.getName() + ".xml");
    }

    // Removes any nulls from projectPIList. This can happen if for some reason a user has blank fields between entries
    // in the ui.
    private void pruneAdminList() {
        for (int i = projectAdminList.size() - 1; i >= 0; i--) {
            if (projectAdminList.get(i) == null) {
                projectAdminList.remove(i);
            }
        }
    }
    
    private void pruneAdminIDList() {
        for (int i = projectAdminIDList.size() - 1; i >= 0; i--) {
            if (projectAdminIDList.get(i) == null) {
                projectAdminIDList.remove(i);
            }
        }
    }

    public Resolution userProjectUpdated() throws BizInternalException, BizPolicyException {
        try {
            assert (messageKeys.containsKey("error.sys-admin-required-for-operation"));
        }
        catch (Exception e) {
            return propertiesFileLoadError();
        }
        
        Person user = getAuthenticatedUser();

        List<Message> updatedProjectMessages = getContext().getMessages("updated");
        if (project == null) {
            return new ForwardResolution(VIEW_PROJECT_PATH);
        }
        
        // First check to see if there are project admins already added for this project.
        if (projectAdminList.size() > 0) {
            pruneAdminList();
            // Loop through all the people in the new admin list, and make sure the exist in the system
            Iterator<Person> iter = projectAdminList.iterator();
            projectAdminIterator(user, iter, updatedProjectMessages);
        }
        if (projectAdminIDList.size() > 0) {
            pruneAdminIDList();
            // Loop through all the people in the new admin list, and make sure the exist in the system
            Iterator<String> iter = projectAdminIDList.iterator();
            projectAdminIterator(user, iter, updatedProjectMessages);
        }

        projectBizService.updateProject(project, getAuthenticatedUser());
        
        if (forwardRequestSource != null && forwardRequestSource.contains("adminhome")) {
            return new RedirectResolution(AdminHomeActionBean.class, "handle");
        }
        else if (getForwardRequestSource().equals(VIEW_PROJECT_PATH)) {
            RedirectResolution res = new RedirectResolution(getClass(), "viewUserProject");
            res.addParameter("selectedProjectId", project.getId());
            return res;
        }
        else {
            RedirectResolution res = new RedirectResolution(getClass(), "viewUserProjectsList");
            return res;
        }
        
    }
    
    public Resolution userProjectAdded() throws BizInternalException, BizPolicyException {
        
        try {
            assert (messageKeys.containsKey("error.sys-admin-required-for-operation"));
            assert (messageKeys.containsKey("error.reject-nonregistered-user-as-project-admin"));
        }
        catch (Exception e) {
            return propertiesFileLoadError();
        }
        
        List<Message> updatedProjectMessages = getContext().getMessages("updated");
        
        Person user = getAuthenticatedUser();
        
        pruneAdminIDList();

        Iterator<String> iter = projectAdminIDList.iterator();
        projectAdminIterator(user, iter, updatedProjectMessages);
        
        String createdProjectId = projectBizService.updateProject(project, user);
        selectedProjectId = createdProjectId;
        
        if (forwardRequestSource != null && forwardRequestSource.contains("adminhome")) {
            return new RedirectResolution(AdminHomeActionBean.class, "handle");
        }
        else {
            return new RedirectResolution(this.getClass(), "viewUserProjectsList");
        }
    }
    
    @DontValidate
    public Resolution cancel() throws BizInternalException, BizPolicyException {
        if (forwardRequestSource != null && forwardRequestSource.contains("adminhome")) {
            return new RedirectResolution(AdminHomeActionBean.class, "handle");
        }
        else if (getForwardRequestSource().equals(VIEW_PROJECT_PATH)) {
            RedirectResolution res = new RedirectResolution(getClass(), "viewUserProject");
            res.addParameter("selectedProjectId", project.getId());
            return res;
        }
        else {
            RedirectResolution res = new RedirectResolution(getClass(), "viewUserProjectsList");
            return res;
        }
    }
    
    /**
     * Helper method that iterates over a list of project admins and adds them to project.
     * 
     * @param user
     * @param iter
     * @param updatedProjectMessages
     */
    private void projectAdminIterator(Person user, Iterator<?> iter, List<Message> updatedProjectMessages) {
        while (iter.hasNext()) {
            Object next = iter.next();
            if (next instanceof String) {
                String personId = (String) next;
                // The current user is added in the biz service so they don't need to be handled here.
                if (!personId.equalsIgnoreCase(user.getId()) && !personId.equalsIgnoreCase(user.getEmailAddress())) {
                    // Try to hydrate the person
                    Person hydratedAdmin = userService.get(personId);
                    
                    if (!authorizationService.canBecomeProjectAdmin(hydratedAdmin, project)) {
                        // Since the person doesn't qualify, remove them from the list
                        iter.remove();
                        // put a message on the flash scope
                        final String s = messageKeys.getProperty("error.reject-nonregistered-user-as-project-admin");
                        String msg = String.format(s, personId, project.getName());
                        updatedProjectMessages.add(new SimpleMessage(msg));
                    }
                    else {
                        project.addPi(personId);
                        projectAdminList.add(hydratedAdmin);
                    }
                }
            }
            else if (next instanceof Person) {
                Person person = (Person) next;
                // Try to hydrate the person
                Person hydratedAdmin = userService.get(person.getId());
                if (!authorizationService.canBecomeProjectAdmin(hydratedAdmin, project)) {
                    // Since the person doesn't qualify, remove them from the list
                    iter.remove();
                    // put a message on the flash scope
                    final String s = messageKeys.getProperty("error.reject-nonregistered-user-as-project-admin");
                    String msg = String.format(s, person.getId(), project.getName());
                    updatedProjectMessages.add(new SimpleMessage(msg));
                }
                else {
                    project.addPi(person.getId());
                }
            }
        }
    }

    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public String getSelectedProjectId() {
        return selectedProjectId;
    }
    
    public void setSelectedProjectId(String selectedProjectId) {
        this.selectedProjectId = selectedProjectId;
    }
    
    public List<Person> getProjectAdminList() {
        return projectAdminList;
    }
    
    public List<String> getProjectAdminIDList() {
        return projectAdminIDList;
    }

    public List<Person> getRemovableAdmins() {
        return removableProjectAdminList;
    }
    
    public void setRemovableProjectAdminList(List<Person> adminList) {
        this.removableProjectAdminList = adminList;
    }
    
    public List<Project> getUserProjects() {
        ArrayList<Project> userProjects = new ArrayList<Project>(projectBizService.findByAdmin(getAuthenticatedUser()));
        return sortProjectListByName(userProjects);
    }
    
    public String getForwardRequestSource() {
        return forwardRequestSource;
    }
    
    public void setForwardRequestSource(String forwardRequestSource) {
        this.forwardRequestSource = forwardRequestSource;
    }
    
    public String getViewProjectPath() {
        return VIEW_PROJECT_PATH;
    }
    
    public String getViewProjectListPath() {
        return VIEW_PROJECT_LIST_PATH;
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
    
    @SpringBean("relationshipService")
    public void injectRelationshipService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }
    
    @SpringBean("projectBizService")
    public void injectProjectBizService(ProjectBizService bizService) {
        this.projectBizService = bizService;
    }
    
    @SpringBean("authorizationService")
    public void injectAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
    
    @SpringBean("businessObjectMapService")
    public void injectBusinessObjectMapService(BusinessObjectMapService businessObjectMapService) {
        this.businessObjectMapService = businessObjectMapService;
    }

}
