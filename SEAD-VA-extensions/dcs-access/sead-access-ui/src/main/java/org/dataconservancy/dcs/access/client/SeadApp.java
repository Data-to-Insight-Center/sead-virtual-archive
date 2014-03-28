/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.access.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.client.event.SearchEvent;
import org.dataconservancy.dcs.access.client.model.SearchInput;
import org.dataconservancy.dcs.access.client.presenter.AdminPresenter;
import org.dataconservancy.dcs.access.client.presenter.EntityPresenter;
import org.dataconservancy.dcs.access.client.presenter.FacetedSearchPresenter;
import org.dataconservancy.dcs.access.client.presenter.LoginPresenter;
import org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter;
import org.dataconservancy.dcs.access.client.presenter.Presenter;
import org.dataconservancy.dcs.access.client.presenter.ProvenancePresenter;
import org.dataconservancy.dcs.access.client.presenter.RegisterPresenter;
import org.dataconservancy.dcs.access.client.presenter.RelationsPresenter;
import org.dataconservancy.dcs.access.client.presenter.UploadPresenter;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.client.view.AdminView;
import org.dataconservancy.dcs.access.client.view.EntityView;
import org.dataconservancy.dcs.access.client.view.FacetedSearchView;
import org.dataconservancy.dcs.access.client.view.LoginView;
import org.dataconservancy.dcs.access.client.view.MediciIngestView;
import org.dataconservancy.dcs.access.client.view.ProvenanceView;
import org.dataconservancy.dcs.access.client.view.RegisterView;
import org.dataconservancy.dcs.access.client.view.RelationsView;
import org.dataconservancy.dcs.access.client.view.UploadView;
import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.UserSession;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * User interface.
 */
public class SeadApp implements EntryPoint {
	
	//Variables
    static final String BUG_SUBMIT_EMAIL = "sead-va-l@indiana.edu";
    public static final int MAX_SEARCH_RESULTS = 20;

    //Load from configuration file
    public static String accessurl;
    public static String queryPath;
    public static String bagIturl;
    public static String deposit_endpoint;
    public static String tmpHome;
    
    public static Map<String,List<String>> selectedItems = new HashMap<String,List<String>>();  
    	
    static TextBox accessurl_tb;
    static String datastreamUrl;
    public static String[] admins;

    static DockLayoutPanel main;
   
    static Panel centerPanel;
    static Panel facetContent;
    static Panel loginPanel;
    Panel header;

    HorizontalPanel OptionsHorz;
        
    Label dataSearch;
    Label uploadData;
    Label dataHistory;
    Label adminPage;
    
    TabPanel uploadPanel;
    Panel localUpload;
    Panel mediciUpload;
    Panel bagUpload;
    Panel facetOuterPanel ;
    Label loginLabel;
    Panel notificationPanel;
    public static final String FILE_UPLOAD_URL =
            GWT.getModuleBaseURL() + "fileupload";
    public static final String BAG_UPLOAD_URL =
            GWT.getModuleBaseURL() + "bagupload";
    public static final String GET_SPARQL_URL =
            GWT.getModuleBaseURL() + "sparql";
    
    public static final String GET_User_URL =
            GWT.getModuleBaseURL() + "usermanage";
    
    public static final String GET_MONITOR_URL =
            GWT.getModuleBaseURL() + "monitor";
    
    public static final String ACRCOMMON =
            GWT.getModuleBaseURL() + "acrcommon";
    
    public static final UserServiceAsync userService =
            GWT.create(UserService.class);
 
    

 //   private final SparqlQueryServletAsync sparql =
   //         GWT.create(SparqlQueryServlet.class);
 
  
    static List<String> fileids = new ArrayList<String>();
    static List<String> colids=  new ArrayList<String>();
    static List<String> duids= new ArrayList<String>();
    public static int colseq;
	public static int fileseq;
	public static int duseq;

  



