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

import org.dataconservancy.dcs.access.ui.client.Util;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a Data Conservancy collection
 */
public final class JsCollection
        extends JsEntity {

    protected JsCollection() {

    }

    /**
     * The parent collection.
     * 
     * @return the parent collection, or <code>null</code> if no parent exists
     */
    public String getParent() {
        return getRef("parent");
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsMetadata> getMetadata() {
        return (JsArray<JsMetadata>) getArray("metadata");
    }

    public JsArrayString getMetadataRefs() {
        return getRefs("metadataRef");
    }

    JsCoreMetadata getCoreMd() {
        return (JsCoreMetadata) getObject("coreMd");
    }

    public Widget display() {
    	FlowPanel panel = new FlowPanel();
        panel.setStylePrimaryName("Entity");

        FlexTable table =
                Util.createTable("Identifier:",
                                 "Entity type:",
                                 "Parent:",
                                 "Metadata refs:");
        panel.add(table);

        table.setWidget(0, 1, Util.entityLink(getId()));
        table.setText(1, 1, "Collection");

        if (getParent() != null) {
            table.setWidget(2, 1, Util.entityLink(getParent()));
        }

        if (getMetadataRefs() != null) {
            table.setWidget(3, 1, Util.entityLinks(getMetadataRefs()));
        }

        panel.add(Util.label("Core metadata", "SubSectionHeader"));
        panel.add(getCoreMd().display());

        if (getMetadata() != null && getMetadata().length() > 0) {
            panel.add(Util.label("Additional metadata", "SubSectionHeader"));
            JsMetadata.display(panel, getMetadata());
        }

        return panel;
    }

    public static void display(Panel panel, JsArray<JsCollection> array) {
        for (int i = 0; i < array.length(); i++) {
            panel.add(array.get(i).display());
        }
    }

    public String summary() {
        return getCoreMd().getTitle();
    }
}
