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

import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.CONTENT_DISPOSITION;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.ETAG;
import static org.dataconservancy.ui.api.support.ResponseHeaderUtil.LAST_MODIFIED;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.dataconservancy.ui.api.support.RequestUtil;
import org.dataconservancy.ui.exceptions.ArchiveServiceException;
import org.dataconservancy.ui.exceptions.BizPolicyException;
import org.dataconservancy.ui.model.DataFile;
import org.dataconservancy.ui.model.Person;
import org.dataconservancy.ui.services.ArchiveService;
import org.dataconservancy.ui.services.FileBizService;
import org.dataconservancy.ui.services.MetadataFileBizService;
import org.dataconservancy.ui.services.RelationshipConstraintException;
import org.dataconservancy.ui.services.UserService;
import org.dataconservancy.ui.util.ETagCalculator;
import org.dataconservancy.ui.util.MimeTypeComparator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Entry point to obtaining representations Data Files.  Currently the specification of the HTTP API is being
 * tracked in the <a href="https://scm.dataconservancy.org/confluence/x/FYCZ">File API</a> wiki page.
 *
 * @see <a href="https://scm.dataconservancy.org/confluence/x/FYCZ">https://scm.dataconservancy.org/confluence/x/FYCZ</a>
 */
@Controller
@RequestMapping("/file")
public class FileController extends BaseController {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private FileBizService fileBizService;
    private MetadataFileBizService metadataFileBizService;
    private ArchiveService archiveService;
    public FileController(UserService userService, FileBizService fileBizService, RequestUtil requestUtil,
                          ArchiveService archiveService, MetadataFileBizService metadataFileBizService) {
        super(userService, requestUtil);
        this.fileBizService = fileBizService;
        this.archiveService = archiveService;
        this.metadataFileBizService = metadataFileBizService;
    }

    /**
     * @param mimeType
     * @param modifiedSince
     * @param request
     * @throws java.io.IOException
     * @throws org.dataconservancy.ui.exceptions.BizPolicyException
     *
     */
    @RequestMapping(method = {RequestMethod.GET})
    public void handleEmptyGetRequest(@RequestHeader(value = "Accept", required = false) String mimeType,
                                      @RequestHeader(value = "If-Modified-Since", required = false) @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
                                      HttpServletRequest request,
                                      HttpServletResponse resp)
            throws ServletException, IOException, BizPolicyException {

        resp.setStatus(HttpStatus.SC_FORBIDDEN);
        resp.getWriter().println("Not yet implemented.");
    }

