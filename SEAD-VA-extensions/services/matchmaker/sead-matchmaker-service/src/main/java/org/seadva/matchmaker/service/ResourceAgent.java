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


import org.seadva.matchmaker.Agent;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * org.dataconservancy.deposit.User: kavchand
 * Date: 11/19/12
 * Time: 7:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceAgent implements Agent {



  //  HashMap<String, RepositoryClassAd> resourceMap ;
    List advertisements;

    public ResourceAgent(){
        advertisements = new ArrayList<ClassAd>();
        loadAds();
    }

/*    public RepositoryClassAd getResource(String resourceId){

         return resourceMap.get(resourceId);
    } */




    @Override
    public List<ClassAd> getAdvertisements() {

        return advertisements;
    }

    @Override
    public void loadAds() {

        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput ( ResourceAgent.class.getResourceAsStream("resources.xml"),null);
            int eventType = xpp.getEventType();
            int charac=0;
           // int req =0;
           // int newResoure=0;
            int key =0;

            String keyStr="";
            String valStr="";

            int subject=0;
            int predicate =0;
            int object =0;

            String subjectStr = null;
            String predicateStr = null;
            String objectStr = null;



            ClassAd classAd  = new RepositoryClassAd();

            List<Rule> ruleList = null;

            while (eventType != xpp.END_DOCUMENT) {
                if(eventType == xpp.START_TAG) {
                    ;
                    if(xpp.getName().equals("resource"))   {
                        classAd = new RepositoryClassAd();
                        //newResoure=1;
                    }

                    if(xpp.getName().equals("characteristics"))//&&newResoure==1)
                        charac =1;

                    if(xpp.getName().equals("requirements")){//&&newResoure==1){
                        ruleList = new ArrayList<Rule>();
                       // req =1;
                    }
                    if(xpp.getName().equals("preferences")){//&&newResoure==1){
                        ruleList = new ArrayList<Rule>();
                        // req =1;
                    }

                    if(charac==1){
                        keyStr = xpp.getName();
                        key =1;
                    }

                    if(xpp.getName().equals("subject")){
                        subject = 1;
                    }
                    if(xpp.getName().equals("predicate")){
                        predicate = 1;
                    }
                    if(xpp.getName().equals("object")){
                        object = 1;
                    }

                }
                else if(eventType == xpp.TEXT) {
                    if(xpp.getText().replace(" ","").length()==0)
                    {
                        eventType = xpp.next();
                        continue;
                    }
                    if(key==1 ){//&& newResoure==1){
                        valStr= xpp.getText();
                        classAd.getCharacteristics().addValue(keyStr,valStr);
                        key=0;
                    }

                    if(subject ==1){//&&req==1){
                        subjectStr = xpp.getText();
                        subject =0;
                    }
                    if(object ==1){//&&req==1){
                        objectStr = xpp.getText();
                        object = 0;
                    }
                    if(predicate ==1){//&&req==1){
                        predicateStr = xpp.getText();
                        predicate = 0;
                    }




                }
                else if(eventType == xpp.END_TAG) {
                    if(xpp.getName().equals("rule"))
                    {
                        Rule rule = new Rule();
                        rule.setSubject(subjectStr);
                        rule.setPredicate(predicateStr);
                        rule.setObject(objectStr);
                        ruleList.add(rule);
                    }

                    if(xpp.getName().equals("characteristics"))
                        charac=0;
                    if(xpp.getName().equals("requirements"))  {
                        classAd.setRequirements(ruleList);
                    }
                    if(xpp.getName().equals("preferences"))  {
                        //classAd.setPreferences(ruleList);
                    }

                    if(xpp.getName().equals("resource")){//&& newResoure==1) {

                        //newResoure = 0;
                        this.getAdvertisements().add(classAd);
                    }


                }
                eventType = xpp.next();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    String readFile(String filePath) throws IOException {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final FileReader reader = new FileReader(filePath);
        try {
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }
}
