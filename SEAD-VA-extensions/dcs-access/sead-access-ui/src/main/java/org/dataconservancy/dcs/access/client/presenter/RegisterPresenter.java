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

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.client.view.RegisterView.Input;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.Event;

public class RegisterPresenter implements Presenter {

	Display display;
	UserServiceAsync user =
	            GWT.create(UserService.class);
	String lName;
	
	public interface Display{
		Button getRegisterButton();
		Panel getContentPanel();
		Input getInput();
	}
	
	public RegisterPresenter(Display view){
		this.display = view;
	}
	
	@Override
	public void bind() {
		
		 final Input formInput = this.display.getInput();
		 lName = "";
		 if(formInput.lastName.getText()!=null)
			 lName = formInput.lastName.getText();
		 this.display.getRegisterButton().addClickHandler(new ClickHandler() {
         	 public void onClick(ClickEvent event) {

         		AsyncCallback<String> cb =
                         new AsyncCallback<String>() {

                             public void onSuccess(String result) {
                                //emails can be sent only from the server side of GWT
                            	 if(result.equalsIgnoreCase("success"))
                            		 History.newItem("login");
                            	 else
                            		formInput.errorMessage.setText("Error: "+result+" Please try again.");
                             }

                             public void onFailure(Throwable error) {
                                 Window.alert("Failed to login: "
                                         + error.getMessage());
                             }

    						
                         };
               user.register(formInput.firstName.getText(), lName, formInput.email.getText(),
            		   formInput.password.getText(),formInput.confirmPassword.getText(),SeadApp.admins,
            		   cb);
    	     	
         	 }
          });

	}

	@Override
	public void display(Panel mainContainer, Panel facetContent, Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		
		bind();
		
		mainContainer.clear();
		facetContent.clear();
		
		mainContainer.add(this.display.getContentPanel());

	}

}
