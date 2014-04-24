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

package org.dataconservancy.dcs.access.client.view;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.ui.UploadFgdcDialog;
import org.dataconservancy.dcs.access.client.upload.Util;
import org.dataconservancy.dcs.access.shared.Role;
import org.dataconservancy.dcs.access.shared.UserSession;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MediciIngestView extends Composite implements org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter.Display{

	HorizontalPanel content;
	Panel mainContentPanel;
	Button getPub;
	Button ingestButton;
	Panel leftPanel;
	Panel rightPanel;
	Panel coverRightPanel;
	ListBox ir ;
	CheckBox restrictCopy;
	CheckBox uploadMetadata;
	Label datasetLbl;
	Label fileLbl;
	Tree tree;
	ListBox sortOrderList;
//	Panel uploadPanel;
//	Panel fileCharacPanel;
//	Panel archivePanel;
//	Panel indexPanel;
	//HorizontalSplitPanel hPanel;
	
	public MediciIngestView(){
		mainContentPanel = new VerticalPanel();
		mainContentPanel.setStyleName("VerticalPanel");
		mainContentPanel.add(Util.label("Select Data from Active Content Repository","BoxHeader"));
		
		Panel coverLeftPanel =new FlowPanel();
		coverRightPanel =new FlowPanel();

		
		getPub = new Button("View Collections to Publish");
		ingestButton = new Button("Ingest Dataset");
		sortOrderList = new ListBox();
		//getPub.setStyleName("ButtonPosition");
		
		int height = Window.getClientHeight();
		final int width = Window.getClientHeight();
	   //System.out.println(height +" px\n");
	    
		leftPanel = new ScrollPanel();
		leftPanel.setStyleName("LeftPanel");
		leftPanel.setHeight((height/2-50)+"px");
		leftPanel.setWidth((width/2.5-5)+"px");
		
		datasetLbl = Util.label("Active Content Repositories","BoxHeader");
		coverLeftPanel.setStyleName("CollectionBorder");
		coverLeftPanel.add(datasetLbl);
		coverLeftPanel.add(sortOrderList);
		sortOrderList.addItem("");
		sortOrderList.addItem("A-Z");
		sortOrderList.addItem("Z-A");
		coverLeftPanel.add(leftPanel);
		coverLeftPanel.setHeight(height/2+"px");
		coverLeftPanel.setWidth((width/2.5)+"px");
		
		rightPanel = new ScrollPanel();
		rightPanel.setStyleName("RightPanel");
		rightPanel.setHeight((height/2-50)+"px");
		rightPanel.setWidth((width-5)+"px");
		
		fileLbl = Util.label("","BoxHeader");
		coverRightPanel.setStyleName("FileBorder");
		coverRightPanel.add(fileLbl);
		coverRightPanel.add(rightPanel);
		coverRightPanel.setHeight(height/2+"px");
		coverRightPanel.setWidth((width)+"px");

	    content = new HorizontalPanel();
	     
	    content.setStyleName("HorizontalPanel2");
	    
	    content.add(coverLeftPanel);
		content.add(coverRightPanel);
		
		
		final Panel buttonPanel = new HorizontalPanel();
		
		final Grid buttonTable = new Grid(2,2);

		
		ir = new ListBox();
		ir.setStyleName("droplist");
            
	        
	        restrictCopy = new CheckBox();
	        
	        restrictCopy.setText("Restrict Access to Data Collection");
	        restrictCopy.setValue(false);
	        restrictCopy.setEnabled(false);
	        restrictCopy.setStyleName("greyFont");
	        
	        ingestButton.setEnabled(false);
	        ingestButton.setStyleName("ButtonPosition");
	        ingestButton.setStyleName("greyFont");
	        
	        getPub.setStyleName("greenFont");
	        getPub.setEnabled(false);
	       
	        buttonTable.setWidget(0,0,ir);
	        //buttonTable.setWidget(1,0,cloudCopy);
	     //  buttonTable.setStyleName("TableBorder");
	        buttonTable.setWidget(0,1,getPub);
	        Panel tempPanel2 = new FlowPanel();
	        tempPanel2.setWidth((width/10)+"px");
	        buttonPanel.add(tempPanel2); 
	    //    buttonTable.setStyleName("Center");
	        buttonPanel.add(buttonTable);
	        buttonTable.setStyleName("topPadding");
	        
	      
	     //   buttonPanel.add(fgdcFileLabel);
//		        buttonPanel.add();
	        Grid grid = new Grid(2,2);
	       
	        grid.setWidget(0,0,restrictCopy);
	        uploadMetadata = new CheckBox();
	        
	        uploadMetadata.setText("Upload Metadata");
	        uploadMetadata.setValue(false);
	        uploadMetadata.setEnabled(false);
	        uploadMetadata.setStyleName("greyFont");
	     //   buttonPanel.add(cloudCopy);
	        grid.setWidget(1,0,uploadMetadata);
	        grid.setWidget(0,1,ingestButton);
	      //  grid.setStyleName("Center");
	        Panel tempPanel3 = new FlowPanel();
	        tempPanel3.setWidth((width/10)+"px");
	        buttonPanel.add(tempPanel3); 

	        buttonPanel.add(grid);
	       
	        // Add a button to upload the file
	        uploadMetadata.addClickHandler(new ClickHandler() {
	          public void onClick(ClickEvent event) {
	           new UploadFgdcDialog(SeadApp.deposit_endpoint + "file");
	          
	          }
	        });
	     //   buttonPanel.add(new HTML("<br>"));
	    //    buttonPanel.add(uploadButton);
	        
	        
	        Panel tempPanel = new FlowPanel();
	        tempPanel.setWidth((width/2.5)+"px");
	        buttonPanel.add(tempPanel);
	        buttonPanel.setStyleName("HorizontalPanel");
	        
	        buttonPanel.setHeight("15%");
			//ir.setEnabled(false);
			mainContentPanel.add(content);
			mainContentPanel.add(buttonPanel);
			
			  
	}

	
	@Override
	public Panel getContent() {
		return this.content;
	}

	@Override
	public Button getPub() {
		return this.getPub;
	}

	public ListBox getSortOrderList(){
		return this.sortOrderList;
	}
	
	
	@Override
	public Panel getLeftPanel() {
		return this.leftPanel;
	}

	@Override
	public Panel getRightPanel() {
		return this.rightPanel;
	}

	@Override
	public Panel getCoverRightPanel() {
		// TODO Auto-generated method stub
		return this.coverRightPanel;
	}

	@Override
	public Button getIngestButton() {
		// TODO Auto-generated method stub
		return this.ingestButton;
	}

	@Override
	public ListBox getIr() {
		return this.ir;
	}

	@Override
	public CheckBox getCloudCopy() {
		return restrictCopy;
	}

	@Override
	public Panel getMainContent() {
		return mainContentPanel;
	}
	
	@Override
	public Label getDatasetLbl() {
		return this.datasetLbl;
	}
	
	@Override
	public Label getFileLbl() {
		return this.fileLbl;
	}


	@Override
	public CheckBox getMetadataCheckBox() {
		// TODO Auto-generated method stub
		return uploadMetadata;
	}

/*	@Override
	public Panel getUploadPanel() {
		return uploadPanel;
	}

	@Override
	public Panel getFileCharacPanel() {
		return fileCharacPanel;
	}

	@Override
	public Panel getArchivePanel() {
		return archivePanel;
	}

	@Override
	public Panel getIndexPanel() {
		return indexPanel;
	}
*/

	
/*	@Override
	public Panel getHPanel() {
		return leftPanel;
	}*/
}
