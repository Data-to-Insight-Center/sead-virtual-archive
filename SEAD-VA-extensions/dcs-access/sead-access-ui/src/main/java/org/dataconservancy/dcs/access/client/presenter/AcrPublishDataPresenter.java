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
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.*;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.TreeViewModel;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.model.DcpTree;
import org.dataconservancy.dcs.access.client.model.FileNode;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.client.ui.MessagePopupPanel;
import org.dataconservancy.dcs.access.client.ui.NotificationPopupPanel;
import org.dataconservancy.dcs.access.client.ui.StatusPopupPanel;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.shared.Query;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.UserSession;

import java.util.*;


public class AcrPublishDataPresenter implements Presenter {

    public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
    public static int eventInvoked=0;
    public static String metadataSrc=null;

    Display display;
    ListBox acrProjectList;	//projectList in PublishDataView
    ListBox ROList;// List of ACR LOs

    public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
    public static final RegistryServiceAsync registryService = GWT.create(RegistryService.class);
    public static final DepositServiceAsync depositService = GWT.create(DepositService.class);

    public interface Display {
        VerticalPanel getPublishContainer();
        CaptionPanel getROPanel();
        ListBox getProjectList();
        ListBox getROList();
        Button getPreviewButton();
        Panel getButtonPanel();
    }

