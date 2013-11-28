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

import org.dataconservancy.dcs.access.ui.client.State;
import org.dataconservancy.dcs.access.ui.client.Util;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

// TODO JSON seems to use metadataRefs and metadataRef...

/**
 * Models a Data Conservancy Deliverable Unit
 */
public final class JsDeliverableUnit
        extends JsEntity {

    protected JsDeliverableUnit() {

    }

    public JsArrayString getCollections() {
        return getRefs("collections");
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsMetadata> getMetadata() {
        return (JsArray<JsMetadata>) getArray("metadata");
    }

    public JsArrayString getMetadataRefs() {
        return getRefs("metadataRefs");
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsRelation> getRelations() {
        return (JsArray<JsRelation>) getArray("relations");
    }

    public JsArrayString getFormerExternalRefs() {
        return getStrings("formerExternalRefs");
    }

    public JsArrayString getParents() {
        return getRefs("parents");
    }

    public Boolean isDigitalSurrogate() {
        return getBooleanObject("digitalSurrogate");
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
                                 "Parents:",
                                 "Collections:",
                                 "Former refs",
                                 "Metadata refs:",
                                 "Surrogate");
        panel.add(table);

        HorizontalPanel hp = new HorizontalPanel();

        table.setWidget(0, 1, hp);

        hp.add(Util.entityLink(getId()));
        hp.setSpacing(5);
        hp.add(new Hyperlink("(related)", State.RELATED.toToken(getId())));

        table.setText(1, 1, "Deliverable Unit");

        if (getParents() != null) {
            table.setWidget(2, 1, Util.entityLinks(getParents()));
        }

        if (getCollections() != null) {
            table.setWidget(3, 1, Util.entityLinks(getCollections()));
        }

        table.setText(4, 1, toString(getFormerExternalRefs()));
        table.setWidget(5, 1, Util.entityLinks(getMetadataRefs()));
        table.setText(6, 1, isDigitalSurrogate() == null ? "Unknown" : ""
                + isDigitalSurrogate());

        panel.add(Util.label("Core metadata", "SubSectionHeader"));
        panel.add(getCoreMd().display());

        if (getMetadata() != null && getMetadata().length() > 0) {
            panel.add(Util.label("Additional metadata", "SubSectionHeader"));
            JsMetadata.display(panel, getMetadata());
        }

        if (getRelations() != null && getRelations().length() > 0) {
            panel.add(Util.label("Relations", "SubSectionHeader"));
            JsRelation.display(panel, getRelations());
        }

        return panel;
    }

    public static void display(Panel panel, JsArray<JsDeliverableUnit> array) {
        for (int i = 0; i < array.length(); i++) {
            if (array.get(i) != null) {
                panel.add(array.get(i).display());
            }
        }
    }

    public String summary() {
        return getCoreMd().getTitle();
    }
}
