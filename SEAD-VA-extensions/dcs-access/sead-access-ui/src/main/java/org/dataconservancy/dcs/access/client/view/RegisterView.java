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

package org.dataconservancy.dcs.access.client.view;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

public class RegisterView extends Composite implements org.dataconservancy.dcs.access.client.presenter.RegisterPresenter.Display{
	
	Button register;
	FlowPanel contentPanel;
	Input input;
	
	public class Input{
		public TextBox firstName;
		public TextBox lastName;
		public TextBox email;
		public PasswordTextBox password;
		public PasswordTextBox confirmPassword;
		public Label errorMessage;
	}
	
	public RegisterView(){

		contentPanel = new FlowPanel();
     	
     	FlexTable table =
                  Util.createTable("First Name: *", "Last Name:", "Email: *", "Password: *","Confirm Password: *");

		TextBox end_tb = new TextBox();
		end_tb.setWidth("40em");
		
		TextBox fName_tb = new TextBox();
		TextBox lName_tb = new TextBox();
		TextBox email_tb = new TextBox();
		PasswordTextBox pass_tb = new PasswordTextBox();
		PasswordTextBox confirm_pass_tb = new PasswordTextBox();
		
		end_tb.setText(SeadApp.deposit_endpoint);
		 
		
		Util.addColumn(table, fName_tb,lName_tb, email_tb, pass_tb,confirm_pass_tb);
		Util.addColumn(table, new Label(""),new Label(""), new Label(""), new Label("(Password should contain atleast 6 characters.)"),new Label(""));
		
		contentPanel.add(table);
		
		register = new Button("Register");
		contentPanel.add(register);
		
		Label errorLabel = Util.label("", "ErrorField");
		contentPanel.add(errorLabel);
		
		input =new Input();
		input.firstName = fName_tb;
		input.lastName = lName_tb;
		input.email = email_tb;
		input.password = pass_tb;
		input.confirmPassword = confirm_pass_tb;
		input.errorMessage = errorLabel;
      
	}

	@Override
	public Button getRegisterButton() {
		return register;
	}

	@Override
	public Panel getContentPanel() {
		return contentPanel;
	}

	@Override
	public Input getInput() {
		return input;
	}
	

}
