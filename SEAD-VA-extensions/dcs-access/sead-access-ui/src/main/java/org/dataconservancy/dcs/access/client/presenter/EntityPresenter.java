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
import org.dataconservancy.dcs.access.client.Search;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.Search.UserField;
import org.dataconservancy.dcs.access.client.model.JsCoreMetadata;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsDeliverableUnit;
import org.dataconservancy.dcs.access.client.model.JsMatch;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.client.ui.DcpDisplay;
import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.Event;

public class EntityPresenter implements Presenter {

	final Display display;
	final Panel contentScrollPanel;
	
	public interface Display {
			String getEntityId();
		    Panel getContentPanel();
		    Panel getContentScrollPanel();
		  }

	public EntityPresenter(Display view)
	{
		this.display = view;
		this.contentScrollPanel = this.display.getContentScrollPanel();
	}
	@Override
	public void bind() {
		final String entityurl = this.display.getEntityId();
        final Panel content = this.display.getContentPanel();
        String query = Search.createLiteralQuery("id", entityurl);
        String searchUrl  = searchURL(query, 
	        		0, 
	        		true, 
	        		Constants.MAX_SEARCH_RESULTS);
        JsonpRequestBuilder rb = new JsonpRequestBuilder();
        rb.requestObject(searchUrl, new AsyncCallback<JsSearchResult>() {

            public void onFailure(Throwable caught) {
//                reportInternalError("Viewing entity", caught);
            	Window.alert("Error getting entity: "+caught.getMessage());
            }
            public void onSuccess(JsSearchResult result) {
            	JsMatch m = result.matches().get(0);
        	  if (m.getEntityType().equalsIgnoreCase("file")) {
        		content.add((( org.dataconservancy.dcs.access.client.model.JsFile)m.getEntity()).display());
              }
        	  if (m.getEntityType().equalsIgnoreCase("deliverableunit")) {
          		content.add((( org.dataconservancy.dcs.access.client.model.JsDeliverableUnit)m.getEntity()).display(null));
                }
            	
            }
        });
	}

	@Override
	public void display(final Panel mainContainer, Panel facetContent, Panel headerPanel, Panel logoutPanel, Panel notificationPanel) {
		mainContainer.clear();
		facetContent.clear();
		bind();
    	mainContainer.add(contentScrollPanel);

	}
	
	 public static String searchURL(String query, int offset, boolean context,
	            int max, String... params) {
	      
	        String s = 
	        		SeadApp.accessurl+
	        		SeadApp.queryPath+"?q=" + URL.encodeQueryString(query)
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
}
