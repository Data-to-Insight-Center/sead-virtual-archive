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

package org.dataconservancy.ui.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.URLDecoder;

import java.util.Date;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.BizPolicyException.Type;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.Collection;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.Project;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.ProjectBizService;
import org.dataconservancy.ui.services.ProjectService;
import org.dataconservancy.ui.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

/**
 * Entry point to obtaining representations of Projects.  Currently the specification of the HTTP API is being
 * tracked <a href="https://scm.dataconservancy.org/confluence/x/GQCV">here</a>
 *
 * @see <a href="https://scm.dataconservancy.org/confluence/x/GQCV">https://scm.dataconservancy.org/confluence/x/GQCV</a>
 */
@Controller
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final BusinessObjectBuilder objectBuilder;

    private ProjectBizService projectBizService;

    private final UserService userService;

    private RequestUtil util;

    private Person manuallyAuthenticatedUser = null;

    private AuthorizationService authorizationService;

    public ProjectController(ProjectService projectService,
                             ProjectBizService projectBizService,
                             UserService userService,
                             BusinessObjectBuilder builder,
                             RequestUtil requestUtil,
                             AuthorizationService authorizationService) {
        this.projectService = projectService;
        this.projectBizService = projectBizService;
        this.userService = userService;
        this.objectBuilder = builder;
        this.util = requestUtil;
        this.authorizationService = authorizationService;
    }

    // TODO: Extracted from BaseActionBean: Should be pulled to shared class.

    private Person getAuthenticatedUser() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication authn = ctx.getAuthentication();

        if (manuallyAuthenticatedUser != null) {
            return manuallyAuthenticatedUser;
        } else {
            if (authn != null && authn.getPrincipal() != null
                    && authn.getPrincipal() instanceof Person) {
                Person loggedInUser = (Person) authn.getPrincipal();

                if (loggedInUser != null && userService != null) {
                    return userService.get(loggedInUser.getId());
                }

                return loggedInUser;
            }
        }
        return null;
    }

    //TODO: Implement actual mock authentication. this is just to keep testing moving forward
    public void setAuthenticatedUser(Person user) {
        manuallyAuthenticatedUser = user;
    }

    /**
     * Handles empty get call this will return a list of projects that are
     * visible to the current user Not yet implemented.
     * 
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @throws IOException
     * @throws BizPolicyException
     */
    @RequestMapping(method = {RequestMethod.GET})
    public void handleEmptyGetRequest(@RequestHeader(value = "Accept", required = false) String mimeType,
                                      @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
                                      HttpServletRequest request,
                                      HttpServletResponse resp)
            throws ServletException, IOException, BizPolicyException {

        Person user = getAuthenticatedUser();

        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            Set<Project> projects = projectBizService.findByAdmin(user);
            Bop bop = new Bop();
            bop.setProjects(projects);
            
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/xml");
            objectBuilder.buildBusinessObjectPackage(bop, resp.getOutputStream());
        }
    }

    /**
     * Handles get request with an id, this returns the serialized project
     * identified by the id. Partially implemented.
     * 
     * @param idpart
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @throws BizPolicyException
     */
    @RequestMapping(value = "/{idpart}", method = {RequestMethod.GET})
    public void handleProjectGetRequest(@PathVariable String idpart,
                                        @RequestHeader(value = "Accept", required = false) String mimeType,
                                        @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
                                        HttpServletRequest request,
                                        HttpServletResponse resp)
            throws IOException, BizPolicyException {

        Person user = getAuthenticatedUser();
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            String id = util.buildRequestUrl(request);

            Project project = projectBizService.getProject(id, user);

            if (project == null) {
                resp.setStatus(HttpStatus.SC_NOT_FOUND);
            } else {
                if (authorizationService.canReadProject(user, project)) {
                    Bop bop = new Bop();
                    bop.addProject(project);
                    resp.setContentType("text/xml");
                    objectBuilder.buildBusinessObjectPackage(bop, resp.getOutputStream());
                }
                else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
        }
    }

    /**
     * Handles a request of getting all the collections in the project of the
     * given id. Not yet implemented.
     * 
     * @param idpart
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @throws IOException
     */
    @RequestMapping(value = "/{idpart}/collections", method = {RequestMethod.GET})
    public void handleProjectCollectionsGetRequest(@PathVariable String idpart,
                                                   @RequestHeader(value = "Accept", required = false) String mimeType,
                                                   @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
                                                   HttpServletRequest request,
                                                   HttpServletResponse resp)
            throws IOException, BizPolicyException {

        Person user = getAuthenticatedUser();
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        else {

            String id = util.buildRequestUrl(request);

            //The ID will come back with collections on the end of it since it's part of the request. 
            //It needs to be removed. 
            id = id.substring(0, id.length() - 12);

            Project project = projectService.get(id);

            if (project != null) {
                if (authorizationService.canReadProject(user, project)) {
                    resp.setCharacterEncoding("UTF-8");
                    resp.setContentType("text/xml");
                    Set<Collection> collections = projectBizService.getCollectionsForProject(project, user);
                    Bop bop = new Bop();
                    bop.setCollections(collections);
                    objectBuilder.buildBusinessObjectPackage(bop, resp.getOutputStream());
                }
                else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } else {
                try {
                    resp.setStatus(HttpStatus.SC_NOT_FOUND);
                    resp.getWriter().print("Could not find project with id: "
                            + idpart);
                    resp.getWriter().flush();
                    resp.getWriter().close();
                } catch (Exception ee) {
                    log.debug("Handling exception", ee);
                }
            }
        }
    }

    /**
     * Handles the posting of a new project, this will add a new project to the
     * system. Note: Dates should be in the format dd mm yyyy
     *
     * @throws InvalidXmlException
     * @throws IOException
     */
    @RequestMapping(method = {RequestMethod.POST})
    public void handleProjectPostRequest(@RequestHeader(value = "Accept", required = false) String mimeType,
                                         @RequestBody byte[] content,
                                         HttpServletRequest req,
                                         HttpServletResponse resp)
            throws BizInternalException, BizPolicyException,
            InvalidXmlException, IOException {


        // TODO Why doesn't spring do this?
        if (req.getContentType().contains("application/x-www-form-urlencoded")) {
            content =
                    URLDecoder.decode(new String(content, "UTF-8"), "UTF-8")
                            .getBytes("UTF-8");
        }

        Project project =
                objectBuilder.buildProject(new ByteArrayInputStream(content));
        Person user = getAuthenticatedUser();

        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        else if (authorizationService.canCreateProject(user)) {
            projectBizService.updateProject(project, user);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/xml");
            resp.setStatus(HttpStatus.SC_CREATED);
            
            Bop bop = new Bop();
            bop.addProject(project);
            objectBuilder.buildBusinessObjectPackage(bop, resp.getOutputStream());
        }
        else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
    }

    /**
     * Handles updating a project matching the given id Note: Dates should be in
     * the format dd mm yyyy
     * 
     * @param idpart
     * @throws ProjectServiceException
     * @throws InvalidXmlException
     * @throws BizPolicyException
     * @throws IOException
     */
    @RequestMapping(value = "/{idpart}", method = {RequestMethod.PUT})
    public void handleUpdateProjectRequest(@PathVariable String idpart,
                                           @RequestHeader(value = "Accept", required = false) String mimeType,
                                           @RequestBody byte[] content,
                                           HttpServletRequest req,
                                           HttpServletResponse resp)
            throws BizInternalException, InvalidXmlException,
            BizPolicyException, IOException {

        // TODO Why doesn't spring do this?
        if (req.getContentType().contains("application/x-www-form-urlencoded")) {
            content =
                    URLDecoder.decode(new String(content, "UTF-8"), "UTF-8")
                            .getBytes("UTF-8");
        }

        Person user = getAuthenticatedUser();
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            Project newProject =
                    objectBuilder
                            .buildProject(new ByteArrayInputStream(content));
            String id = util.buildRequestUrl(req);

            Project originalProject = projectService.get(id);

            if (originalProject != null) {

                if (newProject.getId().equalsIgnoreCase(id)) {
                    if (authorizationService.canUpdateProject(user, newProject)) {
                        projectBizService.updateProject(newProject, user);
                        
                        resp.setCharacterEncoding("UTF-8");
                        resp.setContentType("text/xml");
                        
                        newProject = projectService.get(id);
                        Bop bop = new Bop();
                        bop.addProject(newProject);
                        objectBuilder.buildBusinessObjectPackage(bop, resp.getOutputStream());
                    }
                    else {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                } else {
                    try {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID doesn't match ID of supplied project");
                    } catch (Exception ee) {
                        log.debug("Handling exception", ee);
                    }
                }

            } else {
                try {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find project with id: " + idpart);
                } catch (Exception ee) {
                    log.debug("Handling exception", ee);
                }
            }
        }
    }

    @ExceptionHandler(ProjectServiceException.class)
    public void handleException(ProjectServiceException e, HttpServletRequest req, HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] [" + e.getMessage() + "]");
        resp.setStatus(500);
        
        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        }
        catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }
    
    @ExceptionHandler(BizPolicyException.class)
    public void handleException(BizPolicyException e, HttpServletRequest req, HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] [" + e.getMessage() + "]");
        
        if (e.getType() == Type.AUTHENTICATION_ERROR) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        }
        else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        }
        else if (e.getType() == BizPolicyException.Type.VALIDATION_ERROR) {
            resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
        else {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
        }
        
        // TODO have to set Authentication header?
        
        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        }
        catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }
    
    @ExceptionHandler(InvalidXmlException.class)
    public void handleException(InvalidXmlException e, HttpServletRequest req, HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] [" + e.getMessage() + "]");
        resp.setStatus(HttpStatus.SC_BAD_REQUEST);
        
        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        }
        catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }
    
    @ExceptionHandler(IOException.class)
    public void handleException(IOException e, HttpServletRequest req, HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] [" + e.getMessage() + "]");
        resp.setStatus(500);
        
        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        }
        catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }

    public void setBizService(ProjectBizService bizService) {
        this.projectBizService = bizService;
    }
    
    public ProjectBizService getBizService() {
        return projectBizService;
    }
    
    public AuthorizationService getAuthorizationService() {
        return this.authorizationService;
    }
    
    public void setAuthorizationService(AuthorizationService authorizationService) {
        if (authorizationService == null) {
            throw new IllegalArgumentException("Authorization Service must not be null.");
        }
        this.authorizationService = authorizationService;
    }

}