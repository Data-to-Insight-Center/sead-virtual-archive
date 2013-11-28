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
import java.util.List;

import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.shared.Person;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.RegistrationStatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminPresenter implements Presenter {

	Grid userGrid;
	Display display;
	UserServiceAsync userService;
	Button saveButton;
	
	
	
	
	public interface Display{
		Grid getUserGrid();
		TabPanel getAdminPanel();
		Button getSaveButton();
	}
	
	public AdminPresenter(Display view){
		this.display = view;
	}
	
	List<CheckBox> adminCheckList;
	List<CheckBox> userCheckList;
	List<CheckBox> nonseadCheckList;
	List<CheckBox> approvedList;
	List<Person> changedUserList;
	List<Person> sendEmailList;
	
	@Override
	public void bind() {
		
		userService = GWT.create(UserService.class);
		
		userGrid = this.display.getUserGrid();
		
		saveButton = this.display.getSaveButton();
		
		changedUserList = new ArrayList<Person>();
		sendEmailList = new ArrayList<Person>();
		adminCheckList = new ArrayList<CheckBox>();
		userCheckList = new ArrayList<CheckBox>();
		nonseadCheckList = new ArrayList<CheckBox>();
		approvedList = new ArrayList<CheckBox>();
		
		 AsyncCallback<List<Person>> cb =
                 new AsyncCallback<List<Person>>() {

                     public void onSuccess(final List<Person> userList) {
                    	 
                    	int i=1;
                 		
                 		for(Person user:userList)
                 		{
                 			userGrid.setWidget(i, 0, new Label(user.getFirstName()+" "+user.getLastName()));
                 			userGrid.setWidget(i,1 , new Label(user.getEmailAddress()));
                 			
                 			
                 			//User approval status
                 			CheckBox approveCheck = new CheckBox();
                 			if(user.getRegistrationStatus() == null)
                 			{	approveCheck.setValue(true);
                 				userGrid.setWidget(i, 5, approveCheck);
                 			}
                 			else if(user.getRegistrationStatus() == RegistrationStatus.PENDING)
                 			{	
                 				approveCheck.setValue(false);
                 				approveCheck.setText("Approve?");
                 				userGrid.setWidget(i, 5, approveCheck);
                 				
                 			}
                 			else if(user.getRegistrationStatus() == RegistrationStatus.APPROVED)
                 			{	
                 				approveCheck.setValue(true);
                 				userGrid.setWidget(i, 5, new Label("Approved"));
                 			}
                 			approvedList.add(approveCheck);
                 			
                 			
                 			//User Role
                 			
                 			CheckBox userCheck = new CheckBox();
                 			userCheck.setName(Role.ROLE_USER.getName());
                 			
                 			if(user.getRole() ==Role.ROLE_USER)
                 				userCheck.setValue(true);

                 			else 
                 				userCheck.setValue(false);
                 			userCheckList.add(userCheck);
                 			userGrid.setWidget(i, 2, userCheck);
                 			
                 			CheckBox nonseadUserCheck = new CheckBox();
                 			nonseadUserCheck.setName(Role.ROLE_NONSEADUSER.getName());
                 			if(user.getRole() ==Role.ROLE_NONSEADUSER)
                 				nonseadUserCheck.setValue(true);
                 			else 
                 				nonseadUserCheck.setValue(false);
                 			nonseadCheckList.add(nonseadUserCheck);
                 			userGrid.setWidget(i, 3, nonseadUserCheck);
                 			
                 			CheckBox adminCheck = new CheckBox();
                 			adminCheck.setName(Role.ROLE_ADMIN.getName());
                 			if(user.getRole() ==Role.ROLE_ADMIN)
                 				adminCheck.setValue(true);
                 			else 
                 				adminCheck.setValue(false);
                 			adminCheckList.add(adminCheck);
                 			userGrid.setWidget(i, 4, adminCheck);
                 			
                 			i++;
                 		}
                 		
                 		userGrid.setWidget(i+1, 5, saveButton);
                 		
                 		//add a save button
                 		saveButton.addClickHandler(new ClickHandler() {
							
							@Override
							public void onClick(ClickEvent event) {
								//update all the changes to all the persons on the page
								changedUserList = new ArrayList<Person>(userList);
								int i =0 ;
								for(CheckBox userCheck: userCheckList)
								{
									if(userCheck.getValue())
										changedUserList.get(i).setRole(Role.fromString(userCheck.getName()));
									else if(adminCheckList.get(i).getValue())
										changedUserList.get(i).setRole(Role.fromString(adminCheckList.get(i).getName()));
									else if(nonseadCheckList.get(i).getValue())
										changedUserList.get(i).setRole(Role.fromString(nonseadCheckList.get(i).getName()));
									else
										changedUserList.get(i).setRole(null);
									i++;
								}
								
								i=0;
								for(CheckBox approveCheck: approvedList)
								{
									if(approveCheck.getValue()){
										if(changedUserList.get(i).getRegistrationStatus()==RegistrationStatus.PENDING)
										{
											//Send email 
											sendEmailList.add(changedUserList.get(i));
										}
										changedUserList.get(i).setRegistrationStatus(RegistrationStatus.APPROVED);
									}
									else
										changedUserList.get(i).setRegistrationStatus(RegistrationStatus.PENDING);
									i++;
								}
								
								
								
								//update all users in the database
								
								 AsyncCallback<Void> callback =
						                 new AsyncCallback<Void>() {

											@Override
											public void onFailure(
													Throwable caught) {
												// TODO Auto-generated method stub							
											}
											@Override
											public void onSuccess(Void result) {
												History.newItem("admin");											
 										}
								 };
								userService.updateAllUsers(changedUserList,sendEmailList, callback);
								
								
							}
						});
                     }

                     public void onFailure(Throwable error) {
                         Window.alert("Failed to retrieve users: "
                                 + error.getMessage());
                          
                     }
                 };

                 userService.getAllUsers(cb);
	}

	@Override
	public void display(Panel mainContainer, Panel facetContent, Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		mainContainer.clear();
		facetContent.clear();
		bind();
		
		mainContainer.add(this.display.getAdminPanel());

	}

}
