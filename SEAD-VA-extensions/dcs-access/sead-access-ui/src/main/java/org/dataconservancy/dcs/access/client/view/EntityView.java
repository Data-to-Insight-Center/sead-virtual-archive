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
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.ui.SeadAdvancedSearchWidget;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class EntityView extends Composite implements org.dataconservancy.dcs.access.client.presenter.EntityPresenter.Display{

	ScrollPanel contentScrollPanel;
	Panel content;
	String entityId;
	
	public EntityView(String id){
		
		this.entityId =id;
		content = new FlowPanel();
	    content.setStylePrimaryName("Content");
	    contentScrollPanel = new ScrollPanel(content);
		content.add(new SeadAdvancedSearchWidget(null, null));
		content.add(Util.label("Entity", "SectionHeader"));
		
	}

	@Override
	public String getEntityId() {
		return entityId;
	}

	@Override
	public Panel getContentPanel() {
		return content;
	}

	@Override
	public Panel getContentScrollPanel() {
		return contentScrollPanel;
	}
}
