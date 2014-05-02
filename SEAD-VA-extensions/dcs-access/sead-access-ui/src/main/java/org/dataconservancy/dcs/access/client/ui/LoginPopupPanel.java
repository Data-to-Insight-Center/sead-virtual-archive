package org.dataconservancy.dcs.access.client.ui;

import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.view.LoginView.UserDetails;

import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginPopupPanel extends PopupPanel implements org.dataconservancy.dcs.access.client.presenter.LoginPresenter.Display{

	
	private Label registerLabel;
	private Button loginButton;
	private Button googleLogin;
	//private TextBox userNameBox;
	//private TextBox passwordBox;
	private Label userLabel;
	private Label passwordLabel;
	private Grid loginForm; 
	
	UserDetails userDetails;
	
	public class UserDetails{
		public TextBox user_tb;
		public PasswordTextBox pass_tb;
	}
	
	
	public LoginPopupPanel() {
		super(true);
		this.setGlassEnabled(true);
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

		userDetails = new UserDetails();
		Label loginLabel = new Label("Login");
		loginLabel.setStyleName("loginLabelStyle");
		
		loginForm = new Grid(2,2);
		loginForm.setCellPadding(8);
		outerPanel.add(loginLabel);
		outerPanel.add(loginForm);
		
		HorizontalPanel innerPanel = new HorizontalPanel();
		innerPanel.setSpacing(10);
		googleLogin = new Button(" Google Sign On");
		googleLogin.setStyleName("loginButton");
		googleLogin.setWidth("150px");
		loginButton = new Button("Login");
		loginButton.setStyleName("loginButton");
		loginButton.setWidth("100px");
		
		innerPanel.add(googleLogin);
		innerPanel.add(loginButton);
		outerPanel.add(innerPanel);
		
		/*registerLabel = new Label("Signup1");
		outerPanel.add(registerLabel);*/
		
		Grid registerForm = new Grid(6, 2);
		registerForm.setCellPadding(6);
		createRegisterForm(registerForm);
		DisclosurePanel registerClosure = new DisclosurePanel("SignUp");
		registerClosure.setAnimationEnabled(true);
		registerClosure.setContent(registerForm);
		
		outerPanel.add(registerClosure);
		
		createLoginForm();
		
	}
	

	private void createRegisterForm(Grid registerForm) {
		// TODO Auto-generated method stub
		registerForm.setWidget(0, 0, new Label("First Name"));
		registerForm.setWidget(0, 1, new TextBox());
		registerForm.setWidget(1, 0, new Label("Last Name"));
		registerForm.setWidget(1, 1, new TextBox());
		registerForm.setWidget(2, 0, new Label("Email"));
		registerForm.setWidget(2, 1, new TextBox());
		registerForm.setWidget(3, 0, new Label("Password"));
		registerForm.setWidget(3, 1, new TextBox());
		registerForm.setWidget(4, 0, new Label("Confirm Password"));
		registerForm.setWidget(4, 1, new TextBox());
		registerLabel = new Label("Register");
		registerForm.setWidget(5, 1, registerLabel);
		
	}


	private void createLoginForm() {
		
		userLabel = new Label("Username");
		passwordLabel = new Label("Password");
		/*userLabel.setStyleName("labelStyle");
		passwordLabel.setStyleName("labelStyle");*/
		userDetails.user_tb = new TextBox();
		userDetails.pass_tb = new PasswordTextBox();
		loginForm.setWidget(0, 0, userLabel);
		loginForm.setWidget(0, 1, userDetails.user_tb);
		loginForm.setWidget(1, 0, passwordLabel);
		loginForm.setWidget(1, 1, userDetails.pass_tb);
		
	}


	public Label getRegisterLabel() {
		return registerLabel;
	}
	public void setRegisterLabel(Label registerLabel) {
		this.registerLabel = registerLabel;
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
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public UserDetails getUserDetails() {
		// TODO Auto-generated method stub
		return userDetails;
	}


	@Override
	public Label getError() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Button getGoogleLogin() {
		// TODO Auto-generated method stub
		return googleLogin;
	}

	@Override
	public void hide1() {
		// TODO Auto-generated method stub
		this.hide();
	}



}
	


