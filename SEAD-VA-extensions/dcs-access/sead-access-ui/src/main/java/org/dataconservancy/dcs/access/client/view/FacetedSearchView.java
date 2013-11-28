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

package org.dataconservancy.dcs.access.client.view;


import org.dataconservancy.dcs.access.client.Search;
import org.dataconservancy.dcs.access.client.model.SearchInput;
import org.dataconservancy.dcs.access.client.ui.SeadAdvancedSearchWidget;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class FacetedSearchView extends Composite implements org.dataconservancy.dcs.access.client.presenter.FacetedSearchPresenter.Display{
	
	ScrollPanel contentScrollPanel;
	Panel facetContent;
	Button search;
	Button add;
	FlexTable table;
	SeadAdvancedSearchWidget advWidget;
	Panel content;
//	SearchInput searchInput;
	//SearchInput input;
	
	
	public FacetedSearchView()
	{
	//	searchInput = input;
		//Widgets for displaying search
		content = new FlowPanel();
        content.setStylePrimaryName("Content");
        contentScrollPanel = new ScrollPanel(content);
             
        //content.add(Util
          //      .label("Search for scientific data from projects from various disciplines of science, archived for long-term preservation." ,
            //            "Explanation"));
        
        //content.add(new AdvancedSearchWidget(null, null));
        
        //Widgets for displaying facet Content 
        
        facetContent = new FlowPanel();
        facetContent.setStylePrimaryName("Facets");
	}
	

	 


	
	@Override
	public Button getSearchButton() {
		return search;
	}

	@Override
	public Button getAddButton() {
		return add;
	}

	@Override
	public FlexTable getButtonsTable() {
		// TODO Auto-generated method stub
		return table;
	}

	@Override
	public Panel getFacetContent() {
		return facetContent;
	}

	@Override
	public Panel getContentScrollPanel() {
		return contentScrollPanel;
	}

	@Override
	public Panel getContent() {
		return content;
	}

/*
	@Override
	public SearchInput getInput() {
		return searchInput;
	}*/

	
}
