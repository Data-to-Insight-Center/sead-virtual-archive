package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.user.client.ui.*;

public class AcrDataView extends Composite implements org.dataconservancy.dcs.access.client.presenter.AcrDataPresenter.Display{

	VerticalPanel publishContainer;
	CaptionPanel projectDesciptionPanel;
	CaptionPanel researchObjectPanel;
	CaptionPanel licensePanel;
	
	ListBox projectList;
	ListBox ROList;
	
	
	
	public AcrDataView() {
		publishContainer =  new VerticalPanel();
		projectDesciptionPanel = new CaptionPanel("Project Descritpion");
		researchObjectPanel = new CaptionPanel("Research Object");
		licensePanel = new CaptionPanel("License  ");
		
		projectDesciptionPanel.setStyleName("CaptionPanelStyle");
		researchObjectPanel.setStyleName("CaptionPanelStyle");
		licensePanel.setStyleName("CaptionPanelStyle");
		
		publishContainer.add(projectDesciptionPanel);
		publishContainer.add(researchObjectPanel);
		publishContainer.add(licensePanel);
		
		Grid project = new Grid(2,2);
		Label projectName = new Label("Project Name");
		projectList = new ListBox(false);
		Label projectDescription = new Label("Project Description");
		TextArea descriptionArea = new TextArea();
		project.setCellSpacing(3);
		project.setCellPadding(3);
		project.setWidget(0, 0, projectName);
		project.setWidget(0, 1, projectList);
		project.setWidget(1, 0, projectDescription);
		project.setWidget(1, 1, descriptionArea);
		projectDesciptionPanel.add(project);
		
		
		
		
		Grid ROGrid = new Grid(4,2);
		Label ROLabel = new Label("Research Object");
		ROList = new ListBox();
		//Label uploadLabel = new Label ("Upload Local Bag");
		
		//Button browseButton = new Button("...");
		/*
		browseButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new UploadBagDialog(SeadApp.bagIturl, publishContainer, previewButton);
			}
		});*/

		HorizontalPanel browsePanel = new HorizontalPanel();
		/*browsePanel.add(uploadLabel);
		browsePanel.add(browseButton);*/
		Button validateButton = new Button("Validate Bag");
		ROGrid.setCellSpacing(3);
		ROGrid.setCellPadding(3);
		ROGrid.setWidget(0, 0, ROLabel);
		ROGrid.setWidget(0, 1, ROList);
//		ROGrid.setWidget(1, 1, new HTML("Or"));
//		ROGrid.setWidget(2, 1, browsePanel);
//		ROGrid.setWidget(3, 1, validateButton);
		researchObjectPanel.add(ROGrid);
		
		CheckBox licenseBox = new CheckBox("By clicking this checkbox, I certify that I agree to release my research data under the terms of the Creative Commons license");
		licensePanel.add(licenseBox);
		
		HorizontalPanel previewButtonPanel =  new HorizontalPanel();
		previewButtonPanel.setWidth("600px");
		
		previewButtonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		Button previewButton = new Button("Pull and Preview");
		previewButtonPanel.add(previewButton);
		publishContainer.add(previewButtonPanel);
		
		
	}


	@Override
	public VerticalPanel getPublishContainer() {
		return publishContainer;
		
	}


	@Override
	public ListBox getIr() {	//getIr in PresenterCode. mapping here accordingly
		// TODO Auto-generated method stub
		return projectList;
	}


	@Override
	public ListBox getROList() {
		// TODO Auto-generated method stub
		return ROList;
	}

	
	
}
