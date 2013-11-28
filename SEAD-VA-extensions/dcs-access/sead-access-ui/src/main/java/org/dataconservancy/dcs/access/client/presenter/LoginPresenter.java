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

import java.io.IOException;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.api.GoogleHelper;
import org.dataconservancy.dcs.access.client.api.GoogleHelperAsync;
import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.client.view.LoginView.UserDetails;
import org.dataconservancy.dcs.access.shared.Authentication;
import org.dataconservancy.dcs.access.shared.GoogleDetails;
import org.dataconservancy.dcs.access.shared.OAuthType;
import org.dataconservancy.dcs.access.shared.UserSession;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabPanel;

public class LoginPresenter implements Presenter {

	Display display;
	UserDetails ud;
	String deposit_endpoint ;
	String deposit_user ;
    String deposit_pass ;
    
    Label userNameLbl;
    Label logoutLbl;
	Panel lPanel;
 //   static final DepositService deposit =
   //         GWT.create(DepositService.class);
    		
    
    static final UserServiceAsync user =
            GWT.create(UserService.class);
    static final GoogleHelperAsync googleHelper =
            GWT.create(GoogleHelper.class);
	boolean login = false ;  
	Panel loginTab;
	
	Label errorLabel;
	Button googleLogin;
	private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
	private static final String GOOGLE_SCOPE = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
	    
	private static final Auth AUTH = Auth.get();
	  
	public interface Display {
		
	   
		TabPanel getLoginPanel();
		Panel getLoginTab();
		Button getLoginButton();
		Label getRegisterLabel();
		UserDetails getUserDetails();
		Label getError();
		Label getUserLabel();
		Button getGoogleLogin();
	   
	  }
	public LoginPresenter(Display view)
	{
		this.display = view;
	}
	@Override
	public void bind() {
		
		ud = this.display.getUserDetails();
		loginTab = this.display.getLoginTab();
		errorLabel = this.display.getError();
		userNameLbl = this.display.getUserLabel();
		googleLogin = this.display.getGoogleLogin();
		Auth.export();
		
		ClickHandler  register =  new ClickHandler() {
			public void onClick(ClickEvent event) {
				History.newItem("register");
			}
		};
		this.display.getRegisterLabel().addClickHandler(register);
		
		final AsyncCallback<Authentication> authenticateCB =
                  new AsyncCallback<Authentication>() {

                      public void onSuccess(Authentication result) {
                        if(result.authResult())
                        {
                        	loginTab.clear();
                        	logoutLbl = Util.label("Logout","LogoutButton");
                    		ClickHandler logout = new ClickHandler() {
                    			
                    			@Override
                    			public void onClick(ClickEvent event) {
                    				lPanel.clear();
                    				AUTH.clearAllTokens(); 
                    				Label loginLabel = Util.label("Sign In/ Register","LogoutButton");
                    			        
                    			        loginLabel.addClickHandler(new ClickHandler() {
                    						
                    						@Override
                    						public void onClick(ClickEvent event) {
                    							History.newItem("login");
                    						}
                    					});
                    			        
                    			    lPanel.add(loginLabel);
                    				History.newItem("logout");
                    			}
                    		}; 
                    		
                    		logoutLbl.addClickHandler(logout);
                    		lPanel.clear();
                    		user.checkSession(null,new AsyncCallback<UserSession>() {
								
								@Override
								public void onSuccess(UserSession result) {
									String displayName = "";
									if(result.getfName()!=null)
										displayName = result.getfName();
									if(result.getlName()!=null)
										displayName += " "+result.getlName();
								
									userNameLbl.setText(displayName);
									lPanel.add(userNameLbl);
									
		                    		lPanel.add(logoutLbl);
		                          	History.newItem("upload");
								}
								
								@Override
								public void onFailure(Throwable caught) {
									Window.alert(caught.getMessage());
								}
							});
                    		
                    		
                        }
                        else{
                          	errorLabel.setText(result.getErrorMessage());
                          	errorLabel.setStyleName("ErrorField");
                        }
                      }

                      public void onFailure(Throwable error) {
                          Window.alert("Failed to login: "
                                  + error.getMessage());
                      }
                  };
        
		
		this.display.getLoginButton().addClickHandler(new ClickHandler() {
				
	            public void onClick(ClickEvent arg0) {

	            	deposit_user = ud.user_tb.getText();
                    deposit_pass = ud.pass_tb.getText();
	                user.authenticate(deposit_endpoint, deposit_user, deposit_pass, authenticateCB);
                   
	            }
	        });
		
		googleLogin.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				AsyncCallback<GoogleDetails> clientDetailsCB =
				          new AsyncCallback<GoogleDetails>() {

							@Override
							public void onFailure(Throwable caught) {
								Window.alert("Unable to retrieve client id");
							}

							@Override
							public void onSuccess(GoogleDetails details) {
								final AuthRequest req = new AuthRequest(details.getGoogleAuthUrl(), details.getClientId())
					            .withScopes(GOOGLE_SCOPE);

							  AUTH.login(req, new Callback<String, Throwable>() {
					          @Override
					          public void onSuccess(String token) {
					            	user.authenticateOAuth(token, OAuthType.GOOGLE, SeadApp.admins, authenticateCB);
					            	
					          }

					          @Override
					          public void onFailure(Throwable caught) {
					            Window.alert("Error:\n" + caught.getMessage());
					          }
					        
					        });
						}
				};
				googleHelper.getClientId(clientDetailsCB);
			}
		});
	
	}

	
	@Override
	public void display(Panel mainContainer, Panel facetContent, Panel headerPanel, Panel logoutPanel, Panel notifcationPanel) {
		mainContainer.clear();
		facetContent.clear();
		lPanel = logoutPanel;
		bind();
		mainContainer.add(this.display.getLoginPanel());
	}

}
