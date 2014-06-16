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


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.event.EntityEditEvent;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.client.view.PublishDataView;

public class UploadBagDialog {

	DialogBox dBox;

	
	public UploadBagDialog(String bagUrl
						//, final CaptionPanel researchObjectPanel, final Button ingestButton
						)
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
              
                String[] tempString = event.getResults().split(";");
                final String sipPath = tempString[tempString.length-1].split("<")[0];
                String jsonString = event.getResults();
                jsonString = jsonString.substring(jsonString.indexOf('{'), jsonString.lastIndexOf('}')+1);
              
                dBox.hide();
        		
                JsDcp dcp = JsDcp.create();
	            JsSearchResult result = JsSearchResult.create(jsonString);
	            for (int i = 0; i < result.matches().length(); i++) {
                    Util.add(dcp, result.matches().get(i));
                }
	            	           
		        PublishDataView.EVENT_BUS.fireEvent(new EntityEditEvent(dcp, true, sipPath));
            }
        });
	}
}
