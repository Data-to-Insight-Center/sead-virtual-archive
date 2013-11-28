
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
 * ResourceAgentServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
    package org.seadva.matchmaker.resource.webservice;

import org.seadva.matchmaker.*;
import org.seadva.matchmaker.Agent;
import org.seadva.matchmaker.service.ClassAd;
import org.seadva.matchmaker.service.ResourceAgent;
import org.seadva.matchmaker.service.Rule;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
     *  ResourceAgentServiceSkeleton java skeleton for the axisService
     */
    public class ResourceAgentServiceSkeleton implements ResourceAgentServiceSkeletonInterface{
        
         
        /**
         * Auto generated method signature
         * 
                                     * @param getResourcesRequest0 
             * @return getResourcesResponse1 
         */
        
                 public org.seadva.matchmaker.resource.webservice.GetResourcesResponse getResources
                  (
                  org.seadva.matchmaker.resource.webservice.GetResourcesRequest getResourcesRequest0
                  )
            {
                try{
                    Agent resourceAgent = new ResourceAgent();
                    //get it from  SOAP Service
                    List<ClassAd> resourceAds = resourceAgent.getAdvertisements();

                    GetResourcesResponse response = new GetResourcesResponse();

                    ClassAdType[] resources = new ClassAdType[resourceAds.size()];

                    int i = 0;
                    for(ClassAd resourceAd:resourceAds){
                        resources[i] = new ClassAdType();

                        CharacteristicsType characs = new CharacteristicsType();
                        CharacteristicType[] characArray = new CharacteristicType[resourceAd.getCharacteristics().getValues().size()];
                        Iterator characIt =  resourceAd.getCharacteristics().getValues().entrySet().iterator();
                        int j =0;
                        while(characIt.hasNext()){
                            Map.Entry<String,String> characPair = (Map.Entry<String,String>)characIt.next();
                            characArray[j] = new CharacteristicType();
                            characArray[j].setName(characPair.getKey());
                            characArray[j].setValue(characPair.getValue());
                            j++;
                        }
                        characs.setCharacteristic(characArray);
                        resources[i].setCharacteristics(characs);


                        RequirementsType requirements = new RequirementsType();
                        RuleType[] rules = new RuleType[resourceAd.getRequirements().size()];

                        j =0;
                        for(Rule rule: resourceAd.getRequirements()){
                            rules[j] = new RuleType();
                            rules[j].setSubject(rule.getSubject());
                            rules[j].setPredicate(rule.getPredicate());
                            rules[j].setObject(rule.getObject());
                            j++;
                        }

                        requirements.setRule(rules);
                        resources[i].setRequirements(requirements);

                        resources[i].setPreferences(new PreferencesType());
                        resources[i].setType("resource");
                        i++;
                    }
                    response.setResourceClassAd(resources);
                    return response;
                }
                catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
        }
     
    }
    