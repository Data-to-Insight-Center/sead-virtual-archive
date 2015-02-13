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
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
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
import org.dataconservancy.dcs.access.client.ui.NotificationPopupPanel;
import org.dataconservancy.dcs.access.client.ui.StatusPopupPanel;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.shared.Person;
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


    public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
    public static final DepositServiceAsync depositService = GWT.create(DepositService.class);
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


                                    final Grid curatorData =  new Grid(allCOs.size()+1, 6);
                                    curatorData.setStyleName("curatorTable");
                                    curatorData.setWidth(Window.getClientWidth()/2+"px");

                                    curatorData.setWidget(0, 0, Util.label("Research Object Name", "SubsectionHeader"));
                                    curatorData.setWidget(0, 1, Util.label("Date Last Modified", "SubsectionHeader"));
                                    curatorData.setWidget(0, 2, Util.label("Current Assignment", "SubsectionHeader"));
                                    curatorData.setWidget(0, 3, Util.label("Modify Assignment", "SubsectionHeader"));
                                    curatorData.setWidget(0, 4, Util.label("Edit", "SubsectionHeader"));
                                    curatorData.setWidget(0, 5, Util.label("Publish", "SubsectionHeader"));

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
                                        final Button edit = new Button("Edit");
                                        edit.setEnabled(false);
                                        final Button publish = new Button("Publish");
                                        publish.setEnabled(false);
                                        final int index = i;

                                        final Button reAssign = new Button("Re-assign to me");
                                        final Button unAssign = new Button("Unassign");

                                        final ClickHandler publishHandler = new ClickHandler() {

                                            @Override
                                            public void onClick(ClickEvent event) {
                                                registryService.isObsolete(ro.getIdentifier(), SeadApp.registryUrl, new AsyncCallback<Boolean>() {

                                                    @Override
                                                    public void onSuccess(Boolean isObsolete) {
                                                        if(isObsolete){
                                                            ErrorPopupPanel popupPanel = new ErrorPopupPanel("Sorry, this RO was ingested by another curator. Please select another RO to work on.");
                                                            popupPanel.show();
                                                            popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {

                                                                @Override
                                                                public void onClose(CloseEvent<PopupPanel> event) {
                                                                    History.newItem(SeadState.CURATOR.toToken("refresh"));
                                                                }
                                                            });
                                                        }
                                                        else{
                                                            registryService.getRelation(ro.getIdentifier(), SeadApp.registryUrl, "curatedBy", new AsyncCallback<String>() {
                                                                @Override
                                                                public void onSuccess(String agentId) {
                                                                    if(agentId.equals(session.getRegistryId()))
                                                                        submitSip(ro.getIdentifier(), publish, edit, index, false, curatorData, unAssign);
                                                                    else{
                                                                        ErrorPopupPanel popupPanel = new ErrorPopupPanel("Sorry, this RO was reassigned to someone else. Please select another RO to work on.");
                                                                        popupPanel.show();
                                                                        popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {

                                                                            @Override
                                                                            public void onClose(CloseEvent<PopupPanel> event) {
                                                                                History.newItem(SeadState.CURATOR.toToken("refresh"));
                                                                            }
                                                                        });

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Throwable caught) {
                                                                    new ErrorPopupPanel("Error:" + caught.getMessage()).show();
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        new ErrorPopupPanel("Error:" + caught.getMessage()).show();
                                                    }
                                                });

                                            }
                                        };



                                        final ClickHandler editClickHandler = new ClickHandler() {

                                            @Override
                                            public void onClick(ClickEvent event) {
                                                registryService.isObsolete(ro.getIdentifier(), SeadApp.registryUrl, new AsyncCallback<Boolean>() {

                                                    @Override
                                                    public void onSuccess(Boolean isObsolete) {
                                                        if(isObsolete){
                                                            ErrorPopupPanel popupPanel = new ErrorPopupPanel("Sorry, this RO was ingested by another curator. Please select another RO to work on.");
                                                            popupPanel.show();
                                                            popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {

                                                                @Override
                                                                public void onClose(CloseEvent<PopupPanel> event) {
                                                                    History.newItem(SeadState.CURATOR.toToken("refresh"));
                                                                }
                                                            });
                                                        }
                                                        else{
                                                            registryService.getRelation(ro.getIdentifier(), SeadApp.registryUrl, "curatedBy", new AsyncCallback<String>() {

                                                                @Override
                                                                public void onSuccess(String agentId) {
                                                                    if(agentId.equals(session.getRegistryId()))
                                                                        History.newItem(SeadState.EDIT.toToken(ro.getIdentifier()));
                                                                    else{
                                                                        new ErrorPopupPanel("Sorry, this RO was reassigned to someone else. Please select another RO to work on.").show();
                                                                        History.newItem(SeadState.CURATOR.toToken("refresh"));

                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Throwable caught) {
                                                                    new ErrorPopupPanel("Error:" + caught.getMessage()).show();
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        new ErrorPopupPanel("Error:" + caught.getMessage()).show();
                                                    }
                                                });
                                            }
                                        };

                                        publish.addClickHandler(publishHandler);
                                        edit.addClickHandler(editClickHandler);

                                        final Button assign = new Button("Assign to me");
                                        ClickHandler unAssignHandler = new ClickHandler() {

                                            @Override
                                            public void onClick(ClickEvent event) {
                                                registryService.unassignFromAgent(ro.getIdentifier(), session.getRegistryId()
                                                        , SeadApp.registryUrl, new AsyncCallback<Boolean>() {

                                                    @Override
                                                    public void onSuccess(Boolean result) {
                                                        edit.setEnabled(false);
                                                        publish.setEnabled(false);
                                                        publish.removeStyleName("grayButton");
                                                        publish.setStyleName("gwt-Button");
                                                        curatorData.setWidget(index, 2, new Label("No one"));
                                                        curatorData.setWidget(index, 3, assign);
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        unAssign.setText("Failed");
                                                        unAssign.setEnabled(false);
                                                    }
                                                });
                                            }
                                        };
                                        unAssign.addClickHandler(unAssignHandler);


                                        assign.addClickHandler(new ClickHandler() {
                                            @Override
                                            public void onClick(ClickEvent event) {
                                                registryService.assignToAgent(ro.getIdentifier(), session.getRegistryId()
                                                        , SeadApp.registryUrl, new AsyncCallback<Boolean>() {

                                                    @Override
                                                    public void onSuccess(Boolean result) {
                                                        curatorData.setWidget(index, 2, Util.label("You", "greenFont"));
                                                        curatorData.setWidget(index, 3, unAssign);
                                                        assign.setEnabled(false);
                                                        edit.setEnabled(true);
                                                        publish.setEnabled(true);
                                                        publish.setStyleName("grayButton");
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        assign.setText("Failed");
                                                        assign.setEnabled(false);
                                                    }
                                                });
                                            }
                                        });


                                        reAssign.addClickHandler(new ClickHandler() {

                                            @Override
                                            public void onClick(ClickEvent event) {
                                                registryService.assignToAgent(ro.getIdentifier(), session.getRegistryId()
                                                        , SeadApp.registryUrl, new AsyncCallback<Boolean>() {

                                                    @Override
                                                    public void onSuccess(Boolean result) {
                                                        curatorData.setWidget(index, 2, Util.label("You", "greenFont"));
                                                        curatorData.setWidget(index, 3, unAssign);
                                                        edit.setEnabled(true);
                                                        publish.setEnabled(true);
                                                        publish.setStyleName("grayButton");
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        reAssign.setText("Failed");
                                                        reAssign.setEnabled(false);
                                                    }
                                                });
                                            }
                                        });



                                        if(ro.getAgentId()==null)
                                        {
                                            curatorData.setWidget(i, 2, new Label("No one"));
                                            curatorData.setWidget(i, 3, assign);
                                        }
                                        else if(ro.getAgentId().trim().equalsIgnoreCase(session.getRegistryId().trim())){
                                            curatorData.setWidget(i, 2, Util.label("You", "greenFont"));
                                            curatorData.setWidget(i, 3, unAssign);
                                            edit.setEnabled(true);
                                            publish.setEnabled(true);
                                            publish.setStyleName("grayButton");
                                        }
                                        else if(!ro.getAgentId().equalsIgnoreCase(session.getRegistryId())){

                                            userService.getUser(ro.getAgentId(), new AsyncCallback<Person>() {

                                                @Override
                                                public void onSuccess(Person user) {
                                                    curatorData.setWidget(index, 2, Util.label(user.getFirstName()+" "+user.getLastName(), "greenFont"));

                                                    curatorData.setWidget(index, 3, reAssign);

                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    // TODO Auto-generated method stub

                                                }
                                            });

                                        }

                                        curatorData.setWidget(i, 4, edit);


                                        curatorData.getRowFormatter().setStyleName(i, "CuratorRow");
                                        curatorData.setWidget(i, 5, publish);

                                        i++;
                                    }
                                    String institution = repository;
                                    if(repository.contains("IU"))
                                        institution = "Indiana University";
                                    display.getPublishContainer().clear();
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
        
        mainContainer.addStyleName("Border");

        mainContainer.add(this.display.getPublishContainer());

    }


    private void submitSip(final String datasetId, final Button publish, final Button edit, final int index, final boolean repub,final Grid curatorData, final Button unAssign){
        final NotificationPopupPanel startupPanel = new NotificationPopupPanel("Retrieving Research Object from registry", false);
        startupPanel.show();
         registryService.getRO(datasetId, SeadApp.roUrl,
                  new AsyncCallback<String>() {

                      @Override
                      public void onFailure(Throwable caught) {
                          new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                      }

                      @Override
                      public void onSuccess(String resultStr) {
                            String[] tempString = resultStr.split(";");
                            final String sipPath = tempString[tempString.length-1].split("<")[0].replace("_0.xml", "");
                            mediciService.generateWfInstanceId(new AsyncCallback<String>() {

                              @Override
                              public void onSuccess(String wfInstanceId) {
                                  //Open a status panel that self queries the database for changes

  //								WfEventRefresherPanel eventRefresher = new WfEventRefresherPanel(submitterId, wfInstanceId);
  //								eventRefresher.show();
                                  if(startupPanel.isShowing())
                                      startupPanel.hide();

                                  final NotificationPopupPanel popupPanel = new NotificationPopupPanel("Starting Publication workflow. Please wait for your submission to complete.", false);
                                  popupPanel.show();
                                  mediciService.submitMultipleSips(SeadApp.deposit_endpoint + "sip",
                                          datasetId,
                                          null,
                                          sipPath,
                                          wfInstanceId,
                                          null,
                                          0, n, "", "", false, GWT.getModuleBaseURL(), SeadApp.tmpHome,false, repub,
                                          new AsyncCallback<String>() {

                                              @Override
                                              public void onSuccess(final String statusUrl) {
                                                  depositService.isSuccessful(statusUrl, new AsyncCallback<Boolean>() {

                                                      @Override
                                                      public void onSuccess(Boolean result) {
                                                          l=-1;
                                                          if(popupPanel.isShowing())
                                                              popupPanel.hide();
                                                          NotificationPopupPanel finishPopPanel;
                                                          if(result){
                                                                finishPopPanel = new NotificationPopupPanel("Workflow Completed Succesfully", "done");
                                                              registryService.makeObsolete(datasetId,
                                                                      SeadApp.roUrl, new AsyncCallback<Boolean>() {

                                                                  @Override
                                                                  public void onSuccess(Boolean updated) {
                                                                      registryService.getRelation(datasetId, SeadApp.registryUrl, "publisher", new AsyncCallback<String>() {

                                                                          @Override
                                                                          public void onSuccess(final String agentId) {
                                                                              depositService.getResearchObjectId(statusUrl, new AsyncCallback<String>() {

                                                                                  @Override
                                                                                  public void onSuccess(final String roId) {
                                                                                      userService.emailResearcher(agentId,
                                                                                              GWT.getHostPageBaseURL()+
  //																							GWT.getModuleName().replace("sead_access","") +
                                                                                              "#" + SeadState.ENTITY.toToken(roId),new AsyncCallback<Boolean>() {

                                                                                          @Override
                                                                                          public void onSuccess(Boolean result) {
                                                                                              //Link CO (datasetId) and PO (roId) in Komadu now

                                                                                          //	registryService.trackRevision(datasetId, roId, SeadApp.roUrl, new AsyncCallback<Boolean>() {

                                                                                              registryService.assignToSubmitter(roId, agentId, SeadApp.registryUrl, new AsyncCallback<Boolean>() {

                                                                                                  @Override
                                                                                                  public void onSuccess(Boolean result) {
                                                                                                      publish.removeStyleName("grayButton");
                                                                                                      publish.setStyleName("gwt-Button");
                                                                                                      publish.setText("Published");
                                                                                                      publish.setEnabled(false);
                                                                                                      unAssign.setEnabled(false);
                                                                                                      edit.setEnabled(false);
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
                                                                                  public void onFailure(Throwable caught) {
                                                                                      new ErrorPopupPanel("Error:"+caught.getMessage()).show();

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
                                                                      new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                                                                  }
                                                              });
                                                          }
                                                          else{
                                                              finishPopPanel = new NotificationPopupPanel("Publish workflow did not go through. " +
                                                                      "Please try again when the repository load is lesser.", "failed");
                                                              if(!repub){
                                                                  Button republish = new Button("Publish to SDA");
                                                                  republish.setStyleName("grayButton");
                                                                  curatorData.setWidget(index, 5, republish);
                                                                  republish.addClickHandler(new ClickHandler() {
                                                                      @Override
                                                                      public void onClick(ClickEvent event) {
                                                                          submitSip(datasetId, publish, edit, index, true, curatorData, unAssign);
                                                                      }
                                                                  });
                                                              }
                                                              else{
                                                                  publish.setEnabled(false);
                                                                  publish.removeStyleName("grayButton");
                                                                  publish.setStyleName("gwt-Button");
                                                                  publish.setText("Published");
                                                              }
                                                              edit.setEnabled(false);
                                                          }
                                                          finishPopPanel.show();

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
                              public void onFailure(Throwable caught) {
                                  new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                              }
                          });
                      }
              });
    }
}
