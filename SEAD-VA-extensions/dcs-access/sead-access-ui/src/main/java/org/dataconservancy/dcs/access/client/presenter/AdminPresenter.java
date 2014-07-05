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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.RegistryService;
import org.dataconservancy.dcs.access.client.api.RegistryServiceAsync;
import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.RegistrationStatus;
import org.dataconservancy.dcs.access.shared.Role;

import java.util.ArrayList;
import java.util.List;

public class AdminPresenter implements Presenter {

    Grid userGrid;
    Display display;
    UserServiceAsync userService;
    RegistryServiceAsync registryService;
    Button saveButton;
    CheckBox[][] roleCheckBoxes;
    List<CheckBox> approvedList;


    public interface Display{
        Panel getUsersPanel();
        TabPanel getAdminPanel();
        Button getSaveButton();
    }

    public AdminPresenter(Display view){
        this.display = view;
    }


    @Override
    public void bind() {

        userService = GWT.create(UserService.class);
        registryService = GWT.create(RegistryService.class);
        saveButton = this.display.getSaveButton();
        approvedList = new ArrayList<CheckBox>();


        AsyncCallback<List<Role>> cbGetRoles =
                new AsyncCallback<List<Role>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onSuccess(final List<Role> roles) {
                        userGrid = new Grid(100,roles.size()+3);
                        final HTMLTable.CellFormatter formatter = userGrid.getCellFormatter();
                        userGrid.setWidth("100%");

                        userGrid.setWidget(0,0, Util.label("Name", "SubSectionHeader"));
                        formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

                        userGrid.setWidget(0,1, Util.label("Email", "SubSectionHeader"));
                        formatter.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);

                        int i = 2;
                        for(Role role:roles){
                            userGrid.setWidget(0,i, Util.label(role.getName(), "SubSectionHeader"));
                            formatter.setHorizontalAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER);
                            i++;
                        }
                        userGrid.setWidget(0,i, Util.label("Resgitration Status", "SubSectionHeader"));
                        formatter.setHorizontalAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER);

                        display.getUsersPanel().add(userGrid);

                        final int columnCount = i;


                        AsyncCallback<List<Person>> cb =
                                new AsyncCallback<List<Person>>() {

                                    public void onSuccess(final List<Person> userList) {

                                        int i=1;
                                        roleCheckBoxes = new CheckBox[userList.size()][roles.size()];
                                        for(Person user:userList)
                                        {
                                            userGrid.setWidget(i, 0, new Label(user.getFirstName()+" "+user.getLastName()));
                                            formatter.setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER);

                                            userGrid.setWidget(i,1 , new Label(user.getEmailAddress()));
                                            formatter.setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);


                                            //User approval status
                                            CheckBox approveCheck = new CheckBox();
                                            if(user.getRegistrationStatus() == RegistrationStatus.PENDING)
                                            {
                                                approveCheck.setValue(false);
                                                approveCheck.setText("Approve?");
                                                userGrid.setWidget(i, columnCount, approveCheck);

                                            }
                                            else if(user.getRegistrationStatus() == RegistrationStatus.APPROVED)
                                            {
                                                approveCheck.setValue(false);
                                                userGrid.setWidget(i, columnCount, new Label("Approved"));
                                            }
                                            formatter.setHorizontalAlignment(i,columnCount, HasHorizontalAlignment.ALIGN_CENTER);
                                            approvedList.add(approveCheck);

                                            //User Role

                                            int j = 2;
                                            for(Role role:roles){
                                                CheckBox userCheck = new CheckBox();
                                                userCheck.setName(role.getName());

                                                if(user.getRole() ==role)
                                                    userCheck.setValue(true);
                                                else
                                                    userCheck.setValue(false);


                                                userGrid.setWidget(i, j, userCheck);
                                                roleCheckBoxes[i-1][j-2] = userCheck;
                                                formatter.setHorizontalAlignment(i,j, HasHorizontalAlignment.ALIGN_CENTER);
                                                j++;
                                            }
                                            i++;
                                        }

                                        userGrid.setWidget(i+1, 5, saveButton);

                                        //add a save button
                                        saveButton.addClickHandler(new ClickHandler() {

                                            @Override
                                            public void onClick(ClickEvent event) {
                                                final List<Person> sendEmailList = new ArrayList<Person>();
                                                final List<Person> updatedUserList = new ArrayList<Person>();
                                                for(int i =0 ;i <userList.size(); i++){
                                                    for(int j = 0; j<roles.size(); j++){
                                                        if(roleCheckBoxes[i][j].getValue()){//checkbox is set, then that is the role
                                                            Person updatedPerson = userList.get(i);
                                                            updatedPerson.setRole(roles.get(j));
                                                            updatedUserList.add(updatedPerson);
                                                            break;
                                                        }
                                                    }
                                                }

                                                for(int i = 0; i<approvedList.size();i++)
                                                {
                                                    if(approvedList.get(i).getValue()){
                                                        Person newlyApprovedPerson = updatedUserList.get(i);
                                                        newlyApprovedPerson.setRegistrationStatus(RegistrationStatus.APPROVED);
                                                        sendEmailList.add(newlyApprovedPerson);
                                                        updatedUserList.set(i, newlyApprovedPerson);
                                                    }
                                                }

                                                //update all users in the database

                                                AsyncCallback<Void> callback =
                                                        new AsyncCallback<Void>() {

                                                            @Override
                                                            public void onFailure(
                                                                    Throwable caught) {
                                                                Window.alert("Could not update:" + caught.getMessage());
                                                            }
                                                            @Override
                                                            public void onSuccess(Void result) {
                                                                saveButton.setText("Saved");
                                                                saveButton.setEnabled(false);
                                                            }
                                                        };
                                                userService.updateAllUsers(updatedUserList,sendEmailList, SeadApp.registryUrl, callback);


                                            }
                                        });
                                    }

                                    public void onFailure(Throwable error) {
                                        new ErrorPopupPanel("Failed to retrieve users: "
                                                + error.getMessage()).show();

                                    }
                                };

                        userService.getAllUsers(cb);
                    }
                };
        userService.getAllRoles(cbGetRoles);
    }

    @Override
    public void display(Panel mainContainer, Panel facetContent, Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
        mainContainer.clear();
        facetContent.clear();
        bind();

        mainContainer.add(this.display.getAdminPanel());

    }

}
