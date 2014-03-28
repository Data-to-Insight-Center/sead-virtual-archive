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


import java.util.UUID;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.api.MediciService;
import org.dataconservancy.dcs.access.client.api.MediciServiceAsync;
import org.dataconservancy.dcs.access.server.MediciServiceImpl;
import org.dataconservancy.dcs.access.shared.UserSession;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

public class UploadBagDialog {

	DialogBox dBox;
	public static final MediciServiceAsync mediciService = GWT.create(MediciService.class);
	
	public UploadBagDialog(String bagUrl)
	{
		dBox = new DialogBox(false, true);

		
		Panel panel = new FlowPanel();

        dBox.setAnimationEnabled(true);
        dBox.setText("Upload Bag as a .zip file");
        dBox.setWidget(panel);
        dBox.center();

        final HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(5);

        Button upload = new Button("Upload");
        Button cancel = new Button("Cancel");

        buttons.add(upload);
        buttons.add(cancel);

        final FormPanel form = new FormPanel();
        FlowPanel formcontents = new FlowPanel();
        form.add(formcontents);

        Hidden depositurl = new Hidden("bagUrl");
        depositurl.setValue(bagUrl);
        
        final FileUpload upfile = new FileUpload();
        upfile.setName("file");

        formcontents.add(upfile);
        formcontents.add(depositurl);
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setAction(SeadApp.BAG_UPLOAD_URL);

        panel.add(new Label("Uploaded files will be included in the SIP."));
        panel.add(form);
        panel.add(buttons);

        upload.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                form.submit();
            }
        });

        cancel.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                dBox.hide();
            }
        });

        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {
                if (event.getResults() == null) {
                    Window.alert("File upload failed");
                    dBox.hide();
                    return;
                }

                String[] parts = event.getResults().split("\\^");

                if (parts.length != 3) {
                    Window.alert("File upload failed: " + event.getResults());
                    dBox.hide();
                    return;
                }

                final String sipPath = parts[1].trim();
                dBox.hide();
                SeadApp.userService.checkSession(null, new AsyncCallback<UserSession>() {
        			
        			@Override
        			public void onSuccess(final UserSession userSession) {
        				 mediciService.generateWfInstanceId(new AsyncCallback<String>() {
        						
        						@Override
        						public void onSuccess(final String wfInstanceId) {
        							WfEventRefresherPanel eventRefresher = new WfEventRefresherPanel(userSession.getEmail(), wfInstanceId);
        							eventRefresher.show();
        							mediciService.submitMultipleSips(
        					                SeadApp.deposit_endpoint + "sip",
        					                null,
        									null,
        									sipPath.replace("_0.xml", ""), 
        									wfInstanceId,
        									null,
        									0, 0, "", "", false, GWT.getModuleBaseURL(),SeadApp.tmpHome,
        									new AsyncCallback<String>() {
        										
        										@Override
        										public void onSuccess(String result) {
//        											Window.alert("Done:" + result);
        											MessagePopupPanel popUpPanel = new MessagePopupPanel(result, "done", true);
        											popUpPanel.show();
        										}
        										
        										@Override
        										public void onFailure(Throwable caught) {
        											;
        										}
        									});
        						}
        						
        						@Override
        						public void onFailure(Throwable caught) {
        							;
        						}
        					});
        	            }

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}
        	        });
            }
        });
	}   
}