    static void reportInternalError(String message, Throwable e) {
        Window.alert("This is likely a bug. Please report to "
                + BUG_SUBMIT_EMAIL
                + ". Include your operating system and version, browser and version, url you were visiting, and what you did to trigger the problem. \nInternal error: "
                + message + "\n" + e.getMessage()
                + (e.getCause() == null ? "" : "\nCaused by: " + e.getCause()));
    }

  
    int i  = 1;
    //Module Load function
    public void onModuleLoad() {
  
    	accessurl_tb = new TextBox();
        main = new DockLayoutPanel(Unit.PX);
        main.setStylePrimaryName("Main");
        main.setStyleName("orientation-style");
        main.setSize("100%", "100%");
        
        header = new VerticalPanel();
        header.setStylePrimaryName("TopHeader");
        header.setHeight(Window.getClientHeight()/4 + "px");

        
        Panel footer = new FlowPanel();
        footer.setStylePrimaryName("Footer");

        main.addNorth(header, 150);//,DockPanel.NORTH);
        main.addSouth(footer,40);//,DockPanel.SOUTH);
        
        
        facetOuterPanel = new FlowPanel(); 
        
        facetContent = new ScrollPanel();
        facetContent.setHeight("80%");
        facetContent.setStyleName("CurvedPanel");
        facetOuterPanel.setStyleName("FacetPanel");
        facetOuterPanel.add(facetContent);
      
        main.addWest(facetOuterPanel,250);

       
        final Panel headerOuterPanel = new FlowPanel();
        headerOuterPanel.setStyleName("HeaderOuter");
        HorizontalPanel middlePanel = new HorizontalPanel();
        middlePanel.setWidth("100%");
        
        OptionsHorz = new HorizontalPanel();
        dataSearch =Util.label("Data Search", "Option");
        OptionsHorz.add(dataSearch);
        uploadData =Util.label("Upload Data", "Option");
        OptionsHorz.add(uploadData);
        
        middlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        middlePanel.add(OptionsHorz);
        
        HorizontalPanel externalLinksPanel = new HorizontalPanel();
        notificationPanel = new FlowPanel();
        
        Label seadAcr =Util.label("SEAD ACR", "Option");
        Label seadVivo =Util.label("SEAD VIVO", "Option");
        
        seadAcr.addClickHandler( new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Window.open("http://nced.ncsa.illinois.edu/", "_blank", "");
			}
		});
 
        seadVivo.addClickHandler( new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Window.open("http://sead-vivo.d2i.indiana.edu:8080/sead-vivo/", "_blank", "");
			}
		});
		
		externalLinksPanel.add(notificationPanel);
