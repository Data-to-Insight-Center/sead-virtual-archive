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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataconservancy.dcs.access.client.SeadApp;

import com.google.gwt.core.client.JsArray;

/**
 * Models Sead Facets for faceted search
 */

public final class JsFacet
        extends org.dataconservancy.dcs.access.ui.client.model.JsModel{

	protected JsFacet() {
    }

	@SuppressWarnings("unchecked")
    public JsArray<JsMatch> matches() {
        return (JsArray<JsMatch>) getArray("matches");
    }
  
      
     public Map<String,List<String>> getFacets() {
    	
        JsMatch match = matches().get(0);
    	HashMap<String,List<String>> facets = new HashMap<String,List<String>>();
        String context = 
        		match.getContext();
        		//"nullFacetCategory[author[Karen Campbell(1)][Paul Morin(1)]]FacetCategory[metadataSchema[fgdc(1)]]FacetCategory[entityType[Event(263)][DeliverableUnit(55)]]";
        //nullFacetCategory[primaryDataLocationName[(4)]]FacetCategory[author]FacetCategory[metadataSchema]FacetCategory[location]FacetCategory[entityType[Event(22)][DeliverableUnit(2)][File(2)][Manifestation(2)]]		
        int openBrackets = 0;
        String facetCategory = null;
        String facetString = context;
        
        facetString = facetString.replace("FacetCategory", "");
        List<String> tempFacets = new ArrayList<String>();
        //Json parsing can be used to get these values
        while(facetString.length()>0)
        {
        	
        	int indexOpen = facetString.indexOf('[');
        	int indexClose = facetString.indexOf(']');
        	int index=0;
        	
        	if(indexOpen!=-1&&indexOpen<indexClose)
        	{
        		index=indexOpen;
        		openBrackets++;
        		if(openBrackets>1 &&facetCategory==null)
        			facetCategory = facetString.substring(0, facetString.indexOf('['));
        	}
        	else
        	{
        		index=indexClose;
        		//index = facetString.indexOf(']');
        		if (openBrackets>1)
            	{
            		//fill List
        			String temp =facetString.substring(0, facetString.indexOf(']'));
        			
        			if(temp.contains("DeliverableUnit"))
        			{
        				tempFacets.add(temp.replace("DeliverableUnit","Collection"));
        			}
        			else if(!(temp.contains("Event")||temp.contains("Manifestation")))
        				tempFacets.add(temp);
            		
            	}
        		else if(openBrackets==1)
        		{
        			if(facetCategory!=null)
        				facets.put(SeadApp.constants.facets.get(facetCategory), tempFacets);
	        	}
        		
        		openBrackets--;
        	}
        	if(openBrackets==0)
        	{
        		tempFacets=new ArrayList<String>();
        		facetCategory=null;
        	}
        		
        	facetString = facetString.substring(index+1);
        	
        	
        }   
        return facets;
    }

 

   
}
