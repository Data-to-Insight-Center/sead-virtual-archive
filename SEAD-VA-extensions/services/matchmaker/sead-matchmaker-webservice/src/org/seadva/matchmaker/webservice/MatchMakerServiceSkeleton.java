
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

/**
 * MatchMakerServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
    package org.seadva.matchmaker.webservice;

import org.apache.abdera.parser.stax.FOMCollection;
import org.apache.axiom.om.OMElement;
import org.seadva.matchmaker.*;
import org.seadva.matchmaker.service.ClassAd;
import org.seadva.matchmaker.service.Rule;
import org.seadva.matchmaker.resource.webservice.ResourceAgentServiceStub;
import org.seadva.matchmaker.resource.webservice.GetResourcesRequest;
import org.seadva.matchmaker.resource.webservice.GetResourcesResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
     *  MatchMakerServiceSkeleton java skeleton for the axisService
     */
    public class MatchMakerServiceSkeleton implements MatchMakerServiceSkeletonInterface{
        
         
        /**
         * Auto generated method signature
         * 
                                     * @param getMatchRequest0 
             * @return getMatchResponse1 
         */
        
                 public org.seadva.matchmaker.webservice.GetMatchResponse getMatch
                  (
                  org.seadva.matchmaker.webservice.GetMatchRequest getMatchRequest0
                  )
            {
                //TODO : fill this with the necessary business logic
                //throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#getMatch");
               // GetMatchResponse response = new GetMatchResponse();

                //return response;
                try{
                    ClassAd userAd= new ClassAd();
                    CharacteristicType[] characteristics =  getMatchRequest0.getUserClassAd().getCharacteristics().getCharacteristic();

                    for(int i =0; i < characteristics.length;i++){
                        userAd.getCharacteristics().addValue(characteristics[i].getName(), characteristics[i].getValue());
                    }

                    RuleType[] rules = getMatchRequest0.getUserClassAd().getRequirements().getRule();

                    if(rules!=null)
                        for(int i =0; i < rules.length;i++){
                            Rule tempRule = new Rule();
                            tempRule.setObject(rules[i].getObject());
                            tempRule.setSubject(rules[i].getSubject());
                            tempRule.setPredicate(rules[i].getPredicate());
                            userAd.addRequirement(tempRule);
                        }


                    // Agent userAgent = new UserAgent(); //user agent would have invoked this method
                    //implement a call back handler

                    ResourceAgentServiceStub resourceAgentServiceStub = new ResourceAgentServiceStub();
                    GetResourcesRequest resourcesRequest = new GetResourcesRequest();
                    OMElement paramtemp = new FOMCollection();
                    resourcesRequest.setGetResourcesRequest(paramtemp);
                    GetResourcesResponse resourcesResponse = resourceAgentServiceStub.getResources(resourcesRequest);
                    //Agent resourceAgent = new ResourceAgent();
                    //get it from  SOAP Service

                    List<ClassAd> resourceAds = new ArrayList<ClassAd>();
                    ClassAd resultAd = null;

                    for(ClassAdType resourceAd:resourcesResponse.getResourceClassAd()){
                        ClassAd resource = new ClassAd();
                        characteristics =  resourceAd.getCharacteristics().getCharacteristic();

                        for(int i =0; i < characteristics.length;i++){
                            resource.getCharacteristics().addValue(characteristics[i].getName(), characteristics[i].getValue());
                        }

                        rules = resourceAd.getRequirements().getRule();

                        if(rules!=null)
                            for(int i =0; i < rules.length;i++){
                                Rule tempRule = new Rule();
                                tempRule.setObject(rules[i].getObject());
                                tempRule.setSubject(rules[i].getSubject());
                                tempRule.setPredicate(rules[i].getPredicate());
                                resource.addRequirement(tempRule);
                            }
                        resourceAds.add(resource);
                    }


                    for(int i=0;i<resourceAds.size();i++){
                        if(userAd.matches(resourceAds.get(i))){
                            resultAd = resourceAds.get(i); //found match
                            break;
                        }
                    }

                    ClassAdType matchedAd = new ClassAdType();

                    if(resultAd != null){
                        CharacteristicType[] characs = new CharacteristicType[resultAd.getCharacteristics().getValues().size()];
                        Iterator iterator = resultAd.getCharacteristics().getValues().entrySet().iterator();
                        int index =0 ;
                        while(iterator.hasNext()){
                            Map.Entry pair = (Map.Entry)iterator.next();
                            characs[index] = new CharacteristicType();
                            characs[index].setName((String)pair.getKey());
                            characs[index].setValue((String)pair.getValue());
                            index++;
                        }
                        CharacteristicsType param = new CharacteristicsType();
                        param.setCharacteristic(characs);

                        matchedAd.setCharacteristics(param);
                        matchedAd.setType("resource");
                        matchedAd.setPreferences(new PreferencesType());
                        matchedAd.setRequirements(new RequirementsType());

                    }
                    else{
                        matchedAd.setType("Not found");
                        matchedAd.setCharacteristics(new CharacteristicsType());
                        matchedAd.setPreferences(new PreferencesType());
                        matchedAd.setRequirements(new RequirementsType());
                    }


                    //copy the below requirements too if possible
                    GetMatchResponse response = new GetMatchResponse();
                    response.setResourceClassAd(matchedAd);
                    return response;

                    //return null;

                }
                catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
        }
     
    }
    