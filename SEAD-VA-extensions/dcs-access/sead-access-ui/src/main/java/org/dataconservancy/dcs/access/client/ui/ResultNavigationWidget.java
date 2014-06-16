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

package org.dataconservancy.dcs.access.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.client.event.SearchEvent;
import org.dataconservancy.dcs.access.client.model.SearchInput;
import org.dataconservancy.dcs.access.client.presenter.FacetedSearchPresenter;
import org.dataconservancy.dcs.access.shared.Constants;

public class ResultNavigationWidget extends Composite {

	HorizontalPanel navigationPanel;
	public ResultNavigationWidget(final int page, final int numpages, final SearchInput searchInput, long totalResults, final boolean isAdvanced) {
		navigationPanel = new HorizontalPanel();
		initWidget(navigationPanel);
		navigationPanel.setStylePrimaryName("ResultsNav");
//		navigationPanel.setSpacing(2);
		
		int currentRangeMin = (page)* Constants.MAX_SEARCH_RESULTS+1;
        int currentRangeMax = (page+1) * Constants.MAX_SEARCH_RESULTS;
        if(currentRangeMax>totalResults){
        	currentRangeMax = (int)totalResults;
        }
        Label resultNavigator = Util.label("  ", "ResultNavigator");
        if (SeadState.fromToken(History.getToken()) == SeadState.SEARCH){
        	String queries = "";
        	if(searchInput.getUserqueries().length>0){
        		for(int i =0;i<searchInput.getUserqueries().length;i++){
        			queries+=searchInput.getUserqueries()[i];
        			if(i<searchInput.getUserqueries().length-1)
        				queries+=",";
        		}
        	}
        	
        	if(searchInput.getFacetValue().length>0){
        		if(searchInput.getUserqueries().length>0)
        			queries+=",";
        		for(int i =0;i<searchInput.getFacetField().length;i++){
        			queries+=searchInput.getFacetValue()[i];
        			if(i<searchInput.getFacetValue().length-1)
        				queries+=",";
        		}
        	}
        	String showingString = "\""+queries+ "\"\t Showing "+ currentRangeMin +" - "+currentRangeMax + " out of "+totalResults;
        	resultNavigator = Util.label(showingString, "ResultNavigator");
        }
        navigationPanel.add(resultNavigator);

        if (numpages > 1) {
            if (page > 0) {
            	
            	Label first = Util.label("<<", "Hyperlink");
            	first.addClickHandler(new ClickHandler() {
    				
    				@Override
    				public void onClick(ClickEvent event) {
    		        	FacetedSearchPresenter.EVENT_BUS.fireEvent(
    		        			new SearchEvent(
    		        			new SearchInput(
    							searchInput.getUserfields(),
    							searchInput.getUserqueries(),
    							0,
    							searchInput.getFacetField(),
    							searchInput.getFacetValue()
    							),
    							isAdvanced)); 
    					
    				}
    			});
            	navigationPanel.add(first);
            		
                final int offset = (page - 1) * Constants.MAX_SEARCH_RESULTS;
                
                Label previous = Util.label("Previous", "Hyperlink");
                previous.addClickHandler(new ClickHandler() {
    				
    				@Override
    				public void onClick(ClickEvent event) {
    		        	FacetedSearchPresenter.EVENT_BUS.fireEvent(
    		        			new SearchEvent(
    		        			new SearchInput(
    							searchInput.getUserfields(),
    							searchInput.getUserqueries(),
    							offset,
    							searchInput.getFacetField(),
    							searchInput.getFacetValue()
    							),
    							isAdvanced)); 
    					
    				}
    			});
                navigationPanel.add(previous);
            }

            int max_pages_shown = 10;

            int startpage = page - (max_pages_shown / 2);
            if (startpage < 0) {
                startpage = 0;
            }

            int endpage = startpage + max_pages_shown;

            if (endpage > numpages) {
                endpage = numpages;
            }

            for (int i = startpage; i < endpage; i++) {
                if (i == page) {
                	navigationPanel.add(Util.label("" + (i + 1), "CurrentNavigationPage"));
                } else {
                    final int offset = i * Constants.MAX_SEARCH_RESULTS;
                  
                    Label pageIndex = Util.label(String.valueOf(i + 1), "Hyperlink");
                    pageIndex.addClickHandler(new ClickHandler() {
        				
        				@Override
        				public void onClick(ClickEvent event) {
        		        	FacetedSearchPresenter.EVENT_BUS.fireEvent(
        		        			new SearchEvent(
        		        			new SearchInput(
        							searchInput.getUserfields(),
        							searchInput.getUserqueries(),
        							offset,
        							searchInput.getFacetField(),
        							searchInput.getFacetValue()
        							),
        							isAdvanced)); 
        					
        				}
        			});

                    navigationPanel.add(pageIndex);
                }
            }

            if (page < numpages - 1) {
                final int offset = (page + 1) * Constants.MAX_SEARCH_RESULTS;
                
                Label next = Util.label("Next", "Hyperlink");
                next.addClickHandler(new ClickHandler() {
    				
    				@Override
    				public void onClick(ClickEvent event) {
    		        	FacetedSearchPresenter.EVENT_BUS.fireEvent(
    		        			new SearchEvent(
    		        			new SearchInput(
    							searchInput.getUserfields(),
    							searchInput.getUserqueries(),
    							offset,
    							searchInput.getFacetField(),
    							searchInput.getFacetValue()
    							),
    							isAdvanced)); 
    					
    				}
    			});

                navigationPanel.add(next);
                
                
                Label last = Util.label(">>", "Hyperlink");
                last.addClickHandler(new ClickHandler() {
    				
    				@Override
    				public void onClick(ClickEvent event) {
    		        	FacetedSearchPresenter.EVENT_BUS.fireEvent(
    		        			new SearchEvent(
    		        			new SearchInput(
    							searchInput.getUserfields(),
    							searchInput.getUserqueries(),
    							(numpages - 1) * Constants.MAX_SEARCH_RESULTS,
    							searchInput.getFacetField(),
    							searchInput.getFacetValue()
    							),
    							isAdvanced)); 
    					
    				}
    			});

                navigationPanel.add(last);
               }
            }
        }

}

