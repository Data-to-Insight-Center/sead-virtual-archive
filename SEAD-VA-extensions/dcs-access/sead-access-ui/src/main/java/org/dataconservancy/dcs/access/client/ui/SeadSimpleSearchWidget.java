/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.client.ui;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.SeadState;
import org.dataconservancy.dcs.access.client.Search;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Grid;

public class SeadSimpleSearchWidget extends Composite{

    FlowPanel advancedPanel;
    public SeadSimpleSearchWidget(Search.UserField[] userfields,
                                    String[] userqueries) {

        advancedPanel = new FlowPanel();
        initWidget(advancedPanel);

        final FlexTable table = new FlexTable();
        table.setWidth("100%");
        Label intro = new Label();
        intro.setText("Virtual Archive (SEAD-VA) is a discovery and preservation layer of the SEAD services suite. SEAD-VA federates over multiple institutional repositories and archives and provides a coherent view on published data in sustainability science.");
        intro.setStylePrimaryName("IntroLabel");

        Label lbl = new Label();
        lbl.setText("Search SEAD-VA data registry");
        lbl.setStylePrimaryName("SearchLabel");
        Button add = new Button("Add field");
        
        if(SeadApp.isHome){
        	advancedPanel.add(intro);
        	advancedPanel.add(lbl);
        }
        
        advancedPanel.add(table);

        // Called to search filled in query

        final ClickHandler searchlistener = new ClickHandler() {

            public void onClick(ClickEvent event) {
                // Build up search history token
                System.out.println("In search");
                String[] data = new String[(table.getRowCount() * 2) + 1+1];
                int dataindex = 0;
                boolean emptyquery = true;

                for (int i = 0; i < table.getRowCount(); i++) {
                    // ListBox lb = (ListBox) table.getWidget(i, 2);
                    TextBox tb = (TextBox) table.getWidget(i, 0);

                   /* int sel = lb.getSelectedIndex();

                    if (sel != -1) {*/
                    String userquery = tb.getText().trim();
                      /*  String userfield = Search.UserField.values()[sel]
                                .name();*/
                    String userfield = "ALL";
                    if (userquery.isEmpty()) {
                        userfield = null;
                        userquery = null;
                    } else {
                        emptyquery = false;
                    }
                    System.out.println("UserField: "+userfield);
                    System.out.println("UserAuery" + userquery);
                    data[dataindex++] = userfield;
                    data[dataindex++] = userquery;
                    System.out.println("Data.0: "+data[0] + "Data.1: "+data[1]);
                    //}
                }

                data[dataindex] = "0";
                data[dataindex+1] = "0";

                if (!emptyquery) {
                    History.newItem(SeadState.SEARCH.toToken(data));
                }
            }
        };

        ClickHandler addlistener = new ClickHandler() {

            public void onClick(ClickEvent event) {
                int row = table.getRowCount();



                final TextBox tb = new TextBox();
                tb.setStyleName("Pad");
                tb.setValue("Type name,date or keyword");
                table.setWidget(row, 0, tb);
                //   table.setWidget(row, 1, new Label("in1"));
                //   table.setWidget(row, 2 , createAdvancedSearchFieldSelector());

                tb.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        // TODO Auto-generated method stub
                        tb.setValue(null);
                    }
                });
                tb.addKeyDownHandler(new KeyDownHandler() {

                    public void onKeyDown(KeyDownEvent event) {
                        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                            searchlistener.onClick(null);
                        }
                    }
                });

            }
        };

        add.addClickHandler(addlistener);

        if (userfields != null) {
            for (int i = 0; i < userfields.length; i++) {
                if (userfields[i] == null) {
                    continue;
                }

                int row = table.getRowCount();
                addlistener.onClick(null);

                //     ListBox lb = (ListBox) table.getWidget(row,2);
                //    lb.setItemSelected(userfields[i].ordinal(), true);
                TextBox tb = (TextBox) table.getWidget(row, 0);
                tb.setText(userqueries[i]);
            }
        } else {
            addlistener.onClick(null);
        }


        Grid grid = new Grid(2, 2);
        advancedPanel.add(grid);
        grid.setWidth("80%");

        FlexTable flexTable = new FlexTable();
        grid.setWidget(0, 0, table);

        Button search = new Button("Search");
        search.setStyleName("SearchButton");
        Label advSearch = new Label();
        advSearch.setText("Advanced Search>>");
        advSearch.setStyleName("PadHyperlink");
        advSearch.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                History.newItem(SeadState.ADVANCED.toToken());
            }
        });
        grid.setWidget(0, 1, search);
        grid.setWidget(1, 0, advSearch);
        // search.setWidth("82px");
        search.addClickHandler(searchlistener);

    }

    ListBox createAdvancedSearchFieldSelector() {
        ListBox lb = new ListBox();
        lb.setStyleName("Pad");

        for (Search.UserField uf : Search.UserField.values()) {
            lb.addItem(uf.display);
        }
        lb.setSelectedIndex(8);
        return lb;
    }
}
