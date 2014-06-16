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

package org.dataconservancy.dcs.access.client.ui;

import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import org.dataconservancy.dcs.access.client.SeadState;

public class LoginPopupPanel extends PopupPanel implements org.dataconservancy.dcs.access.client.presenter.LoginPresenter.Display{

	
	private Button registerButton;
	private Button loginButton;
	private Button googleLogin;
	//private TextBox userNameBox;
	//private TextBox passwordBox;
	private Label userLabel;
	private Label passwordLabel;
	private Label errorMessage;
	
	private Grid loginForm;
	
	
	UserLoginDetails userLoginDetails;
	UserRegisterDetails userRegisterDetails;
	DisclosurePanel registerClosure;
	SuggestBox suggestBox;
	Grid registerForm;
	
	public class UserLoginDetails{
		public TextBox user_tb;
		public PasswordTextBox pass_tb;
		
	}
	
	public class UserRegisterDetails{
		public TextBox firstName;
		public TextBox lastName;
		public TextBox email;
		public PasswordTextBox password;
		public PasswordTextBox confirmPassword;
		public Label errorMessage;
		public SuggestBox suggestBox;
	}
	
	
	
	public LoginPopupPanel() {
		super(true);
		this.setGlassEnabled(true);
		this.addCloseHandler(new CloseHandler<PopupPanel>() {
			
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				History.newItem(SeadState.HOME.toToken());
			}
		});
		this.show();
		this.setStyleName("loginPopupContainer");
		//setStyleName(getContainerElement(), "popupContent");
		this.setPopupPosition(Window.WINDOW_WIDTH/3, Window.WINDOW_HEIGHT/4);
		VerticalPanel outerPanel = new VerticalPanel();
		//outerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		outerPanel.setSpacing(10);
		outerPanel.setStyleName("loginPopupContainer");
		setWidget(outerPanel);
		outerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

		userLoginDetails = new UserLoginDetails();
		userRegisterDetails = new UserRegisterDetails();
		Label loginLabel = new Label("Login");
		loginLabel.setStyleName("loginLabelStyle");
		
		loginForm = new Grid(2,2);
		loginForm.setCellPadding(8);
		outerPanel.add(loginLabel);
		errorMessage = new Label();
		errorMessage.setStyleName("greenFont");
		outerPanel.add(errorMessage);
		outerPanel.add(loginForm);
		
		HorizontalPanel innerPanel = new HorizontalPanel();
		innerPanel.setSpacing(10);
		googleLogin = new Button(" Google Sign On");
		googleLogin.setStyleName("popupLoginButton");
		googleLogin.setWidth("150px");
		googleLogin.setHeight("40px");
		loginButton = new Button("Login");
		loginButton.setStyleName("popupLoginButton");
		loginButton.setWidth("100px");
		loginButton.setHeight("40px");
		
		innerPanel.add(loginButton);
		
		innerPanel.add(googleLogin);
		
		outerPanel.add(innerPanel);
		
		registerForm = new Grid(8, 2);
		registerForm.setCellPadding(6);
		createRegisterForm();
		registerClosure = new DisclosurePanel("SignUp");
		registerClosure.setAnimationEnabled(true);
		registerClosure.setContent(registerForm);
		outerPanel.add(registerClosure);	
		createLoginForm();
	}
	

	private void createRegisterForm() {
		userRegisterDetails.firstName = new TextBox();
		userRegisterDetails.lastName = new TextBox();
		userRegisterDetails.email = new TextBox();
		userRegisterDetails.password = new PasswordTextBox();
		userRegisterDetails.confirmPassword = new PasswordTextBox();
		userRegisterDetails.errorMessage = new Label();

		registerForm.setWidget(0, 0, new Label("First Name"));
		registerForm.setWidget(0, 1, userRegisterDetails.firstName);
		registerForm.setWidget(1, 0, new Label("Last Name"));
		registerForm.setWidget(1, 1, userRegisterDetails.lastName);
		registerForm.setWidget(2, 0, new Label("Email"));
		registerForm.setWidget(2, 1, userRegisterDetails.email);
		registerForm.setWidget(3, 0, new Label("Password"));
		registerForm.setWidget(3, 1, userRegisterDetails.password);
		registerForm.setWidget(4, 0, new Label("Confirm Password"));
		registerForm.setWidget(4, 1, userRegisterDetails.confirmPassword);
		registerForm.setWidget(5, 0, userRegisterDetails.errorMessage);
		registerForm.setWidget(6, 0, new Label("VIVO ID"));
	    
		registerButton = new Button("Register");
		registerButton.setWidth("100px");
		registerButton.setHeight("40px");
		registerButton.setStyleName("popupLoginButton");
		registerForm.setWidget(7, 1, registerButton);
		
	}


	private void createLoginForm() {
		
		userLabel = new Label("Username");
		passwordLabel = new Label("Password");
		/*userLabel.setStyleName("labelStyle");
		passwordLabel.setStyleName("labelStyle");*/
		userLoginDetails.user_tb = new TextBox();
		userLoginDetails.pass_tb = new PasswordTextBox();
		loginForm.setWidget(0, 0, userLabel);
		loginForm.setWidget(0, 1, userLoginDetails.user_tb);
		loginForm.setWidget(1, 0, passwordLabel);
		loginForm.setWidget(1, 1, userLoginDetails.pass_tb);
	}


	public Button getRegisterButton() {
		return registerButton;
	}
	public void setRegisterButton(Button registerButton) {
		this.registerButton = registerButton;
	}
	public Button getLoginButton() {
		return loginButton;
	}
	public void setLoginButton(Button loginButton) {
		this.loginButton = loginButton;
	}
	public Button getGoogleLoginButton() {
		return googleLogin;
	}
	public void setGoogleLoginButton(Button googleLoginButton) {
		this.googleLogin = googleLoginButton;
	}
	public Label getUserLabel() {
		return userLabel;
	}
	public void setUserLabel(Label userLabel) {
		this.userLabel = userLabel;
	}
	public Label getPasswordLabel() {
		return passwordLabel;
	}
	public void setPasswordLabel(Label passwordLabel) {
		this.passwordLabel = passwordLabel;
	}
	public Grid getLoginForm() {
		return loginForm;
	}
	public void setLoginForm(Grid loginForm) {
		this.loginForm = loginForm;
	}


	@Override
	public TabPanel getLoginPanel() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Panel getLoginTab() {
		return null;
	}


	@Override
	public UserLoginDetails getUserLoginDetails() {
		return userLoginDetails;
	}
	
	@Override
	public UserRegisterDetails getUserRegisterDetails() {
		return userRegisterDetails;
	}


	@Override
	public Label getError() {
		return errorMessage;
	}


	@Override
	public Button getGoogleLogin() {
		return googleLogin;
	}

	@Override
	public void hide1() {
		this.hide();
	}
	
	@Override
	public SuggestBox getSuggestBox(){
		return this.suggestBox;
	}
	
	@Override
	public DisclosurePanel getDisclosurePanel(){
		return this.registerClosure;
	}
	@Override
	public Grid getRegisterForm(){
		return this.registerForm;
	}
}
	


