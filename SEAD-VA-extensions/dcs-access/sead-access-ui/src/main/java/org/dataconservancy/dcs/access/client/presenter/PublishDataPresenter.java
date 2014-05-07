package org.dataconservancy.dcs.access.client.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import org.dataconservancy.dcs.access.client.PreviewTree;
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
import org.dataconservancy.dcs.access.client.model.*;
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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.TreeViewModel;



public class PublishDataPresenter implements Presenter{

	public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
	public static int eventInvoked=0;
	public static String metadataSrc=null;
	
	Display display;
	ListBox ir;	//projectList in PublishDataView
	ListBox ROList;
	
	public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
	
	public interface Display {
		VerticalPanel getPublishContainer();
		ListBox getIr();
		ListBox getROList();
	}
	
	public PublishDataPresenter(Display view){
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
	
	Map<String,FileTable> existingFileSets;
	Map<String,List<FileNode>> previousSelectedFiles;
	
	@Override
	public void bind() {
		
		ir = this.display.getIr();	
		ROList = this.display.getROList();
		
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
								
								
								FlexTable grid1 = new FlexTable(
										);
								grid1.setWidth("100%");
								grid1.setHeight("100%");
								
//								grid1.setWidget(0, 0, new Label("test11"));
								
								if(instance.getTitle().charAt(0)=='S'||instance.getTitle().charAt(0)=='N'){
									
									//TreeItem parent = new TreeItem(instance.getTitle());
									ArrayList<Label> datasetList = new ArrayList<Label>();
									datasetList.add(new Label(instance.getTitle()));
									/*collectionList.put(instance.getTitle(), datasetList);
									getPublications(instance.getTitle(),parent,datasetList);*/
									//parent.addItem(grid1);
									//rootTree.addItem(parent);
								}
								
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
					     addGetPubHandler();
					    /* getPub.setEnabled(true);
					     addSortHandler();*/
					    /* leftPanel.clear();
						 leftPanel.add(rootTree);*/
						
					}

				});
				}
				@Override
				public void onFailure(Throwable caught) {
					 Window.alert(caught.getMessage());
				}
		});
		
	}

	void addGetPubHandler(){
		
	ir.addChangeHandler(new ChangeHandler()  {
			
			
			@Override
			public void onChange(ChangeEvent event) {
				ROList.clear();
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
											
										//	leftPanel.clear();
										  	JsonpRequestBuilder rb = new JsonpRequestBuilder();
										    rb.setTimeout(100000);
										        
											mediciWait.hide();
											last =result.size()-1;
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
										            		System.out.println("dataset"+dName.substring(dName.lastIndexOf("/")+1));
										            		ROList.addItem(dName.substring(dName.lastIndexOf("/")+1));
//									            	}				            	
//									            	else
//									                	flagHyperlink =0;
										           
										        
									/*	       if(flagHyperlink==1){
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
									*/	        int index;
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
										//    leftPanel.add(grid);		
										    
											
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
	@Override
	public void display(Panel mainContainer, Panel facetContent,Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		
		mainContainer.clear();
		facetContent.clear();
		bind();
		//nPanel = notificationPanel;
		mainContainer.setWidth("100%");
		mainContainer.setHeight("100%");
		mainContainer.setStyleName("Border");
		//mainContainer.add(content);
		
		mainContainer.add(this.display.getPublishContainer());
		
	}
	class FileTable{
		CellTable.Resources resource; 
		CellTable<FileNode> cellTable;		
	    MultiSelectionModel<FileNode> selectionFileModel;
	}

}
