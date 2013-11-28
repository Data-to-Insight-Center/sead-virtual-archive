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
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a Data Conservancy metadata blob
 */
public final class JsMetadata
        extends JsModel {

    protected JsMetadata() {

    }

    public String getSchemaUri() {
        return getString("schemaUri");
    }

    public String getMetadata() {
        return getString("metadata");
    }

    public Widget display() {
        String label =
                getSchemaUri().isEmpty() ? "Schema: Unknown" : "Schema: "
                        + getSchemaUri();

        DisclosurePanel dp = new DisclosurePanel(label);
        dp.setAnimationEnabled(true);
        dp.setContent(formatXML(getMetadata()));

        return dp;
    }

    // TODO Do a real job!
    private Widget formatXML(String xml) {
        PreElement pre = Document.get().createPreElement();
        pre.setInnerText(xml.replace(">", ">\n"));
        
        return HTML.wrap(pre); 
    }

    public static void display(Panel panel, JsArray<JsMetadata> array) {
        for (int i = 0; i < array.length(); i++) {
            panel.add(array.get(i).display());
        }
    }
}
