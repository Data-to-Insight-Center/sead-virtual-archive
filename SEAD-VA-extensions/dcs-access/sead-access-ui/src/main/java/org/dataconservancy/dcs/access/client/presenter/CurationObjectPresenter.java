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

package org.dataconservancy.dcs.access.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.view.client.TreeViewModel;
import org.dataconservancy.dcs.access.client.FileTree;
import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.api.RegistryService;
import org.dataconservancy.dcs.access.client.api.RegistryServiceAsync;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.client.ui.ErrorPopupPanel;

public class CurationObjectPresenter implements Presenter {

	public interface Display {
		
	   
		Panel getContent();
		String getEntityId();

	  }
	String id;
	Display display;
	Panel content;
	public static final RegistryServiceAsync registryService = GWT.create(RegistryService.class);
	
	public CurationObjectPresenter(Display view){
		this.display = view;
	}
	@Override
	public void bind() {
		content = this.display.getContent();
		id=this.display.getEntityId();
		registryService.getSip(id, SeadApp.roUrl, new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String resultStr) {
				 String[] tempString = resultStr.split(";");
                final String sipPath = tempString[tempString.length-1].split("<")[0];
                resultStr = resultStr.substring(resultStr.indexOf('{'), resultStr.lastIndexOf('}')+1);
	              
                JsDcp dcp = JsDcp.create();
                JsSearchResult result = JsSearchResult.create(resultStr);
                int count = result.matches().length();
	            for (int i = 0; i < count; i++) {
//	            	if(result.matches().get(i).getEntity() instanceof JsDeliverableUnit) 
//						continue;
                    Util.add(dcp, result.matches().get(i));
                }
				displayRelated(dcp, id);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				new ErrorPopupPanel("Error:"+caught.getMessage()).show();
			}
		});
	}
	
	    public void displayRelated(JsDcp dcp, String id){
			content.add(Util.label("Entity", "SectionHeader"));
			

	        TreeViewModel treemodel = new FileTree(dcp);
	        

	        CellTree tree = new CellTree(treemodel, null);

	        tree.setStylePrimaryName("RelatedView");
	        tree.setAnimationEnabled(true);
	        tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
	        tree.setDefaultNodeSize(50);
	       
	        if (tree.getRootTreeNode().getChildCount() > 0) {
	            tree.getRootTreeNode().setChildOpen(0, false);
	        }

	        int i = -1;//Util.find(dcp.getCollections(), id);

	        if (i == -1) {
	            i = Util.find(dcp.getDeliverableUnits(), id);

	            if (i == -1) {
	                // TODO other types...
	            } else {
	            	content.add(dcp.getDeliverableUnits().get(i).display(tree, false));
                }
	        } else {
	        	content.add(dcp.getCollections().get(i).display());
	        }
		}
		
	@Override
	public void display(Panel mainContainer, Panel facetContent, Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		mainContainer.clear();
		facetContent.clear();
		bind();
		mainContainer.add(content);

	}

}
