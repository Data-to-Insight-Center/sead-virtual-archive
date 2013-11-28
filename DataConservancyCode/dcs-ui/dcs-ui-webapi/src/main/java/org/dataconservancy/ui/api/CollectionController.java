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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.api.support.ResponseHeaderUtil;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizInternalException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.exceptions.ProjectServiceException;
import org.dataconservancy.ui.model.*;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.services.*;
import org.dataconservancy.ui.util.ETagCalculator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT_APPLICATION_WILDCARD;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT_WILDCARD;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.APPLICATION_XML;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT_OCTET_STREAM;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_LENGTH;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_TYPE;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.DEPOSITED;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

/**
 * Entry point to obtaining representations of Collections (and sub-Collections).  Currently the specification of the
 * HTTP API is being tracked <a href="https://scm.dataconservancy.org/confluence/x/voCZ">here</a>
 *
 * @see <a href="https://scm.dataconservancy.org/confluence/x/voCZ">https://scm.dataconservancy.org/confluence/x/IICZ</a>
 */
@Controller
@RequestMapping("/collection")
public class CollectionController extends BaseController {

    private RequestUtil requestUtil;

    private ResponseHeaderUtil responseHeaderUtil;

    private AuthorizationService authzService;

    private BusinessObjectBuilder bob;

    private ArchiveService archiveService;

    private CollectionBizService collectionBizService;

    public CollectionController(UserService userService,
                                AuthorizationService authzService,
                                ArchiveService archiveService,
                                BusinessObjectBuilder bob,
                                RequestUtil requestUtil,
                                CollectionBizService collectionBizService) {
        super(userService, requestUtil);

        if (authzService == null) {
            throw new IllegalArgumentException("Authorization Service must not be null.");
        }

        if (archiveService == null) {
            throw new IllegalArgumentException("Archive Service must not be null.");
        }

        if (bob == null) {
            throw new IllegalArgumentException("Business Object Builder must not be null.");
        }

        this.requestUtil = requestUtil;
        this.responseHeaderUtil = new ResponseHeaderUtil();
        this.authzService = authzService;
        this.bob = bob;
        this.archiveService = archiveService;
        this.collectionBizService = collectionBizService;
    }

    @RequestMapping(value = "/{idpart}", method = RequestMethod.GET)
    public void handleCollectionGetRequest(@RequestHeader(value = "Accept", required = false) String mimeType,
                                           @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                           @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                                           @RequestHeader(value = "If-Modified-Since", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date modifiedSince,
                                           HttpServletRequest request, HttpServletResponse response) throws IOException, ArchiveServiceException, BizPolicyException, BizInternalException {

        // Check to see if the user is authenticated (TODO: Spring Security should be responsible for this)
        // Note that the fact that the user has to be authenticated, and further authorized, is a policy decision,
        // but the pattern for the Project and Person controllers is that the Controller has handled this.
        final Person authenticatedUser = getAuthenticatedUser();

        // Rudimentary Accept Header handling; accepted values are */*, application/*, application/xml,
        // application/octet-stream
        if (mimeType != null &&
            !(mimeType.contains(APPLICATION_XML) ||
                mimeType.contains(ACCEPT_WILDCARD) ||
                mimeType.contains(ACCEPT_APPLICATION_WILDCARD) ||
                mimeType.contains(ACCEPT_OCTET_STREAM))) {
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Unacceptable value for 'Accept' header: '" +
                mimeType + "'");
            return;
        }

        // Resolve the Request URL to the ID of the Collection (in this case URL == ID)
        String collectionId = requestUtil.buildRequestUrl(request);

        if (collectionId == null || collectionId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Get the Collection
        final Collection collection = getCollection(collectionId);

        // Calculate the ETag for the Collection, may be null.
        final String etag;
        if (collection != null) {
            etag = calculateEtag(collection);
        } else {
            etag = null;
        }

        // Handle the 'If-Match' header first; RFC 2616 14.24
        if (this.responseHeaderUtil.handleIfMatch(request, response, this.requestUtil, ifMatch, collection, etag, collectionId, "Collection")) {
            return;
        }

        final DateTime lastModified;
        if (collection != null) {
          lastModified = getLastModified(collection.getId());
        } else {
          lastModified = null;
        }

        // Handle the 'If-None-Match' header; RFC 2616 14.26
        if (this.responseHeaderUtil.handleIfNoneMatch(request,  response, ifNoneMatch, collection, etag, collectionId, lastModified, modifiedSince)) {
            return;
        }

        if (collection == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Handle the 'If-Modified-Since' header; RFC 2616 14.26
        if (this.responseHeaderUtil.handleIfModifiedSince(request, response, modifiedSince, lastModified)) {
            return;
        }

        // Check to see if the user is authorized
        if (!authzService.canRetrieveCollection(authenticatedUser, collection)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Compose the Business Object Package
        Bop businessPackage = new Bop();
        businessPackage.addCollection(collection);

        // Serialize the package to an output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bob.buildBusinessObjectPackage(businessPackage, out);
        out.close();

        this.responseHeaderUtil.setResponseHeaderFields(response, etag, out, lastModified);

        // Send the Response
        final ServletOutputStream servletOutputStream = response.getOutputStream();
        IOUtils.copy(new ByteArrayInputStream(out.toByteArray()), servletOutputStream);
        servletOutputStream.flush();
        servletOutputStream.close();
    }

    String calculateEtag(Collection collection) {
        return ETagCalculator.calculate(String.valueOf(collection.hashCode()));
    }

    public AuthorizationService getAuthzService() {
        return authzService;
    }

    public void setAuthzService(AuthorizationService authzService) {
        if (authzService == null) {
            throw new IllegalArgumentException("Authorization Service must not be null.");
        }
        this.authzService = authzService;
    }

    public BusinessObjectBuilder getBob() {
        return bob;
    }

    public void setBob(BusinessObjectBuilder bob) {
        if (bob == null) {
            throw new IllegalArgumentException("Business Object Builder must not be null.");
        }
        this.bob = bob;
    }

    public RequestUtil getRequestUtil() {
        return requestUtil;
    }

    public void setRequestUtil(RequestUtil requestUtil) {
        if (requestUtil == null) {
            throw new IllegalArgumentException("Request Util must not be null");
        }
        this.requestUtil = requestUtil;
    }

    public ArchiveService getArchiveService() {
        return archiveService;
    }

    public void setArchiveService(ArchiveService archiveService) {
        if (archiveService == null) {
            throw new IllegalArgumentException("Archive Service must not be null");
        }
        this.archiveService = archiveService;
    }

    /**
     *
     * @param id
     * @return Collection or null if not existing.
     * @throws BizPolicyException
     * @throws ArchiveServiceException
     * @throws BizInternalException
     */
    Collection getCollection(String id) throws ArchiveServiceException, BizPolicyException, BizInternalException {
        return collectionBizService.getCollection(id, getAuthenticatedUser());
    }

    DateTime getLastModified(String collectionId) {
        List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(collectionId, DEPOSITED);
        if (!depositInfo.isEmpty()) {
            return depositInfo.get(0).getDepositDateTime();
        }

        return null;
    }

}
