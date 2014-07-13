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
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.TreeViewModel;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.event.ROEditEvent;
import org.dataconservancy.dcs.access.client.model.EditDcpTreeModel;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsDeliverableUnit;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.shared.UserSession;


public class EditPresenter implements Presenter {

    public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);
    public static int eventInvoked=0;
    public static String metadataSrc=null;
    String entityId = null;

    Display display;
    ListBox ir;	//projectList in PublishDataView
    ListBox ROList;

    public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
    public static final RegistryServiceAsync registryService = GWT.create(RegistryService.class);
    static final UserServiceAsync user = GWT.create(UserService.class);

    public interface Display {
        VerticalPanel getPublishContainer();
        String getId();
        Panel getContentPanel();
        CaptionPanel getMetadataPanel();
        TextBox getName();
        TextArea getAbstract();
        Button getSaveButton();
        Button getBackButton();
    }

    public EditPresenter(Display view){
        this.display = view;
    }

    String title = "";
    String abstrct = "";
    String sipPath ="";

    @Override
    public void bind() {

        display.getBackButton().addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                History.newItem(SeadState.CURATOR.toToken());
            }

        });

        display.getSaveButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                display.getSaveButton().setEnabled(false);
                //Get SIP from JsDcp
                registryService.putRO(sipPath, SeadApp.roUrl, new AsyncCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        user.checkSession(null, new AsyncCallback<UserSession>() {

                            @Override
                            public void onSuccess(UserSession session) {
                                registryService.trackEvent(session.getRegistryId(), entityId, SeadApp.roUrl, new AsyncCallback<Boolean>() {

                                    @Override
                                    public void onSuccess(Boolean result) {
                                        display.getSaveButton().setText("Saved");
                                    }

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        display.getSaveButton().setText("Save Failed");
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
                    public void onFailure(Throwable caught) {
                        new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                    }
                });
            }
        });

        registerPreviewUpdate();


        registryService.getRO(this.display.getId(), SeadApp.roUrl,
                new AsyncCallback<String>() {

                    @Override
                    public void onSuccess(String resultStr) {
                        String[] tempString = resultStr.split(";");
                        final String sipPath = tempString[tempString.length-1].split("<")[0];
                        resultStr = resultStr.substring(resultStr.indexOf('{'), resultStr.lastIndexOf('}')+1);

                        JsDcp dcp = JsDcp.create();
                        JsSearchResult result = JsSearchResult.create(resultStr);
                        int count = result.matches().length();
                        for (int i = 0; i < count; i++) {
                            Util.add(dcp, result.matches().get(i));
                            if(result.matches().get(i).getEntityType().equalsIgnoreCase("deliverableUnit")){
                                title = ((JsDeliverableUnit)result.matches().get(i).getEntity()).getCoreMd().getTitle();
                                abstrct = ((JsDeliverableUnit)result.matches().get(i).getEntity()).getAbstract();
                                entityId = result.matches().get(i).getEntity().getId();
                            }
                        }

                        EditPresenter.EVENT_BUS.fireEvent(new ROEditEvent(/*dcp,*/ true, sipPath));
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
        mainContainer.add(this.display.getPublishContainer());
    }

    //RO final save
    private void registerPreviewUpdate(){
        final ROEditEvent.Handler editHandler = new ROEditEvent.Handler() {
            @Override
            public void onMessageReceived(final ROEditEvent editEvent) {
            	mediciService.getJsonSip(editEvent.getSipPath(), new AsyncCallback<String>() {
        			
        			@Override
        			public void onSuccess(String resultStr) {
        				 String[] tempString = resultStr.split(";");
                         sipPath = tempString[tempString.length-1].split("<")[0];
                         resultStr = resultStr.substring(resultStr.indexOf('{'), resultStr.lastIndexOf('}')+1);

                         JsDcp dcp = JsDcp.create();
                         JsSearchResult result = JsSearchResult.create(resultStr);
                         int count = result.matches().length();
                         for (int i = 0; i < count; i++) {
                             Util.add(dcp, result.matches().get(i));
                         }
                         TreeViewModel treemodel = new EditDcpTreeModel(dcp, editEvent.getSipPath());

                         CellTree tree = new CellTree(treemodel, null);

                         tree.setHeight("90%");
                         tree.setAnimationEnabled(true);
                         tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
                         tree.setDefaultNodeSize(50);

                         if (tree.getRootTreeNode().getChildCount() > 0) {
                             tree.getRootTreeNode().setChildOpen(0, editEvent.isSetOpen());
                         }

                         ScrollPanel previewPanel = new ScrollPanel();
                         previewPanel.setWidth("95%");
                         previewPanel.setHeight("100%");
                         previewPanel.add(tree);

                         display.getContentPanel().clear();
                         display.getContentPanel().add(previewPanel);

                         int currentHeight = display.getContentPanel().getOffsetHeight();
                         int windowBasedHeight = (int) (Window.getClientHeight()/3);

                         int height = (currentHeight>windowBasedHeight) ? currentHeight : windowBasedHeight;

                         display.getContentPanel().setHeight(height+"px");
                         if(dcp.getDeliverableUnits().length()>0){
                             for(int i =0;i<dcp.getDeliverableUnits().length();i++){
                                 title = dcp.getDeliverableUnits().get(i).getCoreMd().getTitle();
                                 abstrct = dcp.getDeliverableUnits().get(i).getAbstract();
                             }
                         }
                         display.getName().setText(title);
                         display.getAbstract().setText(abstrct);
                         sipPath = editEvent.getSipPath();
        			}

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}
            	});
               
                
            }
        };
        ROEditEvent.register(EditPresenter.EVENT_BUS, editHandler);
    }
}
