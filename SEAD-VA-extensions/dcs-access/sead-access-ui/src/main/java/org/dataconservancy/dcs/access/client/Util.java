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
package org.dataconservancy.dcs.access.client;

import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.ui.client.model.JsCollection;
import org.dataconservancy.dcs.access.client.model.JsDcp;
import org.dataconservancy.dcs.access.client.model.JsDeliverableUnit;
import org.dataconservancy.dcs.access.client.model.JsEntity;
import org.dataconservancy.dcs.access.client.model.JsFile;
import org.dataconservancy.dcs.access.client.model.JsSearchResult;
import org.dataconservancy.dcs.access.ui.client.model.JsFormat;
import org.dataconservancy.dcs.access.client.model.JsMatch;
import org.dataconservancy.dcs.access.client.ui.MetadataPopupPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Widget;

public class Util {

    public static Label label(String s, String style) {
        Label w = new Label(s);
        w.setStylePrimaryName(style);
        return w;
    }

    public static Widget entityLink(String id) {
        return entityLink(id, id);
    }

    public static Widget entityLink(String name, String id) {
        if (id == null || id.isEmpty()) {
            return new Label();
        }

        return new Hyperlink(name, SeadState.ENTITY.toToken(id));
    }

    public static FlexTable createTable(String... headers) {
        FlexTable table = new FlexTable();

        for (int i = 0; i < headers.length; i++) {
            table.getCellFormatter().addStyleName(i, 0, "TableHeader");
            table.setText(i, 0, headers[i]);
        }

        return table;
    }
    
    public static Grid createGrid(int rows, int cols, String... headers) {
        Grid table = new Grid(rows, cols);

        for (int i = 0; i < headers.length; i++) {
            table.getCellFormatter().addStyleName(i, 0, "TableHeader");
            table.setText(i, 0, headers[i]);
        }

        return table;
    }

    public static void addColumn(FlexTable table, String... values) {
        int col = table.getCellCount(0);

        for (int i = 0; i < values.length; i++) {
            table.setText(i, col, values[i]);
        }
    }

    public static void addColumn(FlexTable table, Widget... values) {
        int col = table.getCellCount(0);

        for (int i = 0; i < values.length; i++) {
            table.setWidget(i, col, values[i]);
        }
    }