//        externalLinksPanel.add(seadAcr);
//        externalLinksPanel.add(seadVivo);
        
        
        middlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        middlePanel.add(externalLinksPanel);
        
        headerOuterPanel.add(middlePanel);
        headerOuterPanel.setStyleName("Gradient");
        
        headerOuterPanel.setHeight(Window.getClientHeight()/7 + "px");
        
        Window.addResizeHandler(new ResizeHandler() {
         public void onResize(ResizeEvent event) {
           int height = event.getHeight();
           header.setHeight(height/4 + "px");
           headerOuterPanel.setHeight(height/7 + "px");
         }
        });
        

              
       ClickHandler goDataSearch = new ClickHandler() {
            public void onClick(ClickEvent event) {
            	History.newItem(SeadState.HOME.toToken());
            }
        };

        dataSearch.addClickHandler(goDataSearch);
        
      
        ClickHandler goUploadData = new ClickHandler() {
            public void onClick(ClickEvent event) {
                History.newItem(SeadState.AUTH.toToken());
            }
        };
        
        uploadData.addClickHandler(goUploadData);
        
        
      
        adminPage =Util.label("Administration", "Option");
        
        ClickHandler goAdminPage = new ClickHandler() {
            public void onClick(ClickEvent event) {
            	
                History.newItem(SeadState.ADMIN.toToken());
            }
        };
        
        adminPage.addClickHandler(goAdminPage);
       
        dataHistory = Util.label("Data Ingest Monitor", "Option");
        
        ClickHandler dataHistoryPage = new ClickHandler() {
            public void onClick(ClickEvent event) {
            	History.newItem(SeadState.MONITOR.toToken());
            }
        };
        dataHistory.addClickHandler(dataHistoryPage);
        
       
        centerPanel = new ScrollPanel();
        main.add(centerPanel);
        
        uploadPanel =
        		new TabPanel();
        
        localUpload = new FlowPanel();
        mediciUpload = new FlowPanel();
        bagUpload = new FlowPanel();
        uploadPanel.add(localUpload,"Local Upload");
        uploadPanel.add(mediciUpload,"Medici Upload");
        
        
        uploadPanel.selectTab(1);
        uploadPanel.setSize("100%", "100%");
 
        
        Image logo = new Image(GWT.getModuleBaseURL()
                + "../images/sead_logo_2.png");

        ClickHandler gohome = new ClickHandler() {
            public void onClick(ClickEvent event) {
                History.newItem(SeadState.HOME.toToken());
            }
        };

        logo.addClickHandler(gohome);
        
        

        Image toptext = new Image(GWT.getModuleBaseURL()
                + "../images/topic.png");

       
        toptext.addClickHandler(gohome);
        
        
        Panel logoPanel = new HorizontalPanel();
        logoPanel.add(logo);
        logoPanel.add(toptext);
        loginPanel = new FlowPanel();
        loginPanel.setStyleName("logoutPanel");
        loginLabel = Util.label("Sign In/ Register","LogoutButton");
        
        loginLabel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("login");
			}
		});
        

    	
        logoPanel.add(loginPanel);
        header.setWidth("100%");
        header.add(logoPanel);

       
        header.add(headerOuterPanel);
        
        footer.add(createAccessServiceUrlEditor());
        footer.add(new HTML(
                "<a href='http://sead-data.net/'>http://sead-data.net/</a>"));

    
        RootLayoutPanel.get().add(main);
        
        final AsyncCallback<UserSession> cb =
                new AsyncCallback<UserSession>() {

                    public void onSuccess(UserSession result) {
                       
                  	  if(result.isSession())
                  	  {
                  		Label  logoutLbl = Util.label("Logout", "LogoutButton");
                    		ClickHandler logout = new ClickHandler() {
                    			
                    			@Override
                    			public void onClick(ClickEvent event) {
                    				loginPanel.clear();   
                    				loginPanel.add(loginLabel);
                    				dataHistory.setStyleName("Option");
                    				adminPage.setStyleName("Option");
                    				OptionsHorz.add(adminPage);
                    				OptionsHorz.add(dataHistory);
                    				History.newItem("logout");
                    			}
                    		}; 
                    		
                    		logoutLbl.addClickHandler(logout);
                    		loginPanel.add(logoutLbl);
                  	  }
                  	  else
                  		  loginPanel.add(loginLabel);
                       
                        
                    }

                    public void onFailure(Throwable error) {
                        Window.alert("Failed to login: "
                                + error.getMessage());
                         
                    }
                };
                
        History.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {
                historyChanged(event.getValue());
            }
        });

        //uncomment this part
        /*if (accessurl != null) {
            updateAccessServiceUrl();
            deposit_endpoint =
            		accessurl+"deposit/";
            userService.checkSession(null,cb);
            History.fireCurrentHistoryState();
        } else */{
            // load config

        	String temp = GWT.getModuleBaseURL() + "Config.properties";
            HttpGet.request(GWT.getModuleBaseURL() + "Config.properties",
                    new HttpGet.Callback<String>() {

                        public void failure(String error) {
                            Window.alert("Failed to load config: " + error);
                        }

                        public void success(String result) {
                            String[] pairs = result.trim().split(
                                    //"\\w*(\n|\\=)\\w*");
                            		"\\w*\n|\\=\\w*");

                            for (int i = 0; i + 1 < pairs.length;) {
                                String name = pairs[i++].trim();
                                String value = pairs[i++].trim();

                                if (name.equals("accessServiceURL")) {
                                    accessurl = value;
                                    updateAccessServiceUrl();
                                    deposit_endpoint =
                                    		accessurl+"deposit/";
                                }
                                if (name.equals("datastreamServletUrl")) {
                                    datastreamUrl = value;
                                }
                                if (name.equals("bagItServiceURL")) {
                                    bagIturl = value;
                                }
                                
                                if (name.equals("queryPath")) {
                                	queryPath = value;
                                }
                                
                                if (name.equals("tmp")) {
                                	tmpHome = value;
                                }
                                
                                if (name.equals("admins")) {
                               	 String adminStr = value;
                               	 admins = adminStr.split(";");
                                }
                            }
                            userService.checkSession(null,cb);
                            History.fireCurrentHistoryState();
                        }
                    });
        }     
    
    }

    private void handleHistoryTokenError(String token) {
        Window.alert("Error parsing action: " + token);
    }

    Presenter presenter;
    private void historyChanged(String token) {
    	
    	 if (token.isEmpty()) {
        	dataSearch.setStyleName("OptionSelected");
        	uploadData.setStyleName("Option");
        	adminPage.setStyleName("Option");
        	dataHistory.setStyleName("Option");
        	
        	FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
        	SearchInput input = new SearchInput(null, null, 0, new String[0],new String[0]);
         	presenter = new FacetedSearchPresenter(new FacetedSearchView());
         	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        	selectedItems = new HashMap<String, List<String>>();
        	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input));
            return;
        }

        SeadState state = SeadState.fromToken(token);
        final List<String> args = SeadState.tokenArguments(token);

        if (state == null) {
            handleHistoryTokenError(token);
            return;
        }

        if (state == SeadState.HOME) {
        	
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,250);
        	}
        	else
        		main.addWest(facetOuterPanel,250);
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
            
        	dataSearch.setStyleName("OptionSelected");
        	uploadData.setStyleName("Option");
        	adminPage.setStyleName("Option");
        	dataHistory.setStyleName("Option");
        	        	
        	selectedItems = new HashMap<String, List<String>>();
        	SearchInput input = new SearchInput(null, null, 0, new String[0],new String[0]);
        	FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
        	presenter = new FacetedSearchPresenter(new FacetedSearchView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        	
        	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input));
        } else if (state == SeadState.SEARCH) {
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,250);
        	}
        	else
        		main.addWest(facetOuterPanel,250);
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	
            if (args.size() == 0) {
            	SearchInput input = new SearchInput(null, null, 0, new String[0],new String[0]);
            	FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
            	presenter = new FacetedSearchPresenter(new FacetedSearchView());
            	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            	selectedItems = new HashMap<String, List<String>>();
            	
            	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input));
                return;
            }
            
            
           
           /* if (args.size() < 2 || (args.size() & 1) == 0) {
                handleHistoryTokenError(token);
                return;
            }
            */
            int offset = 0;
 
            

            //get facets args.get(args.size()-2) & args.get(args.size()-3)
            
            String[] facetFields =null;
            String[] facetValues =null;
            int facetCount =0;
            try {
        	   facetCount = Integer.parseInt(args.get(args.size() - 1));
            } catch (NumberFormatException e) {
                handleHistoryTokenError(token);
                return;
            }
            
           selectedItems = new HashMap<String, List<String>>();
         
            int userFieldsLength =(args.size() - 2-facetCount*2)/2;
            Search.UserField[] userfields = new Search.UserField[userFieldsLength];
            String[] userqueries = new String[userFieldsLength];

            int argIndex=-1;
            for (int i = 0; i < userFieldsLength; i++) {
                userfields[i] = Search.UserField.valueOf(args.get(++argIndex));
                userqueries[i] = args.get(++argIndex);

                if (userfields[i] == null) {
                    handleHistoryTokenError(token);
                    return;
                }
            }
            
            facetFields = new String[facetCount];
            facetValues = new String[facetCount];
            for (int i = 0; i < facetCount; i++) {
            	facetFields[i] = args.get(++argIndex);
                facetValues[i] = args.get(++argIndex);
                List<String> childFacets = selectedItems.get(facetFields[i]);
                
                if(childFacets == null)
                	childFacets = new ArrayList<String>();                	
                childFacets.add(facetValues[i]);
                selectedItems.put(facetFields[i], childFacets);
                	
            }

            SearchInput input = new SearchInput(userfields, userqueries, offset, facetFields, facetValues);

            FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
            presenter = new FacetedSearchPresenter(new FacetedSearchView());//);
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        	
        	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input));
        } else if (state == SeadState.ENTITY) {
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	
            if (args.size() != 1) {
                handleHistoryTokenError(token);
                return;
            }

            presenter = new EntityPresenter(new EntityView(args.get(0)));
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);

        } else if (state == SeadState.RELATED) {
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
            if (args.size() != 1) {
                handleHistoryTokenError(token);
                return;
            }

            presenter = new RelationsPresenter(new RelationsView(args.get(0)));
            presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        }else if (state == SeadState.LOGIN) {
        	if(facetOuterPanel.isAttached())
        		main.setWidgetSize(facetOuterPanel,0);
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
            
            //DataUpload.viewUpload(centerPanel);
        	presenter = new LoginPresenter(new LoginView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            
        }else if (state == SeadState.LOGOUT) {
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
            
        	

        	AsyncCallback<Void> cb =
                      new AsyncCallback<Void>() {

                          public void onSuccess(Void result) {

                        	// Options.remove(adminPage);
                        	 OptionsHorz.remove(adminPage);
                        	 OptionsHorz.remove(dataHistory);
                        	 dataSearch.setStyleName("OptionSelected");
                             uploadData.setStyleName("Option");
                             History.newItem("home");
                          }

                          public void onFailure(Throwable error) {
                              Window.alert("Failed to logout: "
                                      + error.getMessage());
                               
                          }
                      };

                      userService.clearSession(cb);
          
            
        }else if (state == SeadState.REGISTER) {
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
            presenter = new RegisterPresenter(new RegisterView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            
        }else if (state == SeadState.AUTH) {
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	else
        		centerPanel.clear();
        	
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	
        	
        	
        	dataSearch.setStyleName("Option");
        	uploadData.setStyleName("OptionSelected");
        	
        	AsyncCallback<UserSession> cb =
                      new AsyncCallback<UserSession>() {
                          public void onSuccess(UserSession result) {

                        	  if(result.isSession())
                        	  {
                        		  if(result.getRole() == Role.ROLE_ADMIN)
                        		  {
                        			  //Options.add(adminPage);;//add admin button
                        			  OptionsHorz.add(adminPage);//add admin button
                        	          adminPage.setStyleName("Option");
                        	          OptionsHorz.add(dataHistory);//add admin button
                        	          dataHistory.setStyleName("Option");
                        	          Label  logoutLbl = Util.label("Logout", "LogoutButton");
                              			if(args.size()>0){
                              				ClickHandler logout = new ClickHandler() {
                              			
		                              			@Override
		                              			public void onClick(ClickEvent event) {
		                              				loginPanel.clear();   
		                              				loginPanel.add(loginLabel);
		                              				History.newItem("logout");
		                              			}
		                              		}; 
                              			
		                              		logoutLbl.addClickHandler(logout);
		                              		loginPanel.clear();
		                              		loginPanel.add(logoutLbl);
                              			}
                        		  }
                              	  History.newItem("upload");
                        	  }
                        	  else
                        		  History.newItem("login");
                             
                              
                          }

                          public void onFailure(Throwable error) {
                              Window.alert("Failed to login: "
                                      + error.getMessage());
                               
                          }
                      };

                      String argument = null;
                      if(args.size()>0)
                    	  argument = args.get(1);
                      userService.checkSession(argument,cb);
            
        }else if (state == SeadState.UPLOAD) {
        	if(facetOuterPanel.isAttached())
        		main.setWidgetSize(facetOuterPanel,0);

        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	AsyncCallback<UserSession> cb =
                    new AsyncCallback<UserSession>() {

                        public void onSuccess(UserSession result) {
                           
                      	  if(result.isSession())
                      	  {
                      		  if(result.getRole() == Role.ROLE_ADMIN)
                      		  {
                      			 
                      			OptionsHorz.add(adminPage);//add admin button
                      			OptionsHorz.add(dataHistory);//add upload button
                      		  }
                              History.newItem("upload");
                      	  }
                      	  else
                      		  History.newItem("login");
                           
                            
                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
                    
            userService.checkSession(null,cb);
            centerPanel.clear();
            centerPanel.add(uploadPanel);
            
            presenter = new MediciIngestPresenter(new MediciIngestView());
        	presenter.display(mediciUpload, facetContent, header, loginPanel, notificationPanel);
        	
            presenter = new UploadPresenter(new UploadView());
        	presenter.display(localUpload, facetContent, header, loginPanel, notificationPanel);
        	            
        }else if (state == SeadState.ADMIN) {
        	
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	
        	dataSearch.setStyleName("Option");
        	uploadData.setStyleName("Option");
        	adminPage.setStyleName("OptionSelected");
        	presenter = new AdminPresenter(new AdminView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            
        }else if (state == SeadState.MONITOR) {
        	if(facetOuterPanel.isAttached())
        		main.setWidgetSize(facetOuterPanel,0);

        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	AsyncCallback<UserSession> cb =
                    new AsyncCallback<UserSession>() {

                        public void onSuccess(UserSession result) {
                           
                      	  if(result.isSession())
                      	  {
                      		  if(result.getRole() == Role.ROLE_ADMIN)
                      		  {
                      			 
                      			OptionsHorz.add(adminPage);//add admin button
                      		  }
                              History.newItem("upload");
                      	  }
                      	  else
                      		  History.newItem("login");
                           
                            
                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
          
        	dataSearch.setStyleName("Option");
        	uploadData.setStyleName("Option");
        	adminPage.setStyleName("Option");
        	dataHistory.setStyleName("OptionSelected");
        	presenter = new ProvenancePresenter(new ProvenanceView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            
        }else {
            handleHistoryTokenError(token);
            return;
        }
    }

    
   
    
   
    
    public static String datastreamURL(String id) {
        return accessurl + "datastream/" + encodeURLPath(id);
    }

    public static String datastreamURLnoEncoding(String id) {
        id=id.replace("%2F","/");
        return accessurl + "datastream/" + id;
    }
    
    public static String packageLinkURLnoEncoding(String id) {
        id=id.replace("%2F","/");
        return accessurl + "package/link/" + id;
    }
    private static String encodeURLPath(String path) {
        return URL.encodePathSegment(path);
    }

 

   
   
    public static Constants constants = new Constants();
    
 

   
    private void updateAccessServiceUrl() {
        accessurl_tb.setText(accessurl);
        accessurl_tb.setWidth(accessurl.length() + "ex");
    }

    private Widget createAccessServiceUrlEditor() {
        FlexTable table = Util.createTable("Access service:");

        Button set = new Button("Set");

        Util.addColumn(table, accessurl_tb);
        Util.addColumn(table, set);
        Label aboutLabel = Util.label("About", "Hyperlink");
        Util.addColumn(table, aboutLabel);
        aboutLabel.addStyleName("LeftPad");
        aboutLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("http://sead-data.net/","_blank","");
			}
		});
        
        Label contactLabel = Util.label("Contact Us", "Hyperlink");
        Util.addColumn(table, contactLabel);
        contactLabel.addStyleName("LeftPad");
        contactLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("mailto:seadva-l@indiana.edu","_blank","");
			}
		});

        set.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String s = accessurl_tb.getText().trim();

                if (!s.isEmpty()) {
                    accessurl = s;
                    History.fireCurrentHistoryState();
                }
            }
        });

        return table;
    }

    
    Grid upload;
 
 
 
}
