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
import org.dataconservancy.dcs.access.client.model.ProvenanceDS;
import org.dataconservancy.dcs.access.shared.UserSession;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;

public class ProvenancePresenter implements Presenter {

	Display display;
	Panel provPanel;
	
	public interface Display{
		Panel getProvenancePanel();
		Image getLoadImage();
	}
	
	public ProvenancePresenter(Display view){
		this.display = view;
	}
	@Override
	public void bind() {
		provPanel = this.display.getProvenancePanel();
		final Image image = this.display.getLoadImage();
		SeadApp.userService.checkSession(null, new AsyncCallback<UserSession>() {
			@Override
			public void onSuccess(UserSession result) {
				
				TreeGrid treeGrid =  new TreeGrid();
				ProvenanceDS provDS = ProvenanceDS.getInstance(result.getEmail()); 
		        treeGrid.setDataSource(provDS);
		        
				treeGrid.setStyleName("Center");
		        treeGrid.setLoadDataOnDemand(false);  
		        treeGrid.setWidth("80%");  
		        treeGrid.setHeight("70%");  
		        
		        treeGrid.setCanEdit(true);  
		        treeGrid.setNodeIcon("icons/graph.png");  
		        treeGrid.setFolderIcon("icons/graph.png");  
		        treeGrid.setAutoFetchData(true);  
		        treeGrid.setCanFreezeFields(true);  
		        treeGrid.setCanReparentNodes(true);          
		  
		        TreeGridField nameField = new TreeGridField("id");  
		        nameField.setFrozen(true);  
		  
		        TreeGridField jobField = new TreeGridField("name");  
		        TreeGridField employeeTypeField = new TreeGridField("type");  
		        TreeGridField employeeStatusField = new TreeGridField("status");  
		        TreeGridField salaryField = new TreeGridField("date");  
		        
		        treeGrid.setFields(nameField, jobField, employeeTypeField,employeeStatusField,  
		                salaryField);  
				
		        treeGrid.draw();
		        provPanel.remove(image);
		        provPanel.add(treeGrid);
			}
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Failed:"+caught.getMessage());
			}
        });
	}
	
	@Override
	public void display(Panel mainContainer, Panel facetContent, Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		mainContainer.clear();
		facetContent.clear();
		bind();
		mainContainer.add(provPanel);
	}

}
