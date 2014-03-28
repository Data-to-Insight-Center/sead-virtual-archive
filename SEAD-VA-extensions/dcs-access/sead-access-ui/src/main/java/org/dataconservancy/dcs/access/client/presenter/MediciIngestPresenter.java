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

package org.dataconservancy.dcs.access.client.presenter;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.DepositService;
import org.dataconservancy.dcs.access.client.api.DepositServiceAsync;
import org.dataconservancy.dcs.access.client.api.LogService;
import org.dataconservancy.dcs.access.client.api.LogServiceAsync;
import org.dataconservancy.dcs.access.client.api.MediciService;
import org.dataconservancy.dcs.access.client.api.MediciServiceAsync;
import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.client.event.CollectionClickEvent;
import org.dataconservancy.dcs.access.client.event.CollectionPassiveSelectEvent;
import org.dataconservancy.dcs.access.client.event.CollectionSelectEvent;
import org.dataconservancy.dcs.access.client.event.WorkflowStatusEvent;
import org.dataconservancy.dcs.access.client.model.CollectionNode;
import org.dataconservancy.dcs.access.client.model.CollectionTreeViewModel;
import org.dataconservancy.dcs.access.client.model.DatasetRelation;
import org.dataconservancy.dcs.access.client.model.FileNode;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.client.model.CollectionNode.SubType;
import org.dataconservancy.dcs.access.client.resources.TableResources;
import org.dataconservancy.dcs.access.client.resources.TreeResources;
import org.dataconservancy.dcs.access.client.ui.MessagePopupPanel;
import org.dataconservancy.dcs.access.client.ui.StatusPopupPanel;
import org.dataconservancy.dcs.access.client.ui.WfEventRefresherPanel;
import org.dataconservancy.dcs.access.shared.CheckPointDetail;
import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.shared.Query;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.UserSession;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.TreeViewModel;


public class MediciIngestPresenter  implements Presenter {

	public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
	public static int eventInvoked=0;
	public static String metadataSrc=null;
	
	Panel content;
	Panel mainContentPanel;
	Button getPub;
	Button ingestButton;
	Panel leftPanel;
	Panel rightPanel;
	Panel coverRightPanel;
	Panel nPanel;
	ListBox ir ;
	CheckBox cloudCopy;
	public static CheckBox mdCb;
	Date latestDate = new Date();

	
	Map<String,FileTable> existingFileSets;
	Map<String,List<FileNode>> previousSelectedFiles;
	
	public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
	public static final DepositServiceAsync depositService = GWT.create(DepositService.class);
	public static final LogServiceAsync logService = GWT.create(LogService.class);
	
	
	Label dataset;
	int flagHyperlink;
	int first=0;
	int last;
	Timer statusTimer=null;
	int i =1;
	
	public interface Display {

		Panel getContent();
		Panel getMainContent();
		Button getPub();
		Panel getLeftPanel();
		Panel getRightPanel();
		Panel getCoverRightPanel();
		Button getIngestButton();
		ListBox getIr() ;
		CheckBox getCloudCopy();
		CheckBox getMetadataCheckBox();
		Label getDatasetLbl();
		Label getFileLbl();
	}
	
	Display display;
	
