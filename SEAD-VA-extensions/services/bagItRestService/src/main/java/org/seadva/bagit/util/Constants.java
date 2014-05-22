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

package org.seadva.bagit.util;

import org.apache.commons.io.IOUtils;
import org.seadva.bagit.model.MediciInstance;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants
 * */
public class Constants {

    public static String homeDir = null;
    public static String bagDir = null;
    public static String unzipDir = null;
    public static String untarDir = null;
    public static String sipDir = null;
    public static String FORMAT_IANA_SCHEME = "http://www.iana.org/assignments/media-types/";
    public static String titleTerm = "http://purl.org/dc/terms/title";
    public static String identifierTerm = "http://purl.org/dc/terms/identifier";
    public static String sizeTerm = "http://purl.org/dc/terms/SizeOrDuration";
    public static String rightsTerm = "http://purl.org/dc/terms/rights";
    public static String sourceTerm = "http://www.loc.gov/METS/FLocat";
    public static String formatTerm = "http://purl.org/dc/elements/1.1/format";
    public static String creatorTerm = "http://purl.org/dc/terms/creator";
    public static String issuedTerm = "http://purl.org/dc/terms/issued";
    public static String contactTerm = "http://purl.org/dc/terms/mediator";
    public static String locationTerm = "http://purl.org/dc/terms/Location";
    public static String abstractTerm = "http://purl.org/dc/terms/abstract";
    public static String contentSourceTerm = "http://purl.org/dc/terms/source";
    public static String contributor = "http://purl.org/dc/terms/contributor";
    public static String documentedBy  = "http://purl.org/spar/cito/isDocumentedBy";

    static{
        try {

            acrInstances = new Constants().loadAcrInstances();
            metadataPredicateMap = new Constants().loadAcrMetadataMapping();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<MediciInstance> acrInstances;

    public static Map<String, String> metadataPredicateMap;

    private  Map<String, String> loadAcrMetadataMapping() throws IOException{
        Map<String, String> metadataPredicateMap = new HashMap<String, String>();

        InputStream inputStream =
                Constants.class.getResourceAsStream("./ACR_to_ORE_MappingConfig.properties");

        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);

        String result = writer.toString();
        String[] pairs = result.trim().split(
                "\n|\\=");


        for (int i = 0; i + 1 < pairs.length;) {
            String name = pairs[i++].trim();
            String value = pairs[i++].trim();
            metadataPredicateMap.put(name,value);
        }
        return metadataPredicateMap;
    }

    private List<MediciInstance> loadAcrInstances() throws IOException{
        List<MediciInstance> instances = new ArrayList<MediciInstance>();

        InputStream inputStream =
                Constants.class.getResourceAsStream("./acrInstances.xml");
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);


        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
        try {
            xpp = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        try {

            xpp.setInput ( new StringReader(writer.toString()) );
            int eventType = xpp.getEventType();
            int id = 0;
            int url = 0;
            int remoteAPI = 0;
            int title = 0;
            int type = 0;
            int user = 0;
            int pwd = 0;

            MediciInstance instance = null;

            while (eventType != xpp.END_DOCUMENT) {
                if(eventType == xpp.START_TAG) {
                    if(xpp.getName().equals("instance"))
                        instance = new MediciInstance();
                    if(xpp.getName().equals("id"))
                        id = 1;
                    if(xpp.getName().equals("url"))
                        url = 1;
                    if(xpp.getName().equals("remoteAPI"))
                        remoteAPI = 1;
                    if(xpp.getName().equals("title"))
                        title = 1;
                    if(xpp.getName().equals("type"))
                        type = 1;
                    if(xpp.getName().equals("user"))
                        user = 1;
                    if(xpp.getName().equals("password"))
                        pwd = 1;
                }
                else if(eventType == xpp.TEXT) {
                    if(id==1){
                        instance.setId(Integer.parseInt(xpp.getText()));
                        id = 0;
                    }
                    else if(url==1){
                        instance.setUrl(xpp.getText());
                        url = 0;
                    }
                    else if(remoteAPI==1){
                        instance.setRemoteAPI(xpp.getText());
                        remoteAPI = 0;
                    }
                    else if(title ==1){
                        instance.setTitle(xpp.getText());
                        title = 0;
                    }
                    else if(type==1){
                        instance.setType(xpp.getText());
                        type = 0;
                    }else if(user==1){
                        instance.setUser(xpp.getText());
                        user = 0;
                    }else if(pwd==1){
                        instance.setPassword(xpp.getText());
                        pwd = 0;
                    }
                }
                else if(eventType == xpp.END_TAG) {
                    if(xpp.getName().equals("instance"))
                        instances.add(instance);
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instances;
    }
}
