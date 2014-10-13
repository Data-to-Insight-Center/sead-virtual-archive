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

package org.dataconservancy.archive.impl.dspacerepo;

import org.dataconservancy.model.dcs.DcsMetadata;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Util to load credentials for repositories
 */
public class Util {

    public static Map<String,Credential> loadCredentials(InputStream stream) throws IOException, XmlPullParserException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput (stream, "utf-8");
        int eventType = xpp.getEventType();

        Map<String,Credential> repoCredentials = new HashMap<String, Credential>();
        Credential credential = null;
        String key = "";

        int idFlag =0 ;
        int usernameFlag = 0;
        int pwdFlag =0 ;

        while (eventType != xpp.END_DOCUMENT) {
            if(eventType == xpp.START_TAG) {

                if(xpp.getName().equals("repository"))
                    credential = new Credential();
                if(xpp.getName().equals("id"))
                    idFlag = 1;
                if(xpp.getName().equals("username"))
                    usernameFlag = 1;

                if(xpp.getName().equals("password"))
                    pwdFlag = 1;
            }
            else if(eventType == xpp.TEXT) {

                if(idFlag==1){
                    key =xpp.getText();
                }
                else if(usernameFlag==1){
                    credential.setUsername(xpp.getText());
                }
                else if(pwdFlag==1){
                    credential.setPassword(xpp.getText());
                }
            }
            else if(eventType == xpp.END_TAG) {
                if(xpp.getName().equals("id"))
                    idFlag = 0;
                if(xpp.getName().equals("username"))
                    usernameFlag = 0;

                if(xpp.getName().equals("password"))
                    pwdFlag = 0;

                if(xpp.getName().equals("repository"))
                    repoCredentials.put(key,credential);
            }
            eventType = xpp.next();
        }

        return repoCredentials;
    }

    public static Map<String, List<String>> extractMetadata(Collection<DcsMetadata> metadata) {
        Map<String, List<String>> metadataMap = new HashMap<String, List<String>>();

        for (DcsMetadata meta : metadata) {
            String metadataElement = meta.getMetadata();
            int predicateIndex = metadataElement.indexOf("http");
            if (predicateIndex < 0) {
                continue;
            }
            String temp = metadataElement.substring(predicateIndex);
            String predicate = temp.substring(0, temp.indexOf('<'));
            String value = temp.substring(temp.indexOf('<'));
            value = value.substring(value.indexOf('>') + 1);
            value = value.substring(value.indexOf('>') + 1);
            value = value.substring(0, value.indexOf('<'));
//            System.out.println("**" + predicate + " : " + value);
            List<String> values = metadataMap.get(predicate);
            if (values == null) {
                values = new ArrayList<String>();
                metadataMap.put(predicate, values);
            }
            values.add(value);
        }
        return metadataMap;
    }

    public static String formatName(String name) {
        String formattedName = "";
        if (name != null && !name.equals("")) {
            int i = name.indexOf(',');
            String first = name.substring(0, i).trim();
            String last =  name.substring(i + 1).trim();
            formattedName = first + ", " + last;
        }
        return formattedName;
    }

    public static void addDimMetadata(Element root, String element,
                                      String qualifier, String text) {
        if (element == null) {
            return;
        }
        Namespace ns = root.getNamespace();
        Element child = new Element("field", ns);
        child.setText(text);
        child.setAttribute("element", element);
        if (qualifier != null) {
            child.setAttribute("qualifier", qualifier);
        }
        child.setAttribute("mdschema", "dc");
        root.addContent(child);
    }

    public static Document getDimTemplate() throws JDOMException, IOException {
        // dim template
        String dim = "<dim:dim dspaceType=\"ITEM\" xmlns:dim=\"http://www.dspace.org/xmlns/dspace/dim\">\n" +
                "<dim:field element=\"language\" qualifier=\"rfc3066\" mdschema=\"dc\"/>\n" +
                "</dim:dim>";
        InputStream in = new ByteArrayInputStream(dim.getBytes());
        // parse as a jdom document
        SAXBuilder builder = new SAXBuilder();
        return builder.build(in);
    }
}
