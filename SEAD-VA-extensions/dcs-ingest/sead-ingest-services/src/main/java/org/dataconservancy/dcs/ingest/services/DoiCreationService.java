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
# File:  DoiCreationService.java
# Description:  DoiCreationService creates DOI for data collection
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
import org.seadva.model.SeadPerson;
import org.seadva.model.pack.ResearchObject;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.*;

/**
 * DOI Creation Service
 */
public class DoiCreationService  extends IngestServiceBase
        implements IngestService {

    private ExtendedIdService idService;

    private String doiShoulder;
    private String doiUserName;
    private String doiPassword;


     //Set these
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

        String creator = null;
        String year = null;

        idService.setCredentials(doiUserName, doiPassword);
        idService.setService("https://n2t.net/ezid/shoulder/" +
          //      "doi:10.5967/M0"
          //      "doi:10.5072/FK2"
                doiShoulder
        );  //set using Spring configuration file

        ResearchObject sip = (ResearchObject)ingest.getSipStager().getSIP(sipRef);



        //create DOI only for parent Deliverable Unit
        Collection<DcsDeliverableUnit> dus = sip.getDeliverableUnits();
        for (DcsDeliverableUnit d : dus) {
            Collection<DcsResourceIdentifier> alternateIds = null;
            if(d.getParents().isEmpty())  {
                alternateIds = d.getAlternateIds();
                if(alternateIds==null)
                    alternateIds= new HashSet<DcsResourceIdentifier>();
                else{
                    DcsResourceIdentifier id = null;
                    Iterator<DcsResourceIdentifier> idIt = alternateIds.iterator();

                    int alreadySet =0;
                    while(idIt.hasNext()){
                        id = idIt.next();

                        if(id.getTypeId().equalsIgnoreCase("doi")) {
                           alreadySet=1;
                        }
                    }
                    if(alreadySet==1)
                        continue;
                }
                Map metadata = new HashMap<IdMetadata.Metadata,String>();
                metadata.put(IdMetadata.Metadata.TARGET,"http://dummyUrl");
                if(d.getTitle()!=null)
                    metadata.put(IdMetadata.Metadata.TITLE,d.getTitle());

                if(d.getCreators()!=null)
                {
                    if(d.getCreators().size()>0)
                    {
                        Iterator<SeadPerson> creators = ((SeadDeliverableUnit)d).getDataContributors().iterator();
                        while(creators.hasNext())
                            creator = creators.next().getName()+"\t";

                        metadata.put(IdMetadata.Metadata.CREATOR,creator);
                    }
                }

                if(((SeadDeliverableUnit)d).getPubdate()!=null)
                {
                    String[] date = null;
                    if(((SeadDeliverableUnit)d).getPubdate().contains("/"))
                        date = ((SeadDeliverableUnit)d).getPubdate().split("/");
                    else if(((SeadDeliverableUnit)d).getPubdate().contains("-"))
                        date = ((SeadDeliverableUnit)d).getPubdate().split("-");

                    if(date!=null)
                        for(int i =0; i<date.length; i++){
                            if(date[i].length()==4) {
                                year = date[i];
                                metadata.put(IdMetadata.Metadata.PUBDATE, year);
                                break;
                            }
                        }
                }




                try {
                    DcsResourceIdentifier id = new DcsResourceIdentifier();
                    String doi =idService.createwithMd(metadata, false).getUid();
                    doi = "http://dx.doi.org/"+doi.substring(doi.indexOf("doi:")+4,doi.indexOf("|"));
                    id.setIdValue(doi);
                    id.setTypeId("doi");
                    alternateIds.add(id);//add DOI creation as a common service
                } catch (IdentifierNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                d.setAlternateIds(alternateIds);
            }
        }
        sip.setDeliverableUnits(dus);

        addDoiEvent(sipRef);


        /* save the SIP containing updated entities */
        ingest.getSipStager().updateSIP(sip, sipRef);

    }


    private void addDoiEvent(String sipRef) {
        DcsEvent doiEvent =
                ingest.getEventManager().newEvent(Events.DOI_ID_ASSIGNMENT);

        Dcp dcp = ingest.getSipStager().getSIP(sipRef);
        doiEvent.setOutcome(Integer.toString(dcp.getFiles().size()+1));
        doiEvent.setDetail("DOI Assignment Successful");

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
