/*
 * Copyright 2013 The Trustees of Indiana University
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

package org.dataconservancy.dcs.access.http;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.SeadConfig;
import org.dataconservancy.dcs.query.dcpsolr.SeadDataModelQueryService;
import org.dataconservancy.dcs.query.endpoint.utils.ServletUtil;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFormat;
import org.seadva.archive.ArchiveEnum;
import org.seadva.archive.impl.cloud.Sftp;
import org.seadva.model.SeadDataLocation;
import org.seadva.model.SeadFile;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class SeadDatastreamServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String MIME_TYPE_URI =
            "http://www.iana.org/assignments/media-types/";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private SeadConfig config;
    private DcsModelBuilder dcpbuilder;

    Sftp sftp;

    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
       
        this.dcpbuilder = new SeadXstreamStaxModelBuilder();
        this.config = SeadConfig.instance(
                getServletContext());

    }
    
    public void destroy() {
        try {
            config.dcpQueryService().shutdown();

        } catch (QueryServiceException e) {
           
        } finally {
            super.destroy();
        }
    }
    
    private void prepare(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String id = ServletUtil.getResource(req);

        id = id.replace(":/","://").replace(":///","://");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                           "Malformed entity id " + req.getPathInfo());
            return;
        }
        
        DcsEntity entity = null;
        
        try {

            SeadDataModelQueryService queryService = (SeadDataModelQueryService) config.dcpQueryService();
            entity = queryService.lookupEntity(id);
        } catch (Exception e) {
            final String msg = "Error performing search for entity '" + id + "': " + e.getMessage();
            log.error(msg, e);
            throw new IOException(msg, e);
        }
        
        if (entity == null) {
            final String msg = "No such entity " + id;
            log.error(msg);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return;
        }

        if (!(entity instanceof SeadFile)) {
            final String msg = "Entity is not a file " + id;
            log.error(msg);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
            return;
        }

        SeadFile file = (SeadFile) entity;

        if (file.getSizeBytes() > Integer.MAX_VALUE) {
            // TODO what response code? Do ranges?
            final String msg = "File " + entity.getId() + " too large";
            log.error(msg);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
            return;
        }


        if (file.getSizeBytes() != -1) {
            resp.setContentLength((int) file.getSizeBytes());
        }

        for (DcsFormat fmt : file.getFormats()) {
            if (fmt.getSchemeUri() != null
                    && fmt.getSchemeUri().equals(MIME_TYPE_URI)) {
                if (fmt.getFormat() != null) {
                    resp.setContentType(fmt.getFormat());
                }
            }
        }

        getFile(file, resp.getOutputStream());


        resp.setHeader("ETag", file.getName());
        resp.setHeader("fileName", file.getName());
        if(file.getFormats().size()>0){
            resp.setHeader("Content-Type", file.getFormats().iterator().next().getFormat());
        }
        resp.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + file.getName());
    }

    private void getFile(SeadFile file, OutputStream destination){

     String filePath = null;
     if(file.getPrimaryLocation().getType()!=null&&file.getPrimaryLocation().getType().length()>0
             &&file.getPrimaryLocation().getLocation()!=null&&file.getPrimaryLocation().getLocation().length()>0
             &&file.getPrimaryLocation().getName()!=null&&file.getPrimaryLocation().getName().length()>0
        ){
         if(
                 (file.getPrimaryLocation().getName().equalsIgnoreCase(ArchiveEnum.Archive.IU_SCHOLARWORKS.getArchive()))
                         ||
                         (file.getPrimaryLocation().getName().equalsIgnoreCase(ArchiveEnum.Archive.UIUC_IDEALS.getArchive())
                         )
                 ){
             URLConnection connection = null;
             try {
                 connection = new URL(file.getPrimaryLocation().getLocation()).openConnection();
                 connection.setDoOutput(true);
                 // Create a trust manager that does not validate certificate chains
                 final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                     @Override
                     public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
                     }
                     @Override
                     public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
                     }
                     @Override
                     public X509Certificate[] getAcceptedIssuers() {
                         return null;
                     }
                 } };
                 if(connection.getURL().getProtocol().equalsIgnoreCase("https")){
                     final SSLContext sslContext = SSLContext.getInstance( "SSL" );
                     sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
                     // Create an ssl socket factory with our all-trusting manager
                     final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                     ((HttpsURLConnection) connection ).setSSLSocketFactory( sslSocketFactory );
                 }
                 IOUtils.copy(connection.getInputStream(),destination);
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (KeyManagementException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             return;
         }
         else if( file.getPrimaryLocation().getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText())
                    &&file.getPrimaryLocation().getName().equalsIgnoreCase(
                    ArchiveEnum.Archive.SDA.getArchive())
                    ) {
                filePath = file.getPrimaryLocation().getLocation();

                String[] pathArr = filePath.split("/");

                try {
                    sftp = new Sftp(
                            config.getSdahost(),config.getSdauser(),config.getSdapwd(),config.getSdamount()
                            );
                    sftp.downloadFile(filePath.substring(0,filePath.lastIndexOf('/')), pathArr[pathArr.length-1], destination);
                    sftp.disConnectSession();
                } catch (JSchException e) {
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
          }
        }
        else{
         if(file.getSecondaryDataLocations()!=null&&file.getSecondaryDataLocations().size()>0){
             for(SeadDataLocation dataLocation:file.getSecondaryDataLocations()){
            if( dataLocation.getType().equalsIgnoreCase(ArchiveEnum.Archive.SDA.getType().getText())
                         &&dataLocation.getName().equalsIgnoreCase(
                         ArchiveEnum.Archive.SDA.getArchive())
                         ) {
                     filePath = dataLocation.getLocation();

                     String[] pathArr = filePath.split("/");

                     try {
                         sftp = new Sftp(
                                 config.getSdahost(),config.getSdauser(),config.getSdapwd(),config.getSdamount()
                         );
                         sftp.downloadFile(filePath.substring(0,filePath.lastIndexOf('/')), pathArr[pathArr.length-1], destination);
                         sftp.disConnectSession();
                     } catch (JSchException e) {
                         e.printStackTrace();
                     } catch (SftpException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
        }
        return;
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        prepare(req, resp);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        prepare(req, resp);
    }
}