	public MediciIngestPresenter(Display view)
	{
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

	@Override
	public void bind() {
		mainContentPanel = this.display.getMainContent();
		content = this.display.getContent();
		getPub = this.display.getPub();
		leftPanel = this.display.getLeftPanel();
		rightPanel = this.display.getRightPanel();
		coverRightPanel = this.display.getCoverRightPanel();
		ingestButton = this.display.getIngestButton();
		ir = this.display.getIr();
		cloudCopy = this.display.getCloudCopy();
		mdCb = this.display.getMetadataCheckBox();
		
//		registerSubmitSipEvent();
//		registerWorkflowEvent();
		
		SeadApp.userService.checkSession(null, new AsyncCallback<UserSession>() {
			
			@Override
			public void onSuccess(final UserSession result) {
				mediciService.getAcrInstances(new AsyncCallback<List<MediciInstance>>() {

					@Override
					public void onFailure(Throwable caught) {
						 Window.alert(caught.getMessage());
						
					}

					@Override
					public void onSuccess(List<MediciInstance> instances) {
						submitterId = result.getEmail();
						if(!result.getRole().equals(Role.ROLE_NONSEADUSER))
						{
							for(MediciInstance instance:instances){
								ir.addItem(instance.getTitle());
							}
							ir.setVisibleItemCount(instances.size());
						}
						else{
							for(MediciInstance instance:instances){
								if(instance.getType().equalsIgnoreCase("demo"))
								ir.addItem(instance.getTitle());
							}
							ir.setVisibleItemCount(1);
						}
						
					     ir.setItemSelected(0, true);
					     getPub.setEnabled(true);
					     addGetPubHandler();
						
					}
				
				
				
				});
				}
				@Override
				public void onFailure(Throwable caught) {
					 Window.alert(caught.getMessage());
				}
		});
	}


	
	void registerWorkflowEvent(){//This event simply changes what is displayed on the popup panel
		WorkflowStatusEvent.register(EVENT_BUS, new WorkflowStatusEvent.Handler() {
			   public void onMessageReceived(final WorkflowStatusEvent event) {
				   
				   if(event.getPercent()==-10)
					   statusPopupPanel.setValue(Constants.eventMessages.get(event.getMessage()), 
							   event.getDetail(),
							   event.getSymbol(), 100);
				   else if(event.getPercent()>-1){
					   int number = 0;
					   if(Constants.multiEventCheck.get(event.getMessage())==1)
						   number = totalNumOfFiles;
					   else
						   number = totalNumOfEntities; 
					   statusPopupPanel.setValue(Constants.eventMessages.get(event.getMessage()), 
							   //event.getDetail(),
							   Constants.eventMessages.get(event.getMessage())+ " was completed for "+number+" files and/or collections.",
							   event.getSymbol(), event.getPercent());
				   }
				   else
					   statusPopupPanel.setValue(Constants.eventMessages.get(event.getMessage()), event.getDetail(), event.getSymbol());
			   }		
			});
	}
	
	void addGetPubHandler(){
		getPub.addClickHandler(new ClickHandler() {
			
			
			@Override
			public void onClick(ClickEvent event) {
				final StatusPopupPanel mediciWait = new StatusPopupPanel("Retrieving","wait",false);
				mediciWait.show();
				existingFileSets = new HashMap<String, FileTable>();
				previousSelectedFiles = new HashMap<String,List<FileNode>>();
				int selected = ir.getSelectedIndex();
				
	        	final String instance = ir.getValue(selected);
		        
				mediciService.getAcrInstances(new AsyncCallback<List<MediciInstance>>() {
					
					@Override
					public void onSuccess(List<MediciInstance> result) {

						for(MediciInstance ins:result)
							if(ins.getTitle().equalsIgnoreCase(instance))
								sparqlEndpoint = ins;
				        
				        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, SeadApp.ACRCOMMON+"?instance="+
				        		URL.encodeQueryString(
				        		sparqlEndpoint.getTitle()
				        		)+"&"+
				        		"query="+
				        		URL.encodeQueryString(
				        		Query.PROPOSED_FOR_PUBLICATION.getTitle()
				        		)
				        );

				        rb.setHeader("Content-type", "application/x-www-form-urlencoded");

				        try {
							Request response = rb.sendRequest(null, new RequestCallback() {
								@Override
								public void onError(Request request, Throwable exception) {
									Window.alert("Failed");
								}
								
								@Override
								public void onResponseReceived(Request request,
										Response response) {
									String json = response.getText();
									mediciService.parseJson(json, new AsyncCallback<Map<String,String>>() {
										
										@Override
										public void onSuccess(Map<String,String> result) {
											
											leftPanel.clear();
										  	JsonpRequestBuilder rb = new JsonpRequestBuilder();
										    rb.setTimeout(100000);
										        
											mediciWait.hide();
											last =
													result.size()-1;
											final FlexTable grid = new FlexTable(
													);
											grid.setWidth("100%");
											grid.setHeight("100%");
											
											
											final Iterator it = result.entrySet().iterator();
										    while (it.hasNext()) {
										        final Map.Entry pair = (Map.Entry)it.next();
										        final String dName =(String) pair.getValue();
										       
										        flagHyperlink =0;
										        String tagRetrieveUrl =
										        		SeadApp.accessurl+SeadApp.queryPath+"?q=resourceValue:"+
										        		"("+
										        		URL.encodeQueryString(((String)pair.getKey()).replace(":", "\\:"))+
										        		")";
										        rb.requestObject(tagRetrieveUrl, new AsyncCallback<JsSearchResult>() {

										            public void onFailure(Throwable caught) {
										                Util.reportInternalError("Matching collection in VA failed", caught);
										            
										            }
										           
										            public void onSuccess(JsSearchResult result) {
//									            	if(result.matches().length()==0||sparqlEndpoint.equals("http://sead.ncsa.illinois.edu/acr/resteasy/sparql"))
//									            	{
										            		dataset = Util.label(dName.substring(dName.lastIndexOf("/")+1),"Hyperlink");
										            		flagHyperlink =1;					            		
//									            	}				            	
//									            	else
//									                	flagHyperlink =0;
										           
										        
										       if(flagHyperlink==1){
										        dataset.addClickHandler(new ClickHandler() {
													@Override
													public void onClick(ClickEvent event) {
														mediciService.restartIngest((String)pair.getKey(), SeadApp.tmpHome, new AsyncCallback<CheckPointDetail>(){

															@Override
															public void onFailure(
																	Throwable caught) {
																Window.alert("Error in estimating reingest scenario. \n" + caught.getMessage());
															}

															@Override
															public void onSuccess(
																	final CheckPointDetail result) {
																if(!result.isCheckPointed()){
																	final StatusPopupPanel collectionWait = new StatusPopupPanel("Querying for BagIt Bag","bag",false);
																	collectionWait.show();
																	
															            	final MultiSelectionModel<CollectionNode> selectionModel = new MultiSelectionModel<CollectionNode>();
																			
																			mediciService.getBag( 
																					(String) pair.getKey(), sparqlEndpoint,
																					SeadApp.bagIturl, SeadApp.tmpHome,
																					 new AsyncCallback<String>() {
																						@Override
																						public void onSuccess(final String bagPath) {
																							collectionWait.setValue("Converting to SEAD SIP", "wait");
																						
																							final Timer getSIPTimer = new Timer() {
																						
																							@Override
																							public void run() {
																								String tempguid = null;
																						        if(((String) pair.getKey()).contains("/"))
																						        	tempguid = ((String) pair.getKey()).split("/")
																						            [((String) pair.getKey()).split("/").length-1];
																						        else
																						        	tempguid = ((String) pair.getKey()).split(":")
																						            [((String) pair.getKey()).split(":").length-1];
																						        final String guid = tempguid;
																								mediciService.getSipFromBag(
																										bagPath,
																									SeadApp.tmpHome+guid+"_sip.xml",
																									SeadApp.bagIturl,
																									new AsyncCallback<String>() {
																								
																								@Override
																								public void onSuccess(String result) {

																									mediciService.getFileNos(new AsyncCallback<Integer>(){
																										@Override
																										public void onFailure(
																												Throwable caught) {
																											Window.alert("Failed:"+caught.getMessage());
																											
																										}

																										@Override
																										public void onSuccess(Integer size) {
																											if(size>Constants.MAX){
																												Window.alert("This collection has more than "+Constants.MAX+" files.\n"+
																															 "Hence preview is not possible. But you can start the ingest");
																												if(collectionWait.isShowing())
																													collectionWait.hide();
																												getPub.setEnabled(false);
																												cloudCopy.setEnabled(true);
																												mdCb.setEnabled(true);
																												ingestButton.setEnabled(true);
																												ir.setEnabled(false);
																												ir.setStyleName("greyFont");
																										        getPub.setStyleName("greyFont");
																												cloudCopy.setStyleName("greenFont");
																												mdCb.setStyleName("greenFont");
																												ingestButton.setStyleName("greenFont");
																												
																										       
																												ingestButton.addClickHandler(new ClickHandler() {
																													
																													@Override
																													public void onClick(ClickEvent event) {
																													    ingestButton.setEnabled(false);
																												        cloudCopy.setEnabled(false);
																												        ir.setEnabled(false);
																												        getPub.setEnabled(true);
																														String rootMediciId= (String) pair.getKey();
																														
																														
																														AsyncCallback<Void> vaModelCb = new AsyncCallback<Void>() {
																																@Override
																																public void onSuccess(Void result) {
																																	mediciService.addMetadata(metadataSrc,SeadApp.tmpHome+guid+"_sip", new AsyncCallback<Void>() {
																																		
																																		@Override
																																		public void onSuccess(Void result) {
																																			
																																			
																																			mediciService.splitSip(
																																					SeadApp.tmpHome+guid+"_sip",
																																					new AsyncCallback<Integer>() {
																																				
																																				@Override
																																				public void onSuccess(Integer result) {
																																					n=result;
																																					l++;
																																					if(l<=n){
																																						mediciService.generateWfInstanceId(new AsyncCallback<String>() {
																																							
																																							@Override
																																							public void onSuccess(final String wfInstanceId) {
																																								UserServiceAsync user =
																																							            GWT.create(UserService.class);
																																								user.checkSession(null,new AsyncCallback<UserSession>() {

																																									@Override
																																									public void onFailure(
																																											Throwable caught) {
																																										// TODO Auto-generated method stub
																																										
																																									}

																																									@Override
																																									public void onSuccess(
																																											UserSession result) {

																																										mediciService.submitMultipleSips(SeadApp.deposit_endpoint + "sip",
																																												(String) pair.getKey(),
																																												sparqlEndpoint,
																																												SeadApp.tmpHome+guid+"_sip", 
																																												wfInstanceId,
																																												null,
																																												l, n, "", "", false, GWT.getModuleBaseURL(),SeadApp.tmpHome,
																																												new AsyncCallback<String>() {
																																													
																																													@Override
																																													public void onSuccess(final String result) {
																																														l=-1;
																																														final Label notify = Util.label("!", "Notification");
																																														notify.addClickHandler(new ClickHandler() {
																																															
																																															@Override
																																															public void onClick(ClickEvent event) {
																																																StatusPopupPanel mediciWait = new StatusPopupPanel("Retrieving","done",false);
																																																MessagePopupPanel popUpPanel = new MessagePopupPanel(result, "done", true);
																																																popUpPanel.show();
																																																nPanel.remove(notify);
																																															}
																																														});
																																														nPanel.add(notify);
																																													}
																																													
																																													@Override
																																													public void onFailure(Throwable caught) {
																																														Window.alert("Workflow failed.");
																																													}
																																												});
																																									
																																									}
																																									
																																								});
																																							}

																																							@Override
																																							public void onFailure(
																																									Throwable caught) {
																																								// TODO Auto-generated method stub
																																								
																																							}
																																						});
																																					
																																					}
																																					else{
																																						Window.alert("This dataset is already ingested. Please clear checkpointing if you want to rerun the workflow");
																																					}
																																				}
																																				
																																				@Override
																																				public void onFailure(Throwable caught) {
																																					// TODO Auto-generated method stub
																																					
																																				}
																																			});
																																		}
																																		
																																		@Override
																																		public void onFailure(Throwable caught) {
																																			// TODO Auto-generated method stub
																																			
																																		}
																																	});
																																}
																															
																															@Override
																															public void onFailure(Throwable caught) {
																																// TODO Auto-generated method stub
																																
																															}
																														};
																														mediciService.toVAmodel(rootMediciId,rootMediciId,sparqlEndpoint, SeadApp.tmpHome, vaModelCb );

																													}
																												});
																												
																												coverRightPanel.setVisible(true);
																											}
																											else{
																												mediciService.getRelations(new AsyncCallback<DatasetRelation>(){


																													
																													@Override
																													public void onFailure(
																															Throwable caught) {
																														Window.alert("Failed:"+caught.getMessage());
																														
																													}

																													@Override
																													public void onSuccess(
																															final DatasetRelation relations) {

																														
																													
																														display.getDatasetLbl().setText("Browse Collection and sub-Collections");
																														display.getFileLbl().setText("Browse Files");
																															TreeViewModel model =
																														    		new CollectionTreeViewModel(selectionModel, relations, (String) pair.getKey());
																															CellTree.Resources resource = GWT.create(TreeResources.class);
																														    CellTree tree = new CellTree(model, null,resource);
																														    //collection select
																															CollectionSelectEvent.register(EVENT_BUS, new CollectionSelectEvent.Handler() {
																																   public void onMessageReceived(final CollectionSelectEvent event) {
																																				
																																		rightPanel.clear();
																																		rightPanel.add(getFiles(relations.getDuAttrMap(), relations.getFileAttrMap(), event.getCollection().getId(),event.getValue()));
																																   }
																															});
																															
																															//collection click
																															CollectionClickEvent.register(EVENT_BUS, new CollectionClickEvent.Handler() {
																																   public void onMessageReceived(final CollectionClickEvent event) {
																																	  
																																	   if(existingFileSets.containsKey(event.getCollection().getId())){
																																		    rightPanel.clear();
																																			rightPanel.add(existingFileSets.get(event.getCollection().getId()).cellTable);
																																		}
																																		else{	
																																		  
																																			rightPanel.clear();
																																			rightPanel.add(getFiles(relations.getDuAttrMap(), relations.getFileAttrMap(), event.getCollection().getId(),false));														
																																	   }
																																   }
																																});
																															//collection passive click
																															CollectionPassiveSelectEvent.register(EVENT_BUS, new CollectionPassiveSelectEvent.Handler() {
																																   public void onMessageReceived(final CollectionPassiveSelectEvent event) {
																																	  
																																	   CellTable files ;
																																	   if(existingFileSets.containsKey(event.getCollection().getId())){
																																		   files = existingFileSets.get(event.getCollection().getId()).cellTable;
																																		   for(String file:relations.getDuAttrMap().get(event.getCollection().getId()).getSub().get(SubType.File)){
																																			   files.getSelectionModel().setSelected((FileNode)relations.getFileAttrMap().get(file),event.getValue());
																																		   }
																																	   }
																																	   else{
																																		   files = (CellTable) getFiles(relations.getDuAttrMap(), relations.getFileAttrMap(), event.getCollection().getId(),event.getValue());
																																	   }
																																	  
																																   }
																																});

																															collectionWait.hide();
																															leftPanel.clear();
																															leftPanel.add(tree);
																														
																														if(collectionWait.isShowing())
																															collectionWait.hide();
																														getPub.setEnabled(false);
																														cloudCopy.setEnabled(true);
																														mdCb.setEnabled(true);
																														ingestButton.setEnabled(true);
																														ir.setEnabled(false);
																														ir.setStyleName("greyFont");
																												        getPub.setStyleName("greyFont");
																														cloudCopy.setStyleName("greenFont");
																														mdCb.setStyleName("greenFont");
																														ingestButton.setStyleName("greenFont");
																														
																														ingestButton.addClickHandler(new ClickHandler() {
																															
																															@Override
																															public void onClick(ClickEvent event) {
																															    ingestButton.setEnabled(false);
																														        cloudCopy.setEnabled(false);
																														        ir.setEnabled(false);
																														        getPub.setEnabled(true);
																																String rootMediciId= (String) pair.getKey();
																																CollectionNode root = relations.getDuAttrMap().get(rootMediciId);
																															
																																
																																AsyncCallback<Void> vaModelCb = new AsyncCallback<Void>() {
																																		@Override
																																		public void onSuccess(Void result) {
																																			mediciService.addMetadata(metadataSrc, SeadApp.tmpHome+guid+"_sip",new AsyncCallback<Void>() {
																																				
																																				@Override
																																				public void onSuccess(Void result) {
																																					String tempguid = null;
																																			        if(((String) pair.getKey()).contains("/"))
																																			        	tempguid = ((String) pair.getKey()).split("/")
																																			            [((String) pair.getKey()).split("/").length-1];
																																			        else
																																			        	tempguid = ((String) pair.getKey()).split(":")
																																			            [((String) pair.getKey()).split(":").length-1];
																																			        final String guid = tempguid;
																																					
																																					mediciService.splitSip(
																																							SeadApp.tmpHome+guid+"_sip",
																																							new AsyncCallback<Integer>() {
																																						
																																						@Override
																																						public void onSuccess(Integer result) {
																																							n=result;
																																							l++;
																																							
//																																							Window.alert("Starting ingest of dataset");//. We already have the cached SIP for this dataset.");
																																							mediciService.generateWfInstanceId(new AsyncCallback<String>() {
																																								
																																								@Override
																																								public void onSuccess(String wfInstanceId) {
																																									//Open a status panel that self queries the database for changes
																																									WfEventRefresherPanel eventRefresher = new WfEventRefresherPanel(submitterId, wfInstanceId);
																																									eventRefresher.show();
																																									mediciService.submitMultipleSips(SeadApp.deposit_endpoint + "sip",
																																											(String)pair.getKey(),
																																											sparqlEndpoint,
																																											SeadApp.tmpHome+guid+"_sip",
																																											wfInstanceId,
																																											null,
																																											l, n, "", "", false, GWT.getModuleBaseURL(),SeadApp.tmpHome,
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
																																															nPanel.remove(notify);
																																														}
																																													});
//																																													nPanel.add(notify);
																																												}
																																												
																																												@Override
																																												public void onFailure(Throwable caught) {
																																													
																																												}
																																											});
																																								
																																								}
																																								
																																								@Override
																																								public void onFailure(Throwable caught) {
																																									
																																								}
																																							});
																																							
																																						}
																																						
																																						@Override
																																						public void onFailure(Throwable caught) {
																																							Window.alert("Failed. \n"+caught.getMessage());
																																						}
																																					});
																																				}
																																				
																																				@Override
																																				public void onFailure(Throwable caught) {
																																					Window.alert("Failed. \n"+caught.getMessage());
																																				}
																																			});
																																		}
																																	
																																	@Override
																																	public void onFailure(Throwable caught) {
																																		Window.alert("Failed. \n"+caught.getMessage());
																																	}
																																};
																																mediciService.toVAmodel(rootMediciId,rootMediciId, sparqlEndpoint, SeadApp.tmpHome, vaModelCb );

																															}
																														});
																														coverRightPanel.setVisible(true);
																													}
																												});
																											}
																										}
																									});
																								}
																								
																								@Override
																								public void onFailure(Throwable caught) {
																									Window.alert("Failed:"+caught.getMessage());
																									
																								}
																							});						
																					
																						}
																					};
																					getSIPTimer.schedule(5000);
																						}
																						
																						@Override
																						public void onFailure(Throwable caught) {
																							Window.alert("Failed:"+caught.getMessage());
																							
																						}
																					});						 	
															            		
																}
																else{
																	//restart ingest
																	
																	n=result.getNumSplitSIPs();
																	String[] arr = result.getResumeSipPath().split("_");
																	int sipNumber = Integer.parseInt(arr[arr.length-1].split("\\.")[0]);
																	l = sipNumber;																					
																	if(l<=n){
																		
																		Window.alert("Starting reingest of dataset. We already have the cached SIP for this dataset.");
																		mediciService.generateWfInstanceId(new AsyncCallback<String>() {
																			
																			@Override
																			public void onSuccess(String wfInstanceId) {
																				mediciService.submitMultipleSips(SeadApp.deposit_endpoint + "sip",
																						null,
																						sparqlEndpoint,
																						result.getResumeSipPath().replace("_"+l+".xml", ""),
																						wfInstanceId,
																						result.getPreviousStatusUrls(),
																						l, n, "", "", false, GWT.getModuleBaseURL(),SeadApp.tmpHome,
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
																										nPanel.remove(notify);
																									}
																								});
																								nPanel.add(notify);
																							}
																							
																							@Override
																							public void onFailure(Throwable caught) {
																								
																							}
																						});
																				//Open a status panel that self queries the database for changes
																			WfEventRefresherPanel eventRefresher = new WfEventRefresherPanel(submitterId, wfInstanceId);
																			eventRefresher.show();
																			}
																			
																			@Override
																			public void onFailure(Throwable caught) {
																				
																			}
																		});
																		
																	}
																	else{
																		Window.alert("This dataset is already ingested. Please clear checkpointing if you want to rerun the workflow.");
																	}
																	//MediciIngestPresenter.EVENT_BUS.fireEvent(new SubmitSipEvent(
//																result.getResumeSipPath().replace("_"+l+".xml", ""),
//																result.getPreviousStatusUrls()
//																));
																}
																
															}
										            	});
													}
												});
										            }
										        int index;
										        if(flagHyperlink ==1){
										        	index = first;
										        	first++;
										        }
										        else{
										        	index= last;
										        	last--;
										        }
										        	
										        grid.setWidget(index,0,dataset);
										        grid.getRowFormatter().setStyleName(index, "DatasetsRow");
										        
										       
										            }
										        });
										        it.remove(); // avoids a ConcurrentModificationException
										        }
										    leftPanel.add(grid);		
										    
											
										}
										
										@Override
										public void onFailure(Throwable caught) {
											// TODO Auto-generated method stub
											
										}
									});
								}
							});
						} catch (RequestException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}
				});
			}
		});
	}
	
