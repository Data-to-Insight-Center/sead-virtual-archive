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

import org.dataconservancy.dcs.access.client.Search.UserField;
import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;
import org.dataconservancy.dcs.access.client.event.SearchEvent;
import org.dataconservancy.dcs.access.client.model.SearchInput;
import org.dataconservancy.dcs.access.client.presenter.AcrPublishDataPresenter;
import org.dataconservancy.dcs.access.client.presenter.ActivityPresenter;
import org.dataconservancy.dcs.access.client.presenter.AdminPresenter;
import org.dataconservancy.dcs.access.client.presenter.CuratorViewPresenter;
import org.dataconservancy.dcs.access.client.presenter.EditPresenter;
import org.dataconservancy.dcs.access.client.presenter.EntityPresenter;
import org.dataconservancy.dcs.access.client.presenter.FacetedSearchPresenter;
import org.dataconservancy.dcs.access.client.presenter.LoginPresenter;
import org.dataconservancy.dcs.access.client.presenter.Presenter;
import org.dataconservancy.dcs.access.client.presenter.ProvenancePresenter;
import org.dataconservancy.dcs.access.client.presenter.PublishDataPresenter;
import org.dataconservancy.dcs.access.client.presenter.RelationsPresenter;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;
import org.dataconservancy.dcs.access.client.ui.LoginPopupPanel;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.client.view.AcrPublishDataView;
import org.dataconservancy.dcs.access.client.view.ActivityView;
import org.dataconservancy.dcs.access.client.view.AdminView;
import org.dataconservancy.dcs.access.client.view.CuratorView;
import org.dataconservancy.dcs.access.client.view.EditView;
import org.dataconservancy.dcs.access.client.view.EntityView;
import org.dataconservancy.dcs.access.client.view.FacetedSearchView;
import org.dataconservancy.dcs.access.client.view.ProvenanceView;
import org.dataconservancy.dcs.access.client.view.PublishDataView;
import org.dataconservancy.dcs.access.client.view.RelationsView;
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
    static final String BUG_SUBMIT_EMAIL = "sead-dev-l@indiana.edu";
    public static final int MAX_SEARCH_RESULTS = 20;

    //Load from configuration file
    public static String accessurl;
    public static String queryPath;
    public static String bagIturl;
    public static String deposit_endpoint;
    public static String registryUrl;
    public static String roUrl;
    public static String tmpHome;
    public static boolean isHome;
    
    public static Map<String,List<String>> selectedItems = new HashMap<String,List<String>>();  
    	
    static TextBox accessurl_tb;
    static String datastreamUrl;
    public static String[] admins;

    static HorizontalPanel mainHorz;
    static DockLayoutPanel main;
   
    static Panel centerPanel;
    static Panel facetContent;
    static Panel loginPanel;
    public static HorizontalPanel outerMoreLinks;
    Panel header;

    HorizontalPanel optionsHorz;
        
    Label dataSearch;
    Label uploadData;
    Label dataHistory;
    Label adminPage;
    Label curatorPage;
    Label activityPage;
    Label home;
    Label features;
  //  Label team; 
    Label resources;
    Label partners;
    Button logoutButton;
    Button loginButton;
    
    TabPanel uploadPanel;
    Panel localUpload;
    Panel mediciUpload;
    Panel publishData;
    Panel bagUpload;
    Panel facetOuterPanel;
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

  
    public void initLogoutButton(){
    	if(logoutButton==null){
	    	 logoutButton = new Button("LOG OUT");
	 		 logoutButton.setStyleName("LogoutButton");
	 		 ClickHandler logout = new ClickHandler() {
	   			
	   			@Override
	   			public void onClick(ClickEvent event) {
	   				loginPanel.clear();   
	   				loginPanel.add(loginButton);
	   				dataHistory.setStyleName("Option");
	   				adminPage.setStyleName("Option");
	   				curatorPage.setStyleName("Option");
	   				optionsHorz.add(curatorPage);
	   				optionsHorz.add(adminPage);
	   				optionsHorz.add(dataHistory);
	   				History.newItem("logout");
	   			}
	   		}; 
	   		logoutButton.addClickHandler(logout);
    	}
   		
    }
    int i  = 1;
    //Module Load function
    public void onModuleLoad() {
  
    	mainHorz = new HorizontalPanel();
    	mainHorz.setSize("100%", "100%");
    	mainHorz.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    	mainHorz.setStylePrimaryName("MainHorz");
    	
    	accessurl_tb = new TextBox();
        main = new DockLayoutPanel(Unit.PX);
        main.setStylePrimaryName("Main");
//        main.setStyleName("orientation-style");
        main.setSize("80%", "100%");
        mainHorz.add(main);
        
        //header parameters
        
        header = new VerticalPanel();
        header.setStylePrimaryName("TopHeader");
        header.setHeight(Window.getClientHeight()/4 + "px");

        
        HorizontalPanel footer = new HorizontalPanel();
        footer.setStylePrimaryName("SeadFooter");
        footer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        footer.setWidth("100%");
        
        HorizontalPanel footerContent = new HorizontalPanel();
        footerContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        footerContent.setWidth("30%");
        Image nsfFooterImage = new Image(GWT.getModuleBaseURL()+ "../images/nsf_footer.png");
        Label nsfFooterText = Util.label("SEAD is funded by the National Science Foundation under cooperative agreement #OCI0940824", "greyFont");
        footerContent.add(nsfFooterImage);
        footerContent.add(nsfFooterText);
        
        footer.add(footerContent);
        
        
        outerMoreLinks = new HorizontalPanel();
        outerMoreLinks.setStyleName("MoreLinkStyle");
        
        final Grid moreLinks = new Grid(1,4);
        moreLinks.setCellPadding(7);
        
        Image more = new Image(GWT.getModuleBaseURL()+ "../images/more.png");
        Button browseButton = new Button("Browse Data");
        Button uploadButton = new Button("Publish Data");
          
        browseButton.setStyleName("OptionButtons");
        uploadButton.setStyleName("OptionButtons");
            
        ClickHandler goUploadData1 = new ClickHandler() {
            public void onClick(ClickEvent event) {
                History.newItem(SeadState.AUTH.toToken());
            }
        };
        
        ClickHandler browseDataHandler = new ClickHandler() {
            public void onClick(ClickEvent event) {
                History.newItem("browse"); 
            }
        };
        
        uploadButton.addClickHandler(goUploadData1);
        browseButton.addClickHandler(browseDataHandler);
          
        moreLinks.setWidget(0, 1, browseButton);
        moreLinks.setWidget(0, 2, uploadButton);


        main.addNorth(header, 150);//,DockPanel.NORTH);
        main.addSouth(footer,Window.getClientHeight()/17);
        outerMoreLinks.add(moreLinks); 
        
        facetOuterPanel = new FlowPanel(); 
        
        facetContent = new ScrollPanel();
        facetContent.setHeight("80%");
        facetContent.setStyleName("CurvedPanel");
        facetOuterPanel.setStyleName("FacetPanel");
        facetOuterPanel.add(facetContent);
      
        main.addWest(facetOuterPanel,250);
        
        loginPanel = new FlowPanel();
        loginButton = new Button("LOG IN");
        loginButton.setStyleName("LoginButton");
        loginPanel.add(loginButton);
        //loginPanel1.add(registerLabel);
        loginButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("login");
			}
		});
        
        main.addEast(loginPanel, 200);

        final Panel headerOuterPanel = new FlowPanel();
        headerOuterPanel.setStyleName("HeaderOuter");
        HorizontalPanel middlePanel = new HorizontalPanel();
        middlePanel.setWidth("100%");
        middlePanel.setStyleName("Menu");
        
        optionsHorz = new HorizontalPanel(); 
        dataSearch =Util.label("Data Search", "Option");
       // OptionsHorz.add(dataSearch);
        uploadData =Util.label("Upload Data", "Option");
      //  OptionsHorz.add(uploadData);
        
        home = Util.label("Home", "Option");
        home.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(SeadState.HOME.toToken());	
			}
		});
        features = Util.label("Features", "Option");
     //   team = Util.label("Team", "Option");
        resources = Util.label("Resources", "Option");
        partners = Util.label("Partners", "Option");
        
        optionsHorz.add(home);
        optionsHorz.add(features);
      //  optionsHorz.add(team);
        optionsHorz.add(resources);
        optionsHorz.add(partners);
        
        middlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        middlePanel.add(optionsHorz);
          
        middlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        
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
        
        curatorPage =Util.label("Curate", "Option");
        
        ClickHandler goCuratorPage = new ClickHandler() {
            public void onClick(ClickEvent event) {
            	
                History.newItem(SeadState.CURATOR.toToken());
            }
        };
        
        curatorPage.addClickHandler(goCuratorPage);
        
        activityPage =Util.label("Activity", "Option");
        
        ClickHandler goActivityPage = new ClickHandler() {
            public void onClick(ClickEvent event) {
            	
                History.newItem(SeadState.ACTIVITY.toToken());
            }
        };
        
        activityPage.addClickHandler(goActivityPage);
       
        dataHistory = Util.label("Data Ingest Monitor", "Option");
        
        ClickHandler dataHistoryPage = new ClickHandler() {
            public void onClick(ClickEvent event) {
            	History.newItem(SeadState.MONITOR.toToken());
            }
        };
        dataHistory.addClickHandler(dataHistoryPage);
        
       
        centerPanel = new ScrollPanel();
        main.add(centerPanel);
        
        uploadPanel = new TabPanel();
        uploadPanel.setStylePrimaryName("UploadTabPanel");

        localUpload = new FlowPanel();
        mediciUpload = new FlowPanel();
        bagUpload = new FlowPanel();
        uploadPanel.add(localUpload,"Local Upload");
        uploadPanel.add(mediciUpload,"Medici Upload");
        
        
        uploadPanel.selectTab(1);
        uploadPanel.setSize("100%", "100%");
 
        
        publishData = new FlowPanel();
        
        /* Image logo = new Image(GWT.getModuleBaseURL()
                 + "../images/sead_logo_2.png");*/
         Image logo = new Image(GWT.getModuleBaseURL()
                 + "../images/sead_logoV2_a.png");

        ClickHandler gohome = new ClickHandler() {
            public void onClick(ClickEvent event) {
                History.newItem(SeadState.HOME.toToken());
            }
        };

        logo.addClickHandler(gohome);
        
        

        Image toptext = new Image(GWT.getModuleBaseURL()
        		 + "../images/topic_V2_a.png");

       
        toptext.addClickHandler(gohome);
        
        
        Panel logoPanel = new HorizontalPanel();
        logoPanel.add(logo);
        logoPanel.add(toptext);
       
        header.setWidth("100%");
        header.add(logoPanel);

       
        header.add(headerOuterPanel);
        
        RootLayoutPanel.get().add(mainHorz);
        
        final AsyncCallback<UserSession> cb =
                new AsyncCallback<UserSession>() {

                    public void onSuccess(UserSession result) {
                       
                  	  if(result.isSession())
                  	  { 
                  		    initLogoutButton();
                    		loginPanel.clear();
                    		loginPanel.add(logoutButton);
                  	  }
                  	  else
                  		  loginPanel.add(loginButton);
                    }
                    public void onFailure(Throwable error) {
                        new ErrorPopupPanel("Failed to login: "
                                + error.getMessage()).show();
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
                                if (name.equals("registryUrl")) {
                                    registryUrl = value;
                                }
                                if (name.equals("roUrl")) {
                                    roUrl = value;
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
    		if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
            
        	 home.setStyleName("OptionSelected");
			  features.setStyleName("Option");
			//  team.setStyleName("Option");
			  resources.setStyleName("Option");
			  partners.setStyleName("Option");
     		  
        	isHome = true;
        	        	
        	selectedItems = new HashMap<String, List<String>>();
        	SearchInput input = new SearchInput(null, null, 0, new String[0],new String[0]);
        	FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
        	presenter = new FacetedSearchPresenter(new FacetedSearchView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        	
        	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input, false));
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
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
            
        	AsyncCallback<UserSession> cb =
                    new AsyncCallback<UserSession>() {

                        public void onSuccess(UserSession result) {
                           
                      	  if(result.isSession())
                      	  {
                      		  
                      		  
                      		  
	                  		  if(result.getRole() == Role.ROLE_ADMIN)
	                  		  {                      			
	                  			initAdminLogin();
	                  		  }
	                  		  else if(result.getRole() == Role.ROLE_CURATOR)
	                  		  {                      			
	                  			initCuratorLogin();
	                  		  }
	                  		 else
	                  		  {                      			
	                  			initUserLogin();
	                  		  }
	                  		  initLogoutButton();
	                  		  
	                  		 String displayName = "";
							if(result.getfName()!=null)
								displayName = result.getfName();
							if(result.getlName()!=null)
								displayName += " "+result.getlName();
						
							Label displayNameLabel = new Label();
							displayNameLabel.setStyleName("welcomeFont");
							displayNameLabel.setText("Welcome "+displayName);
							loginPanel.clear();
							loginPanel.add(displayNameLabel);
	                  		  
	                  		  if(!logoutButton.isAttached())
	                  			  loginPanel.add(logoutButton);
	                  		activityPage.setStyleName("Option");
	                        adminPage.setStyleName("Option"); 
	                        home.setStyleName("OptionSelected");
	                        uploadData.setStyleName("Option");
	                        curatorPage.setStyleName("Option");
                      	  }
                      	  else
                      		initNoLogin();

                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
            
            userService.checkSession(null,cb);
		
        	isHome = true;
        	        	
        	selectedItems = new HashMap<String, List<String>>();
        	SearchInput input = new SearchInput(null, null, 0, new String[0],new String[0]);
        	FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
        	presenter = new FacetedSearchPresenter(new FacetedSearchView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        	
        	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input, false));
        }else if (state == SeadState.ADVANCED) {
            if(facetOuterPanel.isAttached()){
                main.setWidgetSize(facetOuterPanel,250);

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

                FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input, true));
            }
        }else if (state == SeadState.BROWSE) {
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,250);
        	}
        	else
        		main.addWest(facetOuterPanel,250);
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	if(loginPanel.isAttached()){
        		main.setWidgetSize(loginPanel, 0);
        	}
        	
        	UserField[] userfields = new UserField[1];
        	String[] userqueries = new String[1];
            String facetFields[] = new String[1];
            String facetValues[] = new String[1];

        	userfields[0] =  UserField.ABSTRACT;
        	userqueries[0] = "'' TO *";
            facetFields[0] = "entityType";
            facetValues[0] = "Collection";
        	
            
            SearchInput input = new SearchInput(userfields, userqueries, 0, facetFields, facetValues);
        	FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
        	presenter = new FacetedSearchPresenter(new FacetedSearchView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        	selectedItems = new HashMap<String, List<String>>();
        	
        	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input, false));
        	return;

        } 
        
        else if (state == SeadState.SEARCH) {
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,250);
        	}
        	else
        		main.addWest(facetOuterPanel,250);
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	if(loginPanel.isAttached()){
        		main.setWidgetSize(loginPanel, 0);
        	}
        	
            if (args.size() == 0) {
            	SearchInput input = new SearchInput(null, null, 0, new String[0],new String[0]);
            	FacetedSearchPresenter.EVENT_BUS = GWT.create(SimpleEventBus.class);
            	presenter = new FacetedSearchPresenter(new FacetedSearchView());
            	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            	selectedItems = new HashMap<String, List<String>>();
            	
            	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input, false));
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
            isHome = false;
           
            int userFieldsLength =(args.size() - 2-facetCount*2)/2;
            UserField[] userfields = new UserField[userFieldsLength];
            String[] userqueries = new String[userFieldsLength];

            int argIndex=-1;
            for (int i = 0; i < userFieldsLength; i++) {
                userfields[i] = UserField.valueOf(args.get(++argIndex));
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
        	
        	FacetedSearchPresenter.EVENT_BUS.fireEvent(new SearchEvent(input, false));
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
        	if(loginPanel.isAttached()){
        		main.setWidgetSize(loginPanel, 0);
        	}
        	if(outerMoreLinks.isAttached()){
        		main.setWidgetSize(outerMoreLinks, 0);
        	}
            if (args.size() != 1) {
                handleHistoryTokenError(token);
                return;
            }

            presenter = new RelationsPresenter(new RelationsView(args.get(0)));
            presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        }else if (state == SeadState.LOGIN) {
        	try{
            	presenter = new LoginPresenter(new LoginPopupPanel());
            	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);

            }
        	catch (Exception e){
        		e.printStackTrace();
        	}
            
        }else if (state == SeadState.LOGOUT) {
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);

        	AsyncCallback<Void> cb =
                      new AsyncCallback<Void>() {

                          public void onSuccess(Void result) {

                        	 initNoLogin();
                             History.newItem(SeadState.HOME.toToken());
                          }

                          public void onFailure(Throwable error) {
                              Window.alert("Failed to logout: "
                                      + error.getMessage());
                               
                          }
                      };

                      userService.clearSession(cb);
          
            
        }else if (state == SeadState.CURATOR) {
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	
        	AsyncCallback<UserSession> cb =
                    new AsyncCallback<UserSession>() {

                        public void onSuccess(UserSession result) {
                           
                      	  if(result.isSession())
                      	  {
                      		 
                      		  if(result.getRole() == Role.ROLE_CURATOR){
                    			initCuratorLogin();
                      		  }
                      		  else if(result.getRole() == Role.ROLE_ADMIN)
                      		  {
                      			initAdminLogin();
                      		  }
                      		  else{
                      			 History.newItem(SeadState.HOME.toToken()); //no permissions
                      		  }
                      		  initLogoutButton();
                      		  if(!logoutButton.isAttached())
                      			  loginPanel.add(logoutButton);
                            //  History.newItem("upload");
                      	  }
                      	  else
                      		  History.newItem(SeadState.LOGIN.toToken());

                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                        }
                    };
                    
                    
            curatorPage.setStyleName("OptionSelected");
            adminPage.setStyleName("Option"); 
            home.setStyleName("Option");
            uploadData.setStyleName("Option");
                    
            userService.checkSession(null,cb);
            presenter = new CuratorViewPresenter(new CuratorView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            
        }
        else if (state == SeadState.EDIT) {
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	List<String> tempargs = SeadState.tokenArguments(token);
        	
        	AsyncCallback<UserSession> cb =
                    new AsyncCallback<UserSession>() {

                        public void onSuccess(UserSession result) {
                           
                      	  if(result.isSession())
                      	  {
	                  		  if(result.getRole() == Role.ROLE_ADMIN)
	                  		  {                      			
	                  			initAdminLogin();
	                  		  }
	                  		  else if(result.getRole() == Role.ROLE_CURATOR)
	                  		  {                      			
	                  			initCuratorLogin();
	                  		  }
	                  		 else
	                  		  {                      			
	                  			initUserLogin();
	                  		  }
	                  		  initLogoutButton();
	                  		  
	                  		 String displayName = "";
							if(result.getfName()!=null)
								displayName = result.getfName();
							if(result.getlName()!=null)
								displayName += " "+result.getlName();
						
							Label displayNameLabel = new Label();
							displayNameLabel.setStyleName("welcomeFont");
							displayNameLabel.setText("Welcome "+displayName);
							loginPanel.clear();
							loginPanel.add(displayNameLabel);
	                  		  
	                  		  if(!logoutButton.isAttached())
	                  			  loginPanel.add(logoutButton);
	                        //  History.newItem("upload");
                      	  }
                      	  else
                      		  History.newItem(SeadState.LOGIN.toToken());

                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
                    
            curatorPage.setStyleName("OptionSelected");
            adminPage.setStyleName("Option"); 
            home.setStyleName("Option");
            uploadData.setStyleName("Option");
                    
            userService.checkSession(null,cb);
            
            presenter = new EditPresenter(new EditView(tempargs.get(0)));
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
            
        }
        else if (state == SeadState.ACTIVITY) {
        /*	if(count==0){
        		forceReload();
        		count++;
        	}*/
        	
        	
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	if(facetOuterPanel.isAttached()){
        		main.setWidgetSize(facetOuterPanel,0);
        	}
        	
        	AsyncCallback<UserSession> cb =
                    new AsyncCallback<UserSession>() {

                        public void onSuccess(UserSession result) {
                           
                      	  if(result.isSession())
                      	  {
                      		  
                      		  
                      		  
	                  		  if(result.getRole() == Role.ROLE_ADMIN)
	                  		  {                      			
	                  			initAdminLogin();
	                  		  }
	                  		  else if(result.getRole() == Role.ROLE_CURATOR)
	                  		  {                      			
	                  			initCuratorLogin();
	                  		  }
	                  		 else
	                  		  {                      			
	                  			initUserLogin();
	                  		  }
	                  		  initLogoutButton();
	                  		  
	                  		 String displayName = "";
							if(result.getfName()!=null)
								displayName = result.getfName();
							if(result.getlName()!=null)
								displayName += " "+result.getlName();
						
							Label displayNameLabel = new Label();
							displayNameLabel.setStyleName("welcomeFont");
							displayNameLabel.setText("Welcome "+displayName);
							loginPanel.clear();
							loginPanel.add(displayNameLabel);
	                  		  
	                  		  if(!logoutButton.isAttached())
	                  			  loginPanel.add(logoutButton);
	                  		activityPage.setStyleName("OptionSelected");
	                        adminPage.setStyleName("Option"); 
	                        home.setStyleName("Option");
	                        uploadData.setStyleName("Option");
	                        curatorPage.setStyleName("Option");
                      	  }
                      	  else
                      		  History.newItem(SeadState.LOGIN.toToken());

                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
            
            userService.checkSession(null,cb);
            presenter = new ActivityPresenter(new ActivityView());
        	presenter.display(centerPanel, facetContent, header, loginPanel, notificationPanel);
        }
        else if (state == SeadState.AUTH) {
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
                        			
                        	          Label  logoutLbl = Util.label("Logout", "LogoutButton");
                              			if(args.size()>0){
                              				ClickHandler logout = new ClickHandler() {
                              			
		                              			@Override
		                              			public void onClick(ClickEvent event) {
		                              				loginPanel.clear();   
//		                              				loginPanel.add(loginLabel);
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
        	 if(args.size()>0)
        		 History.newItem(SeadState.UPLOAD.toToken());
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
	                  			  initAdminLogin();
	                  		  else if(result.getRole() == Role.ROLE_CURATOR)
	                  			  initCuratorLogin();
	                  		  else
	                  			  initUserLogin();
	                  		  initLogoutButton();
	                  		  String displayName = "";
							  if(result.getfName()!=null)
								displayName = result.getfName();
							  if(result.getlName()!=null)
								displayName += " "+result.getlName();
						
							  Label displayNameLabel = new Label();
							  displayNameLabel.setStyleName("welcomeFont");
							  displayNameLabel.setText("Welcome "+displayName);
							  loginPanel.clear();
							  loginPanel.add(displayNameLabel);
	                  		  
	                  		  if(!logoutButton.isAttached())
	                  			  loginPanel.add(logoutButton);
	                        //  History.newItem("upload");
	                  		  curatorPage.setStyleName("Option");
	                          adminPage.setStyleName("Option"); 
	                          home.setStyleName("Option");
	                          uploadData.setStyleName("OptionSelected");
                      	  }
                      	  else
                      		  History.newItem(SeadState.LOGIN.toToken());

                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
                    
            userService.checkSession(null,cb);         
            
            centerPanel.clear();
            centerPanel.add(publishData);
        	presenter = new PublishDataPresenter(new PublishDataView());
        	presenter.display(publishData, facetContent, header, loginPanel, notificationPanel);
        	            
        }else if (state == SeadState.ACRUPLOAD) {
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
                  			  initAdminLogin();
                  		  else if(result.getRole() == Role.ROLE_CURATOR)
                  			  initCuratorLogin();
                  		  else
                  			  initUserLogin();
                  		  initLogoutButton();
                  		  String displayName = "";
						  if(result.getfName()!=null)
							displayName = result.getfName();
						  if(result.getlName()!=null)
							displayName += " "+result.getlName();
					
						  Label displayNameLabel = new Label();
						  displayNameLabel.setStyleName("welcomeFont");
						  displayNameLabel.setText("Welcome "+displayName);
						  loginPanel.clear();
						  loginPanel.add(displayNameLabel);
                  		  
                  		  if(!logoutButton.isAttached())
                  			  loginPanel.add(logoutButton);
                        //  History.newItem("upload");
                  		  curatorPage.setStyleName("Option");
                          adminPage.setStyleName("Option"); 
                          home.setStyleName("Option");
                          uploadData.setStyleName("OptionSelected");
                  	  }
                  	  else
                  		  History.newItem(SeadState.LOGIN.toToken());

                    }

                    public void onFailure(Throwable error) {
                        Window.alert("Failed to login: "
                                + error.getMessage());
                         
                    }
                };
                
        userService.checkSession(null,cb);         
        
        centerPanel.clear();
        centerPanel.add(publishData);
    	presenter = new AcrPublishDataPresenter(new AcrPublishDataView());
    	presenter.display(publishData, facetContent, header, loginPanel, notificationPanel);
    	            
    }else if (state == SeadState.ADMIN) {
        	
        	if(!centerPanel.isAttached())
        		main.add(centerPanel);
        	AsyncCallback<UserSession> cb =
                    new AsyncCallback<UserSession>() {

                        public void onSuccess(UserSession result) {
                           
                      	  if(result.isSession())
                      	  {
                      		  if(result.getRole() == Role.ROLE_ADMIN)
                      		  {
                      			 
                      			initAdminLogin();
                      		  }
                      		  else
                      			  History.newItem(SeadState.HOME.toToken());//no permissions
                      	  }
                      	  else
                      		  History.newItem("login");
                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
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
	                  			initAdminLogin();
	                  		  }
	                  		  else if(result.getRole() == Role.ROLE_CURATOR)
	                  		  {                      			
	                  			initCuratorLogin();
	                  		  }
	                  		 else
	                  		  {                      			
	                  			initUserLogin();
	                  		  }
	                  		  initLogoutButton();
	                  		  
	                  		 String displayName = "";
							if(result.getfName()!=null)
								displayName = result.getfName();
							if(result.getlName()!=null)
								displayName += " "+result.getlName();
						
							Label displayNameLabel = new Label();
							displayNameLabel.setStyleName("welcomeFont");
							displayNameLabel.setText("Welcome "+displayName);
							loginPanel.clear();
							loginPanel.add(displayNameLabel);
	                  		  
                  		    if(!logoutButton.isAttached())
                  			  loginPanel.add(logoutButton);
	                  		  
	                  		dataHistory.setStyleName("OptionSelected");
	                        adminPage.setStyleName("Option"); 
	                        home.setStyleName("Option");
	                        uploadData.setStyleName("Option");
	                        curatorPage.setStyleName("Option");
                      	  }
                      	  else
                      		  History.newItem(SeadState.LOGIN.toToken());

                        }

                        public void onFailure(Throwable error) {
                            Window.alert("Failed to login: "
                                    + error.getMessage());
                             
                        }
                    };
                    
            

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
        return accessurl + "tastream/" + id;
    }
    
    public static String packageLinkURLnoEncoding(String id) {
        id=id.replace("%2F","/").replace(":", "%3A");
        return accessurl + "packageLink/" + id;
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
    	FlexTable table = new FlexTable();
    	table.setText(0, 0, "");
    	table.setStyleName("FooterTable");
        Label aboutLabel = Util.label("About", "Hyperlink");
        Util.addColumn(table, aboutLabel);
        aboutLabel.addStyleName("LeftPad");
        aboutLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.open("http://sead-data.net/","_blank","");
			}
		});
        
        Label faq = Util.label("FAQ", "Hyperlink");
        Util.addColumn(table, faq);
        faq.addStyleName("LeftPad");
        faq.addClickHandler(new ClickHandler() {
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
        
        Label privacyLabel = Util.label("Privay Policy", "Hyperlink");
        Util.addColumn(table, privacyLabel);
        privacyLabel.addStyleName("LeftPad");
        privacyLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});

        /*set.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String s = accessurl_tb.getText().trim();

                if (!s.isEmpty()) {
                    accessurl = s;
                    History.fireCurrentHistoryState();
                }
            }
        });*/

        return table;
    }

    
    Grid upload;
    
    void initCuratorLogin(){
    	home.setStyleName("Option");
		features.setStyleName("Option");
		resources.setStyleName("Option");
		partners.setStyleName("Option");
		uploadData.setStyleName("Option");
		curatorPage.setStyleName("OptionSelected");
		activityPage.setStyleName("Option");
		dataHistory.setStyleName("Option");
		
		 if(!home.isAttached())
			 optionsHorz.add(home);
		 
		 if(!features.isAttached())
			 optionsHorz.add(features);
		 
		 if(!resources.isAttached())
			 optionsHorz.add(resources);
		 
		 if(!partners.isAttached())
			 optionsHorz.add(partners);
		
		 if(!uploadData.isAttached())
			 optionsHorz.add(uploadData);   	 	
		 
		 if(!curatorPage.isAttached())
  	 		optionsHorz.add(curatorPage);
   	
		 if(!activityPage.isAttached())
	  	 		optionsHorz.add(activityPage);
		 
		 if(!dataHistory.isAttached())
	  	 		optionsHorz.add(dataHistory);
    }
    
    void initUserLogin(){
    	home.setStyleName("Option");
		features.setStyleName("Option");
		resources.setStyleName("Option");
		partners.setStyleName("Option");
		uploadData.setStyleName("OptionSelected");
		activityPage.setStyleName("Option");
		dataHistory.setStyleName("Option");
		
		 if(!home.isAttached())
			 optionsHorz.add(home);
		 
		 if(!features.isAttached())
			 optionsHorz.add(features);
		 
		 if(!resources.isAttached())
			 optionsHorz.add(resources);
		 
		 if(!partners.isAttached())
			 optionsHorz.add(partners);
		
		 if(!uploadData.isAttached())
			 optionsHorz.add(uploadData);   	 	
	
		 if(!activityPage.isAttached())
	  	 		optionsHorz.add(activityPage);
		 
		 if(!dataHistory.isAttached())
	  	 		optionsHorz.add(dataHistory);
    }
    
    void initAdminLogin(){
    	home.setStyleName("Option");
		features.setStyleName("Option");
		resources.setStyleName("Option");
		partners.setStyleName("Option");
		uploadData.setStyleName("Option");
		curatorPage.setStyleName("Option");
		adminPage.setStyleName("OptionSelected");
		activityPage.setStyleName("Option");
		dataHistory.setStyleName("Option");
		
		 if(!home.isAttached())
			 optionsHorz.add(home);
		 
		 if(!features.isAttached())
			 optionsHorz.add(features);
		 
		 if(!resources.isAttached())
			 optionsHorz.add(resources);
		 
		 if(!partners.isAttached())
			 optionsHorz.add(partners);
		
		 if(!uploadData.isAttached())
			 optionsHorz.add(uploadData);   	 	
		 
		 if(!curatorPage.isAttached())
  	 		optionsHorz.add(curatorPage);
		 if(!adminPage.isAttached())
  	 		optionsHorz.add(adminPage);
   	
		 if(!activityPage.isAttached())
	  	 		optionsHorz.add(activityPage);
		 
		 if(!dataHistory.isAttached())
	  	 		optionsHorz.add(dataHistory);
		
    }
    
    
    void initNoLogin(){
    	
    	 home.setStyleName("OptionSelected");
		 features.setStyleName("Option");
		// team.setStyleName("Option");
		 resources.setStyleName("Option");
		 partners.setStyleName("Option");
		 
		 if(!home.isAttached())
			 optionsHorz.add(home);
		 
		 if(!features.isAttached())
			 optionsHorz.add(features);
		 
		 if(!resources.isAttached())
			 optionsHorz.add(resources);
		 
		 if(!partners.isAttached())
			 optionsHorz.add(partners);
		
    	if(curatorPage.isAttached())
   	 		optionsHorz.remove(curatorPage);
    	if(adminPage.isAttached())
   	 		optionsHorz.remove(adminPage);
    	
		optionsHorz.remove(uploadData);   	 	
		optionsHorz.remove(activityPage);
		optionsHorz.remove(dataHistory);
    }
}
