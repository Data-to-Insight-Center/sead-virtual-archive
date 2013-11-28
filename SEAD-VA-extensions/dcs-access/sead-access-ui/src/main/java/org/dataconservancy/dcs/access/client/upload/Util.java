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
package org.dataconservancy.dcs.access.client.upload;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class Util {

    public static FlexTable createTable(String... headers) {
        FlexTable table = new FlexTable();

        for (int i = 0; i < headers.length; i++) {
            table.getCellFormatter().addStyleName(i, 0, "TableHeader");
            table.setText(i, 0, headers[i]);
        }

        return table;
    }

    public static void addColumn(FlexTable table, String... values) {
        int col = table.getCellCount(0);

        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                table.setText(i, col, values[i]);
            }
        }
    }

    public static void addColumn(FlexTable table, Widget... widgets) {
        int col = table.getCellCount(0);

        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i] != null) {
                table.setWidget(i, col, widgets[i]);
            }
        }
    }

    public static Label label(String s, String style) {
        Label w = new Label(s);
        w.setStylePrimaryName(style);
        return w;
    }

    public static void addAllFromCSV(List<String> list, String csv) {
        csv = csv.trim();

        if (csv.isEmpty()) {
            return;
        }

        String[] values = csv.split(",");

        for (String s : values) {
            list.add(s.trim());
        }
    }

    public static void popupChooser(final TextBox result,
                                    String description,
                                    final List<String> list,
                                    boolean multiple) {
        final DialogBox db = new DialogBox(false, true);

        Panel panel = new FlowPanel();

        db.setAnimationEnabled(true);
        db.setText(description);
        db.setWidget(panel);
        db.center();

        final HorizontalPanel buttons = new HorizontalPanel();
        buttons.setSpacing(5);

        Button ok = new Button("Ok");
        Button cancel = new Button("Cancel");

        buttons.add(ok);
        buttons.add(cancel);

        final ListBox lb = new ListBox(multiple);
        lb.setVisibleItemCount(5);

        panel.add(lb);
        panel.add(buttons);

        for (String s : list) {
            lb.addItem(s);
        }

        ok.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < list.size(); i++) {
                    if (lb.isItemSelected(i)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }

                        sb.append(list.get(i));
                    }
                }

                result.setText(sb.toString());
                db.hide();
            }
        });

        cancel.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                db.hide();
            }
        });
    }

    public static Widget createListChooser(final TextBox result,
                                           final String description,
                                           final List<String> list,
                                           boolean multiple) {
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(5);
        hp.add(result);

        Button choose = new Button("Choose");

        choose.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                if (list.size() > 0) {
                    Util.popupChooser(result, description, list, true);
                }
            }
        });

        hp.add(choose);

        return hp;
    }
}
