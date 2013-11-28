/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: dcs-ingest-services
# File:  DataOneIdService.java
# Description:  DataOneIdService creates DataONE Ids for data collection
#
# -----------------------------------------------------------------
#
*/

package org.dataconservancy.dcs.ingest.services;

import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadPerson;
import org.seadva.model.pack.ResearchObject;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class DataOneIdService extends IngestServiceBase
        implements IngestService {

     public void execute(String sipRef) {
        if (isDisabled()) return;


        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);

        String creator = "none";
        for(DcsDeliverableUnit du:sip.getDeliverableUnits()){
            SeadDeliverableUnit sdu = (SeadDeliverableUnit)du;
            if(sdu.getParents()==null||sdu.getParents().size()==0){
                Set<SeadPerson> creators = sdu.getDataContributors();
                if(creators!=null&&creators.size()>0)
                    creator = creators.iterator().next().getName();
            }

        }
        Collection<DcsFile> files = sip.getFiles();

        for(DcsFile file:files){
            DcsResourceIdentifier altId = new DcsResourceIdentifier();
            altId.setTypeId("dataone");
            altId.setIdValue("seadva-"+creator.replace(" ","").replace(",","")+UUID.randomUUID().toString());
            file.addAlternateId(altId);
        }

        sip.setFiles(files);

        addIdEvent(sipRef);
        /* save the SIP containing updated entities */
        ingest.getSipStager().updateSIP(sip, sipRef);

    }


    private void addIdEvent(String sipRef) {
        DcsEvent idEvent =
                ingest.getEventManager().newEvent(Events.ID_ASSIGNMENT);

        Dcp dcp = ingest.getSipStager().getSIP(sipRef);
        idEvent.setOutcome(Integer.toString(dcp.getFiles().size()+1));
        idEvent.setDetail("DOI Assignment Successful");

        ingest.getEventManager().addEvent(sipRef, idEvent);
    }
}
