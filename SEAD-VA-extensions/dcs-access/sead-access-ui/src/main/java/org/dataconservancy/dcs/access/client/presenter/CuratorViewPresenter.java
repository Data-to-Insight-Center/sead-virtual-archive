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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.client.ui.MessagePopupPanel;
import org.dataconservancy.dcs.access.client.ui.StatusPopupPanel;
import org.dataconservancy.dcs.access.client.ui.WfEventRefresherPanel;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.shared.ROMetadata;
import org.dataconservancy.dcs.access.shared.UserSession;

import java.util.List;


public class CuratorViewPresenter implements Presenter {

	public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
	public static int eventInvoked=0;
	public static String metadataSrc=null;
	
	Display display;
	ListBox ir;	//projectList in PublishDataView
	ListBox ROList;
	

	Button edit;
	
	public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
	public static final VivoSparqlServiceAsync vivoService = GWT.create(VivoSparqlService.class);
	public static final UserServiceAsync userService = GWT.create(UserService.class);
	public static final RegistryServiceAsync registryService = GWT.create(RegistryService.class);


	
	public interface Display {
		VerticalPanel getPublishContainer();
	}
	
	public CuratorViewPresenter(Display view){
		this.display = view;
	}

	final public static StatusPopupPanel statusPopupPanel = new StatusPopupPanel("Characterizing Files","wait",true);
	
