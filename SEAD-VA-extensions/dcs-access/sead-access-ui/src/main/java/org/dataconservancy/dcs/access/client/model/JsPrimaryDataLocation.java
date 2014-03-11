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

package org.dataconservancy.dcs.access.client.model;

import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Encapsulates data location metadata fields that can be associated with DCS entities.
 */
public final class JsPrimaryDataLocation
        extends JsModel implements IsSerializable {

    protected JsPrimaryDataLocation() {
    }

    public String getLocation() {
        return getString("location");
    }
    
    public String getName() {
        return getString("name");
    }
    
    public  String getType() {
        return getString("type");
    }
    
 
   
    public Widget display() {
        
    	FlexTable smallTable = Util.createTable();
      	Image image;

    	if(getLocation()!=null){
    		String location = getLocation();
    		if(getType()!=null)
    			if(getType().contains("dspace")&&getName().contains("Ideals"))
    				location = location.replace("xmlui/", "");
    		final String locationLink = location;
    		Label locationLabel;
    		if(!getName().contains("SDA")){
    			locationLabel = Util.label(location,"Hyperlink");
	    		locationLabel.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						Window.open(locationLink, "_blank", "");
					}
				});
    		}
    		else{
    			 locationLabel = new Label();
    			 locationLabel.setText(location);
    		 }

        	
        	smallTable.setWidget(0, 0, locationLabel);
    	}
    	
    	if(getType()!=null){
        	if(getType().contains("dspace")&&getName().contains("local"))
    		{
        		image= new Image("images/local_dspace.jpg");
        		smallTable.setWidget(0, 1,image);
    		}
        	else if(getType().contains("dspace")&&getName().contains("IU"))
    		{
        		image= new Image("images/IU_Scholarworks.jpg");
        		smallTable.setWidget(0, 1,image);
    		}
        	else if(getName().contains("SDA"))
    		{
        		image= new Image("images/hpss.jpg");
        		smallTable.setWidget(0, 1,image);
    		}
        	else if(getType().contains("dspace")&&getName().contains("Ideals"))
    		{
        		image= new Image("images/Ideals.png");
        		smallTable.setWidget(0, 1,image);
    		}
        }

    	  return smallTable;
    }
}
