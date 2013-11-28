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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
 
public class MessagePopupPanel extends PopupPanel {
 private Grid grid;
 Label insideProgressPanel = new Label("Progress");
 Button close;
 static Map<String,String> imageMap = new HashMap<String,String>();
 
 static{
	 imageMap.put("wait", "images/wait.gif");
	 imageMap.put("done", "images/done.png");
	 imageMap.put("bag", "images/bag.jpg");
	 imageMap.put("match", "images/match.jpg");
	 imageMap.put("failed", "images/failed.jpg");
 }
 
 Image image ;
 public MessagePopupPanel(String text, String symbol, Boolean closeOption) {
	 super(false);
	 grid = new Grid(4, 2);
	 grid.getRowFormatter().setStyleName(0, "statusPopupPanelText");
	 grid.setStyleName("statusPopupPanel");
	 setWidget(grid);
	 setStyleName("statusPopupPanelContainer");
	 grid.setText(0, 1, text);
	
	 if(symbol.equalsIgnoreCase("wait"))
		 image= new Image("images/wait.gif");
	 else if(symbol.equalsIgnoreCase("done"))
		 image= new Image("images/done.png");
	 else if(symbol.equalsIgnoreCase("failed"))
		 image= new Image("images/failed.jpg");
	 else if(symbol.equalsIgnoreCase("bag"))
		 image= new Image("images/bag.jpg");
	 
	 grid.setWidget(0, 0, image);
	 
	 close = new Button("Close");
		
	 close.addClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {	
			hidePanel();
		}
	 });
	 if(closeOption)
	 {	
		 grid.setWidget(3, 1, close);
		 Panel progressPanel = new FlowPanel();
		 progressPanel.setStyleName("progressBarContainer");
		 insideProgressPanel.setWidth("0%");
//		 insideProgressPanel.setHeight("70%");
		 insideProgressPanel.setStyleName("Gradient");
		 progressPanel.add(insideProgressPanel);
//		 grid.setWidget(2, 1, progressPanel);
	 } 
	
 }
 
 private void hidePanel(){
	 this.hide();
 }
 public void setText(String str) {
 grid.setText(0, 1, str);
 }
 
 public void setValue(String str, String symbol) {
	 grid.setText(0, 1, str);
	 Image image = new Image
				(imageMap.get(symbol));
	 grid.setWidget(0, 0, image);
	 grid.remove(insideProgressPanel);
 }
 
 
 public void setValue(String str, String details, String symbol) {
	 grid.setText(0, 1, str);
	 Image image = new Image
				(imageMap.get(symbol));
	 grid.setWidget(0, 0, image);
	 grid.setText(1, 1, "Details:" + details);
	 grid.remove(insideProgressPanel);
 }
 public void setValue(String str, String details, String symbol, int percent) {
	 grid.setText(0, 1, str);
	 Image image = new Image
				(imageMap.get(symbol));
	 grid.setWidget(0, 0, image);
	 if(details.length()>0)
		 details = "Details:" + details;
	 grid.setText(1, 1, details);
	 String percentage = String.valueOf(percent)+"%";
	 insideProgressPanel.setText("Progress "+percentage);
	 insideProgressPanel.setWidth(String.valueOf(percent/2)+"%");
//	 insideProgressPanel.setHeight("70%");
	 grid.setWidget(2, 1, insideProgressPanel);
	 grid.setWidget(3, 1, close);
 }
 
 public void setSuccess(){
	 Image image = new Image
				("images/done.png");
	 grid.setWidget(0, 0, image);
	 grid.setText(0, 1, "Indexed and Published Successfully");
	 }
}