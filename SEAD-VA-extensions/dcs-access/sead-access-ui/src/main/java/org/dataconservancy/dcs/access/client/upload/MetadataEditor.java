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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import org.dataconservancy.dcs.access.client.upload.model.Metadata;

public class MetadataEditor
        extends Composite {

    private final TextArea content;
    private final String schema;
    
    public MetadataEditor(String schema) {
        this.content = new TextArea();
        this.schema = schema;
        
        VerticalPanel panel = new VerticalPanel();
        
        ScrollPanel scroll = new ScrollPanel(content);
        content.setStylePrimaryName("MetdataContentEditor");

        panel.add(new Label("Schema: " + schema));
        panel.add(scroll);

        initWidget(panel);
    }

    public Metadata getMetadata() {
        return new Metadata(schema, content.getText());
    }
}
