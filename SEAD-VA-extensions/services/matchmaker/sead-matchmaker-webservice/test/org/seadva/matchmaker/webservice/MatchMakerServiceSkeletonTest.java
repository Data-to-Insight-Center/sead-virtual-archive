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

package org.seadva.matchmaker.webservice;

import org.junit.Test;
import org.seadva.matchmaker.*;

/**
 * Created with IntelliJ IDEA.
 * User: kavchand
 * Date: 3/18/13
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchMakerServiceSkeletonTest extends junit.framework.TestCase{

    @Test
    public void testMatch(){

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
        sizeCharac.setValue("1073741823");
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

        MatchMakerServiceSkeleton serviceSkeleton = new MatchMakerServiceSkeleton();
        GetMatchResponse response = serviceSkeleton.getMatch(getMatchRequest);
        System.out.println(response.getResourceClassAd());

    }

    @Test
    public void testIdealsMatch(){

        GetMatchRequest getMatchRequest=
                new GetMatchRequest();

        ClassAdType param = new ClassAdType();

        param.setType("user");
        CharacteristicsType characteristicsType = new CharacteristicsType();
        CharacteristicType[] characs = new CharacteristicType[1];
        CharacteristicType licCharac = new CharacteristicType();
        licCharac.setName("license");
        licCharac.setValue("CC");
        CharacteristicType sizeCharac = new CharacteristicType();
//        sizeCharac.setName("affiliation");
//        sizeCharac.setValue("University of Illinois");

//        sizeCharac.setName("dataCollectionSize");
//        sizeCharac.setValue("2000");
        characs[0] = licCharac;
//        characs[1] = sizeCharac;
        characteristicsType.setCharacteristic(characs);
        param.setCharacteristics(characteristicsType);

        RequirementsType reqs = new RequirementsType();
        RuleType rules = new RuleType();
        rules.setSubject("gentype");
        rules.setPredicate("equals");
        rules.setObject("repo");

        rules = new RuleType();
        rules.setSubject("dataCollectionSize");
        rules.setPredicate("greaterThan");
        rules.setObject("2000");
        reqs.addRule(rules);
        param.setRequirements(reqs);

        PreferencesType preferencesType = new PreferencesType();
        RuleType[] rs = new RuleType[1];
        rs[0] = rules;
        preferencesType.setRule(rs);
        param.setPreferences(preferencesType);

        getMatchRequest.setUserClassAd(param);

        MatchMakerServiceSkeleton serviceSkeleton = new MatchMakerServiceSkeleton();
        GetMatchResponse response = serviceSkeleton.getMatch(getMatchRequest);
        System.out.println(response.getResourceClassAd());

    }
}
