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

import org.apache.http.HttpStatus;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class provides the common elements and methods that are used in its descendant classes.
 */
public class BaseController {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected final UserService userService;

    protected final RequestUtil requestUtil;


    public BaseController(UserService userService, RequestUtil requestUtil) {
        if (requestUtil == null) {
            throw new IllegalArgumentException("Request Util must not be null");
        }
        if (userService == null) {
            throw new IllegalArgumentException("User Service must not be null");
        }
        this.userService = userService;
        this.requestUtil = requestUtil;
    }

    public Person getAuthenticatedUser() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication authn = ctx.getAuthentication();
        if (authn != null && authn.getPrincipal() != null
                && authn.getPrincipal() instanceof Person) {
            Person loggedInUser = (Person) authn.getPrincipal();

            if (loggedInUser != null && userService != null) {
                return userService.get(loggedInUser.getId());
            }

            return loggedInUser;
        }
        return null;
    }


    @ExceptionHandler(InvalidXmlException.class)
    public void handleException(InvalidXmlException e,
                                HttpServletRequest req,
                                HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] ["
                + e.getMessage() + "]");
        resp.setStatus(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);

        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }


    @ExceptionHandler(BizPolicyException.class)
    public void handleException(BizPolicyException e,
                                HttpServletRequest req,
                                HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] ["
                + e.getMessage() + "]");

        if (e.getType() == BizPolicyException.Type.AUTHENTICATION_ERROR) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        } else if (e.getType() == BizPolicyException.Type.AUTHORIZATION_ERROR) {
            resp.setStatus(HttpStatus.SC_FORBIDDEN);
        } else {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
        }

        // TODO have to set Authentication header?

        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }

    @ExceptionHandler(IOException.class)
    public void handleException(IOException e,
                                HttpServletRequest req,
                                HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] ["
                + e.getMessage() + "]");
        resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }

    @ExceptionHandler(RelationshipConstraintException.class)
    public void handleException(RelationshipConstraintException e,
                                HttpServletRequest req,
                                HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] ["
                + e.getMessage() + "]");
        resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }

    @ExceptionHandler(ArchiveServiceException.class)
    public void handleException(ArchiveServiceException e,
                                HttpServletRequest req,
                                HttpServletResponse resp) {
        log.debug("Handling exception [" + e.getClass() + "] ["
                + e.getMessage() + "]");
        resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        try {
            resp.getWriter().print(e.getMessage());
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (Exception ee) {
            log.debug("Handling exception", e);
        }
    }

}
