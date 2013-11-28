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
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.api.DbQueryService;
import org.dataconservancy.dcs.access.client.api.DbQueryServiceAsync;
import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.shared.Event;
import org.dataconservancy.dcs.access.shared.ProvenaceDataset;
import org.dataconservancy.dcs.access.shared.ProvenanceRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
 
public class WfEventRefresherPanel extends PopupPanel {
 private Grid grid;
 Label insideProgressPanel = new Label("Progress");
 Button close;
 static Map<String,String> imageMap = new HashMap<String,String>();
 DbQueryServiceAsync dbService =
         GWT.create(DbQueryService.class);
 
 static{
	 imageMap.put("wait", "images/wait.gif");
	 imageMap.put("done", "images/done.png");
	 imageMap.put("bag", "images/bag.jpg");
	 imageMap.put("match", "images/match.jpg");
 }
 
 Image image ;
 String latestDate = "1800-01-01T01:01:01.000Z";
 int doneFlag = 0;
 String dTitle="";
 public WfEventRefresherPanel(final String submitterId, final String wfInstanceId) {
	 super(false);
	 grid = new Grid(4, 2);
	 grid.getRowFormatter().setStyleName(0, "statusPopupPanelText");
	 grid.setStyleName("statusPopupPanel");
	 setWidget(grid);
	 setStyleName("statusPopupPanelContainer");
	 grid.setText(0, 1, "Starting");
	
	 image= new Image("images/wait.gif");
	 grid.setWidget(0, 0, image);
	 
	
	 final Timer timer = new Timer() {
		 @Override
         public void run() {
			 if(doneFlag==1){
				 grid.setText(0, 1, "Ingest Workflow for "+dTitle+" completed");
					image= new Image("images/done.png");
					grid.setWidget(0, 0, image);
					cancel();
					doneFlag = 0;
			 }
			  dbService.getProv(submitterId, wfInstanceId, latestDate, new AsyncCallback<List<ProvenaceDataset>>() {
				  public void onSuccess(List<ProvenaceDataset> result) {
		            	String events = "<ul>";
		            	for(final ProvenaceDataset dataset:result){
		            		for (Map.Entry<String,List<ProvenanceRecord>> entry : dataset.provRecordbyWf.entrySet()) 
		            		{
		            			for(final ProvenanceRecord provRecord: entry.getValue()){
		            				int i =0;
		            				for(Event event:provRecord.getEvents()){
		            					
		            					events+="<li>"+Constants.eventMessages.get(event.getEventType())+ ": Completed</li>";
		            					if(i==provRecord.getEvents().size()-1){
		            						events+="</ul>";
		            						final String temp =events;
		            						dbService.getDate(event.getEventDate(), new AsyncCallback<String>(){

												@Override
												public void onFailure(
														Throwable caught) {
													// TODO Auto-generated method stub
													
												}

												@Override
												public void onSuccess(
														String result) {
													latestDate = result;
													if(provRecord.getStatus()==org.dataconservancy.dcs.access.shared.Status.Completed)
													{	
														doneFlag = 1;
														dTitle = dataset.datasetTitle;
													}
													
													grid.setWidget(0, 1, new HTML(temp));
													image= new Image("images/wait.gif");
													grid.setWidget(0, 0, image);
									         	    
												}
		            							
		            						}
		            						);
		            					}
		            					
		            					i++;
		            				}
		            			}
		            		}
		            	
			         	    //are the dates sorted? looks like it at a glance - please confirm
		            	}
		            }

				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					
				}
			});
	 	}

	};
     timer.scheduleRepeating(4*1000);//every 20 seconds
	 
     close = new Button("Close");
	 close.addClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {	
			hidePanel();
			timer.cancel();
		}
	 });
	 
	 //close button
	 grid.setWidget(3, 1, close);
	 Panel progressPanel = new FlowPanel();
	 progressPanel.setStyleName("progressBarContainer");
	 insideProgressPanel.setWidth("0%");
	 insideProgressPanel.setStyleName("Gradient");
	 progressPanel.add(insideProgressPanel);

	
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