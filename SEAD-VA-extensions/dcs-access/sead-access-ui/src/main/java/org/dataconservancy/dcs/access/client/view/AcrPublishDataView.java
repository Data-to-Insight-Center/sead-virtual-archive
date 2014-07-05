package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.user.client.ui.*;

public class AcrPublishDataView extends Composite implements org.dataconservancy.dcs.access.client.presenter.AcrPublishDataPresenter.Display{

    VerticalPanel publishContainer;
    CaptionPanel projectDesciptionPanel;
    CaptionPanel researchObjectPanel;
    CaptionPanel licensePanel;
    Button previewButton;

    ListBox projectList;
    ListBox ROList;
    HorizontalPanel previewButtonPanel;



    public AcrPublishDataView() {
        publishContainer =  new VerticalPanel();
        projectDesciptionPanel = new CaptionPanel("Project Descritpion");
        researchObjectPanel = new CaptionPanel("Research Object");
        licensePanel = new CaptionPanel("License  ");

        projectDesciptionPanel.setStyleName("CaptionPanelStyle");
        researchObjectPanel.setStyleName("CaptionPanelStyle");
        licensePanel.setStyleName("CaptionPanelStyle");

        publishContainer.add(projectDesciptionPanel);
        publishContainer.add(researchObjectPanel);
        //publishContainer.add(licensePanel);

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

        ROList = new ListBox(); //contains names of datasets

        ROGrid.setCellSpacing(3);
        ROGrid.setCellPadding(3);
        ROGrid.setWidget(0, 0, ROLabel);
        ROGrid.setWidget(0, 1, ROList);

        researchObjectPanel.add(ROGrid);

        CheckBox licenseBox = new CheckBox("By clicking this checkbox, I certify that I agree to release my research data under the terms of the Creative Commons license");
        //	licensePanel.add(licenseBox);

        previewButtonPanel =  new HorizontalPanel();
        previewButtonPanel.setWidth("600px");
        previewButton = new Button("Preview");
        previewButtonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        previewButtonPanel.add(previewButton);
        publishContainer.add(previewButtonPanel);
    }


    @Override
    public VerticalPanel getPublishContainer() {
        return publishContainer;

    }


    @Override
    public ListBox getProjectList() {	//getIr in PresenterCode. mapping here accordingly
        return projectList;
    }


    @Override
    public ListBox getROList() {
        return ROList;
    }


    @Override
    public Button getPreviewButton() {
        return previewButton;
    }



    @Override
    public CaptionPanel getROPanel() {
        return researchObjectPanel;
    }


    @Override
    public Panel getButtonPanel() {
        return previewButtonPanel;
    }




}