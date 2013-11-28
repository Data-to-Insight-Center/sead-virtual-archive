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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
}
