/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.access.client.model;

import java.util.List;
import javax.management.InstanceAlreadyExistsException;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter;
import org.dataconservancy.dcs.access.shared.MediciInstance;
import org.dataconservancy.dcs.access.ui.client.model.JsFixity;
import org.dataconservancy.dcs.access.ui.client.model.JsFormat;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a Data Conservancy File
 */
public final class JsFile
        extends JsEntity implements IsSerializable {

    protected JsFile() {

    }

    public String getName() {
        return getString("name");
    }

    public boolean isExtant() {
        return getBoolean("extant");
    }

    public long getSizeBytes() {
        return getLong("sizeBytes");
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsFixity> getFixity() {
        return (JsArray<JsFixity>) getArray("fixity");

    }

    @SuppressWarnings("unchecked")
    public JsArray<JsFormat> getFormats() {
        return (JsArray<JsFormat>) getArray("formats");
    }

    public String getSource() {
        return getString("source");
    }

    public Boolean getValid() {
        return getBooleanObject("valid");
    }
    
    public String getParent() {
        return getString("parent");
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsMetadata> getMetadata() {
        return (JsArray<JsMetadata>) getArray("metadata");
    }

    public JsArrayString getMetadataRefs() {
        return getRefs("metadataRef");
    }
    
    public JsPrimaryDataLocation getPrimaryDataLocation(){
    	return (JsPrimaryDataLocation) getObject("primaryLoc");
    }
    

    public Widget display() {
        FlowPanel panel = new FlowPanel();

        if (!getSource().isEmpty()) {

            Button b = new Button("Download");
            panel.add(b);

            b.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                	String source = SeadApp.datastreamURLnoEncoding(getId().replace(":", "%3A"));
                	
                	String strWindowFeatures = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
                    Window.open(
                            source
                            , "_blank", strWindowFeatures);
                }
            });

            //panel.add(new Anchor("Download", false, getSource(), "_blank"));
        }

        final FlexTable table =
                Util.createTable(//"Id",
                        "Entity type",
                        "Name",
                        "Appears in Collections",
                        "Size",
                        "Valid",
                        "Extant",
                        "Metadata refs",
                        "Alternate Ids",
                        "Primary Location",
                        "ACR Location");

        Util.addColumn(table,
               // null,
                "File",
                getName(),
                null,
                "" + getSizeBytes()+" bytes",
                getValid() == null ? "Unknown" : "" + getValid(),
                "" + isExtant());

       // table.setWidget(0, 1, Util.entityLink(getId()));

        
    	JsonpRequestBuilder rb = new JsonpRequestBuilder();
    	String parentdu = SeadApp.accessurl+SeadApp.queryPath+"?q="+"id:(\""+getParent()+"\")"+"&offset=0&max=200";
    	rb.requestObject(parentdu, new AsyncCallback<JsSearchResult>() {

            public void onFailure(Throwable caught) {
               Window.alert("Failed");
            }

            public void onSuccess(JsSearchResult dus) {
            	
            	FlowPanel desc = new FlowPanel();
	          
	           
           	 for (int i = 0; i < dus.matches().length(); i++) {
  	            JsMatch m = dus.matches().get(i);

  	            JsDeliverableUnit entity = (JsDeliverableUnit)m.getEntity();
  	       
  	            //System.out.println("Title="+entity.getCoreMd().getTitle());
  	            desc.add(
	            		new Hyperlink(" "+entity.getCoreMd().getTitle(), true, SeadState.ENTITY
	                            .toToken(entity.getId()))
	            		);
         	 }
           	table.setWidget(2, 1, desc);
            }
        });

        if (getMetadataRefs() != null) {
            table.setWidget(6, 1, Util.entityLinks(getMetadataRefs()));
        }
        
        FlowPanel primaryLocPanel = new FlowPanel();

   	 	if(getPrimaryDataLocation()!=null)
   	 		primaryLocPanel.add(getPrimaryDataLocation().display());
   	 	
        if(getAlternateIds()!=null){
        	final JsArray<JsAlternateId> altIds = getAlternateIds();
        	FlowPanel altIdPanel = new FlowPanel();
        	final FlowPanel altLocPanel = new FlowPanel();
        	
        	
	    	 for(int i=0;i<altIds.length();i++){
	    		  			
	    		 final String type = altIds.get(i).getTypeId();
	    		 final String value = altIds.get(i).getIdValue();
	    		 AsyncCallback<List<MediciInstance>> callback = new AsyncCallback<List<MediciInstance>>() {
					
					@Override
					public void onSuccess(List<MediciInstance> result) {
						for(MediciInstance instance:result){
							if(instance.getType().equalsIgnoreCase(type)){
								final String finalLink = instance.getUrl()+"/#dataset?id="+value;
				    			String altIdStr = getName();
				    		
				    			Label altIdLabel = Util.label(altIdStr,"Hyperlink");
				        		altIdLabel.addClickHandler(new ClickHandler() {	
									@Override
									public void onClick(ClickEvent event) {
										Window.open(finalLink, "_blank", "");
										
									}
								});
				        		altLocPanel.add(altIdLabel);
				        		break;
							}
								
						}
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}
				};
	    		 MediciIngestPresenter.mediciService.getAcrInstances(callback);
    			

    			
	    	}
	    	table.setWidget(7, 1, altIdPanel);
	    	table.setWidget(8, 1, primaryLocPanel);
	    	table.setWidget(9, 1, altLocPanel);
        }

        //belongs to dataset
        panel.add(table);

        if (getFormats() != null && getFormats().length() > 0) {
            panel.add(Util.label("Formats", "SubSectionHeader"));
            JsFormat.display(panel, getFormats());
        }

        if (getMetadata() != null && getMetadata().length() > 0) {
            panel.add(Util.label("Additional metadata", "SubSectionHeader"));
            JsMetadata.display(panel, getMetadata());
        }

        if (getFixity() != null && getFixity().length() > 0) {
            panel.add(Util.label("Fixity", "SubSectionHeader"));
            JsFixity.display(panel, getFixity());
        }

        return panel;
    }

    public static void display(Panel panel, JsArray<JsFile> array) {
        if (array.length() == 1) {
            Widget w = array.get(0).display();
            w.setStylePrimaryName("Entity");
            panel.add(w);
            return;
        }

        TabPanel tabs = new DecoratedTabPanel();
        ScrollPanel top = new ScrollPanel(tabs);
        top.setStylePrimaryName("Entity");

        tabs.setAnimationEnabled(true);

        for (int i = 0; i < array.length(); i++) {
            JsFile file = array.get(i);

            tabs.add(file.display(), file.getName());
        }

        if (array.length() > 0) {
            tabs.selectTab(0);
        }

        panel.add(top);
    }

    public String summary() {
        return getName();
    }
}
