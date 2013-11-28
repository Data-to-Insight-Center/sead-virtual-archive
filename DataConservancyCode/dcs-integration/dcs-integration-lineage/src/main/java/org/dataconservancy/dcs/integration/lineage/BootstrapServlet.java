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
package org.dataconservancy.dcs.integration.lineage;


import org.dataconservancy.archive.api.ArchiveStore;
import org.dataconservancy.dcs.index.dcpsolr.DcpIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class BootstrapServlet {

    static Logger log = LoggerFactory.getLogger(MockIdService.class);
    
    private MockIdService idService;
    
    private DcpIndexService indexService;
    
    private ArchiveStore archiveStore;
    
    @Required
    public void setMockIdService(MockIdService ids) {
        idService = ids;
    }
    
    @Required
    public void setIndexService(DcpIndexService ids) {
        indexService = ids;
    }
    
    @Required
    public void setArchiveStore(ArchiveStore as) {
        archiveStore = as;
    }

    public void init(){
        try {
            log.info("Bootstrapping lineage webapp");

            LineageWebAppBootstrap.seedWebApp(idService, indexService, archiveStore);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}