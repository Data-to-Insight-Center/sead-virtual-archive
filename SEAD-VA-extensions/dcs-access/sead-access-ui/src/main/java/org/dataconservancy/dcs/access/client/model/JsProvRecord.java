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
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import org.dataconservancy.dcs.access.client.Util;
import org.dataconservancy.dcs.access.ui.client.model.JsModel;
/**
 * Models a relationship between DCS entities.
 */
public final class JsProvRecord
        extends JsModel {

    protected JsProvRecord() {
    }

    public String getId() {
        return getString("id");
    }
    
    public String getParentId() {
        return getString("parentId");
    }

    public String getName() {
        return getString("name");
    }
    
    public String getDate() {
        return getString("date");
    }
    
    public String getStatus() {
        return getString("status");
    }
    
    public String getType() {
        return getString("type");
    }

    
}
