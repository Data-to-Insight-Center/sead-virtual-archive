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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.model.JsAgentGraph;
import org.dataconservancy.dcs.access.client.model.JsGenerated;
import org.dataconservancy.dcs.access.client.model.JsProvDocument;
import org.dataconservancy.dcs.access.client.model.JsProvEntity;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.client.ui.StatusPopupPanel;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.shared.UserSession;

import java.util.List;


public class ActivityPresenter implements Presenter {

	public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
	public static int eventInvoked=0;
	public static String metadataSrc=null;
	
	Display display;
	ListBox ir;	//projectList in PublishDataView
	ListBox ROList;
	
	public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
	
	public interface Display {
		VerticalPanel getActivityContainer();
	}
	
	public ActivityPresenter(Display view){
		this.display = view;
	}
	public static MediciInstance sparqlEndpoint = null;
	final public static StatusPopupPanel statusPopupPanel = new StatusPopupPanel("Characterizing Files","wait",true);
	
	static int l = -1 ;
	int n = 0;
	int totalNumOfFiles = 0;
	int totalNumOfEntities = 0;
	int finishedFiles = 0;
	String previousEvent = null; 
	String submitterId = null;
	int flagHyperlink;
	int first=0;
	int last;
	
	Label dataset;
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
			      rb.requestObject(queryUrl, new AsyncCallback<JsAgentGraph>() {

						@Override
						public void onFailure(Throwable caught) {
							new ErrorPopupPanel("Error:"+caught.getMessage()).show();
						}

						@Override
						public void onSuccess(JsAgentGraph agentGraph) {

							  JsProvDocument document = agentGraph.getDocument();

							  String agentName = document.getAgentName(agentUrl);
							  
							  List<String> activityIds = document.getActivityIds();
							  
							  for(String activityId : activityIds){
								
								  String eventType = document.getEventType(activityId);
								  String activityString;
								  if(agentName==null)
									  activityString = "You ";
								  else
									  activityString = "You ("+agentName+")  ";
								  Label agentLabel = new Label(activityString);
								  
								  if(eventType==null)
									  eventType = "an action";
								  if(eventType.equalsIgnoreCase("Curation-Workflow"))
									  eventType = "Submission-Workflow";
								  								  
								  activityString +=" performed "+eventType+" on  ";
								  Label activityLabel = new Label();//Map activity to verb
								  
								  JsGenerated gen = document.getEntityId(activityId);
								  String entityId = gen.getEntity();
								  final JsProvEntity entity = document.getEntity(entityId);
								  Label entityLbl = Util.label(entity.getEntityTitle(), "Hyperlink");
								  
								  HTML html = new HTML("<font color=\"gray\">"+gen.getTimeString().substring(0, gen.getTimeString().length()-2)+"</font>" +
										  "&nbsp;&nbsp;"+
										  activityString + "<a href=\""+ GWT.getModuleName().replace("sead_access",
//												  "Sead_access.html?gwt.codesvr=127.0.0.1:9997"
												  ""
												  ) +
										  					"#"+ SeadState.ENTITY.toToken(entity.getEntityUrl())
										  					+"\"><font color=\"steelblue\">" + entity.getEntityTitle() + "</font></a>" , true);
								  html.setStyleName("ActivityEntry");
								  entityLbl.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										History.newItem(SeadState.ENTITY.toToken(entity.getEntityUrl()));
									}
								  });
								  
								  HorizontalPanel activityPanel = new HorizontalPanel();
								  activityPanel.add(agentLabel);
								  activityPanel.add(activityLabel);
								  activityPanel.add(entityLbl);
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
		mainContainer.setWidth("100%");
		mainContainer.setHeight("100%");
		mainContainer.setStyleName("Border");

		mainContainer.add(this.display.getActivityContainer());
	}


}
