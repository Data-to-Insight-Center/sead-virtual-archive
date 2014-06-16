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

import com.google.gwt.user.client.ui.*;

public class AdminView extends Composite implements org.dataconservancy.dcs.access.client.presenter.AdminPresenter.Display{
	
	TabPanel adminPanel;
	
	Panel usersPanel;
	Button saveButton;

	public AdminView(){
		
		adminPanel = new TabPanel();
		adminPanel.setWidth("100%");
		usersPanel = new FlowPanel();
		usersPanel.setWidth("100%");
		adminPanel.add(usersPanel,"Users");
		saveButton = new Button("Save");
		adminPanel.selectTab(0);
	}

	@Override
	public TabPanel getAdminPanel() {
		return adminPanel;
	}
	
	@Override
	public Button getSaveButton() {
		return saveButton;
	}
	
	@Override
	public Panel getUsersPanel(){
		return usersPanel;
	}

}
