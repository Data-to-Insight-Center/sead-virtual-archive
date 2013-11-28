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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Panel;

import org.dataconservancy.dcs.access.ui.client.Util;

/**
 * Models Data Conservancy fixity
 */

public final class JsFixity
        extends JsModel {

    protected JsFixity() {

    }

    public String getAlgorithm() {
        return getString("algorithm");
    }

    public String getValue() {
        return getString("value");
    }

    public static void display(Panel panel, JsArray<JsFixity> array) {
        FlexTable table = Util.createTable("Algorithm", "Value");

        for (int i = 0; i < array.length(); i++) {
            JsFixity fix = array.get(i);

            Util.addColumn(table, fix.getAlgorithm(), fix.getValue());
        }
        
        panel.add(table);
    }
}
