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

package org.dataconservancy.dcs.access.client.ui;

import java.util.HashMap;
import java.util.Map;

import org.dataconservancy.dcs.access.client.api.MediciService;
import org.dataconservancy.dcs.access.client.api.MediciServiceAsync;
import org.dataconservancy.dcs.access.client.api.TransformerService;
import org.dataconservancy.dcs.access.client.api.TransformerServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
 
public class MetadataPopupPanel extends PopupPanel {
	 public static final TransformerServiceAsync transformService = GWT.create(TransformerService.class);

 public MetadataPopupPanel(String sourceUrl, String format) {
	 super(false);
	 
	
	 setStyleName("metadataPopupPanelContainer");
     transformService.fgdcToHtml(sourceUrl, format, new AsyncCallback<String>() {
		
		@Override
		public void onSuccess(String result) {
			// TODO Auto-generated method stub
			
			 	HTML html5 = new HTML(result);

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
		
		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			
		}
	});
   
 }
 
 private void hidePanel(){
	 this.hide();
 }

}