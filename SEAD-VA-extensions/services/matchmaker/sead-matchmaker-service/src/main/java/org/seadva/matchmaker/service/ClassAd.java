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

package org.seadva.matchmaker.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * org.dataconservancy.deposit.User: kavchand
 * Date: 11/19/12
 * Time: 10:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassAd {


    Characteristics characteristics = new Characteristics();
    List<Rule> requirements = new ArrayList<Rule>();
  //  List<Rule> preferences = new ArrayList<Rule>();

    public Characteristics getCharacteristics(){
        return characteristics;
    }

    public List<Rule> getRequirements(){
        return requirements;
    }

    public void setRequirements(List<Rule> requirements){
        this.requirements = requirements;
    }

    public void addRequirement(Rule rule){
        this.requirements.add(rule);
    }



    public boolean matches(ClassAd otherAd){

        int thisAdFlag = 0 ;
        int otherAdFlag = 0;

        for(int i=0;i<this.getRequirements().size();i++){
            //match requirements with characteristics of otherAd
            Rule rule = this.getRequirements().get(i);
            String otherAdsCharac = otherAd.characteristics.getValues().get(rule.getSubject());

            if(otherAdsCharac == null)  {
                thisAdFlag =1;
                break;
            }
            if(rule.getPredicate().equalsIgnoreCase("equals"))  {  //grammar equals
                if(otherAdsCharac.equalsIgnoreCase(rule.getObject()))
                    continue;
                else {
                    thisAdFlag =1;
                    break;
                }
            }
            if(rule.getPredicate().equalsIgnoreCase("greaterThan"))  {  //grammar greater Than

                try {
                    long value = Long.parseLong(rule.getObject());
                    if(Long.parseLong(otherAdsCharac)>value)
                        continue;
                    else {
                        thisAdFlag =1;
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    thisAdFlag =1;
                    break;
                }

            }

            if(rule.getPredicate().equalsIgnoreCase("lesserThan"))  {  //grammar lesser Than

                try {
                    long value = Long.parseLong(rule.getObject());
                    if(Long.parseLong(otherAdsCharac)<value)
                        continue;
                    else {
                        thisAdFlag =1;
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    thisAdFlag =1;
                    break;
                }

            }

        }

        for(int i=0;i<otherAd.getRequirements().size();i++){
            //match requirements with characteristics of this Ad

            Rule rule = otherAd.getRequirements().get(i);
            String thisAdsCharac =this.characteristics.getValues().get(rule.getSubject());

            if(thisAdsCharac == null)  {
                otherAdFlag =1;
                break;
            }
            if(rule.getPredicate().equalsIgnoreCase("equals"))  {  //grammar equals
                if(thisAdsCharac.equalsIgnoreCase(rule.getObject()))
                    continue;
                else {
                    otherAdFlag =1;
                    break;
                }
            }

            if(rule.getPredicate().equalsIgnoreCase("greaterThan"))  {  //grammar greater Than

                try {
                    long value = Long.parseLong(rule.getObject());
                    if(Long.parseLong(thisAdsCharac)>value)
                        continue;
                    else {
                        otherAdFlag =1;
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    otherAdFlag =1;
                    break;
                }

            }
            if(rule.getPredicate().equalsIgnoreCase("lesserThan"))  {  //grammar lesser Than

                try {
                    long value = Long.parseLong(rule.getObject());
                    if(Long.parseLong(thisAdsCharac)<value)
                        continue;
                    else {
                        otherAdFlag =1;
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    otherAdFlag =1;
                    break;
                }

            }

        }

        if(thisAdFlag==0 && otherAdFlag==0)
            return true;
        return false;
    }

    //public int getWeightage(ClassAd otherAd);

}