    public AcrPublishDataPresenter(Display view){
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


    Map<String,List<FileNode>> previousSelectedFiles;

    @Override
    public void bind() {

        acrProjectList = this.display.getProjectList();
        ROList = this.display.getROList();

        SeadApp.userService.checkSession(null, new AsyncCallback<UserSession>() {

            @Override
            public void onSuccess(final UserSession result) {
                addPreviewClickHandler(result);
                mediciService.getAcrInstances(new AsyncCallback<List<MediciInstance>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                    }

                    @Override
                    public void onSuccess(List<MediciInstance> instances) {
                        submitterId = result.getEmail();
                        if(!result.getRole().equals(Role.ROLE_NONSEADUSER))
                        {
                            for(MediciInstance instance:instances){
                                acrProjectList.addItem(instance.getTitle());


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
                            acrProjectList.setVisibleItemCount(instances.size());
                        }
                        else{
                            for(MediciInstance instance:instances){
                                if(instance.getType().equalsIgnoreCase("demo"))
                                    acrProjectList.addItem(instance.getTitle());
                            }
                            acrProjectList.setVisibleItemCount(1);
                        }

                        acrProjectList.setItemSelected(0, true);
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

        acrProjectList.addChangeHandler(new ChangeHandler()  {


            @Override
            public void onChange(ChangeEvent event) {
                ROList.clear();
                final NotificationPopupPanel mediciWait = new NotificationPopupPanel("Retrieving", false);
                mediciWait.show();
                previousSelectedFiles = new HashMap<String,List<FileNode>>();
                int selected = acrProjectList.getSelectedIndex();
                final String instance = acrProjectList.getValue(selected);

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
                                    new ErrorPopupPanel("Error:"+exception.getMessage()).show();
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
                                                final String datasetId =(String) pair.getKey();

                                                flagHyperlink =0;
                                                String tagRetrieveUrl =
                                                        SeadApp.accessurl+ SeadApp.queryPath+"?q=resourceValue:"+
                                                                "("+
                                                                URL.encodeQueryString(((String) pair.getKey()).replace(":", "\\:"))+
                                                                ")";
                                                rb.requestObject(tagRetrieveUrl, new AsyncCallback<JsSearchResult>() {

                                                    public void onFailure(Throwable caught) {
                                                        Util.reportInternalError("Matching collection in VA failed", caught);
                                                    }

                                                    public void onSuccess(JsSearchResult result) {

                                                        dataset = Util.label(dName.substring(dName.lastIndexOf("/") + 1), "Hyperlink");
                                                        flagHyperlink =1;
                                                        ROList.addItem(dName.substring(dName.lastIndexOf("/")+1), datasetId);

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
                                            //    leftPanel.add(grid);


                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            new ErrorPopupPanel("Error:"+caught.getMessage());
                                        }
                                    });
                                }
                            });
                        } catch (RequestException e) {
                            new ErrorPopupPanel("Error:"+ e.getMessage());
                        }
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        new ErrorPopupPanel("Error:"+caught.getMessage());
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

    public void addPreviewClickHandler(final UserSession session){
        display.getPreviewButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final NotificationPopupPanel mediciWait = new NotificationPopupPanel("Retrieving Bag and converting to SIP", false);
                mediciWait.show();
                int index = display.getROList().getSelectedIndex();
                final String collectionId = display.getROList().getValue(index);
                final String collectionToDownload = display.getROList().getItemText(index);
                int acrIndex = acrProjectList.getSelectedIndex();
                final String acrProjectName = acrProjectList.getItemText(acrIndex);
                mediciService.getBag(collectionId, sparqlEndpoint, SeadApp.bagIturl, null,
                        new AsyncCallback<String>() {
                            @Override
                            public void onSuccess(String bagPath) {
                                mediciService.getSipJsonFromBag(bagPath,
                                        bagPath.replace(".zip", "")
                                        , "http://seadva-test.d2i.indiana.edu:8080/bagit/acrToBag/"
                                        , new AsyncCallback<String>() {
                                    @Override
                                    public void onSuccess(String resultStr) {
                                        mediciWait.hide();


                                        String[] tempString = resultStr.split(";");
                                        final String sipPath = tempString[tempString.length-1].split("<")[0];
                                        String jsonString = resultStr;
                                        jsonString = jsonString.substring(jsonString.indexOf('{'), jsonString.lastIndexOf('}')+1);

                                        JsDcp dcp = JsDcp.create();
                                        JsSearchResult result = JsSearchResult.create(jsonString);
                                        for (int i = 0; i < result.matches().length(); i++) {
                                            Util.add(dcp, result.matches().get(i));
                                        }

                                        display.getROPanel().clear();
                                        TreeViewModel treemodel = new DcpTree(dcp);

                                        CellTree tree = new CellTree(treemodel, null);

                                        //tree.setStylePrimaryName("RelatedView");
                                        tree.setHeight("90%");
                                        tree.setAnimationEnabled(true);
                                        tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
                                        tree.setDefaultNodeSize(50);

                                        if (tree.getRootTreeNode().getChildCount() > 0) {
                                            tree.getRootTreeNode().setChildOpen(0, true);
                                        }

                                        Panel previewPanel = new ScrollPanel();
                                        previewPanel.setWidth("95%");
                                        previewPanel.setHeight("100%");
                                        previewPanel.add(tree);
                                        display.getROPanel().add(previewPanel);
                                        display.getButtonPanel().clear();
                                        Button submitButton = new Button("Pull into Curator's Queue");
                                        submitButton.addClickHandler(new ClickHandler() {
                                            @Override
                                            public void onClick(ClickEvent event) {
                                                startWorkflow(sipPath, session);
                                            }
                                        });

                                        display.getButtonPanel().add(submitButton);
                                    }
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        new ErrorPopupPanel("Error:"+caught.getMessage());
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

    void startWorkflow(final String sipPath, final UserSession userSession){
        mediciService.generateWfInstanceId(new AsyncCallback<String>() {

            @Override
            public void onSuccess(final String wfInstanceId) {
                final MessagePopupPanel popupPanel = new MessagePopupPanel("Starting workflow. Please view status in the monitor panel.","wait", true);
                popupPanel.show();
                mediciService.submitMultipleSips(
                        SeadApp.deposit_endpoint + "sip",
                        null,
                        null,
                        sipPath.replace("_0.xml", ""),
                        wfInstanceId,
                        null,
                        0, 0, "", "", false, GWT.getModuleBaseURL(), SeadApp.tmpHome,true, false,
                        new AsyncCallback<String>() {

                            @Override
                            public void onSuccess(String statusUrl) {
//									Window.alert("Done:" + result);
                                popupPanel.hide();
                                MessagePopupPanel donePanel = new MessagePopupPanel(statusUrl, "done", true);
                                donePanel.show();

                                depositService.getResearchObjectId(statusUrl,new AsyncCallback<String>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                                    }

                                    @Override
                                    public void onSuccess(final String entityId) {
                                        //Get SIP's location and title
                                        registryService.getROAffiliation(entityId, SeadApp.registryUrl, new AsyncCallback<String>() {

                                            @Override
                                            public void onSuccess(String affiliation) {//Email all agents that have affiliation with the updated SIP's location institutional
                                                SeadApp.userService.emailCurators(affiliation, new AsyncCallback<Boolean>() {

                                                    @Override
                                                    public void onSuccess(Boolean result) {
                                                        registryService.assignToSubmitter(entityId, userSession.getRegistryId()
                                                                , SeadApp.registryUrl, new AsyncCallback<Boolean>() {

                                                            @Override
                                                            public void onFailure(
                                                                    Throwable caught) {
                                                                new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                                                            }

                                                            @Override
                                                            public void onSuccess(
                                                                    Boolean result) {
                                                                System.out
                                                                        .println(userSession.getRegistryId());
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
                                            public void onFailure(Throwable caught) {
                                                new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                                            }
                                        });
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
            public void onFailure(Throwable caught) {
                new ErrorPopupPanel("Error:"+caught.getMessage()).show();
            }
        });
    }

}