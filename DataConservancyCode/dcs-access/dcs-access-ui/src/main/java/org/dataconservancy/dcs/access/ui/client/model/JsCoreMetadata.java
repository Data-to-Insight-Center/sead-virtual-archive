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
package org.dataconservancy.dcs.access.ui.client.model;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

import org.dataconservancy.dcs.access.ui.client.Util;

/**
 * Encapsulates core metadata fields that can be associated with DCS entities.
 */
public final class JsCoreMetadata
        extends JsModel {

    protected JsCoreMetadata() {
    }

    public String getTitle() {
        return getString("title");
    }

    public JsArrayString getCreators() {
        return getStrings("creators");
    }

    public JsArrayString getSubjects() {
        return getStrings("subjects");
    }

    public String getType() {
        return getString("type");
    }

    public String getRights() {
        // TODO fix the json builder to be consistent with DCP xml serialization
        // The JSON builder serializes a DcsRights object with a description field
        // while the DCP builder serializes a String representing the rights object.
        // return getString("rights");

        JsModel rights = getObject("rights");
        return rights != null ? rights.getString("description") : "";
    }

    public Widget display() {
        FlexTable table =
                Util.createTable("Title:", "Creators:", "Subjects:", "Type:", "Rights:");

        Util.addColumn(table,
                       getTitle(),
                       toString(getCreators()),
                       toString(getSubjects()),
                       getType(),
                       getRights());

        return table;
    }
}
