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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import org.dataconservancy.dcs.access.ui.client.Util;

/**
 * Models a Data Conservancy Event
 */
public final class JsEvent
        extends JsEntity {

    protected JsEvent() {

    }

    public String getEventType() {
        return getString("eventType");

    }

    public String getDate() {
        return getString("date");

    }

    public String getDetail() {
        return getString("detail");

    }

    public String getOutcome() {
        return getString("outcome");
    }

    public JsArrayString getTargets() {
        return getRefs("targets");
    }

    public static void display(Panel panel, JsArray<JsEvent> array) {
        if (array.length() == 1) {
            Widget w = array.get(0).display();
            w.setStylePrimaryName("Entity");
            panel.add(w);
            return;
        }

        TabPanel tabs = new DecoratedTabPanel();
        tabs.setAnimationEnabled(true);

        for (int i = 0; i < array.length(); i++) {
            JsEvent event = array.get(i);

            tabs.add(event.display(), event.getEventType());
        }

        if (array.length() > 0) {
            tabs.selectTab(0);
        }

        panel.add(tabs);
    }

    public Widget display() {

        FlexTable table =
                Util.createTable("Id:",
                                 "Entity type:",
                                 "Detail:",
                                 "Date:",
                                 "Event Type:",
                                 "Outcome:",
                                 "Targets:");

        Util.addColumn(table,
                       null,
                       "Event",
                       getDetail(),
                       getDate(),
                       getEventType(),
                       getOutcome());

        table.setWidget(0, 1, Util.entityLink(getId()));
        table.setWidget(6, 1, Util.entityLinks(getTargets()));

        return table;
    }

    public String summary() {
        return getEventType();
    }
}
