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
import org.dataconservancy.dcs.access.client.event.*;
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
	ListBox sortOrderList;
	Button ingestButton;
	Panel leftPanel;
	Panel rightPanel;
	Panel coverRightPanel;
	Panel nPanel;
	ListBox ir ;
	CheckBox cloudCopy;
	public static CheckBox mdCb;
	Date latestDate = new Date();
	Tree rootTree;

	Map<String,List<FileNode>> previousSelectedFiles;
	
	public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
	public static final DepositServiceAsync depositService = GWT.create(DepositService.class);
	public static final LogServiceAsync logService = GWT.create(LogService.class);
	
	
	Label dataset;
	
	Map<String, ArrayList<Label>> collectionList;
	
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
		ListBox getSortOrderList();
	}
	
	Display display;
	
	public MediciIngestPresenter(Display view)
	{
		this.display = view;
		rootTree = new Tree();
		collectionList = new HashMap<String, ArrayList<Label>> ();
		
	}
	
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
		sortOrderList = this.display.getSortOrderList();
		
		registerGetBagEvent();
		registerGetSipEvent();
		registerSubmitSipEvent();
		
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
								
								
								FlexTable grid1 = new FlexTable(
										);
								grid1.setWidth("100%");
								grid1.setHeight("100%");
								
								grid1.setWidget(0, 0, new Label("test11"));
								
								if(instance.getTitle().charAt(0)=='S'||instance.getTitle().charAt(0)=='N'){
									TreeItem parent = new TreeItem(instance.getTitle());
									ArrayList<Label> datasetList = new ArrayList<Label>();
									collectionList.put(instance.getTitle(), datasetList);
									getPublications(instance.getTitle(),parent,datasetList);
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
					     getPub.setEnabled(true);
					     addSortHandler();
					     leftPanel.clear();
						 leftPanel.add(rootTree);
						
					}

				});
				}
				@Override
				public void onFailure(Throwable caught) {
					 Window.alert(caught.getMessage());
				}
		});
	}

    private void registerGetSipEvent(){
        GetSipEvent.register(EVENT_BUS, new GetSipEvent.Handler() {
            public void onMessageReceived(final GetSipEvent getSipEvent) {
                mediciService.getSipFromBag(
                        getSipEvent.getBagPath(),
                        SeadApp.tmpHome+getSipEvent.getGuid()+"_sip.xml",
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
//                                            if(collectionWait.isShowing())
//                                                collectionWait.hide();
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
                                                    String rootMediciId= getSipEvent.getDatasetId();


                                                    AsyncCallback<Void> vaModelCb = new AsyncCallback<Void>() {
                                                        @Override
                                                        public void onSuccess(Void result) {
                                                            mediciService.addMetadata(metadataSrc,SeadApp.tmpHome+getSipEvent.getGuid()+"_sip", new AsyncCallback<Void>() {

                                                                @Override
                                                                public void onSuccess(Void result) {


                                                                    mediciService.splitSip(
                                                                            SeadApp.tmpHome+getSipEvent.getGuid()+"_sip",
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

                                                                                                        MediciIngestPresenter.EVENT_BUS.fireEvent(new SubmitSipEvent(
                                                                                                                wfInstanceId,
                                                                                                                getSipEvent.getDatasetId(),
                                                                                                                getSipEvent.getGuid(),
                                                                                                                getSipEvent.getSparqlEndpoint()
                                                                                                        )
                                                                                                        );
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
                                                    mediciService.toVAmodel(rootMediciId,rootMediciId,getSipEvent.getSparqlEndpoint(), SeadApp.tmpHome, vaModelCb );

                                                }
                                            });

                                            coverRightPanel.setVisible(true);
                                        }
                                        else{
                                            mediciService.getSipJson(new AsyncCallback<String>() {


                                                @Override
                                                public void onFailure(
                                                        Throwable caught) {
                                                    Window.alert("Failed:" + caught.getMessage());

                                                }

                                                @Override
                                                public void onSuccess(
                                                        final String json) {


                                                    display.getDatasetLbl().setText("Browse Collection and sub-Collections");
                                                    display.getFileLbl().setText("Browse Files");

                                                    String jsonString = json;
                                                    jsonString = jsonString.substring(jsonString.indexOf('{'), jsonString.lastIndexOf('}') + 1);


                                                    JsDcp dcp = JsDcp.create();
                                                    JsSearchResult result = JsSearchResult.create(jsonString);
                                                    for (int i = 0; i < result.matches().length(); i++) {
                                                        Util.add(dcp, result.matches().get(i));
                                                    }

                                                    TreeViewModel treemodel = new PreviewTree(dcp);

                                                    CellTree tree = new CellTree(treemodel, null);

                                                    tree.setStylePrimaryName("RelatedView");
                                                    tree.setAnimationEnabled(true);
                                                    tree.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.BOUND_TO_SELECTION);
                                                    tree.setDefaultNodeSize(50);

                                                    if (tree.getRootTreeNode().getChildCount() > 0) {
                                                        tree.getRootTreeNode().setChildOpen(0, false);
                                                    }

                                                    leftPanel.clear();
                                                    leftPanel.add(tree);

//                                                    if (collectionWait.isShowing())
//                                                        collectionWait.hide();
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
                                                            String rootMediciId = getSipEvent.getDatasetId();

                                                            AsyncCallback<Void> vaModelCb = new AsyncCallback<Void>() {
                                                                @Override
                                                                public void onSuccess(Void result) {
                                                                    mediciService.addMetadata(metadataSrc, SeadApp.tmpHome + getSipEvent.getGuid() + "_sip", new AsyncCallback<Void>() {

                                                                        @Override
                                                                        public void onSuccess(Void result) {
                                                                            String tempguid = null;
                                                                            if ((getSipEvent.getDatasetId()).contains("/"))
                                                                                tempguid = (getSipEvent.getDatasetId()).split("/")
                                                                                        [(getSipEvent.getDatasetId()).split("/").length - 1];
                                                                            else
                                                                                tempguid = (getSipEvent.getDatasetId()).split(":")
                                                                                        [(getSipEvent.getDatasetId()).split(":").length - 1];
                                                                            final String guid = tempguid;

                                                                            mediciService.splitSip(
                                                                                    SeadApp.tmpHome + guid + "_sip",
                                                                                    new AsyncCallback<Integer>() {

                                                                                        @Override
                                                                                        public void onSuccess(Integer result) {
                                                                                            n = result;
                                                                                            l++;

//																																					Window.alert("Starting ingest of dataset");//. We already have the cached SIP for this dataset.");
                                                                                            mediciService.generateWfInstanceId(new AsyncCallback<String>() {

                                                                                                @Override
                                                                                                public void onSuccess(String wfInstanceId) {
                                                                                                    MediciIngestPresenter.EVENT_BUS.fireEvent(new SubmitSipEvent(
                                                                                                            wfInstanceId,
                                                                                                            getSipEvent.getDatasetId(),
                                                                                                            guid,
                                                                                                            getSipEvent.getSparqlEndpoint()
                                                                                                    )
                                                                                                    );
                                                                                                }

                                                                                                @Override
                                                                                                public void onFailure(Throwable caught) {

                                                                                                }
                                                                                            });

                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Throwable caught) {
                                                                                            Window.alert("Failed. \n" + caught.getMessage());
                                                                                        }
                                                                                    });
                                                                        }

                                                                        @Override
                                                                        public void onFailure(Throwable caught) {
                                                                            Window.alert("Failed. \n" + caught.getMessage());
                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onFailure(Throwable caught) {
                                                                    Window.alert("Failed. \n" + caught.getMessage());
                                                                }
                                                            };
                                                            mediciService.toVAmodel(rootMediciId, rootMediciId, getSipEvent.getSparqlEndpoint(), SeadApp.tmpHome, vaModelCb);

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
        });

    }

    private void registerGetBagEvent(){
        GetBagEvent.register(EVENT_BUS, new GetBagEvent.Handler() {
            public void onMessageReceived(final GetBagEvent getBagEvent) {
                final StatusPopupPanel collectionWait = new StatusPopupPanel("Querying for BagIt Bag","bag",false);
                collectionWait.setPopupPosition(Window.getClientWidth()*4/10, Window.getClientHeight()/3);
                collectionWait.show();

                mediciService.getBag(
                        getBagEvent.getDatasetId(), getBagEvent.getSparqlEndpoint(),
                        SeadApp.bagIturl, SeadApp.tmpHome,
                        new AsyncCallback<String>() {
                            @Override
                            public void onSuccess(final String bagPath) {
                                collectionWait.setValue("Converting to SEAD SIP", "wait");

                                final Timer getSIPTimer = new Timer() {

                                    @Override
                                    public void run() {
                                        String tempguid = null;
                                        if((getBagEvent.getDatasetId()).contains("/"))
                                            tempguid = (getBagEvent.getDatasetId()).split("/")
                                                    [(getBagEvent.getDatasetId()).split("/").length-1];
                                        else
                                            tempguid = (getBagEvent.getDatasetId()).split(":")
                                                    [(getBagEvent.getDatasetId()).split(":").length-1];
                                        final String guid = tempguid;
                                        MediciIngestPresenter.EVENT_BUS.fireEvent(new
                                                GetSipEvent(
//                                                getBagEvent.getWfInstanceId(),
                                                getBagEvent.getDatasetId(),
                                                guid,
                                                bagPath,
                                                getBagEvent.getSparqlEndpoint()
                                        ));

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

    void registerSubmitSipEvent(){//This event simply changes what is displayed on the popup panel
        SubmitSipEvent.register(EVENT_BUS, new SubmitSipEvent.Handler() {
            public void onMessageReceived(final SubmitSipEvent event) {
                WfEventRefresherPanel eventRefresher = new WfEventRefresherPanel(submitterId, event.getWfIsntanceId());
                eventRefresher.show();
                mediciService.submitMultipleSips(SeadApp.deposit_endpoint + "sip",
                        event.getDatasetId(),
                        event.getSparqlEndpoint(),
                        SeadApp.tmpHome + event.getGuid() + "_sip",
                        event.getWfIsntanceId(),
                        null,
                        l, n, "", "", false, GWT.getModuleBaseURL(), SeadApp.tmpHome,
                        new AsyncCallback<String>() {

                            @Override
                            public void onSuccess(final String result) {
                                l = -1;
                                final Label notify = Util.label("!", "Notification");
                                notify.addClickHandler(new ClickHandler() {

                                    @Override
                                    public void onClick(ClickEvent event) {
                                        MessagePopupPanel popUpPanel = new MessagePopupPanel(result, "done", true);
                                        popUpPanel.show();
                                        nPanel.remove(notify);
                                    }
                                });
//																																											nPanel.add(notify);
                            }

                            @Override
                            public void onFailure(Throwable caught) {

                            }
                        });
            }
        });
    }
	
	void getPublications(String passedInstance, TreeItem passedParent, ArrayList<Label> passedDatasetList){
		
		try{
		final StatusPopupPanel mediciWait = new StatusPopupPanel("Retrieving","wait",false);
		//mediciWait.setStyleName("retrievePopoup");
		mediciWait.setPopupPosition(Window.getClientWidth()*4/10, Window.getClientHeight()/3);
		mediciWait.show();
		previousSelectedFiles = new HashMap<String,List<FileNode>>();
		final String instance = passedInstance;
		final TreeItem parent = passedParent;
		final ArrayList<Label> datasetList = passedDatasetList;
        
		mediciService.getAcrInstances(new AsyncCallback<List<MediciInstance>>() {
			
			@Override
			public void onSuccess(List<MediciInstance> result) {
                MediciInstance sparqlEp = null;


                for(MediciInstance ins:result)
					if(ins.getTitle().equalsIgnoreCase(instance))
                        sparqlEp = ins;

                final MediciInstance sparqlEndpoint = sparqlEp;
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
									
									//srt
									
									//leftPanel.clear();
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
//							            	if(result.matches().length()==0||sparqlEndpoint.equals("http://sead.ncsa.illinois.edu/acr/resteasy/sparql"))
//							            	{
								            		dataset = Util.label(dName.substring(dName.lastIndexOf("/")+1),"Hyperlink");
								            		//datasetList.add
								            		//collectionList.put(instance, dataset);
								            		datasetList.add(
								            				dataset);
								            	//			dName.substring(dName.lastIndexOf("/")+1));
								            		flagHyperlink =1;					            		
//							            	}				            	
//							            	else
//							                	flagHyperlink =0;
								           
								        
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
														if(!result.isCheckPointed())   {
                                                            MediciIngestPresenter.EVENT_BUS.fireEvent(new GetBagEvent(
                                                                    (String) pair.getKey(),
                                                                    sparqlEndpoint

                                                            ));
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
//														result.getResumeSipPath().replace("_"+l+".xml", ""),
//														result.getPreviousStatusUrls()
//														));
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
								    parent.addItem(grid);
								    parent.setState(true);
								    rootTree.addItem(parent);
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
				System.out.println("in Falilure");
				System.out.println(caught.getLocalizedMessage());
			}
		});
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	void addSortHandler(){
		
		sortOrderList.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				// TODO Auto-generated method stub
				leftPanel.clear();
				rootTree.clear();
				leftPanel.add(rootTree);
				
				final int index = sortOrderList.getSelectedIndex();
				System.out.println("selected: "+index);
				
				for(String key : collectionList.keySet()){
					FlexTable grid1 = new FlexTable(
						);
					grid1.setWidth("100%");
					grid1.setHeight("100%");
					TreeItem parent = new TreeItem(key);
					ArrayList<Label> datasetList= collectionList.get(key);
					Collections.sort(datasetList, new Comparator<Label>(){
						public int compare(Label l1, Label l2){
							// index value:  0 => Ascending sort, 1 => descending
							if(index == 1)
								return l1.getText().compareToIgnoreCase(l2.getText());
							else
								return l2.getText().compareToIgnoreCase(l1.getText());
						}
					});
					for(int i=0;i<datasetList.size();i++){
					    //System.out.println(datasetList.get(i));
					    grid1.setWidget(i, 0, datasetList.get(i));
					}
					parent.addItem(grid1);
				    rootTree.addItem(parent);
				}
			}
		});
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

}
