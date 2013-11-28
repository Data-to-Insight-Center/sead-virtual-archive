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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class RelationsView extends Composite implements org.dataconservancy.dcs.access.client.presenter.RelationsPresenter.Display{

	//ScrollPanel contentScrollPanel;
	FlowPanel content;
	String entityId;
	
	public RelationsView(final String id){
		 
			content = new FlowPanel();
	        content.setStylePrimaryName("Content");
	       // contentScrollPanel = new ScrollPanel(content);
	       // content.add(new AdvancedSearchWidget(null, null));
	        entityId =id;
	        
	}
	
	
	
		@Override
		public Panel getContent() {
			return content;
		}

		@Override
		public String getEntityId() {
			return entityId;
		}

	
	
}
