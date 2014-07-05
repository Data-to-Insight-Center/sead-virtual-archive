package org.dataconservancy.dcs.access.client.view;

import org.dataconservancy.dcs.access.ui.client.Util;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class EditView extends Composite implements org.dataconservancy.dcs.access.client.presenter.EditPresenter.Display{

    VerticalPanel publishContainer;
    CaptionPanel metadataPanel;
    CaptionPanel contentPanel;
    Panel displayPanel;

    String id;
    TextBox name;
    TextArea descriptionArea;
    Button saveButton;
    Button backButton;


    public EditView(String identifier) {
        this.id = identifier;
        publishContainer =  new VerticalPanel();
        metadataPanel = new CaptionPanel("Research Object Metadata");
        contentPanel = new CaptionPanel("Research Object Content");

        metadataPanel.setStyleName("CaptionPanelStyle");
        contentPanel.setStyleName("CaptionPanelStyle");
        metadataPanel.setHeight(Window.getClientHeight()/5+"px");
        //contentPanel.setHeight(Window.getClientHeight()/2.6+"px");

        displayPanel = new FlowPanel();
        //displayPanel.setHeight(Window.getClientHeight()/3+"px");
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        verticalPanel.setHeight("100%");

        verticalPanel.add(Util.label("To edit, click on entities below", "greenFont-padded"));

        verticalPanel.add(displayPanel);

        contentPanel.add(verticalPanel);

        publishContainer.add(metadataPanel);
        publishContainer.add(contentPanel);
        Grid project = new Grid(2,2);
        Label projectName = new Label("Title");
        name = new TextBox();
        name.setEnabled(false);

        Label projectDescription = new Label("Abstract");
        descriptionArea = new TextArea();
        descriptionArea.setEnabled(false);
        project.setCellSpacing(3);
        project.setCellPadding(3);
        project.setWidget(0, 0, projectName);
        project.setWidget(0, 1, name);
        project.setWidget(1, 0, projectDescription);
        project.setWidget(1, 1, descriptionArea);
        metadataPanel.add(project);


        HorizontalPanel previewButtonPanel =  new HorizontalPanel();
        previewButtonPanel.setWidth("600px");
        saveButton = new Button("Save changes");
        backButton = new Button("Back");

        previewButtonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        previewButtonPanel.add(backButton);
        previewButtonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        previewButtonPanel.add(saveButton);

        publishContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        publishContainer.add(previewButtonPanel);


    }

    @Override
    public VerticalPanel getPublishContainer() {
        return publishContainer;
    }

    @Override
    public Panel getContentPanel(){
        return this.displayPanel;
    }

    @Override
    public CaptionPanel getMetadataPanel(){
        return this.metadataPanel;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public TextBox getName()
    {
        return this.name;
    }

    @Override
    public Button getSaveButton(){
        return this.saveButton;
    }

    @Override
    public Button getBackButton(){
        return this.backButton;
    }

    @Override
    public TextArea getAbstract() {
        return descriptionArea;
    }

}
