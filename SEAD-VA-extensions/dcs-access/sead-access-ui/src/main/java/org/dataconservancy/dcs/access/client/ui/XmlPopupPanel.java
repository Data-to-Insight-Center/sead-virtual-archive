/*
 * Copyright 2014 The Trustees of Indiana University
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

package org.dataconservancy.dcs.access.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dataconservancy.dcs.access.client.api.TransformerService;
import org.dataconservancy.dcs.access.client.api.TransformerServiceAsync;

public class XmlPopupPanel extends PopupPanel {
	 public static final TransformerServiceAsync transformService = GWT.create(TransformerService.class);

 public XmlPopupPanel(String sourceString) {
	 	super(false);
	 	
	 	setStyleName("metadataPopupPanelContainer");
	 	transformService.readFile(sourceString, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(String result) {
				HTML html5 = new HTML("<pre>" + result + "</pre>");
				
			    VerticalPanel flowPanel = new VerticalPanel();
			    ScrollPanel panel = new ScrollPanel(html5);
			   // panel.setStyleName("metadataPopupPanel");
			    //grid.getRowFormatter().setStyleName(0, "statusPopupPanelText");
			    flowPanel.setStyleName("metadataPopupPanel");
			    panel.setStyleName("metadataInPopupPanel");
			    Button close = new Button("[X]");
				
				 close.addClickHandler(new ClickHandler() {
				
					@Override
					public void onClick(ClickEvent event) {
						hidePanel();
					}
				 });
				 Grid grid = new Grid(1,3);
				 close.setStyleName("Center2");
				 grid.setWidget(0, 1, close);
				 flowPanel.add(grid);
				 flowPanel.add(panel);
				
			    setWidget(flowPanel);

			}
		
	 	});
 }
 
 private void hidePanel(){
	 this.hide();
 }

}