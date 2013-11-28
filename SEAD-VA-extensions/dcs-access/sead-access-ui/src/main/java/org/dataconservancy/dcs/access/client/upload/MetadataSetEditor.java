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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedStackPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import org.dataconservancy.dcs.access.client.upload.model.Metadata;

public class MetadataSetEditor
        extends Composite {

    private final StackPanel stack;

    public MetadataSetEditor() {
        this.stack = new DecoratedStackPanel();
        
        VerticalPanel panel = new VerticalPanel();

        panel.add(stack);

        final TextBox schema = new TextBox();

        Button add = new Button("Add");
        Button remove = new Button("Remove");

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(new Label("Schema:"));
        hp.add(schema);
        hp.add(add);
        hp.add(remove);
        hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
        hp.setSpacing(5);

        panel.add(hp);

        add.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                String s = schema.getText().trim();

                if (s.length() > 0) {
                    stack.add(new MetadataEditor(s), s);
                    stack.showStack(stack.getWidgetCount() - 1);
                }
            }
        });

        remove.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                int i = stack.getSelectedIndex();

                if (i != -1) {
                    stack.remove(i);
                    
                    if (stack.getWidgetCount() > 0) {
                        stack.showStack(0);
                    }
                }
            }
        });

        initWidget(panel);
    }

    public List<Metadata> getMetadataSet() {
        List<Metadata> result = new ArrayList<Metadata>();

        for (int i = 0; i < stack.getWidgetCount(); i++) {
            MetadataEditor ed = (MetadataEditor) stack.getWidget(i);

            result.add(ed.getMetadata());
        }

        return result;
    }
}
