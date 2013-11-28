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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import org.dataconservancy.dcs.access.ui.client.Util;

/**
 * Models a Data Conservancy Deliverable Unit
 */
public final class JsDcp
        extends JsModel {

    protected JsDcp() {
    }

    public static JsDcp create() {
        return (JsDcp) JsModel
                .parseJSON("{\"deliverableUnits\": [], \"collections\": [], \"manifestations\": [], \"files\": [], \"events\": []}");
    }

    @SuppressWarnings("unchecked")
    public final JsArray<JsCollection> getCollections() {
        return (JsArray<JsCollection>) getArray("collections");
    }

    @SuppressWarnings("unchecked")
    public final JsArray<JsDeliverableUnit> getDeliverableUnits() {
        return (JsArray<JsDeliverableUnit>) getArray("deliverableUnits");
    }

    @SuppressWarnings("unchecked")
    public final JsArray<JsManifestation> getManifestations() {
        return (JsArray<JsManifestation>) getArray("manifestations");
    }

    @SuppressWarnings("unchecked")
    public final JsArray<JsFile> getFiles() {
        return (JsArray<JsFile>) getArray("files");
    }

    @SuppressWarnings("unchecked")
    public final JsArray<JsEvent> getEvents() {
        return (JsArray<JsEvent>) getArray("events");
    }

    public int size() {
        int size = 0;

        size += getCollections().length();
        size += getDeliverableUnits().length();
        size += getManifestations().length();
        size += getFiles().length();
        size += getEvents().length();

        return size;
    }

    public Widget display() {
        VerticalPanel panel = new VerticalPanel();

        int size = size();

        if (getCollections().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Collections", "SectionHeader"));
            }

            
            JsCollection.display(panel, getCollections());
        }

        if (getDeliverableUnits().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Deliverable Units", "SectionHeader"));
            }

            JsDeliverableUnit.display(panel, getDeliverableUnits());
        }

        if (getManifestations().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Manifestations", "SectionHeader"));
            }

            JsManifestation.display(panel, getManifestations());
        }

        if (getFiles().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Files", "SectionHeader"));
            }

            JsFile.display(panel, getFiles());
        }

        if (getEvents().length() > 0) {
            if (size > 1) {
                panel.add(Util.label("Events", "SectionHeader"));
            }

            JsEvent.display(panel, getEvents());
        }

        return panel;
    }
}