    /**
     * Accepts the parameters as specified below and returns the content of the file with matching id and qualifies the
     * criteria specified in the request header.
     * @param idpart id string that uniquely identify requested.
     * @param mimeType specifies the acceptable type of the response's content. The matching file's mime type is
     *        determined using the {@link javax.activation.MimetypesFileTypeMap}'s default map.
     * @param modifiedSince specifies that the request is only for file with matching id that has been modified
     *        since the {@code modifiedSince} date. Files with matching ids that has not been modified since
     *        {@code modifiedSince} date will be disqualified and not returned.
     * @param request http request
     * @throws {@link IOException} when an exception occurs when writing to the {@link HttpServletResponse} object.
     *
     */
    @RequestMapping(value = "/{idpart}", method = {RequestMethod.GET})
    public void handleFileGetRequest(@PathVariable String idpart,
                                       @RequestHeader(value = "Accept", required = false) String mimeType,
                                       @RequestHeader(value = "If-Modified-Since", required = false)
                                       @DateTimeFormat(iso = DATE_TIME) Date modifiedSince,
                                       HttpServletRequest request,
                                       HttpServletResponse resp) throws IOException {

       // this impl does not require authz at the moment, but need a user for the fileBizService getFile method
       // ok if null
       Person user = getAuthenticatedUser();

        // Get file id from the request
        String id = requestUtil.buildRequestUrl(request);

        // Resolve the Request URL to the ID of the file (in this case URL == ID)
        if (id == null || id.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try {

            archiveService.pollArchive();

            // Get file's last modified date via the fileBizService if it is a DataFile, or
            // via the metadataFileBizService if it is a MetadataFile

            DateTime lastModifiedDate = (fileBizService.getLastModifiedDate(id) != null) ? fileBizService.getLastModifiedDate(id) : metadataFileBizService.getLastModifiedDate(id);

            // Handle if-modified-since header
            if (failIfModifiedSinceHeader(request, resp, modifiedSince, lastModifiedDate)) {
                return;
            }

            // Get file via fileBizService if it is a DataFile, or
            // via the metadataFileBizService if it is a MetadataFile
            DataFile file = null;
            if (fileBizService.getFile(id, user) != null) {
                file = fileBizService.getFile(id, user);
            } else {
                file = metadataFileBizService.retrieveMetadataFile(id);
            }

            //If file is not found
            if (file == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No file matching this id " + id + " was found.");
            } else { //if file is found

                String fileMimeType = file.getFormat();

                if (fileMimeType == null) {
                    fileMimeType = "application/octet-stream";
                }

                //Handling mimeType header
                if (failAcceptHeader(request, resp, mimeType, fileMimeType)) {
                    return;
                }

                //Set the Content-Length
                resp.setContentLength((int)file.getSize());
                resp.setStatus(HttpStatus.SC_OK);
                //Calculate ETAG
                resp.setHeader(ETAG, ETagCalculator.calculate(Integer.toString(file.hashCode())));
                DateTimeFormatter fmt = new DateTimeFormatterBuilder().appendPattern("EEE, dd MMM yyyy HH:mm:ss Z")
                        .toFormatter();
                String rfcDate = fmt.print(lastModifiedDate);
                resp.setHeader(LAST_MODIFIED, rfcDate);
                //Set Content-Disposition
                resp.setHeader(CONTENT_DISPOSITION, getResponseContentDispositionString("\"" + file.getName() + "\"", file.getSize()));
                //Set Content-Type
                resp.setContentType(fileMimeType);
                
                InputStream is = new URL(file.getSource()).openStream();
                IOUtils.copy(is, resp.getOutputStream());
                is.close();
            }
        } catch (BizPolicyException be) {
            handleException(be, request, resp);
        } catch (ArchiveServiceException ae) {
            handleException(ae, request, resp);
        } catch (RelationshipConstraintException re) {
            handleException(re, request, resp);
        }
    }

    private String getResponseContentDispositionString(String fileName, long fileSize) {
        return "attachment; filename=" + fileName + ";size=" + fileSize;
    }

    boolean failAcceptHeader(HttpServletRequest request, HttpServletResponse response,
                             String acceptMimeType, String fileMimeType) throws IOException {
        if (acceptMimeType == null || fileMimeType == null) {
            return false;
        }
        if (MimeTypeComparator.isAcceptableMimeType(acceptMimeType, fileMimeType)) {
            return false;
        }
        response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, String.format("File with id matching %s" +
                " did not have an acceptable mime type (%s) as specified by the request header",
                requestUtil.buildRequestUrl(request), acceptMimeType));
        return true;
    }
    
    boolean failIfModifiedSinceHeader(HttpServletRequest req, HttpServletResponse res, Date ifModifiedSince,
                                      DateTime lastModified) throws IOException {

        if (ifModifiedSince == null) {
            return false;
        }

        if (lastModified == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find associated DataItem for the file to " +
                    "determine its last modified date."  );
            return true;
        }

        final DateTime ifModifiedDateTime = new DateTime(ifModifiedSince);

        if (lastModified.isAfter(ifModifiedDateTime)) {
            return false;
        }

        res.sendError(HttpServletResponse.SC_NOT_MODIFIED  );

        return true;
    }

    public void setFileBizService(FileBizService fileBizService) {
        this.fileBizService = fileBizService;
    }
    
    public FileBizService getFileBizService() {
        return fileBizService;
    }

    public void setMetadataFileBizService(MetadataFileBizService metadataFileBizService) {
        this.metadataFileBizService = metadataFileBizService;
    }

    public MetadataFileBizService getMetadataFileBizService() {
        return metadataFileBizService;
    }
    
    public ArchiveService getArchiveService() {
        return archiveService;
    }
    
    public void setArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }
    
    public RequestUtil getRequestUtil() {
        return requestUtil;
    }
    

}
