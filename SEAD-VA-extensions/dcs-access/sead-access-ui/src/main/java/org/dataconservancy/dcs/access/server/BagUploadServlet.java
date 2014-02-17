/*
 * Copyright 2014 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.access.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.StringWriter;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import org.apache.commons.httpclient.HttpStatus;


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter;
import org.dataconservancy.dcs.access.client.upload.FileEditor;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.jersey.api.client.Client;

/**
 * Post a posted file to a the deposit file upload service. The return value is
 * html containing ^file_source_uri^file_upload_atom_feed_url^
 */
public class BagUploadServlet
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

            String bagUrl = null;

            for (FileItem item : items) {
                if (item.getFieldName() != null
                        && item.getFieldName().equals("bagUrl")) {
                	bagUrl = item.getString();
                }
            }

            if (bagUrl == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Missing required paremeter: depositurl");
                return;
            }
            
            for (FileItem item : items) {
                String name = item.getName();

                if (item.isFormField() || name == null || name.isEmpty()) {
                    continue;
                }
                
                String property = "java.io.tmpdir";


                String tempDir = System.getProperty(property);

                File dir = new File(tempDir);
                String path = dir.getAbsoluteFile()+"/"+item.getName();
                IOUtils.copy(item.getInputStream(), new FileOutputStream(path));
                getSIPfile(bagUrl, path, resp);
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

    public void getSIPfile(String bagitEp,
    						String filename,
    						HttpServletResponse resp
                            ) throws IOException {
    	Client client = Client.create();
        WebResource webResource = client
     		   .resource(bagitEp);

        File file = new File(filename);
        FileDataBodyPart fdp = new FileDataBodyPart("file", file,
                MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();

        formDataMultiPart.bodyPart(fdp);

        ClientResponse response = webResource
                .path("sip")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, formDataMultiPart);
       
        String sipPath = filename.replace(".zip", "_sip_0.xml");
        
        IOUtils.copy(response.getEntityInputStream(),new FileOutputStream(sipPath));
        
        resp.setStatus(200);

        resp.setContentType("text/html");
        resp.getWriter().print("<html><body><p>^" + sipPath
                + "^</p></body></html>");
        resp.flushBuffer();
        
    }
    
}