	static int l = 0 ;
	int n = 0;

	
	Label dataset;

	
	@Override
	public void bind() {
		userService.checkSession(null, new AsyncCallback<UserSession>() {
			
			@Override
			public void onSuccess(final UserSession session) {
				if(session.getVivoId()!=null){
					vivoService.getAgentAffiliation(session.getVivoId(), new AsyncCallback<String>() {
						
						@Override
						public void onSuccess(final String repository) {
							registryService.getAllCOs(repository, null, SeadApp.roUrl, new AsyncCallback<List<ROMetadata>>() {
								
								@Override
								public void onSuccess(final List<ROMetadata> allCOs) {

								
									final Grid curatorData =  new Grid(allCOs.size()+1, 5);
									curatorData.setStyleName("curatorTable");
									curatorData.setWidth(Window.getClientWidth()/2+"px");
								
							        curatorData.setWidget(0, 0, Util.label("Research Object Name", "SubsectionHeader"));
							        curatorData.setWidget(0, 1, Util.label("Date Last Modified", "SubsectionHeader"));
							        curatorData.setWidget(0, 2, Util.label("Assignment", "SubsectionHeader"));
							        curatorData.setWidget(0, 3, Util.label("Edit", "SubsectionHeader"));
							        curatorData.setWidget(0, 4, Util.label("Publish", "SubsectionHeader"));
							        
							        curatorData.getRowFormatter().addStyleName(0, "curatoreHead");
							         
							        int i =1;
							        for(final ROMetadata ro: allCOs){
							        	if(ro.getIsObsolete()==1)
							        		continue;
							        	Label lbName = new Label();
							        	lbName.setText(ro.getName());
							        	Label dateLbl = new Label();
							        	dateLbl.setText(ro.getUpdatedDate());
							        	curatorData.setWidget(i, 0, lbName);
							        	curatorData.setWidget(i, 1, dateLbl);
							        	
							        	if(ro.getAgentId()==null)
							        	{
							        		final Button assign = new Button("Assign to me");
							        		curatorData.setWidget(i, 2, assign);
							        		final int index = i;
							        		assign.addClickHandler(new ClickHandler() {
												
												@Override
												public void onClick(ClickEvent event) {
													registryService.assignToAgent(ro.getIdentifier(), session.getRegistryId()
															, SeadApp.registryUrl, new AsyncCallback<Boolean>() {
														
														@Override
														public void onSuccess(Boolean result) {
															curatorData.setWidget(index, 2, Util.label("Assigned to you", "greenFont"));
														}
														
														@Override
														public void onFailure(Throwable caught) {
															assign.setText("Failed");
															assign.setEnabled(false);
														}
													});
												}
											});
							        	}
							        	else if(ro.getAgentId().trim().equalsIgnoreCase(session.getRegistryId().trim())){
						        			curatorData.setWidget(i, 2, Util.label("Assigned to you", "greenFont"));
							        	}
							        	else if(!ro.getAgentId().equalsIgnoreCase(session.getRegistryId())){
							        		curatorData.setWidget(i, 2, Util.label("Assigned to another user", "greenFont"));
							        	}
							        	
							        	edit = new Button("Edit");
							        	if(ro.getIsObsolete()==0){
								        	edit.addClickHandler(new ClickHandler() {
												
												@Override
												public void onClick(ClickEvent event) {
													History.newItem(SeadState.EDIT.toToken(ro.getIdentifier()));
												}
											});
							        	}
							        	else
							        		edit.setEnabled(false);
							        	curatorData.setWidget(i, 3, edit);
							        	
							        	final Button publish = new Button("Publish");
							        	publish.setStyleName("grayButton");
							        	if(ro.getIsObsolete()==1){
							        		publish.setText("Published");
							        		publish.removeStyleName("grayButton");
							        		publish.setStyleName("gwt-Button");
							        		publish.setEnabled(false);
							        	}
							        	if(ro.getIsObsolete()==0)
								        	publish.addClickHandler(new ClickHandler() {
												
												@Override
												public void onClick(ClickEvent event) {
													AsyncCallback<UserSession> cb =
											                new AsyncCallback<UserSession>() {
	
											                    public void onSuccess(final UserSession result) {
											                    	registryService.makeObsolete(ro.getIdentifier(), 
											                    			SeadApp.roUrl, new AsyncCallback<Boolean>() {
																		
																		@Override
																		public void onSuccess(Boolean updated) {
																			submitSip(ro.getIdentifier(), result.getEmail());
																			publish.setEnabled(false);
																			publish.removeStyleName("grayButton");
															        		publish.setStyleName("gwt-Button");
																			publish.setText("Published");
																			edit.setEnabled(false);
																		}
																		
																		@Override
																		public void onFailure(Throwable caught) {
																			new ErrorPopupPanel("Error:"+caught.getMessage()).show();
																		}
																	});
											                    }
	
											                    public void onFailure(Throwable error) {
											                    	new ErrorPopupPanel("Failed to login: "
											                                + error.getMessage()).show();
											                         
											                    }
											                };
	
													SeadApp.userService.checkSession(null,cb);
													
												}
											});
							        	curatorData.getRowFormatter().setStyleName(i, "CuratorRow");
							        	curatorData.setWidget(i, 4, publish);
							        	
							        	i++;
							        }
							        String institution = repository;
							        if(repository.contains("IU"))
							        	institution = "Indiana University";
							        display.getPublishContainer().add(Util.label("Curation Objects Review for " + institution, "CurationHeader"));
									display.getPublishContainer().add(curatorData);

								}
								
								@Override
								public void onFailure(Throwable caught) {
									new ErrorPopupPanel("Error:"+caught.getMessage()).show();
								}
							});
						}
						
						@Override
						public void onFailure(Throwable caught) {
							new ErrorPopupPanel("Failed to retrieve from VIVO:"+caught.getMessage()).show();
						}
					});	
				}
				else{
					new ErrorPopupPanel("User does not have a VIVO ID, hence cannot find the Affiliation/Curator queue.").show();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				new ErrorPopupPanel("Error:"+caught.getMessage());
			}
		});
			
	}
	@Override
	public void display(Panel mainContainer, Panel facetContent,Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		
		mainContainer.clear();
		facetContent.clear();
		bind();
		//nPanel = notificationPanel;
		mainContainer.setWidth("100%");
		mainContainer.setHeight("100%");
		mainContainer.setStyleName("Border");

		mainContainer.add(this.display.getPublishContainer());
		
	}


	private void submitSip(final String datasetId, final String submitterId){
		registryService.getRO(datasetId, SeadApp.roUrl,
				new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						new ErrorPopupPanel("Error:"+caught.getMessage());						
					}

					@Override
					public void onSuccess(String resultStr) {
						  String[] tempString = resultStr.split(";");
			              final String sipPath = tempString[tempString.length-1].split("<")[0].replace("_0.xml", "");
						mediciService.generateWfInstanceId(new AsyncCallback<String>() {
							
							@Override
							public void onSuccess(String wfInstanceId) {
								//Open a status panel that self queries the database for changes
								
								WfEventRefresherPanel eventRefresher = new WfEventRefresherPanel(submitterId, wfInstanceId);
								eventRefresher.show();
								mediciService.submitMultipleSips(SeadApp.deposit_endpoint + "sip",
										datasetId,
										null,
										sipPath,
										wfInstanceId,
										null,
										l, n, "", "", false, GWT.getModuleBaseURL(), SeadApp.tmpHome,false,
										new AsyncCallback<String>() {
											
											@Override
											public void onSuccess(final String result) {
												l=-1;
												final Label notify = Util.label("!", "Notification");
												notify.addClickHandler(new ClickHandler() {
													
													@Override
													public void onClick(ClickEvent event) {
														MessagePopupPanel popUpPanel = new MessagePopupPanel(result, "done", true);
														popUpPanel.show();
													}
												});
//												nPanel.add(notify);
											}
											
											@Override
											public void onFailure(Throwable caught) {
												
											}
										});
							
							}
							
							@Override
							public void onFailure(Throwable caught) {
								new ErrorPopupPanel("Error:"+caught.getMessage());
							}
						});
					}
			});
		}
}
