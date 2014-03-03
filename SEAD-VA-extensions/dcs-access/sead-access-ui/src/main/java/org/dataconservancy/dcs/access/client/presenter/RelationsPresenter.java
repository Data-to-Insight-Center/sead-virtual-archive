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

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.FileTree;
import org.dataconservancy.dcs.access.client.Search;
import org.dataconservancy.dcs.access.client.SolrField;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.client.view.FacetedSearchView;

import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.TreeViewModel;
import com.google.web.bindery.event.shared.Event;

public class RelationsPresenter implements Presenter {

	public interface Display {
		
	   
		Panel getContent();
		String getEntityId();

	  }
	String id;
	Display display;
	Panel content;
	
	public RelationsPresenter(Display view){
		this.display = view;
	}
	@Override
	public void bind() {
		content = this.display.getContent();
		id=this.display.getEntityId();
		String query = Search.createLiteralQuery("OR",
				SolrField.EntityField.IMM_ANCESTRY.solrName(), id,
//				SolrField.EntityField.ANCESTRY.solrName(), id,
                SolrField.EventField.TARGET.solrName(), id,
                SolrField.EntityField.ID.solrName(), id,
                SolrField.RelationField.TARGET.solrName(), id);

        // content.add(new Label(query));

        collectSearchResults(query, 0, new AsyncCallback<JsDcp>() {

            public void onFailure(Throwable caught) {
            	Util.reportInternalError("Doing related search", caught);
            }

            public void onSuccess(JsDcp result) {
            	displayRelated(result, id);
            }
        });

	}
	
	  static void collectSearchResults(final String query, final int offset,
	            final AsyncCallback<JsDcp> topcb) {
	        collectSearchResults(query, offset, JsDcp.create(), topcb);
	    }

	    static void collectSearchResults(final String query, final int offset,
	            final JsDcp dcp, final AsyncCallback<JsDcp> topcb) {
	        String searchurl = searchURL(query, offset, false, 50);
	        JsonpRequestBuilder rb = new JsonpRequestBuilder();
	        rb.requestObject(searchurl, new AsyncCallback<JsSearchResult>() {

	            public void onFailure(Throwable caught) {
	                topcb.onFailure(caught);
	            }

	            public void onSuccess(JsSearchResult result) {
	                for (int i = 0; i < result.matches().length(); i++) {
	                    Util.add(dcp, result.matches().get(i));
	                }

	                int nextoffset = offset + result.matches().length();

	                if (nextoffset == result.total()) {
	                    topcb.onSuccess(dcp);
	                } else {
	                    collectSearchResults(query, nextoffset, dcp, topcb);
	                }
	            }
	        });
	    }
	    
	    static String searchURL(String query, int offset, boolean context,
	            int max, String... params) {
	        String accessurl = 
	    			SeadApp.accessurl;
	        String s = accessurl + SeadApp.queryPath+"?q=" + URL.encodeQueryString(query)
	                + "&offset=" + offset + "&max=" + max;

	        if (context) {
	            s= s + "&_hl=true&_hl.requireFieldMatch=true&_hl.fl="
	                    + URL.encodeQueryString("*");
	        }
	        for (int i = 0; i < params.length;i+=2) {
	            s=s+"&"+params[i]+"="+params[i+1];
	        }
	        return s;
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
	            	content.add(dcp.getDeliverableUnits().get(i).display(tree));
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
