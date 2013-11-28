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
package org.dataconservancy.dcs.access.http.dataPackager;

import com.jcraft.jsch.JSchException;
import org.dataconservancy.dcs.index.solr.support.SolrQueryUtil;
import org.dataconservancy.dcs.query.api.QueryMatch;
import org.dataconservancy.dcs.query.api.QueryResult;
import org.dataconservancy.dcs.query.api.QueryServiceException;
import org.dataconservancy.dcs.query.dcpsolr.DcsDataModelQueryService;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntity;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsManifestation;
import org.seadva.archive.ArchiveEnum;
import org.seadva.archive.impl.cloud.Sftp;
import org.dataconservancy.dcs.query.endpoint.utils.dcpsolr.Config;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadFile;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeleteServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Config config;
    private org.seadva.archive.impl.cloud.utils.Config archiveconfig;
    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(DeleteServlet.class);
    
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);
        this.config = Config.instance(getServletContext());
        this.archiveconfig = org.seadva.archive.impl.cloud.utils.Config.instance(getServletContext());
    }

    public void destroy() {
        try {
            this.config.dcpQueryService().shutdown();
        } catch (QueryServiceException e) {
           
        } finally {
            super.destroy();
        }
    }

    private boolean deleteEntity(String id) throws IOException {
        Sftp sftp = null;//this.archiveconfig.getSdaStore().getSftp();
        try {
            sftp = new Sftp(null,null,null,null);
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            DcsDataModelQueryService queryService = (DcsDataModelQueryService) config.dcpQueryService();

            String query = SolrQueryUtil.createLiteralQuery("ancestry", id);

            QueryResult<DcsEntity> result = null;
            try {
                result = queryService.query(query, 0L, 200, new String[0]);
            } catch (QueryServiceException e) {
                e.printStackTrace();
            }
            List<QueryMatch<DcsEntity>> matches = result.getMatches();
            List<QueryMatch<DcsEntity>> tempMatches = new ArrayList<QueryMatch<DcsEntity>>(matches);
            for (QueryMatch<DcsEntity> match : tempMatches) {
                if(match.getObject() instanceof DcsFile){
                    String locationName = ((SeadFile)match.getObject()).getPrimaryLocation().getName();
                    if(locationName.equals(ArchiveEnum.Archive.SDA.getArchive()))
                    {
                        String location = ((SeadFile)match.getObject()).getPrimaryLocation().getLocation();
                        sftp.deleteFile(location);
                    }
                //    queryService.deleteEntity(match.getObject().getId());
                    matches.remove(match);
                }
            }

            tempMatches = new ArrayList<QueryMatch<DcsEntity>>(matches);
            for (QueryMatch<DcsEntity> match : tempMatches) {
                if(match.getObject() instanceof DcsManifestation){
                    DcsManifestation manifestation = ((DcsManifestation)match.getObject());
                    String parentDu = manifestation.getDeliverableUnit();
                    DcsEntity entity = queryService.lookupEntity(parentDu);

                    if(((SeadDeliverableUnit)entity).getPrimaryLocation().getName().equals(ArchiveEnum.Archive.SDA.getArchive()))
                    {
                        String idSubstring = match.getObject().getId().substring(match.getObject().getId().lastIndexOf("/")+1);
                        String location = ((SeadDeliverableUnit)entity).getPrimaryLocation().getLocation()+"/man_"+idSubstring;
                        sftp.deleteDirectory(location);
                    }
                 //   queryService.deleteEntity(match.getObject().getId());
                    matches.remove(match);
                }
            }

            tempMatches = new ArrayList<QueryMatch<DcsEntity>>(matches);
            for (QueryMatch<DcsEntity> match : tempMatches) {
                if(match.getObject() instanceof DcsDeliverableUnit){
                    String locationName = ((SeadDeliverableUnit)match.getObject()).getPrimaryLocation().getName();
                    if(locationName.equals(ArchiveEnum.Archive.SDA.getArchive()))
                    {
                        String location = ((SeadDeliverableUnit)match.getObject()).getPrimaryLocation().getLocation();
                        sftp.deleteDirectory(location);
                    }
             //       queryService.deleteEntity(match.getObject().getId());
                    matches.remove(match);
                }
             }

            DcsEntity entity = queryService.lookup(id);
            if(entity instanceof SeadDeliverableUnit){
                String locationName = ((SeadDeliverableUnit)entity).getPrimaryLocation().getName();
                if(locationName.equals(ArchiveEnum.Archive.SDA.getArchive()))
                {
                    String location = ((SeadDeliverableUnit)entity).getPrimaryLocation().getLocation();
                    sftp.deleteDirectory(location);
                }
            }
           // queryService.deleteEntity(id);

        } catch (ClassCastException e) {
            log.debug(e.getMessage());
            return false;
        } catch (QueryServiceException e) {
            log.debug(e.getMessage());
            return false;
        }
        
        return true;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String id = req.getParameter("id");
        //get ancestry
        //get all files and delete files and folders - only if they are stored in our cloud repository
        boolean deleted = deleteEntity(id);
        if (!deleted) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sorry there was some error deleting the collection/file(s) "
                    + id);
            return;
        }
        if(deleted)
            resp.setStatus(200);
        else
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sorry there was some error deleting the collection/file(s) "
                    + id);
        resp.flushBuffer();
    }


}
