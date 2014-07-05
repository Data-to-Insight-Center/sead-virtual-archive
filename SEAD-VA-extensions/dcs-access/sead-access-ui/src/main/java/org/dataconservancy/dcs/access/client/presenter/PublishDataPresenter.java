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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.view.client.TreeViewModel;

import org.dataconservancy.dcs.access.client.model.DcpTree;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.event.EntityEditEvent;
import org.dataconservancy.dcs.access.client.model.*;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.client.ui.NotificationPopupPanel;
import org.dataconservancy.dcs.access.client.ui.StatusPopupPanel;
import org.dataconservancy.dcs.access.client.view.PublishDataView;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.UserSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PublishDataPresenter implements Presenter {

    public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
    public static int eventInvoked=0;
    public static String metadataSrc=null;

    Display display;
    ListBox ir;	//projectList in PublishDataView
    ListBox ROList;
    VerticalPanel verticalPanel;
    VerticalPanel acrContainer;
    CaptionPanel researchObjectPanel;

    TextBox projectNameTB;
    TextArea abstractTB;
    TextBox roId;
    VerticalPanel warningPanel;
    Label provenanceType;
    Label errorMessage;
    Button previewButton;
    CheckBox licenseBox;

    public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
    public static final RegistryServiceAsync registryService = GWT.create(RegistryService.class);
    public static final DepositServiceAsync depositService = GWT.create(DepositService.class);

    public interface Display {
        VerticalPanel getPublishContainer();
        ListBox getIr();
        ListBox getROList();
        Button getUploadBag();
        FormPanel getForm();
        TextBox getProjectNameTB();
        TextArea getAbstractTB();
        VerticalPanel getWarningPanel();
        Label getProvenanceType();
        Button getPreviewButton();
        Label getErrorMessage();
        CaptionPanel getResearchObjectPanel();
        TextBox getRoId();
        CheckBox getLicenseBox();
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

    Map<String,List<FileNode>> previousSelectedFiles;

    @Override
    public void bind() {
        projectNameTB = this.display.getProjectNameTB();
        abstractTB = this.display.getAbstractTB();
        warningPanel = this.display.getWarningPanel();
        previewButton = this.display.getPreviewButton();
        errorMessage = this.display.getErrorMessage();
        researchObjectPanel = this.display.getResearchObjectPanel();
        roId = this.display.getRoId();
        licenseBox = this.display.getLicenseBox();

        ir = this.display.getIr();
        ROList = this.display.getROList();
        registerPreviewUpdate();

        this.display.getUploadBag().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                display.getForm().submit();
                //	new UploadBagDialog(SeadApp.bagIturl);
                if(acrContainer.isAttached())
                    acrContainer.removeFromParent();
            }
        });

        this.display.getForm().addSubmitCompleteHandler(new SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {

                if (event.getResults() == null) {
                    new ErrorPopupPanel("File upload failed").show();
                    return;
                }

                if(event.getResults().contains("Warning")){
                    new ErrorPopupPanel(new HTML(event.getResults())).show();
                    return;
                }

                String[] tempString = event.getResults().split(";");
                final String sipPath = tempString[tempString.length-1].split("<")[0];
                String jsonString = event.getResults();
                jsonString = jsonString.substring(jsonString.indexOf('{'), jsonString.lastIndexOf('}')+1);

                JsDcp dcp = JsDcp.create();
                JsSearchResult result = JsSearchResult.create(jsonString);
                for (int i = 0; i < result.matches().length(); i++) {
                    Util.add(dcp, result.matches().get(i));
                }

                PublishDataView.EVENT_BUS.fireEvent(new EntityEditEvent(dcp, true, sipPath));
            }
        });

        SeadApp.userService.checkSession(null, new AsyncCallback<UserSession>() {

            @Override
            public void onSuccess(final UserSession result) {
                if(result.isSession()){
                    if(result.getRole()== Role.ROLE_CURATOR){
                        acrContainer =  new VerticalPanel();
                        acrContainer.addStyleName("PublishContainer");
                        Button importButton = new Button("Import from ACR");
                        importButton.addClickHandler(new ClickHandler() {

                            @Override
                            public void onClick(ClickEvent event) {
                                History.newItem(SeadState.ACRUPLOAD.toToken());
                            }
                        });
                        Grid grid = new Grid(2,3);
                        grid.getColumnFormatter().setWidth(0, "33%");
                        grid.getColumnFormatter().setWidth(1, "33%");
                        grid.getColumnFormatter().setWidth(2, "33%");
                        HTMLTable.CellFormatter formatter = grid.getCellFormatter();
                        formatter.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
                        formatter.setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_MIDDLE);
                        grid.setWidth("100%");
                        grid.setWidget(1, 1, importButton);
                        acrContainer.setWidth(String.valueOf(display.getPublishContainer().getOffsetWidth())+"px");
                        acrContainer.setHeight(Window.getClientHeight()/10+"px");
                        acrContainer.add(grid);
                        verticalPanel.add(acrContainer);
                    }
                }
            }
            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.getMessage());
            }
        });

    }

    String originalTitle = "";
    String originalAbstract = "";
    String topDuId = "";

    private void registerPreviewUpdate(){
        final EntityEditEvent.Handler editHandler = new EntityEditEvent.Handler() {
            @Override
            public void onMessageReceived(final EntityEditEvent editEvent) {
                //	Panel outerPanel = new FlowPanel();
                //	outerPanel.clear();
                //	outerPanel.add(Util.label("Your SIP is ready for review by your Institutional Repository.", "TopHeaderText"));


                TreeViewModel treemodel = new DcpTree(editEvent.getDcp());
                JsArray<JsDeliverableUnit> dus = editEvent.getDcp().getDeliverableUnits();
                for(int i =0; i<dus.length();i++){
                    if(dus.get(i).getParents().length()==0)
                    {
                        projectNameTB.setEnabled(true);
                        abstractTB.setEnabled(true);
                        originalTitle = dus.get(i).getCoreMd().getTitle();
                        originalAbstract = dus.get(i).getAbstract();
                        projectNameTB.setText(originalTitle);
                        abstractTB.setText(originalAbstract);
                        topDuId = dus.get(i).getId();

                        final JsDeliverableUnit du = dus.get(i);
                        JsArray<JsAlternateId> alternateIds = dus.get(i).getAlternateIds();
                        boolean containsDOI = false;
                        if(alternateIds.length()>0){
                            for(int j =0; j<alternateIds.length();j++){
                                final JsAlternateId alternateId =  alternateIds.get(j);
                                if(alternateId.getTypeId().contains("doi")){
                                    containsDOI = true;
                                    final Label warningLabel = new Label();
                                    warningLabel.setText("It seems like this dataset was already published and " +
                                            "has an identifier." +
                                            "Would you like Virtual Archive to track provenance to this Published dataset?");
                                    final Grid tracked = new Grid(2,4);

                                    Label dataIdentifier = new Label("Data Identifier");
                                    tracked.setWidget(0, 0, dataIdentifier);
                                    TextBox dataIdentifierTB = new TextBox();
                                    dataIdentifierTB.setText(alternateId.getIdValue());
                                    tracked.setWidget(0, 1, dataIdentifierTB);
                                    final Button trackRevision = new Button("Yes");
                                    trackRevision.addClickHandler(new ClickHandler() {
                                        //Revision or derivation allowed

                                        @Override
                                        public void onClick(ClickEvent event) {
                                            warningLabel.setText("");
                                            provenanceType = new Label("http://www.w3.org/ns/prov#wasDerivedFrom");
                                            tracked.setWidget(1, 0, provenanceType);
                                            roId = new TextBox();
                                            roId.setText(du.getId());
                                            tracked.setWidget(1, 1, roId);
                                            warningPanel.removeStyleName("red");
                                            tracked.remove(trackRevision);
                                            previewButton.setEnabled(true);
                                        }
                                    });
                                    tracked.setWidget(0, 2, trackRevision);
                                    //	tracked.setWidget(0, 3, new Button("No"));
                                    warningPanel.setStyleName("red");
                                    warningPanel.add(warningLabel);
                                    warningPanel.add(tracked);
                                    break;
                                }
                            }
                        }
                        if(!containsDOI)
                            previewButton.setEnabled(true);
                        break;
                    }
                }

                CellTree tree = new CellTree(treemodel, null);

                //tree.setStylePrimaryName("RelatedView");
                tree.setHeight("90%");
                tree.setAnimationEnabled(true);
                tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
                tree.setDefaultNodeSize(50);

                if (tree.getRootTreeNode().getChildCount() > 0) {
                    tree.getRootTreeNode().setChildOpen(0, editEvent.isSetOpen());
                }

                Panel previewPanel = new ScrollPanel();
                previewPanel.setWidth("95%");
                previewPanel.setHeight("100%");
                previewPanel.add(tree);
                researchObjectPanel.clear();
                researchObjectPanel.add(previewPanel);

                ClickHandler beginIngest = new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        previewButton.setEnabled(false);
                        if(licenseBox.getValue()){
                            SeadApp.userService.checkSession(null, new AsyncCallback<UserSession>() {
                                @Override
                                public void onSuccess(final UserSession userSession) {

                                    registryService.cleanSip(editEvent.getSipPath(), new AsyncCallback<Void>() {

                                        @Override
                                        public void onSuccess(Void result) {
                                            if(!originalTitle.equalsIgnoreCase(projectNameTB.getText())||!originalAbstract.equalsIgnoreCase(abstractTB.getText())||provenanceType!=null){
                                                Map<String, String> changes = new HashMap<String,String>();
                                                if(provenanceType!=null)
                                                    changes.put(provenanceType.getText(), roId.getText());
                                                if(!originalTitle.equalsIgnoreCase(projectNameTB.getText()))
                                                    changes.put("title",projectNameTB.getText());
                                                if(!originalAbstract.equalsIgnoreCase(abstractTB.getText()))
                                                    changes.put("abstract",abstractTB.getText());

                                                registryService.updateSip(editEvent.getSipPath(), topDuId, changes, new AsyncCallback<Void>() {

                                                    @Override
                                                    public void onSuccess(Void result) {
                                                        startWorkflow(editEvent.getSipPath(), userSession);
                                                    }
                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                                                    }
                                                });
                                            }
                                            else{
                                                startWorkflow(editEvent.getSipPath(), userSession);
                                            }
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
                        else{
                            errorMessage.setStyleName("redFont");
                            errorMessage.setText("*Please agree to our license terms, before publishing the data.");
                        }
                    }
                };
                previewButton.addClickHandler(beginIngest);

            }
        };
        EntityEditEvent.register(PublishDataView.EVENT_BUS, editHandler);
    }

    void startWorkflow(final String sipPath, final UserSession userSession){
        mediciService.generateWfInstanceId(new AsyncCallback<String>() {

            @Override
            public void onSuccess(final String wfInstanceId) {
                final NotificationPopupPanel popupPanel = new NotificationPopupPanel("Starting Submission workflow. Please wait for your submission to complete.", false);
                popupPanel.show();
//				 final MessagePopupPanel popupPanel = new MessagePopupPanel("Starting workflow. Please view status in the monitor panel.","wait", true);
//				 popupPanel.show();
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
                                if(popupPanel.isShowing())
                                    popupPanel.hide();
                                NotificationPopupPanel finishPopPanel = new NotificationPopupPanel("Workflow Completed Succesfully", "done");
//								MessagePopupPanel donePanel = new MessagePopupPanel(statusUrl, "done", true);
                                finishPopPanel.show();

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
        verticalPanel = new VerticalPanel();
        verticalPanel.add(this.display.getPublishContainer());
        mainContainer.add(verticalPanel);

    }

}
