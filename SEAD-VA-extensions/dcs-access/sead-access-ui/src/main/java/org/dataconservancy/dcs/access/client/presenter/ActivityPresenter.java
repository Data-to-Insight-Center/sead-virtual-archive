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

package org.dataconservancy.dcs.access.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.model.*;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.shared.UserSession;

import java.util.*;


public class ActivityPresenter implements Presenter {

    public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);

    Display display;

    public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);

    public interface Display {
        VerticalPanel getActivityContainer();
    }

    public ActivityPresenter(Display view){
        this.display = view;
    }
    public static MediciInstance sparqlEndpoint = null;

    public static final RegistryServiceAsync registryService =
            GWT.create(RegistryService.class);

    public static final UserServiceAsync userService =
            GWT.create(UserService.class);

    @Override
    public void bind() {

        userService.checkSession(null, new AsyncCallback<UserSession>() {

            @Override
            public void onSuccess(UserSession result) {
                final String agentUrl =
                        result.getRegistryId();
                String queryUrl=
                        SeadApp.roUrl+"resource/agentGraph/"
                                +agentUrl;

                JsonpRequestBuilder rb = new JsonpRequestBuilder();
                rb.setTimeout(100000);
                rb.requestObject(queryUrl, new AsyncCallback<JsProvGraph>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                    }

                    @Override
                    public void onSuccess(JsProvGraph agentGraph) {

                        JsProvDocument document = agentGraph.getDocument();

                        try{

                            String agentName = document.getAgentName(agentUrl);
                            String agentId = document.getAgentId(agentUrl);

                            JsArray<JsAssociatedWith> activities = document.getActivities(agentId);

                            JsArray<JsGenerated> generatedBys = document.getSafeGeneratedBys();

                            JsArray<JsProvEntity> entities = document.getSafeEntities();

                            TreeMap<Date, List<HTML>> htmlMap = new TreeMap<Date,  List<HTML>>(Collections.reverseOrder());

                            for(int i =0; i<activities.length(); i++){

                                JsAssociatedWith activity = activities.get(i);
                                String eventType = activity.getEventType();
                                String activityString;
                                if(agentName==null||agentName.contains("null"))
                                    activityString = "You ";
                                else
                                    activityString = "You ("+agentName+")  ";

                                if(eventType==null)
                                    eventType = "an action";
                                if(eventType.equalsIgnoreCase("Curation-Workflow"))
                                    eventType = "Submission-Workflow";

                                activityString +=" performed "+eventType+" on  ";

                                JsGenerated gen = null;
                                String activityId = activity.getActivityId();
                                if(activityId==null){
//									  Window.alert("Continuing 1");
                                    continue;
                                }
                                for(int k =0; k< generatedBys.length();k++){
                                    if(generatedBys.get(k)==null||generatedBys.get(k).getActivityId()==null)
                                        continue;
                                    if(generatedBys.get(k).getActivityId().equalsIgnoreCase(activityId))
                                    {
                                        gen = generatedBys.get(k);
                                        break;
                                    }
                                }
                                if(gen == null){
//									  Window.alert("Continuing 2");
                                    continue;
                                }

                                String entityId = gen.getEntity();

                                JsProvEntity entity = null;
//								  Window.alert(entityId);
                                for(int k =0; k< entities.length();k++){
                                    if(entities.get(k).getEntityId().equalsIgnoreCase(entityId))
                                    {
                                        entity = entities.get(k);
//										 Window.alert("matched");
                                        break;
                                    }
                                }
                                if(entity == null){
//									  Window.alert("Continuing 3");
                                    continue;
                                }

                                if(gen.getTimeString()==null||gen.getTimeString().length()<2){
                                    continue;
                                }

                                String timeString = gen.getTimeString().substring(0, gen.getTimeString().length()-2);
                                String entityLink =  SeadState.ENTITY.toToken(entity.getEntityUrl());
                                if(activityString.contains("Curation")||activityString.contains("Submission"))
                                    entityLink =  SeadState.CURATIONOBJECT.toToken(entity.getEntityUrl());

                                HTML html = new HTML("<font color=\"gray\">"
                                        +timeString
                                        +"</font>" +
                                        "&nbsp;&nbsp;"+
                                        activityString + "<a href=\""+ GWT.getModuleName().replace("sead_access",
//												  "Sead_access.html?gwt.codesvr=127.0.0.1:9997"
                                        ""
                                ) +
                                        "#"+
                                        entityLink+
                                        "\"><font color=\"steelblue\">" +
                                        entity.getEntityTitle() +
                                        "</font></a>"
                                        /*+"&nbsp;&nbsp;&nbsp;&nbsp;<a href=\""+ GWT.getModuleName().replace("sead_access",
          //												  "Sead_access.html?gwt.codesvr=127.0.0.1:9997"
                                                            ""
                                                            ) +
                                                                        "#"+
                                                                        SeadState.PROV.toToken(entity.getEntityUrl())+
                                                                        "\"><font color=\"steelblue\">" +
                                                            "(View RO provenance) " +
                                                                        "</font></a>"*/, true);

                                html.setStyleName("ActivityEntry");

                                DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
                                List<HTML> htmlList;
                                if(htmlMap.containsKey(format.parse(timeString)))
                                    htmlList = htmlMap.get(format.parse(timeString));
                                else
                                    htmlList = new ArrayList<HTML>();

                                htmlList.add(html);
                                htmlMap.put(format.parse(timeString), htmlList);
                            }

                            display.getActivityContainer().clear();

                            //sort the htmlArray by Date

                            if(htmlMap.values().size()==0||htmlMap.size()==0){
                                HTML html = new HTML("<font color=\"gray\"><center>"
                                        + "No Activity Yet"
                                        + "</center></font>" , true);
                                html.setStyleName("ActivityEntry");
                                display.getActivityContainer().add(html);
                            }
                            else
                                for(List<HTML> htmlList:htmlMap.values())
                                    for(HTML html: htmlList)
                                        display.getActivityContainer().add(html);
                        }
                        catch(Exception e){
                            display.getActivityContainer().clear();
                            HTML html = new HTML("<font color=\"gray\"><center>"
                                    + "No Activity Yet"
                                    + "</center></font>" , true);
                            html.setStyleName("ActivityEntry");
                            display.getActivityContainer().add(html);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                new ErrorPopupPanel("Error:"+caught.getMessage()).show();
            }
        });
    }

    @Override
    public void display(Panel mainContainer, Panel facetContent,Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {

        mainContainer.clear();
        facetContent.clear();
        bind();
        mainContainer.addStyleName("Border");

        mainContainer.add(this.display.getActivityContainer());
    }


}
