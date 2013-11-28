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
# File:  DoiUpdationService.java
# Description:  DoiUpdationService updates the DOI target to point to the data location in the Institutional Repository
#
# -----------------------------------------------------------------
#
*/

package org.dataconservancy.dcs.ingest.services;

import org.dataconservancy.dcs.id.api.ExtendedIdService;
import org.dataconservancy.dcs.id.api.IdMetadata;
import org.dataconservancy.dcs.id.api.IdentifierNotFoundException;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEvent;
import org.dataconservancy.model.dcs.DcsFile;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.*;

/**
 * DOI Updation Service
 */
public class DoiUpdationService extends IngestServiceBase
        implements IngestService {

    private ExtendedIdService idService;

    private String idPrefix = "http";
    private String doiShoulder;
    private String doiUserName;
    private String doiPassword;

    //Set these in applicationContext.xml
    @Required
    public void setIdentifierService(ExtendedIdService ids) {
        idService = ids;
    }

    @Required
    public void setDoiShoulder(String doiShoulder) {
        this.doiShoulder = doiShoulder;
    }

    @Required
    public void setDoiUserName(String doiUserName) {
        this.doiUserName = doiUserName;
    }

    @Required
    public void setDoiPassword(String doiPassword) {
        this.doiPassword = doiPassword;
    }

    public void execute(String sipRef) {
        if (isDisabled()) return;
        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);



        //create DOI only for parent Deliverable Unit
        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
        for (DcsDeliverableUnit d : dus) {
            Collection<DcsResourceIdentifier> alternateIds = null;
            if(d.getParents().isEmpty())  {
                alternateIds = d.getAlternateIds();
                if(alternateIds!=null)
                {
                    DcsResourceIdentifier id = null;
                    Iterator<DcsResourceIdentifier> idIt = alternateIds.iterator();

                    while(idIt.hasNext()){
                        id = idIt.next();
                        if(id.getTypeId().equalsIgnoreCase("doi")) {
                            Map metadata = new HashMap<IdMetadata.Metadata,String>();
                            String locationValue = ((SeadDeliverableUnit)d).getPrimaryLocation().getName();
                            if(locationValue==null)
                                locationValue = "http://seadva.d2i.indiana.edu:8181/sead-access/#entity;"+ d.getId();

                            metadata.put(IdMetadata.Metadata.TARGET, locationValue);
                            String[] tempShouler = doiShoulder.split(":");
                            int beginIndex = id.getIdValue().indexOf(tempShouler[tempShouler.length-1]);
                            String identifier = id.getIdValue().substring(beginIndex);
                            idService.setService("https://n2t.net/ezid/id/" +
                                    //"doi:10.5967/M0"
                                    //"doi:10.5072/FK2"
                                    "doi:"+identifier

                            );

                            try {
                                idService.createwithMd(metadata, true).getUid();
                            } catch (IdentifierNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }

                }
            }
        }
        addDoiEvent(sipRef);


        /* save the SIP containing updated entities */
        ingest.getSipStager().updateSIP(sip, sipRef);

    }


    private void addDoiEvent(String sipRef) {
        DcsEvent doiEvent =
                ingest.getEventManager().newEvent(Events.DOI_ID_UPDATION);

        Dcp dcp = ingest.getSipStager().getSIP(sipRef);
        doiEvent.setOutcome(Integer.toString(dcp.getFiles().size()+1));
        doiEvent.setDetail("DOI Updation Successful");

        ingest.getEventManager().addEvent(sipRef, doiEvent);
    }

    public HashMap<String,ArrayList<DcsFile>> getShapeFiles(Set<DcsFile> filesList){

        HashMap<String,ArrayList<DcsFile>> shapeFiles =  new HashMap<String, ArrayList<DcsFile>>();
        for(DcsFile file:filesList){
            String fileName = file.getName();
            String[] name = fileName.split("\\.");
            ArrayList<DcsFile> files =null;
            if(shapeFiles.containsKey(name[0]))
                files = shapeFiles.get(name[0]);
            else
                files = new ArrayList<DcsFile>();

            files.add(file);
            shapeFiles.put(name[0],files);
        }
        return  shapeFiles;
    }



}
