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
import org.dataconservancy.dcs.access.client.upload.DeliverableUnitEditor;
import org.dataconservancy.dcs.access.client.upload.FileEditor;

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

public class UploadCollectionDialog {

	DialogBox dBox;
	
	public UploadCollectionDialog(final TabPanel files, final TabPanel dus, final List<String> fileids,final List<String> duids,final List<String> colids, String fileUploadUrl)
	{

		for(int i=0;i<3;i++){
               files.setVisible(true);
               String id = nextFileId();
               fileids.add(id);
               files
                       .add(new FileEditor(id, "test","test"),
                            id);
               files.selectTab(files.getWidgetCount() - 1);
		}
		for(int i=0;i<3;i++){
           dus.setVisible(true);
           String id = nextDeliverableUnitId();
           duids.add(id);
           dus.add(new DeliverableUnitEditor(id, fileids, duids, colids,null),
                   id);
           dus.selectTab(dus.getWidgetCount() - 1);
		}
	}
	
	 private String nextFileId() {
	        return "file" + ++SeadApp.fileseq;
	    } 
	 private static String nextDeliverableUnitId() {
	        return "du" + ++SeadApp.duseq;
	    }
	   
	  
}
