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

import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.dataconservancy.dcs.access.client.model.DcpTreeModel;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.RegistryService;
import org.dataconservancy.dcs.access.client.api.RegistryServiceAsync;
import org.dataconservancy.dcs.access.client.api.VivoSparqlService;
import org.dataconservancy.dcs.access.client.api.VivoSparqlServiceAsync;
import org.dataconservancy.dcs.access.client.event.ROEditEvent;
import org.dataconservancy.dcs.access.client.model.*;
import org.dataconservancy.dcs.access.client.presenter.EditPresenter;
import org.dataconservancy.dcs.access.shared.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EditPopupPanel extends PopupPanel {

	private Button saveButton;
	private Label userLabel;
	TextBox title;
	
	private Grid loginForm;
	VerticalPanel outerPanel;
	
	String entityName;
	MultiWordSuggestOracle oracle;
	
	public static final RegistryServiceAsync registryService = GWT.create(RegistryService.class);
	 static final VivoSparqlServiceAsync vivo = GWT.create(VivoSparqlService.class);
	  
	
	public EditPopupPanel(final JsEntity entity
			, final DcpTreeModel.JsEntityCell jsEntityCell
			,final JsDcp dcp, final String sipPath, final String type) {
		super(true);
		this.setGlassEnabled(true);
		this.addCloseHandler(new CloseHandler<PopupPanel>() {
			
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
//				
			}
		});
		this.show();
		this.setStyleName("loginPopupContainer");
		//setStyleName(getContainerElement(), "popupContent");
		this.setPopupPosition(Window.WINDOW_WIDTH/3, Window.WINDOW_HEIGHT/4);
		outerPanel = new VerticalPanel();
		//outerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		outerPanel.setSpacing(10);
		outerPanel.setStyleName("loginPopupContainer");
		setWidget(outerPanel);
		outerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

		Label loginLabel = new Label("Edit Entity");
		loginLabel.setStyleName("loginLabelStyle");
		
		
		outerPanel.add(loginLabel);
		
		
		HorizontalPanel innerPanel = new HorizontalPanel();
		innerPanel.setSpacing(10);
		saveButton = new Button("Save changes");
		saveButton.setStyleName("popupLoginButton");
		saveButton.setWidth("150px");
		saveButton.setHeight("40px");
	
		final PopupPanel thisPanel = this;
		saveButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				entityName = title.getText();
				if(type.equalsIgnoreCase("file"))
					((JsFile)entity).setName(entityName);
				else if(type.equalsIgnoreCase("deliverableUnit"))
					((JsDeliverableUnit)entity).setTitle(entityName);
				
				//Also update the DcpTree to contain this updated value
	              Util.add(dcp,
                          entity,
                          type
                  );
	              registryService.updateSip(sipPath, entity.getId(), "title", entityName, new AsyncCallback<Void>() {
					
					@Override
					public void onSuccess(Void result) {
						 EditPresenter.EVENT_BUS.fireEvent(new ROEditEvent(dcp, true, sipPath));
			             thisPanel.hide(); 
					}
					
					@Override
					public void onFailure(Throwable caught) {
						//Window.alert("Failed"+caught.getMessage());
					}
				});
	             
			}
		});
		
		
		
		String titleStr = null;
		String abstractStr = null;
		List<String> authors = new ArrayList<String>(); 
		if(type.equalsIgnoreCase("file"))
			titleStr = ((JsFile)entity).getName();
		else if(type.equalsIgnoreCase("deliverableUnit")){
			titleStr = ((JsDeliverableUnit)entity).getCoreMd().getTitle();
			abstractStr = ((JsDeliverableUnit)entity).getAbstract();
			JsArray<JsCreator> creators = ((JsDeliverableUnit)entity).getCreators();
			for(int i =0; i<creators.length(); i++ )
				authors.add(creators.get(i).getCreatorName());
		}
		
		createLoginForm(titleStr, abstractStr, authors);
		
		innerPanel.add(saveButton);
		outerPanel.add(innerPanel);
	}
	int i ;
	Button add;
	FlowPanel authorsPanel;
	
	private void createLoginForm(String titleStr, String abstractStr, List<String> authors) {
		
		add = new Button("Add");
		add.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(oracle == null){
					vivo.getPeople(new AsyncCallback<Set<Person>>() {
						
						@Override
						public void onSuccess(Set<Person> result) {
							oracle = new MultiWordSuggestOracle();
							for(Person person: result){
								oracle.add(person.getFirstName()+" "+person.getLastName()+";"
										+person.getEmailAddress());
							}
							SuggestBox suggestBox = new SuggestBox(oracle);
							authorsPanel.add(suggestBox);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							new ErrorPopupPanel("Error:"+caught.getMessage());
						}
					});
				}
				else{
					SuggestBox suggestBox = new SuggestBox(oracle);
					authorsPanel.add(suggestBox);
				}
			
			}
			});
		
		
		int size = 2;
		if(abstractStr!=null)
			size++;
		if(authors.size()>0)
			size++;
		
		loginForm = new Grid(size, 3);
		loginForm.setCellPadding(8);
		outerPanel.add(loginForm);
		userLabel = new Label("Name");
		i =0;
	//	passwordLabel = new Label("Size");
		loginForm.setWidget(i, 0, userLabel);
		title = new TextBox();
		title.setText(titleStr);
		loginForm.setWidget(i, 1, title);
		i++;
		if(abstractStr!=null){
			Label abstractLabel = new Label("Abstract");
			loginForm.setWidget(i, 0, abstractLabel);
			TextArea abstractBox = new TextArea();
			abstractBox.setText(abstractStr);
			loginForm.setWidget(i, 1, abstractBox);
			i++;
		}

		if(authors.size()>0){
			authorsPanel = new FlowPanel();
			Label authorsLabel = new Label("Authors");
			loginForm.setWidget(i, 0, authorsLabel);
			loginForm.setWidget(i, 1, authorsPanel);
			for(String author: authors){
				TextBox authorBox = new TextBox();
				authorBox.setText(author);
				authorsPanel.add(authorBox);
				//MultipleTextBox multipleTextBox = new MultipleTextBox();
			//	multipleTextBox.se	
			}
			loginForm.setWidget(i, 2, add);
		}
	}

	public String getEntityName(){
		return this.entityName;
	}

	
}
	


