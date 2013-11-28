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
import org.dataconservancy.dcs.access.client.upload.DepositConfig;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;

public class LoginView extends Composite implements org.dataconservancy.dcs.access.client.presenter.LoginPresenter.Display{
	
	TabPanel loginPanel;
	Panel loginTab;
	Button login;
	Label userNameLbl;
	Label regsiterLbl;
	Label error;
	Button googleLogin;
	
	String deposit_user = "";
	String deposit_pass = "";
	
	UserDetails userDetails;
	
	public class UserDetails{
		public TextBox user_tb;
		public PasswordTextBox pass_tb;
	}
  

	public LoginView()//DepositConfig depositConfig)
	{
		loginPanel = new TabPanel();
		loginPanel.setWidth("100%");
		loginPanel.setHeight("100%");
		loginTab = new FlowPanel();
		
		loginPanel.add(loginTab,"Login");
		loginPanel.selectTab(0);
	
		Label explain =
            new Label("The SEAD Ingest UI allows simple SIPs to be created and uploaded.");
		explain.setStylePrimaryName("Explanation");
		loginTab.add(explain);

		error =
           // Util.label("","ErrorField");
				new Label("");
		loginTab.add(error);
		
		userNameLbl = new Label();
		regsiterLbl =
				Util.label("Register New User","SimpleButton");

    
	    FlexTable tempTable = Util.createTable("");
	    Util.addColumn(tempTable,regsiterLbl);
	    loginTab.add(tempTable);
	    userDetails = new UserDetails();
	 //   userDetails.depositConfig = depositConfig;
	  //  if (depositConfig == null) {
    	
	    	loginTab.add(new Label("Login"));

	        final FlexTable table =
	                Util.createTable("User:", "Pass:");
	
	        userDetails.user_tb = new TextBox();
	        userDetails.pass_tb = new PasswordTextBox();
	
	        userDetails.user_tb.setText(deposit_user);
	        userDetails.pass_tb.setText(deposit_pass);
	
	        Util.addColumn(table, userDetails.user_tb, userDetails.pass_tb);
	
	        loginTab.add(table);

	        login = new Button("Login");
	        login.setStyleName("SimpleGreenButton");
	
	        loginTab.add(login);
	        loginTab.add(new Label("Or"));
	        googleLogin = new Button("Use Google Login");
	        googleLogin.setStyleName("SimpleGreenButton");
	        loginTab.add(googleLogin);
	//    } else {
	    	//Application.main.remove(Application.ingestPanel);
	  //  	loginTab.add(new Label("Logged in to: " + deposit_endpoint));
	    	/*loginTab.add(Application.contentTab2,"Files");
	    	loginTab.add(Application.contentTab3,"Deliverables");
	    	Application.ingestPanel.add(Application.contentTab4,"Submission");
	        //Application.main.add(Application.ingestPanel);//,DockPanel.CENTER);
	    	centerPanel.add(Application.ingestPanel);//,DockPanel.CENTER);*/
	   // }

	}


	@Override
	public TabPanel getLoginPanel() {
		return loginPanel;
	}

	@Override
	public Button getLoginButton() {
		return login;
	}

	@Override
	public Label getRegisterLabel() {
		return regsiterLbl;
	}
	
	@Override
	public Label getUserLabel() {
		return userNameLbl;
	}

	@Override
	public UserDetails getUserDetails() {
		return userDetails;
	}


	@Override
	public Panel getLoginTab() {
		return loginTab;
	}


	@Override
	public Label getError() {
		return error;
	}
	
	@Override
	public Button getGoogleLogin() {
		return googleLogin;
	}
}