    public static Widget entityLinks(JsArrayString ids) {
        if (ids.length() == 0) {
            return new Label();
        }

        MenuBar top = new MenuBar();
        // TODO work around bug with menubar width
        top.setWidth("15ex");
        top.setAnimationEnabled(true);
        
        MenuBar refs = new MenuBar(true);
        
        top.addItem("Entities (" + ids.length() + ")", refs);

        for (int i = 0; i < ids.length(); i++) {
            final String id = ids.get(i);
            
            refs.addItem(id, new Command() {
                public void execute() {
                    History.newItem(SeadState.ENTITY.toToken(id));
                }
            });
        }

        return top;
    }

    
    public static Widget metadataLinks(JsArrayString ids) {
        if (ids.length() == 0) {
            return new Label();
        }

        MenuBar top = new MenuBar();
        top.setWidth("15ex");
        top.setAnimationEnabled(true);
        
        final MenuBar refs = new MenuBar(true);
        
        top.addItem("View Metadata (" + ids.length() + ")", refs);

        for (int i = 0; i < ids.length(); i++) {
            final String id = ids.get(i);
            JsonpRequestBuilder rb = new JsonpRequestBuilder();

            String query = Search.createLiteralQuery("id", id);
            String searchUrl  = Search.searchURL(query, 
    	        		0, 
    	        		true, 
    	        		Constants.MAX_SEARCH_RESULTS);
            rb.requestObject(searchUrl, new AsyncCallback<JsSearchResult>() {

                public void onFailure(Throwable caught) {
                    reportInternalError("Viewing entity", caught);
                }

                public void onSuccess(final JsSearchResult result) {
                	final JsMatch m = result.matches().get(0);
              	  	if (m.getEntityType().equalsIgnoreCase("file")) {
                	
                		final JsArray<JsFormat> formats = ((JsFile)m.getEntity()).getFormats();
	            		if(formats.length()>0){
	                	
	                	refs.addItem(formats.get(0).getFormat(), new Command() {
	                        public void execute() {
	                        	String fileSource = ((JsFile)m.getEntity()).getPrimaryDataLocation().getLocation();
	                        	if(!(fileSource.startsWith("http://")||fileSource.startsWith("https://")))
	                        		fileSource = ((JsFile)m.getEntity()).getSource();
	                        	MetadataPopupPanel statusPopupPanel = new MetadataPopupPanel(fileSource,formats.get(0).getFormat()); 
	        					statusPopupPanel.show();
	                        }
	                    });
                	}
              	  }
                }
            });
            
            
        }

        return top;
    }
    public static void add(JsDcp dcp, JsMatch match) {
        String type = match.getEntityType();
        JsEntity entity = match.getEntity();

        if (type.equals("deliverableUnit")) {
            dcp.getDeliverableUnits().push((JsDeliverableUnit) entity);
        } else if (type.equals("collection")) {
            //dcp.getCollections().push((JsCollection) entity);
        } else if (type.equals("file")) {
            dcp.getFiles().push((JsFile) entity);
        } /*else if (type.equals("manifestation")) {
            dcp.getManifestations().push((JsManifestation) entity);
        }*/ else if (type.equals("event")) {
           // dcp.getEvents().push((JsEvent) entity);
        } else {
            throw new RuntimeException("Unknown type: " + type);
        }
    }

    /**
     * Remove element at index from array preserving order.
     * 
     * @param array
     * @param index
     */
    // TODO Duplicate code because of generics issues...

    //    public static void remove(JsArray<JavaScriptObject> array, int index) {
    //        JavaScriptObject[] jarray = new JavaScriptObject[array.length()];
    //
    //        for (int i = 0; i < jarray.length; i++) {
    //            jarray[i++] = array.shift();
    //        }
    //        
    //        for (int i = 0; i < jarray.length; i++) {
    //            if (i != index) {
    //                array.push(jarray[i]);
    //            }
    //        }
    //    }

    public static void removeCollection(JsArray<JsCollection> array, int index) {
        JsCollection[] jarray = new JsCollection[array.length()];

        for (int i = 0; i < jarray.length; i++) {
            jarray[i++] = array.shift();
        }

        for (int i = 0; i < jarray.length; i++) {
            if (i != index) {
                array.push(jarray[i]);
            }
        }
    }

    public static void removeDeliverableUnit(JsArray<JsDeliverableUnit> array,
                                             int index) {
        JsDeliverableUnit[] jarray = new JsDeliverableUnit[array.length()];

        for (int i = 0; i < jarray.length; i++) {
            jarray[i++] = array.shift();
        }

        for (int i = 0; i < jarray.length; i++) {
            if (i != index) {
                array.push(jarray[i]);
            }
        }
    }

    /**
     * Return index of entity.
     * 
     * @param array
     * @param id
     * @return
     */
    public static int find(JsArray<? extends JsEntity> array, String id) {

        for (int i = 0; i < array.length(); i++) {
            JsEntity entity = array.get(i);

            if (entity.getId().equals(id)) {
                return i;
            }
        }

        return -1;
    }
    
    static final String BUG_SUBMIT_EMAIL = "http://dataconservancy.org";

    public static void reportInternalError(String message, Throwable e) {
        Window.alert("This is likely a bug. Please report to "
                + BUG_SUBMIT_EMAIL
                + ". Include your operating system and version, browser and version, url you were visiting, and what you did to trigger the problem. \nInternal error: "
                + message + "\n" + e.getMessage()
                + (e.getCause() == null ? "" : "\nCaused by: " + e.getCause()));
    }
}
