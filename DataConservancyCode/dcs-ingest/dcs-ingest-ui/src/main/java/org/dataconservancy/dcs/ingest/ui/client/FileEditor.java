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
import com.google.gwt.user.client.ui.VerticalPanel;

import org.dataconservancy.dcs.ingest.ui.client.model.File;

public class FileEditor
        extends Composite {

    private final MetadataSetEditor mdset_editor;

    private final TextBox name;

    private final TextBox source;

    private final TextBox techenv;

    private final String id;

    public FileEditor(String id, String filename, String src) {
        this.id = id;
        this.mdset_editor = new MetadataSetEditor();
        this.name = new TextBox();
        this.source = new TextBox();
        this.techenv = new TextBox();

        source.setText(src);
        name.setText(filename);
        
        VerticalPanel panel = new VerticalPanel();

        FlexTable table =
                Util.createTable("File Id:",
                                 "Name:",
                                 "Source:",
                                 "Tech. Enviroment:");

        Util.addColumn(table, null, name, source, techenv);
        table.setText(0, 1, id);

        panel.add(table);

        panel.add(Util.label("Metadata", "SubSectionHeader"));

        panel.add(mdset_editor);

        initWidget(panel);
    }

    public File getFile() {
        File file = new File();

        file.setId(id);
        
        file.setName(name.getText().trim());
        file.setSource(source.getText().trim());
        file.setTechnicalEnviroment(techenv.getText().trim());
        file.metadata().addAll(mdset_editor.getMetadataSet());

        return file;
    }
}
