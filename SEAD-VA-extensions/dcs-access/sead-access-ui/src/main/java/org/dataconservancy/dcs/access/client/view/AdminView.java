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

import org.dataconservancy.dcs.access.client.Util;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminView extends Composite implements org.dataconservancy.dcs.access.client.presenter.AdminPresenter.Display{
	
	TabPanel adminPanel;
	
	Grid userGrid;
	Button saveButton;

	public AdminView(){
		
		adminPanel = new TabPanel();
		adminPanel.setWidth("100%");
		FlowPanel usersPanel = new FlowPanel();
		usersPanel.setWidth("100%");
		
		adminPanel.add(usersPanel,"Users");
		
		userGrid = new Grid(100,6);
		userGrid.setWidth("100%");
		userGrid.setWidget(0,0, Util.label("Name","SubSectionHeader"));
		userGrid.setWidget(0,1, Util.label("Email","SubSectionHeader"));
		userGrid.setWidget(0,2, Util.label("User","SubSectionHeader"));
		userGrid.setWidget(0,3, Util.label("Non-SEAD user","SubSectionHeader"));
		userGrid.setWidget(0,4, Util.label("Administrator","SubSectionHeader"));
		userGrid.setWidget(0,5, Util.label("Status","SubSectionHeader"));
		
		usersPanel.add(userGrid);
		
		saveButton = new Button("Save");
				
		adminPanel.selectTab(0);
		
		
	}

	@Override
	public Grid getUserGrid() {
		return userGrid;
	}

	@Override
	public TabPanel getAdminPanel() {
		return adminPanel;
	}
	
	@Override
	public Button getSaveButton() {
		return saveButton;
	}

}
