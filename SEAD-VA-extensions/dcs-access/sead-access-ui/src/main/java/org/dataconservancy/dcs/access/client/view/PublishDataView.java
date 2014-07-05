package org.dataconservancy.dcs.access.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;

public class PublishDataView extends Composite implements org.dataconservancy.dcs.access.client.presenter.PublishDataPresenter.Display{

    VerticalPanel publishContainer;
    FormPanel form;
    CaptionPanel projectDesciptionPanel;
    CaptionPanel researchObjectPanel;
    CaptionPanel licensePanel;
    Button previewButton;
    Button uploadBag;
    TextBox projectNameTB;
    TextArea abstractTB;
    VerticalPanel warningPanel;
    Label provenanceType;

    CheckBox licenseBox;
    TextBox roId;
    Label errorMessage;

    ListBox projectList;
    ListBox ROList;

    public static EventBus EVENT_BUS = GWT.create(SimpleEventBus.class);



    public PublishDataView() {

        publishContainer =  new VerticalPanel();
        publishContainer.addStyleName("PublishContainer");
        projectDesciptionPanel = new CaptionPanel("Project Description");
        researchObjectPanel = new CaptionPanel("Research Object");
        licensePanel = new CaptionPanel("License  ");

        projectDesciptionPanel.setStyleName("CaptionPanelStyle");
        researchObjectPanel.setStyleName("CaptionPanelStyle");
        licensePanel.setStyleName("CaptionPanelStyle");


        publishContainer.add(researchObjectPanel);
        publishContainer.add(projectDesciptionPanel);
        publishContainer.add(licensePanel);

        Grid project = new Grid(3,2);
        Label projectName = new Label("Project Name");
        projectList = new ListBox(false);
        Label projectDescription = new Label("Project Description");
        project.setCellSpacing(3);
        project.setCellPadding(3);
        projectNameTB = new TextBox();
        projectNameTB.setEnabled(false);
        project.setWidget(0, 0, projectName);
        project.setWidget(0, 1, projectNameTB);
        abstractTB = new TextArea();
        abstractTB.setEnabled(false);
        project.setWidget(1, 0, projectDescription);
        project.setWidget(1, 1, abstractTB);

        VerticalPanel descriptionPanel = new VerticalPanel();
        descriptionPanel.add(project);

        warningPanel = new VerticalPanel();

        descriptionPanel.add(warningPanel);
        projectDesciptionPanel.add(descriptionPanel);

        Grid ROGrid = new Grid(4,2);

        Label uploadLabel = new Label("Upload Local Bag");
        previewButton = new Button("Submit Dataset for Review");

        form = new FormPanel();
        FlowPanel formcontents = new FlowPanel();
        form.add(formcontents);

        Hidden depositurl = new Hidden("bagUrl");
        depositurl.setValue(SeadApp.bagIturl);

        final FileUpload upfile = new FileUpload();
        upfile.setName("file");

        formcontents.add(upfile);
        formcontents.add(depositurl);
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setAction(SeadApp.BAG_UPLOAD_URL);


        Grid uploadGrid = new Grid(4,2);
        uploadGrid.setWidget(1, 0, uploadLabel);
        uploadGrid.setWidget(1, 1, form);
        uploadBag = new Button("Upload");
        uploadGrid.setWidget(3, 1, uploadBag);
        ROGrid.setCellSpacing(3);
        ROGrid.setCellPadding(3);
        //	ROGrid.setWidget(0, 0, ROLabel);
        //ROGrid.setWidget(0, 1, ROList);
        //ROGrid.setWidget(1, 1, new HTML("Or"));
        //ROGrid.setWidget(2, 1, browsePanel);
        //ROGrid.setWidget(2, 1, uploadBag);

        researchObjectPanel.add(uploadGrid);



        Panel innerLicensePanel = new FlowPanel();
        errorMessage = new Label();
        licenseBox = new CheckBox("By clicking this checkbox, I certify that I agree to release my research data under the terms of the Creative Commons license.");
        innerLicensePanel.add(errorMessage);
        innerLicensePanel.add(licenseBox);
        licensePanel.add(innerLicensePanel);

        HorizontalPanel previewButtonPanel =  new HorizontalPanel();
        previewButtonPanel.setWidth("600px");
        previewButtonPanel.setStyleName("Margin");

        Button clearButton = new Button("Start over");

        clearButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                History.newItem(SeadState.UPLOAD.toToken("new"));
            }
        });

        previewButtonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        previewButtonPanel.add(clearButton);

        previewButtonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        previewButtonPanel.add(previewButton);
        previewButton.setEnabled(false);
        publishContainer.add(previewButtonPanel);


    }

    @Override
    public VerticalPanel getPublishContainer() {
        return publishContainer;

    }


    @Override
    public ListBox getIr() {	//getIr in PresenterCode. mapping here accordingly
        return projectList;
    }


    @Override
    public ListBox getROList() {
        return ROList;
    }

    @Override
    public CheckBox getLicenseBox(){
        return licenseBox;
    }

    @Override
    public Button getUploadBag() {
        return uploadBag;
    }

    @Override
    public FormPanel getForm() {
        return form;
    }

    @Override
    public TextBox getProjectNameTB() {
        return projectNameTB;
    }

    @Override
    public TextArea getAbstractTB() {
        return abstractTB;
    }

    @Override
    public VerticalPanel getWarningPanel() {
        return warningPanel;
    }

    @Override
    public Label getProvenanceType() {
        return provenanceType;
    }

    @Override
    public Button getPreviewButton() {
        return previewButton;
    }

    @Override
    public Label getErrorMessage() {
        return errorMessage;
    }

    @Override
    public CaptionPanel getResearchObjectPanel() {
        return researchObjectPanel;
    }

    @Override
    public TextBox getRoId() {
        return roId;
    }


}
