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
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Window;
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

import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;


public class EntityProvPresenter implements Presenter {

	public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);

	Display display;

	public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
	
	public interface Display {
		VerticalPanel getActivityContainer();
		String getEntityId();
	}
	
	public EntityProvPresenter(Display view){
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
				final String entityUrl = 
						display.getEntityId();
//						"http://seadva-test.d2i.indiana.edu:5667/sead-wf/entity/192410";
						//"http%3A%2F%2Fseadva-test.d2i.indiana.edu%3A5667%2Fsead-wf%2Fentity%2F189712";
//						"agent:f8477e5d-922f-41fb-9496-ba39ff218264";
//						"agent:7dd76828-d615-4c76-9f95-427a1dcdf5f4";
//						"agent:e4b2ea67-e775-498d-af9e-fcb1a0235f01";
//						"agent:4f1e3f38-6fc4-47f8-8e15-4358f80b0986v";
//				 "agent:fb9c6de1-322b-467b-8a88-f6475c6fa0f1";
				String queryUrl=
						  SeadApp.roUrl+"resource/entityGraph/"
						  +entityUrl.replace(":", "%3A").replace("/", "%2F");
//				queryUrl = "http://seadva-test.d2i.indiana.edu/ro/resource/agentGraph/agent:fb9c6de1-322b-467b-8a88-f6475c6fa0f1";
						  						  
				  JsonpRequestBuilder rb = new JsonpRequestBuilder();
			      rb.setTimeout(100000);
			      rb.requestObject(queryUrl, new AsyncCallback<JsProvGraph>() {

						@Override
						public void onFailure(Throwable caught) {
							new ErrorPopupPanel("Error:"+caught.getMessage()).show();
						}

						@Override
						public void onSuccess(JsProvGraph entityGraph) {

							//Get all activities related to this entity
							//Get all entities related to this entity
							//Get all activities related to these entities as well
							  JsProvDocument document = entityGraph.getDocument();

							  JsProvEntity entity = document.getEntity(
									  //"http://seadva-test.d2i.indiana.edu:5667/sead-wf/entity/189712"
									  entityUrl
									  );
							
							//  Window.alert(entityId);
							
							  UtilPopulate populate = new UtilPopulate();
							  populate.populateEntities(entity.getEntityId(), document);
							  JsArrayString entitiesStr = populate.getEArrayString();
							
							  
							  
							  TreeMap<Date,HTML> htmlMap = new TreeMap<Date,HTML>(Collections.reverseOrder());	
							//  JsArray<JsProvEntity> entities = document.getSafeEntities();
							  String relations = "";
							for(int j =0;j<entitiesStr.length();j++){
								 String entityLink = "";
							  JsArray<JsGenerated> generatedBys = document.getGeneratedBysByEntity(entitiesStr.get(j));
							//  Window.alert(generatedBys.length()+" generatedBys");
							  JsArray<JsAssociatedWith> activities = document.getSafeActivities();
							//  Window.alert(activities.length()+" activities");
							 

							 				
							  
							  for(int i =0; i<generatedBys.length(); i++){
								
								  JsGenerated gen = generatedBys.get(i);
								  String activityId = gen.getActivityId();
								
								  JsAssociatedWith activity = null;
								//  Window.alert("activityId = " +activityId);
								  
								  for(int k =0; k< activities.length();k++){			
									  if(activities.get(k)==null||activities.get(k).getActivityId()==null)
										  continue;
									  if(activities.get(k).getActivityId().equalsIgnoreCase(activityId))
									  {
										  activity = activities.get(k);
									  	 break;
									  }
								  }
								  
								  if(activity == null){
									  Window.alert("Continuing 3");
									  continue;
								  }
								  
							
								  
								  if(gen.getTimeString()==null||gen.getTimeString().length()<2){
									  Window.alert("Continuing 4");
									  continue;
								  }
								  
								  String timeString = gen.getTimeString().substring(0, gen.getTimeString().length()-2);
								  
								  String activityString = activity.getEventType() ;
								  
								  if(activityString.contains("Curation-Workflow"))
									  activityString = "Submission-Workflow";
								  entity = document.getEntityById(entitiesStr.get(j));
								 
								  entityLink =  "<a href=\""+ GWT.getModuleName().replace("sead_access",
//										  "Sead_access.html?gwt.codesvr=127.0.0.1:9997"
										  ""
										  ) +
								  					"#"+
								  					SeadState.CURATIONOBJECT.toToken(entity.getEntityUrl())+
								  					"\"><font color=\"steelblue\">" + 
								  					entity.getEntityTitle() +
								  					"</font></a>";
								  
								  HTML html = new HTML("<font color=\"gray\">"
								   +timeString
								  +"</font>" +
										  "&nbsp;&nbsp;"+
										  activityString + "&nbsp; was performed on &nbsp;"+entityLink , true);

								 html.setStyleName("ActivityEntry");
								
								  
								 DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
								 htmlMap.put(format.parse(timeString), html);
							  } 
							  if(j>0)
								  relations += ", ";
							  relations +=  entityLink+
									  "&nbsp;&nbsp;";
							}
							  
							  //sort the htmlArray by Date
							  
							  if(htmlMap.values().size()==0||htmlMap.size()==0){
								  HTML html = new HTML("<font color=\"gray\"><center>"
										  + "No Activity Yet"
										  + "</center></font>" , true);
										 html.setStyleName("ActivityEntry");
										 display.getActivityContainer().add(html);
							  }
							  else					  
								  for(HTML html:htmlMap.values())
									  display.getActivityContainer().add(html);
							  
							  
							  HTML html = new HTML("<font color=\"gray\">"
									  + "Relations"
									  + "</font>" , true);
									 html.setStyleName("ActivityEntry");
							 display.getActivityContainer().add(html);
							 html = new HTML(relations + "&nbsp;&nbsp; represent lineage stages of a single RO."
									  , true);
							 display.getActivityContainer().add(html);
							  
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
