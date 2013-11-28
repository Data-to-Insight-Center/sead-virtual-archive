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
# File:  MatchmakerClient.java
# Description:  MatchmakerClient retrieves data set characteristics and queries Matchmaker service to find a matching Institutional Repository
#
# -----------------------------------------------------------------
#
*/
package org.dataconservancy.dcs.ingest.services;

import org.apache.axis2.AxisFault;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.dcs.ingest.services.util.VivoUtil;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsCollectionRef;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsEntityReference;
import org.dataconservancy.model.dcs.DcsEvent;
import org.seadva.matchmaker.*;
import org.seadva.matchmaker.webservice.GetMatchRequest;
import org.seadva.matchmaker.webservice.GetMatchResponse;
import org.seadva.matchmaker.webservice.MatchMakerServiceStub;
import org.seadva.model.SeadDeliverableUnit;
import org.seadva.model.SeadPerson;
import org.seadva.model.SeadRepository;
import org.seadva.model.builder.api.SeadModelBuilder;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;

/**
 *  MatchmakerClient retrieves data set characteristics and queries Matchmaker service to find a matching Institutional Repository
 *
 *  @author Kavitha Chandrasekar
 */
public class MatchmakerClient
        extends IngestServiceBase
        implements IngestService {

    private static final Logger log =
            LoggerFactory.getLogger(MatchmakerClient.class);

    private DcsModelBuilder builder;
    @Required
    public void setModelBuilder(DcsModelBuilder mb) {
        builder = mb;
    }

    public void execute(String sipRef) throws IngestServiceException {

        if (isDisabled()) return;

        VivoUtil util = new VivoUtil();

        ResearchObject dcp = (ResearchObject)ingest.getSipStager().getSIP(sipRef);


//start query matchmaker
        int j =0;
        GetMatchRequest getMatchRequest=
                new GetMatchRequest();

        CharacteristicsType characteristicsType = new CharacteristicsType();
        CharacteristicType[] characs = new CharacteristicType[5];

        ClassAdType param = new ClassAdType();

        for(DcsDeliverableUnit du: dcp.getDeliverableUnits()){
            if(du.getParents()==null||du.getParents().size()==0){
                if(((SeadDeliverableUnit)du).getSubmitter()!=null){
                    SeadPerson submitter = ((SeadDeliverableUnit)du).getSubmitter();
                    CharacteristicType submitterCharac = new CharacteristicType();
                    submitterCharac.setName("submitter");
                    submitterCharac.setValue(submitter.getIdType());
                    characs[j++] = submitterCharac;
                }
                if(du.getCreators()!=null){
                    for(SeadPerson creator:((SeadDeliverableUnit)du).getDataContributors()){
                        String affiliation = null;
                        if(creator.getName().equals("http")){
                            affiliation = util.getEmailFromDirtyName(creator.getId().replace("\n","").replace("display","individual").replace(" ",""));
                        }
                        else
                            affiliation = util.getEmail(creator.getId().replace("\n","").replace("display","individual").replace(" ",""));
                        if(affiliation!=null)
                        {
                            CharacteristicType affiliationCharac = new CharacteristicType();
                            affiliationCharac.setName("affiliation");
                            affiliationCharac.setValue(affiliation);
                            characs[j++] = affiliationCharac;
                        }

                    }
                }
            }
        }
        param.setType("user");
        CharacteristicType licCharac = new CharacteristicType();
        licCharac.setName("license");
        licCharac.setValue("CC");
        characs[j++] = licCharac;

        long fileNos = dcp.getFiles().size();

        if(fileNos==0)
            for(DcsDeliverableUnit du:dcp.getDeliverableUnits())
                if(du.getParents()==null||du.getParents().size()==0)
                    fileNos = ((SeadDeliverableUnit)du).getFileNo();


        characteristicsType.setCharacteristic(characs);
        param.setCharacteristics(characteristicsType);

        RequirementsType reqs = new RequirementsType();
        RuleType rules = new RuleType();
        rules.setSubject("gentype");
        rules.setPredicate("equals");
        rules.setObject("repo");
        reqs.addRule(rules);

        rules = new RuleType();
        rules.setSubject("dataCollectionSize");
        rules.setPredicate("greaterThan");
        rules.setObject(String.valueOf(fileNos));
        reqs.addRule(rules);

        for(DcsDeliverableUnit du: dcp.getDeliverableUnits()){
            if(du.getParents()==null||du.getParents().size()==0){
               if(du.getRights()!=null&&    du.getRights().equalsIgnoreCase("restricted")){

                   RuleType rules2 = new RuleType();
                   rules2.setSubject("access");
                   rules2.setPredicate("equals");
                   rules2.setObject("restricted");
                    reqs.addRule(rules2);
                   break;
               }
            }
        }
        param.setRequirements(reqs);

        PreferencesType preferencesType = new PreferencesType();
        RuleType[] rs = new RuleType[1];
        rs[0] = rules;
        preferencesType.setRule(rs);
        param.setPreferences(preferencesType);

        getMatchRequest.setUserClassAd(param);
        MatchMakerServiceStub stub = null;
        try {
            stub = new MatchMakerServiceStub();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        GetMatchResponse resourcesResponse;
        try {
            resourcesResponse = stub.getMatch(
                    getMatchRequest);

            SeadRepository repository = new SeadRepository();

            String repositoryName = null;
            String message = "No resource match found.";
            System.out.print("printing "+
                    resourcesResponse.getResourceClassAd().getType()+" \n");
            if(resourcesResponse.getResourceClassAd().getType().equalsIgnoreCase("resource")){
                for(int i =0; i<resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic().length;i++){
                    if(resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getName().equalsIgnoreCase("id"))
                        repository.setIrId(resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getValue());
                    else if(resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getName().equalsIgnoreCase("name"))
                    {
                        repositoryName = resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getValue();
                        if(repositoryName.contains("Ideals"))
                            repository.setName("Ideals");
                        else
                            repository.setName(repositoryName);
                    }
                    else if(resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getName().equalsIgnoreCase("type"))
                        repository.setType(resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getValue());
                    else if(resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getName().equalsIgnoreCase("url"))
                        repository.setUrl(resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getValue());

                }

                param.setPreferences(new PreferencesType());
                message = "Matched Repository " + repositoryName + " for the given dataset collection.";
                dcp.addRepository(repository);
                //addMatchEvent(sipRef, message);//This here does not work because it is here
                ingest.getSipStager().updateSIP(dcp,sipRef);
                addMatchEvent(sipRef, message);
            }
            else{
                addMatchEvent(sipRef, message);
            }


        } catch (RemoteException e) {
            throw new RuntimeException("Error matching to repository", e);
        }
    }

    private void addMatchEvent(String sipRef, String message) {
        DcsEvent matchEvent =
                ingest.getEventManager().newEvent(Events.MATCH_MAKING);

        Dcp dcp = ingest.getSipStager().getSIP(sipRef);
        DcsDeliverableUnit parent = null;
        for(DcsDeliverableUnit du:dcp.getDeliverableUnits()){
            if(du.getParents()==null)
            {
                parent = du;
                break;
            }
            else if(du.getParents().size()==0)
            {
                parent =du;
                break;
            }

        }

       matchEvent.setDetail(message);
        if(parent!=null ) {
            DcsEntityReference ref = new DcsCollectionRef();
            ref.setRef(parent.getId());
            matchEvent.addTargets(ref);
        }


        ingest.getEventManager().addEvent(sipRef, matchEvent);
    }


}
