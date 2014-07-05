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

import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.user.client.ui.*;

public class NotificationPopupPanel extends PopupPanel {

	public NotificationPopupPanel(String errorMessageStr, boolean done) {
		super(false);
		this.setGlassEnabled(true);
		this.show();
		this.setStyleName("notifyPopupContainer");
		this.setPopupPosition((int) Math.ceil(Window.WINDOW_WIDTH / 2.5),
				Window.WINDOW_HEIGHT / 2);
		VerticalPanel outerPanel = new VerticalPanel();
		outerPanel.setSpacing(10);
		outerPanel.setWidth("100%");
		outerPanel.setHeight(Window.WINDOW_HEIGHT / 5 + "px");
		outerPanel.setStyleName("notifyPopupContainer");
		setWidget(outerPanel);
		outerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

		Label errorMessage = new Label();
		errorMessage.setWidth("90%");
		errorMessage.setStyleName("greenFont");
		errorMessage.setText(errorMessageStr);
		outerPanel.add(errorMessage);
		/*Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});*/
		
		Image image; 
		if(!done)
			image = new Image("images/wait.gif");
		else
			image = new Image("images/done.png");
		
	//	image.setStyleName("Pad");
		Grid grid = new Grid(1,3);

		grid.setWidget(0,0,image);
		
		grid.setWidget(0,2,errorMessage);
		grid.setWidth("90%");
		outerPanel.add(grid);

		outerPanel.setVerticalAlignment(HorizontalPanel.ALIGN_BOTTOM);
	//	outerPanel.add(closeButton);

	}
	
	public NotificationPopupPanel(String errorMessageStr, String done) {
		super(true);
		this.setGlassEnabled(true);
		this.show();
		this.setStyleName("notifyPopupContainer");
		this.setPopupPosition((int) Math.ceil(Window.WINDOW_WIDTH / 2.5),
				Window.WINDOW_HEIGHT / 2);
		VerticalPanel outerPanel = new VerticalPanel();
		outerPanel.setSpacing(10);
		outerPanel.setWidth("100%");
		outerPanel.setHeight(Window.WINDOW_HEIGHT / 5 + "px");
		outerPanel.setStyleName("notifyPopupContainer");
		setWidget(outerPanel);
		outerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

		Label errorMessage = new Label();
		errorMessage.setWidth("90%");
		errorMessage.setStyleName("greenFont");
		errorMessage.setText(errorMessageStr);
		outerPanel.add(errorMessage);
		/*Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});*/
		Grid grid = new Grid(1,2);
		if(done.equalsIgnoreCase("failed"))
			grid.setWidget(0,0,new Image("images/failed.jpg"));
		else
			grid.setWidget(0,0,new Image("images/done.png"));
		grid.setWidget(0,1,errorMessage);
		grid.setWidth("90%");
		outerPanel.add(grid);

		outerPanel.setVerticalAlignment(HorizontalPanel.ALIGN_BOTTOM);
	//	outerPanel.add(closeButton);

	}

	public NotificationPopupPanel(HTML errorMessage) {
		super(false);
		this.setGlassEnabled(true);
		this.show();
		this.setStyleName("notifyPopupContainer");
		this.setPopupPosition((int) Math.ceil(Window.WINDOW_WIDTH / 2.5),
				Window.WINDOW_HEIGHT / 2);
		VerticalPanel outerPanel = new VerticalPanel();
		outerPanel.setSpacing(10);
		outerPanel.setWidth("100%");
		outerPanel.setHeight(Window.WINDOW_HEIGHT / 5 + "px");
		outerPanel.setStyleName("notifyPopupContainer");
		setWidget(outerPanel);
		outerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

		/*
		 * Label errorMessage = new Label(); errorMessage.setWidth("90%");
		 * errorMessage.setStyleName("greenFont");
		 * errorMessage.setText(errorMessageStr);
		 */
		outerPanel.add(errorMessage);
		/*Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});*/

		outerPanel.add(errorMessage);

		outerPanel.setVerticalAlignment(HorizontalPanel.ALIGN_BOTTOM);
	//	outerPanel.add(closeButton);

	}

}