public Widget getFiles(Map<String, CollectionNode> dusMap,Map<String, FileNode> filesMap, final String selectedCollection, Boolean valForAll) {	
		 
	List<String> files = dusMap.get(selectedCollection).getSub().get(SubType.File);
	final FileTable fileTable = new FileTable();
	
	CellTable.Resources resource = GWT.create(TableResources.class);
	fileTable.cellTable = new CellTable<FileNode>(files.size(), resource);
	
	
	fileTable.cellTable.setWidth("100%", true);
	fileTable.selectionFileModel = new MultiSelectionModel<FileNode>(
        );
   
    fileTable.cellTable.setSelectionModel(fileTable.selectionFileModel,
        DefaultSelectionEventManager.<FileNode> createCheckboxManager());
    fileTable.selectionFileModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
		
		@Override
		public void onSelectionChange(SelectionChangeEvent event) {
			// TODO Auto-generated method stub
			List<FileNode> nowSelected = new ArrayList<FileNode>(fileTable.selectionFileModel.getSelectedSet());
			List<FileNode> previousSelected;
			List<FileNode> tempSelected;
			
			if(previousSelectedFiles.containsKey(selectedCollection))
				previousSelected = previousSelectedFiles.get(selectedCollection);
			else
				previousSelected = new ArrayList<FileNode>();

			
			if(nowSelected.size()>previousSelected.size())
			{
				tempSelected = new ArrayList<FileNode>	(nowSelected);
				tempSelected.removeAll(previousSelected);
			}
			else{
				tempSelected = new ArrayList<FileNode>(previousSelected);
				tempSelected.removeAll(nowSelected);
			}
			
			previousSelectedFiles.put(selectedCollection, nowSelected); //update previous selected files
		//	if(tempSelected.size()==1)//uncomment these 2 statements later
			//	MediciIngestPresenter.EVENT_BUS.fireEvent(new FileSelectEvent(tempSelected.get(0),true));
					//(FileNode)selectionFileModel.getSelectedSet().toArray()[0],true));
		}
	});
    Column<FileNode, Boolean> checkColumn = new Column<FileNode, Boolean>(
            new CheckboxCell(true, false)) {
          @Override
          public Boolean getValue(FileNode object) {
            // Get the value from the selection model.
            return fileTable.selectionFileModel.isSelected(object);
          }
  
    };
    
    fileTable.cellTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
    fileTable.cellTable.setColumnWidth(checkColumn, 40, Unit.PX);
        
        // First name.
        final Resources resources = GWT.create(Resources.class);
   	
        Column<FileNode, ImageResource> imageColumn = new Column<FileNode, ImageResource>(
        		new ImageResourceCell()) {
              @Override
              public ImageResource getValue(FileNode object) {
                return resources.file();
              }
            
            };
            
            fileTable.cellTable.addColumn(imageColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
            fileTable.cellTable.setColumnWidth(imageColumn, 40, Unit.PX);
            
        Column<FileNode, String> firstNameColumn = new Column<FileNode, String>(
            new EditTextCell()) {
          @Override
          public String getValue(FileNode object) {
            return object.getTitle();
          }
        
        };
        
        fileTable.cellTable.addColumn(firstNameColumn);
       
       List<FileNode> fileNodes = new ArrayList<FileNode>();
       
       for(String file:files){
    	   FileNode node = filesMap.get(file);
    	   fileNodes.add(node);
    	   fileTable.selectionFileModel.setSelected(node, valForAll);
       }
        ListDataProvider<FileNode> dataProvider = new ListDataProvider<FileNode>(
        		fileNodes);
        
        dataProvider.addDataDisplay(fileTable.cellTable);
      
        existingFileSets.put(selectedCollection, fileTable);
		return fileTable.cellTable;
	}


	@Override
	public void display(Panel mainContainer, Panel facetContent,
			Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		mainContainer.clear();
		facetContent.clear();
		bind();
		nPanel = notificationPanel;
		mainContainer.setWidth("100%");
		mainContainer.setHeight("100%");
		mainContainer.setStyleName("Border");
		//mainContainer.add(content);
		mainContainer.add(mainContentPanel);
		
	}
	
	public interface Resources extends ClientBundle {
		 @Source("org/dataconservancy/dcs/access/client/resources/file.png")
		  ImageResource file();
	}
	
	

		
	class FileTable{
		CellTable.Resources resource; 
		CellTable<FileNode> cellTable;		
	    MultiSelectionModel<FileNode> selectionFileModel;
	}
	    	
}
