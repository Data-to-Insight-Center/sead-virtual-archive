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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.Panel;

/**
 * Models a Data Conservancy Manifestation File
 */
public final class JsManifestationFile extends JsModel {

	/**
	 * Constructs a new DcsManifestationFile with no state.
	 */
	protected JsManifestationFile() {

	}

	/**
	 * The path for the manifestation file
	 * 
	 * @return the path
	 */
	public String getPath() {
		return getString("path");
	}

	public String getRef() {
		return getRef("ref");
	}

	public static void display(Panel panel, JsArray<JsManifestationFile> array) {
		// Doesn't deal well with large array
		// FlexTable table = Util.createTable("Path", "Ref");
		// ScrollPanel top = new ScrollPanel(table);
		// top.setSize("300px", "5em");
		//
		// for (int i = 0; i < array.length(); i++) {
		// JsManifestationFile mf = array.get(i);
		//
		// table.setText(0, i + 1, mf.getPath());
		// table.setWidget(1, i + 1, Util.entityLink(mf.getRef()));
		// }
		//
		// panel.add(top);

		MenuBar top = new MenuBar();
		// TODO work around bug with menubar width
		top.setWidth("15ex");
		top.setAnimationEnabled(true);

		MenuBar refs = new MenuBar(true);

		top.addItem("File refs (" + array.length() + ")", refs);

		for (int i = 0; i < array.length(); i++) {
			final JsManifestationFile mf = array.get(i);
			String label = mf.getPath();

			if (label.isEmpty()) {
				label = mf.getRef();
			}

			refs.addItem(label, new Command() {
				public void execute() {
					History.newItem(State.ENTITY.toToken(mf.getRef()));
				}
			});
		}
		
		panel.add(top);
	}
}
