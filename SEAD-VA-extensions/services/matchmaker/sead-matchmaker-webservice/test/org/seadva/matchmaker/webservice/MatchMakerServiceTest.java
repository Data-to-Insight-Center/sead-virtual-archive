

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
 * MatchMakerServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
    package org.seadva.matchmaker.webservice;

    /*
     *  MatchMakerServiceTest Junit test case
    */

import org.apache.axis2.AxisFault;
import org.junit.Test;
import org.seadva.matchmaker.*;

import java.rmi.RemoteException;

public class MatchMakerServiceTest extends junit.framework.TestCase{

     
        /**
         * Auto generated test method
         */
        @Test
        public  void testgetMatch() throws java.lang.Exception{

    /*    org.seadva.matchmaker.webservice.MatchMakerServiceStub stub =
                    new org.seadva.matchmaker.webservice.MatchMakerServiceStub();//the default implementation should point to the right endpoint

           org.seadva.matchmaker.webservice.GetMatchRequest getMatchRequest18=
                                                        (org.seadva.matchmaker.webservice.GetMatchRequest)getTestObject(org.seadva.matchmaker.webservice.GetMatchRequest.class);
                    // TODO : Fill in the getMatchRequest18 here

            ClassAdType param = new ClassAdType();

            param.setType("user");
            CharacteristicsType characteristicsType = new CharacteristicsType();
            CharacteristicType[] characs = new CharacteristicType[1];
            CharacteristicType charac = new CharacteristicType();
            charac.setName("license");
            charac.setValue("CC");
            characs[0] = charac;
            characteristicsType.setCharacteristic(characs);
            param.setCharacteristics(characteristicsType);

            RequirementsType reqs = new RequirementsType();
            RuleType rules = new RuleType();
            rules.setObject("dspace");
            rules.setSubject("type");
            rules.setPredicate("equals");
            reqs.addRule(rules);
            param.setRequirements(reqs);

            PreferencesType preferencesType = new PreferencesType();
            RuleType[] rs = new RuleType[1];
            rs[0] = rules;
            preferencesType.setRule(rs);
            param.setPreferences(preferencesType);

            getMatchRequest18.setUserClassAd(param);
            GetMatchResponse resourcesResponse = stub.getMatch(
                    getMatchRequest18);
            for(int i =0; i<resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic().length;i++)
            System.out.print("printing "+
                    resourcesResponse.getResourceClassAd().getType()+" \n"+
                    resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getName() + " : " +
                    resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getValue() + "\n");
            param.setPreferences(new PreferencesType());
                        assertNotNull(resourcesResponse);
                  
            */


        GetMatchRequest getMatchRequest=
                new GetMatchRequest();

        ClassAdType param = new ClassAdType();

        param.setType("user");
        CharacteristicsType characteristicsType = new CharacteristicsType();
        CharacteristicType[] characs = new CharacteristicType[2];
        CharacteristicType licCharac = new CharacteristicType();
        licCharac.setName("license");
        licCharac.setValue("CC");
        CharacteristicType sizeCharac = new CharacteristicType();
        sizeCharac.setName("dataCollectionSize");
        sizeCharac.setValue("1073741825");
        characs[0] = licCharac;
        characs[1] = sizeCharac;
        characteristicsType.setCharacteristic(characs);
        param.setCharacteristics(characteristicsType);

        RequirementsType reqs = new RequirementsType();
        RuleType rules = new RuleType();
        rules.setSubject("type");
        rules.setPredicate("equals");
        rules.setObject("cloud");
        reqs.addRule(rules);
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
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        GetMatchResponse resourcesResponse = null;
        try {
            resourcesResponse = stub.getMatch(
                    getMatchRequest);
            System.out.print("printing "+
                    resourcesResponse.getResourceClassAd().getType()+" \n");
            for(int i =0; i<resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic().length;i++)
                System.out.print(
                        resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getName() + " : " +
                        resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getValue() + "\n");
            param.setPreferences(new PreferencesType());
            assertNotNull(resourcesResponse);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public  void testgetIdealsMatch() throws java.lang.Exception{

        GetMatchRequest getMatchRequest=
                new GetMatchRequest();

        ClassAdType param = new ClassAdType();

        param.setType("user");
        CharacteristicsType characteristicsType = new CharacteristicsType();
        CharacteristicType[] characs = new CharacteristicType[2];
        CharacteristicType licCharac = new CharacteristicType();
        licCharac.setName("license");
        licCharac.setValue("CC");
        CharacteristicType affiliation = new CharacteristicType();
        affiliation.setName("affiliation");
        affiliation.setValue("University of Illinois");
        characs[0] = licCharac;
        characs[1] = affiliation;
        characteristicsType.setCharacteristic(characs);
        param.setCharacteristics(characteristicsType);

        RequirementsType reqs = new RequirementsType();
        RuleType rules = new RuleType();
        rules.setSubject("type");
        rules.setPredicate("equals");
        rules.setObject("dspace");
        reqs.addRule(rules);
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
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        GetMatchResponse resourcesResponse = null;
        try {
            resourcesResponse = stub.getMatch(
                    getMatchRequest);
            System.out.print("printing "+
                    resourcesResponse.getResourceClassAd().getType()+" \n");
            for(int i =0; i<resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic().length;i++)
                System.out.print(
                        resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getName() + " : " +
                                resourcesResponse.getResourceClassAd().getCharacteristics().getCharacteristic()[i].getValue() + "\n");
            param.setPreferences(new PreferencesType());
            assertNotNull(resourcesResponse);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    /**
         * Auto generated test method
         */
        public  void testStartgetMatch() throws java.lang.Exception{
            org.seadva.matchmaker.webservice.MatchMakerServiceStub stub = new org.seadva.matchmaker.webservice.MatchMakerServiceStub();
             org.seadva.matchmaker.webservice.GetMatchRequest getMatchRequest18=
                                                        (org.seadva.matchmaker.webservice.GetMatchRequest)getTestObject(org.seadva.matchmaker.webservice.GetMatchRequest.class);
                    // TODO : Fill in the getMatchRequest18 here
                

                stub.startgetMatch(
                         getMatchRequest18,
                    new tempCallbackN65548()
                );
              


        }

        private class tempCallbackN65548  extends org.seadva.matchmaker.webservice.MatchMakerServiceCallbackHandler{
            public tempCallbackN65548(){ super(null);}

            public void receiveResultgetMatch(
                         org.seadva.matchmaker.webservice.GetMatchResponse result
                            ) {
                
            }

            public void receiveErrorgetMatch(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    