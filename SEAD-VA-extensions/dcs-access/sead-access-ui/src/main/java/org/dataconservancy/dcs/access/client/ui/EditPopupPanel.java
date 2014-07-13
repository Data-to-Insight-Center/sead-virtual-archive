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
import org.dataconservancy.dcs.access.client.api.*;
import org.dataconservancy.dcs.access.client.event.ROEditEvent;
import org.dataconservancy.dcs.access.client.model.*;
import org.dataconservancy.dcs.access.client.presenter.EditPresenter;
import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.shared.Person;

import java.util.*;
import java.util.Map.Entry;

public class EditPopupPanel extends PopupPanel {

    private Button saveButton;
    private Label userLabel;
    TextBox title;
    TextArea abstractBox;

    private Grid coreMd;
    VerticalPanel outerPanel;
  
    String entityName;
    String extension = null;
    MultiWordSuggestOracle oracle;

    public static final RegistryServiceAsync registryService = GWT.create(RegistryService.class);
    static final VivoSparqlServiceAsync vivo = GWT.create(VivoSparqlService.class);
    static final TransformerServiceAsync transformerServiceAsync = GWT.create(TransformerService.class);


    public EditPopupPanel(final JsEntity entity
            , final EditDcpTreeModel.JsEntityCell jsEntityCell
            , final JsDcp dcp, final String sipPath, final String type) {
        super(true);
        this.setGlassEnabled(true);
        this.addCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> event) {			
            }
        });
        this.show();
        this.setStyleName("loginPopupContainer");
        this.setPopupPosition(Window.WINDOW_WIDTH/3, Window.WINDOW_HEIGHT/4);
        
        outerPanel = new VerticalPanel();
        outerPanel.setSpacing(10);
        outerPanel.setWidth("95%");
        outerPanel.setStyleName("loginPopupContainer");
        setWidget(outerPanel);
        
       
        outerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);

        Label loginLabel = new Label("Edit Entity");
        loginLabel.setStyleName("loginLabelStyle");


        outerPanel.add(loginLabel);


        final HorizontalPanel innerPanel = new HorizontalPanel();
        innerPanel.setSpacing(10);
        saveButton = new Button("Save changes");
        saveButton.setStyleName("popupLoginButton");
        saveButton.setWidth("150px");
        saveButton.setHeight("40px");

        final PopupPanel thisPanel = this;


        String titleStr = null;
        String abstractStr = null;
        
       
        List<String> authors = new ArrayList<String>();
        final FlexTable propertiesGrid = new FlexTable();
    	propertiesGrid.setWidth("85%");
    	propertiesGrid.setCellPadding(8);
        if(type.equalsIgnoreCase("file")){
        	
        
            String fileName = ((JsFile)entity).getName();
            int dotIndex = fileName.lastIndexOf(".");
            if(dotIndex!=-1){
                titleStr = fileName.substring(0, dotIndex);
                extension  = fileName.substring(dotIndex);
            }
            else
                titleStr = fileName;
            final JsArray<JsMetadata> metadataList = ((JsFile)entity).getMetadata();
            int j = 0;
            for(int i = 0; i< metadataList.length();i++){
            	final int index = i;
            	final JsMetadata metadata = metadataList.get(i);
            	transformerServiceAsync.parseEntityMetadata(metadata.getMetadata(), new AsyncCallback<Map<String,String>>() {
					@Override
					public void onSuccess(Map<String, String> map) {
						Iterator iterator = map.entrySet().iterator();
						Entry<String, String> result = null;
				         while(iterator.hasNext()){
				             result = (Entry<String, String>) iterator.next();
				             break;
				         }

				         if(Constants.predicateViewMap.containsKey(result.getKey().trim()))
				         {
					         int k = propertiesGrid.getRowCount();
				        	 propertiesGrid.setWidget(k, 0, new Label(Constants.predicateViewMap.get(result.getKey().trim())));
			        		 TextBox metadataValue = new TextBox();
			        		 metadataValue.setText(result.getValue());
			        		 propertiesGrid.setWidget(k, 1, metadataValue);
				         }
		        		 if(index==metadataList.length()-1){

				        	 outerPanel.add(propertiesGrid);
				             innerPanel.add(saveButton);
				             outerPanel.add(innerPanel);
				         }
					}
					@Override
					public void onFailure(Throwable caught) {
						;
					}
				});
            }
        }
        else if(type.equalsIgnoreCase("deliverableUnit")){

        	titleStr = ((JsDeliverableUnit)entity).getCoreMd().getTitle();
            abstractStr = ((JsDeliverableUnit)entity).getAbstract();
            JsArray<JsCreator> creators = ((JsDeliverableUnit)entity).getCreators();
            for(int i =0; i<creators.length(); i++ )
                authors.add(creators.get(i).getCreatorName());

            final JsArray<JsMetadata> metadataList = ((JsFile)entity).getMetadata();

            for(int i = 0; i< metadataList.length();i++){
            	final int index = i;
            	final JsMetadata metadata = metadataList.get(i);
            	transformerServiceAsync.parseEntityMetadata(metadata.getMetadata(), new AsyncCallback<Map<String,String>>() {
					@Override
					public void onSuccess(Map<String, String> map) {
						Iterator iterator = map.entrySet().iterator();
						Entry<String, String> result = null;
				         while(iterator.hasNext()){
				             result = (Entry<String, String>) iterator.next();
				             break;
				         }
				         
				         if(Constants.predicateViewMap.containsKey(result.getKey().trim()))
				         {
					         int k = propertiesGrid.getRowCount();
					         propertiesGrid.setWidget(k, 0, new Label(Constants.predicateViewMap.get(result.getKey().trim())));
			        		 TextBox metadataValue = new TextBox();
			        		 metadataValue.setText(result.getValue());
			        		 propertiesGrid.setWidget(k, 1, metadataValue);
				         }
				         
		        		 if(index==metadataList.length()-1){
				        	
				        	 outerPanel.add(propertiesGrid);
				             innerPanel.add(saveButton);
				             outerPanel.add(innerPanel);
				         }
					}					
					@Override
					public void onFailure(Throwable caught) {
						;
					}
				});
            }
        }

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	
                entityName = title.getText();
                
                if(type.equalsIgnoreCase("file")){
                    if(extension!=null)
                        entityName+=extension;
                    ((JsFile)entity).setName(entityName);
                }
                else if(type.equalsIgnoreCase("deliverableUnit")){
                    ((JsDeliverableUnit)entity).setTitle(entityName);
                    ((JsDeliverableUnit)entity).setAbstract(abstractBox.getText());
                }

                Map<String, List<String>> changes = new HashMap<String, List<String>>();
                List<String> nameList = new ArrayList<String>();
                nameList.add(entityName);
                changes.put("title", nameList);
                if(abstractBox!=null){
                	List<String> abstractList = new ArrayList<String>();
                	abstractList.add(abstractBox.getText());
                    changes.put("abstract", abstractList);
                }
                
                if(propertiesGrid!=null){
                	int count = propertiesGrid.getRowCount();
                	for(int i =0; i<count; i++){
                		if(propertiesGrid.getWidget(i, 0)!=null && propertiesGrid.getWidget(i, 1)!=null)
                		{
                			String newValue = ((TextBox)propertiesGrid.getWidget(i, 1)).getText().trim();
                    		String predicate = Constants.viewPredicateMap.get(((Label)propertiesGrid.getWidget(i, 0)).getText().trim());
                    		List<String> customMd = new ArrayList<String>();
        					if(changes.containsKey(predicate))
        						customMd = changes.get(predicate);
        					customMd.add(newValue);
                			changes.put(predicate, customMd);
                		}
                	}
                }
         
                //Update the SIP that gets sent to the registry/ro system
                registryService.updateSip(sipPath, entity.getId(),
                        changes,
                        Constants.predicateViewMap,
                        new AsyncCallback<Void>() {

                            @Override
                            public void onSuccess(Void result) {
                            	EditPresenter.EVENT_BUS.fireEvent(new ROEditEvent(true, sipPath));
                                thisPanel.hide();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                new ErrorPopupPanel("Error:"+ caught.getMessage()).show();
                            }
                        });
            }
        });

        createCoreMd(titleStr, extension, abstractStr, authors);
        
       
    }
    
    int i ;
    Button add;
    FlowPanel authorsPanel;

    private void createCoreMd(String titleStr, String extension, String abstractStr, List<String> authors) {

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

        coreMd = new Grid(size, 3);
        coreMd.setWidth("85%");
        coreMd.setCellPadding(8);
        outerPanel.add(coreMd);
        userLabel = new Label("Name");
        i =0;
        //	passwordLabel = new Label("Size");
        coreMd.setWidget(i, 0, userLabel);
        title = new TextBox();
        title.setText(titleStr);
        coreMd.setWidget(i, 1, title);
        if(extension!=null)
        	coreMd.setWidget(i, 2, new Label(extension));

        i++;
        if(abstractStr!=null){
            Label abstractLabel = new Label("Abstract");
            coreMd.setWidget(i, 0, abstractLabel);
            abstractBox = new TextArea();
            abstractBox.setText(abstractStr);
            coreMd.setWidget(i, 1, abstractBox);
            i++;
        }

        if(authors.size()>0){
            authorsPanel = new FlowPanel();
            Label authorsLabel = new Label("Authors");
            coreMd.setWidget(i, 0, authorsLabel);
            coreMd.setWidget(i, 1, authorsPanel);
            for(String author: authors){
                TextBox authorBox = new TextBox();
                authorBox.setText(author);
                authorsPanel.add(authorBox);
                //MultipleTextBox multipleTextBox = new MultipleTextBox();
                //	multipleTextBox.se
            }
            coreMd.setWidget(i, 2, add);
        }
    }

    public String getEntityName(){
        return this.entityName;
    }

}
	


