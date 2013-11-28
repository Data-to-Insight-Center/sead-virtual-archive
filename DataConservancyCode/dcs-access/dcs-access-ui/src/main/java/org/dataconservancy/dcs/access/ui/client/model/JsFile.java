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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a Data Conservancy File
 */
public final class JsFile
        extends JsEntity {

    protected JsFile() {

    }

    public String getName() {
        return getString("name");
    }

    public boolean isExtant() {
        return getBoolean("extant");
    }

    public long getSizeBytes() {
        return getLong("sizeBytes");
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsFixity> getFixity() {
        return (JsArray<JsFixity>) getArray("fixity");

    }

    @SuppressWarnings("unchecked")
    public JsArray<JsFormat> getFormats() {
        return (JsArray<JsFormat>) getArray("formats");
    }

    public String getSource() {
        return getString("source");
    }

    public Boolean getValid() {
        return getBooleanObject("valid");
    }

    @SuppressWarnings("unchecked")
    public JsArray<JsMetadata> getMetadata() {
        return (JsArray<JsMetadata>) getArray("metadata");
    }

    public JsArrayString getMetadataRefs() {
        return getRefs("metadataRef");
    }

    public Widget display() {
    	FlowPanel panel = new FlowPanel();

        if (!getSource().isEmpty()) {
        	
        	Button b = new Button("Download");
        	panel.add(b);
        	
        	b.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Window.open(getSource(), "_blank", "");
				}
			});
        	
        	//panel.add(new Anchor("Download", false, getSource(), "_blank"));
        }

        FlexTable table =
                Util.createTable("Id",
                                 "Entity type",
                                 "Name",
                                 "Size",
                                 "Valid",
                                 "Extant",
                                 "Metadata refs");

        Util.addColumn(table,
                       null,
                       "File",
                       getName(),
                       "" + getSizeBytes(),
                       getValid() == null ? "Unknown" : "" + getValid(),
                       "" + isExtant());

        table.setWidget(0, 1, Util.entityLink(getId()));

        if (getMetadataRefs() != null) {
            table.setWidget(7, 1, Util.entityLinks(getMetadataRefs()));
        }

        panel.add(table);

        if (getFormats() != null && getFormats().length() > 0) {
            panel.add(Util.label("Formats", "SubSectionHeader"));
            JsFormat.display(panel, getFormats());
        }

        if (getMetadata() != null && getMetadata().length() > 0) {
            panel.add(Util.label("Additional metadata", "SubSectionHeader"));
            JsMetadata.display(panel, getMetadata());
        }

        if (getFixity() != null && getFixity().length() > 0) {
            panel.add(Util.label("Fixity", "SubSectionHeader"));
            JsFixity.display(panel, getFixity());
        }

        return panel;
    }

    public static void display(Panel panel, JsArray<JsFile> array) {
        if (array.length() == 1) {
            Widget w = array.get(0).display();
            w.setStylePrimaryName("Entity");
            panel.add(w);
            return;
        }

        TabPanel tabs = new DecoratedTabPanel();
        ScrollPanel top = new ScrollPanel(tabs);
        top.setStylePrimaryName("Entity");

        tabs.setAnimationEnabled(true);

        for (int i = 0; i < array.length(); i++) {
            JsFile file = array.get(i);
            
            tabs.add(file.display(), file.getName());
        }

        if (array.length() > 0) {
            tabs.selectTab(0);
        }

        panel.add(top);
    }

    public String summary() {
        return getName();
    }
}
