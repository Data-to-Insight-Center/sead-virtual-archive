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
package org.dataconservancy.dcs.ingest.ui.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Post a posted file to a the deposit file upload service. The return value is
 * html containing ^file_source_uri^file_upload_atom_feed_url^
 */
public class FileUploadServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                           "Error: Request type not supported.");
            return;
        }

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            List<FileItem> items = upload.parseRequest(req);

            String depositurl = null;

            for (FileItem item : items) {
                if (item.getFieldName() != null
                        && item.getFieldName().equals("depositurl")) {
                    depositurl = item.getString();
                }
            }

            if (depositurl == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Missing required paremeter: depositurl");
                return;
            }

            for (FileItem item : items) {
                String name = item.getName();

                if (item.isFormField() || name == null || name.isEmpty()) {
                    continue;
                }

                uploadfile(depositurl, name, item.getInputStream(), resp);
            }
        } catch (IOException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "Error: " + e.getMessage());
            return;
        } catch (FileUploadException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "Error: " + e.getMessage());
            return;
        }
    }

    private void uploadfile(String depositurl,
                            String filename,
                            InputStream is,
                            HttpServletResponse resp) throws IOException {
        File tmp = null;
        FileOutputStream fos = null;
        PostMethod post = null;

        //System.out.println(filename + " -> " + depositurl);

        try {
            tmp = File.createTempFile("fileupload", null);
            fos = new FileOutputStream(tmp);
            FileUtil.copy(is, fos);

            HttpClient client = new HttpClient();

            post = new PostMethod(depositurl);

            Part[] parts = {new FilePart(filename, tmp)};

            post.setRequestEntity(new MultipartRequestEntity(parts, post
                    .getParams()));

            int status = client.executeMethod(post);

            if (status == HttpStatus.SC_ACCEPTED
                    || status == HttpStatus.SC_CREATED) {
                resp.setStatus(status);

                String src = post.getResponseHeader("X-dcs-src").getValue();
                String atomurl = post.getResponseHeader("Location").getValue();

                resp.setContentType("text/html");
                resp.getWriter().print("<html><body><p>^" + src + "^" + atomurl
                        + "^</p></body></html>");
                resp.flushBuffer();
            } else {
                resp.sendError(status, post.getStatusText());
                return;
            }
        } finally {
            if (tmp != null) {
                tmp.delete();
            }
            
            if (is != null) {
                is.close();
            }

            if (fos != null) {
                fos.close();
            }

            if (post != null) {
                post.releaseConnection();
            }
        }
    }
}
