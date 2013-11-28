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
package org.dataconservancy.dcs.access.client.model;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.DepositService;
import org.dataconservancy.dcs.access.client.api.DepositServiceAsync;
import org.dataconservancy.dcs.access.shared.UserSession;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Encapsulates core metadata fields that can be associated with DCS entities.
 */
public final class JsCoreMetadata
        extends JsModel{

    protected JsCoreMetadata() {
    }
    public String getTitle() {
        return getString("title");
    }
    
    public JsSubmitter getSubmitter(){
    	return (JsSubmitter)getObject("submitter");
    }
    
    public JsArrayString getSubjects() {
        return getStrings("subjects");
    }

    public String getType() {
        return getString("type");
    }

    public final String getContact() {
        return getString("contact");
    }
    
    public String getRights() {
        return getString("rights");
    }
    
    public static final DepositServiceAsync depositService = GWT.create(DepositService.class);
   
    public Widget display(final String entityId, final CellTree tree) {
    	final Grid grid = //new Grid(7,2);
        
      	Util.createGrid(7, 2, 
    					"Title:","", "Creators:", "Contact:","Subjects:", "Type:", "Rights:");
        
        grid.getCellFormatter().setStyleName(1, 1, "PaddedCell");

        final HorizontalPanel hp = new HorizontalPanel();
      
        hp.setSpacing(5);
        
        final Image image= new Image("images/wait.gif");
        final Label delete = Util.label("Delete [X]","SimplerButton");
        final Label title = Util.label(getTitle(),"HeadingField");
        
        final Label expand = new Label("[+]");
        expand.setStyleName("Expand");
        
        final Label collapse = new Label("[-]");
    	collapse.setStyleName("Collapse");
    	final ScrollPanel treePanel = getTree(tree);
    	
    	delete.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				depositService.deleteCollection(entityId, SeadApp.accessurl, new AsyncCallback<Boolean>() {
					
					@Override
					public void onSuccess(Boolean result) {
						Window.alert("The collection was deleted.");
						History.newItem(SeadState.HOME.toToken());
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Sorry, the collection could not be deleted.");
						History.newItem(SeadState.HOME.toToken());
					}
				});
				
			}
		});
        
        expand.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hp.remove(expand);
				hp.remove(title);
				int del = 0;
				if(delete.isAttached()){
            		hp.remove(delete);
            		del = 1;
				}
				hp.add(collapse);
				hp.add(title);
				if(del==1)
					hp.add(delete);
				if(tree!=null){
					//grid.setWidget(1, 0, new Label("Contains"));
		        	grid.setWidget(1, 1, treePanel);
				}
				else
				{
					History.newItem("related;"+entityId);
				}
			}
		});
        
        
    	collapse.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				grid.remove(treePanel);
				hp.remove(collapse);
				hp.remove(title);
				int del = 0;
				if(delete.isAttached()){
            		hp.remove(delete);
            		del = 1;
				}
				hp.add(expand);
				hp.add(title);
				if(del==1)
					hp.add(delete);
			}
		});
        //TODO:Loading of related files takes too much time
        if(tree==null){
        	
        	hp.add(expand);
        }
        else{
        	
        
        	hp.add(collapse);
        	//hp.add(collapse);
        }
        hp.add(title);
        
        hp.add(image);
        final AsyncCallback<UserSession> cb =
                new AsyncCallback<UserSession>() {

                    public void onSuccess(final UserSession result) {
                    	if(image.isAttached())
                    		hp.remove(image);
                    	if(getSubmitter().getSubmitterId().equals(result.getEmail())){
                    		hp.add(delete);
                    	}
                    }
                    public void onFailure(Throwable error) {
                        Window.alert("Failed to login: "
                                + error.getMessage());
                         
                    }
	   };

	   SeadApp.userService.checkSession(null,cb);
        
        
        grid.setWidget(0, 1, hp);
        grid.setWidget(2, 1, new Label(getContact()));
        grid.setWidget(3, 1, new Label(toString(getSubjects())));
        grid.setWidget(4, 1, new Label(getType()));
        grid.setWidget(5, 1, new Label(getRights()));

        if(tree!=null)
        	grid.setWidget(1, 1, treePanel);
        
        return grid;
    }
    
    private ScrollPanel getTree(CellTree tree){
    	ScrollPanel filesPanel = new ScrollPanel();
    	filesPanel.setStyleName("hideHorzScroll");
    	if(tree==null)
    		return null;
    	HorizontalPanel panel = new HorizontalPanel();
    	panel.add(tree);
    	filesPanel.add(panel);
    	filesPanel.setWidth("100%");
    	return filesPanel;
    }
}
