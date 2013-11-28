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
import org.dataconservancy.dcs.util.DateUtility;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.api.support.ResponseHeaderUtil;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.ArchiveDepositInfo;
import org.dataconservancy.ui.model.Bop;
import org.dataconservancy.ui.model.DataItem;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.model.builder.BusinessObjectBuilder;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.AuthorizationService;
import org.dataconservancy.ui.services.DataItemBizService;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.util.ETagCalculator;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT_APPLICATION_WILDCARD;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT_WILDCARD;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.APPLICATION_XML;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ACCEPT_OCTET_STREAM;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_LENGTH;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_TYPE;
import static org.dataconservancy.ui.model.ArchiveDepositInfo.Status.DEPOSITED;

/**
 * Entry point to obtaining representations of Data Items.  Currently the specification of the HTTP API is being
 * tracked <a href="https://scm.dataconservancy.org/confluence/x/IICZ">here</a>
 *
 * @see <a href="https://scm.dataconservancy.org/confluence/x/IICZ">https://scm.dataconservancy.org/confluence/x/IICZ</a>
 */
@Controller
@RequestMapping("/item")
public class DataItemController extends BaseController {

    private RequestUtil requestUtil;

    private ResponseHeaderUtil responseHeaderUtil;

    private AuthorizationService authzService;

    private BusinessObjectBuilder bob;

    private ArchiveService archiveService;

    private DataItemBizService dataItemBizService;
    
    public DataItemController(UserService userService, AuthorizationService authzService, ArchiveService archiveService,
                              BusinessObjectBuilder bob, RequestUtil requestUtil, DataItemBizService dataItemBizService) {
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
        this.dataItemBizService = dataItemBizService;
    }

    @RequestMapping(value = "/{idpart}", method = RequestMethod.GET)
    public void handleDataItemGetRequest(@RequestHeader(value = "Accept", required = false) String mimeType,
                                         @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                         @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                                         @RequestHeader(value = "If-Modified-Since", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date modifiedSince,
                                         HttpServletRequest request, HttpServletResponse response) throws IOException, ArchiveServiceException, BizPolicyException {

        // Check to see if the user is authenticated (TODO: have Spring Security be responsible for this?)
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
        // Resolve the Request URL to the ID of the DataItem (in this case URL == ID)
        String dataItemId = requestUtil.buildRequestUrl(request);

        if (dataItemId == null || dataItemId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        // Get the DataItem
        final DataItem dataItem = getDataItem(dataItemId);
        // Calculate the ETag for the DataItem, may be null.
        final String etag;
        if (dataItem != null) {
            etag = calculateEtag(dataItem);
        } else {
            etag = null;
        }
        // Handle the 'If-Match' header first; RFC 2616 14.24
        if (this.responseHeaderUtil.handleIfMatch(request, response, this.requestUtil, ifMatch, dataItem, etag, dataItemId, "DataItem")) {
            return;
        }

        final DateTime lastModified;
        if (dataItem != null) {
          lastModified = getLastModified(dataItem.getId());
        } else {
          lastModified = null;
        }
        // Handle the 'If-None-Match' header; RFC 2616 14.26
        if (this.responseHeaderUtil.handleIfNoneMatch(request,  response, ifNoneMatch, dataItem, etag, dataItemId, lastModified, modifiedSince)) {
            return;
        }

        if (dataItem == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Handle the 'If-Modified-Since' header; RFC 2616 14.26
        if (this.responseHeaderUtil.handleIfModifiedSince(request, response, modifiedSince, lastModified)) {
            return;
        }
        // Check to see if the user is authorized
        if (!authzService.canRetrieveDataSet(authenticatedUser, dataItem)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // Compose the Business Object Package
        Bop businessPackage = new Bop();
        businessPackage.addDataItem(dataItem);

        // Serialize the package to an output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bob.buildBusinessObjectPackage(businessPackage, out);
        out.close();
        // Compose the Response (headers, entity body)

        this.responseHeaderUtil.setResponseHeaderFields(response, etag, out, lastModified);

        // Send the Response
        final ServletOutputStream servletOutputStream = response.getOutputStream();
        IOUtils.copy(new ByteArrayInputStream(out.toByteArray()), servletOutputStream);
        servletOutputStream.flush();
        servletOutputStream.close();
    }

    String calculateEtag(DataItem dataItem) {
        return ETagCalculator.calculate(String.valueOf(dataItem.hashCode()));
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
     * @return DataItem or null if not existing.
     * @throws BizPolicyException 
     * @throws ArchiveServiceException 
     */
    DataItem getDataItem(String id) throws ArchiveServiceException, BizPolicyException {
       return dataItemBizService.getDataItem(id, getAuthenticatedUser()); 
    }

    DateTime getLastModified(String dataSetId) {
        List<ArchiveDepositInfo> depositInfo = archiveService.listDepositInfo(dataSetId, DEPOSITED);
        if (!depositInfo.isEmpty()) {
            return depositInfo.get(0).getDepositDateTime();
        }

        return null;
    }
}
