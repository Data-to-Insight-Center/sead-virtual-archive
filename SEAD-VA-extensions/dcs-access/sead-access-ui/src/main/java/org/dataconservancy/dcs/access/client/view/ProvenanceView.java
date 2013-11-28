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
import org.dataconservancy.dcs.access.client.model.ProvenanceDS;
import org.dataconservancy.dcs.access.client.presenter.ProvenancePresenter;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabPanel;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.sun.mail.handlers.image_gif;

public class ProvenanceView extends Composite implements ProvenancePresenter.Display{
	
	FlowPanel provenancePanel;
	TreeGrid treeGrid; 
	Image loadImage;
	public ProvenanceView(){
		provenancePanel = new FlowPanel();
		provenancePanel.setWidth("100%");
		Label lbl = new Label("Monitor Workflows");
		lbl.setStyleName("SectionHeader");
		final int width = Window.getClientWidth();
		lbl.getElement().getStyle().setProperty("marginLeft",String.valueOf((width/2)-70)+"px");
	
		provenancePanel.add(lbl);
		loadImage =  new Image("images/loading.gif");
		loadImage.setHeight(String.valueOf(width/80)+"px");
		loadImage.setWidth(String.valueOf(width/80)+"px");
		loadImage.getElement().getStyle().setProperty("marginLeft",String.valueOf((width/2)-20)+"px");
		final int height = Window.getClientHeight();
		loadImage.getElement().getStyle().setProperty("marginTop",String.valueOf((height/4))+"px");
		provenancePanel.add(loadImage);

	}

	
	@Override
	public Panel getProvenancePanel() {
		return provenancePanel;
	}


	@Override
	public Image getLoadImage() {
		return loadImage;
	}

}
