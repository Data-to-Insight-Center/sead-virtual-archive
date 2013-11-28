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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;

import org.dataconservancy.dcs.ingest.ui.client.model.CoreMetadata;

public class CoreMetadataEditor
        extends Composite {
    private final TextBox title, type, creators, subjects;

    public CoreMetadataEditor() {
        title = new TextBox();
        type = new TextBox();
        creators = new TextBox();
        subjects = new TextBox();
        
        FlexTable table =
                Util.createTable("Title:", "Type:", "Creators:", "Subjects:");
        Util.addColumn(table, title, type, creators, subjects);

        title.setText("REQUIRED");
        
        initWidget(table);
    }

    public CoreMetadata getCoreMetadata() {
        CoreMetadata core = new CoreMetadata();

        core.setTitle(title.getText().trim());
        core.setType(type.getText().trim());
        Util.addAllFromCSV(core.subjects(), subjects.getText());
        Util.addAllFromCSV(core.creators(), creators.getText());

        return core;
    }
}
