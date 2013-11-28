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

import org.dataconservancy.dcs.access.client.upload.model.Collection;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class CollectionEditor
        extends Composite {

    private final CoreMetadataEditor coremd_editor;

    private final MetadataSetEditor mdset_editor;

    private final TextBox parent;

    private final String id;

    public CollectionEditor(String id, final List<String> colids) {
        this.id = id;
        this.coremd_editor = new CoreMetadataEditor(null);
        this.parent = new TextBox();
        this.mdset_editor = new MetadataSetEditor();

        VerticalPanel panel = new VerticalPanel();

        FlexTable table = Util.createTable("Collection Id:", "Parent:");

        Widget parentchooser =
                Util.createListChooser(parent,
                                       "Choose Collection parent",
                                       colids,
                                       true);

        table.setText(0, 1, id);
        table.setWidget(1, 1, parentchooser);
        panel.add(table);

        panel.add(Util.label("Core metadata", "SubSectionHeader"));
        panel.add(coremd_editor);

        panel.add(Util.label("Additional metadata", "SubSectionHeader"));

        panel.add(mdset_editor);

        initWidget(panel);
    }

    public Collection getCollection() {
        Collection col = new Collection();

        col.setId(id);

        col.setCoreMetadata(coremd_editor.getCoreMetadata());
        col.setParent(parent.getText().trim());
        col.metadata().addAll(mdset_editor.getMetadataSet());

        return col;
    }
}
