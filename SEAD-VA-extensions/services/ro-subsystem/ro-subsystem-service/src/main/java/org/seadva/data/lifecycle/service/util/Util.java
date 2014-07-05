/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.seadva.data.lifecycle.service.util;

import org.seadva.data.lifecycle.support.model.Entity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods
 */
public class Util {
    public Map<String, Entity> getIdEntityMap() {
        return idEntityMap;
    }

    public Map<String, Entity> getUrlEntityMap() {
        return urlEntityMap;
    }


    public Map<String, String> getGenUsed() {
        return genUsed;
    }

    Map<String, Entity> idEntityMap;
    Map<String, Entity> urlEntityMap;
    Map<String, String> genUsed;

    public int pullParse(InputStream input, String process) throws IOException {

        idEntityMap = new HashMap<String, Entity>();
        urlEntityMap = new HashMap<String, Entity>();

        genUsed = new HashMap<String, String>();


        XmlPullParserFactory factory;
        int count=0;
        try {
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput (input,null);
            int eventType = xpp.getEventType();

            int prov=0;
            int gen=0;
            int used=0;
            int entity=0;
            int url=0;
            int title = 0;
            int type =0;
            int doneGen = 0;
            int doneUsed = 0;
            String genStr = "";
            String usedStr ="";
            String titleStr = "";


            Entity entity1 = null;
            int ct =0;
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if(eventType == XmlPullParser.START_TAG) {

                    int temp =0;

                    if(entity==1)
                        temp=1;

                    if(xpp.getName().contains("wasDerivedFrom"))
                        prov=1;
                    if(xpp.getName().contains("generated"))
                        gen=1;
                    if(xpp.getName().contains("used"))
                        used=1;

                    if(xpp.getName().contains("entity"))
                        entity=1;

                    if(xpp.getName().contains("title"))
                        title=1;
                    if(xpp.getName().contains("type"))
                        type=1;
                    if(xpp.getName().contains("url"))
                        url=1;

                    if(temp==1&&type==0)
                        entity = 0;

                    if(prov==1&&gen ==1 && used ==0){
                        //get generated
                        genStr = xpp.getAttributeValue(0);
                        gen = 0;
                        doneGen = 1;
                    }

                    if(prov==1 && used ==1 && gen ==0){
                        //get used
                        usedStr = xpp.getAttributeValue(0);
                        used = 0;
                        doneUsed = 1;
                    }

                    if(prov==1&& doneGen==1&& doneUsed==1){
                        genUsed.put(genStr,usedStr);
                        prov=doneGen=doneUsed=0;
                    }




                    if(entity==1 && url ==0 && entity1 == null){
                        if(xpp.getAttributeCount()>0){

                            entity1 = new Entity();
                            entity1.setId(xpp.getAttributeValue(0));
                            ct = 0;
                        }
                        else
                            entity =0;

                    }

                    if(prov==1&& doneGen==1&& doneUsed==1)
                        prov=doneGen=doneUsed=0;



                }
                if(eventType == XmlPullParser.TEXT) {


                    if(title==1){
                        titleStr = xpp.getText();
                        if(entity1==null)
                            entity1 = new Entity();
                        entity1.setName(titleStr);
                        title = 0;
                        ct++;


                    }
                    else if(entity==1 && url ==1){
                        entity = 0;
                        url = 0;

                        if(entity1==null)
                            entity1 = new Entity();

                        entity1.setUrl(xpp.getText());
                    }
                }
                else if(eventType == XmlPullParser.END_TAG) {
                    if(xpp.getName().contains("entity")&&entity1!=null){
                        idEntityMap.put(entity1.getId(),entity1);
                        urlEntityMap.put(entity1.getUrl(),entity1);
                        entity1 =null;
                        ct = 0;
                    }
                }
                eventType = xpp.next();

            }

        }
        catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return count;
    }
}
