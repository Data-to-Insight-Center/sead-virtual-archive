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

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.api.DepositService;
import org.dataconservancy.dcs.access.client.api.DepositServiceAsync;
import org.dataconservancy.dcs.access.client.api.UserService;
import org.dataconservancy.dcs.access.client.api.UserServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
 
public class EmailPopupPanel extends PopupPanel {
 private Grid grid = new Grid(1, 2);
 private Grid buttonGrid = new Grid(1, 2);
 private String emailId = "";
 TextBox emailBox;
 
 public EmailPopupPanel(final String entityId) {
 super(false);
 grid.getRowFormatter().setStyleName(0, "emailPopupPanelText");
// grid.setColumnWidth(0, 40, Unit.PX);
 //grid.getColumnFormatter().setStyleName(0, "columnWidth");
 final VerticalPanel panel = new VerticalPanel();

 panel.setStyleName("emailPopupPanel");
 setWidget(panel);
 setStyleName("emailPopupPanelContainer");
 emailBox = new TextBox();
 emailBox.setStyleName("textbox");
 grid.setStyleName("Middle");
 buttonGrid.setStyleName("Middle");
 grid.setWidget(0, 1, emailBox);


 grid.setWidget(0, 0, new Label("Please enter your email address:"));

 Button ok = new Button("Send Email");
 
 Button cancel = new Button("Cancel");
 buttonGrid.setWidget(0, 0, ok);
 //grid.getCellFormatter().setStyleName(1, 0, "LeftPad");
 buttonGrid.setWidget(0, 1, cancel);
 
// panel.setStyleName("Middle");
 panel.add(grid);
 panel.add(buttonGrid);
 ok.addClickHandler(new ClickHandler() {
	
	@Override
	public void onClick(ClickEvent event) {
	
        
		if(emailBox.getText()!=null&&emailBox.getText().length()>0){
			hidePanel();
			final DialogBox db = new DialogBox();
			db.setText("You will receive an email shortly");
			final VerticalPanel panel = new VerticalPanel();
			panel.setStyleName("Middle");
//			panel.setStyleName("alertContainer");
	        
	        final Button buttonClose = new Button("Close",new ClickHandler() {
	            @Override
	            public void onClick(final ClickEvent event) {
	            	db.hide();
	            }
	        });
	        final Label emptyLabel = new Label("");
	        emptyLabel.setSize("auto","25px");
	        panel.add(emptyLabel);
	        panel.add(emptyLabel);
	        buttonClose.setWidth("90px");
	       
	        panel.add(buttonClose);
	        
	        db.add(panel);
	        db.center();
	        db.show();
			final UserServiceAsync userService =
	                GWT.create(UserService.class);
	    	final DepositServiceAsync depositService =
                GWT.create(DepositService.class);
			AsyncCallback<String> callback = new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					AsyncCallback<Void> emailCallback = new AsyncCallback<Void>() {
						
						@Override
						public void onSuccess(Void result) {
							// TODO Auto-generated method stub
							//Do nothing
							
						}
						
						@Override
						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							
						}
					};
					//Window.alert("Sent email");
					if(result!=null){
						String[] recipients = new String[1];
						recipients[0] = emailBox.getText();
						userService.sendEmail(recipients, "Datast ready for download",
							"Your download is ready and can be obtained from the following url(s):\n"
							+ result
							+ ".\n\nThanks,\nSEAD Virtual Archive Team", emailCallback );
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					
				}
			};
			depositService.getLinks(SeadApp.packageLinkURLnoEncoding(entityId),callback);
		}
		else
		{
			Window.alert("Email address cannot be empty");
		}
		
	}
 });
 
 cancel.addClickHandler(new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {	
			emailId = "";
			hidePanel();
		}
	 });	
 
 }
 
/* private boolean isValidEmail(String email) {
	 
	 String emailRegExp = "/^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,3}|[0-9]{1,3})(\]?)$/"; 
	 return !reg1.test(email) && reg2.test(email);
 }*/
 
 private void hidePanel(){
	 this.hide();
 }
 public String getEmailId(){
	 return emailId;
 }
}