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

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.client.ui.LoginPopupPanel.UserLoginDetails;
import org.dataconservancy.dcs.access.client.ui.LoginPopupPanel.UserRegisterDetails;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.shared.*;

import java.util.Set;

public class LoginPresenter implements Presenter {

    Display display;
    UserLoginDetails userLoginDetails;
    UserRegisterDetails userRegisterDetails;
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

    static final VivoSparqlServiceAsync vivo =
            GWT.create(VivoSparqlService.class);
    public static final RegistryServiceAsync registryService =
            GWT.create(RegistryService.class);


    boolean login = false ;
//	Panel loginTab;

    Label errorLabel;
    Button googleLogin;
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String GOOGLE_SCOPE = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";

    private static final Auth AUTH = Auth.get();

    public interface Display {


        TabPanel getLoginPanel();
        Panel getLoginTab();
        Button getLoginButton();
        Button getRegisterButton();
        UserLoginDetails getUserLoginDetails();
        UserRegisterDetails getUserRegisterDetails();
        Label getError();
        Label getUserLabel();
        Button getGoogleLogin();
        void hide1();
        SuggestBox getSuggestBox();
        DisclosurePanel getDisclosurePanel();
        Grid getRegisterForm();

    }
    public LoginPresenter(Display view)
    {
        this.display = view;
    }
    @Override
    public void bind() {


//		loginTab = this.display.getLoginTab();
        errorLabel = this.display.getError();
        userNameLbl = this.display.getUserLabel();
        googleLogin = this.display.getGoogleLogin();
        userLoginDetails = display.getUserLoginDetails();
        userRegisterDetails = display.getUserRegisterDetails();
        Auth.export();

        this.display.getDisclosurePanel().addOpenHandler(new OpenHandler<DisclosurePanel>() {

            @Override
            public void onOpen(OpenEvent<DisclosurePanel> event) {
                vivo.getPeople(new AsyncCallback<Set<Person>>() {

                    @Override
                    public void onSuccess(Set<Person> result) {
                        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
                        for(Person person: result){
                            oracle.add(person.getFirstName()+" "+person.getLastName()+";"
                                    +person.getEmailAddress());
                        }
                        userRegisterDetails.suggestBox = new SuggestBox(oracle);
                        userRegisterDetails.suggestBox.ensureDebugId("cwSuggestBox");
                        display.getRegisterForm().setWidget(5, 1, userRegisterDetails.suggestBox);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO Auto-generated method stub

                    }
                });
            }
        });


        ClickHandler register =  new ClickHandler() {
            public void onClick(ClickEvent event) {


                AsyncCallback<String> cb =
                        new AsyncCallback<String>() {

                            public void onSuccess(String result) {
                                if(result.equalsIgnoreCase("success")){
                                    display.getDisclosurePanel().setOpen(false);
                                    errorLabel.setText("You have requested an account. Please wait for an administrator to provide you the necessary authorization.");
                                }
                                else
                                    userRegisterDetails.errorMessage.setText("Error: "+result+" Please try again.");

                            }

                            public void onFailure(Throwable error) {
                                new ErrorPopupPanel("Failed to register: "
                                        + error.getMessage()).show();
                            }


                        };

                user.register(userRegisterDetails.firstName.getText(), userRegisterDetails.lastName.getText(), userRegisterDetails.email.getText(),
                        userRegisterDetails.password.getText(), SeadApp.admins,
                        userRegisterDetails.suggestBox.getText(),//vivoId
                        cb);

            }
        };
        this.display.getRegisterButton().addClickHandler(register);

        final AsyncCallback<Authentication> authenticateCB =
                new AsyncCallback<Authentication>() {

                    public void onSuccess(Authentication result) {

                        if(result.authResult())
                        {
//                        	loginTab.clear();
                            display.hide1();
                            logoutLbl = Util.label("Logout", "LogoutButton");
                            ClickHandler logout = new ClickHandler() {

                                @Override
                                public void onClick(ClickEvent event) {
                                    //	lPanel.clear();
                                    AUTH.clearAllTokens();
                                    Label loginLabel = Util.label("Sign In/ Register", "LogoutButton");

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


                                    //	lPanel.add(logoutLbl);
                                    History.newItem("upload");
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    new ErrorPopupPanel("Error:"+caught.getMessage()).show();
                                }
                            });


                        }
                        else{
                            errorLabel.setText(result.getErrorMessage());
                            errorLabel.setStyleName("ErrorField");
                        }
                    }

                    public void onFailure(Throwable error) {
                        new ErrorPopupPanel("Error: "
                                + error.getMessage()).show();
                    }
                };


        this.display.getLoginButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                user.authenticate(deposit_endpoint, userLoginDetails.user_tb.getText(), userLoginDetails.pass_tb.getText(), authenticateCB);
            }
        });


        userLoginDetails.pass_tb.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
                    user.authenticate(deposit_endpoint, userLoginDetails.user_tb.getText(), userLoginDetails.pass_tb.getText(), authenticateCB);
            }
        } );

        userRegisterDetails.confirmPassword.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_TAB){
                    if (!userRegisterDetails.password.getText().equals(userRegisterDetails.confirmPassword.getText())){
                        userRegisterDetails.errorMessage.setText("Passwords should match.");
                        display.getRegisterButton().setEnabled(false);
                    }
                    else if(userRegisterDetails.password.getText().length()<6){
                        userRegisterDetails.errorMessage.setText("Please ensure password contains atleast 6 characters.");
                        display.getRegisterButton().setEnabled(false);
                    }
                    else{
                        userRegisterDetails.errorMessage.setText("");
                        display.getRegisterButton().setEnabled(true);
                    }
                }
            }
        } );


        googleLogin.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                AsyncCallback<GoogleDetails> clientDetailsCB =
                        new AsyncCallback<GoogleDetails>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                new ErrorPopupPanel("Unable to retrieve client id:"+caught.getMessage()).show();
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
                                        new ErrorPopupPanel("Error:" + caught.getMessage()).show();
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
        this.lPanel = logoutPanel;
        bind();
        mainContainer.add(this.display.getLoginPanel());
    }

}
