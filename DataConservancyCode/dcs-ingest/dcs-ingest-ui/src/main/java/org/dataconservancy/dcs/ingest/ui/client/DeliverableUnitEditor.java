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
package org.dataconservancy.dcs.ingest.ui.client;

import java.util.List;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import org.dataconservancy.dcs.ingest.ui.client.model.DeliverableUnit;

public class DeliverableUnitEditor
        extends Composite {

    private final CoreMetadataEditor coremd_editor;

    private final MetadataSetEditor mdset_editor;

    private final TextBox parents;

    private final TextBox files;

    private final TextBox cols;

    private final String id;

    public DeliverableUnitEditor(String id,
                                 final List<String> fileids,
                                 final List<String> duids,
                                 final List<String> colids) {
        this.id = id;
        this.coremd_editor = new CoreMetadataEditor();
        this.parents = new TextBox();
        this.files = new TextBox();
        this.cols = new TextBox();
        this.mdset_editor = new MetadataSetEditor();

        VerticalPanel panel = new VerticalPanel();

        FlexTable table =
                Util.createTable("Deliverable Unit Id:",
                                 "Collection:",
                                 "Parent:",
                                 "Files");

        Widget filechooser =
                Util
                        .createListChooser(files,
                                           "Choose files that are part of this Deliverable Unit",
                                           fileids,
                                           true);
        Widget parentchooser =
                Util.createListChooser(parents,
                                       "Choose Deliverable Unit parent",
                                       duids,
                                       true);
        Widget colchooser =
                Util.createListChooser(cols,
                                       "Choose Deliverable Unit collection",
                                       colids,
                                       true);

        Util.addColumn(table, null, colchooser, parentchooser, filechooser);

        table.setText(0, 1, id);

        panel.add(table);

        panel.add(Util.label("Core metadata", "SubSectionHeader"));
        panel.add(coremd_editor);

        panel.add(Util.label("Additional metadata", "SubSectionHeader"));

        panel.add(mdset_editor);

        initWidget(panel);
    }

    public DeliverableUnit getDeliverableUnit() {
        DeliverableUnit du = new DeliverableUnit();

        du.setId(id);

        du.setCoreMetadata(coremd_editor.getCoreMetadata());
        du.metadata().addAll(mdset_editor.getMetadataSet());

        Util.addAllFromCSV(du.files(), files.getText());
        Util.addAllFromCSV(du.parents(), parents.getText());
        Util.addAllFromCSV(du.collections(), cols.getText());

        return du;
    }
}
