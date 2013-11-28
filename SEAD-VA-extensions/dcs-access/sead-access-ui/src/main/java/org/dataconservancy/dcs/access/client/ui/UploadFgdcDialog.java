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

import java.util.List;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter;
import org.dataconservancy.dcs.access.client.upload.FileEditor;
import org.dataconservancy.dcs.access.client.view.MediciIngestView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
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
import com.google.gwt.user.client.ui.TabPanel;

public class UploadFgdcDialog {

	DialogBox dBox;
	public UploadFgdcDialog(String fileUploadUrl)
	{
		dBox = new DialogBox(false, true);

		
		Panel panel = new FlowPanel();

        dBox.setAnimationEnabled(true);
      //  dBox.setText("Upload local file");
        dBox.setStyleName("dialogBox");
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

        final FileUpload upfile = new FileUpload();
        upfile.setName("file");

        Hidden depositurl = new Hidden("depositurl");
        depositurl.setValue(fileUploadUrl);
        		//depositConfig.fileUploadUrl());

        formcontents.add(upfile);
        formcontents.add(depositurl);

        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setAction(SeadApp.FILE_UPLOAD_URL);

        panel.add(Util.label("Upload Metadata file to be included in the SIP.","greenFont"));
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

                if (parts.length != 4) {
                    Window.alert("File upload failed: " + event.getResults());
                    dBox.hide();
                    return;
                }

                String filesrc = parts[1].trim();
                
                MediciIngestPresenter.metadataSrc = filesrc; 
                dBox.hide();
                MediciIngestPresenter.mdCb.setText("Uploaded Metadata!");
                MediciIngestPresenter.mdCb.setEnabled(false);
                
                //Fire an event to update FGDC's successful update
            }
        });
	}
}
