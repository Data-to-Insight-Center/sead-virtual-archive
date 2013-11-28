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
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a Data Conservancy Manifestation
 */
public final class JsManifestation extends JsEntity {

	protected JsManifestation() {

	}

	public String getDeliverableUnit() {
		return getString("deliverableUnit");
	}

	@SuppressWarnings("unchecked")
	public JsArray<JsMetadata> getMetadata() {
		return (JsArray<JsMetadata>) getArray("metadata");
	}

	public JsArrayString getMetadataRefs() {
		return getRefs("metadataRef");
	}

	@SuppressWarnings("unchecked")
	public JsArray<JsManifestationFile> getManifestationFiles() {
		return (JsArray<JsManifestationFile>) getArray("manifestationFiles");
	}

	public JsArrayString getTechnicalEnvironment() {
		return getStrings("technicalEnvironment");
	}

	public String getType() {
		return getString("type");
	}

	public Widget display() {
		FlowPanel panel = new FlowPanel();
		panel.setStylePrimaryName("Entity");

		FlexTable table = Util.createTable("Identifier:", "Entity type:",
				"Deliverable Unit", "Technical Env:", "Type:", "Metadata refs:");
		panel.add(table);

		Util.addColumn(table, null, "Manifestation", null,
				toString(getTechnicalEnvironment()), getType());

		table.setWidget(0, 1, Util.entityLink(getId()));
		table.setWidget(2, 1, Util.entityLink(getDeliverableUnit()));

		if (getMetadataRefs() != null) {
			table.setWidget(5, 1, Util.entityLinks(getMetadataRefs()));
		}

		if (getMetadata() != null && getMetadata().length() > 0) {
			panel.add(Util.label("Additional metadata", "SubSectionHeader"));
			JsMetadata.display(panel, getMetadata());
		}

		if (getManifestationFiles() != null
				&& getManifestationFiles().length() > 0) {
			panel.add(Util.label("Files", "SubSectionHeader"));
			JsManifestationFile.display(panel, getManifestationFiles());
		}

		return panel;
	}

	public static void display(Panel panel, JsArray<JsManifestation> array) {
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
			JsManifestation man = array.get(i);

			tabs.add(man.display(), man.summary());
		}

		if (array.length() > 0) {
			tabs.selectTab(0);
		}

		panel.add(top);
	}

	public String summary() {
		String s = getType();

		if (s.isEmpty()) {
			if (getTechnicalEnvironment().length() > 0) {
				s = getTechnicalEnvironment().get(0);
			}
		}

		if (s.isEmpty()) {
			s = "manifestation";
		}

		return s;
	}
}
