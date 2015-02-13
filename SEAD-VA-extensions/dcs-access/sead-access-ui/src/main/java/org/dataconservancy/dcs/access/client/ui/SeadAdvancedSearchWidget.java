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

package org.dataconservancy.dcs.access.client.ui;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;

import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.Search;

public class SeadAdvancedSearchWidget extends Composite {

	FlowPanel advancedPanel;

	public SeadAdvancedSearchWidget(Search.UserField[] userfields,
			String[] userqueries) {

		advancedPanel = new FlowPanel();
		advancedPanel.setStyleName("advancedSearchPanel");
		initWidget(advancedPanel);
		Button search = new Button("Search");

		final FlexTable table = new FlexTable();

		advancedPanel.add(table);

		// Called to search filled in query

		final ClickHandler searchlistener = new ClickHandler() {

			public void onClick(ClickEvent event) {
				doSearch(table);
			}
		};

		final Button add = createNewAddButton(table);

		if (userfields != null) {
			for (int i = 0; i < userfields.length; i++) {
				if (userfields[i] == null) {
					continue;
				}

				int row = table.getRowCount();
				addRow(table, row);

				ListBox lb = (ListBox) table.getWidget(row, 2);
				TextBox tb = (TextBox) table.getWidget(row, 0);
				tb.setText(userqueries[i]);
			}
		} else {
			addRow(table, 0);
		}

		HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		hp.setSpacing(5);
		// hp.add(add);
		hp.add(search);

		advancedPanel.add(hp);
		hp.setWidth("80%"); // 80% to align hp to the right of AdvancedPanel
		search.addClickHandler(searchlistener);

	}

	ListBox createAdvancedSearchFieldSelector() {
		ListBox lb = new ListBox();
		// lb.setStyleName("SimpleTextBox");

		for (Search.UserField uf : Search.UserField.values()) {
			lb.addItem(uf.display);
		}

		return lb;
	}

	private void doSearch(FlexTable table) {
		// Build up search history token

		String[] data = new String[(table.getRowCount() * 2) + 1 + 1];
		int dataindex = 0;
		boolean emptyquery = true;

		for (int i = 0; i < table.getRowCount(); i++) {
			ListBox lb = (ListBox) table.getWidget(i, 2);
			TextBox tb = (TextBox) table.getWidget(i, 0);

			int sel = lb.getSelectedIndex();

			if (sel != -1) {
				String userquery = tb.getText().trim();
				String userfield = Search.UserField.values()[sel].name();

				if (userquery.isEmpty()) {
					userfield = null;
					userquery = null;
				} else {
					emptyquery = false;
				}

				data[dataindex++] = userfield;
				data[dataindex++] = userquery;
			}
		}

		data[dataindex] = "0";
		data[dataindex + 1] = "0";

		if (!emptyquery) {
			History.newItem(SeadState.SEARCH.toToken(data));
		}
	}

	private void addRow(final FlexTable table, int row) {

		TextBox tb = new TextBox();
		// tb.setStyleName("SimpleTextBox");
		table.setWidget(row, 0, tb);
		table.setWidget(row, 1, new Label("in"));
		table.setWidget(row, 2, createAdvancedSearchFieldSelector());

		tb.addKeyDownHandler(new KeyDownHandler() {

			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					doSearch(table);
				}
			}
		});

		if (row != 0) { // More than one row - can remove a row
			// final Button remove = new Button("Remove");

			table.setWidget(row, 3, createNewRemoveButton(table));

			if (row == 1) {
				// Just added a second row, need to add a remove button
				// on the first row too
				table.setWidget(0, 3, createNewRemoveButton(table));
			}

		}
		table.setWidget(row, 4, createNewAddButton(table));
	}

	private Button createNewAddButton(final FlexTable table) {
		Button newAddButton = new Button("<image src='images/add.ico'>");

		newAddButton.addStyleName("addRemoveButton");

		final ClickHandler addlistener = new ClickHandler() {

			public void onClick(ClickEvent event) {
				int nextRow = table.getRowCount();
				addRow(table, nextRow);
				table.setWidget(nextRow-1, 4, null);
			}
		};
		newAddButton.addClickHandler(addlistener);
		return newAddButton;
	}

	private Button createNewRemoveButton(final FlexTable table) {

		Button remove = new Button("<image src='images/remove.png'>");
		remove.addStyleName("addRemoveButton");

		remove.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				int rowIndex = table.getCellForEvent(event).getRowIndex();
				int count = table.getRowCount();
				table.removeRow(rowIndex);
				if (rowIndex == (count - 1)) {
					// Make sure last row has an add button (if we
					// removed the old last row)

					table.setWidget(rowIndex - 1, 4, createNewAddButton(table));
				}
				if (count == 2) {
					// Don't allow the last row to be removed
					table.setWidget(0, 3, null);
				}
			}
		});
		return remove;
	}

}