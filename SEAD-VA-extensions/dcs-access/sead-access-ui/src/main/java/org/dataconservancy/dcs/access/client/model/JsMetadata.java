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

import org.dataconservancy.dcs.access.client.api.TransformerService;
import org.dataconservancy.dcs.access.client.api.TransformerServiceAsync;
import org.dataconservancy.dcs.access.client.model.SchemaType.Name;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a Data Conservancy metadata blob
 */
public final class JsMetadata
        extends JsModel{

	   
	public static final TransformerServiceAsync transformer =
    GWT.create(TransformerService.class);
	
    protected JsMetadata() {

    }

    public String getSchemaUri() {
        return getString("schemaUri");
    }

    public String getMetadata() {
        return getString("metadata");
    }

    public Widget display() {
    	
    	String label =
                getSchemaUri().isEmpty() ? "Schema: Unknown" : "Schema: "
                        + getSchemaUri();
    	final String inputXml = getMetadata().replace("\n", "").replace("\t", "");
    	
        final DisclosurePanel dp = new DisclosurePanel(label);
        dp.setAnimationEnabled(true);
        Label export = new Label();
        export.setText("Convert to ISO");
        export.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				try{
					transformer.xslTransform(Name.FGDC, Name.ISO19115, inputXml, new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							Window.alert("Unable to transform");
							
						}

						@Override
						public void onSuccess(String output) {
							
							HTML html = new HTML(output);
					        dp.setContent(html);
							System.out.println(html);
					        
						}
					});
					}
					catch(Exception e){
						e.printStackTrace();
					}
			}
		});
        
        dp.add(export);
        
        
        
        final AsyncCallback<String> transformCb = new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Unable to transform");
				
			}

			@Override
			public void onSuccess(String fgdcHtml) {
				
				HTML html = new HTML(fgdcHtml);
		        dp.add(html);
		        //dp.setContent(html);
			}
		};
        
        AsyncCallback<SchemaType.Name> validateCb = new AsyncCallback<SchemaType.Name>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Unable to validate");
				
			}

			@Override
			public void onSuccess(Name result) {
				if(result!=null)
				{
					try{
					transformer.xslTransform(result, Name.HTML, inputXml, transformCb);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				else
					Window.alert("Does not match any of the existing schemas");
			}
        	
        };
        
        
		
		
		transformer.validateXML(inputXml, getSchemaUri(), validateCb);
        return dp;
    }

    private Widget formatXML(String xml) {
        PreElement pre = Document.get().createPreElement();
        
		pre.setInnerText(xml.replace(">", ">\n"));
		return HTML.wrap(pre); 
		
    }

    public static void display(Panel panel, JsArray<JsMetadata> array) {
        for (int i = 0; i < array.length(); i++) {
            panel.add(array.get(i).display());
        }
    }
}
