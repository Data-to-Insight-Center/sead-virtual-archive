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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;

/**
 * Models Data Contributor/Creator.
 */
public final class JsCreator
        extends JsModel implements IsSerializable {

    protected JsCreator() {
    }

    public String getCreatorName() {
        return getString("name");
    }

    public String getCreatorId() {
        return getString("id");
    }
    
    public String getCreatorIdType() {
        return getString("idType");
    }

    public static Widget display(JsArray<JsCreator> array) {
        FlexTable table = Util.createTable();

        for (int i = 0; i < array.length(); i++) {
            final JsCreator creator = array.get(i);

            Label creatorLbl = Util.label(creator.getCreatorName(), "Hyperlink");
            table.setWidget(0, i + 1, creatorLbl);
            creatorLbl.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					Window.open(creator.getCreatorId(), "_blank", "");
				}
			});
        }

        return table;
    }
}
