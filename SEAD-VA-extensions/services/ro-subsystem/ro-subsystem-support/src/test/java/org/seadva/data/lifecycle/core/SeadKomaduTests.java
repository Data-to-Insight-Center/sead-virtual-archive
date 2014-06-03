/*
#
# Copyright 2014 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
*/

package org.seadva.data.lifecycle.core;

import edu.indiana.d2i.komadu.axis2.client.*;
import edu.indiana.d2i.komadu.query.*;
import edu.indiana.d2i.komadu.query.EntityEnumType;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.seadva.data.lifecycle.support.KomaduIngester;
import org.seadva.data.lifecycle.support.model.Entity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

@RunWith(JUnit4.class)
public class SeadKomaduTests {

    private static KomaduServiceStub stub = null;
    private static String agentGraphId = null;

    static String komaduURL = "http://localhost:8080/axis2/services/KomaduService";
    @BeforeClass
    public static void init() {
        try {

            stub = new KomaduServiceStub(komaduURL);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


    @Test
    public void testEventAgentGraph() {
        //Takes time to retrieve the graph

        agentGraphId = "agent1_7000";
        try {
            System.out.println("\n\n Agent Graph without context \n\n");
            GetAgentGraphRequestDocument agentGraphRequest = GetAgentGraphRequestDocument.Factory.newInstance();
            GetAgentGraphRequestType agentRequestType = GetAgentGraphRequestType.Factory.newInstance();
//            agentRequestType.setInformationDetailLevel(DetailEnumType.FINE);
            agentRequestType.setAgentID(agentGraphId);
            agentGraphRequest.setGetAgentGraphRequest(agentRequestType);
            GetAgentGraphResponseDocument agentResponse = stub.getAgentGraph(agentGraphRequest);
            System.out.println(agentResponse.getGetAgentGraphResponse().getDocument());
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testEventEntityGraph() {
        try {
            System.out.println("\n\n Entity Graph without context \n\n");
            GetEntityGraphRequestDocument entityGraphRequest = GetEntityGraphRequestDocument.Factory.newInstance();
            GetEntityGraphRequestType entityRequestType = GetEntityGraphRequestType.Factory.newInstance();
            entityRequestType.setInformationDetailLevel(DetailEnumType.FINE);
            entityRequestType.setEntityURI("test:Event4");
            entityRequestType.setEntityType(EntityEnumType.COLLECTION);
            entityGraphRequest.setGetEntityGraphRequest(entityRequestType);
            GetEntityGraphResponseDocument entityResponse = stub.getEntityGraph(entityGraphRequest);
            System.out.println(entityResponse.getGetEntityGraphResponse().getDocument());
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


    @Test
    public void testActivityGraph() {
        try {
            System.out.println("\n\n Activity Graph without context \n\n");
            GetActivityGraphRequestDocument activityGraphRequest = GetActivityGraphRequestDocument.Factory.newInstance();
            GetActivityGraphRequestType actRequestType = GetActivityGraphRequestType.Factory.newInstance();
            actRequestType.setInformationDetailLevel(DetailEnumType.FINE);
            actRequestType.setActivityURI("workflow1_test6");
            activityGraphRequest.setGetActivityGraphRequest(actRequestType);
            GetActivityGraphResponseDocument actResponse = stub.getActivityGraph(activityGraphRequest);
            System.out.println(actResponse.getGetActivityGraphResponse().getDocument());
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testAgentGraph() {
        try {
            System.out.println("\n\n Agent Graph without context \n\n");
            GetAgentGraphRequestDocument agentGraphRequest = GetAgentGraphRequestDocument.Factory.newInstance();
            GetAgentGraphRequestType agentRequestType = GetAgentGraphRequestType.Factory.newInstance();
            agentRequestType.setAgentID("test:agent1");
            agentGraphRequest.setGetAgentGraphRequest(agentRequestType);
            GetAgentGraphResponseDocument agentResponse = stub.getAgentGraph(agentGraphRequest);
            System.out.println(agentResponse.getGetAgentGraphResponse().getDocument());
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testEntityRevisionRelation() throws Exception {
        Entity collection1 = new Entity();
        collection1.setId("collection:12345");
        collection1.setName("Collection_V1.0");
        Entity file1 = new Entity();
        file1.setId("file:12345");
        collection1.addChild(file1);

        Entity collection2 = new Entity();
        collection2.setId("collection:23456");
        collection2.setName("Collection_V2.0");
        Entity file2 = new Entity();
        file2.setId("file:23456");
        collection2.addChild(file2);

        new KomaduIngester(komaduURL).trackRevision(collection1, collection2);
    }

    @Test
    public void testEntityGraph() {
        try {
            System.out.println("\n\n Entity Graph without context \n\n");
            GetEntityGraphRequestDocument entityGraphRequest = GetEntityGraphRequestDocument.Factory.newInstance();
            GetEntityGraphRequestType entityRequestType = GetEntityGraphRequestType.Factory.newInstance();
            entityRequestType.setInformationDetailLevel(DetailEnumType.FINE);
            entityRequestType.setEntityURI("collection:12345");
            entityRequestType.setEntityType(EntityEnumType.COLLECTION);
            entityGraphRequest.setGetEntityGraphRequest(entityRequestType);
            GetEntityGraphResponseDocument entityResponse = stub.getEntityGraph(entityGraphRequest);
            System.out.println(entityResponse.getGetEntityGraphResponse().getDocument());
            pullParse(new ByteArrayInputStream(entityResponse.getGetEntityGraphResponse().getDocument().toString().getBytes(StandardCharsets.UTF_8)), "");
        } catch (RemoteException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static int pullParse(InputStream input, String process) throws IOException{

        XmlPullParserFactory factory;
        int count=0;
        try {
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput (input,null);// new StringReader (xml.replaceAll("&", "&amp;")) );
            int eventType = xpp.getEventType();

            int prov=0;
            int gen=0;
            int used=0;
            int doneGen = 0;
            int doneUsed = 0;


            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    if(xpp.getName().contains("wasDerivedFrom"))
                        prov=1;
                    if(xpp.getName().contains("generated"))
                        gen=1;
                    if(xpp.getName().contains("used"))
                        used=1;

                    if(prov==1&&gen ==1 && used ==0){
                        gen = 0;
                        doneGen = 1;
                    }

                    if(prov==1 && used ==1 && gen ==0){
                        used = 0;
                        doneUsed = 1;
                    }

                    if(prov==1&& doneGen==1&& doneUsed==1)
                        prov=doneGen=doneUsed=0;
                }
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return count;
    }

}
